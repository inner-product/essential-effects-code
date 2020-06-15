package com.innerproduct.ee.petstore

import cats._
import cats.effect._
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.circe._
import java.time.Instant


case class PetOrder(
    petId: Pet.Id,
    shipDate: Option[Instant],
    status: PetOrder.Status
) {
  def transitionTo(
      newStatus: PetOrder.Status
  ): Either[PetOrder.Error, PetOrder] =
    status.transitionTo(newStatus).as(copy(status = newStatus))
}

object PetOrder {
  case class Id(toLong: Long) extends AnyVal

  object Id {
    def unapply(str: String): Option[Id] =
      if (!str.isEmpty) scala.util.Try(Id(str.toLong)).toOption
      else None

    implicit val decoder: Decoder[Id] = Decoder[Long].map(Id.apply)
    implicit val encoder: Encoder[Id] = Encoder[Long].contramap(_.toLong)
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Id] =
      jsonOf[F, Long].map(Id.apply)
    implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, Id] =
      jsonEncoderOf[F, Long].contramap(_.toLong)
  }

  /** Status: Placed -> Approved -> Delivered */
  sealed trait Status {
    def transitionTo(
        that: Status
    ): Either[PetOrder.Error, Status] =
      (this, that) match {
        case (Status.Placed, Status.Approved) =>
          Status.Approved.asRight[PetOrder.Error]
        case (Status.Approved, Status.Delivered) =>
          Status.Delivered.asRight[PetOrder.Error]
        case _ => PetOrder.Error.IllegalStatusTransition(this, that).asLeft[Status]
      }
  }

  object Status {
    case object Placed extends Status
    case object Approved extends Status
    case object Delivered extends Status

    implicit val decoder: Decoder[Status] = deriveDecoder[Status]
    implicit val encoder: Encoder[Status] = deriveEncoder[Status]
  }

  sealed abstract class Error(msg: String) extends RuntimeException(msg)

  object Error {
    case class OrderNotFound(id: PetOrder.Id)
        extends Error(s"order with id $id not found")
    case class IllegalStatusTransition(from: Status, to: Status)
        extends Error(
          s"can't transition order status from $from to $to"
        )
    case class PetNotFound(id: Pet.Id)
        extends Error(s"pet with id $id not found")

    implicit val decoder: Decoder[Error] = deriveDecoder[Error]
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Error] =
      jsonOf
    implicit val encoder: Encoder[Error] = deriveEncoder[Error]
    implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, Error] =
      jsonEncoderOf
  }

  implicit val decoder: Decoder[PetOrder] = deriveDecoder[PetOrder]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, PetOrder] =
    jsonOf
  implicit val encoder: Encoder[PetOrder] = deriveEncoder[PetOrder]
  implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, PetOrder] =
    jsonEncoderOf
}
