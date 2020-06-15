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
      _ <- IO(s"minimum total time: $minTotalTime").debug()
      duration = FiniteDuration(end - start, TimeUnit.MILLISECONDS)
      _ <- IO(s"actual  total time: $duration").debug()
    } yield ExitCode.Success

  val now = Clock[IO].realTime(TimeUnit.MILLISECONDS)
  val numTasks = Runtime.getRuntime().availableProcessors() * 10
  val tasks = List.range(0, numTasks).parTraverse(task)
  def task(i: Int): IO[Int] =
    IO(Thread.sleep(sleepPerTask.toMillis)).as(i).debug()
  val sleepPerTask = 100.millis
  val minTotalTime = 10 * sleepPerTask
}
