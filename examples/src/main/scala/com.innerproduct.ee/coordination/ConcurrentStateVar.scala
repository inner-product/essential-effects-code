package com.innerproduct.ee.coordination

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object ConcurrentStateVar extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      _ <- (tickingClock, printTicks).parTupled // <1>
    } yield ()

  var ticks: Long = 0L // <2>

  val tickingClock: IO[Unit] =
    for {
      _ <- IO.sleep(1.second)
      _ <- debugWithThread(System.currentTimeMillis)
      _ = (ticks = ticks + 1) // <3>
      _ <- tickingClock
    } yield ()

  val printTicks: IO[Unit] =
    for {
      _ <- IO.sleep(5.seconds)
      _ <- debugWithThread(s"TICKS: $ticks") // <4>
      _ <- printTicks
    } yield ()
}
