package latis.service.hapi

import cats.implicits._
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io._

/** Wrapper for `format` parameter. */
sealed trait Format {
  val format = ""
}

object Format {
  final case object Csv extends Format {
    override val format = "csv"
  }
  final case object Binary extends Format {
    override val format = "binary"
  }
  final case object Json extends Format {
    override val format = "json"
  }

  implicit val formatDecoder: QueryParamDecoder[Format] =
    new QueryParamDecoder[Format] {
      override def decode(qpv: QueryParameterValue) = qpv.value match {
        case "csv" => Csv.validNel
        case _ =>
          ParseFailure(
            "Invalid value for 'format' parameter",
            s"Invalid value for 'format' parameter: ${qpv.value}"
          ).invalidNel
      }
    }

  object FormatMatcher extends OptionalValidatingQueryParamDecoderMatcher[Format]("format")
}
