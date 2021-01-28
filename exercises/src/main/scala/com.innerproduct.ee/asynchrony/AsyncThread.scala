package com.innerproduct.ee.asynchrony

import cats.effect._
import com.innerproduct.ee.debug._
import scala.concurrent.ExecutionContext

object AsyncThread extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO("on default context").debug
      _ <- effect.debug
      _ <- IO("where am I?").debug
    } yield ExitCode.Success

  val effect: IO[String] =
    IO.async { cb =>
      ExecutionContext.global.execute {
        new Runnable {
          def run() = ???
        }
      }
    }
}
