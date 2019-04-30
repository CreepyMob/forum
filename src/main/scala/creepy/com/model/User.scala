package creepy.com.model

case class User(id: Long,
                nick: String,
                email: String)

case class NoSuchUserException(userId: Long) extends IllegalStateException(s"Not found User with id: $userId")