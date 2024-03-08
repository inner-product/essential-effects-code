package com.innerproduct.ee.asynchrony

import cats.effect._
import scala.concurrent._
import scala.util._

object AsyncFuture extends IOApp.Simple {
  def run: IO[Unit] =
    doSomething(api)(ExecutionContext.global).debug().void

  val api = new API {}
  
  // tag::code[]
  trait API {
    def compute: Future[Int] = ??? // <1>
  }

  def doSomething[A](api: API)(implicit ec: ExecutionContext): IO[Int] = {
    IO.async_[Int] { cb => // <2>
      api.compute.onComplete {
        case Failure(t) => cb(Left(t))  // <3>
        case Success(a) => cb(Right(a)) // <3>
      } 
    }
  }
  // end::code[]
}
