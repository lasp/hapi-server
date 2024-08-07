package latis.service.hapi

import io.circe.Encoder
import io.circe.JsonObject
import io.circe.generic.semiauto.*

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
  length: Option[Int],
  units: Option[String],
  size: Option[List[Int]],
  fill: Option[String],
  description: Option[String],
  bins: Option[List[Bin]]
)

object Parameter {

  /**
   * JSON encoder
   *
   * This encoder will drop parameters that are None except for
   * units and fill, which are always required.
   */
  given encoder: Encoder[Parameter] =
    deriveEncoder[Parameter].mapJsonObject { obj =>
      JsonObject.fromIterable {
        obj.toList.filter {
          case ("units", _) => true
          case ("fill", _)  => true
          case (_, v)       => !v.isNull
        }.map {
          case ("dType", v) => ("type", v)
          case x            => x
        }
      }
    }
}
