# picocli - a mighty tiny Command Line Interpreter

A CLI framework in a single file, designed to be easy to include in your application _in source form_:
allowing you to parse command line arguments without dragging in an external dependency.

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. Any option prefix style works,
with special support for POSIX-style short groupable options.
Generates beautiful and easily tailored usage help. Works with Java 5 or higher.

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
If invalid input resulted in a `ParameterException` or if the user requested help, invoke `CommandLine.usage`. For example:
```java
CommandLine.usage(new Example(), System.out);
```

The generated help message looks like this:

![Usage help message with ANSI colors](docs/ExampleUsageANSI.png?raw=true)