package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object MyParMap2 extends IOApp {
  
  def run(args: List[String]): IO[ExitCode] =
    (task(1), task(2))
      .myParMapN(_ + _) // <1>
      .debug()
      .as(ExitCode.Success)

  def task(i: Int): IO[Int] = IO(i).debug()

  implicit class ExtensionMethods[A, B](iab: (IO[A], IO[B]))( // <2>
      implicit cs: ContextShift[IO] // <3>
  ) {
    def myParMapN[C](f: (A, B) => C): IO[C] =
      iab match {
        case (ia, ib) =>
          val shiftedA = cs.shift.flatMap(_ => ia) // <4>
          val shiftedB = cs.shift.flatMap(_ => ib) // <4>

          (shiftedA, shiftedB).mapN(f) // <5>
      }
  }
}
