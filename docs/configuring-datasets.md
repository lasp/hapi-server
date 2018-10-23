# Configuring Datasets

This guide describes how to configure datasets to be served by the
LASP HAPI server.

See the [README][readme] for information on downloading and
configuring the server.

## TSML

The LASP HAPI server is backed by [LaTiS 2][latis-2], a library for
modeling, manipulating, and serving data.

LaTiS 2 uses an XML file format called TSML to describe data so it can
be served. Users of the LASP HAPI server will need to provide TSML
descriptors for the datasets they wish to serve.

## Describing a Simple Dataset

Suppose we have a simple dataset we wish to serve: a single variable
as a function of time, stored in a [CSV file][csv].

```
#time,value
2018-01-01T00:00:00Z,0
2018-01-01T00:00:01Z,1
2018-01-01T00:00:02Z,2
```

We will need to provide a TSML file that describes this dataset.

### Introducing a Dataset

First, here's an example of a bare-bones TSML file for a dataset we
will call `simple_dataset`:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset">

    <!-- We will need to put stuff here. -->

</dataset>
```

The `dataset` tag introduces a dataset. The `id` attribute gives the
dataset a name. We will call our simple CSV dataset `simple_dataset`.

### Specifying an Adapter

Next, we need to specify how to read the dataset. LaTiS uses
*adapters* to read data in various formats. Because we're working with
plain text, we can use the ASCII adapter.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset">

    <!-- We've specified how to read the data. -->
    <adapter class="latis.reader.tsml.AsciiAdapter"
             location="${dataset.dir}/simple_dataset.csv"
             delimiter=","
             commentCharacter="#"/>

    <!-- There is still more to add here. -->

</dataset>
```

We specify information about the adapter with the `adapter` tag.

Adapters have options that are specified as attributes in the tag.
Each `adapter` tag has a `class` attribute that specifies the adapter
to use. In our case, the ASCII adapter is implemented by the
`latis.reader.tsml.AsciiAdapter` class.

The other attributes depend on the adapter being used. We've set the
following for the ASCII adapter:

- `location` : Specifies the location of our CSV file.
  - Note that `${dataset.dir}` is expanded to where the server is
    configured to look for datasets. (See the [README][readme] for
    those configuration options.)
- `delimiter` : The character that separates data values/columns.
- `commentCharacter` : The character that indicates header/comment
  lines.

### Specifying the Model

Next, we need to describe the shape of the data. LaTiS models data
using the [functional data model][fdm], so we need to describe the data in
terms of that model.

The gist of the functional data model is that we have three types of
things to work with:

- *scalars* : A single variable. (e.g. time, flux, temperature)
- *tuples* : A collection of variables. (e.g. a wind vector, a pair of
  flux and temperature)
- *functions* : A mapping from a domain (independent) variable to a
  range (dependent) variable. (e.g. temperature as a function of time)

For our example dataset we know we have two scalars: `time` and
`value`. We also know that `value` is a function of `time`. We encode
that in TSML like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset">

    <adapter class="latis.reader.tsml.AsciiAdapter"
             location="${dataset.dir}/simple_dataset.csv"
             delimiter=","
             commentCharacter="#"/>

    <!-- We've described the shape of the data. -->
    <time type="text">
        <!-- There is still more to add here. -->
    </time>

    <integer id="value">
        <!-- There is still more to add here. -->
    </integer>

</dataset>
```

The `time` tag adds a time variable, and the `type` attribute
specifies that the time is formatted text.

The `integer` tag adds a variable that is an integer, and the `id`
attribute specifies that the variable is named `value`. (We do not
need to specify `id` for `time` as it defaults to being named `time`.)
Other tags include `real` for real numbers and `text` for strings.

Because the `time` tag is first, LaTiS implicitly knows that our
`integer` is a function of our `time`.

### Adding Metadata

Finally, we need to add metadata for our dataset. HAPI requires that
we define the following:

- units for all variables
- length for text variables
- coverage for time variables

This is how we specify the required metadata in TSML:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset">

    <adapter class="latis.reader.tsml.AsciiAdapter"
             location="${dataset.dir}/simple_dataset.csv"
             delimiter=","
             commentCharacter="#"/>

    <time type="text">
        <!-- We've added metadata. -->
        <metadata units="yyyy-MM-dd'T'hh:mm:ss'Z'"/>
        <metadata length="20"/>
        <metadata min="2018-01-01T00:00:00Z"/>
        <metadata max="2018-01-01T00:00:02Z"/>
    </time>

    <integer id="value">
        <!-- We've added metadata. -->
        <metadata units="units"/>
    </integer>

</dataset>
```

A `metadata` tag adds the metadata specified by its attributes:

- `units` : Adds units to a variable.
  - For times represented by formatted text, this is a [time format
    string][java-sdf]. This is so LaTiS can read and convert times.
    The server will report "UTC" as the units as required by the HAPI
    specification.
- `length` : Adds the length of text variables.
- `min` and `max` : Adds coverage to a variable.

The snippet above is a complete TSML file that describes the simple
CSV data. Both the TSML and the CSV files are available in the
`examples/tsml` directory.

## Serving the Dataset

In this section we assume the following:

- You have downloaded the LASP HAPI server, either as an executable
  JAR or as a Docker image. (See the [README][readme].)
- You have both [`simple_dataset.csv`][csv] and
  [`simple_dataset.tsml`][tsml] saved somewhere.

You will need to point the LASP HAPI server to your TSML file before
starting the server. See the [README][readme] for the relevant
configuration options and for instructions on how to run the server.

Once the server is running, check the catalog:

```
$ curl "localhost:8080/hapi/catalog"
{"HAPI":"2.0","status":{"code":1200,"message":"OK"},"catalog":[{"id":"simple_dataset","name":"simple_dataset"}]}
```

Our `simple_dataset` dataset is available!

We can ask for its metadata:

```
$ curl "localhost:8080/hapi/info?id=simple_dataset"
{"HAPI":"2.0","status":{"code":1200,"message":"OK"},"parameters":[{"name":"time","type":"isotime","length":24,"units":"UTC","fill":null},{"name":"value","type":"integer","units":"units","fill":null}],"startDate":"2018-01-01T00:00:00.000Z","stopDate":"2018-01-01T00:00:02.000Z"}
```

We can also ask for the data:

```
$ curl "localhost:8080/hapi/data?id=simple_dataset&time.min=2018&time.max=2019"
2018-01-01T00:00:00.000Z,0
2018-01-01T00:00:01.000Z,1
2018-01-01T00:00:02.000Z,2
```

See the [HAPI specification][hapi-spec] for more information on the
API.

[csv]: ../examples/tsml/simple_dataset.csv
[fdm]: https://github.com/latis-data/latis/wiki/LaTiS-Data-Model
[hapi-spec]: https://github.com/hapi-server/data-specification/blob/master/hapi-2.0.0/HAPI-data-access-spec-2.0.0.md
[java-sdf]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html
[latis-2]: https://github.com/latis-data/latis/
[readme]: ../README.md
[tsml]: ../examples/tsml/simple_dataset.tsml
