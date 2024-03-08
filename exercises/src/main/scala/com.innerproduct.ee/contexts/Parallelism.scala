package com.innerproduct.ee.contexts

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object Parallelism extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      _ <- IO.println(s"number of CPUs: $numCpus")
      _ <- tasks.debug()
    } yield ()

  val numCpus = Runtime.getRuntime().availableProcessors() // <1>
  val tasks = List.range(0, numCpus * 2).parTraverse(task) // <2>
  def task(i: Int): IO[Int] = debugWithThread(i) // <3>
}