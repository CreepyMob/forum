package creepy.com.api

import cats.effect.IO
import creepy.com.db.dao.{TokenDao, UserDao}
import creepy.com.model.User
import doobie.implicits._
import doobie.util.transactor.Transactor

trait SessionApi[F[_]] {

  def getUserIdForToken(token: String): F[Long]

  def getUserForToken(token: String): F[User]
}

class SessionApiIO(tokenDao: TokenDao, userDao: UserDao, xa: Transactor[IO]) extends SessionApi[IO] {

  def getUserIdForToken(token: String): IO[Long] = tokenDao.getUserId(token).transact(xa)

  def getUserForToken(token: String): IO[User] = for {
    userId <- tokenDao.getUserId(token).transact(xa)
    user <- userDao.getUserById(userId).transact(xa)
  } yield user
}