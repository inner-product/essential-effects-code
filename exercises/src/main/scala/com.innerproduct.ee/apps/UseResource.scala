package com.innerproduct.ee.apps

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._

object UseResource extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    (myResource, myInt).tupled
      .use {
        case (r, i) =>
          IO(s"$r is so cool!").debug() *>
          IO(s"$i is also cool!").debug()
      }
      .as(ExitCode.Success)

  val myResource: Resource[IO, String] =
    Resource.make(
      IO("> acquiring MyResource").debug() *> IO("MyResource")
    )(r => IO(s"< releasing $r").debug().void)

  val myInt: Resource[IO, Int] =
    Resource.make(
      IO("> acquiring Int").debug() *> IO(99)
    )(r => IO(s"< releasing $r").debug().void)

}
