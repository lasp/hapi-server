package latis.service.hapi

import cats.effect.IO
import cats.implicits.*
import org.http4s.HttpRoutes

import latis.catalog.Catalog
import latis.metadata.Metadata
import latis.model.Function
import latis.ops.OperationRegistry
import latis.time.Time

// TODO: Would this be better in latis.service?
//
// That way, the package could be:
//
// package latis.service
// package hapi
//
// No import needed, and anything else services might want imported
// could live in the latis.service package.
import latis.server.ServiceInterface

/**
 * A grouping of all five required HAPI endpoints.
 *
 * The HAPI spec defines five required endpoints. This class groups
 * together those five endpoints and a landing page and exposes them
 * as a single service that implements the HAPI spec.
 */
class HapiService(catalog: Catalog) extends ServiceInterface(catalog, OperationRegistry.empty) {

  // The catalog provided by LaTiS contains datasets that cannot be
  // represented with HAPI, so we filter those datasets out.
  //
  // This is a hack to filter out datasets that will obviously not
  // work. The long-term solution is to rework the HAPI service so
  // LaTiS datasets are turned into HAPI datasets when possible, and
  // the ability to construct a HAPI dataset serves as proof that the
  // LaTiS dataset is compatible with HAPI.
  //
  // This checks that:
  // - The Dataset metadata has temporalCoverage
  // - The single domain variable is of type Time
  // - Each scalar in the range has a supported type
  // - If the type of a scalar is string, its size is defined
  private val filteredCatalog: Catalog = {
    val covP:  Metadata => Boolean = _.getProperty("temporalCoverage").isDefined
    val typeP: Metadata => Boolean = md => md.getProperty("type").exists {
      case "string" => md.getProperty("size").isDefined
      case "double" => true
      case "int"    => true
      case "float"  => true //may be converted to double by ConvertHapiTypes
      case "short"  => true //may be converted to int by ConvertHapiTypes
      case _        => false
    }

    catalog.filter { ds =>
      covP(ds.metadata) &&
      (ds.model match {
        case Function(_: Time, range) =>
          // Note: The Time value type does not matter
          // since it will be converted via ToHapiTime.
          range.getScalars.forall { s =>
            val md = s.metadata
            typeP(md)
          }
        case _ => false
      })
    }
  }

  /** A service composed of all five required HAPI endpoints. */
  override def routes: HttpRoutes[IO] = {
    val interpreter = new Latis3Interpreter(filteredCatalog)

    val service = {
      new LandingPageService[IO](interpreter).service <+>
      new AboutService[IO].service                    <+>
      new CapabilitiesService[IO].service             <+>
      new InfoService[IO](interpreter).service        <+>
      new CatalogService[IO](interpreter).service     <+>
      new DataService[IO](interpreter).service
    }

    service
  }
}

object HapiService {

  /** Version of HAPI spec. */
  val version: String = "3.0"
}
