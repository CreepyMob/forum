package creepy.com.api

import java.sql.Timestamp
import java.util.Date

import cats.Applicative
import cats.syntax.applicative._

class DateProvider {

  def apply[F[_] : Applicative]: F[Timestamp] = new Timestamp(new Date().getTime).pure
}
