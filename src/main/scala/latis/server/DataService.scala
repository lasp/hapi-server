package latis.server

import cats.effect.Effect
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

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
