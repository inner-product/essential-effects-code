package com.innerproduct.ee.concurrent

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object Start extends IOApp {
  
  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- task.start // <1>
      _ <- IO("task was started").debug() // <2>
    } yield ExitCode.Success

  val task: IO[String] =
    IO("task").debug() // <2>
}
