package com.innerproduct.ee.control

import cats.effect._

object Start extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      fiber <- task.start // <1>
      // <2>
    } yield ExitCode.Success

  val task: IO[String] =
    ??? // <2>
}
