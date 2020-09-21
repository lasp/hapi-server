package lasp.hapi.service

import cats.effect.Effect
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/capabilities` endpoint. */
class CapabilitiesService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "hapi" / "capabilities" =>
        Ok(
          Capabilities(
            HapiService.version,
            Status.`1200`,
            List("csv")
          ).asJson
        )
    }
}
