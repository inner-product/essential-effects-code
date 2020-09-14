package com.innerproduct.ee.concurrent

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object MyParMapN extends IOApp {
  import MyParMapNSyntax._
  
  def run(args: List[String]): IO[ExitCode] =
    (task1, task2)
      .myParMapN(_ + _) // <1>
      .debug()
      .as(ExitCode.Success)

  val task1 = IO.sleep(1.second) *> task(1)
  val task2 = task(2)

  def task(i: Int): IO[Int] = IO(i).debug()
}

object MyParMapNSyntax {
  implicit class ExtensionMethods[A, B](iab: (IO[A], IO[B]))( // <2>
      implicit cs: ContextShift[IO] // <3>
  ) {
    def myParMapN[C](f: (A, B) => C): IO[C] =
      iab match {
        case (ia, ib) =>
          for {
            fiberA <- ia.start // <4>
            fiberB <- ib.start // <4>
            a <- fiberA.join // <5>
            b <- fiberB.join // <5>
          } yield f(a, b) // <6>
      }
  }
}
