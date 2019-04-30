package creepy.com.api

import cats.effect.IO
import creepy.com.db.dao.{TokenDao, UserDao}
import creepy.com.http.CreateUser
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

trait AuthApi[F[_]] {

  def logOut(token: String): F[Unit]

  def logIn(nickOrEmail: String, password: String): F[String]

  def register(createUser: CreateUser): F[Unit]
}

class AuthApiIO(val tokenDao: TokenDao,
                val userDao: UserDao,
                val tokenGenerator: TokenGenerator,
                val dateProvider: DateProvider,
                val xa: Transactor[IO]) extends AuthApi[IO] {

  override def logIn(nickOrEmail: String, password: String): IO[String] = (for {
    userId <- userDao.getUserBy(nickOrEmail, password)
    token <- tokenGenerator.generate[ConnectionIO]()
    date <- dateProvider.apply[ConnectionIO]
    _ <- tokenDao.insert(token, userId, date)
  } yield token).transact(xa)

  override def register(createUser: CreateUser): IO[Unit] = (for {
    _ <- userDao.userWithEmailNotExist(createUser.email)
    _ <- userDao.userWithNickNotExist(createUser.nick)
    _ <- userDao.create(createUser)
  } yield ()).transact(xa)

  override def logOut(token: String): IO[Unit] = tokenDao
    .removeToken(token)
    .transact(xa)
}