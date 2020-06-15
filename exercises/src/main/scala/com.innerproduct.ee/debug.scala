package com.innerproduct.ee

import cats._
import cats.effect._
import cats.implicits._

/** `import com.innerproduct.ee.debug._` to access the `debug` extension methods. */
object debug {
  /** Extension methods for an effect of type `F[A]`. */
  implicit class EffectHelper[F[_], A](fa: F[A]) { // <1>

    /** Log the value of the effect along with the thread it was computed on. Logging defaults to `println`. */
    def debug(
        logger: String => Unit = println(_)
    )(implicit sync: Sync[F], show: Show[A]): F[A] =
      fa.flatTap { a =>
        ThreadName.current[F].map { tn =>
          logger(show"[$tn] $a")
        }
      }
  }

  implicit val exitCode: Show[ExitCode] = Show.fromToString
  implicit def exitCase[A]: Show[ExitCase[A]] = Show.fromToString
  implicit val throwable: Show[Throwable] = Show.fromToString
}
