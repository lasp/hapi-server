package lasp.hapi.service

/**
 * Represents a request to the `info` service.
 *
 * @param id HAPI dataset ID
 * @param parameters parameters to include in response
 */
final case class InfoRequest(
  id: String,
  parameters: Option[List[String]]
)
