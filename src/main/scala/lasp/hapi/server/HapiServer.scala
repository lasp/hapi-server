package lasp.hapi.server

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect._
import cats.implicits._
import fs2.Stream
import fs2.StreamApp
import fs2.StreamApp.ExitCode
import org.http4s._
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig

import lasp.hapi.service.CapabilitiesService
import lasp.hapi.service.CatalogService
import lasp.hapi.service.DataService
import lasp.hapi.service.HapiService
import lasp.hapi.service.InfoService

/**
 * The HAPI server in IO.
 *
 * This is the entry point for the HAPI server.
 */
object HapiServer extends HapiServerApp[IO] {

  /** Version of HAPI spec. */
  val version: String = "2.0"
}

/** The HAPI server parameterized over the effect type. */
abstract class HapiServerApp[F[_]: Effect] extends StreamApp[F] {

  val corsConfig: CORSConfig = CORSConfig(
    anyOrigin        = true,
    anyMethod        = false,
    allowedMethods   = Set("GET").some,
    allowedHeaders   = Set("Content-Type").some,
    allowCredentials = false,
    maxAge           = 1.day.toSeconds
  )

  /** A service composed of all four endpoints. */
  val endpoints: HttpService[F] = {
    // If you see a red squiggly here it's probably a lie.
    val service = {
      new CapabilitiesService[F].service <+>
      new InfoService[F].service         <+>
      new CatalogService[F].service      <+>
      new DataService[F].service
    }
    CORS(service, corsConfig)
  }

  val landingPage: HttpService[F] =
    new HapiService[F]().service

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(landingPage, "/")
      .mountService(endpoints, "/hapi/")
      .serve
}
