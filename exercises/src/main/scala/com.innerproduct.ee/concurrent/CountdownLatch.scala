package com.innerproduct.ee.concurrent

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._

trait CountdownLatch[F[_]] {
  def decrement(): F[Unit]
  def await(): F[Unit]
}

object CountdownLatch {
  def apply[F[_]: Concurrent](n: Long): F[CountdownLatch[F]] =
    for {
      whenDone <- Deferred[F, Unit]
      state <- Ref.of[F, State[F]](Outstanding(n, whenDone))
    } yield new CountdownLatch[F] {
      def decrement(): F[Unit] =
        state.modify {
          case Outstanding(1, whenDone) => Done() -> whenDone.complete(())
          case Outstanding(n, whenDone) =>
            Outstanding(n - 1, whenDone) -> ().pure[F]
          case Done() => Done() -> ().pure[F]
        }.flatten

      def await(): F[Unit] =
        state.get.flatMap {
          case Outstanding(_, whenDone) => whenDone.get
          case Done()                   => ().pure[F]
        }
    }
    
  private sealed trait State[F[_]]
  private case class Outstanding[F[_]](n: Long, whenDone: Deferred[F, Unit])
      extends State[F]
  private case class Done[F[_]]() extends State[F]
}
