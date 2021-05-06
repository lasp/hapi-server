package latis.service.hapi

import java.time.LocalDateTime

import cats.data.NonEmptyList

/**
 * Represents a request for data.
 *
 * @param dataset HAPI dataset ID
 * @param startTime lower time bound (inclusive)
 * @param stopTime upper time bound (exclusive)
 * @param parameters list of parameter names to return
 * @param header whether to include the metadata header
 * @param format output format
 */
final case class DataRequest(
  dataset: String,
  startTime: LocalDateTime,
  stopTime: LocalDateTime,
  parameters: Option[NonEmptyList[String]],
  header: Boolean,
  format: String
)
