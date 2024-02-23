package latis.ops

import cats.implicits.*

import latis.data.Sample
import latis.model.*
import latis.time.Time
import latis.time.TimeFormat
import latis.util.LatisException

/**
 * An operation that converts a time variable to HAPI time strings.
 *
 * Because this operation is intended to work only with datasets
 * served via the HAPI interface, it assumes that the outermost domain
 * variable is time.
 */
class ToHapiTime extends MapOperation {

  /** Defines a FormatTime operation to delegate to. */
  private lazy val formatTime = TimeFormat.fromExpression("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").map { tf =>
    FormatTime(tf)
  }.fold(throw _, identity) //should be safe

  // Delegate to the FormatTime operation. Set time variable size to support binary encoding.
  override def applyToModel(model: DataType): Either[LatisException, DataType] = {
    formatTime.applyToModel(model) match {
      case Right(Function(t: Time, range)) =>
        for {
          t <- Time.fromMetadata(t.metadata + ("size", "24"))
          f <- Function.from(t, range)
        } yield f
      case _ => LatisException(s"Unsupported model: $model").asLeft
    }
  }

  // Delegate to the FormatTime operation.
  override def mapFunction(model: DataType): Sample => Sample = formatTime.mapFunction(model)

}
