# <img align="right" src="https://lasp.colorado.edu/media/projects/lasp/images/logo/2012/color/lasp-logo.color.transp-bg.small.png" height="75" width="256"/> LASP HAPI Server

A [HAPI][hapi] server built on [LaTiS][latis]. This project is based
on LaTiS v3, which is currently under development.

HAPI (Heliophysics API) is an interoperable, REST-style interface to
timeseries data. We implement [version 3.0][hapi3] of the
specification.

LaTiS is a library for describing and manipulating scientific data. By
building a HAPI server on LaTiS, we can reuse existing LaTiS
capabilities to make datasets in a wide variety of formats available
through a HAPI interface.

[hapi]: https://hapi-server.github.io/
[hapi3]: https://github.com/hapi-server/data-specification/blob/master/hapi-3.0.0/HAPI-data-access-spec-3.0.0.md
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
[about]: https://github.com/hapi-server/data-specification/blob/master/hapi-3.0.0/HAPI-data-access-spec-3.0.0.md#33-about

## Configuring Datasets

See [Configuring Datasets](docs/configuring-datasets.md) for an
example of creating and validating FDML files.

## Configuring the /about Endpoint

The `/about` endpoint provides metadata to describe this server.
**The endpoint will fail to load if values are not set for its three
required parameters `id`, `title`, and `contact`.** You can optionally
set values for `description`, `contactId`, and `citation` as well.
See the [HAPI spec][about] for details about these parameters.

They are namespaced with `latis.hapi.about.<param>`,
so to set a value for `id`, for example, configure the
`latis.hapi.about.id` variable. There are three main ways to configure 
variables:
 - configuration file
 - environment variables
 - Java properties

To configure them with a file, create a `latis.conf` file and start the
server with the `-J-Dconfig.file=<path to latis.conf>` flag.

Here is an example `latis.conf`:

```
# latis.conf

latis {
  # Set /about endpoint values:
  hapi {
    about {
      id = "SERVER"
      title = "Your Server"
      contact = "your@email.here"
    }
  }

  # Enable the HAPI endpoint:
  services = [
    {type: "class", mapping: "/hapi", class: "latis.service.hapi.HapiService"}
  ]
}
```

 

## Other Configuration

This server will run on port 8080 and serve datasets out of
`./datasets` by default. You can change the
port, location of FDML files, or enable additional endpoints with 
configuration options.

Again, to configure the server, create a `latis.conf` file and start the
server with the `-J-Dconfig.file=<path to latis.conf>` flag.

Here is another example `latis.conf`:

```
# latis.conf

latis {
  # Set the port:
  port = 8090

  # Set the directory containing FDML files:
  fdml.dir = <path to FDML files>
  
  # Set /about endpoint values:
  hapi {
    about {
      id = "SERVER"
      title = "Your Server"
      contact = "your@email.here"
    }
  }

  # Enable the DAP2 endpoint:
  services = [
    {type: "class", mapping: "/hapi", class: "latis.service.hapi.HapiService"},
    {type: "class", mapping: "/dap", class: "latis.service.dap2.Dap2Service"}
  ]
}
```
