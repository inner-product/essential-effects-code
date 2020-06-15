package com.innerproduct.ee.petstore

import cats._
import cats.effect._
import io.circe._
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.circe._

case class Pet(
    name: String,
    category: String
)

object Pet {
  implicit val show: Show[Pet] = Show.fromToString

  case class Id(toLong: Long) extends AnyVal

  object Id {
    def unapply(str: String): Option[Id] =
      if (!str.isEmpty) scala.util.Try(Id(str.toLong)).toOption
      else None

    implicit val show: Show[Id] = Show.fromToString
    implicit val decoder: Decoder[Id] = Decoder[Long].map(Id.apply)
    implicit val encoder: Encoder[Id] = Encoder[Long].contramap(_.toLong)
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Id] =
      jsonOf[F, Long].map(Id.apply)
    implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, Id] =
      jsonEncoderOf[F, Long].contramap(_.toLong)
  }

  implicit val decoder: Decoder[Pet] = deriveDecoder[Pet]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Pet] =
    jsonOf
  implicit val encoder: Encoder[Pet] = deriveEncoder[Pet]
  implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, Pet] =
    jsonEncoderOf
}
