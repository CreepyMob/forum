package creepy.com.model

case class User(id: Long,
                nick: String)

object User {

  val ANONYMOUS = User(0, "Anonymous")
}

case class NoSuchUserException(userId: Long) extends IllegalStateException(s"Not found User with id: $userId")