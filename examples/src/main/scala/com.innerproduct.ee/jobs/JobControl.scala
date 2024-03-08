package com.innerproduct.ee.jobs

import cats.effect._

trait JobControl {
  def cancel(id: Job.Id): IO[Unit]
}

object JobControl {
  def apply(stateRef: Ref[IO, JobScheduler.State]): JobControl =
    new JobControl {
      def cancel(id: Job.Id): IO[Unit] = {
        def next(state: JobScheduler.State): (JobScheduler.State, IO[Unit]) =
          state
            .cancel(id)
            .map {
              case (s, _: Job.Scheduled) => s -> IO.unit
              // TODO: move canceled Running job to Completed
              case (s, Job.Running(_, fiber, _)) => s -> fiber.cancel
              case (s, _: Job.Completed)         => s -> IO.unit
            }
            .getOrElse(state -> IO.unit)

        stateRef.modify(next).flatten
      }
    }
}
