package com.innerproduct.ee.apps

import cats.effect._

object HelloWorld extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    helloWorld.as(ExitCode.Success)

  val helloWorld: IO[Unit] =
    for {
      _ <- putStr("hello")
      _ <- putStr("world")
    } yield ()

  def putStr(s: => String): IO[String] =
    IO(s)
}
