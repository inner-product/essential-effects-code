package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._

class Par1[A, B, C](implicit cs: ContextShift[IO]) {
  val ia: IO[A] = IO(???)
  val ib: IO[B] = IO(???)

  def f(a: A, b: B): C = ???

  val ipa: IO.Par[A] = IO.Par(ia) // <1>
  val ipb: IO.Par[B] = IO.Par(ib) // <1>

  val ipc: IO.Par[C] = (ipa, ipb).mapN(f) // <2>

  val ic: IO[C] = IO.Par.unwrap(ipc) // <3>
}