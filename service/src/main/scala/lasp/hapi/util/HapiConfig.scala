package lasp.hapi.util

/**
 * Configuration for HAPI
 *
 * @param mapping URL component before /hapi
 * @param port port to listen on
 * @param catalogDir directory containing catalog
 */
final case class HapiConfig(
  mapping: String,
  port: Int,
  catalogDir: String
)
