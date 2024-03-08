package com.innerproduct.ee.control

import cats.effect._

object Start extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      fiber <- task.start // <1>
      // <2>
    } yield ()

  val task: IO[String] =
    ??? // <2>
}
