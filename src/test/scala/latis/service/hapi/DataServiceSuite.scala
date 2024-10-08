package latis.service.hapi

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.all.*
import io.circe.Json
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.http4s.{Status as _}
import scodec.codecs

import latis.catalog.Catalog
import latis.data.DomainData
import latis.data.RangeData
import latis.data.Sample
import latis.data.SeqFunction
import latis.dataset.MemoizedDataset
import latis.metadata.Metadata
import latis.model.*
import latis.service.hapi.HapiInterpreter.noopInterpreter
import latis.service.hapi.{Status as HStatus}
import latis.time.Time
import latis.util.Identifier.*

class DataServiceSuite extends CatsEffectSuite {

  /** Build a simple test DataService[IO] with a time -> int dataset using the Latis3Interpreter */
  private lazy val dataset = (for {
    time <- Time.fromMetadata(
      Metadata(
        "id" -> "time",
        "type" -> "string",
        "units" -> "yyyy-MM-dd"
      )
    )
    disp <- Scalar.fromMetadata(
      Metadata(
        "id" -> "displacement",
        "type" -> "int",
        "units" -> "meters"
      )
    )
    model <- Function.from(id"testfunc", time, disp)
    data = new SeqFunction(Seq(
      Sample(DomainData("2000-01-01"), RangeData(1)),
      Sample(DomainData("2000-01-02"), RangeData(5)),
      Sample(DomainData("2000-01-03"), RangeData(4)),
      Sample(DomainData("2000-01-04"), RangeData(2)),
      Sample(DomainData("2000-01-05"), RangeData(0)),
    ))
    md = Metadata(
      "id" -> "testdataset",
      "temporalCoverage" -> "2000-01-01/2000-01-05"
    )
    dataset = new MemoizedDataset(md, model, data)
  } yield dataset).fold(err => fail(err.message), identity)
  private lazy val latisInterp = new Latis3Interpreter(Catalog(dataset))
  private lazy val dataService = new DataService[IO](latisInterp).service

  /** Assert GET request to given URI returns a particular status. */
  def assertStatus(uri: Uri, status: HStatus)(using loc: munit.Location): IO[Unit] = {
    val service = new DataService[IO](noopInterpreter).service
    val req = Request[IO](Method.GET, uri)

    service.orNotFound(req).flatMap { res =>
      res.as[Json].assertEquals(HapiError(status).asJson)
    }
  }

  /** Assert the CSV decoder rejects the argument. */
  def csvDecoderReject(arg: String)(using loc: munit.Location): Unit =
    QueryDecoders.csvDecoder[String].decode(QueryParameterValue(arg))
      .fold(_ => assert(cond = true), _ => fail(s"Accepted bad input: '$arg'"))

  test("return a 1402 for invalid start times") {
    assertStatus(
      uri"/data?dataset=0&start=invalid&stop=2018Z",
      HStatus.`1402`
    )
  }

  test("return a 1403 for invalid stop times") {
    assertStatus(
      uri"/data?dataset=0&start=2018Z&stop=invalid",
      HStatus.`1403`
    )
  }

  test("return a 1404 for misordered times") {
    assertStatus(
      uri"/data?dataset=0&start=2018Z&stop=2017Z",
      HStatus.`1404`
    )
  }

  test("return a 1409 for invalid formats") {
    assertStatus(
      uri"/data?dataset=0&start=2017Z&stop=2018Z&format=cats",
      HStatus.`1409`
    )
  }

  test("return a 1410 for invalid include settings") {
    assertStatus(
      uri"/data?dataset=0&start=2017Z&stop=2018Z&include=cats",
      HStatus.`1410`
    )
  }

  test("be backwards compatible with old param names") {
    assertStatus(
      uri"/data?id=0&time.min=invalid&time.max=2018Z",
      HStatus.`1402`
    ) >>
    assertStatus(
      uri"/data?id=0&time.min=2018Z&time.max=invalid",
      HStatus.`1403`
    ) >>
    assertStatus(
      uri"/data?id=0&time.min=2018Z&time.max=2017Z",
      HStatus.`1404`
    ) >>
    assertStatus(
      uri"/data?id=0&time.min=2017Z&time.max=2018Z&format=cats",
      HStatus.`1409`
    ) >>
    assertStatus(
      uri"/data?id=0&time.min=2017Z&time.max=2018Z&include=cats",
      HStatus.`1410`
    )
  }

  test("accept 'header' for the 'include' parameter") {
    val decoded = Include.includeDecoder.decode(QueryParameterValue("header"))
    decoded.fold(_ => fail("Failed to accept good input"), x => assert(x.header))
  }

  test("reject other arguments for the 'include' parameter") {
    val decoded = Include.includeDecoder.decode(QueryParameterValue("yolo"))
    decoded.fold(_ => assert(true), _ => fail("Accepted bad input"))
  }

  test("accept 'csv' for the 'format' parameter") {
    val fmt = "csv"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail("Failed to accept good input"), x => assertEquals(x, Format.Csv))
  }

  test("accept 'binary' for the 'format' parameter") {
    val fmt = "binary"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail("Failed to accept good input"), x => assertEquals(x, Format.Binary))
  }

  test("accept 'json' for the 'format' parameter") {
    val fmt = "json"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail("Failed to accept good input"), x => assertEquals(x, Format.Json))
  }

  test("reject other arguments for the 'format' parameter") {
    val decoded = Format.formatDecoder.decode(QueryParameterValue("yolo"))
    decoded.fold(_ => assert(true), _ => fail("Accepted bad input."))
  }

  test("accept a list of parameter names for the 'parameters' parameter") {
    val params = NonEmptyList.of("a", "b", "c")
    val decoded = QueryDecoders.csvDecoder[String].decode(
      QueryParameterValue(params.mkString_("", ",", ""))
    )
    decoded.fold(_ => fail("Failed to accept good input"), x => assertEquals(x, params))
  }

  test("reject an empty parameter list for the 'parameters' parameter") {
    csvDecoderReject("")
  }

  test("reject commas with no values for the 'parameters' parameter") {
    csvDecoderReject(",,,")
  }

  test("reject empty fields for the 'parameters' parameter") {
    csvDecoderReject("a,,c")
  }

  test("reject leading commas for the 'parameters' parameter") {
    csvDecoderReject(",b,c")
  }

  test("reject trailing commas for the 'parameters' parameter") {
    csvDecoderReject("a,b,")
  }

  test("correctly generate a response for a CSV request") {
    val req = Request[IO](Method.GET, uri"/data?dataset=testdataset&start=2000-01-01&stop=2000-01-06&format=csv")
    val resp = dataService.orNotFound(req)

    val testStr = "2000-01-01T00:00:00.000Z,1\r\n" +
      "2000-01-02T00:00:00.000Z,5\r\n" +
      "2000-01-03T00:00:00.000Z,4\r\n" +
      "2000-01-04T00:00:00.000Z,2\r\n" +
      "2000-01-05T00:00:00.000Z,0\r\n"

    resp.flatMap { res =>
      res.headers.get[`Content-Type`].map(_.mediaType) match {
        case Some(mType) => assertEquals(mType, MediaType.text.csv)
        case None => fail("No content type header")
      }
      res.bodyText.compile.string.assertEquals(testStr)
    }
  }

  test("correctly generate a response for a binary request") {
    val req = Request[IO](Method.GET, uri"/data?dataset=testdataset&start=2000-01-01&stop=2000-01-06&format=binary")
    val resp = dataService.orNotFound(req)

    val encoder = codecs.list(codecs.utf8 :: codecs.int32L)
    val testBin = encoder.encode(
      List(
        ("2000-01-01T00:00:00.000Z", 1),
        ("2000-01-02T00:00:00.000Z", 5),
        ("2000-01-03T00:00:00.000Z", 4),
        ("2000-01-04T00:00:00.000Z", 2),
        ("2000-01-05T00:00:00.000Z", 0)
      )
    ).require.toByteArray.toList

    resp.flatMap { res =>
      res.headers.get[`Content-Type`].map(_.mediaType)  match {
        case Some(mType) => assertEquals(mType, MediaType.application.`octet-stream`)
        case None => fail("No content type header")
      }
      res.body.compile.toList.assertEquals(testBin)
    }
  }

  test("correctly generate a response for a json request") {
    val req = Request[IO](Method.GET, uri"/data?dataset=testdataset&start=2000-01-01&stop=2000-01-06&format=json")
    val resp = dataService.orNotFound(req)

    val testJson = Json.obj(
      ("HAPI", Json.fromString("3.0")),
      ("status", Json.obj(
        ("code", Json.fromInt(1200)),
        ("message", Json.fromString("OK"))
      )),
      ("parameters", Json.arr(
        Json.obj(
          ("name", Json.fromString("time")),
          ("type", Json.fromString("isotime")),
          ("length", Json.fromInt(24)),
          ("units", Json.fromString("UTC")),
          ("fill", Json.Null),
        ),
        Json.obj(
          ("name", Json.fromString("displacement")),
          ("type", Json.fromString("integer")),
          ("units", Json.fromString("meters")),
          ("fill", Json.Null),
        ),
      )),
      ("startDate", Json.fromString("2000-01-01Z")),
      ("stopDate", Json.fromString("2000-01-05Z")),
      ("format", Json.fromString("json")),
      ("data", Json.arr(
        Json.arr(Json.fromString("2000-01-01T00:00:00.000Z"), Json.fromInt(1)),
        Json.arr(Json.fromString("2000-01-02T00:00:00.000Z"), Json.fromInt(5)),
        Json.arr(Json.fromString("2000-01-03T00:00:00.000Z"), Json.fromInt(4)),
        Json.arr(Json.fromString("2000-01-04T00:00:00.000Z"), Json.fromInt(2)),
        Json.arr(Json.fromString("2000-01-05T00:00:00.000Z"), Json.fromInt(0))
      ))
    )

    resp.flatMap { res =>
      res.headers.get[`Content-Type`].map(_.mediaType) match {
        case Some(mType) => assertEquals(mType, MediaType.application.json)
        case None => fail("no content type header")
      }

      res.as[Json].map(assertEquals(_, testJson))
    }
  }
}
