package com.innerproduct.ee.jobs

import cats.effect._

// tag::loop[]
object RunLoop {
  def whileInUse(
      step: IO[_] // <1>
  ): Resource[IO, IO[OutcomeIO[Nothing]]] =
    step.foreverM // <2>
    .background // <3>
}
// end::loop[]
