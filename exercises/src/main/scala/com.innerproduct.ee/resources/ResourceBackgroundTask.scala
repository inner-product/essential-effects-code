package com.innerproduct.ee.resources

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object ResourceBackgroundTask extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- backgroundTask.use { _ =>
        IO.sleep(200.millis) *> IO("$s is so cool!").debug // <1>
      }
      _ <- IO("done!").debug
    } yield ExitCode.Success

  val backgroundTask: Resource[IO, Unit] = {
    val loop = (IO("looping...").debug *> IO.sleep(100.millis)).foreverM // <2>

    Resource
      .make(IO("> forking backgroundTask").debug *> loop.start)( // <3>
        IO("< canceling backgroundTask").debug.void *> _.cancel // <4>
      )
      .void // <5>
  }
}
