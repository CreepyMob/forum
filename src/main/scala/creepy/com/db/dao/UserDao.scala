package creepy.com.db.dao

import creepy.com.model.User
import doobie.free.connection.ConnectionIO

trait UserDao {

  def getUserById(id: Long): ConnectionIO[User]

  def getUserByNick(nick: String): ConnectionIO[User]

  def getAnonymousUser: ConnectionIO[User]
}
