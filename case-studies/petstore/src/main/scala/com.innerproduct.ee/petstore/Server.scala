package com.innerproduct.ee.petstore

import cats._
import cats.arrow.FunctionK
import cats.effect._
import cats.implicits._
import cats.tagless._
import cats.tagless.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object Server extends IOApp {
  
  def run(args: List[String]) =
    runR.use(_ => IO.never)

  def runR: Resource[IO, Unit] =
    for {
      rs <- routes
      _ <- server(Router("/" -> rs).orNotFound)
    } yield ()

  def server(app: HttpApp[IO]): Resource[IO, org.http4s.server.Server[IO]] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(8080, "localhost")
      .withHttpApp(app)
      .resource

  def routes: Resource[IO, HttpRoutes[IO]] =
    for {
      pets <- ServerResources
        .pets[IO]
        .map(addExtraLatency(100.millis))
      orderRepo <- ServerResources.orderRepo[IO]
      orders = addExtraLatency(100.millis)(ServerResources.orders(pets, orderRepo))
    } yield Routes.pets[IO](pets) <+> Routes.orders[IO](orders)

  def addExtraLatency[Alg[_[_]]: FunctorK, F[_]: Monad: Timer](
      extraLatency: FiniteDuration
  )(fa: Alg[F]): Alg[F] =
    fa.mapK(λ[FunctionK[F, F]](Timer[F].sleep(extraLatency) >> _))
}
