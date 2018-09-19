package lasp.hapi.service

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import io.circe.syntax._
import org.http4s._
import org.http4s.implicits._
import org.scalatest.Assertion
import org.scalatest.FlatSpec

class DataServiceSpec extends FlatSpec {

  /** Assert GET request to given URI returns a particular status. */
  def assertStatus(uri: Uri, status: Status): Assertion = {
    val service = new DataService[IO].service
    val req = Request[IO](Method.GET, uri)

    val body = service.orNotFound(req).flatMap { res =>
      res.bodyAsText.compile.toList.map(_.head)
    }.unsafeRunSync

    assert(body == HapiError(status).asJson.noSpaces)
  }

  "The data service" should "return a 1402 for invalid start times" in {
    assertStatus(
      Uri.uri("/hapi/data?id=0&time.min=invalid&time.max=2018Z"),
      Status.`1402`
    )
  }

  it should "return a 1403 for invalid stop times" in {
    assertStatus(
      Uri.uri("/hapi/data?id=0&time.min=2018Z&time.max=invalid"),
      Status.`1403`
    )
  }

  it should "return a 1404 for misordered times" in {
    assertStatus(
      Uri.uri("/hapi/data?id=0&time.min=2018Z&time.max=2017Z"),
      Status.`1404`
    )
  }

  it should "return a 1409 for invalid formats" in {
    assertStatus(
      Uri.uri("/hapi/data?id=0&time.min=2017Z&time.max=2018Z&format=cats"),
      Status.`1409`
    )
  }

  it should "return a 1410 for invalid include settings" in {
    assertStatus(
      Uri.uri("/hapi/data?id=0&time.min=2017Z&time.max=2018Z&include=cats"),
      Status.`1410`
    )
  }

  "The 'include' parameter" should "accept 'header'" in {
    val decoded = Include.includeDecoder.decode(QueryParameterValue("header"))
    decoded.fold(_ => fail, x => assert(x.header))
  }

  it should "reject other arguments" in {
    val decoded = Include.includeDecoder.decode(QueryParameterValue("yolo"))
    decoded.fold(_ => succeed, _ => fail("Accepted bad input."))
  }

  "The 'format' parameter" should "accept 'csv'" in {
    val fmt = "csv"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail, x => assert(x.format == fmt))
  }

  it should "accept 'binary'" in {
    val fmt = "binary"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail, x => assert(x.format == fmt))
  }

  it should "accept 'json'" in {
    val fmt = "json"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail, x => assert(x.format == fmt))
  }

  it should "reject other arguments" in {
    val decoded = Format.formatDecoder.decode(QueryParameterValue("yolo"))
    decoded.fold(_ => succeed, _ => fail("Accepted bad input."))
  }

  "The 'parameters' parameter" should "accept a list of parameter names" in {
    val params = NonEmptyList.of("a", "b", "c")
    val decoded = QueryDecoders.csvDecoder[String].decode(
      QueryParameterValue(params.mkString_("", ",", ""))
    )
    decoded.fold(_ => fail, x => assert(x == params))
  }

  it should "reject an empty parameter list" in {
    val decoded = QueryDecoders.csvDecoder[String].decode(
      QueryParameterValue("")
    )
    decoded.fold(_ => succeed, _ => fail("Accepted empty parameter list."))
  }
}
