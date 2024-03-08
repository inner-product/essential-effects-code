package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ShiftingMultiple extends IOApp.Simple {

  def run: IO[Unit] =
    (ec("1"), ec("2")) match { // <1>
      case (ec1, ec2) =>
        for {
          _ <- debugWithThread("one").evalOn(ec1) // <2>
          _ <- debugWithThread("two").evalOn(ec2) // <3>
          _ <- debugWithThread("three") // <4>
        } yield ()
    }

  def ec(name: String): ExecutionContext = // <5>
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
      val t = new Thread(r, s"pool-$name-thread-1")
      t.setDaemon(true) // <6>
      t
    })
}
