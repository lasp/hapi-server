package latis.service.hapi

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._

/**
 * Represents a response from the `info` service.
 *
 * @param version HAPI version
 * @param status HAPI status code
 * @param metadata dataset metadata
 */
final case class InfoResponse(
  version: String,
  status: Status,
  metadata: Metadata
)

object InfoResponse {

  /**
   * JSON encoder
   *
   * Note that we are flattening out the `metadata` field.
   */
  implicit val encoder: Encoder[InfoResponse] =
    new Encoder[InfoResponse] {
      override def apply(x: InfoResponse): Json =
        Encoder[Metadata].apply(x.metadata).deepMerge(
          Json.obj("HAPI" -> x.version.asJson, "status" -> x.status.asJson)
        )
    }
}
