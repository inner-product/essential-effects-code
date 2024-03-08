package com.innerproduct.ee.resources

import cats.effect._
import com.innerproduct.ee.debug._
import scala.io.Source

object EarlyRelease extends IOApp.Simple {
  def run: IO[Unit] =
    dbConnectionResource.use { conn =>
      conn.query("SELECT * FROM users WHERE id = 12").debug()
    }.void

  val dbConnectionResource: Resource[IO, DbConnection] =
    for {
      config <- configResource
      conn <- DbConnection.make(config.connectURL)
    } yield conn

  lazy val configResource: Resource[IO, Config] = // <1>
    for {
      source <- sourceResource
      config <- Resource.eval(Config.fromSource(source)) // <2>
    } yield config

  lazy val sourceResource: Resource[IO, Source] =
    Resource.make(
      debugWithThread(s"> opening Source to config")
        *> IO(Source.fromString(config))
    )(source =>
      debugWithThread(s"< closing Source to config") *> IO(source.close)
    )

  val config = "exampleConnectURL"
}

case class Config(connectURL: String)

object Config {
  def fromSource(source: Source): IO[Config] =
    for {
      config <- IO(Config(source.getLines().next()))
      _ <- debugWithThread(s"read $config")
    } yield config
}

trait DbConnection {
  def query(sql: String): IO[String] // Why not!?
}

object DbConnection {
  def make(connectURL: String): Resource[IO, DbConnection] =
    Resource.make(
      debugWithThread(s"> opening Connection to $connectURL") *> IO(
        new DbConnection {
          def query(sql: String): IO[String] =
            IO(s"""(results for SQL "$sql")""")
        }
      )
    )(_ => debugWithThread(s"< closing Connection to $connectURL").void)
}
