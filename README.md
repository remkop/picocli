# picocli - a mighty tiny command line interface

A Java command line parsing framework in a single file, so you can include it _in source form_.
This lets users run picocli-based applications without requiring picocli as an external dependency.

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. Supports [subcommands](http://picocli.info/#_subcommands),
any option prefix style, POSIX-style [grouped short options](http://picocli.info/#_short_options),
custom [type converters](http://picocli.info/#_custom_type_converters) and more.

Distinguishes between [named options](http://picocli.info/#_options) and
[positional parameters](http://picocli.info/#_positional_parameters) and allows _both_ to be 
[strongly typed](http://picocli.info/#_strongly_typed_everything).
[Multi-valued fields](http://picocli.info/#_multiple_values) can specify 
an exact number of parameters or a [range](http://picocli.info/#_arity) (e.g., `0..*`, `1..2`).

Generates polished and easily tailored [usage help](http://picocli.info/#_usage_help),
using [ANSI colors](http://picocli.info/#_ansi_colors_and_styles) where possible.
Works with Java 5 or higher (but is designed to facilitate the use of Java 8 lambdas).

![Picocli Demo help message with ANSI colors](docs/images/picocli.Demo.png?raw=true)

* user manual: [http://picocli.info](http://picocli.info)
* [API Javadoc](http://picocli.info/apidocs/)
* [FAQ](https://github.com/remkop/picocli/wiki/FAQ)
* [Releases](https://github.com/remkop/picocli/releases)
* [![Build Status](https://travis-ci.org/remkop/picocli.svg?branch=master)](https://travis-ci.org/remkop/picocli) 
[![codecov](https://codecov.io/gh/remkop/picocli/branch/master/graph/badge.svg)](https://codecov.io/gh/remkop/picocli)


## Example

Annotate fields with the command line parameter names and description.

```java
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.File;

public class Example {
    @Option(names = { "-v", "--verbose" }, description = "Be verbose.")
    private boolean verbose = false;

    @Option(names = { "-h", "--help" }, help = true,
            description = "Displays this help message and quits.")
    private boolean helpRequested = false;

    @Parameters(arity = "1..*", paramLabel = "FILE", description = "File(s) to process.")
    private File[] inputFiles;
    ...
}
```

Then invoke `CommandLine.populateCommand` with the command line parameters and an object you want to initialize.

```java
String[] args = { "-v", "inputFile1", "inputFile2" };
Example app = CommandLine.populateCommand(new Example(), args);

assert !app.helpRequested;
assert  app.verbose;
assert  app.inputFiles != null && app.inputFiles.length == 2;
```

Invoke `CommandLine.usage` if the user requested help or the input was invalid and a `ParameterException` was thrown.

```java
CommandLine.usage(new Example(), System.out);
```

![Usage help message with ANSI colors](docs/images/ExampleUsageANSI.png?raw=true)

## Polishing Usage Help

Colors, styles, headers, footers and section headings are easily customized with annotations.
For example:

![Longer help message with ANSI colors](docs/images/UsageHelpWithStyle.png?raw=true)

See the [source code](https://github.com/remkop/picocli/blob/v0.9.4/src/test/java/picocli/Demo.java#L337). 



## Usage Help API

Picocli annotations offer many ways to customize the usage help message.

If annotations are not sufficient, you can use picocli's [Help API](http://picocli.info/#_usage_help_api) to customize even further.
For example, your application can generate help like this with a custom layout:

![Usage help message with two options per row](docs/images/UsageHelpWithCustomLayout.png?raw=true)

See the [source code](https://github.com/remkop/picocli/blob/master/src/test/java/picocli/CustomLayoutDemo.java#L61).