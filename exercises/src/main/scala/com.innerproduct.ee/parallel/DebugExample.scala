package com.innerproduct.ee.parallel

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object DebugExample extends IOApp.Simple {
  def run: IO[Unit] =
    seq.void

  val hello = debugWithThread("hello") // <1>
  val world = debugWithThread("world") // <1>

  val seq =
    (hello, world)
      .mapN((h, w) => s"$h $w")
      .debug() // <2>
}
