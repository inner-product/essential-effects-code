package com.innerproduct.ee.asynchrony

import cats.effect._
import com.innerproduct.ee.debug._
import scala.concurrent.Future

object FromFuture extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    asIO.debug().as(ExitCode.Success)

  val asIO: IO[String] =
    IO.fromFuture(IO(asFuture))

  def asFuture(): Future[String] =
    Future.successful("woo!")
}