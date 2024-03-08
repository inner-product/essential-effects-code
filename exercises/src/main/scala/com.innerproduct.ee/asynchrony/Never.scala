package com.innerproduct.ee.asynchrony

import cats.effect._

object Never extends IOApp.Simple {
  def run: IO[Unit] =
    never
      .guarantee(IO.println("i guess never is now"))

  val never: IO[Nothing] =
    IO.async_(???) // <1>
}
