package lasp.hapi.service
package latis2

import cats.data.EitherT
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import latis.dm
import latis.dm.{ Dataset => _, _ }
import latis.ops.Operation
import latis.ops.Projection
import latis.reader.CatalogReader

/** Interpreter for HAPI algebras using LaTiS 2. */
class Latis2Interpreter[F[_]: Sync] extends HapiInterpreter[F] {

  override val getCatalog: F[List[Dataset]] =
    Sync[F].delay {
      new CatalogReader().getDataset() match {
        case dm.Dataset(Function(it)) =>
          it.map {
            case Sample(Text(name), _) =>
              Dataset(name)
          }.toList
      }
    }

  override def getMetadata(
    id: String,
    params: Option[NonEmptyList[String]]
  ): EitherT[F, InfoError, Metadata] =
    for {
      // Check that the dataset ID is in the catalog, short-circuit
      // with UnknownId if not found.
      catalog  <- EitherT.liftF(getCatalog)
      _        <- EitherT.cond[F](
        catalog.exists(_.name === id),
        (), UnknownId(id)
      )
      // Get the dataset, projecting the requested parameters.
      project   = params.fold(
        List.empty[Operation]
      )(
        // Time must always be included. LaTiS ignores duplicates in a
        // projection, so there's no need to check that time isn't
        // already projected.
        ps => List(Projection("time" :: ps.toList))
      )
      dataset  <- EitherT.liftF(Latis2Util.getDataset(id, project))
      // Check for parameters that were requested but not returned.
      // This would indicate that the parameter wasn't found, so we
      // should return an error.
      missing   = params.flatMap(_.find(dataset.findVariableByName(_).isEmpty))
      _        <- EitherT.fromOption[F](missing.map(UnknownParam), ()).swap
      // Get the metadata.
      metadata <- EitherT.liftF(Latis2Util.getDatasetMetadata(dataset))
    } yield metadata
}
