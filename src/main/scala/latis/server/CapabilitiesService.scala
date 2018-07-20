package latis.server

import cats.effect.Effect
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

/** Implements the `/capabilities` endpoint. */
class CapabilitiesService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "capabilities" =>
        Ok("Hello from HAPI!")
    }
}
