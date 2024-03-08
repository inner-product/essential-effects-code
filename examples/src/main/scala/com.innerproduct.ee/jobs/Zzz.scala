package com.innerproduct.ee.jobs

import cats.effect._

/** A binary state machine: asleep or awake. */
// tag::trait[]
trait Zzz {
  def sleep: IO[Unit] // <1>
  def wakeUp: IO[Unit] // <2>
}
// end::trait[]

object Zzz {

  def asleep: IO[Zzz] = {
    sealed trait State
    case class Asleep(wakeUp: Deferred[IO, Unit]) extends State
    case object Awake extends State

    for {
      wakeUp <- Deferred[IO, Unit]
      state <- Ref[IO].of[State](Asleep(wakeUp))
    } yield new Zzz {
      def sleep: IO[Unit] =
        for {
          asleep <- Deferred[IO, Unit].map(Asleep(_))
          _ <- state.modify {
            case zzz @ Asleep(wakeUp) => zzz -> wakeUp.get
            case Awake                => asleep -> IO.unit
          }.flatten
        } yield ()

      def wakeUp: IO[Unit] =
        state.modify {
          case Asleep(wakeUp) => Awake -> wakeUp.complete(()).void
          case Awake          => Awake -> IO.unit
        }.flatten
    }
  }
}
