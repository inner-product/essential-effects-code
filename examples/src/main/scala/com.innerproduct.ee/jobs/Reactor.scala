package com.innerproduct.ee.jobs

import cats.effect._
import cats.syntax.all._

// tag::reactor[]
trait Reactor {
  def whenAwake(
      onStart: Job.Id => IO[Unit],
      onComplete: (Job.Id, OutcomeIO[_]) => IO[Unit]
  ): IO[Unit]
}
// end::reactor[]

// tag::reactor-impl[]
object Reactor {
  def apply(stateRef: Ref[IO, JobScheduler.State]): Reactor =
    new Reactor {
      def whenAwake(
          onStart: Job.Id => IO[Unit],
          onComplete: (Job.Id, OutcomeIO[_]) => IO[Unit]
      ): IO[Unit] = {
        // tag::reactor-impl-exclude[]
        // tag::startNextJob[]
        def startNextJob: IO[Option[Job.Running]] =
          for {
            job <- stateRef.modify(_.dequeue) // <1>
            running <- job.traverse(startJob) // <2>
          } yield running
        // end::startNextJob[]

        // tag::startJob[]
        def startJob(scheduled: Job.Scheduled): IO[Job.Running] =
          for {
            running <- scheduled.start // <1>
            _ <- stateRef.update(_.running(running)) // <2>
            _ <- registerOnComplete(running) // <3>
            _ <- onStart(running.id).attempt // <4>
          } yield running
        // end::startJob[]

        // tag::registerOnComplete[]
        def registerOnComplete(job: Job.Running) =
          job.await
            .flatMap(jobCompleted)
            .start

        def jobCompleted(job: Job.Completed): IO[Unit] =
          stateRef
            .update(_.onComplete(job))
            .flatTap(_ => onComplete(job.id, job.exitCase).attempt)
        // end::registerOnComplete[]
            
        // end::reactor-impl-exclude[]
        startNextJob // <1>
          .iterateUntil(_.isEmpty) // <2>
          .void
      }
    }
}
// end::reactor-impl[]
