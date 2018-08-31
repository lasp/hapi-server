# LASP HAPI Server

A [HAPI][hapi] server built on LaTiS.

[hapi]: https://hapi-server.github.io/

## Running with Docker

The `docker` SBT task will build a Docker image. The Docker daemon
must be running.

Then run the container:

```
$ docker run -p 8080:8080 <IMAGE ID>
```

The endpoints will be available at `http://localhost:8080/`.

## Running with an executable JAR

The `assembly` SBT task will build an executable JAR.

Then run the JAR:

```
$ java -jar <JAR>
```

The endpoints will be available at `http://localhost:8080/`.
