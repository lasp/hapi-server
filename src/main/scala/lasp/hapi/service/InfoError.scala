package lasp.hapi.service

/** Base trait for errors returned by the info service. */
sealed abstract trait InfoError

/** Error for unknown dataset IDs. */
final case class UnknownId(id: String) extends InfoError

/** Error for unknown dataset parameters. */
final case class UnknownParam(param: String) extends InfoError
