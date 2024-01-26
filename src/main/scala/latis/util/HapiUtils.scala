package latis.util

import cats.syntax.all.*

import latis.service.hapi.*
import latis.time.TimeFormat

object HapiUtils {

  /**
   * Parse temporalCoverage from Dataset metadata to be used for HAPI info.
   *
   * The coverage is expected to be "/" separated ISO 8601 times. These will
   * be converted to the form yyyy-MM-ddZ for the HAPI info response. A coverage
   * without a value after the "/" will use the current date as the end time.
   */
  def parseCoverage(coverage: String): Either[InfoError, (String, String)] = {
    coverage.split('/').toList match {
      case s :: e :: Nil if s.nonEmpty && e.nonEmpty =>
        (for {
          targetFomat <- TimeFormat.fromExpression("yyyy-MM-dd'Z'")
          startMillis <- TimeFormat.parseIso(s)
          endMillis   <- TimeFormat.parseIso(e)
          startDate    = targetFomat.format(startMillis)
          endDate      = targetFomat.format(endMillis)
        } yield (startDate, endDate)).leftMap { _ =>
          MetadataError(s"Invalid temporalCoverage: $coverage")
        }
      // Replace open end (e.g. "2000-01-01/") with current date
      case s :: Nil if s.nonEmpty && coverage.endsWith("/") =>
        (for {
          targetFomat <- TimeFormat.fromExpression("yyyy-MM-dd'Z'")
          startMillis <- TimeFormat.parseIso(s)
          endMillis    = System.currentTimeMillis() //TODO: beware side effect
          startDate    = targetFomat.format(startMillis)
          endDate      = targetFomat.format(endMillis)
        } yield (startDate, endDate)).leftMap { _ =>
          MetadataError(s"Invalid temporalCoverage: $coverage")
        }
      case _ => MetadataError(s"Invalid temporalCoverage: $coverage").asLeft
    }
  }
}
