package com.innerproduct.ee.asynchrony

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import java.util.concurrent.CompletableFuture
import scala.annotation.nowarn
import scala.jdk.FunctionConverters._

object AsyncCompletable extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    effect.debug().as(ExitCode.Success)

  val effect: IO[String] =
    fromCF(IO(cf()))

  @nowarn
  def fromCF[F[_]: Async, A](cfa: F[CompletableFuture[A]]): F[A] =
    Async[F].flatMap(cfa) { fa =>
      Async[F].async { cb =>
        val handler: (A, Throwable) => Unit = ??? // <1>

        fa.handle[Unit](handler.asJavaBiFunction)
        
        ()
      }
    }

  def cf(): CompletableFuture[String] =
    CompletableFuture.completedFuture("woo!")
}
