package latis.server

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect._
import cats.implicits._
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s._
import org.http4s.server.blaze._

/**
 * The HAPI server in IO.
 *
 * This is the entry point for the HAPI server.
 */
object HapiServer extends HapiServerApp[IO]

/** The HAPI server parameterized over the effect type. */
abstract class HapiServerApp[F[_]: Effect] extends StreamApp[F] {

  /** A service composed of all four endpoints. */
  val endpoints: HttpService[F] = {
    // If you see a red squiggly here it's probably a lie.
    new CapabilitiesService[F].service <+>
    new InfoService[F].service         <+>
    new CatalogService[F].service      <+>
    new DataService[F].service
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
