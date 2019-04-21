package creepy.com.model

import io.circe.generic.JsonCodec

@JsonCodec
case class Topic(id: Long,
                 ownerId: Long,
                 title: String,
                 description: Option[String] = None)


case class NoSuchTopicException(topicId: Long) extends IllegalStateException(s"Not found topic with id: $topicId")
