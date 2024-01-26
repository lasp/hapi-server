package latis.service.hapi

import io.circe.Encoder
import io.circe.Json

/** Represents data type of a parameter. */
sealed trait DataType

/** String data */
case object HString extends DataType

/** Double data, 8-byte IEEE 754 format */
case object HDouble extends DataType

/** Integer data, 4-byte signed, little-endian */
case object HInteger extends DataType

/** Time string data, restricted ISO 8601 */
case object HIsoTime extends DataType

object DataType {

  /** JSON encoder */
  given encoder: Encoder[DataType] =
    Encoder.instance {
      case HString  => Json.fromString("string")
      case HDouble  => Json.fromString("double")
      case HInteger => Json.fromString("integer")
      case HIsoTime => Json.fromString("isotime")
    }
}
