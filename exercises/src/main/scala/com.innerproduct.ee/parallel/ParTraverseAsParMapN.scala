package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object ParTraverseAsParMapN extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    li1.debug *>
      li2.debug *>
      li3.debug *>
      li4.debug *>
      IO(ExitCode.Success)

  val li1 = (task(1), task(2)).parMapN((a, b) => List(a, b)) // IO[List[Int]] <1>
  val li2 = (task(1), task(2), task(3)).parMapN((a, b, c) => List(a, b, c)) // IO[List[Int]] <2>
  val li3 = (task(1), task(2), task(3), task(4)).parMapN((a, b, c, d) =>
    List(a, b, c, d)
  ) // IO[List[Int]] <3>
  val li4 = List(1, 2, 3, 4).parTraverse(task) // IO[List[Int]] <4>

  def task(i: Int): IO[Int] = IO(i).debug
}
