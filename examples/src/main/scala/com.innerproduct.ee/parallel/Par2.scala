package com.innerproduct.ee.parallel

import cats._
import cats.effect._
import cats.syntax.all._

class Par2[A, B, C] {
  val ia: IO[A] = IO(???)
  val ib: IO[B] = IO(???)

  def f(a: A, b: B): C = ???

  val ipa: IO.Par[A] = Parallel[IO].parallel(ia)
  val ipb: IO.Par[B] = Parallel[IO].parallel(ib)

  val ipc: IO.Par[C] = (ipa, ipb).mapN(f)

  val ic: IO[C] = Parallel[IO].sequential(ipc)
}