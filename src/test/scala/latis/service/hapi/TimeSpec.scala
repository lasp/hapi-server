package latis.service.hapi

import java.time.LocalDateTime

import org.scalactic.Equality
import org.scalatest.Assertion
import org.scalatest.FlatSpec

class TimeSpec extends FlatSpec {

  val expected = LocalDateTime.of(2018, 1, 1, 0, 0, 0)

  def testParse(str: String): Assertion =
    assert {
      expected === Time.parse(str).getOrElse {
        fail(s"Failed to parse time string: $str")
      }
    }

  // The `equals` method should be used to compare `LocalDateTime`
  // instances. See the Javadocs.
  implicit val eq: Equality[LocalDateTime] =
    new Equality[LocalDateTime] {
      override def areEqual(a: LocalDateTime, b: Any): Boolean = a.equals(b)
    }

  "The time string parser" should "parse date and time strings" in {
    testParse("2018-01-01T00:00:00.000Z")

    // Missing the trailing 'Z'
    testParse("2018-01-01T00:00:00.000")
  }

  it should "parse ordinal date and time strings" in {
    testParse("2018-001T00:00:00Z")

    // Missing the trailing 'Z'
    testParse("2018-001T00:00:00")
  }

  it should "parse date strings" in {
    testParse("2018-01-01Z")

    // Missing the trailing 'Z'
    testParse("2018-01-01")
  }

  it should "parse ordinal date strings" in {
    testParse("2018-001Z")

    // Missing the trailing 'Z'
    testParse("2018-001")
  }

  it should "handle missing date or time elements" in {
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
