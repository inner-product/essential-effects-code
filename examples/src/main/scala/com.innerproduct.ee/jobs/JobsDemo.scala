package com.innerproduct.ee.jobs

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._
import scala.util.Random

object JobsDemo extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      resources <- JobScheduler.resources(maxRunning = 2)
      _ <- resources.use {
        case (scheduler, reporting) =>
          runAllTasks(scheduler, reporting)
      }
    } yield ()

  def runAllTasks(scheduler: JobScheduler, reporting: JobReporting): IO[Unit] =
    for {
      _ <- reporting.register(eventLogger)
      _ <- DemoState
        .awaitAll(
          reporting,
          whenAwaitingCompletion
        )
        .use { _ =>
          for {
            _ <- tasks.parTraverse(scheduler.schedule)
            _ <- reporting.status.map(_.show).debug()
          } yield ()
        }
      _ <- whenDemoComplete(reporting)
    } yield ()

  val tasks =
    List(
      randomSleep *> debugWithThread("uno"),
      randomSleep *> debugWithThread("dos"),
      randomSleep *> IO.raiseError(new RuntimeException("oh noes!")),
      randomSleep *> debugWithThread("tres")
    )

  lazy val randomSleep: IO[Unit] =
    IO(Random.between(1500, 2500)).flatMap(ms => IO.sleep(ms.millis))

  val eventLogger = new JobReporting.EventHandler {
    def scheduled(id: Job.Id): IO[Unit] =
      debugWithThread(show"LOG: -> Scheduled: $id").void

    def running(id: Job.Id): IO[Unit] =
      debugWithThread(show"LOG: Scheduled -> Running: $id").void

    def completed(id: Job.Id, exitCase: OutcomeIO[_]): IO[Unit] =
      exitCase match {
        case Outcome.Canceled() =>
          debugWithThread(show"LOG: Running -> Canceled: $id").void
        case Outcome.Succeeded(_) =>
          debugWithThread(show"LOG: Running -> Complete: $id").void
        case Outcome.Errored(t) =>
          debugWithThread(show"LOG: Running -> Error: $id, ${t.toString}").void
      }
  }

  def whenAwaitingCompletion(demoState: DemoState): IO[Unit] =
    for {
      _ <- debugWithThread(
        show"DEM: ... waiting for ${demoState.scheduled.size} job(s) to start: ${demoState.scheduled.keySet}"
      ).whenA(demoState.scheduled.size > 0)
      _ <- debugWithThread(
        show"DEM: ... waiting for ${demoState.running.size} job(s) to finish: ${demoState.running.keySet}"
      ).whenA(demoState.running.size > 0)
    } yield ()

  def whenDemoComplete(reporting: JobReporting): IO[Unit] =
    reporting.status
      .flatMap(status => debugWithThread(show"DEM: all job(s) complete: $status"))
      .void

  case class DemoState(
      jobs: Map[Job.Id, (Status, Deferred[IO, Unit])] = Map.empty
  ) {
    def scheduled = jobs.filter {
      case (_, (status, _)) => status == Status.Scheduled
    }

    def running = jobs.filter {
      case (_, (status, _)) => status == Status.Running
    }

    def transition(
        id: Job.Id,
        to: Status,
        completionIfEmpty: Deferred[IO, Unit]
    ): DemoState =
      jobs.get(id) match {
        case None =>
          copy(jobs = jobs + (id -> (to -> completionIfEmpty)))
        case Some(status -> completion) =>
          // Use `max` to ensure "late" events are ignored.
          copy(jobs = jobs + (id -> ((status max to) -> completion)))
      }

    def awaitAll: IO[Unit] =
      jobs.values.toList.traverse_(_._2.get)
  }

  object DemoState {
    def awaitAll(
        reporting: JobReporting,
        whenAwaitingCompletion: DemoState => IO[Unit]
    ): Resource[IO, Unit] =
      Resource
        .make(
          Ref[IO]
            .of(DemoState())
            .flatTap(stateRef => reporting.register(track(stateRef)))
        ) { stateRef =>
          1.iterateWhileM[IO] { _ =>
              for {
                s <- stateRef.get.debug()
                _ <- whenAwaitingCompletion(s)
                _ <- s.awaitAll.timeoutTo(1.second, IO.unit)
              } yield s.scheduled.size + s.running.size
            }(_ > 0)
            .void
        }
        .void

    def track(stateRef: Ref[IO, DemoState]): JobReporting.EventHandler =
      new JobReporting.EventHandler {
        def scheduled(id: Job.Id): IO[Unit] =
          for {
            completion <- Deferred[IO, Unit]
            _ <- stateRef.update(_.transition(id, Status.Scheduled, completion))
          } yield ()

        def running(id: Job.Id): IO[Unit] =
          for {
            completion <- Deferred[IO, Unit]
            _ <- stateRef.update(_.transition(id, Status.Running, completion))
          } yield ()

        def completed(id: Job.Id, exitCase: OutcomeIO[_]): IO[Unit] =
          for {
            completion <- Deferred[IO, Unit]
            _ <- stateRef.update(_.transition(id, Status(exitCase), completion))
            _ <- stateRef.get.flatMap(_.jobs(id)._2.complete(()))
          } yield ()
      }
  }
}
