package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object ParMapNErrors extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    e1.attempt.debug() *> // <1>
      e2.attempt.debug() *>
      e3.attempt.debug() *>
      IO.pure(ExitCode.Success)

  val ok = IO("hi").debug()
  val ko1 = IO.raiseError[String](new RuntimeException("oh!")).debug()
  val ko2 = IO.raiseError[String](new RuntimeException("noes!")).debug()

  val e1 = (ok, ko1).parMapN((_, _) => ())
  val e2 = (ko1, ok).parMapN((_, _) => ())
  val e3 = (ko1, ko2).parMapN((_, _) => ())
}
