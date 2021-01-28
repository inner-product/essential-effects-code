package com.innerproduct.ee.resources

import cats.effect._
import cats.implicits._

object ResourceApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    resources // <1>
      .use { // <3>
        case (a, b, c) =>
          applicationLogic(a, b, c) // <2>
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
  }

trait DependencyA
trait DependencyB
trait DependencyC
