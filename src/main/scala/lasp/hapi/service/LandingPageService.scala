package lasp.hapi.service

import cats.effect.Effect
import cats.implicits._
import org.http4s.HttpService
import org.http4s.MediaType.`text/html`
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import scalatags.Text.all._

/** Implements the HAPI landing page. */
class LandingPageService[F[_]: Effect] extends Http4sDsl[F] {

  val landingPage: Frag =
    html(
      body(
        h1("LASP HAPI Server"),
        p("""This server supports the HAPI 2.0 specification for
        |delivery of time series data. The server consists of the
        |following 4 REST-like endpoints that will respond to HTTP GET
        |requests:""".stripMargin.replaceAll("\n", " ")),
        ul(
          li(
            a(href := "hapi/capabilities")("capabilities"),
            " - list the output formats the server can emit"
          ),
          li(
            a(href := "hapi/catalog")("catalog"),
            " - list the datasets that are available"
          ),
          li(
            a(href := "hapi/info")("info"),
            " - describe a dataset"
          ),
          li(
            a(href := "hapi/data")("data"),
            " - stream content of a dataset"
          )
        )
      )
    )

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "hapi" =>
        Ok(landingPage.render).map {
          _.withContentType(`Content-Type`(`text/html`))
        }
    }
}
