package creepy.com.http

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives.{AuthenticationResult, complete, cookie, get, path, pathPrefix, _}
import akka.http.scaladsl.server.directives.AuthenticationResult
import akka.http.scaladsl.server.{Directive1, Route}
import cats.effect.IO
import creepy.com.api.{AuthApi, ForumApi}
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.Future




class ForumRoute(authApiIO: AuthApi[IO], forumApi: ForumApi[IO], implicit val logger: Logger[IO]) {

  val SESSION_TOKEN: String = "session_token"

  def createTopic: Route = (post & path("create") & entity(as[CreateTopic]) & withToken) { (topic, token) =>
    complete(forumApi.createTopic(token, topic))
  }

  def getAllTopics: Route = (get & path("all")) {
    complete(forumApi.allTopics())
  }

  def postMessageInTopic(topicId: Long): Route = (post & entity(as[CreateMessage]) & withToken) { (createMessage, token) =>
    complete(forumApi.postMessage(token, topicId, createMessage))
  }

  def updateMessage(messageId: Long): Route = (post & path("update") & entity(as[UpdateMessage]) & withToken) { (updateMessage, token) =>
    complete(forumApi.updateMessage(token, messageId, updateMessage))
  }

  def allTopicMessage(topicId: Long): Route = get {
    complete(forumApi.allMessage(topicId))
  }

  def updateTopic(topicId: Long): Route = (post & path("update") & entity(as[UpdateTopic]) & withToken) { (input, token) =>
    complete(forumApi.updateTopic(token, topicId, input))
  }

  def messageInTopicApi(topicId: Long): Route = path("message") {
    allTopicMessage(topicId) ~ postMessageInTopic(topicId)
  }

  def inTopicApi: Route = pathPrefix(LongNumber) { topicId =>
    updateTopic(topicId) ~ deleteTopic(topicId) ~ messageInTopicApi(topicId)
  }

  def deleteTopic(topicId: Long): Route = (post & path("delete") & withToken) { token =>
    complete(forumApi.removeTopic(token, topicId))
  }

  def deleteMessage(messageId: Long): Route = (post & path("delete") & withToken) { token =>
    complete(forumApi.deleteMessage(token, messageId))
  }

  def topicApi: Route = pathPrefix("topic") {
    getAllTopics ~ createTopic ~ inTopicApi
  }

  def messageApi: Route = pathPrefix("message" / LongNumber) { messageId =>
    deleteMessage(messageId) ~ updateMessage(messageId)
  }

  def apply(): Route = topicApi ~ messageApi ~ authApi

  def authApi: Route = pathPrefix("auth") {
    logIn() ~ register() ~ logOut()
  }

  def register(): Route = (post & path("register") & entity(as[CreateUser])) { createUser =>
    complete(authApiIO.register(createUser))
  }

  def logIn(): Route = (post & path("in")) {
    authenticateOrRejectWithChallenge(authenticator) { token =>
      setCookie(HttpCookie(SESSION_TOKEN, token)) {
        complete("Authenticated! 2")
      }
    }
  }

  def authenticator: Option[HttpCredentials] => Future[AuthenticationResult[String]] = {
    case Some(BasicHttpCredentials(username, password)) => authApiIO.logIn(username, password)
      .map(AuthenticationResult.success)
      .unsafeToFuture()
    case _ => IO(AuthenticationResult.failWithChallenge(HttpChallenges.basic("secure site"))).unsafeToFuture()
  }

  def logOut(): Route = (post & path("out") & withToken) { token =>
    deleteCookie(SESSION_TOKEN) {
      complete(authApiIO.logOut(token))
    }
  }

  def withToken: Directive1[String] = cookie(SESSION_TOKEN).map(_.value)
}

