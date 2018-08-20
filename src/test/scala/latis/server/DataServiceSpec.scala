package latis.server

import org.http4s.QueryParameterValue
import org.scalatest.FlatSpec

class DataServiceSpec extends FlatSpec {

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
    val params = List("a", "b", "c")
    val decoded = QueryDecoders.csvDecoder[String].decode(
      QueryParameterValue(params.mkString(","))
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
