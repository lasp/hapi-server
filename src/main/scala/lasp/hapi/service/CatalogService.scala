package lasp.hapi.service

import cats.effect.Effect
import cats.implicits._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/**
 * Implements the `/catalog` endpoint.
 *
 * @constructor Create a `CatalogService` with an interpreter for the
 *              catalog algebra.
 * @param alg interpreter for catalog algebra
 */
class CatalogService[F[_]: Effect](alg: CatalogAlgebra[F]) extends Http4sDsl[F] {

  val service: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "hapi" / "catalog" =>
        val res = for {
          version <- HapiService.version.pure[F]
          status  <- Status.`1200`.pure[F]
          catalog <- alg.getCatalog
        } yield Catalog(version, status, catalog)
        res.flatMap(catalog => Ok(catalog.asJson))
    }
}
