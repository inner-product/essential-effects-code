package com.innerproduct.ee.apps

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object TickingClock extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    tickingClock
      .guaranteeCase { // <2>
        case ExitCase.Canceled => IO("canceled").debug().void
        case ExitCase.Completed => IO("completed").debug().void
        case ExitCase.Error(t) => IO(s"error: $t").debug().void
      }
      .as(ExitCode.Success)

  val tickingClock: IO[Unit] = // <1>
    IO(System.currentTimeMillis).debug() >>
      IO.sleep(1.second) >>
      tickingClock
}
