package com.innerproduct.ee.jobs

import cats._
import cats.effect._
import com.innerproduct.ee.Colorize
import java.util.UUID

sealed trait Job {
  def id: Job.Id

  def status: Status =
    this match {
      case _: Job.Scheduled                     => Status.Scheduled
      case _: Job.Running                       => Status.Running
      case Job.Completed(_, Outcome.Canceled())  => Status.Canceled
      case Job.Completed(_, Outcome.Succeeded(_)) => Status.Success
      case Job.Completed(_, Outcome.Errored(_))  => Status.Error
    }
}

object Job {
  // tag::scheduled[]
  case class Scheduled(id: Id, task: IO[_]) extends Job {
    def start: IO[Job.Running] =
      for {
        exitCase <- Deferred[IO, OutcomeIO[_]] // <1>
        fiber <- task.void
          .guaranteeCase(exitCase.complete(_).void) // <2>
          .start // <3>
      } yield Job.Running(id, fiber, exitCase) // <4>
  }
  // end::scheduled[]

  // tag::running[]
  case class Running(
      id: Id,
      fiber: FiberIO[Unit],
      exitCase: Deferred[IO, OutcomeIO[_]]
  ) extends Job {
    val await: IO[Completed] =
      exitCase.get.map(Completed(id, _)) // <1>
  }
  // end::running[]

  case class Completed(id: Id, exitCase: OutcomeIO[_]) extends Job

  case class Id(value: UUID) extends AnyVal

  object Id {
    implicit val show: Show[Id] =
      _.value.toString
        .split("-")
        .zipWithIndex
        .map { case (s, i) => if (i == 0) Colorize(s) else s }
        .mkString("-")
  }

  // tag::create[]
  def create[A](task: IO[A]): IO[Scheduled] =
    IO(Id(UUID.randomUUID())).map(Scheduled(_, task))
  // end::create[]
}

sealed trait Status

object Status {
  case object Scheduled extends Status
  case object Running extends Status
  case object Success extends Status
  case object Error extends Status
  case object Canceled extends Status

  def apply(exitCase: OutcomeIO[_]): Status =
    exitCase match {
      case Outcome.Succeeded(_) => Success
      case Outcome.Canceled() => Canceled
      case Outcome.Errored(_) => Error
    }

  implicit val show: Show[Status] = Show.fromToString
  implicit val order: Order[Status] =
    Order.by {
      case Scheduled => 0
      case Running => 1
      case Success => 2
      case Error => 2
      case Canceled => 2
    }
}
