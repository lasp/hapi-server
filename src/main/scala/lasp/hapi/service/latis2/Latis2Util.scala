package lasp.hapi.service
package latis2

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import latis.dm._
import latis.ops.Operation
import latis.reader.DatasetAccessor

/** Utility methods for working with LaTiS 2. */
object Latis2Util {

  /**
   * Get a dataset from LaTiS.
   *
   * This method will not handle any exceptions thrown by LaTiS.
   *
   * @param id dataset name
   * @param ops operations to apply to dataset
   * @return a LaTiS dataset in `F`
   */
  def getDataset[F[_]: Sync](id: String, ops: List[Operation]): F[Dataset] =
    Sync[F].delay {
      // This will not handle any exceptions thrown by LaTiS.
      DatasetAccessor.fromName(id).getDataset(ops)
    }

  /**
   * Get metadata from a LaTiS dataset.
   *
   * This is specific to the sorts of metadata HAPI cares about, not
   * all the metadata that might be available.
   *
   * @param ds dataset to get metadata for
   * @return metadata for the dataset in `F`
   */
  def getDatasetMetadata[F[_]: Sync](ds: Dataset): F[Metadata] =
    Sync[F].delay {
      ds match {
        case Dataset(f: Function) =>
          // If the domain of the function is an index, the range is
          // the time variable. Time is always projected, so this
          // would imply that the only requested variable was time.
          val tVar = f.getDomain match {
            case _: Index => f.getRange
            case d        => d
          }

          for {
            tParam <- getParameterMetadata(tVar)
            timeMd  = tVar.getMetadata
            // We are assuming no nested functions. We also need to
            // check that a scalar range variables isn't time so we
            // don't include it twice. (See above.)
            params <- f.getRange match {
              case s: Scalar if !s.hasName("time") =>
                List(getParameterMetadata(s)).sequence
              case Tuple(vs) =>
                // We are assuming no empty tuples and no nested tuples.
                vs.toList.traverse(getParameterMetadata(_))
              case _         => List().pure[F]
            }
          } yield Metadata(
            NonEmptyList(tParam, params),
            timeMd.get("min").get,
            timeMd.get("max").get,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None
          )
      }
   }.flatten

  /** Extract metadata from a parameter. */
  private def getParameterMetadata[F[_]: Sync](v: Variable): F[Parameter] =
    Sync[F].delay {
      val md = v.getMetadata

      v match {
        case v if v.hasName("time") =>
          Parameter(
            "time",
            HIsoTime,
            // This will only be correct if the time is already a HAPI
            // time string.
            md.get("length").map(_.toInt),
            "UTC",
            None,
            None,
            None,
            None
          )
        case _: Real =>
          Parameter(
            md.get("name").get,
            HDouble,
            None,
            md.get("units").get,
            None,
            None,
            None,
            None
          )
        case _: Integer =>
          Parameter(
            md.get("name").get,
            HInteger,
            None,
            md.get("units").get,
            None,
            None,
            None,
            None
          )
        case _: Text =>
          Parameter(
            md.get("name").get,
            HString,
            md.get("length").map(_.toInt),
            md.get("units").get,
            None,
            None,
            None,
            None
          )
      }
    }
}
