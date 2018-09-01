package lasp.hapi.service

/** Algebra of catalog operations. */
trait CatalogAlgebra[F[_]] {

  /** Get the catalog of datasets. */
  val getCatalog: F[List[Dataset]]
}
