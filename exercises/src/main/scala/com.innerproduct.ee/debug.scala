package com.innerproduct.ee

import cats.effect._
import cats.implicits._

/** `import com.innerproduct.ee.debug._` to access the `debug` extension methods. */
object debug {
  /** Extension methods for an effect of type `F[A]`. */
  implicit class DebugHelper[A](ioa: IO[A]) { // <1>

    /** Log the value of the effect along with the thread it was computed on. Logging defaults to `println`. */
    def debug(
        logger: String => Unit = println(_)
    ): IO[A] =
      ioa.flatTap { a =>
        ThreadName.current.map { tn =>
          logger(s"[$tn] $a")
        }
      }
  }
}
