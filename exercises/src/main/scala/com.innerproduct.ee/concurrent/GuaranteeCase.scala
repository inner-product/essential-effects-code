package com.innerproduct.ee.concurrent

import cats.effect._
import com.innerproduct.ee.debug._

object GuaranteeCase extends IOApp {

  def run(args: List[String]): IO[ExitCode] = // <1>
    (runGoal(Goal.Complete) *>
      runGoal(Goal.Error) *>
      runGoal(Goal.Cancel)).as(ExitCode.Success)

  def runGoal(goal: Goal): IO[Unit] =
    for {
      fiber <- task(goal).start // <2>
      _ <- goal match {
        case Goal.Complete => fiber.join // <3>
        case Goal.Error    => fiber.join.attempt // <3>
        case Goal.Cancel   => fiber.cancel // <4>
      }
    } yield ()

  def task[A](goal: Goal): IO[Int] =
    (goal match {
      case Goal.Complete => IO.pure(0)
      case Goal.Error    => IO.raiseError(new RuntimeException("boom!"))
      case Goal.Cancel   => IO.never // <4>
    }).guaranteeCase { // <5>
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
