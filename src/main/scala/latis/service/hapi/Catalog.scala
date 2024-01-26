package latis.service.hapi

import io.circe.Encoder

/**
 * Representation of a HAPI catalog.
 *
 * @param version version of HAPI
 * @param status HAPI status object
 * @param catalog list of available datasets
 */
final case class Catalog(
  version: String,
  status: Status,
  catalog: List[Dataset]
)

object Catalog {

  /** JSON encoder */
  given encoder: Encoder[Catalog] =
    Encoder.forProduct3("HAPI", "status", "catalog") { x =>
      (x.version, x.status, x.catalog)
    }
}
