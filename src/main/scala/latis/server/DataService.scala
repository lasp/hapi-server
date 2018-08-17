package latis.server

import cats.effect.Effect
import cats.implicits._
import org.http4s.HttpService
import org.http4s.ParseFailure
import org.http4s.QueryParamDecoder
import org.http4s.QueryParameterValue
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._

/** Implements the `/data` endpoint. */
class DataService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "data" =>
        Ok("Hello from HAPI!")
    }
}

/**
 * Represents a request for data.
 *
 * @param id HAPI dataset ID
 * @param minTime lower time bound (inclusive)
 * @param maxTime upper time bound (exclusive)
 * @param parameters list of parameter names to return
 * @param header whether to include the metadata header
 * @param format output format
 */
final case class DataRequest(
  id: String,
  minTime: String,
  maxTime: String,
  parameters: Option[List[String]],
  header: Boolean,
  format: String
)

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
}

/** Wrapper for `format` parameter. */
final case class Format(format: String) extends AnyVal

object Format {

  implicit val formatDecoder: QueryParamDecoder[Format] =
    new QueryParamDecoder[Format] {
      override def decode(qpv: QueryParameterValue) = qpv.value match {
        case fmt @ ("csv" | "binary" | "json") => Format(fmt).validNel
        case _ =>
          ParseFailure(
            "Invalid value for 'format' parameter",
            s"Invalid value for 'format' parameter: ${qpv.value}"
          ).invalidNel
      }
    }
}

object QueryDecoders {

  object IdMatcher extends QueryParamDecoderMatcher[String]("id")
  object MinTimeMatcher extends QueryParamDecoderMatcher[String]("time.min")
  object MaxTimeMatcher extends QueryParamDecoderMatcher[String]("time.max")
  object ParamMatcher extends OptionalQueryParamDecoderMatcher[List[String]]("parameters")
  object IncludeMatcher extends OptionalQueryParamDecoderMatcher[Include]("include")
  object FormatMatcher extends OptionalQueryParamDecoderMatcher[Format]("format")

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
