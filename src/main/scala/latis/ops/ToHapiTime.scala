package latis.ops

import cats.implicits._

import latis.data.Number
import latis.data.Sample
import latis.data.SampledFunction
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

/**
 * An operation that converts a time variable to HAPI time strings.
 *
 * Because this operation is intended to work only with datasets
 * served via the HAPI interface, it assumes that the outermost domain
 * variable is time.
 */
class ToHapiTime extends UnaryOperation {

  // Convert the units and coverage in the time metadata.
  override def applyToModel(model: DataType): DataType = model match {
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
        _ => throw new RuntimeException("Failed to apply to model"),
        Function(_, r)
      )
    case _ => throw new RuntimeException("Failed to apply to model")
  }

  // Convert the first Data in the domain (assumed to be time, because
  // this is for HAPI).
  override def applyToData(data: SampledFunction, model: DataType): SampledFunction = {
    val time: Time = model match {
      case Function(t: Time, _) => t
      case _ => throw new RuntimeException("Failed to apply to data")
    }

    if (time.timeFormat.map(_ != TimeFormat.Iso).getOrElse(true)) {
      val converter = UnitConverter(time.timeScale, TimeScale.Default)
      data.map(mkMapFunction(converter, time))
    } else data
  }

  private def mkMapFunction(converter: UnitConverter, dt: Time): Sample => Sample = {
    case Sample(Number(t) :: rest, r) =>
      val tC = converter.convert(t).toLong
      val tF = TimeFormat.formatIso(tC)

      Sample(tF :: rest, r)
    case Sample(Text(t) :: rest, r)   =>
      val fmt = dt.timeFormat.getOrElse {
        throw new RuntimeException("Failed to apply to data")
      }
      val tD = fmt.parse(t).fold(throw _, _.toDouble)
      val tC = converter.convert(tD).toLong
      val tF = TimeFormat.formatIso(tC)

      Sample(tF :: rest, r)
    case _ => throw new RuntimeException("Failed to apply to data")
  }

  private def getMetadata(dt: DataType, key: String): Either[MetadataError, String] =
    Either.fromOption(
      dt.metadata.getProperty(key),
      MetadataError(s"Missing metadata property $key")
    )
}
