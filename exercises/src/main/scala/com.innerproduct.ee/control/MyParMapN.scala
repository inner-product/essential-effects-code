package com.innerproduct.ee.control

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object MyParMapN extends IOApp.Simple {
  import MyParMapNSyntax._
  
  def run: IO[Unit] =
    (task1, task2)
      .myParMapN(_ + _) // <1>
      .debug()
      .void

  val task1 = IO.sleep(1.second) *> task(1)
  val task2 = task(2)

  def task(i: Int): IO[Int] = debugWithThread(i)
}

object MyParMapNSyntax {
  implicit class ExtensionMethods[A, B](iab: (IO[A], IO[B])) { // <2>
    def myParMapN[C](f: (A, B) => C): IO[C] =
      iab match {
        case (ia, ib) =>
          for {
            fiberA <- ia.start // <3>
            fiberB <- ib.start // <3>
            a <- fiberA.joinWithNever // <4>
            b <- fiberB.joinWithNever // <4>
          } yield f(a, b) // <5>
      }
  }
}
