package latis.ops

import cats.implicits._

import latis.data.Number
import latis.data.Sample
import latis.data.Text
import latis.metadata.Metadata
import latis.model._
import latis.service.hapi.InfoError
import latis.service.hapi.MetadataError
import latis.time.Time
import latis.time.TimeFormat
import latis.time.TimeScale
import latis.units.UnitConverter
import latis.util.HapiUtils
import latis.util.LatisException

/**
 * An operation that converts a time variable to HAPI time strings.
 *
 * Because this operation is intended to work only with datasets
 * served via the HAPI interface, it assumes that the outermost domain
 * variable is time.
 */
class ToHapiTime extends MapOperation {

  // Convert the units and coverage in the time metadata.
  override def applyToModel(model: DataType): Either[LatisException, DataType] = model match {
    case Function(t: Time, r) =>
      val tHapi: Either[InfoError, Time] = for {
        fmt  <- getMetadata(t, "units")
        cov  <- getMetadata(t, "coverage")
        covP <- HapiUtils.parseCoverage(cov, fmt)
      } yield {
        val md = t.metadata ++ Metadata(
          "units" -> TimeFormat.Iso.toString,
          "coverage" -> s"${covP._1}/${covP._2}"
        )
        Time(md)
      }

      tHapi.fold(
        _ => Left(LatisException("Failed to apply to model")),
        time => Right(Function(time, r))
      )
    case _ => Left(LatisException("Failed to apply to model"))
  }

  // Convert the first Data in the domain (assumed to be time, because
  // this is for HAPI).
  override def mapFunction(model: DataType): Sample => Sample = {
    val time: Time = model match {
      case Function(t: Time, _) => t
      case _ => throw new RuntimeException("Failed to create map function: domain is not time")
    }
    val converter = UnitConverter(time.timeScale, TimeScale.Default)

    if (!time.timeFormat.contains(TimeFormat.Iso)) {
      case Sample(Number(t) :: rest, r) =>
        val tC = converter.convert(t).toLong
        val tF = TimeFormat.formatIso(tC)

        Sample(tF :: rest, r)
      case Sample(Text(t) :: rest, r)   =>
        val fmt = time.timeFormat.getOrElse {
          throw new LatisException("Failed to create map function: TimeFormat not found")
        }
        val tD = fmt.parse(t).fold(throw _, _.toDouble)
        val tC = converter.convert(tD).toLong
        val tF = TimeFormat.formatIso(tC)

        Sample(tF :: rest, r)
      case _ => throw new LatisException("Failed to create map function: invalid Sample")
    } else {
      s: Sample => s
    }
  }

  private def getMetadata(dt: DataType, key: String): Either[MetadataError, String] =
    Either.fromOption(
      dt.metadata.getProperty(key),
      MetadataError(s"Missing metadata property $key")
    )
}
