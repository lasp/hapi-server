package latis.service.hapi

import java.time.LocalDateTime

import cats.data.NonEmptyList
import cats.implicits._
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io._

/** Shared query decoders and matchers. */
object QueryDecoders {

  object IdMatcher extends QueryParamDecoderMatcher[String]("id")
  object MinTimeMatcher extends ValidatingQueryParamDecoderMatcher[LocalDateTime]("time.min")
  object MaxTimeMatcher extends ValidatingQueryParamDecoderMatcher[LocalDateTime]("time.max")
  object ParamMatcher extends OptionalQueryParamDecoderMatcher[NonEmptyList[String]]("parameters")

  /** Decoder for non-empty simple CSV query parameters. */
  implicit def csvDecoder[A : QueryParamDecoder]: QueryParamDecoder[NonEmptyList[A]] =
    new QueryParamDecoder[NonEmptyList[A]] {
      override def decode(qpv: QueryParameterValue) =
        if (qpv.value.isEmpty) {
          ParseFailure(
            "Empty parameter list.", "Empty parameter list."
          ).invalidNel
        } else {
          // The '-1' means we get empty strings where there are empty
          // fields rather than ignoring them. The call to 'split'
          // will always return a non-empty list, even for empty
          // inputs, so '.get' is safe.
          qpv.value.split(",", -1).toList.toNel.get.traverse { x =>
            if (x.nonEmpty) {
              QueryParamDecoder[A].decode(QueryParameterValue(x))
            } else {
              ParseFailure("Empty field.", "Empty field.").invalidNel
            }
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
