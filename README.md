# picocli - a mighty tiny Command Line Interpreter

A Java command line parsing framework in a single file, so you can include it _in source form_.
This lets users run your application without requiring picocli as an external dependency.

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. Supports sub-commands, any option prefix style,
POSIX-style short groupable options, custom type converters and more.

Distinguishes between named options and positional parameters and allows both to be strongly typed.
Multi-valued fields can specify either an open or a precise range of parameters (e.g., `0..*`, `1..2`).

Generates polished and easily tailored usage help, using ANSI colors where possible.
Works with Java 5 or higher (but is designed to facilitate the use of Java 8 lambdas).


See the [manual](https://remkop.github.io/picocli) for details.

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

Then invoke `CommandLine.parse` with the command line parameters and an object you want to initialize.

```java
String[] args = { "-v", "inputFile1", "inputFile2" };
Example app = CommandLine.parse(new Example(), args);

assert !app.helpRequested;
assert  app.verbose;
assert  app.inputFiles != null && app.inputFiles.length == 2;
```

## Usage Help

If the user requested help or if invalid input resulted in a `ParameterException`,
picocli can generate a usage help message. For example:
```java
CommandLine.usage(new Example(), System.out);
```

The generated help message looks like this (colors only rendered when ANSI codes are enabled):

![Usage help message with ANSI colors](docs/images/ExampleUsageANSI.png?raw=true)

## Customized Usage Help

Usage help is highly customizable.
A more elaborate usage help example is shown below:

![Longer help message with ANSI colors](docs/images/UsageHelpWithStyle.png?raw=true)

See the [source code](https://github.com/remkop/picocli/blob/master/src/test/java/picocli/Demo.java#L155).

## More Customized Usage Help

Picocli annotations offer many ways to customize the usage help message.

If annotations are not sufficient, you can use picocli's Help API to customize even further.
For example, your application can generate help like this with a custom layout:

![Usage help message with two options per row](docs/images/UsageHelpWithCustomLayout.png?raw=true)

See the [source code](https://github.com/remkop/picocli/blob/master/src/test/java/picocli/CustomLayoutDemo.java#L61).