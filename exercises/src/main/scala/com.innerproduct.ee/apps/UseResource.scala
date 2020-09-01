package com.innerproduct.ee.apps

import cats.effect._
import com.innerproduct.ee.debug._

object UseResource extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    myResource
      .use { r =>
        IO(s"$r is so cool!").debug()
      }
      .as(ExitCode.Success)

  val myResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring MyResource").debug() *> IO("MyResource")
    )(r => IO(s"< releasing $r").debug().void)
}
