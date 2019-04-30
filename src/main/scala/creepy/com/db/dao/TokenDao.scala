package creepy.com.db.dao

import java.sql.Timestamp

import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._

class TokenDao {

  def removeToken(token: String): ConnectionIO[Unit] = sql"DELETE FROM session_token WHERE token = $token"
    .update
    .run
    .void

  def getUserId(token: String): ConnectionIO[Long] = sql"""SELECT user_id FROM session_token WHERE token = $token""".stripMargin
    .query[Long]
    .option
    .flatMap(_.liftTo[ConnectionIO](NoSuchTokenException(token)))

  def insert(token: String, userId: Long, date: Timestamp): ConnectionIO[Unit] =
    sql"""INSERT INTO session_token (token, user_id, last_update_time) VALUES
         |($token,$userId,$date)"""
      .stripMargin
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .void
}

case class NoSuchTokenException(token: String) extends IllegalStateException(s"token: $token")

