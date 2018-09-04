package lasp.hapi.service
package latis2

import cats.effect.Sync
import latis.dm
import latis.dm.{ Dataset => _, _ }
import latis.reader.CatalogReader

/**
 * Interpreter for HAPI algebras using LaTiS 2.
 *
 * @constructor Create an interpreter that reads the catalog from the
 *              given directory.
 * @param dir directory that is the root of the catalog
 */
class Latis2Interpreter[F[_]: Sync](dir: String) extends HapiInterpreter[F] {

  override val getCatalog: F[List[Dataset]] =
    Sync[F].delay {
      new CatalogReader(dir).getDataset() match {
        case dm.Dataset(Function(it)) =>
          it.flatMap {
            case Sample(_, Function(it)) =>
              it.map {
                case Sample(Text(name), _) =>
                  Dataset(name)
              }
          }.toList
      }
    }
}
