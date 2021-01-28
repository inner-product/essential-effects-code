package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._

object Shifting extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO("one").debug
      _ <- IO.shift
      _ <- IO("two").debug
      _ <- IO.shift
      _ <- IO("three").debug
    } yield ExitCode.Success
}
