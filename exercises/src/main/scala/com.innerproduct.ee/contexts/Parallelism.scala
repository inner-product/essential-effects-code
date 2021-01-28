package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object Parallelism extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO(s"number of CPUs: $numCpus").debug
      _ <- tasks.debug
    } yield ExitCode.Success

  val numCpus = Runtime.getRuntime().availableProcessors() // <1>
  val tasks = List.range(0, numCpus * 2).parTraverse(task) // <2>
  def task(i: Int): IO[Int] = IO(i).debug // <3>
}