package lasp.hapi.service

import cats.Applicative
import cats.data.EitherT
import cats.data.NonEmptyList
import cats.implicits._

/** Interpreter for the algebras making up the HAPI services. */
trait HapiInterpreter[F[_]] extends CatalogAlgebra[F] with InfoAlgebra[F]

object HapiInterpreter {

  /** Interpreter that does nothing. */
  def noopInterpreter[F[_]: Applicative]: HapiInterpreter[F] =
    new HapiInterpreter[F] {
      override val getCatalog: F[List[Dataset]] =
        List.empty[Dataset].pure[F]

      override def getMetadata(
        id: String,
        params: Option[NonEmptyList[String]]
      ): EitherT[F, InfoError, Metadata] =
        EitherT.leftT(UnknownId(""))
    }
}
