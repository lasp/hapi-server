package latis.service.hapi

import cats.implicits.*
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io.*

/** Wrapper for `format` parameter. */
sealed trait Format

object Format {
  case object Csv extends Format
  case object Binary extends Format
  case object Json extends Format

  given formatDecoder: QueryParamDecoder[Format] =
    new QueryParamDecoder[Format] {
      override def decode(qpv: QueryParameterValue) = qpv.value match {
        case "csv" => Csv.validNel
        case "binary" => Binary.validNel
        case "json" => Json.validNel
        case _ =>
          ParseFailure(
            "Invalid value for 'format' parameter",
            s"Invalid value for 'format' parameter: ${qpv.value}"
          ).invalidNel
      }
    }

  object FormatMatcher extends OptionalValidatingQueryParamDecoderMatcher[Format]("format")
}
