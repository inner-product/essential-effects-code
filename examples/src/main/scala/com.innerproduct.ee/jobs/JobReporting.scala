package com.innerproduct.ee.jobs

import cats._
import cats.effect._
import cats.syntax.all._

trait JobReporting {
  def status: IO[JobReporting.JobStatus]
  def status(id: Job.Id): IO[Option[Status]]

  def register(eventHandler: JobReporting.EventHandler): IO[Unit]
}

object JobReporting {
  def apply(
      schedulerState: Ref[IO, JobScheduler.State],
      reportingState: Ref[IO, JobReporting.State]
  ): JobReporting =
    new JobReporting {
      def status: IO[JobStatus] =
        schedulerState.get.map(state => JobStatus(state.status))

      def status(id: Job.Id): IO[Option[Status]] =
        schedulerState.get.map(_.status(id))

      def register(eventHandler: EventHandler): IO[Unit] =
        reportingState.update(_.register(eventHandler))
    }

  /** "Newtype" holding job status in order to delegate pretty-printing to a `Show` instance. */
  case class JobStatus(jobs: Map[Job.Id, Status])

  object JobStatus {

    /** Pretty-print job status as a table, grouped by status, that looks like:
      *
      * {{{
      * Error: 291405c0-65b1-47a6-9917-751b1b8e6b0d
      * Success: e8e73a83-ebcc-4245-adba-329959f4abc5
      * Success: 4cf2a19f-0078-4a8f-8e52-68081cd2f54c
      * Success: 5f330c05-fe13-4f54-8181-428024bd623d
      * }}}
      */
    implicit val show: Show[JobStatus] =
      _.jobs.toList
        .groupBy(_._2)
        .toList
        .flatMap {
          case (status, idStatuses) =>
            idStatuses.map(_._1).sortBy(_.value).map(id => show"$status: $id")
        }
        .mkString("\n", "\n", "")
  }

  // tag::event-handler[]
  trait EventHandler {
    def scheduled(id: Job.Id): IO[Unit]
    def running(id: Job.Id): IO[Unit]
    def completed(id: Job.Id, exitCase: OutcomeIO[_]): IO[Unit]
  }
  // end::event-handler[]

  case class State(
      eventHandlers: List[JobReporting.EventHandler] = List.empty
  ) {
    def register(eventHandler: JobReporting.EventHandler): State =
      copy(eventHandlers = eventHandler :: eventHandlers)
  }

  trait Notifier {
    def notify(f: JobReporting.EventHandler => IO[Unit]): IO[Unit]
  }

  /** Create a handler that asynchronously notifies zero or more other handlers of events. */
  object Notifier {
    def async(state: Ref[IO, State]): Notifier =
      f =>
        state.get
          .flatMap(_.eventHandlers.parTraverse(f))
          .start
          .void
  }
}
