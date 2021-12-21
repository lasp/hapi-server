package latis.service.hapi

import org.scalatest.flatspec._

class CatalogServiceSpec extends AnyFlatSpec {

  "A dataset" should "default to using the ID as the title" in {
    val id = "id"
    val ds = Dataset(id)

    assert(ds.title == id)
  }
}
