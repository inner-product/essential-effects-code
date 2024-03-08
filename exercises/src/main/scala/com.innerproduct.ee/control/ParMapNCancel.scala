package com.innerproduct.ee.control

import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object ParMapNCancel extends IOApp.Simple {
  def run: IO[Unit] =
    composed.attempt.debug().void

  val ok1 = debugWithThread(12).onCancel(debugWithThread("ok1 canceled!").void) // <2>
  val ok2 = debugWithThread(1).onCancel(debugWithThread("ok2 canceled!").void) // <2>
  val ko = IO.raiseError[Int](new RuntimeException("oh noes!")).debug()

  val composed: IO[Int] =
    (ok1, ko, ok2).parMapN(_ + _ + _) // <1>
}
