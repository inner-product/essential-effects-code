package com.innerproduct.ee.apps

import cats.effect._
import cats.implicits._
import scala.annotation.nowarn

@nowarn
object ResourceApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    resources
      .use { // <3>
        case (a, b, c) =>
          applicationLogic(a, b, c)
      }
      .as(ExitCode.Success)

  val resources: Resource[IO, (DependencyA, DependencyB, DependencyC)] = // <1>
    (resourceA, resourceB, resourceC).tupled

  val resourceA: Resource[IO, DependencyA] = ???
  val resourceB: Resource[IO, DependencyB] = ???
  val resourceC: Resource[IO, DependencyC] = ???

  def applicationLogic( // <2>
      a: DependencyA,
      b: DependencyB,
      c: DependencyC
  ): IO[ExitCode] =
    ???

  trait DependencyA
  trait DependencyB
  trait DependencyC
}
