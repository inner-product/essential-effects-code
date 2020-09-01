package com.innerproduct.ee.concurrent

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import com.innerproduct.ee.debug._

object ParMapNCancel extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    composed.attempt.debug().as(ExitCode.Success)

  val ok1 = IO(12).debug().onCancel(IO("ok1 canceled!").debug().void) // <2>
  val ok2 = IO(1).debug().onCancel(IO("ok2 canceled!").debug().void) // <2>
  val ko = IO.raiseError[Int](new RuntimeException("oh noes!")).debug()

  val composed: IO[Int] =
    (ok1, ko, ok2).parMapN(_ + _ + _) // <1>
}
