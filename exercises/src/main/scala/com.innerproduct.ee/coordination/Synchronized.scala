// package com.innerproduct.ee.coordination

// import cats.effect._

// trait Synchronized[A] {
//   def use[B](f: A => IO[B]): IO[B]
// }

// object Synchronized {
//   def of[A](a: A): IO[Synchronized[A]] =
//     for {
//       gate <- Deferred[IO, Unit]
//       _ <- gate.complete(())
//       state <- Ref[IO].of(gate)
//     } yield new Synchronized[A] {
//       def use[B](f: A => IO[B]): IO[B] =
//         for {
//           // TODO: get current state, set next state
//           // TODO: wait for current state completion
//           b <- f(a)
//           // TODO: complete next state
//         } yield b
//     }
// }