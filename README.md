# LASP HAPI Server

A [HAPI][hapi] server built on LaTiS.

[hapi]: https://hapi-server.github.io/

## Configuration

There are two methods for configuring the HAPI server:

1. [HOCON][hocon] configuration file
2. Environment variables

### Configuration Options

| Configuration key | Environment variable | Description                  | Default          |
| ----------------- | -------------------- | ---------------------------- | ---------------- |
| `port`            | `HAPI_PORT`          | Port to listen on            | 8080             |
| `mapping`         | `HAPI_MAPPING`       | URL segment before `/hapi`   | "/"              |
| `catalog-dir`     | `HAPI_CATALOG`       | Catalog directory            | `./catalog` (JAR); `/srv/hapi` (Docker) |

[hocon]: https://github.com/lightbend/config/blob/master/HOCON.md

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
