package com.innerproduct.ee.effects

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object Timing extends App {

  val clock: MyIO[Long] =
    MyIO(() => System.currentTimeMillis) // <1>

  def time[A](action: MyIO[A]): MyIO[(FiniteDuration, A)] =
    for { // <2>
      start <- clock
      a <- action
      end <- clock
    } yield (FiniteDuration(end - start, TimeUnit.MILLISECONDS), a)

  val timedHello = Timing.time(Printing.hello)

  timedHello.unsafeRun() match {
    case (duration, _) => println(s"'hello' took $duration")
  }
}
