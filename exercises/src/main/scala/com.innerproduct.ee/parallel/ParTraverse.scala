package com.innerproduct.ee.parallel

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object ParTraverse extends IOApp.Simple {
  def run: IO[Unit] =
    tasks
      .parTraverse(task) // <1>
      .debug()
      .void

  val numTasks = 100
  val tasks: List[Int] = List.range(0, numTasks)

  def task(id: Int): IO[Int] = debugWithThread(id) // <2>
}