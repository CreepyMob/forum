package creepy.com

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import creepy.com.api._
import creepy.com.db.Database
import creepy.com.db.dao.{MessageDao, TokenDao, TopicDao, UserDao}
import creepy.com.http.ForumRoute
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger


object App extends IOApp {

  def runServer(route: Route): IO[Unit] =
    IO.fromFuture(IO {
      implicit val system: ActorSystem = ActorSystem()
      implicit val mat: ActorMaterializer = ActorMaterializer()

      Http().bindAndHandle(route, "0.0.0.0", 8080)
    })
      .flatMap(s => IO(println(s)))
      .void

  override def run(args: List[String]): IO[ExitCode] =

    for {
      config <- Config.load()
      exitCode <- Database.transactor(config).use { xa =>
        for {
          logger <- Slf4jLogger.create[IO]
          _ <- Database.initialize(xa)
          tokenDao = new TokenDao()
          tokenGenerator = new TokenGenerator
          userDao = new UserDao()
          topicDao = new TopicDao()
          messageDao = new MessageDao()
          dateProvider = new DateProvider()
          authApi = new AuthApiIO(tokenDao, userDao, tokenGenerator, dateProvider, xa)
          sessionApi = new SessionApiIO(tokenDao, userDao, xa)
          forumApi = new ForumApiImpl(sessionApi, topicDao, messageDao, dateProvider, xa, logger)
          forumHttp = new ForumRoute(authApi, forumApi, logger)
          _ <- runServer(forumHttp())
          _ <- IO.never
        } yield ExitCode.Success
      }
    } yield exitCode

}

