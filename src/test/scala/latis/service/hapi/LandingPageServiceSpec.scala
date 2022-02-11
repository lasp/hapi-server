package latis.service.hapi

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s
import org.http4s.Method
import org.http4s.Request
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.flatspec.AnyFlatSpec
import org.typelevel.ci.CIStringSyntax

import latis.catalog.Catalog
import latis.dataset.MemoizedDataset
import latis.metadata.Metadata

class LandingPageServiceSpec extends AnyFlatSpec {
  private lazy val dataset1 = new MemoizedDataset(Metadata("id"->"id1", "title"->"title1"), null, null)
  private lazy val dataset2 = new MemoizedDataset(Metadata("id"->"id2", "title"->"title2"), null, null)
  private lazy val latisInterp = new Latis3Interpreter(Catalog(dataset1, dataset2))
  private lazy val landingPageService = new LandingPageService[IO](latisInterp)

  "The Landing Page" should "create a non-zero length '200 OK' response" in {
    val req = Request[IO](Method.GET, uri"/")
    (for {
      response <- landingPageService.service.orNotFound(req)
    } yield {
      assert(response.status == http4s.Status.Ok)
      assert(response.headers.get(ci"Content-Length").get.head.value.toInt > 0)
    }).unsafeRunSync()
  }

  it should "correctly generate the catalog table" in {
    (for {
      catalog <- landingPageService.catalogTable
    } yield {
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
      assert(catalog.render == expected)
    }).unsafeRunSync()
  }
}
