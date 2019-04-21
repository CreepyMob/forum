package creepy.com.api

import cats.effect.IO
import cats.implicits.{toFunctorOps, _}
import creepy.com.db.dao.{MessageDao, TopicDao}
import creepy.com.http.{CreateMessage, CreateTopic, UpdateMessage, UpdateTopic}
import creepy.com.model._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

trait ForumApi[F[_]] {

  def createTopic(initiatorToken: String, createTopic: CreateTopic): F[Unit]

  def removeTopic(initiatorToken: String, topicId: Long): F[Unit]

  def updateTopic(initiatorToken: String, topicId: Long, updateTopic: UpdateTopic): F[Unit]

  def allTopics(): F[List[Topic]]

  def allMessage(initiatorToken: String, topicId: Long): F[List[Message]]

  def postMessage(initiatorToken: String, topicId: Long, message: CreateMessage): F[Unit]

  def updateMessage(initiatorToken: String, messageId: Long, updatedMessage: UpdateMessage): F[Unit]

  def deleteMessage(initiatorToken: String, messageId: Long): F[Unit]

}

class ForumApiImpl(topicDao: TopicDao, messageDao: MessageDao, dateProvider: DateProvider, xa: Transactor[IO]) extends ForumApi[IO] {
  override def createTopic(initiatorToken: String, createTopic: CreateTopic): IO[Unit] = (for {
    date <- dateProvider.apply[ConnectionIO]
    topic <- topicDao.createTopic(0, createTopic)
    _ <- messageDao.addMessage(0, topic.id, date, createTopic.initialMessage)
  } yield ()).transact[IO](xa)

  override def removeTopic(initiatorToken: String, topicId: Long): IO[Unit] = topicDao.removeTopic(topicId).transact[IO](xa)

  override def updateTopic(initiatorToken: String, topicId: Long, updateTopic: UpdateTopic): IO[Unit] = topicDao.updateTopic(topicId, updateTopic).transact[IO](xa)

  override def allTopics(): IO[List[Topic]] = topicDao.allTopics.transact[IO](xa)

  override def postMessage(initiatorToken: String, topicId: Long, message: CreateMessage): IO[Unit] = (for {
    date <- dateProvider.apply[ConnectionIO]
    eitherTopic <- topicDao.getTopic(topicId)
    topic <- eitherTopic.liftTo[ConnectionIO]
    _ <- messageDao.addMessage(0, topic.id, date, message)
  } yield ()).transact[IO](xa)

  override def updateMessage(initiatorToken: String, messageId: Long, updatedMessage: UpdateMessage): IO[Unit] = messageDao.updateMessage(messageId, updatedMessage).transact[IO](xa).void

  override def deleteMessage(initiatorToken: String, messageId: Long): IO[Unit] = messageDao.removeMessage(messageId).transact[IO](xa).void

  override def allMessage(initiatorToken: String, topicId: Long): IO[List[Message]] = messageDao.allMessage(topicId).transact[IO](xa)
}