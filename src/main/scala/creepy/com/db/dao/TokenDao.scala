package creepy.com.db.dao

import doobie.free.connection.ConnectionIO

trait TokenDao {

  def getUserId(token: String): ConnectionIO[Long]

}


