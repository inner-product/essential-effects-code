package com.innerproduct.ee.coordination

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._

object RefUpdatePure extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      ref <- Ref[IO].of(0)
      _ <- List(1, 2, 3).parTraverse(task(_, ref))
    } yield ()

  def task(id: Int, ref: Ref[IO, Int]): IO[Unit] =
    ref
      .modify(previous => id -> debugWithThread(s"$previous->$id"))
      .flatten
      .replicateA(3)
      .void
}
