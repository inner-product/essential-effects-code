package com.innerproduct.ee.resources

import cats.effect._

object NeverFinish extends IOApp.Simple {
  def run: IO[Unit] =
    IO.println("You'll never stop me!!!") *>
      IO.never *>
      IO.println("None shall read this... ever!")
}
