package com.innerproduct.ee.coordination

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import com.innerproduct.ee.debug._

object RefUpdatePure extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      ref <- Ref[IO].of(0)
      _ <- List(1, 2, 3).parTraverse(task(_, ref))
    } yield ExitCode.Success

  def task(id: Int, ref: Ref[IO, Int]): IO[Unit] =
    ref
      .modify(previous => id -> IO(s"$previous->$id").debug())
      .flatten
      .replicateA(3)
      .void
}
