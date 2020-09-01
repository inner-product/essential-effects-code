package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._
import scala.annotation.nowarn

object Blocking extends IOApp {

  @nowarn
  def run(args: List[String]): IO[ExitCode] =
    withBlocker(???).as(ExitCode.Success) // <1>

  @nowarn
  def withBlocker(blocker: Blocker): IO[Unit] =
    for {
      _ <- IO("on default").debug()
      _ <- IO("on blocker").debug() // <2>
    } yield ()
}
