package com.innerproduct.ee.petstore

import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.client.{Client => Http4sClient}
import org.http4s.Status._

object ClientResources {
  def pets[F[_]: Sync](
      client: Http4sClient[F],
      baseURI: Uri
  ): PetService[F] =
    new PetService[F] {
      def find(id: Pet.Id): F[Option[Pet]] =
        client.expectOption[Pet](
          Request[F](Method.GET, baseURI / "pets" / id.toLong.toString)
        )
      def give(pet: Pet): F[Pet.Id] =
        client.expect[Pet.Id](
          Request[F](Method.POST, baseURI / "pets").withEntity(pet)
        )
    }

  def orders[F[_]: Sync](
      client: Http4sClient[F],
      baseURI: Uri
  ): OrderService[F] =
    new OrderService[F] {
      // implicit val petIdQueryParamEncoder: QueryParamEncoder[Pet.Id] =
      //   QueryParamEncoder[Long].contramap(_.toLong)
      implicit val petIdQueryParamKey: QueryParamKeyLike[Pet.Id] =
        new QueryParamKeyLike[Pet.Id] {
          def getKey(t: Pet.Id): QueryParameterKey = QueryParameterKey("petId")
        }

      def applyForAdoption(id: Pet.Id): F[Either[PetOrder.Error, PetOrder.Id]] =
        client.fetch(Request[F](Method.POST, baseURI / "orders" +? id)) {
          case Successful(res) => res.as[PetOrder.Id].map(_.asRight[PetOrder.Error])
          case res             => res.as[PetOrder.Error].map(_.asLeft[PetOrder.Id])
        }
      def approve(id: PetOrder.Id): F[Either[PetOrder.Error, Unit]] =
        EitherT(
          client.fetch(
            Request[F](
              Method.POST,
              baseURI / "orders" / id.toLong.toString / "approved"
            )
          ) {
            case Successful(res) => res.as[Unit].map(_.asRight[PetOrder.Error])
            case res             => res.as[PetOrder.Error].map(_.asLeft[Unit])
          }
        ).value
      def deliver(id: PetOrder.Id): F[Either[PetOrder.Error, Unit]] = ???
      def find(id: PetOrder.Id): F[Option[PetOrder]] = ???
      def findByPetId(id: Pet.Id): F[Option[PetOrder]] = ???
      def findByStatus(status: PetOrder.Status): F[List[PetOrder]] = ???
    }
}