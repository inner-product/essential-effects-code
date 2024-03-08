package com.innerproduct.ee.io

import cats.effect._

object TickingClock extends IOApp.Simple {
  def run: IO[Unit] =
    tickingClock

  val tickingClock: IO[Unit] = ??? // <1>
}
