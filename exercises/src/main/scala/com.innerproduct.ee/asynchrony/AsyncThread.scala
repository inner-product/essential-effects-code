package com.innerproduct.ee.asynchrony

import cats.effect._
import com.innerproduct.ee.debug._
import scala.annotation.nowarn
import scala.concurrent.ExecutionContext

object AsyncThread extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    effect.debug().as(ExitCode.Success)

  @nowarn
  val effect: IO[String] =
    IO.async { cb =>
      ExecutionContext.global.execute {
        new Runnable {
          def run() = ???
        }
      }
    }
}
