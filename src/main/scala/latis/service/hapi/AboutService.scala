package latis.service.hapi

import cats.effect.Concurrent
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/about` endpoint. */
class AboutService[F[_]: Concurrent] extends Http4sDsl[F] {

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "about" =>
        //TODO: handle optional params
        Ok(
          AboutResponse(
            HapiService.version,
            Status.`1200`,
            "LASP",
            "LASP HAPI Server",
            "web.support@lasp.colorado.edu"
          ).asJson
        )
    }
}
