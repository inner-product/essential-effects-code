package com.innerproduct.ee.apps

import cats.effect._
import com.innerproduct.ee.debug._

object BasicResource extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    stringResource
      .use { s => // <2>
        IO(s"$s is so cool!").debug()
      }
      .as(ExitCode.Success)

  val stringResource: Resource[IO, String] = // <1>
    Resource.make(
      IO("> acquiring stringResource").debug() *> IO("String")
    )(_ => IO("< releasing stringResource").debug().void)
}
