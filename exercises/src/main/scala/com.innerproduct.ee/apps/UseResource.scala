package com.innerproduct.ee.apps

import cats.effect._
import cats.implicits._

object UseResource extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    myResource[IO]
      .use { r =>
        IO(println(s"$r is so cool!"))
      }
      .as(ExitCode.Success)

  def myResource[F[_]: Sync]: Resource[F, String] =
    Resource.make(
      Sync[F].delay(println("> acquiring MyResource")) *> Sync[F]
        .delay("MyResource")
    )(r => Sync[F].delay(println(s"< releasing $r")))
}
