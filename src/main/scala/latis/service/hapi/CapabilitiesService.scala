package latis.service.hapi

import cats.effect.Concurrent
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/capabilities` endpoint. */
class CapabilitiesService[F[_]: Concurrent] extends Http4sDsl[F] {

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "capabilities" =>
        Ok(
          Capabilities(
            HapiService.version,
            Status.`1200`,
            List("csv", "binary")
          ).asJson
        )
    }
}
