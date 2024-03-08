package com.innerproduct.ee.coordination

import cats.effect._
import cats.syntax.all._

trait CountdownLatch {
  def await: IO[Unit]
  def decrement: IO[Unit]
}

object CountdownLatch {
  def apply(n: Long): IO[CountdownLatch] =
    for {
      whenDone <- Deferred[IO, Unit]
      state <- Ref[IO].of[State](Outstanding(n, whenDone))
    } yield new CountdownLatch {
      def await: IO[Unit] =
        state.get.flatMap {
          case Outstanding(_, whenDone) => whenDone.get
          case Done()                   => IO.unit
        }

      def decrement: IO[Unit] =
        state.modify {
          case Outstanding(1, whenDone) => Done() -> whenDone.complete(()).void
          case Outstanding(n, whenDone) =>
            Outstanding(n - 1, whenDone) -> IO.unit
          case Done() => Done() -> IO.unit
        }.flatten
    }

  sealed trait State
  case class Outstanding(n: Long, whenDone: Deferred[IO, Unit]) extends State
  case class Done() extends State
}

object LatchExample extends IOApp.Simple {
  import com.innerproduct.ee.debug._

  def run: IO[Unit] =
    for {
      latch <- CountdownLatch(1)
      _ <- (actionWithPrerequisites(latch), runPrerequisite(latch)).parTupled
    } yield ()

  def runPrerequisite(latch: CountdownLatch) =
    for {
      result <- debugWithThread("prerequisite")
      _ <- latch.decrement // <1>
    } yield result

  def actionWithPrerequisites(latch: CountdownLatch) =
    for {
      _ <- debugWithThread("waiting for prerequisites")
      _ <- latch.await // <1>
      result <- debugWithThread("action") // <2>
    } yield result
}
