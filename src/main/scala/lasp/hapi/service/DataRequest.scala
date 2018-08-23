package lasp.hapi.service

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
