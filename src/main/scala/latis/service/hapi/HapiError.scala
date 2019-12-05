package latis.service.hapi

import io.circe.Encoder

/**
 * Represents an error response from HAPI.
 *
 * @param version HAPI version
 * @param status HAPI status code
 */
final case class HapiError(version: String, status: Status)

object HapiError {

  def apply(status: Status): HapiError =
    HapiError(HapiService.version, status)

  /** JSON encoder */
  implicit val encoder: Encoder[HapiError] =
    Encoder.forProduct2("HAPI", "status") { x =>
      (x.version, x.status)
    }
}
