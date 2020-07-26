package com.innerproduct.ee.apps

import cats.effect._
import scala.annotation.nowarn

object TickingClock extends IOApp {
  @nowarn
  def run(args: List[String]): IO[ExitCode] =
    tickingClock
      .guaranteeCase(???) // <2>
      .as(ExitCode.Success)

  @nowarn
  val tickingClock: IO[Unit] = ??? // <1>
}
