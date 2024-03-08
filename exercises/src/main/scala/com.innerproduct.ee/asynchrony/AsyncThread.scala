package com.innerproduct.ee.asynchrony

import cats.effect._
import scala.concurrent.ExecutionContext

object AsyncThread extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      _ <- IO("on default context").debug()
      _ <- effect.debug()
      _ <- IO("where am I?").debug()
    } yield ()

  val effect: IO[String] =
    IO.async_ { cb =>
      ExecutionContext.global.execute {
        new Runnable {
          def run() = ???
        }
      }
    }
}
