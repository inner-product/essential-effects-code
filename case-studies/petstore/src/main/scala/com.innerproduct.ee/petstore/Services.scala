package com.innerproduct.ee.petstore

import cats.tagless._

/** Pet inventory service. */
@autoFunctorK
@autoInstrument
trait PetService[F[_]] { self =>

  /** Find a [Pet] by its [Pet.Id]. */
  def find(id: Pet.Id): F[Option[Pet]]

  /** Give a [Pet] for adoption. */
  def give(pet: Pet): F[Pet.Id]
}

// workaround for https://github.com/typelevel/cats-tagless/issues/125
object PetService

/** Pet adoption order service .*/
@autoFunctorK
@autoInstrument
trait OrderService[F[_]] {
  def find(id: PetOrder.Id): F[Option[PetOrder]]

  def findByPetId(id: Pet.Id): F[Option[PetOrder]]

  /** Find all [PetOrder] with a given [PetOrder.Status]. */
  def findByStatus(status: PetOrder.Status): F[List[PetOrder]]

  /** Begin an adoption of a [Pet]. */
  def applyForAdoption(id: Pet.Id): F[Either[PetOrder.Error, PetOrder.Id]]

  /** Approve an existing [PetOrder] for delivery. */
  def approve(id: PetOrder.Id): F[Either[PetOrder.Error, Unit]]

  /** Deliver a pet to their approved owner. */
  def deliver(id: PetOrder.Id): F[Either[PetOrder.Error, Unit]]
}

// workaround for https://github.com/typelevel/cats-tagless/issues/125
object OrderService

/** Pet adoption order persistence repository. */
@autoFunctorK
@autoInstrument
trait OrderRepository[F[_]] {
  def find(id: PetOrder.Id): F[Option[PetOrder]]

  def findByPetId(id: Pet.Id): F[Option[PetOrder]]

  /** Find all [Order] with a given [Order.Status]. */
  def findByStatus(status: PetOrder.Status): F[List[PetOrder]]

  /** Give a [Order] for adoption. */
  def create(order: PetOrder): F[PetOrder.Id]

  /** Update an [Order]. */
  def update(id: PetOrder.Id, order: PetOrder): F[Unit]
}

// workaround for https://github.com/typelevel/cats-tagless/issues/125
object OrderRepository