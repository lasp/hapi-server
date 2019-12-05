package latis.service.hapi

/** Base trait for errors returned by the info service. */
sealed trait InfoError

/** Error for unknown dataset IDs. */
final case class UnknownId(id: String) extends InfoError

/** Error for unknown dataset parameters. */
final case class UnknownParam(param: String) extends InfoError

/** Error for misconfigured metadata. */
final case class MetadataError(msg: String) extends InfoError

/** Error for unsupported datasets. */
final case class UnsupportedDataset(msg: String) extends InfoError
