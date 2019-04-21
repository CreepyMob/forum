package creepy.com.db.dao

import cats.implicits.{catsSyntaxOption, toFunctorOps}
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import creepy.com.http.{CreateTopic, UpdateTopic}
import creepy.com.model.Topic
import doobie.free.connection.ConnectionIO
import doobie.implicits._

class TopicDao {
  def createTopic(ownerId: Long, topic: CreateTopic): ConnectionIO[Topic] =
    sql"""
         |INSERT INTO topic (owner_id, title, description)
         |VALUES ($ownerId, ${topic.title}, ${topic.description})
       """.stripMargin
      .update.withUniqueGeneratedKeys[Long]("id").map(id =>
      Topic(id, ownerId, topic.title, topic.description)
    )

  def getTopic(topicId: Long): ConnectionIO[Either[Throwable, Topic]] = sql"SELECT * FROM topic WHERE id = $topicId"
    .query[Topic]
    .option
    .flatMap(_.liftTo[ConnectionIO](TopicNotFoundException(topicId)))
    .attempt


  def removeTopic(topicId: Long): ConnectionIO[Unit] = sql"DELETE FROM topic WHERE id = $topicId"
    .update
    .run
    .void

  def topicExist(topicId: Long): ConnectionIO[Boolean] = getTopic(topicId).flatMap {
    case Right(_) => true.pure[ConnectionIO]
    case Left(TopicNotFoundException(_)) => false.pure[ConnectionIO]
    case Left(internal) => internal.raiseError[ConnectionIO, Boolean]
  }

  def updateTopic(topicId: Long, updateTopic: UpdateTopic): ConnectionIO[Unit] =
    sql"""UPDATE topic SET
         |title = ${updateTopic.title},
         |description = ${updateTopic.description}
       WHERE id =  $topicId
       """.stripMargin
      .update
      .run
      .void

  def allTopics: ConnectionIO[List[Topic]] = sql"SELECT * FROM topic"
    .query[Topic]
    .stream
    .compile
    .toList

}

case class TopicNotFoundException(id: Long) extends IllegalStateException(s"No such topic with id: $id")

