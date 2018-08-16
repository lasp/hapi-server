package latis.server

import cats.effect.Effect
import io.circe.Encoder
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

/** Implements the `/catalog` endpoint. */
class CatalogService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "catalog" =>
        Ok("Hello from HAPI!")
    }
}

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
  implicit val encoder: Encoder[Catalog] =
    Encoder.forProduct3("HAPI", "status", "catalog") { x =>
      (x.version, x.status, x.catalog)
    }
}

/**
 * Representation of a dataset in the HAPI catalog.
 *
 * @param id computer-friendly identifier
 * @param name human-friendly name
 *
 */
final case class Dataset(
  id: String,
  name: String
)

object Dataset {

  /**
   * Constructor for datasets with only an ID.
   *
   * According to the spec, datasets without names must re-use the ID
   * as a name, which this constructor enforces.
   *
   * @param id computer-friendly identifier
   *
   */
  def apply(id: String): Dataset =
    Dataset(id, id)

  /** JSON encoder */
  implicit val encoder: Encoder[Dataset] =
    Encoder.forProduct2("id", "name") { x =>
      (x.id, x.name)
    }
}
