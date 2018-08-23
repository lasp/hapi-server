package lasp.hapi.service

import io.circe.Encoder
import io.circe.Json

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
