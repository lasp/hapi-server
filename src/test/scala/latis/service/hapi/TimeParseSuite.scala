package latis.service.hapi

import java.time.LocalDateTime

import munit.FunSuite

class TimeParseSuite extends FunSuite {

  val expected: LocalDateTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0)

  def testParse(str: String)(implicit loc: munit.Location): Unit =
    Time.parse(str) match {
      case Some(t) => assertEquals(t, expected)
      case None => fail(s"Failed to parse time string: $str")
    }

  test("parse date and time strings") {
    testParse("2018-01-01T00:00:00.000Z")

    // Missing the trailing 'Z'
    testParse("2018-01-01T00:00:00.000")
  }

  test("parse ordinal date and time strings") {
    testParse("2018-001T00:00:00Z")

    // Missing the trailing 'Z'
    testParse("2018-001T00:00:00")
  }

  test("parse date strings") {
    testParse("2018-01-01Z")

    // Missing the trailing 'Z'
    testParse("2018-01-01")
  }

  test("parse ordinal date strings") {
    testParse("2018-001Z")

    // Missing the trailing 'Z'
    testParse("2018-001")
  }

  test("handle missing date or time elements") {
    // No milliseconds
    testParse("2018-01-01T00:00:00")

    // No milliseconds (ordinal date)
    testParse("2018-001T00:00:00")

    // No seconds
    testParse("2018-01-01T00:00Z")

    // No seconds (ordinal date)
    testParse("2018-001T00:00Z")

    // No minutes
    testParse("2018-01-01T00Z")

    // No minutes (ordinal date)
    testParse("2018-001T00Z")

    // No day of month
    testParse("2018-01Z")

    // No month
    testParse("2018Z")
  }
}
