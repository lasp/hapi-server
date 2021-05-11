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
  description: String = "",
  contactId: String = "",
  citation: String = ""
)

object AboutResponse {

  /** JSON encoder */
  implicit val encoder: Encoder[AboutResponse] =
    //TODO: don't include empty optional params
    Encoder.forProduct8(
      "version",
      "status",
      "id",
      "title",
      "contact",
      "description",
      "contactID",
      "citation"
    ) { x => (
          x.version,
          x.status,
          x.id,
          x.title,
          x.contact,
          x.description,
          x.contactId,
          x.citation
        )
      }
}
