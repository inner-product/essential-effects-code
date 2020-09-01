package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object EvalOn extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (cs("1"), cs("2")) match {
      case (cs1, cs2) =>
        for {
          _ <- IO("one").debug()
          _ <- cs1.shift // <1>
          _ <- IO("two").debug()
          _ <- cs2.shift // <1>
          _ <- IO("three").debug()
        } yield ExitCode.Success
    }

  def cs(name: String): ContextShift[IO] =
    IO.contextShift(
      ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
        val t = new Thread(r, s"pool-$name-thread-1")
        t.setDaemon(true) // so the JVM can exit
        t
      })
    )
}
