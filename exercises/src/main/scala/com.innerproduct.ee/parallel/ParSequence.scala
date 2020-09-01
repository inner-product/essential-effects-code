package com.innerproduct.ee.parallel

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object ParSequence extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    tasks.parSequence // <1>
      .debug() // <2>
      .as(ExitCode.Success)

  val numTasks = 100
  val tasks: List[IO[Int]] = List.tabulate(numTasks)(task)

  def task(id: Int): IO[Int] = IO(id).debug() // <2>
}
