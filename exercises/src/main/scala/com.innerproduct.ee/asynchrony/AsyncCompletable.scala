package com.innerproduct.ee.asynchrony

import cats.effect._
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
  def fromCF[A](cfa: IO[CompletableFuture[A]]): IO[A] =
    cfa.flatMap { fa =>
      IO.async { cb =>
        val handler: (A, Throwable) => Unit = {// <1>
          case (a, null) => cb(Right(a))
          case (null, t) => cb(Left(t))
          case (null, null) => sys.error("java is broken")
        }

        fa.handle[Unit](handler.asJavaBiFunction)
        
        ()
      }
    }

  def cf(): CompletableFuture[String] =
    CompletableFuture.completedFuture("woo!")
}
