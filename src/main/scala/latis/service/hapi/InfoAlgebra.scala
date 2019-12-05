package latis.service.hapi

import cats.data.EitherT
import cats.data.NonEmptyList

/** Algebra of metadata operations. */
trait InfoAlgebra[F[_]] {

  /**
   * Get metadata for a dataset.
   *
   * @param id dataset ID
   * @param params parameters to include
   */
  def getMetadata(
    id: String,
    params: Option[NonEmptyList[String]]
  ): EitherT[F, InfoError, Metadata]
}
