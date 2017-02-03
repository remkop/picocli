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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * <p>
 * CommandLine interpreter that uses reflection to initialize an annotated domain object with values obtained from the
 * command line arguments.
 * </p><p>
 * <h2>Example</h2>
 * </p>
 * <pre>import static picocli.CommandLine.*;
 *
 * public class MyClass {
 *     &#064;Parameters(type = File.class, description = "Any number of input files")
 *     private List<File> files = new ArrayList<File>();
 *
 *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely list files processed")
 *     private boolean verbose;
 *
 *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
 *     private File outputFile;
 *
 *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, help = true, description = "Display this help and exit")
 *     private boolean help;
 *
 *     &#064;Option(names = { "-V", "--version"}, help = true, description = "Display version information and exit")
 *     private boolean version;
 * }
 * </pre>
 * <p>
 * Use {@code CommandLine} to initialize a domain object as follows:
 * </p><pre>
 * public static void main(String... args) {
 *     try {
 *         MyClass myClass = CommandLine.parse(new MyClass(), args);
 *         if (myClass.help) {
 *             CommandLine.usage(MyClass.class, System.out);
 *         } else if (myClass.version) {
 *             System.out.println("MyProgram version 1.2.3");
 *         } else {
 *             runProgram(myClass);
 *         }
 *     } catch (ParameterException ex) { // command line arguments could not be parsed
 *         System.err.println(ex.getMessage());
 *         CommandLine.usage(MyClass.class, System.err);
 *     }
 * }
 * </pre><p>
 * Invoke the above program with some command line arguments. The below are all equivalent:
 * </p>
 * <pre>
 * -vooutfile in1 in2
 * -vo outfile in1 in2
 * -vo=outfile in1 in2
 * -v -ooutfile in1 in2
 * -v -o outfile in1 in2
 * -v -o=outfile in1 in2
 * -v --out outfile in1 in2
 * -v --out=outfile in1 in2
 * --verbose --out=outfile in1 in2
 * </pre>
 *
 * https://www.gnu.org/prep/standards/html_node/Command_002dLine-Interfaces.html#Command_002dLine-Interfaces
 * http://stackoverflow.com/questions/2160083/what-is-the-general-syntax-of-a-unix-shell-command/2160165#2160165
 * http://catb.org/~esr/writings/taoup/html/ch10s05.html
 *
 */
// TODO support long options with value separated by "=" or by space
// TODO support short options with value attached or separated by space
// TODO support abbreviated long options when unambiguous
// TODO support arity with array or collection fields, negative arity means variable arity (1 or more)
// TODO ignore arity for single-value fields, special case arity=1 for booleans to make parameter mandatory
// TODO support positional arguments (either with "--" separator or without)
// TODO Usage (with description and footer)
// TODO support commands
// TODO do we need to check duplicates if child & super have same name? same option name?
public class CommandLine {
    /** This is PicoCLI version {@value}. */
    public static final String VERSION = "0.1.0";

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
     * @param annotatedClass the class with fields annotated with {@code @Parameters} and {@code @Option}
     * @param out the {@code PrintStream} to print the usage help message to
     */
    public static void usage(Class<?> annotatedClass, PrintStream out) {
        throw new UnsupportedOperationException("TODO");// FIXME
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
     *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely list files processed")
     *     private boolean verbose;
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
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
        String description() default "";

        /**
         * Indicates whether this option is required. By default this is false.
         * If an option is required, but a user invokes the program without specifying the required option,
         * a {@link MissingParameterException} is thrown from the {@link #parse(String...)} method.
         * @return whether this option is required
         */
        boolean required() default false;

        /**
         * Specifies how many parameters are required. If a positive arity is declared, and the user
         * specifies an insufficient number of parameters on the command line,
         * {@link MissingParameterException} is thrown by the {@link #parse(String...)} method.
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
         * specify {@code @Option(names="-name", arity=2, varargs=true)}.
         * </p>
         * <b>A note on boolean options</b>
         * <p>
         * By default picoCLI does not expect boolean options (also called "flags" or "switches") to have a parameter.
         * You can make a boolean option take a required parameter by defining your field with arity=1. For example:
         * </p>
         * <pre>&#064;Option(names="-v", arity=1) boolean verbose;</pre>
         * <p>
         * Because this boolean field is defined with arity 1, the user must specify either {@code <program> -v false}
         * or {@code <program> -v true}
         * on the command line, or a {@link MissingParameterException} is thrown by the {@link #parse(String...)}
         * method.
         * </p><p>
         * To make the boolean parameter possible but optional, define the field with varargs=true. For example:
         * </p>
         * <pre>&#064;Option(names="-v", varargs=true) boolean verbose;</pre>
         * <p>
         * This will accept any of the below without throwing an exception:
         * </p>
         * <pre>
         * -v
         * -v true
         * -v false
         * </pre>
         * @return how many arguments this option requires
         */
        int arity() default -1;

        /**
         * Set {@code varargs=true} when this option accepts a variable number of parameters.
         * The default is {@code false}, so the number of parameters required by this option is fixed
         * (and determined by the field type and the specified {@linkplain #arity() arity}).
         * <p>
         * For example, if an option has 2 required parameters and any number of optional parameters,
         * specify {@code @Option(names="-name", arity=2, varargs=true)}.</p>
         * @return whether this option accepts any optional arguments
         */
        boolean varargs() default false;

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
     *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely show process information")
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
        String description() default "";

        /**
         * Specifies how many parameters are required. If a positive arity is declared, and the user
         * specifies an insufficient number of parameters on the command line,
         * {@link MissingParameterException} is thrown by the {@link #parse(String...)} method.
         * <p>
         * The default is zero: all parameters are optional.
         * </p>
         * @return how many parameters this command requires
         */
        int arity() default 0;

        /**
         * Set {@code varargs=false} when a fixed number of parameters is required.
         * The default is {@code true}, meaning any number of parameters can be specified.
         * <p>
         * For example, if a program has 2 required parameters and no optional parameters,
         * specify {@code @Parameters(arity=2, varargs=false)}.</p>
         * @return whether the program accepts a variable number of parameters
         */
        boolean varargs() default true;

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

    /**
     * Helper class responsible for processing command line arguments.
     */
    private class Interpreter {
        private final Map<Class<?>, ITypeConverter<?>> converterRegistry = new HashMap<Class<?>, ITypeConverter<?>>();
        private final Object annotatedObject;
        private final Map<String, Field> optionName2Field;
        private final Map<Character, Field> singleCharOption2Field;
        private Field positionalParametersField;
        private final List<Field> requiredFields;
        private String separator = "=";
        private boolean isHelpRequested;

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
            optionName2Field        = new HashMap<String, Field>();
            singleCharOption2Field  = new HashMap<Character, Field>();
            requiredFields          = new ArrayList<Field>();

            Class<?> cls = annotatedObject.getClass();
            while (cls != null) {
                init(cls);
                cls = cls.getSuperclass();
            }
        }

        private void init(Class<?> cls) {
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
        }

        Object parse(String... args) {
            Assert.notNull(args, "argument array");
            Set<Field> required = new HashSet<Field>(requiredFields);
            for (int i = 0; i < args.length; i++) {
                String arg = trim(args[i]);
                try {
                    i = processOption(required, arg, i, args);
                } catch (ParameterException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw ParameterException.create(ex, arg, i, args);
                }
                if (isHelpRequested) {
                    break;
                }
            }
            if (!isHelpRequested && !required.isEmpty()) {
                throw MissingParameterException.create(required);
            }
            return annotatedObject;
        }

        private int processOption(Set<Field> required, String arg, int index, String[] args) throws Exception {
            // arg must be one of:
            // 1. the "--" double dash separating options from positional arguments
            // 1. a stand-alone flag, like "-v" or "--verbose": no value required, must map to boolean or Boolean field
            // 2. a short option followed by an argument, like "-f file" or "-ffile": may map to any type of field
            // 3. a long option followed by an argument, like "-file out.txt" or "-file=out.txt"
            // 3. one or more remaining arguments without any associated options. Must be the last in the list.
            // 4. a combination of stand-alone options, like "-vxr". Equivalent to "-v -x -r", "-v true -x true -r true"
            // 5. a combination of stand-alone options and one option with an argument, like "-vxrffile"

            // Double-dash separates options from positional arguments.
            // If found, then interpret the remaining args as positional parameters.
            if ("--".equals(arg)) {
                setPositionalArguments(args, index + 1);
                return args.length; // we are done
            }

            // First try to interpret the argument as a single option (as opposed to a compact group of options).
            // A single option may be without option parameters, like "-v" or "--verbose" (a boolean value),
            // or an option may have one or more option parameters.
            // A parameter may be attached to the option.
            String optionParam = null;
            int separatorIndex = arg.indexOf(separator);
            if (separatorIndex > 0) {
                String key = arg.substring(0, separatorIndex);
                // be greedy. Consume the whole arg as an option if possible.
                if (optionName2Field.containsKey(key) && !optionName2Field.containsKey(arg)) {
                    optionParam = arg.substring(separatorIndex + separator.length());
                    arg = key;
                }
            }
            if (optionName2Field.containsKey(arg)) {
                return processStandaloneOption(required, arg, index, args, optionParam);
            }
            // Compact (single-letter) options can be grouped with other options or with an argument.
            // only single-letter options can be combined with other options or with an argument
            if (arg.length() > 2 && arg.startsWith("-")) {
                return processClusteredOptions(required, arg, index, args);
            }

            // The argument could not be interpreted as an option.
            // We take this to mean that the remainder are positional arguments
            setPositionalArguments(args, index);
            return args.length; // we are done
        }

        private int processStandaloneOption(final Set<Field> required, final String arg, final int index,
                final String[] args, String optionParam) throws Exception {
            Field field = optionName2Field.get(arg);
            required.remove(field);
            int arity = arity(field);
            boolean varargs = field.getAnnotation(Option.class).varargs();
            boolean paramAttachedToKey = optionParam != null;
            int paramIndex = index;
            if (paramAttachedToKey) {
                arity = Math.max(1, arity); // if key=value, arity is at least 1
            } else {
                paramIndex = index + 1;
                optionParam = (args.length > paramIndex) ? args[paramIndex] : null;
            }
            int argsConsumed = applyOption(field, Option.class, varargs, arity, optionParam, paramIndex, args);
            if (paramAttachedToKey) {
                argsConsumed = argsConsumed > 0 ? argsConsumed - 1 : argsConsumed;
            }
            return index + argsConsumed;
        }

        private int processClusteredOptions(Set<Field> required, String arg, int index, String[] args) throws Exception {
            String optionParam;
            String compact = arg.substring(1);
            Field field;
            int arity = 0;
            do {
                if (compact.length() > 0 && singleCharOption2Field.containsKey(compact.charAt(0))) {
                    field = singleCharOption2Field.get(compact.charAt(0));
                    required.remove(field);
                    compact = compact.length() > 0 ? compact.substring(1) : "";
                    boolean varargs = field.getAnnotation(Option.class).varargs();
                    arity = arity(field);
                    if (arity >= 1 || compact.startsWith(separator)) { // must interpret the remainder as an option argument
                        optionParam = compact; // assume arg is attached: -fFILE
                        boolean paramAttachedToOption = compact.length() > 0; // but only if we *have* a remainder
                        if (paramAttachedToOption) {
                            if (compact.startsWith(separator)) { // attached with separator: -f=FILE
                                arity = Math.max(1, arity); // arity is at least 1
                                optionParam = compact.substring(separator.length());
                            }
                        } else {
                            index++; // we consume the next argument, need to pass that back
                            optionParam = (args.length > index) ? args[index] : null;
                        }
                        int consumed = applyOption(field, Option.class, varargs, arity, optionParam, index, args);
                        // if 1 consumed then don't advance position: option param was attached to option
                        consumed = consumed > 0 ? consumed - 1: 0;
                        return index + consumed;
                    } else { // arity <= 0 && !compact.startsWith(separator)
                        // e.g., boolean @Option("-v", arity=0, varargs=true); arg "-rvTRUE", remainder compact="TRUE"
                        int consumed = applyOption(field, Option.class, varargs, 0, compact, index, args);
                        // only return if compact (and maybe more) was consumed, otherwise continue do-while loop
                        if (consumed > 0) {
                            return index + consumed - 1; // don't count compact: it was attached
                        }
                    }
                } else { // compact is empty || compact.charAt(0) is not a short option key
                    if (compact.length() == 0) { // we finished parsing a group of short options like -rxv
                        return index; // return normally and parse the next arg
                    }
                    // We get here when the remainder of the compact group is neither an option,
                    // nor a parameter that the last option could consume.
                    // Consume it and any other remaining parameters as positional parameters.
                    String[] remainder = new String[args.length - index];
                    System.arraycopy(args, index, remainder, 0, remainder.length);
                    remainder[0] = compact;
                    setPositionalArguments(remainder, 0);
                    return args.length;
                }
            } while (field != null);
            throw new IllegalStateException(); // we should never get here
        }

        private boolean isOption(String arg) {
            // FIXME not just arg prefix: we may be in the middle of parsing -xrvfFILE
            return optionName2Field.containsKey(arg);
        }

        private int arity(Field field) {
            int arity = field.getAnnotation(Option.class).arity();
            if (arity >= 0) { // if arity was specified, use the specified value
                return arity;
            }
            Class<?> type = field.getType();
            if (type == Boolean.class || type == Boolean.TYPE) {
                return 0;
            }
            if (type.isArray() || Collection.class.isAssignableFrom(type)) {
                return Integer.MAX_VALUE;
            }
            return 1;
        }

        @SuppressWarnings("unchecked")
        private int applyOption(Field field, Class<?> annotation, boolean varargs, int arity, String value, int index, String[] args) throws Exception {
            updateHelpRequested(field);
            Class<?> cls = field.getType();
            int length = args.length - index;
            if (arity == Integer.MAX_VALUE) {
                arity = length; // consume all available args
            }
            assertNoMissingParameters(field, arity, length);
            if (cls.isArray()) {
                Class<?> type = cls.getComponentType();
                ITypeConverter converter = getTypeConverter(type);
                List<Object> converted = consumeArguments(annotation, varargs, arity, value, index, args, converter, cls);
                Object array = Array.newInstance(type, converted.size());
                field.set(annotatedObject, array);
                for (int i = 0; i < converted.size(); i++) { // get remaining values from the args array
                    Array.set(array, i, converted.get(i));
                }
                return converted.size();
            } else if (Collection.class.isAssignableFrom(cls)) {
                Collection<Object> collection = (Collection<Object>) field.get(annotatedObject);
                Class<?> type = getTypeAttribute(field);
                ITypeConverter converter = getTypeConverter(type);
                List<Object> converted = consumeArguments(annotation, varargs, arity, value, index, args, converter, type);
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
            // special logic for booleans: BooleanConverter accepts only "true" or "false". Use when arity >= 1.
            if ((cls == Boolean.class || cls == Boolean.TYPE) && arity <= 0) {
                if (varargs && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                    arity = 1; // if it is a varargs we only consume 1 argument if it is a boolean value
                } else {
                    value = "true"; // just specifying the option name sets the boolean to true
                }
            }
            ITypeConverter<?> converter = getTypeConverter(cls);
            Object objValue = tryConvert(converter, value, cls);
            field.set(annotatedObject, objValue);
            return arity;
        }

        private Object tryConvert(ITypeConverter<?> converter, String value, Class<?> type) throws Exception {
            try {
                return converter.convert(value);
            } catch (ParameterException ex) {
                throw ex;
            } catch (Exception other) {
                throw new ParameterException("Could not convert '" + value + "' to a " + type.getSimpleName());
            }
        }

        private List<Object> consumeArguments(Class<?> annotation, boolean varargs, int arity,
                String value, int index, String[] args, ITypeConverter converter, Class<?> type) throws Exception {
            List<Object> result = new ArrayList<Object>();
            if (arity > 0) { // first do the arity mandatory parameters
                // special treatment for the first value: it may have been attached to the option name
                result.add(tryConvert(converter, value, type));
                for (int i = 1; i < arity; i++) { // get the remaining values from the args array
                    result.add(tryConvert(converter, args[i + index], type));
                }
            }
            if (varargs) { // now process the varargs if any
                if (result.isEmpty()) {
                    if (annotation == Parameters.class || !isOption(value)) {
                        result.add(tryConvert(converter, value, type));
                    }
                }
                for (int i = index + result.size(); i < args.length; i++) {
                    if (annotation == Parameters.class || !isOption(args[i])) {
                        result.add(tryConvert(converter, args[i], type));
                    }
                }
            }
            return result;
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

        private void setPositionalArguments(String[] args, int index) throws Exception {
            if (positionalParametersField == null) {
                return;
            }
            int length = args.length - index;
            boolean varargs = positionalParametersField.getAnnotation(Parameters.class).varargs();
            applyOption(positionalParametersField, Parameters.class, varargs, length, args[index], index, args);
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
                    public Object convert(final String value) throws Exception {
                        return Enum.valueOf((Class<Enum>) type, value);
                    }
                };
            }
            throw new MissingTypeConverterException("No TypeConverter registered for " + type.getName());
        }

        private void assertNoMissingParameters(Field field, int arity, int length) {
            if (arity > length) {
                if (arity == 1) {
                    throw new MissingParameterException("Missing required parameter for " + field.getName());
                }
                throw new MissingParameterException(field.getName() + " requires at least " + arity
                        + " parameters, but only " + length + " were specified.");
            }
        }

        private String trim(String value) {
            return unquote(value);
        }

        private String unquote(String value) {
            return (value.length() > 1 && value.startsWith("\"") && value.endsWith("\""))
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
        static class ByteConverter implements ITypeConverter<Byte> {
            public Byte convert(String value) { return Byte.valueOf(value); }
        }
        static class BooleanConverter implements ITypeConverter<Boolean> {
            public Boolean convert(String value) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    return Boolean.parseBoolean(value);
                } else {
                    throw new ParameterException("'" + value + "' is not a boolean.");
                }
            }
        }
        static class CharacterConverter implements ITypeConverter<Character> {
            public Character convert(String value) {
                if (value.length() > 1) {
                    throw new ParameterException("'" + value + "' is not a single character.");
                }
                return value.charAt(0);
            }
        }
        static class ShortConverter implements ITypeConverter<Short> {
            public Short convert(String value) { return Short.valueOf(value); }
        }
        static class IntegerConverter implements ITypeConverter<Integer> {
            public Integer convert(String value) { return Integer.valueOf(value); }
        }
        static class LongConverter implements ITypeConverter<Long> {
            public Long convert(String value) { return Long.valueOf(value); }
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
        static class ISO8601DateConverter implements ITypeConverter<Date> {
            public Date convert(String value) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(value);
                } catch (ParseException e) {
                    throw new ParameterException("'" + value + "' is not a yyyy-MM-dd date");
                }
            }
        }
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
            public Charset convert(final String s) { return Charset.forName(s); }
        }
        static class InetAddressConverter implements ITypeConverter<InetAddress> {
            public InetAddress convert(final String s) throws Exception { return InetAddress.getByName(s); }
        }
        static class PatternConverter implements ITypeConverter<Pattern> {
            public Pattern convert(final String s) { return Pattern.compile(s); }
        }
        static class UUIDConverter implements ITypeConverter<UUID> {
            public UUID convert(final String s) throws Exception { return UUID.fromString(s); }
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
        static <T> T notNull(final T object, final String description) {
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
                return new MissingParameterException(missing.iterator().next().getName());
            }
            List<String> names = new ArrayList<String>(missing.size());
            for (Field field : missing) {
                names.add(field.getName());
            }
            return new MissingParameterException("Missing options " + names.toString());
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
