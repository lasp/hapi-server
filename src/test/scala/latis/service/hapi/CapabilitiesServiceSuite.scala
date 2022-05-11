package latis.service.hapi

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._

class CapabilitiesServiceSuite extends CatsEffectSuite {

  test("produce correct capabilities endpoint") {
    val req = Request[IO](Method.GET, uri"/capabilities")

    val expected = Json.obj(
      ("HAPI", Json.fromString(HapiService.version)),
      ("status", latis.service.hapi.Status.`1200`.asJson),
      ("outputFormats", Json.fromValues(
        List(
          Json.fromString("csv"),
          Json.fromString("binary"),
          Json.fromString("json")
        )
      ))
    )

    val capabilities = new CapabilitiesService[IO]()
    capabilities.service.orNotFound(req).flatMap { res =>
      res.bodyText.compile.toList.map(_.head)
    }.map { resp =>
      assertEquals(resp, expected.noSpaces)
    }
  }
}
