package lasp.hapi.service

import cats.data.NonEmptyList
import io.circe.Encoder
import io.circe.generic.semiauto._

/**
 * Represents metadata for HAPI datasets.
 *
 * @param parameters selected parameters in dataset
 * @param startDate time of first sample in dataset (restricted ISO 8601)
 * @param stopDate time of last sample in dataset (restricted ISO 8601)
 * @param timeStampLocation location of time stamp within measurement window
 * @param cadence time cadence (ISO 8601 duration)
 * @param sampleStartDate time of first sample in example subset (restricted ISO 8601)
 * @param sampleStopDate time of last sample in example subset (restricted ISO 8601)
 * @param description description of dataset
 * @param resourceURL URL with more information about dataset
 * @param creationDate time of dataset creation (restricted ISO 8601)
 * @param modificationDate time of last modification (restricted ISO 8601)
 * @param contact information about contact person
 * @param contactID ID in discovery system for contact person
 */
final case class Metadata(
  parameters: NonEmptyList[Parameter],
  startDate: String,
  stopDate: String,
  timeStampLocation: Option[TimeStampLocation],
  cadence: Option[String],
  sampleStartDate: Option[String],
  sampleStopDate: Option[String],
  description: Option[String],
  resourceURL: Option[String],
  creationDate: Option[String],
  modificationDate: Option[String],
  contact: Option[String],
  contactID: Option[String]
)

object Metadata {

  /**
   * JSON encoder
   *
   * This encoder will drop parameters that are None.
   */
  implicit val encoder: Encoder[Metadata] =
    deriveEncoder[Metadata].mapJsonObject {
      _.filter {
        case (_, v) => !v.isNull
      }
    }
}
