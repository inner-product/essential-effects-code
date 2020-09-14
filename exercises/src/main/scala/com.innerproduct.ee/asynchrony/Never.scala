package com.innerproduct.ee.asynchrony

import cats.effect._
import com.innerproduct.ee.debug._

object Never extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    never
      .guarantee(IO("i guess never is now").debug().void)
      .as(ExitCode.Success)

  val never: IO[Nothing] =
    IO.async(_ => ()) // <1>
}
