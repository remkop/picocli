# picocli Release Notes

## Unreleased

This is the twelfth public release.
Picocli follows [semantic versioning](http://semver.org/).

## Why the major version increase?

This release has a number of incompatible changes:
* Multi-value options (array, list and map fields) are **not greedy by default** any more.
* **Arity is not max values**: end users may specify multi-value options (array, list and map fields) an unlimited number of times.
* A single argument that is split into parts with a regex now **counts as a single argument** (so `arity="1"` won't prevent all parts from being added to the field)

I am not happy about the disruption this may cause, but I felt these changes were needed for three reasons:
the old picocli v1.0 behaviour caused ambiguity in common use cases,
was inconsistent with most Unix tools, 
and prevented supporting mixing options with positional arguments on the command line.

To illustrate the new non-greedy behaviour, consider this common use case:
```
class Args {
    @Option(names="-o") List<String> options;
    @Parameters         List<String> positionalParams;
}
```
With picocli v1.0, arguments like the below would all end up in the `options` list.
From picocli v2.0, only the first argument following `-o` is added to the `options` list, the remainder is parsed as positional parameters.

```
Args args = CommandLine.populateCommand(new Args(), "-o", "1", "2", "3");

// in picocli v1.0:
assert args.options.size() == 3
assert args.positionalParams.isEmpty()

// in picocli v2.0:
assert args.options.size() == 1
assert args.positionalParams.size() == 2
```
To put multiple values in the options list in picocli v2.0, users need to specify the `-o` option multiple times:
```
<command> -o 1 -o 2 -o 3
```
Alternatively, application authors can make a multi-value option greedy in picocli v2.0 by explicitly setting a variable arity:
```
class Args {
    @Option(names = "-o", arity = "1..*") List<String> options;
}
```

### Issues fixed

- #192 Default arity should be 1, not *, for array and collection options
- #193 Splitting an argument should not cause max arity to be exceeded
- #191 Arity should not limit the total number of values put in an array or collection
- #195 Usage help should show Map types if paramLabel not specified
- #185 Missing option exception text should not use field names but be more descriptive and consistent with usage help. Thanks to [AlexFalappa](https://github.com/AlexFalappa). 
- #186 Confusing usage message for collection options
- #181 Fixed bug where incorrect help message is displayed for short options with paramLabel when arity > 1
- #184 Improved CommandLine.setSeparator javadoc to clarify that this affects parsing only and link to the `@Command` `separator` annotation attribute.

## 1.0.1 - Bugfix release.

### Summary: zsh autocompletion bugfix

This is the eleventh public release.
Picocli follows [semantic versioning](http://semver.org/).

- #178 Fixed autocompletion bug for subcommands in zsh. Autocomplete on zsh would show only the global command options even when a subcommand was specified. Autocompletion now works for nested subcommands.

## 1.0.0 - Bugfix and enhancements release.

### Summary: command line autocompletion, `-Dkey=value`-like Map options, parser tracing, stricter parsing, bugfixes

This is the tenth public release.
Picocli follows [semantic versioning](http://semver.org/).

* #121 New feature: command line autocompletion. Picocli can generate bash and zsh completion scripts that allow the shell to generate potential completion matches based on the `@Option` and `@Command` annotations in your application. After this script is installed, the shell will show the options and subcommands available in your java command line application, and in some cases show possible option values.
* #67  New feature: Map options like `-Dkey1=val1 -Dkey2=val2`. Both key and value can be strongly typed (not just Strings).
* #158 New feature: parser TRACING for easy troubleshooting. The trace level can be controlled with a system property.
* #170 New feature: added `call` convenience method similar to `run`. Applications whose main business logic may throw an exception or returns a result can now implement `Callable` and reduce some boilerplate code.
* #149 Parser now throws UnmatchedArgumentException for args that resemble options but are not, instead of treating like them positional parameters. Thanks to [giaco777](https://github.com/giaco777).
* #172 Parser now throws MaxValuesforFieldExceededException when multi-valued option or parameters max arity exceeded
* #173 Parser now throws UnmatchedArgumentException when not all positional parameters are assigned to a field
* #171 WARN when option overwritten with different value (when isOverwrittenOptionsAllowed=true); WARN for unmatched args (when isUnmatchedArgumentsAllowed=true). Thanks to [ddimtirov](https://github.com/ddimtirov).
* #164 API change: Support format patterns in version string and printVersionHelp
* #167 API change: Change `type` attribute from `Class` to `Class[]`. This was needed for Map options support.
* #168 API change: `CommandLine::setSeparator` method now returns this CommandLine (was void), allowing for chained method calls.
* #156 Added example to user manual to clarify main command common usage. Thanks to [nagkumar](https://github.com/nagkumar).
* #166 Fixed bug where adjacent markup sections resulted in incorrect ANSI escape sequences
* #174 Fixed bug where under some circumstances, unmatched parameters were added to UnmatchedParameters list twice

## 0.9.8 - Bugfix and enhancements release for public review. API may change.

### Summary: improved version and usage help, improved subcommand support, bugfixes

* #162 Added new Version Help section to user manual; added `version` attribute on `@Command`; added `CommandLine::printVersionHelp` convenience method to print version information from this annotation to the console
* #145 Added `usageHelp` and `versionHelp` attributes on `@Option`; added `CommandLine::isUsageHelpRequested` and `CommandLine::isVersionHelpRequested` to allow external components to detect whether usage help or version information was requested (without inspecting the annotated domain object). Thanks to [kakawait](https://github.com/kakawait).
* #160 Added `@since` version in javadoc for recent API changes.
* #157 API change: added `CommandLine::getParent` method to get the parent command of a subcommand. Thanks to [nagkumar](https://github.com/nagkumar).
* #152 Added support for registering subcommands declaratively with the `@Command(subcommands#{...})` annotation. Thanks to [nagkumar](https://github.com/nagkumar).
* #146 Show underlying error when type conversion fails
* #147 Toggle boolean flags instead of setting to `true`
* #148 Long string in default value no longer causes infinite loop when printing usage. Thanks to [smartboyathome](https://github.com/smartboyathome).
* #142 First line of long synopsis no longer overshoots 80-character usage help width. Thanks to [waacc-gh](https://github.com/waacc-gh).

## 0.9.7 - Bugfix and enhancements release for public review. API may change.

* #127 Added support for nested sub-subcommands
* #135 API change: renamed static convenience method `CommandLine::parse` to `populateCommand`
* #134 API change: `CommandLine::parse` now returns `List<CommandLine>` (was `List<Object>`)
* #133 API change: `CommandLine::getCommands` now returns `Map<String, CommandLine>` (was `Map<String, Object>`)
* #133 API change: Added method `CommandLine::getCommand`
* #136 API change: renamed method `CommandLine::addCommand` to `addSubcommand`;
* #136 API change: renamed method `CommandLine::getCommands` to `getSubcommands`
* #131 API change: Renamed class `Arity` to `Range`
* #137 Improve validation: disallow index gap in @Parameters annotations
* #132 Improve validation: parsing should fail when unmatched arguments remain
* #138 Improve validation: disallow option overwriting by default
* #129 Make "allow option overwriting" configurable
* #140 Make "allow unmatched arguments" configurable
* #139 Improve validation: CommandLine must be constructed with a command that has at least one of @Command, @Option or @Parameters annotation
* #141 Bugfix: prevent NullPointerException when sorting required options/parameters

## 0.9.6 - Bugfix release for public review. API may change.

* #128 Fix unexpected MissingParameterException when a help-option is supplied (bug)

## 0.9.5 - Bugfix and enhancements release for public review. API may change.

* #122 API change: remove field CommandLine.ansi (enhancement)
* #123 API change: make public Arity fields final (enhancement)
* #124 API change: make Help fields optionFields and positionalParameterFields final and unmodifiable (enhancement)
* #118 BumpVersion gradle task scrambles chars in manual (bug)
* #119 Add gradle task to publish to local folder (enhancement)

## 0.9.4 - Bugfix release for public review. API may change.

* #114 Replace ISO-8613-3 "true colors" with more widely supported 256-color palette (enhancement)
* #113 Fix javadoc warnings (doc enhancement)
* #117 The build should work for anyone checking out the project (bug)
* #112 Improve (shorten) user manual (doc enhancement)
* #105 Automate publishing to JCentral & Maven Central

## 0.9.3 - Bugfix release for public review. API may change.

* #90 Automate release
* #111 Improve picocli.Demo (enhancement)
* #110 Fix javadoc for `parse(String...)` return value (doc enhancement)
* #108 Improve user manual (doc enhancement)
* #109 `run` convenience method should accept PrintStream (enhancement)

## 0.9.2 - Bugfix release for public review. API may change.

* #106 MissingParameterException not thrown for missing mandatory @Parameters when options are specified
* #104 Investigate why colors don't show by default on Cygwin

## 0.9.1 - Bugfix release for public review. API may change.

* #103 Replace javadoc occurences of ASCII with ANSI.  (doc bug)
* #102 Move ColorScheme inside Ansi class  (enhancement question wontfix)
* #101 Cosmetics: indent `Default: <value>` by 2 spaces  (enhancement)
* #100 Improve error message for DuplicateOptionAnnotationsException  (enhancement)
* #99 MissingRequiredParams error shows optional indexed Parameters  (bug)
* #98 MissingRequiredParams error shows indexed Parameters in wrong order when not declared in index order  (bug)
* #97 Fix compiler warnings  (bug)
* #96 Synopsis shows indexed Parameters in wrong order when subclassing for reuse (bug)
* #95 EmptyStackException when no args are passed to object annotated with Parameters (bug)
* #94 heading fields are not inherited when subclassing for reuse  (bug)
* #93 Only option fields are set accessible, not parameters fields  (bug)
* #91 Syntax highlighting in manual source blocks  (doc enhancement)

## 0.9.0 (was 0.4.0) - User Manual and API Changes. Initial public release.

* #89 Improve error message for missing required options and parameters  (enhancement)
* #88 Code cleanup  (enhancement)
* #87 Add `CommandLine.usage` methods with a ColorScheme parameter  (enhancement)
* #86 Work around issue on Windows (Jansi?) where style OFF has no effect  (bug)
* #85 Javadoc for Ansi classes  (doc)
* #84 System property to let end users set color scheme  (enhancement)
* #81 Improve README  (doc enhancement)
* #80 Support customizable Ansi color scheme  (enhancement)
* #79 Approximate `istty()` by checking `System.console() != null`  (enhancement)
* #78 Add method CommandLine.setUsageWidth(int)  (enhancement wontfix)
* #77 Replace PicoCLI in javadoc with picocli  (doc enhancement)
* #76 @Parameters javadoc is out of date  (bug doc)
* #75 The default value for the `showDefaultValues` attribute should be `false`  (bug)
* #74 rename attribute `valueLabel` to `paramLabel`  (enhancement)
* #73 Remove @Parameters synopsis attribute  enhancement)
* #72 numeric parameter conversion should parse as decimal  (bug enhancement)
* #71 Allow multiple values for an option -pA,B,C or -q="A B C"  (enhancement)
* #66 Support ansi coloring  (doc enhancement)
* #65 Consider removing the `required` Option attribute  (enhancement question wontfix)
* #64 Test that boolean options with arity=1 throw MissingParameterException when no value exists (not ParameterException)  (bug QA)
* #35 Allow users to express arity as a range: 0..* or 1..3 (remove "varargs" attribute)  (enhancement)
* #30 Test & update manual for exceptions thrown from custom type converters  (doc QA)
* #26 Ergonomic API - convenience method to parse & run an app  (duplicate enhancement)
* #12 Create comparison feature table with prior art  (doc)
* #11 Write user manual  (doc in-progress)
* #6 Array field values should be preserved (like Collections) and new values appended  (enhancement)
* #4 Should @Option and @Parameters have listConverter attribute instead of elementType?  (enhancement question wontfix)


## 0.3.0 - Customizable Usage Help

* #69 Improve TextTable API  (enhancement question)
* #63 Unify @Option and @Parameters annotations  (enhancement wontfix)
* #59 Support declarative API for customizing usage help message  (enhancement wontfix)
* #58 Add unit tests for ShortestFirst comparator  (QA)
* #57 Consider using @Usage separator for parsing as well as for usage help  (enhancement)
* #56 Add unit tests for customizable option parameter name and positional parameter name  (QA)
* #55 Add unit tests for detailed Usage line  (QA)
* #54 Add unit tests for DefaultLayout  (QA)
* #53 Add unit tests for DefaultParameterRenderer  (QA)
* #52 Add unit tests for DefaultOptionRenderer  (QA)
* #51 Add unit tests for MinimalOptionRenderer  (QA)
* #50 Add unit tests for Arity  (QA)
* #49 Detailed usage header should cluster boolean options  (enhancement)
* #48 Show positional parameters details in TextTable similar to option details  (enhancement)
* #47 Reduce API surface for usage Help  (enhancement)
* #44 Support detailed Usage line instead of generic Usage \<main> \[option] [parameters]  (enhancement)
* #43 Generated help message should show parameter default value (except for boolean fields)  (enhancement)
* #42 Show option parameter in generated help (use field name or field type?)  (enhancement)
* #41 Required options should be visually distinct from optional options in usage help details  (enhancement)
* #40 Test SortByShortestOptionName  (QA)
* #39 Test that first declared option is selected by ShortestFirst comparator if both equally short  (QA)
* #38 Test DefaultRenderer chooses shortest option name in left-most field  (QA)
* #37 Consider returning a list of Points from TextTable::putValue  (enhancement wontfix)
* #36 javadoc ILayout, IRenderer, DefaultLayout, DefaultRenderer  (doc)
* #34 Usage should not show options if no options exist  (enhancement)
* #32 Support customizable user help format.  (enhancement)
* #31 Add test for recognizing clustered short option when parsing varargs array  (bug QA)
* #27 Support git-like commands  (enhancement)
* #8 Add positional @Parameter annotation  (enhancement)
* #7 Implement online usage help  (enhancement)
* #5 Rename `description` attribute to `helpText` or `usage`  (enhancement wontfix)


## 0.2.0 - Vararg Support

* #25 Use Integer.decode(String) rather than Integer.parseInt  (enhancement)
* #23 @Option should not greedily consume args if varargs=false  (bug)


## 0.1.0 - Basic Option and Parameter Parsing

* #20 add test where option name is "-p", give it input "-pa-p"  (QA)
* #19 Improve error message for type conversion: include field name (and option name?)  (enhancement)
* #18 test superclass bean and child class bean where child class field shadows super class and have different annotation Option name  (QA)
* #17 Test superclass bean and child class bean where child class field shadows super class and have same annotation Option name  (invalid QA)
* #16 Test arity > 1 for single-value fields (int, File, ...)  (QA)
* #13 Test for enum type conversation  (QA)
* #3 Interpreter should set helpRequested=false before parse()  (bug)
* #2 Test that separators other than '=' can be configured  (QA)
* #1 Test with other option prefixes than '-'  (QA)
