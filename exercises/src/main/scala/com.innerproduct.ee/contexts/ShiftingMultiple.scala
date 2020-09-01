package com.innerproduct.ee.contexts

import cats.effect._
import com.innerproduct.ee.debug._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ShiftingMultiple extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (cs("1"), cs("2")) match { // <1>
      case (cs1, cs2) =>
        for {
          _ <- IO("one").debug() // <2>
          _ <- cs1.shift            // <3>
          _ <- IO("two").debug()    // <3>
          _ <- cs2.shift                // <4>
          _ <- IO("three").debug()      // <4>
        } yield ExitCode.Success
    }

  def cs(name: String): ContextShift[IO] = // <5>
    IO.contextShift(
      ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor { r =>
        val t = new Thread(r, s"pool-$name-thread-1")
        t.setDaemon(true) // <6>
        t
      })
    )
}
