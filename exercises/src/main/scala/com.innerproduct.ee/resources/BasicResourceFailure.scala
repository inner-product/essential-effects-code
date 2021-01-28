package com.innerproduct.ee.resources

import cats.effect._
import com.innerproduct.ee.debug._

object BasicResourceFailure extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    stringResource
      .use(_ => IO.raiseError(new RuntimeException("oh noes!"))) // <1>
      .attempt
      .debug
      .as(ExitCode.Success)

  val stringResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring stringResource").debug *> IO("String")
    )(_ => IO("< releasing stringResource").debug.void)
}
