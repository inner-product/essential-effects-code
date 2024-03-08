package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._

object Blocking extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      _ <- debugWithThread("on default")
      _ <- debugWithThread("on blocker")
    } yield ()
}
