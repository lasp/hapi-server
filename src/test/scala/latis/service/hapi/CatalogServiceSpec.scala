package latis.service.hapi

import munit.CatsEffectSuite

class CatalogServiceSpec extends CatsEffectSuite {

  test("default to using the ID as the dataset title") {
    val id = "id"
    val ds = Dataset(id)

    assertEquals(ds.title, id)
  }
}
