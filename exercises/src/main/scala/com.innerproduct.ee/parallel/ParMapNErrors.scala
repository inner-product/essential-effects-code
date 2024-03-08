package com.innerproduct.ee.parallel

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object ParMapNErrors extends IOApp.Simple {
  def run: IO[Unit] =
    e1.attempt.debug() *> // <1>
      debugWithThread("---") *>
      e2.attempt.debug() *>
      debugWithThread("---") *>
      e3.attempt.debug() *>
      IO.unit

  val ok = debugWithThread("hi")
  val ko1 = IO.raiseError[String](new RuntimeException("oh!")).debug()
  val ko2 = IO.raiseError[String](new RuntimeException("noes!")).debug()

  val e1 = (ok, ko1).parMapN((_, _) => ())
  val e2 = (ko1, ok).parMapN((_, _) => ())
  val e3 = (ko1, ko2).parMapN((_, _) => ())
}
