package com.innerproduct.ee.resources

import cats.effect._

object BasicResourceFailure extends IOApp.Simple {
  def run: IO[Unit] =
    stringResource
      .use(_ => IO.raiseError(new RuntimeException("oh noes!"))) // <1>
      .attempt
      .debug()
      .void

  val stringResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring stringResource").debug() *> IO("String")
    )(_ => IO("< releasing stringResource").debug().void)
}
