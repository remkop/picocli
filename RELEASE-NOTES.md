# picocli Release Notes

# <a name="2.1.0"></a> (UNRELEASED) Picocli 2.1.0

## <a name="2.1.0-fixes"></a> Fixed issues

- [#220] Improve tracing for positional parameters (provide detail on current position).

# <a name="2.0.1"></a> Picocli 2.0.1

The picocli community is pleased to announce picocli 2.0.1.

This is a bugfix release that removes a dependency on Java 1.7 which was accidentally included.

## <a name="2.0.1-toc"></a> Table of Contents

* [New and noteworthy](#2.0.1-new)
* [Promoted features](#2.0.1-promoted)
* [Fixed issues](#2.0.1-fixes)
* [Deprecations](#2.0.1-deprecated)
* [Potentially breaking changes](#2.0.1-breaking-changes)

## <a name="2.0.1-new"></a> New and noteworthy

This is a bugfix release and does not include any new features.

## <a name="2.0.1-promoted"></a> Promoted features
Promoted features are features that were incubating in previous versions of picocli but are now supported and subject to backwards compatibility. 

No features have been promoted in this picocli release.

## <a name="2.0.1-fixes"></a> Fixed issues

- [#214] Removed a dependency on Java 1.7 that was accidentally included. Thanks to [sjsajj](https://github.com/sjsajj).

## <a name="2.0.1-deprecated"></a> Deprecations

This release does not deprecate any features.

## <a name="2.0.1-breaking-changes"></a> Potentially breaking changes

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
* [Potentially breaking changes](#2.0-breaking-changes)



## <a name="2.0-new"></a> New and noteworthy

### <a name="2.0-groovy-script"></a> Groovy Script Support
Picocli 2.0 introduces special support for Groovy scripts.

Scripts annotated with `@picocli.groovy.PicocliScript` are automatically transformed to use
`picocli.groovy.PicocliBaseScript` as their base class and can also use the `@Command` annotation to
customize parts of the usage message like command name, description, headers, footers etc.

Before the script body is executed, the `PicocliBaseScript` base class parses the command line and initializes
`@Field` variables annotated with `@Option` or `@Parameters`.
The script body is executed if the user input was valid and did not request usage help or version information.

```
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


## <a name="2.0-breaking-changes"></a> Potentially breaking changes

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
(... with the caveat that "greedy" means consume until the next option, not all remaining command line arguments.)


# <a name="1.0.1"></a> 1.0.1 - Bugfix release.

## <a name="1.0.1-summary"></a> Summary: zsh autocompletion bugfix

This is the eleventh public release.
Picocli follows [semantic versioning](http://semver.org/).

- [#178] Fixed autocompletion bug for subcommands in zsh. Autocomplete on zsh would show only the global command options even when a subcommand was specified. Autocompletion now works for nested subcommands.

# <a name="1.0.0"></a> 1.0.0 - Bugfix and enhancements release.

## <a name="1.0.0-summary"></a> Summary: command line autocompletion, `-Dkey=value`-like Map options, parser tracing, stricter parsing, bugfixes

This is the tenth public release.
Picocli follows [semantic versioning](http://semver.org/).

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

## <a name="0.9.8-summary"></a> Summary: improved version and usage help, improved subcommand support, bugfixes

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

* [#103] Replace javadoc occurences of ASCII with ANSI.  (doc bug)
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
