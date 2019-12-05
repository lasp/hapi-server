# <img align="right" src="http://lasp.colorado.edu/home/wp-content/uploads/2012/01/lasp-logo.color_.1line-subtext.transp-bg.med_.png" height="75" width="256"/> LASP HAPI Server

A [HAPI][hapi] server built on [LaTiS][latis]. This project is based
on LaTiS v3, which is currently under development.

HAPI (Heliophysics API) is an interoperable, REST-style interface to
timeseries data. We implement [version 2.1][hapi2] of the
specification.

LaTiS is a library for describing and manipulating scientific data. By
building a HAPI server on LaTiS, we can reuse existing LaTiS
capabilities to make datasets in a wide variety of formats available
through a HAPI interface.

[hapi]: https://hapi-server.github.io/
[hapi2]: https://github.com/hapi-server/data-specification/blob/master/hapi-2.1.0/HAPI-data-access-spec-2.1.0.md
[latis]: https://github.com/latis-data/latis3

## Getting Started

First, download the [latest release][releases] of the `latis-hapi`
script.

By default, running this script will start a HAPI server running on
port 8080. It will look for FDML files in a directory called
`datasets` in its working directory.

Visit `http://localhost:8080/hapi/catalog` to verify that your
datasets are visible to the server.

[releases]: https://github.com/lasp/hapi-server/releases

## Configuring Datasets

See [Configuring Datasets](docs/configuring-datasets.md) for an
example of creating and validating FDML files.

## Configuration

This server will run on port 8080 and serve datasets out of
`./datasets` by default. Configuration is only necessary to change the
port, location of FDML files, or enable additional endpoints.

To configure the server, create a `latis.conf` file and start the
server with the `-J-Dconfig.file=<path to latis.conf>` flag.

Here is an example `latis.conf`:

```
# latis.conf

latis {
  # Set the port:
  port = 8090

  # Set the directory containing FDML files:
  fdml.dir = <path to FDML files>

  # Enable the DAP2 endpoint:
  services = [
    {type: "class", mapping: "/hapi", class: "latis.service.hapi.HapiService"},
    {type: "class", mapping: "/dap", class: "latis.service.dap2.Dap2Service"}
  ]
}
```
