# <img align="right" src="http://lasp.colorado.edu/home/wp-content/uploads/2012/01/lasp-logo.color_.1line-subtext.transp-bg.med_.png" height="75" width="256"/> LASP HAPI Server

A [HAPI][hapi] server built on [LaTiS][latis].

HAPI (Heliophysics API) is an interoperable, REST-style interface to
timeseries data. We implement [version 2.0][hapi2] of the
specification.

LaTiS is a library for describing and manipulating scientific data. By
building a HAPI server on LaTiS, we can

- use existing LaTiS adapters for a variety of data formats (ASCII,
  NetCDF, FITS, CDF, relational databases, etc.)
- use existing LaTiS operations (filtering, subsetting, time and
  format conversions, etc.)
- aggregate granules (e.g., a collection of files) into virtual
  datasets

to make datasets in a wide variety of formats available through a HAPI
interface.

[hapi]: https://hapi-server.github.io/
[hapi2]: https://github.com/hapi-server/data-specification/blob/master/hapi-2.0.0/HAPI-data-access-spec-2.0.0.md
[latis]: http://lasp.colorado.edu/home/mission-ops-data/tools-and-technologies/latis/

## Configuring Datasets

See [Configuring Datasets](docs/configuring-datasets.md) for an
example.

## Running with Docker

The `docker` SBT task will build a Docker image. The Docker daemon
must be running.

Then run the container:

```
$ docker run -p 8080:8080 --mount type=bind,src=<CATALOG DIR>,dst=/srv/hapi <IMAGE ID>
```

The landing page will be available at `http://localhost:8080/hapi`
with the default configuration.

## Running with an executable JAR

The `assembly` SBT task will build an executable JAR.

Then run the JAR:

```
$ java -jar <JAR>
```

Or with a specific configuration file:

```
$ java -Dconfig.file=<CONF> -jar <JAR>
```

The landing page will be available at `http://localhost:8080/hapi`
with the default configuration.

## Configuration

There are two methods for configuring the HAPI server:

1. [HOCON][hocon] configuration file
2. Environment variables

### Configuration Options

| Configuration key | Environment variable | Description                  | Default          |
| ----------------- | -------------------- | ---------------------------- | ---------------- |
| `port`            | `HAPI_PORT`          | Port to listen on            | 8080             |
| `mapping`         | `HAPI_MAPPING`       | URL segment before `hapi` (e.g., `"/"` → `/hapi`, `"/foo/"` → `/foo/hapi`) | "/" |
| `catalog-dir`     | `HAPI_CATALOG`       | Catalog directory            | `./catalog` (JAR); `/srv/hapi` (Docker) |

[hocon]: https://github.com/lightbend/config/blob/master/HOCON.md
