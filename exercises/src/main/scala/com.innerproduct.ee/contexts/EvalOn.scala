package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object EvalOn extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (ec("1"), ec("2")) match {
      case (ec1, ec2) =>
        for {
          _ <- IO("one").debug()
          _ <- ContextShift[IO].evalOn(ec1)(IO("on ec1").debug())
          _ <- IO("two").debug()
          _ <- ContextShift[IO].evalOn(ec2)(IO("on ec2").debug())
          _ <- IO("three").debug()
        } yield ExitCode.Success
    }

  def ec(name: String): ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
      val t = new Thread(r, s"pool-$name-thread-1")
      t.setDaemon(true) // so the JVM can exit
      t
    })
}
