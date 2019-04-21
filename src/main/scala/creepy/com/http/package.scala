package creepy.com

import java.sql.Timestamp

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.effect.IO
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, _}

package object http {

  val noSpaceDropNullValuesPrinter: Printer = Printer(
    preserveOrder = true,
    dropNullValues = true,
    indent = ""
  )

  implicit def toResponseMarshaller[A: Encoder]: ToEntityMarshaller[IO[A]] =
    Marshaller.withFixedContentType(`application/json`) { ia: IO[A] =>
      HttpEntity(`application/json`, Source.fromFuture(ia.map(a => ByteString(noSpaceDropNullValuesPrinter.pretty(a.asJson))).unsafeToFuture()))
    }

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset) => data.decodeString(charset.nioCharset.name)
      }

  implicit def fromEntityUnmarshaller[T](implicit decoder: Decoder[T]): FromEntityUnmarshaller[T] =
    jsonStringUnmarshaller.map(jawn.decode(_).fold(throw _, identity))

}

object TimestampAdapter {
  implicit val timestampEncoder: Encoder[Timestamp] = Encoder.instance[Timestamp](a => a.toString.asJson)
  implicit val timestampDecoder: Decoder[Timestamp] = Decoder.instance[Timestamp](a => a.as[String].map(Timestamp.valueOf))
}
