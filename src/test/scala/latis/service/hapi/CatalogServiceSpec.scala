package latis.service.hapi

import org.scalatest.FlatSpec

class CatalogServiceSpec extends FlatSpec {

  "A dataset" should "default to using the ID as the title" in {
    val id = "id"
    val ds = Dataset(id)

    assert(ds.title == id)
  }
}
