package creepy.com.http

import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import cats.effect.IO
import creepy.com.api.ForumApi
import io.chrisdavenport.log4cats.Logger

class ForumRoute(forumApi: ForumApi[IO], implicit val logger: Logger[IO]) {

  def getAllTopics: Route = (get & path("all")) {
    complete(forumApi.allTopics())
  }

  def createTopic: Route = (post & path("create") & entity(as[CreateTopic])) { input =>
    complete(forumApi.createTopic("", input))
  }

  def postMessageInTopic(topicId: Long): Route = (post & entity(as[CreateMessage])) { createMessage =>
    complete(forumApi.postMessage("", topicId, createMessage))
  }

  def updateMessage(messageId: Long): Route = (post & path("update") & entity(as[UpdateMessage])) { updateMessage =>
    complete(forumApi.updateMessage("", messageId, updateMessage))
  }

  def allTopicMessage(topicId: Long): Route = get {
    complete(forumApi.allMessage("", topicId))
  }

  def messageInTopicApi(topicId: Long): Route = path("message") {
    allTopicMessage(topicId) ~ postMessageInTopic(topicId)
  }

  def inTopicApi: Route = pathPrefix(LongNumber) { topicId =>
    updateTopic(topicId) ~ deleteTopic(topicId) ~ messageInTopicApi(topicId)
  }

  def updateTopic(topicId: Long): Route = (post & path("update") & entity(as[UpdateTopic])) { input =>
    complete(forumApi.updateTopic("", topicId, input))
  }

  def deleteTopic(topicId: Long): Route = (post & path("delete")) {
    complete(forumApi.removeTopic("", topicId))
  }

  def topicApi: Route = pathPrefix("topic") {
    getAllTopics ~ createTopic ~ inTopicApi
  }

  def messageApi: Route = pathPrefix("message" / LongNumber) { messageId =>
    deleteMessage(messageId) ~ updateMessage(messageId)
  }

  def deleteMessage(messageId: Long): Route = (post & path("delete")) {
    complete(forumApi.deleteMessage("", messageId))
  }

  def apply(): Route = topicApi ~ messageApi
}
