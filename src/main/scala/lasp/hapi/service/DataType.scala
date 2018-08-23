package lasp.hapi.service

import io.circe.Encoder
import io.circe.Json

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
