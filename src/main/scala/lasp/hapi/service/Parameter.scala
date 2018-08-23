package lasp.hapi.service

import io.circe.Encoder

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
