package creepy.com

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import creepy.com.api.{DateProvider, ForumApiHelloWorld}
import creepy.com.db.Database
import creepy.com.db.dao.{MessageDaoImpl, TopicDaoImpl}
import creepy.com.http.ForumRoute


object App extends IOApp {

  def runServer(route: Route): IO[Unit] =
    IO.fromFuture(IO {
      implicit val system: ActorSystem = ActorSystem()
      implicit val mat: ActorMaterializer = ActorMaterializer()

      Http().bindAndHandle(route, "0.0.0.0", 8080)
    })
      .flatMap(s => IO(println(s)))
      .void

  def log[T](title: String, value: T) = IO.pure(println(s"$title log: $value"))

  override def run(args: List[String]): IO[ExitCode] =

    for {
      config <- Config.load()
      exitCode <- Database.transactor(config).use { xa =>
        for {
          _ <- Database.initialize(xa)
          topicDao = new TopicDaoImpl(xa)
          messageDao = new MessageDaoImpl(xa)
          dateProvider = new DateProvider()
          forumApi <- IO.pure(new ForumApiHelloWorld(topicDao, messageDao, dateProvider, xa))
          forumHttp <- IO.pure(new ForumRoute(forumApi))
          route1 <- IO.pure(forumHttp())
          _ <- runServer(route1)
          _ <- IO.never
        } yield ExitCode.Success
      }
    } yield exitCode

}

