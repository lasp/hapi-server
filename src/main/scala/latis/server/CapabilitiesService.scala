package latis.server

import cats.effect.Effect
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/capabilities` endpoint. */
class CapabilitiesService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "capabilities" =>
        Ok(
          Capabilities(
            HapiServer.version,
            Status.`1200`,
            List("binary", "csv", "json")
          ).asJson
        )
    }
}

/**
 * Response from the `/capabilities` endpoint.
 *
 * @param version version of HAPI
 * @param status HAPI status object
 * @param formats list of supported formats
 */
final case class Capabilities(
  version: String,
  status: Status,
  formats: List[String]
)

object Capabilities {

  /** JSON encoder */
  implicit val encoder: Encoder[Capabilities] =
    Encoder.forProduct3("HAPI", "status", "outputFormats") { x =>
      (x.version, x.status, x.formats)
    }
}
