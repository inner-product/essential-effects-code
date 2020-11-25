package com.innerproduct.ee.coordination

import cats.effect._
import cats.effect.concurrent.Ref // <1>
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object ConcurrentStateRef extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      ticks <- Ref[IO].of(0L) // <2>
      _ <- (tickingClock(ticks), printTicks(ticks)).parTupled // <3>
    } yield ExitCode.Success

  def tickingClock(ticks: Ref[IO, Long]): IO[Unit] =
    for {
      _ <- IO.sleep(1.second)
      _ <- IO(System.currentTimeMillis).debug()
      _ = ticks.update(_ + 1) // <4>
      _ <- tickingClock(ticks)
    } yield ()

  def printTicks(ticks: Ref[IO, Long]): IO[Unit] =
    for {
      _ <- IO.sleep(5.seconds)
      _ <- IO(s"TICKS: ${ticks.get}").debug().void // <5>
      _ <- printTicks(ticks)
    } yield ()
}