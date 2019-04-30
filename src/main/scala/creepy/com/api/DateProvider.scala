package creepy.com.api

import java.sql.Timestamp
import java.util.Date

import cats.effect.Sync

class DateProvider {

  def apply[F[_] : Sync]: F[Timestamp] = Sync[F].delay(new Timestamp(new Date().getTime))
}
