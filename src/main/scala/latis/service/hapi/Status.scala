package latis.service.hapi

import io.circe.Encoder

/** Representation of a HAPI status code and message. */
final case class Status(code: Int, message: String)

object Status {
  val `1200`: Status = Status(1200, "OK")
  val `1201`: Status = Status(1201, "OK - no data for time range")
  val `1400`: Status = Status(1400, "Bad request - user input error")
  val `1401`: Status = Status(1401, "Bad request - unknown API parameter name")
  val `1402`: Status = Status(1402, "Bad request - error in start time")
  val `1403`: Status = Status(1403, "Bad request - error in stop time")
  val `1404`: Status = Status(1404, "Bad request - start time equal to or after stop time")
  val `1405`: Status = Status(1405, "Bad request - time outside valid range")
  val `1406`: Status = Status(1406, "Bad request - unknown dataset id")
  val `1407`: Status = Status(1407, "Bad request - unknown dataset parameter")
  val `1408`: Status = Status(1408, "Bad request - too much time or data requested")
  val `1409`: Status = Status(1409, "Bad request - unsupported output format")
  val `1410`: Status = Status(1410, "Bad request - unsupported include value")
  val `1500`: Status = Status(1500, "Internal server error")
  val `1501`: Status = Status(1501, "Internal server error - upstream request error")

  /** JSON encoder */
  given encoder: Encoder[Status] =
    Encoder.forProduct2("code", "message") { x =>
      (x.code, x.message)
    }
}
