package com.innerproduct.ee.control

import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object CancelledClockOnCancel extends IOApp.Simple {
  def run: IO[Unit] =
    together.void

  // tag::composed[]
  val together =
    (
      tickingClock.onCancel(debugWithThread("cancelled!").void), // <1>
      IO.sleep(2.seconds) *> IO.raiseError(new RuntimeException("oh noes!"))
    ).parTupled
  // end::composed[]

  lazy val tickingClock: IO[Unit] =
    for {
      _ <- debugWithThread(System.currentTimeMillis)
      _ <- IO.sleep(1.second)
      _ <- tickingClock
    } yield ()
}
