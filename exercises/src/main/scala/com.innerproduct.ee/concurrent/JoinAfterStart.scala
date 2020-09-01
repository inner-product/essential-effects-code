package com.innerproduct.ee.concurrent

import cats.effect._
import com.innerproduct.ee.debug._
import scala.annotation.nowarn

object JoinAfterStart extends IOApp {
  
  @nowarn
  def run(args: List[String]): IO[ExitCode] =
    for {
      fiber <- task.start // <1>
      _ <- IO("pre-join").debug() // <3>
      // <2>
      _ <- IO("post-join").debug() // <3>
    } yield ExitCode.Success

  @nowarn
  val task: IO[String] =
    ??? // <3>
}
