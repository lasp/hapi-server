package latis.service.hapi

import cats.Monad
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags.*
import scalatags.Text
import scalatags.Text.all.*

/** Implements the HAPI landing page. */
class LandingPageService[F[_]: Monad](alg: CatalogAlgebra[F]) extends Http4sDsl[F] {

  val catalogTable: F[Text.TypedTag[String]] = for {
    catalog <- alg.getCatalog
    catalogEntries = catalog.foldLeft(frag()) { (acc, dataset) =>
      frag(
        acc,
        tr(
          td(dataset.id),
          td(a(href := "hapi/info?dataset="+dataset.id)(dataset.title))
        )
      )
    }
  } yield {
    table(
      caption(b(i(u("Catalog")))),
      tr(
        th("id"),
        th("title"),
      ),
      catalogEntries
    )
  }

  val landingPage: F[Text.TypedTag[String]] = {
    for {
      table <- catalogTable
    } yield {
      html(
        body(
          h1("LASP HAPI Server"),
          p(
            """This server supports the HAPI 3.0 specification for
              |delivery of time series data. The server consists of the
              |following 5 REST-like endpoints that will respond to HTTP GET
              |requests:""".stripMargin.replaceAll(System.lineSeparator(), " ")),
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
          ),
          table
        )
      )
    }
  }

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(landingPage)
    }
}
