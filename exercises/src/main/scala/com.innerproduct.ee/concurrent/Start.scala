package com.innerproduct.ee.concurrent

import cats.effect._
import scala.annotation.nowarn

object Start extends IOApp {
  
  @nowarn
  def run(args: List[String]): IO[ExitCode] =
    for {
      fiber <- task.start // <1>
      // run another task, what thread is it on? <2>
    } yield ExitCode.Success

  @nowarn
  val task: IO[String] =
    ??? // ensure we can see the effect <2>
}
