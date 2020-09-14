package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object ParMapN extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    par.as(ExitCode.Success)

  val hello = IO("hello").debug() // <1>
  val world = IO("world").debug() // <1>

  val par =
    (hello, world)
      .parMapN((h, w) => s"$h $w") // <2>
      .debug() // <3>
}
