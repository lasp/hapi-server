package latis.service.hapi

import io.circe.Encoder
import io.circe.Json

/** Represents location of time stamp within measurement window. */
sealed trait TimeStampLocation

/** Time stamp at the beginning of the window. */
case object Begin extends TimeStampLocation

/** Time stamp at the center of the window. */
case object Center extends TimeStampLocation

/** Time stamp at the end of the window. */
case object End extends TimeStampLocation

/** Time stamp elsewhere in window or unknown. */
case object Other extends TimeStampLocation

object TimeStampLocation {

  def apply(str: String): Option[TimeStampLocation] =
    str.toLowerCase match {
      case "begin"  => Option(Begin)
      case "center" => Option(Center)
      case "end"    => Option(End)
      case "other"  => Option(Other)
      case _        => None
    }

  /** JSON encoder */
  given encoder: Encoder[TimeStampLocation] =
    Encoder.instance {
      case Begin => Json.fromString("begin")
      case Center => Json.fromString("center")
      case End => Json.fromString("end")
      case Other => Json.fromString("other")
    }
}
