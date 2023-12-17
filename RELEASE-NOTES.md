# picocli Release Notes

# <a name="4.7.6"></a> Picocli 4.7.6 (UNRELEASED)
The picocli community is pleased to announce picocli 4.7.6.

This release includes bugfixes and enhancements.

Many thanks to the picocli community for raising these issues and providing the pull requests to address them!

This is the eighty-fifth public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.6-toc"></a> Table of Contents
* [New and noteworthy](#4.7.6-new)
* [Fixed issues](#4.7.6-fixes)
* [Deprecations](#4.7.6-deprecated)
* [Potential breaking changes](#4.7.6-breaking-changes)

## <a name="4.7.6-new"></a> New and Noteworthy

`PropertiesDefaultProvider` now tries to load properties from the classpath if the file cannot be found  in the user.home directory.



## <a name="4.7.6-fixes"></a> Fixed issues

* [#2102][#2107] Enhancement: `PropertiesDefaultProvider` should try to load properties from classpath (last). Thanks to [Lumír Návrat](https://github.com/rimuln) for the pull request.
* [#2058] Bugfix: `defaultValue` should not be applied in addition to user-specified value for options with a custom `IParameterConsumer`. Thanks to [Staffan Arvidsson McShane](https://github.com/StaffanArvidsson) for raising this.
* [#2148] Bugfix: Fix NPE in jline3 `Example.jar` as `ConfigurationPath` cannot be `null` anymore. Thanks to [llzen44](https://github.com/llzen44) for the pull request.
* [#2171] Fix a few typos in CommandLine's JavaDoc. Thanks to [Michael Vorburger](https://github.com/vorburger) for the pull request.
* [#2172] Fix broken build. Thanks to [Michael Vorburger](https://github.com/vorburger) for the pull request.
* [#2174] Fix .gitattributes related CR/LF problems. Thanks to [Michael Vorburger](https://github.com/vorburger) for the pull request.
* [#2053] [#2175] Remove unused extra format arguments. Thanks to [Michael Vorburger](https://github.com/vorburger) for the pull request.
* [#2054][#2176] Add Error Prone. Thanks to [Michael Vorburger](https://github.com/vorburger) for the pull request.
* [#2170] Use `...` vararg instead of array parameter to match overridden method signature. Thanks to [Michael Vorburger](https://github.com/vorburger) for the pull request.
* [#2047] DEP: Bump andymckay/append-gist-action from 1fbfbbce708a39bd45846f0955ed5521f2099c6d to 6e8d64427fe47cbacf4ab6b890411f1d67c07f3e
* [#2091] DEP: Bump actions/checkout from 3.5.2 to 3.6.0
* [#2108] DEP: Bump actions/checkout from 3.6.0 to 4.0.0
* [#2120] DEP: Bump actions/checkout from 4.0.0 to 4.1.0
* [#2098] DEP: Bump actions/setup-java from 3.11.0 to 3.12.0
* [#2158] DEP: Bump actions/setup-java from 3.12.0 to 4.0.0
* [#2111] DEP: Bump actions/upload-artifact from 3.1.2 to 3.1.3
* [#2099] DEP: Bump gradle/gradle-build-action from 2.4.2 to 2.8.0
* [#2165] DEP: Bump gradle/gradle-build-action from 2.8.0 to 2.11.0
* [#2096] DEP: Bump gradle/wrapper-validation-action from 1.0.6 to 1.1.0
* [#2085] DEP: Bump github/codeql-action from 2.3.5 to 2.21.4
* [#2114] DEP: Bump github/codeql-action from 2.21.4 to 2.21.7
* [#2167] DEP: Bump github/codeql-action from 2.21.7 to 3.22.11
* [#2093] DEP: Bump junit5Version from 5.9.3 to 5.10.0
* [#2140] DEP: Bump log4j2Version from 2.20.0 to 2.21.1
* [#2095] DEP: Bump org.apache.ivy:ivy from 2.5.1 to 2.5.2
* [#2094] DEP: Bump org.asciidoctor:asciidoctorj-pdf from 2.3.7 to 2.3.9
* [#2135] DEP: Bump org.fusesource.jansi:jansi from 2.4.0 to 2.4.1
* [#2089] DEP: Bump org.jetbrains.kotlin:kotlin-gradle-plugin from 1.8.21 to 1.9.10
* [#2154] DEP: Bump org.jetbrains.kotlin:kotlin-gradle-plugin from 1.9.10 to 1.9.21
* [#2090] DEP: Bump org.jetbrains.kotlin:kotlin-script-runtime from 1.8.21 to 1.9.10
* [#2146] DEP: Bump org.jline:jline from 3.23.0 to 3.24.1
* [#2049] DEP: Bump org.hibernate.validator:hibernate-validator from 8.0.0.Final to 8.0.1.Final
* [#2037] DEP: Bump org.scala-lang:scala-library from 2.13.10 to 2.13.11
* [#2112] DEP: Bump org.scala-lang:scala-library from 2.13.11 to 2.13.12
* [#2052] DEP: Bump ossf/scorecard-action from 2.1.3 to 2.2.0
* [#2082] DEP: Bump step-security/harden-runner from 2.4.0 to 2.5.1
* [#2152] DEP: Bump step-security/harden-runner from 2.5.1 to 2.6.1
* DEP: Bump SpringBoot from 2.7.1 to 2.7.2 (for Java versions prior to 17)


## <a name="4.7.6-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.6-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.7.5"></a> Picocli 4.7.5
The picocli community is pleased to announce picocli 4.7.5.

This release includes bugfixes and enhancements.

Many thanks to the picocli community for raising these issues and providing the pull requests to address them!

This is the eighty-forth public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.5-toc"></a> Table of Contents
* [New and noteworthy](#4.7.5-new)
* [Fixed issues](#4.7.5-fixes)
* [Deprecations](#4.7.5-deprecated)
* [Potential breaking changes](#4.7.5-breaking-changes)

## <a name="4.7.5-new"></a> New and Noteworthy



## <a name="4.7.5-fixes"></a> Fixed issues
* [#2083][#2084] Enhancement: Java 22 update: improve logic for detecting if the output stream is connected to a terminal. Thanks to [Liam Miller-Cushon](https://github.com/cushon) for the pull request.
* [#2087] Enhancement: Mask parameters in trace log when `echo=false` for `interactive` options and positional parameters. Thanks to [szzsolt](https://github.com/szzsolt) for raising this.
* [#2060] Bugfix: Fix positional parameters bug with late-resolved arity variable. Thanks to [daisukeoto](https://github.com/daisukeoto) for raising this.
* [#2074][#2075] Bugfix: Don't generate auto-complete for hidden attributes in `picocli.shell.jline3.PicoCommand`. Thanks to [clebertsuconic](https://github.com/clebertsuconic) for the pull request.
* [#2059] Bugfix: ArgGroup with `exclusive=false` and `multiplicity=1` should require at least one option; fix regression and refine solution introduced in [#1848][#2030].  Thanks to [Utkarsh Mittal](https://github.com/utmittal) for raising this.
* [#2080] DOC: Improve GraalVM documentation: add `graalvm-native-image-plugin`. Thanks to [Bhavik Patel](https://github.com/bhavikp19) for the pull request.
* [#2045] DOC: Commit html files with LF line-endings. Thanks to [Fridrich Strba](https://github.com/fridrich) for the pull request.

## <a name="4.7.5-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.5-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.7.4"></a> Picocli 4.7.4
The picocli community is pleased to announce picocli 4.7.4.

This release includes bugfixes and enhancements.

Many thanks to the picocli community for raising these issues and providing the pull requests to address them!

This is the eighty-third public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.4-toc"></a> Table of Contents
* [New and noteworthy](#4.7.4-new)
* [Fixed issues](#4.7.4-fixes)
* [Deprecations](#4.7.4-deprecated)
* [Potential breaking changes](#4.7.4-breaking-changes)

## <a name="4.7.4-new"></a> New and Noteworthy

See fixed items below.

I cheated on the semantic versioning in this release: a public setter method was added in a class in the `picocli.shell.jline3` module. In spite of that, the version number went from 4.7.3 to 4.7.4 (not 4.8.0). My apologies.


## <a name="4.7.4-fixes"></a> Fixed issues
* [#2028][#2031] API: Add setter for name in `picocli.shell.jline3.PicocliCommands`. Thanks to [Irina Leontyuk](https://github.com/irinaleo) for raising this.
* [#2026][#2027] Enhancement: Improved feedback on mistyped subcommands. Thanks to [David Pond](https://github.com/mauvo) for the pull request.
* [#2029][#2034] Enhancement: prevent `java.nio.charset.UnsupportedCharsetException: cp0` on windows, and fall back to the default charset if the charset provided by System property `sun.stdout.encoding` is invalid. Thanks to [
  Bartosz Spyrko-Smietanko](https://github.com/spyrkob) for the pull request.
* [#2035][#2036] Bugfix: Option "mapFallbackValue" ignored when inherited to subcommand. Thanks to [Dan Ziemba](https://github.com/zman0900) for the pull request.
* [#1848][#2030] Bugfix: fix issue with required options in `ArgGroup` becoming optional when combined with `DefaultValueProvider`. Thanks to [Ruud Senden](https://github.com/rsenden) and [Mike Snowden](https://github.com/wtfacoconut) for the pull request.
* [#2020] DEP: Bump step-security/harden-runner from 2.3.0 to 2.4.0
* [#2033] DEP: Bump github/codeql-action from 2.2.12 to 2.3.5
* [#2015] DEP: Bump junit5Version from 5.9.2 to 5.9.3
* [#2014] DEP: Bump org.jetbrains.kotlin:kotlin-gradle-plugin from 1.8.20 to 1.8.21
* [#2013] DEP: Bump org.jetbrains.kotlin:kotlin-script-runtime from 1.8.20 to 1.8.21
* [#2008] DEP: Bump springBootVersion from 2.7.10 to 2.7.11
* [#2007] DEP: Bump org.asciidoctor:asciidoctorj-pdf from 2.3.6 to 2.3.7

## <a name="4.7.4-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.4-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.7.3"></a> Picocli 4.7.3
The picocli community is pleased to announce picocli 4.7.3.

This release includes bugfixes and enhancements.

This is the eighty-second public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.3-toc"></a> Table of Contents
* [New and noteworthy](#4.7.3-new)
* [Fixed issues](#4.7.3-fixes)
* [Deprecations](#4.7.3-deprecated)
* [Potential breaking changes](#4.7.3-breaking-changes)

## <a name="4.7.3-new"></a> New and Noteworthy

Fixed a regression introduced in picocli 4.7.2.
Multi-value options whose `fallbackValue` is set to `CommandLine.Option.NULL_VALUE`, may see a `NullPointerException` if the option is specified on the command line without a parameter but with a `=` attached, like `--option=`.

Also, this release fixes broken links in the Javadoc.


## <a name="4.7.3-fixes"></a> Fixed issues
* [#1998] Bugfix: ParameterException: NullPointerException: null while processing argument at or before arg[0]. Thanks to [Jiri Daněk](https://github.com/jiridanek) for raising this.
* [#1957] DOC: Fixed broken links in the javadoc. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#2002] DEP: Bump actions/checkout from 3.5.0 to 3.5.2
* [#2005] DEP: Bump gradle/gradle-build-action from 2.4.0 to 2.4.2
* [#2003] DEP: Bump github/codeql-action from 2.2.11 to 2.2.12
* [#1997] DEP: Bump org.asciidoctor:asciidoctorj-pdf from 2.3.4 to 2.3.6

## <a name="4.7.3-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.3-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.7.2"></a> Picocli 4.7.2
The picocli community is pleased to announce picocli 4.7.2.

This release includes bugfixes and enhancements.

This is the eighty-first public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.2-toc"></a> Table of Contents
* [New and noteworthy](#4.7.2-new)
* [Fixed issues](#4.7.2-fixes)
* [Deprecations](#4.7.2-deprecated)
* [Potential breaking changes](#4.7.2-breaking-changes)

## <a name="4.7.2-new"></a> New and Noteworthy

* Bugfix: `fallbackValue=Option.NULL_VALUE` did not work for `Collection` or array options.
* Fixed `isJansiConsoleInstalled` performance issue.
* Kotlin enhancement: improved `paramLabel` string auto-generated from Kotlin `internal` methods which have mangled names with embedded "$".
* Various documentation fixes.

## <a name="4.7.2-fixes"></a> Fixed issues
* [#1959] API: Add ability to enable loading resource bundles in annotation processor for tests.
* [#1993] Bugfix: `fallbackValue=Option.NULL_VALUE` did not work for `Collection` or array options. Thanks to [Jiri Daněk](https://github.com/jiridanek) for raising this.
* [#1975][#1976] Enhancement: Fixed `isJansiConsoleInstalled` performance issue. Thanks to [ChrisTrenkamp](https://github.com/ChrisTrenkamp) for the pull request.
* [#1932] Enhancement: Move System-Rules tests to Java 5 test module; move System-Lambda tests to Java 8+ test module. Facilitate testing with recent JRE's.
* [#1984] Enhancement (Kotlin): improve `paramLabel` string auto-generated from Kotlin `internal` methods which have mangled names with embedded "$". Thanks to [Ken Yee](https://github.com/kenyee) for raising this.
* [#1945] DOC: Code sample: add Java version. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1956] Doc: Fix broken link in user manual. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1955] DEP: Bump asciidoctorj from 2.5.5 to 2.5.7. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1980] DEP: Bump actions/checkout from 3.3.0 to 3.5.0
* [#1952] DEP: Bump actions/setup-java from 3.9.0 to 3.10.0
* [#1985] DEP: Bump actions/setup-java from 3.10.0 to 3.11.0
* [#1941] DEP: Bump emibcn/badge-action from 1.2.4 to 2.0.2
* [#1942] DEP: Bump github/codeql-action from 2.1.39 to 2.2.1
* [#1953] DEP: Bump github/codeql-action from 2.2.1 to 2.2.3
* [#1958] DEP: Bump github/codeql-action from 2.2.3 to 2.2.4
* [#1979] DEP: Bump github/codeql-action from 2.2.4 to 2.2.8
* [#1995] DEP: Bump github/codeql-action from 2.2.8 to 2.2.11
* [#1961] DEP: Bump gradle/gradle-build-action from 2.3.3 to 2.4.0
* [#1960] DEP: Bump gradle/wrapper-validation-action from 1.0.5 to 1.0.6
* [#1962] DEP: Bump log4j2Version from 2.19.0 to 2.20.0
* [#1947] DEP: Bump org.jetbrains.kotlin:kotlin-gradle-plugin from 1.7.20 to 1.8.10
* [#1989] DEP: Bump org.jetbrains.kotlin:kotlin-gradle-plugin from 1.8.10 to 1.8.20
* [#1948] DEP: Bump org.jetbrains.kotlin:kotlin-script-runtime from 1.7.20 to 1.8.10
* [#1988] DEP: Bump org.jetbrains.kotlin:kotlin-script-runtime from 1.8.10 to 1.8.20
* [#1968] DEP: Bump org.jline:jline from 3.22.0 to 3.23.0
* [#1990] DEP: Bump ossf/scorecard-action from 2.1.2 to 2.1.3
* [#1964] DEP: Bump springBootVersion from 2.7.8 to 2.7.9
* [#1978] DEP: Bump springBootVersion from 2.7.9 to 2.7.10
* [#1963] DEP: Bump step-security/harden-runner from 2.1.0 to 2.2.0
* [#1970] DEP: Bump step-security/harden-runner from 2.2.0 to 2.2.1
* [#1992] DEP: Bump step-security/harden-runner from 2.2.1 to 2.3.0


## <a name="4.7.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.





# <a name="4.7.1"></a> Picocli 4.7.1
The picocli community is pleased to announce picocli 4.7.1.

This release includes bugfixes and enhancements.



This is the eightieth public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.1-toc"></a> Table of Contents
* [New and noteworthy](#4.7.1-new)
* [Fixed issues](#4.7.1-fixes)
* [Deprecations](#4.7.1-deprecated)
* [Potential breaking changes](#4.7.1-breaking-changes)

## <a name="4.7.1-new"></a> New and Noteworthy

## <a name="4.7.1-fixes"></a> Fixed issues
* [#1874][#1885][#1933] Bugfix: The `picocli-groovy` module should not declare `org.codehaus.groovy:groovy-all` as dependency. Thanks to [Mattias Andersson](https://github.com/attiand) and [Michael Kutz](https://github.com/mkutz) for raising this, and to [Paul King](https://github.com/paulk-asert) for the analysis.
* [#1886][#1896] Bugfix: AsciiDoc generator now correctly outputs options even if all options are in ArgGroups. Thanks to [Ruud Senden](https://github.com/rsenden) for the discussion and the pull request.
* [#1878][#1876] Bugfix: Annotation processor now avoids loading resource bundles at compile time. Thanks to [Ruud Senden](https://github.com/rsenden) for the discussion and the pull request.
* [#1911] Avoid using boxed boolean in `CommandLine.Interpreter.applyValueToSingleValuedField`. Thanks to [Jiehong](https://github.com/Jiehong) for the pull request.
* [#1870] Bugfix: `StringIndexOutOfBoundsException` in usage help when command has too many (and long) aliases. Thanks to [Martin](https://github.com/martlin2cz) for raising this.
* [#1904] Bugfix: Apply `fallbackValue` to vararg multi-value options, not just single-value options. Thanks to [Andreas Sewe](https://github.com/sewe) for raising this.
* [#1930] Bugfix: Ensure tests pass in environments for Java 5-18.
* [#1940] Bugfix: fix 3 failing tests in `ManPageGeneratorTest`. Thanks to [Mike Snowden](https://github.com/wtfacoconut) for the pull request.
* [#1881] DOC: Many documentation improvements. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1855][#1857] DOC: Add new user manual section called [Rare Use Cases](https://picocli.info/#_rare_use_cases) detailing `System.exit` usage. Thanks to [Tadaya Tsuyukubo](https://github.com/ttddyy) for the pull request.
* [#1880] DOC: Improve documentation for negatable options that are true by default. Thanks to [Sebastian Hoß](https://github.com/sebhoss) for raising this.
* [#1815] DOC: Improve user manual section for non-validating ArgGroups. Thanks for [Paul Harris](https://github.com/rolfyone) for raising this.
* [#1908] DOC: Update the user manual GraalVM section to use the new official native-maven-plugin. Thanks to [tison](https://github.com/tisonkun) for the pull request.
* [#1924] DOC: Update `picocli-codegen/README.adoc`. Thanks to [Seyyed Emad Razavi](https://github.com/razavioo) for the pull request.
* [#1910][#1917] DOC: Fix broken link to Zero Bug Commitment. Thanks to [Jiehong](https://github.com/Jiehong) for raising this and thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1915] DOC: Improve default provider examples. Thanks to [David](https://github.com/DavidTheExplorer) for raising this.
* [#1918][#1920] DOC: Removed unused Travis CI badge and associated broken link from README. Thanks to [Andreas Deininger](https://github.com/deining) for raising this and the pull request.
* [#706] DOC: Add GitHub badge with test count to README.
* [#1939] BUILD: Fix `picocli-annotation-processing-tests` failures on Java 16+: rewrite tests to avoid Google `compiler-test` API that internally uses `com.sun.tools.javac.util.Context`.
* [#1887] DEP: Bump biz.aQute.bnd.gradle from 6.3.1 to 6.4.0
* [#1865] DEP: Bump ivy from 2.5.0 to 2.5.1
* [#1931] DEP: Bump springBootVersion from 2.7.5 to 3.0.2
* [#1929] DEP: Bump github/codeql-action from 2.1.29 to 2.1.39
* [#1926] DEP: Bump step-security/harden-runner from 1.5.0 to 2.1.0
* [#1914] DEP: Bump actions/checkout from 3.1.0 to 3.3.0
* [#1897] DEP: Bump actions/setup-java from 3.6.0 to 3.9.0
* [#1902] DEP: Bump ossf/scorecard-action from 2.0.6 to 2.1.2
* [#1938] DEP: Bump actions/upload-artifact from 3.1.1 to 3.1.2
* [#1937] DEP: Bump asciidoctorj-pdf from 2.3.3 to 2.3.4
* [#1936] DEP: Bump jline from 3.21.0 to 3.22.0
* [#1935] DEP: Bump compile-testing from 0.19 to 0.21.0

## <a name="4.7.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.




# <a name="4.7.0"></a> Picocli 4.7.0
The picocli community is pleased to announce picocli 4.7.0.

This release includes bugfixes and enhancements.

A potentially breaking change is that the parser now treats `char[]` as a single-value type.

From this release, applications can programmatically set the trace level, and use tracing in custom components.

Applications can improve startup time by setting system property `picocli.disable.closures` to `true` to disable support for [closures in annotations](https://picocli.info/#_closures_in_annotations).

Many more fixes and enhancements, see the sections below for more details.


This is the seventy-ninth public release.
Picocli follows [semantic versioning](https://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.7.0-toc"></a> Table of Contents
* [New and noteworthy](#4.7.0-new)
* [Fixed issues](#4.7.0-fixes)
* [Deprecations](#4.7.0-deprecated)
* [Potential breaking changes](#4.7.0-breaking-changes)

## <a name="4.7.0-new"></a> New and Noteworthy

### Tracing API
From picocli 4.7.0, applications can programmatically set the trace level, and use tracing in custom components.

In addition to setting system property `picocli.trace`, applications can now change the trace level via the `Tracer::setLevel` method. For example:

```java
CommandLine.tracer().setLevel(CommandLine.TraceLevel.INFO);
```

The new public method `CommandLine.tracer()` returns the singleton `Tracer` object that is used internally by picocli, and can also be used by custom component implementations to do tracing. For example:

```java
class MyIntConverter implements ITypeConverter<Integer> {
    public Integer convert(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            CommandLine.tracer().info(
                    "Could not convert %s to Integer, returning default value -1", value);
            return -1;
        }
    }
}
```

### Enable Consuming Option Names or Subcommands

By default, options that take a parameter do not consume values that match a subcommand name or an option name.

This release introduces two parser configuration options to change this behaviour:

* `CommandLine::setAllowOptionsAsOptionParameters` allows options to consume option names
* `CommandLine::setAllowSubcommandsAsOptionParameters` allows options to consume subcommand names

When set to `true`, all options in the command (options that take a parameter) can consume values that match option names or subcommand names.

This means that any option will consume the maximum number of arguments possible for its [arity](https://picocli.info/#_arity).

USE WITH CAUTION!

If an option is defined as `arity = "*"`, this option will consume _all_ remaining command line arguments following this option (until the [End-of-options delimiter](https://picocli.info/#_double_dash)) as parameters of this option.

### Unsorted Synopsis
By default, the synopsis displays options in alphabetical order.
Picocli 4.7.0 introduces a `sortSynopsis = false` attribute to let the synopsis display options in the order they are declared in your class, or sorted by their `order` attribute.

```java
@Command(sortSynopsis = false)
```

### Parser change for `char[]` options
Prior to 4.7, the picocli parser treated options and positional parameters with type `char[]` as array (multi-value) options, except for interactive options. However, it is more intuitive to treat all `char[]` options as single-value options, similar to `String` options.

For end users, this means that existing applications that use non-interactive `char[]` options will no longer allow multiple characters to be specified separately on the command line. That is, input like `-c A -c B -c C` will be rejected and the user needs to specify `-c ABC` instead.

Applications that want to preserve the previous behaviour will need to change their code to use `java.lang.Character[]` instead of `char[]`.

## <a name="4.7.0-fixes"></a> Fixed issues
* [#1599] API: The `picocli-codegen` artifact is now an explicitly declared named JPMS module with a `module-info.class`.
* [#1611] API: The `picocli-groovy` artifact is now an explicitly declared named JPMS module with a `module-info.class`.
* [#1610] API: The `picocli-shell-jline2` is now an explicitly declared named JPMS module with a `module-info.class`.
* [#1609] API: The `picocli-shell-jline3` is now an explicitly declared named JPMS module with a `module-info.class`.
* [#1608] API: The `picocli-spring-boot-starter` is now an explicitly declared named JPMS module with a `module-info.class`. NOTE: its module name changed to `info.picocli.spring.boot` from `info.picocli.spring`.
* [#1614] API: Change `picocli-spring-boot-starter` JPMS module name to `info.picocli.spring.boot` from `info.picocli.spring`.
* [#1600] API: Add `requires static java.sql` to picocli `module-info`.
* [#1471] API: Provide a programmatic way to configure Picocli's `TraceLevel`. Thanks to [ekinano](https://github.com/ekinano) for raising this.
* [#1125] API: Add parser configuration to allow options to consume values that match subcommand names or option names.
* [#1396][#1401] API: Support generic types in containers (e.g. List, Map). Thanks to [Michał Górniewski](https://github.com/mgorniew) for the pull request.
* [#1380][#1505] API, bugfix: `requiredOptionMarker` should not be displayed on `ArgGroup` options. Thanks to [Ahmed El Khalifa](https://github.com/ahmede41) for the pull request.
* [#1563] API: Add constructor to `PicocliSpringFactory` to allow custom fallback `IFactory`. Thanks to [Andrew Holland](https://github.com/a1dutch) for raising this.
* [#1767][#1802] API: avoid NPE on `OptionSpec.getValue()` and add `IScoped` internal API. Thanks to [Ruud Senden](https://github.com/rsenden) for the discussion and the pull request.
* [#1574] API: Add annotation API to control whether synopsis should be sorted alphabetically or by explicit `order`.
* [#1708][#1712][#1723] API: The `setUsageHelpLongOptionsMaxWidth` method no longer throws an exception when an invalid value is specified; instead, the value is ignored and an INFO-level trace message is logged. Thanks to [Fabio](https://github.com/fabio-franco) for the pull request.
* [#648][#1846] Enhancement: Treat `char[]` as single-value types (Potentially breaking change). Thanks to [Lukáš Petrovický](https://github.com/triceo) for the pull request with the test to verify the solution.
* [#1571] Enhancement: Variables in values from the default value provider should be interpolated. Thanks to [Bas Passon](https://github.com/bpasson) for raising this.
* [#1773] Enhancement: Applications can improve startup time by setting system property `picocli.disable.closures` to `true` to disable support for [closures in annotations](https://picocli.info/#_closures_in_annotations). Thanks to [patric-r](https://github.com/patric-r) for raising this.
* [#1408] Enhancement: Synopsis should respect `order` if specified. Thanks to [Simon](https://github.com/sbernard31) for raising this.
* [#964][#1080] Enhancement: ArgGroup synopsis should respect `order` (if specified). Thanks to [Enderaoe](https://github.com/Lyther) for the pull request with unit tests.
* [#1706][#1710] Enhancement: Subcommands should get missing messages from parent command resource bundle. Thanks to [Ruud Senden](https://github.com/rsenden) and [Mike Snowden](https://github.com/wtfacoconut) for the pull request.
* [#899][#1578][#1579] Enhancement: improve built-in `Help` command description. Thanks to [Michael L Heuer](https://github.com/heuermh) for the pull request. Thanks to [Garret Wilson](https://github.com/garretwilson) for raising this.
* [#1713][#1714] Enhancement: Support optional booleans in annotation processor. Thanks to [Jan Waś](https://github.com/nineinchnick) for the pull request.
* [#1387][#1711] Enhancement: Annotation processor should validate final primitive and String fields with `Option` and `Parameters` annotations. Thanks to [xehpuk](https://github.com/xehpuk) for raising this, and thanks to [Adam McElwee](https://github.com/acmcelwee) for the pull request.
* [#1572] Enhancement: Remove redundant braces in ArgGroup synopsis.
* [#1602] Enhancement: Fix incorrect debug output for add/removeAlias.
* [#1603] Enhancement: Improve debug tracing information for help requests and command execution.
* [#1629] Enhancement: Omit empty braces in standard prompt for interactive options without description. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1778] Enhancement: Add support for new Spring Boot auto configuration introduced in Spring Boot 2.7. Thanks to [Andreas Asplund](https://github.com/aspan) for the pull request.
* [#1836][#1841] Enhancement: Command aliases on Mixin were not being applied. Thanks to [Mike Snowden](https://github.com/wtfacoconut) for the pull request and to [Ruud Senden](https://github.com/rsenden) for raising this.
* [#1754][#1759] Enhancement: Autocompletion now correctly handles completion candidates with spaces. Thanks to [Juan Martín Sotuyo Dodero](https://github.com/jsotuyod) for the pull request.
* [#1834][#1838] Bugfix: Incorrect synopsis for char[] options. Thanks to [Ruud Senden](https://github.com/rsenden) and [Mike Snowden](https://github.com/wtfacoconut) for the pull request.
* [#1680] Bugfix: ArgGroups with `multiplicity="0"` are now disallowed at construction time and no longer throw a `StackOverflowError` while parsing. Thanks to [ARNOLD Somogyi](https://github.com/zappee) for raising this.
* [#1615][#1616] Bugfix: `getCJKAdjustedLength()` no longer miscalculates for supplementary code points. Thanks to [gwalbran](https://github.com/gwalbran) for the pull request.
* [#1575] Bugfix: Synopsis should not cluster boolean options if `posixClusteredShortOptionsAllowed` is set to false.
* [#1642] Bugfix: Negatable options should negate explicit values. Thanks to [Nat Burns](https://github.com/burnnat) for raising this.
* [#1696][#1697] Bugfix: ManPageGenerator asciidoc output now correctly shows options in nested ArgGroups. Thanks to [Ruud Senden](https://github.com/rsenden) for the pull request.
* [#1741] Bugfix: `@Command`-annotated method parameters are assigned incorrect indices when contained in a `@Command` class that is added as a subcommand to another `@Command` class which has `scope = CommandLine.ScopeType.INHERIT`. Thanks to [Onedy](https://github.com/Onedy) for raising this.
* [#1779] Bugfix: Custom factory should be used when creating `CommandSpec`. Thanks to [Philippe Charles](https://github.com/charphi) for raising this.
* [#1644][#1863] Bugfix: autocompletion of directory names stopped working from picocli 4.6.3. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request, and thanks to [philgdn](https://github.com/philgdn) for raising this and verifying the solution.
* [#1807] BUILD: Optimize incremental builds and local build cache usage. Thanks to [Jean André Gauthier](https://github.com/jean-andre-gauthier) for the pull request and [Nelson Osacky](https://github.com/runningcode) for the review.
* [#1298] DOC: Publish all-in-one javadoc for all picocli modules.
* [#812] DOC: Document how to test a picocli spring-boot application.
* [#1596] DOC: fix javadoc typos and incorrect links.
* [#1597] DOC: Add examples to Execution Configuration section in user manual.
* [#1140] DOC: Add subsection Forcing Interactive Input to user manual Interactive Options section. Thanks to [smalirizvi](https://github.com/smalirizvi) for raising this.
* [#967] DOC: User manual now shows how to configure `null` as `defaultValue` and `fallbackValue`.
* [#1625] DOC: Fix broken links after renaming default branch to `main` from `master`. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1628][#1630] DOC: Fix broken link in `picocli-codegen` `README`. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1627] DOC: User guide, chapter 3.2.3. Forcing Interactive Input: code sample: add Kotlin version. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1650] DOC: User guide, Spring Boot section: add warning about dynamic proxies. Thanks to [Ernst Plüss](https://github.com/pluess) for raising this.
* [#1677] DOC: Many improvements and corrections to the user manual. Thanks to [Björn Kautler](https://github.com/Vampire) for the pull request.
* [#1678] DOC: Change links from http to https, fix broken links. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1750] DOC: Clarify that GPL licensing NOTICE is for docs only.
* [#1788] DOC: add link to `picocli-examples` in the user manual. Thanks to [Markus Elfring](https://github.com/Markus-Elfring) for raising this.
* [#1796] DOC: Fixing broken links and typos. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1798] DOC: update examples for jakarta.validation-api. Thanks to [Roy](https://github.com/ashr123) for raising this.
* [#1803] DOC: show `@Command`-annotated method with `int` return value in user manual. Thanks to [SinaMobasheri](https://github.com/SinaMobasheri) for raising this.
* [#1581] BUILD: Fix dependabot config.
* [#1613] DEP: The `picocli-groovy` module now declares `groovy-all` as dependency.
* [#1604] DEP: Remove dependency on `slf4j` from `picocli-spring-boot-starter`.
* [#1783] DEP: Update actions/checkout requirement to 2541b1294d2704b0964813337f33b291d3f8596b
* [#1837] DEP: Bump actions/checkout from 3.0.2 to 3.1.0
* [#1607] DEP: Bump actions/setup-java from 2.5.0 to 3
* [#1646] DEP: Bump actions/setup-java from 3.0.0 to 3.1.0
* [#1655] DEP: Bump actions/setup-java from 3.1.0 to 3.1.1
* [#1667] DEP: Bump actions/setup-java from 3.1.1 to 3.2.0
* [#1674] DEP: Bump actions/setup-java from 3.2.0 to 3.3.0
* [#1717] DEP: Bump actions/setup-java from 3.3.0 to 3.4.0
* [#1736] DEP: Bump actions/setup-java from 3.4.0 to 3.4.1
* [#1806] DEP: Bump actions/setup-java from 3.4.1 to 3.5.0
* [#1826] DEP: Bump actions/setup-java from 3.5.0 to 3.5.1
* [#1624] DEP: Bump actions/upload-artifact from 2.3.1 to 3
* [#1687] DEP: Bump actions/upload-artifact from 3.0.0 to 3.1.0
* [#1859] DEP: Bump actions/upload-artifact from 3.1.0 to 3.1.1
* [#1585] DEP: Bump github/codeql-action from 1.0.30 to 1.1.0
* [#1593] DEP: Bump github/codeql-action from 1.1.0 to 1.1.2
* [#1601] DEP: Bump github/codeql-action from 1.1.2 to 1.1.3
* [#1631] DEP: Bump github/codeql-action from 1.1.3 to 1.1.4
* [#1636] DEP: Bump github/codeql-action from 1.1.4 to 1.1.5
* [#1643] DEP: Bump github/codeql-action from 1.1.5 to 2.1.6
* [#1652] DEP: Bump github/codeql-action from 2.1.6 to 2.1.7
* [#1654] DEP: Bump github/codeql-action from 2.1.7 to 2.1.8
* [#1669] DEP: Bump github/codeql-action from 2.1.8 to 2.1.9
* [#1676] DEP: Bump github/codeql-action from 2.1.9 to 2.1.10
* [#1682] DEP: Bump github/codeql-action from 2.1.10 to 2.1.11
* [#1700] DEP: Bump github/codeql-action from 2.1.11 to 2.1.12
* [#1720] DEP: Bump github/codeql-action from 2.1.12 to 2.1.14
* [#1728] DEP: Bump github/codeql-action from 2.1.14 to 2.1.15
* [#1739] DEP: Bump github/codeql-action from 2.1.15 to 2.1.16
* [#1781] DEP: Bump github/codeql-action from 2.1.18 to 2.1.1
* [#1786] DEP: Bump github/codeql-action from 2.1.18 to 2.1.20
* [#1792] DEP: Bump github/codeql-action from 2.1.20 to 2.1.21
* [#1797] DEP: Bump github/codeql-action from 2.1.21 to 2.1.22
* [#1817] DEP: Bump github/codeql-action from 2.1.22 to 2.1.23
* [#1820] DEP: Bump github/codeql-action from 2.1.22 to 2.1.24
* [#1823] DEP: Bump github/codeql-action from 2.1.24 to 2.1.25
* [#1831] DEP: Bump github/codeql-action from 2.1.25 to 2.1.26
* [#1842] DEP: Bump github/codeql-action from 2.1.26 to 2.1.27
* [#1862] DEP: Bump github/codeql-action from 2.1.28 to 2.1.29
* [#1782] DEP: Bump gradle/gradle-build-action from c6619898ec857b418d6436d3efe8a0becf74eb9e to 2.2.4
* [#1787] DEP: Bump gradle/gradle-build-action from c6619898ec857b418d6436d3efe8a0becf74eb9e to 2.2.5
* [#1825] DEP: Bump gradle/gradle-build-action from 2.3.0 to 2.3.1
* [#1832] DEP: Bump gradle/gradle-build-action from 2.3.1 to 2.3.2
* [#1860] DEP: Bump gradle/gradle-build-action from 2.3.2 to 2.3.3
* [#1861] DEP: Bump gradle/wrapper-validation-action from 1.0.4 to 1.0.5
* [#1586] DEP: Bump ossf/scorecard-action from 1.0.2 to 1.0.3
* [#1594] DEP: Bump ossf/scorecard-action from 1.0.3 to 1.0.4
* [#1691] DEP: Bump ossf/scorecard-action from 1.0.4 to 1.1.0
* [#1699] DEP: Bump ossf/scorecard-action from 1.1.0 to 1.1.1
* [#1805] DEP: Bump ossf/scorecard-action from 1.1.2 to 2.0.0
* [#1813] DEP: Bump ossf/scorecard-action from 2.0.0 to 2.0.2
* [#1816] DEP: Bump ossf/scorecard-action from 2.0.0 to 2.0.3
* [#1828] DEP: Bump ossf/scorecard-action from 2.0.3 to 2.0.4
* [#1583] DEP: Bump step-security/harden-runner from 1.3.0 to 1.4.0
* [#1639] DEP: Bump step-security/harden-runner from 1.4.0 to 1.4.1
* [#1666] DEP: Bump step-security/harden-runner from 1.4.1 to 1.4.2
* [#1730] DEP: Bump step-security/harden-runner from 1.4.3 to 1.4.4
* [#1833] DEP: Bump step-security/harden-runner from 1.4.5 to 1.5.0
* [#1580] DEP: Bump asciidoctor to 2.5.3 from 2.5.2. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1688] DEP: Bump asciidoctorj-pdf from 1.6.2 to 2.0.0
* [#1690] DEP: Bump asciidoctorj-pdf from 2.0.0 to 2.0.2
* [#1692] DEP: Bump asciidoctorj-pdf from 2.0.2 to 2.0.3
* [#1694] DEP: Bump asciidoctorj-pdf from 2.0.3 to 2.0.4
* [#1695] DEP: Bump asciidoctorj-pdf from 2.0.4 to 2.0.6
* [#1715] DEP: Bump asciidoctorj-pdf from 2.0.6 to 2.0.8
* [#1722] DEP: Bump asciidoctorj-pdf from 2.0.8 to 2.1.2
* [#1785] DEP: Bump asciidoctorj-pdf from 2.1.6 to 2.3.0
* [#1854] DEP: Bump asciidoctorj-pdf from 2.3.0 to 2.3.3
* [#1618] DEP: Bump biz.aQute.bnd.gradle from 6.1.0 to 6.2.0
* [#1698] DEP: Bump biz.aQute.bnd.gradle from 6.2.0 to 6.3.0
* [#1703] DEP: Bump biz.aQute.bnd.gradle from 6.3.0 to 6.3.1
* [#1582] DEP: Bump groovy-all from 2.4.10 to 2.5.15 // latest version of Groovy that supports Java 5
* [#1589] DEP: Bump hamcrest-core from 1.3 to 2.2
* [#1621] DEP: Bump hibernate-validator from 7.0.2.Final to 7.0.3.Final
* [#1633][#1635] DEP: Bump hibernate-validator from 7.0.3.Final to 7.0.4.Final
* [#1821] DEP: Bump hibernate-validator from 7.0.5.Final to 8.0.0.Final
* [#1812] DEP: Bump hibernate-validator from 7.0.5.Final to 8.0.0.Final
* [#1622] DEP: Bump hibernate-validator-annotation-processor from 7.0.2.Final to 7.0.3.Final
* [#1634] DEP: Bump hibernate-validator-annotation-processor from 7.0.3.Final to 7.0.4.Final
* [#1587] DEP: Bump ivy from 2.4.0 to 2.5.0
* [#1584] DEP: Bump jansi from 2.1.0 to 2.4.0
* [#1573] DEP: Bump JLine3 version to 3.21.0 from 3.19.0.
* [#1590] DEP: Bump junit-dep from 4.11 to 4.11.20120805.1225
* [#1591] DEP: Bump junit from 4.12 to 4.13.2
* [#1649] DEP: Bump kotlin-gradle-plugin from 1.6.10 to 1.6.20
* [#1829] DEP: Bump kotlin-gradle-plugin from 1.7.10 to 1.7.20
* [#1648] DEP: Bump kotlin-script-runtime from 1.6.10 to 1.6.20
* [#1830] DEP: Bump kotlin-script-runtime from 1.7.10 to 1.7.20
* [#1617] DEP: Bump log4j2Version from 2.17.1 to 2.17.2
* [#1729] DEP: Bump log4j2Version from 2.17.2 to 2.18.0
* [#1819] DEP: Bump log4j2Version from 2.18.0 to 2.19.0
* [#1822] DEP: Bump scala-library from 2.13.8 to 2.13.9
* [#1843] DEP: Bump scala-library from 2.13.9 to 2.13.10
* [#1576] DEP: Bump Spring Boot version from 2.5.6 to 2.6.3
* [#1606] DEP: Bump Spring Boot version from 2.6.3 to 2.6.4
* [#1641] DEP: Bump Spring Boot version from 2.6.4 to 2.6.5
* [#1645] DEP: Bump Spring Boot version from 2.6.5 to 2.6.6
* [#1684] DEP: Bump Spring Boot version from 2.6.7 to 2.6.8
* [#1686] DEP: Bump Spring Boot version from 2.6.8 to 2.7.0
* [#1719] DEP: Bump Spring Boot version from 2.7.0 to 2.7.1
* [#1747] DEP: Bump Spring Boot version from 2.7.1 to 2.7.2
* [#1780] DEP: Bump spring Boot Version from 2.7.2 to 2.7.3
* [#1824] DEP: Bump springBootVersion from 2.7.3 to 2.7.4
* [#1853] DEP: Bump springBootVersion from 2.7.4 to 2.7.5
* [#1588] DEP: Bump system-rules from 1.17.1 to 1.19.0


## <a name="4.7.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.7.0-breaking-changes"></a> Potential breaking changes

* The JPMS module name of `picocli-spring-boot-starter` has been changed to `info.picocli.spring.boot` from `info.picocli.spring`.
* The `picocli-groovy` module now declares `groovy-all` as dependency.
* The parser now treats `char[]` as a single-value type.
* Redundant braces are now omitted in ArgGroup synopsis in usage help messages.


# <a name="4.6.3"></a> Picocli 4.6.3
The picocli community is pleased to announce picocli 4.6.3.

This release includes bugfixes and enhancements, as well as documentation and security improvements.

Several improvements to the annotation processor and the generated autocompletion scripts.

Much gratitude to the picocli community for the many pull requests and other contributions!

This is the seventy-eighth public release.
Picocli follows [semantic versioning](http://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.6.3-toc"></a> Table of Contents
* [New and noteworthy](#4.6.3-new)
* [Fixed issues](#4.6.3-fixes)
* [Deprecations](#4.6.3-deprecated)
* [Potential breaking changes](#4.6.3-breaking-changes)

## <a name="4.6.3-new"></a> New and Noteworthy

### Default branch renamed to `main`
The default branch has been renamed! `master` is now named `main`.

If you have a local clone, you can update it by running the following commands:

```bash
git branch -m master main
git fetch origin
git branch -u origin/main main
git remote set-head origin -a
```

### Autocompletion script improvements

* Autocompletion now shows subcommand aliases in the completion candidates
* Autocompletion now displays completion candidates on exact match
* Autocompletion now supports file names containing spaces
* Remove file name extension and local dir prefix from the command name in generated autocomplete scripts
* Fix Bash error `compopt: command not found` on older versions of Bash
* Autocompletion on ZSH should only call `compinit` once

## <a name="4.6.3-fixes"></a> Fixed issues
* [#1440] Bugfix: annotation processor incorrectly failed with `DuplicateOptionAnnotationsException` when multiple commands had a subcommand in common and an inherited (`scope = ScopeType.INHERIT`) option. Thanks to [nemetsSY](https://github.com/nemetsSY) for raising this.
* [#1472] Bugfix: annotation processor option `-Averbose=true` no longer incorrectly triggers `warning: The following options were not recognized by any processor: '[verbose]'`. Thanks to [Lorenz Leutgeb](https://github.com/lorenzleutgeb) for raising this.
* [#1384][#1493] Bugfix: parser now correctly handles ArgGroups with optional positional parameters. Thanks to [Matthew Lewis](https://github.com/mattjlewis) for raising this and to [Kurt Kaiser](https://github.com/kurtkaiser) for the pull request.
* [#1474] Bugfix: Avoid `UnsupportedCharsetException: cp65001` on Microsoft Windows console when code page is set to UTF-8. Thanks to [epuni](https://github.com/epuni) for raising this.
* [#1528][#1529] Bugfix: Allow aliases of a CommandSpec that is already a subcommand to be properly & consistently modified. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1466][#1467] Bugfix/Enhancement: Autocomplete now shows subcommand aliases in the completion candidates. Thanks to [Ruud Senden](https://github.com/rsenden) for the pull request.
* [#1468] Bugfix/Enhancement: Autocompletion now displays completion candidates on exact match. Thanks to [Ruud Senden](https://github.com/rsenden) for raising this.
* [#1537][#1541] Bugfix: AbbreviationMatcher now treats aliases of the same object as one match. Thanks to [Staffan Arvidsson McShane](https://github.com/StaffanArvidsson) for raising this and [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1531] Bugfix: Options defined as annotated methods should reset between `parseArgs` invocations when `CommandLine` instance is reused. Thanks to [kaushalkumar](https://github.com/kaushalkumar) for raising this.
* [#1458][#1473] Enhancement: Autocompletion now supports file names containing spaces. Thanks to [zpater345](https://github.com/zpater345) for raising this and thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1477] Enhancement: Remove file name extension and local dir prefix from the command name in generated autocomplete scripts. Thanks to [Andrea Peruffo](https://github.com/andreaTP) for the pull request.
* [#1464] Enhancement: Fix Bash error `compopt: command not found` on older versions Bash. Thanks to [Andres Almiray](https://github.com/aalmiray) for raising this.
* [#1476] Enhancement: improve error message in `AbstractCommandSpecProcessor#extractTypedMember`. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for raising this.
* [#1475] Enhancement: Fix typo in annotation target-type error message. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1366][#1370] Enhancement: show in usage help that the built-in `help` command only works on the first argument. Thanks to [Patrice Duroux](https://github.com/peutch) for the pull request.
* [#1492] Enhancement: Use EditorConfig to define file formats and coding style; Thanks to [Goooler](https://github.com/Goooler) for the pull request.
* [#1530] Enhancement: Simplified `CommandSpec#validateSubcommandName` implementation. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1484] Enhancement: Fixed `org.junit.Assert.assertThat` deprecation warning; Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1485] Enhancement: Fix build warnings; build doc enhancements; Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1483] Enhancement: Improved `AbstractCommandSpecProcessor#isSubcommand`; Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1499] Enhancement: Improved `DefaultFactory#create(Class<T>)`. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1518] Enhancement: Simplified detection of getter & setter in `TypedMember` constructor. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1568] Enhancement: CommandLine cleanup. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1526][#1548] Enhancement: Autocompletion on ZSH should only call `compinit` once. Thanks to [Ben Herweyer](https://github.com/bherw) for the pull request.
* [#1539] DOC: Various documentation improvements. Thanks to [Hamid Nazari](https://github.com/hamid-nazari) for the pull request.
* [#1481] DOC: Removed repeated "whether" typo in JavaDoc; Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1125][#1538] DOC: Update "Option Names or Subcommands as Option Values" section in user manual; Thanks to [Scott Turner](https://github.com/turnef) for raising this.
* [#1409][#1463] DOC: add documentation section on using default values in argument groups. Thanks to [Ben Kedo](https://github.com/MadFoal) for the pull request.
* [#1383][#1502] DOC: add tests demonstrating usage of multiple arguments. Thanks to [Ben Kedo](https://github.com/MadFoal) and [lind6](https://github.com/lind6) for the pull request.
* [#1462] DOC, BUILD, DEP: Extend documentation on argument files, fix broken/outdated links, update dependencies. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1457] DOC: add caution about arguments in @files with quoted option parameters. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1544][#1545] DOC: Add NOTICE file with GPL v2 + CPE license. Thanks to [Keith M Swartz](https://github.com/kswartz26) for the pull request.
* [#1553] SECURITY: Fix code scanning alert - Token-Permissions
* [#1554] SECURITY: Fix code scanning alert - Pinned-Dependencies
* [#1555] SECURITY: Fix code scanning alert - Create SECURITY.md
* [#1556][#1557] SECURITY: Fix code scanning alert - SAST
* [#1558] SECURITY: Fix code scanning alert - Pinned-Dependencies in codeql-analysis.yml
* [#1559] SECURITY: Fix code scanning alert - Token-Permissions in codeql-analysis.yml
* [#1560] SECURITY: Fix code scanning alert - Binary-Artifacts - Validate Gradle Wrapper
* [#1561] SECURITY: Fix code scanning alert - Doc/example code uses a broken or risky cryptographic algorithm
* [#1562] BUILD: Rename 'master' branch to 'main'
* [#1491] BUILD: Add build job in CI; Thanks to [Goooler](https://github.com/Goooler) for the pull request.
* [#1482] BUILD: Optimize gradle; Thanks to [Goooler](https://github.com/Goooler) for the pull request.
* [#1461] BUILD: Allow publishing without signing for non-release versions. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1459] BUILD: The nexus-staging Gradle plugin must be applied to the root project, not to subprojects. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1503] BUILD: Fix failing `System.exit` tests on Java 18.
* [#1504] BUILD: Run tests on Java 6-7 in CI environment, semi-automate testing on Java 5.
* [#1516] DEP: Remove redundant dependency on `jline-console` in `picocli-shell-jline3`. Thanks to [kaushalkumar](https://github.com/kaushalkumar) for raising this.
* [#1495] DEP: Bump validation-api from 2.0.0.Final to 2.0.1.Final
* [#1496] DEP: Bump biz.aQute.bnd.gradle from 5.1.2 to 6.1.0
* [#1494] DEP: Bump kotlin-gradle-plugin from 1.5.31 to 1.6.0
* [#1497] DEP: Bump badass-jar from 1.1.3 to 1.2.0
* [#1498] DEP: Bump hibernate-validator from 6.1.2.Final to 7.0.1.Final
* [#1490] DEP: Bump hibernate-validator-annotation-processor from 6.1.2.Final to 7.0.1.Final
* [#1489] DEP: Bump log4j-core from 2.13.0 to 2.14.1
* [#1488] DEP: Bump log4j-api from 2.13.0 to 2.14.1
* [#1487] DEP: Bump kotlin-script-runtime from 1.5.31 to 1.6.0
* [#1486] DEP: Bump gradle-nexus-staging-plugin from 0.21.0 to 0.30.0
* [#1500][#1517] DEP: Bump gradle from 7.3 to 7.3.3. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1569] DEP: Bump gradle from 7.3.3 to 7.4. Thanks to [Ross Goldberg](https://github.com/rgoldberg) for the pull request.
* [#1515] DEP: Bump kotlin-gradle-plugin from 1.6.0 to 1.6.10
* [#1512] DEP: Bump kotlin-script-runtime from 1.6.0 to 1.6.10
* [#1514] DEP: Bump log4j-api from 2.14.1 to 2.17.1
* [#1513] DEP: Bump log4j-core from 2.14.1 to 2.17.1
* [#1543] DEP: Bump asciidoctorj-pdf from 1.6.0 to 1.6.2
* [#1547] DEP: Bump scala-library from 2.13.7 to 2.13.8
* [#1550] DEP: Bump hibernate-validator from 7.0.1.Final to 7.0.2.Final
* [#1551] DEP: Bump hibernate-validator-annotation-processor from 7.0.1.Final to 7.0.2.Final


## <a name="4.6.3-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.6.3-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.6.2"></a> Picocli 4.6.2
The picocli community is pleased to announce picocli 4.6.2.

This release includes bugfixes and enhancements. Many improvements in the documentation.

Thanks to the many people in the picocli community for raising issues and contributing pull requests to fix issues!

From this release, picocli uses system properties `sun.stdout.encoding` and `sun.stderr.encoding` when creating the `PrintWriters` returned by  `CommandLine::getOut` and `CommandLine::getErr`.
When these system properties do not exist, picocli falls back to the default charset (determined by `file.encoding`).
This addresses an issue on Windows, where the default charset is not the same as the encoding for its console (often the older `cp437` codepage on English-language versions of Windows).
Note that these system properties [seem to have been](https://wrapper.tanukisoftware.com/doc/english/prop-jvm-encoding.html) introduced in Java 8 (although I cannot find it in the JDK 8 release notes) and may not exist on earlier JVMs or on JVMs other than the Oracle and OpenJDK implementations.

This is the seventy-seventh public release.
Picocli follows [semantic versioning](http://semver.org/).
Artifacts in this release are signed by Remko Popma (6601 E5C0 8DCC BB96).

## <a name="4.6.2-toc"></a> Table of Contents
* [New and noteworthy](#4.6.2-new)
* [Fixed issues](#4.6.2-fixes)
* [Deprecations](#4.6.2-deprecated)
* [Potential breaking changes](#4.6.2-breaking-changes)

## <a name="4.6.2-new"></a> New and Noteworthy


## <a name="4.6.2-fixes"></a> Fixed issues
* [#1422] API: Un-deprecate the `@Option` `help` attribute; it is useful for custom help options. Thanks to [kaushalkumar](https://github.com/kaushalkumar) for raising this.
* [#1337][#1338] Enhancement: prevent spurious `WARNING: tag not found in include file` messages when generating man pages. Thanks to [Philip Crotwell](https://github.com/crotwell) for the pull request.
* [#1340] Enhancement: add xref to generated man page document to link to subcommands from the parent command page. Thanks to [Philip Crotwell](https://github.com/crotwell) for the pull request.
* [#1351][#1362] Enhancement: avoid calling option `completionCandidates` when displaying usage help if `${COMPLETION-CANDIDATES}` is not specified in description. Thanks to [Wenhao ZHANG](https://github.com/wtd2), [sustc11810424](https://github.com/sustc11810424) and [Yining Tang](https://github.com/Lanninger08) for the pull request.
* [#1303] Bugfix: Prevent `IllegalArgumentException: argument type mismatch` error in method subcommands with inherited mixed-in standard help options. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1300] Bugfix: Avoid spurious warning "Could not set initial value for field boolean" when reusing `CommandLine` with ArgGroup. Thanks to [Yashodhan Ghadge](https://github.com/codexetreme) for raising this.
* [#1316] Bugfix: Avoid `DuplicateOptionAnnotationsException` thrown on `mixinStandardHelpOptions` for subcommands when parent has `scope = INHERIT` by `picocli-codegen` annotation processor. Thanks to [Philippe Charles](https://github.com/charphi) for raising this.
* [#1319] Bugfix: Avoid `DuplicateOptionAnnotationsException` when parent has inherited mixed-in help options and the built-in `HelpCommand` subcommand. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1331] Bugfix: Avoid `IllegalArgumentException` when parent has no standard help options and `scope = INHERIT`, while subcommand does have mixed-in standard help options. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1381][#1382] Bugfix: Default value of option in repeated subcommand was not applied correctly. Thanks to [sfeuerhahn](https://github.com/sfeuerhahn) for the pull request.
* [#1434][#1435] `CommandSpec.remove(arg)` should also remove the arg from the `args` collection in the CommandSpec. Thanks to [kaushalkumar](https://github.com/kaushalkumar) for the pull request.
* [#1404] Bugfix/Enhancement: Print paramLabel only when it could exist. Thanks to [João Guerra](https://github.com/joca-bt) for the pull request.
* [#1320][#1321] Bugfix/Enhancement: Use system properties `sun.stdout.encoding` and `sun.stderr.encoding` when creating the `PrintWriters` returned by  `CommandLine::getOut` and `CommandLine::getErr`. Thanks to [Philippe Charles](https://github.com/charphi) for the investigation and the pull request.
* [#1431] Bugfix/enhancement: `.gitattributes` should include HTML files to convert CRLF to LF. Thanks to [wenhoujx](https://github.com/wenhoujx) for pointing this out.
* [#1388][#1430] Bugfix: Fix subcommand aliases autocomplete regression. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1415] Bugfix/DOC: fix NullPointerException in README Example. Thanks to [shannonbay](https://github.com/shannonbay) for raising this.
* [#1421] Bugfix/DOC: `execute` method in `RunAll`, `RunFirst` and `RunLast` should not be deprecated. Thanks to [Gamal DeWeever](https://github.com/gadeweever) for raising this.
* [#1326][#1339] DOC: Added documentation and examples for controlling the locale. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1296] DOC: add Kotlin code samples to user manual; other user manual improvements. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1299] DOC: Link to `IParameterPreprocessor` from `IParameterConsumer` javadoc. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1304] DOC: Manual, chapter '17.9 Inherited Command Attributes': added Kotlin version of code sample. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1305] DOC: Document use of `IParameterConsumer` as n-ary type converter. Thanks to [Martin](https://github.com/martlin2cz) for raising this.
* [#1307] DOC: Added CAUTION admonitions, Kotlin code sample. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1308] DOC: Add example for Option `converter`, improve text for default values. Thanks to [Abhijit Sarkar](https://github.com/asarkar) for raising this.
* [#1314] DOC: Fix use of deprecated Maven properties in README. Thanks to [Philippe Charles](https://github.com/charphi) for the pull request.
* [#1323] DOC: Update Testing section of the user manual for Stefan Birkner's library [System-Lambda](https://github.com/stefanbirkner/system-lambda).
* [#1325] DOC: Add section on Short and Long Option Columns to user manual. Thanks to [Andrei Ciobanu](https://github.com/nomemory) for raising this.
* [#1336] DOC: Kotlin sample code, documentation improvements. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1342] DOC: Improve user manual chapter 28.3 'Testing the Exit Code'. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1344] DOC: Documentation, chapter 'Testing environment variables:': add Kotlin test sample. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1443] DOC: Fix incorrect method name `setLongOptionsMaxWidth` to `setUsageHelpLongOptionsMaxWidth` in user manual section 'Long Option Column Width'. Thanks to [kunlk](https://github.com/kunlk) for raising this.
* [#1360][#1359] DOC: add JReleaser as packaging option. Thanks to [Andres Almiray](https://github.com/aalmiray) for the pull request.
* [#1363][#1364] DOC: Add caution on Variable interpolation in Kotlin. Thanks to [MagnusMG](https://github.com/MagnusMG) for the pull request.
* [#1397][#1399] DOC: Update Jline2 README.md to add some recommended workaround about ANSI incompatible terminals. Thanks to [Simon](https://github.com/sbernard31) for the pull request.
* [#1398][#1400] DOC: Arity of boolean options is now documented correctly. Thanks to [João Guerra](https://github.com/joca-bt) for the pull request.
* [#1428][#1433] DOC: Add section about subcommands with the same name as option default value. Thanks to [cbcmg](https://github.com/cbcmg) for the pull request.
* [#1390][#1432] DOC: Update examples to not throw Exception from main method. Thanks to [wenhoujx](https://github.com/wenhoujx) for the pull request.
* [#1423] DOC: Fixed broken link in README.md to annotation processor documentation. Thanks to [Sevy007](https://github.com/Sevy007) for raising this.
* [#1449] DOC: Fix typo in `picocli-shell-jline2/README.md`. Thanks to [Ahmed Ashour](https://github.com/asashour) for the pull request.
* [#1426] Fix README adoption logos. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1313] DEP: Bump jline3Version in order to avoid stackoverflow error. Thanks to [Rupert Madden-Abbott](https://github.com/rupert-madden-abbott) for the pull request.
* [#1455] DEP: Bump spring-boot version to 2.5.6.
* [#1369][#1371] Upgrade jline3 to version 3.19.0 to avoid "ReadConsoleInputW failed: Incorrect function" error. Thanks to [auricgoldfinger](https://github.com/auricgoldfinger) for the pull request.
* [#1336] BUILD: Bump Spring Boot, Gradle and Kotlin to latest version. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1327] BUILD: fix incorrect version in `MANIFEST.MF`. Thanks to [Fiouz](https://github.com/Fiouz) for the pull request.
* [#1328] BUILD: Upgrade Gradle to 6.8.2 and enable official Gradle Wrapper Validation GitHub Action. Thanks to [Fiouz](https://github.com/Fiouz) for the pull request.
* [#1329] BUILD: OSGi manifest entry in picocli-4.6.0.jar should not require groovy. Thanks to [Fiouz](https://github.com/Fiouz) for raising this.
* [#1330] BUILD: use type-safe DSL instead of direct XML manipulation to generate `pom.xml`. Thanks to [Fiouz](https://github.com/Fiouz) for the pull request.
* [#1332] BUILD: Bumping asciidoctor + asciidoctor gradle plugin to latest versions. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1413] BUILD: Improve Travis CI build Performance. Thanks to [YunLemon](https://github.com/YunLemon) for the pull request.
* [#1322] BUILD: switch from Bintray jcenter to Maven Central.

## <a name="4.6.2-deprecated"></a> Deprecations
No features were deprecated in this release.

The `@Option` `help` attribute is no longer deprecated from this release; it is useful for custom help options.

## <a name="4.6.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.6.1"></a> Picocli 4.6.1
The picocli community is pleased to announce picocli 4.6.1.

This release fixes a problem with dependency scope in the following sub-modules:

* picocli-codegen
* picocli-groovy
* picocli-shell-jline2
* picocli-shell-jline3
* picocli-spring-boot-starter

The problem was that these modules contained dependencies that were declared to have `implementation` scope instead of `api` scope.
However, these were transitive dependencies, and necessary to compile any project that uses the above picocli modules.

In this release, transitive dependencies are declared with `api` scope in the above modules.

Special thanks to [Sualeh Fatehi](https://github.com/sualeh), [Dejan Stojadinović](https://github.com/dejan2609) and [Semyon Levin](https://github.com/remal) for the quick community feedback, pull request and reviews!


This is the seventy-sixth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.6.1-toc"></a> Table of Contents
* [New and noteworthy](#4.6.1-new)
* [Fixed issues](#4.6.1-fixes)
* [Deprecations](#4.6.1-deprecated)
* [Potential breaking changes](#4.6.1-breaking-changes)

## <a name="4.6.1-new"></a> New and Noteworthy


## <a name="4.6.1-fixes"></a> Fixed issues
* [#1291] Fix dependency scope for `picocli-shell-jline3`. Thanks to [Sualeh Fatehi](https://github.com/sualeh) for raising this.
* [#1292] PR to fix dependency scope for `picocli-shell-jline3`. Thanks to [Dejan Stojadinović](https://github.com/dejan2609) for the pull request, and [Semyon Levin](https://github.com/remal) for the review.
* [#1294] Fix dependency scope for `picocli-spring-boot-starter`. Thanks to [Semyon Levin](https://github.com/remal) for raising this.


## <a name="4.6.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.6.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.6.0"></a> Picocli 4.6.0
The picocli community is pleased to announce picocli 4.6.0.

This release contains new features, bug fixes and other enhancements.

## <a name="4.6.0-community-contributions"></a> Community Contributions

This release contains many, many community contributions, for which I am extremely grateful. Let's give them some credit!

* [Andreas Deininger](https://github.com/deining) has been contributing to the documentation and other areas for a long time, but recently went into overdrive :-) and contributed many, many new pull requests to improve the documentation. The user manual and Quick Guide now have a "foldable" table of contents, and examples in tabs, with many additional examples in Kotlin, Scala and Groovy. A lot of work went into this! Many thanks, Andreas!
* [Marko Mackic](https://github.com/MarkoMackic) contributed a pull request to add `IModelTransformer` API for user-defined model transformations after initialization and before parsing.
* [Sualeh Fatehi](https://github.com/sualeh) contributed a pull request to the `picocli-shell-jline3` module that adds a built-in `clear` command and improves the `help` command.
* [H.Sakata](https://github.com/sakata1222) contributed a pull request that adds support for `echo` and `prompt` for interactive options and positional parameters.
* [Daniel Gray](https://github.com/danielthegray) contributed a bug fix to prevent incorrectly defaulting inherited positional params after a subcommand.
* [nveeser-google](https://github.com/nveeser-google) contributed a fix for compiler warnings about `Annotation::getClass` and assignment in `if` condition.
* [Petr Hála](https://github.com/pehala) contributed a pull request to add a section on Mocking to user manual.
* [Max Rydahl Andersen](https://github.com/maxandersen) contributed a pull request to include jbang in the Build Tools section of the user manual.
* [David Phillips](https://github.com/electrum) contributed a section to the user manual on [Really Executable JARs](https://skife.org/java/unix/2011/06/20/really_executable_jars.html).
* [Laurent Almeras](https://github.com/lalmeras) contributed a pull request to fix the user manual: `@ParentObject` should be `@ParentCommand`.
* [Mattias Andersson](https://github.com/attiand) raised the idea of supporting subcommand methods in Groovy scripts.
* [Adrian A.](https://github.com/aadrian) raised the idea of using closures in the picocli annotations in Groovy programs instead of specifying a class.
* [Nick Cross](https://github.com/rnc) raised the idea of inheriting `@Command` attributes with `scope=INHERIT`.
* [Marko Mackic](https://github.com/MarkoMackic) raised the idea of adding a `CommandSpec::removeSubcommand` method.
* [Max Rydahl Andersen](https://github.com/maxandersen) raised the idea of supporting `Optional<T>` type for options and positional parameters.
* [Max Rydahl Andersen](https://github.com/maxandersen) and [David Walluck](https://github.com/dwalluck) raised the idea of supporting key-only Map options (to support `-Dkey` as well as `-Dkey=value`).
* [David Walluck](https://github.com/dwalluck) raised the idea of a "preprocessor" parser plugin.
* [Jannick Hemelhof](https://github.com/clone1612) raised the idea of supporting `@Spec`-annotated members in `ArgGroup` classes.
* [Vitaly Shukela](https://github.com/vi) raised a bug report: the error message for unmatched positional argument reports an incorrect index when value equals a previously matched argument.
* [drkilikil](https://github.com/drkilikil) raised a bug report: `MissingParameterException` should not be thrown when subcommand has required options and help option is specified on parent command.
* [Sebastian Thomschke](https://github.com/sebthom) raised a bug report: `ReflectionConfigGenerator` should not generate method section in subclass config for private superclass methods in `reflect-config.json`.
* [Lukas Heumos](https://github.com/Zethson) added the picocli-based [cli-java template](https://cookietemple.readthedocs.io/en/latest/available_templates/available_templates.html#cli-java) to CookieTemple.
* [Sualeh Fatehi](https://github.com/sualeh) raised the idea of adding add `CommandLine::getFactory` accessor method.
* [David Walluck](https://github.com/dwalluck) contributed a test improvement that allows the tests to run reliably in more environments.


## What is in this release
Improved Groovy support: this release introduces a new `@PicocliScript2` annotation that adds support for exit codes and `@Command`-annotated methods to define subcommands. Also, from this release, Groovy programs can use closures in the picocli annotations instead of specifying a class.

From this release, Map options accept key-only parameters, so end users can specify `-Dkey` as well as `-Dkey=value`.
There is a new `mapFallbackValue` attribute that enables this, which can be used to control the value that is put into the map when only a key was specified on the command line.

Also, this release adds support for `java.util.Optional<T>`: single-value types can be wrapped in an `Optional` container object when running on Java 8 or higher.
If the option or positional parameter was not specified on the command line, picocli assigns the value `Optional.empty()` instead of `null`.

This release also adds support for commands with `scope = ScopeType.INHERIT`. Commands with this scope have their attributes copied to all subcommands (and sub-subcommands).

New parser plugin: `IParameterPreprocessor` and new configuration plugin: `IModelTransformer`.

From this release, `@Spec`-annotated elements can be used in `ArgGroup` classes, which can be convenient for validation.

Interactive options and positional parameters can now set `echo = true` (for non-security sensitive data) so that user input is echoed to the console, and control the `prompt` text that is shown before asking the user for input.

Help API: this release adds public methods `Help.Layout::colorScheme`, `Help.Layout::textTable`, `Help.Layout::optionRenderer`, `Help.Layout::parameterRenderer`, and `Help::calcLongOptionColumnWidth`, making it easier to customize the table format used to lay out options and positional parameters in the usage help message.

CommandSpec API: added method `CommandSpec::removeSubcommand`.

This is the seventy-fifth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.6.0-toc"></a> Table of Contents
* [New and noteworthy](#4.6.0-new)
  * [New `@PicocliScript2` annotation](#4.6.0-PicocliScript2-annotation)
  * [Groovy Closures in Annotations](#4.6.0-closures-in-annotation)
  * [Key-only map parameters](#4.6.0-key-only-map-params)
  * [System Properties](#4.6.0-system-properties)
  * [`java.util.Optional<T>`](#4.6.0-java-util-optional)
  * [Inherited Command Attributes](#4.6.0-inherited-command-attributes)
  * [Preprocessor Parser Plugin](#4.6.0-preprocessor)
  * [Model Transformations](#4.6.0-model-transformations)
* [Fixed issues](#4.6.0-fixes)
* [Deprecations](#4.6.0-deprecated)
* [Potential breaking changes](#4.6.0-breaking-changes)

## <a name="4.6.0-new"></a> New and Noteworthy

### <a name="4.6.0-PicocliScript2-annotation"></a> New `@PicocliScript2` annotation
The older `@picocli.groovy.PicocliScript` annotation is deprecated from picocli 4.6.
New scripts should use the `@picocli.groovy.PicocliScript2` annotation (and associated `picocli.groovy.PicocliBaseScript2` base class) instead.
The table below lists the differences between the `PicocliBaseScript2` and `PicocliBaseScript` script base classes.

| `PicocliBaseScript2` | `PicocliBaseScript`
|---- | ----
| Subcommands can be defined as `@Command`-annotated methods in the script. | No support for `@Command`-annotated methods.
| Support for `help` subcommands (both the built-in one and custom ones). | No support for `help` subcommands.
| Exit code support: scripts can override `afterExecution(CommandLine, int, Exception)` to call `System.exit`.| No support for exit code.
| Invokes `CommandLine::execute`. Scripts can override `beforeParseArgs(CommandLine)` to install a custom  `IExecutionStrategy`.| Execution after parsing is defined in `PicocliBaseScript::run` and is not easy to customize. Any subcommand _and_ the main script are _both_ executed.
| Scripts can override `beforeParseArgs(CommandLine)` to install a custom `IParameterExceptionHandler`. | Invalid input handling can be customized by overriding `PicocliBaseScript::handleParameterException`.
| Scripts can override `beforeParseArgs(CommandLine)` to install a custom `IExecutionExceptionHandler`. | Runtime exception handling can be customized by overriding `PicocliBaseScript::handleExecutionException`.
| Implements `Callable<Object>`, script body is transformed to the `call` method. | Script body is transformed to the `runScriptBody` method.

### <a name="4.6.0-closures-in-annotation"></a> Groovy Closures in Annotations
From picocli 4.6, Groovy programs can use closures in the picocli annotations instead of specifying a class.
This can be especially useful in Groovy scripts, where one cannot define a static inner class.

Example:

```java
@Command(name = "ClosureDemo",
        versionProvider = {
            { -> ["line1" , "line2"] as String[] } as IVersionProvider // <1>
        },
        defaultValueProvider = {
            { argSpec -> "some default" } as IDefaultValueProvider // <2>
        })
class ClosureDemo {
    @Option(names = '-x', completionCandidates = {["A", "B", "C"]}) // <3>
    String x

    @Option(names = '-y',
            parameterConsumer = {
                { args, argSpec, commandSpec ->  // <4>
                    argSpec.setValue(args.toString() + commandSpec.name())
                    args.clear()
                } as IParameterConsumer
            })
    String y

    @Option(names = '-z', converter = [ // requires Groovy 3.0.7
            { { str -> MessageDigest.getInstance(str) } as ITypeConverter } // <5>
    ])
    MessageDigest z
}
```

When a class is specified, picocli creates an instance of the class. By contrast, when a closure is specified, picocli _calls the closure_ to get an instance.
(To be precise, both of these are delegated to the configured [factory](https://picocli.info/#_custom_factory), and the default factory implementation supports closures from picocli 4.6.)

As you can see in the above example, each closure in the annotation should contain another closure that has the required type (`IVersionProvider`, `IDefaultValueProvider`, etc.)

* <1> Command `versionProvider`: note the empty parameter list before the `->` arrow. This is needed to help the Groovy compiler. The closure must be cast to `IVersionProvider`.
* <2> Command `defaultProvider`: return a default value for the specified `ArgSpec` parameter. The closure must be cast to `IDefaultValueProvider`.
* <3> Option or Parameters `completionCandidates`: return a list of Strings. No parameter list or casting is required.
* <4> Option or Parameters `parameterConsumer`: given a `Stack`, `ArgSpec` and `CommandSpec`, process the remaining arguments. The closure must be cast to `IParameterConsumer`.
* <5> Option or Parameters type `converter` takes an array of closures. Groovy 3.0.7 or greater is required: older versions of Groovy ignore closures in class array annotations. Each closure must have a parameter and be cast to `ITypeConverter`.


### <a name="4.6.0-key-only-map-params"></a> Key-only map parameters
By default, picocli expects Map options and positional parameters to look like `key=value`, that is, the option parameter or positional parameter is expected to have a key part and a value part, separated by a `=` character. If this is not the case, picocli shows a user-facing error message: `Value for ... should be in KEY=VALUE format but was ...`.

From picocli 4.6, applications can specify a `mapFallbackValue` to allow end users to specify only the key part. The specified `mapFallbackValue` is put into the map when end users to specify only a key. The value type can be wrapped in a `java.util.Optional`. For example:

```java
@Option(names = {"-P", "--properties"}, mapFallbackValue = Option.NULL_VALUE)
Map<String, Optional<Integer>> properties;

@Parameters(mapFallbackValue = "INFO", description = "... ${MAP-FALLBACK-VALUE} ...")
Map<Class<?>, LogLevel> logLevels;
```

This allows input like the following:

```
<cmd> --properties=key1 -Pkey2 -Pkey3=3 org.myorg.MyClass org.myorg.OtherClass=DEBUG
```

The above input would give the following results:

```
properties = [key1: Optional.empty, key2: Optional.empty, key3: Optional[3]]
logLevels  = [org.myorg.MyClass: INFO, org.myorg.OtherClass: DEBUG]
```

Note that the option description may contain the [`${MAP-FALLBACK-VALUE}` variable](https://picocli.info/#_predefined_variables) which will be replaced with the actual map fallback value when the usage help is shown.

### <a name="4.6.0-system-properties"></a> System Properties
A common requirement for command line applications is to support the `-Dkey=value` syntax to allow end users to set system properties.

The example below uses the `Map` type to define an `@Option`-annotated method that delegates all key-value pairs to `System::setProperty`.
Note the use of `mapFallbackValue = ""` to allow key-only option parameters.

```java
class SystemPropertiesDemo {
    @Option(names = "-D", mapFallbackValue = "") // allow -Dkey
    void setProperty(Map<String, String> props) {
        props.forEach((k, v) -> System.setProperty(k, v));
    }
}
```

### <a name="4.6.0-java-util-optional"></a> `java.util.Optional<T>`
From version 4.6, picocli supports single-value types wrapped in a `java.util.Optional` container when running on Java 8 or higher.
If the option or positional parameter was not specified on the command line, picocli assigns the value `Optional.empty()` instead of `null`.
For example:

```java
@Option(names = "-x")
Optional<Integer> x;

@Option(names = "-D", mapFallbackValue = Option.NULL_VALUE)
Map<String, Optional<Integer>> map;
```

WARNING: Picocli has only limited support for `java.util.Optional` types:
only single-value types, and the values in a `Map` (but not the keys!) can be wrapped in an `Optional` container.
`java.util.Optional` cannot be combined with arrays or other `Collection` classes.

### <a name="4.6.0-inherited-command-attributes"></a> Inherited Command Attributes
Picocli 4.6 adds support for inheriting `@Command` attributes with the `scope = ScopeType.INHERIT` annotation.
Commands with this scope have their `@Command` attributes copied to all subcommands (and sub-subcommands, to any level of depth).

When a subcommand specifies an explicit value in its `@Command` annotation, this value is used instead of the inherited value.
For example:

```java
@Command(name = "app", scope = ScopeType.INHERIT,
        mixinStandardHelpOptions = true, version = "app version 1.0",
        header = "App header",
        description = "App description",
        footerHeading = "Copyright%n", footer = "(c) Copyright by the authors",
        showAtFileInUsageHelp = true)
class App implements Runnable {
    @Option(names = "-x") int x;

    public void run() { System.out.printf("Hello from app %d%n!", x); }

    @Command(header = "Subcommand header", description = "Subcommand description")
    void sub(@Option(names = "-y") int y) {
        System.out.printf("Hello app sub %d%n!", y);
    }
}
```

The `app` command in the above example has `scope = ScopeType.INHERIT`, so its `@Command` properties are inherited by the `sub` subcommand.

The `sub` subcommand defines its own `header` and `description`, so these are not inherited from the parent command.
The help message for the subcommand looks like this:

```
Subcommand header
Usage: app sub [-hV] [-y=<arg0>] [@<filename>...]
Subcommand description
      [@<filename>...]   One or more argument files containing options.
  -h, --help             Show this help message and exit.
  -V, --version          Print version information and exit.
  -y=<arg0>
Copyright
(c) Copyright by the authors
```

Note that the subcommand has inherited the mixed-in standard help options (`--help` and `--version`), the `@file` usage help, and the footer and footer heading.
It also inherited the version string, shown when the user invokes `app sub --version`.

When a command has `scope = INHERIT`, the following attributes are copied to its subcommands:

* all usage help attributes: description, descriptionHeading, header, headerHeading, footer, footerHeading, customSynopsis, synopsisHeading, synopsisSubcommandLabel, abbreviateSynopsis, optionListHeading, parameterListHeading, commandListHeading, exitCodeList, exitCodeListHeading, requiredOptionMarker, showDefaultValues, sortOptions, autoWidth, width, showAtFileInUsageHelp, showEndOfOptionsDelimiterInUsageHelp, and hidden
* exit codes: exitCodeOnSuccess, exitCodeOnUsageHelp, exitCodeOnVersionHelp, exitCodeOnInvalidInput, exitCodeOnExecutionException
* the help and version options mixed in by `mixinStandardHelpOptions`
* separator between option and option parameter
* version
* versionProvider
* defaultValueProvider
* subcommandsRepeatable
* whether this command is a `helpCommand`

Attributes that are _not_ copied include:

* command name
* command aliases
* options and parameters (other than the help and version options mixed in by `mixinStandardHelpOptions`)
* other mixins than `mixinStandardHelpOptions`
* subcommands
* argument groups


### <a name="4.6.0-preprocessor"></a> Preprocessor Parser Plugin

Introduced in picocli 4.6, the `IParameterPreprocessor` is also a parser plugin, similar to `IParameterConsumer`, but more flexible.

Options, positional parameters and commands can be assigned a `IParameterPreprocessor` that implements custom logic to preprocess the parameters for this option, position or command.
When an option, positional parameter or command with a custom `IParameterPreprocessor` is matched on the command line, picocli's internal parser is temporarily suspended, and this custom logic is invoked.

This custom logic may completely replace picocli's internal parsing for this option, positional parameter or command, or augment it by doing some preprocessing before picocli's internal parsing is resumed for this option, positional parameter or command.

The "preprocessing" actions can include modifying the stack of command line parameters, or modifying the model.


#### Example use case
This may be useful when disambiguating input for commands that have both a positional parameter and an option with an optional parameter.
For example, suppose we have a command with the following synopsis:

```
edit [--open[=<editor>]] <file>
```

One of the limitations of options with an optional parameter is that they are difficult to combine with positional parameters.

With a custom parser plugin, we can customize the parser, such that `VALUE` in `--option=VALUE` is interpreted as the option parameter, and in `--option VALUE` (without the `=` separator), VALUE is interpreted as the positional parameter.
The code below demonstrates:


```java
@Command(name = "edit")
class Edit {

    @Parameters(index = "0", description = "The file to edit.")
    File file;

    enum Editor { defaultEditor, eclipse, idea, netbeans }

    @Option(names = "--open", arity = "0..1", preprocessor = Edit.MyPreprocessor.class,
        description = {
            "Optionally specify the editor to use; if omitted the default editor is used. ",
            "Example: edit --open=idea FILE opens IntelliJ IDEA (notice the '=' separator)",
            "         edit --open FILE opens the specified file in the default editor"
        })
    Editor editor = Editor.defaultEditor;

    static class MyPreprocessor implements IParameterPreprocessor {
        public boolean preprocess(Stack<String> args,
                                  CommandSpec commandSpec,
                                  ArgSpec argSpec,
                                  Map<String, Object> info) {
            // we need to decide whether the next arg is the file to edit
            // or the name of the editor to use...
            if (" ".equals(info.get("separator"))) { // parameter was not attached to option

                // act as if the user specified --open=defaultEditor
                args.push(Editor.defaultEditor.name());
            }
            return false; // picocli's internal parsing is resumed for this option
        }
    }
}
```

With this preprocessor, the following user input gives the following command state:

```
# User input # Command State
# --------------------------
--open A B   # editor: defaultEditor, file: A,    unmatched: [B]
--open A     # editor: defaultEditor, file: A,    unmatched: []
--open=A B   # editor: A,             file: B,    unmatched: []
--open=A     # editor: A,             file: null, unmatched: []
```

### <a name="4.6.0-model-transformations"></a> Model Transformations
From picocli 4.6, it is possible to use the annotations API to modify the model (commands, options, subcommands, etc.) dynamically at runtime.
The `@Command` annotation now has a `modelTransformer` attribute where applications can specify a class that implements the `IModelTransformer` interface:

This allows applications to dynamically add or remove options, positional parameters or subcommands, or modify the command in any other way, based on some runtime condition.

```java
@Command(modelTransformer = Dynamic.SubCmdFilter.class)
class Dynamic {

    private static class SubCmdFilter implements IModelTransformer {
        public CommandSpec transform(CommandSpec commandSpec) {
            if (Boolean.getBoolean("disable_sub")) {
                commandSpec.removeSubcommand("sub");
            }
            return commandSpec;
        }
    }

    @Command
    private void sub() {
        // subcommand business logic
    }
}
```

## <a name="4.6.0-fixes"></a> Fixed issues
* [#1164] API: Add support for `@Command(scope=INHERIT)`. Thanks to [Nick Cross](https://github.com/rnc) for raising this.
* [#1191] API: Add `@PicocliScript2` annotation to support subcommand methods in Groovy scripts. Thanks to [Mattias Andersson](https://github.com/attiand) for raising this.
* [#1241] API: Add `mapFallbackValue` attribute to `@Options` and `@Parameters` annotations, and corresponding `ArgSpec.mapFallbackValue()`.
* [#1217] API: Add `IParameterPreprocessor` parser plugin to invoke custom logic when a command, option or positional parameter is matched. Thanks to [David Walluck](https://github.com/dwalluck) for raising this.
* [#1259][#1266] API: Add `IModelTransformer` to support user-defined model transformations after initialization and before parsing. Thanks to [Marko Mackic](https://github.com/MarkoMackic) for the pull request.
* [#802][#1284] API: Add support for `echo` and `prompt` in for interactive options and positional parameters. Thanks to [H.Sakata](https://github.com/sakata1222) for the pull request.
* [#1184] API: Added public methods `Help.Layout::colorScheme`, `Help.Layout::textTable`, `Help.Layout::optionRenderer`, `Help.Layout::parameterRenderer`, and `Help::calcLongOptionColumnWidth`.
* [#1254] API: Added `ArgSpec::root`: this method returns the original `ArgSpec` for inherited `ArgSpec` objects, and `null` for other `ArgSpec` objects. Thanks to [Daniel Gray](https://github.com/danielthegray) for the pull request.
* [#1256] API: Added `CommandSpec::removeSubcommand` method. Thanks to [Marko Mackic](https://github.com/MarkoMackic) for raising this.
* [#1258] API: Groovy programs can now use closures in the picocli annotations instead of specifying a class. Thanks to [Adrian A.](https://github.com/aadrian) for raising this.
* [#1267] API: Add `CommandLine::getFactory` accessor for the factory. Thanks to [Sualeh Fatehi](https://github.com/sualeh) for the suggestion.
* [#1108] Enhancement: Support `Optional<T>` type for options and positional parameters. Thanks to [Max Rydahl Andersen](https://github.com/maxandersen) for raising this.
* [#1214] Enhancement: Support Map options with key-only (support `-Dkey` as well as `-Dkey=value`). Thanks to [Max Rydahl Andersen](https://github.com/maxandersen) and [David Walluck](https://github.com/dwalluck) for raising this and subsequent discussion.
* [#1260] Enhancement: Support `@Spec`-annotated members in `ArgGroup` classes. Thanks to [Jannick Hemelhof](https://github.com/clone1612) for raising this.
* [#1265] Enhancement in `picocli-shell-jline3`: add built-in `clear` command and improve `help` command. Thanks to [Sualeh Fatehi](https://github.com/sualeh) for the pull request.
* [#1236] Enhancement/bugfix: Fix compiler warnings about `Annotation::getClass` and assignment in `if` condition. Thanks to [nveeser-google](https://github.com/nveeser-google) for the pull request.
* [#1229] Bugfix: Fix compilation error introduced with fc5ef6de6 (#1184). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1225] Bugfix: Error message for unmatched positional argument reports an incorrect index when value equals a previously matched argument. Thanks to [Vitaly Shukela](https://github.com/vi) for raising this.
* [#1250] Bugfix: Inherited positional parameter should not be overridden by default value if placed after subcommand. Thanks to [Daniel Gray](https://github.com/danielthegray) for the pull request.
* [#1183] Bugfix: Prevent `MissingParameterException` thrown when subcommand has required options and help option is specified on parent command. Thanks to [drkilikil](https://github.com/drkilikil) for raising this.
* [#1273] Bugfix: The `Help.calcLongOptionColumnWidth` now calls `Help.createDefaultOptionRenderer`, so overriding `createDefaultOptionRenderer` uses the correct column width in the options and parameters list.
* [#1274] Bugfix: `ReflectionConfigGenerator` should not generate method section in subclass config for private superclass methods in `reflect-config.json`. Thanks to [Sebastian Thomschke](https://github.com/sebthom) for raising this.
* [#1215] DOC: User manual improvements, including more tabs with Kotlin source code. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1219] DOC: User manual improvements: added more tabs with Kotlin code. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1220] DOC: User manual improvements: corrections, more Kotlin tabs. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1221] DOC: User manual improvements: add tabs with Kotlin code for samples (chapter 14: Usage help). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1222] DOC: User manual improvements: add tabs with Kotlin code for samples (chapter 7 + 12). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1223] DOC: User manual improvements: add tabs with Kotlin code for samples (chapter 10: Validation). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1224] DOC: User manual improvements: add tabs with Kotlin code for samples (chapter 5: Default Values). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1226] DOC: User manual improvements: add tabs with Kotlin code for samples (chapter 9.6 - 9.8: Executing commands). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1228] DOC: User manual improvements: add tabs with Kotlin code for samples (chapters 8, 16, 20). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1230] DOC: User manual improvements: add tabs with Kotlin code for samples (Chapters 6, 11, 15, 19). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1232] DOC: User manual improvements for Micronaut example: add Kotlin version, extended description of Micronaut usage. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1233] DOC: User manual improvements: add tabs with Kotlin code for samples (Chapter 21: Tips & Tricks). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1234] DOC: add system properties example to user manual.
* [#1235][#1238] DOC: User manual: update DI section on Quarkus. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1246] DOC: User manual improvements: Guice and Spring Boot examples: add Kotlin versions. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1242] DOC: "Foldable" table of contents for User Manual and Quick Guide. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1247] DOC: User manual: extended Spring Boot example. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1249] DOC: Added section on Mocking to user manual. Thanks to [Petr Hála](https://github.com/pehala) for the pull request.
* [#1244] DEP: Bump `Spring-Boot-Starter` version to 2.3.5.RELEASE. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1289] DEP: Bump Spring boot dependency to 2.4.1. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1248] BUILD: Fix gradle warnings. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1280] BUILD: Remove trailing comment from gradle.properties to prevent build error. Thanks to [David Walluck](https://github.com/dwalluck) for raising this.
* [#1253] DOC: Fix line endings to LF in documentation files. Thanks to [Daniel Gray](https://github.com/danielthegray) for the pull request.
* [#1255] DOC: User manual and Quick Guide: add Groovy, Kotlin and Scala examples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1261] DOC: User manual improvements: add Scala code samples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1262] DOC: User manual: include jbang in the Build Tools section. Thanks to [Max Rydahl Andersen](https://github.com/maxandersen) for the pull request.
* [#1263] DOC: User manual: show build scripts in tabs. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1264] DOC: Fix broken links to GraalVm native image build configuration. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1005] DOC: Add link to the CookieTemple [cli-java template](https://cookietemple.readthedocs.io/en/latest/available_templates/available_templates.html#cli-java) README. Thanks to [Lukas Heumos](https://github.com/Zethson) for getting this added to CookieTemple.
* [#1276] DOC: User manual: add section for "really executable JARs". Thanks to [David Phillips](https://github.com/electrum) for the pull request.
* [#1286] DOC: Fix: `@ParentObject` should be `@ParentCommand`. Thanks to [Laurent Almeras](https://github.com/lalmeras) for the pull request.
* [#1290] DOC: JLine: change keystroke syntax 'Ctl-D' to more common used syntax 'Ctrl-D'. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1270] TEST: Fix issue #1103 in `Issue1225UnmatchedArgBadIndex`. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.

## <a name="4.6.0-deprecated"></a> Deprecations
From this release, the `@picocli.groovy.PicocliScript` annotation in the `picocli-groovy` module is deprecated in favor of `@picocli.groovy.PicocliScript2`, and the `picocli.groovy.PicocliBaseScript` class is deprecated in favor of `picocli.groovy.PicocliBaseScript2`.

## <a name="4.6.0-breaking-changes"></a> Potential breaking changes
Added method `isOptional()` to the `picocli.CommandLine.Model.ITypeInfo` interface.


# <a name="4.5.2"></a> Picocli 4.5.2
The picocli community is pleased to announce picocli 4.5.2.

This release contains bug fixes and enhancements:

* Auto-enable ANSI colors on MSYS2 terminals.
* Abbreviated options are now matched correctly even when value attached with '=' separator.
* The built-in `HelpCommand` now respects subcommands case-sensitivity and abbreviations.
* Required parameters no longer consume negated options.
* Positional parameters in Argument Groups no longer result in `ArithmeticException: / by zero` exceptions.
* The user manual now has tabs showing examples in languages other than Java.
  This is a work in progress: many examples still only have a Java version.
  Contributions welcome!
* Many, many documentation enhancements, most of which contributed by the community.

Many thanks to the picocli community who contributed 28 pull requests in this release!
Please see the Fixed Issues section below for the individual contributors. Great work!

This is the seventy-fourth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.5.2-toc"></a> Table of Contents
* [New and noteworthy](#4.5.2-new)
* [Fixed issues](#4.5.2-fixes)
* [Deprecations](#4.5.2-deprecated)
* [Potential breaking changes](#4.5.2-breaking-changes)

## <a name="4.5.2-new"></a> New and Noteworthy
The user manual now has tabs showing examples in languages other than Java.
This is a work in progress: many examples still only have a Java version.
Contributions welcome!

## <a name="4.5.2-fixes"></a> Fixed issues
* [#1186] Enhancement: Auto-enable ANSI colors on MSYS2 (Git for Windows, MSYS2-based Windows Terminal shells, etc.). Thanks to [Sysmat](https://github.com/sysmat) for raising this.
* [#1162] Bugfix: Abbreviated options are not matched if value attached with '=' separator (like `-x=3`). Thanks to [Chris Laprun](https://github.com/metacosm) for raising this.
* [#1156][#1172] Bugfix: the built-in `HelpCommand` now respects subcommands case-sensitivity and abbreviations. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1197] Bugfix: required parameters should not consume negated options. Thanks to [Kevin Turner](https://github.com/keturn) for raising this.
* [#1213] Bugfix: `@Parameters` in `@ArgGroup` should not result in `ArithmeticException: / by zero`. Thanks to [Loren Keagle](https://github.com/LorenKeagle) for raising this.
* [#1158] DOC: Fix broken links to GraalVM repo. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1155] DOC: Fix sample code in chapter "Validation". Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1157] DOC: Fix typo "a argument group" in user manual. Thanks to sabrina for raising this.
* [#1160] DOC: Added section [Was a Value Defaulted?](https://picocli.info/#_was_a_value_defaulted) to the user manual.
* [#1161] DOC: Fix typo "4,2" (should be 4.2) in user manual. Thanks to sabrina for raising this.
* [#1165] DOC: Fix jline3 example: add `AnsiConsole::systemUninstall` in `finally` clause. Thanks to [David Walluck](https://github.com/dwalluck) for raising this.
* [#1168][#1169] DOC: Ensure `org.jline.terminal.Terminal` is closed when done. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
* [#1167] DOC: Fix broken links in Quick Guide. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1171] DOC: Various documentation improvements. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1173] DOC: Improve example applications for the user manual and Quick Guide. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1175] DOC: section on compatible versions to `picocli-shell-jline3/README.md`. Thanks to [Nick Cross](https://github.com/rnc) for raising this.
* [#1176] DOC: Update JLine `picocli-shell-jline3` example to 3.16.0. Thanks to [Nick Cross](https://github.com/rnc) for the pull request.
* [#890][#1187] DOC: Extend and improve subcommands documentation. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1190] DOC: Improve InetSocketAddressConverter demo. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1192] DOC: Fix broken links in documentation. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1196] DOC: Quick Guide examples can now be executed on the documentation page via [JDoodle.com](https://www.jdoodle.com). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1200] DOC: User manual `checksum` example can now be executed on the documentation page via [JDoodle.com](https://www.jdoodle.com). Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1199] DOC: Fix `paramLabel` in examples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1198] DOC: Add copy button to code blocks. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1201] DOC: User manual `checksum` example: add Kotlin source code on second tab. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1202] DOC: Update to latest Asciidoctor gradle plugin. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1203] DOC: Replace 'coderay' source code highlighter with 'rouge' to support Kotlin, Scala and Groovy. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1205] DOC: User manual `checksum` example: add more tabs for Groovy, Groovy script and Scala. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1208] DOC: Fix: Show copy buttons in code blocks with latest Asciidoctor gradle plugin. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1209] DOC: Show Maven coordinates in JLine2/3 README. Thanks to [Jiří Holuša](https://github.com/Holmistr) for the pull request.
* [#1210] DOC: User manual `subcommands` example: add tab with Kotlin source code. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1211] DOC: User manual `subcommands` section: add several tabs with Kotlin source code. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1170] TEST: Ensure ANSI is disabled in `ManPageGeneratorTest` regardless of environment. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
* [#1166][#1103] TEST: Ensure ANSI is disabled in `TracerTest` regardless of environment. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
* [#1179] TEST: Use `.invalid` domain name for `InetAddress` test. Thanks to [David Phillips](https://github.com/electrum) for the pull request.
* [#1178] BUILD: Run Travis build on macOS. Thanks to [David Phillips](https://github.com/electrum) for the pull request.
* [#1192] Dependency Upgrade: Bump AsciiDoctor to 2.1.0 from 1.6.2. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.

## <a name="4.5.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.5.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.5.1"></a> Picocli 4.5.1
The picocli community is pleased to announce picocli 4.5.1.

This release contains bug fixes and enhancements.

Fixed bug in the `picocli-codegen` annotation processor that resulted in errors in native images that used `ManPageGenerator` as subcommand.

Suppress generation of Gradle Module Metadata, to fix Gradle build failures for projects using picocli 4.4.0 or 4.5.0.

Fixed terminal width detection on macOS.

The user manual now has a new section on [Validation](https://picocli.info/#_validation). Various other documentation improvements.


This is the seventy-third public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.5.1-toc"></a> Table of Contents
* [New and noteworthy](#4.5.1-new)
* [Fixed issues](#4.5.1-fixes)
* [Deprecations](#4.5.1-deprecated)
* [Potential breaking changes](#4.5.1-breaking-changes)

## <a name="4.5.1-new"></a> New and Noteworthy


## <a name="4.5.1-fixes"></a> Fixed issues
* [#1151] Bugfix: `ManPageGenerator` as subcommand with native-image throws exception. Thanks to [Sebastian Hoß](https://github.com/sebhoss) for raising this.
* [#1152] Bugfix: Gradle build fail when using picocli 4.4.0 or 4.5.0: Gradle Module Metadata for picocli 4.4.0+ missing from Maven Central. Thanks to [Frank Pavageau](https://github.com/fpavageau) for reporting this.
* [#1150] Bugfix: Fix terminal width detection on macOS. Thanks to [David Phillips](https://github.com/electrum) for the pull request.
* [#1142] DOC: Update Kotlin GraalVM native image example - Update native image gradle plugin. Now supports jdk 11. Thanks to [OndrejMalek](https://github.com/OndrejMalek) for the pull request.
* [#1153] DOC: Fix documentation leading code quote. Thanks to sabrina for raising this.
* [#1147] DOC: Add documentation on how to do custom parameter validation. Thanks to [Loren Keagle](https://github.com/LorenKeagle) for raising this.


## <a name="4.5.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.5.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.5.0"></a> Picocli 4.5.0
The picocli community is pleased to announce picocli 4.5.0.

This release contains bug fixes and enhancements.

The `ColorScheme` class now has new methods `stackTraceText` and `richStackTraceString`, which can be convenient when creating [custom error handlers](https://picocli.info/#_handling_errors) with colors.

Various bugfixes in the `picocli-codegen` annotation processor.

The user manual now has anchor links before all section titles.

This is the seventy-second public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.5.0-toc"></a> Table of Contents
* [New and noteworthy](#4.5.0-new)
* [Fixed issues](#4.5.0-fixes)
* [Deprecations](#4.5.0-deprecated)
* [Potential breaking changes](#4.5.0-breaking-changes)

## <a name="4.5.0-new"></a> New and Noteworthy


## <a name="4.5.0-fixes"></a> Fixed issues
* [#1129] API: Add methods `ColorScheme::stackTraceText(Throwable)` and `ColorScheme::richStackTraceString(Throwable)`.
* [#1124] Enhancement: automatically generate a better summary in the `AutoComplete.GenerateCompletion` generated man page.
* [#1126] Enhancement: Make picocli trace levels case-insensitive.
* [#1128] Enhancement: `ParameterException` caused by `TypeConversionException` now have their cause exception set.
* [#1137] Bugfix: The `picocli-codegen` annotation processor causes the build to fail with a `ClassCastException` when an option has `completionCandidates` defined.
* [#1134] Bugfix: The `picocli-codegen` annotation processor should allow `@Spec`-annotated field in classes implementing `IVersionProvider`.
* [#1138] Bugfix: The `picocli-codegen` annotation processor no longer gives `FATAL ERROR: picocli.CommandLine$InitializationException: ArgGroup has no options or positional parameters, and no subgroups` during incremental compilation in Intelli/J IDEA.
* [#1127] DOC: Custom ShortErrorMessageHandler manual example should use bold red for error message.
* [#1130] DOC: Clarify how to run picocli-based applications.
* [#1131] DOC: Add anchor links before section titles in user manual.

## <a name="4.5.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.5.0-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.4.0"></a> Picocli 4.4.0
The picocli community is pleased to announce picocli 4.4.0.

This release contains over 45 bugfixes, enhancements, and new features.

A major new feature in this release is support for abbreviated options and subcommands. When abbreviations are enabled, users can specify the initial letter(s) of the first "component" and optionally of one or more subsequent components of an option or subcommand name.
"Components" are parts of a name, separated by `-` dash characters or by upper/lower case. So for example, both `--CamelCase` and `--kebab-case` have two components.
For details see the [New and Noteworthy](#4.4.0-new) section below.

Another important change are parser fixes and improvements: the parser will no longer assign values that match an option name to options that take a parameter, unless the value is in quotes. Also, values that resemble, but not exactly match, option names are now treated more consistently and parser behaviour for such values is configurable.

Also worth highlighting: from this release, the `ManPageGenerator` tool can be used as a subcommand in your application.

This release has many more improvements for customizing the usage help message, JANSI fixes, and other bugfixes and improvements. See the [Fixed Issues](#4.4.0-fixes) list for details.

This is the seventy-first public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.4.0-toc"></a> Table of Contents
* [New and noteworthy](#4.4.0-new)
  * [Abbreviated Options and Subcommands](#4.4.0-abbreviated-options-and-commands)
  * [Parser Fixes and Improvements](#4.4.0-parser)
  * [ManPageGenerator as Subcommand in Your App](#4.4.0-gen-manpage-subcommand)
* [Fixed issues](#4.4.0-fixes)
* [Deprecations](#4.4.0-deprecated)
* [Potential breaking changes](#4.4.0-breaking-changes)

## <a name="4.4.0-new"></a> New and Noteworthy

### <a name="4.4.0-abbreviated-options-and-commands"></a> Abbreviated Options and Subcommands

Since picocli 4.4, the parser can recognize abbreviated options and subcommands.
This needs to be enabled explicitly with `CommandLine::setAbbreviatedOptionsAllowed` and `CommandLine::setAbbreviatedSubcommandsAllowed`.

#### Recognized Abbreviations
When abbreviations are enabled, users can specify the initial letter(s) of the first component and optionally of one or more subsequent components of an option or subcommand name.

"Components" are separated by `-` dash characters or by case, so for example, both `--CamelCase` and `--kebab-case` have two components.

NOTE: When case sensitivity is disabled, only the `-` dash character can be used to separate components.

Examples of valid abbreviations:

```
Option or Subcommand | Recognized Abbreviations
-------------------- | ------------------------
--veryLongCamelCase  | --very, --vLCC  --vCase
--super-long-option  | --sup, --sLO, --s-l-o, --s-lon, --s-opt, --sOpt
some-long-command    | so, sLC, s-l-c, soLoCo, someCom
```

#### Ambiguous Abbreviations
When the user specifies input that can match multiple options or subcommands, the parser throws a `ParameterException`.
When applications use the `execute` method, a short error message and the usage help is displayed to the user.

For example, given a command with subcommands `help` and `hello`, then ambiguous user input like `hel` will show this error message:

```
Error: 'hel' is not unique: it matches 'hello', 'help'
```

#### Abbreviated Long Options and POSIX Clustered Short Options

When an argument can match both a long option and a set of clustered short options, picocli matches the long option.

For example:

```java
class AbbreviationsAndPosix {
    @Option(names = "-A") boolean a;
    @Option(names = "-B") boolean b;
    @Option(names = "-AaaBbb") boolean aaaBbb;
}
AbbreviationsAndPosix app = new AbbreviationsAndPosix();
new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("-AB");
assertTrue(app.aaaBbb);
assertFalse(app.a);
assertFalse(app.b);
```

When abbreviated options are enabled, user input `-AB` will match the long `-AaaBbb` option, but not the `-A` and `-B` options.

### <a name="4.4.0-parser"></a> Parser Fixes and Improvements

#### Option Names as Option Values

Options that take a parameter previously were able to take option names as the parameter value.
From this release, this is no longer possible.
The parser will no longer assign values that match an option name to an option, unless the value is in quotes. For example:

```java
class App {
    @Option(names = "-x") String x;
    @Option(names = "-y") String y;

    public static void main(String... args) {
        App app = new App();
        new CommandLine(app).setTrimQuotes(true).parseArgs(args);
        System.out.printf("x='%s', y='%s'%n", app.x, app.y);
    }
}
```

In previous versions of picocli, the above command would accept input `-x -y`, and the value `-y` would be assigned to the `x` String field. From this release, the above input will be rejected with an error message indicating that the `-x` option requires a parameter.

If it is necessary to accept values that match option names, these values need to be quoted. For example:

```
java App -x="-y"
```

This will print the following output:

```
x='-y', y='null'
```

#### Vararg Positional Parameters No Longer Consume Unmatched Options

Vararg positional arguments no longer consume unmatched options unless configured to do so. For example:

```java
class App {
    @Parameters(arity = "*") String[] positionals;
}
```

In previous versions of picocli, the parser behaviour was not consistent:

* input `-z 123` would be rejected with error `"Unmatched argument: '-z'`
* input `123 -z` would be accepted and the `positionals` String array would contain two values, `123` and `-z`

(Note that this problem only occurred with multi-value positional parameters defined with variable arity: `arity = "*"`.)

From this release, both of the above input sequences will be rejected with an error message indicating that `-z` is an unknown option.
As before, to accept such values as positional parameters, call `CommandLine::setUnmatchedOptionsArePositionalParams` with `true`.

#### Configure Whether Options Should Consume Unknown Options

By default, options accept parameter values that "resemble" (but don't exactly match) an option.

This release introduces a `CommandLine::setUnmatchedOptionsAllowedAsOptionParameters` method that makes it possible to configure the parser to reject values that resemble options as option parameters.
Setting it to `false` will result in values resembling option names being rejected as option values.

For example:

```java
class App {
    @Option(names = "-x") String x;
}
```

By default, a value like `-z`, which resembles an option, is accepted as the parameter for `-x`:

```java
App app = new App();
new CommandLine(app).parseArgs("-x", "-z");
assertEquals("-z", app.x);
```

After setting the `unmatchedOptionsAllowedAsOptionParameters` parser option to `false`, values resembling an option are rejected as parameter for `-x`:

```java
new CommandLine(new App())
        .setUnmatchedOptionsAllowedAsOptionParameters(false)
        .parseArgs("-x", "-z");
```

This will throw an `UnmatchedArgumentException` with message:

```
"Unknown option '-z'; Expected parameter for option '-x' but found '-z'"
```

NOTE: Negative numbers are not considered to be unknown options, so even when `unmatchedOptionsAllowedAsOptionParameters` is set to `false`, option parameters like `-123`, `-NaN`, `-Infinity`, `-#ABC` and `-0xCAFEBABE` will not be rejected for resembling but not matching an option name.



### <a name="4.4.0-gen-manpage-subcommand"></a> ManPageGenerator as Subcommand in Your App

From picocli 4.4, the `ManPageGenerator` tool can be used as a subcommand in your application, with the usual syntax:

```
import picocli.codegen.docgen.manpage.ManPageGenerator;

@Command(subcommands = ManPageGenerator.class)
...
```

To use the `ManPageGenerator` tool as a subcommand, you will need the `picocli-codegen` jar in your classpath.

## <a name="4.4.0-fixes"></a> Fixed issues
* [#10][#732][#1047] API: Support abbreviated options and commands. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#639] API: Add method `CommandLine::is/setUnmatchedOptionsAllowedAsOptionParameters` to disallow option parameter values resembling option names. Thanks to [Peter Murray-Rust ](https://github.com/petermr) for raising this.
* [#1074][#1075] API: Added method `ParseResult::expandedArgs` to return the list of arguments after `@-file` expansion. Thanks to [Kevin Bedi](https://github.com/mashlol) for the pull request.
* [#1052] API: Show/Hide commands in usage help on specific conditions. Thanks to [Philippe Charles](https://github.com/charphi) for raising this.
* [#1088] API: Add method `Help::allSubcommands` to return all subcommands, including hidden ones. Clarify the semantics of `Help::subcommands`.
* [#1090] API: Add methods `Help::optionListExcludingGroups` to return a String with the rendered section of the usage help containing only the specified options, including hidden ones.
* [#1092] API: Add method `Help::parameterList(List<PositionalParamSpec>)` to return a String with the rendered section of the usage help containing only the specified positional parameters, including hidden ones.
* [#1093] API: Add method `Help::commandList(Map<String, Help>)` to return a String with the rendered section of the usage help containing only the specified subcommands, including hidden ones.
* [#1091] API: Add method `Help::optionListGroupSections` to return a String with the rendered section of the usage help containing only the option groups.
* [#1089] API: Add method `Help::createDefaultOptionSort` to create a `Comparator` that follows the command and options' configuration.
* [#1084][#1094] API: Add method `Help::createDefaultLayout(List<OptionSpec>, List<PositionalParamSpec>, ColorScheme)` to create a layout for the specified options and positionals.
* [#1087] API: Add methods `ArgumentGroupSpec::allOptionsNested` and `ArgumentGroupSpec::allPositionalParametersNested`.
* [#1086] API: add methods `Help.Layout::addAllOptions` and `Help.Layout::addAllPositionals`, to show all specified options, including hidden ones.
* [#1085] API: Add method `Help::optionSectionGroups` to get argument groups with a header.
* [#1101] API: Add method `Help::createDetailedSynopsisOptionsText` to specify which options to show in the synopsis.
* [#1061] API: Add method `Help::makeSynopsisFromParts` for building complex synopsis strings; synopsis now shows non-group options before argument groups, for a more natural synopsis when groups contain only positional parameters.
* [#983] Allow making inherited options hidden on subcommands. This can now be accomplished with the new `Help` methods by providing a custom option list and customizing the synopsis.
* [#1051][#1056] Enhancement: `GenerateCompletion` command no longer needs to be a direct subcommand of the root command. Thanks to [Philippe Charles](https://github.com/charphi) for the pull request.
* [#1083] Enhancement: `@Command`-annotated methods no longer need the enclosing class to have a `@Command` annotation.
* [#1068] Enhancement: Make `ParserSpec::toString` output settings in alphabetic order.
* [#1069] Enhancement: Debug output should show `optionsCaseInsensitive` and `subcommandsCaseInsensitive` settings.
* [#1070] Enhancement: Code cleanup: removed redundant modifiers and initializations, unused variables, incorrect javadoc references, and more. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1096] Enhancement: Override `Help.Column` `equals`, `hashCode` and `toString` methods to facilitate testing.
* [#1106] Enhancement: First check if JANSI is explicitly disabled _without loading any JANSI classes_, to avoid JANSI extracting a DLL to the temporary folder when one of its classes is loaded. This avoids problems where AppLocker can forbid loading of non-signed libraries from the Windows temporary folder. Thanks to [Philippe Charles](https://github.com/charphi) for raising this.
* [#1110] Enhancement: Fix broken javadoc links, fix Kotlin compiler warnings, bump to latest Kotlin and Scala versions. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1109][#1112] Enhancement: Fix `ManPageGenerator` to ensure generated AsciiDoc man pages use UTF-8 encoding. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1063][#1064] `ManPageGenerator` now correctly excludes hidden options, parameters, and subcommands from man page generation. Thanks to [Brian Demers](https://github.com/bdemers) for the pull request.
* [#1103] Enhancement: Tests no longer fail under Cygwin/ConEmu due to ANSI in output. Thanks to [David Walluck](https://github.com/dwalluck) for raising this.
* [#1055] Bugfix: The parser will no longer assign values that match an option name to options that take a parameter, unless the value is in quotes. Thanks to [waacc-gh](https://github.com/waacc-gh) for raising this.
* [#1015] Bugfix: Parser improvement: varargs positional arguments no longer consume unmatched options unless `unmatchedOptionsArePositionalParams` is configured. Thanks to [Chris Smowton](https://github.com/smowton) for raising this.
* [#1071] Bugfix: Usage help no longer renders options header when it is specified via `optionListHeading` when all options are hidden.
* [#1076] Bugfix: Don't generate Autocomplete for hidden commands. Thanks to [power721](https://github.com/power721) for raising this.
* [#1081] Bugfix: `CommandLine.Help` constructor no longer calls overridable methods `addAllSubcommands` and `createDefaultParamLabelRenderer`.
* [#1065] Bugfix: With a `List<>` option in `@ArgGroup`, group incorrectly appears twice in the synopsis. Thanks to [kap4lin](https://github.com/kap4lin) for raising this.
* [#1067] Bugfix: `ParserSpec::initFrom` was not copying `useSimplifiedAtFiles`.
* [#1054] Bugfix: Fixed issue in argument group parsing where incorrect input with missing mandatory elements was accepted when an option was specified multiple times. Thanks to [waacc-gh](https://github.com/waacc-gh) for raising this.
* [#1072] Bugfix: Mixin `UsageMessageSpec::width` and `UsageMessageSpec::longOptionsMaxWidth` is no longer ignored.
* [#1100] Bugfix: The factory of the original `CommandSpec` is now correctly used in the `CommandSpec` copy for repeatable subcommands. Thanks to [Michael Kunz](https://github.com/protogenes) for the pull request.
* [#1058][#1059] DOC: Man page generator: fix incorrect asciidoctor call in synopsis.  Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1058][#1060] DOC: Man page generator: add documentation about creating language variants.  Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1120] Clean up compiler warnings.
* [#1073] DOC: Improve user manual: fix typos, update content. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1102] DOC: Show `descriptionKeys` for `@file` and EndOfOptions (--) delimiter in resource bundles.
* [#1116] DOC: Improved Guice example in user manual. Thanks to [H.Sakata](https://github.com/sakata1222) for the pull request.
* [#1098][#1117] DOC: Simplify JLine 3 documentation by moving examples for older JLine 3 and picocli to the [picocli wiki](https://github.com/remkop/picocli/wiki/JLine-3-Examples). Thanks to [Kevin Arthur](https://github.com/thetoothpick) for the pull request.
* [#1121] DOC: Link to alternative in `@deprecated` Javadoc tag for `Help::addSubcommand`.
* [#1099] Dependency Upgrade: Bump JLine to 3.15.0. Thanks to [mattirn](https://github.com/mattirn) for the pull request.


## <a name="4.4.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.4.0-breaking-changes"></a> Potential breaking changes

### Parser Changes
The parser behaviour has changed: picocli will no longer assign values that match an option name to options that take a parameter, unless the value is in quotes.
Applications that rely on this behaviour need to use quoted values.

### Error Message for Unknown Options
Unmatched arguments that look like options now result in an error message `Unknown option: '-unknown'`.

Previously, the error message was: `Unmatched argument: '-unknown'`.


### Usage Help: Synopsis for Arg Groups
This release changes the synopsis for commands with argument groups:
the synopsis now shows the non-group options before argument groups, where previously argument groups were shown first.

This gives a more natural synopsis when groups contain only positional parameters.


# <a name="4.3.2"></a> Picocli 4.3.2
The picocli community is pleased to announce picocli 4.3.2.

This release fixes a bug where the stack trace of an exception in the business logic would omit nested cause exceptions.

This is the seventieth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.3.2-toc"></a> Table of Contents
* [New and noteworthy](#4.3.2-new)
* [Fixed issues](#4.3.2-fixes)
* [Deprecations](#4.3.2-deprecated)
* [Potential breaking changes](#4.3.2-breaking-changes)

## <a name="4.3.2-new"></a> New and Noteworthy


## <a name="4.3.2-fixes"></a> Fixed issues
* [#1048][#1049] Bugfix: Cause exception not printed by default execution exception handler. Thanks to [Neko Null](https://github.com/jerrylususu) for the pull request.


## <a name="4.3.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.3.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.3.1"></a> Picocli 4.3.1
The picocli community is slightly embarrassed to announce picocli 4.3.1. :-)

This release fixes some critical bugs:

* an `IllegalArgumentException: wrong number of arguments` was thrown when the `@Option(scope = INHERIT)` feature is used in a command that has subcommands defined in `@Command`-annotated methods
* a `NullPointerException` was thrown in `DefaultParamLabelRenderer.renderParameterLabel` for programmatically built models that have a non-`null` `split` regex and do not have a `splitSynopsisLabel`
* removed call to a `String` method introduced in Java 6, which prevented picocli from running on Java 5

See [Fixed issues](#4.3.1-fixes) for the full list of changes.

This is the sixty-ninth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.3.1-toc"></a> Table of Contents
* [New and noteworthy](#4.3.1-new)
* [Fixed issues](#4.3.1-fixes)
* [Deprecations](#4.3.1-deprecated)
* [Potential breaking changes](#4.3.1-breaking-changes)

## <a name="4.3.1-new"></a> New and Noteworthy


## <a name="4.3.1-fixes"></a> Fixed issues
* [#1042] Bugfix: "wrong number of arguments" exception when using inherited options with `@Command`-annotated methods. Thanks to [Garret Wilson](https://github.com/garretwilson) for raising this.
* [#1043] Bugfix: NullPointerException thrown in `DefaultParamLabelRenderer.renderParameterLabel` for programmatically built models that have a non-`null` `split` regex and do not have a `splitSynopsisLabel`.
* [#1044] Bugfix: only display `splitSynopsisLabel` in usage help message if the option has a `split` regex. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#1045] Bugfix: replace use of Java 6 API `String.isEmpty` with picocli-internal Java 5 equivalent.
* [#1046] DOC: mention picocli's programmatic API and link to the programmatic API documentation from the user manual.

## <a name="4.3.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.3.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.3.0"></a> Picocli 4.3.0
The picocli community is pleased to announce picocli 4.3.0.

This is a fairly big release with 70 [tickets closed](https://github.com/remkop/picocli/milestone/65?closed=1), and over 50 [bugfixes and enhancements](#4.3.0-fixes). Many thanks to the picocli community who contributed 21 pull requests!

A major theme of this release is sharing options between commands:
* New feature: "inherited" options. Options defined with `scope = ScopeType.INHERIT` are shared with all subcommands (and sub-subcommands, to any level of depth). Applications can define an inherited option on the top-level command, in one place, to allow end users to specify this option anywhere: not only on the top-level command, but also on any of the subcommands and nested sub-subcommands.
* More powerful mixins. Mixin classes can declare a `@Spec(MIXEE)`-annotated field, and picocli will inject the `CommandSpec` of the command _receiving_ this mixin (the "mixee") into this field. This is useful for mixins containing shared logic, in addition to shared options and parameters.

Another major theme is improved support for positional parameters:
* Automatic indexes for positional parameters. Single-value positional parameters without an explicit `index = "..."` attribute are now automatically assigned an index based on the other positional parameters in the command. One use case is mixins with positional parameters.
* Repeatable ArgGroups can now define positional parameters.

Other improvements:
* The parser now supports case-insensitive mode for options and subcommands.
* Error handlers now use ANSI colors and styles. The default styles are bold red for the error message, and italic for stack traces. Applications can customize with the new `Help.ColorScheme` methods `errors` and `stackTraces`.
* The usage help message can now show an entry for `--` in the options list with the `@Command(showEndOfOptionsDelimiterInUsageHelp = true)` annotation.
* Easily make subcommands mandatory by making the top-level command a class that does not implement `Runnable` or `Callable`.


This is the sixty-eighth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.3.0-toc"></a> Table of Contents
* [New and noteworthy](#4.3.0-new)
  * [Inherited Options](#4.3.0-inherited-options)
  * [Case-insensitive mode](#4.3.0-case-insensitive)
  * [Automatic Indexes for Positional Parameters](#4.3.0-auto-index)
  * [Repeatable ArgGroups with Positional Parameters](#4.3.0-positionals-in-groups)
  * [`@Spec(MIXEE)` Annotation](#4.3.0-mixee)
  * [Showing `--` End of Options in usage help](#4.3.0-end-of-options)
* [Fixed issues](#4.3.0-fixes)
* [Deprecations](#4.3.0-deprecated)
* [Potential breaking changes](#4.3.0-breaking-changes)

## <a name="4.3.0-new"></a> New and Noteworthy
### <a name="4.3.0-inherited-options"></a> Inherited Options

This release adds support for "inherited" options. Options defined with `scope = ScopeType.INHERIT` are shared with all subcommands (and sub-subcommands, to any level of depth). Applications can define an inherited option on the top-level command, in one place, to allow end users to specify this option anywhere: not only on the top-level command, but also on any of the subcommands and nested sub-subcommands.

Below is an example where an inherited option is used to configure logging.

```java
@Command(name = "app", subcommands = Sub.class)
class App implements Runnable {
    private static Logger logger = LogManager.getLogger(App.class);

    @Option(names = "-x", scope = ScopeType.LOCAL) // option is not shared: this is the default
    int x;

    @Option(names = "-v", scope = ScopeType.INHERIT) // option is shared with subcommands, sub-subcommands, etc
    public void setVerbose(boolean verbose) {
        // Configure log4j.
        // This is a simplistic example: you probably only want to modify the ConsoleAppender level.
        Configurator.setRootLevel(verbose ? Level.DEBUG : Level.INFO);
    }

    public void run() {
        logger.debug("-x={}", x);
    }
}

@Command(name = "sub")
class Sub implements Runnable {
    private static Logger logger = LogManager.getLogger(Sub.class);

    @Option(names = "-y")
    int y;

    public void run() {
        logger.debug("-y={}", y);
    }
}
```

Users can specify the `-v` option on either the top-level command or on the subcommand, and it will have the same effect.

```
# the -v option can be specified on the top-level command
java App -x=3 -v sub -y=4
```

Specifying the `-v` option on the subcommand will have the same effect. For example:
```
# specifying the -v option on the subcommand also changes the log level
java App -x=3 sub -y=4 -v
```

NOTE: Subcommands don't need to do anything to receive inherited options, but a potential drawback is that subcommands do not get a reference to inherited options.

Subcommands that need to inspect the value of an inherited option can use the `@ParentCommand` annotation to get a reference to their parent command, and access the inherited option via the parent reference.
Alternatively, for such subcommands, sharing options via mixins may be a more suitable mechanism.

### <a name="4.3.0-case-insensitive"></a> Case-insensitive mode
By default, all options and subcommands are case sensitive. Case sensitivity can be switched off globally, as well as on a per-command basis.

To toggle case sensitivity for all commands, use the `CommandLine::setSubcommandsCaseInsensitive` and `CommandLine::setOptionsCaseInsensitive` methods. Use the `CommandSpec::subcommandsCaseInsensitive` and `CommandSpec::optionsCaseInsensitive` methods to give some commands a different case sensitivity than others.


### <a name="4.3.0-auto-index"></a> Automatic Indexes for Positional Parameters

From this release, when the `index = "..."` attribute is omitted, the default index is `index = "0+"`, which tells picocli to assign an index automatically, starting from zero, based on the other positional parameters defined in the same command.

A simple example can look like this:

```java
class AutomaticIndex {
    @Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
    List<String> allParameters; // no "index" attribute: captures _all_ arguments

    @Parameters String group;    // assigned index = "0"
    @Parameters String artifact; // assigned index = "1"
    @Parameters String version;  // assigned index = "2"
}
```

Picocli initializes fields with the values at the specified index in the arguments array.

```java
String[] args = { "info.picocli", "picocli", "4.3.0" };
AutomaticIndex auto = CommandLine.populateCommand(new AutomaticIndex(), args);

assert auto.group.equals("info.picocli");
assert auto.artifact.equals("picocli");
assert auto.version.equals("4.3.0");
assert auto.allParameters.equals(Arrays.asList(args));
```

The default automatic index (`index = "0+"`) for single-value positional parameters is "anchored at zero": it starts at zero, and is increased with each additional positional parameter.

Sometimes you want to have indexes assigned automatically from a different starting point than zero. This can be useful when defining Mixins with positional parameters.

To accomplish this, specify an index with the anchor point and a `+` character to indicate that picocli should start to automatically assign indexes from that anchor point. For example:

```java
class Anchored {
    @Parameters(index = "1+") String p1; // assigned index = "1" or higher
    @Parameters(index = "1+") String p2; // assigned index = "2" or higher
}
```

Finally, sometimes you want to have indexes assigned automatically to come at the end. Again, this can be useful when defining Mixins with positional parameters.

To accomplish this, specify an index with a `+` character to indicate that picocli should automatically assign indexes that come at the end. For example:

```java
class Unanchored {
    @Parameters(index = "+") String penultimate; // assigned the penultimate index in the command
    @Parameters(index = "+") String last;        // assigned the last index in the command
}
```

### <a name="4.3.0-positionals-in-groups"></a>  Repeatable ArgGroups with Positional Parameters

From this release, positional parameters can be used in repeating [Argument Groups](https://picocli.info/#_argument_groups).

When a `@Parameters` positional parameter is part of a group, its `index` is the index _within the group_, not within the command.

Below is an example of an application that uses a repeating group of positional parameters:

```java
@Command(name = "grades", mixinStandardHelpOptions = true, version = "grades 1.0")
public class Grades implements Runnable {

    static class StudentGrade {
        @Parameters(index = "0") String name;
        @Parameters(index = "1") BigDecimal grade;
    }

    @ArgGroup(exclusive = false, multiplicity = "1..*")
    List<StudentGrade> gradeList;

    @Override
    public void run() {
        gradeList.forEach(e -> System.out.println(e.name + ": " + e.grade));
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Grades()).execute(args));
    }
}
```

Running the above program with this input:

```
Alice 3.1 Betty 4.0 "X Æ A-12" 3.5 Zaphod 3.4
```
Produces the following output:

```
Alice: 3.1
Betty: 4.0
X Æ A-12: 3.5
Zaphod: 3.4
```

### <a name="4.3.0-mixee"></a>  `@Spec(MIXEE)` Annotation
From this release, mixins are more powerful. Mixin classes can declare a `@Spec(MIXEE)`-annotated field, and picocli will inject the `CommandSpec` of the command _receiving_ this mixin (the "mixee") into this field. This is useful for mixins containing shared logic, in addition to shared options and parameters.

Since picocli 4.3, the `@Spec` annotation has a `value` element.
The value is `Spec.Target.SELF` by default, meaning that the `CommandSpec` of the enclosing class is injected into the `@Spec`-annotated field.

For classes that are used as a [mixins](https://picocli.info/#_mixins), there is another value that may be useful.
When `@Spec(Spec.Target.MIXEE)` is specified in a mixin class, the `CommandSpec` of the command _receiving_ this mixin (the "mixee") is injected into the `@Spec`-annotated field.
This can be useful when a mixin contains logic that is common to many commands. For example:

```java
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

class AdvancedMixin {
    @Spec(Spec.Target.MIXEE) CommandSpec mixee;

    /**
     * When the -x option is specified on any subcommand,
     * multiply its value with another integer supplied by this subcommand
     * and set the result on the top-level command.
     * @param x the value of the -x option
     */
    @Option(names = "-x")
    void setValue(int x) {
        // Get another value from the command we are mixed into.
        // This mixin requires the command(s) it is mixed into to implement `IntSupplier`.
        int y = ((java.util.function.IntSupplier) mixee.userObject()).getAsInt();

        int product = x * y;

        // Set the result on the top-level (root) command.
        // This mixin requires the root command to implement `IntConsumer`.
        ((java.util.function.IntConsumer) mixee.root().userObject()).accept(product);
    }
}
```

### <a name="4.3.0-end-of-options"></a> Showing `--` End of Options in usage help
From picocli 4.3, an entry for the `--` End of Options delimiter can be shown in the options list of the usage help message of a command with the `@Command(showEndOfOptionsDelimiterInUsageHelp = true)` annotation.

Example command:

```java
@Command(name = "myapp", showEndOfOptionsDelimiterInUsageHelp = true,
        mixinStandardHelpOptions = true, description = "Example command.")
class MyApp {
    @Parameters(description = "A file.") File file;
}
```

The usage help message for this command looks like this:

```
Usage: myapp [-hV] [--] <file>
Example command.
      <file>      A file.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
  --              This option can be used to separate command-line options from
                    the list of positional parameters.
```


## <a name="4.3.0-fixes"></a> Fixed issues
* [#649][#948] Provide convenience API for inherited/global options (was: Feature request: inheriting mixins in subcommands). Thanks to [Garret Wilson](https://github.com/garretwilson) for the request and subsequent discussion (and patience!).
* [#1001] Support required inherited options.
* [#996] Default values should not be applied to inherited options.
* [#985] API: Show end-of-options `--` in usage help options list.
* [#958] API: Add `@Spec(Spec.Target.MIXEE)` annotation element to allow mixins to get a reference to the command they are mixed into.
* [#960] API: Add method `CommandSpec::root` to return the `CommandSpec` of the top-level command.
* [#484][#845][#1008] API: Error handlers now use ANSI colors and styles. Added methods `errors` and `stackTraces` to `Help.ColorScheme`. Thanks to [Neko Null](https://github.com/jerrylususu) for the pull request.
* [#765][#1017] API: Added `splitSynopsisLabel` attribute on `@Option` and `@Parameters` for controlling how `split` regular expressions are displayed in the synopsis. Thanks to [Murphy Han](https://github.com/Hannnnnn) for the pull request and thanks to [deining](https://github.com/deining) for raising this.
* [#9][#1021][#1020][#1023][#154] API: Added support for case-insensitive subcommands and options. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request, thanks to [ifsheldon](https://github.com/ifsheldon) for exploring alternative solutions and helping clarify the requirements, and thanks to [Neko Null](https://github.com/jerrylususu) for the pull request with documentation and executable examples.
* [#564] Add support for relative indices for positional parameters. Useful in mixins and inherited positional parameters. Thanks to [krisleonard-mcafee](https://github.com/krisleonard-mcafee) for raising this topic.
* [#956] Enhancement: Default ParameterExceptionHandler should show stack trace when tracing is set to DEBUG level.
* [#952] Enhancement: Make annotation processor quiet by default; add `-Averbose` annotation processor option to enable printing NOTE-level diagnostic messages to the console.
* [#959] Enhancement: Print "Missing required subcommand" instead of throwing exception if command with subcommands does not implement `Runnable` or `Callable`. Thanks to [Max Rydahl Andersen](https://github.com/maxandersen) for the suggestion.
* [#693][#1009][#1011] Enhancement: Add autocompletion for the built-in `HelpCommand`. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1022][#1029] Enhancement/Bugfix: Duplicate negated options were incorrectly accepted. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1030][#1029] Enhancement/Bugfix: `setOptionsCaseInsensitive` should make negatable options case insensitive. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#1027][#1036] Enhancement: Support repeatable ArgGroups with positional parameters. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#974] Enhancement/Bugfix: Add support for `@ArgGroup` argument groups in `@Command`-annotated methods. Thanks to [Usman Saleem](https://github.com/usmansaleem) for raising this.
* [#962][#961] Enhancement/Bugfix: Default value should only be applied if value is missing. Thanks to [粟嘉逸](https://github.com/sjyMystery) and [chirlo](https://github.com/chirlo) for raising this.
* [#995][#1024][#1035] Enhancement/Bugfix: Reset multi-value options/positional params to initial value when reusing `CommandLine` instances. Thanks to [Linyer-qwq](https://github.com/Linyer-qwq), [WU Jiangning](https://github.com/licia-tia), and [Wycers](https://github.com/Wycers) for the pull request.
* [#991][#993] Enhancement/Bugfix: Detecting terminal width fails on non-English Windows versions. Thanks to [Stefan Gärtner](https://github.com/S-Gaertner) for the pull request.
* [#1040] Enhancement: internal code cleanup and minor fixes. Thanks to [NewbieOrange](https://github.com/NewbieOrange) for the pull request.
* [#987] Bugfix: Bump JLine to 3.14.1 and fix [#969] autocompletion in Picocli Shell JLine3. Thanks to [mattirn](https://github.com/mattirn) for the pull request.
* [#969] Bugfix: Fixed broken autocompletion for nested subcommands in Picocli Shell JLine3. Thanks to [niklas97](https://github.com/niklas97) for raising this.
* [#968] Bugfix: Avoid creating user object in Help constructor. Thanks to [Immueggpain](https://github.com/Immueggpain) for raising this.
* [#990] Bugfix: Options in subcommands were not reset to their initial value between invocations when the `CommandLine` object is reused. Thanks to [marinier](https://github.com/marinier) for [pointing this out](https://stackoverflow.com/questions/61191211).
* [#984][#997] Bugfix: Parameters heading is now shown in the usage help message when `@filename` is the only parameter. Thanks to [Wycer](https://github.com/Wycers) for the pull request.
* [#1004] Bugfix: Prevent `NullPointerException` in `IParameterConsumer` with `@Option` in `@ArgGroup`. Thanks to [masupilami](https://github.com/masupilami) for raising this.
* [#988][#1002] Bugfix: Option group sections in the usage help message now include subgroup options. Thanks to [Wycer](https://github.com/Wycers) for the pull request.
* [#957] Bugfix: Debug tracing now shows variable value instead of variable name.
* [#955] Bugfix: TargetInvocationMessage handling in `MethodBinding.set` methods should use `getTargetException` not `getCause`; better error reporting.
* [#1007] Bugfix: Custom Type Converters are missing for repeated subcommands. Thanks to [Bastian Diehl](https://github.com/diba1013) for raising this.
* [#1026] Bugfix: Hidden options should not impact usage help.
* [#1034] Bugfix: Writer should `flush()` in `UnmatchedArgumentException.printSuggestions`. Thanks to [darkmoonka](https://github.com/darkmoonka) for raising this.
* [#963] DOC: Fixed broken link in README. Thanks to [vladimirf7](https://github.com/vladimirf7) for the pull request.
* [#895] DOC: Added [Initialization Before Execution](https://picocli.info/#_initialization_before_execution) section on initialization with subcommands to the user manual. Thanks to [Walter Scott Johnson](https://github.com/li-wjohnson) for raising this.
* [#951] DOC: Fixed typo in `picocli-codegen` annotation processor documentation: `disable.resource.config` is correct (the option name was incorrectly spelled as `disable.resources.config`). Thanks to [Max Rydahl Andersen](https://github.com/maxandersen) for raising this.
* [#966] DOC: Add section about Testing to the user manual.
* [#973] DOC: Update documentation for using the `picocli-codegen` annotation processor during the build with Kotlin.
* [#972] DOC: Add section "Handling Invalid Input" for custom type converters to user manual, demonstrating `TypeConversionException`. Add example `InetSocketAddressConverter` to `picocli-examples`. Thanks to [Simon](https://github.com/sbernard31) for raising this.
* [#975] DOC: Update user manual [Annotation Processor](https://picocli.info/#_enabling_the_annotation_processor) section to use `${project.groupId}` instead of deprecated `${groupId}`. Thanks to [Dmitry Timofeev](https://github.com/dmitry-timofeev) for the pull request.
* [#976] DOC: Update user manual Testing section; add subsection on [Testing Environment Variables](file:///C:/Users/remko/IdeaProjects/picocli3/build/docs/html5/index.html#_testing_environment_variables). Thanks to [David M. Carr](https://github.com/davidmc24) for raising this and providing a [sample project](https://github.com/remkop/picocli/files/4359943/bulk-scripts-public.zip).
* [#979][#981] DOC: Update user manual: add section [Options with an Optional Parameter](https://picocli.info/#_options_with_an_optional_parameter). Thanks to [razvanh](https://github.com/razvanh), [Jake](https://github.com/kyeo138) and [mohdpasha](https://github.com/mohdpasha) for raising this.
* [#989] DOC: Update examples for `picocli-shell-jline3` prior to and after the [#987][#969] bugfix. Thanks to [Ralf D. Müller](https://github.com/rdmueller) for raising this.
* [#998] DOC: Update manual: quote option parameter containing pipe characters in `split` regex for FIX message example. Thanks to [Galder Zamarreño](https://github.com/galderz) and [Max Rydahl Andersen](https://github.com/maxandersen) for raising this and subsequent discussion.
* [#1012] DOC: Update user manual: add to ArgGroup limitations. Thanks to [masupilami](https://github.com/masupilami) and [patric-r](https://github.com/patric-r) for raising this and subsequent discussion.
* [#1015] DOC: Update user manual: added section Variable Arity Options and Unknown Options. Thanks to [Chris Smowton](https://github.com/smowton) for raising this.
* [#1019] DOC: Fix PrintExceptionMessageHandler example. Thanks to [Adam Hosman](https://github.com/hosmanadam) for the pull request.
* [#1006] DOC: Add Mixin example: MyLogger to the user manual.
* [#1028][#1031] DOC: Update user manual: added Java 15 text blocks example. Thanks to [Enderaoe](https://github.com/Lyther) for the pull request.
* [#1037] DOC: Update user manual for programmatic API: fix typo. Thanks to [Yoshida](https://github.com/grimrose) for the pull request.
* [#1041] DOC: Fix broken links in javaDoc. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#1033] TEST: Added tests for [#984][#997]. Thanks to [WU Jiangning](https://github.com/licia-tia) for the pull request.
* [#965] Dependency Upgrade: in `picocli-examples`, bump `hibernate-validator` from 6.0.2 to 6.1.2 to deal with [CVE-2019-10219](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2019-10219). Thanks to [https://github.com/Security3rd](Security3rd) for raising this.

## <a name="4.3.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.3.0-breaking-changes"></a> Potential breaking changes

Behaviour has changed for some cases involving positional parameters.
One example is applications that define multiple positional parameters without an explicit `index` (see next section).
I hope these are edge cases.
Other than that, some error messages and details of the usage help message have changed.
See details below.

### Default index for single-value positional parameters

Prior to picocli 4.3.0, if your application defines any single-value positional parameters without explicit `index`, these parameters would all point to index zero.
From picocli 4.3.0, picocli automatically assigns an index, so the first such parameter gets index `0` (zero), the next parameter gets index `1` (one), the next parameter gets index `2` (two), etc.

This may break applications that have multiple single-value positional parameters without explicit `index`, that expect to capture the first argument in all of these parameters.

### Different error when user specifies too many parameters

The error message has changed when a user specifies more positional parameters than the program can accept. For example:

```java
class SingleValue {
    @Parameters String str;
}
```

This program only accepts one parameter. What happens when this program is invoked incorrectly with two parameters, like this:

```java
java SingleValue val1 val2
```

Before this release, picocli would throw an `OverwrittenOptionException` with message `"positional parameter at index 0..* (<str>) should be specified only once"`.

From picocli 4.3, picocli throws an `UnmatchedArgumentException` with message `"Unmatched argument at index 1: 'val2'"`.

This may break applications that have error handling that depends on an `OverwrittenOptionException` being thrown.

### Different mechanism for dealing with too many parameters

Continuing with the previous example, before this release, applications could deal with this by allowing single-value options to be overwritten:

```java
// before
CommandLine cmd = new CommandLine(new SingleValue());
cmd.setOverwrittenOptionsAllowed(true);
// ...
```

From picocli 4.3, applications need to allow unmatched arguments instead:

```java
// after
CommandLine cmd = new CommandLine(new SingleValue());
cmd.setUnmatchedArgumentsAllowed(true);
// ...
// get the invalid values
cmd.getUnmatchedArguments();
```

### Usage help message for single-value positional parameters
Before picocli 4.3.0, single-value positional parameters would incorrectly show an ellipsis (`...`) after their parameter label. This ellipsis is incorrect because it indicates that multiple values can be specified. The ellipsis is no longer shown for single-value positional parameters from picocli 4.3.0.

Before:

```
Usage: <main class> PARAM...
      PARAM...   Param description.
```

After:

```
Usage: <main class> PARAM
      PARAM   Param description.
```

This may break application tests that expect a specific usage help message format.

### Different error for missing required options or parameters

#### Missing options list now starts with colon, no more square brackets
Before:

```
Missing required option '--required=<required>'
Missing required options [-a=<first>, -b=<second>, -c=<third>]
```

After:

```
Missing required option: '--required=<required>'
Missing required options: '-a=<first>', '-b=<second>', '-c=<third>'
```

#### Better message when both options and positional parameters are missing
Before:

```
Missing required options [-x=<x>, params[0]=<p0>, params[1]=<p1>]
```

After:

```
Missing required options and parameters: '-x=<x>', '<p0>', '<p1>'
```

#### Missing positional parameters are now quoted
Before:

```
Missing required parameter: <mandatory>
Missing required parameters: <mandatory>, <anotherMandatory>
```

After:

```
Missing required parameter: '<mandatory>'
Missing required parameters: '<mandatory>', '<anotherMandatory>'
```


# <a name="4.2.0"></a> Picocli 4.2.0
The picocli community is pleased to announce picocli 4.2.0.

This release adds support for Repeatable Subcommands: when a command is marked as `@Command(subcommandsRepeatable = true)` it becomes possible to specify that command's subcommands multiple times on the command line.

The `picocli-codegen` module can now generate AsciiDoc documentation for picocli-based applications. AsciiDoc is a lightweight markup language that can easily can be converted to unix man pages, HTML and PDF with the wonderful [asciidoctor](https://asciidoctor.org/docs/user-manual/#man-pages) tool.

From this release, subcommands are not instantiated until they are matched on the command line. This should improve the startup time for applications with subcommands that do a lot of initialization when they are instantiated.

Autocompletion improvements: from this release the generated bash completions scripts support completing positional parameters, and are implemented without the use of associative arrays (so they should work on MacOS or other systems that use older versions of bash).
Additionally there are now automated tests using Expect to verify that the generated completion scripts work as expected.

GraalVM configuration generation improvement: added `--factory` option to `ReflectionConfigGenerator`, `ResourceConfigGenerator` and `DynamicProxyConfigGenerator`.
This makes it possible to generate configurations for command classes without a default no-arg constructor.

From this release it is possible to inject the `CommandSpec` into a `IVersionProvider`, making it easier to write version provider implementations that are reusable across multiple commands or even applications.

Similarly, from this release it is possible to inject the parent command object into mixins via a `@ParentCommand`-annotated field.

This release adds programmatic API to allow the long options column to grow larger than 20 characters in the usage help message via the `CommandLine::setLongOptionsMaxWidth` and `UsageMessageSpec::longOptionsMaxWidth` methods.


Finally, it is now possible let the usage help show that [@-files](https://picocli.info/#AtFiles) are supported by listing a `@<filename>` entry above the list of positional parameters in the usage help.

This is the sixty-seventh public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.2.0-toc"></a> Table of Contents
* [New and noteworthy](#4.2.0-new)
  * [Generate AsciiDoc Documentation for Your Application](#4.2.0-generate-docs)
  * [Repeatable Subcommands](#4.2.0-repeatable-subcommands)
  * [Inject `CommandSpec` into a `IVersionProvider`](#4.2.0-versionprovider-with-spec)
  * [Subcommands are now lazily instantiated](#4.2.0-lazy-instantiation)
  * [Mixins with `@ParentCommand`-annotated fields](#4.2.0-mixins)
  * [Showing `@filename` in usage help](#4.2.0-atfiles-usage)
  * [Configurable long options column width](#4.2.0-long-options-width)
* [Fixed issues](#4.2.0-fixes)
* [Deprecations](#4.2.0-deprecated)
* [Potential breaking changes](#4.2.0-breaking-changes)

## <a name="4.2.0-new"></a> New and Noteworthy

### <a name="4.2.0-generate-docs"></a> Generate AsciiDoc Documentation for Your Application (convertable to unix man pages, HTML and PDF)
This release adds a new class `picocli.codegen.docgen.manpage.ManPageGenerator` to the `picocli-codegen` module that generates AsciiDoc documentation for picocli-based applications using the `manpage` doctype and manpage document structure.
The generated AsciiDoc files can be converted to HTML, PDF and unix man pages with the [asciidoctor](https://asciidoctor.org/docs/user-manual/#man-pages) tool.

The [`picocli-codegen` README](https://github.com/remkop/picocli/blob/main/picocli-codegen/README.adoc) has more details.

### <a name="4.2.0-repeatable-subcommands"></a> Repeatable Subcommands
From picocli 4.2, it is possible to specify that a command's subcommands can be specified multiple times by marking it with `@Command(subcommandsRepeatable = true)`.

#### Example
Below is an example where the top-level command `myapp` is marked as `subcommandsRepeatable = true`.
This command has three subcommands, `add`, `list` and `send-report`:

```java
@Command(name = "myapp", subcommandsRepeatable = true)
class MyApp implements Runnable {

    @Command
    void add(@Option(names = "-x") String x, @Option(names = "-w") double w) { ... }

    @Command
    void list(@Option(names = "--where") String where) { ... }

    @Command(name = "send-report")
    void sendReport(@Option(names = "--to", split = ",") String[] recipients) { ... }

    // ...
}
```

The above example command allows users to specify one or more of its subcommands multiple time. For example, this would be a valid invocation:

```bash
myapp add -x=item1 -w=0.2 \
      add -x=item2 -w=0.5 \
      add -x=item3 -w=0.7 \
      list --where "w>0.2" \
      send-report --to=recipient@myorg.com
```

In the above command line invocation, the `myapp` top-level command is followed by its subcommand `add`.
Next, this is followed by another two occurrences of `add`, followed by `list` and `send-report`.
These are all "sibling" commands, that share the same parent command `myapp`.
This invocation is valid because `myapp` is marked with `subcommandsRepeatable = true`.

#### Repeatable Subcommands Specification

Normally, `subcommandsRepeatable` is `false`, so for each command, only one of its subcommands can be specified, potentially followed by only one sub-subcommand of that subcommand, etc.
In mathematical terms, a valid sequence of commands and subcommands can be represented by a _directed rooted tree_ that starts at the top-level command. This is illustrated by the diagram below.

![subcommands not repeatable](https://picocli.info/images/subcommands-non-repeatable.png)

When `subcommandsRepeatable` is set to `true` on a command, the subcommands of this command may appear multiple times.
Also, a subcommand can be followed by a "sibling" command (another command with the same parent command).

In mathematical terms, when a parent command has this property, the additional valid sequences of its subcommands form a fully connected subgraph (_a complete digraph_).

The blue and green dotted arrows in the diagram below illustrate the additional sequences that are allowed when a command has repeatable subcommands.

![subcommands-repeatable](https://picocli.info/images/subcommands-repeatable2.png)


Note that it is not valid to specify a subcommand followed by its parent command:

```bash
# invalid: cannot move _up_ the hierarchy
myapp add -x=item1 -w=0.2 myapp
```

### <a name="4.2.0-lazy-instantiation"></a> Subcommands are now lazily instantiated

From this release,  subcommands are not instantiated until they are matched on the command line,
unless the user object has a `@Spec` or `@ParentObject`-annotated field; these are instantiated during initialization.


### <a name="4.2.0-versionprovider-with-spec"></a> Injecting `CommandSpec` Into a `IVersionProvider`

From this release, `IVersionProvider` implementations can have `@Spec`-annotated fields. If such a field
exists, picocli will inject the `CommandSpec` of the command that uses this version provider.
This gives the version provider access to the full command hierarchy,
and may make it easier to implement version providers that can be reused among multiple commands.

For example:

```java
class MyVersionProvider implements IVersionProvider {
    @Spec CommandSpec spec;

    public String[] getVersion() {
        return new String[] { "Version info for " + spec.qualifiedName() };
    }
}
```

### <a name="4.2.0-atfiles-usage"></a> Showing `@filename` in usage help

From picocli 4.2, an entry for `@<filename>` can be shown in the options and parameters list of the usage help message of a command with the `@Command(showAtFileInUsageHelp = true)` annotation.

Example:

```java
@Command(name = "myapp", showAtFileInUsageHelp = true,
        mixinStandardHelpOptions = true, description = "Example command.")
class MyApp {
    @Parameters(description = "A file.") File file;
}
```

The usage help message for this command looks like this:

```
Usage: myapp [-hV] [@<filename>...] <file>
Example command.
      [@<filename>...]   One or more argument files containing options.
      <file>             A file.
  -h, --help             Show this help message and exit.
  -V, --version          Print version information and exit.
```

By default, the `@<filename>` entry is shown before the positional parameters in the synopsis as well as in the parameters list.
This can be changed with the Help API for [reordering sections](https://picocli.info/#_reordering_sections).

Both the label and the description of the `@<filename>` entry have been defined with [custom variables](https://picocli.info/#_custom_variables), to allow applications to change the text. The variables are:

* `picocli.atfile.label`
* `picocli.atfile.description`

By setting the above variables in either system properties, environment variables or the [resource bundle](https://picocli.info/#_internationalization) for a command, the text can be customized.

See the [user manual](https://picocli.info/#_show_at_files) for examples.

### <a name="4.2.0-mixins"></a> Mixins with `@ParentCommand`-annotated fields

A common use case is sharing options between different levels of the command hierarchy, so that "global" options from the top-level command are also available on subcommands.

Since picocli 4.2, [`@ParentCommand`-annotated](https://picocli.info/#_parentcommand_annotation) fields can be used in mixins, which makes this easier. See the [Use Case: Sharing Options](https://picocli.info/#_use_case_sharing_options) section of the user manual for a full example.

For mixins that need to be reusable across more than two levels in the command hierarchy, injecting a [`@Spec`-annotated](https://picocli.info/#_spec_annotation) field gives the mixin access to the full command hierarchy.

### <a name="4.2.0-long-options-width"></a> Configurable long options column width

The default layout shows short options and long options in separate columns, followed by the description column.
The width of the long options column shrinks automatically if all long options are very short,
but by default this column does not grow larger than 20 characters.

If the long option with its option parameter is longer than 20 characters
(for example: `--output=<outputFolder>`), the long option overflows into the description column, and the option description is shown on the next line.

This (the default) looks like this:

```
Usage: myapp [-hV] [-o=<outputFolder>]
  -h, --help      Show this help message and exit.
  -o, --output=<outputFolder>
                  Output location full path.
  -V, --version   Print version information and exit.
```

From picocli 4.2, there is programmatic API to change this via the `CommandLine::setLongOptionsMaxWidth` and `UsageMessageSpec::longOptionsMaxWidth` methods.

In the above example, if we call `commandLine.setLongOptionsMaxWidth(23)` before printing the usage help, we get this result:

```
Usage: myapp [-hV] [-o=<outputFolder>]
  -h, --help                    Show this help message and exit.
  -o, --output=<outputFolder>   Output location full path.
  -V, --version                 Print version information and exit.
```

## <a name="4.2.0-fixes"></a> Fixed issues
* [#454] API: Added support for repeatable subcommands. Thanks to [Idan Arye](https://github.com/idanarye), [Miroslav Kravec](https://github.com/kravemir), [Philipp Hanslovsky](https://github.com/hanslovsky) and [Jay](https://github.com/lakemove) for raising this and the subsequent discussion.
* [#629] API: Support injecting `@Spec CommandSpec spec` into `IVersionProvider` implementations. Thanks to [Garret Wilson](https://github.com/garretwilson) for raising this.
* [#795] API: Added `@Command(showAtFileInUsageHelp=true)` attribute to show `@filename` in usage help.
* [#925] API: Support `@ParentCommand`-annotated fields in mixin classes.
* [#936] API: Change visibility of `Help.subcommands()` method from protected to public.
* [#459] API: Generate manpage documentation. Thanks to [Miroslav Kravec](https://github.com/kravemir) for raising this. The `picocli-codegen` module can now generate AsciiDoc documentation that uses the `manpage` doctype and adheres to the manpage document structure so it can be converted to unix man pages in troff format with the [asciidoctor](https://asciidoctor.org/docs/user-manual/#man-pages) tool.
* [#299] API: Generate AsciiDoc documentation. Thanks to [Philippe Charles](https://github.com/charphi) for raising this.
         Added a new class `picocli.codegen.docgen.manpage.ManPageGenerator` to the `picocli-codegen` module that generates AsciiDoc documentation using the `manpage` doctype and manpage document structure.
         Custom markup like `@|bold mytext|@`, `@|italic mytext|@` etc., originally intended to be converted to ANSI escape codes, can from this release also be converted to custom markup like `<b>mytext</b>` and `<i>mytext</i>` in HTML, or `*mytext*` and `_mytext_` in lightweight markup languages like AsciiDoc.
         Applications can control this by setting a `ColorScheme` with a custom markup map.
         This ticket resulted in the following additional methods: `ColorScheme::text`, `ColorScheme::string`, `ColorScheme::customMarkupMap`, `ColorScheme::parse`, `ColorScheme::resetStyle`, `ColorScheme::apply`,  `ColorScheme.Builder::customMarkupMap` (getter and setter) and a new `picocli.CommandLine.Help.Ansi.Text(String, ColorScheme)` constructor.
         The `picocli.CommandLine.Help.Ansi::apply` method is now deprecated in favor of `ColorScheme::apply`.
* [#906] Auto-completion: Added automated tests for picocli-generated bash/zsh completion scripts.
* [#468][#505][#852] Auto-completion: added support for positional parameter completion. Thanks to [Serhii Avsheniuk](https://github.com/avshenuk) for the pull request.
* [#644][#671] Auto-completion: fix [shellcheck](https://github.com/koalaman/shellcheck) warnings in generated autocompletion scripts. Thanks to [Dylan Cali](https://github.com/calid) for raising this, and thanks to [AlcaYezz](https://github.com/AlcaYezz) for the pull request.
* [#396] Auto-completion: completion scripts no longer use associative arrays, and should now work on OSX.
* [#934] Enhancement: Make long options column width configurable. Thanks to [tomerz90](https://github.com/tomerz90) for raising this.
* [#930] Enhancement: Add `--factory` option to `ReflectionConfigGenerator`, `ResourceConfigGenerator` and `DynamicProxyConfigGenerator`. Thanks to [Santiago Acosta](https://github.com/hanzo2001) for raising this.
* [#690] Enhancement: Postpone instantiating subcommands until they are matched on the command line. Thanks to [Daniel Breitlauch](https://github.com/danielBreitlauch) for raising this.
* [#942] Enhancement: Show at files in usage help for picocli built-in commands.
* [#941] Enhancement: Allow default values for predefined variables.
* [#926] Enhancement: Clarify debug trace output when adding aliases.
* [#928] Enhancement: Improve debug tracing: show command user object identity hashcode and prefix "Processing argument..." with argument index.
* [#920] Enhancement: Reduce `DEBUG` tracing noise if no resource bundle is set.
* [#946] Enhancement: Add `--exit` option to picocli codegen utilities.
* [#940] Bugfix: ArgGroups with a negatable option no longer cause a `NullPointerException` during initialization.
* [#938] Bugfix: ArgGroups validation now correctly reports an error when a required option is missing, while an optional subgroup is present. Thanks to [Trent Mohay](https://github.com/rain-on) for raising this.
* [#933] Bugfix: Incorrect error message when multiple exclusive groups are used. Thanks to [Mikaël Barbero](https://github.com/mbarbero) for raising this.
* [#905] Bugfix: non-public `@Command`-annotated methods without arguments caused a `IllegalAccessException`. From this release such methods no longer need to be public. Thanks to [David Connelly](https://github.com/dconnelly) for raising this.
* [#924] Bugfix: `CommandSpec.mixinAnnotatedElements` map should be initialized when discovering `@Mixin`-annotated fields and methods via reflection.
* [#937] Bugfix: `Text.getStyledChars` no longer incorrectly inserts ANSI escape chars into the next line prefix when lines are broken.
* [#945] Bugfix: `Text.substring` now leaves out `StyledSection` instances that do not apply.
* [#944] DOC: Fix typo in picocli user manual - remove unnecessary semicolon. Thanks to [ztbx](https://github.com/ztbx) for raising this.
* [#943] DOC: Create man pages for built-in picocli commands.
* [#929] DOC: Add [jbang](https://github.com/maxandersen/jbang) under [packaging](https://picocli.info/#_packaging_your_application) in the user manual.
* [#927] DOC: Show current picocli version in the Spring Boot section of the user manual. Thanks to [Christian Grobmeier](https://github.com/grobmeier) for the pull request.
* [#919] DOC: Added example Gradle project with Kotlin and Graal Native Image. Thanks to [OndrejMalek](https://github.com/OndrejMalek) for the pull request.
* [#918] DOC: Added more Scala examples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#916] DOC: Added Scala examples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#914] DOC: Added Java and Kotlin examples for using `ResourceBundle` to internationalize and localize your CLI app. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#915] DOC: Ensure Kotlin examples compile correctly. Thanks to [Andreas Deininger](https://github.com/deining) for the suggestion.
* [#913] DOC: Added more Java and Kotlin examples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#912] DOC: Fixed broken links in javadoc. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#911] DOC: Fixed syntax error in javadoc. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#909] DOC: User manual: minor fixes. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#908] DOC: Fix typo in user manual: Add missing closing curly bracket. Thanks to [Piotrek Żygieło](https://github.com/pzygielo) for the pull request.
* [#907] DOC: Updated and added Kotlin examples. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#910] Dependency Upgrade: Bump Spring Boot dependency to 2.2.2 to allow it to work under Java 13. Thanks to [Stéphane Vanacker](https://github.com/svanacker) for raising this.

## <a name="4.2.0-deprecated"></a> Deprecations
* The `picocli.CommandLine.Help.Ansi#apply` method has been deprecated in favor of the `picocli.CommandLine.Help.ColorScheme#apply` method.
* The `picocli.CommandLine.Help.TextTable forDefaultColumns(Ansi, int, int)` method has been deprecated in favor of the new `TextTable.forDefaultColumns(ColorScheme, int, int)` method.
* The `picocli.CommandLine.Help.TextTable forColumnWidths(Ansi, int...)` method has been deprecated in favor of the new `TextTable.forColumnWidths(ColorScheme, int...)` method.
* The `picocli.CommandLine.Help.TextTable forColumns(Ansi, Column...)` method has been deprecated in favor of the new `TextTable.forColumns(ColorScheme, Column...)` method.
* The `picocli.CommandLine.Help.TextTable constructor (Ansi, Column[])` has been deprecated in favor of the new `TextTable(ColorScheme, Column...)` constructor.


## <a name="4.2.0-breaking-changes"></a> Potential breaking changes
Annotated command objects are now not instantiated until the command is matched on the command line.
Previously all subcommands were instantiated when the top-level command's `CommandLine` was constructed.


# <a name="4.1.4"></a> Picocli 4.1.4
The picocli community is pleased to announce picocli 4.1.4.

This release contains a bugfix for GraalVM users, and minor documentation enhancements.

This release fixes a bug in the `picocli-codegen` annotation processor that generates an incorrect `reflect-config.json` file with invalid entries for inner classes of commands in the unnamed package, that are unnecessarily prefixed with a dot. This makes the GraalVM `native-image` generator fail with an error like "Class .Outer$Inner not found".



This is the sixty-sixth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.1.4-toc"></a> Table of Contents
* [New and noteworthy](#4.1.4-new)
* [Fixed issues](#4.1.4-fixes)
* [Deprecations](#4.1.4-deprecated)
* [Potential breaking changes](#4.1.4-breaking-changes)

## <a name="4.1.4-new"></a> New and Noteworthy


## <a name="4.1.4-fixes"></a> Fixed issues
[#903] Bugfix: `picocli-codegen` generates invalid reflect-config.json for classes in unnamed package.
[#902] DOC: replace deprecated `CommandLine.invoke()` function. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.

## <a name="4.1.4-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.1.4-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.




# <a name="4.1.3"></a> Picocli 4.1.3
The picocli community is pleased to announce picocli 4.1.3.

This release contains a bugfix for GraalVM users.

This release fixes a bug in the `picocli-codegen` annotation processor that generates an incorrect `reflect-config.json` file with duplicate entries for inner classes of a command, one with the standard class name and one with the canonical class name. This makes the GraalVM `native-image` generator fail with an error like "Class Outer.Inner not found".



This is the sixty-fifth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.1.3-toc"></a> Table of Contents
* [New and noteworthy](#4.1.3-new)
* [Fixed issues](#4.1.3-fixes)
* [Deprecations](#4.1.3-deprecated)
* [Potential breaking changes](#4.1.3-breaking-changes)

## <a name="4.1.3-new"></a> New and Noteworthy


## <a name="4.1.3-fixes"></a> Fixed issues
* [#901] Bugfix: `picocli-codegen` generated invalid reflect-config.json for inner classes.

## <a name="4.1.3-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.1.3-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.1.2"></a> Picocli 4.1.2
The picocli community is pleased to announce picocli 4.1.2.

This release contains bugfixes, improvements, and documentation enhancements.

This version of picocli requires JLine 3.13.2 or higher and adds a `PicocliCommands` class that provides command descriptions that can be displayed in the terminal status bar via the new JLine `TailTipWidgets` functionality.

The built-in `picocli.AutoComplete.GenerateCompletion` (`generate-completion`) subcommand now omits validation of mandatory options in the parent command.

"Hidden" subcommands and options are no longer shown as suggestions in unmatched argument usage help or autocompletion scripts.

From picocli 4.1.2, all options in an exclusive group are automatically considered required, even if they are not marked as `required = true` in the annotations. Applications using older versions of picocli should mark all options in exclusive groups as required.




This is the sixty-fourth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.1.2-toc"></a> Table of Contents
* [New and noteworthy](#4.1.2-new)
* [Fixed issues](#4.1.2-fixes)
* [Deprecations](#4.1.2-deprecated)
* [Potential breaking changes](#4.1.2-breaking-changes)

## <a name="4.1.2-new"></a> New and Noteworthy

### <a name="4.1.2-jline3"></a> JLine3
JLine has had some interesting improvements in its 3.12 release.

This version of picocli requires JLine 3.13.2 or higher and adds a `PicocliCommands` class that provides command descriptions that can be displayed in the terminal status bar via the new JLine `TailTipWidgets` functionality.

See the `picocli-shell-jline3` [README](https://github.com/remkop/picocli/tree/main/picocli-shell-jline3) for details.

### <a name="4.1.2-completion"></a> Completion
The built-in `picocli.AutoComplete.GenerateCompletion` (`generate-completion`) subcommand now omits validation of mandatory options in the parent command.

Also, "hidden" subcommands and options are no longer shown as suggestions in unmatched argument usage help or autocompletion scripts.


## <a name="4.1.2-fixes"></a> Fixed issues
* [#888] (API) Added new `PicocliCommands` class to `picocli-shell-jline3` module; bumped `JLine` to 3.13.2. Thanks to [mattirn](https://github.com/mattirn) for the pull request.
* [#884] (Bugfix) Built-in `picocli.AutoComplete.GenerateCompletion` (`generate-completion`) subcommand now omits validation of mandatory options in the parent command. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#887] (Bugfix) "Hidden" subcommands and options are no longer shown as suggestions in unmatched argument usage help or autocompletion scripts. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#871] (Bugfix) All options in an exclusive group are now automatically considered `required`, to prevent unexpected results when mixing required and non-required options in exclusive ArgGroups. Thanks to [W Scott Johnson](https://github.com/wjohnson5) for raising this.
* [#883] (DOC) Update of Quick Guide. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#889][#885] (DOC) Update of Picocli Programmatic API documentation. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#891] (DOC) Fixed broken links in README. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#892] (DOC) Minor improvements to example app in `picocli-shell-jline3`.

## <a name="4.1.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.1.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.1.1"></a> Picocli 4.1.1
The picocli community is pleased to announce picocli 4.1.1.

This release contains bugfixes, and documentation enhancements.



This is the sixty-third public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.1.1-toc"></a> Table of Contents
* [New and noteworthy](#4.1.1-new)
* [Fixed issues](#4.1.1-fixes)
* [Deprecations](#4.1.1-deprecated)
* [Potential breaking changes](#4.1.1-breaking-changes)

## <a name="4.1.1-new"></a> New and Noteworthy



## <a name="4.1.1-fixes"></a> Fixed issues
* [#880] (Bugfix) Built-in `picocli.AutoComplete.GenerateCompletion` (`generate-completion`) subcommand does not flush, resulting in no output. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#875] (DOC) Fix broken internal links in RELEASE-NOTES for 4.1. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#881] (DOC) Update of Quick Guide to the latest `execute` API. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#874] (DOC) Fix Javadoc issues. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.


## <a name="4.1.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.1.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.1.0"></a> Picocli 4.1.0
The picocli community is pleased to announce picocli 4.1.0.

This release contains bugfixes, and enhancements.

The library now provides functionality that previously required custom code:

[PropertiesDefaultProvider](#4.1.0-propertiesdefaultprovider) - this release includes a built-in default provider allows end users to maintain their own default values for options and positional parameters, which may override the defaults that are hard-coded in the application.

[AutoComplete.GenerateCompletion](#4.1.0-completion) - this release includes a built-in `generate-completion` subcommand that end users can use to easily install Bash/ZSH completion for your application.

[Help API improvements](#4.1.0-helpapi) make it even easier to add custom sections to the usage help message.

This release also includes various bug fixes for [ArgGroups](#4.1.0-arggroups), which were first introduced in picocli 4.0, and are still maturing.

This is the sixty-second public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.1.0-toc"></a> Table of Contents
* [New and noteworthy](#4.1.0-new)
* [Fixed issues](#4.1.0-fixes)
* [Deprecations](#4.1.0-deprecated)
* [Potential breaking changes](#4.1.0-breaking-changes)

## <a name="4.1.0-new"></a> New and Noteworthy


###  <a name="4.1.0-propertiesdefaultprovider" id="4.1.0-propertiesdefaultprovider"></a> PropertiesDefaultProvider
From picocli 4.1, applications can use the built-in `PropertiesDefaultProvider` implementation that loads default values from a properties file.

By default, this implementation tries to find a properties file named `.${COMMAND-NAME}.properties` in the user home directory, where `${COMMAND-NAME}` is the name of the command. If a command has aliases in addition to its name, these aliases are also used to try to find the properties file. For example:

```java
import picocli.CommandLine.PropertiesDefaultProvider;
// ...
@Command(name = "git", defaultValueProvider = PropertiesDefaultProvider.class)
class Git { }
```

The above will try to load default values from `new File(System.getProperty("user.home"), ".git.properties")`.
The location of the properties file can also be controlled with system property `"picocli.defaults.${COMMAND-NAME}.path"` (`"picocli.defaults.git.path"` in this example), in which case the value of the property must be the path to the file containing the default values.

The location of the properties file may also be specified programmatically. For example:

```java
CommandLine cmd = new CommandLine(new MyCommand());
File defaultsFile = new File("path/to/config/mycommand.properties");
cmd.setDefaultValueProvider(new PropertiesDefaultProvider(defaultsFile));
cmd.execute(args);
```


#### PropertiesDefaultProvider Format
The `PropertiesDefaultProvider` expects the properties file to be in the standard java `.properties` https://en.wikipedia.org/wiki/.properties[format].

For options, the key is either the [descriptionKey](https://picocli.info/apidocs/picocli/CommandLine.Option.html#descriptionKey--), or the option's [longest name](https://picocli.info/apidocs/picocli/CommandLine.Model.OptionSpec.html#longestName--), without the prefix. So, for an option `--verbose`, the key would be `verbose`, and for an option `/F`, the key would be `F`.

For positional parameters, the key is either the [descriptionKey](https://picocli.info/apidocs/picocli/CommandLine.Parameters.html#descriptionKey--), or the positional parameter's [param label](https://picocli.info/apidocs/picocli/CommandLine.Parameters.html#paramLabel--).

End users may not know what the `descriptionKey` of your options and positional parameters are, so be sure  to document that with your application.

#### Subcommands Default Values

The default values for options and positional parameters of subcommands can be included in the properties file for the top-level command, so that end users need to maintain only a single file.
This can be achieved by prefixing the keys for the options and positional parameters with their command's qualified name.
For example, to give the  `git commit` command's `--cleanup` option a default value of `strip`, define a key of `git.commit.cleanup` and assign it a default value:

```
# /home/remko/.git.properties
git.commit.cleanup = strip
```


### <a name="4.1.0-completion"></a> AutoComplete.GenerateCompletion

This release adds a built-in `generate-completion` subcommand that generates a completion script for its parent command.

Example usage:

```java
@Command(name = "myapp",
        subcommands = picocli.AutoComplete.GenerateCompletion.class)
static class MyApp { //...
}
```

This allows users to install completion for the `myapp` command by running the following command:

```bash
source <(myapp generate-completion)
```

### Autocompletion script improvements

The generated completion script itself now enables bash completion in zsh.

That means it is no longer necessary to run the below commands in ZSH before sourcing the completion script:

```zsh
autoload -U +X compinit && compinit
autoload -U +X bashcompinit && bashcompinit
```


### <a name="4.1.0-helpapi"></a> Help API improvements
The new `Help.createHeading(String, Object...)` and  `Help.createTextTable(Map<?, ?>)` methods
 facilitate creating tabular custom Help sections.


The below example shows how to add a custom Environment Variables section to the usage help message.

```java
// help section keys
static final String SECTION_KEY_ENV_HEADING = "environmentVariablesHeading";
static final String SECTION_KEY_ENV_DETAILS = "environmentVariables";
// ...

// the data to display
Map<String, String> env = new LinkedHashMap<>();
env.put("FOO", "explanation of foo");
env.put("BAR", "explanation of bar");
env.put("XYZ", "xxxx yyyy zzz");

// register the custom section renderers
CommandLine cmd = new CommandLine(new MyApp());
cmd.getHelpSectionMap().put(SECTION_KEY_ENV_HEADING,
                            help -> help.createHeading("Environment Variables:%n"));
cmd.getHelpSectionMap().put(SECTION_KEY_ENV_DETAILS,
                            help -> help.createTextTable(env).toString());

// specify the location of the new sections
List<String> keys = new ArrayList<>(cmd.getHelpSectionKeys());
int index = keys.indexOf(CommandLine.Model.UsageMessageSpec.SECTION_KEY_FOOTER_HEADING);
keys.add(index, SECTION_KEY_ENV_HEADING);
keys.add(index + 1, SECTION_KEY_ENV_DETAILS);
cmd.setHelpSectionKeys(keys);
```

There are also new convenience methods `Help.fullSynopsis()` and `CommandLine.getHelp()`.

### <a name="4.1.0-arggroups"></a> ArgGroup improvements

* ArgGroups with `@Option`-annotated methods no longer fail with `NullPointerException`
* ArgGroups now match multiple occurrences of a multi-value `@Option` in the same group instance, and don't create a new group for each occurrence
* ArgGroups now don't validate when marked as `validate = false`
* ArgGroups now correctly validate that required options are present
* Non-validating ArgGroups are now automatically set to be non-exclusive

## <a name="4.1.0-fixes"></a> Fixed issues
* [#841] (API) Add `JniConfigGenerator` to `picocli-codegen` module.
* [#865] (API) Add `Help.createHeading(String, Object...)` and  `Help.createTextTable(Map<?, ?>)` to facilitate creating tabular custom Help sections.
* [#829] (Bugfix) `@ArgGroup` with `@Option`-annotated methods fail with `NullPointerException`. Thanks to [A2 Geek](https://github.com/a2geek) for raising this.
* [#828] (Bugfix/enhancement) Subcommands should not be parsed as option values for options with optional parameters. Thanks to [Martin Paljak](https://github.com/martinpaljak) for raising this.
* [#811] (Bugfix) `CommandLine.setResourceBundle` did not propagate resource bundle to subcommands recursively. Thanks to [thope](https://github.com/frontfact) for the pull request with the bug fix.
* [#850] (Bugfix) `@Mixin`-annotated fields were not included in `reflect-config.json` by `picocli-codegen` annotation processor. Thanks to [Nikolaos Georgiou](https://github.com/ngeor) for raising this.
* [#826] (Enhancement) Suppress compiler warning "Supported source version 'RELEASE_6' from annotation processor... less than -source..." in picocli-codegen.
* [#815] (Enhancement) `@ArgGroup` should match multiple occurrences of a multi-value `@Option` in the same group instance, not create new group for each occurrence. Thanks to [kacchi](https://github.com/kacchi) for raising this.
* [#810] (Bugfix) `@ArgGroup` should not validate when marked as `validate = false`. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#870] (Bugfix) Required options were not validated when mixing required and non-required options in an ArgGroup. Thanks to [W Scott Johnson](https://github.com/wjohnson5) for raising this.
* [#868] (Enhancement) Add built-in default value provider implementation `PropertiesDefaultProvider` that loads default values from properties file in home directory or specified location.
* [#809] (Enhancement) Add built-in `generate-completion` subcommand that generates a completion script for its parent command. Thanks to [George Gastaldi](https://github.com/gastaldi) for the suggestion.
* [#836] (Enhancement) Add convenience methods `Help.fullSynopsis()` and `CommandLine.getHelp()`.
* [#833] (Enhancement) Non-validating ArgGroups are now automatically set to be non-exclusive. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
* [#830] (Enhancement) Enum constants can now be matched by their `toString()` as well as their `name()`. Improved error reporting. Thanks to [Henning Makholm](https://github.com/hmakholm) for the pull request.
* [#846] (Enhancement) Allow value `tty` for system property `picocli.ansi`: force picocli to emit ANSI escape characters if the process is using an interactive console.
* [#772] (Enhancement) Generated completion script should enable bash completion in zsh. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for raising this.
* [#480] (DOC) Added a [Handling Errors](https://picocli.info#_handling_errors) subsection to the Executing Commands section of the user manual to show how to customize how your application deals with invalid input or business logic exceptions.
* [#813] (DOC) Clarify usage of negatable boolean `@Option` with default value "true". Thanks to [Yann ROBERT](https://github.com/YannRobert) for raising this.
* [#814] (DOC) Document how a CLI application can be packaged for distribution.
* [#820] (DOC) Update user manual section on ANSI supported platforms: mention Windows Subsystem for Linux under Windows 10.
* [#819] (DOC) Update user manual section on Variable Interpolation: improve example, link to this section from other parts of the manual.
* [#818] (DOC) Update user manual section on Usage Help API to point to `picocli-examples`.
* [#816] (DOC) Update user manual for `IHelpCommandInitializable2`.
* [#817] (DOC) Update user manual section on Subcommands for the execute API.
* [#809] (DOC) Output completion script as a subcommand of the command itself. Thanks to [George Gastaldi](https://github.com/gastaldi) for the suggestion.
* [#456] (DOC) Integrate completion generation into command itself. Thanks to [jvassev](https://github.com/jvassev) for the suggestion.
* [#835] (DOC) Document how to create ANSI colored output in an application.
* [#847] (DOC) Improve documentation for ANSI colors in Windows.
* [#844] (DOC) Clarify when `@ArgGroup`-annotated fields are initialized in user manual.
* [#860] (DOC) Fix broken link to GraalVM Native Image section. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#861] (DOC) Fix broken link to `ShowCommandHierarchy` example. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#864] (DOC) Fix code examples in documentation. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
* [#867] (DOC) Update user manual to clarify that the toggling behaviour is no longer the default from picocli 4.0. Thanks to [Linus Fernandes](https://github.com/Fernal73) for raising this.

## <a name="4.1.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.1.0-breaking-changes"></a> Potential breaking changes

* From picocli 4.1, subcommands will not be parsed as option values for options with optional parameters.
* Enum constants can now be matched by their `toString()` as well as their `name()`.


# <a name="4.0.4"></a> Picocli 4.0.4
The picocli community is pleased to announce picocli 4.0.4.

This release contains a bugfixes and enhancements.

GraalVM native image-configuration generation for options or positional parameters with custom type converters or custom parameter consumers now work correctly.

Also fixed a bug where validation was performed on `ArgGroup`s even when they were marked as `validate = false`.

This is the sixty-first public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.4"></a> Table of Contents
* [New and noteworthy](#4.0.4-new)
* [Fixed issues](#4.0.4-fixes)
* [Deprecations](#4.0.4-deprecated)
* [Potential breaking changes](#4.0.4-breaking-changes)

## <a name="4.0.4-new"></a> New and Noteworthy


## <a name="4.0.4-fixes"></a> Fixed issues
* [#803] (Bugfix) Custom `IParameterConsumer` caused native-image to fail. Thanks to [Patrick Plenefisch](https://github.com/byteit101) for raising this.
* [#804][#806] (Bugfix) Visit Parameter Consumers when doing GraalVM reflection generation; added test. Thanks to [Patrick Plenefisch](https://github.com/byteit101) for the pull requests.
* [#808] (Bugfix) Option-specific `ITypeConverter` class is now correctly included in generated `reflect-config.json`.
* [#807] (Bugfix) `ArgGroup` should not validate when marked as `validate = false`. Thanks to [cranphin](https://github.com/cranphin) for the bug report.
* [#799] (DOC) Update adoption section in README.
* [#805] (DOC) Add example for alphabetically sorting subcommands by subclassing `Help`. Thanks to []() for raising this issue.

## <a name="4.0.4-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.0.4-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.0.3"></a> Picocli 4.0.3
The picocli community is pleased to announce picocli 4.0.3.

This release contains a bugfixes and enhancements.

GraalVM native image-configuration generation for picocli commands with argument groups now work correctly.

This is the sixtieth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.3"></a> Table of Contents
* [New and noteworthy](#4.0.3-new)
* [Fixed issues](#4.0.3-fixes)
* [Deprecations](#4.0.3-deprecated)
* [Potential breaking changes](#4.0.3-breaking-changes)

## <a name="4.0.3-new"></a> New and Noteworthy


## <a name="4.0.3-fixes"></a> Fixed issues
* [#794] (Bugfix) Perform topological sort on ArgGroups in annotation processor before wiring up the model to prevent FATAL ERROR in annotation processor: picocli.CommandLine$InitializationException: ArgGroup has no options or positional parameters, and no subgroups.
* [#793] (Bugfix) Argument groups disappear in GraalVM native-image (the generated `reflect-config.json` was missing the `@ArgGroup`-annotated fields). Thanks to [Mike Hearn](https://github.com/mikehearn) for the bug report.
* [#787] (Enhancement) Throw `InitializationException` instead of `StackOverflowError` when subcommand is subclass of itself. Thanks to [Peter Murray-Rust](https://github.com/petermr) for raising this.
* [#784] (DOC) Update documentation to show custom `IFactory` implementations should fall back to the default factory to enable the creation of collections for `@Option`-annotated methods and fields.
* [#788] (DOC) Add link to GitHub examples in user manual Mixins section. Thanks to [Peter Murray-Rust](https://github.com/petermr) for the suggestion.
* [#789] (DOC) Add example usage help to the user manual Negatable Options section.

## <a name="4.0.3-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.0.3-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.




# <a name="4.0.2"></a> Picocli 4.0.2
The picocli community is pleased to announce picocli 4.0.2.

This release contains a bugfixes and enhancements.

This is the fifty-ninth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.2"></a> Table of Contents
* [New and noteworthy](#4.0.2-new)
* [Fixed issues](#4.0.2-fixes)
* [Deprecations](#4.0.2-deprecated)
* [Potential breaking changes](#4.0.2-breaking-changes)

## <a name="4.0.2-new"></a> New and Noteworthy


## <a name="4.0.2-fixes"></a> Fixed issues
- [#781] Bugfix: Standard help mixin options not added in source order when running on Java 12+.
- [#773] Bugfix: Add public `NativeImageConfigGeneratorProcessor` constructor to fix build error in IntelliJ IDEA 2019.2. Thanks to [Lukáš Petrovický](https://github.com/triceo) for raising this issue.
- [#779] Bugfix: `DuplicateOptionAnnotationsException` when a nested group is defined inside a mixin. Thanks to [Matteo Melli](https://github.com/teoincontatto) for the bug report.
- [#777] Bugfix: Codegen failed when command contains field with argGroup annotation. Thanks to [eomeara](https://github.com/eomeara) for the bug report.
- [#776] Bugfix: Argument groups in mixins were ignored. Thanks to [Matteo Melli](https://github.com/teoincontatto) for the bug report.
- [#780] (DOC) Fixed the provided flag usage in the `picocli-codegen` readme. Thanks to [Lasantha Kularatne](https://github.com/lasanthak) for the pull request.
- [#778] (DOC) Improve documentation for argument group sections in the help. Thanks to [Matteo Melli](https://github.com/teoincontatto) for raising this.
- [#774] (DOC) Add example demonstrating how to left-align long options in the usage help.
- [#766] (DOC) Update user manual: mention the dependency required for using `PicocliSpringFactory`. Thanks to [rome-legacy](https://github.com/rome-legacy) for the suggestion.
- [#775] (DOC) Document jline2 incompatibility with picocli's `interactive` options for passwords, and update the example to show a workaround. Thanks to [querqueq](https://github.com/querqueq) for the pull request.
- [#770][#771] (DOC) Improve example code in `picocli-spring-boot-starter` README. Thanks to [Stéphane Nicoll](https://github.com/snicoll) for the pull requests.

## <a name="4.0.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.0.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.0.1"></a> Picocli 4.0.1
The picocli community is pleased to announce picocli 4.0.1.

This release contains a fix for a bug in the annotation processor that causes a compilation error when a subcommand contains a `@Mixin`-annotated field or method.

This is the fifty-eighth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.1"></a> Table of Contents
* [New and noteworthy](#4.0.1-new)
* [Fixed issues](#4.0.1-fixes)
* [Deprecations](#4.0.1-deprecated)
* [Potential breaking changes](#4.0.1-breaking-changes)

## <a name="4.0.1-new"></a> New and Noteworthy


## <a name="4.0.1-fixes"></a> Fixed issues
- [#769] Annotation processor fails on subcommands with mixins. Thanks to [MortronMeymo](https://github.com/MortronMeymo) for the bug report.

## <a name="4.0.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.0.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="4.0.0"></a> Picocli 4.0.0 GA
The picocli community is pleased to announce picocli 4.0. This is a big release.

First, the `picocli-codegen` module now includes an [annotation processor](#4.0.0-annotation-processor) that instantly enables your JAR for GraalVM native images. It also gives compile-time errors for invalid annotations and attributes. We recommend that all projects using picocli enable this annotation processor.

The [`execute` API](#4.0.0-execute) is an easy way to execute your command with almost no code. It allows for more flexible configuration than previous APIs, and introduces much improved exit code support. This replaces the static methods `call` and `run`, as well as the `parseWithHandlers` methods, which are now deprecated.

Improved Spring support: the new [`picocli-spring-boot-starter` module](#4.0.0-spring-boot) includes a `PicocliSpringFactory` and auto-configuration to use Spring dependency injection in your picocli command line application. This is especially useful if your application contains subcommands.

The parser has been enhanced to handle [argument groups](#4.0.0-argument-groups): mutually exclusive options, mutually dependent options, and option sections in the usage help. What makes the picocli design unique and extremely powerful is that argument groups can be nested, so applications can define repeating composite groups of mutually exclusive or co-dependent options.

Annotation attributes can now contain [variables](#4.0.0-variable-expansion) that can be resolved as system properties, environment variables and resource bundle keys.

The picocli JAR is now an [explicit JPMS module](#4.0.0-jpms-module), as well as an OSGi bundle. As part of this change, the Groovy support classes and annotations have been moved to a separate [`picocli-groovy`](#4.0.0-groovy-module) artifact.

Boolean options can now easily be made [negatable](#4.0.0-negatable-options), which adds a "no-" version of the option. This is a common feature in command line parser libraries for Perl, PHP, Ruby, Lisp, Dart and Go, but we are not aware of any other Java libraries that support this.

All in all, this release contains 96 [bugfixes and improvements](#4.0.0-fixes) over picocli 3.9.6.


Many thanks to the following community contributors to this release of picocli:

[AkosCz](https://github.com/akoscz), [AlcaYezz](https://github.com/AlcaYezz), [Andreas Deininger](https://github.com/deining), [andrewbleonard](https://github.com/andrewbleonard), [Arturo Alonso](https://github.com/thefang12), [Bob Tiernay](https://github.com/bobtiernay-okta), [Devin Smith](https://github.com/devinrsmith), [feinstein](https://github.com/feinstein), [Garret Wilson](https://github.com/garretwilson), [Gerard Bosch](https://github.com/gerardbosch), [gitfineon](https://github.com/gitfineon), [jrevault](https://github.com/jrevault), [Judd Gaddie](https://github.com/juddgaddie), [Liam Esteban Prince](https://github.com/leliamesteban), [marinier](https://github.com/marinier), [Michael D. Adams](https://github.com/adamsmd), [Mikaël Barbero](https://github.com/mbarbero), [Mikusch](https://github.com/Mikusch), [Nicolas Mingo](https://github.com/nicolasmingo), [Paolo Di Tommaso](https://github.com/pditommaso), [Philipp Hanslovsky](https://github.com/hanslovsky), [Radu Cotescu](https://github.com/raducotescu), [Reinhard Pointner](https://github.com/rednoah), [Sebastian Thomschke](https://github.com/sebthom), [Shane Rowatt](https://github.com/srowatt), [shanetreacy](https://github.com/shanetreacy), [Steffen Rehberg](https://github.com/StefRe), [Sualeh Fatehi](https://github.com/sualeh), Takuya Ishibashi, [Thibaud Lepretre](https://github.com/kakawait) and [Warkdev](https://github.com/Warkdev).



This is the fifty-seventh public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.0"></a> Table of Contents
* [New and noteworthy](#4.0.0-new)
  * [Annotation processor](#4.0.0-annotation-processor)
  * [Execution API](#4.0.0-execute)
  * [Spring Boot support](#4.0.0-spring-boot)
  * [Argument groups](#4.0.0-argument-groups)
  * [Variable interpolation](#4.0.0-variable-expansion) in annotation attributes
  * Explicit [JPMS module](#4.0.0-jpms-module) and OSGi bundle
  * New [Groovy module](#4.0.0-groovy-module)
  * [Negatable options](#4.0.0-negatable-options)
  * [Fallback value](#4.0.0-fallback-values) for options
  * [Custom parameter processing](#4.0.0-parameterConsumer)
  * Improved parsing of [quoted parameters](#4.0.0-quoted-params)
  * [Auto-detect terminal width](#4.0.0-auto-width) for usage help
  * Improved support for [Chinese, Japanese and Korean](#4.0.0-cjk) usage help
* [Fixed issues](#4.0.0-fixes)
* [Deprecations](#4.0.0-deprecated)
  * [`run`, `call`, `invoke`, and `parseWithHandlers` methods](#4.0.0-deprecated-run-call) replaced by `execute`
  * [`CommandLine.setSplitQuotedStrings`](#4.0.0-deprecated-setSplitQuotedStrings) deprecated
  * [`parse`](#4.0.0-deprecated-parse) deprecated in favor of `parseArgs`
  * [Range public fields](#4.0.0-deprecated-range-public-fields)

* [Potential breaking changes](#4.0.0-breaking-changes)
  * `picocli.groovy` classes [moved to separate artifact](4.0.0-breaking-groovy)
  * [Split regex on single-value options is now disallowed](#4.0.0-breaking-split)
  * [ColorScheme is now immutable](#4.0.0-breaking-colorscheme)
  * [Boolean options no longer toggle](#4.0.0-breaking-toggle) by default
  * [ParseResult `matchedOptions`](#4.0.0-breaking-matchedOptions) now returns full list
  * [Error message for unmatched arguments](#4.0.0-breaking-unmatched-error) changed
  * [Option order](#4.0.0-breaking-option-order) changed
  * [Factory](#4.0.0-breaking-factory)

## <a name="4.0.0-new"></a> New and Noteworthy

### <a name="4.0.0-annotation-processor"></a> Annotation processor

<img src="https://www.graalvm.org/resources/img/logo-colored.svg" alt="GraalVM">

The `picocli-codegen` module now includes an annotation processor that instantly enables your JAR for GraalVM native images. The annotation processor can build a model from the picocli annotations at compile time rather than at runtime.

Use this if you’re interested in:
* **Compile time error checking**. The annotation processor shows errors for invalid annotations and attributes immediately when you compile, instead of during testing at runtime, resulting in shorter feedback cycles.
* **Graal native images**. The annotation processor generates and updates [Graal configuration](https://github.com/oracle/graal/blob/master/substratevm/BuildConfiguration.md) files under
`META-INF/native-image/picocli-generated/$project` during compilation, to be included in the application jar.
This includes configuration files for [reflection](https://github.com/oracle/graal/blob/master/substratevm/Reflection.md), [resources](https://github.com/oracle/graal/blob/master/substratevm/Resources.md) and [dynamic proxies](https://github.com/oracle/graal/blob/master/substratevm/DynamicProxy.md).
By embedding these configuration files, your jar is instantly Graal-enabled.
The `$project` location is configurable, see [processor options](#picocli-processor-options) below.
In most cases no further configuration is needed when generating a native image.


#### Processor option: `project`

The picocli annotation processor supports a number of [options](https://github.com/remkop/picocli/tree/main/picocli-codegen#picocli-processor-options), most important of which is the `project` option to control the output subdirectory: the generated files are written to `META-INF/native-image/picocli-generated/${project}`. A good convention is to use the Maven `${groupId}/${artifactId}` as the value; a unique subdirectory ensures your jar can be shaded with other jars that may also contain generated configuration files.

To configure this option, pass the `-Aproject=<some value>` to the javac compiler. The examples below show how to do this for Maven and Gradle.

#### Enabling the Annotation Processor

Since Java 6, annotation processing is part of the standard `javac` compiler, but many IDEs and build tools require something extra to enable annotation processing.

##### IDE
[This page](https://immutables.github.io/apt.html) shows the steps to configure Eclipse and IntelliJ IDEA to enable annotation processing.

##### Maven
In Maven, use `annotationProcessorPaths` in the `configuration` of the `maven-compiler-plugin`. This requires `maven-compiler-plugin` plugin version 3.5 or higher.

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <!-- annotationProcessorPaths requires maven-compiler-plugin version 3.5 or higher -->
  <version>${maven-compiler-plugin-version}</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>info.picocli</groupId>
        <artifactId>picocli-codegen</artifactId>
        <version>4.0.0</version>
      </path>
    </annotationProcessorPaths>
    <compilerArgs>
      <arg>-Aproject=${groupId}/${artifactId}</arg>
    </compilerArgs>
  </configuration>
</plugin>
```

See the [`picocli-codegen` README](https://github.com/remkop/picocli/tree/main/picocli-codegen) for more details.


##### Gradle
Use the `annotationProcessor` path in Gradle [4.6 and higher](https://docs.gradle.org/4.6/release-notes.html#convenient-declaration-of-annotation-processor-dependencies):
```
dependencies {
    compile 'info.picocli:picocli:4.0.0'
    annotationProcessor 'info.picocli:picocli-codegen:4.0.0'
}
```

To set an annotation processor option in Gradle, add these options to the `options.compilerArgs` list in the `compileJava` block.

```
compileJava {
    options.compilerArgs += ["-Aproject=${project.group}/${project.name}"]
}
```

See the [`picocli-codegen` README](https://github.com/remkop/picocli/tree/main/picocli-codegen) for more details.



### <a name="4.0.0-execute"></a> Execution API

<img src="https://picocli.info/images/executable.png" alt="executable commands">


Picocli 4.0 introduces new API to execute commands. Let’s take a quick look at what changed.

#### Exit Code
Many command line applications return an [exit code](https://en.wikipedia.org/wiki/Exit_status) to signify success or failure. Zero often means success, a non-zero exit code is often used for errors, but other than that, meanings differ per application.

The new `CommandLine.execute` method introduced in picocli 4.0 returns an `int`, and applications can use this return value to call `System.exit` if desired. For example:

```java
public static void main(String... args) {
  CommandLine cmd = new CommandLine(new App());
  int exitCode = cmd.execute(args);
  System.exit(exitCode);
}
```

Older versions of picocli had some limited exit code support where picocli would call `System.exit`, but this is now deprecated.

#### Generating an Exit Code

`@Command`-annotated classes that implement `Callable` and `@Command`-annotated methods can simply return an `int` or `Integer`, and this value will be returned from `CommandLine.execute`. For example:

```java
@Command(name = "greet")
class Greet implements Callable<Integer> {
  public Integer call() {
    System.out.println("hi");
    return 1;
  }

  @Command
  int shout() {
    System.out.println("HI!");
    return 2;
  }
}

assert 1 == new CommandLine(new Greet()).execute();
assert 2 == new CommandLine(new Greet()).execute("shout");
```

Commands with a user object that implements `Runnable` can implement the `IExitCodeGenerator` interface to generate an exit code. For example:

```java
@Command(name = "wave")
class Gesture implements Runnable, IExitCodeGenerator {
  public void run() {
    System.out.println("wave");
  }
  public int getExitCode() {
    return 3;
  }
}

assert 3 == new CommandLine(new Gesture()).execute();
```

#### Exception Exit Codes

By default, the `execute` method returns `CommandLine.ExitCode.USAGE` (`64`) for invalid input, and `CommandLine.ExitCode.SOFTWARE` (`70`) when an exception occurred in the Runnable, Callable or command method. (For reference, these values are `EX_USAGE` and `EX_SOFTWARE`, respectively, from Unix and Linux [sysexits.h](https://www.freebsd.org/cgi/man.cgi?query=sysexits&sektion=3)). This can be customized with the `@Command` annotation. For example:

```java
@Command(exitCodeOnInvalidInput = 123,
   exitCodeOnExecutionException = 456)
```

Additionally, applications can configure a `IExitCodeExceptionMapper` to map a specific exception to an exit code:

```java
class MyMapper implements IExitCodeExceptionMapper {
  public int getExitCode(Throwable t) {
    if (t instanceof FileNotFoundException) {
      return 74;
    }
    return 1;
  }
}
```

When the end user specified invalid input, the `execute` method prints an error message followed by the usage help message of the command, and returns an exit code. This can be customized by configuring a `IParameterExceptionHandler`.

If the business logic of the command throws an exception, the `execute` method prints the stack trace of the exception and returns an exit code. This can be customized by configuring a `IExecutionExceptionHandler`.


#### Configuration
The new `CommandLine.execute` method is an instance method. The older `run`, `call` and `invoke` methods are static methods. Static methods don’t allow configuration. The new API lets applications configure the parser or other aspects before execution. For example:

```java
public static void main(String... args) {
  CommandLine cmd = new CommandLine(new App());
  cmd.setCaseInsensitiveEnumValuesAllowed(true);
  cmd.setUnmatchedArgumentsAllowed(true);
  cmd.setStopAtPositional(true);
  cmd.setExpandAtFiles(false);
  cmd.execute(args);
}
```


### <a name="4.0.0-spring-boot"></a> Spring support

<img src="https://picocli.info/images/spring-boot.png" alt="spring and spring boot logos" width="350" height="167">

[Thibaud Lepretre](https://github.com/kakawait), the author of [kakawait/picocli-spring-boot-starter](https://github.com/kakawait/picocli-spring-boot-starter) has graciously contributed a pull request to the picocli project with a new `picocli-spring-boot-starter` module. This includes a `PicocliSpringFactory` and auto-configuration and makes it extremely easy to use Spring dependency injection in your picocli command line application.


#### Spring Boot example

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class MySpringApp implements CommandLineRunner, ExitCodeGenerator {
    private int exitCode;

    @Autowired
    IFactory factory; // auto-configured to inject PicocliSpringFactory

    @Autowired
    MyCommand myCommand; // your @picocli.CommandLine.Command-annotated class

    @Override
    public void run(String... args) {
        // let picocli parse command line args and run the business logic
        exitCode = new CommandLine(myCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        // let Spring instantiate and inject dependencies
        System.exit(SpringApplication.exit(SpringApplication.run(MySpringApp.class, args)));
    }
}
```

When your command is annotated with `@Component`, Spring can autodetect it for dependency injection.
The business logic of your command looks like any other picocli command with options and parameters.

```java
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;

@Component
@Command(name = "myCommand")
public class MyCommand implements Callable<Integer> {

    @Autowired
    private SomeService someService;

    // Prevent "Unknown option" error when users use
    // the Spring Boot parameter 'spring.config.location' to specify
    // an alternative location for the application.properties file.
    @Option(names = "--spring.config.location", hidden = true)
    private String springConfigLocation;

    @Option(names = { "-x", "--option" }, description = "example option")
    private boolean flag;

    public Integer call() throws Exception {
        // business logic here
        return 0;
    }
}
```

### <a name="4.0.0-argument-groups"></a> Argument groups
<img src="https://picocli.info/images/groups.jpg" alt="argument groups">

This release introduces a new `@ArgGroup` annotation and its `ArgGroupSpec` programmatic equivalent.

Argument Groups can be used to define:

* mutually exclusive options
* options that must co-occur (dependent options)
* option sections in the usage help message
* repeating composite arguments

To create a group using the annotations API, annotate a field or method with `@ArgGroup`.
The field's type refers to the class containing the options and positional parameters in the group.
(For annotated interface methods this would be the return type, for annotated setter methods in a concrete class this would be the setter's parameter type.)

Picocli will instantiate this class as necessary to capture command line argument values in the `@Option` and `@Parameters`-annotated fields and methods of this class.

#### Mutually Exclusive Options

Annotate a field or method with `@ArgGroup(exclusive = true)` to create a group of mutually exclusive options and positional parameters. For example:

```java
@Command(name = "exclusivedemo")
public class MutuallyExclusiveOptionsDemo {

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }
}
```

The above example defines a command with mutually exclusive options `-a`, `-b` and `-c`.

The group itself has a `multiplicity` attribute that defines how many times the group may be specified within the command.
The default is `multiplicity = "0..1"`, meaning that by default a group may be omitted or specified once.
In this example the group has `multiplicity = "1"`, so the group must occur once: one of the exclusive options must occur on the command line.

The synopsis of this command is `exclusivedemo (-a=<a> | -b=<b> | -c=<c>)`.

Note that the options are defined as `required = true`; this means required _within the group_, not required within the command.

Picocli will validate the arguments and throw a `MutuallyExclusiveArgsException` if multiple mutually exclusive arguments were specified. For example:

```java
MutuallyExclusiveOptionsDemo example = new MutuallyExclusiveOptionsDemo();
CommandLine cmd = new CommandLine(example);

try {
    cmd.parseArgs("-a=1", "-b=2");
} catch (MutuallyExclusiveArgsException ex) {
    assert "Error: -a=<a>, -b=<b> are mutually exclusive (specify only one)"
            .equals(ex.getMessage());
}
```

For the above group, only one of the options can be specified. Any other combination of options, or the absence of options, is invalid.

#### Mutually Dependent Options

Annotate a field or method with `@ArgGroup(exclusive = false)` to create a group of dependent options and positional parameters that must co-occur. For example:

```java
@Command(name = "co-occur")
public class DependentOptionsDemo {

    @ArgGroup(exclusive = false)
    Dependent dependent;

    static class Dependent {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }
}
```

The above example defines a command with dependent options `-a`, `-b` and `-c` that must co-occur.

The group itself has a `multiplicity` attribute that defines how many times the group may be specified within the command.
In this example the group uses the default multiplicity, `multiplicity = "0..1"`, meaning that the group may be omitted or specified once.

The synopsis of this command is `co-occur [-a=<a> -b=<b> -c=<c>]`.

Note that the options are defined as `required = true`; this means required _within the group_, not required within the command.

Picocli will validate the arguments and throw a `MissingParameterException` if not all dependent arguments were specified. For example:

```java
DependentOptionsDemo example = new DependentOptionsDemo();
CommandLine cmd = new CommandLine(example);

try {
    cmd.parseArgs("-a=1", "-b=2");
} catch (MissingParameterException ex) {
    assert "Error: Missing required argument(s): -c=<c>".equals(ex.getMessage());
}
```

#### Option Sections in Usage Help

The example below uses groups to define options sections in the usage help.
When a group has a non-null `heading` (or `headingKey`), the options in the group are given the specified heading in the usage help message.
The `headingKey` attribute can be used to get the heading text from the command's resource bundle.

This works for mutually exclusive or co-occurring groups, but it is also possible to define a group that does no validation but only creates an option section in the usage help.

Annotate a field or method with `@ArgGroup(validate = false)` to create a group for display purposes only. For example:

```java
@Command(name = "sectiondemo", description = "Section demo")
public class OptionSectionDemo {

    @ArgGroup(validate = false, heading = "This is the first section%n")
    Section1 section1;

    static class Section1 {
        @Option(names = "-a", description = "Option A") int a;
        @Option(names = "-b", description = "Option B") int b;
        @Option(names = "-c", description = "Option C") int c;
    }

    @ArgGroup(validate = false, heading = "This is the second section%n")
    Section2 section2;

    static class Section2 {
        @Option(names = "-x", description = "Option X") int x;
        @Option(names = "-y", description = "Option Y") int y;
        @Option(names = "-z", description = "Option X") int z;
    }

    public static void main(String[] args) {
        new CommandLine(new OptionSectionDemo()).usage(System.out);
    }
}
```

This prints the following usage help message:
```
Usage: sectiondemo [-a=<a>] [-b=<b>] [-c=<c>] [-x=<x>] [-y=<y>] [-z=<z>]
Section demo
This is the first section
  -a=<a>    Option A
  -b=<b>    Option B
  -c=<c>    Option C
This is the second section
  -x=<x>    Option X
  -y=<y>    Option Y
  -z=<z>    Option X
```

Note that the heading text must end with `%n` to insert a newline between the heading text and the first option.
This is for consistency with other headings in the usage help, like `@Command(headerHeading = "Usage:%n", optionListHeading = "%nOptions:%n")`.

#### Repeating Composite Argument Groups

The below example shows how groups can be composed of other groups, and how arrays and collections can be used to capture repeating groups (with a `multiplicity` greater than one):

```java
@Command(name = "repeating-composite-demo")
public class CompositeGroupDemo {

    @ArgGroup(exclusive = false, multiplicity = "1..*")
    List<Composite> composites;

    static class Composite {
        @ArgGroup(exclusive = false, multiplicity = "0..1")
        Dependent dependent;

        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;
    }

    static class Dependent {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }

    static class Exclusive {
        @Option(names = "-x", required = true) boolean x;
        @Option(names = "-y", required = true) boolean y;
        @Option(names = "-z", required = true) boolean z;
    }
}
```

In the above example, the annotated `composites` field defines a composite group that must be specified at least once, and may be specified many times (`multiplicity = "1..*"`), on the command line.

The synopsis of this command is:

```
Usage: repeating-composite-demo ([-a=<a> -b=<b> -c=<c>] (-x | -y | -z))...
```

Each time the group is matched, picocli creates an instance of the `Composite` class and adds it to the `composites` list.

The `Composite` class itself contains two groups: an optional (`multiplicity = "0..1"`) group of dependent options that must co-occur, and another group of mutually exclusive options, which is mandatory (`multiplicity = "1"`).

The below example illustrates:

```java
CompositeGroupDemo example = new CompositeGroupDemo();
CommandLine cmd = new CommandLine(example);

cmd.parseArgs("-x", "-a=1", "-b=1", "-c=1", "-a=2", "-b=2", "-c=2", "-y");
assert example.composites.size() == 2;

Composite c1 = example.composites.get(0);
assert c1.exclusive.x;
assert c1.dependent.a == 1;
assert c1.dependent.b == 1;
assert c1.dependent.c == 1;

Composite c2 = example.composites.get(1);
assert c2.exclusive.y;
assert c2.dependent.a == 2;
assert c2.dependent.b == 2;
assert c2.dependent.c == 2;
```

#### Positional Parameters

When a `@Parameters` positional parameter is part of a group, its `index` is the index _within the group_, not within the command.


### <a name="4.0.0-variable-expansion"></a> Variable expansion
From this release, picocli supports variable interpolation (variable expansion) in annotation attributes as well as in text attributes of the programmatic API.

#### Variable Interpolation Example

```java
@Command(name = "status", description = "This command logs the status for ${PARENT-COMMAND-NAME}.")
class Status {
    @Option(names = {"${dirOptionName1:--d}", "${dirOptionName2:---directories}"}, // -d or --directories
            description = {"Specify one or more directories, separated by '${sys:path.separator}'.",
                           "The default is the user home directory (${DEFAULT-VALUE})."},
            arity = "${sys:dirOptionArity:-1..*}",
            defaultValue = "${sys:user.home}",
            split = "${sys:path.separator}")
    String[] directories;
}
```

#### Predefined Variables

See the [user manual](https://picocli.info/#_predefined_variables) for the list of predefined variables.

#### Custom Variables

In addition, you can define your own variables. Currently the following syntaxes are supported:

* `${sys:key}`: system property lookup, replaced by the value of `System.getProperty("key")`
* `${env:key}`: environment variable lookup, replaced by the value of `System.getEnv("key")`
* `${bundle:key}`: look up the value of `key` in the resource bundle of the command
* `${key}`: search all of the above, first system properties, then environment variables, and finally the resource bundle of the command

#### Default Values for Custom Variables

You can specify a default value to use when no value is found for a custom variable. The syntax for specifying a default is `${a:-b}`, where `a` is the variable name and `b` is the default value to use if `a` is not found.


### <a name="4.0.0-jpms-module"></a> Explicit JPMS module
<img src="https://picocli.info/images/modules.jpg" alt="modules">

The main `picocli-4.0.0.jar` is a JPMS module named `info.picocli`.

Starting from picocli 4.0, this jar will be an explicit module instead of an automatic module, so the [`jlink` tool](https://docs.oracle.com/en/java/javase/12/tools/jlink.html) can be used to provide a trimmed binary image that has only the required modules.

Typically, a modular jar includes the `module-info.class` file in its root directory. This causes problems for some older tools, which incorrectly process the module descriptor as if it were a normal Java class. To provide the best backward compatibility, the main picocli artifact is a [modular multi-release jar](https://openjdk.java.net/jeps/238#Modular-multi-release-JAR-files) with the `module-info.class` file located in `META-INF/versions/9`.

### <a name="4.0.0-groovy-module"></a> Separate `picocli-groovy` module

<img src="https://picocli.info/images/groovy-logo.png" alt="Groovy logo">

Also, from this release the main `picocli-4.x` artifact no longer contains the `picocli.groovy` classes: these have been split off into a separate `picocli-groovy-4.x` artifact. This was necessary to make the main `picocli-4.x.jar` an explicit JPMS module.


### <a name="4.0.0-negatable-options"></a> Negatable options
From picocli 4.0, options can be `negatable`.

```java
class App {
    @Option(names = "--verbose",           negatable = true) boolean verbose;
    @Option(names = "-XX:+PrintGCDetails", negatable = true) boolean printGCDetails;
    @Option(names = "-XX:-UseG1GC",        negatable = true) boolean useG1GC = true;
}
```

When an option is negatable, picocli will recognize negative aliases of the option on the command line.

For *nix-style long options, aliases have the prefix 'no-' to the given names.
For Java JVM-style options, the `:+` is turned into `:-` and vice versa. (This can be changed by customizing the `INegatableOptionTransformer`.)

If the negated form of the option is found, for example `--no-verbose`, the value is set to the provided default. Otherwise, with a regular call, for example `--verbose`, it is set to the opposite of the default.

### <a name="4.0.0-fallback-values"></a> Fallback value for options
This release introduces a new attribute on the `Option` annotation: `fallbackValue` for options with optional parameter: assign this value when the option was specified on the command line without parameter.

This is different from the `defaultValue`, which is assigned if the option is not specified at all on the command line.

Using a `fallbackValue` allows applications to distinguish between cases where
* the option was not specified on the command line (default value assigned)
* the option was specified without parameter on the command line (fallback value assigned)
* the option was specified with parameter on the command line (command line argument value assigned)

This is useful to define options that can function as a boolean "switch" and optionally allow users to provide a (strongly typed) extra parameter value.

The option description may contain the `${FALLBACK-VALUE}` variable which will be replaced with the actual fallback value when the usage help is shown.


### <a name="4.0.0-parameterConsumer"></a> Custom parameter processing
Options or positional parameters can be assigned a `IParameterConsumer` that implements custom logic to process the parameters for this option or this position. When an option or positional parameter with a custom `IParameterConsumer` is matched on the command line, picocli's internal parser is temporarily suspended, and the custom parameter consumer becomes responsible for consuming and processing as many command line arguments as needed.

This can be useful when passing options through to another command.

For example, the unix [`find`](https://en.wikipedia.org/wiki/Find_(Unix)) command has a [`-exec`](https://en.wikipedia.org/wiki/Find_(Unix)#Execute_an_action) option to execute some action for each file found. Any arguments following the `-exec` option until a `;` or `+` argument are not options for the `find` command itself, but are interpreted as a separate command and its options.

The example below demonstrates how to implement `find -exec` using this API:

```java
@Command(name = "find")
class Find {
    @Option(names = "-exec", parameterConsumer = ExecParameterConsumer.class)
    List<String> list = new ArrayList<String>();
}

class ExecParameterConsumer implements IParameterConsumer {
    public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
        List<String> list = argSpec.getValue();
        while (!args.isEmpty()) {
            String arg = args.pop();
            list.add(arg);

            // `find -exec` semantics: stop processing after a ';' or '+' argument
            if (";".equals(arg) || "+".equals(arg)) {
                break;
            }
        }
    }
}
```


### <a name="4.0.0-quoted-params"></a> Improved parsing of quoted parameters
Also, from this release, support for quoted parameter values has been improved. Quoted parameter values can now contain nested quoted substrings to give end users fine-grained control over how values are split. See the [user manual](https://picocli.info/#_quoted_values) for details.

### <a name="4.0.0-auto-width"></a> Auto-detect terminal width for usage help
From this release, commands defined with `@Command(usageHelpAutoWidth = true)` will try to adjust the usage message help layout to the terminal width.
There is also programmatic API to control this via the `CommandLine::setUsageHelpAutoWidth` and `UsageMessageSpec::autoWidth` methods.

End users may enable this by setting system property `picocli.usage.width` to `AUTO`, and may disable this by setting this system property to a numeric value.

This feature requires Java 7.


### <a name="4.0.0-cjk"></a> Improved support for Chinese, Japanese and Korean usage help
Picocli will align the usage help message to fit within some user-defined width (80 columns by default).
A number of characters in Chinese, Japanese and Korean (CJK) are wider than others.
If those characters are treated to have the same width as other characters, the usage help message may extend past the right margin.

From this release, picocli will use 2 columns for these wide characters when calculating where to put line breaks, resulting in better usage help message text.

This can be switched off with `CommandLine.setAdjustLineBreaksForWideCJKCharacters(false)`.


## <a name="4.0.0-fixes"></a> Fixed issues
### 4.0.0-GA
- [#752][#658][#496] Add `picocli-spring-boot-starter` module including a `PicocliSpringFactory` and auto-configuration. Thanks to [Thibaud Lepretre](https://github.com/kakawait) for the pull request.
- [#736] API: Allow removal of `ArgSpec` from `CommandSpec`. Thanks to [AkosCz](https://github.com/akoscz) for the feature request.
- [#756] API: Make synopsis indent for multi-line synopsis configurable (related to #739).
- [#761] API: Add `ParseResult.matchedArgs()` method to return all matched arguments in order; change `ParseResult.matchedOptions()` and `ParseResult.matchedPositionals()` to return the full list of matched options and positional parameters, including duplicates if the option or positional parameter was matched multiple times. Thanks to [Michael D. Adams](https://github.com/adamsmd) for the feature request.
- [#760] API: Deprecate `CommandLine.setSplitQuotedStrings`: the vast majority of applications want to `split` while respecting quotes.
- [#754] API/Enhancement: Allow boolean options to get value from fallback instead of defaultProvider. Thanks to [Michael D. Adams](https://github.com/adamsmd) for the feature request.
- [#696][#741] Enhancement: Automatically split lines in TextTable. Thanks to [Sualeh Fatehi](https://github.com/sualeh) for the pull request.
- [#744] Enhancement: Composite Argument Groups: more informative error messages.  Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#745] Enhancement: Picocli should disallow `split` regex for single-value type options. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#748] Enhancement: Provide API to use a custom Layout in usage help message: ensure `Help.createDefaultLayout()` is used internally so that subclasses overriding this method can control the Layout that is used.
- [#595] Enhancement: Support for quoted arguments containing nested quoted substrings, allowing end-users to control how values are split in parts when a `split` regex is defined.
- [#739] Bugfix: infinite loop or exception when command name plus synopsis heading length equals or exceeds usage help message width. Thanks to [Arturo Alonso](https://github.com/thefang12) for raising this.
- [#746] Bugfix: Apply default values to options and positional parameters in argument groups. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#742] Bugfix: Default values prevent correct parsing in argument groups. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#759] Bugfix: Correct tracing when custom end-of-option delimiter is matched on the command line.
- [#738] Bugfix: `setTrimQuotes` does not trim quotes from option names. Thanks to [Judd Gaddie](https://github.com/juddgaddie) for raising this.
- [#758] Bugfix: Duplicate name exception in argument group: better / more concise error message. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#751] Build: Make build more portable.
- [#753] Doc: Improve documentation for multi-value fields: mention the `split` attribute. Thanks to [feinstein](https://github.com/feinstein).
- [#740] Doc: Update user manual to replace `parse` examples with `parseArgs`.
- [#713] Doc: Update UML class diagrams for picocli 4.0.

### 4.0.0-beta-2
- [#280] API: `@Option(fallbackValue = "...")` for options with optional parameter: assign this value when the option was specified on the command line without parameter. Thanks to [Paolo Di Tommaso](https://github.com/pditommaso) and [marinier](https://github.com/marinier) for the suggestion and in-depth discussion.
- [#625] API: `@Command(synopsisSubcommandLabel = "...")` to allow customization of the subcommands part of the synopsis: by default this is `[COMMAND]`. Thanks to [Sebastian Thomschke](https://github.com/sebthom) and [AlcaYezz](https://github.com/AlcaYezz) for the feature request and subsequent discussion.
- [#718] API: Add `IParameterConsumer` and `@Option(parameterConsumer = Xxx.class)` for passing arguments through to another command, like `find -exec`. Thanks to [Reinhard Pointner](https://github.com/rednoah) for the suggestion.
- [#721] API: Add public method Text.getCJKAdjustedLength().
- [#634] API: Dynamically detect terminal size. Requires Java 7. Thanks to my colleague Takuya Ishibashi for the suggestion.
- [#737] Deprecate the `parse` method in favor of `parseArgs`.
- [#717] Negatable options change: avoid unmappable character `±` for synopsis: it renders as scrambled characters in encoding ASCII and in some terminals.
- [#734][#735] Make the picocli jar OSGi friendly. Thanks to [Radu Cotescu](https://github.com/raducotescu) for the pull request.
- [#733] Improve error message for unmatched arguments. Thanks to my colleague Takuya Ishibashi for raising this.
- [#719] Bugfix: options with variable arity should stop consuming arguments on custom end-of-options delimiter.
- [#720] Bugfix: `@Unmatched` list should be cleared prior to subsequent invocations.
- [#723] Bugfix: variables in `defaultValue` were not expanded in usage help option description line for `showDefaultValues = true`. Thanks to [Mikaël Barbero](https://github.com/mbarbero) for raising this.
- [#722] Bugfix: synopsis of deeply nested `@ArgGroup` shows `@Options` duplicate on outer level of command. Thanks to [Shane Rowatt](https://github.com/srowatt) for raising this.
- [#724] Bugfix: Usage message exceeds width.
- [#731] Doc: Add Zero Bugs Commitment to README.

### 4.0.0-beta-1b
- [#500] Add a generic and extensible picocli annotation processor
- [#699] Add annotation processor that generates `reflect-config.json` during build
- [#703] Add annotation processor that generates `resource-config.json` during build
- [#704] Add annotation processor that generates `proxy-config.json` during build
- [#707] Add example maven/gradle projects that demonstrate using the annotation processor
- [#711] API: Create separate `picocli-groovy` module, make `picocli` an explicit module (a modular multiversion jar)
- [#694] API: `negatable` boolean options. Thanks to [Michael D. Adams](https://github.com/adamsmd) for the feature request.
- [#712] Boolean options should not toggle by default, to be consistent with negatable options
- [#709] Fix scrambled characters for the `±` character when running on system with non-UTF8 encoding
- [#717] Fix unmappable character for encoding ASCII by setting compiler encoding to UTF8 explicitly. Thanks to [Liam Esteban Prince](https://github.com/leliamesteban) for raising this.
- [#697] Option sort in usage help should ignore option name prefix; long options without short name should be inserted alphabetically, instead of always appear at the top.
- [#695] Fix runtime warnings about illegal reflective access to field `java.io.FilterOutputStream.out`. Thanks to [gitfineon](https://github.com/gitfineon) for reporting this issue.
- [#698] Reduce `reflect-config.json` used by GraalVM native-image generation
- [#700] Change default exit codes to `1` for Exceptions in client code, `2` for invalid usage. Add links to `ExitCode` javadoc.
- [#715] processor tests should not fail when running in different locale
- [#710] Let annotation processor validate negatable options, usageHelp options
- [#716] Revert `@Inherited` annotation for `@Command`. Thanks to [Mikusch](https://github.com/Mikusch) for raising this.

### 4.0.0-alpha-3
 - [#516] API: Add support for color schemes in the convenience methods and associated classes and interfaces. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the suggestion.
 - [#561] API: Parser configuration for convenience methods.
 - [#650] API: Global parser configuration if using Runnable. Thanks to [gitfineon](https://github.com/gitfineon) for raising this.
 - [#424] API: Exit on help, version or invalid arguments. Thanks to [Gerard Bosch](https://github.com/gerardbosch) for raising this.
 - [#541] API: Improved exception handling for Runnable/Callable.
 - [#680] API: Add annotation API for exitCodeList and exitCodeListHeading.
 - [#611] API: Add `CommandLine.addSubcommand` overloaded method without name or alias. Thanks to [andrewbleonard](https://github.com/andrewbleonard) for the request.
 - [#684] API: Make `CommandLine.defaultFactory` method public.
 - [#675] API: Make `Help.ColorScheme` immutable. This is a breaking API change.
 - [#673] API: Deprecate `CommandLine.Range` public fields, add accessor methods to use instead.
 - [#663] How to remove stacktraces on error. Thanks to [Nicolas Mingo](https://github.com/nicolasmingo) and [jrevault](https://github.com/jrevault) for raising this and subsequent discussion.
 - [#672] Need way to send errors back from subcommand. Thanks to [Garret Wilson](https://github.com/garretwilson) for raising this.
 - [#678] Exit Status section in usage help message.
 - [#683] Ensure exitCodeList implementation is consistent with other usage message attributes.
 - [#575] Codegen: Use mixinStandardHelpOptions in `AutoComplete$App` (add support for the `--version` option)
 - [#645] Codegen: Exclude Jansi Console from generated GraalVM reflection configuration. Thanks to [shanetreacy](https://github.com/shanetreacy) for raising this.
 - [#686] Codegen: Add support for `@Command` interfaces (dynamic proxies) in GraalVM native image.
 - [#669] Codegen: Add support for resource bundles in GraalVM native image.
 - [#691] Codegen bugfix: `ReflectionConfigGenerator` should not generate config for `picocli.CommandLine$Model$ObjectScope`.
 - [#674] JPMS module: move module-info.class to root of jar.
 - [#676] Bugfix: non-defined variables in `defaultValue` now correctly resolve to `null`, and options and positional parameters are now correctly considered `required` only if their default value is `null` after variable interpolation. Thanks to [ifedorenko](https://github.com/ifedorenko) for raising this.
 - [#682] Bugfix: incorrect evaluation for multiple occurrences of a variable.
 - [#689] NPE in codegen OutputFileMixin.
 - [#679] Documentation: Update examples for new execute API. Add examples for exit code control and custom exception handlers.
 - [#681] Documentation: Add exit code section to Internationalization example in user manual.

### 4.0.0-alpha-2
 - [#495] Publish picocli as a JPMS module in a new artifact `picocli-core-module`. Thanks to [Warkdev](https://github.com/Warkdev) for the pull request.
 - [#21] Count double-width Asian characters as two characters for line-breaking purposes.
 - [#526] Add support for variable interpolation in message strings. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the suggestion.
 - [#660] Added `@java.lang.annotation.Inherited` to the `@picocli.CommandLine.Command` annotation. Thanks to [Devin Smith](https://github.com/devinrsmith) for the suggestion.
 - [#661] Bugfix for stack overflow when option in an argument group had a default value. Thanks to [Andreas Deininger](https://github.com/deining) for reporting this.
 - [#656] Bugfix for issue where synopsis for composite argument groups did not expand for n..* (n > 1). Thanks to Arno Tuomainen for finding this issue.
 - [#654] Bugfix: argument group heading text was not retrieved from ResourceBundle. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
 - [#635] Bugfix in argument group validation: did not show an error if some but not all parts of a co-occurring group were specified. Thanks to [Philipp Hanslovsky](https://github.com/hanslovsky) for the pull request.
 - [#653] Bugfix: argument group validation should be skipped if help was requested. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
 - [#655] Bugfix: argument group validation silently accepts missing subgroup with multiplicity=1.
 - [#652] Documentation: fixes in user manual. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
 - [#651] Documentation: fixes in user manual. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.

### 4.0.0-alpha-1
 - [#643] Change `%` to `%%` when using `${DEFAULT-VALUE}` in option description. Thanks to [Steffen Rehberg](https://github.com/StefRe) for the pull request.
 - [#638] Document fallback descriptionKey for options and parameters in user manual. Thanks to [Mikusch](https://github.com/Mikusch) for the suggestion.
 - [#199] mutually exclusive options
 - [#295] options that must co-occur (dependent options)
 - [#450] option grouping in the usage help message
 - [#358] (also [#635]) repeating composite arguments (this should also cover the use cases presented in #454 and #434 requests for repeatable subcommands)


## <a name="4.0.0-deprecated"></a> Deprecations

### <a name="4.0.0-deprecated-run-call"></a> `run`, `call`, `invoke`, and `parseWithHandlers` methods replaced by `execute`
All variants of the `run`, `call`, `invoke`, and `parseWithHandlers` methods are deprecated from this release, in favor of the new `execute` method.

Similarly, the following classes and interfaces are deprecated:

* `IParseResultHandler2` is deprecated in favor of the new `IExecutionStrategy` interface.
* `IExceptionHandler2` is deprecated in favor of the new `IParameterExceptionHandler` `IExecutionExceptionHandler` interfaces.
* The `AbstractHandler` and `AbstractParseResultHandler` classes are deprecated with no replacement.

### <a name="4.0.0-deprecated-setSplitQuotedStrings"></a> `CommandLine.setSplitQuotedStrings` deprecated
The `CommandLine.setSplitQuotedStrings` (and `isSplitQuotedStrings`) methods have been deprecated:
Most applications should not change the default. The rare application that _does_ need to split parameter values without respecting quotes should use [`ParserSpec.splitQuotedStrings(boolean)`](https://picocli.info/apidocs/picocli/CommandLine.Model.ParserSpec.html#splitQuotedStrings-boolean-).

### <a name="4.0.0-deprecated-parse"></a> `parse` deprecated in favor of `parseArgs`
From this release, the `parse` method is deprecated in favor of `parseArgs`.

### <a name="4.0.0-deprecated-range-public-fields"></a> Range public fields
The public fields of the `Range` class have been deprecated and public methods `min()`, `max()`, `isVariable()` have been added that should be used instead.


## <a name="4.0.0-breaking-changes"></a> Potential breaking changes

### <a name="4.0.0-breaking-groovy"></a> `picocli.groovy` classes moved to separate artifact
From this release the main `picocli-4.x` artifact no longer contains the `picocli.groovy` classes: these have been split off into a separate `picocli-groovy-4.x` artifact.

Scripts upgrading to picocli 4.0 must change more than just the version number!
Scripts should use `@Grab('info.picocli:picocli-groovy:4.x')` from version 4.0, `@Grab('info.picocli:picocli:4.x')` will not work.

### <a name="4.0.0-breaking-split"></a> Split regex on single-value options is now disallowed
Picocli now throws an `InitializationException` when a single-value type option or positional parameter has a `split` regex.
Only multi-value options or positional parameters should have a `split` regex. The runtime check can be disabled by setting system property `picocli.ignore.invalid.split` to any value.
(The annotation processor also checks this at compile time; this check cannot be disabled.)

### <a name="4.0.0-breaking-colorscheme"></a> ColorScheme is now immutable
The `Help.ColorScheme` class has been made immutable. Its public fields are no longer public.
A new `Help.ColorScheme.Builder` class has been introduced to create `ColorScheme` instances.

This is a breaking API change: I could not think of a way to do this without breaking backwards compatibility.

### <a name="4.0.0-breaking-toggle"></a> Boolean options do not toggle by default
From this release, when a flag option is specified on the command line picocli will set its value to the opposite of its _default_ value.

Prior to 4.0, the default was to "toggle" boolean flags to the opposite of their _current_ value:
if the previous value was `true` it is set to `false`, and when the value was `false` it is set to `true`.

Applications can call `CommandLine.setToggleBooleanFlags(true)` to enable toggling.
Note that when toggling is enabled, specifying a flag option twice on the command line will have no effect because they cancel each other out.

### <a name="4.0.0-breaking-matchedOptions"></a> ParseResult `matchedOptions` now returns full list
`ParseResult.matchedOptions()` and `ParseResult.matchedPositionals()` now return the full list of matched options and positional parameters, including duplicates if the option or positional parameter was matched multiple times.
Prior to this release, these methods would return a list that did not contain duplicates.
Applications interested in the old behavior should use the new `matchedOptionSet()` and `matchedPositionalSet()` methods that return a `Set`.

### <a name="4.0.0-breaking-unmatched-error"></a> Error message for unmatched arguments changed
The error message for unmatched arguments now shows the index in the command line arguments where the unmatched argument was found,
and shows the unmatched value in single quotes. This is useful when the unmatched value is whitespace or an empty String.

For example:

```
Previously:  Unmatched arguments: B, C
New       :  Unmatched arguments from index 1: 'B', 'C'
```

This may break tests that rely on the exact error message.

### <a name="4.0.0-breaking-option-order"></a> Option order changed
Previously, options that only have a long name (and do not have a short name) were always shown before options with a short name.
From this release, they are inserted in the option list by their first non-prefix letter.
This may break tests that expect a specific help message.


### <a name="4.0.0-breaking-factory"></a> Factory

From version 4.0, picocli delegates all object creation to the [factory](https://picocli.info/#_custom_factory), including creating `Collection` instances to capture [multi-value](https://picocli.info/#_arrays_and_collections) `@Option` values. Previously, `Collection` objects were instantiated separately without involving the factory.

It is recommended that custom factories should fall back to the default factory. Something like this:

```java
@Override
public <K> K create(Class<K> clazz) throws Exception {
    try {
        return doCreate(clazz); // custom factory lookup or instantiation
    } catch (Exception e) {
        return CommandLine.defaultFactory().create(clazz); // fallback if missing
    }
}
```




# <a name="4.0.0-beta-2"></a> Picocli 4.0.0-beta-2
The picocli community is pleased to announce picocli 4.0.0-beta-2.

Bugfixes and improvements.

This release introduces two new attributes on the `Option` annotation:

* `fallbackValue`
* `parameterConsumer`

`fallbackValue` is for options with optional parameter: assign this value when the option was specified on the command line without parameter. `parameterConsumer` and the associated `IParameterConsumer` interface allows for options to bypass picocli's parsing logic and replace it with custom logic. One use case is collecting arguments to pass them through to another command.

This release introduces a new `synopsisSubcommandLabel` attribute on the `@Command` annotation to allow customization of the subcommands part of the synopsis. This is useful for applications that have required subcommands.

Also, this release adds the ability to dynamically detect the terminal width.

From this release, the picocli JAR is an OSGi bundle with `Bundle-Name: picocli` and other appropriate metadata in the manifest.

_Please try this and provide feedback. We can still make changes._

_What do you think of the `@ArgGroup` annotations API? What about the programmatic API? Does it work as expected? Are the input validation error messages correct and clear? Is the documentation clear and complete? Anything you want to change or improve? Any other feedback?_


Many thanks to the picocli community for the contributions!

This is the fifty-sixth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.0-beta-2"></a> Table of Contents
* [New and noteworthy](#4.0.0-beta-2-new)
* [Fixed issues](#4.0.0-beta-2-fixes)
* [Deprecations](#4.0.0-beta-2-deprecated)
* [Potential breaking changes](#4.0.0-beta-2-breaking-changes)

## <a name="4.0.0-beta-2-new"></a> New and Noteworthy

### Fallback Value API
This release introduces a new attribute on the `Option` annotation: `fallbackValue` for options with optional parameter: assign this value when the option was specified on the command line without parameter.

This is different from the `defaultValue`, which is assigned if the option is not specified at all on the command line.

Using a `fallbackValue` allows applications to distinguish between cases where
* the option was not specified on the command line (default value assigned)
* the option was specified without parameter on the command line (fallback value assigned)
* the option was specified with parameter on the command line (command line argument value assigned)

This is useful to define options that can function as a boolean "switch" and optionally allow users to provide a (strongly typed) extra parameter value.

The option description may contain the `${FALLBACK-VALUE}` variable which will be replaced with the actual fallback value when the usage help is shown.


### Synopsis Subcommand Label
For commands with subcommands, the string `[COMMAND]` is appended to the end of the synopsis (whether the synopsis is abbreviated or not). This looks something like this:

```
<cmd> [OPTIONS] FILES [COMMAND]
```

From picocli 4.0, this can be customized with the `synopsisSubcommandLabel` attribute.

For example, to clarify that a subcommand is mandatory, an application may specify `COMMAND`, without the `[` and `]` brackets:

```java
@Command(name = "git", synopsisSubcommandLabel = "COMMAND")
class Git implements Runnable {
    @Spec CommandSpec spec;
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
```

An application with a limited number of subcommands may want to show them all in the synopsis, for example:

```java
@Command(name = "fs", synopsisSubcommandLabel = "(list | add | delete)",
         subcommands = {List.class, Add.class, Delete.class})
class Fs { ... }
```

### Dynamically Detect Terminal Size

From this release, commands defined with `@Command(usageHelpAutoWidth = true)` will try to adjust the usage message help layout to the terminal width.
There is also programmatic API to control this via the `CommandLine::setUsageHelpAutoWidth` and `UsageMessageSpec::autoWidth` methods.

End users may enable this by setting system property `picocli.usage.width` to `AUTO`, and may disable this by setting this system property to a numeric value.

This feature requires Java 7.

### Custom Parameter Processing

Options or positional parameters can be assigned a `IParameterConsumer` that implements custom logic to process the parameters for this option or this position. When an option or positional parameter with a custom `IParameterConsumer` is matched on the command line, picocli's internal parser is temporarily suspended, and the custom parameter consumer becomes responsible for consuming and processing as many command line arguments as needed.

This can be useful when passing options through to another command.

For example, the unix [`find`](https://en.wikipedia.org/wiki/Find_(Unix)) command has a [`-exec`](https://en.wikipedia.org/wiki/Find_(Unix)#Execute_an_action) option to execute some action for each file found. Any arguments following the `-exec` option until a `;` or `+` argument are not options for the `find` command itself, but are interpreted as a separate command and its options.

The example below demonstrates how to implement `find -exec` using this API:

```java
@Command(name = "find")
class Find {
    @Option(names = "-exec", parameterConsumer = ExecParameterConsumer.class)
    List<String> list = new ArrayList<String>();
}

class ExecParameterConsumer implements IParameterConsumer {
    public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
        List<String> list = argSpec.getValue();
        while (!args.isEmpty()) {
            String arg = args.pop();
            list.add(arg);

            // `find -exec` semantics: stop processing after a ';' or '+' argument
            if (";".equals(arg) || "+".equals(arg)) {
                break;
            }
        }
    }
}
```


## <a name="4.0.0-beta-2-fixes"></a> Fixed issues
- [#280] API: `@Option(fallbackValue = "...")` for options with optional parameter: assign this value when the option was specified on the command line without parameter. Thanks to [Paolo Di Tommaso](https://github.com/pditommaso) and [marinier](https://github.com/marinier) for the suggestion and in-depth discussion.
- [#625] API: `@Command(synopsisSubcommandLabel = "...")` to allow customization of the subcommands part of the synopsis: by default this is `[COMMAND]`. Thanks to [Sebastian Thomschke](https://github.com/sebthom) and [AlcaYezz](https://github.com/AlcaYezz) for the feature request and subsequent discussion.
- [#718] API: Add `IParameterConsumer` and `@Option(parameterConsumer = Xxx.class)` for passing arguments through to another command, like `find -exec`. Thanks to [Reinhard Pointner](https://github.com/rednoah) for the suggestion.
- [#721] API: Add public method Text.getCJKAdjustedLength().
- [#634] API: Dynamically detect terminal size. Requires Java 7. Thanks to my colleague Takuya Ishibashi for the suggestion.
- [#737] Deprecate the `parse` method in favor of `parseArgs`.
- [#717] Negatable options change: avoid unmappable character `±` for synopsis: it renders as scrambled characters in encoding ASCII and in some terminals.
- [#734][#735] Make the picocli jar OSGi friendly. Thanks to [Radu Cotescu](https://github.com/raducotescu) for the pull request.
- [#733] Improve error message for unmatched arguments. Thanks to my colleague Takuya Ishibashi for raising this.
- [#719] Bugfix: options with variable arity should stop consuming arguments on custom end-of-options delimiter.
- [#720] Bugfix: `@Unmatched` list should be cleared prior to subsequent invocations.
- [#723] Bugfix: variables in `defaultValue` were not expanded in usage help option description line for `showDefaultValues = true`. Thanks to [Mikaël Barbero](https://github.com/mbarbero) for raising this.
- [#722] Bugfix: synopsis of deeply nested `@ArgGroup` shows `@Options` duplicate on outer level of command. Thanks to [Shane Rowatt](https://github.com/srowatt) for raising this.
- [#724] Bugfix: Usage message exceeds width.
- [#731] Doc: Add Zero Bugs Commitment to README.


## <a name="4.0.0-beta-2-deprecated"></a> Deprecations
From this release, the `parse` method is deprecated in favor of `parseArgs`.

## <a name="4.0.0-beta-2-breaking-changes"></a> Potential breaking changes

The error message for unmatched arguments now shows the index in the command line arguments where the unmatched argument was found,
and shows the unmatched value in single quotes. This is useful when the unmatched value is whitespace or an empty String.

For example:

```
Previously:  Unmatched arguments: B, C
New       :  Unmatched arguments from index 1: 'B', 'C'
```

This may break tests that rely on the exact error message.





# <a name="4.0.0-beta-1b"></a> Picocli 4.0.0-beta-1b
The picocli community is pleased to announce picocli 4.0.0-beta-1b.

This release includes the first cut of an annotation processor that can build a model from the picocli annotations at compile time rather than at runtime.

Use this if you’re interested in:
* **Compile time error checking**. The annotation processor shows errors for invalid annotations and attributes immediately when you compile, instead of during testing at runtime, resulting in shorter feedback cycles.
* **Graal native images**. The annotation processor generates [Graal configuration](https://github.com/oracle/graal/blob/master/substratevm/BuildConfiguration.md)
files under `META-INF/native-image/picocli-generated/$project` during compilation, to be included in the application jar.
By embedding these configuration files, your jar is instantly Graal-enabled.
In most cases no further configuration is needed when generating a native image.

Also, from this release the main `picocli-4.x` artifact no longer contains the `picocli.groovy` classes: these have been split off into a separate `picocli-groovy-4.x` artifact.
The main `picocli-4.x.jar` is now an explicit JPMS module, with a `module-info.class` located in `META-INF/versions/9`.
The `picocli-jpms-module` subproject has been removed.

From picocli 4.0, options can be `negatable`.
When an option is negatable, picocli will recognize negative aliases of the option on the command line. See the New and Noteworthy section below for more details.


_Please try this and provide feedback. We can still make changes._

_What do you think of the `@ArgGroup` annotations API? What about the programmatic API? Does it work as expected? Are the input validation error messages correct and clear? Is the documentation clear and complete? Anything you want to change or improve? Any other feedback?_


Many thanks to the picocli community for the contributions!

This is the fifty-fifth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.0-beta-1b"></a> Table of Contents
* [New and noteworthy](#4.0.0-beta-1b-new)
* [Fixed issues](#4.0.0-beta-1b-fixes)
* [Deprecations](#4.0.0-beta-1b-deprecated)
* [Potential breaking changes](#4.0.0-beta-1b-breaking-changes)

## <a name="4.0.0-beta-1b-new"></a> New and Noteworthy

### <a name="4.0.0-beta-1b-processor"></a> Annotation Processor

This release includes the first cut of an annotation processor that can build a model from the picocli annotations at compile time rather than at runtime.

Use this if you’re interested in:
* **Compile time error checking**. The annotation processor shows errors for invalid annotations and attributes immediately when you compile, instead of during testing at runtime, resulting in shorter feedback cycles.
* **Graal native images**. The annotation processor generates and updates [Graal configuration](https://github.com/oracle/graal/blob/master/substratevm/BuildConfiguration.md) files under
`META-INF/native-image/picocli-generated/$project` during compilation, to be included in the application jar.
This includes configuration files for [reflection](https://github.com/oracle/graal/blob/master/substratevm/Reflection.md), [resources](https://github.com/oracle/graal/blob/master/substratevm/Resources.md) and [dynamic proxies](https://github.com/oracle/graal/blob/master/substratevm/DynamicProxy.md).
By embedding these configuration files, your jar is instantly Graal-enabled.
The `$project` location is configurable, see [processor options](#picocli-processor-options) below.
In most cases no further configuration is needed when generating a native image.

#### Enabling the Annotation Processor

Since Java 6, annotation processing is part of the standard `javac` compiler, but many IDEs and build tools require something extra to enable annotation processing.

##### IDE
[This page](https://immutables.github.io/apt.html) shows the steps to configure Eclipse and IntelliJ IDEA to enable annotation processing.

##### Maven
In Maven, use `annotationProcessorPaths` in the `configuration` of the `maven-compiler-plugin`.
This requires `maven-compiler-plugin` plugin version 3.5 or higher.

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <!-- annotationProcessorPaths requires maven-compiler-plugin version 3.5 or higher -->
  <version>${maven-compiler-plugin-version}</version>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>info.picocli</groupId>
        <artifactId>picocli-codegen</artifactId>
        <version>4.0.0-beta-1b</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

An alternative that works with older versions of the `maven-compiler-plugin` is to specify the `picocli-codegen` module on the classpath as a `provided` dependency. This also prevents the `picocli-codegen` module from being included in the artifact the module produces as a transitive dependency.

```
<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli</artifactId>
  <version>4.0.0-beta-1b</version>
</dependency>

<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli-codegen</artifactId>
  <version>4.0.0-beta-1b</version>
  <provided>true</provided>
</dependency>
```


See Processor Options below.


##### Gradle
Use the `annotationProcessor` path in Gradle [4.6 and higher](https://docs.gradle.org/4.6/release-notes.html#convenient-declaration-of-annotation-processor-dependencies):
```
dependencies {
    compile 'info.picocli:picocli:4.0.0-beta-1b'
    annotationProcessor 'info.picocli:picocli-codegen:4.0.0-beta-1b'
}
```

For Gradle versions prior to 4.6, use `compileOnly`, to prevent the `picocli-codegen` jar from being a transitive dependency included in the artifact the module produces.
```
dependencies {
    compile 'info.picocli:picocli:4.0.0-beta-1b'
    compileOnly 'info.picocli:picocli-codegen:4.0.0-beta-1b'
}
```

#### Picocli Processor Options

The picocli annotation processor supports the options below.

##### Recommended Options
* `project` - output subdirectory

The generated files are written to `META-INF/native-image/picocli-generated/${project}`.

The `project` option can be omitted, but it is a good idea to specify the `project` option with a unique value for your project (e.g. `${groupId}/${artifactId}`) if your jar may be [shaded](https://stackoverflow.com/a/49811665) with other jars into an uberjar.


##### Other Options
* `other.resource.patterns` - comma-separated list of regular expressions matching additional resources to include in the image
* `other.resource.bundles` - comma-separated list of the base names of additional resource bundles to include in the image
* `other.proxy.interfaces` - comma-separated list of the fully qualified class names of additional interfaces for which to generate proxy classes when building the image
* `disable.proxy.config` - don’t generate `proxy-config.json`
* `disable.reflect.config` - don’t generate `reflect-config.json`
* `disable.resource.config` - don’t generate `resources-config.json`


##### Javac
To pass an annotation processor option with `javac`, specify the `-A` command line option:

```
javac -Aproject=org.myorg.myproject/myapp -cp ...
```
The `-A` option lets you pass options to annotation processors. See the [javac documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javac.html) for details.

##### Maven

To set an annotation processor option in Maven, you need to use the `maven-compiler-plugin` and configure the `compilerArgs` section.

```
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <!-- annotationProcessorPaths requires maven-compiler-plugin version 3.5 or higher -->
      <version>${maven-compiler-plugin-version}</version>
      <configuration>
        <compilerArgs>
          <arg>-Aproject=${groupId}/${artifactId}</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

See https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html for details.

#### Gradle Example
To set an annotation processor option in Gradle, add these options to the `options.compilerArgs` list in the `compileJava` block.

```
compileJava {
    // minimum 1.6
    sourceCompatibility = ${java-version}
    targetCompatibility = ${java-version}
    options.compilerArgs += ["-Aproject=${project.group}/${project.name}"]
}
```

See the [Gradle documentation](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.CompileOptions.html) for details.


### <a name="4.0.0-beta-1b-negatable"></a> Negatable Options
From picocli 4.0, options can be `negatable`.

```java
class App {
    @Option(names = "--verbose",           negatable = true) boolean verbose;
    @Option(names = "-XX:+PrintGCDetails", negatable = true) boolean printGCDetails;
    @Option(names = "-XX:-UseG1GC",        negatable = true) boolean useG1GC = true;
}
```

When an option is negatable, picocli will recognize negative aliases of the option on the command line.

For *nix-style long options, aliases have the prefix 'no-' to the given names.
For Java JVM-style options, the `:+` is turned into `:-` and vice versa. (This can be changed by customizing the `INegatableOptionTransformer`.)

If the negated form of the option is found, for example `--no-verbose`, the value is set to the provided default. Otherwise, with a regular call, for example `--verbose`, it is set to the opposite of the default.



## <a name="4.0.0-beta-1b-fixes"></a> Fixed issues
- [#500] Add a generic and extensible picocli annotation processor
- [#699] Add annotation processor that generates `reflect-config.json` during build
- [#703] Add annotation processor that generates `resource-config.json` during build
- [#704] Add annotation processor that generates `proxy-config.json` during build
- [#707] Add example maven/gradle projects that demonstrate using the annotation processor
- [#711] API: Create separate `picocli-groovy` module, make `picocli` an explicit module (a modular multiversion jar)
- [#694] API: `negatable` boolean options. Thanks to [Michael D. Adams](https://github.com/adamsmd) for the feature request.
- [#712] Boolean options should not toggle by default, to be consistent with negatable options
- [#709] Fix scrambled characters for the `±` character when running on system with non-UTF8 encoding
- [#717] Fix unmappable character for encoding ASCII by setting compiler encoding to UTF8 explicitly. Thanks to [Liam Esteban Prince](https://github.com/leliamesteban) for raising this.
- [#697] Option sort in usage help should ignore option name prefix; long options without short name should be inserted alphabetically, instead of always appear at the top.
- [#695] Fix runtime warnings about illegal reflective access to field `java.io.FilterOutputStream.out`. Thanks to [gitfineon](https://github.com/gitfineon) for reporting this issue.
- [#698] Reduce `reflect-config.json` used by GraalVM native-image generation
- [#700] Change default exit codes to `1` for Exceptions in client code, `2` for invalid usage. Add links to `ExitCode` javadoc.
- [#715] processor tests should not fail when running in different locale
- [#710] Let annotation processor validate negatable options, usageHelp options
- [#716] Revert `@Inherited` annotation for `@Command`. Thanks to [Mikusch](https://github.com/Mikusch) for raising this.

## <a name="4.0.0-beta-1b-deprecated"></a> Deprecations


## <a name="4.0.0-beta-1b-breaking-changes"></a> Potential breaking changes

### `picocli.groovy` Classes Moved to Separate Artifact
From this release the main `picocli-4.x` artifact no longer contains the `picocli.groovy` classes: these have been split off into a separate `picocli-groovy-4.x` artifact.

Scripts upgrading to picocli 4.0 must change more than just the version number!
Scripts should use `@Grab('info.picocli:picocli-groovy:4.x')` from version 4.0, `@Grab('info.picocli:picocli:4.x')` will not work.

### Option Order Changed
Previously, options that only have a long name (and do not have a short name) were always shown before options with a short name.
From this release, they are inserted in the option list by their first non-prefix letter.
This may break tests that expect a specific help message.

### Boolean Options Do Not Toggle By Default
From this release, when a flag option is specified on the command line picocli will set its value to the opposite of its _default_ value.

Prior to 4.0, the default was to "toggle" boolean flags to the opposite of their _current_ value:
if the previous value was `true` it is set to `false`, and when the value was `false` it is set to `true`.

Applications can call `CommandLine.setToggleBooleanFlags(true)` to enable toggling.
Note that when toggling is enabled, specifying a flag option twice on the command line will have no effect because they cancel each other out.

### Revert `@Inherited` annotation on `@Command`
The `@Inherited` annotated that was added to `@Command` in picocli 4.0.0-alpha-2 turned out to cause
issues in scenarios with multiple levels of inheritance and is reverted in this release.


# <a name="4.0.0-alpha-3"></a> Picocli 4.0.0-alpha-3
The picocli community is pleased to announce picocli 4.0.0-alpha-3.

This release adds improved support for command execution via the new `execute` method.
This method returns an exit code that applications can use to call `System.exit`.

The older `run`, `call`, `invoke` and `parseWithHandlers` convenience methods that were similar to `execute` but had limited support for parser configuration and and limited support for exit codes are deprecated from this release.

This release also improves the picocli tools for configuring GraalVM native image builds: there is now support for commands with resource bundles and jewelcli-style `@Command`-annotated interfaces for which picocli generates a dynamic proxy.

_Please try this and provide feedback. We can still make changes._

_What do you think of the `@ArgGroup` annotations API? What about the programmatic API? Does it work as expected? Are the input validation error messages correct and clear? Is the documentation clear and complete? Anything you want to change or improve? Any other feedback?_


Many thanks to the picocli community for the contributions!

This is the fifty-fourth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.0-alpha-3"></a> Table of Contents
* [New and noteworthy](#4.0.0-alpha-3-new)
* [Fixed issues](#4.0.0-alpha-3-fixes)
* [Deprecations](#4.0.0-alpha-3-deprecated)
* [Potential breaking changes](#4.0.0-alpha-3-breaking-changes)

## <a name="4.0.0-alpha-3-new"></a> New and Noteworthy

### <a name="4.0.0-alpha-3-execute"></a> Executing Commands

Picocli 4.0 introduces new API to execute commands. Let’s take a quick look at what changed.

#### Exit Code
Many command line applications return an [exit code](https://en.wikipedia.org/wiki/Exit_status) to signify success or failure. Zero often means success, a non-zero exit code is often used for errors, but other than that, meanings differ per application.

The new `CommandLine.execute` method introduced in picocli 4.0 returns an `int`, and applications can use this return value to call `System.exit` if desired. For example:

```java
public static void main(String... args) {
  CommandLine cmd = new CommandLine(new App());
  int exitCode = cmd.execute(args);
  System.exit(exitCode);
}
```

Older versions of picocli had some limited exit code support where picocli would call `System.exit`, but this is now deprecated.

#### Generating an Exit Code

`@Command`-annotated classes that implement `Callable` and `@Command`-annotated methods can simply return an `int` or `Integer`, and this value will be returned from `CommandLine.execute`. For example:

```java
@Command(name = "greet")
class Greet implements Callable<Integer> {
  public Integer call() {
    System.out.println("hi");
    return 1;
  }

  @Command
  int shout() {
    System.out.println("HI!");
    return 2;
  }
}

assert 1 == new CommandLine(new Greet()).execute();
assert 2 == new CommandLine(new Greet()).execute("shout");
```

Commands with a user object that implements `Runnable` can implement the `IExitCodeGenerator` interface to generate an exit code. For example:

```java
@Command(name = "wave")
class Gesture implements Runnable, IExitCodeGenerator {
  public void run() {
    System.out.println("wave");
  }
  public int getExitCode() {
    return 3;
  }
}

assert 3 == new CommandLine(new Gesture()).execute();
```

#### Exception Exit Codes

By default, the `execute` method returns `CommandLine.ExitCode.USAGE` (`64`) for invalid input, and `CommandLine.ExitCode.SOFTWARE` (`70`) when an exception occurred in the Runnable, Callable or command method. (For reference, these values are `EX_USAGE` and `EX_SOFTWARE`, respectively, from Unix and Linux [sysexits.h](https://www.freebsd.org/cgi/man.cgi?query=sysexits&sektion=3)). This can be customized with the `@Command` annotation. For example:

```java
@Command(exitCodeOnInvalidInput = 123,
   exitCodeOnExecutionException = 456)
```

Additionally, applications can configure a `IExitCodeExceptionMapper` to map a specific exception to an exit code:

```java
class MyMapper implements IExitCodeExceptionMapper {
  public int getExitCode(Throwable t) {
    if (t instance of FileNotFoundException) {
      return 74;
    }
    return 1;
  }
}
```

When the end user specified invalid input, the `execute` method prints an error message followed by the usage help message of the command, and returns an exit code. This can be customized by configuring a `IParameterExceptionHandler`.

If the business logic of the command throws an exception, the `execute` method prints the stack trace of the exception and returns an exit code. This can be customized by configuring a `IExecutionExceptionHandler`.


#### Configuration
The new `CommandLine.execute` method is an instance method. The older `run`, `call` and `invoke` methods are static methods. Static methods don’t allow configuration. The new API lets applications configure the parser or other aspects before execution. For example:

```java
public static void main(String... args) {
  CommandLine cmd = new CommandLine(new App());
  cmd.setCaseInsensitiveEnumValuesAllowed(true);
  cmd.setUnmarchedArgumentsAllowed(true);
  cmd.setStopAtPositional(true);
  cmd.setExpandAtFiles(false);
  cmd.execute(args);
}
```

#### Execution Configuration

The following configuration methods are new and are only applicable with the `execute` method (and `executeHelpRequest`):

* get/setOut
* get/setErr
* get/setColorScheme
* get/setExecutionStrategy
* get/setParameterExceptionHandler
* get/setExecutionExceptionHandler
* get/setExitCodeExceptionMapper

The above methods are not applicable (and ignored) with other entry points like `parse`, `parseArgs`, `populateCommand`, `run`, `call`, `invoke`, `parseWithHandler` and `parseWithHandlers`.

#### API Evolution and Trade-offs

Previous versions of picocli offered the `run`, `call` and `invoke` methods to execute a `Runnable`, `Callable` or `Method` command. Here are some trade-offs versus the new `execute` method:

* *Static* - These are static methods, with the drawback that they don't allow configuration, as mentioned above.
* *Type Safety* - It is a compile-time error when an application tries to pass anything else than a `Runnable` to the `run` method, and a `Callable` to the `call` method. The `execute` method does not have this type safety, since the `CommandLine` constructor allows any `Object` as a parameter.
* *Return Value* - The `call` and `invoke` static methods allow commands to return _any_ value, while the `execute` method only returns an `int` exit code. From 4.0 the result object will be available from the `CommandLine.getExecutionResult` method.

#### Feedback Requested

With the new execute API the `ColorScheme` class will start to play a more central role. I decided to make the `ColorScheme` class immutable from this release. This is a breaking API change.
Should it be deprecated first, or not changed at all, or is the 4.0 release a good time to make breaking changes? Your feedback is very welcome on https://github.com/remkop/picocli/issues/675.

### <a name="4.0.0-alpha-3-codegen"></a> Tools for Configuring GraalVM Native Image Builds

The `picocli-codegen` module now has two new tools, in addition to the existing `ReflectionConfigGenerator`:

* ResourceConfigGenerator
* DynamicProxyConfigGenerator

#### ResourceConfigGenerator
The GraalVM native-image builder by default will not integrate any of the
[classpath resources](https://github.com/oracle/graal/blob/master/substratevm/Resources.md) into the image it creates.

`ResourceConfigGenerator` generates a JSON String with the resource bundles and other classpath resources
that should be included in the Substrate VM native image.

The output of `ResourceConfigGenerator` is intended to be passed to the `-H:ResourceConfigurationFiles=/path/to/reflect-config.json` option of the `native-image` GraalVM utility,
or placed in a `META-INF/native-image/` subdirectory of the JAR.

This allows picocli-based native image applications to access these resources.

#### DynamicProxyConfigGenerator

Substrate VM doesn't provide machinery for generating and interpreting bytecodes at run time. Therefore all dynamic proxy classes
[need to be generated](https://github.com/oracle/graal/blob/master/substratevm/DynamicProxy.md) at native image build time.

`DynamicProxyConfigGenerator` generates a JSON String with the fully qualified interface names for which
dynamic proxy classes should be generated at native image build time.

The output of `DynamicProxyConfigGenerator` is intended to be passed to the `-H:DynamicProxyConfigurationFiles=/path/to/proxy-config.json` option of the `native-image` GraalVM utility,
or placed in a `META-INF/native-image/` subdirectory of the JAR.

This allows picocli-based native image applications that use `@Command`-annotated interfaces with
`@Option` and `@Parameters`-annotated methods.


## <a name="4.0.0-alpha-3-fixes"></a> Fixed issues
- [#516] API: Add support for color schemes in the convenience methods and associated classes and interfaces. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the suggestion.
- [#561] API: Parser configuration for convenience methods.
- [#650] API: Global parser configuration if using Runnable. Thanks to [gitfineon](https://github.com/gitfineon) for raising this.
- [#424] API: Exit on help, version or invalid arguments. Thanks to [Gerard Bosch](https://github.com/gerardbosch) for raising this.
- [#541] API: Improved exception handling for Runnable/Callable.
- [#680] API: Add annotation API for exitCodeList and exitCodeListHeading.
- [#611] API: Add `CommandLine.addSubcommand` overloaded method without name or alias. Thanks to [andrewbleonard](https://github.com/andrewbleonard) for the request.
- [#684] API: Make `CommandLine.defaultFactory` method public.
- [#675] API: Make `Help.ColorScheme` immutable. This is a breaking API change.
- [#673] API: Deprecate `CommandLine.Range` public fields, add accessor methods to use instead.
- [#663] How to remove stacktraces on error. Thanks to [Nicolas Mingo](https://github.com/nicolasmingo) and [jrevault](https://github.com/jrevault) for raising this and subsequent discussion.
- [#672] Need way to send errors back from subcommand. Thanks to [Garret Wilson](https://github.com/garretwilson) for raising this.
- [#678] Exit Status section in usage help message.
- [#683] Ensure exitCodeList implementation is consistent with other usage message attributes.
- [#575] Codegen: Use mixinStandardHelpOptions in `AutoComplete$App` (add support for the `--version` option)
- [#645] Codegen: Exclude Jansi Console from generated GraalVM reflection configuration. Thanks to [shanetreacy](https://github.com/shanetreacy) for raising this.
- [#686] Codegen: Add support for `@Command` interfaces (dynamic proxies) in GraalVM native image.
- [#669] Codegen: Add support for resource bundles in GraalVM native image.
- [#691] Codegen bugfix: `ReflectionConfigGenerator` should not generate config for `picocli.CommandLine$Model$ObjectScope`.
- [#674] JPMS module: move module-info.class to root of jar.
- [#676] Bugfix: non-defined variables in `defaultValue` now correctly resolve to `null`, and options and positional parameters are now correctly considered `required` only if their default value is `null` after variable interpolation. Thanks to [ifedorenko](https://github.com/ifedorenko) for raising this.
- [#682] Bugfix: incorrect evaluation for multiple occurrences of a variable.
- [#689] NPE in codegen OutputFileMixin.
- [#679] Documentation: Update examples for new execute API. Add examples for exit code control and custom exception handlers.
- [#681] Documentation: Add exit code section to Internationalization example in user manual.

## <a name="4.0.0-alpha-3-deprecated"></a> Deprecations

### Convenience Methods Replaced by `execute`
All variants of the `run`, `call`, `invoke`, and `parseWithHandlers` methods are deprecated from this release, in favor of the new `execute` method.

Similarly, the following classes and interfaces are deprecated:

* `IParseResultHandler2` is deprecated in favor of the new `IExecutionStrategy` interface.
* `IExceptionHandler2` is deprecated in favor of the new `IParameterExceptionHandler` `IExecutionExceptionHandler` interfaces.
* The `AbstractHandler` and `AbstractParseResultHandler` classes are deprecated with no replacement.

### Range
The public fields of the `Range` class have been deprecated and public methods `min()`, `max()`, `isVariable()` have been added that should be used instead.

## <a name="4.0.0-alpha-3-breaking-changes"></a> Potential breaking changes

The `Help.ColorScheme` class has been made immutable. Its public fields are no longer public.
A new `Help.ColorScheme.Builder` class has been introduced to create `ColorScheme` instances.

This is a breaking API change: I could not think of a way to do this without breaking backwards compatibility.


# <a name="4.0.0-alpha-2"></a> Picocli 4.0.0-alpha-2
The picocli community is pleased to announce picocli 4.0.0-alpha-2.

Thanks to great feedback from the picocli community on the 4.0.0-alpha-1 release, this release contains many argument group-related bugfixes.
See the [4.0.0-alpha-1 New and Noteworthy section](#4.0.0-alpha-1-new) below for more details on argument groups.

_Please try this and provide feedback. We can still make changes._

_What do you think of the annotations API? What about the programmatic API? Does it work as expected? Are the input validation error messages correct and clear? Is the documentation clear and complete? Anything you want to change or improve? Any other feedback?_

This release also has support for variable expansion and improved support for Chinese, Japanese and Korean.

Many thanks to the picocli community for the contributions!

This is the fifty-third public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.0-alpha-2"></a> Table of Contents
* [New and noteworthy](#4.0.0-alpha-2-new)
* [Fixed issues](#4.0.0-alpha-2-fixes)
* [Deprecations](#4.0.0-alpha-2-deprecated)
* [Potential breaking changes](#4.0.0-alpha-2-breaking-changes)

## <a name="4.0.0-alpha-2-new"></a> New and Noteworthy

### Argument Groups

Many bugfixes for argument groups.

### Variable Interpolation

From this release, picocli supports variable interpolation (variable expansion) in annotation attributes as well as in text attributes of the programmatic API.

#### Variable Interpolation Example

```java
@Command(name = "status", description = "This command logs the status for ${PARENT-COMMAND-NAME}.")
class Status {
    @Option(names = {"${dirOptionName1:--d}", "${dirOptionName2:---directories}"}, // -d or --directories
            description = {"Specify one or more directories, separated by '${sys:path.separator}'.",
                           "The default is the user home directory (${DEFAULT-VALUE})."},
            arity = "${sys:dirOptionArity:-1..*}",
            defaultValue = "${sys:user.home}",
            split = "${sys:path.separator}")
    String[] directories;
}
```

#### Predefined Variables

The following variables are predefined:

* `${DEFAULT-VALUE}`: (since 3.2) - can be used in the description for an option or positional parameter, replaced with the default value for that option or positional parameter
* `${COMPLETION-CANDIDATES}`: (since 3.2) - can be used in the description for an option or positional parameter, replaced with the completion candidates for that option or positional parameter
* `${COMMAND-NAME}`: (since 4.0) - can be used in any section of the usage help message for a command, replaced with the name of the command
* `${COMMAND-FULL-NAME}`: (since 4.0) - can be used in any section of the usage help message for a command, replaced with the fully qualified name of the command (that is, preceded by its parent fully qualified name)
* `${PARENT-COMMAND-NAME}`: (since 4.0) - can be used in any section of the usage help message for a command, replaced with the name of its parent command
* `${PARENT-COMMAND-FULL-NAME}`: (since 4.0) - can be used in any section of the usage help message for a command, replaced with the fully qualified name of its parent command (that is, preceded by the name(s) of the parent command's ancestor commands)

#### Custom Variables

In addition, you can define your own variables. Currently the following syntaxes are supported:

* `${sys:key}`: system property lookup, replaced by the value of `System.getProperty("key")`
* `${env:key}`: environment variable lookup, replaced by the value of `System.getEnv("key")`
* `${bundle:key}`: look up the value of `key` in the resource bundle of the command
* `${key}`: search all of the above, first system properties, then environment variables, and finally the resource bundle of the command

#### Default Values for Custom Variables

You can specify a default value to use when no value is found for a custom variable. The syntax for specifying a default is `${a:-b}`, where `a` is the variable name and `b` is the default value to use if `a` is not found.

So, for the individual lookups, this looks like this:

```
${key:-defaultValue}
${sys:key:-defaultValue}
${env:key:-defaultValue}
${bundle:key:-defaultValue}
```

The default value may contain other custom variables. For example:

```
${bundle:a:-${env:b:-${sys:c:-X}}}
```

The above variable is expanded as follows. First, try to find key `a` in the command's resource bundle. If `a` is not found in the resource bundle, get the value of environment variable `b`. If no environment variable `b` exists, get the value of system property `c`. Finally, no system property `c` exists, the value of the expression becomes `X`.

#### Escaping Variables
Sometimes you want to show a string like `"${VAR}"` in a description.
A `$` character can be escaped with another `$` character. Therefore, `$${VAR}` will not be interpreted as a `VAR` variable, but will be replaced by `${VAR}` instead.

#### Switching Off Variable Interpolation

Variable interpolation can be switched off for the full command hierarchy by calling `CommandLine.setInterpolateVariables(false)`, or for a particular command by calling `CommandSpec.interpolateVariables(false)`.

#### Limitations of Variable Interpolation

Some attribute values need to be resolved early, when the model is constructed from the annotation values.

Specifically:

* command names and aliases, option names, mixin names
* `arity` (for options and positional parameters)
* `index` (for positional parameters)
* `separator` (for commands)

It is possible for these attributes to contain variables, but be aware of the limitations.

If these attributes have variables, and the variables get a different value after the model is constructed, the change will not be reflected in the model.



### Improved Support for Chinese, Japanese and Korean
Picocli will align the usage help message to fit within some user-defined width (80 columns by default).
A number of characters in Chinese, Japanese and Korean (CJK) are wider than others.
If those characters are treated to have the same width as other characters, the usage help message may extend past the right margin.

From this release, picocli will use 2 columns for these wide characters when calculating where to put line breaks, resulting in better usage help message text.

This can be switched off with `CommandLine.setAdjustLineBreaksForWideCJKCharacters(false)`.

## <a name="4.0.0-alpha-2-fixes"></a> Fixed issues
- [#495] Publish picocli as a JPMS module in a new artifact `picocli-core-module`. Thanks to [Warkdev](https://github.com/Warkdev) for the pull request.
- [#21] Count double-width Asian characters as two characters for line-breaking purposes.
- [#526] Add support for variable interpolation in message strings. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the suggestion.
- [#660] Added `@java.lang.annotation.Inherited` to the `@picocli.CommandLine.Command` annotation. Thanks to [Devin Smith](https://github.com/devinrsmith) for the suggestion.
- [#661] Bugfix for stack overflow when option in an argument group had a default value. Thanks to [Andreas Deininger](https://github.com/deining) for reporting this.
- [#656] Bugfix for issue where synopsis for composite argument groups did not expand for n..* (n > 1). Thanks to Arno Tuomainen for finding this issue.
- [#654] Bugfix: argument group heading text was not retrieved from ResourceBundle. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#635] Bugfix in argument group validation: did not show an error if some but not all parts of a co-occurring group were specified. Thanks to [Philipp Hanslovsky](https://github.com/hanslovsky) for the pull request.
- [#653] Bugfix: argument group validation should be skipped if help was requested. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#655] Bugfix: argument group validation silently accepts missing subgroup with multiplicity=1.
- [#652] Documentation: fixes in user manual. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
- [#651] Documentation: fixes in user manual. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.

## <a name="4.0.0-alpha-2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.0.0-alpha-2-breaking-changes"></a> Potential breaking changes
The following classes and methods introduced in 4.0.0-alpha-1 have been renamed:

Classes:

* `picocli.CommandLine.ParseResult.MatchedGroup` -> `picocli.CommandLine.ParseResult.GroupMatchContainer`
* `picocli.CommandLine.ParseResult.MatchedGroupMultiple` -> `picocli.CommandLine.ParseResult.GroupMatch`

Methods:

* `ParseResult::getMatchedGroupMultiples` has been renamed to `ParseResult::getGroupMatches`
* `ParseResult::findMatchedGroup(ArgGroupSpec)` has been renamed to `ParseResult::findMatches(ArgGroupSpec)`

Removed:

These may be implemented in a future version.

* `picocli.CommandLine.Option.excludes()` and `picocli.CommandLine.Parameters.excludes()`
* `picocli.CommandLine.Option.needs(()` and `picocli.CommandLine.Parameters.needs(()`

# <a name="3.9.6"></a> Picocli 3.9.6
The picocli community is pleased to announce picocli 3.9.6.

This release improves support for interactive (password) options:

* interactive options can now use type `char[]` instead of String, to allow applications to null out the array after use so that sensitive information is no longer resident in memory
* interactive options can be optionally interactive if configured with `arity = "0..1"`

This is the fifty-second public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.6"></a> Table of Contents
* [New and noteworthy](#3.9.6-new)
* [Fixed issues](#3.9.6-fixes)
* [Deprecations](#3.9.6-deprecated)
* [Potential breaking changes](#3.9.6-breaking-changes)

## <a name="3.9.6-new"></a> New and Noteworthy

This release improves support for interactive (password) options:

* interactive options can now use type `char[]` instead of String, to allow applications to null out the array after use so that sensitive information is no longer resident in memory
* interactive options can be optionally interactive if configured with `arity = "0..1"`


For example, if an application has these options:

```java
@Option(names = "--user")
String user;

@Option(names = "--password", arity = "0..1", interactive = true)
char[] password;
```

With the following input, the `password` field will be initialized to `"123"` without prompting the user for input:

```
--password 123 --user Joe
```

However, if the password is not specified, the user will be prompted to enter a value. In the following example, the password option has no parameter, so the user will be prompted to type in a value on the console:

```
--password --user Joe
```

## <a name="3.9.6-fixes"></a> Fixed issues
* [#657] Support type `char[]` for interactive options. Thanks to [Lukáš Petrovický](https://github.com/triceo) for raising this issue.
* [#536] Support optionally interactive options. Thanks to [Lukas Heumos](https://github.com/Zethson) for raising this issue.

## <a name="3.9.6-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.6-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="4.0.0-alpha-1"></a> Picocli 4.0.0-alpha-1
The picocli community is pleased to announce picocli 4.0.0-alpha-1.

This release adds support for argument groups (incubating). Argument groups enable the following:

* mutually exclusive options
* options that must co-occur (dependent options)
* option sections in the usage help message
* repeating composite arguments

See the [New and Noteworthy section](#4.0.0-alpha-1-new) below for more details.

_Please try this and provide feedback. We can still make changes._

_What do you think of the annotations API? What about the programmatic API? Does it work as expected? Are the input validation error messages correct and clear? Is the documentation clear and complete? Anything you want to change or improve? Any other feedback?_

Many thanks to the picocli community members who contributed!

This is the fifty-first public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="4.0.0-alpha-1"></a> Table of Contents
* [New and noteworthy](#4.0.0-alpha-1-new)
* [Fixed issues](#4.0.0-alpha-1-fixes)
* [Deprecations](#4.0.0-alpha-1-deprecated)
* [Potential breaking changes](#4.0.0-alpha-1-breaking-changes)

## <a name="4.0.0-alpha-1-new"></a> New and Noteworthy
### <a name="4.0.0-alpha-1-new-arggroups"></a> Argument Groups (Incubating)

This release introduces a new `@ArgGroup` annotation and its `ArgGroupSpec` programmatic equivalent.

Argument Groups can be used to define:

* mutually exclusive options
* options that must co-occur (dependent options)
* option sections in the usage help message
* repeating composite arguments

To create a group using the annotations API, annotate a field or method with `@ArgGroup`.
The field's type refers to the class containing the options and positional parameters in the group.
(For annotated interface methods this would be the return type, for annotated setter methods in a concrete class this would be the setter's parameter type.)

Picocli will instantiate this class as necessary to capture command line argument values in the `@Option` and `@Parameters`-annotated fields and methods of this class.

#### Mutually Exclusive Options

Annotate a field or method with `@ArgGroup(exclusive = true)` to create a group of mutually exclusive options and positional parameters. For example:

```java
@Command(name = "exclusivedemo")
public class MutuallyExclusiveOptionsDemo {

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }
}
```

The above example defines a command with mutually exclusive options `-a`, `-b` and `-c`.

The group itself has a `multiplicity` attribute that defines how many times the group may be specified within the command.
The default is `multiplicity = "0..1"`, meaning that by default a group may be omitted or specified once.
In this example the group has `multiplicity = "1"`, so the group must occur once: one of the exclusive options must occur on the command line.

The synopsis of this command is `exclusivedemo (-a=<a> | -b=<b> | -c=<c>)`.

Note that the options are defined as `required = true`; this means required _within the group_, not required within the command.

Picocli will validate the arguments and throw a `MutuallyExclusiveArgsException` if multiple mutually exclusive arguments were specified. For example:

```java
MutuallyExclusiveOptionsDemo example = new MutuallyExclusiveOptionsDemo();
CommandLine cmd = new CommandLine(example);

try {
    cmd.parseArgs("-a=1", "-b=2");
} catch (MutuallyExclusiveArgsException ex) {
    assert "Error: -a=<a>, -b=<b> are mutually exclusive (specify only one)"
            .equals(ex.getMessage());
}
```

For the above group, only one of the options can be specified. Any other combination of options, or the absence of options, is invalid.

#### Co-occurring (Dependent) Options

Annotate a field or method with `@ArgGroup(exclusive = false)` to create a group of dependent options and positional parameters that must co-occur. For example:

```java
@Command(name = "co-occur")
public class DependentOptionsDemo {

    @ArgGroup(exclusive = false)
    Dependent dependent;

    static class Dependent {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }
}
```

The above example defines a command with dependent options `-a`, `-b` and `-c` that must co-occur.

The group itself has a `multiplicity` attribute that defines how many times the group may be specified within the command.
In this example the group uses the default multiplicity, `multiplicity = "0..1"`, meaning that the group may be omitted or specified once.

The synopsis of this command is `co-occur [-a=<a> -b=<b> -c=<c>]`.

Note that the options are defined as `required = true`; this means required _within the group_, not required within the command.

Picocli will validate the arguments and throw a `MissingParameterException` if not all dependent arguments were specified. For example:

```java
DependentOptionsDemo example = new DependentOptionsDemo();
CommandLine cmd = new CommandLine(example);

try {
    cmd.parseArgs("-a=1", "-b=2");
} catch (MissingParameterException ex) {
    assert "Error: Missing required argument(s): -c=<c>".equals(ex.getMessage());
}
```

#### Option Sections in Usage Help

The example below uses groups to define options sections in the usage help.
When a group has a non-null `heading` (or `headingKey`), the options in the group are given the specified heading in the usage help message.
The `headingKey` attribute can be used to get the heading text from the command's resource bundle.

This works for mutually exclusive or co-occurring groups, but it is also possible to define a group that does no validation but only creates an option section in the usage help.

Annotate a field or method with `@ArgGroup(validate = false)` to create a group for display purposes only. For example:

```java
@Command(name = "sectiondemo", description = "Section demo")
public class OptionSectionDemo {

    @ArgGroup(validate = false, heading = "This is the first section%n")
    Section1 section1;

    static class Section1 {
        @Option(names = "-a", description = "Option A") int a;
        @Option(names = "-b", description = "Option B") int b;
        @Option(names = "-c", description = "Option C") int c;
    }

    @ArgGroup(validate = false, heading = "This is the second section%n")
    Section2 section2;

    static class Section2 {
        @Option(names = "-x", description = "Option X") int x;
        @Option(names = "-y", description = "Option Y") int y;
        @Option(names = "-z", description = "Option X") int z;
    }

    public static void main(String[] args) {
        new CommandLine(new OptionSectionDemo()).usage(System.out);
    }
}
```

This prints the following usage help message:
```
Usage: sectiondemo [-a=<a>] [-b=<b>] [-c=<c>] [-x=<x>] [-y=<y>] [-z=<z>]
Section demo
This is the first section
  -a=<a>    Option A
  -b=<b>    Option B
  -c=<c>    Option C
This is the second section
  -x=<x>    Option X
  -y=<y>    Option Y
  -z=<z>    Option X
```

Note that the heading text must end with `%n` to insert a newline between the heading text and the first option.
This is for consistency with other headings in the usage help, like `@Command(headerHeading = "Usage:%n", optionListHeading = "%nOptions:%n")`.

#### Repeating Composite Argument Groups

The below example shows how groups can be composed of other groups, and how arrays and collections can be used to capture repeating groups (with a `multiplicity` greater than one):

```java
public class CompositeGroupDemo {

    @ArgGroup(exclusive = false, multiplicity = "1..*")
    List<Composite> composites;

    static class Composite {
        @ArgGroup(exclusive = false, multiplicity = "1")
        Dependent dependent;

        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;
    }

    static class Dependent {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }

    static class Exclusive {
        @Option(names = "-x", required = true) boolean x;
        @Option(names = "-y", required = true) boolean y;
        @Option(names = "-z", required = true) boolean z;
    }
}
```

In the above example, the annotated `composites` field defines a composite group that must be specified at least once, and may be specified many times, on the command line.
Each time the group is matched, picocli creates an instance of the `Composite` class and adds it to the `composites` list.

The `Composite` class itself contains two groups: a group of dependent options that must co-occur, and another group of mutually exclusive options.
Both of these subgroups have `multiplicity = "1"`, so they can occur exactly once for each multiple of the `Composite` group. The below example illustrates:

```java
CompositeGroupDemo example = new CompositeGroupDemo();
CommandLine cmd = new CommandLine(example);

cmd.parseArgs("-x", "-a=1", "-b=1", "-c=1", "-a=2", "-b=2", "-c=2", "-y");
assert example.composites.size() == 2;

Composite c1 = example.composites.get(0);
assert c1.exclusive.x;
assert c1.dependent.a == 1;
assert c1.dependent.b == 1;
assert c1.dependent.c == 1;

Composite c2 = example.composites.get(1);
assert c2.exclusive.y;
assert c2.dependent.a == 2;
assert c2.dependent.b == 2;
assert c2.dependent.c == 2;
```

#### Positional Parameters

When a `@Parameters` positional parameter is part of a group, its `index` is the index _within the group_, not within the command.


#### Limitations and Points of Caution

* Options with the same name cannot be defined in multiple groups. Similarly, it is not possible to define an option outside of a group with the same name as a different option that is part of a group.
* Positional parameters in a single group work fine, but take care (or avoid) defining positional parameters in multiple groups or positional parameters in a group as well as outside a group. Positional parameters are matched by index, and while the index of a group is reset when a new group multiple is encountered, the index of positional parameters outside a group only increases and is never reset.


## <a name="4.0.0-alpha-1-fixes"></a> Fixed issues
- [#643] Change `%` to `%%` when using `${DEFAULT-VALUE}` in option description. Thanks to [Steffen Rehberg](https://github.com/StefRe) for the pull request.
- [#638] Document fallback descriptionKey for options and parameters in user manual. Thanks to [Mikusch](https://github.com/Mikusch) for the suggestion.
- [#199] mutually exclusive options
- [#295] options that must co-occur (dependent options)
- [#450] option grouping in the usage help message
- [#358] (also [#635]) repeating composite arguments (this should also cover the use cases presented in #454 and #434 requests for repeatable subcommands)

## <a name="4.0.0-alpha-1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="4.0.0-alpha-1-breaking-changes"></a> Potential breaking changes
No breaking changes in this release.


# <a name="3.9.5"></a> Picocli 3.9.5
The picocli community is pleased to announce picocli 3.9.5.

This release contains a critical workaround to protect against JVM crashes when running on RedHat Linux 3.10.0-327.44.2.el7.x86_64.

Picocli 3.9.0 introduced a change in the heuristics for emitting ANSI escape characters. As part of this change, picocli may load the `org.fusesource.jansi.AnsiConsole` class from the JAnsi library when not running on Windows. This may crash the JVM (see [fusesource/jansi-native#17](https://github.com/fusesource/jansi-native/issues/17)).

The workaround in this release is to only load the `AnsiConsole` class when running on Windows.

Users using 3.9.0 and higher are strongly recommended to upgrade to 3.9.5 or later.


This is the fiftieth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.5"></a> Table of Contents
* [New and noteworthy](#3.9.5-new)
* [Fixed issues](#3.9.5-fixes)
* [Deprecations](#3.9.5-deprecated)
* [Potential breaking changes](#3.9.5-breaking-changes)

## <a name="3.9.5-new"></a> New and Noteworthy


## <a name="3.9.5-fixes"></a> Fixed issues
- [#630] Avoid loading `org.fusesource.jansi.AnsiConsole` when not running on Windows to avoid JVM crashes on non-Windows platforms.
- [#632] ReflectionConfigGenerator now specifies the `allowWrite = true` attribute for final fields.

## <a name="3.9.5-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.5-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.9.4"></a> Picocli 3.9.4
The picocli community is pleased to announce picocli 3.9.4.

This release contains bugfixes and enhancements.

From this release, `enum`-typed options and positional parameters that are multi-value can be stored in `EnumSet` collections (in addition to other Collections, arrays and Maps).

Also, a better error message is now shown when unknown options are encountered while processing clustered short options. The new error message includes both the failing part and the original command line argument.

Bugfixes:
* `ReflectionConfigGenerator` incorrectly listed superclass fields as fields of the concrete subclass, causing "GraalVM error: Error parsing reflection configuration in json" when creating a native image.
* Method subcommands in commands that subclass another command caused `InitializationException`.


This is the forty-ninth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.4"></a> Table of Contents
* [New and noteworthy](#3.9.4-new)
* [Fixed issues](#3.9.4-fixes)
* [Deprecations](#3.9.4-deprecated)
* [Potential breaking changes](#3.9.4-breaking-changes)

## <a name="3.9.4-new"></a> New and Noteworthy


## <a name="3.9.4-fixes"></a> Fixed issues
- [#628] Add support for collecting `enum` multi-value options and positional parameters in `EnumSet<>` collections. Thanks to [Lee Atkinson](https://github.com/leeatkinson) for raising this.
- [#619] Bugfix: Method subcommands in commands that subclass another command caused `InitializationException`: "Another subcommand named 'method' already exists...". Thanks to [PorygonZRocks](https://github.com/PorygonZRocks) for the bug report.
- [#622] Bugfix: `ReflectionConfigGenerator` incorrectly listed superclass fields as fields of the concrete subclass, causing "GraalVM error: Error parsing reflection configuration in json". Thanks to [Sebastian Thomschke](https://github.com/sebthom) for the bug report.
- [#623] `ReflectionConfigGenerator` now generates json in alphabetic order.
- [#627] Improve error message for unknown options when processing clustered short options.

## <a name="3.9.4-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.4-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="3.9.3"></a> Picocli 3.9.3
The picocli community is pleased to announce picocli 3.9.3.

This release contains bugfixes and enhancements.


This is the forty-eight public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.3"></a> Table of Contents
* [New and noteworthy](#3.9.3-new)
* [Fixed issues](#3.9.3-fixes)
* [Deprecations](#3.9.3-deprecated)
* [Potential breaking changes](#3.9.3-breaking-changes)

## <a name="3.9.3-new"></a> New and Noteworthy


## <a name="3.9.3-fixes"></a> Fixed issues
- [#613] Enhancement: Improve picocli heuristics for unmatched options: single-character arguments that don't exactly match options (like `-`) should be considered positional parameters. Thanks to [Oliver Weiler](https://github.com/helpermethod) for the bug report.
- [#615] Bugfix: Opaque stacktrace for "%" in Option description. Thanks to [petermr](https://github.com/petermr) for the bug report.
- [#616] Bugfix: showDefaultValues=true with defaultValueProvider did not render defaultValues in usage help. Thanks to [Sebastian Thomschke](https://github.com/sebthom) for the bug report.

## <a name="3.9.3-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.3-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.9.2"></a> Picocli 3.9.2
The picocli community is pleased to announce picocli 3.9.2.

This release contains bugfixes and enhancements.

Picocli now has a mailing list `picocli at googlegroups dot com`. Alternatively visit the [picocli Google group](https://groups.google.com/d/forum/picocli) web interface.

The user manual has improved documentation for internationalization and localization, and the section on Dependency Injection now has a Spring Boot example and link to the Micronaut user manual.

Bugfixes: `AutoComplete` now uses the specified `IFactory` correctly for `CommandLine`; defaulting `usageHelp` or `versionHelp` options no longer prevents validation of required options; and usage help for booleans options with `arity = "1"` now correctly show the option parameter in the synopsis.

Many thanks to the many members of the picocli community who contributed pull requests, bug reports and participated in discussions!

This is the forty-seventh public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.2"></a> Table of Contents
* [New and noteworthy](#3.9.2-new)
* [Fixed issues](#3.9.2-fixes)
* [Deprecations](#3.9.2-deprecated)
* [Potential breaking changes](#3.9.2-breaking-changes)

## <a name="3.9.2-new"></a> New and Noteworthy

Picocli now has a mailing list `picocli at googlegroups dot com`. Alternatively visit the [picocli Google group](https://groups.google.com/d/forum/picocli) web interface.

The user manual has improved documentation for internationalization and localization. Dependency Injection is now a top-level section and now has a Spring Boot example and link to the Micronaut user manual.

## <a name="3.9.2-fixes"></a> Fixed issues
- [#602] Make `CommandLine` in `AutoComplete` use correct `IFactory` implementation. Thanks to [Mikołaj Krzyżanowski](https://github.com/MikolajK) for the pull request.
- [#608] Bugfix: defaulting `usageHelp` or `versionHelp` options incorrectly prevented validation of required options and positional parameters. Thanks to [Pietro Braione](https://github.com/pietrobraione) for the bug report.
- [#612] Bugfix: Usage help for booleans options with `arity = "1"` now correctly show the option parameter in synopsis. Thanks to [prewersk](https://github.com/prewersk) for the bug report.
- [#606] Doc: Added subcommand example. Thanks to [Andreas Deininger](https://github.com/deining) for the pull request.
- [#605] Doc: Improved documentation for internationalization and localization. Thanks to [Andreas Deininger](https://github.com/deining) for raising this.
- [#604] Doc: Improve user manual section on Dependency Injection: add Spring Boot example. Thanks to [Alistair Rutherford](https://github.com/alistairrutherford) for the example code.
- [#610] Build: add JDKs to Travis CI build.
- [#609] Created mailing list `picocli at googlegroups dot com`: [picocli Google group](https://groups.google.com/d/forum/picocli).

## <a name="3.9.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.9.1"></a> Picocli 3.9.1
The picocli community is pleased to announce picocli 3.9.1.

The `picocli.AutoComplete` application no longer calls `System.exit()` unless requested by setting system property `picocli.autocomplete.systemExitOnError` or `picocli.autocomplete.systemExitOnSuccess` to any value other than `false`. Applications that rely on the exit codes introduced in picocli 3.9.0 need to set these system properties.

This release adds support for quoted map keys with embedded '=' characters.

This release contains bugfixes and enhancements.

This is the forty-sixth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.1"></a> Table of Contents
* [New and noteworthy](#3.9.1-new)
* [Fixed issues](#3.9.1-fixes)
* [Deprecations](#3.9.1-deprecated)
* [Potential breaking changes](#3.9.1-breaking-changes)

## <a name="3.9.1-new"></a> New and Noteworthy


## <a name="3.9.1-fixes"></a> Fixed issues
- [#592] Error message now shows `enum` constant names, not `toString()` values, after value mismatch. Thanks to [startewho](https://github.com/startewho) for the bug report.
- [#591] Replace some String concatenation in `picocli.AutoComplete` with StringBuilder. Thanks to [Sergio Escalante](https://github.com/sergioescala) for the pull request.
- [#594] Add support for quoted map keys with embedded '=' characters. Thanks to [Pubudu Fernando](https://github.com/pubudu91) for the suggestion.
- [#596] `picocli.AutoComplete` should not call `System.exit()` unless requested. Thanks to [Markus Heiden](https://github.com/markusheiden), [Bob Tiernay](https://github.com/bobtiernay-okta) and [RobertZenz](https://github.com/RobertZenz) for analysis and ideas contributing to the solution.
- [#593] Use Gradle Bintray Plugin to publish artifacts to Bintray.

## <a name="3.9.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.1-breaking-changes"></a> Potential breaking changes
The `picocli.AutoComplete` application no longer calls `System.exit()` unless requested by setting system property `picocli.autocomplete.systemExitOnError` or `picocli.autocomplete.systemExitOnSuccess` to any value other than `false`.
Applications that rely on the exit codes introduced in picocli 3.9.0 need to set these system properties.

The new support for quoted map keys with embedded '=' characters [#594] may impact some existing applications.
If `CommandLine::setTrimQuotes()` is set to `true`, quotes are now removed from map keys and map values. This did not use to be the case.

For example:

```java
class App {
    @Option(names = "-p") Map<String, String> map;
}
```
When `CommandLine::setTrimQuotes()` was set to `true`, given input like the below:

```
-p AppOptions="-Da=b -Dx=y"
```
The above used to result in a map with key `AppOptions` and value `"-Da=b -Dx=y"` (including the quotes), but the same program and input now results in a map with key `AppOptions` and value `-Da=b -Dx=y` (without quotes).

Also, when `CommandLine::setTrimQuotes()` is `false` (the default), input like the below will now cause a `ParameterException` ("value should be in KEY=VALUE format"):

```
-p "AppOptions=-Da=b -Dx=y"
```
Prior to this release, the above was silently ignored (no errors but also no key-value pairs in the resulting map).


# <a name="3.9.0"></a> Picocli 3.9.0
The picocli community is pleased to announce picocli 3.9.0.

This release contains bugfixes and enhancements in the main picocli module, and adds a new module: `picocli-shell-jline3`.

The new module Picocli Shell JLine3 (`picocli-shell-jline3`) contains components and documentation for building
interactive shell command line applications with JLine 3 and picocli.

This release contains API enhancements to allow customization of the usage help message:

* help section renderers can be added, replaced or removed
* help section keys to reorder sections in the usage help message
* help factory to create custom `Help` instances
* option order attribute to reorder options in the usage help message option list

This release also has improved heuristics to decide whether ANSI escape codes should be emitted or not.

The simplified @-file (argument file) format is now fully compatible with JCommander: empty lines are ignored and comments may start with leading whitespace.

The `picocli.Autocompletion` application now accepts a parameter specifying a custom factory, and returns a non-zero exit code on error, to facilitate incorporating it into the build.

Bug fixes in this release:

* `@Command` method options and positional parameter values are now cleared correctly when reusing a `CommandLine` instance
* the default exception handler now correctly respects the exit code for all exceptions

Finally, this release improves internal quality and robustness by increasing the test code coverage. About 300 tests were added to bring the total to 1300+ tests. This improved line coverage to 98% (was 88%) and complexity coverage to 98% (was 82%).

This is the forty-fifth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.9.0"></a> Table of Contents
* [New and noteworthy](#3.9.0-new)
* [Fixed issues](#3.9.0-fixes)
* [Deprecations](#3.9.0-deprecated)
* [Potential breaking changes](#3.9.0-breaking-changes)

## <a name="3.9.0-new"></a> New and Noteworthy

### <a name="3.9.0-helpsectionrenderer"></a> Help Section Renderer API

This release introduces new API to facilitate customizing the usage help message: `IHelpFactory` allows applications to plug in `Help` subclasses, and `IHelpSectionRenderer` allows applications to add custom sections to the usage help message, or redefine existing sections.

The usage help message is no longer hard-coded, but is now constructed from the section renderers defined in `CommandLine::getHelpSectionMap` (or `UsageMessageSpec::sectionMap` for a single `CommandSpec`).

By default this map contains the predefined section renderers:

```java
// The default section renderers delegate to methods in Help for their implementation
// (using Java 8 lambda notation for brevity):
Map<String, IHelpSectionRenderer> map = new HashMap<>();
map.put(SECTION_KEY_HEADER_HEADING,         help -> help.headerHeading());
map.put(SECTION_KEY_HEADER,                 help -> help.header());

//e.g. Usage:
map.put(SECTION_KEY_SYNOPSIS_HEADING,       help -> help.synopsisHeading());

//e.g. <cmd> [OPTIONS] <subcmd> [COMMAND-OPTIONS] [ARGUMENTS]
map.put(SECTION_KEY_SYNOPSIS,               help -> help.synopsis(help.synopsisHeadingLength()));

//e.g. %nDescription:%n%n
map.put(SECTION_KEY_DESCRIPTION_HEADING,    help -> help.descriptionHeading());

//e.g. {"Converts foos to bars.", "Use options to control conversion mode."}
map.put(SECTION_KEY_DESCRIPTION,            help -> help.description());

//e.g. %nPositional parameters:%n%n
map.put(SECTION_KEY_PARAMETER_LIST_HEADING, help -> help.parameterListHeading());

//e.g. [FILE...] the files to convert
map.put(SECTION_KEY_PARAMETER_LIST,         help -> help.parameterList());

//e.g. %nOptions:%n%n
map.put(SECTION_KEY_OPTION_LIST_HEADING,    help -> help.optionListHeading());

//e.g. -h, --help   displays this help and exits
map.put(SECTION_KEY_OPTION_LIST,            help -> help.optionList());

//e.g. %nCommands:%n%n
map.put(SECTION_KEY_COMMAND_LIST_HEADING,   help -> help.commandListHeading());

//e.g.    add       adds the frup to the frooble
map.put(SECTION_KEY_COMMAND_LIST,           help -> help.commandList());
map.put(SECTION_KEY_FOOTER_HEADING,         help -> help.footerHeading());
map.put(SECTION_KEY_FOOTER,                 help -> help.footer());
```

Applications can add, remove or replace sections in this map. The `CommandLine::getHelpSectionKeys` method (or `UsageMessageSpec::sectionKeys` for a single `CommandSpec`) returns the section keys in the order that the usage help message should render the sections. The default keys are (in order):
1. SECTION_KEY_HEADER_HEADING
1. SECTION_KEY_HEADER
1. SECTION_KEY_SYNOPSIS_HEADING
1. SECTION_KEY_SYNOPSIS
1. SECTION_KEY_DESCRIPTION_HEADING
1. SECTION_KEY_DESCRIPTION
1. SECTION_KEY_PARAMETER_LIST_HEADING
1. SECTION_KEY_PARAMETER_LIST
1. SECTION_KEY_OPTION_LIST_HEADING
1. SECTION_KEY_OPTION_LIST
1. SECTION_KEY_COMMAND_LIST_HEADING
1. SECTION_KEY_COMMAND_LIST
1. SECTION_KEY_FOOTER_HEADING
1. SECTION_KEY_FOOTER

This ordering may be modified with the `CommandLine::setHelpSectionKeys` setter method (or `UsageMessageSpec::sectionKeys(List)` for a single `CommandSpec`).


### <a name="3.9.0-order"></a> Option `order` Attribute

Options are sorted alphabetically by default, but this can be switched off by specifying `@Command(sortOptions = false)` on the command declaration. This displays options in the order they are declared.

However, when mixing `@Option` methods and `@Option` fields, options do not reliably appear in declaration order.

The `@Option(order = <int>)` attribute can be used to explicitly control the position in the usage help message at which the option should be shown. Options with a lower number are shown before options with a higher number.


### <a name="3.9.0-picocli-shell-jline3"></a> New Module `picocli-shell-jline3`
Picocli Shell JLine3 contains components and documentation for building interactive shell command line applications with JLine 3 and picocli.

This release contains the `picocli.shell.jline3.PicocliJLineCompleter` class.
`PicocliJLineCompleter` is a small component that generates completion candidates to allow users to get command line TAB auto-completion for a picocli-based application running in a JLine 3 shell.
It is similar to the class with the same name in the `picocli.shell.jline2` package in the `picocli-shell-jline2` module.

See the module's [README](https://github.com/remkop/picocli/blob/main/picocli-shell-jline3/README.md) for more details.

### <a name="3.9.0-ANSI-heuristics"></a> Improved ANSI Heuristics
This release has improved heuristics to decide whether ANSI escape codes should be emitted or not.

Support was added for the following environment variables to control enabling ANSI:

* [`NO_COLOR`](https://no-color.org/)
* [`CLICOLOR_FORCE`](https://bixense.com/clicolors/)
* [`CLICOLOR`](https://bixense.com/clicolors/)
* [`ConEmuANSI`](https://conemu.github.io/en/AnsiEscapeCodes.html#Environment_variable)
* [`ANSICON`](https://github.com/adoxa/ansicon/blob/master/readme.txt)

## <a name="3.9.0-fixes"></a> Fixed issues
- [#574] Add `picocli-shell-jline3` module. Thanks to [mattirn](https://github.com/mattirn) for the pull request.
- [#587] Enhance `picocli-shell-jline3` example by using JLine's `DefaultParser` to split lines into arguments. Thanks to [mattirn](https://github.com/mattirn) for the pull request.
- [#567] Usage message customization API initial implementation. Thanks to [Christian Helmer](https://github.com/SysLord) for the pull request.
- [#530] Added API for easily customizing the usage help message. Thanks to [stechio](https://github.com/stechio) for raising the request and productive discussions.
- [#569] Facilitate customization of the synopsis: split `Help.detailedSynopsis()` into protected methods.
- [#508] Annotation API: added `@Option(order = <int>)` attribute to allow explicit control of option ordering in the usage help message; useful when mixing methods and fields with `@Option` annotation.
- [#588] Added method `CommandSpec.names` returning both `name` and `aliases`.
- [#578] Add API for simplified @files argument files.
- [#573] Make simplified @files JCommander-compatible: ignore empty lines and comments starting with whitespace. Thanks to [Lukáš Petrovický](https://github.com/triceo) for the pull request with test to reproduce the issue.
- [#572] `CommandSpec.addMethodSubcommands` now throws `picocli.CommandLine.InitializationException` instead of `java.lang.UnsupportedOperationException` when the user object of the parent command is a `java.lang.reflect.Method`.
- [#581] Added support for ConEmu, ANSICON and other environment variables to improve the ANSI heuristics. Documented the heuristics in the user manual.
- [#579] Improved `AutoComplete` error message when not overwriting existing files.
- [#585] `picocli.AutoComplete` now accepts a parameter specifying a custom `IFactory` implementation. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the suggestion.
- [#582] `picocli.AutoComplete` now returns a non-zero return code on error. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the suggestion.
- [#570] Bugfix: Command method options and positional parameter Object values are now cleared correctly when reusing CommandLine. Thanks to [Christian Helmer](https://github.com/SysLord) for the pull request.
- [#576] Bugfix: fixed StringIndexOutOfBoundsException in shell-jline2 completion when cursor was before `=` when option parameter was attached to option name.
- [#583] Bugfix: Default exception handler now exits on exception if exitCode was set, regardless of exception type.
- [#584] Add documentation for generating autocompletion script during a Maven build. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta).
- [#586] Replace Ansi.Text.clone() with copy constructor.
- [#571] Improve test code coverage. Added ~300 tests to bring the total to 1300+ tests. Improved line coverage to 98% (was 88%) and complexity coverage to 98% (was 82%).
- [#590] Fail the build if test coverage falls below minimum threshold.
- [#589] Fix index.adoc to eliminate warnings; suppress javadoc warnings.
- [#566] Add example showing how to customize the usage help message to show the full command tree including nested subcommands. Thanks to [lgawron](https://github.com/lgawron) for the request.

## <a name="3.9.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.9.0-breaking-changes"></a> Potential breaking changes
`CommandSpec.addMethodSubcommands` now throws `InitializationException` instead of `java.lang.UnsupportedOperationException` when the user object of the parent command is a `java.lang.reflect.Method`.

AutoComplete application now prints different error message when not overwriting existing script files. This may break tests that verify the console output.


# <a name="3.8.2"></a> Picocli 3.8.2
The picocli community is pleased to announce picocli 3.8.2.

This release contains bugfixes only.

When running a native image with Graal, ANSI colors are now shown correctly.

This is the forty-forth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.8.2"></a> Table of Contents
* [New and noteworthy](#3.8.2-new)
* [Fixed issues](#3.8.2-fixes)
* [Deprecations](#3.8.2-deprecated)
* [Potential breaking changes](#3.8.2-breaking-changes)

## <a name="3.8.2-new"></a> New and Noteworthy


## <a name="3.8.2-fixes"></a> Fixed issues
- [#557] Bugfix: No colors are shown when compiling to a native image with Graal on MacOS. Thanks to [Oliver Weiler](https://github.com/helpermethod) for the bug report.

## <a name="3.8.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.8.2-breaking-changes"></a> Potential breaking changes
This is a patch release and has no breaking changes.


# <a name="3.8.1"></a> Picocli 3.8.1
The picocli community is pleased to announce picocli 3.8.1.

This release contains bugfixes and minor enhancements.

Command methods explicitly throwing a `ParametersException` is now correctly handled by picocli, showing the error message and the usage help message.

This release adds support for JCommander-style argument files (one argument per line, no quoting) and better tracing.

Many thanks to the many members of the picocli community who contributed!

This is the forty-third public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.8.1"></a> Table of Contents
* [New and noteworthy](#3.8.1-new)
* [Fixed issues](#3.8.1-fixes)
* [Deprecations](#3.8.1-deprecated)
* [Potential breaking changes](#3.8.1-breaking-changes)

## <a name="3.8.1-new"></a> New and Noteworthy

### <a name="3.8.0-simplified-argument-files"></a> Simplified Argument Files

In this argument file format every line (except comment lines) is interpreted as a single argument. Arguments containing whitespace do not need to be quoted, but it is not possible to have arguments with embedded newlines.

Set system property `picocli.useSimplifiedAtFiles` without a value or with value `"true"` (case-insensitive) to enable this simpler argument file format.

This format is similar to the way JCommander processes argument files, which makes it easier for command line applications to migrate from JCommander to picocli.

### <a name="3.8.1-improved-tracing"></a> Improved Tracing

The following information has been added to the tracing output in this release:

* Version information (picocli version, java version, os version), logged at INFO level
* ANSI enabled status, logged at DEBUG level
* Log at DEBUG level when a Map or Collection binding for an option or positional parameter is initialized with a new instance
* Log at DEBUG level when parameters are being split (into how many parts, show resulting parts)


## <a name="3.8.1-fixes"></a> Fixed issues
- [#551] Enhancement: Add support for JCommander-style argument files (one argument per line, no quoting). Thanks to [Lukáš Petrovický](https://github.com/triceo) for the bug report and unit tests.
- [#562] Enhancement: Allow for enabling quote trimming via system property `picocli.trimQuotes`. Thanks to [Lukáš Petrovický](https://github.com/triceo) for the pull request.
- [#560] Enhancement: Better tracing.
- [#554] Bugfix: Convenience method error handling was broken for command methods that explicitly throw an ParameterException: InvocationTargetException hides the ParameterException. Thanks to [SysLord](https://github.com/SysLord) for the bug report.
- [#553] Doc: Fix broken link to CommandLine.java source code. Thanks to [Simon Legner](https://github.com/simon04) for the pull request.
- [#563] Doc: Improve documentation for explicitly showing usage help from subcommands. Thanks to [Steve Johnson](https://github.com/Blatwurst) for raising this issue.

## <a name="3.8.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.8.1-breaking-changes"></a> Potential breaking changes
This is a patch release and has no breaking changes.


# <a name="3.8.0"></a> Picocli 3.8.0
The picocli community is pleased to announce picocli 3.8.0.

This release contains bugfixes and minor enhancements.

`@Command` methods now support `@Mixin` parameters. `OverwrittenOptionException` now has an accessor for the `ArgSpec` that was overwritten.

The `ReflectionConfigGenerator` tool in the `picocli-codegen` module now correctly generates configuration for `@Mixin` fields.

Many thanks to the many members of the picocli community who contributed!

This is the forty-second public release.
Picocli follows [semantic versioning](http://semver.org/). (This release could have been called 3.7.1 except that it has a minor additional API change, which means it cannot be called a patch release by semver rules.)

## <a name="3.8.0"></a> Table of Contents
* [New and noteworthy](#3.8.0-new)
* [Fixed issues](#3.8.0-fixes)
* [Deprecations](#3.8.0-deprecated)
* [Potential breaking changes](#3.8.0-breaking-changes)

## <a name="3.8.0-new"></a> New and Noteworthy

### <a name="3.8.0-command-method-mixins"></a> Mixin Support in `@Command` Methods

`@Command` methods now accept `@Mixin` parameters. All options and positional parameters defined in the mixin class are added to the command.

Example:

```java
class CommonParams {
    @Option(names = "-x") int x;
    @Option(names = "-y") int y;
}

class App {
    @Command
    public void doit(@Mixin CommonParams params, @Option(names = "-z") int z) {}
}
```

In the above example, the `-x` and `-y` options are added to the other options of the `doit` command.

## <a name="3.8.0-fixes"></a> Fixed issues
- [#525] Enhancement: Allow `@Mixin` parameters in `@Command` methods. Thanks to [Paul Horn](https://github.com/knutwalker) for the pull request.
- [#532] Enhancement: `OverwrittenOptionException` now has an accessor for the `ArgSpec` that was overwritten. Thanks to [Steven Fontaine](https://github.com/acid1103) for the pull request.
- [#524] Enhancement/Bugfix: `ReflectionConfigGenerator` in `picocli-codegen` should generate configuration for `@Mixin` fields. Thanks to [Paul Horn](https://github.com/knutwalker) for the pull request.
- [#301] Enhancement/Bugfix: The subcommand listing now correctly renders `%n` as line breaks in the brief description for each subcommand. Thanks to [Vlad Topala](https://github.com/topalavlad) for the pull request.
- [#523] Bugfix: Array should be initialized before calling setter method. Thanks to [Paul Horn](https://github.com/knutwalker) for the pull request.
- [#527] Bugfix: Quoting logic did not work for some Unicode code points.
- [#531] Bugfix: Usage help should not show space between short option name and parameter (for options that only have a short name).
- [#538] Bugfix: Command methods and interface methods should pass `null` for unmatched primitive wrapper options.
- [#547] Bugfix: Fix infinite loop when print help. Thanks to [Patrick Kuo](https://github.com/patrickkuo) for the pull request.
- [#528] Doc: Javadoc for xxxHandler API referred to non-existant prototypeReturnValue.
- [#545] Doc: Include mention of command methods for options using collections. Thanks to [Bob Tiernay](https://github.com/bobtiernay-okta) for the pull request.


## <a name="3.8.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.8.0-breaking-changes"></a> Potential breaking changes

### Help Layout

The usage help no longer shows a space between short option names and the parameter (for options that only have a short name).
This may break tests that rely on the exact output format.

Before:
```
Usage: times [-l=<arg0>] [-r=<arg1>]
  -l= <arg0>
  -r= <arg1>
```

After:
```
Usage: times [-l=<arg0>] [-r=<arg1>]
  -l=<arg0>
  -r=<arg1>
```

### Unmatched Primitive Wrapper Type Options

Another behavioral change is that command methods now pass in `null` for primitive wrapper options that were not matched on the command line.
This impacts methods annotated with `@Command`, and interface methods annotated with `@Option`. Classes annotated with `@Command` already behaved like this and this has not changed.

This behaviour is now consistent for all annotation-based and programmatic ways of defining commands.


# <a name="3.7.0"></a> Picocli 3.7.0
The picocli community is pleased to announce picocli 3.7.0.

This release contains bugfixes and enhancements in the main picocli module, and adds two new modules:
`picocli-codegen` and `picocli-shell-jline2`.

Picocli Code Generation (`picocli-codegen`) contains tools for generating source code, documentation and configuration files
for picocli-based applications.

Picocli Shell JLine2 (`picocli-shell-jline2`) contains components and documentation for building
interactive shell command line applications with JLine 2 and picocli.

Many thanks to the many members of the picocli community who contributed!

This is the forty-first public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.7.0"></a> Table of Contents
* [New and noteworthy](#3.7.0-new)
* [Fixed issues](#3.7.0-fixes)
* [Deprecations](#3.7.0-deprecated)
* [Potential breaking changes](#3.7.0-breaking-changes)

## <a name="3.7.0-new"></a> New and Noteworthy

### <a name="3.7.0-quoting"></a> Improved Parsing of Quoted Values
This release improves parser behaviour of quoted arguments:

* Quotes around command line parameters are now preserved by default (previously they were removed). This can be configured with `CommandLine::setTrimQuotes`.
* When [splitting](https://picocli.info/#_split_regex) parameters, quoted strings are no longer split. This can be configured with `CommandLine::setSplitQuotedStrings`.

Example:
```
@Option(names = "-x", split = ",")
String[] parts;
```

Given input like below:

```
<command> -x a,b,"c,d,e",f,"xxx,yyy"
```
This results in the `parts` array having the following values:
```
a
b
"c,d,e"
f
"xxx,yyy"
```

### <a name="3.7.0-picocli-codegen"></a> New Module `picocli-codegen`
Picocli Code Generation contains tools for generating source code, documentation and configuration files
for picocli-based applications.

This release contains the `ReflectionConfigGenerator` class.
`ReflectionConfigGenerator` generates a JSON String with the program elements that will be accessed reflectively in a picocli-based application, in order to compile this application ahead-of-time into a native executable with GraalVM.

The output of `ReflectionConfigGenerator` is intended to be passed to the `-H:ReflectionConfigurationFiles=/path/to/reflectconfig` option of the `native-image` GraalVM utility. This allows picocli-based applications to be compiled to a native image.

See [Picocli on GraalVM: Blazingly Fast Command Line Apps](https://github.com/remkop/picocli/wiki/Picocli-on-GraalVM:-Blazingly-Fast-Command-Line-Apps) for details.

The module's [README](https://github.com/remkop/picocli/blob/main/picocli-codegen/README.md) explains how to configure your build to generate the configuration file automatically as part of your build.


### <a name="3.7.0-picocli-shell-jline2"></a> New Module `picocli-shell-jline2`
Picocli Shell JLine2 contains components and documentation for building
interactive shell command line applications with JLine 2 and picocli.

This release contains the `PicocliJLineCompleter` class.
`PicocliJLineCompleter` is a small component that generates completion candidates to allow users to
get command line TAB auto-completion for a picocli-based application running in a JLine 2 shell.

See the module's [README](https://github.com/remkop/picocli/blob/main/picocli-shell-jline2/README.md) for more details.

## <a name="3.7.0-fixes"></a> Fixed issues
- [#503] Build: Upgrade to gradle 4.10.2.
- [#497] add module `picocli-shell-jline2` for components and documentation for building interactive shell command line applications with JLine 2 and picocli.
- [#499] add module `picocli-codegen` for tools to generate documentation, configuration, source code and other files from a picocli model
- [#410] add `ReflectionConfigGenerator` class for GraalVM `native-image`
- [#513] Enhancement: Simplify AutoCompletion script generator code.
- [#481] Enhancement: Add `@Command(usageHelpWidth = <int>)` annotation attribute.
- [#379] Option with split property should not split quoted strings. Thanks to [Markus Kramer](https://github.com/MarkusKramer) for the feature request.
- [#514] Bugfix/Enhancement: picocli no longer removes opening and closing quotes around arguments by default. This is configurable with `CommandLine::setTrimQuotes`. Thanks to [mshatalov](https://github.com/mshatalov) for the bug report.
- [#509] Bugfix: Long boolean options with arity 0 should not allow parameters.  Thanks to [Adam Zegelin](https://github.com/zegelin) for the bug report.
- [#510] Documentation: Fix broken link for moved example files. Thanks to [Anthony Keenan](https://github.com/anthonykeenan) for the pull request.
-  [#24] Documentation: Added Chinese translations of "Picocli 2.0 Do More With Less" and "Picocli 2.0 Groovy Scripts on Steroids".

## <a name="3.7.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.7.0-breaking-changes"></a> Potential breaking changes

From this release, picocli no longer removes opening and closing quotes around arguments by default.
This is configurable with `CommandLine::setTrimQuotes`.


# <a name="3.6.1"></a> Picocli 3.6.1
The picocli community is pleased to announce picocli 3.6.1.

This release contains bugfixes, minor enhancements and documentation improvements.

ANSI is automatically enabled on Windows if Jansi's `AnsiConsole` has been installed.

It is now possible to selectively avoid loading type converters with reflection.

Bugfix: Enum values were not rendered in `${COMPLETION-CANDIDATES}` for collection type options.

Many thanks to the many members of the picocli community who contributed!

This is the fortieth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.6.1"></a> Table of Contents
* [New and noteworthy](#3.6.1-new)
* [Fixed issues](#3.6.1-fixes)
* [Deprecations](#3.6.1-deprecated)
* [Potential breaking changes](#3.6.1-breaking-changes)

## <a name="3.6.1-new"></a> New and Noteworthy

## <a name="3.6.1-fixes"></a> Fixed issues
- [#487] Enhancement: Auto-completion script should return from `generateOptionsSwitch` immediately if there is nothing to generate. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
- [#483][#486] Enhancement: Improve `Help.Ansi.AUTO`: automatically enable ANSI on Windows if Jansi's `AnsiConsole` has been installed. Thanks to [Philippe Charles](https://github.com/charphi) for the pull request.
- [#491] Enhancement: Improve `Help.Ansi.AUTO` cygwin/msys detection on Windows.
- [#451] Enhancement: Selectively disable reflective type converter registration. Thanks to [Paolo Di Tommaso](https://github.com/pditommaso) for the suggestion.
- [#488] Doc: Clarify in user manual that `CommandLine.setPosixClusteredShortOptionsAllowed(false)` means that option parameters cannot be attached to the option name. Thanks to [Maryam Ziyad](https://github.com/MaryamZi) for raising this.
- [#492][#493] Doc: Add section on `@Command(aliases)` attribute to user manual. Thanks to [marinier](https://github.com/marinier) for the pull request.
- [#494] Bugfix: Enum values were not rendered in `${COMPLETION-CANDIDATES}` for collection type options.

## <a name="3.6.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.6.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.6.0"></a> Picocli 3.6.0
The picocli community is pleased to announce picocli 3.6.0.

This release contains new features, bugfixes and enhancements.

New interface: `IDefaultProvider` allows you to get default values from a configuration file or some other central place.

`@Command` Methods: From this release, methods can be annotated with `@Command`. The method parameters provide the command options and parameters.

Internationalization: from this release, usage help message sections and the description for options and positional parameters can be specified in a resource bundle. A resource bundle can be set via annotations and programmatically.

The error message on invalid user input has been improved.

This release also contains various improvements the the bash/zsh completion script generation to be more consistent with standard completion on these shells.

Many thanks to the many members of the picocli community who raised issues and contributed solutions!

This is the thirty-ninth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.6.0"></a> Table of Contents
* [New and noteworthy](#3.6.0-new)
* [Fixed issues](#3.6.0-fixes)
* [Deprecations](#3.6.0-deprecated)
* [Potential breaking changes](#3.6.0-breaking-changes)

## <a name="3.6.0-new"></a> New and Noteworthy
### Default Provider

This release allows you to specify a default provider in the `@Command` annotation:

```java
@Command(defaultValueProvider = MyDefaultProvider.class)
class MyCommand // ...
```

The default provider allows you to get default values from a configuration file or some other central place.
Default providers need to implement the `picocli.CommandLine.IDefaultValueProvider` interface:

```java
public interface IDefaultValueProvider {

    /**
     * Returns the default value for an option or positional parameter or {@code null}.
     * The returned value is converted to the type of the option/positional parameter
     * via the same type converter used when populating this option/positional
     * parameter from a command line argument.
     *
     * @param argSpec the option or positional parameter, never {@code null}
     * @return the default value for the option or positional parameter, or {@code null} if
     *       this provider has no default value for the specified option or positional parameter
     * @throws Exception when there was a problem obtaining the default value
     */
    String defaultValue(ArgSpec argSpec) throws Exception;
}
```

### `@Command` Methods
From picocli 3.6, methods can be annotated with `@Command`. The method parameters provide the command options and parameters. For example:

```java
class Cat {
    public static void main(String[] args) {
        CommandLine.invoke("cat", Cat.class, args);
    }

    @Command(description = "Concatenate FILE(s) to standard output.",
             mixinStandardHelpOptions = true, version = "3.6.0")
    void cat(@Option(names = {"-E", "--show-ends"}) boolean showEnds,
             @Option(names = {"-n", "--number"}) boolean number,
             @Option(names = {"-T", "--show-tabs"}) boolean showTabs,
             @Option(names = {"-v", "--show-nonprinting"}) boolean showNonPrinting,
             @Parameters(paramLabel = "FILE") File[] files) {
        // process files
    }
}
```
The usage help of the above command looks like this:

```
Usage: cat [-EhnTvV] [FILE...]
Concatenate FILE(s) to standard output.
      [FILE...]
  -E, --show-ends
  -h, --help               Show this help message and exit.
  -n, --number
  -T, --show-tabs
  -v, --show-nonprinting
  -V, --version            Print version information and exit.
```
See below for an example that uses a resource bundle to define usage help descriptions outside the code.

For positional parameters, the `@Parameters` annotation may be omitted on method parameters.

TIP: If compiled with the `-parameters` flag on Java 8 or higher, the `paramLabel` of positional parameters is obtained from the method parameter name using reflection instead of the generic arg0, arg1, etc.

#### Subcommand Methods

If the enclosing class is annotated with `@Command`, method commands are added as subcommands to the class command, unless the class command has attribute `@Command(addMethodSubcommands = false)`.
For example:

```java
@Command(name = "git", mixinStandardHelpOptions = true, version = "picocli-3.6.0")
class Git {
    @Option(names = "--git-dir", descriptionKey = "GITDIR")
    Path path;

    @Command
    void commit(@Option(names = {"-m", "--message"}) String commitMessage,
                @Option(names = "--squash", paramLabel = "<commit>") String squash,
                @Parameters(paramLabel = "<file>") File[] files) {
        // ... implement business logic
    }
}
```

Use `@Command(addMethodSubcommands = false)` on the class `@Command` annotation if the `@Command`-annotated methods in this class should not be added as subcommands.

The usage help of the `git commit` command looks like this:

```
Usage: git commit [--squash=<commit>] [-m=<arg0>] [<file>...]
      [<file>...]
      --squash=<commit>
  -m, --message=<arg0>
```


### Internationalization

From version 3.6, usage help message sections and the description for options and positional parameters can be specified in a resource bundle. A resource bundle can be set via annotations and programmatically.

Annotation example:

```java
@Command(name = "i18n-demo", resourceBundle = "my.org.I18nDemo_Messages")
class I18nDemo {}
```

Programmatic example:

```java
@Command class I18nDemo2 {}

CommandLine cmd = new CommandLine(new I18nDemo2());
cmd.setResourceBundle(ResourceBundle.getBundle("my.org.I18nDemo2_Messages"));
```


Resources for multiple commands can be specified in a single ResourceBundle. Keys and their value can be shared by multiple commands (so you don't need to repeat them for every command), but keys can be prefixed with `fully qualified command name + "."` to specify different values for different commands. The most specific key wins.

This is especially convenient for `@Command` methods where long description annotations would make the code less easy to read.

You can use a resource bundle to move the descriptions out of the code:

```
# shared between all commands
help = Show this help message and exit.
version = Print version information and exit.

# command-specific strings
git.usage.description = Version control system
git.GITDIR = Set the path to the repository

git.commit.usage.description = Record changes to the repository
git.commit.message = Use the given <msg> as the commit message.
git.commit.squash = Construct a commit message for use with rebase --autosquash.
git.commit.<file>[0..*] = The files to commit.
```

With this resource bundle, the usage help for the above `git commit` command looks like this:


```
Usage: git commit [--squash=<commit>] [-m=<arg0>] [<file>...]
Record changes to the repository
      [<file>...]         The files to commit.
      --squash=<commit>   Construct a commit message for use with rebase
                            --autosquash.
  -m, --message=<arg0>    Use the given <msg> as the commit message.
```

### Improved Error Messages

The error messages on invalid input have been improved. For example:

Previously, if an argument could not be converted to a primitive type, the error looked like this:

`Could not convert 'abc' to int for option '-num': java.lang.NumberFormatException: For input string: \"abc\"`

The new error message for primitive types looks like this:

`Invalid value for option '-num': 'abc' is not an int`

Previously, if an argument could not be converted to an enum, the error looked like this:

`Could not convert 'xyz' to TimeUnit for option '-timeUnit': java.lang.IllegalArgumentException: No enum constant java.util.concurrent.TimeUnit.xyz`

The new error message for enums looks like this:

`Invalid value for option '-timeUnit': expected one of [NANOSECONDS, MILLISECONDS, MICROSECONDS, SECONDS, MINUTES, HOURS, DAYS] but was 'xyz'`


## <a name="3.6.0-fixes"></a> Fixed issues
- [#321] API: Add support for IDefaultValueProvider. Thanks to [Nicolas MASSART](https://github.com/NicolasMassart) for the pull request.
- [#416] API: Added support for `@Command` annotation on methods (in addition to classes). Thanks to [illes](https://github.com/illes) for the pull request.
- [#433] API: Added method `printHelpIfRequested` that accepts a `ColorScheme` parameter. Thanks to [Benny Bottema](https://github.com/bbottema) for the suggestion.
- [#441] API: Added `hideParamSyntax` attribute to `@Option` and `@Parameters` to allow suppressing usage syntax decorations around the param label. Thanks to [Benny Bottema](https://github.com/bbottema) for the pull request.
- [#22], [#415], [#436] API: Added internationalization and localization support via resource bundles.
- [#473] Enhancement: Improved error messages for invalid input.
- [#461] Bugfix: Script auto-completion only suggests options and never default bash completions. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
- [#466] Bugfix: Script auto-completion should not generate suggestions for options with arguments that have no known completions. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
- [#470] Bugfix: Script auto-completion should generate suggestions for short options with arguments. Thanks to [David Walluck](https://github.com/dwalluck) for the pull request.
- [#444] Bugfix: Usage help shows duplicate aliases if registered with same alias multiple times.
- [#452] Doc: Add UML class diagrams to picocli Javadoc.
- [#475] Doc: Renamed module `examples` to `picocli-examples`.
- [#478] Doc: Add convenience API example to `CommandLine` class Javadoc.

## <a name="3.6.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.6.0-breaking-changes"></a> Potential breaking changes
The error message displayed on invalid input is different from previous releases. This may break unit tests that expect an exact error message.


# <a name="3.5.2"></a> Picocli 3.5.2
The picocli community is pleased to announce picocli 3.5.2.

This is a bugfix release that fixes an issue where subcommand aliases were not recognized in some cases.

This is the thirty-eighth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.5.2"></a> Table of Contents
* [New and noteworthy](#3.5.2-new)
* [Promoted features](#3.5.2-promoted)
* [Fixed issues](#3.5.2-fixes)
* [Deprecations](#3.5.2-deprecated)
* [Potential breaking changes](#3.5.2-breaking-changes)

## <a name="3.5.2-new"></a> New and Noteworthy


## <a name="3.5.2-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.5.2-fixes"></a> Fixed issues
- [#443] Bugfix: Subcommand aliases were not recognized in some cases. Thanks to [K. Alex Mills](https://github.com/kalexmills) for the bug report.

## <a name="3.5.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.5.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.



# <a name="3.5.1"></a> Picocli 3.5.1
The picocli community is pleased to announce picocli 3.5.1.

This is a bugfix release that fixes an issue where CommandSpec injected into Mixins had a `null` CommandLine.

This is the thirty-seventh public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.5.1"></a> Table of Contents
* [New and noteworthy](#3.5.1-new)
* [Promoted features](#3.5.1-promoted)
* [Fixed issues](#3.5.1-fixes)
* [Deprecations](#3.5.1-deprecated)
* [Potential breaking changes](#3.5.1-breaking-changes)

## <a name="3.5.1-new"></a> New and Noteworthy



## <a name="3.5.1-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.5.1-fixes"></a> Fixed issues
- [#439] Bugfix: CommandSpec injected into Mixins had a `null` CommandLine. Thanks to [Adam Zegelin](https://github.com/zegelin) for the bug report.

## <a name="3.5.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.5.1-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.5.0"></a> Picocli 3.5.0
The picocli community is pleased to announce picocli 3.5.0.

This release contains new features, bugfixes and enhancements.

Password support: for options and positional parameters marked as `interactive`, the user is prompted to enter a value on the console.
When running on Java 6 or higher, picocli will use the <a href="https://docs.oracle.com/javase/8/docs/api/java/io/Console.html#readPassword-java.lang.String-java.lang.Object...-"><code>Console.readPassword</code></a> API so that user input is not echoed to the console.

Client code can now perform simple validation in annotated setter methods by throwing a `ParameterException` on invalid input.

Also, from this release, the comment character in @-files (argument files) and the end-of-options delimiter (`--` by default) are configurable.


This is the thirty-sixth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.5.0"></a> Table of Contents
* [New and noteworthy](#3.5.0-new)
* [Promoted features](#3.5.0-promoted)
* [Fixed issues](#3.5.0-fixes)
* [Deprecations](#3.5.0-deprecated)
* [Potential breaking changes](#3.5.0-breaking-changes)

## <a name="3.5.0-new"></a> New and Noteworthy

### <a name="3.5.0-passwords"></a><a name="3.5.0-interactive"></a> `Interactive` Options for Passwords or Passphrases
This release introduces password support: for options and positional parameters marked as `interactive`, the user is prompted to enter a value on the console.
When running on Java 6 or higher, picocli will use the <a href="https://docs.oracle.com/javase/8/docs/api/java/io/Console.html#readPassword-java.lang.String-java.lang.Object...-"><code>Console.readPassword</code></a> API so that user input is not echoed to the console.

Example usage:

```java
class Login implements Callable<Object> {
    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    @Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true)
    String password;

    public Object call() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        System.out.printf("Hi %s, your passphrase is hashed to %s.%n", user, base64(md.digest()));
        return null;
    }

    private String base64(byte[] arr) { /* ... */ }
}
```

When this command is invoked like this:

```java
CommandLine.call(new Login(), "-u", "user123", "-p");
```

Then the user will be prompted to enter a value:

```
Enter value for --password (Passphrase):
```

When running on Java 6 or higher, the user input is not echoed to the console.
After the user enters a value and presses enter, the `call()` method is invoked, which prints the following:

```bash
Hi user123, your passphrase is hashed to 75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=.
```

### <a name="3.5.0-validation"></a> Simple Validation in Setter Methods
Methods annotated with `@Option` and `@Parameters` can do simple input validation by throwing a `ParameterException` when invalid values are specified on the command line.

```java
class ValidationExample {
    private Map<String, String> properties = new LinkedHashMap<>();

    @Spec private CommandSpec spec; // injected by picocli

    @Option(names = {"-D", "--property"}, paramLabel = "KEY=VALUE")
    public void setProperty(Map<String, String> map) {
        for (String key : map.keySet()) {
            String newValue = map.get(key);
            validateUnique(key, newValue);
            properties.put(key, newValue);
        }
    }

    private void validateUnique(String key, String newValue) {
        String existing = properties.get(key);
        if (existing != null && !existing.equals(newValue)) {
            throw new ParameterException(spec.commandLine(),
                    String.format("Duplicate key '%s' for values '%s' and '%s'.",
                    key, existing, newValue));
        }
    }
}
```

Prior to this release, the exception thrown from the method was wrapped in a `java.lang.reflect.InvocationTargetException`, which in turn was wrapped in a `PicocliException`, and finally in another `ParameterException`.

By following the recipe above and throwing a `ParameterException` on invalid input, all these intermediate exceptions are skipped.


## <a name="3.5.0-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.5.0-fixes"></a> Fixed issues
- [#430] Bugfix: formatting was incorrect (did not break on embedded newlines) in the subcommands list descriptions. Thanks to [Benny Bottema](https://github.com/bbottema) for the bug report.
- [#431] Better support for validation in setter methods: cleaner stack trace.
- [#432] Make comment character in @-files (argument files) configurable.
- [#359] Make end-of-options delimiter configurable.
- [#82] Support reading passwords from the console with echoing disabled.

## <a name="3.5.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.5.0-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.

# <a name="3.4.0"></a> Picocli 3.4.0
The picocli community is pleased to announce picocli 3.4.0.

This release contains new features, bugfixes and enhancements.
The parser can now ignore case when parsing arguments for an Enum option or positional parameter.
New methods `Help.Ansi.text(String)` and `Help.Ansi.string(String)` assist client code in easily creating ANSI messages outside usage help and version help.

This is the thirty-fifth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.4.0"></a> Table of Contents
* [New and noteworthy](#3.4.0-new)
* [Promoted features](#3.4.0-promoted)
* [Fixed issues](#3.4.0-fixes)
* [Deprecations](#3.4.0-deprecated)
* [Potential breaking changes](#3.4.0-breaking-changes)

## <a name="3.4.0-new"></a> New and Noteworthy

## <a name="3.4.0-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.4.0-fixes"></a> Fixed issues
- [#14] New API: Support enum values to be parsed in an case-insensitive way.
- [#376] New API: `Help.Ansi.text(String)` and `Help.Ansi.string(String)` help client code easily create ANSI messages outside usage help and version help.
- [#412] Enhancement: Enum constant names are now returned from `ArgSpec::completionCandidates()`. Thanks to [Radovan Panák](https://github.com/rpanak).
- [#417] Enhancement: Ensure bash scripts have correct line separators. Thanks to [Holger Stenger](https://github.com/stengerh).
- [#425] Enhancement: Fix autocomplete script errors in zsh. Thanks to [Anthony Keenan](https://github.com/anthonykeenan).
- [#419] Bugfix: Default value for arrays was not rendered correctly with `@{DEFAULT-VALUE}`.
- [#418] Doc: Improve installation instructions for autocompletion scripts.
- [#420] Doc: Added a Quick Guide


## <a name="3.4.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.4.0-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.3.0"></a> Picocli 3.3.0
The picocli community is pleased to announce picocli 3.3.0.

This release contains a bugfix for the JLine TAB completion support and improves the error messages for missing required parameters and unmatched arguments.

This is the thirty-fourth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.3.0"></a> Table of Contents
* [New and noteworthy](#3.3.0-new)
* [Promoted features](#3.3.0-promoted)
* [Fixed issues](#3.3.0-fixes)
* [Deprecations](#3.3.0-deprecated)
* [Potential breaking changes](#3.3.0-breaking-changes)

## <a name="3.3.0-new"></a> New and Noteworthy
### `UnmatchedArgumentException` Improvements
The `UnmatchedArgumentException` class now has several methods that allow an application to offer suggestions for fixes to the end user.

For example:
```java
class App {
    @Option(names = "--file") File[] files;
    @Option(names = "--find") String pattern;

    public static void main(String[] args) {
        App app = new App();
        try {
            new CommandLine(app).parse(args);
            // ...

        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            if (!UnmatchedArgumentException.printSuggestions(ex, System.err)) { // new API
                ex.getCommandLine().usage(System.err, ansi);
            }
        }
    }
}
```

If you run this class with an invalid option that is similar to an actual option, the `UnmatchedArgumentException.printSuggestions` method will show the actual options. For example:

```
<cmd> -fi
```

Prints this output:

```
Unknown option: -fi
Possible solutions: --file, --find
```

This is the behaviour for the `CommandLine` convenience methods `run`, `call` and `parseWithHandlers`.
Note that if possible fixes are found, the usage help message is not displayed.

## <a name="3.3.0-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.3.0-fixes"></a> Fixed issues
- [#411] Bugfix: Completion candidates were only generated for the first option, not for subsequent options.
- [#409] Enhancement: Improve error message for missing required positional parameters. Thanks to [Mārtiņš Kalvāns](https://github.com/sisidra) and [Olle Lundberg](https://github.com/lndbrg).
- [#298] Enhancement: Add help for mistyped commands and options. Added new API to `UnmatchedArgumentException`. Thanks to [Philippe Charles](https://github.com/charphi).

## <a name="3.3.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.3.0-breaking-changes"></a> Potential breaking changes
The error message format has changed. This may impact client tests that expect a specific error message.



# <a name="3.2.0"></a> Picocli 3.2.0
The picocli community is pleased to announce picocli 3.2.0.

This release contains new features and enhancements:

* Improved support for Dependency Injection
* Methods can now be annotated with `@Option` and `@Parameters`
* Support for JLine-based interactive command line interfaces (`completionCandidates` attribute on `@Option` and `@Parameters`, and the `AutoComplete.complete` method)
* New `@Spec` annotation for injecting a command with its `CommandSpec`

This is the thirty-third public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.2.0"></a> Table of Contents
* [New and noteworthy](#3.2.0-new)
* [Promoted features](#3.2.0-promoted)
* [Fixed issues](#3.2.0-fixes)
* [Deprecations](#3.2.0-deprecated)
* [Potential breaking changes](#3.2.0-breaking-changes)

## <a name="3.2.0-new"></a> New and Noteworthy
### <a name="3.2.0-di"></a> Dependency Injection
This release makes integration with Dependency Injection containers extremely easy:

* `CommandLine` constructor now accepts a `Class` instance as the user object, and will delegate to the `IFactory` to get an instance.
* New `CommandLine.run(Class<Runnable>, IFactory, ...)` and `CommandLine.call(Class<Callable>, IFactory, ...)` methods. These work the same as the existing `run` and `call` methods except that the `Runnable` or `Callable` instance is created by the factory.

The below example shows how to create an `IFactory` implementation with a Guice `Injector`:

```java
import com.google.inject.*;
import picocli.CommandLine.IFactory;

public class GuiceFactory implements IFactory {
    private final Injector injector = Guice.createInjector(new DemoModule());

    @Override
    public <K> K create(Class<K> aClass) throws Exception {
        return injector.getInstance(aClass);
    }

    static class DemoModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(java.util.List.class).to(java.util.LinkedList.class);
            bind(Runnable.class).to(InjectionDemo.class);
        }
    }
}
```


Use the custom factory when creating a `CommandLine` instance, or when invoking the `run` or `call` convenience methods:

```java
import javax.inject.Inject;

@Command(name = "di-demo")
public class InjectionDemo implements Runnable {
    @Inject java.util.List list;

    @Option(names = "-x") int x;

    public static void main(String[] args) {
        CommandLine.run(Runnable.class, new GuiceFactory(), args);
    }

    @Override
    public void run() {
        assert list instanceof java.util.LinkedList;
    }
}
```


### <a name="3.2.0-method-annotations"></a> Annotated Methods
From this release, `@Option` and `@Parameter` annotations can be added to methods as well as fields of a class.

For concrete classes, annotate "setter" methods (methods that accept a parameter) and when the option is specified on the command line, picocli will invoke the method with the value specified on the command line, converted to the type of the method parameter.

Alternatively, you may annotate "getter-like" methods (methods that return a value) on an interface, and picocli will create an instance of the interface that returns the values specified on the command line, converted to the method return type. This feature is inspired by [Jewel CLI](https://github.com/lexicalscope/jewelcli).

#### Annotating Methods of an Interface
The `@Option` and `@Parameters` annotations can be used on methods of an interface that return a value. For example:

```java
interface Counter {
    @Option(names = "--count")
    int getCount();
}
```
You use it by specifying the class of the interface:
```java
CommandLine cmd = new CommandLine(Counter.class); // specify a class
String[] args = new String[] {"--count", "3"};
cmd.parse(args);
Counter counter = cmd.getCommand(); // picocli created an instance
assert counter.getCount() == 3; // method returns command line value
```

#### Annotating Methods of a Concrete Class
The `@Option` and `@Parameters` annotations can be used on methods of a class that accept a parameter. For example:

```java
class Counter {
    int count;

    @Option(names = "--count")
    void setCount(int count) {
        this.count = count;
    }
}
```
You use it by passing an instance of the class:
```java
Counter counter = new Counter(); // the instance to populate
CommandLine cmd = new CommandLine(counter);
String[] args = new String[] {"--count", "3"};
cmd.parse(args);
assert counter.count == 3; // method was invoked with command line value
```

### <a name="3.2.0-jline"></a> JLine Tab-Completion Support

This release adds support for JLine Tab-Completion.

[Jline 2.x](https://github.com/jline/jline2) and [3.x](https://github.com/jline/jline3) is a Java library for handling console input, often used to create interactive shell applications.

Command line applications based on picocli can generate completion candidates for the command line in the JLine shell. The generated completion candidates are context sensitive, so once a subcommand is specified, only the options for that subcommand are shown, and once an option is specified, only parameters for that option are shown.

Below is an example picocli `Completer` implementation for JLine 2.x:

```java
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.util.List;

public class PicocliJLineCompleter implements Completer {
    private final CommandSpec spec;

    public PicocliJLineCompleter(CommandSpec spec) {
        this.spec = spec;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // use the jline internal parser to split the line into tokens
        ArgumentCompleter.ArgumentList list =
                new ArgumentCompleter.WhitespaceArgumentDelimiter().delimit(buffer, cursor);

        // let picocli generate completion candidates for the token where the cursor is at
        return AutoComplete.complete(spec,
                list.getArguments(),
                list.getCursorArgumentIndex(),
                list.getArgumentPosition(),
                cursor,
                candidates);
    }
}
```

### <a name="3.2.0-completion-candidates"></a> Completion Candidates
From this release, `@Options` and `@Parameters` have a new `completionCandidates` attribute that can be used to generate a list of completions for this option or positional parameter. For example:

```java
static class MyAbcCandidates extends ArrayList<String> {
    MyAbcCandidates() { super(Arrays.asList("A", "B", "C")); }
}

class ValidValuesDemo {
    @Option(names = "-o", completionCandidates = MyAbcCandidates.class)
    String option;
}
```

This will generate completion option values `A`, `B` and `C` in the generated bash auto-completion script and in JLine.


### <a name="3.2.0-default-variable"></a> `${DEFAULT-VALUE}` Variable
From picocli 3.2, it is possible to embed the default values in the description for an option or positional parameter by
specifying the variable `${DEFAULT-VALUE}` in the description text.
Picocli uses reflection to get the default values from the annotated fields.

The variable is replaced with the default value regardless of the `@Command(showDefaultValues)` attribute
and regardless of the `@Option(showDefaultValues)` or `@Parameters(showDefaultValues)` attribute.

```java
class DefaultValues {
    @Option(names = {"-f", "--file"},
            description = "the file to use (default: ${DEFAULT-VALUE})")
    File file = new File("config.xml");
}

CommandLine.usage(new DefaultValues(), System.out);
```
This produces the following usage help:
```text
Usage: <main class> -f=<file>
  -f, --file=<file>   the file to use (default: config.xml)
```

### <a name="3.2.0-completion-variable"></a>  `${COMPLETION-CANDIDATES}` Variable
Similarly, it is possible to embed the completion candidates in the description for an option or positional parameter by
specifying the variable `${COMPLETION-CANDIDATES}` in the description text.

This works for java `enum` classes and for options or positional parameters of non-enum types for which completion candidates are specified.

```java
enum Lang { java, groovy, kotlin, javascript, frege, clojure }

static class MyAbcCandidates extends ArrayList<String> {
    MyAbcCandidates() { super(Arrays.asList("A", "B", "C")); }
}

class ValidValuesDemo {
    @Option(names = "-l", description = "Enum. Values: ${COMPLETION-CANDIDATES}")
    Lang lang = null;

    @Option(names = "-o", completionCandidates = MyAbcCandidates.class,
            description = "Candidates: ${COMPLETION-CANDIDATES}")
    String option;
}

CommandLine.usage(new ValidValuesDemo(), System.out);
```
This produces the following usage help:
```text
Usage: <main class> -l=<lang> -o=<option>
  -l=<lang>     Enum. Values: java, groovy, kotlin, javascript, frege, clojure
  -o=<option>   Candidates: A, B, C
```

### <a name="3.2.0-Spec"></a> `@Spec` Annotation
A new `@Spec` annotation is now available that injects the `CommandSpec` model of the command into a command field.

This is useful when a command needs to use the picocli API, for example to walk the command hierarchy and iterate over its sibling commands.
This complements the `@ParentCommand` annotation;  the `@ParentCommand` annotation injects a user-defined command object, whereas this annotation injects a picocli class.

```java
class InjectSpecExample implements Runnable {
   @Spec CommandSpec commandSpec;
   //...
   public void run() {
       // do something with the injected spec
   }
}

```




### <a name="3.2.0-lenient-parse"></a> Lenient Parse Mode

This release adds the ability to continue parsing invalid input to the end.
When `collectErrors` is set to `true`, and a problem occurs during parsing, an `Exception` is added to the `ParseResult.errors()` list and parsing continues. The default behaviour (when `collectErrors` is `false`) is to abort parsing by throwing the `Exception`.

This is useful when generating completion candidates on partial input, and is also useful when using picocli in
languages like Clojure where idiomatic error handling does not involve throwing and catching exceptions.

When using this feature, applications are responsible for actively verifying that no errors occurred before executing the business logic. Use with care!


## <a name="3.2.0-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.2.0-fixes"></a> Fixed issues
- [#182] New Feature: Add support for annotating methods with `@Option` and `@Parameters`.
- [#393] New feature: Add support for JLine completers.
- [#389] New feature: Support 'lenient' parsing mode: don't throw `Exceptions` but add them to the `ParseResult.errors()` list and continue parsing.
- [#392] New feature: Ability to map command line arguments to picocli spec elements. Internally used for generating completion candidates.
- [#391] New feature: Add API to get completion candidates for option and positional parameter values of any type.
- [#395] New feature: Allow embedding default values anywhere in description for `@Option` or `@Parameters`.
- [#259] New Feature: Added `@Spec` annotation to inject `CommandSpec` into application field.
- [#400] Enhancement: Add run/call static methods that accept an `IFactory`. This allows Dependency Injection containers to provide the Runnable/Callable implementation.
- [#404] Enhancement: Ask IFactory for implementation before creating Proxy for interface. Needed for Dependency Injection.
- [#398] Enhancement: Allow `@PicocliScript` annotation on Groovy script `@Field` variables instead of just on imports.
- [#322] Enhancement: Add `defaultValue` attribute to @Option and @Parameters annotation.
- [#375] Enhancement: Improve `ParameterIndexGapException` error message. Thanks to [gpettey](https://github.com/gpettey).
- [#405] Enhancement: Add method `CommandLine.getUsageMessage()`.
- [#406] Enhancement: Added fields to `ParameterException`. Thanks to [David Hait](https://github.com/dhait).
- [#401] Doc: The user manual no longer includes the full `CommandLine.java` source code.

## <a name="3.2.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.2.0-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.1.0"></a> Picocli 3.1.0
The picocli community is pleased to announce picocli 3.1.0.

This release contains bugfixes and support for command aliases.

Picocli has a new logo! Many thanks to [Reallinfo](https://github.com/reallinfo) for the design!

<img src="https://picocli.info/images/logo/horizontal.png" height="100">

This is the thirty-second public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.1.0"></a> Table of Contents
* [New and noteworthy](#3.1.0-new)
* [Promoted features](#3.1.0-promoted)
* [Fixed issues](#3.1.0-fixes)
* [Deprecations](#3.1.0-deprecated)
* [Potential breaking changes](#3.1.0-breaking-changes)

## <a name="3.1.0-new"></a> New and Noteworthy
### Command Aliases
This release adds support for command aliases.

```java
@Command(name = "top", subcommands = {SubCommand.class},
        description = "top level command")
static class TopLevelCommand { }

@Command(name = "sub", aliases = {"s", "sb"},
        description = "I'm a subcommand")
static class SubCommand {}

new CommandLine(new TopLevelCommand()).usage(System.out);
```
The above would print the following usage help message:

```text
Usage: top [COMMAND]
top level command
Commands:
  sub, s, sb   I'm a subcommand
```

## <a name="3.1.0-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.1.0-fixes"></a> Fixed issues
- [#288] New feature: add support for command aliases.
- [#383] Enhancement: [Reallinfo](https://github.com/reallinfo) designed the new picocli logo. Amazing work, many thanks!
- [#388] Bugfix: Prevent AnnotationFormatError "Duplicate annotation for class" with @PicocliScript when the script contains classes. Thanks to [Bradford Powell](https://github.com/bpow) for the bug report.

## <a name="3.1.0-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.1.0-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.0.2"></a> Picocli 3.0.2
The picocli community is pleased to announce picocli 3.0.2.

This release contains bugfixes and enhancements for programmatic configuration.

This is the thirty-first public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.2"></a> Table of Contents
* [New and noteworthy](#3.0.2-new)
* [Promoted features](#3.0.2-promoted)
* [Fixed issues](#3.0.2-fixes)
* [Deprecations](#3.0.2-deprecated)
* [Potential breaking changes](#3.0.2-breaking-changes)

## <a name="3.0.2-new"></a> New and Noteworthy

## <a name="3.0.2-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.2-fixes"></a> Fixed issues
- [#381] Bugfix: Prevent NPE when adding programmatically created subcommands to CommandLine. Thanks to [Mikusch](https://github.com/Mikusch) for the bug report.
- [#382] Enhancement: Subcommand name should be initialized when added to parent.

## <a name="3.0.2-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.0.2-breaking-changes"></a> Potential breaking changes
This release has no breaking changes.


# <a name="3.0.1"></a> Picocli 3.0.1
The picocli community is pleased to announce picocli 3.0.1.

This release fixes a bug for map options and has several improvements for the usage help message, especially for subcommands.

This is the thirtieth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.1"></a> Table of Contents
* [New and noteworthy](#3.0.1-new)
* [Promoted features](#3.0.1-promoted)
* [Fixed issues](#3.0.1-fixes)
* [Deprecations](#3.0.1-deprecated)
* [Potential breaking changes](#3.0.1-breaking-changes)

## <a name="3.0.1-new"></a> New and Noteworthy
From this release, the usage help synopsis of the subcommand shows not only the subcommand name but also the parent command name. For example, take the following hierarchy of subcommands.

```java
@Command(name = "main", subcommands = {Sub.class}) class App { }
@Command(name = "sub", subcommands = {SubSub.class}) class Sub { }
@Command(name = "subsub", mixinStandardHelpOptions = true) class SubSub { }

CommandLine parser = new CommandLine(new App());
ParseResult result = parser.parseArgs("sub", "subsub", "--help");
CommandLine.printHelpIfRequested(result);
```
The above code prints the usage help for the `subsub` nested subcommand. Notice that this shows not only the subcommand name but the full command hierarchy:

```
Usage: main sub subsub [-hV]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
```

## <a name="3.0.1-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.1-fixes"></a> Fixed issues
- [#287] Enhancement: Subcommand help now also shows parent command name in synopsis.
- [#378] Bugfix: Map option problem when value contains '=' separator. Thanks to [Markus Kramer](https://github.com/MarkusKramer) for the bug report.
- [#377] Bugfix: Standard help options should be added last: when `mixinStandardHelpOptions` is set and `sortOptions` is false, the help options should appear after the command options in the usage help message.

## <a name="3.0.1-deprecated"></a> Deprecations
No features were deprecated in this release.

## <a name="3.0.1-breaking-changes"></a> Potential breaking changes
The usage help synopsis of the subcommand shows not only the subcommand name but also the parent command name (and parent's parent command name, up to the top-level command).



# <a name="3.0.0"></a> Picocli 3.0.0
The picocli community is pleased to announce picocli 3.0.0.

This release offers a [programmatic API](https://picocli.info/picocli-3.0-programmatic-api.html) for creating command line applications, in addition to the annotations API. The programmatic API allows applications to dynamically create command line options on the fly, and also makes it possible to create idiomatic domain-specific languages for processing command line arguments, using picocli, in other JVM languages. The picocli community is proud to announce that [Apache Groovy](http://groovy-lang.org/)'s [CliBuilder](https://docs.groovy-lang.org/docs/next/html/gapi/groovy/cli/picocli/CliBuilder.html) DSL for command line applications has been rewritten to use the picocli programmatic API, starting from Groovy 2.5.

Another new feature in this release are Mixins. Mixins allow reusing common options, parameters and command attributes in multiple applications without copy-and-paste duplication.

This release aims to reduce boilerplate code in user applications even further with the new `mixinStandardHelpOptions` command attribute. Picocli adds standard `usageHelp` and `versionHelp` options to commands with this attribute. Additionally picocli now offers a `HelpCommand` that can be installed as a subcommand on any application command to provide usage help for the parent command or sibling subcommands.

From this release, picocli is better at following unix conventions: by default it now prints to stdout when the user requested help, and prints to stderr when the input was invalid or an unexpected error occurred. This release also gives better control over the process exit code.

A new `@Unmatched` annotation allows applications to easily capture unmatched arguments (arguments that could not be matched with any of the registered options or positional parameters).

Usage help message improvements: the usage help message width is now configurable, and the message layout is improved to reduce horizontal padding. Furthermore, you can now specify for individual options or positional parameters whether their default value should be shown in the description or hidden.

Finally, this release adds several options to configure parser behaviour. Picocli can now be configured to function like Apache Commons CLI, to facilitate migration from Apache Commons CLI to picocli.


This is the twenty-ninth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0"></a> Table of Contents
* [New and noteworthy](#3.0.0-new)
* [Promoted features](#3.0.0-promoted)
* [Fixed issues](#3.0.0-fixes)
* [Deprecations](#3.0.0-deprecated)
* [Potential breaking changes](#3.0.0-breaking-changes)

## <a name="3.0.0-new"></a> New and Noteworthy

### <a name="3.0.0-Programmatic-API"></a> Programmatic API
This release offers a programmatic API for creating command line applications, in addition to the annotations API. The programmatic API allows applications to dynamically create command line options on the fly, and also makes it possible to create idiomatic domain-specific languages for processing command line arguments, using picocli, in other JVM languages. (Example: Groovy [CliBuilder](http://docs.groovy-lang.org/docs/next/html/gapi/groovy/cli/picocli/CliBuilder.html).)

_If you have suggestions for improving the programmatic API, please raise a ticket on GitHub!_

#### Example
```java
CommandSpec spec = CommandSpec.create();
spec.mixinStandardHelpOptions(true); // --help and --version options
spec.addOption(OptionSpec.builder("-c", "--count")
        .paramLabel("COUNT")
        .type(int.class)
        .description("number of times to execute").build());
spec.addPositional(PositionalParamSpec.builder()
        .paramLabel("FILES")
        .type(List.class).auxiliaryTypes(File.class) // List<File>
        .description("The files to process").build());

CommandLine commandLine = new CommandLine(spec);
try {
    // see also the CommandLine.parseWithHandler(s) convenience methods
    ParseResult pr = commandLine.parseArgs(args);

    if (CommandLine.printHelpIfRequested(pr)) {
        return;
    }
    int count = pr.matchedOptionValue('c', 1);
    List<File> files = pr.matchedPositionalValue(0, Collections.<File>emptyList());
    for (File f : files) {
        for (int i = 0; i < count; i++) {
            System.out.printf("%d: %s%n", i, f);
        }
    }
} catch (ParseException invalidInput) {
    System.err.println(invalidInput.getMessage());
    invalidInput.getCommandLine().usage(System.err);
}
```

#### CommandSpec
`CommandSpec` models a command. It is the programmatic variant of the `@Command` annotation. It has a name and a version, both of which may be empty.  It also has a `UsageMessageSpec` to configure aspects of the usage help message and a `ParserSpec` that can be used to control the behaviour of the parser.

#### OptionSpec and PositionalParamSpec
`OptionSpec` models a named option, and `PositionalParamSpec` models one or more positional parameters. They are the programmatic variant of the `@Option` and `@Parameters` annotations, respectively.

An `OptionSpec` must have at least one name, which is used during parsing to match command line arguments. Other attributes can be left empty and picocli will give them a reasonable default value. This defaulting is why `OptionSpec` objects are created with a builder: this allows you to specify only some attributes and let picocli initialise the other attributes. For example, if only the option’s name is specified, picocli assumes the option takes no parameters (arity = 0), and is of type `boolean`. Another example, if arity is larger than `1`, picocli sets the type to `List` and the `auxiliary type` to `String`.

`PositionalParamSpec` objects don’t have names, but have an index range instead. A single `PositionalParamSpec` object can capture multiple positional parameters. The default index range is set to `0..*` (all indices). A command may have multiple `PositionalParamSpec` objects to capture positional parameters at different index ranges. This can be useful if positional parameters at different index ranges have different data types.

Similar to `OptionSpec` objects, Once a `PositionalParamSpec` is constructed, its configuration becomes immutable, but its `value` can still be modified. Usually the value is set during command line parsing when a non-option command line argument is encountered at a position in its index range.

#### <a name="3.0.0-ParseResult"></a> ParseResult
A `ParseResult` class is now available that allows applications to inspect the result of parsing a sequence of command line arguments.

This class provides methods to query whether the command line arguments included certain options or position parameters, and what the value or values of these options and positional parameters was. Both the original command line argument String value as well as a strongly typed value can be obtained.


### Mixins for Reuse
Mixins are a convenient alternative to subclassing: picocli annotations from _any_ class can be added to ("mixed in" with) another command. This includes options, positional parameters, subcommands and command attributes. Picocli [autoHelp](#3.0.0-alpha-1-autohelp) internally uses a mixin.

A mixin is a separate class with options, positional parameters, subcommands and command attributes that can be reused in other commands. Mixins can be installed by calling the `CommandLine.addMixin` method with an object of this class, or annotating a field in your command with `@Mixin`. Here is an example mixin class:

```java
public class ReusableOptions {

    @Option(names = { "-v", "--verbose" }, description = {
        "Specify multiple -v options to increase verbosity.", "For example, `-v -v -v` or `-vvv`" })
    protected boolean[] verbosity = new boolean[0];
}
```

#### Adding Mixins Programmatically
The below example shows how a mixin can be added programmatically with the `CommandLine.addMixin` method.

```java
CommandLine commandLine = new CommandLine(new MyCommand());
commandline.addMixin("myMixin", new ReusableOptions());
```
#### `@Mixin` Annotation
A command can also include mixins by annotating fields with `@Mixin`. All picocli annotations found in the mixin class are added to the command that has a field annotated with `@Mixin`. For example:

```java
@Command(name = "zip", description = "Example reuse with @Mixin annotation.")
public class MyCommand {

    // adds the options defined in ReusableOptions to this command
    @Mixin
    private ReusableOptions myMixin;
}
```


### <a name="3.0.0-mixinStandardHelpOptions"></a> Standard Help Options
This release introduces the `mixinStandardHelpOptions` command attribute. When this attribute is set to `true`, picocli adds a mixin to the command that adds `usageHelp` and `versionHelp` options to the command. For example:

```java
@Command(mixinStandardHelpOptions = true, version = "auto help demo - picocli 3.0")
class AutoHelpDemo implements Runnable {

    @Option(names = "--option", description = "Some option.")
    String option;

    @Override public void run() { }
}
```

Commands with `mixinStandardHelpOptions` do not need to explicitly declare fields annotated with `@Option(usageHelp = true)` and `@Option(versionHelp = true)` any more. The usage help message for the above example looks like this:
```text
Usage: <main class> [-hV] [--option=<option>]
      --option=<option>   Some option.
  -h, --help              Show this help message and exit.
  -V, --version           Print version information and exit.
```

### <a name="3.0.0-HelpCommand"></a> Help Command

From this release, picocli provides a `help` subcommand (`picocli.CommandLine.HelpCommand`) that can be installed as a subcommand on any application command to provide usage help for the parent command or sibling subcommands. For example:

```java
@Command(subcommands = HelpCommand.class)
class AutoHelpDemo implements Runnable {

    @Option(names = "--option", description = "Some option.")
    String option;

    @Override public void run() { }
}
```


```text
# print help for the `maincommand` command
maincommand help

# print help for the `subcommand` command
maincommand help subcommand
```

For applications that want to create a custom help command, this release also introduces a new interface `picocli.CommandLine.IHelpCommandInitializable` that provides custom help commands with the information they need: access to the parent command and sibling commands, whether to use Ansi colors or not, and the streams to print the usage help message to.

### <a name="3.0.0-Unmatched"></a> `@Unmatched` Annotation
Unmatched arguments are the command line arguments that could not be assigned to any of the defined options or positional parameters. From this release, fields annotated with `@Unmatched` will be populated with the unmatched arguments. The field must be of type `String[]` or `List<String>`.

If picocli finds a field annotated with `@Unmatched`, it automatically sets `unmatchedArgumentsAllowed` to `true` so no `UnmatchedArgumentException` is thrown when a command line argument cannot be assigned to an option or positional parameter.

### <a name="3.0.0-std"></a> Stdout or Stderr
From picocli v3.0, the `run` and `call` convenience methods follow unix conventions: print to stdout when the user requested help, and print to stderr when the input was invalid or an unexpected error occurred.

Custom handlers can extend `AbstractHandler` to facilitate following this convention. `AbstractHandler` also provides `useOut` and `useErr` methods to allow customizing the target output streams, and `useAnsi` to customize the Ansi style to use:

```java
@Command class CustomizeTargetStreamsDemo implements Runnable {
    public void run() { ... }

    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new CustomizeTargetStreamsDemo());

        PrintStream myOut = getOutputPrintStream(); // custom stream to send command output to
        PrintStream myErr = getErrorPrintStream();  // custom stream for error messages

        cmd.parseWithHandlers(
                new RunLast().useOut(myOut).useAnsi(Help.Ansi.ON),
                new DefaultExceptionHandler().useErr(myErr).useAnsi(Help.Ansi.OFF),
                args);
    }
}
```

### <a name="3.0.0-exit-code"></a> Exit Code Support
From picocli v3.0, the built-in parse result handlers (`RunFirst`, `RunLast` and `RunAll`) and exception handler (`DefaultExceptionHandler`) can specify an exit code. If an exit code was specified, the handler terminates the JVM with the specified status code when finished.

```java
@Command class ExitCodeDemo implements Runnable {
    public void run() { throw new ParameterException(new CommandLine(this), "exit code demo"); }

    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new ExitCodeDemo());
        cmd.parseWithHandlers(
                new RunLast().andExit(123),
                new DefaultExceptionHandler().andExit(456),
                args);
    }
}
```
Running this command prints the following to stderr and exits the JVM with status code `456`.

```
exit code demo
Usage: <main class>
```

Custom handlers can extend `AbstractHandler` to inherit this behaviour.


### <a name="3.0.0-ShowDefault"></a> Fine-grained ShowDefault

This release adds a `showDefaultValue` attribute to the `@Option` and `@Parameters` annotation. This allows you to specify for each individual option and positional parameter whether its default value should be shown in the usage help.

This attribute accepts three values:

* `ALWAYS` - always display the default value of this option or positional parameter, even `null` values, regardless what value of `showDefaultValues` was specified on the command
* `NEVER` - don't show the default value for this option or positional parameter, regardless what value of `showDefaultValues` was specified on the command
* `ON_DEMAND` - (this is the default) only show the default value for this option or positional parameter if `showDefaultValues` was specified on the command

The `NEVER` value is useful for security sensitive command line arguments like passwords. The `ALWAYS` value is useful when you only want to show the default value for a few arguments but not for all (in combination with `@Command(showDefaultValues = false)`).

### <a name="3.0.0-UsageHelpLayout"></a> Improved Usage Help Message Layout
Previously, the usage message layout had a fixed width long option name column: this column is always 24 characters, even if none of the options have a long option name.

This gave strange-looking usage help messages in some cases. For example:
```java
@Command(name="ls")
class App {
    @Option(names = "-a", description = "display all files") boolean a;
    @Option(names = "-l", description = "use a long listing format") boolean l;
    @Option(names = "-t", description = "sort by modification time") boolean t;
}
```

The usage message for this example was:
```
Usage: ls [-alt]
  -a                          display all files
  -l                          use a long listing format
  -t                          sort by modification time
```
From this release, picocli adjusts the width of the long option name column to the longest name (up to max 24).

The new usage message for this example looks like this:
```
Usage: ls [-alt]
  -a     display all files
  -l     use a long listing format
  -t     sort by modification time
```

### <a name="3.0.0-StricterArity"></a> Stricter Arity Validation

Until this release, options with mandatory parameters would consume as many arguments as required, even if those arguments matched other option flags. For example:

Given a command like this:
```java
class App {
  @Option(names = "-a", arity = "2")
  String[] a;

  @Option(names = "-v")
  boolean v;
}
```
Prior to this change, the following input would be accepted:
```
<command> -a 1 -v
```
In previous versions, picocli accepted this and assigned `"1"` and `"-v"` as the two values for the `-a` option.
From this release, the parser notices that one of the arguments is an option and throws a `MissingParameterException` because not enough parameters were specified for the first option.


## <a name="3.0.0-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-fixes"></a> Fixed issues
- [#371] Fixed bug where autocompletion did not work correctly for subcommands with embedded hyphens. Thanks to [Paulius Fadelis](https://github.com/Fadelis) for the bug report.
- [#372] Simplify Kotlin example in user manual. Thanks to [Dustin Spicuzza](https://github.com/virtuald).

### 3.0.0-alpha-1
- [#245] New Feature: from 3.0, picocli offers an API for programmatic configuration.
- [#257] New Feature: new `ParseResult` class allows programmatic inspection of the result of parsing a sequence of command line arguments.
- [#144] New Feature: added support for mixins to allow reusing common options, positional parameters, subcommands and command attributes from any object.
- [#253] New Feature: added `@Unmatched` annotation for unmatched arguments.
- [#175] New Feature: `mixinStandardHelpOptions` attribute to install the standard `--help` and `--version` options, obviating the need for fields annotated with `@Option(usageHelp = true)` and `@Option(versionHelp = true)`.
- [#175] New Feature: picocli now provides a `HelpCommand` that can be installed as a subcommand on any application command to provide usage help for the parent command or sibling subcommands.
- [#175] New Feature: new `IHelpCommandInitializable` interface facilitates construction of custom help commands.
- [#250] Enhancement: the `run` and `call` convenience methods now follow convention: print to stdout when the user requested help, print to stderr when the input was invalid or an unexpected error occurred. Added `AbstractHandler` to facilitate following this convention for custom parse result handlers and exception handlers.
- [#251] New Feature: exit code support. The built-in parse result handlers (`RunFirst`, `RunLast` and `RunAll`) and exception handler  (`DefaultExceptionHandler`) can now optionally specify an exit code. If specified, the handler terminates the JVM with the specified status code when finished. Custom handlers can extend `AbstractHandler` to inherit this behaviour.
- [#262] New Feature: new `showDefaultValue` attribute on `@Option` and `@Parameters` gives fine-grained control over which default values to show or hide. Thanks to [ymenager](https://github.com/ymenager) for the request.
- [#268] New Feature: new `helpCommand` attribute on `@Command`: if the command line arguments contain a subcommand annotated with `helpCommand`, the parser will not validate the required options or positional parameters of the parent command. Thanks to [ymenager](https://github.com/ymenager) for the request.
- [#277] New Feature: new `hidden` attribute on `@Command` to omit the specified subcommand from the usage help message command list of the parent command. Thanks to [pditommaso](https://github.com/pditommaso).
- [#159] Enhancement: make help usage message width configurable. Thanks to [pditommaso](https://github.com/pditommaso).

### 3.0.0-alpha-2
- [#312] Enhancement and API change (against earlier alpha version): Remove `AbstractSimpleParseResultHandler` class and `parseWithSimpleHandlers` method.
- [#311] Enhancement and API change (against earlier alpha version): Simplify parseWithHandlers: removed prototypeReturnValue parameter.
- [#307] Enhancement: Provide CommandLine.usage(PrintWriter) method for testing and to facilitate [GROOVY-8520](https://issues.apache.org/jira/browse/GROOVY-8520) migration from commons-cli to picocli.
- [#306] Enhancement: Support generating autocompletion scripts for non-public @Command classes. Thanks to [cbeams](https://github.com/cbeams) for the request.
- [#308] Enhancement: Provide API to disallow POSIX clustered short options.
- [#310] Enhancement: PicocliBaseScript should follow conventions for stdout and stderr: requested help to stdout, invalid input usage help to stderr.
- [#309] Bugfix: Tests were failing on environments that support ANSI colors.

### 3.0.0-alpha-3
- [#313] Enhancement and New API: add method (later removed in 3.0.0-beta-1) `CommandLine::setMaxArityIsMaxTotalParams` to configure the parser to use `arity` to limit the total number of values accumulated in an option or positional parameter.
- [#314] Enhancement and New API: add method `CommandLine::setUsageHelpWidth` and `UsageMessageSpec::width` to set the max usage help message width.
- [#316] Enhancement: Support lenient mode where annotations are optional when extracting annotations.
- [#317] Enhancement: Change semantics of ParseResult.rawOptionValue to mean values after split (but before type conversion).

### 3.0.0-alpha-4
- [#318] API Change: Split model IBinding into IGetter and ISetter.
- [#320] API Change: Rename parser config `maxArityIsMaxTotalParams` to `arityRestrictsCumulativeSize`. (Property was removed in 3.0.0-beta-1.)
- [#216] Enhancement: Parsed values now replace the default value of multi-value (array, Collection or Map) options and positional parameters instead of being appended to them. Thanks to [wiwie](https://github.com/wiwie) for the request.
- [#261] Enhancement: Options and positional parameters with a `defaultValue` are never required. Thanks to [ymenager](https://github.com/ymenager) for the request.
- [#315] Enhancement: Initialize ArgSpec value with `defaultValue` before parsing command line.
- [#263] Bugfix: positional parameter defaults were not shown in usage help message. Thanks to [ymenager](https://github.com/ymenager) for the bug report.

### 3.0.0-alpha-5
- [#329] New API: Add parser configuration to control whether boolean flags should be toggled.
- [#328] New API: Provide getter methods on `OptionSpec.Builder` and `PositionalParamSpec.Builder`.
- [#326] New API: Add parser configuration to treat unmatched options as positional parameters.
- [#283] New API: Provide `getMissing` method on MissingParameterException to get a reference to the problematic options and positional parameters. Thanks to [jcapsule](https://github.com/jcapsule) for the suggestion.
- [#334] API Change: Rename `ArgSpec.rawStringValues()` to `ArgSpec.stringValues()`.
- [#342] API Change: Prefix ParseResult methods with `matched` if they return only matched options/positionals.
- [#340] API Change: Rename `ParseResult.optionValue(String, T)` to `matchedOptionValue(String, T)`.
- [#338] API Change: Remove `ParseResult.rawOptionValue(s)` and `rawPositionalValue(s)` methods.
- [#339] API Change: Remove `ParseResult.matchedOptionValue(OptionSpec)` and `matchedPositionalValue(PositionalParamSpec)` methods.
- [#347] API Change: Make `ArgSpec.getValue`, `setValue` and `isMultiValue` public methods.
- [#333] Enhancement: Added subcommand to synopsis in generated usage help. Thanks to [jcapsule](https://github.com/jcapsule) for the pull request.
- [#323] Enhancement: Remove dependency on java.sql package: picocli should only require the java.base module when running in Java 9.
- [#325] Enhancement: Allow custom type converter to map empty String to custom default value for empty options. Thanks to [jesselong](https://github.com/jesselong) for the suggestion.
- [#303] Enhancement: Improve validation to prevent common mistakes.
- [#70]  Enhancement: Positional parameters should only consume values where type conversion succeeds.
- [#346] Enhancement: Validate that arity min is never greater than max.
- [#348] Enhancement: Interpreter should call `ArgSpec.setValue` for every matched option or positional parameter.
- [#327] Bugfix: Default values should not cause options and positional parameters to be added to ParseResult.
- [#330] Bugfix: `Interpreter` should clear option's and positional parameter's `stringValues` list before parsing new input.
- [#335] Bugfix: Abstract class `ArgSpec` should not implement `equals` and `hashCode`.
- [#345] Bugfix: Stop processing varargs when cumulative size reached. (This functionality was removed in 3.0.0-beta-1.)

### 3.0.0-alpha-6
- [#349] New API: Add `longestName()` convenience method to OptionSpec.
- [#352] New API: Add method to copy all attributes of a ParserSpec to a CommandSpec.
- [#353] New API: Add method to copy all attributes of a UsageMessageSpec to a CommandSpec.
- [#343] New API: Add method `Help.Ansi.Text::concat` and deprecate the `append` method. ("Append" suggests the Text object is modified, like StringBuilder, but Text is immutable.)
- [#350] Enhancement: Improve error message for `usageHelp` and `versionHelp` validation.
- [#344] Enhancement: Don't show WARN message for unmatched args or overwritten options.
- [#351] Documentation: Improve javadoc for OptionSpec.usageHelp and versionHelp.
- [#354] Bug fix: Interpreter should reset options and positional parameters to their initial value before parsing new input.

### 3.0.0-beta-1
- [#364] API Change: Remove parser option `arityRestrictsCumulativeSize`.
- [#355] API Change: Add method `ArgSpec.hasInitialValue`.
- [#361] API Change: Add parser option `aritySatisfiedByAttachedOptionParam` for commons-cli compatibility.
- [#363] API Change: Add parser option to limit the number of parts when splitting to max arity, for compatibility with commons-cli.
- [#360] Enhancement: Dynamically adjust width of long option name column (up to max 24).

### 3.0.0-beta-2
- [#366] API Change: Add `ArgSpec.getTypedValues()` method.
- [#365] Enhancement: Stricter arity validation: options with mandatory parameters no longer consume other option flags.
- [#357] Enhancement: Improve synopsis format. Be more succinct when `limitSplit` is true. Support repeating groups.


## <a name="3.0.0-deprecated"></a> Deprecations
### 3.0.0-alpha-1
The `picocli.CommandLine.Help::Help(Object, CommandLine.Help.ColorScheme)` constructor has been deprecated. Use the `picocli.CommandLine.Help::Help(CommandLine.CommandSpec, CommandLine.Help.ColorScheme)` constructor instead.

The `picocli.CommandLine.IParseResultHandler` interface has been deprecated. Use the `picocli.CommandLine.IParseResultHandler2` interface instead.

The `picocli.CommandLine.IExceptionHandler` interface has been deprecated. Use the `picocli.CommandLine.IExceptionHandler2` interface instead.

### 3.0.0-alpha-6
- The `Help.Ansi.Text::append` method is now deprecated in favour of the new `concat` method.


## <a name="3.0.0-breaking-changes"></a> Potential breaking changes
### 3.0.0-alpha-1
#### Help API Changes
The following public fields were removed from the `picocli.CommandLine.Help` class. Instead, set these attributes on a `CommandLine.CommandSpec` object passed to any of the `Help` constructors.

* abbreviateSynopsis
* commandListHeading
* commandName
* customSynopsis
* description
* descriptionHeading
* footer
* footerHeading
* header
* headerHeading
* optionFields
* optionListHeading
* parameterLabelRenderer - replaced with the `Help.parameterLabelRenderer()` method
* parameterListHeading
* requiredOptionMarker
* separator
* showDefaultValues
* sortOptions
* synopsisHeading

Method signature changes on inner classes and interfaces of the `Help` class:

* Interface method `CommandLine.Help.IOptionRenderer::render` signature changed: `CommandLine.Option` and `Field` parameters are replaced with a single `CommandLine.OptionSpec` parameter.
* Interface method `CommandLine.Help.IParameterRenderer::render` signature changed: `CommandLine.Parameters` and `Field` parameters are replaced with a single `CommandLine.PositionalParamSpec` parameter.
* Interface method `CommandLine.Help.IParamLabelRenderer::renderParameterLabel` signature changed: `Field` parameter replaced with `CommandLine.ArgSpec` parameter.
* Class `CommandLine.Help.Layout` all methods changed: `Field` parameters replaced by `CommandLine.ArgSpec`, `CommandLine.OptionSpec` and `CommandLine.PositionalParamSpec` parameters.

### 3.0.0-alpha-2
- [#311] API change from 3.0.0-alpha-1: the `parseWithHandlers` methods signature changed: removed the `prototypeReturnValue` parameter.
- [#312] API change from 3.0.0-alpha-1: Remove `AbstractSimpleParseResultHandler` class and `parseWithSimpleHandlers` method.

### 3.0.0-alpha-3
- Utility method `CommandLine.Help.join` signature changed: now takes an additional `usageHelpWidth` parameter.
- Constructor `CommandLine.Help.Layout(ColorScheme)` signature changed: now takes an additional `usageHelpWidth` parameter.
- Public field `CommandLine.Help.TextTable.columns` is now private; added public method `CommandLine.Help.TextTable.columns()`.
- Constructor `CommandLine.Help.TextTable(Ansi)` is replaced with factory method `CommandLine.Help.TextTable.forDefaultColumns(Ansi, int)`.
- Constructor `CommandLine.Help.TextTable(Ansi, int...)` is replaced with factory method `CommandLine.Help.TextTable.forColumnWidths(Ansi, int...)`.
- Constructor `CommandLine.Help.TextTable(Ansi, Column...)` modifier changed from public to protected.
- Added factory method `CommandLine.Help.TextTable.forColumns(Ansi, Column...)`.
- Renamed `CommandLine.MaxValuesforFieldExceededException` to `CommandLine.MaxValuesExceededException`.

### 3.0.0-alpha-4
- Parsed values now replace the default value of multi-value (array, Collection or Map) options and positional parameters instead of being appended to them.
- The `IBinding` interface introduced in v3.0.0-alpha-1 has been replaced with two functional interfaces `IGetter` and `ISetter`.
- The `UnmatchedArgsBinding` factory methods introduced in v3.0.0-alpha-1 have been replaced with `forStringArrayConsumer` and `forStringCollectionSupplier`.

### 3.0.0-alpha-5
Changes against earlier 3.0.0-alpha versions:

* Renamed `ArgSpec.rawStringValues()` to `ArgSpec.stringValues()`.
* Renamed `ParseResult` methods with `matched` if they return only matched options/positionals:
    * `options` to `matchedOptions`
    * `positionalParams` to `matchedPositionals`
    * `option(char)`, `option(String)` to `matchedOption`
    * `positional(int)` to `matchedPositional`
    * `hasOption(char)`, `hasOption(String)`, `hasOption(OptionSpec)` to `hasMatchedOption`
    * `hasPositional(int)`, `hasPositional(PositionalParamSpec)` to `hasMatchedPositional`
* Renamed `ParseResult.optionValue(String, T)` to `matchedOptionValue(String, T)`, and `positionalValue` to `matchedPositionalValue`.
* Removed `ParseResult::rawOptionValue(s)` and `rawPositionalValue(s)` methods.
* Removed `ParseResult.matchedOptionValue(OptionSpec)` and `matchedPositionalValue(PositionalParamSpec)` methods.

### 3.0.0-beta-1
- The usage message format changed: it now dynamically adjusts the width of the long option name column. This may break tests.
- API Change: Removed parser option `arityRestrictsCumulativeSize` introduced in 3.0.0-alpha-3.

### 3.0.0-beta-2
- Stricter arity validation may break existing applications that intentionally consume arguments regardless of whether arguments are other options.


# <a name="3.0.0-beta-2"></a> Picocli 3.0.0-beta-2
The picocli community is pleased to announce picocli 3.0.0-beta-2.

This release contains enhancements and bug fixes.

This is the twenty-eighth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-beta-2-toc"></a> Table of Contents
* [New and noteworthy](#3.0.0-beta-2-new)
* [Promoted features](#3.0.0-beta-2-promoted)
* [Fixed issues](#3.0.0-beta-2-fixes)
* [Deprecations](#3.0.0-beta-2-deprecated)
* [Potential breaking changes](#3.0.0-beta-2-breaking-changes)

## <a name="3.0.0-beta-2-new"></a> New and Noteworthy

### Stricter Arity Validation

Until this release, options with mandatory parameters would consume as many arguments as required, even if those arguments matched other option flags. For example:

Given a command like this:
```java
class App {
  @Option(names = "-a", arity = "2")
  String[] a;

  @Option(names = "-v")
  boolean v;
}
```
Prior to this change, the following input would be accepted:
```
<command> -a 1 -v
```
In previous versions, picocli accepted this and assigned `"1"` and `"-v"` as the two values for the `-a` option.
From this release, the parser notices that one of the arguments is an option and throws a `MissingParameterException` because not enough parameters were specified for the first option.


## <a name="3.0.0-beta-2-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-beta-2-fixes"></a> Fixed issues
- [#366] API Change: Add `ArgSpec.getTypedValues()` method.
- [#365] Enhancement: Stricter arity validation: options with mandatory parameters no longer consume other option flags.
- [#357] Enhancement: Improve synopsis format. Be more succinct when `limitSplit` is true. Support repeating groups.

## <a name="3.0.0-beta-2-deprecated"></a> Deprecations

## <a name="3.0.0-beta-2-breaking-changes"></a> Potential breaking changes
- Stricter arity validation may break existing applications that intentionally consume arguments regardless of whether arguments are other options.


# <a name="3.0.0-beta-1"></a> Picocli 3.0.0-beta-1
The picocli community is pleased to announce picocli 3.0.0-beta-1.

This release contains enhancements and bug fixes.

This is the twenty-seventh public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-beta-1-toc"></a> Table of Contents
* [New and noteworthy](#3.0.0-beta-1-new)
* [Promoted features](#3.0.0-beta-1-promoted)
* [Fixed issues](#3.0.0-beta-1-fixes)
* [Deprecations](#3.0.0-beta-1-deprecated)
* [Potential breaking changes](#3.0.0-beta-1-breaking-changes)

## <a name="3.0.0-beta-1-new"></a> New and Noteworthy
Previously, the usage message layout had a fixed width long option name column: this column is always 24 characters, even if none of the options have a long option name.

This gives weird-looking usage help messages in some cases. For example:
```java
@Command(name="ls")
class App {
    @Option(names = "-a", description = "display all files") boolean a;
    @Option(names = "-l", description = "use a long listing format") boolean l;
    @Option(names = "-t", description = "sort by modification time") boolean t;
}
```

The usage message for this example was:
```
Usage: ls [-alt]
  -a                          display all files
  -l                          use a long listing format
  -t                          sort by modification time
```
From this release, picocli adjusts the width of the long option name column to the longest name (max 24).

The new usage message for this example looks like this:
```
Usage: ls [-alt]
  -a     display all files
  -l     use a long listing format
  -t     sort by modification time
```

## <a name="3.0.0-beta-1-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-beta-1-fixes"></a> Fixed issues
- [#364] API Change: Remove parser option `arityRestrictsCumulativeSize`.
- [#355] API Change: Add method `ArgSpec.hasInitialValue`.
- [#361] API Change: Add parser option `aritySatisfiedByAttachedOptionParam` for commons-cli compatibility.
- [#363] API Change: Add parser option to limit the number of parts when splitting to max arity, for compatibility with commons-cli.
- [#360] Enhancement: Dynamically adjust width of long option name column (up to max 24).


## <a name="3.0.0-beta-1-deprecated"></a> Deprecations

## <a name="3.0.0-beta-1-breaking-changes"></a> Potential breaking changes

- The usage message format changed: it now dynamically adjusts the width of the long option name column. This may break tests.
- API Change: Removed parser option `arityRestrictsCumulativeSize` introduced in 3.0.0-alpha-3.

# <a name="3.0.0-alpha-6"></a> Picocli 3.0.0-alpha-6
The picocli community is pleased to announce picocli 3.0.0-alpha-6.

This release contains enhancements and bug fixes.

This is the twenty-sixth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-alpha-6-toc"></a> Table of Contents
* [New and noteworthy](#3.0.0-alpha-6-new)
* [Promoted features](#3.0.0-alpha-6-promoted)
* [Fixed issues](#3.0.0-alpha-6-fixes)
* [Deprecations](#3.0.0-alpha-6-deprecated)
* [Potential breaking changes](#3.0.0-alpha-6-breaking-changes)

## <a name="3.0.0-alpha-6-new"></a> New and Noteworthy


## <a name="3.0.0-alpha-6-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-alpha-6-fixes"></a> Fixed issues
- [#349] New API: Add `longestName()` convenience method to OptionSpec.
- [#352] New API: Add method to copy all attributes of a ParserSpec to a CommandSpec.
- [#353] New API: Add method to copy all attributes of a UsageMessageSpec to a CommandSpec.
- [#343] New API: Add method `Help.Ansi.Text::concat` and deprecate the `append` method. ("Append" suggests the Text object is modified, like StringBuilder, but Text is immutable.)
- [#350] Enhancement: Improve error message for `usageHelp` and `versionHelp` validation.
- [#344] Enhancement: Don't show WARN message for unmatched args or overwritten options.
- [#351] Documentation: Improve javadoc for OptionSpec.usageHelp and versionHelp.
- [#354] Bug fix: Interpreter should reset options and positional parameters to their initial value before parsing new input.

## <a name="3.0.0-alpha-6-deprecated"></a> Deprecations
- The `Help.Ansi.Text::append` method is now deprecated in favour of the new `concat` method.

See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-deprecated)

## <a name="3.0.0-alpha-6-breaking-changes"></a> Potential breaking changes
See also breaking changes for
[3.0.0-alpha-5](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-5#3.0.0-alpha-5-breaking-changes),
[3.0.0-alpha-4](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-4#3.0.0-alpha-4-breaking-changes),
[3.0.0-alpha-3](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-3#3.0.0-alpha-3-breaking-changes),
[3.0.0-alpha-2](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-2#3.0.0-alpha-2-breaking-changes),
and [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-breaking-changes).




# <a name="3.0.0-alpha-5"></a> Picocli 3.0.0-alpha-5
The picocli community is pleased to announce picocli 3.0.0-alpha-5.

This release contains enhancements and bug fixes.

This is the twenty-fifth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-alpha-5-toc"></a> Table of Contents

* [New and noteworthy](#3.0.0-alpha-5-new)
* [Promoted features](#3.0.0-alpha-5-promoted)
* [Fixed issues](#3.0.0-alpha-5-fixes)
* [Deprecations](#3.0.0-alpha-5-deprecated)
* [Potential breaking changes](#3.0.0-alpha-5-breaking-changes)

## <a name="3.0.0-alpha-5-new"></a> New and Noteworthy


## <a name="3.0.0-alpha-5-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-alpha-5-fixes"></a> Fixed issues

- [#329] New API: Add parser configuration to control whether boolean flags should be toggled.
- [#328] New API: Provide getter methods on `OptionSpec.Builder` and `PositionalParamSpec.Builder`.
- [#326] New API: Add parser configuration to treat unmatched options as positional parameters.
- [#283] New API: Provide `getMissing` method on MissingParameterException to get a reference to the problematic options and positional parameters. Thanks to [jcapsule](https://github.com/jcapsule) for the suggestion.
- [#334] API Change: Rename `ArgSpec.rawStringValues()` to `ArgSpec.stringValues()`.
- [#342] API Change: Prefix ParseResult methods with `matched` if they return only matched options/positionals.
- [#340] API Change: Rename `ParseResult.optionValue(String, T)` to `matchedOptionValue(String, T)`.
- [#338] API Change: Remove `ParseResult.rawOptionValue(s)` and `rawPositionalValue(s)` methods.
- [#339] API Change: Remove `ParseResult.matchedOptionValue(OptionSpec)` and `matchedPositionalValue(PositionalParamSpec)` methods.
- [#347] API Change: Make `ArgSpec.getValue`, `setValue` and `isMultiValue` public methods.
- [#333] Enhancement: Added subcommand to synopsis in generated usage help. Thanks to [jcapsule](https://github.com/jcapsule) for the pull request.
- [#323] Enhancement: Remove dependency on java.sql package: picocli should only require the java.base module when running in Java 9.
- [#325] Enhancement: Allow custom type converter to map empty String to custom default value for empty options. Thanks to [jesselong](https://github.com/jesselong) for the suggestion.
- [#303] Enhancement: Improve validation to prevent common mistakes.
- [#70]  Enhancement: Positional parameters should only consume values where type conversion succeeds.
- [#346] Enhancement: Validate that arity min is never greater than max.
- [#348] Enhancement: Interpreter should call `ArgSpec.setValue` for every matched option or positional parameter.
- [#327] Bugfix: Default values should not cause options and positional parameters to be added to ParseResult.
- [#330] Bugfix: `Interpreter` should clear option's and positional parameter's `stringValues` list before parsing new input.
- [#335] Bugfix: Abstract class `ArgSpec` should not implement `equals` and `hashCode`.
- [#345] Bugfix: Stop processing varargs when cumulative size reached.

## <a name="3.0.0-alpha-5-deprecated"></a> Deprecations
See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-deprecated)

## <a name="3.0.0-alpha-5-breaking-changes"></a> Potential breaking changes

* Renamed `ArgSpec.rawStringValues()` to `ArgSpec.stringValues()`.
* Renamed `ParseResult` methods with `matched` if they return only matched options/positionals:
    * `options` to `matchedOptions`
    * `positionalParams` to `matchedPositionals`
    * `option(char)`, `option(String)` to `matchedOption`
    * `positional(int)` to `matchedPositional`
    * `hasOption(char)`, `hasOption(String)`, `hasOption(OptionSpec)` to `hasMatchedOption`
    * `hasPositional(int)`, `hasPositional(PositionalParamSpec)` to `hasMatchedPositional`
* Renamed `ParseResult.optionValue(String, T)` to `matchedOptionValue(String, T)`, and `positionalValue` to `matchedPositionalValue`.
* Removed `ParseResult::rawOptionValue(s)` and `rawPositionalValue(s)` methods.
* Removed `ParseResult.matchedOptionValue(OptionSpec)` and `matchedPositionalValue(PositionalParamSpec)` methods.

See also breaking changes for
[3.0.0-alpha-4](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-4#3.0.0-alpha-4-breaking-changes),
[3.0.0-alpha-3](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-3#3.0.0-alpha-3-breaking-changes),
[3.0.0-alpha-2](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-2#3.0.0-alpha-2-breaking-changes),
and [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-breaking-changes).




# <a name="3.0.0-alpha-4"></a> Picocli 3.0.0-alpha-4
The picocli community is pleased to announce picocli 3.0.0-alpha-4.

This release contains enhancements and bug fixes.

This is the twenty-fourth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-alpha-4-toc"></a> Table of Contents

* [New and noteworthy](#3.0.0-alpha-4-new)
* [Promoted features](#3.0.0-alpha-4-promoted)
* [Fixed issues](#3.0.0-alpha-4-fixes)
* [Deprecations](#3.0.0-alpha-4-deprecated)
* [Potential breaking changes](#3.0.0-alpha-4-breaking-changes)

## <a name="3.0.0-alpha-4-new"></a> New and Noteworthy


## <a name="3.0.0-alpha-4-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-alpha-4-fixes"></a> Fixed issues

- [#318] API Change: Split model IBinding into IGetter and ISetter.
- [#320] API Change: Rename parser config `maxArityIsMaxTotalParams` to `arityRestrictsCumulativeSize`.
- [#216] Enhancement: Parsed values now replace the default value of multi-value (array, Collection or Map) options and positional parameters instead of being appended to them. Thanks to [wiwie](https://github.com/wiwie) for the request.
- [#261] Enhancement: Options and positional parameters with a `defaultValue` are never required. Thanks to [ymenager](https://github.com/ymenager) for the request.
- [#315] Enhancement: Initialize ArgSpec value with `defaultValue` before parsing command line.
- [#263] Bugfix: positional parameter defaults were not shown in usage help message. Thanks to [ymenager](https://github.com/ymenager) for the bug report.

## <a name="3.0.0-alpha-4-deprecated"></a> Deprecations
See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-deprecated)

## <a name="3.0.0-alpha-4-breaking-changes"></a> Potential breaking changes
- Parsed values now replace the default value of multi-value (array, Collection or Map) options and positional parameters instead of being appended to them.
- The `IBinding` interface introduced in v3.0.0-alpha-1 has been replaced with two functional interfaces `IGetter` and `ISetter`.
- The `UnmatchedArgsBinding` factory methods introduced in v3.0.0-alpha-1 have been replaced with `forStringArrayConsumer` and `forStringCollectionSupplier`.


See also breaking changes for [3.0.0-alpha-3](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-3#3.0.0-alpha-3-breaking-changes),
[3.0.0-alpha-2](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-2#3.0.0-alpha-2-breaking-changes),
and [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-breaking-changes).



# <a name="3.0.0-alpha-3"></a> Picocli 3.0.0-alpha-3
The picocli community is pleased to announce picocli 3.0.0-alpha-3.

This release includes changes to allow picocli to be configured to function like Apache Commons CLI, to support [GROOVY-8520](https://issues.apache.org/jira/browse/GROOVY-8520):
* `maxArityIsMaxTotalParams` parser configuration option to use `arity` to limit the total number of values accumulated in an option or positional parameter.
* Usage message width can now be configured programmatically.
* "Lenient" mode when extracting annotations from a class where picocli annotations are optional (to allow mixing Groovy CLI annotations in Groovy CliBuilder).
* Change semantics of ParseResult.rawOptionValue to mean values after split (but before type conversion).

See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1) and [3.0.0-alpha-2](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-2#3.0.0-alpha-2) for recent functional changes.

This is the twenty-third public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-alpha-3-toc"></a> Table of Contents

* [New and noteworthy](#3.0.0-alpha-3-new)
* [Promoted features](#3.0.0-alpha-3-promoted)
* [Fixed issues](#3.0.0-alpha-3-fixes)
* [Deprecations](#3.0.0-alpha-3-deprecated)
* [Potential breaking changes](#3.0.0-alpha-3-breaking-changes)

## <a name="3.0.0-alpha-3-new"></a> New and Noteworthy
### Max Arity Is Max Total Params

This release introduces a `maxArityIsMaxTotalParams` parser configuration option.

By default, the arity of an option is the number of arguments _for each occurrence_ of the option.
For example, if option `-a` has `arity = "2"`, then the following is a perfectly valid command:
for each occurrence of the option, two option parameters are specified.
```bash
<command> -a 1 2 -a 3 4 -a 5 6
```
However, if `CommandLine.setMaxArityIsMaxTotalParams(true)` is called first, the above example would result in a `MaxValuesExceededException` because the total number of values (6) exceeds the arity of 2.

Additionally, by default, when `maxArityIsMaxTotalParams` is `false`, arity is only applied _before_ the argument is split into parts,
while when `maxArityIsMaxTotalParams` is set to `true`, validation is applied _after_ a command line argument has been split into parts.

For example, if we have an option like this:
```java
@Option(name = "-a", arity = "1..2", split = ",") String[] values;
```
By default, the following input would be a valid command:
```bash
<command> -a 1,2,3
```
By default, the option arity tells the parser to consume 1 to 2 arguments, and the option was followed by a single parameter, `"1,2,3"`, which is fine.

However, if `maxArityIsMaxTotalParams` is set to true, the above example would result in a `MaxValuesExceededException` because the argument is split into 3 parts, which exceeds the max arity of 2.

## <a name="3.0.0-alpha-3-promoted"></a> Promoted Features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-alpha-3-fixes"></a> Fixed issues

- [#313] Enhancement and New API: add method `CommandLine::setMaxArityIsMaxTotalParams` to configure the parser to use `arity` to limit the total number of values accumulated in an option or positional parameter.
- [#314] Enhancement and New API: add method `CommandLine::setUsageHelpWidth` and `UsageMessageSpec::width` to set the max usage help message width.
- [#316] Enhancement: Support lenient mode where annotations are optional when extracting annotations.
- [#317] Enhancement: Change semantics of ParseResult.rawOptionValue to mean values after split (but before type conversion).

## <a name="3.0.0-alpha-3-deprecated"></a> Deprecations
See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-deprecated)

## <a name="3.0.0-alpha-3-breaking-changes"></a> Potential breaking changes
- Utility method `CommandLine.Help.join` signature changed: now takes an additional `usageHelpWidth` parameter.
- Constructor `CommandLine.Help.Layout(ColorScheme)` signature changed: now takes an additional `usageHelpWidth` parameter.
- Public field `CommandLine.Help.TextTable.columns` is now private; added public method `CommandLine.Help.TextTable.columns()`.
- Constructor `CommandLine.Help.TextTable(Ansi)` is replaced with factory method `CommandLine.Help.TextTable.forDefaultColumns(Ansi, int)`.
- Constructor `CommandLine.Help.TextTable(Ansi, int...)` is replaced with factory method `CommandLine.Help.TextTable.forColumnWidths(Ansi, int...)`.
- Constructor `CommandLine.Help.TextTable(Ansi, Column...)` modifier changed from public to protected.
- Added factory method `CommandLine.Help.TextTable.forColumns(Ansi, Column...)`.
- Renamed `CommandLine.MaxValuesforFieldExceededException` to `CommandLine.MaxValuesExceededException`.

See [3.0.0-alpha-2](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-2#3.0.0-alpha-2-breaking-changes).
See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-breaking-changes).



# <a name="3.0.0-alpha-2"></a> Picocli 3.0.0-alpha-2
The picocli community is pleased to announce picocli 3.0.0-alpha-2.

This release includes some bug fixes and small enhancements. See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1) for recent functional changes.

This is the twenty-second public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-alpha-2-fixes"></a> Fixed issues

- [#312] Enhancement and API change: Remove `AbstractSimpleParseResultHandler` class and `parseWithSimpleHandlers` method.
- [#311] Enhancement and API change: Simplify parseWithHandlers: removed prototypeReturnValue parameter.
- [#307] Enhancement: Provide CommandLine.usage(PrintWriter) method for testing and to facilitate [GROOVY-8520](https://issues.apache.org/jira/browse/GROOVY-8520) migration from commons-cli to picocli.
- [#306] Enhancement: Support generating autocompletion scripts for non-public @Command classes. Thanks to [cbeams](https://github.com/cbeams) for the request.
- [#308] Enhancement: Provide API to disallow POSIX clustered short options.
- [#310] Enhancement: PicocliBaseScript should follow conventions for stdout and stderr: requested help to stdout, invalid input usage help to stderr.
- [#309] Bugfix: Tests were failing on environments that support ANSI colors.

## <a name="3.0.0-alpha-2-deprecated"></a> Deprecations
See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-deprecated)

## <a name="3.0.0-alpha-2-breaking-changes"></a> Potential breaking changes
- [#311] API change from 3.0.0-alpha-1: the `parseWithHandlers` methods signature changed: removed the `prototypeReturnValue` parameter.
- [#312] API change from 3.0.0-alpha-1: Remove `AbstractSimpleParseResultHandler` class and `parseWithSimpleHandlers` method.

See [3.0.0-alpha-1](https://github.com/remkop/picocli/releases/tag/v3.0.0-alpha-1#3.0.0-alpha-1-breaking-changes)

# <a name="3.0.0-alpha-1"></a> Picocli 3.0.0-alpha-1
The picocli community is pleased to announce picocli 3.0.0-alpha-1.

This release offers a programmatic API for creating command line applications, in addition to annotations. The programmatic API allows applications to dynamically create command line options on the fly, and also makes it possible to create idiomatic domain-specific languages for processing command line arguments, using picocli, in other JVM languages.

Another new feature in this release are Mixins. Mixins allow reusing common options, parameters and command attributes in multiple applications without copy-and-paste duplication.

Third, this release aims to reduce boilerplate code in user applications even further with the new `mixinStandardHelpOptions` command attribute. Picocli adds standard `usageHelp` and `versionHelp` options to commands with this attribute. Additionally picocli now offers a `HelpCommand` that can be installed as a subcommand on any application command to provide usage help for the parent command or sibling subcommands.

From this release, picocli is better at following unix conventions: print to stdout when the user requested help, and print to stderr when the input was invalid or an unexpected error occurred.

Also, this release gives better control over the process exit code.

Additionally, fields annotated with `@Unmatched` will be populated with the unmatched arguments.

Furthermore, this release adds a `showDefaultValue` attribute to the `@Option` and `@Parameters` annotation.

This is the twenty-first public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="3.0.0-alpha-1-toc"></a> Table of Contents

* [New and noteworthy](#3.0.0-alpha-1-new)
* [Promoted features](#3.0.0-alpha-1-promoted)
* [Fixed issues](#3.0.0-alpha-1-fixes)
* [Deprecations](#3.0.0-alpha-1-deprecated)
* [Potential breaking changes](#3.0.0-alpha-1-breaking-changes)

## <a name="3.0.0-alpha-1-new"></a> New and Noteworthy

### <a name="3.0.0-alpha-1-Programmatic-API"></a> Programmatic API (INCUBATING)
This release offers a programmatic API for creating command line applications, in addition to annotations. The programmatic API allows applications to dynamically create command line options on the fly, and also makes it possible to create idiomatic domain-specific languages for processing command line arguments, using picocli, in other JVM languages.

Note that the programmatic API is incubating and the API may change in subsequent releases. _If you have suggestions for improving the programmatic API, please raise a ticket on GitHub!_

#### Example
```java
CommandSpec spec = CommandSpec.create();
spec.mixinStandardHelpOptions(true); // usageHelp and versionHelp options
spec.addOption(OptionSpec.builder("-c", "--count")
        .paramLabel("COUNT")
        .type(int.class)
        .description("number of times to execute").build());
spec.addPositional(PositionalParamSpec.builder()
        .paramLabel("FILES")
        .type(List.class)
        .auxiliaryTypes(File.class) // List<File>
        .description("The files to process").build());
CommandLine commandLine = new CommandLine(spec);

commandLine.parseWithSimpleHandlers(new AbstractSimpleParseResultHandler() {
    public void handle(ParseResult pr) {
        int count = pr.optionValue('c', 1);
        List<File> files = pr.positionalValue(0, Collections.<File>emptyList());
        for (int i = 0; i < count; i++) {
            for (File f : files) {
                System.out.printf("%d: %s%n", i, f);
            }
        }
    }
}, args);
```

#### CommandSpec (INCUBATING)
`CommandSpec` models a command. It is the programmatic variant of the `@Command` annotation. It has a name and a version, both of which may be empty.  It also has a `UsageMessageSpec` to configure aspects of the usage help message and a `ParserSpec` that can be used to control the behaviour of the parser.

#### OptionSpec and PositionalParamSpec (INCUBATING)
`OptionSpec` models a named option, and `PositionalParamSpec` models one or more positional parameters. They are the programmatic variant of the `@Option` and `@Parameters` annotations, respectively.

An `OptionSpec` must have at least one name, which is used during parsing to match command line arguments. Other attributes can be left empty and picocli will give them a reasonable default value. This defaulting is why `OptionSpec` objects are created with a builder: this allows you to specify only some attributes and let picocli initialise the other attributes. For example, if only the option’s name is specified, picocli assumes the option takes no parameters (arity = 0), and is of type `boolean`. Another example, if arity is larger than `1`, picocli sets the type to `List` and the `auxiliary type` to `String`.

`PositionalParamSpec` objects don’t have names, but have an index range instead. A single `PositionalParamSpec` object can capture multiple positional parameters. The default index range is set to `0..*` (all indices). A command may have multiple `PositionalParamSpec` objects to capture positional parameters at different index ranges. This can be useful if positional parameters at different index ranges have different data types.

Similar to `OptionSpec` objects, Once a `PositionalParamSpec` is constructed, its configuration becomes immutable, but its `value` can still be modified. Usually the value is set during command line parsing when a non-option command line argument is encountered at a position in its index range.

#### <a name="3.0.0-alpha-1-ParseResult"></a> ParseResult (INCUBATING)
A `ParseResult` class is now available that allows applications to inspect the result of parsing a sequence of command line arguments.

This class provides methods to query whether the command line arguments included certain options or position parameters, and what the value or values of these options and positional parameters was. Both the original command line argument String value as well as a strongly typed value can be obtained.


### Mixins for Reuse
Mixins are a convenient alternative to subclassing: picocli annotations from _any_ class can be added to ("mixed in" with) another command. This includes options, positional parameters, subcommands and command attributes. Picocli [autoHelp](#3.0.0-alpha-1-autohelp) internally uses a mixin.

A mixin is a separate class with options, positional parameters, subcommands and command attributes that can be reused in other commands. Mixins can be installed by calling the `CommandLine.addMixin` method with an object of this class, or annotating a field in your command with `@Mixin`. Here is an example mixin class:

```java
public class ReusableOptions {

    @Option(names = { "-v", "--verbose" }, description = {
        "Specify multiple -v options to increase verbosity.", "For example, `-v -v -v` or `-vvv`" })
    protected boolean[] verbosity = new boolean[0];
}
```

#### Adding Mixins Programmatically
The below example shows how a mixin can be added programmatically with the `CommandLine.addMixin` method.

```java
CommandLine commandLine = new CommandLine(new MyCommand());
commandline.addMixin("myMixin", new ReusableOptions());
```
#### `@Mixin` Annotation
A command can also include mixins by annotating fields with `@Mixin`. All picocli annotations found in the mixin class
are added to the command that has a field annotated with `@Mixin`. For example:

```java
@Command(name = "zip", description = "Example reuse with @Mixin annotation.")
public class MyCommand {

    // adds the options defined in ReusableOptions to this command
    @Mixin
    private ReusableOptions myMixin;
}
```


### <a name="3.0.0-alpha-1-mixinStandardHelpOptions"></a> Standard Help Options
This release introduces the `mixinStandardHelpOptions` command attribute. When this attribute is set to `true`, picocli adds a mixin to the command that adds `usageHelp` and `versionHelp` options to the command. For example:

```java
@Command(mixinStandardHelpOptions = true, version = "auto help demo - picocli 3.0")
class AutoHelpDemo implements Runnable {

    @Option(names = "--option", description = "Some option.")
    String option;

    @Override public void run() { }
}
```

Commands with `mixinStandardHelpOptions` do not need to explicitly declare fields annotated with `@Option(usageHelp = true)` and `@Option(versionHelp = true)` any more. The usage help message for the above example looks like this:
```text
Usage: <main class> [-hV] [--option=<option>]
      --option=<option>       Some option.
  -h, --help                  Show this help message and exit.
  -V, --version               Print version information and exit.
```

### <a name="3.0.0-alpha-1-HelpCommand"></a> Help Command

From this release, picocli provides a `help` subcommand (`picocli.CommandLine.HelpCommand`) that can be installed as a subcommand on any application command to provide usage help for the parent command or sibling subcommands. For example:

```java
@Command(subcommands = HelpCommand.class)
class AutoHelpDemo implements Runnable {

    @Option(names = "--option", description = "Some option.")
    String option;

    @Override public void run() { }
}
```


```text
# print help for the `maincommand` command
maincommand help

# print help for the `subcommand` command
maincommand help subcommand
```

For applications that want to create a custom help command, this release also introduces a new interface `picocli.CommandLine.IHelpCommandInitializable` that provides custom help commands with the information they need: access to the parent command and sibling commands, whether to use Ansi colors or not, and the streams to print the usage help message to.

### <a name="3.0.0-alpha-1-Unmatched"></a> `@Unmatched` Annotation
From this release, fields annotated with `@Unmatched` will be populated with the unmatched arguments.
The field must be of type `String[]` or `List<String>`.

If picocli finds a field annotated with `@Unmatched`, it automatically sets `unmatchedArgumentsAllowed` to `true`
so no `UnmatchedArgumentException` is thrown when a command line argument cannot be assigned to an option or positional parameter.

### <a name="3.0.0-alpha-1-std"></a> Stdout or Stderr
From picocli v3.0, the `run` and `call` convenience methods follow unix conventions:
print to stdout when the user requested help, and print to stderr when the input was invalid or an unexpected error occurred.

Custom handlers can extend `AbstractHandler` to facilitate following this convention.
`AbstractHandler` also provides `useOut` and `useErr` methods to allow customizing the target output streams,
and `useAnsi` to customize the Ansi style to use:

```java
@Command class CustomizeTargetStreamsDemo implements Runnable {
    public void run() { ... }

    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new CustomizeTargetStreamsDemo());

        PrintStream myOut = getOutputPrintStream(); // custom stream to send command output to
        PrintStream myErr = getErrorPrintStream();  // custom stream for error messages

        cmd.parseWithHandlers(
                new RunLast().useOut(myOut).useAnsi(Help.Ansi.ON),
                new DefaultExceptionHandler().useErr(myErr).useAnsi(Help.Ansi.OFF),
                args);
    }
}
```

### <a name="3.0.0-alpha-1-exit-code"></a> Exit Code Support
From picocli v3.0, the built-in parse result handlers (`RunFirst`, `RunLast` and `RunAll`) and exception handler
(`DefaultExceptionHandler`) can specify an exit code.
If an exit code was specified, the handler terminates the JVM with the specified status code when finished.

```java
@Command class ExitCodeDemo implements Runnable {
    public void run() { throw new ParameterException(new CommandLine(this), "exit code demo"); }

    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new ExitCodeDemo());
        cmd.parseWithHandlers(
                new RunLast().andExit(123),
                new DefaultExceptionHandler().andExit(456),
                args);
    }
}
```
Running this command prints the following to stderr and exits the JVM with status code `456`.

```
exit code demo
Usage: <main class>
```

Custom handlers can extend `AbstractHandler` to inherit this behaviour.


### Fine-grained ShowDefault

This release adds a `showDefaultValue` attribute to the `@Option` and `@Parameters` annotation. This allows you to specify for each individual option and positional parameter whether its default value should be shown in the usage help.

This attribute accepts three values:

* `ALWAYS` - always display the default value of this option or positional parameter, even `null` values, regardless what value of `showDefaultValues` was specified on the command
* `NEVER` - don't show the default value for this option or positional parameter, regardless what value of `showDefaultValues` was specified on the command
* `ON_DEMAND` - (this is the default) only show the default value for this option or positional parameter if `showDefaultValues` was specified on the command

The `NEVER` value is useful for security sensitive command line arguments like passwords. The `ALWAYS` value is useful when you only want to show the default value for a few arguments but not for all (in combination with `@Command(showDefaultValues = false)`).


## <a name="3.0.0-alpha-1-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="3.0.0-alpha-1-fixes"></a> Fixed issues

- [#245] New Feature: from 3.0, picocli offers an API for programmatic configuration.
- [#257] New Feature: new `ParseResult` class allows programmatic inspection of the result of parsing a sequence of command line arguments.
- [#144] New Feature: added support for mixins to allow reusing common options, positional parameters, subcommands and command attributes from any object.
- [#253] New Feature: added `@Unmatched` annotation for unmatched arguments.
- [#175] New Feature: `mixinStandardHelpOptions` attribute to install the standard `--help` and `--version` options, obviating the need for fields annotated with `@Option(usageHelp = true)` and `@Option(versionHelp = true)`.
- [#175] New Feature: picocli now provides a `HelpCommand` that can be installed as a subcommand on any application command to provide usage help for the parent command or sibling subcommands.
- [#175] New Feature: new `IHelpCommandInitializable` interface facilitates construction of custom help commands.
- [#250] Enhancement: the `run` and `call` convenience methods now follow convention: print to stdout when the user requested help, print to stderr when the input was invalid or an unexpected error occurred. Added `AbstractHandler` to facilitate following this convention for custom parse result handlers and exception handlers.
- [#251] New Feature: exit code support. The built-in parse result handlers (`RunFirst`, `RunLast` and `RunAll`) and exception handler  (`DefaultExceptionHandler`) can now optionally specify an exit code. If specified, the handler terminates the JVM with the specified status code when finished. Custom handlers can extend `AbstractHandler` to inherit this behaviour.
- [#262] New Feature: new `showDefaultValue` attribute on `@Option` and `@Parameters` gives fine-grained control over which default values to show or hide. Thanks to [ymenager](https://github.com/ymenager) for the request.
- [#268] New Feature: new `helpCommand` attribute on `@Command`: if the command line arguments contain a subcommand annotated with `helpCommand`, the parser will not validate the required options or positional parameters of the parent command. Thanks to [ymenager](https://github.com/ymenager) for the request.
- [#277] New Feature: new `hidden` attribute on `@Command` to omit the specified subcommand from the usage help message command list of the parent command. Thanks to [pditommaso](https://github.com/pditommaso).
- [#159] Enhancement: make help usage message width configurable. Thanks to [pditommaso](https://github.com/pditommaso).

## <a name="3.0.0-alpha-1-deprecated"></a> Deprecations

The `picocli.CommandLine.Help::Help(Object, CommandLine.Help.ColorScheme)` constructor has been deprecated. Use the `picocli.CommandLine.Help::Help(CommandLine.CommandSpec, CommandLine.Help.ColorScheme)` constructor instead.

The `picocli.CommandLine.IParseResultHandler` interface has been deprecated. Use the `picocli.CommandLine.IParseResultHandler2` interface instead.

The `picocli.CommandLine.IExceptionHandler` interface has been deprecated. Use the `picocli.CommandLine.IExceptionHandler2` interface instead.

## <a name="3.0.0-alpha-1-breaking-changes"></a> Potential breaking changes

### Help API Changes
The following public fields were removed from the `picocli.CommandLine.Help` class. Instead, set these attributes on a `CommandLine.CommandSpec` object passed to any of the `Help` constructors.

* abbreviateSynopsis
* commandListHeading
* commandName
* customSynopsis
* description
* descriptionHeading
* footer
* footerHeading
* header
* headerHeading
* optionFields
* optionListHeading
* parameterLabelRenderer - replaced with the `Help.parameterLabelRenderer()` method
* parameterListHeading
* requiredOptionMarker
* separator
* showDefaultValues
* sortOptions
* synopsisHeading

Method signature changes on inner classes and interfaces of the `Help` class:

* Interface method `CommandLine.Help.IOptionRenderer::render` signature changed: `CommandLine.Option` and `Field` parameters are replaced with a single `CommandLine.OptionSpec` parameter.
* Interface method `CommandLine.Help.IParameterRenderer::render` signature changed: `CommandLine.Parameters` and `Field` parameters are replaced with a single `CommandLine.PositionalParamSpec` parameter.
* Interface method `CommandLine.Help.IParamLabelRenderer::renderParameterLabel` signature changed: `Field` parameter replaced with `CommandLine.ArgSpec` parameter.
* Class `CommandLine.Help.Layout` all methods changed: `Field` parameters replaced by `CommandLine.ArgSpec`, `CommandLine.OptionSpec` and `CommandLine.PositionalParamSpec` parameters.




# <a name="2.3.0"></a> Picocli 2.3.0
The picocli community is pleased to announce picocli 2.3.0.

This release contains bugfixes and new features.

This release introduces a new parser flag `stopAtPositional` to treat the first positional parameter as end-of-options, and a `stopAtUnmatched` parser flag to stop matching options and positional parameters as soon as an unmatched argument is encountered.

These flags are useful for applications that need to delegate part of the command line to third party commands.

This release offers better support for options with optional values, allowing applications to distinguish between cases where the option was not specified at all, and cases where the option was specified without a value.


This is the twentieth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.3.0-toc"></a> Table of Contents

* [New and noteworthy](#2.3.0-new)
* [Promoted features](#2.3.0-promoted)
* [Fixed issues](#2.3.0-fixes)
* [Deprecations](#2.3.0-deprecated)
* [Potential breaking changes](#2.3.0-breaking-changes)

## <a name="2.3.0-new"></a> New and noteworthy

### Stop At Positional
By default, positional parameters can be mixed with options on the command line, but this is not always desirable. From this release, applications can call `CommandLine.setStopAtPositional(true)` to force the parser to treat all values following the first positional parameter as positional parameters.

When this flag is set, the first positional parameter effectively serves as an "end of options" marker, without requiring a separate `--` argument.

### Stop At Unmatched
From this release, applications can call `CommandLine.setStopAtUnmatched(true)` to force the parser to stop interpreting options and positional parameters as soon as it encounters an unmatched argument.

When this flag is set, the first unmatched argument and all subsequent command line arguments are added to the unmatched arguments list returned by `CommandLine.getUnmatchedArguments()`.


### Optional Values
If an option is defined with `arity = "0..1"`, it may or not have a parameter value. If such an option is specified without a value on the command line, it is assigned an empty String. If the option is not specified, it keeps its default value. For example:

```java
class OptionalValueDemo implements Runnable {
    @Option(names = "-x", arity = "0..1", description = "optional parameter")
    String x;

    public void run() { System.out.printf("x = '%s'%n", x); }

    public static void main(String... args) {
       CommandLine.run(new OptionalValueDemo(), System.out, args);
    }
}
```
Gives the following results:
```bash
java OptionalValueDemo -x value
x = 'value'

java OptionalValueDemo -x
x = ''

java OptionalValueDemo
x = 'null'
```


## <a name="2.3.0-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.3.0-fixes"></a> Fixed issues

- [#215] API: `stopAtUnmatched` flag to stop parsing on first unmatched argument. Thanks to [defnull](https://github.com/defnull) for the request.
- [#284] API: `stopAtPositional` flag to treat first positional parameter as end-of-options. Thanks to [defnull](https://github.com/defnull) and [pditommaso](https://github.com/pditommaso) for the request.
- [#279] Enhancement: assign empty String when String option was specified without value. Thanks to [pditommaso](https://github.com/pditommaso) for the request.
- [#285] Bugfix: Vararg positional parameters should not consume options. Thanks to [pditommaso](https://github.com/pditommaso) for the bug report.
- [#286] Documentation: clarify when picocli instantiates fields for options and positional parameters. Thanks to [JanMosigItemis](https://github.com/JanMosigItemis) for pointing this out.

## <a name="2.3.0-deprecated"></a> Deprecations

This release has no additional deprecations.

## <a name="2.3.0-breaking-changes"></a> Potential breaking changes

This release has no breaking changes.


# <a name="2.2.2"></a> Picocli 2.2.2
The picocli community is pleased to announce picocli 2.2.2.

This is a bugfix release.


This is the nineteenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.2.2-toc"></a> Table of Contents

* [New and noteworthy](#2.2.2-new)
* [Promoted features](#2.2.2-promoted)
* [Fixed issues](#2.2.2-fixes)
* [Deprecations](#2.2.2-deprecated)
* [Potential breaking changes](#2.2.2-breaking-changes)

## <a name="2.2.2-new"></a> New and noteworthy

This is a bugfix release and does not include any new features.

## <a name="2.2.2-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.2.2-fixes"></a> Fixed issues

- [#282] Bugfix: unmatched option heuristic did not work when there were no options to compare against. Thanks to [jcapsule](https://github.com/jcapsule).

## <a name="2.2.2-deprecated"></a> Deprecations

This release has no additional deprecations.

## <a name="2.2.2-breaking-changes"></a> Potential breaking changes

This release has no breaking changes.



# <a name="2.2.1"></a> Picocli 2.2.1
The picocli community is pleased to announce picocli 2.2.1.

This is a bugfix release.


This is the eighteenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.2.1-toc"></a> Table of Contents

* [New and noteworthy](#2.2.1-new)
* [Promoted features](#2.2.1-promoted)
* [Fixed issues](#2.2.1-fixes)
* [Deprecations](#2.2.1-deprecated)
* [Potential breaking changes](#2.2.1-breaking-changes)

## <a name="2.2.1-new"></a> New and noteworthy

This is a bugfix release and does not include any new features.

## <a name="2.2.1-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.2.1-fixes"></a> Fixed issues

- [#254] Bugfix: Starting from 2.0.3, usage help didn't render empty lines in multi-line descriptions.
- [#255] Documentation: Update Kotlin example in user manual for the new Kotlin 1.2 array literal syntax in annotations.

## <a name="2.2.1-deprecated"></a> Deprecations

This release has no additional deprecations.

## <a name="2.2.1-breaking-changes"></a> Potential breaking changes

This release has no breaking changes.




# <a name="2.2.0"></a> Picocli 2.2.0

The picocli community is pleased to announce picocli 2.2.

This release is a "Project Coin"-like release for picocli: small changes with a nice pay-off.


In command line applications with subcommands, options of the parent command are often intended as "global" options that apply to all the subcommands. This release introduces a new `@ParentCommand` annotation that makes it easy for subcommands to access such parent command options: fields of the subcommand annotated with `@ParentCommand` are initialized with a reference to the parent command.

This release adds support for more built-in types, so applications don't need to register custom converters for common types. The new types include Java 7 classes like `java.nio.file.Path` and Java 8 classes like the value classes in the `java.time` package. These converters are loaded using reflection and are not available when running on Java 5 or Java 6.

This release also adds a `converter` attribute to the `@Option` and `@Parameter` annotations. This allows a specific option or positional parameter to use a different converter than would be used by default based on the type of the field.

Furthermore, the `@Command` annotation now supports a `versionProvider` attribute. This is useful when the version of an application should be detected dynamically at runtime. For example, an implementation may return version information obtained from the JAR manifest, a properties file or some other source.

Finally, applications may now specify a custom factory for instantiating classes that were configured as annotation attributes, like subcommands, type converters and version providers.



This is the seventeenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.2.0-toc"></a> Table of Contents

* [New and noteworthy](#2.2.0-new)
* [Promoted features](#2.2.0-promoted)
* [Fixed issues](#2.2.0-fixes)
* [Deprecations](#2.2.0-deprecated)
* [Potential breaking changes](#2.2.0-breaking-changes)

## <a name="2.2.0-new"></a> New and noteworthy

### New `@ParentCommand` annotation

In command line applications with subcommands, options of the top level command are often intended as "global" options that apply to all the subcommands. Prior to this release, subcommands had no easy way to access their parent command options unless the parent command made these values available in a global variable.

The `@ParentCommand` annotation makes it easy for subcommands to access their parent command options: subcommand fields annotated with `@ParentCommand` are initialized with a reference to the parent command. For example:

```java
@Command(name = "fileutils", subcommands = List.class)
class FileUtils {

    @Option(names = {"-d", "--directory"},
            description = "this option applies to all subcommands")
    File baseDirectory;
}

@Command(name = "list")
class List implements Runnable {

    @ParentCommand
    private FileUtils parent; // picocli injects reference to parent command

    @Option(names = {"-r", "--recursive"},
            description = "Recursively list subdirectories")
    private boolean recursive;

    @Override
    public void run() {
        list(new File(parent.baseDirectory, "."));
    }

    private void list(File dir) {
        System.out.println(dir.getAbsolutePath());
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                System.out.println(f.getAbsolutePath());
                if (f.isDirectory() && recursive) {
                    list(f);
                }
            }
        }
    }
}
```

### More built-in types

This release adds support for more built-in types, so applications don't need to register custom converters for common types. The new types include Java 7 classes like `java.nio.file.Path` and Java 8 classes like the value classes in the `java.time` package. These converters are loaded using reflection and are not available when running on Java 5 or Java 6.

Converters for the following types were added in this release:

* `java.nio.file.Path` (requires Java 7 or higher)
* `java.time` value objects: `Duration`, `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `MonthDay`, `OffsetDateTime`, `OffsetTime`, `Period`, `Year`, `YearMonth`, `ZonedDateTime`, `ZoneId`, `ZoneOffset`  (requires Java 8 or higher, invokes the `parse` method of these classes)
* `java.lang.Class` (for the fully qualified class name)
* `java.nio.ByteOrder` (for the Strings `"BIG_ENDIAN"` or `"LITTLE_ENDIAN"`)
* `java.util.Currency` (for the ISO 4217 code of the currency)
* `java.net.NetworkInterface` (for the InetAddress or name of the network interface)
* `java.util.TimeZoneConverter` (for the ID for a TimeZone)
* `java.sql.Connection` (for a database url of the form `jdbc:subprotocol:subname`)
* `java.sql.Driver` (for a database URL of the form `jdbc:subprotocol:subname`)
* `java.sql.Timestamp` (for values in the `"yyyy-MM-dd HH:mm:ss"` or `"yyyy-MM-dd HH:mm:ss.fffffffff"` formats)

### Option-specific Type Converters
This release adds a `converter` attribute to the `@Option` and `@Parameter` annotations. This allows a specific option or positional parameter to use a different converter than would be used by default based on the type of the field.

For example, you may want to convert the constant names defined in [`java.sql.Types`](https://docs.oracle.com/javase/9/docs/api/java/sql/Types.html) to their `int` value for a specific field, but this should not impact any other `int` fields: other `int` fields should continue to use the default int converter that parses numeric values.

Example usage:

```java
class App {
    @Option(names = "--sqlType", converter = SqlTypeConverter.class)
    int sqlType;
}
```

Example implementation:

```java
class SqlTypeConverter implements ITypeConverter<Integer> {
    public Integer convert(String value) throws Exception {
        switch (value) {
            case "ARRAY"  : return Types.ARRAY;
            case "BIGINT" : return Types.BIGINT;
            case "BINARY" : return Types.BINARY;
            case "BIT"    : return Types.BIT;
            case "BLOB"   : return Types.BLOB;
            //...
        }
    }
}
```

### Dynamic Version Information
From this release, the `@Command` annotation supports a `versionProvider` attribute. Applications may specify a `IVersionProvider` implementation in this attribute, and picocli will instantiate this class
and invoke it to collect version information.

This is useful when the version of an application should be detected dynamically at runtime. For example, an implementation may return version information obtained from the JAR manifest, a properties file or some other source.

Custom version providers need to implement the `picocli.CommandLine.IVersionProvider` interface:

```java
public interface IVersionProvider {
    /**
     * Returns version information for a command.
     * @return version information (each string in the array is displayed on a separate line)
     * @throws Exception an exception detailing what went wrong when obtaining version information
     */
    String[] getVersion() throws Exception;
}
```

The GitHub project has a manifest file-based [example](https://github.com/remkop/picocli/blob/main/examples/src/main/java/picocli/examples/VersionProviderDemo2.java) and a build-generated version properties file-based [example](https://github.com/remkop/picocli/blob/main/examples/src/main/java/picocli/examples/VersionProviderDemo1.java) version provider implementation.

### Custom factory
Declaratively registered subcommands, type converters and version providers must be instantiated somehow. From this release, a custom factory can be specified when constructing a `CommandLine` instance. This allows full control over object creation and opens possibilities for Inversion of Control and Dependency Injection. For example:

```jshelllanguage
IFactory myFactory = getCustomFactory();
CommandLine cmdLine = new CommandLine(new Git(), myFactory);
```

## <a name="2.2.0-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.2.0-fixes"></a> Fixed issues

- [#247] New `@ParentCommand` annotation to inject a reference to the parent command into subcommand fields. Thanks to [michaelpj](https://github.com/michaelpj) for the request.
- [#83]  Add more built-in converters. Thanks to [garydgregory](https://github.com/garydgregory/jcommander-addons) for the inspiration.
- [#237] Option and Positional Parameter-specific type converters. Thanks to [godzsa](https://github.com/godzsa) for the request.
- [#236] Allow obtaining version information dynamically at runtime. Thanks to [kcris](https://github.com/kcris) for the request.
- [#169] Configurable factory to instantiate subcommands that are registered via annotation attributes. Thanks to [kakawait](https://github.com/kakawait) for the request.
- [#252] Example version provider implementations.

## <a name="2.2.0-deprecated"></a> Deprecations

This release has no additional deprecations.

## <a name="2.2.0-breaking-changes"></a> Potential breaking changes

This release has no breaking changes.



# <a name="2.1.0"></a> Picocli 2.1.0

This release contains bugfixes and new features.

Users sometimes run into system limitations on the length of a command line when creating a command line with lots of options or with long arguments for options.
Starting from this release, picocli supports "argument files" or "@-files". Argument files are files that themselves contain arguments to the command. When picocli encounters an argument beginning with the character `@', it expands the contents of that file into the argument list.

Secondly, this release adds support for multi-value boolean flags. A common use case where this is useful is to let users control the level of output verbosity by specifying more `-v` flags on the command line. For example, `-v` could give high-level output, `-vv` could show more detailed output, and `-vvv` could show debug-level information.

Finally, thanks to [aadrian](https://github.com/aadrian) and [RobertZenz](https://github.com/RobertZenz), an `examples` subproject containing running examples has been added. (Your contributions are welcome!)

This is the sixteenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.1.0-toc"></a> Table of Contents

* [New and noteworthy](#2.1.0-new)
* [Promoted features](#2.1.0-promoted)
* [Fixed issues](#2.1.0-fixes)
* [Deprecations](#2.1.0-deprecated)
* [Potential breaking changes](#2.1.0-breaking-changes)

## <a name="2.1.0-new"></a> New and noteworthy

### Argument Files (`@`-files)
An argument file can include options and positional parameters in any combination. The arguments within a file can be space-separated or newline-separated. If an argument contains embedded whitespace, put the whole argument in double or single quotes (`"-f=My Files\Stuff.java"`).

Lines starting with `#` are comments and are ignored. The file may itself contain additional @-file arguments; any such arguments will be processed recursively.

Multiple @-files may be specified on the command line.

For example, suppose a file with arguments exists at `/home/foo/args`, with these contents:

```text
# This line is a comment and is ignored.
ABC -option=123
'X Y Z'
```

A command may be invoked with the @file argument, like this:
```bash
java MyCommand @/home/foo/args
```

The above will be expanded to the contents of the file:
```bash
java MyCommand ABC -option=123 "X Y Z"
```

This feature is similar to the 'Command Line Argument File' processing supported by gcc, javadoc and javac.
The documentation for these tools shows further examples.

### Repeated Boolean Flags

Multi-valued boolean options are now supported. For example:
```jshelllanguage
@Option(names = "-v", description = { "Specify multiple -v options to increase verbosity.",
                                      "For example, `-v -v -v` or `-vvv`"})
boolean[] verbosity;
```

Users may specify multiple boolean flag options without parameters. For example:
```bash
<command> -v -v -v -vvv
```
The above example results in six `true` values being added to the `verbosity` array.


## <a name="2.1.0-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.1.0-fixes"></a> Fixed issues

- [#126] New feature: Support expanding argument files, also called `@-files`.
- [#241] New feature (enhancing [#126]): Recursively process nested @-files; allow multiple arguments per line, allow quoted arguments with embedded whitespace.
- [#217] New feature: Support repeated boolean flag options captured in multi-valued fields.
- [#223] New feature: Added `examples` subproject containing running examples.  Thanks to [aadrian](https://github.com/aadrian) and [RobertZenz](https://github.com/RobertZenz).
- [#68]  Enhancement: Reject private final primitive fields annotated with @Option or @Parameters: because compile-time constants are inlined, updates by picocli to such fields would not be visible to the application.
- [#239] Enhancement: Improve error message when Exception thrown from Runnable/Callable.
- [#240] Bugfix: RunAll handler should return empty list, not null, when help is requested.
- [#244] Bugfix: the parser only considered `help` options instead of any of `help`, `usageHelp` and `versionHelp` to determine if missing required options can be ignored when encountering a subcommand. Thanks to [mkavanagh](https://github.com/mkavanagh).

## <a name="2.1.0-deprecated"></a> Deprecations

The `Range::defaultArity(Class)` method is now deprecated in favour of the `Range::defaultArity(Field)` method introduced in v2.0.

## <a name="2.1.0-breaking-changes"></a> Potential breaking changes

Private final fields that are either `String` or primitive types can no longer be annotated with `@Option` or `@Parameters`.
Picocli will throw an `InitializationException` when it detects such fields,
because compile-time constants are inlined, and updates by picocli to such fields would not be visible to the application.



# <a name="2.0.3"></a> Picocli 2.0.3

The picocli community is pleased to announce picocli 2.0.3.

This is a bugfix release that fixes a parser bug where the first argument following a clustered options was treated as a positional parameter, and removes the erroneous runtime dependency on `groovy-lang` that slipped in with the 2.0 release.

This release also includes a minor enhancement to support embedded newlines in usage help sections like header or descriptions. This allows Groovy applications to use [triple-quoted](http://groovy-lang.org/syntax.html#_triple_double_quoted_string) and [dollar slashy](http://groovy-lang.org/syntax.html#_dollar_slashy_string) multi-line strings in usage help sections.

This is the fifteenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.0.3-toc"></a> Table of Contents

* [New and noteworthy](#2.0.3-new)
* [Promoted features](#2.0.3-promoted)
* [Fixed issues](#2.0.3-fixes)
* [Deprecations](#2.0.3-deprecated)
* [Potential breaking changes](#2.0.3-breaking-changes)

## <a name="2.0.3-new"></a> New and noteworthy

This is a bugfix release and does not include any new features.

## <a name="2.0.3-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.0.3-fixes"></a> Fixed issues
- [#230] Enhancement: Support embedded newlines in usage help sections like header or descriptions. Thanks to [ddimtirov](https://github.com/ddimtirov).
- [#233] Bugfix: Parser bug: first argument following clustered options is treated as a positional parameter. Thanks to [mgrossmann](https://github.com/mgrossmann).
- [#232] Bugfix: Remove required runtime dependency on `groovy-lang`. Thanks to [aadrian](https://github.com/aadrian).

## <a name="2.0.3-deprecated"></a> Deprecations

This release does not deprecate any features.

## <a name="2.0.3-breaking-changes"></a> Potential breaking changes

This release does not include any breaking features.


# <a name="2.0.2"></a> Picocli 2.0.2

The picocli community is pleased to announce picocli 2.0.2.

This is a bugfix release that prevents an EmptyStackException from being thrown when the command line
ends in a cluster of boolean options, and furthermore fixes two scripting-related minor issues.

This is the fourteenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.0.2-toc"></a> Table of Contents

* [New and noteworthy](#2.0.2-new)
* [Promoted features](#2.0.2-promoted)
* [Fixed issues](#2.0.2-fixes)
* [Deprecations](#2.0.2-deprecated)
* [Potential breaking changes](#2.0.2-breaking-changes)

## <a name="2.0.2-new"></a> New and noteworthy

This is a bugfix release and does not include any new features.

## <a name="2.0.2-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.0.2-fixes"></a> Fixed issues

- [#226] Bugfix: EmptyStackException when command line ends in a cluster of boolean options. Thanks to [RobertZenz](https://github.com/RobertZenz).
- [#222] Bugfix: Register default converter for Object fields for better scripting support.
- [#219] Bugfix: Command line system property -Dpicocli.trace (without value) throws exception when used with Groovy.
- [#220] Enhancement: Improve tracing for positional parameters (provide detail on current position).
- [#221] Enhancement: Add documentation for Grapes bug workaround on Groovy versions before 2.4.7.

## <a name="2.0.2-deprecated"></a> Deprecations

This release does not deprecate any features.

## <a name="2.0.2-breaking-changes"></a> Potential breaking changes

This release does not include any breaking features.


# <a name="2.0.1"></a> Picocli 2.0.1

The picocli community is pleased to announce picocli 2.0.1.

This is a bugfix release that removes a dependency on Java 1.7 which was accidentally included.

This is the thirteenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.0.1-toc"></a> Table of Contents

* [New and noteworthy](#2.0.1-new)
* [Promoted features](#2.0.1-promoted)
* [Fixed issues](#2.0.1-fixes)
* [Deprecations](#2.0.1-deprecated)
* [Potential breaking changes](#2.0.1-breaking-changes)

## <a name="2.0.1-new"></a> New and noteworthy

This is a bugfix release and does not include any new features.

## <a name="2.0.1-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

No features have been promoted in this picocli release.

## <a name="2.0.1-fixes"></a> Fixed issues

- [#214] Removed a dependency on Java 1.7 that was accidentally included. Thanks to [sjsajj](https://github.com/sjsajj).

## <a name="2.0.1-deprecated"></a> Deprecations

This release does not deprecate any features.

## <a name="2.0.1-breaking-changes"></a> Potential breaking changes

This release does not include any breaking features.


# <a name="2.0"></a> Picocli 2.0

The picocli community is pleased to announce picocli 2.0.

First, picocli now provides tight integration for Groovy scripts.
The new `@picocli.groovy.PicocliScript` annotation allows Groovy scripts to use the `@Command` annotation,
and turns a Groovy script into a picocli-based command line application.
When the script is run, `@Field` variables annotated with `@Option` or `@Parameters`
are initialized from the command line arguments before the script body is executed.

Applications with subcommands will also benefit from upgrading to picocli 2.0.
The `CommandLine.run` and `CommandLine.call` methods now work for subcommands,
and if more flexibility is needed, take a look at the new `parseWithHandler` methods.
Error handling for subcommands has been improved in this release with the new `ParseException.getCommandLine` method.

Improved ease of use: Usage help is now printed automatically from the `CommandLine.run` and `CommandLine.call` methods
and from the built-in handlers used with the `parseWithHandler` method.

The parser has been improved and users can now mix positional arguments with options on the command line.
Previously, positional parameter had to follow the options.
**Important notice:** To make this feature possible, the default parsing behaviour of multi-value options and parameters changed to be non-greedy by default.

Finally, this release includes many [bug fixes](#2.0-fixes).

This is the twelfth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="2.0-toc"></a> Table of Contents

* [New and noteworthy](#2.0-new)
    * [Groovy Script Support](#2.0-groovy-script)
    * [Better Subcommand Support](#2.0-subcommands)
    * [Easier To Use](#2.0-ease-of-use)
    * [Parser Improvements](#2.0-parser-improvements)
    * [Usage Help Format Improvements](#2.0-help-improvements)
* [Promoted features](#2.0-promoted)
* [Fixed issues](#2.0-fixes)
* [Deprecations](#2.0-deprecated)
* [Potential breaking changes](#2.0-breaking-changes)



## <a name="2.0-new"></a> New and noteworthy

### <a name="2.0-groovy-script"></a> Groovy Script Support
Picocli 2.0 introduces special support for Groovy scripts.

Scripts annotated with `@picocli.groovy.PicocliScript` are automatically transformed to use
`picocli.groovy.PicocliBaseScript` as their base class and can also use the `@Command` annotation to
customize parts of the usage message like command name, description, headers, footers etc.

Before the script body is executed, the `PicocliBaseScript` base class parses the command line and initializes
`@Field` variables annotated with `@Option` or `@Parameters`.
The script body is executed if the user input was valid and did not request usage help or version information.

```groovy
// exampleScript.groovy
@Grab('info.picocli:picocli:2.0.0')
@Command(name = "myCommand", description = "does something special")
@PicocliScript
import picocli.groovy.PicocliScript
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import groovy.transform.Field

@Option(names = "-x", description = "number of repetitions")
@Field int count;

@Option(names = ["-h", "--help"], usageHelp = true, description = "print this help message and exit")
@Field boolean helpRequested;

//if (helpRequested) { // not necessary: PicocliBaseScript takes care of this
//    CommandLine.usage(this, System.err); return 0;
//}
count.times {
   println "hi"
}
// the CommandLine that parsed the args is available as a property
assert this.commandLine.commandName == "myCommand"
```

### <a name="2.0-subcommands"></a> Better Subcommand Support
New methods `CommandLine::parseWithHandler` were added. These methods intend to offer the same ease of use as
the `run` and `call` methods, but with more flexibility and better support for nested subcommands.

The `CommandLine::call` and `CommandLine::run` now support subcommands and will execute the **last** subcommand
specified by the user. (Previously subcommands were ignored and only the top-level command was executed.)

From this release, picocli exceptions provide a `getCommandLine` method
that returns the command or subcommand where parsing or execution failed.
Previously, if the user provided invalid input for applications with subcommands,
it was difficult to pinpoint exactly which subcommand failed to parse the input.

### <a name="2.0-ease-of-use"></a> Easier To Use
The convenience methods will automatically print usage help and version information
when the user specifies options annotated with the `versionHelp` or `usageHelp` attributes.

Methods that automatically print help:

* CommandLine::call
* CommandLine::run
* CommandLine::parseWithHandler (with the built-in Run…​ handlers)
* CommandLine::parseWithHandlers (with the built-in Run…​ handlers)

Methods that do not automatically print help:

* CommandLine::parse
* CommandLine::populateCommand

Also notable: Collection and Map fields no longer require the `type` annotation attribute:
picocli now infers the conversion target type from the generic type parameters where possible.

### <a name="2.0-parser-improvements"></a> Parser Improvements
Positional parameters can now be mixed with options on the command line. Previously, positional parameter had to follow the options.

To make this feature possible, the default parsing behaviour of multi-value options and parameters changed to be non-greedy by default.

### <a name="2.0-help-improvements"></a> Usage Help Format Improvements
This release contains various bugfixes related to improving the usage help format for multi-value options and collections.
For example, for Maps that don't have a `paramLabel`, picocli now shows key type and value type instead of the internal Java field name.

## <a name="2.0-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility.

The following are the features that have been promoted in this picocli release.

* Autocomplete - as of picocli 2.0, autocompletion is no longer beta.

## <a name="2.0-fixes"></a> Fixed issues

- [#207] API Change: Provide ability to find which subcommand threw a ParameterException API enhancement. Thanks to [velit](https://github.com/velit) and [AshwinJay](https://github.com/AshwinJay).
- [#179] API Change to remove full JRE dependency and require only Compact Profile. Replace use of `java.awt.Point` with `picocli.CommandLine.Help.TextTable.Cell`. Thanks to [webfolderio](https://github.com/webfolderio).
- [#205] API Change: `CommandLine::getCommand` now returns a generic type instead of Object so client code can avoid type casting.
- [#196] API Change: `Option::type()` and `Parameters::type()` now return empty array by default (was `{String.class}`).
- [#210] API Change: Deprecated the option `help` attribute in favour of the `usageHelp` and `versionHelp` attributes.
- [#115] New feature: Added new `CommandLine::parseWithHandler` convenience methods for commands with subcommands.
- [#130] New feature: Options and positional parameters can now be mixed on the command line.
- [#196] New feature: Infer type from collections and maps when `type` annotation not specified. Thanks to [ddimtirov](https://github.com/ddimtirov) for the suggestion.
- [#197] New feature: Use `type` attribute to determine conversion target type instead of field type. This allows fields to be declared as interfaces or abstract types (or arrays/collections/maps of these) and via the `type` attribute picocli will be able to convert String arguments to concrete implementation objects.
- [#187] New feature: Added methods to programmatically get and set the command name.
- [#192] Bugfix: Default arity should be 1, not *, for array and collection options. Thanks to [RobertZenz](https://github.com/RobertZenz).
- [#193] Bugfix: Splitting an argument should not cause max arity to be exceeded.
- [#191] Bugfix: Arity should not limit the total number of values put in an array or collection. Thanks to [RobertZenz](https://github.com/RobertZenz).
- [#186] Bugfix: Confusing usage message for collection options. Thanks to [AlexFalappa](https://github.com/AlexFalappa).
- [#181] Bugfix: Incorrect help message was displayed for short options with paramLabel when arity > 1.
- [#184] Bugfix/Enhancement: Programmatically setting the separator is now reflected in the usage help message. Thanks to [defnull](https://github.com/defnull).
- [#200] Bugfix: Prevent NPE when command name is set to empty string or spaces. Thanks to [jansohn](https://github.com/jansohn).
- [#203] Bugfix: Empty string parameter parsed incorrectly. Thanks to [AshwinJay](https://github.com/AshwinJay).
- [#194] Enhancement: Usage help should show split regex for option/parameters.
- [#198] Enhancement: Usage help parameter list details should indicate arity for positional parameters.
- [#195] Enhancement: Usage help should show Map types if paramLabel not specified.
- [#183] Enhancement: Add examples to user manual for using picocli in other JVM languages. Thanks to [binkley](https://github.com/binkley) for pointing out that Kotlin may support array literals in annotations from 1.2.
- [#185] Enhancement: Exception message text for missing options should not use field names but be more descriptive and consistent with usage help. Thanks to [AlexFalappa](https://github.com/AlexFalappa).
- [#201] Enhancement: Usage help should not show `null` default values. Thanks to [jansohn](https://github.com/jansohn).
- [#202] Enhancement: Java 9: add `Automatic-Module-Name: info.picocli` to MANIFEST.MF to make picocli play nice in the Java 9 module system.
- [#204] Enhancement: instantiate `LinkedHashSet` instead of `HashSet` for `Set` fields to preserve input ordering.
- [#208] Enhancement: Remove pom.xml, which was not being maintained. Picocli can only be built with gradle going forward.
- [#212] Enhancement: Improve javadoc for picocli.AutoComplete.

## <a name="2.0-deprecated"></a> Deprecations
The `help` attribute for options is now deprecated. Please change to use `usageHelp` and `versionHelp` attributes instead.
From picocli v2.0, the convenience methods will automatically print usage help and version information
when requested with the `versionHelp` and `usageHelp` option attributes (but not for the `help` attribute).


## <a name="2.0-breaking-changes"></a> Potential breaking changes

This release has a number of incompatible changes:
* Multi-value options (array, list and map fields) are **not greedy by default** any more.
* **Arity is not max values**: end users may specify multi-value options (array, list and map fields) an unlimited number of times.
* A single argument that is split into parts with a regex now **counts as a single argument** (so `arity="1"` won't prevent all parts from being added to the field)
* API change: replaced `java.awt.Point` with custom `Cell` class as return value type for public method `Help.TextTable.putValue()`.
* API change: `@Option.type()` and `@Parameters.type()` now return an empty array by default (was `{String.class}`).
* API change: `ParameterException` and all subclasses now require a `CommandLine` object indicating the command or subcommand that the user provided invalid input for.
* The `CommandLine::call` and `CommandLine::run` now support subcommands and will execute the **last** subcommand specified by the user. Previously subcommands were ignored and the top-level command was executed unconditionally.

I am not happy about the disruption these changes may cause, but I felt they were needed for three reasons:
the old picocli v1.0 behaviour caused ambiguity in common use cases,
was inconsistent with most Unix tools,
and prevented supporting mixing options with positional arguments on the command line.

To illustrate the new non-greedy behaviour, consider this example program:
```java
class MixDemo {
  @Option(names="-o") List<String> options;
  @Parameters         List<String> positionalParams;

  public void run() {
    System.out.println("positional: " + positionalParams);
    System.out.println("options   : " + options);
  }

  public static void main(String[] args) {
    CommandLine.run(new MixDemo(), System.err, args);
  }
}
```
We run this program as below, where the option is followed by multiple values:

```bash
$ java MixDemo -o 1 2 3
```

Previously, the arguments following `-o` would all end up in the `options` list. Running the above command with picocli 1.0 would print out the following:

```bash
# (Previously, in picocli-1.0.1)
$ java MixDemo -o 1 2 3

positional: null
options   : [1, 2, 3]
```

From picocli 2.0, only the first argument following `-o` is added to the `options` list, the remainder is parsed as positional parameters:

```bash
# (Currently, in picocli-2.0)
$ java MixDemo -o 1 2 3

positional: [2, 3]
options   : [1]
```

To put multiple values in the options list in picocli 2.0, users can specify the `-o` option multiple times:
```bash
$ java MixDemo -o 1 -o 2 -o 3

positional: null
options   : [1, 2, 3]
```

Alternatively, application authors can make a multi-value option greedy in picocli v2.0 by explicitly setting a variable arity:
```java
class Args {
    @Option(names = "-o", arity = "1..*") List<String> options;
}
```
(... "greedy" means consume until the next option, so not necessarily all remaining command line arguments.)



# <a name="1.0.1"></a> 1.0.1 - Bugfix release.

## <a name="1.0.1-summary"></a> Summary: zsh autocompletion bugfix

This is the eleventh public release.
Picocli follows [semantic versioning](http://semver.org/).

- [#178] Fixed autocompletion bug for subcommands in zsh. Autocomplete on zsh would show only the global command options even when a subcommand was specified. Autocompletion now works for nested subcommands.

# <a name="1.0.0"></a> 1.0.0 - Bugfix and enhancements release.

## <a name="1.0.0-summary"></a> Summary

New features: command line autocompletion, `-Dkey=value`-like Map options and parser tracing.

Non-breaking changes to support Callable commands, Map options and format specifiers in version help.

This is the tenth public release.
Picocli follows [semantic versioning](http://semver.org/).

## <a name="1.0.0-fixes"></a> Fixed issues

* [#121] New feature: command line autocompletion. Picocli can generate bash and zsh completion scripts that allow the shell to generate potential completion matches based on the `@Option` and `@Command` annotations in your application. After this script is installed, the shell will show the options and subcommands available in your java command line application, and in some cases show possible option values.
* [#67] New feature: Map options like `-Dkey1=val1 -Dkey2=val2`. Both key and value can be strongly typed (not just Strings).
* [#158] New feature: parser TRACING for easy troubleshooting. The trace level can be controlled with a system property.
* [#170] New feature: added `call` convenience method similar to `run`. Applications whose main business logic may throw an exception or returns a result can now implement `Callable` and reduce some boilerplate code.
* [#149] Parser now throws UnmatchedArgumentException for args that resemble options but are not, instead of treating like them positional parameters. Thanks to [giaco777](https://github.com/giaco777).
* [#172] Parser now throws MaxValuesforFieldExceededException when multi-valued option or parameters max arity exceeded
* [#173] Parser now throws UnmatchedArgumentException when not all positional parameters are assigned to a field
* [#171] WARN when option overwritten with different value (when isOverwrittenOptionsAllowed=true); WARN for unmatched args (when isUnmatchedArgumentsAllowed=true). Thanks to [ddimtirov](https://github.com/ddimtirov).
* [#164] API change: Support format patterns in version string and printVersionHelp
* [#167] API change: Change `type` attribute from `Class` to `Class[]`. This was needed for Map options support.
* [#168] API change: `CommandLine::setSeparator` method now returns this CommandLine (was void), allowing for chained method calls.
* [#156] Added example to user manual to clarify main command common usage. Thanks to [nagkumar](https://github.com/nagkumar).
* [#166] Fixed bug where adjacent markup sections resulted in incorrect ANSI escape sequences
* [#174] Fixed bug where under some circumstances, unmatched parameters were added to UnmatchedParameters list twice

# <a name="0.9.8"></a> 0.9.8 - Bugfix and enhancements release for public review. API may change.

## <a name="0.9.8-summary"></a> Summary

Non-breaking changes to add better help support and better subcommand support.

## <a name="0.9.8-fixes"></a> Fixed issues

* [#162] Added new Version Help section to user manual; added `version` attribute on `@Command`; added `CommandLine::printVersionHelp` convenience method to print version information from this annotation to the console
* [#145] Added `usageHelp` and `versionHelp` attributes on `@Option`; added `CommandLine::isUsageHelpRequested` and `CommandLine::isVersionHelpRequested` to allow external components to detect whether usage help or version information was requested (without inspecting the annotated domain object). Thanks to [kakawait](https://github.com/kakawait).
* [#160] Added `@since` version in javadoc for recent API changes.
* [#157] API change: added `CommandLine::getParent` method to get the parent command of a subcommand. Thanks to [nagkumar](https://github.com/nagkumar).
* [#152] Added support for registering subcommands declaratively with the `@Command(subcommands#{...})` annotation. Thanks to [nagkumar](https://github.com/nagkumar).
* [#146] Show underlying error when type conversion fails
* [#147] Toggle boolean flags instead of setting to `true`
* [#148] Long string in default value no longer causes infinite loop when printing usage. Thanks to [smartboyathome](https://github.com/smartboyathome).
* [#142] First line of long synopsis no longer overshoots 80-character usage help width. Thanks to [waacc-gh](https://github.com/waacc-gh).

# <a name="0.9.7"></a> 0.9.7 - Bugfix and enhancements release for public review. API may change.

## <a name="0.9.7-summary"></a> Summary

Version 0.9.7 has some breaking API changes.

**Better Groovy support**

It was [pointed out](https://github.com/remkop/picocli/issues/135) that Groovy had trouble distinguishing between
the static `parse(Object, String...)` method and the instance method `parse(String...)`.

To address this, the static `parse(Object, String...)` method has been renamed
to `populateCommand(Object, String...)` in  version 0.9.7.

**Nested subcommands**

* Version 0.9.7 adds support for [nested sub-subcommands](https://github.com/remkop/picocli/issues/127)
* `CommandLine::parse` now returns `List<CommandLine>` (was `List<Object>`)
* `CommandLine::getCommands` now returns `Map<String, CommandLine>` (was `Map<String, Object>`)
* renamed method `CommandLine::addCommand` to `addSubcommand`
* renamed method `CommandLine::getCommands` to `getSubcommands`

**Miscellaneous**

Renamed class `Arity` to `Range` since it is not just used for @Option and @Parameters `arity` but also for `index` in positional @Parameters.

## <a name="0.9.7-fixes"></a> Fixed issues

* [#127] Added support for nested sub-subcommands
* [#135] API change: renamed static convenience method `CommandLine::parse` to `populateCommand`
* [#134] API change: `CommandLine::parse` now returns `List<CommandLine>` (was `List<Object>`)
* [#133] API change: `CommandLine::getCommands` now returns `Map<String, CommandLine>` (was `Map<String, Object>`)
* [#133] API change: Added method `CommandLine::getCommand`
* [#136] API change: renamed method `CommandLine::addCommand` to `addSubcommand`;
* [#136] API change: renamed method `CommandLine::getCommands` to `getSubcommands`
* [#131] API change: Renamed class `Arity` to `Range`
* [#137] Improve validation: disallow index gap in @Parameters annotations
* [#132] Improve validation: parsing should fail when unmatched arguments remain
* [#138] Improve validation: disallow option overwriting by default
* [#129] Make "allow option overwriting" configurable
* [#140] Make "allow unmatched arguments" configurable
* [#139] Improve validation: CommandLine must be constructed with a command that has at least one of @Command, @Option or @Parameters annotation
* [#141] Bugfix: prevent NullPointerException when sorting required options/parameters

# <a name="0.9.6"></a> 0.9.6 - Bugfix release for public review. API may change.

* [#128] Fix unexpected MissingParameterException when a help-option is supplied (bug)

# <a name="0.9.5"></a> 0.9.5 - Bugfix and enhancements release for public review. API may change.

* [#122] API change: remove field CommandLine.ansi (enhancement)
* [#123] API change: make public Arity fields final (enhancement)
* [#124] API change: make Help fields optionFields and positionalParameterFields final and unmodifiable (enhancement)
* [#118] BumpVersion gradle task scrambles chars in manual (bug)
* [#119] Add gradle task to publish to local folder (enhancement)

# <a name="0.9.4"></a> 0.9.4 - Bugfix release for public review. API may change.

* [#114] Replace ISO-8613-3 "true colors" with more widely supported 256-color palette (enhancement)
* [#113] Fix javadoc warnings (doc enhancement)
* [#117] The build should work for anyone checking out the project (bug)
* [#112] Improve (shorten) user manual (doc enhancement)
* [#105] Automate publishing to JCentral & Maven Central

# <a name="0.9.3"></a> 0.9.3 - Bugfix release for public review. API may change.

* [#90] Automate release
* [#111] Improve picocli.Demo (enhancement)
* [#110] Fix javadoc for `parse(String...)` return value (doc enhancement)
* [#108] Improve user manual (doc enhancement)
* [#109] `run` convenience method should accept PrintStream (enhancement)

# <a name="0.9.2"></a> 0.9.2 - Bugfix release for public review. API may change.

* [#106] MissingParameterException not thrown for missing mandatory @Parameters when options are specified
* [#104] Investigate why colors don't show by default on Cygwin

# <a name="0.9.1"></a> 0.9.1 - Bugfix release for public review. API may change.

* [#103] Replace javadoc occurrences of ASCII with ANSI.  (doc bug)
* [#102] Move ColorScheme inside Ansi class  (enhancement question wontfix)
* [#101] Cosmetics: indent `Default: <value>` by 2 spaces  (enhancement)
* [#100] Improve error message for DuplicateOptionAnnotationsException  (enhancement)
* [#99] MissingRequiredParams error shows optional indexed Parameters  (bug)
* [#98] MissingRequiredParams error shows indexed Parameters in wrong order when not declared in index order  (bug)
* [#97] Fix compiler warnings  (bug)
* [#96] Synopsis shows indexed Parameters in wrong order when subclassing for reuse (bug)
* [#95] EmptyStackException when no args are passed to object annotated with Parameters (bug)
* [#94] heading fields are not inherited when subclassing for reuse  (bug)
* [#93] Only option fields are set accessible, not parameters fields  (bug)
* [#91] Syntax highlighting in manual source blocks  (doc enhancement)

# <a name="0.9.0"></a> 0.9.0 (was 0.4.0) - User Manual and API Changes. Initial public release.

* [#89] Improve error message for missing required options and parameters  (enhancement)
* [#88] Code cleanup  (enhancement)
* [#87] Add `CommandLine.usage` methods with a ColorScheme parameter  (enhancement)
* [#86] Work around issue on Windows (Jansi?) where style OFF has no effect  (bug)
* [#85] Javadoc for Ansi classes  (doc)
* [#84] System property to let end users set color scheme  (enhancement)
* [#81] Improve README  (doc enhancement)
* [#80] Support customizable Ansi color scheme  (enhancement)
* [#79] Approximate `istty()` by checking `System.console() != null`  (enhancement)
* [#78] Add method CommandLine.setUsageWidth(int)  (enhancement wontfix)
* [#77] Replace PicoCLI in javadoc with picocli  (doc enhancement)
* [#76] @Parameters javadoc is out of date  (bug doc)
* [#75] The default value for the `showDefaultValues` attribute should be `false`  (bug)
* [#74] rename attribute `valueLabel` to `paramLabel`  (enhancement)
* [#73] Remove @Parameters synopsis attribute  enhancement)
* [#72] numeric parameter conversion should parse as decimal  (bug enhancement)
* [#71] Allow multiple values for an option -pA,B,C or -q="A B C"  (enhancement)
* [#66] Support ansi coloring  (doc enhancement)
* [#65] Consider removing the `required` Option attribute  (enhancement question wontfix)
* [#64] Test that boolean options with arity=1 throw MissingParameterException when no value exists (not ParameterException)  (bug QA)
* [#35] Allow users to express arity as a range: 0..* or 1..3 (remove "varargs" attribute)  (enhancement)
* [#30] Test & update manual for exceptions thrown from custom type converters  (doc QA)
* [#26] Ergonomic API - convenience method to parse & run an app  (duplicate enhancement)
* [#12] Create comparison feature table with prior art  (doc)
* [#11] Write user manual  (doc in-progress)
* [#6] Array field values should be preserved (like Collections) and new values appended  (enhancement)
* [#4] Should @Option and @Parameters have listConverter attribute instead of elementType?  (enhancement question wontfix)


# <a name="0.3.0"></a> 0.3.0 - Customizable Usage Help

* [#69] Improve TextTable API  (enhancement question)
* [#63] Unify @Option and @Parameters annotations  (enhancement wontfix)
* [#59] Support declarative API for customizing usage help message  (enhancement wontfix)
* [#58] Add unit tests for ShortestFirst comparator  (QA)
* [#57] Consider using @Usage separator for parsing as well as for usage help  (enhancement)
* [#56] Add unit tests for customizable option parameter name and positional parameter name  (QA)
* [#55] Add unit tests for detailed Usage line  (QA)
* [#54] Add unit tests for DefaultLayout  (QA)
* [#53] Add unit tests for DefaultParameterRenderer  (QA)
* [#52] Add unit tests for DefaultOptionRenderer  (QA)
* [#51] Add unit tests for MinimalOptionRenderer  (QA)
* [#50] Add unit tests for Arity  (QA)
* [#49] Detailed usage header should cluster boolean options  (enhancement)
* [#48] Show positional parameters details in TextTable similar to option details  (enhancement)
* [#47] Reduce API surface for usage Help  (enhancement)
* [#44] Support detailed Usage line instead of generic Usage \<main> \[option] [parameters]  (enhancement)
* [#43] Generated help message should show parameter default value (except for boolean fields)  (enhancement)
* [#42] Show option parameter in generated help (use field name or field type?)  (enhancement)
* [#41] Required options should be visually distinct from optional options in usage help details  (enhancement)
* [#40] Test SortByShortestOptionName  (QA)
* [#39] Test that first declared option is selected by ShortestFirst comparator if both equally short  (QA)
* [#38] Test DefaultRenderer chooses shortest option name in left-most field  (QA)
* [#37] Consider returning a list of Points from TextTable::putValue  (enhancement wontfix)
* [#36] javadoc ILayout, IRenderer, DefaultLayout, DefaultRenderer  (doc)
* [#34] Usage should not show options if no options exist  (enhancement)
* [#32] Support customizable user help format.  (enhancement)
* [#31] Add test for recognizing clustered short option when parsing varargs array  (bug QA)
* [#27] Support git-like commands  (enhancement)
* [#8] Add positional @Parameter annotation  (enhancement)
* [#7] Implement online usage help  (enhancement)
* [#5] Rename `description` attribute to `helpText` or `usage`  (enhancement wontfix)


# <a name="0.2.0"></a> 0.2.0 - Vararg Support

* [#25] Use Integer.decode(String) rather than Integer.parseInt  (enhancement)
* [#23] @Option should not greedily consume args if varargs=false  (bug)


# <a name="0.1.0"></a> 0.1.0 - Basic Option and Parameter Parsing

* [#20] add test where option name is "-p", give it input "-pa-p"  (QA)
* [#19] Improve error message for type conversion: include field name (and option name?)  (enhancement)
* [#18] test superclass bean and child class bean where child class field shadows super class and have different annotation Option name  (QA)
* [#17] Test superclass bean and child class bean where child class field shadows super class and have same annotation Option name  (invalid QA)
* [#16] Test arity > 1 for single-value fields (int, File, ...)  (QA)
* [#13] Test for enum type conversation  (QA)
* [#3] Interpreter should set helpRequested=false before parse()  (bug)
* [#2] Test that separators other than '=' can be configured  (QA)
* [#1] Test with other option prefixes than '-'  (QA)
