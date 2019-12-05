package latis.util

import cats.implicits._

import latis.service.hapi._
import latis.time.TimeFormat
import latis.time.TimeScale
import latis.units.UnitConverter

object HapiUtils {

  /** Convert a time value to a HAPI time given its original units. */
  def toHapiTime(fmt: String, value: String): Either[InfoError, String] = for {
    fromTime <- Either.catchNonFatal(TimeFormat(fmt))
    .flatMap(_.parse(value).map(_.toDouble))
    .orElse(Either.catchNonFatal(value.toDouble))
    .leftMap(_ => MetadataError("Unsupported time units"))
    scale     = TimeScale(fmt)
    converter = UnitConverter(scale, TimeScale.Default)
    toTime    = converter.convert(fromTime).toLong
  } yield TimeFormat.formatIso(toTime)

  /** Parse coverage metadata from FDML and convert to HAPI time. */
  def parseCoverage(coverage: String, fmt: String): Either[InfoError, (String, String)] = {
    coverage.split('/').toList match {
      case s :: e :: Nil if s.nonEmpty && e.nonEmpty => for {
        sF <- toHapiTime(fmt, s)
        eF <- toHapiTime(fmt, e)
      } yield (sF, eF)
      case _ => MetadataError("Invalid coverage").asLeft
    }
  }
}
