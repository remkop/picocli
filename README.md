<p align="center"><img src="docs/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>

[![GitHub Release](https://img.shields.io/github/release/remkop/picocli.svg)](https://github.com/remkop/picocli/releases)
[![Maven Central](https://img.shields.io/maven-central/v/info.picocli/picocli.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22info.picocli%22%20AND%20a:%22picocli%22)
[![GitHub Actions Build Status](https://github.com/remkop/picocli/actions/workflows/ci.yml/badge.svg)](https://github.com/remkop/picocli/actions/workflows/ci.yml)
[![Tests](https://gist.githubusercontent.com/remkop/36bc8a3b4395f2fbdb9bc271e97ba2dd/raw/badge.svg)](https://github.com/remkop/picocli/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/remkop/picocli/branch/master/graph/badge.svg)](https://codecov.io/gh/remkop/picocli)
[![Follow @remkopopma](https://img.shields.io/twitter/follow/remkopopma.svg?style=social)](https://twitter.com/intent/follow?screen_name=remkopopma)
[![Follow @picocli](https://img.shields.io/twitter/follow/picocli.svg?style=social)](https://twitter.com/intent/follow?screen_name=picocli)
[![Follow picocli on StackShare](https://img.shields.io/badge/Follow%20on-StackShare-blue.svg?logo=stackshare&style=flat)](https://stackshare.io/picocli)

# picocli - a mighty tiny command line interface

Picocli aims to be the easiest-to-use way to create rich command line applications that can run on and off the JVM.
Considering picocli? Check [what happy users say](https://github.com/remkop/picocli/wiki/Feedback-from-Users) about picocli.

Picocli is a modern library and framework, written in Java, that contains both an annotations API and a programmatic API. It features usage help with [ANSI colors and styles](https://picocli.info/#_ansi_colors_and_styles), [TAB autocompletion](https://picocli.info/autocomplete.html) and nested subcommands.
In a single file, so you can include it _in source form_.
This lets users run picocli-based applications without requiring picocli as an external dependency.

Picocli-based applications can be ahead-of-time compiled to a <img src="https://www.graalvm.org/resources/img/logo-colored.svg" alt="GraalVM">
[native image](https://picocli.info/#_graalvm_native_image), with extremely fast startup time and lower memory requirements,
which can be distributed as a single executable file.
Picocli comes with an [annotation processor](https://picocli.info/#_annotation_processor) that automatically Graal-enables your jar during compilation.

Picocli applications can be very compact with no boilerplate code: your command (or subcommand) can be executed with a [single line of code](#example "(example below)").
Simply implement `Runnable` or `Callable`, or put the business logic of your command in a `@Command`-annotated method.

<a id="picocli_demo"></a>
![Picocli Demo help message with ANSI colors](docs/images/picocli.Demo.png?raw=true)

Picocli makes it easy to follow [Command Line Interface Guidelines](https://clig.dev/#guidelines).

How it works: annotate your class and picocli initializes it from the command line arguments,
converting the input to strongly typed data. Supports git-like [subcommands](https://picocli.info/#_subcommands)
(and nested [sub-subcommands](https://picocli.info/#_nested_sub_subcommands)),
any option prefix style, POSIX-style [grouped short options](https://picocli.info/#_short_posix_options),
custom [type converters](https://picocli.info/#_custom_type_converters),
[password options](https://picocli.info/#_interactive_password_options) and more.

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
Requires at minimum Java 5, but is designed to facilitate the use of Java 8 lambdas. Tested on all [Java versions between 5 and 18-ea](https://github.com/remkop/picocli/actions/workflows/ci.yml) (inclusive).

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
[`@Spec` annotation](https://picocli.info/#spec-annotation), and
implement [custom parameter processing](https://picocli.info/#_custom_parameter_processing) for option parameters if the built-in logic is insufficient.


Picocli-based applications can easily [integrate](https://picocli.info/#_dependency_injection) with Dependency Injection containers.
The [Micronaut](https://micronaut.io/) microservices framework has [built-in support](https://docs.micronaut.io/latest/guide/index.html#commandLineApps) for picocli.
[Quarkus](https://quarkus.io/) has a [Command Mode with Picocli](https://quarkus.io/guides/picocli) extension for facilitating the creation of picocli-based CLI applications with Quarkus.
Picocli ships with a [`picocli-spring-boot-starter` module](https://github.com/remkop/picocli/tree/main/picocli-spring-boot-starter)
that includes a `PicocliSpringFactory` and Spring Boot auto-configuration to use Spring dependency injection in your picocli command line application.
The user manual has examples of integrating with [Guice](https://picocli.info/#_guice_example), [Spring Boot](https://picocli.info/#_spring_boot_example), [Micronaut](https://picocli.info/#_micronaut_example), [Quarkus](https://picocli.info/#_quarkus_example) and with containers that comply to [CDI 2.0 specification](https://picocli.info/#_cdi_2_0_jsr_365) (JSR 365).

### Releases
* [All Releases](https://github.com/remkop/picocli/releases)
* Latest: 4.7.5 [Release Notes](https://github.com/remkop/picocli/releases/tag/v4.7.5)
* Older: Picocli 4.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v4.0.0)
* Older: Picocli 3.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v3.0.0)
* Older: Picocli 2.0 [Release Notes](https://github.com/remkop/picocli/releases/tag/v2.0.0)

### Documentation
* [4.x User manual: https://picocli.info](https://picocli.info)
* [4.x Quick Guide](https://picocli.info/quick-guide.html)
* [4.x API Javadoc](https://picocli.info/apidocs/)
* [PREVIEW: Modular Javadoc for all artifacts (4.7.5-SNAPSHOT)](https://picocli.info/apidocs-all/)
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
* [6 things you can do with JBang but you can’t with Shell](http://www.mastertheboss.com/java/jbang-vs-jshell/) (2022-02-28) by [F.Marchioni](http://www.mastertheboss.com/author/admin/).
* [VIDEO][Kotlin, CLIs and StarWars! - An introduction to creating CLI applications with Kotlin using Picocli](https://fosdem.org/2022/schedule/event/kotlin_clis_and_starwars/?utm_medium=social&utm_source=twitter&utm_campaign=postfity&utm_content=postfity77511) (2022-02-05) by [Julien Lengrand-Lambert](https://fosdem.org/2022/schedule/speaker/julien_lengrand_lambert/).
* [VIDEO][Autocomplete Java CLI using Picocli](https://www.youtube.com/watch?v=tCrQqgOYszQ) (2022-01-24) by [raksrahul](https://www.youtube.com/channel/UCpYkDrjOq3xtt0Uyg9tEqvw).
* [Picocli – Easiness for CLI arguments in Java](https://blog.adamgamboa.dev/picocli-easiness-for-cli-arguments-in-java/) (2021-10-27) by [agamboa](https://blog.adamgamboa.dev/author/agamboa/).
* [Building Command Line Interfaces with Kotlin using picoCLI](https://foojay.io/today/building-command-line-interfaces-with-kotlin-using-picocli/) (2021-09-23) by [Julien Lengrand-Lambert](https://foojay.io/today/author/jlengrand/).
* [VIDEO][Create Java CLI applications with picocli](https://www.youtube.com/watch?v=PaxBXABJIzY) (2021-09-14) by [coder4life](https://www.youtube.com/channel/UCt9lHt5bMpafypEDwj6J2WQ).
* [PICOCLI](https://www.linkedin.com/pulse/picocli-sybren-boland/) (2021-06-30) by [Sybren Boland](https://www.linkedin.com/in/sybrenboland/).
* [Picocli | Create your first Kotlin /JVM CLI application with GraalVM](https://manserpatrice.medium.com/picocli-create-your-first-kotlin-jvm-cli-application-with-graalvm-a7fea4da7e2) (2021-02-13) by [manserpatrice](https://manserpatrice.medium.com/).
* [VIDEO] [Building kubectl plugins with Quarkus, picocli, fabric8io and jbang](https://www.youtube.com/watch?v=ZL29qrpk_Kc) (2021-01-22) by [Sébastien Blanc](https://twitter.com/sebi2706).
* [VIDEO] [J-Fall Virtual 2020: Julien Lengrand - An introduction to creating CLI applications using picoCLI](https://www.youtube.com/watch?v=Rc_D4OTKidU&list=PLpQuPreMkT6D36w9d13uGpIPi5nf9I_0c&index=13) (2020-12-07) by [Julien Lengrand-Lambert](https://twitter.com/jlengrand). This was the top rated talk for [@nljug](https://twitter.com/nljug) #jfall virtual 2020! Congrats, Julien!
* [Paginate results in a command line application using picoCLI](https://lengrand.fr/paginate-results-in-a-jvm-cli-application-using-picocli/) (2020-11-17) by [Julien Lengrand-Lambert](https://twitter.com/jlengrand).
* [CLI applications with GraalVM Native Image](https://medium.com/graalvm/cli-applications-with-graalvm-native-image-d629a40aa0be) (2020-11-13) by [Oleg Šelajev](https://twitter.com/shelajev).
* [Picocli subcommands - One program, many purposes](https://aragost.com/blog/java/picocli-subcommands.html) (2020-09-22) by [Jonas Andersen](https://twitter.com/PrimusAlgo).
* [Native CLI with Picocli and GraalVM](https://dev.to/jbebar/native-cli-with-picocli-and-graalvm-566m) (2020-08-20) by [jbebar](https://dev.to/jbebar).
* [How to build a CLI app in Java using jbang and picocli](https://www.twilio.com/blog/cli-app-java-jbang-picocli) (2020-08-13) by [Matthew Gilliard](https://twitter.com/MaximumGilliard).
* [Building a GitHub Dependents Scraper with Quarkus and Picocli](https://blog.marcnuri.com/github-dependents-scraper-quarkus-picocli/) (2020-07-31) by [Marc Nuri](https://twitter.com/MarcNuri).
* [Building a decent Java CLI](https://atextor.de/2020/07/27/building-a-decent-java-cli.html) (2020-07-27) by [Andreas Textor](https://twitter.com/atextor).
* [VIDEO] (Another very well-produced video by Szymon Stepniak) [Implementing OAuth 2.0 in a Java command-line app using Micronaut, Picocli, and GraalVM](https://www.youtube.com/watch?v=js5H9UbmmMY) (2020-07-23) by [Szymon Stepniak](https://e.printstacktrace.blog/) ([YouTube channel](https://www.youtube.com/channel/UCEf8e5YAYnowq-2deW4tpsw)).
* [Micronaut, Picocli, and GraalVM](https://e.printstacktrace.blog/building-stackoverflow-cli-with-java-11-micronaut-picocli-and-graalvm/) (2020-07-08) by [Szymon Stepniak](https://e.printstacktrace.blog/).
* [VIDEO] (Extremely well-produced and informative, recommended!) [Building command-line app with Java 11, Micronaut, Picocli, and GraalVM](https://www.youtube.com/watch?v=Xdcg4Drg1hc) (2020-07-01) by [Szymon Stepniak](https://e.printstacktrace.blog/) ([YouTube channel](https://www.youtube.com/channel/UCEf8e5YAYnowq-2deW4tpsw)).
* [AUDIO] [Scala Valentines #2](https://scala.love/scala-valentines-2/) (2020-06-21) Podcast talks about picocli (from 18:11).
* [How to create a command line tool using Java?](https://fullstackdeveloper.guru/2020/06/18/how-to-create-a-command-line-tool-using-java/) (2020-06-18) by [Vijay SRJ](https://twitter.com/FullStackDevel6).
* [Command-line tools with Quarkus and Picocli](https://quarkify.net/command-line-tools-with-quarkus-and-picocli/) (2020-06-08) by [Dmytro Chaban](https://twitter.com/dmi3coder).
* Quarkus guide for [Quarkus command mode with picocli](https://quarkus.io/guides/picocli), thanks to a picocli extension by [Michał Górniewski](https://github.com/mgorniew) included in [Quarkus 1.5](https://quarkus.io/blog/quarkus-1-5-final-released/) (2020-06-03).
* [Native images with Micronaut and GraalVM](https://dev.to/stack-labs/native-images-with-micronaut-and-graalvm-4koe) (2020-06-01) by [Λ\: Olivier Revial](https://twitter.com/pommeDouze).
* [CLI applications with Micronaut and Picocli](https://dev.to/stack-labs/cli-applications-with-micronaut-and-picocli-4mc8) (2020-06-01) by [Λ\: Olivier Revial](https://twitter.com/pommeDouze).
* [Picocli introduction - Modern Java command-line parsing](https://aragost.com/blog/java/picocli-introduction.html) (2020-05-19) by [Jonas Andersen](https://twitter.com/PrimusAlgo).
* [Building Native Covid19 Tracker CLI using Java, PicoCLI & GraalVM](https://aboullaite.me/java-covid19-cli-picocli-graalvm/) (2020-05-11) by [Mohammed Aboullaite](https://aboullaite.me/author/mohammed/).
* [Quarkus Command mode with Picocli](https://quarkify.net/quarkus-command-mode-with-picocli/) (2020-04-27) by [Dmytro Chaban](https://twitter.com/dmi3coder).
* [Creating CLI tools with Scala, Picocli and GraalVM](https://medium.com/@takezoe/creating-cli-tools-with-scala-picocli-and-graalvm-ffde05bbd01d) (2020-03-09) by [Naoki Takezoe](https://twitter.com/takezoen)
* [Building native Java CLIs with GraalVM, Picocli, and Gradle](https://medium.com/@mitch.seymour/building-native-java-clis-with-graalvm-picocli-and-gradle-2e8a8388d70d) (2020-03-08) by [Mitch Seymour](https://medium.com/@mitch.seymour)
* [Build Great Native CLI Apps in Java with Graalvm and Picocli](https://www.infoq.com/articles/java-native-cli-graalvm-picocli/) (2020-03-07)
* [Picocli Structured Objects](https://gist.github.com/hanslovsky/8276da86c53bc6d95bf01447cd5cb2b7#file-00_picocli-structured-objects-md) (2019-09-10) by [Philipp Hanslovsky](https://gist.github.com/hanslovsky) explains how to use picocli's support for repeating argument groups to add or configure structured objects from the command line.
* [Create a Java Command Line Program with Picocli|Baeldung](https://www.baeldung.com/java-picocli-create-command-line-program) (2019-05-07) by [François Dupire](https://www.baeldung.com/author/francois-dupire/).
* A whirlwind tour of picocli [JAX Magazine "Putting the spotlight on Java tools"](https://jaxenter.com/jax-mag-java-tools-157592.html) (2019-04-08).
* [An Introduction to PicoCLI](https://devops.datenkollektiv.de/an-introduction-to-picocli.html) (2019-02-10) by [devop](https://devops.datenkollektiv.de/author/devop.html).
* [Corda CLI UX (User Experience) Guide](https://docs.corda.net/head/cli-ux-guidelines.html) (2018 by R3 Limited) gives useful advice.
* [Develop a CLI tool using groovy scripts](https://medium.com/@chinthakadinadasa/develop-a-cli-tool-using-groovy-scripts-a7d545eecddd) (2018-10-26) by [Chinthaka Dinadasa](https://medium.com/@chinthakadinadasa).
* [Migrating from Commons CLI to picocli](https://picocli.info/migrating-from-commons-cli.html). You won't regret it! :-) (also on: [DZone](https://dzone.com/articles/migrating-from-commons-cli-to-picocli) and [Java Code Geeks](https://www.javacodegeeks.com/2018/11/migrating-commons-cli-picocli.html)).
* [Groovy 2.5 CliBuilder Renewal](https://picocli.info/groovy-2.5-clibuilder-renewal.html) (also on [blogs.apache.org](https://blogs.apache.org/logging/entry/groovy-2-5-clibuilder-renewal)). In two parts: [Part 1](https://picocli.info/groovy-2.5-clibuilder-renewal-part1.html) (also on: [DZone](https://dzone.com/articles/groovy-25-clibuilder-renewal), [Java Code Geeks](https://www.javacodegeeks.com/2018/06/groovy-clibuilder-renewal-part-1.html)), [Part 2](https://picocli.info/groovy-2.5-clibuilder-renewal-part2.html) (also on: [DZone](https://dzone.com/articles/groovy-25-clibuilder-renewal-part-2), [Java Code Geeks](https://www.javacodegeeks.com/2018/06/groovy-clibuilder-renewal-part-2.html)).
* Micronaut user manual for running microservices [standalone with picocli](https://docs.micronaut.io/snapshot/guide/index.html#commandLineApps).
* [Java Command-Line Interfaces (Part 30): Observations](https://marxsoftware.blogspot.jp/2017/11/java-cmd-line-observations.html) by Dustin Marx about picocli 2.0.1 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-30-finale-observations), [Java Code Geeks](https://www.javacodegeeks.com/2017/11/java-command-line-interfaces-part-30-observations.html))
* [Java Command-Line Interfaces (Part 10): Picocli](https://marxsoftware.blogspot.jp/2017/08/picocli.html) by Dustin Marx about picocli 0.9.7 (also on: [DZone](https://dzone.com/articles/java-command-line-interfaces-part-10-picocli), [Java Code Geeks](https://www.javacodegeeks.com/2017/08/java-command-line-interfaces-part-10-picocli.html))
* [Picocli 2.0: Groovy Scripts on Steroids](https://picocli.info/picocli-2.0-groovy-scripts-on-steroids.html) (also on: [DZone](https://dzone.com/articles/picocli-v2-groovy-scripts-on-steroids), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-groovy-scripts-steroids.html))
* [Picocli 2.0: Do More With Less](https://picocli.info/picocli-2.0-do-more-with-less.html) (also on: [DZone](https://dzone.com/articles/whats-new-in-picocli-20), [Java Code Geeks](https://www.javacodegeeks.com/2018/01/picocli-2-0-less.html))
* [Announcing picocli 1.0](https://picocli.info/announcing-picocli-1.0.html) (also on: [DZone](https://dzone.com/articles/announcing-picocli-10))

#### русский
* [Выбор необходимых опций Picocli на основе основного варианта](https://coderoad.ru/61665865/%D0%92%D1%8B%D0%B1%D0%BE%D1%80-%D0%BD%D0%B5%D0%BE%D0%B1%D1%85%D0%BE%D0%B4%D0%B8%D0%BC%D1%8B%D1%85-%D0%BE%D0%BF%D1%86%D0%B8%D0%B9-Picocli-%D0%BD%D0%B0-%D0%BE%D1%81%D0%BD%D0%BE%D0%B2%D0%B5-%D0%BE%D1%81%D0%BD%D0%BE%D0%B2%D0%BD%D0%BE%D0%B3%D0%BE-%D0%B2%D0%B0%D1%80%D0%B8%D0%B0%D0%BD%D1%82%D0%B0) (2020-05-07)
* [Интерфейсы командной строки Java: picocli](https://habr.com/ru/company/otus/blog/419401/) (2018-08-06): Russian translation by [MaxRokatansky](https://habr.com/ru/users/MaxRokatansky/) of Dustin Marx' blog post.

#### Español
* [picocli, un poderoso framework para la creación de aplicaciones de línea de comandos](https://laboratoriolinux.es/index.php/-noticias-mundo-linux-/software/34261-picocli-un-poderoso-framework-para-la-creacion-de-aplicaciones-de-linea-de-comandos.html) (2023-09-09) by [Darkcrizt](https://ubunlog.com/author/darkcrizt/)
* [picocli, un poderoso framework para la creación de aplicaciones de línea de comandos](https://ubunlog.com/picocli-un-poderoso-framework-para-la-creacion-de-aplicaciones-de-linea-de-comandos/) (2023-09-09) by [Darkcrizt](https://ubunlog.com/author/darkcrizt/)
* [Quarkus + Picocli: Web scaper para extraer proyectos dependientes en GitHub](https://blog.marcnuri.com/quarkus-picocli-web-scaper-dependientes-github/) (2020-08-15) by [Marc Nuri](https://twitter.com/MarcNuri).
* [Quarkus - Introducción: picocli](https://gerardo.dev/aws-quarkus-picocli.html) (2020-06-15) by [Gerardo Arroyo](https://twitter.com/codewarrior506).
* [VIDEO] [Picocli - Spring Boot example](https://youtu.be/y9ayfjfrTF4) (2020-05-24) 7-minute quick introduction by Gonzalo H. Mendoza.

#### Français
* [Application mobile: Créez de superbes applications CLI natives en Java avec Graalvm et Picocli](https://seodigitalmarketing.net/application-mobile-creez-de-superbes-applications-cli-natives-en-java-avec-graalvm-et-picocli/) (2020-05-07) Translation of [Build Great Native CLI Apps in Java with Graalvm and Picocli](https://www.infoq.com/articles/java-native-cli-graalvm-picocli/) by [bouf1450](https://seodigitalmarketing.net/author/bouf1450/).
* [VIDEO] [Des applications en ligne de commande avec Picocli et GraalVM (N. Peters)](https://www.youtube.com/watch?v=8ENbMwkaFyk) (2019-05-07): 15-minute presentation by Nicolas Peters during Devoxx FR. Presentation slides are [available on GitHub](https://t.co/tXhtpTpAff?amp=1).

#### Português
* [Desenvolva aplicações CLI nativas em Java com Graalvm e Picocli](https://www.infoq.com/br/articles/java-native-cli-graalvm-picocli/) (2020-08-28): Portuguese translation of [Build Great Native CLI Apps in Java with Graalvm and Picocli](https://www.infoq.com/articles/java-native-cli-graalvm-picocli/), thanks to [Rodrigo Ap G Batista](https://www.infoq.com/br/profile/Rodrigo-Ap-G-Batista/).
* [VIDEO] [Quarkus #40: Command Mode com Picocli](https://www.youtube.com/watch?v=LweGDh-Jxlc) (2020-06-23): 13-minute presentation by [Vinícius Ferraz](https://www.youtube.com/channel/UCJNOHl-pTTTj4S9yq60Ps9A) (@viniciusfcf).

#### 日本語
* [CLI applications with GraalVM Native Image](https://logico-jp.io/2020/11/21/cli-applications-with-graalvm-native-image/) (2020-11-21) translation by [Logico_jp](https://logico-jp.io/who-is-logico/) of Oleg Šelajev's [post](https://medium.com/graalvm/cli-applications-with-graalvm-native-image-d629a40aa0be).
* [Picocli + Kotlin + graalvm-native-image plugin でネイティブツールを作る](https://mike-neck.hatenadiary.com/entry/2020/04/24/090000) (2020-04-24) blog post by [mike-neck](https://mike-neck.hatenadiary.com/about) ([引きこもり持田](https://twitter.com/mike_neck) on Twitter).
* [pythonのArgumentParserような使い心地！picocliのご紹介](https://lab.astamuse.co.jp/entry/2020/04/15/115000) (2020-04-15) by [@astamuseLab](https://lab.astamuse.co.jp/)
* [Javaのコマンドラインアプリケーション向けのフレームワーク、picocliで遊ぶ](https://kazuhira-r.hatenablog.com/entry/2020/03/07/013626) (2020-03-07) blog post by [かずひら](https://twitter.com/kazuhira_r).
* [KuromojiのCLIコマンドとpicocliとGraalVM](https://blog.johtani.info/blog/2020/02/28/kuromoji-cli/) (2020-02-28) blog post by [@johtani](https://twitter.com/johtani).
* [GraalVM, PicocliとJavaでときめくネイティブコマンドラインアプリを作ろう](https://remkop.github.io/presentations/20191123/) (2019-11-23) Slides for my presentation at Japan Java User Group's [JJUG CCC 2019 Fall](https://ccc2019fall.java-users.jp/) conference.
* [Picocliを使用してJavaコマンドラインプログラムを作成する - 開発者ドキュメント](https://ja.getdocs.org/java-picocli-create-command-line-program/) (2019-10-18)
* [GraalVM と Picocliで Javaのネイティブコマンドラインアプリを作ろう](https://remkop.github.io/presentations/20190906/) (2019-09-06) Slides for my lightning talk presentation at [【東京】JJUG ナイトセミナー: ビール片手にLT大会 9/6（金）](https://jjug.doorkeeper.jp/events/95987)
* [Picocli＋Spring Boot でコマンドラインアプリケーションを作成してみる](https://ksby.hatenablog.com/entry/2019/07/20/092721) (2019-07-20) by [かんがるーさんの日記](https://ksby.hatenablog.com/).
* [GraalVM の native image を使って Java で爆速 Lambda の夢を見る](https://qiita.com/kencharos/items/69e43965515f368bc4a3) (2019-05-02) by [@kencharos](https://qiita.com/kencharos)

#### 中文
* [Java命令行界面（第10部分）：picocli](https://blog.csdn.net/dnc8371/article/details/106702365) (2020-06-07) translation by [dnc8371](https://blog.csdn.net/dnc8371).
* [如何借助 Graalvm 和 Picocli 构建 Java 编写的原生 CLI 应用](https://www.infoq.cn/article/4RRJuxPRE80h7YsHZJtX) (2020-03-26): Chinese translation of [Build Great Native CLI Apps in Java with Graalvm and Picocli](https://www.infoq.com/articles/java-native-cli-graalvm-picocli/), thanks to [张卫滨](https://www.infoq.cn/profile/1067660).
* [从Commons CLI迁移到Picocli](https://blog.csdn.net/genghaihua/article/details/88529409) (2019-03-13): Chinese translation of Migrating from Commons CLI to picocli, thanks to [genghaihua](https://me.csdn.net/genghaihua).
* [Picocli 2.0: Steroids上的Groovy脚本](https://picocli.info/zh/picocli-2.0-groovy-scripts-on-steroids.html)
* [Picocli 2.0: 以少求多](https://picocli.info/zh/picocli-2.0-do-more-with-less.html)

### Mailing List
Join the [picocli Google group](https://groups.google.com/d/forum/picocli) if you are interested in discussing anything picocli-related and receiving announcements on new releases.

### Credit
<img src="https://picocli.info/images/logo/horizontal-400x150.png" height="100">

[Reallinfo](https://github.com/reallinfo) designed the picocli logo! Many thanks!

### Commitments

| This project follows [semantic versioning](https://semver.org/) and adheres to the **[Zero Bugs Commitment](https://github.com/classgraph/classgraph/blob/f24fb4e8f2e4f3221065d755be6e65d59939c5d0/Zero-Bugs-Commitment.md)**. |
|------------------------|

## Adoption

<div>
<img src="https://picocli.info/images/groovy-logo.png" height="50"> <!--groovy--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://objectcomputing.com/files/3416/2275/4315/micronaut_horizontal_black.svg" height="50"><!--micronaut--><img src="https://picocli.info/images/1x1.png" width="10"><img src="https://avatars.githubusercontent.com/u/47638783?s=200&v=4" height="50"><!--quarkus--><img src="https://picocli.info/images/1x1.png" width="10"><img src="https://picocli.info/images/junit5logo-172x50.png" height="50"><!--junit5--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/debian-logo-192x50.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/8433081?s=200&v=4" height="50"><!--intel--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/4430336?s=200&v=4" height="50"><!--oracle--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/1666512?s=200&v=4" height="50"><!--JOOQ--> <img src="https://picocli.info/images/1x1.png" width="10"><img src="https://avatars.githubusercontent.com/u/545988?s=200&v=4" height="50"><!--SonarSource--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/47359?s=200&v=4" height="50"><!--Apache--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/3287599?s=200&v=4" height="50"><!--ESA--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/438548?s=200&v=4" height="50"><!--Liquibase--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/8976946?s=200&v=4" height="50"><!--Harvard PL--> <img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://avatars.githubusercontent.com/u/2810941?s=200&v=4" height="50"><!--Google Cloud services--> <img src="https://picocli.info/images/1x1.png" width="10">

<img src="https://avatars.githubusercontent.com/u/317776?s=200&v=4" height="50"><!--Spring-->
<img src="https://avatars0.githubusercontent.com/u/3299148?s=200&v=4" height="50">
<img src="https://avatars3.githubusercontent.com/u/39734771?s=200&v=4" height="50">
<img src="https://avatars3.githubusercontent.com/u/1453152?s=200&v=4" height="50">
<img src="https://avatars1.githubusercontent.com/u/201120?s=200&v=4" height="50">
<img src="https://avatars0.githubusercontent.com/u/6154722?s=200&v=4" height="50">
<img src="https://avatars3.githubusercontent.com/u/453694?s=200&v=4" height="50">
<img src="https://avatars0.githubusercontent.com/u/82592?s=200&v=4" height="50">
<img src="https://avatars0.githubusercontent.com/u/9312489?s=200&v=4" height="50">
<img src="https://avatars0.githubusercontent.com/u/59439283?s=200&v=4" height="50">
<img src="https://avatars1.githubusercontent.com/u/4186383?s=200&v=4" height="50">
<img src="https://redis.com/wp-content/uploads/2021/08/redis-logo.png" height="50">
<img src="https://picocli.info/images/karate-logo.png" height="50" width="50"/>  <img src="https://picocli.info/images/checkstyle-logo-260x50.png" height="50"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/ballerina-logo.png" height="40"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/apache-hive-logo.png" height="50"><img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://hadoop.apache.org/hadoop-logo.jpg" height="50"><img src="https://picocli.info/images/1x1.png" width="10"> <img src="https://picocli.info/images/apache-ozone-logo.png" height="50"> <img src="https://picocli.info/images/1x1.png" width="10">  <img src="https://picocli.info/images/stackshare-logo.png" height="50"> <img src="https://ignite.apache.org/images/Ignite_tm_Logo_blk_RGB.svg" height="50"> <img src="https://camo.githubusercontent.com/501aae78d282faf7a904bbb92f46eb8d19445ad5/687474703a2f2f736c696e672e6170616368652e6f72672f7265732f6c6f676f732f736c696e672e706e67" height="50">
<img src="https://avatars1.githubusercontent.com/u/541152?s=200&v=4" height="50">  <img src="https://camo.qiitausercontent.com/ec81e80366e061c8488b25c013003267b7a578d4/68747470733a2f2f71696974612d696d6167652d73746f72652e73332e616d617a6f6e6177732e636f6d2f302f3939352f33323331306534352d303537332d383534322d373035652d6530313138643434323632302e706e67" height="50">
<img src="https://mk0upserved13l70iwek.kinstacdn.com/media/Upserve_LS_Lockup_000000_RGB_Vertical.svg" height="50">
<img src="https://www.kloudtek.com/logo-dark.png" height="50">
<img src="https://www.schemacrawler.com/images/schemacrawler_logo.svg" height="50">
<img src="https://avatars1.githubusercontent.com/u/22600631?s=200&v=4" height="50">
<img src="https://fisco-bcos-documentation.readthedocs.io/en/latest/_static/images/FISCO_BCOS_Logo.svg" height="50">
<img src="https://avatars0.githubusercontent.com/u/35625214?s=200&v=4" height="50">
<img src="https://avatars1.githubusercontent.com/u/2386734?s=200&v=4" height="50">
<img src="https://www.e-contract.be/images/logo.svg" height="50">
<img src="https://present.co/images/logn-new@2x.png" height="50">
<img src="https://avatars2.githubusercontent.com/u/13641167?s=200&v=4" height="50">
<img src="https://concord.walmartlabs.com/assets/img/logo.png" height="50">
<img src="https://res-3.cloudinary.com/crunchbase-production/image/upload/c_lpad,h_120,w_120,f_auto,b_white,q_auto:eco/etxip1k2sx4sphvwgkdu" height="50">
<img src="https://avatars.githubusercontent.com/u/1390178?s=200&v=4" height="50"><!-- minecraft forge-->

</div>


* Picocli is now part of Groovy. From Groovy 2.5, all Groovy command line tools are picocli-based, and picocli is the underlying parser for Groovy's [CliBuilder DSL](https://groovy-lang.org/dsls.html#_clibuilder).
* Picocli is now part of Micronaut. The Micronaut CLI has been rewritten with picocli, and Micronaut has dedicated support for running microservices [standalone with picocli](https://docs.micronaut.io/snapshot/guide/index.html#commandLineApps). See also [Micronaut Picocli Guide](https://micronaut-projects.github.io/micronaut-picocli/latest/guide/).
* Quarkus now offers [Command mode with picocli](https://quarkus.io/guides/picocli).
* Picocli is now part of JUnit 5. JUnit 5.3 migrated its `ConsoleLauncher` from jopt-simple to picocli to support @-files (argument files); this helps users who need to specify many tests on the command line and run into system limitations.
* Debian now offers a [libpicocli-java package](https://tracker.debian.org/pkg/picocli). Thanks to [Miroslav Kravec](https://udd.debian.org/dmd/?kravec.miroslav%40gmail.com).
* Picocli is used in the Intuit [Karate](https://github.com/intuit/karate) standalone JAR / executable.
* Picocli is part of [Ballerina](https://ballerina.io/). Ballerina uses picocli for all its command line utilities.
* Picocli is used in the [CheckStyle](https://checkstyle.org/cmdline.html) standalone JAR / executable from Checkstyle 8.15.
* Picocli is included in the [OpenJDK Quality Outreach](https://wiki.openjdk.java.net/display/quality/Quality+Outreach) list of Free Open Source Software (FOSS) projects that actively test against OpenJDK builds.
* Picocli is used in the Apache Hadoop Ozone/HDDS command line tools, the Apache Hive benchmark CLI, Apache [Ignite TensorFlow](https://github.com/apache/ignite), and Apache Sling [Feature Model Converter](https://github.com/apache/sling-org-apache-sling-feature-modelconverter).
* Picocli is listed on [StackShare](https://stackshare.io/picocli). Please add it to your stack and add/upvote reasons why you like picocli!
* Picocli is used in Pinterest [ktlint](https://ktlint.github.io/).
* Picocli is used in Spring IO [nohttp-cli](https://github.com/spring-io/nohttp/tree/main/nohttp-cli).
* The [MinecraftPicocli](https://github.com/Rubydesic/MinecraftPicocli) library facilitates the use of picocli in [Minecraft Forge](https://files.minecraftforge.net/).
* [Simple Java Mail](https://www.simplejavamail.org/) now offers a picocli-based [CLI](https://www.simplejavamail.org/cli.html#navigation).
* [jbang](https://github.com/maxandersen/jbang) not only uses picocli internally, but also has a CLI template to generate an initial script: use `jbang --init=cli helloworld.java` to generate a sample picocli-enabled jbang script. See [asciinema](https://asciinema.org/a/AVwA19yijKRNKEO0bJENN2ME3?autoplay=true&speed=2).
* Picocli is the main library used in the CookieTemple [cli-java template](https://cookietemple.readthedocs.io/en/latest/available_templates/available_templates.html#cli-java) for building GraalVM native CLI executables in Java. See [this preview](https://user-images.githubusercontent.com/21954664/86740903-474a3000-c037-11ea-9ae3-1a8f7bf1743f.gif).
* Picocli is [mentioned](https://clig.dev/#the-basics) in [Command Line Interface Guidelines](https://clig.dev/).

<img src="https://picocli.info/images/downloads-202011.png">

Glad to see more people are using picocli. We must be doing something right. :-)

### Contribute by helping to promote picocli
If you like picocli, help others discover picocli:

#### Easy and impactful :sweat_smile:
* Give picocli a star on GitHub!
* Upvote my [StackOverflow answer](https://stackoverflow.com/a/43780433/1446916) to "How do I parse command line arguments in Java?"
* Upvote my [Quora answer](https://www.quora.com/What-is-the-best-way-to-parse-command-line-arguments-with-Java/answer/Remko-Popma) to "What is the best way to parse command-line arguments with Java?"

#### Spread the joy! :tada:
* Tweet about picocli! What do you like about it? How has it helped you? How is it different from the alternatives?
* Mention that your project uses picocli in the documentation of your project.
* Show that your GitHub project uses picocli, with this badge in your README.md: [![picocli](https://img.shields.io/badge/picocli-4.7.5-green.svg)](https://github.com/remkop/picocli)
```
[![picocli](https://img.shields.io/badge/picocli-4.7.5-green.svg)](https://github.com/remkop/picocli)
```

#### Preach it! :muscle:
* Perhaps the most impactful way to show people how picocli can make their life easier is to write a blog post or article, or even do a video!


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

See the [source code](https://github.com/remkop/picocli/blob/main/src/test/java/picocli/CustomLayoutDemo.java#L61).

## Download
You can add picocli as an external dependency to your project, or you can include it as source.
See the [source code](https://github.com/remkop/picocli/blob/main/src/main/java/picocli/CommandLine.java). Copy and paste it into a file called `CommandLine.java`, add it to your project, and enjoy!

### Gradle
```
implementation 'info.picocli:picocli:4.7.5'
```
### Maven
```
<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli</artifactId>
  <version>4.7.5</version>
</dependency>
```
### Scala SBT
```
libraryDependencies += "info.picocli" % "picocli" % "4.7.5"
```
### Ivy
```
<dependency org="info.picocli" name="picocli" rev="4.7.5" />
```
### Grape
```groovy
@Grapes(
    @Grab(group='info.picocli', module='picocli', version='4.7.5')
)
```
### Leiningen
```
[info.picocli/picocli "4.7.5"]
```
### Buildr
```
'info.picocli:picocli:jar:4.7.5'
```

### JBang
```
//DEPS info.picocli:picocli:4.7.5
```

