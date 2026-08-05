package latis.ops

import cats.syntax.all.*

import latis.data.*
import latis.data.Data.*
import latis.model.*
import latis.util.LatisException

/**
 * Converts scalar values to be consistent with supported HAPI types.
 *
 * HAPI supports only doubles, 32-bit integers, and string types.
 * This will convert LaTiS types that can safely be converted. Longs
 * are an exception. Although HAPI does not support 64-bit integers,
 * they are common enough in data sources (even when an int is sufficient)
 * that this attempts to convert long values to 32-bit integers. If the
 * long value exceeds the range of an Int, a fill value will be used
 * if defined for that variable. Otherwise, an exception will be thrown.
 *
 * Datasets with other types will be excluded from the Catalog by
 * HapiService.filteredCatalog. This operation needs to be consistent
 * with that filter.
 *
 * This assumes flat datasets with no nesting.
 *
 * This is only needed for the binary output, so it is otherwise not
 * applicable.
 */
class ConvertHapiTypes extends MapOperation {

  def mapFunction(model: DataType): Sample => Sample = {
    // Note, domain can only be time and it is handled elsewhere
    case Sample(d, r) =>
      val rdata = model.getScalars.tail.zip(r).map(convertValue)
      Sample(d, RangeData(rdata))
  }

  private def convertValue(scalar: Scalar, data: Data): Data = data match {
    case v: ShortValue => IntValue(v.value.toInt)
    case v: LongValue  =>
      if (v.value > Int.MaxValue.toLong || v.value < Int.MinValue.toLong)
        scalar.fillValue.getOrElse(throw LatisException("Integer overflow"))
      else IntValue(v.value.toInt)
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
