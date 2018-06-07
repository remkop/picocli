<p align="center"><img src="docs/images/logo/horizontalv2.png" alt="picocli" height="150px"></p>

[![GitHub Release](https://img.shields.io/github/release/remkop/picocli.svg)](https://github.com/remkop/picocli/releases) 
[![Build Status](https://travis-ci.org/remkop/picocli.svg?branch=master)](https://travis-ci.org/remkop/picocli) 
[![codecov](https://codecov.io/gh/remkop/picocli/branch/master/graph/badge.svg)](https://codecov.io/gh/remkop/picocli) 
[![Follow @remkopopma](https://img.shields.io/twitter/follow/remkopopma.svg?style=social)](https://twitter.com/intent/follow?screen_name=remkopopma) 
[![Follow @picocli](https://img.shields.io/twitter/follow/picocli.svg?style=social)](https://twitter.com/intent/follow?screen_name=picocli) 


# picocli - a mighty tiny command line interface

Java command line parser with both an annotations API and a programmatic API, featuring usage help with ANSI colors, autocomplete and nested subcommands.
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
* [Releases](https://github.com/remkop/picocli/releases) - latest: 3.0.2
* [Picocli 3.0.0 Release Notes](https://github.com/remkop/picocli/releases/tag/v3.0.0) - note there are some [potential breaking changes](https://github.com/remkop/picocli/releases/tag/v3.0.0#3.0.0-breaking-changes) from prior versions
* [Picocli 2.0 Release Notes](https://github.com/remkop/picocli/releases/tag/v2.0.0) - note there are some [potential breaking changes](https://github.com/remkop/picocli/releases/tag/v2.0.0#2.0-breaking-changes) from prior versions

#### Documentation
* [3.x User manual: http://picocli.info](http://picocli.info)
* [2.x User manual](http://picocli.info/man/2.x)
* [Command line autocompletion](http://picocli.info/autocomplete.html)
* [API Javadoc](http://picocli.info/apidocs/)
* [3.0 Programmatic API](http://picocli.info/picocli-3.0-programmatic-api.html)
* [FAQ](https://github.com/remkop/picocli/wiki/FAQ)

#### Articles
* [Announcing picocli 1.0](http://picocli.info/announcing-picocli-1.0.html) (also on: [DZone](https://dzone.com/articles/announcing-picocli-10))
* [Picocli 2.0: Do More With Less](http://picocli.info/picocli-2.0-do-more-with-less.html) (also on: [DZone](https://dzone.com/articles/whats-new-in-picocli-20), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-less.html))
* [Picocli 2.0: Groovy Scripts on Steroids](http://picocli.info/picocli-2.0-groovy-scripts-on-steroids.html) (also on: [DZone](https://dzone.com/articles/picocli-v2-groovy-scripts-on-steroids), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-groovy-scripts-steroids.html))
* [Java Command-Line Interfaces (Part 10): Picocli](http://marxsoftware.blogspot.jp/2017/08/picocli.html) by Dustin Marx about picocli 0.9.7 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-10-picocli), [Java Code Geeks](https://www.javacodegeeks.com/2017/08/java-command-line-interfaces-part-10-picocli.html)) 
* [Java Command-Line Interfaces (Part 30): Observations](http://marxsoftware.blogspot.jp/2017/11/java-cmd-line-observations.html) by Dustin Marx about picocli 2.0.1 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-30-finale-observations), [Java Code Geeks](https://www.javacodegeeks.com/2017/11/java-command-line-interfaces-part-30-observations.html))

#### Related
* Check out Thibaud Lepretre's [picocli Spring boot starter](https://github.com/kakawait/picocli-spring-boot-starter)!


## Example

Annotate fields with the command line parameter names and description. Optionally implement `Runnable` or `Callable` to delegate error handling and requests for usage help or version help to picocli. For example:


```java
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.File;

@Command(name = "example", mixinStandardHelpOptions = true, version = "Picocli example 3.0")
public class Example implements Runnable {
    @Option(names = { "-v", "--verbose" }, description = "Verbose mode. Helpful for troubleshooting. " +
                                                         "Multiple -v options increase the verbosity.")
    private boolean[] verbose = new boolean[0];

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

If your command implements `Runnable`, all the code that is necessary to parse the command line and execute the command is a call to `CommandLine.run` with the command line parameters and the `Runnable` command. When the program is run on the command line, the command line arguments are converted to Java objects and assigned to the annotated fields. After the arguments are successfully parsed, picocli calls the command's `run` method.

```bash
$ java Example -v inputFile1 inputFile2

2 files to process...
```

The `CommandLine.run` convenience method automatically prints the usage help message if the user requested help or when the input was invalid.

![Usage help message with ANSI colors](docs/images/ExampleUsageANSI.png?raw=true)

If you want more control, you may be interested in the `CommandLine.parse` or `CommandLine.parseWithHandlers` methods. See the user manual for details.

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

## Download
You can add picocli as an external dependency to your project, or you can include it as source.
See the [source code](https://github.com/remkop/picocli/blob/master/src/main/java/picocli/CommandLine.java). Copy and paste it into a file called `CommandLine.java`, add it to your project, and enjoy!

### Gradle
```
compile 'info.picocli:picocli:3.0.2'
```
### Maven
```
<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli</artifactId>
  <version>3.0.2</version>
</dependency>
```
### Scala SBT
```
libraryDependencies += "info.picocli" % "picocli" % "3.0.2"
```
### Ivy
```
<dependency org="info.picocli" name="picocli" rev="3.0.2" />
```
### Grape
```groovy
@Grapes(
    @Grab(group='info.picocli', module='picocli', version='3.0.2')
)
```
### Leiningen
```
[info.picocli/picocli "3.0.2"]
```
### Buildr
```
'info.picocli:picocli:jar:3.0.2'
```
