package lasp.hapi.service

import cats.implicits._
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io._

/** Wrapper for `include` parameter. */
final case class Include(header: Boolean) extends AnyVal

object Include {

  implicit val includeDecoder: QueryParamDecoder[Include] =
    new QueryParamDecoder[Include] {
      override def decode(qpv: QueryParameterValue) = qpv.value match {
        case "header" => Include(true).validNel
        case _ =>
          ParseFailure(
            "Invalid value for 'include' parameter",
            s"Invalid value for 'include' parameter: ${qpv.value}"
          ).invalidNel
      }
    }

  object IncludeMatcher extends OptionalQueryParamDecoderMatcher[Include]("include")
}
