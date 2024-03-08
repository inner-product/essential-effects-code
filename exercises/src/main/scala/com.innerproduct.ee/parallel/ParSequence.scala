package com.innerproduct.ee.parallel

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object ParSequence extends IOApp.Simple {
  def run: IO[Unit] =
    tasks.parSequence // <1>
      .debug()
      .void

  val numTasks = 100
  val tasks: List[IO[Int]] = List.tabulate(numTasks)(task) // <3>

  def task(id: Int): IO[Int] = debugWithThread(id) // <2>
}
