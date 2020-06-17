package com.innerproduct.ee.asynchrony

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.ExecutionContext

object AsyncThread extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    (IO("on default context").debug() *> effect.debug())
      .as(ExitCode.Success)

  val effect: IO[String] =
    IO.async { cb =>
      ExecutionContext.global.execute {
        new Runnable {
          def run() = cb(Right("on global context"))
        }
      }
    }
}
