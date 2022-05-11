package latis.service.hapi

import io.circe._
import io.circe.syntax._
import munit.CatsEffectSuite

class BinSpec extends CatsEffectSuite {

  test("keep 'centers' if 'ranges' is not defined") {
    val bin = Bin("", None, None, "", Option(""))

    val expected = Json.obj(
      ("name", "".asJson),
      ("centers", Json.Null),
      ("units", "".asJson),
      ("description", "".asJson)
    )

    assertEquals(bin.asJson, expected)
  }

  test("remove 'centers' if null and 'ranges' is defined") {
    val bin = Bin("", None, Option(List((1,2))), "", Option(""))

    val expected = Json.obj(
      ("name", "".asJson),
      ("ranges", Json.arr(Json.arr(1.asJson, 2.asJson))),
      ("units", "".asJson),
      ("description", "".asJson)
    )

    assertEquals(bin.asJson, expected)
  }

  test("remove null values otherwise"){
    val bin = Bin("", None, None, "", None)

    val expected = Json.obj(
      ("name", "".asJson),
      ("centers", Json.Null),
      ("units", "".asJson),
    )

    assertEquals(bin.asJson, expected)
  }
}
