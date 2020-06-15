package com.innerproduct.ee.petstore

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._

object Routes {

  def pets[F[_]: Sync](pets: PetService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "pets" / Pet.Id(id) =>
        for {
          pet <- pets.find(id)
          resp <- pet.fold(NotFound())(Ok(_))
        } yield resp
      case req @ POST -> Root / "pets" =>
        for {
          pet <- req.as[Pet]
          id <- pets.give(pet)
          resp <- Ok(id)
        } yield resp
    }
  }

  def orders[F[_]: Sync](orders: OrderService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    implicit val petIdQueryParamDecoder: QueryParamDecoder[Pet.Id] =
      QueryParamDecoder[Long].map(Pet.Id(_))

    object PetIdQueryParamMatcher
        extends QueryParamDecoderMatcher[Pet.Id]("petId")

    implicit val orderStatusQueryParamDecoder: QueryParamDecoder[PetOrder.Status] =
      QueryParamDecoder[String].map {
        case "placed"    => PetOrder.Status.Placed
        case "approved"  => PetOrder.Status.Approved
        case "delivered" => PetOrder.Status.Delivered
      }

    object OrderStatusQueryParamMatcher
        extends QueryParamDecoderMatcher[PetOrder.Status]("status")

    HttpRoutes.of[F] {
      case GET -> Root / "orders" / PetOrder.Id(id) =>
        for {
          order <- orders.find(id)
          resp <- order.fold(NotFound())(Ok(_))
        } yield resp
      case GET -> Root / "orders" :? PetIdQueryParamMatcher(petId) =>
        for {
          order <- orders.findByPetId(petId)
          resp <- order.fold(NotFound())(Ok(_))
        } yield resp
      case GET -> Root / "orders" :? OrderStatusQueryParamMatcher(status) =>
        orders.findByStatus(status).flatMap(Ok(_))
      case POST -> Root / "orders" :? PetIdQueryParamMatcher(petId) =>
        for {
          res <- orders.applyForAdoption(petId)
          resp <- res
            .leftMap {
              case e: PetOrder.Error.IllegalStatusTransition =>
                BadRequest().map(_.withEntity(e: PetOrder.Error))
              case PetOrder.Error.OrderNotFound(_) =>
                InternalServerError() // shouldn't get this for this operation
              case PetOrder.Error.PetNotFound(_) => NotFound()
            }
            .map(Ok(_))
            .merge
        } yield resp
      case POST -> Root / "orders" / PetOrder.Id(id) / "approved" =>
        for {
          res <- orders.approve(id)
          resp <- res
            .leftMap {
              case PetOrder.Error.OrderNotFound(_)              => NotFound()
              case PetOrder.Error.IllegalStatusTransition(_, _) => BadRequest()
              case PetOrder.Error.PetNotFound(_)                => InternalServerError()
            }
            .map(Ok(_))
            .merge
        } yield resp
      case POST -> Root / "orders" / PetOrder.Id(id) / "delivered" =>
        for {
          res <- orders.deliver(id)
          resp <- res
            .leftMap {
              case PetOrder.Error.OrderNotFound(_)              => NotFound()
              case PetOrder.Error.IllegalStatusTransition(_, _) => BadRequest()
              case PetOrder.Error.PetNotFound(_)                => InternalServerError()
            }
            .map(Ok(_))
            .merge
        } yield resp
    }
  }
}
