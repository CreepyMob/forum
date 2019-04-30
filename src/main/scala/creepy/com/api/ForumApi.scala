package creepy.com.api

import cats.effect.IO
import cats.implicits._
import creepy.com.db.dao.{MessageDao, TopicDao}
import creepy.com.http.{CreateMessage, CreateTopic, UpdateMessage, UpdateTopic}
import creepy.com.model._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger

trait ForumApi[F[_]] {

  def createTopic(initiatorToken: String, createTopic: CreateTopic): F[Unit]

  def removeTopic(initiatorToken: String, topicId: Long): F[Unit]

  def updateTopic(initiatorToken: String, topicId: Long, updateTopic: UpdateTopic): F[Unit]

  def allTopics(): F[List[Topic]]

  def allMessage(topicId: Long): F[List[Message]]

  def postMessage(initiatorToken: String, topicId: Long, message: CreateMessage): F[Unit]

  def updateMessage(initiatorToken: String, messageId: Long, updatedMessage: UpdateMessage): F[Unit]

  def deleteMessage(initiatorToken: String, messageId: Long): F[Unit]

}

class ForumApiImpl(sessionApi: SessionApi[IO],
                   topicDao: TopicDao,
                   messageDao: MessageDao,
                   dateProvider: DateProvider,
                   xa: Transactor[IO],
                   logger: Logger[IO]) extends ForumApi[IO] {

  override def createTopic(initiatorToken: String, createTopic: CreateTopic): IO[Unit] = for {
    userId <- sessionApi.getUserIdForToken(initiatorToken)
    _ <- createTopicTransaction(userId, createTopic).transact(xa)
  } yield ()

  private def createTopicTransaction(ownerId: Long, createTopic: CreateTopic): ConnectionIO[Unit] = for {
    date <- dateProvider.apply[ConnectionIO]
    topic <- topicDao.createTopic(ownerId, createTopic)
    _ <- messageDao.addMessage(ownerId, topic.id, date, createTopic.initialMessage)
  } yield ()

  override def removeTopic(initiatorToken: String, topicId: Long): IO[Unit] = for {
    userId <- sessionApi.getUserIdForToken(initiatorToken)
    _ <- removeTopicTransaction(userId, topicId).transact[IO](xa)
  } yield ()

  def removeTopicTransaction(initiatorId: Long, topicId: Long): ConnectionIO[Unit] = for {
    originEitherTopic <- topicDao.getTopic(topicId)
    originTopic <- originEitherTopic.liftTo[ConnectionIO]
    _ <- if (originTopic.ownerId == topicId) {
      topicDao.removeTopic(topicId)
    } else {
      OnlyTopicOwnerCanRemoveTopic(initiatorId, originTopic.id).raiseError[ConnectionIO, Unit]
    }
  } yield ()

  override def updateTopic(initiatorToken: String, topicId: Long, updateTopic: UpdateTopic): IO[Unit] = for {
    userId <- sessionApi.getUserIdForToken(initiatorToken)
    _ <- updateTopicTransaction(userId, topicId, updateTopic).transact[IO](xa)
  } yield ()

  def updateTopicTransaction(initiatorId: Long, topicId: Long, updateTopic: UpdateTopic): ConnectionIO[Unit] = for {
    originEitherTopic <- topicDao.getTopic(topicId)
    originTopic <- originEitherTopic.liftTo[ConnectionIO]
    _ <- if (originTopic.ownerId == topicId) {
      topicDao.updateTopic(topicId, updateTopic)
    } else {
      OnlyTopicOwnerCanUpdateTopic(initiatorId, originTopic.id).raiseError[ConnectionIO, Unit]
    }
  } yield ()

  override def allTopics(): IO[List[Topic]] = topicDao.allTopics.transact[IO](xa)

  override def postMessage(initiatorToken: String, topicId: Long, message: CreateMessage): IO[Unit] = for {
    initiatorId <- sessionApi.getUserIdForToken(initiatorToken)
    _ <- postMessageTransaction(initiatorId, topicId, message).transact(xa)
  } yield ()

  def postMessageTransaction(initiatorId: Long, topicId: Long, message: CreateMessage): ConnectionIO[Unit] = for {
    date <- dateProvider.apply[ConnectionIO]
    eitherTopic <- topicDao.getTopic(topicId)
    topic <- eitherTopic.liftTo[ConnectionIO]
    _ <- messageDao.addMessage(initiatorId, topic.id, date, message)
  } yield ()

  override def updateMessage(initiatorToken: String, messageId: Long, updatedMessage: UpdateMessage): IO[Unit] = for {
    userId <- sessionApi.getUserIdForToken(initiatorToken)
    _ <- updateMessageTransaction(userId, messageId, updatedMessage).transact[IO](xa)
  } yield ()

  def updateMessageTransaction(initiatorId: Long, messageId: Long, updatedMessage: UpdateMessage): ConnectionIO[Unit] = for {
    originMessageEither <- messageDao.getMessage(messageId)
    originMessage <- originMessageEither.liftTo[ConnectionIO]
    _ <- if (originMessage.ownerId == initiatorId) {
      messageDao.updateMessage(messageId, updatedMessage)
    } else {
      OnlyMessageOwnerCanUpdateMessage(initiatorId, originMessage.id).raiseError[ConnectionIO, Unit]
    }
  } yield ()

  override def deleteMessage(initiatorToken: String, messageId: Long): IO[Unit] = for {
    initiatorId <- sessionApi.getUserIdForToken(initiatorToken)
    _ <- deleteMessageTransaction(initiatorId, messageId).transact[IO](xa)
  } yield ()

  def deleteMessageTransaction(initiatorId: Long, messageId: Long): ConnectionIO[Unit] = for {
    originMessageEither <- messageDao.getMessage(messageId)
    originMessage <- originMessageEither.liftTo[ConnectionIO]
    _ <- if (originMessage.ownerId == initiatorId) {
      messageDao.removeMessage(messageId)
    } else {
      OnlyMessageOwnerCanRemoveMessage(initiatorId, originMessage.id).raiseError[ConnectionIO, Unit]
    }
  } yield ()

  override def allMessage(topicId: Long): IO[List[Message]] = messageDao.allMessage(topicId).transact[IO](xa)
}

case class OnlyTopicOwnerCanUpdateTopic(initiatorId: Long, topicId: Long) extends IllegalStateException(s"Current user: $initiatorId not owner of topic: $topicId and can't update it")

case class OnlyTopicOwnerCanRemoveTopic(initiatorId: Long, topicId: Long) extends IllegalStateException(s"Current user: $initiatorId not owner of topic: $topicId and can't remove it")

case class OnlyMessageOwnerCanUpdateMessage(initiatorId: Long, messageId: Long) extends IllegalStateException(s"Current user: $initiatorId not owner of message: $messageId and can't update it")

case class OnlyMessageOwnerCanRemoveMessage(initiatorId: Long, messageId: Long) extends IllegalStateException(s"Current user: $initiatorId not owner of message: $messageId and can't remove it")