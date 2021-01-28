package com.innerproduct.ee.asynchrony

import cats.effect._
import com.innerproduct.ee.debug._
import scala.concurrent._
import scala.util._

object AsyncFuture extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    doSomething(api)(ExecutionContext.global).debug.as(ExitCode.Success)

  val api = new API {}
  
  // tag::code[]
  trait API {
    def compute: Future[Int] = ??? // <1>
  }

  def doSomething[A](api: API)(implicit ec: ExecutionContext): IO[Int] = {
    IO.async[Int] { cb => // <2>
      api.compute.onComplete {
        case Failure(t) => cb(Left(t))  // <3>
        case Success(a) => cb(Right(a)) // <3>
      } 
    }.guarantee(IO.shift) // <4>
  }
  // end::code[]
}
