package com.innerproduct.ee.effects

case class MyIO[A](unsafeRun: () => A) {
  def map[B](f: A => B): MyIO[B] =
    MyIO(() => f(unsafeRun())) // <1>

  def flatMap[B](f: A => MyIO[B]): MyIO[B] =
    MyIO(() => f(unsafeRun()).unsafeRun()) // <2>
}

object MyIO {
  def putStr(s: => String): MyIO[Unit] =
    MyIO(() => println(s))
}

object Printing extends App {
  lazy val hello = MyIO.putStr("hello!")
  lazy val world = MyIO.putStr("world!")

  val helloWorld = // <3>
    for {
      _ <- hello
      _ <- world
    } yield ()

  helloWorld.unsafeRun()
}