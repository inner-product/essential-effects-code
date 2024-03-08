package com.innerproduct.ee.control

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object CancelledClock extends IOApp.Simple {
  def run: IO[Unit] =
    together.void

  // tag::tickingClock[]
  val tickingClock: IO[Unit] =
    for {
      _ <- debugWithThread(System.currentTimeMillis)
      _ <- IO.sleep(1.second)
      _ <- tickingClock
    } yield ()
  // end::tickingClock[]

  // tag::composed[]
  val ohNoes =
    IO.sleep(2.seconds) *> IO.raiseError(new RuntimeException("oh noes!")) // <1>

  val together =
    (tickingClock, ohNoes).parTupled
  // end::composed[]
}
