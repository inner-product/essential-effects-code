package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._

object Shifting extends IOApp {

  val cs = ContextShift[IO] // <1>

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO("one").debug()
      _ <- cs.shift // <2>
      _ <- IO("two").debug()
      _ <- cs.shift // <2>
      _ <- IO("three").debug()
    } yield ExitCode.Success
}
