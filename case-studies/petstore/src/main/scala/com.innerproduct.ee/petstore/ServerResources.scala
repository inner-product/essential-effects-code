package com.innerproduct.ee.petstore

import cats._
import cats.data._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

object ServerResources {

  def pets[F[_]: Sync]: Resource[F, PetService[F]] =
    for {
      pets <- Resource.liftF(Ref[F].of(Map.empty[Pet.Id, Pet]))
    } yield new PetService[F] {
      def find(id: Pet.Id): F[Option[Pet]] =
        pets.get.map(_.get(id))
      def give(pet: Pet): F[Pet.Id] =
        pets.modify { pets =>
          pets.find(_._2 == pet) match {
            case None =>
              val id = Pet.Id(
                (if (pets.isEmpty) 0 else pets.keySet.maxBy(_.toLong).toLong) + 1
              )
              (pets + (id -> pet), id)
            case Some(id -> _) => (pets, id)
          }

        }
    }

  def orders[F[_]: Monad](
      pets: PetService[F],
      orderRepo: OrderRepository[F]
  ): OrderService[F] =
    new OrderService[F] {

      /** Find an [Order] by its [PetOrder.Id]. */
      def find(id: PetOrder.Id): F[Option[PetOrder]] = orderRepo.find(id)

      def findByPetId(id: Pet.Id): F[Option[PetOrder]] =
        orderRepo.findByPetId(id)

      /** Find all [Order] with a given [PetOrder.Status]. */
      def findByStatus(status: PetOrder.Status): F[List[PetOrder]] =
        orderRepo.findByStatus(status)

      /** Begin an adoption of a [Pet]. */
      def applyForAdoption(id: Pet.Id): F[Either[PetOrder.Error, PetOrder.Id]] =
        ???

      /** Approve an existing [PetOrder] for delivery. */
      def approve(id: PetOrder.Id): F[Either[PetOrder.Error, Unit]] =
        (for {
          order <- OptionT(orderRepo.find(id))
            .toRight[PetOrder.Error](PetOrder.Error.OrderNotFound(id))
          newOrder <- order
            .transitionTo(PetOrder.Status.Approved)
            .toEitherT[F]
          _ <- orderRepo
            .update(id, newOrder)
            .asRight[PetOrder.Error]
            .toEitherT[F]
        } yield ()).value

      /** Deliver a pet to their approved owner. */
      def deliver(id: PetOrder.Id): F[Either[PetOrder.Error, Unit]] =
        (for {
          order <- OptionT(orderRepo.find(id))
            .toRight[PetOrder.Error](PetOrder.Error.OrderNotFound(id))
          _ <- OptionT(
            pets
              .find(order.petId)
          ).toRight[PetOrder.Error](PetOrder.Error.PetNotFound(order.petId))
          newOrder <- order.transitionTo(PetOrder.Status.Delivered).toEitherT[F]
          _ <- orderRepo
            .update(id, newOrder)
            .asRight[PetOrder.Error]
            .toEitherT[F]
        } yield ()).value
    }

  def orderRepo[F[_]: Sync]: Resource[F, OrderRepository[F]] =
    for {
      orders <- Resource.liftF(Ref[F].of(Map.empty[PetOrder.Id, PetOrder]))
    } yield new OrderRepository[F] {
      def create(order: PetOrder): F[PetOrder.Id] =
        orders.modify { orders =>
          // TODO: assumes all given orders are unique
          val id =
            PetOrder.Id(
              (if (orders.isEmpty) 0 else orders.keySet.maxBy(_.toLong).toLong) + 1
            )
          (orders + (id -> order), id)
        }
      def find(id: PetOrder.Id): F[Option[PetOrder]] =
        orders.get.map(_.get(id))
      def findByStatus(status: PetOrder.Status): F[List[PetOrder]] =
        orders.get.map(_.values.find(_.status == status).toList)
      def findByPetId(id: Pet.Id): F[Option[PetOrder]] =
        orders.get.map(_.values.find((_.petId == id)))
      def update(id: PetOrder.Id, order: PetOrder): F[Unit] =
        orders.update(_ + (id -> order)).void
    }
}
