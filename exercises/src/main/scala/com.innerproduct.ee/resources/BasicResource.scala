package com.innerproduct.ee.resources

import cats.effect._

object BasicResource extends IOApp.Simple {
  def run: IO[Unit] =
    stringResource
      .use { s => // <2>
        IO.println(s"$s is so cool!")
      }

  val stringResource: Resource[IO, String] = // <1>
    Resource.make(
      IO.println("> acquiring stringResource") *> IO("String")
    )(_ => IO.println("< releasing stringResource"))
}
