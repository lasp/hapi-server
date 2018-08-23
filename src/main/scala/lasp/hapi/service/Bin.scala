package lasp.hapi.service

import io.circe.Encoder

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
