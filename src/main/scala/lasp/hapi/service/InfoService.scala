package lasp.hapi.service

import cats.effect.Effect
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/info` endpoint. */
class InfoService[F[_]: Effect] extends Http4sDsl[F] {
  import QueryDecoders._

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "info"
          :? IdMatcher(_)
          +& ParamMatcher(_) =>
        Ok("Hello from HAPI!")
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "info" :? _ =>
        BadRequest(Status.`1400`.asJson)
    }
}
