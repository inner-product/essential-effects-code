package com.innerproduct.ee.control

import cats.effect._
import com.innerproduct.ee.debug._

object Cancel extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      fiber <- task.start // <2>
      _ <- debugWithThread("pre-cancel")
      // <3>
      _ <- debugWithThread("canceled")
    } yield ()

  val task: IO[Nothing] =
    debugWithThread("task") *>
      IO.never // <1>
}
