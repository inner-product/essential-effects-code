package com.innerproduct.ee.contexts

import cats.effect._
import cats.syntax.all._

object ParBlocking extends IOApp.Simple {
  def run: IO[Unit] =
    List(1, 2, 3)
      .parTraverse(i => task(i))
      .debug()
      .void

  def task(i: Int): IO[Int] = IO.blocking(i).debug()
}
