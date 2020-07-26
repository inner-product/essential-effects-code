package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object Throughput extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      start <- now
      _ <- tasks.debug()
      end <- now
      _ <- IO(s"numCpus: $numCpus").debug()
      _ <- IO(s"numTasks (numCpus * 10): $numTasks").debug()
      _ <- IO(s"sleepPerTask: $sleepPerTask").debug()
      _ <- IO(s"minimum total time (sleepPerTask * 10 tasks/CPU): $minTotalTime").debug()
      duration = (end - start).millis // <5>
      _ <- IO(s"actual total time: $duration").debug()
    } yield ExitCode.Success
    
  val now = Clock[IO].realTime(TimeUnit.MILLISECONDS)
  val numCpus = Runtime.getRuntime().availableProcessors()
  val numTasks = numCpus * 10 // <2>
  val tasks = List.range(0, numTasks).parTraverse(task) // <1>
  def task(i: Int): IO[Int] =
    IO(Thread.sleep(sleepPerTask.toMillis)).as(i).debug() // <3>
  val sleepPerTask = 100.millis
  val minTotalTime = 10 * sleepPerTask // <4>
}
