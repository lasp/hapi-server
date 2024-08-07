package latis.service.hapi

import scala.collection.immutable.ListMap

import cats.data.EitherT
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.*
import fs2.Stream
import fs2.data.json
import fs2.data.json.circe.tokenizerForEncoder
import fs2.text.utf8
import io.circe.Json
import io.circe.JsonObject

import latis.catalog.Catalog
import latis.model.DoubleValueType
import latis.model.FloatValueType
import latis.model.Function
import latis.model.IntValueType
import latis.model.Scalar
import latis.model.ShortValueType
import latis.model.StringValueType
import latis.model.ValueType
import latis.ops.ConvertHapiTypes
import latis.ops.Projection
import latis.ops.Selection
import latis.ops.ToHapiTime
import latis.ops.UnaryOperation
import latis.output.BinaryEncoder
import latis.output.CsvEncoder
import latis.output.JsonEncoder
import latis.time.{Time as LTime}
import latis.util.HapiUtils.*
import latis.util.Identifier
import latis.util.Identifier.*
import latis.util.dap2.parser.ast.*
import latis.util.hapi.DataCodec

class Latis3Interpreter(catalog: Catalog) extends HapiInterpreter[IO] {

  type T = latis.dataset.Dataset

  override val getCatalog: IO[List[Dataset]] =
    catalog.datasets.compile.toList.map { dss =>
      dss.map { ds =>
        val id = ds.id.fold("")(_.asString)
        val title = ds.metadata.getProperty("title").getOrElse(id)
        Dataset(id, title)
      }
    }

  override def getMetadata(
    id: String,
    params: Option[NonEmptyList[String]]
  ): EitherT[IO, InfoError, Metadata] =
    for {
      // Check that the dataset ID is in the catalog, short-circuit
      // with UnknownId if not found.
      catalog  <- EitherT.liftF(getCatalog)
      _        <- EitherT.cond[IO](
        catalog.exists(_.id === id),
        (), UnknownId(id)
      )
      dataset  <- EitherT.liftF(getDataset(id, List()))
      // Check for parameters that were requested but not returned.
      // This would indicate that the parameter wasn't found, so we
      // should return an error.
      missing   = params.flatMap(_.find(! hasVariable(dataset, _)))
      _        <- EitherT.fromOption[IO](missing.map(UnknownParam.apply), ()).swap
      // Get the metadata.
      metadata <- EitherT.fromEither[IO](getDatasetMetadata(dataset))
    } yield params.map { ps =>
      // Filter the returned parameters. We wait until the end to do
      // this because we need to keep all the parameters in order to
      // get all the metadata. (Specifically, for making bins.)
      val newParams = metadata.parameters.filter {
        case Parameter("time", _, _, _, _, _, _, _) => true
        case Parameter(name, _, _, _, _, _, _, _)   => ps.exists(_ === name)
      }
      // This should be safe because we should at least have time.
      metadata.copy(parameters = newParams.toNel.get)
    }.getOrElse(metadata)

  override def getData(req: DataRequest): IO[T] = req match {
    // Header and format are handled by DataService.
    case DataRequest(id, startT, stopT, params, _, _) =>
      val ops: List[UnaryOperation] = List(
        Selection(id"time", GtEq, s"$startT"),
        Selection(id"time", Lt, s"$stopT")
      ) ++ makeProjection(params)

      getDataset(id, ops)
  }

  override def streamCsv(data: T): Stream[IO, Byte] = {
    val x = CsvEncoder().encode(data)
    x.through(fs2.text.utf8.encode)
  }

  override def streamBinary(data: T): Stream[IO, Byte] = {
    val enc: BinaryEncoder = new BinaryEncoder(DataCodec.hapiCodec)
    // Apply type conversion then encode.
    // This should be consistent with the HapiService.filteredCatalog.
    enc.encode(data.withOperation(new ConvertHapiTypes()))
  }

  override def streamJson(data: T, header: JsonObject): Stream[IO, Byte] = {
    val dataJson = new JsonEncoder().encode(data)
    val headerList = header.add("format", Json.fromString("json")).toList
    val map = ListMap(headerList: _*)
    dataJson
      .through(json.ast.tokenize)
      .through(json.wrap.asArrayInObject(at = "data", in = map))
      .through(json.render.pretty())
      .through(utf8.encode)
  }

  private def getDataset(id: String, ops: List[UnaryOperation]): IO[T] = for {
    ident   <- IO.fromOption(Identifier.fromString(id))(UnknownId(id))
    dataset <- catalog.findDataset(ident).flatMap {
      case Some(ds) => ds.pure[IO]
      case None     => IO.raiseError(UnknownId(id))
    }
  } yield ops.foldLeft(dataset)(_.withOperation(_)).withOperation(new ToHapiTime)

  private def getDatasetMetadata(ds: T): Either[InfoError, Metadata] = for {
    model  <- Either.catchNonFatal(ds.model).leftMap(_ => UnsupportedDataset(""))
    tVar   <- model match {
      case Function(t: LTime, _) => Right(t)
      case _                     => Left(UnsupportedDataset(""))
    }
    params <- getParameterMetadata(ds)
    psNel  <- Either.fromOption(
      NonEmptyList.fromList(params),
      UnsupportedDataset("")
    )
    tCov   <- Either.fromOption(
      ds.metadata.getProperty("temporalCoverage"),
      MetadataError("Dataset missing metadata property 'temporalCoverage'")
    )
    tCovP  <- parseCoverage(tCov)
  } yield Metadata(
    psNel,
    tCovP._1,
    tCovP._2,
    tVar.metadata.getProperty("tuneStampLocation").flatMap(TimeStampLocation(_)),
    tVar.metadata.getProperty("cadence"),
    ds.metadata.getProperty("sampleStartDate"),
    ds.metadata.getProperty("sampleStopDate"),
    ds.metadata.getProperty("description"),
    ds.metadata.getProperty("resourceURL"),
    ds.metadata.getProperty("creationDate"),
    ds.metadata.getProperty("modificationDate"),
    ds.metadata.getProperty("contact"),
    ds.metadata.getProperty("concactID")
  )

  def getParameterMetadata(ds: T): Either[InfoError, List[Parameter]] =
    ds.model match {
      case Function(t: Scalar, d) => for {
        rMd <- d.getScalars.traverse(getScalarMetadata)
      } yield getTimeMetadata(t) :: rMd
      case _ => Left(UnsupportedDataset(""))
    }

  private def getScalarMetadata(s: Scalar): Either[InfoError, Parameter] = for {
    ty <- Either.fromOption(
      toDataType(s.valueType),
      MetadataError("Parameter has unsupported value for metadata property 'type'")
    )
  } yield Parameter(
    s.id.asString,
    ty,
    None,
    s.units,
    None,
    s.metadata.getProperty("missing_value"),
    s.metadata.getProperty("description"),
    None
  )

  private def getTimeMetadata(dt: Scalar): Parameter = Parameter(
    "time",
    HIsoTime,
    Option(24),
    "UTC".some,
    None,
    dt.metadata.getProperty("missing_value"),
    dt.metadata.getProperty("description"),
    None
  )

  // This is for things other than Time, which is handled elsewhere.
  // This needs to be consistent with the HapiService.filteredCatalog
  // and the ConvertHapiTypes operation.
  private def toDataType(vtype: ValueType): Option[DataType] = vtype match {
    case StringValueType => HString.some
    case DoubleValueType => HDouble.some
    case FloatValueType  => HDouble.some
    case IntValueType    => HInteger.some
    case ShortValueType  => HInteger.some
    case _               => None
  }

  private def makeProjection(params: Option[NonEmptyList[String]]): List[UnaryOperation] =
    params.map { ps =>
      val names: List[String] = "time" :: ps.toList
      List(Projection.fromArgs(names).fold(throw _, identity))
    }.getOrElse(List.empty)

  private def hasVariable(ds: T, vname: String): Boolean = Identifier.fromString(vname)
    .fold(false)(ds.model.findVariable(_).isDefined)

}
