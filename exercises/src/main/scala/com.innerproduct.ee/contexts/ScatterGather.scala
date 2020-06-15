package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object ScatterGather extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO]
      .use { blocker =>
        scatterGather(blocker, tasks)
      }
      .as(ExitCode.Success)

  val tasks = List.range(0, 100)

  def scatterGather(blocker: Blocker, tasks: List[Int]): IO[Unit] =
    for {
      scattered <- scatter(blocker, tasks)
      _ <- gather(scattered)
    } yield ()

  def scatter(blocker: Blocker, tasks: List[Int]): IO[List[String]] =
    ???

  def task(i: Int): IO[String] =
    IO(Thread.sleep(1000)) *>
      IO(s"scatter $i").debug()

  def gather(results: List[String]) =
    IO(s"gathered: $results").debug()
}
