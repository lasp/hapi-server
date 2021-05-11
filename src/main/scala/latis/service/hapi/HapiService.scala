package latis.service.hapi

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.ContextShift
import cats.effect.IO
import cats.implicits._
import org.http4s.HttpRoutes

import latis.catalog.Catalog

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
 * those five endpoints into a landing page and exposes them as a
 * single service that implements the HAPI spec.
 */
class HapiService(catalog: Catalog) extends ServiceInterface(catalog) {

  // TODO: We want to get this from the server.
  private implicit val cs: ContextShift[IO] =
    IO.contextShift(global)

  /** A service composed of all five required HAPI endpoints. */
  override def routes: HttpRoutes[IO] = {
    val interpreter = new Latis3Interpreter(catalog)

    val service = {
      new LandingPageService[IO].service          <+>
      new AboutService[IO].service                <+>
      new CapabilitiesService[IO].service         <+>
      new InfoService[IO](interpreter).service    <+>
      new CatalogService[IO](interpreter).service <+>
      new DataService[IO](interpreter).service
    }

    service
  }
}

object HapiService {

  /** Version of HAPI spec. */
  val version: String = "3.0"
}
