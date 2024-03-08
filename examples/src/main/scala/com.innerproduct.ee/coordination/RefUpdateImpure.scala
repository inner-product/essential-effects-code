package com.innerproduct.ee.coordination

import cats.effect._
import cats.syntax.all._

object RefUpdateImpure extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      ref <- Ref[IO].of(0)
      _ <- List(1, 2, 3).parTraverse(task(_, ref)) // <1>
    } yield ()

  def task(id: Int, ref: Ref[IO, Int]): IO[Unit] =
    ref
      .modify(previous => id -> println(s"$previous->$id")) // <2>
      .replicateA(3) // <3>
      .void
}
