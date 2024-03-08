package com.innerproduct.ee.control

import cats.effect._
import com.innerproduct.ee.debug._

object JoinAfterStart extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      fiber <- task.start // <1>
      _ <- debugWithThread("pre-join") // <3>
      // <2>
      _ <- debugWithThread("post-join") // <3>
    } yield ()

  val task: IO[String] =
    ??? // <3>
}
