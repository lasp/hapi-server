package lasp.hapi.service

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.ContextShift
import cats.effect.IO
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.middleware.Logger

/**
 * A grouping of all four required HAPI endpoints.
 *
 * The HAPI spec defines four required endpoints. This class groups
 * those four endpoints and exposes them as a single service that
 * implements the HAPI spec.
 *
 * These are the routes for each service:
 *
 *  - `/hapi/capabilities`
 *  - `/hapi/catalog`
 *  - `/hapi/data`
 *  - `/hapi/info`
 *
 * @constructor Create a HAPI service with the given interpreter.
 * @param interpreter interpreter for HAPI algebras
 */
class HapiService(interpreter: HapiInterpreter[IO]) {

  private implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  private val corsConfig: CORSConfig = CORSConfig(
    anyOrigin        = true,
    anyMethod        = false,
    allowedMethods   = Set("GET").some,
    allowedHeaders   = Set("Content-Type").some,
    allowCredentials = false,
    maxAge           = 1.day.toSeconds
  )

  /** A service composed of all four required HAPI endpoints. */
  val service: HttpRoutes[IO] = {
    // If you see a red squiggly here it's probably a lie.
    val service = {
      new CapabilitiesService[IO].service         <+>
      new InfoService[IO](interpreter).service    <+>
      new CatalogService[IO](interpreter).service <+>
      new DataService[IO](interpreter).service
    }
    Logger.httpRoutes(true, false)(
      CORS(service, corsConfig)
    )
  }
}

object HapiService {

  /** Version of HAPI spec. */
  val version: String = "2.0"
}
