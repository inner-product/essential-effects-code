package com.innerproduct.ee.parallel

import cats.implicits._
import scala.concurrent._
import scala.concurrent.duration._

object Future1 extends App {
  implicit val ec = ExecutionContext.global

  val hello = Future(println(s"[${Thread.currentThread.getName}] Hello")) // <1>
  val world = Future(println(s"[${Thread.currentThread.getName}] World")) // <1>

  val hw1: Future[Unit] =
    for {
      _ <- hello
      _ <- world
    } yield ()

  Await.ready(hw1, 5.seconds) // <2>

  val hw2: Future[Unit] =
    (hello, world).mapN((_, _) => ())

  Await.ready(hw2, 5.seconds) // <3>
}
