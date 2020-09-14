package com.innerproduct.ee.apps

import cats.effect._
import scala.annotation.nowarn

@nowarn
object BasicApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      a <- dependencyA
      b <- dependencyB
      c <- dependencyC

      _ <- applicationLogic(a, b, c)

      _ <- cleanupC(c)
      _ <- cleanupB(b)
      _ <- cleanupA(a)
    } yield ExitCode.Success

  val dependencyA: IO[DependencyA] = ???
  def cleanupA(depA: DependencyA): IO[Unit] = ???

  val dependencyB: IO[DependencyB] = ???
  def cleanupB(depB: DependencyB): IO[Unit] = ???

  val dependencyC: IO[DependencyC] = ???
  def cleanupC(depC: DependencyC): IO[Unit] = ???

  def applicationLogic(
      a: DependencyA,
      b: DependencyB,
      c: DependencyC
  ): IO[ExitCode] = // <2>
    ???

  trait DependencyA
  trait DependencyB
  trait DependencyC
  
  import cats._

  implicit val id: ApplicativeError[Id, Throwable] =
    new ApplicativeError[Id, Throwable] {
      def ap[A, B](ff: Id[A => B])(fa: Id[A]): Id[B] = ff(fa)
      def handleErrorWith[A](fa: Id[A])(f: Throwable => Id[A]): Id[A] =
        try fa catch { case t => f(t) }
      def pure[A](x: A): Id[A] = x
      def raiseError[A](e: Throwable): Id[A] = throw e
    }
}
