package latis.service.hapi

import cats.effect.Concurrent
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import latis.util.LatisConfig
import latis.util.LatisException

/** Implements the `/about` endpoint. */
class AboutService[F[_]: Concurrent] extends Http4sDsl[F] {

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "about" =>
        Ok(
          AboutResponse(
            HapiService.version,
            Status.`1200`,
            LatisConfig.get("latis.hapi.about.id").getOrElse {
              throw LatisException("No 'latis.hapi.about.id' configured")
            },
            LatisConfig.get("latis.hapi.about.title").getOrElse {
              throw LatisException("No 'latis.hapi.about.title' configured")
            },
            LatisConfig.get("latis.hapi.about.contact").getOrElse {
              throw LatisException("No 'latis.hapi.about.contact' configured")
            },
            LatisConfig.get("latis.hapi.about.description"),
            LatisConfig.get("latis.hapi.about.contactId"),
            LatisConfig.get("latis.hapi.about.citation")
          ).asJson
        )
    }
}
