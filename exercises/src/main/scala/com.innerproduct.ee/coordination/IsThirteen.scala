package com.innerproduct.ee.coordination

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object IsThirteen extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      ticks <- Ref[IO].of(0L)
      is13 <- Deferred[IO, Unit] // <1>
      _ <- (beepWhen13(is13), tickingClock(ticks, is13)).parTupled // <2>
    } yield ExitCode.Success

  def beepWhen13(is13: Deferred[IO, Unit]) =
    for {
      _ <- is13.get // <3>
      _ <- IO("BEEP!").debug()
    } yield ()

  def tickingClock(ticks: Ref[IO, Long], is13: Deferred[IO, Unit]): IO[Unit] =
    for {
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis).debug()
      count <- ticks.updateAndGet(_ + 1)
      _ <- if (count >= 13) is13.complete(()) else IO.unit // <4>
      _ <- tickingClock(ticks, is13)
    } yield ()
}
