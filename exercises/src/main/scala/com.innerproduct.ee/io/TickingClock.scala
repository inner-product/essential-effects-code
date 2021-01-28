package com.innerproduct.ee.io

import cats.effect._

object TickingClock extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    tickingClock.as(ExitCode.Success)

  val tickingClock: IO[Unit] = ??? // <1>
}
