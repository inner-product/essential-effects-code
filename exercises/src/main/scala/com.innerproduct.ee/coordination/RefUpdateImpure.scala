package com.innerproduct.ee.coordination

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

object RefUpdateImpure extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      ref <- Ref[IO].of(0)
      _ <- List(1, 2, 3).parTraverse(task(_, ref)) // <1>
    } yield ExitCode.Success

  def task(id: Int, ref: Ref[IO, Int]): IO[Unit] =
    ref
      .modify(previous => id -> println(s"$previous->$id")) // <2>
      .replicateA(3) // <3>
      .void
}
