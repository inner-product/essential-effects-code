package com.innerproduct.ee.effects

case class MyIO[A](unsafeRun: () => A) // <1>

object MyIO {
  def putStr(s: => String): MyIO[Unit] =
    ??? // <2>
}

object Printing extends App { // <3>
  val hello = MyIO.putStr("hello!")

  hello.unsafeRun()
}