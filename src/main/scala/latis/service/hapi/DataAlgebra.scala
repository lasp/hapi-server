package latis.service.hapi

import fs2.Stream
import io.circe.JsonObject

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
   * @return a stream of UTF-8 encoded Bytes
   */
  def streamCsv(data: T): Stream[F, Byte]

  /**
   * Write data as binary stream.
   *
   * @param data data to write
   * @return a stream of Bytes
   */
  def streamBinary(data: T): Stream[F, Byte]

  /**
   * Write data as Json.
   *
   * @param data data to write
   * @param header Json with the header content to prepend to the data
   * @return a stream of Bytes
   */
  def streamJson(data: T, header: JsonObject): Stream[F, Byte]
}
