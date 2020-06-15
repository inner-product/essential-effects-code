package com.innerproduct.ee.concurrent

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object JoinAfterStart extends IOApp {
  
  def run(args: List[String]): IO[ExitCode] =
    for {
      fiber <- task.start // <1>
      _ <- IO("pre-join").debug() // <3>
      _ <- fiber.join // <2>
      _ <- IO("post-join").debug() // <3>
    } yield ExitCode.Success

  val task: IO[String] =
    IO("task").debug() // <3>
}
