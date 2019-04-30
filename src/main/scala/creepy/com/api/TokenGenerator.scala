package creepy.com.api

import java.security.SecureRandom

import cats.effect.Sync

class TokenGenerator {

  val TOKEN_LENGTH = 32
  val TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._"

  val secureRandom: SecureRandom = new SecureRandom()

  def generate[F[_] : Sync](): F[String] = Sync[F].delay(Stream.continually(TOKEN_CHARS(secureRandom.nextInt(TOKEN_CHARS.length))).take(TOKEN_LENGTH).mkString)
}
