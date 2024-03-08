package com.innerproduct.ee.parallel

import cats._
import cats.effect._
import cats.syntax.all._

class Par1[A, B, C] {
  val ia: IO[A] = IO(???)
  val ib: IO[B] = IO(???)

  def f(a: A, b: B): C = ???

  val ipa: Par[A] = Par(ia) // <1>
  val ipb: Par[B] = Par(ib) // <1>

  val ipc: Par[C] = (ipa, ipb).mapN(f) // <2>

  val ic: IO[C] = Par.value(ipc) // <3>
}

class Par[+A]

object Par {
  def apply[A](ioa: IO[A]): Par[A] = ???
  def value[A](pa: Par[A]): IO[A] = ???

  implicit def ap: Applicative[Par] =
    new Applicative[Par] {
      def pure[A](a: A): Par[A] = Par(IO.pure(a))
      override def map[A, B](pa: Par[A])(f: A => B): Par[B] = ???
      override def product[A, B](pa: Par[A], pb: Par[B]): Par[(A, B)] = ???
      override def ap[A, B](ff: Par[A => B])(fa: Par[A]): Par[B] = ???
    }
}
