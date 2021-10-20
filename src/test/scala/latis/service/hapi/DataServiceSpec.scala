package latis.service.hapi

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.circe.syntax._
import org.http4s.{Status => _, _}
import org.http4s.implicits._
import org.scalatest.Assertion
import org.scalatest.FlatSpec
import org.typelevel.ci.CIString
import scodec.codecs

import latis.catalog.Catalog
import latis.data.DomainData
import latis.data.RangeData
import latis.data.Sample
import latis.data.SeqFunction
import latis.dataset.MemoizedDataset
import latis.metadata.Metadata
import latis.model._
import latis.service.hapi.HapiError._
import latis.service.hapi.HapiInterpreter.noopInterpreter
import latis.service.hapi.{Status => HStatus}
import latis.time.Time
import latis.util.Identifier.IdentifierStringContext

class DataServiceSpec extends FlatSpec {

  /** Build a simple test DataService[IO] with a time -> int dataset using the Latis3Interpreter*/
  private lazy val dataset = (for {
    time <- Time.fromMetadata(Metadata("id"->"time", "type"->"string", "units"->"yyyy-MM-dd", "coverage"->"2000-01-01/2000-01-05"))
    disp <- Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters"))
    model <- Function.from(id"testfunc", time, disp)
    data = new SeqFunction(Seq(
      Sample(DomainData("2000-01-01"), RangeData(1)),
      Sample(DomainData("2000-01-02"), RangeData(5)),
      Sample(DomainData("2000-01-03"), RangeData(4)),
      Sample(DomainData("2000-01-04"), RangeData(2)),
      Sample(DomainData("2000-01-05"), RangeData(0)),
    ))
    dataset = new MemoizedDataset(Metadata("id"->"testdataset"), model, data)
  } yield dataset).fold(err => fail(err.message), identity)
  private lazy val latisInterp = new Latis3Interpreter(Catalog(dataset))
  private lazy val dataService = new DataService[IO](latisInterp).service

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
    decoded.fold(_ => fail(), x => assert(x == Format.Csv))
  }

  it should "accept 'binary'" in {
    val fmt = "binary"
    val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
    decoded.fold(_ => fail(), x => assert(x == Format.Binary))
  }

  // it should "accept 'json'" in {
  //   val fmt = "json"
  //   val decoded = Format.formatDecoder.decode(QueryParameterValue(fmt))
  //   decoded.fold(_ => fail(), x => assert(x == Format.Json))
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

  "The DataService" should "correctly generate a response for a CSV request" in {
    val req = Request[IO](Method.GET, uri"/data?dataset=testdataset&start=2000-01-01&stop=2000-01-06&format=csv")
    val resp = dataService.orNotFound(req)
    (for {
      body <- resp.flatMap { res =>
        res.bodyText.compile.toList
      }
      contentType = resp.unsafeRunSync().headers.get(CIString("Content-Type")).get.head.value
    } yield {
      val testStr = "2000-01-01T00:00:00.000Z,1\n" +
                    "2000-01-02T00:00:00.000Z,5\n" +
                    "2000-01-03T00:00:00.000Z,4\n" +
                    "2000-01-04T00:00:00.000Z,2\n" +
                    "2000-01-05T00:00:00.000Z,0\n"
      assert(body.mkString == testStr)
      assert(contentType == "text/csv")
    }).unsafeRunSync()
  }

  it should "correctly generate a response for a binary request" in {
    val req = Request[IO](Method.GET, uri"/data?dataset=testdataset&start=2000-01-01&stop=2000-01-06&format=binary")
    val resp = dataService.orNotFound(req)
    (for {
      body <- resp.flatMap { res =>
        res.body.compile.toList
      }
      contentType = resp.unsafeRunSync().headers.get(CIString("Content-Type")).get.head.value
    } yield {
      val encoder = codecs.list(codecs.utf8 ~ codecs.int32L)
      val testBin = encoder.encode(
        List(
          ("2000-01-01T00:00:00.000Z", 1),
          ("2000-01-02T00:00:00.000Z", 5),
          ("2000-01-03T00:00:00.000Z", 4),
          ("2000-01-04T00:00:00.000Z", 2),
          ("2000-01-05T00:00:00.000Z", 0)
        )
      ).require.toByteArray.toList
      assert(body == testBin)
      assert(contentType == "application/octet-stream")
    }).unsafeRunSync()
  }
}
