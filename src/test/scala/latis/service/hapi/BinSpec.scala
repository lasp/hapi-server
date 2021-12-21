package latis.service.hapi

import io.circe._
import io.circe.syntax._
import org.scalatest.flatspec._

class BinSpec extends AnyFlatSpec {

  "The Bin encoder" should "keep 'centers' if 'ranges' is not defined" in {
    val bin = Bin("", None, None, "", Option(""))

    val expected = Json.obj(
      ("name", "".asJson),
      ("centers", Json.Null),
      ("units", "".asJson),
      ("description", "".asJson)
    )

    assert(bin.asJson == expected)
  }

  it should "remove 'centers' if null and 'ranges' is defined" in {
    val bin = Bin("", None, Option(List((1,2))), "", Option(""))

    val expected = Json.obj(
      ("name", "".asJson),
      ("ranges", Json.arr(Json.arr(1.asJson, 2.asJson))),
      ("units", "".asJson),
      ("description", "".asJson)
    )

    assert(bin.asJson == expected)
  }

  it should "remove null values otherwise" in {
    val bin = Bin("", None, None, "", None)

    val expected = Json.obj(
      ("name", "".asJson),
      ("centers", Json.Null),
      ("units", "".asJson),
    )

    assert(bin.asJson == expected)
  }
}
