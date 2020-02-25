<p align="center"><img src="docs/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>

[![GitHub Release](https://img.shields.io/github/release/remkop/picocli.svg)](https://github.com/remkop/picocli/releases) 
[![Build Status](https://travis-ci.org/remkop/picocli.svg?branch=master)](https://travis-ci.org/remkop/picocli) 
[![codecov](https://codecov.io/gh/remkop/picocli/branch/master/graph/badge.svg)](https://codecov.io/gh/remkop/picocli) 
[![Follow @remkopopma](https://img.shields.io/twitter/follow/remkopopma.svg?style=social)](https://twitter.com/intent/follow?screen_name=remkopopma) 
[![Follow @picocli](https://img.shields.io/twitter/follow/picocli.svg?style=social)](https://twitter.com/intent/follow?screen_name=picocli) 
[![Follow picocli on StackShare](https://img.shields.io/badge/Follow%20on-StackShare-blue.svg?logo=stackshare&style=flat)](https://stackshare.io/picocli)

# picocli - a mighty tiny command line interface

Picocli aims to be the easiest-to-use way to create rich command line applications that can run on and off the JVM.

Picocli is a modern library and framework, written in Java, that contains both an annotations API and a programmatic API. It features usage help with [ANSI colors and styles](https://picocli.info/#_ansi_colors_and_styles), [TAB autocompletion](https://picocli.info/autocomplete.html) and nested subcommands.
In a single file, so you can include it _in source form_.
This lets users run picocli-based applications without requiring picocli as an external dependency.

Picocli-based applications can be ahead-of-time compiled to <img src="https://www.graalvm.org/resources/img/logo-colored.svg" alt="GraalVM">
[native image](https://picocli.info/#_graalvm_native_image), with extremely fast startup time and lower memory requirements,
which can be distributed as a single executable file.
Picocli comes with an [annotation processor](https://github.com/remkop/picocli/releases#4.0.0-annotation-processor) that automatically Graal-enables your jar during compilation.

Picocli applications can be very compact with no boilerplate code: your command (or subcommand) can be executed with a [single line of code](#example "(example below)").
Simply implement `Runnable` or `Callable`, or put the business logic of your command in a `@Command`-annotated method.

<a id="picocli_demo"></a>
![Picocli Demo help message with ANSI colors](docs/images/picocli.Demo.png?raw=true)

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. Supports git-like [subcommands](https://picocli.info/#_subcommands)
(and nested [sub-subcommands](https://picocli.info/#_nested_sub_subcommands)),
any option prefix style, POSIX-style [grouped short options](https://picocli.info/#_short_posix_options),
custom [type converters](https://picocli.info/#_custom_type_converters),
[password options](http://picocli.info/#_interactive_password_options) and more.

Picocli distinguishes between [named options](https://picocli.info/#_options) and
[positional parameters](https://picocli.info/#_positional_parameters) and allows _both_ to be 
[strongly typed](https://picocli.info/#_strongly_typed_everything).
[Multi-valued fields](https://picocli.info/#_multiple_values) can specify 
an exact number of parameters or a [range](https://picocli.info/#_arity) (e.g., `0..*`, `1..2`).
Supports [Map options](https://picocli.info/#_maps) like `-Dkey1=val1 -Dkey2=val2`, where both key and value can be strongly typed.
Parser [tracing](https://picocli.info/#_tracing) facilitates troubleshooting.
Command-line [argument files](https://picocli.info/#AtFiles) (@-files) allow applications to handle very long command lines.

Generates polished and easily tailored [usage help](https://picocli.info/#_usage_help)
and  [version help](https://picocli.info/#_version_help),
using [ANSI colors](https://picocli.info/#_ansi_colors_and_styles) where possible.
Requires at minimum Java 5, but is designed to facilitate the use of Java 8 lambdas. Tested on all Java versions between 5 and 15-ea (inclusive).

Picocli-based command line applications can have [TAB autocompletion](https://picocli.info/autocomplete.html),
interactively showing users what options and subcommands are available.
When an option has [`completionCandidates`](https://picocli.info/#_completion_candidates_variable) or has an `enum` type, autocompletion can also suggest option values.
Picocli can generate completion scripts for bash and zsh, and offers [`picocli-shell-jline2`](picocli-shell-jline2/README.md) and [`picocli-shell-jline3`](picocli-shell-jline3/README.md) modules with JLine `Completer` implementations for building interactive shell applications.

Unique features in picocli include support for [negatable options](https://picocli.info/#_negatable_options),
advanced [quoted values](https://picocli.info/#_quoted_values),
and [argument groups](https://picocli.info/#_argument_groups).
Argument groups can be used to create mutually [exclusive](https://picocli.info/#_mutually_exclusive_options) options,
mutually [dependent](https://picocli.info/#_mutually_dependent_options) options,
option [sections](https://picocli.info/#_option_sections_in_usage_help) in the usage help message
and [repeating composite arguments](https://picocli.info/#_repeating_composite_argument_groups) like
`([-a=<a> -b=<b> -c=<c>] (-x | -y | -z))...`.
For advanced use cases, applications can access the picocli command object model with the
[`@Spec` annotation](https://picocli.info/#_spec_annotation), and
implement [custom parameter processing](https://picocli.info/#_custom_parameter_processing) for option parameters if the built-in logic is insufficient.


Picocli-based applications can easily [integrate](https://picocli.info/#_dependency_injection) with Dependency Injection containers.
The [Micronaut](https://micronaut.io/) microservices framework has [built-in support](https://docs.micronaut.io/latest/guide/index.html#commandLineApps) for picocli.
Picocli ships with a [`picocli-spring-boot-starter` module](https://github.com/remkop/picocli/tree/master/picocli-spring-boot-starter) 
that includes a `PicocliSpringFactory` and Spring Boot auto-configuration to use Spring dependency injection in your picocli command line application.
The user manual has an [example](https://picocli.info/#_guice_example) of integrating with Guice.

### Releases
* [All Releases](https://github.com/remkop/picocli/releases)
* Latest: 4.2.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v4.2.0)
* Older: Picocli 4.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v4.0.0)
* Older: Picocli 3.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v3.0.0)
* Older: Picocli 2.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v2.0.0)

### Documentation
* [4.x User manual: https://picocli.info](https://picocli.info)
* [4.x Quick Guide](https://picocli.info/quick-guide.html)
* [4.x API Javadoc](https://picocli.info/apidocs/)
* [Command line autocompletion](https://picocli.info/autocomplete.html)
* [Programmatic API](https://picocli.info/picocli-programmatic-api.html)
* [FAQ](https://github.com/remkop/picocli/wiki/FAQ)
* [GraalVM AOT Compilation to Native Image](https://picocli.info/picocli-on-graalvm.html) <img src="https://www.graalvm.org/resources/img/logo-colored.svg" > 

### Older
* ~~[3.x User manual](https://picocli.info/man/3.x)~~
* ~~[3.x Quick Guide](https://picocli.info/man/3.x/quick-guide.html)~~
* ~~[3.x API Javadoc](https://picocli.info/man/3.x/apidocs/)~~
* ~~[2.x User manual](https://picocli.info/man/2.x)~~
* ~~[2.x API Javadoc](https://picocli.info/man/2.x/apidocs/)~~
* ~~[1.x User manual](https://picocli.info/man/1.x)~~

### Articles & Presentations
#### English
* [Picocli Structured Objects](https://gist.github.com/hanslovsky/8276da86c53bc6d95bf01447cd5cb2b7#file-00_picocli-structured-objects-md) (2019-09-10) by [Philipp Hanslovsky](https://gist.github.com/hanslovsky) explains how to use picocli's support for repeating argument groups to add or configure structured objects from the command line.
* [Create a Java Command Line Program with Picocli|Baeldung](https://www.baeldung.com/java-picocli-create-command-line-program) (2019-05-07) by [François Dupire](https://www.baeldung.com/author/francois-dupire/).
* A whirlwind tour of picocli [JAX Magazine "Putting the spotlight on Java tools"](https://jaxenter.com/jax-mag-java-tools-157592.html) (2019-04-08).
* [An Introduction to PicoCLI](https://devops.datenkollektiv.de/an-introduction-to-picocli.html) (2019-02-10) by [devop](https://devops.datenkollektiv.de/author/devop.html).
* [Corda CLI UX (User Experience) Guide](https://docs.corda.net/head/cli-ux-guidelines.html) (2018 by R3 Limited) gives useful advice.
* [Develop a CLI tool using groovy scripts](https://medium.com/@chinthakadinadasa/develop-a-cli-tool-using-groovy-scripts-a7d545eecddd) (2018-10-26) by [Chinthaka Dinadasa](https://medium.com/@chinthakadinadasa).
* [Migrating from Commons CLI to picocli](https://picocli.info/migrating-from-commons-cli.html). You won't regret it! :-) (also on: [DZone](https://dzone.com/articles/migrating-from-commons-cli-to-picocli) and [Java Code Geeks](https://www.javacodegeeks.com/2018/11/migrating-commons-cli-picocli.html)).
* [Groovy 2.5 CliBuilder Renewal](https://picocli.info/groovy-2.5-clibuilder-renewal.html) (also on [blogs.apache.org](https://blogs.apache.org/logging/entry/groovy-2-5-clibuilder-renewal)). In two parts: [Part 1](https://picocli.info/groovy-2.5-clibuilder-renewal-part1.html) (also on: [DZone](https://dzone.com/articles/groovy-25-clibuilder-renewal), [Java Code Geeks](https://www.javacodegeeks.com/2018/06/groovy-clibuilder-renewal-part-1.html)), [Part 2](https://picocli.info/groovy-2.5-clibuilder-renewal-part2.html) (also on: [DZone](https://dzone.com/articles/groovy-25-clibuilder-renewal-part-2), [Java Code Geeks](https://www.javacodegeeks.com/2018/06/groovy-clibuilder-renewal-part-2.html)). 
* Micronaut user manual for running microservices [standalone with picocli](https://docs.micronaut.io/snapshot/guide/index.html#commandLineApps).
* [Java Command-Line Interfaces (Part 30): Observations](http://marxsoftware.blogspot.jp/2017/11/java-cmd-line-observations.html) by Dustin Marx about picocli 2.0.1 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-30-finale-observations), [Java Code Geeks](https://www.javacodegeeks.com/2017/11/java-command-line-interfaces-part-30-observations.html))
* [Java Command-Line Interfaces (Part 10): Picocli](http://marxsoftware.blogspot.jp/2017/08/picocli.html) by Dustin Marx about picocli 0.9.7 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-10-picocli), [Java Code Geeks](https://www.javacodegeeks.com/2017/08/java-command-line-interfaces-part-10-picocli.html)) 
* [Picocli 2.0: Groovy Scripts on Steroids](https://picocli.info/picocli-2.0-groovy-scripts-on-steroids.html) (also on: [DZone](https://dzone.com/articles/picocli-v2-groovy-scripts-on-steroids), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-groovy-scripts-steroids.html))
* [Picocli 2.0: Do More With Less](https://picocli.info/picocli-2.0-do-more-with-less.html) (also on: [DZone](https://dzone.com/articles/whats-new-in-picocli-20), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-less.html))
* [Announcing picocli 1.0](https://picocli.info/announcing-picocli-1.0.html) (also on: [DZone](https://dzone.com/articles/announcing-picocli-10))

#### русский
* [Интерфейсы командной строки Java: picocli](https://habr.com/ru/company/otus/blog/419401/) (2018-08-06): Russian translation by [MaxRokatansky](https://habr.com/ru/users/MaxRokatansky/) of Dustin Marx' blog post.

#### Français
* [VIDEO] [Des applications en ligne de commande avec Picocli et GraalVM (N. Peters)](https://www.youtube.com/watch?v=8ENbMwkaFyk) (2019-05-07): 15 minute presentation by Nicolas Peters during Devoxx FR. Presentation slides are [available on GitHub](https://t.co/tXhtpTpAff?amp=1).

#### 日本語
* [GraalVM, PicocliとJavaでときめくネイティブコマンドラインアプリを作ろう](https://remkop.github.io/presentations/20191123/) (2019-11-23) Slides for my presentation at Japan Java User Group's [JJUG CCC 2019 Fall](https://ccc2019fall.java-users.jp/) conference.
* [GraalVM と Picocliで Javaのネイティブコマンドラインアプリを作ろう](https://remkop.github.io/presentations/20190906/) (2019-09-06) Slides for my lightning talk presentation at [【東京】JJUG ナイトセミナー: ビール片手にLT大会 9/6（金）](https://jjug.doorkeeper.jp/events/95987)
* [Picocli＋Spring Boot でコマンドラインアプリケーションを作成してみる](https://ksby.hatenablog.com/entry/2019/07/20/092721) (2019-07-20) by [かんがるーさんの日記](https://ksby.hatenablog.com/).
* [GraalVM の native image を使って Java で爆速 Lambda の夢を見る](https://qiita.com/kencharos/items/69e43965515f368bc4a3) (2019-05-02) by [@kencharos](https://qiita.com/kencharos)

#### 中文
* [从Commons CLI迁移到Picocli](https://blog.csdn.net/genghaihua/article/details/88529409) (2019-03-13): Chinese translation of Migrating from Commons CLI to picocli, thanks to [genghaihua](https://me.csdn.net/genghaihua).
* [Picocli 2.0: Steroids上的Groovy脚本](https://picocli.info/zh/picocli-2.0-groovy-scripts-on-steroids.html)
* [Picocli 2.0: 以少求多](https://picocli.info/zh/picocli-2.0-do-more-with-less.html) 

### Mailing List
Join the [picocli Google group](https://groups.google.com/d/forum/picocli) if you are interested in discussing anything picocli-related and receiving announcements on new releases.

### Credit
<img src="https://picocli.info/images/logo/horizontal-400x150.png" height="100"> 

[Reallinfo](https://github.com/reallinfo) designed the new picocli logo! Many thanks! 

### Commitments

| This project follows [semantic versioning](http://semver.org/) and adheres to the **[Zero Bugs Commitment](https://github.com/classgraph/classgraph/blob/master/Zero-Bugs-Commitment.md)**. |
|------------------------|

## Adoption

<img src="https://picocli.info/images/groovy-logo.png" height="50">  <img src="https://picocli.info/images/1x1.png" width="10"> <img src="http://micronaut.io/images/micronaut_mini_copy_tm.svg" height="50">  <img src="https://picocli.info/images/1x1.png" width="10"><img src="https://picocli.info/images/junit5logo-172x50.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/debian-logo-192x50.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/karate-logo.png" height="50" width="50"/>  <img src="https://picocli.info/images/checkstyle-logo-260x50.png" height="50"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/ballerina-logo.png" height="40"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/apache-hive-logo.png" height="50"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://hadoop.apache.org/hadoop-logo.jpg" height="50"><img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/apache-ozone-logo.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/stackshare-logo.png" height="50"> <img src="https://ignite.apache.org/images/Ignite_tm_Logo_blk_RGB.svg" height="50"> <img src="https://camo.githubusercontent.com/501aae78d282faf7a904bbb92f46eb8d19445ad5/687474703a2f2f736c696e672e6170616368652e6f72672f7265732f6c6f676f732f736c696e672e706e67" height="50"> 
<img src="https://avatars1.githubusercontent.com/u/541152?s=200&v=4" height="50">  <img src="https://camo.qiitausercontent.com/ec81e80366e061c8488b25c013003267b7a578d4/68747470733a2f2f71696974612d696d6167652d73746f72652e73332e616d617a6f6e6177732e636f6d2f302f3939352f33323331306534352d303537332d383534322d373035652d6530313138643434323632302e706e67" height="50">
<img src="https://spring.io/img/spring-by-pivotal.png" height="50">
<img src="https://www.schemacrawler.com/images/schemacrawler_logo.svg" height="50">
<img src="https://avatars1.githubusercontent.com/u/22600631?s=200&v=4" height="50">
<img src="https://fisco-bcos-documentation.readthedocs.io/en/latest/_static/images/FISCO_BCOS_Logo.svg" height="50">
<img src="https://avatars0.githubusercontent.com/u/35625214?s=200&v=4" height="50">
<img src="https://avatars1.githubusercontent.com/u/2386734?s=200&v=4" height="50">
<img src="https://www.e-contract.be/images/logo.svg" height="50">
<img src="https://present.co/images/logn-new@2x.png" height="50">
<img src="https://avatars2.githubusercontent.com/u/13641167?s=200&v=4" height="50">
<img src="https://www.viva64.com/media/img/logo.png" height="50">
<img src="https://concord.walmartlabs.com/assets/img/logo.png" height="50">
<img src="https://res-3.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/etxip1k2sx4sphvwgkdu" height="50">
<img src="https://www.minecraftforge.net/forum/uploads/set_resources_2/4eeef9d314eb4c008c0f37dacad2cdd5_logo.svg" height="50">

* Picocli is now part of Groovy. From Groovy 2.5, all Groovy command line tools are picocli-based, and picocli is the underlying parser for Groovy's [CliBuilder DSL](http://groovy-lang.org/dsls.html#_clibuilder). 
* Picocli is now part of Micronaut. The Micronaut CLI has been rewritten with picocli, and Micronaut has dedicated support for running microservices [standalone with picocli](https://docs.micronaut.io/snapshot/guide/index.html#commandLineApps).
* Picocli is now part of JUnit 5. JUnit 5.3 migrated its `ConsoleLauncher` from jopt-simple to picocli to support @-files (argument files); this helps users who need to specify many tests on the command line and run into system limitations.
* Debian now offers a [libpicocli-java package](https://tracker.debian.org/pkg/picocli). Thanks to [Miroslav Kravec](https://udd.debian.org/dmd/?kravec.miroslav%40gmail.com).
* Picocli is used in the Intuit [Karate](https://github.com/intuit/karate) standalone JAR / executable.
* Picocli is part of [Ballerina](https://ballerina.io/). Ballerina uses picocli for all its command line utilities.
* Picocli is used in the [CheckStyle](https://checkstyle.org/cmdline.html) standalone JAR / executable from Checkstyle 8.15.
* Picocli is included in the [OpenJDK Quality Outreach](https://wiki.openjdk.java.net/display/quality/Quality+Outreach) list of Free Open Source Software (FOSS) projects that actively test against OpenJDK builds.
* Picocli is used in the Apache Hadoop Ozone/HDDS command line tools, the Apache Hive benchmark CLI, Apache [Ignite TensorFlow](https://github.com/apache/ignite), and Apache Sling [Feature Model Converter](https://github.com/apache/sling-org-apache-sling-feature-modelconverter).
* Picocli is listed on [StackShare](https://stackshare.io/picocli). Please add it to your stack and add/upvote reasons why you like picocli!
* Picocli is used in Pinterest [ktlint](https://ktlint.github.io/).
* Picocli is used in Spring IO [nohttp-cli](https://github.com/spring-io/nohttp/tree/master/nohttp-cli).
* The [MinecraftPicocli](https://github.com/Rubydesic/MinecraftPicocli) library allows the use of picocli in [Minecraft Forge](https://files.minecraftforge.net/).
* [Simple Java Mail](http://www.simplejavamail.org/) now offers a picocli-based [CLI](http://www.simplejavamail.org/cli.html#navigation).
* [jbang](https://github.com/maxandersen/jbang) not only uses picocli internally, but also has a CLI template to generate an initial script: use `jbang --init=cli helloworld.java` to generate a sample picocli-enabled jbang script. See [asciinema](https://asciinema.org/a/AVwA19yijKRNKEO0bJENN2ME3?autoplay=true&speed=2). 


<img src="https://picocli.info/images/downloads-201911.png">

Glad to see more people are using picocli. We must be doing something right. :-) 

### Help to promote picocli
If you like picocli, there are a few things you can do to help:
* Upvote my [Quora answer](https://www.quora.com/What-is-the-best-way-to-parse-command-line-arguments-with-Java/answer/Remko-Popma) to "What is the best way to parse command-line arguments with Java?"
* Upvote my [StackOverflow answer](https://stackoverflow.com/a/43780433/1446916) to "How do I parse command line arguments in Java?"
* Tweet about picocli! What do you like about it? How has it helped you? How is it different from the alternatives?

If you like picocli and your project is on GitHub, consider adding this badge to your README.md: [![picocli](https://img.shields.io/badge/picocli-4.2.0-green.svg)](https://github.com/remkop/picocli)
```
[![picocli](https://img.shields.io/badge/picocli-4.2.0-green.svg)](https://github.com/remkop/picocli)
```


## Example

Annotate fields with the command line parameter names and description. Optionally implement `Runnable` or `Callable` to delegate error handling and requests for usage help or version help to picocli. For example:


```java
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.io.File;

@Command(name = "example", mixinStandardHelpOptions = true, version = "Picocli example 4.0")
public class Example implements Runnable {

    @Option(names = { "-v", "--verbose" },
      description = "Verbose mode. Helpful for troubleshooting. Multiple -v options increase the verbosity.")
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
        // By implementing Runnable or Callable, parsing, error handling and handling user
        // requests for usage help or version help can be done with one line of code.

        int exitCode = new CommandLine(new Example()).execute(args);
        System.exit(exitCode);
    }
}
```

Implement `Runnable` or `Callable`, and your command can be [executed](https://picocli.info/#execute) in one line of code. The example above uses the `CommandLine.execute` method to parse the command line, handle errors, handle requests for usage and version help, and invoke the business logic. Applications can call `System.exit` with the returned exit code to signal success or failure to their caller.

```bash
$ java Example -v inputFile1 inputFile2

2 files to process...
```

The `CommandLine.execute` method automatically prints the usage help message if the user requested help or when the input was invalid.

![Usage help message with ANSI colors](docs/images/ExampleUsageANSI.png?raw=true)

This can be customized in many ways. See the user manual [section on Executing Commands](https://picocli.info/#execute) for details.

## Usage Help with ANSI Colors and Styles

Colors, styles, headers, footers and section headings are easily [customized with annotations](https://picocli.info/#_ansi_colors_and_styles).
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
compile 'info.picocli:picocli:4.2.0'
```
### Maven
```
<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli</artifactId>
  <version>4.2.0</version>
</dependency>
```
### Scala SBT
```
libraryDependencies += "info.picocli" % "picocli" % "4.2.0"
```
### Ivy
```
<dependency org="info.picocli" name="picocli" rev="4.2.0" />
```
### Grape
```groovy
@Grapes(
    @Grab(group='info.picocli', module='picocli', version='4.2.0')
)
```
### Leiningen
```
[info.picocli/picocli "4.2.0"]
```
### Buildr
```
'info.picocli:picocli:jar:4.2.0'
```
