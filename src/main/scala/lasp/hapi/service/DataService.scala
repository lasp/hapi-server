package lasp.hapi.service

import cats.effect.Effect
import cats.implicits._
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
          :? IdMatcher(_id)
          +& MinTimeMatcher(_minTime)
          +& MaxTimeMatcher(_maxTime)
          +& ParamMatcher(_params)
          +& IncludeMatcher(_inc)
          +& FormatMatcher(_fmt) =>
        val req: Either[Status, DataRequest] =
          for {
            id      <- _id.asRight
            minTime <- _minTime.leftMap(_ => Status.`1402`).toEither
            maxTime <- _maxTime.leftMap(_ => Status.`1403`).toEither
            _       <- Either.cond(minTime.isBefore(maxTime), (), Status.`1404`)
            params  <- _params.asRight
            inc     <- _inc.getOrElse(Include(false).validNel).bimap(
              _ => Status.`1410`, _.header
            ).toEither
            fmt     <- _fmt.getOrElse(Format("csv").validNel).bimap(
              _ => Status.`1409`, _.format
            ).toEither
          } yield DataRequest(id, minTime, maxTime, params, inc, fmt)
        req.fold(
          err => BadRequest(HapiError(err).asJson),
          _   => Ok("Hello from HAPI!")
        )
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "hapi" / "data" :? _ =>
        BadRequest(HapiError(Status.`1400`).asJson)
    }
}
