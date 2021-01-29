package com.innerproduct.advanced.interpreters

import cats.effect._
import cats.syntax.all._
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
  // import RetryPolicy._ // bring ADT types into scope

  /* *Combinators* */

  /** set an upper bound on the delay between retries */
  def capDelay(max: FiniteDuration): RetryPolicy =
    ???

  /** if the current policy fails, try another */
  def followedBy(that: RetryPolicy): RetryPolicy =
    ???

  /** give up when the delay between retries reaches a certain limit */
  def limitRetriesByDelay(max: FiniteDuration): RetryPolicy =
    ???

  /** give up when the total delay reaches a certain limit */
  def limitRetriesByCumulativeDelay(max: FiniteDuration): RetryPolicy =
    ???

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
    * @param cs is a `ContextShift[IO]` you might need in order to run effects in parallel.
    * @param timer is a `Timer[IO]` you might need in order to create a "sleep" effect.
    */
  def retry[A](
      io: IO[A]
  )(implicit cs: ContextShift[IO], timer: Timer[IO]): IO[A] =
    ???
}

object RetryPolicy {
  /* *Introduction forms*
   *
   * The earlier ones are simpler to implement, start there first.
   */

  /** retry forever, with a fixed delay between retries */
  def constantDelay(duration: FiniteDuration): RetryPolicy =
    ???

  /** retry up to N times, with no delay between retries */
  def limitRetries(atMost: Int): RetryPolicy =
    ???

  /** double the delay after each retry */
  def exponentialBackoff(initialDelay: FiniteDuration): RetryPolicy =
    ???

  /** delay(n) = (delay(n - 2) + delay(n - 1) */
  def fibonacciBackoff(initialDelay: FiniteDuration): RetryPolicy =
    ???

  /** randomised exponential backoff */
  def fullJitter(initialDelay: FiniteDuration): RetryPolicy =
    ???

  /* *Reified algebra*: cases of the ADT. */
}

/** An example app used to test various retry policies. */
object RetryApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    policy
      .retry(ohNoes.debug)
      .as(ExitCode.Success)

  val retryFourTimes = RetryPolicy.limitRetries(4)
  val atMostTwoSeconds =
    RetryPolicy.constantDelay(400.millis).capDelay(2.seconds)
  val policy = atMostTwoSeconds.followedBy(retryFourTimes)

  // An always-failing effect to test retry policies.
  val ohNoes = IO.raiseError(new Exception("Oh noes!!!"))
}
