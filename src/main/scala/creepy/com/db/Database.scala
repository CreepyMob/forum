package creepy.com.db

import cats.effect.{ContextShift, IO, Resource}
import cats.implicits._
import creepy.com.config.DatabaseConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway

object Database {
  def transactor(config: DatabaseConfig)(implicit contextShift: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] = for {
    ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
    te <- ExecutionContexts.cachedThreadPool[IO] // our transaction EC
    xa <- HikariTransactor.newHikariTransactor[IO](config.driver, config.url, config.user, config.password, ce, te)
  } yield xa

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
  }
}
