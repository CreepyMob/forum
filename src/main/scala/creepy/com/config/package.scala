package creepy.com

import cats.effect.IO
import creepy.com.config.DatabaseConfig

package object config {

  case class DatabaseConfig(driver: String, url: String, user: String, password: String)

}


object Config {

  def load(): IO[DatabaseConfig] = IO.pure(DatabaseConfig(
    "org.postgresql.Driver",
    "jdbc:postgresql:forum",
    "postgres",
    "q12345"
  ))
}