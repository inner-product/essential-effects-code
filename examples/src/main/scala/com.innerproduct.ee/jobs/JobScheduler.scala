package com.innerproduct.ee.jobs

import cats.data._
import cats.effect._
import cats.syntax.all._

// tag::scheduler[]
trait JobScheduler {
  def schedule(task: IO[_]): IO[Job.Id] // <1>
}
// end::scheduler[]

object JobScheduler {
  def resources(maxRunning: Int): IO[Resource[IO, (JobScheduler, JobReporting)]] =
    for {
      schedulerState <- Ref[IO].of(JobScheduler.State(maxRunning))
      reportingState <- Ref[IO].of(JobReporting.State())
      notifier = JobReporting.Notifier.async(reportingState)
      zzz <- Zzz.asleep
      scheduler = new JobScheduler {
        def schedule(task: IO[_]): IO[Job.Id] =
          for {
            job <- Job.create(task) // <1>
            _ <- schedulerState.update(_.enqueue(job)) // <2>
            _ <- zzz.wakeUp *> notifier.notify(_.scheduled(job.id))
          } yield job.id
      }
      reactor = Reactor(schedulerState)
      onStart = (id: Job.Id) => notifier.notify(_.running(id))
      onComplete = (id: Job.Id, exitCase: OutcomeIO[_]) =>
        zzz.wakeUp *> notifier.notify(_.completed(id, exitCase))
      loop =
      // format: off
      // tag::loop[]
        (zzz.sleep *> reactor.whenAwake(onStart, onComplete)) // <1>
          .foreverM // <2>
      // end::loop[]
      // format: on
      reporting = JobReporting(schedulerState, reportingState)
    } yield loop.background.as(scheduler -> reporting)

  // tag::state[]
  case class State(
      maxRunning: Int, // <1>
      scheduled: Chain[Job.Scheduled] = Chain.empty, // <2>
      running: Map[Job.Id, Job.Running] = Map.empty, // <3>
      completed: Chain[Job.Completed] = Chain.empty // <4>
  )
  // end::state[]
  {
    // tag::enqueue[]
    def enqueue(job: Job.Scheduled): State =
      copy(scheduled = scheduled :+ job)
    // end::enqueue[]

    /** If there are at most `maxRunning` running jobs,
      * or no scheduled jobs, do nothing. Otherwise,
      * dequeue the first scheduled job.
      */
    // tag::dequeue[]
    def dequeue: (State, Option[Job.Scheduled]) =
      if (running.size >= maxRunning) this -> None // <1>
      else
        scheduled.uncons
          .map {
            case (head, tail) =>
              copy(scheduled = tail) -> Some(head) // <2>
          }
          .getOrElse(this -> None) // <3>
    // end::dequeue[]

    // tag::running[]
    def running(job: Job.Running): State =
      copy(running = running + (job.id -> job))
    // end::running[]

    // tag::onComplete[]
    def onComplete(job: Job.Completed): State =
      copy(running = running - job.id, completed = completed :+ job)
    // end::onComplete[]

    def status: Map[Job.Id, Status] =
      (scheduled.map(job => job.id -> job.status).toList ++
        running.values.map(job => job.id -> job.status).toList ++
        completed.map(job => job.id -> job.status).toList).toMap

    def status(id: Job.Id): Option[Status] =
      scheduled
        .find(_.id == id)
        .orElse(running.get(id))
        .orElse(completed.find(_.id == id))
        .map(_.status)

    def cancel(id: Job.Id): Option[(State, Job)] =
      scheduled
        .deleteFirst(_.id == id)
        .map { case (job, newQueue) => copy(scheduled = newQueue) -> job }
        .orElse(
          running.get(id).map(job => copy(running = (running - id)) -> job)
        )
  }
}
