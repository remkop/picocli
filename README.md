# picocli - a mighty tiny Command Line Interpreter

A Java command line parsing framework in a single file, so you can include it _in source form_.
This lets users run your application without requiring picocli as an external dependency.

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. 
Supports sub-commands, any option prefix style,
POSIX-style short groupable options, multi-valued options 
with a precise range of parameters (e.g., `0..*`, `1..2`), and more.

Generates beautiful and easily tailored usage help, using ANSI colors where possible.
Works with Java 5 or higher.

See the [manual](docs/index.adoc) for details.

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

![Usage help message with ANSI colors](docs/ExampleUsageANSI.png?raw=true)

Usage help is highly customizable.
A more elaborate usage help example is shown below:

![Longer help message with ANSI colors](docs/UsageHelpWithStyle.png?raw=true)

Customizing your usage help message is easily done with annotations.
If this is not sufficient, you can use picocli's Help API to customize even further.
For example, your application can generate help like this with a custom layout:
```
Copyright (c) 1990-2008 Info-ZIP - Type 'zip "-L"' for software license.
Zip 3.0 (July 5th 2008). Command:
zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]
  The default action is to add or replace zipfile entries from list, which
  can include the special name - to compress standard input.
  If zipfile and list are omitted, zip compresses stdin to stdout.
  -f   freshen: only changed files  -u   update: only changed or new files
  -d   delete entries in zipfile    -m   move into zipfile (delete OS files)
  -r   recurse into directories     -j   junk (don't record) directory names
  -0   store only                   -l   convert LF to CR LF (-ll CR LF to LF)
  -1   compress faster              -9   compress better
  -q   quiet operation              -v   verbose operation/print version info
  -c   add one-line comments        -z   add zipfile comment
  -@   read names from stdin        -o   make zipfile as old as latest entry
  -x   exclude the following names  -i   include only the following names
  -F   fix zipfile (-FF try harder) -D   do not add directory entries
  -A   adjust self-extracting exe   -J   junk zipfile prefix (unzipsfx)
  -T   test zipfile integrity       -X   eXclude eXtra file attributes
  -y   store symbolic links as the link instead of the referenced file
  -e   encrypt                      -n   don't compress these suffixes
  -h2  show more help
```
