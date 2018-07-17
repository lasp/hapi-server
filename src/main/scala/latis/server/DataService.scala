package latis.server

import cats.effect.Effect
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

/** Implements the `/data` endpoint. */
class DataService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "data" =>
        Ok("Hello from HAPI!")
    }
}
