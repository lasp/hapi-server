package latis.service.hapi

import io.circe.Encoder

/**
 * Response from the `/about` endpoint.
 *
 * @param version version of HAPI
 * @param status HAPI status object
 * @param id unique ID for the server
 * @param title human-readable name for the server
 * @param contact contact info for server issues
 * @param description brief description of the type of data served
 * @param contactId ID in the discovery system for info about the contact
 * @param citation how to cite the server
 */
final case class AboutResponse(
  version: String,
  status: Status,
  id: String,
  title: String,
  contact: String,
  description: Option[String] = None,
  contactId: Option[String] = None,
  citation: Option[String] = None
)

object AboutResponse {

  /** JSON encoder */
  given encoder: Encoder[AboutResponse] =
    Encoder.forProduct8(
      "version",
      "status",
      "id",
      "title",
      "contact",
      "description",
      "contactID",
      "citation"
    ) { (ar: AboutResponse) => (
      ar.version,
      ar.status,
      ar.id,
      ar.title,
      ar.contact,
      ar.description,
      ar.contactId,
      ar.citation
    ) }.mapJson(_.dropNullValues)
}
