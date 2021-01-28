package com.innerproduct.ee.resources

import cats.effect._

object HelloWorld extends IOApp { // <1>
  def run(args: List[String]): IO[ExitCode] = // <2>
    helloWorld.as(ExitCode.Success) // <3>

  val helloWorld: IO[Unit] =
    IO(println("Hello world!"))
}