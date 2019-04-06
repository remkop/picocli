<p align="center"><img src="docs/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>

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
converting the input to strongly typed data. Supports git-like [subcommands](https://picocli.info/#_subcommands)
(and nested [sub-subcommands](https://picocli.info/#_nested_sub_subcommands)),
any option prefix style, POSIX-style [grouped short options](https://picocli.info/#_short_options),
custom [type converters](https://picocli.info/#_custom_type_converters),
[password options](http://picocli.info/#_interactive_password_options) and more.
Parser [tracing](https://picocli.info/#_tracing) facilitates troubleshooting.
Command-line [argument files](https://picocli.info/#AtFiles) (@-files) allow applications to handle very long command lines.

Distinguishes between [named options](https://picocli.info/#_options) and
[positional parameters](https://picocli.info/#_positional_parameters) and allows _both_ to be 
[strongly typed](https://picocli.info/#_strongly_typed_everything).
[Multi-valued fields](https://picocli.info/#_multiple_values) can specify 
an exact number of parameters or a [range](https://picocli.info/#_arity) (e.g., `0..*`, `1..2`).
Supports [Map options](https://picocli.info/#_maps) like `-Dkey1=val1 -Dkey2=val2`, where both key and value can be strongly typed.

Generates polished and easily tailored [usage help](https://picocli.info/#_usage_help)
and  [version help](https://picocli.info/#_version_help),
using [ANSI colors](https://picocli.info/#_ansi_colors_and_styles) where possible.
Works with Java 5 or higher (but is designed to facilitate the use of Java 8 lambdas).

Picocli-based command line applications can have [TAB autocompletion](https://picocli.info/autocomplete.html),
interactively showing users what options and subcommands are available.
When an option has [`completionCandidates`](https://picocli.info/#__code_completion_candidates_code_variable) or has an `enum` type, autocompletion can also suggest option values.
Picocli can generate completion scripts for bash and zsh, and offers [`picocli-shell-jline2`](picocli-shell-jline2/README.md) and [`picocli-shell-jline3`](picocli-shell-jline3/README.md) modules with JLine `Completer` implementations for building interactive shell applications.

Picocli-based applications can easily [integrate](https://picocli.info/#_dependency_injection) with Dependency Injection containers.

<a id="picocli_demo"></a>
![Picocli Demo help message with ANSI colors](docs/images/picocli.Demo.png?raw=true)

### Releases
* [Releases](https://github.com/remkop/picocli/releases) - Latest: 3.9.6 [Release Notes](https://github.com/remkop/picocli/releases/tag/v3.9.6)
* Older: Picocli 3.0.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v3.0.0)
* Older: Picocli 2.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v2.0.0)

### Documentation
* [3.x User manual: https://picocli.info](https://picocli.info)
* [3.x Quick Guide](https://picocli.info/quick-guide.html)
* [2.x User manual](https://picocli.info/man/2.x)
* [Command line autocompletion](https://picocli.info/autocomplete.html)
* [API Javadoc](https://picocli.info/apidocs/)
* [3.0 Programmatic API](https://picocli.info/picocli-3.0-programmatic-api.html)
* [FAQ](https://github.com/remkop/picocli/wiki/FAQ)
* [GraalVM AOT Compilation to Native Image](https://picocli.info/picocli-on-graalvm.html) <img src="https://www.graalvm.org/resources/img/logo-colored.svg" > 

### Articles
* [Migrating from Commons CLI to picocli](https://picocli.info/migrating-from-commons-cli.html). You won't regret it! :-) (also on: [DZone](https://dzone.com/articles/migrating-from-commons-cli-to-picocli) and [Java Code Geeks](https://www.javacodegeeks.com/2018/11/migrating-commons-cli-picocli.html)).
* [Groovy 2.5 CliBuilder Renewal](https://picocli.info/groovy-2.5-clibuilder-renewal.html) (also on [blogs.apache.org](https://blogs.apache.org/logging/entry/groovy-2-5-clibuilder-renewal)). In two parts: [Part 1](https://picocli.info/groovy-2.5-clibuilder-renewal-part1.html) (also on: [DZone](https://dzone.com/articles/groovy-25-clibuilder-renewal), [Java Code Geeks](https://www.javacodegeeks.com/2018/06/groovy-clibuilder-renewal-part-1.html)), [Part 2](https://picocli.info/groovy-2.5-clibuilder-renewal-part2.html) (also on: [DZone](https://dzone.com/articles/groovy-25-clibuilder-renewal-part-2), [Java Code Geeks](https://www.javacodegeeks.com/2018/06/groovy-clibuilder-renewal-part-2.html)). 
* Micronaut user manual for running microservices [standalone with picocli](https://docs.micronaut.io/snapshot/guide/index.html#commandLineApps).
* [Java Command-Line Interfaces (Part 30): Observations](http://marxsoftware.blogspot.jp/2017/11/java-cmd-line-observations.html) by Dustin Marx about picocli 2.0.1 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-30-finale-observations), [Java Code Geeks](https://www.javacodegeeks.com/2017/11/java-command-line-interfaces-part-30-observations.html))
* [Java Command-Line Interfaces (Part 10): Picocli](http://marxsoftware.blogspot.jp/2017/08/picocli.html) by Dustin Marx about picocli 0.9.7 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-10-picocli), [Java Code Geeks](https://www.javacodegeeks.com/2017/08/java-command-line-interfaces-part-10-picocli.html)) 
* [Picocli 2.0: Groovy Scripts on Steroids](https://picocli.info/picocli-2.0-groovy-scripts-on-steroids.html) (also on: [DZone](https://dzone.com/articles/picocli-v2-groovy-scripts-on-steroids), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-groovy-scripts-steroids.html))
* [Picocli 2.0: Do More With Less](https://picocli.info/picocli-2.0-do-more-with-less.html) (also on: [DZone](https://dzone.com/articles/whats-new-in-picocli-20), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-less.html))
* [Announcing picocli 1.0](https://picocli.info/announcing-picocli-1.0.html) (also on: [DZone](https://dzone.com/articles/announcing-picocli-10))

### 中文
* [Picocli 2.0: Steroids上的Groovy脚本](https://picocli.info/zh/picocli-2.0-groovy-scripts-on-steroids.html)
* [Picocli 2.0: 以少求多](https://picocli.info/zh/picocli-2.0-do-more-with-less.html) 

### Mailing List
Join the [picocli Google group](https://groups.google.com/d/forum/picocli) if you are interested in discussing anything picocli-related and receiving announcements on new releases.

### Related
* Check out Thibaud Lepretre's [picocli Spring boot starter](https://github.com/kakawait/picocli-spring-boot-starter)!

### Credit
<img src="https://picocli.info/images/logo/horizontal-400x150.png" height="100"> 

[Reallinfo](https://github.com/reallinfo) designed the new picocli logo! Many thanks! 

## Adoption

<img src="https://picocli.info/images/groovy-logo.png" height="50">  <img src="https://picocli.info/images/1x1.png" width="10"> <img src="http://micronaut.io/images/micronaut_mini_copy_tm.svg" height="50">  <img src="https://picocli.info/images/1x1.png" width="10"><img src="https://picocli.info/images/junit5logo-172x50.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/debian-logo-192x50.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/karate-logo.png" height="50" width="50"/>  <img src="https://picocli.info/images/checkstyle-logo-260x50.png" height="50"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/ballerina-logo.png" height="40">  

* Picocli is now part of Groovy. From Groovy 2.5, all Groovy command line tools are picocli-based, and picocli is the underlying parser for Groovy's [CliBuilder DSL](http://groovy-lang.org/dsls.html#_clibuilder). 
* Picocli is now part of Micronaut. The Micronaut CLI has been rewritten with picocli, and Micronaut has dedicated support for running microservices [standalone with picocli](https://docs.micronaut.io/snapshot/guide/index.html#commandLineApps).
* Picocli is now part of JUnit 5. JUnit 5.3 migrated its `ConsoleLauncher` from jopt-simple to picocli to support @-files (argument files); this helps users who need to specify many tests on the command line and run into system limitations.
* Debian now offers a [libpicocli-java package](https://tracker.debian.org/pkg/picocli). Thanks to [Miroslav Kravec](https://udd.debian.org/dmd/?kravec.miroslav%40gmail.com).
* Picocli is used in the Intuit [Karate](https://github.com/intuit/karate) standalone JAR / executable.
* Picocli is part of [Ballerina](https://ballerina.io/). Ballerina uses picocli for all its command line utilities.
* Picocli is used in the [CheckStyle](https://checkstyle.org/cmdline.html) standalone JAR / executable from Checkstyle 8.15.
* Picocli is included in the [OpenJDK Quality Outreach](https://wiki.openjdk.java.net/display/quality/Quality+Outreach) list of Free Open Source Software (FOSS) projects that actively test against OpenJDK builds.

<img src="https://picocli.info/images/downloads-201812.png">

Glad to see more people are using picocli. We must be doing something right. :-) 


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
        CommandLine.run(new Example(), args);
    }
}
```

If your command implements `Runnable`, all that is necessary to parse the command line and execute the command is a call to `CommandLine.run` with the command line parameters and the `Runnable` command. When the program is run on the command line, the command line arguments are converted to Java objects and assigned to the annotated fields. After the arguments are successfully parsed, picocli calls the command's `run` method.

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

If annotations are not sufficient, you can use picocli's [Help API](https://picocli.info/#_usage_help_api) to customize even further.
For example, your application can generate help like this with a custom layout:

![Usage help message with two options per row](docs/images/UsageHelpWithCustomLayout.png?raw=true)

See the [source code](https://github.com/remkop/picocli/blob/master/src/test/java/picocli/CustomLayoutDemo.java#L61).

## Download
You can add picocli as an external dependency to your project, or you can include it as source.
See the [source code](https://github.com/remkop/picocli/blob/master/src/main/java/picocli/CommandLine.java). Copy and paste it into a file called `CommandLine.java`, add it to your project, and enjoy!

### Gradle
```
compile 'info.picocli:picocli:3.9.6'
```
### Maven
```
<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli</artifactId>
  <version>3.9.6</version>
</dependency>
```
### Scala SBT
```
libraryDependencies += "info.picocli" % "picocli" % "3.9.6"
```
### Ivy
```
<dependency org="info.picocli" name="picocli" rev="3.9.6" />
```
### Grape
```groovy
@Grapes(
    @Grab(group='info.picocli', module='picocli', version='3.9.6')
)
```
### Leiningen
```
[info.picocli/picocli "3.9.6"]
```
### Buildr
```
'info.picocli:picocli:jar:3.9.6'
```
