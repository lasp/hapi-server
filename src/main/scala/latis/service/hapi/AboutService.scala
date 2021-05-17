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
            LatisConfig.get("latis.about.id").getOrElse(throw LatisException("No 'id' configured")),
            LatisConfig.get("latis.about.title").getOrElse(throw LatisException("No 'title' configured")),
            LatisConfig.get("latis.about.contact").getOrElse(throw LatisException("No 'contact' configured")),
            LatisConfig.get("latis.about.description"),
            LatisConfig.get("latis.about.contactId"),
            LatisConfig.get("latis.about.citation")
          ).asJson
        )
    }
}
