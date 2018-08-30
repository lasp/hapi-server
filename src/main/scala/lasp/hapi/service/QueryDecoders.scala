package lasp.hapi.service

import java.time.LocalDateTime

import cats.implicits._
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io._

import lasp.hapi.util.Time

/** Shared query decoders and matchers. */
object QueryDecoders {

  object IdMatcher extends QueryParamDecoderMatcher[String]("id")
  object MinTimeMatcher extends ValidatingQueryParamDecoderMatcher[LocalDateTime]("time.min")
  object MaxTimeMatcher extends ValidatingQueryParamDecoderMatcher[LocalDateTime]("time.max")
  object ParamMatcher extends OptionalQueryParamDecoderMatcher[List[String]]("parameters")

  /** Decoder for simple CSV query parameters. */
  implicit def csvDecoder[A : QueryParamDecoder]: QueryParamDecoder[List[A]] =
    new QueryParamDecoder[List[A]] {
      override def decode(qpv: QueryParameterValue) =
        if (qpv.value.isEmpty) {
          ParseFailure(
            "Empty parameter list.", "Empty parameter list."
          ).invalidNel
        } else {
          qpv.value.split(',').toList.traverse { x =>
            QueryParamDecoder[A].decode(QueryParameterValue(x))
          }
        }
    }

  /** Decoder for restricted ISO 8601 time strings. */
  implicit val restrictedISO8601: QueryParamDecoder[LocalDateTime] =
    new QueryParamDecoder[LocalDateTime] {
      override def decode(qpv: QueryParameterValue) =
        Time.parse(qpv.value).toValidNel(
          ParseFailure(
            "Failed to parse time string.", "Failed to parse time string."
          )
        )
    }
}
