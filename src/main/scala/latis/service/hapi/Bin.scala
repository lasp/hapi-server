package latis.service.hapi

import io.circe.Encoder
import io.circe.generic.semiauto._

/**
 * Represents a parameter's bin.
 *
 * At least one of `centers` or `ranges` must be given.
 *
 * @param name name of dimension
 * @param centers center of bins
 * @param ranges boundaries of each bin
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
    deriveEncoder[Bin].mapJsonObject { obj =>
      obj.filter {
        case ("centers", _) =>
          // Keep "centers" regardless of its value if "ranges" is
          // null or missing.
          obj("ranges").map(_.isNull).getOrElse(true)
        case (_, v)         =>
          // Remove all other null fields.
          !v.isNull
      }
    }
}
