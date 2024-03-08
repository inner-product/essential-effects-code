package com.innerproduct.ee.control

import cats.effect._

trait DescribeAndRun[A, B, C] {
  // tag::code[]
  val i1: IO[A] = ??? // <1>
  val i2: IO[B] = ??? // <1>
  val i3: IO[C] = doSomething(i1, i2) // <1>

  import cats.effect.unsafe.implicits.global

  val c: C = i3.unsafeRunSync() // <2>
  // end::code[]

  def doSomething(ia: IO[A], ib: IO[B]): IO[C] = ???
}
