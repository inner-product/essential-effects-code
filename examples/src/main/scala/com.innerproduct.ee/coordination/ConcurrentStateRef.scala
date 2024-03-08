package com.innerproduct.ee.coordination

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object ConcurrentStateRef extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      ticks <- Ref[IO].of(0L) // <1>
      _ <- (tickingClock(ticks), printTicks(ticks)).parTupled // <2>
    } yield ()

  def tickingClock(ticks: Ref[IO, Long]): IO[Unit] =
    for {
      _ <- IO.sleep(1.second)
      _ <- debugWithThread(System.currentTimeMillis)
      _ <- ticks.update(_ + 1) // <3>
      _ <- tickingClock(ticks)
    } yield ()

  def printTicks(ticks: Ref[IO, Long]): IO[Unit] =
    for {
      _ <- IO.sleep(5.seconds)
      n <- ticks.get // <4>
      _ <- debugWithThread(s"TICKS: $n")
      _ <- printTicks(ticks)
    } yield ()
}