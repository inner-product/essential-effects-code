package com.innerproduct.ee.concurrent

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

// format: off
object Timeout extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      // race task and timeout <1>
      // print which task finished first <2>
      _ <- IO("done").debug()
    } yield ExitCode.Success

  val task: IO[Unit]    = annotatedSleep("   task", 100.millis) // <7>
  val timeout: IO[Unit] = annotatedSleep("timeout", 500.millis)

  def annotatedSleep(name: String, duration: FiniteDuration): IO[Unit] =
    (
      IO(s"$name: starting").debug() *>
      Timer[IO].sleep(duration) *> // <5>
      IO(s"$name: done").debug()
    ).onCancel(IO(s"$name: cancelled").debug().void).void // <6>
}