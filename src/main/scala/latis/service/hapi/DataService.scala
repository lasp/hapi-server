package latis.service.hapi

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits._
import fs2.Stream
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.log4s._

/** Implements the `/data` endpoint. */
class DataService[F[_]: Concurrent](
  alg: DataAlgebra[F] with InfoAlgebra[F]
) extends Http4sDsl[F] {
  import Format._
  import Include._
  import QueryDecoders._

 private[this] val logger = getLogger

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "data"
          :? DatasetMatcher(_dataset)
          +& IdMatcher(_id)
          +& StartTimeMatcher(_startTime)
          +& StopTimeMatcher(_stopTime)
          +& MinTimeMatcher(_minTime)
          +& MaxTimeMatcher(_maxTime)
          +& ParamMatcher(_params)
          +& IncludeMatcher(_inc)
          +& FormatMatcher(_fmt) =>
        val req: Either[Status, DataRequest] = {
          // For backwards compatibility, allow both
          // "dataset" or "id",
          // "start" or "time.min",
          // "stop" or "time.max"
          for {
            dataset   <- Either.fromOption(_dataset <+> _id, Status.`1400`)
            startTime <- Either.fromOption(_startTime <+> _minTime, Status.`1400`)
              .flatMap(_.leftMap(_ => Status.`1402`).toEither)
            stopTime  <- Either.fromOption(_stopTime <+> _maxTime, Status.`1400`)
              .flatMap(_.leftMap(_ => Status.`1403`).toEither)
            _         <- Either.cond(startTime.isBefore(stopTime), (), Status.`1404`)
            params    <- Either.cond(
              _params.forall(_.count(_ != "time") > 0),
              _params,
              Status.`1400`
            )
            inc       <- _inc.getOrElse(Include(false).validNel).bimap(
              _ => Status.`1410`, _.header
            ).toEither
            fmt       <- _fmt.getOrElse(Csv.validNel).bimap(
              _ => Status.`1409`, _.format
            ).toEither
          } yield DataRequest(dataset, startTime, stopTime, params, inc, fmt)
        }
        val records: EitherT[F, Status, Stream[F, String]] = for {
          req    <- EitherT.fromEither[F](req)
          dataset = req.dataset
          header <- alg.getMetadata(dataset, _params).leftMap {
            case UnknownId(_)          => Status.`1406`
            case UnknownParam(_)       => Status.`1407`
            case err @ MetadataError(_)      => logger.info(err.toString); Status.`1501`
            case err @ UnsupportedDataset(_) => logger.info(err.toString); Status.`1501`
          }.map { md =>
            "#" ++ InfoResponse(
              HapiService.version,
              Status.`1200`,
              md
            ).asJson.noSpaces
          }
          data    <- EitherT.liftF(alg.getData(req))
          records <- EitherT.pure[F, Status](alg.writeData(data))
        } yield if (req.header) {
          Stream.emit(header ++ "\n") ++ records
        } else {
          records
        }
        records.fold(
          err => err match {
            case Status.`1406` | Status.`1407` =>
              logger.info(err.message)
              NotFound(HapiError(err).asJson)
            case Status.`1501` =>
              logger.info(err.message)
              InternalServerError(HapiError(err).asJson)
            case _ =>
              logger.info(err.message)
              BadRequest(HapiError(err).asJson)
          },
          rs  => Ok(rs).map(_.withContentType(`Content-Type`(MediaType.text.csv)))
        ).flatten
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "data" :? _ =>
        logger.info(Status.`1400`.message)
        BadRequest(HapiError(Status.`1400`).asJson)
    }
}
