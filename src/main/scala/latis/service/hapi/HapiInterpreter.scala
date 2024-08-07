package latis.service.hapi

import cats.Applicative
import cats.data.EitherT
import cats.data.NonEmptyList
import cats.implicits.*
import fs2.Stream
import io.circe.JsonObject

/** Interpreter for the algebras making up the HAPI services. */
trait HapiInterpreter[F[_]]
    extends CatalogAlgebra[F]
    with DataAlgebra[F]
    with InfoAlgebra[F]

object HapiInterpreter {

  /** Interpreter that does nothing. */
  def noopInterpreter[F[_]: Applicative]: HapiInterpreter[F] =
    new HapiInterpreter[F] {

      type T = Unit

      override val getCatalog: F[List[Dataset]] =
        List.empty[Dataset].pure[F]

      override def getMetadata(
        id: String,
        params: Option[NonEmptyList[String]]
      ): EitherT[F, InfoError, Metadata] =
        EitherT.leftT(UnknownId(""))

      override def getData(req: DataRequest): F[Unit] =
        ().pure[F]

      override def streamCsv(data: Unit): Stream[F, Byte] =
        Stream.empty

      override def streamBinary(data: Unit): Stream[F, Byte] =
        Stream.empty

      override def streamJson(data: Unit, header: JsonObject): Stream[F, Byte] =
        Stream.empty
    }
}
