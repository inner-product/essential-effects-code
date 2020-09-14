package com.innerproduct.ee.apps

import cats.effect._
import com.innerproduct.ee.debug._
import scala.io.Source

object EarlyRelease extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    dbConnectionResource
      .use { conn =>
        conn.query("SELECT * FROM users WHERE id = 12").debug()
      }
      .as(ExitCode.Success)

  val dbConnectionResource: Resource[IO, DbConnection] =
    for {
      config <- configResource
      conn <- DbConnection.make(config.connectURL)
    } yield conn

  lazy val configResource: Resource[IO, Config] = // <1>
    for {
      source <- sourceResource
      config <- Resource.liftF(Config.fromSource(source)) // <2>
    } yield config

  lazy val sourceResource: Resource[IO, Source] =
    Resource.make(
      IO(s"> opening Source to config")
        .debug() *> IO(Source.fromString(config))
    )(source => IO(s"< closing Source to config").debug() *> IO(source.close))

  val config = "exampleConnectURL"
}

case class Config(connectURL: String)

object Config {
  def fromSource(source: Source): IO[Config] =
    for {
      config <- IO(Config(source.getLines().next))
      _ <- IO(s"read $config").debug()
    } yield config
}

trait DbConnection {
  def query(sql: String): IO[String] // Why not!?
}

object DbConnection {
  def make(connectURL: String): Resource[IO, DbConnection] =
    Resource.make(
      IO(s"> opening Connection to $connectURL").debug() *> IO(
        new DbConnection {
          def query(sql: String): IO[String] =
            IO(s"""(results for SQL "$sql")""")
        }
      )
    )(_ => IO(s"< closing Connection to $connectURL").debug().void)
}
