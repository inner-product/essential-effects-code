package com.innerproduct.ee.testing

import cats.effect._
import cats.effect.laws.util.TestContext
import java.util.concurrent.TimeoutException
import munit.FunSuite
import scala.concurrent.duration._
import scala.util._

trait EffectTesting {
  val ctx = TestContext()
  implicit val cs: ContextShift[IO] = ctx.ioContextShift
  implicit val timer: Timer[IO] = ctx.timer
}

class TestContextExamples extends FunSuite with EffectTesting {
  test("IO.sleep") {
    val timeoutError = new TimeoutException
    val timeout = IO.sleep(10.seconds) *> IO.raiseError[Int](timeoutError)
    val f = timeout.unsafeToFuture()

    // Not yet
    ctx.tick()
    assertEquals(f.value, None)

    // Not yet
    ctx.tick(5.seconds)
    assertEquals(f.value, None)

    // Good to go:
    ctx.tick(5.seconds)
    assertEquals(f.value, Some(Failure(timeoutError)))
  }

  test("IO.race") {
    val t1 = IO.sleep(100.millis)
    val t2 = IO.sleep(200.millis)
    val f = IO.race(t1, t2).unsafeToFuture()

    ctx.tick()
    assertEquals(f.value, None)

    ctx.tick(100.millis)
    assertEquals(f.value, Some(Success(Left(()))))
  }
}
