package latis.service.hapi

import fs2.Stream

/** Algebra for reading data. */
trait DataAlgebra[F[_]] {

  /** Type of data read by algebra. */
  type T

  /**
   * Read data given a request.
   *
   * @param req request information
   */
  def getData(req: DataRequest): F[T]

  /**
   * Write data as CSV.
   *
   * @param data data to write
   * @return a stream of CSV records
   */
  def writeData(data: T): Stream[F, String]
}
