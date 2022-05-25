package latis.service.hapi

import munit.FunSuite

class CatalogServiceSuite extends FunSuite {

  test("default to using the ID as the dataset title") {
    val id = "id"
    val ds = Dataset(id)

    assertEquals(ds.title, id)
  }
}
