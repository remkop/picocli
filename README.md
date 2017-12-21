[![GitHub Release](https://img.shields.io/github/release/remkop/picocli.svg)](https://github.com/remkop/picocli/releases) 
[![Build Status](https://travis-ci.org/remkop/picocli.svg?branch=master)](https://travis-ci.org/remkop/picocli) 
[![codecov](https://codecov.io/gh/remkop/picocli/branch/master/graph/badge.svg)](https://codecov.io/gh/remkop/picocli) 
[![Follow @remkopopma](https://img.shields.io/twitter/follow/remkopopma.svg?style=social)](https://twitter.com/intent/follow?screen_name=remkopopma) 
[![Follow @picocli](https://img.shields.io/twitter/follow/picocli.svg?style=social)](https://twitter.com/intent/follow?screen_name=picocli) 


# picocli - a mighty tiny command line interface

Annotation-based Java command line parser, featuring usage help with ANSI colors, autocomplete and nested subcommands.
In a single file, so you can include it _in source form_.
This lets users run picocli-based applications without requiring picocli as an external dependency.

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. Supports git-like [subcommands](http://picocli.info/#_subcommands)
(and nested [sub-subcommands](http://picocli.info/#_nested_sub_subcommands)),
any option prefix style, POSIX-style [grouped short options](http://picocli.info/#_short_options),
custom [type converters](http://picocli.info/#_custom_type_converters) and more.
Parser [tracing](http://picocli.info/#_tracing) facilitates troubleshooting.

Distinguishes between [named options](http://picocli.info/#_options) and
[positional parameters](http://picocli.info/#_positional_parameters) and allows _both_ to be 
[strongly typed](http://picocli.info/#_strongly_typed_everything).
[Multi-valued fields](http://picocli.info/#_multiple_values) can specify 
an exact number of parameters or a [range](http://picocli.info/#_arity) (e.g., `0..*`, `1..2`).
Supports [Map options](http://picocli.info/#_maps) like `-Dkey1=val1 -Dkey2=val2`, where both key and value can be strongly typed.

Generates polished and easily tailored [usage help](http://picocli.info/#_usage_help)
and  [version help](http://picocli.info/#_version_help),
using [ANSI colors](http://picocli.info/#_ansi_colors_and_styles) where possible.
Works with Java 5 or higher (but is designed to facilitate the use of Java 8 lambdas).

Picocli-based command line applications can have [TAB autocompletion](http://picocli.info/autocomplete.html),
interactively showing users what options and subcommands are available.

<a id="picocli_demo"></a>
![Picocli Demo help message with ANSI colors](docs/images/picocli.Demo.png?raw=true)

#### Releases
* [Releases](https://github.com/remkop/picocli/releases) - latest: 2.2
* [Picocli 2.0 Release Notes](https://github.com/remkop/picocli/releases/tag/v2.0.0) - note there are some [potential breaking changes](https://github.com/remkop/picocli/releases/tag/v2.0.0#2.0-breaking-changes)

#### Documentation
* [User manual: http://picocli.info](http://picocli.info)
* [Command line autocompletion](http://picocli.info/autocomplete.html)
* [API Javadoc](http://picocli.info/apidocs/)
* [FAQ](https://github.com/remkop/picocli/wiki/FAQ)

#### Articles
* [Announcing picocli 1.0](http://picocli.info/announcing-picocli-1.0.html)
* [Picocli 2.0: Do More With Less](http://picocli.info/picocli-2.0-do-more-with-less.html)
* [Picocli 2.0: Groovy Scripts on Steroids](http://picocli.info/picocli-2.0-groovy-scripts-on-steroids.html)

#### Related
* Check out Thibaud Lepretre's [picocli Spring boot starter](https://github.com/kakawait/picocli-spring-boot-starter)!


## Example

Annotate fields with the command line parameter names and description.

```java
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.File;

public class Example implements Runnable {
    @Option(names = { "-v", "--verbose" }, description = "Verbose mode. Helpful for troubleshooting. " +
                                                         "Multiple -v options increase the verbosity.")
    private boolean[] verbose = new boolean[0];

    @Option(names = { "-h", "--help" }, usageHelp = true,
            description = "Displays this help message and quits.")
    private boolean helpRequested = false;

    @Parameters(arity = "1..*", paramLabel = "FILE", description = "File(s) to process.")
    private File[] inputFiles;
    
    public void run() {
        if (verbose.length > 0) {
            System.out.println(inputFiles.length + " files to process...");
        }
        if (verbose.length > 1) {
            for (File f : inputFiles) {
                System.out.println(f.getAbsolutePath());
            }
        }
    }
    
    public static void main(String[] args) {
        CommandLine.run(new Example(), System.out, args);
    }
}
```

All that is necessary to run the program is to invoke the `CommandLine.run` method with the command line parameters and the object you want to initialize.

```bash
$ java Example -v inputFile1 inputFile2
```

The `CommandLine.run` method automatically prints the usage help message if the user requested help or when the input was invalid.

```bash
$ java Example

Missing required parameters at positions 0..*: FILE
Usage: <main class> [-h] [-v]... FILE...
      FILE...                 File(s) to process.
  -h, --help                  Displays this help message and quits.
  -v, --verbose               Verbose mode. Helpful for troubleshooting.
                                Multiple -v options increase the verbosity.
```

![Usage help message with ANSI colors](docs/images/ExampleUsageANSI.png?raw=true)

## Usage Help with ANSI Colors and Styles

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

