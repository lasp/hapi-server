package lasp.hapi.server

import scala.concurrent.ExecutionContext

import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import pureconfig.generic.auto._
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
object HapiServer extends HapiServerApp(
  new lasp.hapi.service.latis2.Latis2Interpreter[IO]
)

abstract class HapiServerApp(interpreter: HapiInterpreter[IO]) extends IOApp {
  private val hapiService: HttpRoutes[IO] =
    new HapiService(interpreter).service

  private val landingPage: HttpRoutes[IO] =
    new LandingPageService[IO]().service

  private val service: HttpRoutes[IO] =
    landingPage <+> hapiService

  private def config(blocker: Blocker): IO[HapiConfig] = for {
    config <- loadConfigF[IO, HapiConfig](blocker)
    // For LaTiS 2.
    _      <- IO {
      System.setProperty("dataset.dir", config.catalogDir)
    }
  } yield config

  override def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use { blocker =>
      config(blocker).flatMap {
        case HapiConfig(mapping, port, _) =>
          BlazeServerBuilder[IO](ExecutionContext.global)
            .bindHttp(port, "0.0.0.0")
            .withHttpApp {
              Router(mapping -> service).orNotFound
            }
            .serve
            .compile
            .drain
      }.as(ExitCode.Success)
    }
}
