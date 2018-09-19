package lasp.hapi.service

import io.circe.Encoder

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
    Encoder.forProduct15(
      "HAPI", "status", "parameters", "startDate", "stopDate",
      "timeStampLocation", "cadence", "sampleStartDate", "sampleStopDate",
      "description", "resourceURL", "creationDate", "modificationDate",
      "contact", "contactID"
    ) { x =>
      (x.version, x.status, x.metadata.parameters,
        x.metadata.startDate, x.metadata.stopDate,
        x.metadata.timeStampLocation, x.metadata.cadence,
        x.metadata.sampleStartDate, x.metadata.sampleStopDate,
        x.metadata.description, x.metadata.resourceURL,
        x.metadata.creationDate, x.metadata.modificationDate,
        x.metadata.contact, x.metadata.contactID)
    }
}
