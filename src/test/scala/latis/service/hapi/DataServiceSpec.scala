package latis.service.hapi

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.circe.syntax._
import org.http4s._
import org.http4s.implicits._
import org.scalatest.Assertion
import org.scalatest.FlatSpec

import latis.service.hapi.HapiError._
import latis.service.hapi.{Status => HStatus}
import latis.service.hapi.HapiInterpreter.noopInterpreter

class DataServiceSpec extends FlatSpec {

  /** Assert GET request to given URI returns a particular status. */
  def assertStatus(uri: Uri, status: HStatus): Assertion = {
    val service = new DataService[IO](noopInterpreter).service
    val req = Request[IO](Method.GET, uri)

    val body = service.orNotFound(req).flatMap { res =>
      res.bodyText.compile.toList.map(_.head)
    }.unsafeRunSync()

    assert(body == HapiError(status).asJson.noSpaces)
  }

  /** Assert the CSV decoder rejects the argument. */
  def csvDecoderReject(arg: String): Assertion =
    QueryDecoders.csvDecoder[String].decode(QueryParameterValue(arg))
      .fold(_ => succeed, _ => fail(s"Accepted bad input: '$arg'"))

  "The data service" should "return a 1402 for invalid start times" in {
    assertStatus(
      uri"/data?dataset=0&start=invalid&stop=2018Z",
      HStatus.`1402`
    )
    assertStatus(
      uri"/data?dataset=0&start=invalid&stop=2018Z",
      HStatus.`1402`
    )
    assertStatus(
      uri"/data?dataset=0&start=invalid&stop=2018Z",
      HStatus.`1402`
    )
  }

  it should "return a 1403 for invalid stop times" in {
    assertStatus(
      uri"/data?dataset=0&start=2018Z&stop=invalid",
      HStatus.`1403`
    )
  }

  it should "return a 1404 for misordered times" in {
    assertStatus(
      uri"/data?dataset=0&start=2018Z&stop=2017Z",
      HStatus.`1404`
    )
  }

  it should "return a 1409 for invalid formats" in {
    assertStatus(
      uri"/data?dataset=0&start=2017Z&stop=2018Z&format=cats",
      HStatus.`1409`
    )
  }

  it should "return a 1410 for invalid include settings" in {
    assertStatus(
      uri"/data?dataset=0&start=2017Z&stop=2018Z&include=cats",
      HStatus.`1410`
    )
  }

  it should "be backwards compatible with old param names" in {
    assertStatus(
      uri"/data?id=0&time.min=invalid&time.max=2018Z",
      HStatus.`1402`
    )
    assertStatus(
      uri"/data?id=0&time.min=invalid&time.max=2018Z",
      HStatus.`1402`
    )
    assertStatus(
      uri"/data?id=0&time.min=invalid&time.max=2018Z",
      HStatus.`1402`
    )
    assertStatus(
      uri"/data?id=0&time.min=2018Z&time.max=invalid",
      HStatus.`1403`
    )
    assertStatus(
      uri"/data?id=0&time.min=2018Z&time.max=2017Z",
      HStatus.`1404`
    )
    assertStatus(
      uri"/data?id=0&time.min=2017Z&time.max=2018Z&format=cats",
      HStatus.`1409`
    )
    assertStatus(
      uri"/data?id=0&time.min=2017Z&time.max=2018Z&include=cats",
      HStatus.`1410`
    )
  }

  "The 'include' parameter" should "accept 'header'" in {
    val decoded = Include.includeDecoder.decode(QueryParameterValue("header"))
    decoded.fold(_ => fail(), x => assert(x.header))
  }

  it should "reject other arguments" in {
    val decoded = Include.includeDecoder.decode(QueryParameterValue("yolo"))
    decoded.fold(_ => succeed, _ => fail("Accepted bad input."))
  }

  "The 'format' parameter" should "accept 'csv'" in {
    val fmt = "csv"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail(), x => assert(x.format == fmt))
  }

  // it should "accept 'binary'" in {
  //   val fmt = "binary"
  //   val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
  //   decoded.fold(_ => fail, x => assert(x.format == fmt))
  // }

  // it should "accept 'json'" in {
  //   val fmt = "json"
  //   val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
  //   decoded.fold(_ => fail, x => assert(x.format == fmt))
  // }

  it should "reject other arguments" in {
    val decoded = Format.formatDecoder.decode(QueryParameterValue("yolo"))
    decoded.fold(_ => succeed, _ => fail("Accepted bad input."))
  }

  "The 'parameters' parameter" should "accept a list of parameter names" in {
    val params = NonEmptyList.of("a", "b", "c")
    val decoded = QueryDecoders.csvDecoder[String].decode(
      QueryParameterValue(params.mkString_("", ",", ""))
    )
    decoded.fold(_ => fail(), x => assert(x == params))
  }

  it should "reject an empty parameter list" in {
    csvDecoderReject("")
  }

  it should "reject commas with no values" in {
    csvDecoderReject(",,,")
  }

  it should "reject empty fields" in {
    csvDecoderReject("a,,c")
  }

  it should "reject leading commas" in {
    csvDecoderReject(",b,c")
  }

  it should "reject trailing commas" in {
    csvDecoderReject("a,b,")
  }
}
