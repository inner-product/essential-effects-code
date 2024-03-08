package com.innerproduct.ee.resources

import cats.effect._
import cats.syntax.all._

object ResourceApp extends IOApp.Simple {
  def run: IO[Unit] =
    resources // <1>
      .use { // <3>
        case (a, b, c) =>
          applicationLogic(a, b, c) // <2>
      }
      .void

  val resources: Resource[IO, (DependencyA, DependencyB, DependencyC)] = // <1>
    (resourceA, resourceB, resourceC).tupled

  def resourceA: Resource[IO, DependencyA] = ???
  def resourceB: Resource[IO, DependencyB] = ???
  def resourceC: Resource[IO, DependencyC] = ???

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
