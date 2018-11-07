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
      dataset  <- EitherT.liftF(Latis2Util.getDataset(id, List.empty))
      // Check for parameters that were requested but not returned.
      // This would indicate that the parameter wasn't found, so we
      // should return an error.
      missing   = params.flatMap(_.find(dataset.findVariableByName(_).isEmpty))
      _        <- EitherT.fromOption[F](missing.map(UnknownParam), ()).swap
      // Get the metadata.
      metadata <- EitherT.liftF(Latis2Util.getDatasetMetadata(dataset))
    } yield params.fold(metadata) { ps =>
      // Filter the returned parameters. We wait until the end to do
      // this because we need to keep all the parameters in order to
      // get all the metadata. (Specifically, for making bins.)
      val newParams = metadata.parameters.filter {
        case Parameter("time", _, _, _, _, _, _, _) => true
        case Parameter(name, _, _, _, _, _, _, _)   => ps.exists(_ === name)
      }
      // This should be safe because we should at least have time.
      metadata.copy(parameters = newParams.toNel.get)
    }

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
          case Sample(Text(t), Function(it)) =>
            s"$t," + it.map {
              case Sample(_, Scalar(x)) => List(x.toString)
              case Sample(_, Tuple(xs)) =>
                xs.map {
                  case Scalar(x) => x.toString
                }
            }.toList.transpose.flatten.mkString(",")
        }.map(_ + "\n")
        Stream.fromIterator(rows)
    }
}
