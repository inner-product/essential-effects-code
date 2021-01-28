package com.innerproduct.ee.effects

import scala.concurrent.duration.FiniteDuration

object Timing extends App {
  val clock: MyIO[Long] =
    ??? // <1>

  def time[A](action: MyIO[A]): MyIO[(FiniteDuration, A)] =
    ??? // <2>

  val timedHello = Timing.time(MyIO.putStr("hello"))

  timedHello.unsafeRun() match {
    case (duration, _) => println(s"'hello' took $duration")
  }
}
