package com.innerproduct.ee.concurrent

import cats.effect._
import scala.annotation.nowarn

object Start extends IOApp {
  
  @nowarn
  def run(args: List[String]): IO[ExitCode] =
    for {
      fiber <- task.start // <1>
      // <2>
    } yield ExitCode.Success

  @nowarn
  val task: IO[String] =
    ??? // <2>
}
