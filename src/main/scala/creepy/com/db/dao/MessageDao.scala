package creepy.com.db.dao

import java.sql.Timestamp

import cats.implicits.{catsSyntaxOption, toFunctorOps}
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import creepy.com.http.{CreateMessage, UpdateMessage}
import creepy.com.model.Message
import doobie.free.connection.ConnectionIO
import doobie.implicits._

class MessageDao {

  def addMessage(ownerId: Long, topicId: Long, date: Timestamp, message: CreateMessage): ConnectionIO[Message] =
    sql"""
         |INSERT INTO message (owner_id, topic_id, forward_id, body, message_date)
         |VALUES ($ownerId, $topicId, ${message.forwardId}, ${message.body}, $date)
       """.stripMargin
      .update.withUniqueGeneratedKeys[Long]("id")
      .map(id =>
        Message(id, ownerId, topicId, message.forwardId, message.body, date)
      )

  def getMessage(messageId: Long): ConnectionIO[Either[Throwable, Message]] = sql"SELECT * FROM message WHERE id = $messageId".query[Message]
    .option
    .flatMap(_.liftTo[ConnectionIO](MessageNotFoundException(messageId)))
    .attempt

  def removeMessage(messageId: Long): ConnectionIO[Unit] = sql"DELETE FROM message WHERE id = $messageId"
    .update
    .run
    .void

  def updateMessage(messageId: Long, updatedMessage: UpdateMessage): ConnectionIO[Unit] =
    sql"""UPDATE message SET
         |body = ${updatedMessage.body}
         |WHERE id =  $messageId
       """.stripMargin
      .update
      .run
      .void

  def allMessage(topicId: Long, offset: Int, limit: Int): ConnectionIO[List[Message]] =
    sql"""SELECT * FROM message WHERE topic_id = $topicId
         |ORDER BY create_date OFFSET $offset LIMIT $limit
       """
      .stripMargin
      .query[Message]
      .stream
      .compile
      .toList

  def messageExist(messageId: Long): ConnectionIO[Boolean] = getMessage(messageId).flatMap {
    case Right(_) => true.pure[ConnectionIO]
    case Left(MessageNotFoundException(_)) => false.pure[ConnectionIO]
    case Left(internal) => internal.raiseError[ConnectionIO, Boolean]
  }
}

case class MessageNotFoundException(id: Long) extends IllegalStateException(s"No such message with id: $id")