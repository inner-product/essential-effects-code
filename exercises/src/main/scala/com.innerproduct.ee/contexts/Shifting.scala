package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._

object Shifting extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      _ <- debugWithThread("one")
      _ <- IO.cede
      _ <- debugWithThread("two")
      _ <- IO.cede
      _ <- debugWithThread("three")
    } yield ()
}
