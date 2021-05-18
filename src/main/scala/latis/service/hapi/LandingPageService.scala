package latis.service.hapi

import cats.effect.Effect
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags._
import scalatags.Text.all._

/** Implements the HAPI landing page. */
class LandingPageService[F[_]: Effect] extends Http4sDsl[F] {

  val landingPage =
    html(
      body(
        h1("LASP HAPI Server"),
        p("""This server supports the HAPI 3.0 specification for
        |delivery of time series data. The server consists of the
        |following 5 REST-like endpoints that will respond to HTTP GET
        |requests:""".stripMargin.replaceAll("\n", " ")),
        ul(
          li(
            a(href := "hapi/about")("about"),
            " - information about the server"
          ),
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

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(landingPage)
    }
}
