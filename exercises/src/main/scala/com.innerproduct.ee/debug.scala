package com.innerproduct.ee

import cats.effect._

/** `import com.innerproduct.ee.debug._` to access
  * the `debugWithThread` method. */
object debug {

  /** Print to the console the value of the effect
    * along with the thread it was computed on. */
  def debugWithThread[A](a: => A): IO[A] =
    WithThreadName(a).debug()

  case class WithThreadName[A] private (value: IO[(A, String)]) {
    def debug(colorize: Boolean = true): IO[A] =
      for {
        atn <- value
        (a, tn) = atn
        msg = if (colorize) s"[${Colorize.reversed(tn)}] $a" else s"[$tn] $a" // <1>
        _ <- IO(msg).debug()
      } yield a
  }

  object WithThreadName {
    def apply[A](a: => A): WithThreadName[A] =
      WithThreadName(IO(a -> Thread.currentThread.getName))
  }
}
