package com.innerproduct.ee.concurrent

import cats.effect._
import cats.effect.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

// format: off
object Timeout extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      done <- IO.race(task, timeout) // <1>
      _ <- done match { // <2>
        case Left(_)  => IO("   task: won").debug() // <3>
        case Right(_) => IO("timeout: won").debug() // <4>
      }
    } yield ExitCode.Success

  val task: IO[Unit]    = annotatedSleep("   task", 100.millis) // <6>
  val timeout: IO[Unit] = annotatedSleep("timeout", 500.millis)

  def annotatedSleep(name: String, duration: FiniteDuration): IO[Unit] =
    (
      IO(s"$name: starting").debug() *>
      Timer[IO].sleep(duration) *> // <5>
      IO(s"$name: done").debug()
    ).onCancel(IO(s"$name: cancelled").debug().void).void
}