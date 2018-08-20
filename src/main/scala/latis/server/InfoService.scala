package latis.server

import cats.effect.Effect
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/info` endpoint. */
class InfoService[F[_]: Effect] extends Http4sDsl[F] {
  import QueryDecoders._

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "info"
          :? IdMatcher(_)
          +& ParamMatcher(_) =>
        Ok("Hello from HAPI!")
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "info" :? _ =>
        BadRequest(Status.`1400`.asJson)
    }
}

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

/**
 * Represents a response from the `info` service.
 *
 * @param version HAPI version
 * @param status HAPI status code
 * @param format output format of data
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
final case class InfoResponse(
  version: String,
  status: Status,
  format: String,
  parameters: List[Parameter],
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

object InfoResponse {

  /** JSON encoder */
  implicit val encoder: Encoder[InfoResponse] =
    Encoder.forProduct16(
      "HAPI", "status", "format", "parameters", "startDate", "stopDate",
      "timeStampLocation", "cadence", "sampleStartDate", "sampleStopDate",
      "description", "resourceURL", "creationDate", "modificationDate",
      "contact", "contactID"
    ) { x =>
      (x.version, x.status, x.format, x.parameters, x.startDate, x.stopDate,
        x.timeStampLocation, x.cadence, x.sampleStartDate, x.sampleStopDate,
        x.description, x.resourceURL, x.creationDate, x.modificationDate,
        x.contact, x.contactID)
    }
}

/**
 * Represents a parameter in a HAPI dataset.
 *
 * @param name parameter's name
 * @param dType parameter's data type
 * @param length parameter's length (only for `string`s and `isotime`)
 * @param units parameter's units (must be "UTC" for `isotime`)
 * @param size parameter's dimensions (only for arrays)
 * @param fill parameter's fill value
 * @param description parameter's description
 * @param bins description of each dimension (for array parameters)
 */
final case class Parameter(
  name: String,
  dType: DataType,
  length: Option[Integer],
  units: String,
  size: Option[List[Integer]],
  fill: Option[String],
  description: Option[String],
  bins: Option[List[Bin]]
)

object Parameter {

  /** JSON encoder */
  implicit val encoder: Encoder[Parameter] =
    Encoder.forProduct8(
      "name", "type", "length", "units", "size", "fill", "description", "bins"
    ) { x =>
      (x.name, x.dType, x.length, x.units, x.size, x.fill, x.description, x.bins)
    }
}

/**
 * Represents a parameter's bin.
 *
 * @param name name of dimension
 * @param centers center of bins (only if `ranges` is not defined)
 * @param ranges boundaries of each bin (only if `centers` is not defined)
 * @param units units for bin ranges or center values
 * @param description description of bin
 */
final case class Bin(
  name: String,
  centers: Option[List[Double]],
  ranges: Option[List[(Double, Double)]],
  units: String,
  description: Option[String]
)

object Bin {

  /** JSON encoder */
  implicit val encoder: Encoder[Bin] =
    Encoder.forProduct5(
      "name", "centers", "ranges", "units", "description"
    ) { x =>
      (x.name, x.centers, x.ranges, x.units, x.description)
    }
}

/** Represents location of time stamp within measurement window. */
sealed abstract trait TimeStampLocation
/** Time stamp at the beginning of the window. */
final case object Begin extends TimeStampLocation
/** Time stamp at the center of the window. */
final case object Center extends TimeStampLocation
/** Time stamp at the end of the window. */
final case object End extends TimeStampLocation
/** Time stamp elsewhere in window or unknown. */
final case object Other extends TimeStampLocation

object TimeStampLocation {

  /** JSON encoder */
  implicit val encoder: Encoder[TimeStampLocation] =
    Encoder.instance {
      case Begin  => Json.fromString("BEGIN")
      case Center => Json.fromString("CENTER")
      case End    => Json.fromString("END")
      case Other  => Json.fromString("OTHER")
    }
}

/** Represents data type of a parameter. */
sealed abstract trait DataType
/** String data */
final case object HString extends DataType
/** Double data, 8-byte IEEE 754 format */
final case object HDouble extends DataType
/** Integer data, 4-byte signed, little-endian */
final case object HInteger extends DataType
/** Time string data, restricted ISO 8601 */
final case object HIsoTime extends DataType

object DataType {

  /** JSON encoder */
  implicit val encoder: Encoder[DataType] =
    Encoder.instance {
      case HString  => Json.fromString("string")
      case HDouble  => Json.fromString("double")
      case HInteger => Json.fromString("integer")
      case HIsoTime => Json.fromString("isotime")
    }
}
