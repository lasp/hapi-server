package latis.service.hapi

import cats.data.EitherT
import cats.data.NonEmptyList
import cats.effect.Concurrent
import cats.implicits._
import fs2.Stream
import io.circe.syntax._
import org.http4s.{Status => _, _}
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

  private def req2Resp(req: DataRequest, _params:Option[NonEmptyList[String]]): EitherT[F, Status, Response[F]] = {
    val dataset = req.dataset
    for {
      header <- alg.getMetadata(dataset, _params).leftMap {
        case UnknownId(_) => Status.`1406`
        case UnknownParam(_) => Status.`1407`
        case err@MetadataError(_) => logger.info(err.toString); Status.`1501`
        case err@UnsupportedDataset(_) => logger.info(err.toString); Status.`1501`
      }.map { md =>
        "#" ++ InfoResponse(
          HapiService.version,
          Status.`1200`,
          md
        ).asJson.noSpaces
      }
      data <- EitherT.liftF(alg.getData(req))
      resp <- req.format match {
        case Csv =>
          val records = alg.streamCsv(data)
          val stream: Stream[F, Byte] = if(req.header) Stream.emits((header + "\n").getBytes("UTF-8")) ++ records else records
          EitherT.right[Status](Ok(stream).map(_.withContentType(`Content-Type`(MediaType.text.csv))))
        case Binary =>
          val records = alg.streamBinary(data)
          val stream: Stream[F, Byte] = if(req.header) Stream.emits(header.getBytes("UTF-8")) ++ records else records
          EitherT.right[Status](Ok(stream).map(_.withContentType(`Content-Type`(MediaType.application.`octet-stream`))))
      }
    } yield resp
  }

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
            fmt       <- _fmt.getOrElse(Csv.validNel).leftMap(
              _ => Status.`1409`
            ).toEither
          } yield DataRequest(dataset, startTime, stopTime, params, inc, fmt)
        }
        EitherT.fromEither[F](req).flatMap(req2Resp(_, _params)).leftSemiflatMap {
          case err@(Status.`1406` | Status.`1407`) =>
            logger.info(err.message)
            NotFound(HapiError(err).asJson)
          case err@Status.`1501` =>
            logger.info(err.message)
            InternalServerError(HapiError(err).asJson)
          case err =>
            logger.info(err.message)
            BadRequest(HapiError(err).asJson)
        }.merge
      // Return a 1400 error if the required parameters are not given.
      case GET -> Root / "data" :? _ =>
        logger.info(Status.`1400`.message)
        BadRequest(HapiError(Status.`1400`).asJson)
    }
}
