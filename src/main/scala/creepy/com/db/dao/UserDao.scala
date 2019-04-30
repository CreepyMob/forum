package creepy.com.db.dao

import creepy.com.http.CreateUser
import creepy.com.model.User
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import cats.implicits._

class UserDao {

  def userWithNickNotExist(nick: String): ConnectionIO[Unit] = sql"""SELECT id FROM users WHERE nick = $nick""".stripMargin
    .query[Long]
    .option
    .flatMap(_.liftTo[ConnectionIO](UserWithNickExist(nick)))
    .void

  def userWithEmailNotExist(email: String): ConnectionIO[Unit] = sql"""SELECT id FROM users WHERE email = $email""".stripMargin
    .query[Long]
    .option
    .flatMap(_.liftTo[ConnectionIO](UserWithEmailExist(email)))
    .void

  def create(createUser: CreateUser): ConnectionIO[User] =
    sql"""INSERT INTO users (email, nick, password) VALUES (
         |    ${createUser.email},
         |    ${createUser.nick},
         |  crypt('${createUser.password}', gen_salt('bf'))
         |  )"""
      .update.withUniqueGeneratedKeys[Long]("id")
      .map(id =>
        User(id, createUser.nick, createUser.email)
      )

  def getUserBy(nickOrEmail: String, password: String): ConnectionIO[Long] =
    sql"""SELECT id
         |  FROM users
         | WHERE (email = $nickOrEmail
         |   AND password = crypt($password, password)) OR
         |  (nick = $nickOrEmail
         |   AND password = crypt($password, password))
       """.stripMargin
      .query[Long]
      .option
      .flatMap(_.liftTo[ConnectionIO](NoUserWithNickOrEmailToPasswordExist(nickOrEmail, password)))


  def getUserById(id: Long): ConnectionIO[User] = sql"""SELECT (id, email, nick) FROM users WHERE id = $id""".stripMargin
    .query[User]
    .option
    .flatMap(_.liftTo[ConnectionIO](NotFoundUserWithId(id)))
}

case class UserWithNickExist(nick: String) extends IllegalStateException(s"user with nick: $nick exist")

case class UserWithEmailExist(email: String) extends IllegalStateException(s"user with email: $email exist")

case class NoUserWithNickOrEmailToPasswordExist(nickOrEmail: String, password: String) extends IllegalStateException(s"no user with nick or email: $nickOrEmail or password: $password")

case class NotFoundUserWithId(id: Long) extends IllegalStateException(s"no user with id: $id")
