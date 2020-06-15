package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object EvalOn extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (cs, cs) match {
      case (cs1, cs2) =>
        for {
          _ <- IO("on default").debug()
          _ <- cs1.shift // <1>
          _ <- IO("on cs1").debug()
          _ <- cs2.shift // <1>
          _ <- IO("on cs2").debug()
        } yield ExitCode.Success
    }

  def cs: ContextShift[IO] =
    IO.contextShift(
      ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
        val t = new Thread(r)
        t.setDaemon(true) // so the JVM can exit
        t
      })
    )
}
