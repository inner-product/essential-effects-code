package com.innerproduct.ee.resources

import cats.effect._
import cats.syntax.all._

object BasicResourceComposed extends IOApp.Simple {
  def run: IO[Unit] =
    (stringResource, intResource).tupled // <2>
      .use {
        case (s, i) => // <2>
          IO(s"$s is so cool!").debug() *>
          IO(s"$i is also cool!").debug()
      }
      .void

  val stringResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring stringResource").debug() *> IO("String")
    )(_ => IO("< releasing stringResource").debug().void)

  val intResource: Resource[IO, Int] = // <1>
    Resource.make(
      IO("> acquiring intResource").debug() *> IO(99)
    )(_ => IO("< releasing intResource").debug().void)
}
