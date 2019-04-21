package creepy.com.http

import io.circe.generic.JsonCodec

@JsonCodec
case class CreateMessage(forwardId: Option[Long],
                         body: String)

@JsonCodec
case class UpdateMessage(body: String)

@JsonCodec
case class CreateTopic(title: String,
                       initialMessage: CreateMessage,
                       description: Option[String] = None)

@JsonCodec
case class UpdateTopic(title: String,
                       description: Option[String] = None)