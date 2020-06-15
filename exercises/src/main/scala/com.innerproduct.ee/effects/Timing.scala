package com.innerproduct.ee.effects

import scala.annotation.nowarn
import scala.concurrent.duration.FiniteDuration

object Timing extends App {
  @nowarn
  val clock: MyIO[Long] =
    ??? // <1>

  def time[A](action: MyIO[A]): MyIO[(FiniteDuration, A)] =
    ??? // <2>

  val timedHello = Timing.time(Printing.hello)

  timedHello.unsafeRun() match {
    case (duration, _) => println(s"'hello' took $duration")
  }
}
