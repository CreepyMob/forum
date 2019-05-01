package creepy.com.api

import cats.implicits.catsSyntaxApplicativeErrorId
import cats.{Applicative, MonadError}

class Guard {

  def conditionOrError[F[_] : MonadError[?[_], Throwable]](condition: => Boolean, error: => IllegalStateException): F[Unit] = if (condition) {
    Applicative[F].pure(Unit)
  } else {
    error.raiseError[F, Unit]
  }

}
