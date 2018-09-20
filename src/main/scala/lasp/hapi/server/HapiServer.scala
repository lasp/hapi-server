package lasp.hapi.server

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect._
import cats.implicits._
import fs2.Stream
import fs2.StreamApp
import fs2.StreamApp.ExitCode
import org.http4s.HttpService
import org.http4s.server.blaze._
import pureconfig.module.catseffect._

import lasp.hapi.service.HapiInterpreter
import lasp.hapi.service.HapiService
import lasp.hapi.service.LandingPageService
import lasp.hapi.util.HapiConfig

/**
 * The HAPI server in IO.
 *
 * This is the entry point for the HAPI server.
 */
object HapiServer extends HapiServerApp[IO]

/** The HAPI server parameterized over the effect type. */
abstract class HapiServerApp[F[_]: Effect] extends StreamApp[F] {

  private val hapiService: HttpService[F] =
    new HapiService(HapiInterpreter.noopInterpreter[F]).service

  private val landingPage: HttpService[F] =
    new LandingPageService().service

  private val service: HttpService[F] =
    landingPage <+> hapiService

  val config: F[HapiConfig] = for {
    config <- loadConfigF[F, HapiConfig]
    // For LaTiS 2.
    _      <- Effect[F].delay {
      System.setProperty("dataset.dir", config.catalogDir)
    }
  } yield config

  override def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
    Stream.eval(config).flatMap {
      case HapiConfig(mapping, port, _) =>
        BlazeBuilder[F]
          .bindHttp(port, "0.0.0.0")
          .mountService(service, mapping)
          .serve
    }
}
