package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._

class Par3[A, B, C](implicit cs: ContextShift[IO]) {
  val ia: IO[A] = IO(???)
  val ib: IO[B] = IO(???)

  def f(a: A, b: B): C = ???

  val ic: IO[C] = (ia, ib).parMapN(f) // <1>
}
