package com.innerproduct.ee.parallel

object Coherence {
  // tag::typeclass-coherence[]
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  trait Applicative[F[_]] extends Functor[F] {
    def pure[A](a: A): F[A]

    def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]

    override def map[A, B](fa: F[A])(f: A => B): F[B] =
      map2(pure(f), fa)((f, a) => f(a)) // <1>
  }

  trait Monad[F[_]] extends Applicative[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

    override def map[A, B](fa: F[A])(f: A => B): F[B] =
      flatMap(fa)(a => pure(f(a))) // <2>

    override def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
      flatMap(fa)(a => map(fb)(b => f(a, b))) // <2>
  }
  // end::typeclass-coherence[]
}
