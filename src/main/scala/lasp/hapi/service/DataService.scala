package lasp.hapi.service

import cats.effect.Effect
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Implements the `/data` endpoint. */
class DataService[F[_]: Effect] extends Http4sDsl[F] {
  import Format._
  import Include._
  import QueryDecoders._

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "hapi" / "data"
          :? IdMatcher(_)
          +& MinTimeMatcher(_)
          +& MaxTimeMatcher(_)
          +& ParamMatcher(_)
          +& IncludeMatcher(_)
          +& FormatMatcher(_) =>
        Ok("Hello from HAPI!")
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "hapi" / "data" :? _ =>
        BadRequest(Status.`1400`.asJson)
    }
}
