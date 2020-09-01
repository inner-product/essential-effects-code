package com.innerproduct.ee

import cats.effect._

case class ThreadName(name: String) extends AnyVal {
  override def toString(): String =
    Colorize.reversed(name)
}

object ThreadName {
  def current(): IO[ThreadName] =
    IO(ThreadName(Thread.currentThread().getName))
}
