package latis.service.hapi

import cats.implicits._
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io._

/** Wrapper for `format` parameter. */
final case class Format(format: String) extends AnyVal

object Format {

  implicit val formatDecoder: QueryParamDecoder[Format] =
    new QueryParamDecoder[Format] {
      override def decode(qpv: QueryParameterValue) = qpv.value match {
        case fmt @ "csv" => Format(fmt).validNel
        case _ =>
          ParseFailure(
            "Invalid value for 'format' parameter",
            s"Invalid value for 'format' parameter: ${qpv.value}"
          ).invalidNel
      }
    }

  object FormatMatcher extends OptionalValidatingQueryParamDecoderMatcher[Format]("format")
}
