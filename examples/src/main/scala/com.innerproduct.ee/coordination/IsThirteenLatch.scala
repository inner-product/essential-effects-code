package com.innerproduct.ee.coordination

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object IsThirteenLatch extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      latch <- CountdownLatch(13)
      _ <- (beeper(latch), tickingClock(latch)).parTupled
    } yield ()

  def beeper(latch: CountdownLatch) =
    for {
      _ <- latch.await
      _ <- debugWithThread("BEEP!")
    } yield ()

  def tickingClock(latch: CountdownLatch): IO[Unit] =
    for {
      _ <- IO.sleep(1.second)
      _ <- IO.realTimeInstant.debug()
      _ <- latch.decrement
      _ <- tickingClock(latch)
    } yield ()
}
