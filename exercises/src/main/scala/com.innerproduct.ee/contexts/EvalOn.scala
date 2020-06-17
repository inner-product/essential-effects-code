package com.innerproduct.ee.contexts

import cats.effect._
import cats.implicits._
import com.innerproduct.ee.debug._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object EvalOn extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (ec, ec) match {
      case (ec1, ec2) =>
        for {
          _ <- IO("on default").debug()
          _ <- ContextShift[IO].evalOn(ec1)(IO("on ec1").debug())
          _ <- ContextShift[IO].evalOn(ec2)(IO("on ec2").debug())
        } yield ExitCode.Success
    }

  def ec: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
      val t = new Thread(r)
      t.setDaemon(true) // so the JVM can exit
      t
    })
}
