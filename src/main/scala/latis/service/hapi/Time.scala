package latis.service.hapi

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.DAY_OF_YEAR
import java.time.temporal.ChronoField.HOUR_OF_DAY
import java.time.temporal.ChronoField.MINUTE_OF_HOUR
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.NANO_OF_SECOND
import java.time.temporal.ChronoField.SECOND_OF_MINUTE

/** Time utilties. */
object Time {

  /**
   * Parses a HAPI time string.
   *
   * @param str HAPI time string
   */
  def parse(str: String): Option[LocalDateTime] =
    tryParse(str, format) orElse tryParse(str, formatOrd)

  private def tryParse(str: String, fmt: DateTimeFormatter): Option[LocalDateTime] =
    try {
      Option(LocalDateTime.parse(str, fmt))
    } catch {
      case _: DateTimeParseException => None
    }

  /** Format for normal date/time strings. */
  val format: DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .appendPattern("yyyy[-MM[-dd['T'HH[:mm[:ss[.SSS]]]]]]['Z']")
      .parseDefaulting(MONTH_OF_YEAR, 1)
      .parseDefaulting(DAY_OF_MONTH, 1)
      .parseDefaulting(HOUR_OF_DAY, 0)
      .parseDefaulting(MINUTE_OF_HOUR, 0)
      .parseDefaulting(SECOND_OF_MINUTE, 0)
      .parseDefaulting(NANO_OF_SECOND, 0)
      .toFormatter()

  /** Format as `SimpleDateFormat` string. */
  val formatSDF: String =
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  /** Format for ordinal date/time strings. */
  val formatOrd: DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .appendPattern("yyyy[-DDD['T'HH[:mm[:ss[.SSS]]]]]['Z']")
      .parseDefaulting(DAY_OF_YEAR, 1)
      .parseDefaulting(HOUR_OF_DAY, 0)
      .parseDefaulting(MINUTE_OF_HOUR, 0)
      .parseDefaulting(SECOND_OF_MINUTE, 0)
      .parseDefaulting(NANO_OF_SECOND, 0)
      .toFormatter()
}
