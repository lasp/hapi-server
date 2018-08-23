package lasp.hapi.service

import io.circe.Encoder

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
