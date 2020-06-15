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
// *>
// <*

// flatMap
// flatTap
// >>

// attempt
// adaptError
// handleErrorWith
// onError
// recover
// recoverWith
// redeem
// redeemWith

// guarantee
// guaranteeCase
// onCancel
