package com.innerproduct.ee.io

import cats.effect._

object HelloWorld extends IOApp.Simple { // <1>
  def run: IO[Unit] = // <2>
    IO.println("Hello world!") // <3>
}