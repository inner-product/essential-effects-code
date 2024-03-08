package com.innerproduct.ee.jobs

import cats.effect._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

/** Specify effect retry policies as an algebra, run them with an interpreter.
  *
  * Totally stolen from https://cb372.github.io/cats-retry.
  *
  * The algebra is the interface. It consists of:
  *
  * 1. introduction forms: creates an elements of our "algebra" from some other data;
  * 2. combinators: combines elements of our "algebra"; and
  * 3. elimination forms: turns elements of our algebra into some other type (usually by running the program specified in the algebra).
  *
  * "Reification" means to make something abstract concrete. Concretely in our case it means to turn method calls into data.
  *
  * Steps to implementing a reified interpreter:
  *
  * 1. Write out the type definitions or interface of the algebra.
  * 2. Reify all the introduction and composition forms, usually as an algebraic data type.
  * 3. Implement the interpreter in the elimination forms, usually as a structural recursion over the ADT.
  */
sealed trait RetryPolicy {
  import RetryPolicy._ // bring ADT types into scope

  /* *Combinators* */

  /** set an upper bound on the delay between retries */
  def capDelay(max: FiniteDuration): RetryPolicy =
    CapDelay(this, max)

  /** if the current policy fails, try another */
  def followedBy(that: RetryPolicy): RetryPolicy =
    FollowedBy(this, that)

  /** give up when the delay between retries reaches a certain limit */
  def limitRetriesByDelay(max: FiniteDuration): RetryPolicy =
    LimitRetriesByDelay(this, max)

  /** give up when the total delay reaches a certain limit */
  def limitRetriesByCumulativeDelay(max: FiniteDuration): RetryPolicy =
    LimitRetriesByCumulativeDelay(this, max)

  /* *Interpreters* */

  /** Apply the retry policy to an effect, returning a new effect.
    *
    * Retry policies should be applied when the effect fails, i.e.,
    * via `handleErrorWith` or some variant.
    *
    * Important implementation considerations:
    * - Some policies require cumulative information not available in the ADT itself, like
    *   the total amount of delay so far. How can this information be carried along inside
    *   the interpreter?
    *
    * Optional functionality:
    * - You may want to log about the current failure and retry strategy via the `debug` extension method.
    * - Alternatively, the caller could provide a callback that is given information about the current
    *   failure and retry strategy, returning an `IO[Unit]`.
    *
    * @param io is the effect to apply the retry policy to.
    * @param onRetry an effect that is executed when a retry happens; by default it logs the error that triggered the retry
    */
  def retry[A](
      io: IO[A],
      onRetry: (RetryPolicy, Throwable) => IO[Unit] = { case (policy, t) =>
        debugWithThread(retryDetails(t)).void
      }
  ): IO[A] =
    this match {
      case ConstantDelay(duration) =>
        io.handleErrorWith(t =>
          onRetry(this, t) *> IO.sleep(duration) *> retry(io)
        )

      case LimitRetries(atMost) =>
        if (atMost > 0)
          io.handleErrorWith { t =>
            onRetry(this, t) *> LimitRetries(atMost - 1).retry(io)
          }
        else io

      case ExponentialBackoff(initialDelay) =>
        io.handleErrorWith { t =>
          onRetry(this, t) *>
            IO.sleep(initialDelay) *>
            ExponentialBackoff(initialDelay * 2).retry(io)
        }

      case FibonacciBackoff(first, second) =>
        io.handleErrorWith { t =>
          onRetry(this, t) *>
            IO.sleep(first) *> FibonacciBackoff(second, first + second).retry(
              io
            )
        }

      case FullJitter(initialDelay) => ???

      case CapDelay(policy, max) =>
        policy.retry(io).timeout(max).onError(t => onRetry(this, t).void)

      case FollowedBy(first, second) =>
        first
          .retry(io)
          .handleErrorWith(t => onRetry(this, t) *> second.retry(io))

      case LimitRetriesByDelay(policy, max) => ???

      case LimitRetriesByCumulativeDelay(policy, max) => ???
    }

  // Carry cumulative state via inner recursive call.
  def retry2[A](
      io: IO[A],
      onRetry: (RetryPolicy, Throwable) => IO[Unit] = { case (policy, t) =>
        debugWithThread(policy.retryDetails(t)).void
      }
  ): IO[A] = {
    def go(policy: RetryPolicy, state: Ref[IO, RetryState]): IO[A] =
      state.get.map(rs => s"--> $policy, $rs").debug() *>
        (policy match {
          case ConstantDelay(duration) =>
            io.handleErrorWith(t =>
              onRetry(policy, t) *>
                IO.sleep(duration) *>
                state.update(_.update(duration)) *>
                go(policy, state)
            )

          case LimitRetries(atMost) =>
            if (atMost > 0)
              io.handleErrorWith { t =>
                onRetry(policy, t) *> go(LimitRetries(atMost - 1), state)
              }
            else io

          case ExponentialBackoff(initialDelay) =>
            io.handleErrorWith { t =>
              onRetry(policy, t) *>
                IO.sleep(initialDelay) *>
                state.update(_.update(initialDelay)) *>
                go(ExponentialBackoff(initialDelay * 2), state)
            }

          case FibonacciBackoff(first, second) =>
            io.handleErrorWith { t =>
              onRetry(policy, t) *>
                IO.sleep(first) *>
                state.update(_.update(first)) *>
                go(FibonacciBackoff(second, first + second), state)
            }

          case FullJitter(initialDelay) => ???

          case CapDelay(policy, max) =>
            go(policy, state)
              .timeout(max)
              .onError(t => onRetry(policy, t).void)

          case FollowedBy(first, second) =>
            go(first, state)
              .handleErrorWith(t => onRetry(policy, t) *> go(second, state))

          case LimitRetriesByDelay(policy, max) =>
            state
              .update(_.pushDelayLimit(max))
              .bracket(_ => go(policy, state))(_ =>
                state.update(_.popDelayLimit)
              )

          case LimitRetriesByCumulativeDelay(policy, max) =>
            state
              .update(_.pushCumulativeDelayLimit(max))
              .bracket(_ => go(policy, state))(_ =>
                state.update(_.popCumulativeDelayLimit)
              )
        })

    Ref[IO].of(RetryState(Duration.Zero, Duration.Zero, Nil, Nil))
      .flatMap(state => go(this, state))
  }

  private case class RetryState(
      currentDelay: FiniteDuration,
      cumulativeDelay: FiniteDuration,
      delayLimits: List[FiniteDuration],
      cumulativeDelayLimits: List[FiniteDuration]
  ) {
    def update(delay: FiniteDuration): RetryState = {
      // this is a cheesy way to check the invariants
      require(
        delayLimits.headOption.fold(true)(_ > delay),
        s"delay limit $delay >= ${delayLimits.head} exceeded"
      )
      require(
        cumulativeDelayLimits.headOption.fold(true)(_ > (cumulativeDelay + delay)),
        s"cumulative delay limit ${cumulativeDelay + delay} >= ${cumulativeDelayLimits.head} exceeded"
      )
      copy(delay, cumulativeDelay + delay)
    }

    def pushDelayLimit(delay: FiniteDuration): RetryState =
      copy(delayLimits = delay :: delayLimits)

    def popDelayLimit: RetryState =
      copy(delayLimits = delayLimits.tail)

    def pushCumulativeDelayLimit(delay: FiniteDuration): RetryState =
      copy(cumulativeDelayLimits = delay :: delayLimits)

    def popCumulativeDelayLimit: RetryState =
      copy(cumulativeDelayLimits = cumulativeDelayLimits.tail)
  }

  def retryDetails(cause: Throwable): String =
    this match {
      case CapDelay(_, max)   => s"$cause: retry period of $max expired"
      case ConstantDelay(duration) => s"$cause: retrying in $duration"
      case ExponentialBackoff(initialDelay) =>
        s"$cause: retrying in $initialDelay"
      case FibonacciBackoff(first, _) => s"$cause: retrying in $first"
      case FollowedBy(first, second) =>
        s"$cause: policy $first failed, retrying with policy $second"
      case FullJitter(initialDelay) => ???
      case LimitRetries(atMost) =>
        s"$cause: retrying at most ${atMost - 1} times"
      case LimitRetriesByCumulativeDelay(policy, max) => ???
      case LimitRetriesByDelay(policy, max)           => ???
    }
}

object RetryPolicy {
  /* *Introduction forms*
   *
   * The earlier ones are simpler to implement, start there first.
   */

  /** retry forever, with a fixed delay between retries */
  def constantDelay(duration: FiniteDuration): RetryPolicy =
    ConstantDelay(duration)

  /** retry up to N times, with no delay between retries */
  def limitRetries(atMost: Int): RetryPolicy =
    LimitRetries(atMost)

  /** double the delay after each retry */
  def exponentialBackoff(initialDelay: FiniteDuration): RetryPolicy =
    ExponentialBackoff(initialDelay)

  /** delay(n) = (delay(n - 2) + delay(n - 1) */
  def fibonacciBackoff(initialDelay: FiniteDuration): RetryPolicy =
    FibonacciBackoff(Duration.Zero, initialDelay)

  /** randomised exponential backoff */
  def fullJitter(initialDelay: FiniteDuration): RetryPolicy =
    FullJitter(initialDelay)

  /* *Reified algebra*: cases of the ADT. */
  case class ConstantDelay(duration: FiniteDuration) extends RetryPolicy
  case class LimitRetries(atMost: Int) extends RetryPolicy
  case class ExponentialBackoff(initialDelay: FiniteDuration)
      extends RetryPolicy
  case class FibonacciBackoff(first: FiniteDuration, second: FiniteDuration)
      extends RetryPolicy
  case class FullJitter(initialDelay: FiniteDuration) extends RetryPolicy
  case class CapDelay(policy: RetryPolicy, max: FiniteDuration)
      extends RetryPolicy
  case class FollowedBy(first: RetryPolicy, second: RetryPolicy)
      extends RetryPolicy
  case class LimitRetriesByDelay(policy: RetryPolicy, max: FiniteDuration)
      extends RetryPolicy
  case class LimitRetriesByCumulativeDelay(
      policy: RetryPolicy,
      max: FiniteDuration
  ) extends RetryPolicy
}

/** An example app used to test various retry policies. */
object RetryApp extends IOApp.Simple {
  def run: IO[Unit] =
    policy
      .retry2(ohNoes.debug())

  val retryFourTimes = RetryPolicy.limitRetries(4)
  val atMostTwoSeconds =
    RetryPolicy
      .exponentialBackoff(100.millis)
      .limitRetriesByDelay(400.millis) //.capDelay(2.seconds)
  val policy = atMostTwoSeconds.followedBy(retryFourTimes)

  // An always-failing effect to test retry policies.
  val ohNoes: IO[Unit] = IO.raiseError(new Exception("Oh noes!!!"))
}
