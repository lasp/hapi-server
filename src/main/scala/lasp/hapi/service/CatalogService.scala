package lasp.hapi.service

import cats.effect.Effect
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

/** Implements the `/catalog` endpoint. */
class CatalogService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "hapi" / "catalog" =>
        Ok("Hello from HAPI!")
    }
}
