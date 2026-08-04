package latis.ops

import cats.syntax.all.*

import latis.data.*
import latis.data.Data.*
import latis.model.*
import latis.util.LatisException

/**
 * Converts scalar values to be consistent with supported HAPI types.
 *
 * HAPI supports only double, int, and string types. This will convert
 * some types that can safely be converted. Longs are an exception.
 * Although HAPI does not support 64-bit integers, they are common enough
 * in data sources that they are converted to 32-bit integers here at
 * the risk of integer overflow (wrapped to negative numbers, not an error).
 * Datasets with other types will be excluded from the Catalog by
 * HapiService.filteredCatalog. This operation needs to be consistent
 * with that filter.
 *
 * This assumes flat datasets with no nesting.
 *
 * This is only needed for the binary output.
 */
class ConvertHapiTypes extends MapOperation {

  def mapFunction(model: DataType): Sample => Sample = {
    // Note, domain can only be time and it is handled elsewhere
    case Sample(d, r) => Sample(d, RangeData(r.map(convertValue)))
  }

  private def convertValue(data: Data): Data = data match {
    case v: ShortValue => IntValue(v.value.toInt)
    case v: LongValue  => IntValue(v.value.toInt) //risk of overflow but no exception
    case v: FloatValue => DoubleValue(v.value.toDouble)
    case _             => data //no-op, shouldn't get here due to catalog filter
  }

  def applyToModel(model: DataType): Either[LatisException, DataType] = model.map {
    case s: Scalar => convertValueType(s)
    case dt => dt
  }.asRight

  private def convertValueType(scalar: Scalar): Scalar = scalar.valueType match {
    case FloatValueType =>
      Scalar.fromMetadata(
        scalar.metadata + ("type" -> "double")
      ).fold(throw _, identity) //should not fail
    case ShortValueType =>
      Scalar.fromMetadata(
        scalar.metadata + ("type" -> "int")
      ).fold(throw _, identity) //should not fail
    case LongValueType =>
      Scalar.fromMetadata(
        scalar.metadata + ("type" -> "int")
      ).fold(throw _, identity) //should not fail
    case _ => scalar //no-op, shouldn't get here due to catalog filter
  }
}
