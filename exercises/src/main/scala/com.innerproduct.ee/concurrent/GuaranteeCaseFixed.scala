package com.innerproduct.ee.concurrent

import cats.effect._
import cats.effect.concurrent.Deferred
import com.innerproduct.ee.debug._

object GuaranteeCaseFixed extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (runGoal(Goal.Complete) *>
      runGoal(Goal.Error) *>
      runGoal(Goal.Cancel)).as(ExitCode.Success)

  def runGoal(goal: Goal): IO[Unit] =
    for {
      started <- Deferred[IO, Unit] // <1>
      fiber <- task(goal, started).start // <2>
      _ <- started.get // <3>
      _ <- goal match {
        case Goal.Complete => fiber.join
        case Goal.Error    => fiber.join.attempt
        case Goal.Cancel   => fiber.cancel
      }
    } yield ()

  def task[A](
      goal: Goal,
      started: Deferred[IO, Unit]
  ): IO[Int] =
    (started.complete(()) *> (goal match { // <2>
      case Goal.Complete => IO.pure(0)
      case Goal.Error    => IO.raiseError(new RuntimeException("boom!"))
      case Goal.Cancel   => IO.never
    })).guaranteeCase {
      case ExitCase.Completed => IO("completed").debug().void
      case ExitCase.Error(t)  => IO(s"error: $t").debug().void
      case ExitCase.Canceled  => IO("canceled").debug().void
    }

  sealed trait Goal

  object Goal {
    case object Complete extends Goal
    case object Error extends Goal
    case object Cancel extends Goal
  }
}
