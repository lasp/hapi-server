package latis.server

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.implicits._
import org.scalatest.FlatSpec

class CapabilitiesServiceSpec extends FlatSpec {

  "The capabilities service" should "advertise all output options" in {
    val req = Request[IO](Method.GET, Uri.uri("/capabilities"))

    val expected = Json.obj(
      ("HAPI", Json.fromString(HapiServer.version)),
      ("status", Status.`1200`.asJson),
      ("outputFormats", Json.fromValues(
        List(
          Json.fromString("binary"),
          Json.fromString("csv"),
          Json.fromString("json")
        )
      ))
    )


    val body = {
      val capabilities = new CapabilitiesService[IO]()
      // False error
      capabilities.service.orNotFound(req).flatMap { res =>
        // The stream ought to contain only the body.
        res.bodyAsText.compile.toList.map(_.head)
      }.unsafeRunSync
    }
    assert(body == expected.noSpaces)
  }
}
