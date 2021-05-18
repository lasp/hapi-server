package latis.service.hapi

import cats.effect.ContextShift
import cats.effect.IO
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.implicits._
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class CapabilitiesServiceSpec extends FlatSpec {

  private implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  "The capabilities service" should "advertise CSV only" in {
    val req = Request[IO](Method.GET, uri"/hapi/capabilities")

    val expected = Json.obj(
      ("HAPI", Json.fromString(HapiService.version)),
      ("status", latis.service.hapi.Status.`1200`.asJson),
      ("outputFormats", Json.fromValues(
        List(
          Json.fromString("csv")
        )
      ))
    )


    val body = {
      val capabilities = new CapabilitiesService[IO]()
      // False error
      capabilities.service.orNotFound(req).flatMap { res =>
        // The stream ought to contain only the body.
        res.bodyText.compile.toList.map(_.head)
      }.unsafeRunSync
    }
    assert(body == expected.noSpaces)
  }
}
