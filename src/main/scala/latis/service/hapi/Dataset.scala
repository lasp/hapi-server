package latis.service.hapi

import io.circe.Encoder

/**
 * Representation of a dataset in the HAPI catalog.
 *
 * @param id computer-friendly identifier
 * @param title human-friendly title
 *
 */
final case class Dataset(
  id: String,
  title: String
)

object Dataset {

  /**
   * Constructor for datasets with only an ID.
   *
   * According to the spec, datasets without titles must re-use the ID
   * as a title, which this constructor enforces.
   *
   * @param id computer-friendly identifier
   *
   */
  def apply(id: String): Dataset =
    Dataset(id, id)

  /** JSON encoder */
  implicit val encoder: Encoder[Dataset] =
    Encoder.forProduct2("id", "title") { x =>
      (x.id, x.title)
    }
}
