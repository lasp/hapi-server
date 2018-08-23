package lasp.hapi.service

import cats.implicits._
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.io._

/** Shared query decoders and matchers. */
object QueryDecoders {

  object IdMatcher extends QueryParamDecoderMatcher[String]("id")
  object MinTimeMatcher extends QueryParamDecoderMatcher[String]("time.min")
  object MaxTimeMatcher extends QueryParamDecoderMatcher[String]("time.max")
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
}
