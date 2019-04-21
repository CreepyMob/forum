package creepy.com.model

import java.sql.Timestamp

import io.circe.generic.JsonCodec

@JsonCodec
case class Message(id: Long,
                   ownerId: Long,
                   topicId: Long,
                   forwardId: Option[Long],
                   body: String,
                   date: Timestamp)


case class OnlyMessageOwnerCanUpdateMessageException(userId: Long, messageId: Long) extends IllegalStateException(s"User with id: $userId not owner of message with id: $messageId")

case class OnlyMessageOwnerCanDeleteMessageException(userId: Long, messageId: Long) extends IllegalStateException(s"User with id: $userId not owner of message with id: $messageId")