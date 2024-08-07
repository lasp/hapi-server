package latis.service.hapi

import io.circe.Encoder

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
  given encoder: Encoder[Capabilities] =
    Encoder.forProduct3("HAPI", "status", "outputFormats") { x =>
      (x.version, x.status, x.formats)
    }
}
