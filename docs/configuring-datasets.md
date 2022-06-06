# Configuring Datasets

This guide describes how to configure datasets to be served by the
LASP HAPI server.

See the [README][readme] for information on downloading and
configuring the server.

## FDML

The LASP HAPI server is backed by [LaTiS 3][latis-3], a library for
modeling, manipulating, and serving data.

LaTiS 3 uses an XML file format called FDML to describe data so it can
be served. Users of the LASP HAPI server will need to provide FDML
descriptors for the datasets they wish to serve.

## Describing a Simple Dataset

Suppose we have a simple dataset we wish to serve: a single variable
as a function of time, stored in a [CSV file][csv].

```
#time,value
2018-01-01,0
2018-01-02,1
2018-01-03,2
```

We will need to provide an FDML file that describes this dataset.

### Introducing a Dataset

First, here's an example of a bare-bones FDML file for a dataset we
will call `simple_dataset`:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="http://latis-data.io/schemas/1.0/fdml-with-text-adapter.xsd">

    <!-- We will need to put stuff here. -->

</dataset>
```

The `dataset` tag introduces a dataset. The `id` attribute gives the
dataset a name. We will call our simple CSV dataset `simple_dataset`.

### Specifying the Source

LaTiS needs to know where to look for the data being described in the
FDML file.

For the sake of this example, imagine there is a data file on your
filesystem at `/data/simple_dataset.csv`.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="http://latis-data.io/schemas/1.0/fdml-with-text-adapter.xsd">

    <!-- Tell LaTiS to look for /data/simple_dataset.csv -->
    <source uri="file:///data/simple_dataset.csv"/>

    <!-- There is still more to add here. -->

</dataset>
```

The `source` tag specifies where to look for data.

### Specifying an Adapter

Next, we need to specify how to read the dataset. LaTiS uses
*adapters* to read data in various formats. Because we're working with
plain text, we can use the text adapter.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://latis-data.io/schemas/1.0/fdml-with-text-adapter.xsd">

    <source uri="file:///data/simple_dataset.csv"/>

    <!-- We've specified how to read the data. -->
    <adapter class="latis.input.TextAdapter"
             commentCharacter="#"
             delimiter=","/>

    <!-- There is still more to add here. -->

</dataset>
```

We specify information about the adapter with the `adapter` tag.

Adapters have options that are specified as attributes in the tag.
Each `adapter` tag has a `class` attribute that specifies the adapter
to use. In our case, the text adapter is implemented by the
`latis.input.TextAdapter` class.

There are other attributes that may be used depending on the adapter
being used. Some of the attributes available for the text adapter are:

- `delimiter` : The character that separates data values/columns.
- `commentCharacter` : The character that indicates header/comment
  lines.
- `skipLines` : The number of lines to skip before reading data.

### Specifying the Model

Next, we need to describe the shape of the data. LaTiS models data
using the [functional data model][fdm], so we need to describe the data in
terms of that model.

The gist of the functional data model is that we have three types of
things to work with:

- *scalars* : A single variable (e.g. time, flux, temperature).
- *tuples* : A collection of variables (e.g. a wind vector, a pair of
  flux and temperature).
- *functions* : A mapping from a domain (independent) variable to a
  range (dependent) variable (e.g. temperature as a function of time).

For our example dataset we know we have two scalars: `time` and
`value`. We also know that `value` is a function of `time`. We encode
that in FDML like this:

```xml
<dataset id="simple_dataset"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://latis-data.io/schemas/1.0/fdml-with-text-adapter.xsd">

    <source uri="file:///data/simple_dataset.csv"/>

    <adapter class="latis.input.TextAdapter"
             commentCharacter="#"
             delimiter=","/>

    <!-- We've described the shape of the data. -->
    <function>
        <scalar id="time"
                type="string"
                class="latis.time.Time"/>
        <scalar id="value" type="int"/>
    </function>
</dataset>
```

The `function` tag adds a function to the model. The first element in
the tag is the domain of the function, and the second element is the
range.

The `scalar` tag adds a scalar to the model. The `id` attribute
specifies an identifier for the scalar. The `type` attribute specifies
the data type of the underlying data. In our example, the times are
strings and the values are integers. The `class` attribute specifies
that our first scalar, representing time, is a special kind of scalar
implemented by the `latis.time.Time` class.

### Adding Metadata

Finally, we need to add metadata for our dataset. HAPI requires that
we define the following:

- time coverage
- units for all variables

This is how we specify the required metadata in FDML:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<dataset id="simple_dataset"
    temporalCoverage="2018-01-01/2018-01-03"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://latis-data.io/schemas/1.0/fdml-with-text-adapter.xsd">

    <source uri="file:///data/simple_dataset.csv"/>

    <adapter class="latis.input.TextAdapter"
             commentCharacter="#"
             delimiter=","/>

    <function>
        <scalar id="time"
                type="string"
                class="latis.time.Time"
                units="yyyy-MM-dd"/>
        <scalar id="value" type="int" units="units"/>
    </function>
</dataset>
```

We can add metadata to the dataset by adding attributes:

- `temporalCoverage` : Adds time coverage (specified as `<start>/<stop>`)
  - The `start` and `stop` times must be in ISO 8601 format. If the `stop`
    time is not present, the current date will be used.
- `units` : Adds units
  - For times represented by formatted text, this is a [time format
    string][java-dtf]. This is so LaTiS can read and convert times.
    The server will report "UTC" as the units as required by the HAPI
    specification.

The snippet above is a complete FDML file that describes the simple
CSV data. Both the FDML and the CSV files are available in the
`examples/fdml` directory.

## Validating FDML

We provide a script for validating FDML called `validate-fdml` that is
available in the [releases][releases].

The FDML validator script checks that your FDML files are
syntactically valid. It does not check whether the data file is
readable or whether the model specified in the FDML matches the data.

You can run the validator like this:

```
./validate-fdml <path to FDML file>
```

If the FDML file is invalid, you'll get a message with the error.

[csv]: ../examples/fdml/simple_dataset.csv
[fdm]: https://github.com/latis-data/latis/wiki/LaTiS-Data-Model
[fdml]: ../examples/fdml/simple_dataset.fdml
[hapi-spec]: https://github.com/hapi-server/data-specification/blob/master/hapi-2.1.0/HAPI-data-access-spec-2.1.0.md
[java-dtf]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html
[latis-3]: https://github.com/latis-data/latis3
[readme]: ../README.md
[releases]: https://github.com/lasp/hapi-server/releases
