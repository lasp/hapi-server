package latis.service.hapi

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s
import org.http4s.Method
import org.http4s.Request
import org.http4s.headers.`Content-Length`
import org.http4s.implicits.http4sLiteralsSyntax

import latis.catalog.Catalog
import latis.dataset.MemoizedDataset
import latis.metadata.Metadata

class LandingPageServiceSuite extends CatsEffectSuite {
  private lazy val dataset1 = new MemoizedDataset(Metadata("id"->"id1", "title"->"title1"), null, null)
  private lazy val dataset2 = new MemoizedDataset(Metadata("id"->"id2", "title"->"title2"), null, null)
  private lazy val latisInterp = new Latis3Interpreter(Catalog(dataset1, dataset2))
  private lazy val landingPageService = new LandingPageService[IO](latisInterp)

  test("create a non-zero length '200 OK' response") {
    val req = Request[IO](Method.GET, uri"/")
    val resp = landingPageService.service.orNotFound(req)

    resp.map { res =>
      assertEquals(res.status, http4s.Status.Ok)
      res.headers.get[`Content-Length`].map(_.length) match {
        case Some(length) => assert(length > 0, "zero length response")
        case None => fail("no content length header")
      }
    }
  }

  test("correctly generate the catalog table") {
    val table = landingPageService.catalogTable

    val expected =
      """<table>
        |<caption><b><i><u>Catalog</u></i></b></caption>
        |<tr>
        |<th>id</th>
        |<th>title</th>
        |</tr>
        |<tr>
        |<td>id1</td>
        |<td><a href="hapi/info?dataset=id1">title1</a></td>
        |</tr>
        |<tr>
        |<td>id2</td>
        |<td><a href="hapi/info?dataset=id2">title2</a></td>
        |</tr>
        |</table>""".stripMargin.replaceAll(System.lineSeparator(), "")

    table.map { html =>
      assertEquals(html.render, expected)
    }
  }
}
