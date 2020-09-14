import cats.effect._

//// Constructors ////

// delay
IO.delay(1 + 2)
// apply
// pure
// raiseError

//// Eliminators, do not ever use in normal code! ////

// unsafeRunSync
// unsafeToFuture

//// Combinators ////

// map
// as
// void

// mapN
// tupled

// flatMap

// handleErrorWith
// handleError
// adaptError
// attempt