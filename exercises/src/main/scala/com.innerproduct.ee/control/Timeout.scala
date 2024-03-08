package com.innerproduct.ee.control

import cats.effect._
import scala.concurrent.duration._

object Timeout extends IOApp.Simple {
  def run: IO[Unit] =
    IO.race(task, timeout) // <1>
      .debug()
      .void

  val task: IO[String] = annotatedSleep("task", 100.millis) // <3>
  val timeout: IO[String] = annotatedSleep("timeout", 500.millis)

  def annotatedSleep(name: String, duration: FiniteDuration): IO[String] =
    IO.sleep(duration) // <2>
      .as(name)
      .debug(name)
}
