package com.innerproduct.ee.parallel

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object ParMapN extends IOApp.Simple {
  def run: IO[Unit] =
    par.void

  val hello = debugWithThread("hello") // <1>
  val world = debugWithThread("world") // <1>

  val par =
    (hello, world)
      .parMapN((h, w) => s"$h $w") // <2>
      .debug() // <3>
}
