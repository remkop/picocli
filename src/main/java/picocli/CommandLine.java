/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.awt.Point;
import java.io.File;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import static picocli.CommandLine.Help.Column.Overflow.*;

/**
 * <p>
 * CommandLine interpreter that uses reflection to initialize an annotated domain object with values obtained from the
 * command line arguments.
 * </p><p>
 * <h2>Example</h2>
 * </p>
 * <pre>import static picocli.CommandLine.*;
 *
 * &#064;Usage(summary = "Encrypt FILE(s), or standard input, to standard output or to the output file.")
 * public class Encrypt {
 *
 *     &#064;Parameters(type = File.class, description = "Any number of input files")
 *     private List<File> files = new ArrayList<File>();
 *
 *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
 *     private File outputFile;
 *
 *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely list files processed")
 *     private boolean verbose;
 *
 *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, help = true, description = "Display this help and exit")
 *     private boolean help;
 * }
 * </pre>
 * <p>
 * Use {@code CommandLine} to initialize a domain object as follows:
 * </p><pre>
 * public static void main(String... args) {
 *     try {
 *         Encrypt encrypt = CommandLine.parse(new Encrypt(), args);
 *         if (encrypt.help) {
 *             CommandLine.usage(Encrypt.class, System.out);
 *         } else {
 *             runProgram(encrypt);
 *         }
 *     } catch (ParameterException ex) { // command line arguments could not be parsed
 *         System.err.println(ex.getMessage());
 *         CommandLine.usage(Encrypt.class, System.err);
 *     }
 * }
 * </pre><p>
 * Invoke the above program with some command line arguments. The below are all equivalent:
 * </p>
 * <pre>
 * --verbose --out=outfile in1 in2
 * --verbose --out outfile in1 in2
 * -v --out=outfile in1 in2
 * -v -o outfile in1 in2
 * -v -o=outfile in1 in2
 * -vo outfile in1 in2
 * -vo=outfile in1 in2
 * -v -ooutfile in1 in2
 * -vooutfile in1 in2
 * </pre>
 *
 * @see <a href="http://catb.org/~esr/writings/taoup/html/ch10s05.html#id2948149">TAOUP's hints for designing good
 *        command line user interfaces</a>
 * @see <a href="https://www.gnu.org/prep/standards/html_node/Command_002dLine-Interfaces.html#Command_002dLine-Interfaces">GNU
 *        Standards for Command Line Interfaces</a>
 */
public class CommandLine {
    /** This is PicoCLI version {@value}. */
    public static final String VERSION = "0.2.0";

    private final Interpreter interpreter;

    /**
     * Constructs a new {@code CommandLine} interpreter with the specified annotated object.
     * When the {@link #parse(String...)} method is called, fields of the specified object that are annotated
     * with {@code @Option} or {@code @Parameters} will be initialized based on command line arguments.
     * @param annotatedObject the object to initialize from the command line arguments
     */
    public CommandLine(Object annotatedObject) {
        interpreter = new Interpreter(annotatedObject);
    }

    /**
     * <p>
     * Convenience method that initializes the specified annotated object from the specified command line arguments.
     * </p><p>
     * This is equivalent to
     * </p><pre>
     * CommandLine cli = new CommandLine(annotatedObject);
     * cli.parse(args);
     * return annotatedObject;
     * </pre>
     *
     * @param annotatedObject the object to initialize. This object contains fields annotated with
     *          {@code @Option} or {@code @Parameters}.
     * @param args the command line arguments to parse
     * @param <T> the type of the annotated object
     * @return the specified annotated object
     */
    public static <T> T parse(T annotatedObject, String... args) {
        CommandLine cli = new CommandLine(annotatedObject);
        cli.parse(args);
        return annotatedObject;
    }

    /**
     * <p>
     * Initializes the annotated object that this {@code CommandLine} was constructed with, based on
     * the specified command line arguments.
     * </p>
     *
     * @param args the command line arguments to parse
     * @return the annotated object that this {@code CommandLine} was constructed with
     */
    public Object parse(String... args) {
        return interpreter.parse(args);
    }

    /**
     * Prints a usage help message for the specified annotated class to the specified {@code PrintStream}.
     * Delegates construction of the usage help message to the {@link Help} inner class and is equivalent to:
     * <pre>
     * StringBuilder sb = new StringBuilder();
     * new Help(annotatedClass).appendUsage(sb)         // Usage &lt;main class&gt; [OPTIONS] [PARAMETERS]
     *                         .appendSummary(sb)       // from @Usage.summary() - may be omitted
     *                         .appendOptionDetails(sb) // from @Option names and descriptions
     *                         .appendFooter(sb);       // from @Usage.footer() - may be omitted
     * out.print(sb);</pre>
     * <p>Annotate your class with {@link Usage} to control the program name, or whether the "usage" message
     * should be a generic {@code "Usage <main class> [OPTIONS] [PARAMETERS]"} message or a detailed message
     * that shows options and parameters.</p>
     * <p>To customize how option details are displayed, instantiate a {@link Help} object and install a custom
     * option {@linkplain Help.IOptionRenderer renderer} and/or a custom {@linkplain Help.ILayout layout} to control
     * which aspects of an Option or Field are displayed where in the {@link Help.TextTable}.</p>
     * @param annotatedClass the class annotated with {@link Usage}, {@link Option} and {@link Parameters}
     * @param out the {@code PrintStream} to print the usage help message to
     */
    public static void usage(Class<?> annotatedClass, PrintStream out) {
        StringBuilder sb = new StringBuilder();
        new Help(annotatedClass).appendUsageTo(sb)
                                .appendSummaryTo(sb)
                                .appendOptionDetailsTo(sb)
                                .appendParameterDetailsTo(sb)
                                .appendFooterTo(sb);
        out.print(sb);
    }

    /**
     * Registers the specified type converter for the specified class. When initializing fields annotated with
     * {@link Option}, the field's type is used as a lookup key to find the associated type converter, and this
     * type converter converts the original command line argument string value to the correct type.
     * <p>
     * Java 8 lambdas make it easy to register custom type converters:
     * </p>
     * <pre>
     * commandLine.registerConverter(java.nio.file.Path.class, s -> java.nio.file.Paths.get(s));
     * commandLine.registerConverter(java.time.Duration.class, s -> java.time.Duration.parse(s));</pre>
     * <p>
     * Built-in type converters are pre-registered for the following java 1.5 types:
     * </p>
     * <ul>
     *   <li>all primitive types</li>
     *   <li>all primitive wrapper types: Boolean, Byte, Character, Double, Float, Integer, Long, Short</li>
     *   <li>any enum</li>
     *   <li>java.io.File</li>
     *   <li>java.math.BigDecimal</li>
     *   <li>java.math.BigInteger</li>
     *   <li>java.net.InetAddress</li>
     *   <li>java.net.URI</li>
     *   <li>java.net.URL</li>
     *   <li>java.nio.charset.Charset</li>
     *   <li>java.sql.Time</li>
     *   <li>java.util.Date</li>
     *   <li>java.util.UUID</li>
     *   <li>java.util.regex.Pattern</li>
     *   <li>StringBuilder</li>
     *   <li>CharSequence</li>
     *   <li>String</li>
     * </ul>
     *
     * @param cls the target class to convert parameter string values to
     * @param converter the class capable of converting string values to the specified target type
     * @param <K> the target type
     */
    public <K> void registerConverter(Class<K> cls, ITypeConverter<K> converter) {
        interpreter.converterRegistry.put(Assert.notNull(cls, "class"), Assert.notNull(converter, "converter"));
    }

    /** Returns the String that separates option names from option values. {@code '='} by default. */
    public String getSeparator() {
        return interpreter.separator;
    }

    /** Sets the String that separates option names from option values to the specified value. */
    public void setSeparator(String separator) {
        interpreter.separator = Assert.notNull(separator, "separator");
    }
    private static boolean empty(String str) { return str == null || str.trim().length() == 0; }

    /**
     * <p>
     * Annotate fields in your class with {@code @Option} and picoCLI will initialize these fields when matching
     * arguments are specified on the command line.
     * </p><p>
     * For example:
     * </p>
     * <pre>import static picocli.CommandLine.*;
     *
     * public class MyClass {
     *     &#064;Parameters(type = File.class, description = "Any number of input files")
     *     private List<File> files = new ArrayList<File>();
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
     *
     *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely list files processed")
     *     private boolean verbose;
     *
     *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, help = true, description = "Display this help and exit")
     *     private boolean help;
     *
     *     &#064;Option(names = { "-V", "--version"}, help = true, description = "Display version information and exit")
     *     private boolean version;
     * }
     * </pre>
     * <p>
     * A field cannot be annotated with both {@code @Parameters} and {@code @Option} or a
     * {@code ParameterException} is thrown.
     * </p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Option {
        /**
         * One or more option names. At least one option name is required.
         * <p>
         * Different environments have different conventions for naming options, but usually options have a prefix
         * that sets them apart from parameters.
         * PicoCLI supports all of the below styles. The default separator is {@code '='}, but this can be configured.
         * </p><p>
         * <b>*nix</b>
         * </p><p>
         * In Unix and Linux, options have a short (single-character) name, a long name or both.
         * Short options
         * (<a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_02">POSIX
         * style</a> are single-character and are preceded by the {@code '-'} character, e.g., {@code `-v'}.
         * <a href="https://www.gnu.org/software/tar/manual/html_node/Long-Options.html">GNU-style</a> long
         * (or <em>mnemonic</em>) options start with two dashes in a row, e.g., {@code `--file'}.
         * </p><p>PicoCLI supports the POSIX convention that short options can be grouped, with the last option
         * optionally taking a parameter, which may be attached to the option name or separated by a space or
         * a {@code '='} character. The below examples are all equivalent:
         * </p><pre>
         * -xvfFILE
         * -xvf FILE
         * -xvf=FILE
         * -xv --file FILE
         * -xv --file=FILE
         * -x -v --file FILE
         * -x -v --file=FILE
         * </pre><p>
         * <b>DOS</b>
         * </p><p>
         * DOS options mostly have upper case single-character names and start with a single slash {@code '/'} character.
         * Option parameters are separated by a {@code ':'} character. Options cannot be grouped together but
         * must be specified separately. For example:
         * </p><pre>
         * DIR /S /A:D /T:C
         * </pre><p>
         * <b>PowerShell</b>
         * </p><p>
         * Windows PowerShell options generally are a word preceded by a single {@code '-'} character, e.g., {@code `-Help'}.
         * Option parameters are separated by a space or by a {@code ':'} character.
         * </p>
         * @return one or more option names
         */
        String[] names();

        /**
         * Description of this option, used when generating the usage documentation.
         * @return the description of this option
         */
        String[] description() default {};

        /**
         * Indicates whether this option is required. By default this is false.
         * If an option is required, but a user invokes the program without specifying the required option,
         * a {@link MissingParameterException} is thrown from the {@link #parse(String...)} method.
         * @return whether this option is required
         */
        boolean required() default false;

        /**
         * Specifies the minimum number of required parameters and the maximum number of accepted parameters.
         * If an option declares a positive arity, and the user specifies an insufficient number of parameters on the
         * command line, a {@link MissingParameterException} is thrown by the {@link #parse(String...)} method.
         * <p>
         * In many cases picoCLI can deduce the number of required parameters from the field's type.
         * By default, flags (boolean options) have arity zero,
         * and single-valued type fields (String, int, Integer, double, Double, File, Date, etc) have arity one.
         * Generally, fields with types that cannot hold multiple values can omit the {@code arity} attribute.
         * </p><p>
         * Fields used to capture options with arity two or higher should have a type that can hold multiple values,
         * like arrays or Collections. See {@link #type()} for strongly-typed Collection fields.
         * </p><p>
         * For example, if an option has 2 required parameters and any number of optional parameters,
         * specify {@code @Option(names = "-example", arity = "2..*")}.
         * </p>
         * <b>A note on boolean options</b>
         * <p>
         * By default picoCLI does not expect boolean options (also called "flags" or "switches") to have a parameter.
         * You can make a boolean option take a required parameter by annotating your field with {@code arity="1"}.
         * For example: </p>
         * <pre>&#064;Option(names = "-v", arity = "1") boolean verbose;</pre>
         * <p>
         * Because this boolean field is defined with arity 1, the user must specify either {@code <program> -v false}
         * or {@code <program> -v true}
         * on the command line, or a {@link MissingParameterException} is thrown by the {@link #parse(String...)}
         * method.
         * </p><p>
         * To make the boolean parameter possible but optional, define the field with {@code arity = "0..1"}.
         * For example: </p>
         * <pre>&#064;Option(names="-v", arity="0..1") boolean verbose;</pre>
         * <p>This will accept any of the below without throwing an exception:</p>
         * <pre>
         * -v
         * -v true
         * -v false
         * </pre>
         * @return how many arguments this option requires
         */
        String arity() default "";

        /**
         * Specify a {@code paramLabel} for the option parameter to be used in the usage help message. If omitted,
         * picoCLI uses the field name in fish brackets ({@code '<'} and {@code '>'}) by default. Example:
         * <pre>class Example {
         *     &#064;Option(names = {"-o", "--output"}, paramLabel="FILE", description="path of the output file")
         *     private File out;
         *     &#064;Option(names = {"-j", "--jobs"}, arity="0..1", description="Allow N jobs at once; infinite jobs with no arg.")
         *     private int maxJobs = -1;
         * }</pre>
         * <p>By default, the above gives a usage help message like the following:</p><pre>
         * Usage: &lt;main class&gt; [OPTIONS]
         * -o, --output FILE       path of the output file
         * -j, --jobs [&lt;maxJobs&gt;]  Allow N jobs at once; infinite jobs with no arg.
         * </pre>
         * @return name of the option parameter used in the usage help message
         */
        String paramLabel() default "";

        /**
         * <p>
         * Specify a {@code type} if the annotated field is a {@code Collection} that should hold objects other than Strings.
         * </p><p>
         * If the field's type is a {@code Collection}, the generic type parameter of the collection is erased and
         * cannot be determined at runtime. Specify a {@code type} attribute to store values other than String in
         * the Collection. PicoCLI will use the {@link ITypeConverter}
         * that is {@linkplain #registerConverter(Class, ITypeConverter) registered} for that type to convert
         * the raw String values before they are added to the collection.
         * </p><p>
         * When the field's type is an array, the {@code type} attribute is ignored: the values will be converted
         * to the array component type and the array will be replaced with a new instance.
         * </p>
         * @return the type to convert the raw String values to before adding them to the Collection
         */
        Class<?> type() default String.class;

        /**
         * Set {@code hidden=true} if this option should not be included in the usage documentation.
         * @return whether this option should be excluded from the usage message
         */
        boolean hidden() default false;

        /**
         * Set {@code help=true} if this option should disable validation of the remaining arguments:
         * If the {@code help} option is specified, no error message is generated for missing required options.
         * <p>
         * This attribute is useful for special options like help ({@code -h} and {@code --help} on unix,
         * {@code -?} and {@code -Help} on Windows) or version ({@code -V} and {@code --version} on unix,
         * {@code -Version} on Windows).
         * </p>
         * <p>
         * Note that the {@link #parse(String...)} method will not print help documentation. It will only set
         * the value of the annotated field. It is the responsibility of the caller to inspect the annotated fields
         * and take the appropriate action.
         * </p>
         * @return whether this option disables validation of the other arguments
         */
        boolean help() default false;
    }

    /**
     * <p>
     * Annotate at most one field in your class with {@code @Parameters} and picoCLI will initialize this field
     * with the positional parameters.
     * </p><p>
     * When parsing the command line arguments, picoCLI first tries to match arguments to {@link Option Options}.
     * Positional parameters are the arguments that follow the options, or the arguments that follow a "--" (double
     * dash) argument on the command line.
     * </p><p>
     * For example:
     * </p>
     * <pre>import static picocli.CommandLine.*;
     *
     * public class MyCalcParameters {
     *     &#064;Parameters(type = BigDecimal.class, description = "Any number of input numbers")
     *     private List<BigDecimal> files = new ArrayList<BigDecimal>();
     *
     *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, help = true, description = "Display this help and exit")
     *     private boolean help;
     * }
     * </pre>
     * <p>
     * By default a variable number of parameters can be specified,
     * so the field with this annotation must either be an array (and the value will be replaced by a new array),
     * or a class that implements {@code Collection}.
     * See {@link #type()} for strongly-typed Collection fields.
     * </p><p>
     * There can be only one field annotated with {@code @Parameters}, and a field cannot be annotated with
     * both {@code @Parameters} and {@code @Option} or a {@code ParameterException} is thrown.
     * </p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Parameters {

        /**
         * Description of the parameter(s), used when generating the usage documentation.
         * @return the description of the parameter(s)
         */
        String[] description() default {};

        /**
         * Specifies the minimum number of required parameters and the maximum number of accepted parameters. If a
         * positive arity is declared, and the user specifies an insufficient number of parameters on the command line,
         * {@link MissingParameterException} is thrown by the {@link #parse(String...)} method.
         * <p>The default depends on the type of the parameter: booleans require no parameters, arrays and Collections
         * accept zero to any number of parameters, and any other type accepts one parameter.</p>
         * @return the range of minimum and maximum parameters accepted by this command
         */
        String arity() default "";

        /**
         * Specify a {@code paramLabel} for the parameter to be used in the usage help message. If omitted,
         * picoCLI uses the field name in fish brackets ({@code '<'} and {@code '>'}) by default. Example:
         * <pre>class Example {
         *     &#064;Parameters(paramLabel="FILE", description="path of the input FILE(s)")
         *     private File[] inputFiles;
         * }</pre>
         * <p>By default, the above gives a usage help message like the following:</p><pre>
         * Usage: &lt;main class&gt; [FILE...]
         * [FILE...]       path of the input FILE(s)
         * </pre>
         * @return name of the]] parameter used in the usage help message
         */
        String paramLabel() default "";

        /**
         * <p>
         * Specify a {@code type} if the annotated field is a {@code Collection} that should hold objects other than Strings.
         * </p><p>
         * If the field's type is a {@code Collection}, the generic type parameter of the collection is erased and
         * cannot be determined at runtime. Specify a {@code type} attribute to store values other than String in
         * the Collection. PicoCLI will use the {@link ITypeConverter}
         * that is {@linkplain #registerConverter(Class, ITypeConverter) registered} for that type to convert
         * the raw String values before they are added to the collection.
         * </p><p>
         * When the field's type is an array, the {@code type} attribute is ignored: the values will be converted
         * to the array component type and the array will be replaced with a new instance.
         * </p>
         * @return the type to convert the raw String values to before adding them to the Collection
         */
        Class<?> type() default String.class;

        boolean hidden() default true;
    }

    /**
     * <p>
     * Annotate your class with {@code @Usage} when you want more control over the format of the generated help message
     * or when you want to add a summary description of what the program does or a footer following the option details.
     * </p><pre>
     * &#064;Usage(programName = "Encrypt",
     *        summary     = "Encrypt FILE(s), or standard input, to standard output or to the output file.",
     *        footer      = "Copyright (c) 2017")
     * public class Encrypt {
     *     &#064;Parameters(paramLabel = "FILE", type = File.class, description = "Any number of input files")
     *     private List<File> files = new ArrayList<File>();
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
     * }</pre>
     * <p>
     * The structure of a help message looks like this:
     * </p><ul>
     *   <li>Usage header line: {@code Usage: <programName> [OPTIONS] [FILE...]}</li>
     *   <li>[summary]</li>
     *   <li>a table of options with their usage message</li>
     *   <li>[footer]</li>
     * </ul>
     * <p>
     * If the {@link #detailedUsageHeader()} attribute is {@code true}, the "Usage" header line will have
     * a detailed list of options and parameters.
     * </p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Usage {
        /** Program name to show in the "Usage" message. If omitted, the annotated class name is used. */
        String programName() default "<main class>";
        /** If {@code true}, the "Usage" header line will have a detailed list of options and parameters. */
        boolean detailedUsageHeader() default false;
        /** String that separates options from option parameters. Default {@value}, a popular alternative is {@code "="}. */
        String separator() default " ";
        /** Optional text to display between the first "Usage" line and the list of options. */
        String[] summary() default {};
         /** Optional text to display after the list of options. */
        String[] footer() default {};
    }
    /**
     * <p>
     * When parsing command line arguments and initializing
     * fields annotated with {@link Option @Option} or {@link Parameters @Parameters},
     * String values can be converted to any type for which a {@code ITypeConverter} is registered.
     * </p><p>
     * This interface defines the contract for classes that know how to convert a String into some domain object.
     * Custom converters can be registered with the {@link #registerConverter(Class, ITypeConverter)} method.
     * </p><p>
     * Java 8 lambdas make it easy to register custom type converters:
     * </p>
     * <pre>
     * commandLine.registerConverter(java.nio.file.Path.class, s -> java.nio.file.Paths.get(s));
     * commandLine.registerConverter(java.time.Duration.class, s -> java.time.Duration.parse(s));</pre>
     * <p>
     * Built-in type converters are pre-registered for the following java 1.5 types:
     * </p>
     * <ul>
     *   <li>all primitive types</li>
     *   <li>all primitive wrapper types: Boolean, Byte, Character, Double, Float, Integer, Long, Short</li>
     *   <li>any enum</li>
     *   <li>java.io.File</li>
     *   <li>java.math.BigDecimal</li>
     *   <li>java.math.BigInteger</li>
     *   <li>java.net.InetAddress</li>
     *   <li>java.net.URI</li>
     *   <li>java.net.URL</li>
     *   <li>java.nio.charset.Charset</li>
     *   <li>java.sql.Time</li>
     *   <li>java.util.Date</li>
     *   <li>java.util.UUID</li>
     *   <li>java.util.regex.Pattern</li>
     *   <li>StringBuilder</li>
     *   <li>CharSequence</li>
     *   <li>String</li>
     * </ul>
     * @param <K> the type of the object that is the result of the conversion
     */
    public interface ITypeConverter<K> {
        /**
         * Converts the specified command line option value to some domain object.
         * @param value the command line option String value
         * @return the resulting domain object
         * @throws Exception an exception detailing what went wrong during the conversion
         */
        K convert(String value) throws Exception;
    }
    /** Describes the number of parameters required and accepted by an option or a positional parameter. */
    public static class Arity {
        /** Required number of parameters for an option or positional parameter. */
        public int min;
        /** Maximum accepted number of parameters for an option or positional parameter. */
        public int max;
        public boolean isVariable;
        private final boolean isUnspecified;
        private final String originalValue;

        /** Constructs a new Arity object with the specified parameters.
         * @param min minimum number of required parameters
         * @param max maximum number of allowed parameters (or Integer.MAX_VALUE if variable)
         * @param variable {@code true} if any number or parameters is allowed, {@code false} otherwise
         * @param unspecified {@code true} if no arity was specified on the option or parameter
         * @param originalValue the original value that was specified on the option or parameter
         */
        public Arity(int min, int max, boolean variable, boolean unspecified, String originalValue) {
            this.min = min;
            this.max = max;
            this.isVariable = variable;
            this.isUnspecified = unspecified;
            this.originalValue = originalValue;
        }
        /** Returns a new {@code Arity} based on the Option annotation on the specified field, or the field type if no
         * arity was specified. */
        public static Arity forOption(Field field) {
            return adjustForType(Arity.valueOf(field.getAnnotation(Option.class).arity()), field);
        }
        /** Returns a new {@code Arity} based on the Parameters annotation on the specified field, or the field type
         * if no arity was specified. */
        public static Arity forParameters(Field field) {
            return adjustForType(Arity.valueOf(field.getAnnotation(Parameters.class).arity()), field);
        }
        static Arity adjustForType(Arity result, Field field) {
            if (result.isUnspecified) {
                Arity def = forType(field.getType());
                result.min = def.min;
                result.max = def.max;
                result.isVariable = def.isVariable;
            }
            return result;
        }
        /** Returns a new {@code Arity} based on the specified type: booleans have arity 0, arrays or Collections have
         * arity "0..*", and other types have arity 1. */
        public static Arity forType(Class<?> type) {
            if (type == Boolean.class || type == Boolean.TYPE) {
                return Arity.valueOf("0");
            } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
                return Arity.valueOf("0..*");
            }
            return Arity.valueOf("1");// for single-valued fields
        }
        /** Leniently parses the specified String as an {@code Arity} value and return the result. An arity string can
         * be a fixed integer value or a range of the form {@code MIN_VALUE + ".." + MAX_VALUE}. If the
         * {@code MIN_VALUE} string is not numeric, the minimum is zero. If the {@code MAX_VALUE} is not numeric, the
         * arity is taken to be variable and the maximum is {@code Integer.MAX_VALUE}.*/
        public static Arity valueOf(String arity) {
            arity = arity.trim();
            boolean unspecified = arity.length() == 0 || arity.startsWith(".."); // || arity.endsWith("..");
            int min = -1, max = -1;
            boolean variable = false;
            int dots = -1;
            if ((dots = arity.indexOf("..")) >= 0) {
                min = parseInt(arity.substring(0, dots), 0);
                max = parseInt(arity.substring(dots + 2), Integer.MAX_VALUE);
                variable = max == Integer.MAX_VALUE;
            } else {
                max = parseInt(arity, Integer.MAX_VALUE);
                variable = max == Integer.MAX_VALUE;
                min = variable ? 0 : max;
            }
            Arity result = new Arity(min, max, variable, unspecified, arity);
            return result;
        }
        private static int parseInt(String str, int defaultValue) {
            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        public boolean equals(Object object) {
            if (!(object instanceof Arity)) { return false; }
            Arity other = (Arity) object;
            return other.max == this.max && other.min == this.min && other.isVariable == this.isVariable;
        }
        public int hashCode() {
            return ((17 * 37 + max) * 37 + min) * 37 + (isVariable ? 1 : 0);
        }
        public String toString() {
            String result = min == max ? String.valueOf(min) : min + ".." + (isVariable ? "*" : max);
            return result + (empty(originalValue) || result.equals(originalValue) ? "" : " ('" + originalValue + "')");
        }
    }
    private static Field init(Class<?> cls,
                              List<Field> requiredFields,
                              Map<String, Field> optionName2Field,
                              Map<Character, Field> singleCharOption2Field,
                              Field positionalParametersField) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Option.class)) {
                field.setAccessible(true);
                Option option = field.getAnnotation(Option.class);
                if (option.required()) {
                    requiredFields.add(field);
                }
                for (String name : option.names()) { // cannot be null or empty
                    Field existing = optionName2Field.put(name, field);
                    if (existing != null && existing != field) {
                        throw DuplicateOptionAnnotationsException.create(name, field, existing);
                    }
                    if (name.length() == 2 && name.startsWith("-")) {
                        char flag = name.charAt(1);
                        Field existing2 = singleCharOption2Field.put(flag, field);
                        if (existing2 != null && existing2 != field) {
                            throw DuplicateOptionAnnotationsException.create(name, field, existing2);
                        }
                    }
                }
            }
            if (field.isAnnotationPresent(Parameters.class)) {
                if (positionalParametersField != null) {
                    throw new ParameterException("Only one @Parameters field allowed, but found on '"
                            + positionalParametersField.getName() + "' and '" + field.getName() + "'.");
                } else if (field.isAnnotationPresent(Option.class)) {
                    throw new ParameterException("A field can be either @Option or @Parameters, but '"
                            + field.getName() + "' is both.");
                }
                positionalParametersField = field;
            }
        }
        return positionalParametersField;
    }

    /**
     * Helper class responsible for processing command line arguments.
     */
    private class Interpreter {
        private final Map<Class<?>, ITypeConverter<?>> converterRegistry = new HashMap<Class<?>, ITypeConverter<?>>();
        private final Map<String, Field> optionName2Field                = new HashMap<String, Field>();
        private final Map<Character, Field> singleCharOption2Field       = new HashMap<Character, Field>();
        private final List<Field> requiredFields                         = new ArrayList<Field>();
        private final Object annotatedObject;
        private Field positionalParametersField;
        private boolean isHelpRequested;
        private String separator = "=";

        Interpreter(Object annotatedObject) {
            converterRegistry.put(String.class,        new BuiltIn.StringConverter());
            converterRegistry.put(StringBuilder.class, new BuiltIn.StringBuilderConverter());
            converterRegistry.put(CharSequence.class,  new BuiltIn.CharSequenceConverter());
            converterRegistry.put(Byte.class,          new BuiltIn.ByteConverter());
            converterRegistry.put(Byte.TYPE,           new BuiltIn.ByteConverter());
            converterRegistry.put(Boolean.class,       new BuiltIn.BooleanConverter());
            converterRegistry.put(Boolean.TYPE,        new BuiltIn.BooleanConverter());
            converterRegistry.put(Character.class,     new BuiltIn.CharacterConverter());
            converterRegistry.put(Character.TYPE,      new BuiltIn.CharacterConverter());
            converterRegistry.put(Short.class,         new BuiltIn.ShortConverter());
            converterRegistry.put(Short.TYPE,          new BuiltIn.ShortConverter());
            converterRegistry.put(Integer.class,       new BuiltIn.IntegerConverter());
            converterRegistry.put(Integer.TYPE,        new BuiltIn.IntegerConverter());
            converterRegistry.put(Long.class,          new BuiltIn.LongConverter());
            converterRegistry.put(Long.TYPE,           new BuiltIn.LongConverter());
            converterRegistry.put(Float.class,         new BuiltIn.FloatConverter());
            converterRegistry.put(Float.TYPE,          new BuiltIn.FloatConverter());
            converterRegistry.put(Double.class,        new BuiltIn.DoubleConverter());
            converterRegistry.put(Double.TYPE,         new BuiltIn.DoubleConverter());
            converterRegistry.put(File.class,          new BuiltIn.FileConverter());
            converterRegistry.put(URI.class,           new BuiltIn.URIConverter());
            converterRegistry.put(URL.class,           new BuiltIn.URLConverter());
            converterRegistry.put(Date.class,          new BuiltIn.ISO8601DateConverter());
            converterRegistry.put(Time.class,          new BuiltIn.ISO8601TimeConverter());
            converterRegistry.put(BigDecimal.class,    new BuiltIn.BigDecimalConverter());
            converterRegistry.put(BigInteger.class,    new BuiltIn.BigIntegerConverter());
            converterRegistry.put(Charset.class,       new BuiltIn.CharsetConverter());
            converterRegistry.put(InetAddress.class,   new BuiltIn.InetAddressConverter());
            converterRegistry.put(Pattern.class,       new BuiltIn.PatternConverter());
            converterRegistry.put(UUID.class,          new BuiltIn.UUIDConverter());

            this.annotatedObject    = Assert.notNull(annotatedObject, "annotatedObject");
            Class<?> cls = annotatedObject.getClass();
            while (cls != null) {
                positionalParametersField = init(cls, requiredFields, optionName2Field, singleCharOption2Field, positionalParametersField);
                cls = cls.getSuperclass();
            }
        }

        /**
         * Entry point into parsing command line arguments.
         * @param args the command line arguments
         * @return the annotated object, initialized with the command line arguments
         */
        Object parse(String... args) {
            Assert.notNull(args, "argument array");
            if (positionalParametersField != null) {
                Arity arity = Arity.forParameters(positionalParametersField);
                assertNoMissingParameters(positionalParametersField, arity.min, args.length);
            }
            // first reset any state in case this CommandLine instance is being reused
            isHelpRequested = false;
            Set<Field> required = new HashSet<Field>(requiredFields);
            Stack<String> arguments = new Stack<String>();
            for (int i = args.length - 1; i >= 0; i--) {
                arguments.push(args[i]);
            }
            try {
                processArguments(arguments, required);
            } catch (ParameterException ex) {
                throw ex;
            } catch (Exception ex) {
                //throw ParameterException.create(ex, arg, i, args);
                throw ParameterException.create(ex, "???", arguments.size(), args);
            }
            if (!isHelpRequested && !required.isEmpty()) {
                throw MissingParameterException.create(required);
            }
            return annotatedObject;
        }

        private void processArguments(Stack<String> args, Set<Field> required) throws Exception {
            // arg must be one of:
            // 1. the "--" double dash separating options from positional arguments
            // 1. a stand-alone flag, like "-v" or "--verbose": no value required, must map to boolean or Boolean field
            // 2. a short option followed by an argument, like "-f file" or "-ffile": may map to any type of field
            // 3. a long option followed by an argument, like "-file out.txt" or "-file=out.txt"
            // 3. one or more remaining arguments without any associated options. Must be the last in the list.
            // 4. a combination of stand-alone options, like "-vxr". Equivalent to "-v -x -r", "-v true -x true -r true"
            // 5. a combination of stand-alone options and one option with an argument, like "-vxrffile"

            while (!args.isEmpty()) {
                String arg = args.pop();

                // Double-dash separates options from positional arguments.
                // If found, then interpret the remaining args as positional parameters.
                if ("--".equals(arg)) {
                    processPositionalParameters(args);
                    return; // we are done
                }

                // First try to interpret the argument as a single option (as opposed to a compact group of options).
                // A single option may be without option parameters, like "-v" or "--verbose" (a boolean value),
                // or an option may have one or more option parameters.
                // A parameter may be attached to the option.
                boolean paramAttachedToOption = false;
                int separatorIndex = arg.indexOf(separator);
                if (separatorIndex > 0) {
                    String key = arg.substring(0, separatorIndex);
                    // be greedy. Consume the whole arg as an option if possible.
                    if (optionName2Field.containsKey(key) && !optionName2Field.containsKey(arg)) {
                        paramAttachedToOption = true;
                        String optionParam = arg.substring(separatorIndex + separator.length());
                        args.push(optionParam);
                        arg = key;
                    }
                }
                if (optionName2Field.containsKey(arg)) {
                    processStandaloneOption(required, arg, args, paramAttachedToOption);
                }
                // Compact (single-letter) options can be grouped with other options or with an argument.
                // only single-letter options can be combined with other options or with an argument
                else if (arg.length() > 2 && arg.startsWith("-")) {
                    processClusteredShortOptions(required, arg, args);
                }
                // The argument could not be interpreted as an option.
                // We take this to mean that the remainder are positional arguments
                else {
                    args.push(arg);
                    processPositionalParameters(args);
                    return;
                }
            }
        }

        private void processPositionalParameters(Stack<String> args) throws Exception {
            if (positionalParametersField == null || args.isEmpty()) {
                return;
            }
            Arity arity = Arity.forParameters(positionalParametersField);
            assertNoMissingParameters(positionalParametersField, arity.min, args.size());
            applyOption(positionalParametersField, Parameters.class, arity, false, args);
        }

        private void processStandaloneOption(Set<Field> required,
                                             String arg,
                                             Stack<String> args,
                                             boolean paramAttachedToKey) throws Exception {
            Field field = optionName2Field.get(arg);
            required.remove(field);
            Arity arity = Arity.forOption(field);
            if (paramAttachedToKey) {
                arity.min = Math.max(1, arity.min); // if key=value, minimum arity is at least 1
            }
            applyOption(field, Option.class, arity, paramAttachedToKey, args);
        }

        private void processClusteredShortOptions(Set<Field> required, String arg, Stack<String> args)
                throws Exception {
            String cluster = arg.substring(1);
            do {
                if (cluster.length() > 0 && singleCharOption2Field.containsKey(cluster.charAt(0))) {
                    Field field = singleCharOption2Field.get(cluster.charAt(0));
                    required.remove(field);
                    cluster = cluster.length() > 0 ? cluster.substring(1) : "";
                    boolean paramAttachedToOption = cluster.length() > 0;
                    Arity arity = Arity.forOption(field);
                    if (cluster.startsWith(separator)) {// attached with separator, like -f=FILE or -v=true
                        cluster = cluster.substring(separator.length());
                        arity.min = Math.max(1, arity.min); // if key=value, minimum arity is at least 1
                    }
                    args.push(cluster); // interpret remainder as option parameter (CAUTION: may be empty string!)
                    // arity may be >= 1, or
                    // arity <= 0 && !cluster.startsWith(separator)
                    // e.g., boolean @Option("-v", arity=0, varargs=true); arg "-rvTRUE", remainder cluster="TRUE"
                    int consumed = applyOption(field, Option.class, arity, paramAttachedToOption, args);
                    // only return if cluster (and maybe more) was consumed, otherwise continue do-while loop
                    if (consumed > 0) {
                        return;
                    }
                    cluster = args.pop();
                } else { // cluster is empty || cluster.charAt(0) is not a short option key
                    if (cluster.length() == 0) { // we finished parsing a group of short options like -rxv
                        return; // return normally and parse the next arg
                    }
                    // We get here when the remainder of the cluster group is neither an option,
                    // nor a parameter that the last option could consume.
                    // Consume it and any other remaining parameters as positional parameters.
                    args.push(cluster);
                    processPositionalParameters(args);
                    return;
                }
            } while (true);
        }

        private int applyOption(Field field,
                                Class<?> annotation,
                                Arity arity,
                                boolean valueAttachedToOption,
                                Stack<String> args) throws Exception {
            updateHelpRequested(field);
            if (!args.isEmpty() && args.peek().length() == 0 && !valueAttachedToOption) {
                args.pop(); // throw out empty string we get at the end of a group of clustered short options
            }
            int length = args.size();
            if (arity.max == Integer.MAX_VALUE) {
                arity.max = length; // consume all available args
            }
            assertNoMissingParameters(field, arity.min, length);

            Class<?> cls = field.getType();
            if (cls.isArray()) {
                return applyValuesToArrayField(field, annotation, arity, args, cls);
            }
            if (Collection.class.isAssignableFrom(cls)) {
                return applyValuesToCollectionField(field, annotation, arity, args, cls);
            }
            return applyValueToSingleValuedField(field, arity, args, cls);
        }

        private int applyValueToSingleValuedField(Field field, Arity arity, Stack<String> args, Class<?> cls) throws Exception {
            String value = args.isEmpty() ? null : trim(args.pop()); // unquote the value
            int result = arity.min; // the number or args we need to consume

            // special logic for booleans: BooleanConverter accepts only "true" or "false".
            if ((cls == Boolean.class || cls == Boolean.TYPE) && arity.min <= 0) {

                // boolean option with arity = 0..1 or 0..*: value MAY be a param
                if (arity.max > 0 && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                    result = 1;            // if it is a varargs we only consume 1 argument if it is a boolean value
                } else {
                    if (value != null) {
                        args.push(value); // we don't consume the value
                    }
                    value = "true";      // just specifying the option name sets the boolean to true
                }
            }
            ITypeConverter<?> converter = getTypeConverter(cls);
            Object objValue = tryConvert(field, -1, converter, value, cls);
            field.set(annotatedObject, objValue);
            return result;
        }

        private int applyValuesToArrayField(Field field,
                                            Class<?> annotation,
                                            Arity arity,
                                            Stack<String> args,
                                            Class<?> cls) throws Exception {
            Class<?> type = cls.getComponentType();
            ITypeConverter converter = getTypeConverter(type);
            List<Object> converted = consumeArguments(field, annotation, arity, args, converter, cls);
            Object array = Array.newInstance(type, converted.size());
            field.set(annotatedObject, array);
            for (int i = 0; i < converted.size(); i++) { // get remaining values from the args array
                Array.set(array, i, converted.get(i));
            }
            return converted.size();
        }

        @SuppressWarnings("unchecked")
        private int applyValuesToCollectionField(Field field,
                                                 Class<?> annotation,
                                                 Arity arity,
                                                 Stack<String> args,
                                                 Class<?> cls) throws Exception {
            Collection<Object> collection = (Collection<Object>) field.get(annotatedObject);
            Class<?> type = getTypeAttribute(field);
            ITypeConverter converter = getTypeConverter(type);
            List<Object> converted = consumeArguments(field, annotation, arity, args, converter, type);
            if (collection == null) {
                collection = createCollection(cls);
                field.set(annotatedObject, collection);
            }
            for (Object element : converted) {
                if (element instanceof Collection) {
                    collection.addAll((Collection) element);
                } else {
                    collection.add(element);
                }
            }
            return converted.size();
        }

        private List<Object> consumeArguments(Field field,
                                              Class<?> annotation,
                                              Arity arity,
                                              Stack<String> args,
                                              ITypeConverter converter,
                                              Class<?> type) throws Exception {
            List<Object> result = new ArrayList<Object>();
            for (int i = 0; i < arity.min; i++) { // first do the arity.min mandatory parameters
                result.add(tryConvert(field, i, converter, trim(args.pop()), type));
            }
            int remain = arity.max - arity.min;
            while (remain > 0 && !args.isEmpty()) {// now process the varargs if any
                remain--;
                if (annotation == Parameters.class || !isOption(args.peek())) { // don't trim: quoted strings are not options
                    result.add(tryConvert(field, result.size(), converter, trim(args.pop()), type));
                } else {
                    return result;
                }
            }
            return result;
        }

        /**
         * Called when parsing varargs parameters for a multi-value option.
         * When an option is encountered, the remainder should not be interpreted as vararg elements.
         * @param arg the string to determine whether it is an option or not
         * @return true if it is an option, false otherwise
         */
        private boolean isOption(String arg) {
            if ("--".equals(arg)) {
                return true;
            }
            // not just arg prefix: we may be in the middle of parsing -xrvfFILE
            if (optionName2Field.containsKey(arg)) { // -v or -f or --file (not attached to param or other option)
                return true;
            }
            int separatorIndex = arg.indexOf(separator);
            if (separatorIndex > 0) { // -f=FILE or --file==FILE (attached to param via separator)
                if (optionName2Field.containsKey(arg.substring(0, separatorIndex))) {
                    return true;
                }
            }
            return (arg.length() > 2 && arg.startsWith("-") && singleCharOption2Field.containsKey(arg.charAt(1)));
        }
        private Object tryConvert(Field field, int index, ITypeConverter<?> converter, String value, Class<?> type)
                throws Exception {
            try {
                return converter.convert(value);
            } catch (ParameterException ex) {
                throw new ParameterException(ex.getMessage() + optionDescription(field, index));
            } catch (Exception other) {
                String desc = optionDescription(field, index);
                throw new ParameterException("Could not convert '" + value + "' to " + type.getSimpleName() + desc);
            }
        }

        private String optionDescription(Field field, int index) {
            String desc = "";
            if (field.isAnnotationPresent(Option.class)) {
                desc = " for option '" + field.getAnnotation(Option.class).names()[0] + "'";
                if (index >= 0) {
                    desc += " parameter[" + index + "]";
                }
            } else if (field.isAnnotationPresent(Parameters.class)) {
                desc = " for parameter[" + index + "]";
            }
            return desc;
        }

        private Class<?> getTypeAttribute(Field field) {
            if (field.isAnnotationPresent(Parameters.class)) {
                return field.getAnnotation(Parameters.class).type();
            } else if (field.isAnnotationPresent(Option.class)) {
                return field.getAnnotation(Option.class).type();
            }
            throw new IllegalStateException(field + " has neither @Parameters nor @Option annotation");
        }

        private void updateHelpRequested(Field field) {
            if (field.isAnnotationPresent(Option.class)) {
                isHelpRequested |= field.getAnnotation(Option.class).help();
            }
        }

        @SuppressWarnings("unchecked")
        private Collection<Object> createCollection(Class<?> collectionClass) throws Exception {
            if (collectionClass.isInterface()) {
                if (List.class.isAssignableFrom(collectionClass)) {
                    return new ArrayList<Object>();
                } else if (SortedSet.class.isAssignableFrom(collectionClass)) {
                    return new TreeSet<Object>();
                } else if (Set.class.isAssignableFrom(collectionClass)) {
                    return new HashSet<Object>();
                } else if (Queue.class.isAssignableFrom(collectionClass)) {
                    return new LinkedList<Object>(); // ArrayDeque is only available since 1.6
                }
                return new ArrayList<Object>();
            }
            // custom Collection implementation class must have default constructor
            return (Collection<Object>) collectionClass.newInstance();
        }

        private ITypeConverter<?> getTypeConverter(final Class<?> type) {
            ITypeConverter<?> result = converterRegistry.get(type);
            if (result != null) {
                return result;
            }
            if (type.isEnum()) {
                return new ITypeConverter<Object>() {
                    @SuppressWarnings("unchecked")
                    public Object convert(String value) throws Exception {
                        return Enum.valueOf((Class<Enum>) type, value);
                    }
                };
            }
            throw new MissingTypeConverterException("No TypeConverter registered for " + type.getName());
        }

        private void assertNoMissingParameters(Field field, int arity, int length) {
            if (arity > length) {
                if (arity == 1) {
                    throw new MissingParameterException("Missing required parameter for field '"
                            + field.getName() + "'");
                }
                throw new MissingParameterException("Field '" + field.getName() + "' requires at least " + arity
                        + " parameters, but only " + length + " were specified.");
            }
        }

        private String trim(String value) {
            return unquote(value);
        }

        private String unquote(String value) {
            return value == null
                    ? null
                    : (value.length() > 1 && value.startsWith("\"") && value.endsWith("\""))
                        ? value.substring(1, value.length() - 1)
                        : value;
        }
    }

    /**
     * Inner class to group the built-in {@link ITypeConverter} implementations.
     */
    private static class BuiltIn {
        static class StringConverter implements ITypeConverter<String> {
            public String convert(String value) { return value; }
        }
        static class StringBuilderConverter implements ITypeConverter<StringBuilder> {
            public StringBuilder convert(String value) { return new StringBuilder(value); }
        }
        static class CharSequenceConverter implements ITypeConverter<CharSequence> {
            public String convert(String value) { return value; }
        }
        /** Converts text to a {@code Byte} by delegating to {@link Byte#decode(String)}.*/
        static class ByteConverter implements ITypeConverter<Byte> {
            public Byte convert(String value) { return Byte.decode(value); }
        }
        /** Converts {@code "true"} or {@code "false"} to a {@code Boolean}. Other values result in a ParameterException.*/
        static class BooleanConverter implements ITypeConverter<Boolean> {
            public Boolean convert(String value) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    return Boolean.parseBoolean(value);
                } else {
                    throw new ParameterException("'" + value + "' is not a boolean");
                }
            }
        }
        static class CharacterConverter implements ITypeConverter<Character> {
            public Character convert(String value) {
                if (value.length() > 1) {
                    throw new ParameterException("'" + value + "' is not a single character");
                }
                return value.charAt(0);
            }
        }
        /** Converts text to a {@code Short} by delegating to {@link Short#decode(String)}.*/
        static class ShortConverter implements ITypeConverter<Short> {
            public Short convert(String value) { return Short.decode(value); }
        }
        /** Converts text to an {@code Integer} by delegating to {@link Integer#decode(String)}.*/
        static class IntegerConverter implements ITypeConverter<Integer> {
            public Integer convert(String value) { return Integer.decode(value); }
        }
        /** Converts text to a {@code Long} by delegating to {@link Long#decode(String)}.*/
        static class LongConverter implements ITypeConverter<Long> {
            public Long convert(String value) { return Long.decode(value); }
        }
        static class FloatConverter implements ITypeConverter<Float> {
            public Float convert(String value) { return Float.valueOf(value); }
        }
        static class DoubleConverter implements ITypeConverter<Double> {
            public Double convert(String value) { return Double.valueOf(value); }
        }
        static class FileConverter implements ITypeConverter<File> {
            public File convert(String value) { return new File(value); }
        }
        static class URLConverter implements ITypeConverter<URL> {
            public URL convert(String value) throws MalformedURLException { return new URL(value); }
        }
        static class URIConverter implements ITypeConverter<URI> {
            public URI convert(String value) throws URISyntaxException { return new URI(value); }
        }
        /** Converts text in {@code yyyy-mm-dd} format to a {@code java.util.Date}. ParameterException on failure. */
        static class ISO8601DateConverter implements ITypeConverter<Date> {
            public Date convert(String value) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(value);
                } catch (ParseException e) {
                    throw new ParameterException("'" + value + "' is not a yyyy-MM-dd date");
                }
            }
        }
        /** Converts text in any of the following formats to a {@code java.sql.Time}: {@code HH:mm}, {@code HH:mm:ss},
         * {@code HH:mm:ss.SSS}, {@code HH:mm:ss,SSS}. Other formats result in a ParameterException. */
        static class ISO8601TimeConverter implements ITypeConverter<Time> {
            public Time convert(String value) {
                try {
                    if (value.length() <= 5) {
                        return new Time(new SimpleDateFormat("HH:mm").parse(value).getTime());
                    } else if (value.length() <= 8) {
                        return new Time(new SimpleDateFormat("HH:mm:ss").parse(value).getTime());
                    } else if (value.length() <= 12) {
                        try {
                            return new Time(new SimpleDateFormat("HH:mm:ss.SSS").parse(value).getTime());
                        } catch (ParseException e2) {
                            return new Time(new SimpleDateFormat("HH:mm:ss,SSS").parse(value).getTime());
                        }
                    }
                } catch (ParseException ignored) {
                    // ignored because we throw a ParameterException below
                }
                throw new ParameterException("'" + value + "' is not a HH:mm[:ss[.SSS]] time");
            }
        }
        static class BigDecimalConverter implements ITypeConverter<BigDecimal> {
            public BigDecimal convert(String value) { return new BigDecimal(value); }
        }
        static class BigIntegerConverter implements ITypeConverter<BigInteger> {
            public BigInteger convert(String value) { return new BigInteger(value); }
        }
        static class CharsetConverter implements ITypeConverter<Charset> {
            public Charset convert(String s) { return Charset.forName(s); }
        }
        /** Converts text to a {@code InetAddress} by delegating to {@link InetAddress#getByName(String)}. */
        static class InetAddressConverter implements ITypeConverter<InetAddress> {
            public InetAddress convert(String s) throws Exception { return InetAddress.getByName(s); }
        }
        static class PatternConverter implements ITypeConverter<Pattern> {
            public Pattern convert(String s) { return Pattern.compile(s); }
        }
        static class UUIDConverter implements ITypeConverter<UUID> {
            public UUID convert(String s) throws Exception { return UUID.fromString(s); }
        }
    }

    /**
     * A collection of methods and inner classes that provide fine-grained control over the contents and layout of
     * the usage help message to display to end users when help is requested or invalid input values were specified.
     */
    public static class Help {
        /** Constant String holding the default program name: {@value} */
        protected static final String DEFAULT_PROGRAM_NAME = "<main class>";

        /** LinkedHashMap mapping {@link Option} instances to the {@code Field} they annotate, in declaration order. */
        //public final Map<Option, Field> option2Field = new LinkedHashMap<Option, Field>();
        public List<Field> optionFields = new ArrayList<Field>();

        /** The {@code Field} annotated with {@link Parameters}, or {@code null} if no such field exists. */
        public Field positionalParametersField;

        /** The String to use as the separator between options and option parameters. {@value} by default,
         * initialized from {@link Usage#separator()} if defined. */
        private String separator;

        /** The String to use as the program name in the Usage line of the help message. {@value} by default,
         * initialized from {@link Usage#programName()} if defined. */
        public String programName = DEFAULT_PROGRAM_NAME;

        /** If {@code true}, the "Usage" line will have a detailed list of options and parameters. */
        public Boolean detailedOptionHeader;

        /** The text lines to use as the summary of the help message. Initialized from {@link Usage#summary()}
         *  if the {@code Usage} annotation is present, otherwise this is an empty array and the help message has no
         *  summary. Applications may programmatically set this field to create a custom help message. */
        public String[] summary = {};

        /** The text lines to use as the summary of the help message. Initialized from {@link Usage#footer()}
         *  if the {@code Usage} annotation is present, otherwise this is an empty array and the help message has no
         *  footer. Applications may programmatically set this field to create a custom help message. */
        public String[] footer = {};
        /** Parameter label renderer used for the Usage header line and the option details table. */
        public IParameterLabelRenderer parameterLabelRenderer;

        /** Constructs a new {@code Help} instance, initialized from annotatations on the specified class and super classes. */
        public Help(Class<?> cls) {
            while (cls != null) {
                for (Field field : cls.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Option.class)) {
                        Option option = field.getAnnotation(Option.class);
                        if (!option.hidden()) { // hidden options should not appear in usage help
                            // TODO remember longest concatenated option string length (issue #45)
                            optionFields.add(field);
                        }
                    }
                    if (field.isAnnotationPresent(Parameters.class)) {
                        positionalParametersField = positionalParametersField == null ? field : positionalParametersField;
                    }
                }
                // superclass values should not overwrite values if both class and superclass have a @Usage annotation
                if (cls.isAnnotationPresent(Usage.class)) {
                    Usage usage = cls.getAnnotation(Usage.class);
                    if (DEFAULT_PROGRAM_NAME.equals(programName)) {
                        programName = usage.programName();
                    }
                    if (separator == null) {
                        separator = usage.separator();
                    }
                    if (detailedOptionHeader == null) {
                        detailedOptionHeader = usage.detailedUsageHeader();
                    }
                    if (summary == null || summary.length == 0) {
                        summary = usage.summary();
                    }
                    if (footer == null || footer.length == 0) {
                        footer = usage.footer();
                    }
                }
                cls = cls.getSuperclass();
            }
            parameterLabelRenderer = new DefaultParameterLabelRenderer(separator == null ? " " : separator);
        }
        /**
         * Appends a "Usage" help message to the specified StringBuilder. By default, this generates a generic
         * {@code "Usage <main class> [OPTIONS] [PARAMETERS]"} message. Annotate your class with {@link Usage} to
         * control the program name, or whether the generic "Usage" message should be replaced by a detailed message
         * that shows options and parameters.
         * @param sb the StringBuilder to append the "Usage" help message line to
         * @return this {@code Help} object, to allow method chaining for a more fluent API
         */
        public Help appendUsageTo(StringBuilder sb) {
            if (detailedOptionHeader) {
                return appendDetailedUsagePatternsTo(sb, "Usage: ", new SortByOptionArityAndName(), true);
            }
            return appendGenericUsageTo("Usage: ", sb);
        }

        public Help appendDetailedUsagePatternsTo(StringBuilder sb, String prefix,
                                                  Comparator<Field> optionSort, final boolean clusterBooleanOptions) {
            sb.append(prefix).append(programName);
            List<Field> fields = new ArrayList<Field>(optionFields); // iterate in declaration order
            if (optionSort != null) {
                Collections.sort(fields, optionSort);// iterate in specified sort order
            }
            if (clusterBooleanOptions) { // cluster all short boolean options into a single string
                List<Field> booleanOptions = new ArrayList<Field>();
                StringBuilder clusteredRequired = new StringBuilder();
                StringBuilder clusteredOptional = new StringBuilder();
                for (Field field : fields) {
                    if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        Option option = field.getAnnotation(Option.class);
                        String shortestName = ShortestFirst.sort(option.names())[0];
                        if (shortestName.length() == 2 && shortestName.startsWith("-")) {
                            booleanOptions.add(field);
                            if (option.required()) {
                                clusteredRequired.append(shortestName.substring(1));
                            } else {
                                clusteredOptional.append(shortestName.substring(1));
                            }
                        }
                    }
                }
                fields.removeAll(booleanOptions);
                if (clusteredRequired.length() > 0) {
                    sb.append(" -").append(clusteredRequired);
                }
                if (clusteredOptional.length() > 0) {
                    sb.append(" [-").append(clusteredOptional).append("]");
                }
            }
            for (Field field : fields) {
                Option option = field.getAnnotation(Option.class);
                sb.append(" ");
                String pattern = option.required() ? "%s" : "[%s]";
                String optionNames = ShortestFirst.sort(option.names())[0];
                optionNames += parameterLabelRenderer.renderParameterLabel(field);
                sb.append(String.format(pattern, optionNames));
            }
            if (this.positionalParametersField != null) {
                sb.append(" ");
                sb.append(parameterLabelRenderer.renderParameterLabel(positionalParametersField));
            }
            sb.append(System.getProperty("line.separator"));
            return this;
        }
        public Help appendGenericUsageTo(String prefix, StringBuilder sb) {
            sb.append(prefix).append(programName);
            if (!this.optionFields.isEmpty()) { // only show if annotated object actually has options
                sb.append(" [OPTIONS]");
            }
            if (this.positionalParametersField != null) {
                // sb.append(" [--] "); // implied
                sb.append(' ').append(parameterLabelRenderer.renderParameterLabel(positionalParametersField));
            }
            sb.append(System.getProperty("line.separator"));
            return this;
        }
        /**
         * <p>Appends a description of the {@linkplain Option options} supported by the application to the specified
         * StringBuilder. This implementation {@linkplain SortByShortestOptionName sorts options alphabetically}, and shows
         * only the {@linkplain Option#hidden() non-hidden} options in a {@linkplain TextTable tabular format}
         * using the {@linkplain DefaultOptionRenderer default renderer} and {@linkplain DefaultLayout default layout}.</p>
         * <p>To customize how option details are displayed, instantiate a {@link TextTable} object and install a
         * custom option {@linkplain IOptionRenderer renderer} and/or a custom {@linkplain Help.ILayout layout} to
         * control which aspects of an Option or Field are displayed where.</p>
         * @param sb the StringBuilder to append the "Usage" help message line to
         * @return this {@code Help} object, to allow method chaining for a more fluent API
         */
        public Help appendOptionDetailsTo(StringBuilder sb) {
            TextTable textTable = new TextTable();
            textTable.parameterLabelRenderer = this.parameterLabelRenderer;
            List<Field> fields = new ArrayList<Field>(optionFields); // options are stored in order of declaration
            Collections.sort(fields, new SortByShortestOptionName()); // default: sort options ABC
            for (Field field : fields) {
                Option option = field.getAnnotation(Option.class);
                if (!option.hidden()) {
                    textTable.addOption(option, field);
                }
            }
            //FIXME should this be a separate method?
//            if (positionalParametersField != null) {
//                Parameters parameters = positionalParametersField.getAnnotation(Parameters.class);
//                textTable.addOption(parameters, positionalParametersField);
//            }
            textTable.toString(sb);
            return this;
        }
        public Help appendParameterDetailsTo(StringBuilder sb) {
            if (positionalParametersField != null) {
                Parameters parameters = positionalParametersField.getAnnotation(Parameters.class);
                if (!parameters.hidden()) {
                    TextTable textTable = new TextTable();
                    textTable.parameterLabelRenderer = this.parameterLabelRenderer;
                    textTable.addPositionalParameter(parameters, positionalParametersField);
                }
            }
            return this;
        }
        /** Appends program summary text to the specified StringBuilder. Summary text can be zero or more lines, and
         * can be specified declaratively with the {@link Usage#summary()} annotation attribute. Text values are
         * appended as is and are not reformatted. Applications can programmatically specify a custom summary by
         * instantiating a {@link Help} instance and setting its {@link Help#summary} field.
         * @param sb the StringBuilder to append the program summary text lines to
         * @return this {@code Help} object, to allow method chaining for a more fluent API
         */
        public Help appendSummaryTo(StringBuilder sb) {
            if (summary != null) {
                for (String summaryLine : summary) {
                    sb.append(summaryLine).append(System.getProperty("line.separator"));
                }
            }
            return this;
        }
        /** Appends usage help footer text to the specified StringBuilder. Footer text can be zero or more lines, and
         * can be specified declaratively with the {@link Usage#footer()} annotation attribute. Text values are
         * appended as is and are not reformatted. Applications can programmatically specify a custom footer by
         * instantiating a {@link Help} instance and setting its {@link Help#footer} field.
         * @param sb the StringBuilder to append the usage help footer text lines to
         * @return this {@code Help} object, to allow method chaining for a more fluent API
         */
        public Help appendFooterTo(StringBuilder sb) {
            if (footer != null) {
                for (String line : footer) {
                    sb.append(line).append(System.getProperty("line.separator"));
                }
            }
            return this;
        }
        private static String join(String[] names, int offset, int length, String separator) {
            StringBuilder result = new StringBuilder();
            for (int i = offset; i < offset + length; i++) {
                result.append((i > offset) ? separator : "").append(names[i]);
            }
            return result.toString();
        }
        /** Returns a new default OptionRenderer which converts {@link Option Options} to four columns of text to match
         *  the default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>2-character short option name (or empty string if no short option exists)</li>
         * <li>comma separator (only if both short option and long option exist, empty string otherwise)</li>
         * <li>comma-separated string with long option name(s)</li>
         * <li>first element of the {@link Option#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   Option#description()} array, and these rows look like {@code {"", "", "", option.description()[i]}}.</p>
         */
        public static IOptionRenderer createDefaultOptionRenderer() {
            return new DefaultOptionRenderer();
        }
        /** Returns a new minimal OptionRenderer which converts {@link Option Options} to a single row with two columns
         * of text: an option name and a description. If multiple names or descriptions exist, the first value is used. */
        public static IOptionRenderer createMinimalOptionRenderer() {
            return new MinimalOptionRenderer();
        }
        public static IParameterRenderer createMinimalParameterRenderer() {
            return new MinimalParameterRenderer();
        }
        /** Returns a new default parameter renderer that separates option parameters from their {@linkplain Option
         * options} with the specified separator string, surrounds optional parameters with {@code '['} and {@code ']'}
         * characters and uses ellipses ("...") to indicate that any number of a parameter are allowed.
         * @param separator string that separates options from option parameters
         */
        public static IParameterLabelRenderer createDefaultParameterRenderer(String separator) {
            return new DefaultParameterLabelRenderer(separator);
        }
        /** Returns a new default Layout which displays each array of text values representing an Option on a separate
         * row in the {@linkplain TextTable TextTable}. */
        public static ILayout createDefaultLayout() {
            return new DefaultLayout();
        }
        /** Sorts Fields annotated with {@code Option} by their option name in case-insensitive alphabetic order. If an
         * Option has multiple names, the shortest name is used for the sorting. Help options follow non-help options. */
        public static Comparator<Field> createShortOptionNameComparator() {
            return new SortByShortestOptionName();
        }
        /** Sorts Fields annotated with {@code Option} by their option {@linkplain Arity#max max arity} first, by
         * {@linkplain Arity#min min arity} next, and by {@linkplain #createShortOptionNameComparator() option name} last. */
        public static Comparator<Field> createShortOptionArityAndNameComparator() {
            return new SortByOptionArityAndName();
        }
        /** Sorts short strings before longer strings. */
        public static Comparator<String> shortestFirst() {
            return new ShortestFirst();
        }
        /** When customizing online help for {@link Option Option} details, a custom {@code IOptionRenderer} can be
         * used to create textual representation of an Option in a tabular format: one or more rows, each containing
         * one or more columns. The {@link ILayout ILayout} is responsible for placing these text values in the
         * {@link TextTable TextTable}. */
        public interface IOptionRenderer {
            /**
             * Returns a text representation of the specified Option and the Field that captures the option value.
             * @param option the command line option to show online usage help for
             * @param field the field that will hold the value for the command line option
             * @param parameterLabelRenderer responsible for rendering option parameters to text
             * @return a 2-dimensional array of text values: one or more rows, each containing one or more columns
             */
            String[][] render(Option option, Field field, IParameterLabelRenderer parameterLabelRenderer);
        }
        /** The DefaultOptionRenderer converts {@link Option Options} to four columns of text to match the default
         * {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>2-character short option name (or empty string if no short option exists)</li>
         * <li>comma separator (only if both short option and long option exist, empty string otherwise)</li>
         * <li>comma-separated string with long option name(s)</li>
         * <li>first element of the {@link Option#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   Option#description()} array, and these rows look like {@code {"", "", "", option.description()[i]}}.</p>
         */
        static class DefaultOptionRenderer implements IOptionRenderer {
            public String[][] render(Option option, Field field, IParameterLabelRenderer parameterLabelRenderer) {
                String[] names = new ShortestFirst().sort(option.names());
                int shortOptionCount = names[0].length() == 2 ? 1 : 0;
                String shortOption = shortOptionCount > 0 ? names[0] : "";
                String sep = shortOptionCount > 0 && names.length > 1 ? "," : "";
                String longOption = join(names, shortOptionCount, names.length - shortOptionCount, ", ");
                longOption += parameterLabelRenderer.renderParameterLabel(field);

                String[][] result = new String[option.description().length][4];
                result[0] = new String[] { shortOption, sep, longOption, option.description()[0] };
                for (int i = 1; i < option.description().length; i++) {
                    result[i] = new String[] { "", "", "", option.description()[i] };
                }
                return result;
            }
        }
        /** The MinimalOptionRenderer converts {@link Option Options} to a single row with two columns of text: an
         * option name and a description. If multiple names or description lines exist, the first value is used. */
        static class MinimalOptionRenderer implements IOptionRenderer {
            public String[][] render(Option option, Field field, IParameterLabelRenderer parameterLabelRenderer) {
                return new String[][] {{ option.names()[0] + parameterLabelRenderer.renderParameterLabel(field),
                                           option.description().length == 0 ? "" : option.description()[0] }};
            }
        }
        static class MinimalParameterRenderer implements IParameterRenderer {
            public String[][] render(Parameters param, Field field, IParameterLabelRenderer parameterLabelRenderer) {
                return new String[][] {{ parameterLabelRenderer.renderParameterLabel(field),
                        param.description().length == 0 ? "" : param.description()[0] }};
            }
        }
        /** When customizing online help for {@link Parameters Parameters} details, a custom {@code IParameterRenderer}
         * can be used to create textual representation of a Parameters field in a tabular format: one or more rows,
         * each containing one or more columns. The {@link ILayout ILayout} is responsible for placing these text
         * values in the {@link TextTable TextTable}. */
        public interface IParameterRenderer {
            /**
             * Returns a text representation of the specified Parameters and the Field that captures the parameter values.
             * @param parameters the command line parameters to show online usage help for
             * @param field the field that will hold the value for the command line parameters
             * @param parameterLabelRenderer responsible for rendering parameter labels to text
             * @return a 2-dimensional array of text values: one or more rows, each containing one or more columns
             */
            String[][] render(Parameters parameters, Field field, IParameterLabelRenderer parameterLabelRenderer);
        }
        static class DefaultParameterRenderer implements IParameterRenderer {
            public String[][] render(Parameters params, Field field, IParameterLabelRenderer parameterLabelRenderer) {
                String label = parameterLabelRenderer.renderParameterLabel(field);

                String[][] result = new String[params.description().length][4];
                result[0] = new String[] { "", "", label, params.description()[0] };
                for (int i = 1; i < params.description().length; i++) {
                    result[i] = new String[] { "", "", "", params.description()[i] };
                }
                return result;
            }
        }
        /** When customizing online usage help for an option parameter or a positional parameter, a custom
         * {@code IParameterLabelRenderer} can be used to render the parameter name or label to a String. */
        public interface IParameterLabelRenderer {
            /** Returns a text rendering of the Option parameter or positional parameter; returns an empty string
             * {@code ""} if the option is a boolean and does not take a parameter. */
            String renderParameterLabel(Field field);
        }
        /**
         * DefaultParameterLabelRenderer separates option parameters from their {@linkplain Option options} with a
         * {@linkplain DefaultParameterLabelRenderer#separator separator} string, surrounds optional parameters
         * with {@code '['} and {@code ']'} characters and uses ellipses ("...") to indicate that any number of
         * parameters is allowed for options with variable arity.
         */
        static class DefaultParameterLabelRenderer implements IParameterLabelRenderer {
            /** The string to use to separate option parameters from their options. */
            public final String separator;
            /** Constructs a new DefaultParameterLabelRenderer with the specified separator string. */
            public DefaultParameterLabelRenderer(String separator) {
                this.separator = Assert.notNull(separator, "separator");
            }
            public String renderParameterLabel(Field field) {
                boolean isOptionParameter = field.isAnnotationPresent(Option.class);
                Arity arity = isOptionParameter ? Arity.forOption(field) : Arity.forParameters(field);
                String result = "";
                String sep = isOptionParameter ? separator : "";
                if (arity.min > 0) {
                    for (int i = 0; i < arity.min; i++) {
                        result += sep + renderParameterName(field);
                        sep = " ";
                    }
                }
                if (arity.max > arity.min) {
                    sep = result.length() == 0 ? (isOptionParameter ? separator : "") : " ";
                    int max = arity.isVariable ? 1 : arity.max - arity.min;
                    for (int i = 0; i < max; i++) {
                        if (sep.trim().length() == 0) {
                            result += sep + "[" + renderParameterName(field);
                        } else {
                            result += "[" + sep + renderParameterName(field);
                        }
                        sep  = " ";
                    }
                    if (arity.isVariable) {
                        result += "...";
                    }
                    for (int i = 0; i < max; i++) { result += "]"; }
                }
                return result;
            }
            private String renderParameterName(Field field) {
                String result = null;
                if (field.isAnnotationPresent(Option.class)) {
                    result = field.getAnnotation(Option.class).paramLabel();
                } else if (field.isAnnotationPresent(Parameters.class)) {
                    result = field.getAnnotation(Parameters.class).paramLabel();
                }
                if (result != null && result.trim().length() > 0) {
                    return result.trim();
                }
                return "<" + field.getName() + ">";
            }
        }
        /** When customizing online usage help for {@link Option Option} details, a custom {@code ILayout} can be used
         * to control placing the text values produced by the {@linkplain IOptionRenderer option renderer} in the
         * desired location in the {@link TextTable TextTable}. */
        public interface ILayout {
            /**
             * Copies the specified text values into the correct cells in the {@link TextTable}. Implementations are
             * responsible for ensuring the row exists by {@linkplain TextTable#addEmptyRow() adding an empty row}
             * before populating cells in the table. The {@link TextTable#putValue(int, int, String) putValue} method
             * may write into multiple columns or even multiple rows if the value is larger than the {@link Column}
             * width, depending on the Column's {@link Column.Overflow} setting.
             * It is the responsibility of the Layout to detect that this happened by inspecting the return value
             * and to adjust the location of subsequent values accordingly.
             * @param option the Option that the text values are generated from
             * @param field the field annotated with the specified Option
             * @param values the text values representing the Option, which are to be displayed in tabular form
             * @param textTable the data structure holding {@code char[]} objects for each cell in the table
             */
            void layout(Option option, Field field, String[][] values, TextTable textTable);
        }
        /** The default Layout displays each array of text values representing an Option on a separate row in the
         * {@linkplain TextTable TextTable}. */
        static class DefaultLayout implements ILayout {
            /**
             * The DefaultLayout relies on the {@link IOptionRenderer} having created exactly as many {@link Column Columns}
             * as the {@link TextTable} was constructed with, so it can simply call {@link TextTable#addRow(String...)}
             * for each row.
             */
            public void layout(Option option, Field field, String[][] cellValues, TextTable table) {
                for (String[] oneRow : cellValues) {
                    table.addRow(oneRow);
                }
            }
        }
        /** Sorts short strings before longer strings. */
        static class ShortestFirst implements Comparator<String> {
            public int compare(String o1, String o2) {
                return o1.length() - o2.length();
            }
            /** Sorts the specified array of Strings shortest-first and returns it. */
            public static String[] sort(String[] names) {
                Arrays.sort(names, new ShortestFirst());
                return names;
            }
        }
        /** Sorts {@code Option} instances by their name in case-insensitive alphabetic order. If an Option has
         * multiple names, the shortest name is used for the sorting. Help options follow non-help options. */
        static class SortByShortestOptionName implements Comparator<Field> {
            ShortestFirst shortestFirst = new ShortestFirst();
            public int compare(Field f1, Field f2) {
                Option o1 = f1.getAnnotation(Option.class);
                Option o2 = f2.getAnnotation(Option.class);
                String[] names1 = shortestFirst.sort(o1.names());
                String[] names2 = shortestFirst.sort(o2.names());
                int result = names1[0].toUpperCase().compareTo(names2[0].toUpperCase()); // case insensitive sort
                result = result == 0 ? -names1[0].compareTo(names2[0]) : result; // lower case before upper case
                return o1.help() == o2.help() ? result : o2.help() ? -1 : 1; // help options come last
            }
        }
        /** Sorts {@code Option} instances by their max arity first, then their min arity, then delegates to super class. */
        static class SortByOptionArityAndName extends SortByShortestOptionName {
            public int compare(Field f1, Field f2) {
                Option o1 = f1.getAnnotation(Option.class);
                Option o2 = f2.getAnnotation(Option.class);
                Arity arity1 = Arity.forOption(f1);
                Arity arity2 = Arity.forOption(f2);
                int result = arity1.max - arity2.max;
                if (result == 0) {
                    result = arity1.min - arity2.min;
                }
                return result == 0 ? super.compare(f1, f2) : result;
            }
        }
        /**
         * <p>Provides a table layout for text values, applicable for arranging option names and their description on
         * the console. A table has a fixed number of {@link Column Columns}, where each column has a fixed width,
         * indent and {@link Column.Overflow} policy. The Overflow policy determines what happens when a value is
         * longer than the column width.</p>
         */
        public static class TextTable {
            /** The column definitions of this table. */
            protected final Column[] columns;
            /** The {@code char[]} slots of the {@code TextTable} to copy text values into. */
            protected final List<char[]> columnValues = new ArrayList<char[]>();
            /** By default, indent wrapped lines by 2 spaces. */
            public int indentWrappedLines = 2;
            /** The option renderer used to create a text representation of an Option. */
            public IOptionRenderer optionRenderer = new DefaultOptionRenderer();
            public IParameterRenderer parameterRenderer = new DefaultParameterRenderer();
            /** The parameter renderer used to create a text representation of an Option parameter. */
            public IParameterLabelRenderer parameterLabelRenderer = new DefaultParameterLabelRenderer(" "); //FIXME out of sync with  Help paramRenderer...
            /** The layout used to select which columns and rows in the text table to populate for an Option. */
            public ILayout layout = new DefaultLayout();
            /** Constructs a default TextTable with 4 columns. Default TextTable column definitions are:
             * <ol>
             * <li>short option name (width: 4, indent: 2, TRUNCATE on overflow)</li>
             * <li>comma separator (width: 1, indent: 0, TRUNCATE on overflow)</li>
             * <li>long option name(s) (width: 24, indent: 1, SPAN multiple columns on overflow)</li>
             * <li>description line(s) (width: 51, indent: 1, WRAP to next row on overflow)</li>
             * </ol>
             */
            public TextTable() {
                // "  -c, --create                Creates a ...."
                this(  new Column(4,  2, TRUNCATE),   // "  -c"
                        new Column(1,  0, TRUNCATE),   // ","
                        new Column(24, 1, SPAN),  // " --create"
                        new Column(51, 1, WRAP)); // " Creates a ..."
            }
            /** Constructs a {@code TextTable} with the specified column definitions. */
            public TextTable(Column... columns) {
                this.columns = Assert.notNull(columns, "columns");
            }
            /** Returns the {@code char[]} slot at the specified row and column to write a text value into. */
            public char[] cellAt(int row, int col) { return columnValues.get(col + (row * columns.length)); }

            /** Returns the current number of rows of this {@code TextTable}. */
            public int rowCount() { return columnValues.size() / columns.length; }

            /** Adds the required {@code char[]} slots for a new row to the {@link #columnValues} field. */
            public void addEmptyRow() {
                for (int i = 0; i < columns.length; i++) {
                    char[] array = new char[columns[i].width];
                    Arrays.fill(array, ' ');
                    columnValues.add(array);
                }
            }
            /**
             * Convenience method that delegates to the configured {@link TextTable#optionRenderer renderer} to obtain text
             * values for the specified {@link Option}, and then delegates to the configured {@link TextTable#layout
             * layout} to write these text values into the correct cells in this TextTable.
             * @param option the Option whose details and description to print to the console
             * @param field the field annotated with the specified Option
             */
            public void addOption(Option option, Field field) {
                String[][] values = optionRenderer.render(option, field, parameterLabelRenderer);
                layout.layout(option, field, values, this);
            }
            public void addPositionalParameter(Parameters option, Field field) {
                String[][] values = parameterRenderer.render(option, field, parameterLabelRenderer);
                layout.layout(null, field, values, this);
            }
            /**
             * Adds a new {@linkplain TextTable#addEmptyRow() empty row}, then calls {@link
             * TextTable#putValue(int, int, String) putValue} for each of the specified values, adding more empty rows
             * if the return value indicates that the value spanned multiple columns or was wrapped to multiple rows.
             * @param values the values to write into a new row in this TextTable
             * @throws IllegalArgumentException if the number of values exceeds the number of Columns in this table
             */
            public void addRow(String... values) {
                if (values.length > columns.length) {
                    throw new IllegalArgumentException(values.length + " values don't fit in " +
                            columns.length + " columns");
                }
                addEmptyRow();
                for (int col = 0; col < values.length; col++) {
                    int row = rowCount() - 1;// write to last row: previous value may have wrapped to next row
                    Point cell = putValue(row, col, values[col]);

                    // add row if a value spanned/wrapped and there are still remaining values
                    if ((cell.y != row || cell.x != col) && col != values.length - 1) {
                        addEmptyRow();
                    }
                }
            }
            /**
             * Writes the specified value into the cell at the specified row and column and returns the last row and
             * column written to. Depending on the Column's {@link Column#overflow Overflow} policy, the value may span
             * multiple columns or wrap to multiple rows when larger than the column width.
             * @param row the target row in the table
             * @param col the target column in the table to write to
             * @param value the value to write
             * @return a Point whose {@code x} value is the last column written to and whose {@code y} value is the
             *          last row written to
             * @throws IllegalArgumentException if the specified row exceeds the table's {@linkplain
             *          TextTable#rowCount() row count}
             */
            public Point putValue(int row, int col, String value) {
                if (row > rowCount() - 1) {
                    throw new IllegalArgumentException("Cannot write to row " + row + ": rowCount=" + rowCount());
                }
                if (value == null || value.length() == 0) {return new Point(col, row);}
                Column column = columns[col];
                int indent = column.indent;
                switch (column.overflow) {
                    case TRUNCATE:
                        copy(value, cellAt(row, col), indent);
                        return new Point(col, row);
                    case SPAN:
                        int startColumn = col;
                        do {
                            boolean lastColumn = col == columns.length - 1;
                            int charsWritten = lastColumn
                                    ? copy(BreakIterator.getLineInstance(), value, cellAt(row, col), indent)
                                    : copy(value, cellAt(row, col), indent);
                            value = value.substring(charsWritten);
                            indent = 0;
                            if (value.length() > 0) { // value did not fit in column
                                ++col;                // write remainder of value in next column
                            }
                            if (value.length() > 0 && col >= columns.length) { // we filled up all columns on this row
                                addEmptyRow();
                                row++;
                                col = startColumn;
                                indent = column.indent + indentWrappedLines;
                            }
                        } while (value.length() > 0);
                        return new Point(col, row);
                    case WRAP:
                        BreakIterator lineBreakIterator = BreakIterator.getLineInstance();
                        do {
                            int charsWritten = copy(lineBreakIterator, value, cellAt(row, col), indent);
                            value = value.substring(charsWritten);
                            indent = column.indent + indentWrappedLines;
                            if (value.length() > 0) {  // value did not fit in column
                                ++row;         // write remainder of value in next row
                                addEmptyRow();
                            }
                        } while (value.length() > 0);
                        return new Point(col, row);
                }
                throw new IllegalStateException(column.overflow.toString());
            }
            private static int length(String str) {
                return str.length(); // TODO count some characters as double length
            }
            private int copy(BreakIterator line, String text, char[] columnValue, int offset) {
                line.setText(text);
                int done = 0;
                for (int start = line.first(), end = line.next(); end != BreakIterator.DONE; start = end, end = line.next()) {
                    String word = text.substring(start, end);
                    if (columnValue.length >= offset + done + length(word)) {
                        done += copy(word, columnValue, offset + done); // TODO localized length
                    } else {
                        break;
                    }
                }
                return done;
            }
            private static int copy(String value, char[] destination, int offset) {
                int length = Math.min(value.length(), destination.length - offset);
                value.getChars(0, length, destination, offset);
                return length;
            }

            /** Copies the text representation that we built up from the options into the specified StringBuilder. */
            public StringBuilder toString(StringBuilder text) {
                int columnCount = this.columns.length;
                for (int i = 0; i < columnValues.size(); i++) {
                    char[] column = columnValues.get(i);
                    text.append(column);
                    if (i % columnCount == columnCount - 1) {
                        text.append(System.getProperty("line.separator"));
                    }
                }
                return text;
            }
            public String toString() { return toString(new StringBuilder()).toString(); }
        }
        /** Columns define the width, indent (leading number of spaces in a column before the value) and
         * {@linkplain Overflow Overflow} policy of a column in a {@linkplain TextTable TextTable}. */
        public static class Column {
            /** Policy for handling text that is longer than the column width:
             *  span multiple columns, wrap to the next row, or simply truncate the portion that doesn't fit. */
            public enum Overflow { TRUNCATE, SPAN, WRAP }
            /** Column width in characters */
            public final int width;
            /** Indent (number of empty spaces at the start of the column preceding the text value) */
            public final int indent;
            /** Policy that determines how to handle values larger than the column width. */
            public final Overflow overflow;
            public Column(int width, int indent, Overflow overflow) {
                this.width = width;
                this.indent = indent;
                this.overflow = Assert.notNull(overflow, "overflow");
            }
        }
    }

    /**
     * Utility class providing some defensive coding convenience methods.
     */
    private static final class Assert {
        /**
         * Throws a NullPointerException if the specified object is null.
         * @param object the object to verify
         * @param description error message
         * @param <T> type of the object to check
         * @return the verified object
         */
        static <T> T notNull(T object, String description) {
            if (object == null) {
                throw new NullPointerException(description);
            }
            return object;
        }
    }

    /**
     * Exception indicating something went wrong while parsing command line options.
     */
    public static class ParameterException extends RuntimeException {
        public ParameterException(String msg) {
            super(msg);
        }

        public ParameterException(String msg, Exception ex) {
            super(msg, ex);
        }

        private static ParameterException create(Exception ex, String arg, int i, String[] args) {
            String next = args.length < i + 1 ? " " + args[i + 1] : "";
            String msg = ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage()
                    + " while processing option[" + i + "] '" + arg + next + "'.";
            return new ParameterException(msg, ex);
        }
    }

    /**
     * Exception indicating that a required parameter was not specified.
     */
    public static class MissingParameterException extends ParameterException {
        public MissingParameterException(String msg) {
            super(msg);
        }

        private static MissingParameterException create(Set<Field> missing) {
            if (missing.size() == 1) {
                return new MissingParameterException("Missing required option '"
                        + missing.iterator().next().getName() + "'");
            }
            List<String> names = new ArrayList<String>(missing.size());
            for (Field field : missing) {
                names.add(field.getName());
            }
            return new MissingParameterException("Missing required options " + names.toString());
        }
    }

    /**
     * Exception indicating that multiple fields have been annotated with the same Option name.
     */
    public static class DuplicateOptionAnnotationsException extends ParameterException {
        public DuplicateOptionAnnotationsException(String msg) {
            super(msg);
        }

        private static DuplicateOptionAnnotationsException create(String name, Field field1, Field field2) {
            return new DuplicateOptionAnnotationsException("Option name '" + name + "' is used in both "
                    + field1.getName() + " and " + field2.getName());
        }
    }

    /**
     * Exception indicating that an annotated field had a type for which no {@link ITypeConverter} was
     * {@linkplain #registerConverter(Class, ITypeConverter) registered}.
     */
    public static class MissingTypeConverterException extends ParameterException {
        public MissingTypeConverterException(String msg) {
            super(msg);
        }
    }
}
