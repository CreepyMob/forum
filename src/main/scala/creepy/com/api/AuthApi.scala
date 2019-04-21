package creepy.com.api

import cats.effect.IO
import creepy.com.db.dao.{TokenDao, UserDao}
import creepy.com.model.User
import doobie.implicits._
import doobie.util.transactor.Transactor

trait AuthApi[F[_]] {

  def getUserByToke(token: String): F[User]

}

class AuthApiIO(val tokenDao: TokenDao,
                val userDao: UserDao,
                val xa: Transactor[IO]) extends AuthApi[IO] {

  override def getUserByToke(token: String): IO[User] = (for {
    userId <- tokenDao.getUserId(token)
    user <- userDao.getUserById(userId)
  } yield user).transact(xa)
}