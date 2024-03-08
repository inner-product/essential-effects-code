package com.innerproduct.ee.testing

import cats._
import cats.effect._
import cats.effect.testkit.TestControl
import java.util.concurrent.TimeoutException
import munit.CatsEffectSuite
import scala.concurrent.duration._
import scala.util._

class TestContextExamples extends CatsEffectSuite {
  test("IO.sleep") {
    // tag::sleep[]
    val timeoutError = new TimeoutException
    val timeout = IO.sleep(10.seconds) *> IO.raiseError[Int](timeoutError) // <1>
    TestControl
      .execute(timeout) // <2>
      .flatMap { ctx => 
        // Not yet
        ctx.tickFor(5.seconds) *> // <3>
          ctx.results.map(r => assertEquals(r, None)) *> // <3>
          // Good to go:
          ctx.tickFor(5.seconds) *> // <4>
          ctx.results.map(r =>
            assertEquals( // <4>
              r,
              Some(Outcome.errored[Id, Throwable, Int](timeoutError))
            )
          )
      }
    // end::sleep[]
  }

  test("IO.race") {
    // tag::race[]
    val t1 = IO.sleep(100.seconds)
    val t2 = IO.sleep(200.seconds)
    val racing = IO.race(t1, t2) // <1>

    TestControl
      .executeEmbed(racing) // <2>
      .map { r =>
        assertEquals(r, Left(())) // <3>
      }
    // end::race[]
  }
}
