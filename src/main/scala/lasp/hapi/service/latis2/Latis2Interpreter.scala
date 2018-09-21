package lasp.hapi.service
package latis2

import cats.data.EitherT
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import fs2.Stream
import latis.dm
import latis.dm.{ Dataset => _, _ }
import latis.ops.Operation
import latis.ops.Projection
import latis.ops.TimeFormatter
import latis.ops.filter.Selection
import latis.reader.CatalogReader

import lasp.hapi.util.Time

/** Interpreter for HAPI algebras using LaTiS 2. */
class Latis2Interpreter[F[_]: Sync] extends HapiInterpreter[F] {

  type T = dm.Dataset

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

  override def getData(req: DataRequest): F[dm.Dataset] = req match {
    case DataRequest(id, minTime, maxTime, params, _, _) =>
      val ops = List(
        Selection(s"time>=${minTime.format(Time.format)}"),
        Selection(s"time<${maxTime.format(Time.format)}"),
        TimeFormatter(Time.formatSDF)
      ) ++ params.fold(
        List.empty[Operation]
      )(
        ps => List(Projection("time" :: ps.toList))
      )
      Latis2Util.getDataset(id, ops)
  }

  override def writeData(data: dm.Dataset): Stream[F, String] =
    Stream.eval(data.pure[F]).flatMap {
      case dm.Dataset(Function(it)) =>
        val rows = it.map {
          case Sample(Index(_), Text(t))  => t
          case Sample(Text(t), Scalar(x)) => s"$t,$x"
          case Sample(Text(t), Tuple(xs)) =>
            val scalars = xs.map {
              case Scalar(x) => x.toString
            }.mkString(",")
            s"$t,$scalars"
        }.map(_ + "\n")
        Stream.fromIterator(rows)
    }
}
