package lasp.hapi.service

import cats.effect.Effect
import cats.implicits._
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.log4s._

/** Implements the `/info` endpoint. */
class InfoService[F[_]: Effect](alg: InfoAlgebra[F]) extends Http4sDsl[F] {
  import QueryDecoders._

  private[this] val logger = getLogger

  val service: HttpService[F] =
    HttpService[F] {
      case GET -> Root / "hapi" / "info"
          :? IdMatcher(id)
          +& ParamMatcher(params) =>
        val ps = params.map(_.distinct)
        alg.getMetadata(id, ps).leftMap {
          case UnknownId(_)    => Status.`1406`
          case UnknownParam(_) => Status.`1407`
        }.fold(
          err => {
            logger.info(err.message)
            NotFound(HapiError(err).asJson)
          },
          res => Ok(
            InfoResponse(HapiService.version, Status.`1200`, res).asJson
          )
        ).flatten
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "hapi" / "info" :? _ =>
        logger.info(Status.`1400`.message)
        BadRequest(HapiError(Status.`1400`).asJson)
    }
}
