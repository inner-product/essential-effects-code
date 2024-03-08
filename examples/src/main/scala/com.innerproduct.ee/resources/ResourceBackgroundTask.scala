package com.innerproduct.ee.resources

import cats.effect._
import cats.syntax.all._
import com.innerproduct.ee.debug._
import scala.concurrent.duration._

object ResourceBackgroundTask extends IOApp.Simple {
  def run: IO[Unit] =
    for {
      _ <- backgroundTask.use { _ =>
        debugWithThread("other work while background task is running") *>
        IO.sleep(200.millis) *>
        debugWithThread("other work done") // <1>
      }
      _ <- debugWithThread("all done")
    } yield ()

  val backgroundTask: Resource[IO, Unit] = {
    val loop =
      (debugWithThread("looping...") *> IO.sleep(100.millis))
        .foreverM // <2>

    Resource
      .make(debugWithThread("> forking backgroundTask") *> loop.start)( // <3>
        debugWithThread("< canceling backgroundTask").void *> _.cancel // <4>
      )
      .void // <5>
  }
}
