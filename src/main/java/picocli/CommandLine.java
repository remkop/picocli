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

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Model.*;
import picocli.CommandLine.ParseResult.GroupMatchContainer;

import static java.util.Locale.ENGLISH;
import static picocli.CommandLine.Help.Column.Overflow.SPAN;
import static picocli.CommandLine.Help.Column.Overflow.TRUNCATE;
import static picocli.CommandLine.Help.Column.Overflow.WRAP;

/**
 * <p>
 * CommandLine interpreter that uses reflection to initialize an annotated user object with values obtained from the
 * command line arguments.
 * </p><h2>Example</h2>
 * <pre>import static picocli.CommandLine.*;
 *
 * &#064;Command(mixinStandardHelpOptions = true, version = "v3.0.0",
 *         header = "Encrypt FILE(s), or standard input, to standard output or to the output file.")
 * public class Encrypt {
 *
 *     &#064;Parameters(description = "Any number of input files")
 *     private List&lt;File&gt; files = new ArrayList&lt;File&gt;();
 *
 *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
 *     private File outputFile;
 *
 *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbose mode. Helpful for troubleshooting. Multiple -v options increase the verbosity.")
 *     private boolean[] verbose;
 * }
 * </pre>
 * <p>
 * Use {@code CommandLine} to initialize a user object as follows:
 * </p><pre>
 * public static void main(String... args) {
 *     Encrypt encrypt = new Encrypt();
 *     try {
 *         ParseResult parseResult = new CommandLine(encrypt).parseArgs(args);
 *         if (!CommandLine.printHelpIfRequested(parseResult)) {
 *             runProgram(encrypt);
 *         }
 *     } catch (ParameterException ex) { // command line arguments could not be parsed
 *         System.err.println(ex.getMessage());
 *         ex.getCommandLine().usage(System.err);
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
 * <p>
 * Another example that implements {@code Callable} and uses the {@link #execute(String...) CommandLine.execute} convenience API to run in a single line of code:
 * </p><a name = "checksum_example"></a>
 * <pre>
 *  &#064;Command(description = "Prints the checksum (MD5 by default) of a file to STDOUT.",
 *           name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0")
 * class CheckSum implements Callable&lt;Integer&gt; {
 *
 *     &#064;Parameters(index = "0", description = "The file whose checksum to calculate.")
 *     private File file;
 *
 *     &#064;Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
 *     private String algorithm = "MD5";
 *
 *     public static void main(String[] args) throws Exception {
 *         // CheckSum implements Callable, so parsing, error handling and handling user
 *         // requests for usage help or version help can be done with one line of code.
 *
 *         int exitCode = new CommandLine(new CheckSum()).execute(args);
 *         System.exit(exitCode);
 *     }
 *
 *     &#064;Override
 *     public Integer call() throws Exception { // your business logic goes here...
 *         byte[] fileContents = Files.readAllBytes(file.toPath());
 *         byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
 *         System.out.printf("%0" + (digest.length*2) + "x%n", new BigInteger(1,digest));
 *         return 0;
 *     }
 * }
 * </pre>
 * <h2>Classes and Interfaces for Defining a CommandSpec Model</h2>
 * <p>
 * <img src="doc-files/class-diagram-definition.png" alt="Classes and Interfaces for Defining a CommandSpec Model">
 * </p>
 * <h2>Classes Related to Parsing Command Line Arguments</h2>
 * <p>
 * <img src="doc-files/class-diagram-parsing.png" alt="Classes Related to Parsing Command Line Arguments">
 * </p>
 */
public class CommandLine {

    /** This is picocli version {@value}. */
    public static final String VERSION = "4.0.0-alpha-3-SNAPSHOT";

    private final Tracer tracer = new Tracer();
    private final CommandSpec commandSpec;
    private final Interpreter interpreter;
    private final IFactory factory;

    private Object executionResult;
    private PrintWriter out;
    private PrintWriter err;
    private Help.ColorScheme colorScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
    private IExitCodeExceptionMapper exitCodeExceptionMapper;
    private IExecutionStrategy executionStrategy = new RunLast();
    private IParameterExceptionHandler parameterExceptionHandler = new IParameterExceptionHandler() {
        public int handleParseException(ParameterException ex, String[] args) {
            CommandLine cmd = ex.getCommandLine();
            DefaultExceptionHandler.internalHandleParseException(ex, cmd.getErr(), cmd.getColorScheme());
            return mappedExitCode(ex, cmd.getExitCodeExceptionMapper(), cmd.getCommandSpec().exitCodeOnInvalidInput());
        }
    };
    private IExecutionExceptionHandler executionExceptionHandler = new IExecutionExceptionHandler() {
        public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
            throw ex;
        }
    };

    /**
     * Constructs a new {@code CommandLine} interpreter with the specified object (which may be an annotated user object or a {@link CommandSpec CommandSpec}) and a default {@linkplain IFactory factory}.
     * <p>The specified object may be a {@link CommandSpec CommandSpec} object, or it may be a {@code @Command}-annotated
     * user object with {@code @Option} and {@code @Parameters}-annotated fields, in which case picocli automatically
     * constructs a {@code CommandSpec} from this user object.
     * </p><p> If the specified command object is an interface {@code Class} with {@code @Option} and {@code @Parameters}-annotated methods,
     * picocli creates a {@link java.lang.reflect.Proxy Proxy} whose methods return the matched command line values.
     * If the specified command object is a concrete {@code Class}, picocli delegates to the default factory to get an instance.
     * </p><p>
     * If the specified object implements {@code Runnable} or {@code Callable}, or if it is a {@code Method} object,
     * the command can be run as an application in a <a href="#checksum_example">single line of code</a> by using the
     * {@link #execute(String...) execute} method to omit some boilerplate code for handling help requests and invalid input.
     * See {@link #getCommandMethods(Class, String) getCommandMethods} for a convenient way to obtain a command {@code Method}.
     * </p><p>
     * When the {@link #parseArgs(String...)} method is called, the {@link CommandSpec CommandSpec} object will be
     * initialized based on command line arguments. If the commandSpec is created from an annotated user object, this
     * user object will be initialized based on the command line arguments.
     * </p>
     * @param command an annotated user object or a {@code CommandSpec} object to initialize from the command line arguments
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     */
    public CommandLine(Object command) {
        this(command, new DefaultFactory());
    }
    /**
     * Constructs a new {@code CommandLine} interpreter with the specified object (which may be an annotated user object or a {@link CommandSpec CommandSpec}) and object factory.
     * <p>The specified object may be a {@link CommandSpec CommandSpec} object, or it may be a {@code @Command}-annotated
     * user object with {@code @Option} and {@code @Parameters}-annotated fields, in which case picocli automatically
     * constructs a {@code CommandSpec} from this user object.
     * </p><p> If the specified command object is an interface {@code Class} with {@code @Option} and {@code @Parameters}-annotated methods,
     * picocli creates a {@link java.lang.reflect.Proxy Proxy} whose methods return the matched command line values.
     * If the specified command object is a concrete {@code Class}, picocli delegates to the {@linkplain IFactory factory} to get an instance.
     * </p><p>
     * If the specified object implements {@code Runnable} or {@code Callable}, or if it is a {@code Method} object,
     * the command can be run as an application in a <a href="#checksum_example">single line of code</a> by using the
     * {@link #execute(String...) execute} method to omit some boilerplate code for handling help requests and invalid input.
     * See {@link #getCommandMethods(Class, String) getCommandMethods} for a convenient way to obtain a command {@code Method}.
     * </p><p>
     * When the {@link #parseArgs(String...)} method is called, the {@link CommandSpec CommandSpec} object will be
     * initialized based on command line arguments. If the commandSpec is created from an annotated user object, this
     * user object will be initialized based on the command line arguments.
     * </p>
     * @param command an annotated user object or a {@code CommandSpec} object to initialize from the command line arguments
     * @param factory the factory used to create instances of {@linkplain Command#subcommands() subcommands}, {@linkplain Option#converter() converters}, etc., that are registered declaratively with annotation attributes
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @since 2.2 */
    public CommandLine(Object command, IFactory factory) {
        this.factory = Assert.notNull(factory, "factory");
        interpreter = new Interpreter();
        commandSpec = CommandSpec.forAnnotatedObject(command, factory);
        commandSpec.commandLine(this);
        commandSpec.validate();
        if (commandSpec.unmatchedArgsBindings().size() > 0) { setUnmatchedArgumentsAllowed(true); }
    }

    /**
     * Returns the {@code CommandSpec} model that this {@code CommandLine} was constructed with.
     * @return the {@code CommandSpec} model
     * @since 3.0 */
    public CommandSpec getCommandSpec() { return commandSpec; }

    /**
     * Adds the options and positional parameters in the specified mixin to this command.
     * <p>The specified object may be a {@link CommandSpec CommandSpec} object, or it may be a user object with
     * {@code @Option} and {@code @Parameters}-annotated fields, in which case picocli automatically
     * constructs a {@code CommandSpec} from this user object.
     * </p>
     * @param name the name by which the mixin object may later be retrieved
     * @param mixin an annotated user object or a {@link CommandSpec CommandSpec} object whose options and positional parameters to add to this command
     * @return this CommandLine object, to allow method chaining
     * @since 3.0 */
    public CommandLine addMixin(String name, Object mixin) {
        getCommandSpec().addMixin(name, CommandSpec.forAnnotatedObject(mixin, factory));
        return this;
    }

    /**
     * Returns a map of user objects whose options and positional parameters were added to ("mixed in" with) this command.
     * @return a new Map containing the user objects mixed in with this command. If {@code CommandSpec} objects without
     *          user objects were programmatically added, use the {@link CommandSpec#mixins() underlying model} directly.
     * @since 3.0 */
    public Map<String, Object> getMixins() {
        Map<String, CommandSpec> mixins = getCommandSpec().mixins();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (String name : mixins.keySet()) { result.put(name, mixins.get(name).userObject); }
        return result;
    }

    /** Registers a subcommand with the name obtained from the {@code @Command(name = "...")} {@linkplain Command#name() annotation attribute} of the specified command.
     * @param command the object to initialize with command line arguments following the subcommand name.
     *                This may be a {@code Class} that has a {@code @Command} annotation, or an instance of such a
     *                class, or a {@code ComandSpec} or {@code CommandLine} instance with its own (nested) subcommands.
     * @return this CommandLine object, to allow method chaining
     * @since 4.0
     * @throws InitializationException if no name could be found for the specified subcommand,
     *          or if another subcommand was already registered under the same name, or if one of the aliases
     *          of the specified subcommand was already used by another subcommand.
     * @see #addSubcommand(String, Object) */
    public CommandLine addSubcommand(Object command) {
        return addSubcommand(null, command, new String[0]);
    }

    /** Registers a subcommand with the specified name. For example:
     * <pre>
     * CommandLine commandLine = new CommandLine(new Git())
     *         .addSubcommand("status",   new GitStatus())
     *         .addSubcommand("commit",   new GitCommit();
     *         .addSubcommand("add",      new GitAdd())
     *         .addSubcommand("branch",   new GitBranch())
     *         .addSubcommand("checkout", new GitCheckout())
     *         //...
     *         ;
     * </pre>
     *
     * <p>The specified object can be an annotated object or a
     * {@code CommandLine} instance with its own nested subcommands. For example:</p>
     * <pre>
     * CommandLine commandLine = new CommandLine(new MainCommand())
     *         .addSubcommand("cmd1",                 new ChildCommand1()) // subcommand
     *         .addSubcommand("cmd2",                 new ChildCommand2())
     *         .addSubcommand("cmd3", new CommandLine(new ChildCommand3()) // subcommand with nested sub-subcommands
     *                 .addSubcommand("cmd3sub1",                 new GrandChild3Command1())
     *                 .addSubcommand("cmd3sub2",                 new GrandChild3Command2())
     *                 .addSubcommand("cmd3sub3", new CommandLine(new GrandChild3Command3()) // deeper nesting
     *                         .addSubcommand("cmd3sub3sub1", new GreatGrandChild3Command3_1())
     *                         .addSubcommand("cmd3sub3sub2", new GreatGrandChild3Command3_2())
     *                 )
     *         );
     * </pre>
     * <p>The default type converters are available on all subcommands and nested sub-subcommands, but custom type
     * converters are registered only with the subcommand hierarchy as it existed when the custom type was registered.
     * To ensure a custom type converter is available to all subcommands, register the type converter last, after
     * adding subcommands.</p>
     * <p>See also the {@link Command#subcommands()} annotation to register subcommands declaratively.</p>
     *
     * @param name the string to recognize on the command line as a subcommand.
     *             If {@code null}, the {@linkplain CommandSpec#name() name} of the specified subcommand is used;
     *             if this is also {@code null}, the first {@linkplain CommandSpec#aliases() alias} is used.
     * @param command the object to initialize with command line arguments following the subcommand name.
     *                This may be a {@code Class} that has a {@code @Command} annotation, or an instance of such a
     *                class, or a {@code ComandSpec} or {@code CommandLine} instance with its own (nested) subcommands.
     * @return this CommandLine object, to allow method chaining
     * @see #registerConverter(Class, ITypeConverter)
     * @since 0.9.7
     * @see Command#subcommands()
     * @throws InitializationException if the specified name is {@code null}, and no alternative name could be found,
     *          or if another subcommand was already registered under the same name, or if one of the aliases
     *          of the specified subcommand was already used by another subcommand.
     */
    public CommandLine addSubcommand(String name, Object command) {
        return addSubcommand(name, command, new String[0]);
    }

    /** Registers a subcommand with the specified name and all specified aliases. See also {@link #addSubcommand(String, Object)}.
     * @param name the string to recognize on the command line as a subcommand.
     *             If {@code null}, the {@linkplain CommandSpec#name() name} of the specified subcommand is used;
     *             if this is also {@code null}, the first {@linkplain CommandSpec#aliases() alias} is used.
     * @param command the object to initialize with command line arguments following the subcommand name.
     *                This may be a {@code Class} that has a {@code @Command} annotation, or an instance of such a
     *                class, or a {@code ComandSpec} or {@code CommandLine} instance with its own (nested) subcommands.
     * @param aliases zero or more alias names that are also recognized on the command line as this subcommand
     * @return this CommandLine object, to allow method chaining
     * @since 3.1
     * @see #addSubcommand(String, Object)
     * @throws InitializationException if the specified name is {@code null}, and no alternative name could be found,
     *          or if another subcommand was already registered under the same name, or if one of the aliases
     *          of the specified subcommand was already used by another subcommand.
     */
    public CommandLine addSubcommand(String name, Object command, String... aliases) {
        CommandLine subcommandLine = toCommandLine(command, factory);
        subcommandLine.getCommandSpec().aliases.addAll(Arrays.asList(aliases));
        getCommandSpec().addSubcommand(name, subcommandLine);
        CommandLine.Model.CommandReflection.initParentCommand(subcommandLine.getCommandSpec().userObject(), getCommandSpec().userObject());
        return this;
    }
    /** Returns a map with the subcommands {@linkplain #addSubcommand(String, Object) registered} on this instance.
     * @return a map with the registered subcommands
     * @since 0.9.7
     */
    public Map<String, CommandLine> getSubcommands() {
        return new LinkedHashMap<String, CommandLine>(getCommandSpec().subcommands());
    }
    /**
     * Returns the command that this is a subcommand of, or {@code null} if this is a top-level command.
     * @return the command that this is a subcommand of, or {@code null} if this is a top-level command
     * @see #addSubcommand(String, Object)
     * @see Command#subcommands()
     * @since 0.9.8
     */
    public CommandLine getParent() {
        CommandSpec parent = getCommandSpec().parent();
        return parent == null ? null : parent.commandLine();
    }

    /** Returns the annotated user object that this {@code CommandLine} instance was constructed with.
     * @param <T> the type of the variable that the return value is being assigned to
     * @return the annotated object that this {@code CommandLine} instance was constructed with
     * @since 0.9.7
     */
    @SuppressWarnings("unchecked")
    public <T> T getCommand() {
        return (T) getCommandSpec().userObject();
    }

    /** Returns {@code true} if an option annotated with {@link Option#usageHelp()} was specified on the command line.
     * @return whether the parser encountered an option annotated with {@link Option#usageHelp()}.
     * @since 0.9.8 */
    public boolean isUsageHelpRequested() { return interpreter.parseResultBuilder != null && interpreter.parseResultBuilder.usageHelpRequested; }

    /** Returns {@code true} if an option annotated with {@link Option#versionHelp()} was specified on the command line.
     * @return whether the parser encountered an option annotated with {@link Option#versionHelp()}.
     * @since 0.9.8 */
    public boolean isVersionHelpRequested() { return interpreter.parseResultBuilder != null && interpreter.parseResultBuilder.versionHelpRequested; }

    /** Returns the {@code IHelpFactory} that is used to construct the usage help message.
     * @see #setHelpFactory(IHelpFactory)
     * @since 3.9
     */
    public IHelpFactory getHelpFactory() {
        return getCommandSpec().usageMessage().helpFactory();
    }

    /** Sets a new {@code IHelpFactory} to customize the usage help message.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param helpFactory the new help factory. Must be non-{@code null}.
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.9
     */
    public CommandLine setHelpFactory(IHelpFactory helpFactory) {
        getCommandSpec().usageMessage().helpFactory(helpFactory);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setHelpFactory(helpFactory);
        }
        return this;
    }

    /**
     * Returns the section keys in the order that the usage help message should render the sections.
     * This ordering may be modified with {@link #setHelpSectionKeys(List) setSectionKeys}. The default keys are (in order):
     * <ol start="0">
     *   <li>{@link UsageMessageSpec#SECTION_KEY_HEADER_HEADING SECTION_KEY_HEADER_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_HEADER SECTION_KEY_HEADER}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_SYNOPSIS_HEADING SECTION_KEY_SYNOPSIS_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_SYNOPSIS SECTION_KEY_SYNOPSIS}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_DESCRIPTION_HEADING SECTION_KEY_DESCRIPTION_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_DESCRIPTION SECTION_KEY_DESCRIPTION}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_PARAMETER_LIST_HEADING SECTION_KEY_PARAMETER_LIST_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_PARAMETER_LIST SECTION_KEY_PARAMETER_LIST}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_OPTION_LIST_HEADING SECTION_KEY_OPTION_LIST_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_OPTION_LIST SECTION_KEY_OPTION_LIST}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_COMMAND_LIST_HEADING SECTION_KEY_COMMAND_LIST_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_COMMAND_LIST SECTION_KEY_COMMAND_LIST}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_EXIT_CODE_LIST_HEADING SECTION_KEY_EXIT_CODE_LIST_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_EXIT_CODE_LIST SECTION_KEY_EXIT_CODE_LIST}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_FOOTER_HEADING SECTION_KEY_FOOTER_HEADING}</li>
     *   <li>{@link UsageMessageSpec#SECTION_KEY_FOOTER SECTION_KEY_FOOTER}</li>
     * </ol>
     * @since 3.9
     */
    public List<String> getHelpSectionKeys() { return getCommandSpec().usageMessage().sectionKeys(); }

    /**
     * Sets the section keys in the order that the usage help message should render the sections.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * <p>Use {@link UsageMessageSpec#sectionKeys(List)} to customize a command without affecting its subcommands.</p>
     * @see #getHelpSectionKeys
     * @since 3.9
     */
    public CommandLine setHelpSectionKeys(List<String> keys) {
        getCommandSpec().usageMessage().sectionKeys(keys);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setHelpSectionKeys(keys);
        }
        return this;
    }

    /**
     * Returns the map of section keys and renderers used to construct the usage help message.
     * The usage help message can be customized by adding, replacing and removing section renderers from this map.
     * Sections can be reordered with {@link #setHelpSectionKeys(List) setSectionKeys}.
     * Sections that are either not in this map or not in the list returned by {@link #getHelpSectionKeys() getSectionKeys} are omitted.
     * <p>
     * NOTE: By modifying the returned {@code Map}, only the usage help message <em>of this command</em> is affected.
     * Use {@link #setHelpSectionMap(Map)} to customize the usage help message for this command <em>and all subcommands</em>.
     * </p>
     * @since 3.9
     */
    public Map<String, IHelpSectionRenderer> getHelpSectionMap() { return getCommandSpec().usageMessage().sectionMap(); }

    /**
     * Sets the map of section keys and renderers used to construct the usage help message.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * <p>Use {@link UsageMessageSpec#sectionMap(Map)} to customize a command without affecting its subcommands.</p>
     * @see #getHelpSectionMap
     * @since 3.9
     */
    public CommandLine setHelpSectionMap(Map<String, IHelpSectionRenderer> map) {
        getCommandSpec().usageMessage().sectionMap(map);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setHelpSectionMap(map);
        }
        return this;
    }
    /**
     * Returns whether line breaks should take wide Chinese, Japanese and Korean characters into account for line-breaking purposes. The default is {@code true}.
     * @return true if wide Chinese, Japanese and Korean characters are counted as double the size of other characters for line-breaking purposes
     * @since 4.0 */
    public boolean isAdjustLineBreaksForWideCJKCharacters() { return getCommandSpec().usageMessage().adjustLineBreaksForWideCJKCharacters(); }
    /** Sets whether line breaks should take wide Chinese, Japanese and Korean characters into account, and returns this UsageMessageSpec. The default is {@code true}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param adjustForWideChars if true, wide Chinese, Japanese and Korean characters are counted as double the size of other characters for line-breaking purposes
     * @since 4.0 */
    public CommandLine setAdjustLineBreaksForWideCJKCharacters(boolean adjustForWideChars) {
        getCommandSpec().usageMessage().adjustLineBreaksForWideCJKCharacters(adjustForWideChars);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setAdjustLineBreaksForWideCJKCharacters(adjustForWideChars);
        }
        return this;
    }

    /** Returns whether the value of boolean flag options should be "toggled" when the option is matched.
     * By default, flags are toggled, so if the value is {@code true} it is set to {@code false}, and when the value is
     * {@code false} it is set to {@code true}. If toggling is off, flags are simply set to {@code true}.
     * @return {@code true} the value of boolean flag options should be "toggled" when the option is matched, {@code false} otherwise
     * @since 3.0
     */
    public boolean isToggleBooleanFlags() {
        return getCommandSpec().parser().toggleBooleanFlags();
    }

    /** Sets whether the value of boolean flag options should be "toggled" when the option is matched. The default is {@code true}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.0
     */
    public CommandLine setToggleBooleanFlags(boolean newValue) {
        getCommandSpec().parser().toggleBooleanFlags(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setToggleBooleanFlags(newValue);
        }
        return this;
    }

    /** Returns whether whether variables should be interpolated in String values. The default is {@code true}.
     * @since 4.0 */
    public boolean isInterpolateVariables() { return getCommandSpec().interpolateVariables(); }
    /** Sets whether whether variables should be interpolated in String values. The default is {@code true}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @since 4.0 */
    public CommandLine setInterpolateVariables(boolean interpolate) {
        getCommandSpec().interpolateVariables(interpolate);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setInterpolateVariables(interpolate);
        }
        return this;
    }

    /** Returns whether options for single-value fields can be specified multiple times on the command line.
     * The default is {@code false} and a {@link OverwrittenOptionException} is thrown if this happens.
     * When {@code true}, the last specified value is retained.
     * @return {@code true} if options for single-value fields can be specified multiple times on the command line, {@code false} otherwise
     * @since 0.9.7
     */
    public boolean isOverwrittenOptionsAllowed() {
        return getCommandSpec().parser().overwrittenOptionsAllowed();
    }

    /** Sets whether options for single-value fields can be specified multiple times on the command line without a {@link OverwrittenOptionException} being thrown.
     * The default is {@code false}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 0.9.7
     */
    public CommandLine setOverwrittenOptionsAllowed(boolean newValue) {
        getCommandSpec().parser().overwrittenOptionsAllowed(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setOverwrittenOptionsAllowed(newValue);
        }
        return this;
    }

    /** Returns whether the parser accepts clustered short options. The default is {@code true}.
     * @return {@code true} if short options like {@code -x -v -f SomeFile} can be clustered together like {@code -xvfSomeFile}, {@code false} otherwise
     * @since 3.0 */
    public boolean isPosixClusteredShortOptionsAllowed() { return getCommandSpec().parser().posixClusteredShortOptionsAllowed(); }

    /** Sets whether short options like {@code -x -v -f SomeFile} can be clustered together like {@code -xvfSomeFile}. The default is {@code true}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.0
     */
    public CommandLine setPosixClusteredShortOptionsAllowed(boolean newValue) {
        getCommandSpec().parser().posixClusteredShortOptionsAllowed(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setPosixClusteredShortOptionsAllowed(newValue);
        }
        return this;
    }

    /** Returns whether the parser should ignore case when converting arguments to {@code enum} values. The default is {@code false}.
     * @return {@code true} if enum values can be specified that don't match the {@code toString()} value of the enum constant, {@code false} otherwise;
     * e.g., for an option of type <a href="https://docs.oracle.com/javase/8/docs/api/java/time/DayOfWeek.html">java.time.DayOfWeek</a>,
     * values {@code MonDaY}, {@code monday} and {@code MONDAY} are all recognized if {@code true}.
     * @since 3.4 */
    public boolean isCaseInsensitiveEnumValuesAllowed() { return getCommandSpec().parser().caseInsensitiveEnumValuesAllowed(); }

    /** Sets whether the parser should ignore case when converting arguments to {@code enum} values. The default is {@code false}.
     * When set to true, for example, for an option of type <a href="https://docs.oracle.com/javase/8/docs/api/java/time/DayOfWeek.html">java.time.DayOfWeek</a>,
     * values {@code MonDaY}, {@code monday} and {@code MONDAY} are all recognized if {@code true}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.4
     */
    public CommandLine setCaseInsensitiveEnumValuesAllowed(boolean newValue) {
        getCommandSpec().parser().caseInsensitiveEnumValuesAllowed(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setCaseInsensitiveEnumValuesAllowed(newValue);
        }
        return this;
    }

    /** Returns whether the parser should trim quotes from command line arguments before processing them. The default is
     * read from the system property "picocli.trimQuotes" and will be {@code true} if the property is present and empty,
     * or if its value is "true".
     * @return {@code true} if the parser should trim quotes from command line arguments before processing them, {@code false} otherwise;
     * @since 3.7 */
    public boolean isTrimQuotes() { return getCommandSpec().parser().trimQuotes(); }

    /** Sets whether the parser should trim quotes from command line arguments before processing them. The default is
     * read from the system property "picocli.trimQuotes" and will be {@code true} if the property is set and empty, or
     * if its value is "true".
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * <p>Calling this method will cause the "picocli.trimQuotes" property to have no effect.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.7
     */
    public CommandLine setTrimQuotes(boolean newValue) {
        getCommandSpec().parser().trimQuotes(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setTrimQuotes(newValue);
        }
        return this;
    }

    /** Returns whether the parser is allowed to split quoted Strings or not. The default is {@code false},
     * so quoted strings are treated as a single value that cannot be split.
     * @return {@code true} if the parser is allowed to split quoted Strings, {@code false} otherwise;
     * @see ArgSpec#splitRegex()
     * @since 3.7 */
    public boolean isSplitQuotedStrings() { return getCommandSpec().parser().splitQuotedStrings(); }

    /** Sets whether the parser is allowed to split quoted Strings. The default is {@code false}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @see ArgSpec#splitRegex()
     * @since 3.7
     */
    public CommandLine setSplitQuotedStrings(boolean newValue) {
        getCommandSpec().parser().splitQuotedStrings(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setSplitQuotedStrings(newValue);
        }
        return this;
    }

    /** Returns the end-of-options delimiter that signals that the remaining command line arguments should be treated as positional parameters.
     * @return the end-of-options delimiter. The default is {@code "--"}.
     * @since 3.5 */
    public String getEndOfOptionsDelimiter() { return getCommandSpec().parser().endOfOptionsDelimiter(); }

    /** Sets the end-of-options delimiter that signals that the remaining command line arguments should be treated as positional parameters.
     * @param delimiter the end-of-options delimiter; must not be {@code null}. The default is {@code "--"}.
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.5 */
    public CommandLine setEndOfOptionsDelimiter(String delimiter) {
        getCommandSpec().parser().endOfOptionsDelimiter(delimiter);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setEndOfOptionsDelimiter(delimiter);
        }
        return this;
    }

    /** Returns the default value provider for the command, or {@code null} if none has been set.
     * @return the default value provider for this command, or {@code null}
     * @since 3.6
     * @see Command#defaultValueProvider()
     * @see CommandSpec#defaultValueProvider()
     * @see ArgSpec#defaultValueString()
     */
    public IDefaultValueProvider getDefaultValueProvider() {
        return getCommandSpec().defaultValueProvider();
    }

    /** Sets a default value provider for the command and sub-commands
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * sub-commands and nested sub-subcommands <em>at the moment this method is called</em>. Sub-commands added
     * later will have the default setting. To ensure a setting is applied to all
     * sub-commands, call the setter last, after adding sub-commands.</p>
     * @param newValue the default value provider to use
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.6
     */
    public CommandLine setDefaultValueProvider(IDefaultValueProvider newValue) {
        getCommandSpec().defaultValueProvider(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setDefaultValueProvider(newValue);
        }
        return this;
    }

    /** Returns whether the parser interprets the first positional parameter as "end of options" so the remaining
     * arguments are all treated as positional parameters. The default is {@code false}.
     * @return {@code true} if all values following the first positional parameter should be treated as positional parameters, {@code false} otherwise
     * @since 2.3
     */
    public boolean isStopAtPositional() {
        return getCommandSpec().parser().stopAtPositional();
    }

    /** Sets whether the parser interprets the first positional parameter as "end of options" so the remaining
     * arguments are all treated as positional parameters. The default is {@code false}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue {@code true} if all values following the first positional parameter should be treated as positional parameters, {@code false} otherwise
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 2.3
     */
    public CommandLine setStopAtPositional(boolean newValue) {
        getCommandSpec().parser().stopAtPositional(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setStopAtPositional(newValue);
        }
        return this;
    }

    /** Returns whether the parser should stop interpreting options and positional parameters as soon as it encounters an
     * unmatched option. Unmatched options are arguments that look like an option but are not one of the known options, or
     * positional arguments for which there is no available slots (the command has no positional parameters or their size is limited).
     * The default is {@code false}.
     * <p>Setting this flag to {@code true} automatically sets the {@linkplain #isUnmatchedArgumentsAllowed() unmatchedArgumentsAllowed} flag to {@code true} also.</p>
     * @return {@code true} when an unmatched option should result in the remaining command line arguments to be added to the
     *      {@linkplain #getUnmatchedArguments() unmatchedArguments list}
     * @since 2.3
     */
    public boolean isStopAtUnmatched() {
        return getCommandSpec().parser().stopAtUnmatched();
    }

    /** Sets whether the parser should stop interpreting options and positional parameters as soon as it encounters an
     * unmatched option. Unmatched options are arguments that look like an option but are not one of the known options, or
     * positional arguments for which there is no available slots (the command has no positional parameters or their size is limited).
     * The default is {@code false}.
     * <p>Setting this flag to {@code true} automatically sets the {@linkplain #setUnmatchedArgumentsAllowed(boolean) unmatchedArgumentsAllowed} flag to {@code true} also.</p>
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue {@code true} when an unmatched option should result in the remaining command line arguments to be added to the
     *      {@linkplain #getUnmatchedArguments() unmatchedArguments list}
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 2.3
     */
    public CommandLine setStopAtUnmatched(boolean newValue) {
        getCommandSpec().parser().stopAtUnmatched(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setStopAtUnmatched(newValue);
        }
        if (newValue) { setUnmatchedArgumentsAllowed(true); }
        return this;
    }

    /** Returns whether arguments on the command line that resemble an option should be treated as positional parameters.
     * The default is {@code false} and the parser behaviour depends on {@link #isUnmatchedArgumentsAllowed()}.
     * @return {@code true} arguments on the command line that resemble an option should be treated as positional parameters, {@code false} otherwise
     * @see #getUnmatchedArguments()
     * @since 3.0
     */
    public boolean isUnmatchedOptionsArePositionalParams() {
        return getCommandSpec().parser().unmatchedOptionsArePositionalParams();
    }

    /** Sets whether arguments on the command line that resemble an option should be treated as positional parameters.
     * The default is {@code false}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting. When {@code true}, arguments on the command line that resemble an option should be treated as positional parameters.
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.0
     * @see #getUnmatchedArguments()
     * @see #isUnmatchedArgumentsAllowed
     */
    public CommandLine setUnmatchedOptionsArePositionalParams(boolean newValue) {
        getCommandSpec().parser().unmatchedOptionsArePositionalParams(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setUnmatchedOptionsArePositionalParams(newValue);
        }
        return this;
    }

    /** Returns whether the end user may specify arguments on the command line that are not matched to any option or parameter fields.
     * The default is {@code false} and a {@link UnmatchedArgumentException} is thrown if this happens.
     * When {@code true}, the last unmatched arguments are available via the {@link #getUnmatchedArguments()} method.
     * @return {@code true} if the end use may specify unmatched arguments on the command line, {@code false} otherwise
     * @see #getUnmatchedArguments()
     * @since 0.9.7
     */
    public boolean isUnmatchedArgumentsAllowed() {
        return getCommandSpec().parser().unmatchedArgumentsAllowed();
    }

    /** Sets whether the end user may specify unmatched arguments on the command line without a {@link UnmatchedArgumentException} being thrown.
     * The default is {@code false}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting. When {@code true}, the last unmatched arguments are available via the {@link #getUnmatchedArguments()} method.
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 0.9.7
     * @see #getUnmatchedArguments()
     */
    public CommandLine setUnmatchedArgumentsAllowed(boolean newValue) {
        getCommandSpec().parser().unmatchedArgumentsAllowed(newValue);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setUnmatchedArgumentsAllowed(newValue);
        }
        return this;
    }

    /** Returns the list of unmatched command line arguments, if any.
     * @return the list of unmatched command line arguments or an empty list
     * @see #isUnmatchedArgumentsAllowed()
     * @since 0.9.7
     */
    public List<String> getUnmatchedArguments() {
        return interpreter.parseResultBuilder == null ? Collections.<String>emptyList() : UnmatchedArgumentException.stripErrorMessage(interpreter.parseResultBuilder.unmatched);
    }

    /**
     * Defines some exit codes used by picocli as default return values from the {@link #execute(String...) execute}
     * and {@link #executeHelpRequest(ParseResult) executeHelpRequest} methods.
     * <p>Commands can override these defaults with annotations (e.g. {@code @Command(exitCodeOnInvalidInput = 12345)}
     * or programmatically (e.g. {@link CommandSpec#exitCodeOnInvalidInput(int)}).</p>
     * <p>Additionally, there are several mechanisms for commands to return custom exit codes.
     * See the javadoc of the {@link #execute(String...) execute} method for details.</p>
     * @since 4.0 */
    public static final class ExitCode {
        /** Return value from the {@link #execute(String...) execute} and
         * {@link #executeHelpRequest(ParseResult) executeHelpRequest} methods signifying successful termination.
         * <p>The value of this constant is {@value}, following unix C/C++ system programming <a href="https://www.freebsd.org/cgi/man.cgi?query=sysexits&sektion=3">conventions</a>.</p> */
        public static final int OK = 0;
        /** Return value from the {@link #execute(String...) execute} method signifying command line usage error: user input for the command was incorrect, e.g., the wrong number of arguments, a bad flag, a bad syntax in a parameter, or whatever. <p>The value of this constant is {@value}, following unix C/C++ system programming <a href="https://www.freebsd.org/cgi/man.cgi?query=sysexits&sektion=3">conventions</a>.</p>*/
        public static final int USAGE = 64;
        /** Return value from the {@link #execute(String...) execute} method signifying internal software error: an exception occurred when invoking the Runnable, Callable or Method user object of a command. <p>The value of this constant is {@value}, following unix C/C++ system programming <a href="https://www.freebsd.org/cgi/man.cgi?query=sysexits&sektion=3">conventions</a>.</p> */
        public static final int SOFTWARE = 70;
        private ExitCode() {} // don't instantiate
    }

    /** {@code @Command}-annotated classes can implement this interface to specify an exit code that will be returned
     * from the {@link #execute(String...) execute} method when the command is successfully invoked.
     *
     * <p>Example usage:</p>
     * <pre>
     * &#064Command
     * class MyCommand implements Runnable, IExitCodeGenerator {
     *     public void run() { System.out.println("Hello"); }
     *     public int getExitCode() { return 123; }
     * }
     * CommandLine cmd = new CommandLine(new MyCommand());
     * int exitCode = cmd.execute(args);
     * assert exitCode == 123;
     * System.exit(exitCode);
     * </pre>
     * @since 4.0
     */
    public interface IExitCodeGenerator {
        /** Returns the exit code that should be returned from the {@link #execute(String...) execute} method.
         * @return the exit code
         */
        int getExitCode();
    }
    /** Interface that provides the appropriate exit code that will be returned from the {@link #execute(String...) execute}
     * method for an exception that occurred during parsing or while invoking the command's Runnable, Callable, or Method.
     * <p>Example usage:</p>
     * <pre>
     * &#064Command
     * class FailingCommand implements Callable&lt;Void&gt; {
     *     public Void call() throws IOException {
     *         throw new IOException("error");
     *     }
     * }
     * IExitCodeExceptionMapper mapper = new IExitCodeExceptionMapper() {
     *     public int getExitCode(Throwable t) {
     *         if (t instanceof IOException && "error".equals(t.getMessage())) {
     *             return 123;
     *         }
     *         return 987;
     *     }
     * }
     *
     * CommandLine cmd = new CommandLine(new FailingCommand());
     * cmd.setExitCodeExceptionMapper(mapper);
     * int exitCode = cmd.execute(args);
     * assert exitCode == 123;
     * System.exit(exitCode);
     * </pre>
     * @see #setExitCodeExceptionMapper(IExitCodeExceptionMapper)
     * @since 4.0
     */
    public interface IExitCodeExceptionMapper {
        /** Returns the exit code that should be returned from the {@link #execute(String...) execute} method.
         * @param exception the exception that occurred during parsing or while invoking the command's Runnable, Callable, or Method.
         * @return the exit code
         */
        int getExitCode(Throwable exception);
    }
    private static int mappedExitCode(Throwable t, IExitCodeExceptionMapper mapper, int defaultExitCode) {
        try {
            return (mapper != null) ? mapper.getExitCode(t) : defaultExitCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultExitCode;
        }
    }

    /** Returns the color scheme to use when printing help.
     * The default value is the {@linkplain picocli.CommandLine.Help#defaultColorScheme(Help.Ansi) default color scheme} with {@link Help.Ansi#AUTO Ansi.AUTO}.
     * @see #execute(String...)
     * @see #usage(PrintStream)
     * @see #usage(PrintWriter)
     * @see #getUsageMessage()
     * @see Help#defaultColorScheme(Help.Ansi)
     * @since 4.0
     */
    public Help.ColorScheme getColorScheme() { return colorScheme; }

    /** Sets the color scheme to use when printing help.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param colorScheme the new color scheme
     * @see #execute(String...)
     * @see #usage(PrintStream)
     * @see #usage(PrintWriter)
     * @see #getUsageMessage()
     * @since 4.0
     */
    public CommandLine setColorScheme(Help.ColorScheme colorScheme) {
        this.colorScheme = Assert.notNull(colorScheme, "colorScheme");
        for (CommandLine sub : getSubcommands().values()) { sub.setColorScheme(colorScheme); }
        return this;
    }

    /** Returns the writer used when printing user-requested usage help or version help during command {@linkplain #execute(String...) execution}.
     * Defaults to a PrintWriter wrapper around {@code System.out} unless {@link #setOut(PrintWriter)} was called with a different writer.
     * <p>This method is used by {@link #execute(String...)}. Custom {@link IExecutionStrategy IExecutionStrategy} implementations should also use this writer.
     * </p><p>
     * By <a href="http://www.gnu.org/prep/standards/html_node/_002d_002dhelp.html">convention</a>, when the user requests
     * help with a {@code --help} or similar option, the usage help message is printed to the standard output stream so that it can be easily searched and paged.</p>
     * @since 4.0 */
    public PrintWriter getOut() { return out != null ? out : new PrintWriter(System.out, true); }

    /** Sets the writer to use when printing user-requested usage help or version help during command {@linkplain #execute(String...) execution}.
     * <p>This method is used by {@link #execute(String...)}. Custom {@link IExecutionStrategy IExecutionStrategy} implementations should also use this writer.</p>
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param out the new PrintWriter to use
     * @return this CommandLine for method chaining
     * @since 4.0
     */
    public CommandLine setOut(PrintWriter out) {
        this.out = Assert.notNull(out, "out");
        for (CommandLine sub : getSubcommands().values()) { sub.setOut(out); }
        return this;
    }

    /** Returns the writer to use when printing diagnostic (error) messages during command {@linkplain #execute(String...) execution}.
     * Defaults to a PrintWriter wrapper around {@code System.err}, unless {@link #setErr(PrintWriter)} was called with a different writer.
     * <p>This method is used by {@link #execute(String...)}.
     * {@link IParameterExceptionHandler IParameterExceptionHandler} and {@link IExecutionExceptionHandler IExecutionExceptionHandler} implementations
     * should use this writer to print error messages (which may include a usage help message) when an unexpected error occurs.</p>
     * @since 4.0 */
    public PrintWriter getErr() { return err != null ? err : new PrintWriter(System.err, true); }

    /** Sets the writer to use when printing diagnostic (error) messages during command {@linkplain #execute(String...) execution}.
     * <p>This method is used by {@link #execute(String...)}.
     * {@link IParameterExceptionHandler IParameterExceptionHandler} and {@link IExecutionExceptionHandler IExecutionExceptionHandler} implementations
     * should use this writer to print error messages (which may include a usage help message) when an unexpected error occurs.</p>
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param err the new PrintWriter to use
     * @return this CommandLine for method chaining
     * @since 4.0 */
    public CommandLine setErr(PrintWriter err) {
        this.err = Assert.notNull(err, "err");
        for (CommandLine sub : getSubcommands().values()) { sub.setErr(err); }
        return this;
    }

    /**
     * Returns the mapper that was set by the application to map from exceptions to exit codes, for use by the {@link #execute(String...) execute} method.
     * @return the mapper that was {@linkplain #setExitCodeExceptionMapper(IExitCodeExceptionMapper) set}, or {@code null} if none was set
     * @since 4.0 */
    public IExitCodeExceptionMapper getExitCodeExceptionMapper() { return exitCodeExceptionMapper; }

    /** Sets the mapper used by the {@link #execute(String...) execute} method to map exceptions to exit codes.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param exitCodeExceptionMapper the new value
     * @return this CommandLine for method chaining
     * @since 4.0 */
    public CommandLine setExitCodeExceptionMapper(IExitCodeExceptionMapper exitCodeExceptionMapper) {
        this.exitCodeExceptionMapper = Assert.notNull(exitCodeExceptionMapper, "exitCodeExceptionMapper");
        for (CommandLine sub : getSubcommands().values()) { sub.setExitCodeExceptionMapper(exitCodeExceptionMapper); }
        return this;
    }

    /** Returns the execution strategy used by the {@link #execute(String...) execute} method to invoke
     * the business logic on the user objects of this command and/or the user-specified subcommand(s).
     * The default value is {@link RunLast RunLast}.
     * @return the execution strategy to run the user-specified command
     * @since 4.0 */
    public IExecutionStrategy getExecutionStrategy() { return executionStrategy; }

    /** Sets the execution strategy that the {@link #execute(String...) execute} method should use to invoke
     * the business logic on the user objects of this command and/or the user-specified subcommand(s).
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param executionStrategy the new execution strategy to run the user-specified command
     * @return this CommandLine for method chaining
     * @since 4.0 */
    public CommandLine setExecutionStrategy(IExecutionStrategy executionStrategy) {
        this.executionStrategy = Assert.notNull(executionStrategy, "executionStrategy");
        for (CommandLine sub : getSubcommands().values()) { sub.setExecutionStrategy(executionStrategy); }
        return this;
    }

    /**
     * Returns the handler for dealing with invalid user input when the command is {@linkplain #execute(String...) executed}.
     * <p>The default implementation prints an error message describing the problem, followed by either {@linkplain UnmatchedArgumentException#printSuggestions(PrintWriter) suggested alternatives}
     * for mistyped options, or the full {@linkplain #usage(PrintWriter, Help.ColorScheme) usage} help message of the {@linkplain ParameterException#getCommandLine() problematic command};
     * it then delegates to the {@linkplain #getExitCodeExceptionMapper() exit code execution mapper} for an exit code, with
     * {@link CommandSpec#exitCodeOnInvalidInput() exitCodeOnInvalidInput} as the default exit code.</p>
     * @return the handler for dealing with invalid user input
     * @since 4.0 */
    public IParameterExceptionHandler getParameterExceptionHandler() { return parameterExceptionHandler; }

    /**
     * Sets the handler for dealing with invalid user input when the command is {@linkplain #execute(String...) executed}.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param parameterExceptionHandler  the new handler for dealing with invalid user input
     * @return this CommandLine for method chaining
     * @since 4.0 */
    public CommandLine setParameterExceptionHandler(IParameterExceptionHandler parameterExceptionHandler) {
        this.parameterExceptionHandler = Assert.notNull(parameterExceptionHandler, "parameterExceptionHandler");
        for (CommandLine sub : getSubcommands().values()) { sub.setParameterExceptionHandler(parameterExceptionHandler); }
        return this;
    }

    /** Returns the handler for dealing with exceptions that occurred in the {@code Callable}, {@code Runnable} or {@code Method}
     * user object of a command when the command was {@linkplain #execute(String...) executed}.
     * <p>The default implementation rethrows the specified exception.</p>
     * @return the handler for dealing with exceptions that occurred in the business logic when the {@link #execute(String...) execute} method was invoked.
     * @since 4.0 */
    public IExecutionExceptionHandler getExecutionExceptionHandler() { return executionExceptionHandler; }

    /**
     * Sets a custom handler for dealing with exceptions that occurred in the {@code Callable}, {@code Runnable} or {@code Method}
     * user object of a command when the command was executed via the {@linkplain #execute(String...) execute} method.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param executionExceptionHandler the handler for dealing with exceptions that occurred in the business logic when the {@link #execute(String...) execute} method was invoked.
     * @return this CommandLine for method chaining
     * @since 4.0 */
    public CommandLine setExecutionExceptionHandler(IExecutionExceptionHandler executionExceptionHandler) {
        this.executionExceptionHandler = Assert.notNull(executionExceptionHandler, "executionExceptionHandler");
        for (CommandLine sub : getSubcommands().values()) { sub.setExecutionExceptionHandler(executionExceptionHandler); }
        return this;
    }

    /**
     * <p>
     * Convenience method that initializes the specified annotated object from the specified command line arguments.
     * </p><p>
     * This is equivalent to
     * </p><pre>
     * CommandLine cli = new CommandLine(command);
     * cli.parse(args);
     * return command;
     * </pre>
     *
     * @param command the object to initialize. This object contains fields annotated with
     *          {@code @Option} or {@code @Parameters}.
     * @param args the command line arguments to parse
     * @param <T> the type of the annotated object
     * @return the specified annotated object
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ParameterException if the specified command line arguments are invalid
     * @since 0.9.7
     */
    public static <T> T populateCommand(T command, String... args) {
        CommandLine cli = toCommandLine(command, new DefaultFactory());
        cli.parse(args);
        return command;
    }

    /**
     * <p>
     * Convenience method that derives the command specification from the specified interface class, and returns an
     * instance of the specified interface. The interface is expected to have annotated getter methods. Picocli will
     * instantiate the interface and the getter methods will return the option and positional parameter values matched on the command line.
     * </p><p>
     * This is equivalent to
     * </p><pre>
     * CommandLine cli = new CommandLine(spec);
     * cli.parse(args);
     * return cli.getCommand();
     * </pre>
     *
     * @param spec the interface that defines the command specification. This object contains getter methods annotated with
     *          {@code @Option} or {@code @Parameters}.
     * @param args the command line arguments to parse
     * @param <T> the type of the annotated object
     * @return an instance of the specified annotated interface
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ParameterException if the specified command line arguments are invalid
     * @since 3.1
     */
    public static <T> T populateSpec(Class<T> spec, String... args) {
        CommandLine cli = toCommandLine(spec, new DefaultFactory());
        cli.parse(args);
        return cli.getCommand();
    }

    /** Parses the specified command line arguments and returns a list of {@code CommandLine} objects representing the
     * top-level command and any subcommands (if any) that were recognized and initialized during the parsing process.
     * <p>
     * If parsing succeeds, the first element in the returned list is always {@code this CommandLine} object. The
     * returned list may contain more elements if subcommands were {@linkplain #addSubcommand(String, Object) registered}
     * and these subcommands were initialized by matching command line arguments. If parsing fails, a
     * {@link ParameterException} is thrown.
     * </p>
     *
     * @param args the command line arguments to parse
     * @return a list with the top-level command and any subcommands initialized by this method
     * @throws ParameterException if the specified command line arguments are invalid; use
     *      {@link ParameterException#getCommandLine()} to get the command or subcommand whose user input was invalid
     */
    public List<CommandLine> parse(String... args) {
        return interpreter.parse(args);
    }
    /** Parses the specified command line arguments and returns a list of {@code ParseResult} with the options, positional
     * parameters, and subcommands (if any) that were recognized and initialized during the parsing process.
     * <p>If parsing fails, a {@link ParameterException} is thrown.</p>
     *
     * @param args the command line arguments to parse
     * @return a list with the top-level command and any subcommands initialized by this method
     * @throws ParameterException if the specified command line arguments are invalid; use
     *      {@link ParameterException#getCommandLine()} to get the command or subcommand whose user input was invalid
     */
    public ParseResult parseArgs(String... args) {
        interpreter.parse(args);
        return getParseResult();
    }
    public ParseResult getParseResult() { return interpreter.parseResultBuilder == null ? null : interpreter.parseResultBuilder.build(); }

    /** Returns the result of calling the user object {@code Callable} or invoking the user object {@code Method}
     * after parsing the user input, or {@code null} if this command has not been {@linkplain #execute(String...) executed}
     * or if this {@code CommandLine} is for a subcommand that was not specified by the end user on the command line.
     * <p><b>Implementation note:</b></p>
     * <p>It is the responsibility of the {@link IExecutionStrategy IExecutionStrategy} to set this value.</p>
     * @param <T> type of the result value
     * @return the result of the user object {@code Callable} or {@code Method} (may be {@code null}), or {@code null} if this (sub)command was not executed
     * @since 4.0
     */
    @SuppressWarnings("unchecked") public <T> T getExecutionResult() { return (T) executionResult; }

    /** Sets the result of calling the business logic on the command's user object.
     * @param result the business logic result, may be {@code null}
     * @see #execute(String...)
     * @see IExecutionStrategy
     * @since 4.0
     */
    public void setExecutionResult(Object result) { executionResult = result; }

    /** Clears the {@linkplain #getExecutionResult() execution result} of a previous invocation from this {@code CommandLine} and all subcommands.
     * @since 4.0 */
    public void clearExecutionResults() {
        executionResult = null;
        for (CommandLine sub : getSubcommands().values()) { sub.clearExecutionResults(); }
    }
    /**
     * Represents a function that can process a List of {@code CommandLine} objects resulting from successfully
     * {@linkplain #parse(String...) parsing} the command line arguments. This is a
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">functional interface</a>
     * whose functional method is {@link #handleParseResult(List, PrintStream, CommandLine.Help.Ansi)}.
     * <p>
     * Implementations of this functions can be passed to the {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...) CommandLine::parseWithHandler}
     * methods to take some next step after the command line was successfully parsed.
     * </p>
     * @see RunFirst
     * @see RunLast
     * @see RunAll
     * @deprecated Use {@link IExecutionStrategy} instead.
     * @since 2.0 */
    @Deprecated public static interface IParseResultHandler {
        /** Processes a List of {@code CommandLine} objects resulting from successfully
         * {@linkplain #parse(String...) parsing} the command line arguments and optionally returns a list of results.
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return a list of results, or an empty list if there are no results
         * @throws ParameterException if a help command was invoked for an unknown subcommand. Any {@code ParameterExceptions}
         *      thrown from this method are treated as if this exception was thrown during parsing and passed to the {@link IExceptionHandler}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) throws ExecutionException;
    }

    /**
     * Represents a function that can process the {@code ParseResult} object resulting from successfully
     * {@linkplain #parseArgs(String...) parsing} the command line arguments. This is a
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">functional interface</a>
     * whose functional method is {@link IParseResultHandler2#handleParseResult(CommandLine.ParseResult)}.
     * <p>
     * Implementations of this function can be passed to the {@link #parseWithHandlers(IParseResultHandler2,  IExceptionHandler2, String...) CommandLine::parseWithHandlers}
     * methods to take some next step after the command line was successfully parsed.
     * </p><p>
     * This interface replaces the {@link IParseResultHandler} interface; it takes the parse result as a {@code ParseResult}
     * object instead of a List of {@code CommandLine} objects, and it has the freedom to select the {@link Help.Ansi} style
     * to use and what {@code PrintStreams} to print to.
     * </p>
     * @param <R> the return type of this handler
     * @see RunFirst
     * @see RunLast
     * @see RunAll
     * @deprecated use {@link IExecutionStrategy} instead, see {@link #execute(String...)}
     * @since 3.0 */
    @Deprecated public static interface IParseResultHandler2<R> {
        /** Processes the {@code ParseResult} object resulting from successfully
         * {@linkplain CommandLine#parseArgs(String...) parsing} the command line arguments and returns a return value.
         * @param parseResult the {@code ParseResult} that resulted from successfully parsing the command line arguments
         * @throws ParameterException if a help command was invoked for an unknown subcommand. Any {@code ParameterExceptions}
         *      thrown from this method are treated as if this exception was thrown during parsing and passed to the {@link IExceptionHandler2}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        R handleParseResult(ParseResult parseResult) throws ExecutionException;
    }

    /**
     * Implementations are responsible for "executing" the user input and returning an exit code.
     * The {@link #execute(String...)} method delegates to a {@linkplain #setExecutionStrategy(IExecutionStrategy) configured} execution strategy.
     * <p><b>Implementation Requirements:</b></p>
     * <p>Implementers responsibilities are:</p>
     * <ul>
     *   <li>From the {@code ParseResult}, select which {@code CommandSpec} should be executed. This is especially important for commands that have subcommands.</li>
     *   <li>"Execute" the selected {@code CommandSpec}. Often this means invoking a method on the spec's {@linkplain CommandSpec#userObject() user object}.</li>
     *   <li>Call {@link CommandLine#setExecutionResult(Object) setExecutionResult} to make the return value of that method invocation available to the application</li>
     *   <li>Return an exit code. Common sources of exit values are the invoked method's return value, or the user object if it implements {@link IExitCodeGenerator}.</li>
     * </ul>
     * <p>Implementors that need to print messages to the console should use the {@linkplain #getOut() output} and {@linkplain #getErr() error} PrintWriters,
     * and the {@linkplain #getColorScheme() color scheme} from the CommandLine object obtained from ParseResult's CommandSpec.</p>
     * <p><b>API Note:</b></p>
     * <p>This interface supersedes {@link IParseResultHandler2}.</p>
     * @since 4.0 */
    public interface IExecutionStrategy {
        /**
         * "Executes" the user input and returns an exit code.
         * Execution often means invoking a method on the selected CommandSpec's {@linkplain CommandSpec#userObject() user object},
         * and making the return value of that invocation available via {@link CommandLine#setExecutionResult(Object) setExecutionResult}.
         * @param parseResult the parse result from which to select one or more {@code CommandSpec} instances to execute.
         * @return an exit code
         * @throws ParameterException if the invoked method on the CommandSpec's user object threw a ParameterException to signify invalid user input.
         * @throws ExecutionException if any problem occurred while executing the command. Any exceptions (other than ParameterException) should be wrapped in a ExecutionException and not thrown as is.
         */
        int execute(ParseResult parseResult) throws ExecutionException, ParameterException;
    }

    /**
     * Represents a function that can handle a {@code ParameterException} that occurred while
     * {@linkplain #parse(String...) parsing} the command line arguments. This is a
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">functional interface</a>
     * whose functional method is {@link #handleException(CommandLine.ParameterException, PrintStream, CommandLine.Help.Ansi, String...)}.
     * <p>
     * Implementations of this function can be passed to the {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...) CommandLine::parseWithHandlers}
     * methods to handle situations when the command line could not be parsed.
     * </p>
     * @deprecated see {@link #execute(String...)}, {@link IParameterExceptionHandler} and {@link IExecutionExceptionHandler}
     * @since 2.0 */
    @Deprecated public static interface IExceptionHandler {
        /** Handles a {@code ParameterException} that occurred while {@linkplain #parse(String...) parsing} the command
         * line arguments and optionally returns a list of results.
         * @param ex the ParameterException describing the problem that occurred while parsing the command line arguments,
         *           and the CommandLine representing the command or subcommand whose input was invalid
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @param args the command line arguments that could not be parsed
         * @return a list of results, or an empty list if there are no results
         */
        List<Object> handleException(ParameterException ex, PrintStream out, Help.Ansi ansi, String... args);
    }
    /**
     * Classes implementing this interface know how to handle {@code ParameterExceptions} (usually from invalid user input)
     * and {@code ExecutionExceptions} that occurred while executing the {@code Runnable} or {@code Callable} command.
     * <p>
     * Implementations of this interface can be passed to the
     * {@link #parseWithHandlers(IParseResultHandler2,  IExceptionHandler2, String...) CommandLine::parseWithHandlers} method.
     * </p><p>
     * This interface replaces the {@link IParseResultHandler} interface.
     * </p>
     * @param <R> the return type of this handler
     * @see DefaultExceptionHandler
     * @deprecated see {@link #execute(String...)}, {@link IParameterExceptionHandler} and {@link IExecutionExceptionHandler}
     * @since 3.0 */
    @Deprecated public static interface IExceptionHandler2<R> {
        /** Handles a {@code ParameterException} that occurred while {@linkplain #parseArgs(String...) parsing} the command
         * line arguments and optionally returns a list of results.
         * @param ex the ParameterException describing the problem that occurred while parsing the command line arguments,
         *           and the CommandLine representing the command or subcommand whose input was invalid
         * @param args the command line arguments that could not be parsed
         * @return an object resulting from handling the exception
         */
        R handleParseException(ParameterException ex, String[] args);
        /** Handles a {@code ExecutionException} that occurred while executing the {@code Runnable} or
         * {@code Callable} command and optionally returns a list of results.
         * @param ex the ExecutionException describing the problem that occurred while executing the {@code Runnable} or
         *          {@code Callable} command, and the CommandLine representing the command or subcommand that was being executed
         * @param parseResult the result of parsing the command line arguments
         * @return an object resulting from handling the exception
         */
        R handleExecutionException(ExecutionException ex, ParseResult parseResult);
    }

    /** Classes implementing this interface know how to handle {@code ParameterExceptions} (usually from invalid user input).
     * <p><b>Implementation Requirements:</b></p>
     * <p>Implementors that need to print messages to the console should use the {@linkplain #getOut() output} and {@linkplain #getErr() error} PrintWriters,
     * and the {@linkplain #getColorScheme() color scheme} from the CommandLine object obtained from the exception.</p>
     * <p><b>Implementation Note:</b></p>
     * <p>See {@link #getParameterExceptionHandler()} for a description of the default handler.</p>
     * <p><b>API Note:</b></p>
     * <p>This interface supersedes {@link IExceptionHandler2}.</p>
     * @see CommandLine#setParameterExceptionHandler(IParameterExceptionHandler)
     * @since 4.0
     */
    public interface IParameterExceptionHandler {
        /** Handles a {@code ParameterException} that occurred while {@linkplain #parseArgs(String...) parsing} the command
         * line arguments and returns an exit code suitable for returning from {@link #execute(String...)}.
         * @param ex the ParameterException describing the problem that occurred while parsing the command line arguments,
         *           and the CommandLine representing the command or subcommand whose input was invalid
         * @param args the command line arguments that could not be parsed
         * @return an exit code
         */
        int handleParseException(ParameterException ex, String[] args) throws Exception;
    }
    /**
     * Classes implementing this interface know how to handle Exceptions that occurred while executing the {@code Runnable}, {@code Callable} or {@code Method} user object of the command.
     * <p><b>Implementation Requirements:</b></p>
     * <p>Implementors that need to print messages to the console should use the {@linkplain #getOut() output} and {@linkplain #getErr() error} PrintWriters,
     * and the {@linkplain #getColorScheme() color scheme} from the CommandLine object obtained from the exception.</p>
     * <p><b>API Note:</b></p>
     * <p>This interface supersedes {@link IExceptionHandler2}.</p>
     * @see CommandLine#setExecutionExceptionHandler(IExecutionExceptionHandler)
     * @since 4.0
     */
    public interface IExecutionExceptionHandler {
        /** Handles an {@code Exception} that occurred while executing the {@code Runnable} or
         * {@code Callable} command and returns an exit code suitable for returning from {@link #execute(String...)}.
         * @param ex the Exception thrown by the {@code Runnable}, {@code Callable} or {@code Method} user object of the command
         * @param commandLine the CommandLine representing the command or subcommand where the exception occurred
         * @param parseResult the result of parsing the command line arguments
         * @return an exit code
         */
        int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception;
    }

    /** Abstract superclass for {@link IParseResultHandler2} and {@link IExceptionHandler2} implementations.
     * <p>Note that {@code AbstractHandler} is a generic type. This, along with the abstract {@code self} method,
     * allows method chaining to work properly in subclasses, without the need for casts. An example subclass can look like this:</p>
     * <pre>{@code
     * class MyResultHandler extends AbstractHandler<MyReturnType, MyResultHandler> implements IParseResultHandler2<MyReturnType> {
     *
     *     public MyReturnType handleParseResult(ParseResult parseResult) { ... }
     *
     *     protected MyResultHandler self() { return this; }
     * }
     * }</pre>
     * @param <R> the return type of this handler
     * @param <T> The type of the handler subclass; for fluent API method chaining
     * @deprecated see {@link #execute(String...)}
     * @since 3.0 */
    @Deprecated public static abstract class AbstractHandler<R, T extends AbstractHandler<R, T>> {
        private Help.ColorScheme colorScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
        private Integer exitCode;
        private PrintStream out = System.out;
        private PrintStream err = System.err;

        /** Returns the stream to print command output to. Defaults to {@code System.out}, unless {@link #useOut(PrintStream)}
         * was called with a different stream.
         * <p>{@code IParseResultHandler2} implementations should use this stream.
         * By <a href="http://www.gnu.org/prep/standards/html_node/_002d_002dhelp.html">convention</a>, when the user requests
         * help with a {@code --help} or similar option, the usage help message is printed to the standard output stream so that it can be easily searched and paged.</p> */
        public PrintStream out()     { return out; }
        /** Returns the stream to print diagnostic messages to. Defaults to {@code System.err}, unless {@link #useErr(PrintStream)}
         * was called with a different stream. <p>{@code IExceptionHandler2} implementations should use this stream to print error
         * messages (which may include a usage help message) when an unexpected error occurs.</p> */
        public PrintStream err()     { return err; }
        /** Returns the ANSI style to use. Defaults to {@code Help.Ansi.AUTO}, unless {@link #useAnsi(CommandLine.Help.Ansi)} was called with a different setting.
         * @deprecated use {@link #colorScheme()} instead */
        @Deprecated public Help.Ansi ansi()      { return colorScheme.ansi(); }
        /** Returns the ColorScheme to use. Defaults to {@code Help#defaultColorScheme(Help.Ansi.AUTO)}.
         * @since 4.0*/
        public Help.ColorScheme colorScheme() { return colorScheme; }
        /** Returns the exit code to use as the termination status, or {@code null} (the default) if the handler should
         * not call {@link System#exit(int)} after processing completes.
         * @see #andExit(int) */
        public Integer exitCode()    { return exitCode; }
        /** Returns {@code true} if an exit code was set with {@link #andExit(int)}, or {@code false} (the default) if
         * the handler should not call {@link System#exit(int)} after processing completes. */
        public boolean hasExitCode() { return exitCode != null; }

        /** Convenience method for subclasses that returns the specified result object if no exit code was set,
         * or otherwise, if an exit code {@linkplain #andExit(int) was set}, calls {@code System.exit} with the configured
         * exit code to terminate the currently running Java virtual machine. */
        protected R returnResultOrExit(R result) {
            if (hasExitCode()) { exit(exitCode()); }
            return result;
        }

        /** Convenience method for subclasses that throws the specified ExecutionException if no exit code was set,
         * or otherwise, if an exit code {@linkplain #andExit(int) was set}, prints the stacktrace of the specified exception
         * to the diagnostic error stream and calls {@code System.exit} with the configured
         * exit code to terminate the currently running Java virtual machine. */
        protected R throwOrExit(ExecutionException ex) {
            if (hasExitCode()) {
                ex.printStackTrace(this.err());
                exit(exitCode());
                return null;
            }
            throw ex;
        }
        /** Calls {@code System.exit(int)} with the specified exit code. */
        protected void exit(int exitCode) { System.exit(exitCode); }

        /** Returns {@code this} to allow method chaining when calling the setters for a fluent API. */
        protected abstract T self();

        /** Sets the stream to print command output to.
         * @deprecated use {@link CommandLine#setOut(PrintWriter)} and {@link CommandLine#execute(String...)} instead */
        @Deprecated public T useOut(PrintStream out)   { this.out =  Assert.notNull(out, "out");   return self(); }
        /** Sets the stream to print diagnostic messages to.
         * @deprecated use {@link CommandLine#setErr(PrintWriter)} and {@link CommandLine#execute(String...)} instead */
        @Deprecated public T useErr(PrintStream err)   { this.err =  Assert.notNull(err, "err");   return self(); }
        /** Sets the ANSI style to use and resets the color scheme to the default.
         * @deprecated use {@link CommandLine#setColorScheme(Help.ColorScheme)} and {@link CommandLine#execute(String...)} instead
         * @see #ansi() */
        @Deprecated public T useAnsi(Help.Ansi ansi) { this.colorScheme = Help.defaultColorScheme(Assert.notNull(ansi, "ansi")); return self(); }
        /** Indicates that the handler should call {@link System#exit(int)} after processing completes and sets the exit code to use as the termination status.
         * @deprecated use {@link CommandLine#execute(String...)} instead, and call {@code System.exit()} in the application. */
        @Deprecated public T andExit(int exitCode)     { this.exitCode = exitCode; return self(); }
    }

    /**
     * Default exception handler that handles invalid user input by printing the exception message, followed by the usage
     * message for the command or subcommand whose input was invalid.
     * <p>{@code ParameterExceptions} (invalid user input) is handled like this:</p>
     * <pre>
     *     err().println(paramException.getMessage());
     *     paramException.getCommandLine().usage(err(), ansi());
     *     if (hasExitCode()) System.exit(exitCode()); else return returnValue;
     * </pre>
     * <p>{@code ExecutionExceptions} that occurred while executing the {@code Runnable} or {@code Callable} command are simply rethrown and not handled.</p>
     * @deprecated see {@link #execute(String...)}, {@link #getParameterExceptionHandler()} and {@link #getExecutionExceptionHandler()}
     * @since 2.0 */
    @Deprecated public static class DefaultExceptionHandler<R> extends AbstractHandler<R, DefaultExceptionHandler<R>> implements IExceptionHandler, IExceptionHandler2<R> {
        public List<Object> handleException(ParameterException ex, PrintStream out, Help.Ansi ansi, String... args) {
            internalHandleParseException(ex, new PrintWriter(out, true), Help.defaultColorScheme(ansi)); return Collections.<Object>emptyList(); }

        /** Prints the message of the specified exception, followed by the usage message for the command or subcommand
         * whose input was invalid, to the stream returned by {@link #err()}.
         * @param ex the ParameterException describing the problem that occurred while parsing the command line arguments,
         *           and the CommandLine representing the command or subcommand whose input was invalid
         * @param args the command line arguments that could not be parsed
         * @return the empty list
         * @since 3.0 */
        public R handleParseException(ParameterException ex, String[] args) {
            internalHandleParseException(ex, new PrintWriter(err(), true), colorScheme()); return returnResultOrExit(null); }

        static void internalHandleParseException(ParameterException ex, PrintWriter writer, Help.ColorScheme colorScheme) {
            writer.println(ex.getMessage());
            if (!UnmatchedArgumentException.printSuggestions(ex, writer)) {
                ex.getCommandLine().usage(writer, colorScheme);
            }
        }
        /** This implementation always simply rethrows the specified exception.
         * @param ex the ExecutionException describing the problem that occurred while executing the {@code Runnable} or {@code Callable} command
         * @param parseResult the result of parsing the command line arguments
         * @return nothing: this method always rethrows the specified exception
         * @throws ExecutionException always rethrows the specified exception
         * @since 3.0 */
        public R handleExecutionException(ExecutionException ex, ParseResult parseResult) { return throwOrExit(ex); }

        @Override protected DefaultExceptionHandler<R> self() { return this; }
    }
    /** Convenience method that returns {@code new DefaultExceptionHandler<List<Object>>()}. */
    public static DefaultExceptionHandler<List<Object>> defaultExceptionHandler() { return new DefaultExceptionHandler<List<Object>>(); }

    /** @deprecated use {@link #printHelpIfRequested(ParseResult)} instead
     * @since 2.0 */
    @Deprecated public static boolean printHelpIfRequested(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
        return printHelpIfRequested(parsedCommands, out, out, ansi);
    }
    /**
     * Delegates to {@link #executeHelpRequest(ParseResult)}.
     * @param parseResult contains the {@code CommandLine} objects found during parsing; check these to see if help was requested
     * @return {@code true} if help was printed, {@code false} otherwise
     * @since 3.0 */
    public static boolean printHelpIfRequested(ParseResult parseResult) {
        return executeHelpRequest(parseResult) != null;
    }
    /**
     * Delegates to the implementation of {@link #executeHelpRequest(ParseResult)}.
     * @deprecated use {@link #executeHelpRequest(ParseResult)} instead
     * @param parsedCommands the list of {@code CommandLine} objects to check if help was requested
     * @param out the {@code PrintStream} to print help to if requested
     * @param err the error string to print diagnostic messages to, in addition to the output from the exception handler
     * @param ansi for printing help messages using ANSI styles and colors
     * @return {@code true} if help was printed, {@code false} otherwise
     * @since 3.0 */
    @Deprecated public static boolean printHelpIfRequested(List<CommandLine> parsedCommands, PrintStream out, PrintStream err, Help.Ansi ansi) {
        return printHelpIfRequested(parsedCommands, out, err, Help.defaultColorScheme(ansi));
    }
    /**
     * Delegates to the implementation of {@link #executeHelpRequest(ParseResult)}.
     * @deprecated use {@link #executeHelpRequest(ParseResult)} instead
     * @param parsedCommands the list of {@code CommandLine} objects to check if help was requested
     * @param out the {@code PrintStream} to print help to if requested
     * @param err the error string to print diagnostic messages to, in addition to the output from the exception handler
     * @param colorScheme for printing help messages using ANSI styles and colors
     * @return {@code true} if help was printed, {@code false} otherwise
     * @since 3.6 */
    @Deprecated public static boolean printHelpIfRequested(List<CommandLine> parsedCommands, PrintStream out, PrintStream err, Help.ColorScheme colorScheme) {
        // for backwards compatibility
        for (CommandLine cmd : parsedCommands) { cmd.setOut(new PrintWriter(out, true)).setErr(new PrintWriter(err, true)).setColorScheme(colorScheme); }
        return executeHelpRequest(parsedCommands) != null;
    }

    /**
     * Helper method that may be useful when processing the {@code ParseResult} that results from successfully
     * {@linkplain #parseArgs(String...) parsing} command line arguments. This method prints out
     * {@linkplain #usage(PrintWriter, Help.ColorScheme) usage help} to the {@linkplain CommandLine#getOut() configured output writer}
     * if {@linkplain #isUsageHelpRequested() requested} or {@linkplain #printVersionHelp(PrintWriter, Help.Ansi, Object...) version help}
     * to the {@linkplain CommandLine#getOut() configured output writer} if {@linkplain #isVersionHelpRequested() requested}
     * and returns {@link CommandSpec#exitCodeOnUsageHelp()} or {@link CommandSpec#exitCodeOnVersionHelp()}, respectively.
     * If the command is a {@link Command#helpCommand()} and {@code runnable} or {@code callable},
     * that command is executed and this method returns {@link CommandSpec#exitCodeOnUsageHelp()}.
     * Otherwise, if none of the specified {@code CommandLine} objects have help requested,
     * this method returns {@code null}.<p>
     * Note that this method <em>only</em> looks at the {@link Option#usageHelp() usageHelp} and
     * {@link Option#versionHelp() versionHelp} attributes. The {@link Option#help() help} attribute is ignored.
     * </p><p><b>Implementation note:</b></p><p>
     * When an error occurs while processing the help request, it is recommended custom Help commands throw a
     * {@link ParameterException} with a reference to the parent command. This will print the error message and the
     * usage for the parent command, and will use the exit code of the exception handler if one was set.
     * </p>
     * @param parseResult contains the {@code CommandLine} objects found during parsing; check these to see if help was requested
     * @return {@link CommandSpec#exitCodeOnUsageHelp()} if usage help was requested,
     *      {@link CommandSpec#exitCodeOnVersionHelp()} if version help was requested, and {@code null} otherwise
     * @see IHelpCommandInitializable2
     * @since 4.0 */
    public static Integer executeHelpRequest(ParseResult parseResult) {
        return executeHelpRequest(parseResult.asCommandLineList());
    }
    /** @since 4.0 */
    static Integer executeHelpRequest(List<CommandLine> parsedCommands) {
        for (int i = 0; i < parsedCommands.size(); i++) {
            CommandLine parsed = parsedCommands.get(i);
            Help.ColorScheme colorScheme = parsed.getColorScheme();
            PrintWriter out = parsed.getOut();
            if (parsed.isUsageHelpRequested()) {
                parsed.usage(out, colorScheme);
                return parsed.getCommandSpec().exitCodeOnUsageHelp();
            } else if (parsed.isVersionHelpRequested()) {
                parsed.printVersionHelp(out, colorScheme.ansi);
                return parsed.getCommandSpec().exitCodeOnVersionHelp();
            } else if (parsed.getCommandSpec().helpCommand()) {
                PrintWriter err = parsed.getErr();
                if (parsed.getCommand() instanceof IHelpCommandInitializable2) {
                    ((IHelpCommandInitializable2) parsed.getCommand()).init(parsed, colorScheme, out, err);
                } else if (parsed.getCommand() instanceof IHelpCommandInitializable) {
                    ((IHelpCommandInitializable) parsed.getCommand()).init(parsed, colorScheme.ansi, System.out, System.err);
                }
                executeUserObject(parsed, new ArrayList<Object>());
                return parsed.getCommandSpec().exitCodeOnUsageHelp();
            }
        }
        return null;
    }
    private static List<Object> executeUserObject(CommandLine parsed, List<Object> executionResultList) {
        Object command = parsed.getCommand();
        if (command instanceof Runnable) {
            try {
                ((Runnable) command).run();
                parsed.setExecutionResult(null); // 4.0
                executionResultList.add(null); // for compatibility with picocli 2.x
                return executionResultList;
            } catch (ParameterException ex) {
                throw ex;
            } catch (ExecutionException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ExecutionException(parsed, "Error while running command (" + command + "): " + ex, ex);
            }
        } else if (command instanceof Callable) {
            try {
                @SuppressWarnings("unchecked") Callable<Object> callable = (Callable<Object>) command;
                Object executionResult = callable.call();
                parsed.setExecutionResult(executionResult);
                executionResultList.add(executionResult);
                return executionResultList;
            } catch (ParameterException ex) {
                throw ex;
            } catch (ExecutionException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ExecutionException(parsed, "Error while calling command (" + command + "): " + ex, ex);
            }
        } else if (command instanceof Method) {
            try {
                Method method = (Method) command;
                Object[] parsedArgs = parsed.getCommandSpec().argValues();
                Object executionResult;
                if (Modifier.isStatic(method.getModifiers())) {
                    executionResult = method.invoke(null, parsedArgs); // invoke static method
                } else if (parsed.getCommandSpec().parent() != null) {
                    executionResult = method.invoke(parsed.getCommandSpec().parent().userObject(), parsedArgs);
                } else {
                    executionResult = method.invoke(parsed.factory.create(method.getDeclaringClass()), parsedArgs);
                }
                parsed.setExecutionResult(executionResult);
                executionResultList.add(executionResult);
                return executionResultList;
            } catch (InvocationTargetException ex) {
                Throwable t = ex.getTargetException();
                if (t instanceof ParameterException) {
                    throw (ParameterException) t;
                } else if (t instanceof ExecutionException) {
                    throw (ExecutionException) t;
                } else {
                    throw new ExecutionException(parsed, "Error while calling command (" + command + "): " + t, t);
                }
            } catch (Exception ex) {
                throw new ExecutionException(parsed, "Unhandled error while calling command (" + command + "): " + ex, ex);
            }
        }
        throw new ExecutionException(parsed, "Parsed command (" + command + ") is not a Method, Runnable or Callable");
    }

    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * To use this method, the annotated object that this {@code CommandLine} is constructed with needs to
     * either implement {@link Runnable}, {@link Callable}, or be a {@code Method} object.
     * See {@link #getCommandMethods(Class, String) getCommandMethods} for a convenient way to obtain a command {@code Method}.
     * <p>This method replaces the {@link #run(Runnable, String...) run}, {@link #call(Callable, String...) call}
     * and {@link #invoke(String, Class, String...) invoke} convenience methods that were available with previous versions of picocli.
     * </p><p>
     * <b>Exit Code</b>
     * </p><p>
     * This method returns an exit code that applications can use to call {@code System.exit}.
     * (The return value of the {@code Callable} or {@code Method} can still be obtained via {@link #getExecutionResult() getExecutionResult}.)
     * If the user object {@code Callable} or {@code Method} returns an {@code int} or {@code Integer},
     * this will be used as the exit code. Additionally, if the user object implements {@link CommandLine.IExitCodeGenerator IExitCodeGenerator},
     * an exit code is obtained by calling its {@code getExitCode()} method (after invoking the user object).
     * </p><p>
     * In the case of multiple exit codes the highest value will be used (or if all values are negative, the lowest value will be used).
     * </p><p>
     * <b>Exception Handling</b>
     * </p><p>
     * This method never throws an exception.
     * </p><p>
     * If the user specified invalid input, the {@linkplain #getParameterExceptionHandler() parameter exception handler} is invoked.
     * By default this prints an error message and the usage help message, and returns an exit code.
     * </p><p>
     * If an exception occurred while the user object {@code Runnable}, {@code Callable}, or {@code Method}
     * was invoked, this exception is caught and passed to the {@linkplain #getExecutionExceptionHandler() execution exception handler}.
     * The default {@code IExecutionExceptionHandler} will rethrow this Exception.
     * </p><p>
     * Any exception thrown from the {@code IParameterExceptionHandler} or {@code IExecutionExceptionHandler} is caught,
     * it stacktrace is printed and is mapped to an exit code, using the following logic:
     * </p><p>
     * If an {@link CommandLine.IExitCodeExceptionMapper IExitCodeExceptionMapper} is {@linkplain #setExitCodeExceptionMapper(IExitCodeExceptionMapper) configured},
     * this mapper is used to determine the exit code based on the exception.
     * </p><p>
     * If an {@code IExitCodeExceptionMapper} is not set, by default this method will return the {@code @Command} annotation's
     * {@link Command#exitCodeOnInvalidInput() exitCodeOnInvalidInput} or {@link Command#exitCodeOnExecutionException() exitCodeOnExecutionException} value, respectively.
     * </p><p><b>Example Usage:</b></p>
     * <pre>
     * &#064Command
     * class MyCommand implements Callable&lt;Integer&gt; {
     *     public Integer call() { return 123; }
     * }
     * CommandLine cmd = new CommandLine(new MyCommand());
     * int exitCode = cmd.execute(args);
     * assert exitCode == 123;
     * System.exit(exitCode);
     * </pre>
     * <p>Since {@code execute} is an instance method, not a static method, applications can do configuration before invoking the command. For example:</p>
     * <pre>{@code
     * CommandLine cmd = new CommandLine(new MyCallable())
     *         .setCaseInsensitiveEnumValuesAllowed(true) // configure a non-default parser option
     *         .setOut(myOutWriter()) // configure an alternative to System.out
     *         .setErr(myErrWriter()) // configure an alternative to System.err
     *         .setColorScheme(myColorScheme()); // configure a custom color scheme
     * int exitCode = cmd.execute(args);
     * System.exit(exitCode);
     * }</pre>
     * <p>
     * If the specified command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed. This can be configured by setting the {@linkplain #setExecutionStrategy(IExecutionStrategy) execution strategy}.
     * Built-in alternatives are executing the {@linkplain RunFirst first} subcommand, or executing {@linkplain RunAll all} specified subcommands.
     * </p>
     * @param args the command line arguments to parse
     * @return the exit code
     * @see ExitCode
     * @see IExitCodeGenerator
     * @see #getExecutionResult()
     * @see #getExecutionStrategy()
     * @see #getParameterExceptionHandler()
     * @see #getExecutionExceptionHandler()
     * @see #getExitCodeExceptionMapper()
     * @since 4.0
     */
    public int execute(String... args) {
        ParseResult[] parseResult = new ParseResult[1];
        clearExecutionResults();
        try {
            parseResult[0] = parseArgs(args);
            return enrichForBackwardsCompatibility(getExecutionStrategy()).execute(parseResult[0]);
        } catch (ParameterException ex) {
            try {
                return getParameterExceptionHandler().handleParseException(ex, args);
            } catch (Exception ex2) {
                return handleUnhandled(ex2, ex.getCommandLine(), ex.getCommandLine().getCommandSpec().exitCodeOnInvalidInput());
            }
        } catch (ExecutionException ex) {
            try {
                @SuppressWarnings("unchecked")
                Exception cause = ex.getCause() instanceof Exception ? (Exception) ex.getCause() : ex;
                return getExecutionExceptionHandler().handleExecutionException(cause, ex.getCommandLine(), parseResult[0]);
            } catch (Exception ex2) {
                return handleUnhandled(ex2, ex.getCommandLine(), ex.getCommandLine().getCommandSpec().exitCodeOnExecutionException());
            }
        } catch (Exception ex) {
            return handleUnhandled(ex, this, getCommandSpec().exitCodeOnExecutionException());
        }
    }
    private static int handleUnhandled(Exception ex, CommandLine cmd, int defaultExitCode) {
        ex.printStackTrace(cmd.getErr());
        return mappedExitCode(ex, cmd.getExitCodeExceptionMapper(), defaultExitCode);
    }

    private <T> T enrichForBackwardsCompatibility(T obj) {
        // in case the IExecutionStrategy is a built-in like RunLast,
        // and the application called #useOut, #useErr or #useAnsi on it
        if (obj instanceof AbstractHandler<?, ?>) {
            AbstractHandler<?, ?> handler = (AbstractHandler<?, ?>) obj;
            if (handler.out()  != System.out)     { setOut(new PrintWriter(handler.out(), true)); }
            if (handler.err()  != System.err)     { setErr(new PrintWriter(handler.err(), true)); }
            if (handler.ansi() != Help.Ansi.AUTO) { setColorScheme(handler.colorScheme()); }
        }
        return obj;
    }
    /** Command line parse result handler that returns a value. This handler prints help if requested, and otherwise calls
     * {@link #handle(CommandLine.ParseResult)} with the parse result. Facilitates implementation of the {@link IParseResultHandler2} interface.
     * <p>Note that {@code AbstractParseResultHandler} is a generic type. This, along with the abstract {@code self} method,
     * allows method chaining to work properly in subclasses, without the need for casts. An example subclass can look like this:</p>
     * <pre>{@code
     * class MyResultHandler extends AbstractParseResultHandler<MyReturnType> {
     *
     *     protected MyReturnType handle(ParseResult parseResult) throws ExecutionException { ... }
     *
     *     protected MyResultHandler self() { return this; }
     * }
     * }</pre>
     * @deprecated see {@link #execute(String...)}, {@link #getExecutionStrategy()}, {@link #getParameterExceptionHandler()}, {@link #getExecutionExceptionHandler()}
     * @since 3.0 */
    @Deprecated public abstract static class AbstractParseResultHandler<R> extends AbstractHandler<R, AbstractParseResultHandler<R>> implements IParseResultHandler2<R>, IExecutionStrategy {
        /** Prints help if requested, and otherwise calls {@link #handle(CommandLine.ParseResult)}.
         * Finally, either a list of result objects is returned, or the JVM is terminated if an exit code {@linkplain #andExit(int) was set}.
         *
         * @param parseResult the {@code ParseResult} that resulted from successfully parsing the command line arguments
         * @return the result of {@link #handle(CommandLine.ParseResult) processing parse results}
         * @throws ParameterException if the {@link HelpCommand HelpCommand} was invoked for an unknown subcommand. Any {@code ParameterExceptions}
         *      thrown from this method are treated as if this exception was thrown during parsing and passed to the {@link IExceptionHandler2}
         * @throws ExecutionException if a problem occurred while processing the parse results; client code can use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public R handleParseResult(ParseResult parseResult) throws ExecutionException {
            if (printHelpIfRequested(parseResult.asCommandLineList(), out(), err(), colorScheme())) {
                return returnResultOrExit(null);
            }
            return returnResultOrExit(handle(parseResult));
        }

        public int execute(ParseResult parseResult) throws ExecutionException {
            Integer helpExitCode = executeHelpRequest(parseResult);
            if (helpExitCode != null) { return helpExitCode; }

            R executionResult = handle(parseResult);
            List<IExitCodeGenerator> exitCodeGenerators = extractExitCodeGenerators(parseResult);
            return resolveExitCode(parseResult.commandSpec().exitCodeOnSuccess(), executionResult, exitCodeGenerators);
        }

        // Use the highest value (or if all values are negative, use the lowest value).
        private int resolveExitCode(int exitCodeOnSuccess, R executionResult, List<IExitCodeGenerator> exitCodeGenerators) {
            int result = 0;
            for (IExitCodeGenerator generator : exitCodeGenerators) {
                try {
                    int exitCode = generator.getExitCode();
                    if ((exitCode > 0 && exitCode > result) || (exitCode < 0 && result <= 0 && exitCode < result)) {
                        result = exitCode;
                    }
                } catch (Exception ex) {
                    result = (result == 0) ? 1 : result;
                    ex.printStackTrace();
                }
            }
            if (executionResult instanceof List) {
                List<?> resultList = (List<?>) executionResult;
                for (Object obj : resultList) {
                    if (obj instanceof Integer) {
                        Integer exitCode = (Integer) obj;
                        if ((exitCode > 0 && exitCode > result) || (exitCode < 0 && result <= 0 && exitCode < result)) {
                            result = exitCode;
                        }
                    }
                }
            }
            return result == 0 ? exitCodeOnSuccess : result;
        }

        /** Processes the specified {@code ParseResult} and returns the result as a list of objects.
         * Implementations are responsible for catching any exceptions thrown in the {@code handle} method, and
         * rethrowing an {@code ExecutionException} that details the problem and captures the offending {@code CommandLine} object.
         *
         * @param parseResult the {@code ParseResult} that resulted from successfully parsing the command line arguments
         * @return the result of processing parse results
         * @throws ExecutionException if a problem occurred while processing the parse results; client code can use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        protected abstract R handle(ParseResult parseResult) throws ExecutionException;

        protected List<IExitCodeGenerator> extractExitCodeGenerators(ParseResult parseResult) { return Collections.emptyList(); }
    }
    /**
     * Command line {@linkplain IExecutionStrategy execution strategy} that prints help if requested, and otherwise executes the top-level
     * {@code Runnable} or {@code Callable} command.
     * For use by the {@link #execute(String...) execute} method.
     * @since 2.0 */
    @SuppressWarnings("deprecation")
    public static class RunFirst extends AbstractParseResultHandler<List<Object>> implements IParseResultHandler {
        /** Prints help if requested, and otherwise executes the top-level {@code Runnable} or {@code Callable} command.
         * Finally, either a list of result objects is returned, or the JVM is terminated if an exit code {@linkplain #andExit(int) was set}.
         * If the top-level command does not implement either {@code Runnable} or {@code Callable}, an {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return an empty list if help was requested, or a list containing a single element: the result of calling the
         *      {@code Callable}, or a {@code null} element if the top-level command was a {@code Runnable}
         * @throws ParameterException if the {@link HelpCommand HelpCommand} was invoked for an unknown subcommand. Any {@code ParameterExceptions}
         *      thrown from this method are treated as if this exception was thrown during parsing and passed to the {@link IExceptionHandler}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
            if (printHelpIfRequested(parsedCommands, out, err(), ansi)) { return returnResultOrExit(Collections.emptyList()); }
            return returnResultOrExit(executeUserObject(parsedCommands.get(0), new ArrayList<Object>()));
        }
        /** Executes the top-level {@code Runnable} or {@code Callable} subcommand.
         * If the top-level command does not implement either {@code Runnable} or {@code Callable} and is not a {@code Method}, an {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parseResult the {@code ParseResult} that resulted from successfully parsing the command line arguments
         * @return an empty list if help was requested, or a list containing a single element: the result of calling the
         *      {@code Callable}, or a {@code null} element if the last (sub)command was a {@code Runnable}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         * @since 3.0 */
        protected List<Object> handle(ParseResult parseResult) throws ExecutionException {
            return executeUserObject(parseResult.commandSpec().commandLine(), new ArrayList<Object>()); // first
        }

        protected List<IExitCodeGenerator> extractExitCodeGenerators(ParseResult parseResult) {
            if (parseResult.commandSpec().userObject() instanceof IExitCodeGenerator) {
                return Arrays.asList((IExitCodeGenerator) parseResult.commandSpec().userObject());
            }
            return Collections.emptyList();
        }

        @Override protected RunFirst self() { return this; }
    }
    /**
     * Command line {@linkplain IExecutionStrategy execution strategy} that prints help if requested, and otherwise executes the most specific
     * {@code Runnable} or {@code Callable} subcommand.
     * For use by the {@link #execute(String...) execute} method.
     * <p>
     * Something like this:</p>
     * <pre>{@code
     *     // RunLast implementation: print help if requested, otherwise execute the most specific subcommand
     *     List<CommandLine> parsedCommands = parseResult.asCommandLineList();
     *     if (CommandLine.printHelpIfRequested(parsedCommands, out(), err(), ansi())) {
     *         return emptyList();
     *     }
     *     CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
     *     Object command = last.getCommand();
     *     Object result = null;
     *     if (command instanceof Runnable) {
     *         try {
     *             ((Runnable) command).run();
     *         } catch (Exception ex) {
     *             throw new ExecutionException(last, "Error in runnable " + command, ex);
     *         }
     *     } else if (command instanceof Callable) {
     *         try {
     *             result = ((Callable) command).call();
     *         } catch (Exception ex) {
     *             throw new ExecutionException(last, "Error in callable " + command, ex);
     *         }
     *     } else {
     *         throw new ExecutionException(last, "Parsed command (" + command + ") is not Runnable or Callable");
     *     }
     *     last.setExecutionResult(result);
     *     return Arrays.asList(result);
     * }</pre>
     * <p>
     * From picocli v2.0, {@code RunLast} is used to implement the {@link #run(Runnable, PrintStream, PrintStream, Help.Ansi, String...) run}
     * and {@link #call(Callable, PrintStream, PrintStream, Help.Ansi, String...) call} convenience methods.
     * </p>
     * @since 2.0 */
    @SuppressWarnings("deprecation")
    public static class RunLast extends AbstractParseResultHandler<List<Object>> implements IParseResultHandler {
        /** Prints help if requested, and otherwise executes the most specific {@code Runnable} or {@code Callable} subcommand.
         * Finally, either a list of result objects is returned, or the JVM is terminated if an exit code {@linkplain #andExit(int) was set}.
         * If the last (sub)command does not implement either {@code Runnable} or {@code Callable}, an {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return an empty list if help was requested, or a list containing a single element: the result of calling the
         *      {@code Callable}, or a {@code null} element if the last (sub)command was a {@code Runnable}
         * @throws ParameterException if the {@link HelpCommand HelpCommand} was invoked for an unknown subcommand. Any {@code ParameterExceptions}
         *      thrown from this method are treated as if this exception was thrown during parsing and passed to the {@link IExceptionHandler}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
            if (printHelpIfRequested(parsedCommands, out, err(), ansi)) { return returnResultOrExit(Collections.emptyList()); }
            return returnResultOrExit(executeUserObject(parsedCommands.get(parsedCommands.size() - 1), new ArrayList<Object>()));
        }
        /** Executes the most specific {@code Runnable} or {@code Callable} subcommand.
         * If the last (sub)command does not implement either {@code Runnable} or {@code Callable} and is not a {@code Method}, an {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parseResult the {@code ParseResult} that resulted from successfully parsing the command line arguments
         * @return an empty list if help was requested, or a list containing a single element: the result of calling the
         *      {@code Callable}, or a {@code null} element if the last (sub)command was a {@code Runnable}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         * @since 3.0 */
        protected List<Object> handle(ParseResult parseResult) throws ExecutionException {
            List<CommandLine> parsedCommands = parseResult.asCommandLineList();
            return executeUserObject(parsedCommands.get(parsedCommands.size() - 1), new ArrayList<Object>());
        }
        protected List<IExitCodeGenerator> extractExitCodeGenerators(ParseResult parseResult) {
            List<CommandLine> parsedCommands = parseResult.asCommandLineList();
            Object userObject = parsedCommands.get(parsedCommands.size() - 1).getCommandSpec().userObject();
            if (userObject instanceof IExitCodeGenerator) { return Arrays.asList((IExitCodeGenerator) userObject); }
            return Collections.emptyList();
        }
        @Override protected RunLast self() { return this; }
    }
    /**
     * Command line {@linkplain IExecutionStrategy execution strategy} that prints help if requested, and otherwise executes the top-level command and
     * all subcommands as {@code Runnable}, {@code Callable} or {@code Method}.
     * For use by the {@link #execute(String...) execute} method.
     * @since 2.0 */
    @SuppressWarnings("deprecation")
    public static class RunAll extends AbstractParseResultHandler<List<Object>> implements IParseResultHandler {
        /** Prints help if requested, and otherwise executes the top-level command and all subcommands as {@code Runnable},
         * {@code Callable} or {@code Method}. Finally, either a list of result objects is returned, or the JVM is terminated if an exit
         * code {@linkplain #andExit(int) was set}. If any of the {@code CommandLine} commands does not implement either
         * {@code Runnable} or {@code Callable}, an {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return an empty list if help was requested, or a list containing the result of executing all commands:
         *      the return values from calling the {@code Callable} commands, {@code null} elements for commands that implement {@code Runnable}
         * @throws ParameterException if the {@link HelpCommand HelpCommand} was invoked for an unknown subcommand. Any {@code ParameterExceptions}
         *      thrown from this method are treated as if this exception was thrown during parsing and passed to the {@link IExceptionHandler}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
            if (printHelpIfRequested(parsedCommands, out, err(), ansi)) { return returnResultOrExit(Collections.emptyList()); }
            List<Object> result = new ArrayList<Object>();
            for (CommandLine parsed : parsedCommands) {
                executeUserObject(parsed, result);
            }
            return returnResultOrExit(result);
        }
        /** Executes the top-level command and all subcommands as {@code Runnable} or {@code Callable}.
         * If any of the {@code CommandLine} commands does not implement either {@code Runnable} or {@code Callable} and is not a {@code Method}, an {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parseResult the {@code ParseResult} that resulted from successfully parsing the command line arguments
         * @return an empty list if help was requested, or a list containing the result of executing all commands:
         *      the return values from calling the {@code Callable} commands, {@code null} elements for commands that implement {@code Runnable}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         * @since 3.0 */
        protected List<Object> handle(ParseResult parseResult) throws ExecutionException {
            List<Object> result = new ArrayList<Object>();
            executeUserObject(parseResult.commandSpec().commandLine(), result);
            while (parseResult.hasSubcommand()) {
                parseResult = parseResult.subcommand();
                executeUserObject(parseResult.commandSpec().commandLine(), result);
            }
            return returnResultOrExit(result);
        }
        protected List<IExitCodeGenerator> extractExitCodeGenerators(ParseResult parseResult) {
            List<IExitCodeGenerator> result = new ArrayList<IExitCodeGenerator>();
            if (parseResult.commandSpec().userObject() instanceof IExitCodeGenerator) { result.add((IExitCodeGenerator) parseResult.commandSpec().userObject()); }
            while (parseResult.hasSubcommand()) {
                parseResult = parseResult.subcommand();
                if (parseResult.commandSpec().userObject() instanceof IExitCodeGenerator) { result.add((IExitCodeGenerator) parseResult.commandSpec().userObject()); }
            }
            return result;
        }
        @Override protected RunAll self() { return this; }
    }

    /**
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 2.0 */
    @Deprecated public List<Object> parseWithHandler(IParseResultHandler handler, PrintStream out, String... args) {
        return parseWithHandlers(handler, out, Help.Ansi.AUTO, defaultExceptionHandler(), args);
    }
    /**
     * Returns the result of calling {@link #parseWithHandlers(IParseResultHandler2,  IExceptionHandler2, String...)} with
     * a new {@link DefaultExceptionHandler} in addition to the specified parse result handler and the specified command line arguments.
     * <p>
     * This is a convenience method intended to offer the same ease of use as the {@link #run(Runnable, PrintStream, PrintStream, Help.Ansi, String...) run}
     * and {@link #call(Callable, PrintStream, PrintStream, Help.Ansi, String...) call} methods, but with more flexibility and better
     * support for nested subcommands.
     * </p>
     * <p>Calling this method roughly expands to:</p>
     * <pre>{@code
     * try {
     *     ParseResult parseResult = parseArgs(args);
     *     return handler.handleParseResult(parseResult);
     * } catch (ParameterException ex) {
     *     return new DefaultExceptionHandler<R>().handleParseException(ex, args);
     * }
     * }</pre>
     * <p>
     * Picocli provides some default handlers that allow you to accomplish some common tasks with very little code.
     * The following handlers are available:</p>
     * <ul>
     *   <li>{@link RunLast} handler prints help if requested, and otherwise gets the last specified command or subcommand
     * and tries to execute it as a {@code Runnable} or {@code Callable}.</li>
     *   <li>{@link RunFirst} handler prints help if requested, and otherwise executes the top-level command as a {@code Runnable} or {@code Callable}.</li>
     *   <li>{@link RunAll} handler prints help if requested, and otherwise executes all recognized commands and subcommands as {@code Runnable} or {@code Callable} tasks.</li>
     *   <li>{@link DefaultExceptionHandler} prints the error message followed by usage help</li>
     * </ul>
     * @param <R> the return type of this handler
     * @param handler the function that will handle the result of successfully parsing the command line arguments
     * @param args the command line arguments
     * @return an object resulting from handling the parse result or the exception that occurred while parsing the input
     * @throws ExecutionException if the command line arguments were parsed successfully but a problem occurred while processing the
     *      parse results; use {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
     * @see RunLast
     * @see RunAll
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.0 */
    @Deprecated public <R> R parseWithHandler(IParseResultHandler2<R> handler, String[] args) {
        return parseWithHandlers(handler, new DefaultExceptionHandler<R>(), args);
    }

    /**
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 2.0 */
    @Deprecated public List<Object> parseWithHandlers(IParseResultHandler handler, PrintStream out, Help.Ansi ansi, IExceptionHandler exceptionHandler, String... args) {
        clearExecutionResults();
        try {
            List<CommandLine> result = parse(args);
            return handler.handleParseResult(result, out, ansi);
        } catch (ParameterException ex) {
            return exceptionHandler.handleException(ex, out, ansi, args);
        }
    }
    /**
     * Tries to {@linkplain #parseArgs(String...) parse} the specified command line arguments, and if successful, delegates
     * the processing of the resulting {@code ParseResult} object to the specified {@linkplain IParseResultHandler2 handler}.
     * If the command line arguments were invalid, the {@code ParameterException} thrown from the {@code parse} method
     * is caught and passed to the specified {@link IExceptionHandler2}.
     * <p>
     * This is a convenience method intended to offer the same ease of use as the {@link #run(Runnable, PrintStream, PrintStream, Help.Ansi, String...) run}
     * and {@link #call(Callable, PrintStream, PrintStream, Help.Ansi, String...) call} methods, but with more flexibility and better
     * support for nested subcommands.
     * </p>
     * <p>Calling this method roughly expands to:</p>
     * <pre>
     * ParseResult parseResult = null;
     * try {
     *     parseResult = parseArgs(args);
     *     return handler.handleParseResult(parseResult);
     * } catch (ParameterException ex) {
     *     return exceptionHandler.handleParseException(ex, (String[]) args);
     * } catch (ExecutionException ex) {
     *     return exceptionHandler.handleExecutionException(ex, parseResult);
     * }
     * </pre>
     * <p>
     * Picocli provides some default handlers that allow you to accomplish some common tasks with very little code.
     * The following handlers are available:</p>
     * <ul>
     *   <li>{@link RunLast} handler prints help if requested, and otherwise gets the last specified command or subcommand
     * and tries to execute it as a {@code Runnable} or {@code Callable}.</li>
     *   <li>{@link RunFirst} handler prints help if requested, and otherwise executes the top-level command as a {@code Runnable} or {@code Callable}.</li>
     *   <li>{@link RunAll} handler prints help if requested, and otherwise executes all recognized commands and subcommands as {@code Runnable} or {@code Callable} tasks.</li>
     *   <li>{@link DefaultExceptionHandler} prints the error message followed by usage help</li>
     * </ul>
     *
     * @param handler the function that will handle the result of successfully parsing the command line arguments
     * @param exceptionHandler the function that can handle the {@code ParameterException} thrown when the command line arguments are invalid
     * @param args the command line arguments
     * @return an object resulting from handling the parse result or the exception that occurred while parsing the input
     * @throws ExecutionException if the command line arguments were parsed successfully but a problem occurred while processing the parse
     *      result {@code ParseResult} object; use {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
     * @param <R> the return type of the result handler and exception handler
     * @see RunLast
     * @see RunAll
     * @see DefaultExceptionHandler
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.0 */
    @Deprecated public <R> R parseWithHandlers(IParseResultHandler2<R> handler, IExceptionHandler2<R> exceptionHandler, String... args) {
        clearExecutionResults();
        ParseResult parseResult = null;
        try {
            parseResult = parseArgs(args);
            return handler.handleParseResult(parseResult);
        } catch (ParameterException ex) {
            return exceptionHandler.handleParseException(ex, args);
        } catch (ExecutionException ex) {
            return exceptionHandler.handleExecutionException(ex, parseResult);
        }
    }
    static String versionString() {
        return String.format("%s, JVM: %s (%s %s %s), OS: %s %s %s", VERSION,
                System.getProperty("java.version"), System.getProperty("java.vendor"), System.getProperty("java.vm.name"), System.getProperty("java.vm.version"),
                System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
    }
    /**
     * Equivalent to {@code new CommandLine(command).usage(out)}. See {@link #usage(PrintStream)} for details.
     * @param command the object annotated with {@link Command}, {@link Option} and {@link Parameters}
     * @param out the print stream to print the help message to
     * @throws IllegalArgumentException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     */
    public static void usage(Object command, PrintStream out) {
        toCommandLine(command, new DefaultFactory()).usage(out);
    }

    /**
     * Equivalent to {@code new CommandLine(command).usage(out, ansi)}.
     * See {@link #usage(PrintStream, Help.Ansi)} for details.
     * @param command the object annotated with {@link Command}, {@link Option} and {@link Parameters}
     * @param out the print stream to print the help message to
     * @param ansi whether the usage message should contain ANSI escape codes or not
     * @throws IllegalArgumentException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     */
    public static void usage(Object command, PrintStream out, Help.Ansi ansi) {
        toCommandLine(command, new DefaultFactory()).usage(out, ansi);
    }

    /**
     * Equivalent to {@code new CommandLine(command).usage(out, colorScheme)}.
     * See {@link #usage(PrintStream, Help.ColorScheme)} for details.
     * @param command the object annotated with {@link Command}, {@link Option} and {@link Parameters}
     * @param out the print stream to print the help message to
     * @param colorScheme the {@code ColorScheme} defining the styles for options, parameters and commands when ANSI is enabled
     * @throws IllegalArgumentException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     */
    public static void usage(Object command, PrintStream out, Help.ColorScheme colorScheme) {
        toCommandLine(command, new DefaultFactory()).usage(out, colorScheme);
    }

    /**
     * Delegates to {@link #usage(PrintStream, Help.ColorScheme)} with the {@linkplain #getColorScheme() configured} color scheme.
     * @param out the printStream to print to
     * @see #usage(PrintStream, Help.ColorScheme)
     */
    public void usage(PrintStream out) { usage(out, getColorScheme()); }
    /**
     * Delegates to {@link #usage(PrintWriter, Help.ColorScheme)} with the {@linkplain #getColorScheme() configured} color scheme.
     * @param writer the PrintWriter to print to
     * @see #usage(PrintWriter, Help.ColorScheme)
     * @since 3.0 */
    public void usage(PrintWriter writer) { usage(writer, getColorScheme()); }

    /**
     * Delegates to {@link #usage(PrintStream, Help.ColorScheme)} with the {@linkplain Help#defaultColorScheme(CommandLine.Help.Ansi) default color scheme}.
     * @param out the printStream to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @see #usage(PrintStream, Help.ColorScheme)
     */
    public void usage(PrintStream out, Help.Ansi ansi) { usage(out, Help.defaultColorScheme(ansi)); }
    /** Similar to {@link #usage(PrintStream, Help.Ansi)} but with the specified {@code PrintWriter} instead of a {@code PrintStream}.
     * @since 3.0 */
    public void usage(PrintWriter writer, Help.Ansi ansi) { usage(writer, Help.defaultColorScheme(ansi)); }

    /**
     * Prints a usage help message for the annotated command class to the specified {@code PrintStream}.
     * Delegates construction of the usage help message to the {@link Help} inner class and is equivalent to:
     * <pre>
     * Help.ColorScheme colorScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
     * Help help = getHelpFactory().create(getCommandSpec(), colorScheme)
     * StringBuilder sb = new StringBuilder();
     * for (String key : getHelpSectionKeys()) {
     *     IHelpSectionRenderer renderer = getHelpSectionMap().get(key);
     *     if (renderer != null) { sb.append(renderer.render(help)); }
     * }
     * out.print(sb);
     * </pre>
     * <p>Annotate your class with {@link Command} to control many aspects of the usage help message, including
     * the program name, text of section headings and section contents, and some aspects of the auto-generated sections
     * of the usage help message.
     * <p>To customize the auto-generated sections of the usage help message, like how option details are displayed,
     * instantiate a {@link Help} object and use a {@link Help.TextTable} with more of fewer columns, a custom
     * {@linkplain Help.Layout layout}, and/or a custom option {@linkplain Help.IOptionRenderer renderer}
     * for ultimate control over which aspects of an Option or Field are displayed where.</p>
     * @param out the {@code PrintStream} to print the usage help message to
     * @param colorScheme the {@code ColorScheme} defining the styles for options, parameters and commands when ANSI is enabled
     * @see UsageMessageSpec
     */
    public void usage(PrintStream out, Help.ColorScheme colorScheme) {
        out.print(usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), colorScheme)));
        out.flush();
    }
    /** Similar to {@link #usage(PrintStream, Help.ColorScheme)}, but with the specified {@code PrintWriter} instead of a {@code PrintStream}.
     * @since 3.0 */
    public void usage(PrintWriter writer, Help.ColorScheme colorScheme) {
        writer.print(usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), colorScheme)));
        writer.flush();
    }
    /** Similar to {@link #usage(PrintStream)}, but returns the usage help message as a String instead of printing it to the {@code PrintStream}.
     * @since 3.2 */
    public String getUsageMessage() {
        return usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), getColorScheme())).toString();
    }
    /** Similar to {@link #usage(PrintStream, Help.Ansi)}, but returns the usage help message as a String instead of printing it to the {@code PrintStream}.
     * @since 3.2 */
    public String getUsageMessage(Help.Ansi ansi) {
        return usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), Help.defaultColorScheme(ansi))).toString();
    }
    /** Similar to {@link #usage(PrintStream, Help.ColorScheme)}, but returns the usage help message as a String instead of printing it to the {@code PrintStream}.
     * @since 3.2 */
    public String getUsageMessage(Help.ColorScheme colorScheme) {
        return usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), colorScheme)).toString();
    }

    private StringBuilder usage(StringBuilder sb, Help help) {
        for (String key : getHelpSectionKeys()) {
            IHelpSectionRenderer renderer = getHelpSectionMap().get(key);
            if (renderer != null) { sb.append(renderer.render(help)); }
        }
        return sb;
    }

    /**
     * Delegates to {@link #printVersionHelp(PrintStream, Help.Ansi)} with the ANSI setting of the {@linkplain #getColorScheme() configured} color scheme.
     * @param out the printStream to print to
     * @see #printVersionHelp(PrintStream, Help.Ansi)
     * @since 0.9.8
     */
    public void printVersionHelp(PrintStream out) { printVersionHelp(out, getColorScheme().ansi()); }

    /**
     * Prints version information from the {@link Command#version()} annotation to the specified {@code PrintStream}.
     * Each element of the array of version strings is printed on a separate line. Version strings may contain
     * <a href="http://picocli.info/#_usage_help_with_styles_and_colors">markup for colors and style</a>.
     * @param out the printStream to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @see Command#version()
     * @see Option#versionHelp()
     * @see #isVersionHelpRequested()
     * @since 0.9.8
     */
    public void printVersionHelp(PrintStream out, Help.Ansi ansi) {
        for (String versionInfo : getCommandSpec().version()) {
            out.println(ansi.new Text(versionInfo));
        }
        out.flush();
    }
    /**
     * Prints version information from the {@link Command#version()} annotation to the specified {@code PrintStream}.
     * Each element of the array of version strings is {@linkplain String#format(String, Object...) formatted} with the
     * specified parameters, and printed on a separate line. Both version strings and parameters may contain
     * <a href="http://picocli.info/#_usage_help_with_styles_and_colors">markup for colors and style</a>.
     * @param out the printStream to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param params Arguments referenced by the format specifiers in the version strings
     * @see Command#version()
     * @see Option#versionHelp()
     * @see #isVersionHelpRequested()
     * @since 1.0.0
     */
    public void printVersionHelp(PrintStream out, Help.Ansi ansi, Object... params) {
        for (String versionInfo : getCommandSpec().version()) {
            out.println(ansi.new Text(format(versionInfo, params)));
        }
        out.flush();
    }
    /**
     * Delegates to {@link #printVersionHelp(PrintWriter, Help.Ansi, Object...)} with the ANSI setting of the {@linkplain #getColorScheme() configured} color scheme.
     * @param out the PrintWriter to print to
     * @since 4.0 */
    public void printVersionHelp(PrintWriter out) { printVersionHelp(out, getColorScheme().ansi()); }
    /**
     * Prints version information from the {@link Command#version()} annotation to the specified {@code PrintWriter}.
     * Each element of the array of version strings is {@linkplain String#format(String, Object...) formatted} with the
     * specified parameters, and printed on a separate line. Both version strings and parameters may contain
     * <a href="http://picocli.info/#_usage_help_with_styles_and_colors">markup for colors and style</a>.
     * @param out the PrintWriter to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param params Arguments referenced by the format specifiers in the version strings
     * @see Command#version()
     * @see Option#versionHelp()
     * @see #isVersionHelpRequested()
     * @since 4.0 */
    public void printVersionHelp(PrintWriter out, Help.Ansi ansi, Object... params) {
        for (String versionInfo : getCommandSpec().version()) {
            out.println(ansi.new Text(format(versionInfo, params)));
        }
        out.flush();
    }

    /**
     * Equivalent to {@code new CommandLine(callable).execute(args)}, except for the return value.
     * @param callable the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param args the command line arguments to parse
     * @param <C> the annotated object must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @see #execute(String...)
     * @since 3.0
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     */
    @Deprecated public static <C extends Callable<T>, T> T call(C callable, String... args) {
        CommandLine cmd = new CommandLine(callable);
        List<Object> results = cmd.parseWithHandler(new RunLast(), args);
        return firstElement(results);
    }

    /**
     * Delegates to {@link #call(Callable, PrintStream, PrintStream, Help.Ansi, String...)} with {@code System.err} for
     * diagnostic error messages and {@link Help.Ansi#AUTO}.
     * @param callable the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param out the printStream to print the usage help message to when the user requested help
     * @param args the command line arguments to parse
     * @param <C> the annotated object must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @see RunLast
     */
    @Deprecated public static <C extends Callable<T>, T> T call(C callable, PrintStream out, String... args) {
        return call(callable, out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #call(Callable, PrintStream, PrintStream, Help.Ansi, String...)} with {@code System.err} for diagnostic error messages.
     * @param callable the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param out the printStream to print the usage help message to when the user requested help
     * @param ansi the ANSI style to use
     * @param args the command line arguments to parse
     * @param <C> the annotated object must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @see RunLast
     */
    @Deprecated public static <C extends Callable<T>, T> T call(C callable, PrintStream out, Help.Ansi ansi, String... args) {
        return call(callable, out, System.err, ansi, args);
    }
    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * The annotated object needs to implement {@link Callable}.
     * <p>Consider using the {@link #execute(String...)} method instead:</p>
     * <pre>{@code
     * CommandLine cmd = new CommandLine(callable)
     *         .setOut(myOutWriter()) // System.out by default
     *         .setErr(myErrWriter()) // System.err by default
     *         .setColorScheme(myColorScheme()); // default color scheme, Ansi.AUTO by default
     * int exitCode = cmd.execute(args);
     * //System.exit(exitCode);
     * }</pre>
     * <p>
     * If the specified Callable command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed.
     * </p>
     * @param callable the command to call when {@linkplain #parse(String...) parsing} succeeds.
     * @param out the printStream to print the usage help message to when the user requested help
     * @param err the printStream to print diagnostic messages to
     * @param ansi including whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <C> the annotated object must implement Callable
     * @param <T> the return type of the specified {@code Callable}
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.0
     */
    @Deprecated public static <C extends Callable<T>, T> T call(C callable, PrintStream out, PrintStream err, Help.Ansi ansi, String... args) {
        CommandLine cmd = new CommandLine(callable);
        List<Object> results = cmd.parseWithHandlers(new RunLast().useOut(out).useAnsi(ansi), new DefaultExceptionHandler<List<Object>>().useErr(err).useAnsi(ansi), args);
        return firstElement(results);
    }
    /**
     * Equivalent to {@code new CommandLine(callableClass, factory).execute(args)}, except for the return value.
     * @param callableClass class of the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified callable class and potentially inject other components
     * @param args the command line arguments to parse
     * @param <C> the annotated class must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @see #execute(String...)
     * @since 3.2
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     */
    @Deprecated public static <C extends Callable<T>, T> T call(Class<C> callableClass, IFactory factory, String... args) {
        CommandLine cmd = new CommandLine(callableClass, factory);
        List<Object> results = cmd.parseWithHandler(new RunLast(), args);
        return firstElement(results);
    }
    /**
     * Delegates to {@link #call(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)} with
     * {@code System.err} for diagnostic error messages, and {@link Help.Ansi#AUTO}.
     * @param callableClass class of the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified callable class and potentially injecting other components
     * @param out the printStream to print the usage help message to when the user requested help
     * @param args the command line arguments to parse
     * @param <C> the annotated class must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.2
     */
    @Deprecated public static <C extends Callable<T>, T> T call(Class<C> callableClass, IFactory factory, PrintStream out, String... args) {
        return call(callableClass, factory, out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #call(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)} with
     * {@code System.err} for diagnostic error messages.
     * @param callableClass class of the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified callable class and potentially injecting other components
     * @param out the printStream to print the usage help message to when the user requested help
     * @param ansi the ANSI style to use
     * @param args the command line arguments to parse
     * @param <C> the annotated class must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.2
     */
    @Deprecated public static <C extends Callable<T>, T> T call(Class<C> callableClass, IFactory factory, PrintStream out, Help.Ansi ansi, String... args) {
        return call(callableClass, factory, out, System.err, ansi, args);
    }

    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * The specified {@linkplain IFactory factory} will create an instance of the specified {@code callableClass};
     * use this method instead of {@link #call(Callable, PrintStream, PrintStream, Help.Ansi, String...) call(Callable, ...)}
     * if you want to use a factory that performs Dependency Injection.
     * The annotated class needs to implement {@link Callable}.
     * <p>Consider using the {@link #execute(String...)} method instead:</p>
     * <pre>{@code
     * CommandLine cmd = new CommandLine(callableClass, factory)
     *         .setOut(myOutWriter()) // System.out by default
     *         .setErr(myErrWriter()) // System.err by default
     *         .setColorScheme(myColorScheme()); // default color scheme, Ansi.AUTO by default
     * int exitCode = cmd.execute(args);
     * //System.exit(exitCode);
     * }</pre>
     * <p>
     * If the specified Callable command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed.
     * </p>
     * @param callableClass class of the command to call when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified callable class and potentially injecting other components
     * @param out the printStream to print the usage help message to when the user requested help
     * @param err the printStream to print diagnostic messages to
     * @param ansi the ANSI style to use
     * @param args the command line arguments to parse
     * @param <C> the annotated class must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.2
     */
    @Deprecated public static <C extends Callable<T>, T> T call(Class<C> callableClass, IFactory factory, PrintStream out, PrintStream err, Help.Ansi ansi, String... args) {
        CommandLine cmd = new CommandLine(callableClass, factory);
        List<Object> results = cmd.parseWithHandlers(new RunLast().useOut(out).useAnsi(ansi), new DefaultExceptionHandler<List<Object>>().useErr(err).useAnsi(ansi), args);
        return firstElement(results);
    }

    @SuppressWarnings("unchecked") private static <T> T firstElement(List<Object> results) {
        return (results == null || results.isEmpty()) ? null : (T) results.get(0);
    }

    /**
     * Equivalent to {@code new CommandLine(runnable).execute(args)}.
     * @param runnable the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param args the command line arguments to parse
     * @param <R> the annotated object must implement Runnable
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @see #execute(String...)
     * @since 3.0
     * @deprecated use {@link #execute(String...)} instead
     */
    @Deprecated public static <R extends Runnable> void run(R runnable, String... args) {
        run(runnable, System.out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #run(Runnable, PrintStream, PrintStream, Help.Ansi, String...)} with {@code System.err} for diagnostic error messages and {@link Help.Ansi#AUTO}.
     * @param runnable the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param out the printStream to print the usage help message to when the user requested help
     * @param args the command line arguments to parse
     * @param <R> the annotated object must implement Runnable
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @deprecated use {@link #execute(String...)} instead
     * @see RunLast
     */
    @Deprecated public static <R extends Runnable> void run(R runnable, PrintStream out, String... args) {
        run(runnable, out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #run(Runnable, PrintStream, PrintStream, Help.Ansi, String...)} with {@code System.err} for diagnostic error messages.
     * @param runnable the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param out the printStream to print the usage help message to when the user requested help
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <R> the annotated object must implement Runnable
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @deprecated use {@link #execute(String...)} instead
     * @see RunLast
     */
    @Deprecated public static <R extends Runnable> void run(R runnable, PrintStream out, Help.Ansi ansi, String... args) {
        run(runnable, out, System.err, ansi, args);
    }
    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * The annotated object needs to implement {@link Runnable}.
     * <p>Consider using the {@link #execute(String...)} method instead:</p>
     * <pre>{@code
     * CommandLine cmd = new CommandLine(runnable)
     *         .setOut(myOutWriter()) // System.out by default
     *         .setErr(myErrWriter()) // System.err by default
     *         .setColorScheme(myColorScheme()); // default color scheme, Ansi.AUTO by default
     * int exitCode = cmd.execute(args);
     * //System.exit(exitCode);
     * }</pre>
     * <p>
     * If the specified Runnable command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed.
     * </p><p>
     * From picocli v2.0, this method prints usage help or version help if {@linkplain #printHelpIfRequested(List, PrintStream, PrintStream, Help.Ansi) requested},
     * and any exceptions thrown by the {@code Runnable} are caught and rethrown wrapped in an {@code ExecutionException}.
     * </p>
     * @param runnable the command to run when {@linkplain #parse(String...) parsing} succeeds.
     * @param out the printStream to print the usage help message to when the user requested help
     * @param err the printStream to print diagnostic messages to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <R> the annotated object must implement Runnable
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @deprecated use {@link #execute(String...)} instead
     * @since 3.0
     */
    @Deprecated public static <R extends Runnable> void run(R runnable, PrintStream out, PrintStream err, Help.Ansi ansi, String... args) {
        CommandLine cmd = new CommandLine(runnable);
        cmd.parseWithHandlers(new RunLast().useOut(out).useAnsi(ansi), new DefaultExceptionHandler<List<Object>>().useErr(err).useAnsi(ansi), args);
    }
    /**
     * Equivalent to {@code new CommandLine(runnableClass, factory).execute(args)}.
     * @param runnableClass class of the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified Runnable class and potentially injecting other components
     * @param args the command line arguments to parse
     * @param <R> the annotated class must implement Runnable
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @see #execute(String...)
     * @since 3.2
     * @deprecated use {@link #execute(String...)} instead
     */
    @Deprecated public static <R extends Runnable> void run(Class<R> runnableClass, IFactory factory, String... args) {
        run(runnableClass, factory, System.out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #run(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)} with
     * {@code System.err} for diagnostic error messages, and {@link Help.Ansi#AUTO}.
     * @param runnableClass class of the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified Runnable class and potentially injecting other components
     * @param out the printStream to print the usage help message to when the user requested help
     * @param args the command line arguments to parse
     * @param <R> the annotated class must implement Runnable
     * @see #run(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler2, IExceptionHandler2, String...)
     * @see RunLast
     * @deprecated use {@link #execute(String...)} instead
     * @since 3.2
     */
    @Deprecated public static <R extends Runnable> void run(Class<R> runnableClass, IFactory factory, PrintStream out, String... args) {
        run(runnableClass, factory, out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #run(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)} with
     * {@code System.err} for diagnostic error messages.
     * @param runnableClass class of the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified Runnable class and potentially injecting other components
     * @param out the printStream to print the usage help message to when the user requested help
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <R> the annotated class must implement Runnable
     * @see #run(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified class cannot be instantiated by the factory, or does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler2, IExceptionHandler2, String...)
     * @see RunLast
     * @deprecated use {@link #execute(String...)} instead
     * @since 3.2
     */
    @Deprecated public static <R extends Runnable> void run(Class<R> runnableClass, IFactory factory, PrintStream out, Help.Ansi ansi, String... args) {
        run(runnableClass, factory, out, System.err, ansi, args);
    }

    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * The specified {@linkplain IFactory factory} will create an instance of the specified {@code runnableClass};
     * use this method instead of {@link #run(Runnable, PrintStream, PrintStream, Help.Ansi, String...) run(Runnable, ...)}
     * if you want to use a factory that performs Dependency Injection.
     * The annotated class needs to implement {@link Runnable}.
     * <p>Consider using the {@link #execute(String...)} method instead:</p>
     * <pre>{@code
     * CommandLine cmd = new CommandLine(runnableClass, factory)
     *         .setOut(myOutWriter()) // System.out by default
     *         .setErr(myErrWriter()) // System.err by default
     *         .setColorScheme(myColorScheme()); // default color scheme, Ansi.AUTO by default
     * int exitCode = cmd.execute(args);
     * //System.exit(exitCode);
     * }</pre>
     * <p>
     * If the specified Runnable command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed.
     * </p><p>
     * This method prints usage help or version help if {@linkplain #printHelpIfRequested(List, PrintStream, PrintStream, Help.Ansi) requested},
     * and any exceptions thrown by the {@code Runnable} are caught and rethrown wrapped in an {@code ExecutionException}.
     * </p>
     * @param runnableClass class of the command to run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param factory the factory responsible for instantiating the specified Runnable class and potentially injecting other components
     * @param out the printStream to print the usage help message to when the user requested help
     * @param err the printStream to print diagnostic messages to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <R> the annotated class must implement Runnable
     * @deprecated use {@link #execute(String...)} instead
     * @since 3.2
     */
    @Deprecated public static <R extends Runnable> void run(Class<R> runnableClass, IFactory factory, PrintStream out, PrintStream err, Help.Ansi ansi, String... args) {
        CommandLine cmd = new CommandLine(runnableClass, factory);
        cmd.parseWithHandlers(new RunLast().useOut(out).useAnsi(ansi), new DefaultExceptionHandler<List<Object>>().useErr(err).useAnsi(ansi), args);
    }

    /**
     * Delegates to {@link #invoke(String, Class, PrintStream, PrintStream, Help.Ansi, String...)} with {@code System.out} for
     * requested usage help messages, {@code System.err} for diagnostic error messages, and {@link Help.Ansi#AUTO}.
     * @param methodName the {@code @Command}-annotated method to build a {@link CommandSpec} model from,
     *                   and run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param cls the class where the {@code @Command}-annotated method is declared, or a subclass
     * @param args the command line arguments to parse
     * @see #invoke(String, Class, PrintStream, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified method does not have a {@link Command} annotation,
     *      or if the specified class contains multiple {@code @Command}-annotated methods with the specified name
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler2, IExceptionHandler2, String...)
     * @since 3.6
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     */
    @Deprecated public static Object invoke(String methodName, Class<?> cls, String... args) {
        return invoke(methodName, cls, System.out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #invoke(String, Class, PrintStream, PrintStream, Help.Ansi, String...)} with the specified stream for
     * requested usage help messages, {@code System.err} for diagnostic error messages, and {@link Help.Ansi#AUTO}.
     * @param methodName the {@code @Command}-annotated method to build a {@link CommandSpec} model from,
     *                   and run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param cls the class where the {@code @Command}-annotated method is declared, or a subclass
     * @param out the printstream to print requested help message to
     * @param args the command line arguments to parse
     * @see #invoke(String, Class, PrintStream, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified method does not have a {@link Command} annotation,
     *      or if the specified class contains multiple {@code @Command}-annotated methods with the specified name
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler2, IExceptionHandler2, String...)
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.6
     */
    @Deprecated public static Object invoke(String methodName, Class<?> cls, PrintStream out, String... args) {
        return invoke(methodName, cls, out, System.err, Help.Ansi.AUTO, args);
    }
    /**
     * Delegates to {@link #invoke(String, Class, PrintStream, PrintStream, Help.Ansi, String...)} with the specified stream for
     * requested usage help messages, {@code System.err} for diagnostic error messages, and the specified Ansi mode.
     * @param methodName the {@code @Command}-annotated method to build a {@link CommandSpec} model from,
     *                   and run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param cls the class where the {@code @Command}-annotated method is declared, or a subclass
     * @param out the printstream to print requested help message to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @see #invoke(String, Class, PrintStream, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified method does not have a {@link Command} annotation,
     *      or if the specified class contains multiple {@code @Command}-annotated methods with the specified name
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler2, IExceptionHandler2, String...)
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.6
     */
    @Deprecated public static Object invoke(String methodName, Class<?> cls, PrintStream out, Help.Ansi ansi, String... args) {
        return invoke(methodName, cls, out, System.err, ansi, args);
    }

    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * Constructs a {@link CommandSpec} model from the {@code @Option} and {@code @Parameters}-annotated method parameters
     * of the {@code @Command}-annotated method, parses the specified command line arguments and invokes the specified method.
     * <p>Consider using the {@link #execute(String...)} method instead:</p>
     * <pre>{@code
     * Method commandMethod = getCommandMethods(cls, methodName).get(0);
     * CommandLine cmd = new CommandLine(commandMethod)
     *         .setOut(myOutWriter()) // System.out by default
     *         .setErr(myErrWriter()) // System.err by default
     *         .setColorScheme(myColorScheme()); // default color scheme, Ansi.AUTO by default
     * int exitCode = cmd.execute(args);
     * //System.exit(exitCode);
     * }</pre>
     * @param methodName the {@code @Command}-annotated method to build a {@link CommandSpec} model from,
     *                   and run when {@linkplain #parseArgs(String...) parsing} succeeds.
     * @param cls the class where the {@code @Command}-annotated method is declared, or a subclass
     * @param out the printStream to print the usage help message to when the user requested help
     * @param err the printStream to print diagnostic messages to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @throws InitializationException if the specified method does not have a {@link Command} annotation,
     *      or if the specified class contains multiple {@code @Command}-annotated methods with the specified name
     * @throws ExecutionException if the method throws an exception
     * @deprecated use {@link #execute(String...)} and {@link #getExecutionResult()} instead
     * @since 3.6
     */
    @Deprecated public static Object invoke(String methodName, Class<?> cls, PrintStream out, PrintStream err, Help.Ansi ansi, String... args) {
        List<Method> candidates = getCommandMethods(cls, methodName);
        if (candidates.size() != 1) { throw new InitializationException("Expected exactly one @Command-annotated method for " + cls.getName() + "::" + methodName + "(...), but got: " + candidates); }
        Method method = candidates.get(0);
        CommandLine cmd = new CommandLine(method);
        List<Object> list = cmd.parseWithHandlers(new RunLast().useOut(out).useAnsi(ansi), new DefaultExceptionHandler<List<Object>>().useErr(err).useAnsi(ansi), args);
        return list == null ? null : list.get(0);
    }
    /**
     * Helper to get methods of a class annotated with {@link Command @Command} via reflection, optionally filtered by method name (not {@link Command#name() @Command.name}).
     * Methods have to be either public (inherited) members or be declared by {@code cls}, that is "inherited" static or protected methods will not be picked up.
     *
     * @param cls the class to search for methods annotated with {@code @Command}
     * @param methodName if not {@code null}, return only methods whose method name (not {@link Command#name() @Command.name}) equals this string. Ignored if {@code null}.
     * @return the matching command methods, or an empty list
     * @see #invoke(String, Class, String...)
     * @since 3.6.0
     */
    public static List<Method> getCommandMethods(Class<?> cls, String methodName) {
        Set<Method> candidates = new HashSet<Method>();
        // traverse public member methods (excludes static/non-public, includes inherited)
        candidates.addAll(Arrays.asList(Assert.notNull(cls, "class").getMethods()));
        // traverse directly declared methods (includes static/non-public, excludes inherited)
        candidates.addAll(Arrays.asList(Assert.notNull(cls, "class").getDeclaredMethods()));

        List<Method> result = new ArrayList<Method>();
        for (Method method : candidates) {
            if (method.isAnnotationPresent(Command.class)) {
                if (methodName == null || methodName.equals(method.getName())) { result.add(method); }
            }
        }
        Collections.sort(result, new Comparator<Method>() {
            public int compare(Method o1, Method o2) { return o1.getName().compareTo(o2.getName()); }
        });
        return result;
    }

    /**
     * Registers the specified type converter for the specified class. When initializing fields annotated with
     * {@link Option}, the field's type is used as a lookup key to find the associated type converter, and this
     * type converter converts the original command line argument string value to the correct type.
     * <p>
     * Java 8 lambdas make it easy to register custom type converters:
     * </p>
     * <pre>
     * commandLine.registerConverter(java.nio.file.Path.class, s -&gt; java.nio.file.Paths.get(s));
     * commandLine.registerConverter(java.time.Duration.class, s -&gt; java.time.Duration.parse(s));</pre>
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
     * <p>The specified converter will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment the converter is registered</em>. Subcommands added
     * later will not have this converter added automatically. To ensure a custom type converter is available to all
     * subcommands, register the type converter last, after adding subcommands.</p>
     *
     * @param cls the target class to convert parameter string values to
     * @param converter the class capable of converting string values to the specified target type
     * @param <K> the target type
     * @return this CommandLine object, to allow method chaining
     * @see #addSubcommand(String, Object)
     */
    public <K> CommandLine registerConverter(Class<K> cls, ITypeConverter<K> converter) {
        interpreter.converterRegistry.put(Assert.notNull(cls, "class"), Assert.notNull(converter, "converter"));
        for (CommandLine command : getCommandSpec().commands.values()) {
            command.registerConverter(cls, converter);
        }
        return this;
    }

    /** Returns the String that separates option names from option values when parsing command line options.
     * @return the String the parser uses to separate option names from option values
     * @see ParserSpec#separator() */
    public String getSeparator() { return getCommandSpec().parser().separator(); }

    /** Sets the String the parser uses to separate option names from option values to the specified value.
     * The separator may also be set declaratively with the {@link CommandLine.Command#separator()} annotation attribute.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param separator the String that separates option names from option values
     * @see ParserSpec#separator(String)
     * @return this {@code CommandLine} object, to allow method chaining */
    public CommandLine setSeparator(String separator) {
        getCommandSpec().parser().separator(Assert.notNull(separator, "separator"));
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setSeparator(separator);
        }
        return this;
    }

    /** Returns the ResourceBundle of this command or {@code null} if no resource bundle is set.
     * @see Command#resourceBundle()
     * @see CommandSpec#resourceBundle()
     * @since 3.6 */
    public ResourceBundle getResourceBundle() { return getCommandSpec().resourceBundle(); }

    /** Sets the ResourceBundle containing usage help message strings.
     * <p>The specified bundle will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will not be impacted. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param bundle the ResourceBundle containing usage help message strings
     * @return this {@code CommandLine} object, to allow method chaining
     * @see Command#resourceBundle()
     * @see CommandSpec#resourceBundle(ResourceBundle)
     * @since 3.6 */
    public CommandLine setResourceBundle(ResourceBundle bundle) {
        getCommandSpec().resourceBundle(bundle);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.getCommandSpec().resourceBundle(bundle);
        }
        return this;
    }

    /** Returns the maximum width of the usage help message. The default is 80.
     * @see UsageMessageSpec#width() */
    public int getUsageHelpWidth() { return getCommandSpec().usageMessage().width(); }

    /** Sets the maximum width of the usage help message. Longer lines are wrapped.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param width the maximum width of the usage help message
     * @see UsageMessageSpec#width(int)
     * @return this {@code CommandLine} object, to allow method chaining */
    public CommandLine setUsageHelpWidth(int width) {
        getCommandSpec().usageMessage().width(width);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setUsageHelpWidth(width);
        }
        return this;
    }

    /** Returns the command name (also called program name) displayed in the usage help synopsis.
     * @return the command name (also called program name) displayed in the usage
     * @see CommandSpec#name()
     * @since 2.0 */
    public String getCommandName() { return getCommandSpec().name(); }

    /** Sets the command name (also called program name) displayed in the usage help synopsis to the specified value.
     * Note that this method only modifies the usage help message, it does not impact parsing behaviour.
     * The command name may also be set declaratively with the {@link CommandLine.Command#name()} annotation attribute.
     * @param commandName command name (also called program name) displayed in the usage help synopsis
     * @return this {@code CommandLine} object, to allow method chaining
     * @see CommandSpec#name(String)
     * @since 2.0 */
    public CommandLine setCommandName(String commandName) {
        getCommandSpec().name(Assert.notNull(commandName, "commandName"));
        return this;
    }

    /** Returns whether arguments starting with {@code '@'} should be treated as the path to an argument file and its
     * contents should be expanded into separate arguments for each line in the specified file.
     * This property is {@code true} by default.
     * @return whether "argument files" or {@code @files} should be expanded into their content
     * @since 2.1 */
    public boolean isExpandAtFiles() { return getCommandSpec().parser().expandAtFiles(); }

    /** Sets whether arguments starting with {@code '@'} should be treated as the path to an argument file and its
     * contents should be expanded into separate arguments for each line in the specified file. ({@code true} by default.)
     * @param expandAtFiles whether "argument files" or {@code @files} should be expanded into their content
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 2.1 */
    public CommandLine setExpandAtFiles(boolean expandAtFiles) {
        getCommandSpec().parser().expandAtFiles(expandAtFiles);
        return this;
    }

    /** Returns the character that starts a single-line comment or {@code null} if all content of argument files should
     * be interpreted as arguments (without comments).
     * If specified, all characters from the comment character to the end of the line are ignored.
     * @return the character that starts a single-line comment or {@code null}. The default is {@code '#'}.
     * @since 3.5 */
    public Character getAtFileCommentChar() { return getCommandSpec().parser().atFileCommentChar(); }

    /** Sets the character that starts a single-line comment or {@code null} if all content of argument files should
     * be interpreted as arguments (without comments).
     * If specified, all characters from the comment character to the end of the line are ignored.
     * @param atFileCommentChar the character that starts a single-line comment or {@code null}. The default is {@code '#'}.
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.5 */
    public CommandLine setAtFileCommentChar(Character atFileCommentChar) {
        getCommandSpec().parser().atFileCommentChar(atFileCommentChar);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setAtFileCommentChar(atFileCommentChar);
        }
        return this;
    }

    /** Returns whether to use a simplified argument file format that is compatible with JCommander.
     * In this format, every line (except empty lines and comment lines)
     * is interpreted as a single argument. Arguments containing whitespace do not need to be quoted.
     * When system property {@code "picocli.useSimplifiedAtFiles"} is defined, the system property value overrides the programmatically set value.
     * @return whether to use a simplified argument file format. The default is {@code false}.
     * @since 3.9 */
    public boolean isUseSimplifiedAtFiles() { return getCommandSpec().parser().useSimplifiedAtFiles(); }

    /** Sets whether to use a simplified argument file format that is compatible with JCommander.
     * In this format, every line (except empty lines and comment lines)
     * is interpreted as a single argument. Arguments containing whitespace do not need to be quoted.
     * When system property {@code "picocli.useSimplifiedAtFiles"} is defined, the system property value overrides the programmatically set value.
     * @param simplifiedAtFiles whether to use a simplified argument file format. The default is {@code false}.
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 3.9 */
    public CommandLine setUseSimplifiedAtFiles(boolean simplifiedAtFiles) {
        getCommandSpec().parser().useSimplifiedAtFiles(simplifiedAtFiles);
        for (CommandLine command : getCommandSpec().subcommands().values()) {
            command.setUseSimplifiedAtFiles(simplifiedAtFiles);
        }
        return this;
    }
    private static boolean empty(String str) { return str == null || str.trim().length() == 0; }
    private static boolean empty(Object[] array) { return array == null || array.length == 0; }
    private static String str(String[] arr, int i) { return (arr == null || arr.length <= i) ? "" : arr[i]; }
    private static boolean isBoolean(Class<?> type) { return type == Boolean.class || type == Boolean.TYPE; }
    private static CommandLine toCommandLine(Object obj, IFactory factory) { return obj instanceof CommandLine ? (CommandLine) obj : new CommandLine(obj, factory);}
    private static boolean isMultiValue(Class<?> cls) { return cls.isArray() || Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls); }
    private static String format(String formatString, Object... params) {
        try {
            return formatString == null ? "" : String.format(formatString, params);
        } catch (IllegalFormatException ex) {
            new Tracer().warn("Could not format '%s' (Underlying error: %s). " +
                    "Using raw String: '%%n' format strings have not been replaced with newlines. " +
                    "Please ensure to escape '%%' characters with another '%%'.%n", formatString, ex.getMessage());
            return formatString;
        }
    }

    private static class NoCompletionCandidates implements Iterable<String> {
        public Iterator<String> iterator() { throw new UnsupportedOperationException(); }
    }
    /**
     * <p>
     * Annotate fields in your class with {@code @Option} and picocli will initialize these fields when matching
     * arguments are specified on the command line. In the case of command methods (annotated with {@code @Command}),
     * command options can be defined by annotating method parameters with {@code @Option}.
     * </p><p>
     * Command class example:
     * </p>
     * <pre>
     * import static picocli.CommandLine.*;
     *
     * public class MyClass {
     *     &#064;Parameters(description = "Any number of input files")
     *     private List&lt;File&gt; files = new ArrayList&lt;File&gt;();
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
     *
     *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbose mode. Helpful for troubleshooting. Multiple -v options increase the verbosity.")
     *     private boolean[] verbose;
     *
     *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, usageHelp = true, description = "Display this help and exit")
     *     private boolean help;
     * }
     * </pre>
     * <p>
     * A field cannot be annotated with both {@code @Parameters} and {@code @Option} or a
     * {@code ParameterException} is thrown.
     * </p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    public @interface Option {
        /**
         * One or more option names. At least one option name is required.
         * <p>
         * Different environments have different conventions for naming options, but usually options have a prefix
         * that sets them apart from parameters.
         * Picocli supports all of the below styles. The default separator is {@code '='}, but this can be configured.
         * </p><p>
         * <b>*nix</b>
         * </p><p>
         * In Unix and Linux, options have a short (single-character) name, a long name or both.
         * Short options
         * (<a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_02">POSIX
         * style</a> are single-character and are preceded by the {@code '-'} character, e.g., {@code `-v'}.
         * <a href="https://www.gnu.org/software/tar/manual/html_node/Long-Options.html">GNU-style</a> long
         * (or <em>mnemonic</em>) options start with two dashes in a row, e.g., {@code `--file'}.
         * </p><p>Picocli supports the POSIX convention that short options can be grouped, with the last option
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
         * Indicates whether this option is required. By default this is false.
         * <p>If an option is required, but a user invokes the program without specifying the required option,
         * a {@link MissingParameterException} is thrown from the {@link #parse(String...)} method.</p>
         * <p>Required options that are part of a {@linkplain ArgGroup group} are required <em>within the group</em>, not required within the command:
         * the group's {@linkplain ArgGroup#multiplicity() multiplicity} determines whether the group itself is required or optional.</p>
         * @return whether this option is required
         */
        boolean required() default false;

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
         * @deprecated Use {@link #usageHelp()} and {@link #versionHelp()} instead. See {@link #printHelpIfRequested(List, PrintStream, CommandLine.Help.Ansi)}
         */
        @Deprecated boolean help() default false;

        /**
         * Set {@code usageHelp=true} for the {@code --help} option that triggers display of the usage help message.
         * The <a href="http://picocli.info/#_printing_help_automatically">convenience methods</a> {@code Commandline.call},
         * {@code Commandline.run}, and {@code Commandline.parseWithHandler(s)} will automatically print usage help
         * when an option with {@code usageHelp=true} was specified on the command line.
         * <p>
         * By default, <em>all</em> options and positional parameters are included in the usage help message
         * <em>except when explicitly marked {@linkplain #hidden() hidden}.</em>
         * </p><p>
         * If this option is specified on the command line, picocli will not validate the remaining arguments (so no "missing required
         * option" errors) and the {@link CommandLine#isUsageHelpRequested()} method will return {@code true}.
         * </p><p>
         * Alternatively, consider annotating your command with {@linkplain Command#mixinStandardHelpOptions() @Command(mixinStandardHelpOptions = true)}.
         * </p>
         * @return whether this option allows the user to request usage help
         * @since 0.9.8
         * @see #hidden()
         * @see #run(Runnable, String...)
         * @see #call(Callable, String...)
         * @see #parseWithHandler(IParseResultHandler2, String[])
         * @see #printHelpIfRequested(List, PrintStream, PrintStream, Help.Ansi)
         */
        boolean usageHelp() default false;

        /**
         * Set {@code versionHelp=true} for the {@code --version} option that triggers display of the version information.
         * The <a href="http://picocli.info/#_printing_help_automatically">convenience methods</a> {@code Commandline.call},
         * {@code Commandline.run}, and {@code Commandline.parseWithHandler(s)} will automatically print version information
         * when an option with {@code versionHelp=true} was specified on the command line.
         * <p>
         * The version information string is obtained from the command's {@linkplain Command#version() version} annotation
         * or from the {@linkplain Command#versionProvider() version provider}.
         * </p><p>
         * If this option is specified on the command line, picocli will not validate the remaining arguments (so no "missing required
         * option" errors) and the {@link CommandLine#isUsageHelpRequested()} method will return {@code true}.
         * </p><p>
         * Alternatively, consider annotating your command with {@linkplain Command#mixinStandardHelpOptions() @Command(mixinStandardHelpOptions = true)}.
         * </p>
         * @return whether this option allows the user to request version information
         * @since 0.9.8
         * @see #hidden()
         * @see #run(Runnable, String...)
         * @see #call(Callable, String...)
         * @see #parseWithHandler(IParseResultHandler2, String[])
         * @see #printHelpIfRequested(List, PrintStream, PrintStream, Help.Ansi)
         */
        boolean versionHelp() default false;

        /**
         * Description of this option, used when generating the usage documentation. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.
         * </p><p>
         * The description may contain variables that are rendered when help is requested.
         * The string {@code ${DEFAULT-VALUE}} is replaced with the default value of the option. This is regardless of
         * the command's {@link Command#showDefaultValues() showDefaultValues} setting or the option's {@link #showDefaultValue() showDefaultValue} setting.
         * The string {@code ${COMPLETION-CANDIDATES}} is replaced with the completion candidates generated by
         * {@link #completionCandidates()} in the description for this option.
         * Also, embedded {@code %n} newline markers are converted to actual newlines.
         * </p>
         * @return the description of this option
         */
        String[] description() default {};

        /**
         * Specifies the minimum number of required parameters and the maximum number of accepted parameters.
         * If an option declares a positive arity, and the user specifies an insufficient number of parameters on the
         * command line, a {@link MissingParameterException} is thrown by the {@link #parse(String...)} method.
         * <p>
         * In many cases picocli can deduce the number of required parameters from the field's type.
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
         * By default picocli does not expect boolean options (also called "flags" or "switches") to have a parameter.
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
         * picocli uses the field name in fish brackets ({@code '<'} and {@code '>'}) by default. Example:
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

        /** Returns whether usage syntax decorations around the {@linkplain #paramLabel() paramLabel} should be suppressed.
         * The default is {@code false}: by default, the paramLabel is surrounded with {@code '['} and {@code ']'} characters
         * if the value is optional and followed by ellipses ("...") when multiple values can be specified.
         * @since 3.6.0 */
        boolean hideParamSyntax() default false;

        /** <p>
         * Optionally specify a {@code type} to control exactly what Class the option parameter should be converted
         * to. This may be useful when the field type is an interface or an abstract class. For example, a field can
         * be declared to have type {@code java.lang.Number}, and annotating {@code @Option(type=Short.class)}
         * ensures that the option parameter value is converted to a {@code Short} before setting the field value.
         * </p><p>
         * For array fields whose <em>component</em> type is an interface or abstract class, specify the concrete <em>component</em> type.
         * For example, a field with type {@code Number[]} may be annotated with {@code @Option(type=Short.class)}
         * to ensure that option parameter values are converted to {@code Short} before adding an element to the array.
         * </p><p>
         * Picocli will use the {@link ITypeConverter} that is
         * {@linkplain #registerConverter(Class, ITypeConverter) registered} for the specified type to convert
         * the raw String values before modifying the field value.
         * </p><p>
         * Prior to 2.0, the {@code type} attribute was necessary for {@code Collection} and {@code Map} fields,
         * but starting from 2.0 picocli will infer the component type from the generic type's type arguments.
         * For example, for a field of type {@code Map<TimeUnit, Long>} picocli will know the option parameter
         * should be split up in key=value pairs, where the key should be converted to a {@code java.util.concurrent.TimeUnit}
         * enum value, and the value should be converted to a {@code Long}. No {@code @Option(type=...)} type attribute
         * is required for this. For generic types with wildcards, picocli will take the specified upper or lower bound
         * as the Class to convert to, unless the {@code @Option} annotation specifies an explicit {@code type} attribute.
         * </p><p>
         * If the field type is a raw collection or a raw map, and you want it to contain other values than Strings,
         * or if the generic type's type arguments are interfaces or abstract classes, you may
         * specify a {@code type} attribute to control the Class that the option parameter should be converted to.
         * @return the type(s) to convert the raw String values
         */
        Class<?>[] type() default {};

        /**
         * Optionally specify one or more {@link ITypeConverter} classes to use to convert the command line argument into
         * a strongly typed value (or key-value pair for map fields). This is useful when a particular field should
         * use a custom conversion that is different from the normal conversion for the field's type.
         * <p>For example, for a specific field you may want to use a converter that maps the constant names defined
         * in {@link java.sql.Types java.sql.Types} to the {@code int} value of these constants, but any other {@code int} fields should
         * not be affected by this and should continue to use the standard int converter that parses numeric values.</p>
         * @return the type converter(s) to use to convert String values to strongly typed values for this field
         * @see CommandLine#registerConverter(Class, ITypeConverter)
         */
        Class<? extends ITypeConverter<?>>[] converter() default {};

        /**
         * Specify a regular expression to use to split option parameter values before applying them to the field.
         * All elements resulting from the split are added to the array or Collection. Ignored for single-value fields.
         * @return a regular expression to split option parameter values or {@code ""} if the value should not be split
         * @see String#split(String)
         */
        String split() default "";

        /**
         * Set {@code hidden=true} if this option should not be included in the usage help message.
         * @return whether this option should be excluded from the usage documentation
         */
        boolean hidden() default false;

        /** Returns the default value of this option, before splitting and type conversion.
         * @return a String that (after type conversion) will be used as the value for this option if no value was specified on the command line
         * @since 3.2 */
        String defaultValue() default "__no_default_value__";

        /** Use this attribute to control for a specific option whether its default value should be shown in the usage
         * help message. If not specified, the default value is only shown when the {@link Command#showDefaultValues()}
         * is set {@code true} on the command. Use this attribute to specify whether the default value
         * for this specific option should always be shown or never be shown, regardless of the command setting.
         * <p>Note that picocli 3.2 allows {@linkplain #description() embedding default values} anywhere in the description that ignores this setting.</p>
         * @return whether this option's default value should be shown in the usage help message
         */
        Help.Visibility showDefaultValue() default Help.Visibility.ON_DEMAND;

        /** Use this attribute to specify an {@code Iterable<String>} class that generates completion candidates for this option.
         * For map fields, completion candidates should be in {@code key=value} form.
         * <p>
         * Completion candidates are used in bash completion scripts generated by the {@code picocli.AutoComplete} class.
         * Bash has special completion options to generate file names and host names, and the bash completion scripts
         * generated by {@code AutoComplete} delegate to these bash built-ins for {@code @Options} whose {@code type} is
         * {@code java.io.File}, {@code java.nio.file.Path} or {@code java.net.InetAddress}.
         * </p><p>
         * For {@code @Options} whose {@code type} is a Java {@code enum}, {@code AutoComplete} can generate completion
         * candidates from the type. For other types, use this attribute to specify completion candidates.
         * </p>
         *
         * @return a class whose instances can iterate over the completion candidates for this option
         * @see picocli.CommandLine.IFactory
         * @since 3.2 */
        Class<? extends Iterable<String>> completionCandidates() default NoCompletionCandidates.class;

        /**
         * Set {@code interactive=true} if this option will prompt the end user for a value (like a password).
         * Only supported for single-value options (not arrays, collections or maps).
         * When running on Java 6 or greater, this will use the {@link Console#readPassword()} API to get a value without echoing input to the console.
         * @return whether this option prompts the end user for a value to be entered on the command line
         * @since 3.5
         */
        boolean interactive() default false;

        /** ResourceBundle key for this option. If not specified, (and a ResourceBundle {@linkplain Command#resourceBundle() exists for this command}) an attempt
         * is made to find the option description using any of the option names (without leading hyphens) as key.
         * @see OptionSpec#description()
         * @since 3.6
         */
        String descriptionKey() default "";

        /**
         * When {@link Command#sortOptions() @Command(sortOptions = false)} is specified, this attribute can be used to control the order in which options are listed in the usage help message.
         * @return the position in the options list at which this option should be shown. Options with a lower number are shown before options with a higher number. Gaps are allowed.
         * @since 3.9
         */
        int order() default -1;
    }
    /**
     * <p>
     * Fields annotated with {@code @Parameters} will be initialized with positional parameters. By specifying the
     * {@link #index()} attribute you can pick the exact position or a range of positional parameters to apply. If no
     * index is specified, the field will get all positional parameters (and so it should be an array or a collection).
     * </p><p>
     * In the case of command methods (annotated with {@code @Command}), method parameters may be annotated with {@code @Parameters},
     * but are are considered positional parameters by default, unless they are annotated with {@code @Option}.
     * </p><p>
     * Command class example:
     * </p>
     * <pre>
     * import static picocli.CommandLine.*;
     *
     * public class MyCalcParameters {
     *     &#064;Parameters(description = "Any number of input numbers")
     *     private List&lt;BigDecimal&gt; files = new ArrayList&lt;BigDecimal&gt;();
     *
     *     &#064;Option(names = { "-h", "--help" }, usageHelp = true, description = "Display this help and exit")
     *     private boolean help;
     * }
     * </pre><p>
     * A field cannot be annotated with both {@code @Parameters} and {@code @Option} or a {@code ParameterException}
     * is thrown.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    public @interface Parameters {
        /** Specify an index ("0", or "1", etc.) to pick which of the command line arguments should be assigned to this
         * field. For array or Collection fields, you can also specify an index range ("0..3", or "2..*", etc.) to assign
         * a subset of the command line arguments to this field. The default is "*", meaning all command line arguments.
         * @return an index or range specifying which of the command line arguments should be assigned to this field
         */
        String index() default "";

        /** Description of the parameter(s), used when generating the usage documentation. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.
         * </p><p>
         * The description may contain variables that are rendered when help is requested.
         * The string {@code ${DEFAULT-VALUE}} is replaced with the default value of the positional parameter. This is regardless of
         * the command's {@link Command#showDefaultValues() showDefaultValues} setting or the positional parameter's {@link #showDefaultValue() showDefaultValue} setting.
         * The string {@code ${COMPLETION-CANDIDATES}} is replaced with the completion candidates generated by
         * {@link #completionCandidates()} in the description for this positional parameter.
         * Also, embedded {@code %n} newline markers are converted to actual newlines.
         * </p>
         * @return the description of the parameter(s)
         */
        String[] description() default {};

        /**
         * Specifies the minimum number of required parameters and the maximum number of accepted parameters. If a
         * positive arity is declared, and the user specifies an insufficient number of parameters on the command line,
         * {@link MissingParameterException} is thrown by the {@link #parse(String...)} method.
         * <p>The default depends on the type of the parameter: booleans require no parameters, arrays and Collections
         * accept zero to any number of parameters, and any other type accepts one parameter.</p>
         * <p>For single-value parameters, setting {@code arity = "0..1"} makes a positional parameter optional, while setting {@code arity = "1"} makes it required.</p>
         * <p>Required parameters that are part of a {@linkplain ArgGroup group} are required <em>within the group</em>, not required within the command:
         * the group's {@linkplain ArgGroup#multiplicity() multiplicity} determines whether the group itself is required or optional.</p>
         * @return the range of minimum and maximum parameters accepted by this command
         */
        String arity() default "";

        /**
         * Specify a {@code paramLabel} for the parameter to be used in the usage help message. If omitted,
         * picocli uses the field name in fish brackets ({@code '<'} and {@code '>'}) by default. Example:
         * <pre>class Example {
         *     &#064;Parameters(paramLabel="FILE", description="path of the input FILE(s)")
         *     private File[] inputFiles;
         * }</pre>
         * <p>By default, the above gives a usage help message like the following:</p><pre>
         * Usage: &lt;main class&gt; [FILE...]
         * [FILE...]       path of the input FILE(s)
         * </pre>
         * @return name of the positional parameter used in the usage help message
         */
        String paramLabel() default "";

        /** Returns whether usage syntax decorations around the {@linkplain #paramLabel() paramLabel} should be suppressed.
         * The default is {@code false}: by default, the paramLabel is surrounded with {@code '['} and {@code ']'} characters
         * if the value is optional and followed by ellipses ("...") when multiple values can be specified.
         * @since 3.6.0 */
        boolean hideParamSyntax() default false;

        /**
         * <p>
         * Optionally specify a {@code type} to control exactly what Class the positional parameter should be converted
         * to. This may be useful when the field type is an interface or an abstract class. For example, a field can
         * be declared to have type {@code java.lang.Number}, and annotating {@code @Parameters(type=Short.class)}
         * ensures that the positional parameter value is converted to a {@code Short} before setting the field value.
         * </p><p>
         * For array fields whose <em>component</em> type is an interface or abstract class, specify the concrete <em>component</em> type.
         * For example, a field with type {@code Number[]} may be annotated with {@code @Parameters(type=Short.class)}
         * to ensure that positional parameter values are converted to {@code Short} before adding an element to the array.
         * </p><p>
         * Picocli will use the {@link ITypeConverter} that is
         * {@linkplain #registerConverter(Class, ITypeConverter) registered} for the specified type to convert
         * the raw String values before modifying the field value.
         * </p><p>
         * Prior to 2.0, the {@code type} attribute was necessary for {@code Collection} and {@code Map} fields,
         * but starting from 2.0 picocli will infer the component type from the generic type's type arguments.
         * For example, for a field of type {@code Map<TimeUnit, Long>} picocli will know the positional parameter
         * should be split up in key=value pairs, where the key should be converted to a {@code java.util.concurrent.TimeUnit}
         * enum value, and the value should be converted to a {@code Long}. No {@code @Parameters(type=...)} type attribute
         * is required for this. For generic types with wildcards, picocli will take the specified upper or lower bound
         * as the Class to convert to, unless the {@code @Parameters} annotation specifies an explicit {@code type} attribute.
         * </p><p>
         * If the field type is a raw collection or a raw map, and you want it to contain other values than Strings,
         * or if the generic type's type arguments are interfaces or abstract classes, you may
         * specify a {@code type} attribute to control the Class that the positional parameter should be converted to.
         * @return the type(s) to convert the raw String values
         */
        Class<?>[] type() default {};

        /**
         * Optionally specify one or more {@link ITypeConverter} classes to use to convert the command line argument into
         * a strongly typed value (or key-value pair for map fields). This is useful when a particular field should
         * use a custom conversion that is different from the normal conversion for the field's type.
         * <p>For example, for a specific field you may want to use a converter that maps the constant names defined
         * in {@link java.sql.Types java.sql.Types} to the {@code int} value of these constants, but any other {@code int} fields should
         * not be affected by this and should continue to use the standard int converter that parses numeric values.</p>
         * @return the type converter(s) to use to convert String values to strongly typed values for this field
         * @see CommandLine#registerConverter(Class, ITypeConverter)
         */
        Class<? extends ITypeConverter<?>>[] converter() default {};

        /**
         * Specify a regular expression to use to split positional parameter values before applying them to the field.
         * All elements resulting from the split are added to the array or Collection. Ignored for single-value fields.
         * @return a regular expression to split operand values or {@code ""} if the value should not be split
         * @see String#split(String)
         */
        String split() default "";

        /**
         * Set {@code hidden=true} if this parameter should not be included in the usage message.
         * @return whether this parameter should be excluded from the usage message
         */
        boolean hidden() default false;

        /** Returns the default value of this positional parameter, before splitting and type conversion.
         * @return a String that (after type conversion) will be used as the value for this positional parameter if no value was specified on the command line
         * @since 3.2 */
        String defaultValue() default "__no_default_value__";

        /** Use this attribute to control for a specific positional parameter whether its default value should be shown in the usage
         * help message. If not specified, the default value is only shown when the {@link Command#showDefaultValues()}
         * is set {@code true} on the command. Use this attribute to specify whether the default value
         * for this specific positional parameter should always be shown or never be shown, regardless of the command setting.
         * <p>Note that picocli 3.2 allows {@linkplain #description() embedding default values} anywhere in the description that ignores this setting.</p>
         * @return whether this positional parameter's default value should be shown in the usage help message
         */
        Help.Visibility showDefaultValue() default Help.Visibility.ON_DEMAND;

        /** Use this attribute to specify an {@code Iterable<String>} class that generates completion candidates for
         * this positional parameter. For map fields, completion candidates should be in {@code key=value} form.
         * <p>
         * Completion candidates are used in bash completion scripts generated by the {@code picocli.AutoComplete} class.
         * Unfortunately, {@code picocli.AutoComplete} is not very good yet at generating completions for positional parameters.
         * </p>
         *
         * @return a class whose instances can iterate over the completion candidates for this positional parameter
         * @see picocli.CommandLine.IFactory
         * @since 3.2 */
        Class<? extends Iterable<String>> completionCandidates() default NoCompletionCandidates.class;

        /**
         * Set {@code interactive=true} if this positional parameter will prompt the end user for a value (like a password).
         * Only supported for single-value positional parameters (not arrays, collections or maps).
         * When running on Java 6 or greater, this will use the {@link Console#readPassword()} API to get a value without echoing input to the console.
         * @return whether this positional parameter prompts the end user for a value to be entered on the command line
         * @since 3.5
         */
        boolean interactive() default false;

        /** ResourceBundle key for this option. If not specified, (and a ResourceBundle {@linkplain Command#resourceBundle() exists for this command}) an attempt
         * is made to find the positional parameter description using {@code paramLabel() + "[" + index() + "]"} as key.
         *
         * @see PositionalParamSpec#description()
         * @since 3.6
         */
        String descriptionKey() default "";
    }

    /**
     * <p>
     * Fields annotated with {@code @ParentCommand} will be initialized with the parent command of the current subcommand.
     * If the current command does not have a parent command, this annotation has no effect.
     * </p><p>
     * Parent commands often define options that apply to all the subcommands.
     * This annotation offers a convenient way to inject a reference to the parent command into a subcommand, so the
     * subcommand can access its parent options. For example:
     * </p><pre>
     * &#064;Command(name = "top", subcommands = Sub.class)
     * class Top implements Runnable {
     *
     *     &#064;Option(names = {"-d", "--directory"}, description = "this option applies to all subcommands")
     *     File baseDirectory;
     *
     *     public void run() { System.out.println("Hello from top"); }
     * }
     *
     * &#064;Command(name = "sub")
     * class Sub implements Runnable {
     *
     *     &#064;ParentCommand
     *     private Top parent;
     *
     *     public void run() {
     *         System.out.println("Subcommand: parent command 'directory' is " + parent.baseDirectory);
     *     }
     * }
     * </pre>
     * @since 2.2
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParentCommand { }

    /**
     * Fields annotated with {@code @Unmatched} will be initialized with the list of unmatched command line arguments, if any.
     * If this annotation is found, picocli automatically sets {@linkplain CommandLine#setUnmatchedArgumentsAllowed(boolean) unmatchedArgumentsAllowed} to {@code true}.
     * @see CommandLine#isUnmatchedArgumentsAllowed()
     * @since 3.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Unmatched { }

    /**
     * <p>
     * Fields annotated with {@code @Mixin} are "expanded" into the current command: {@link Option @Option} and
     * {@link Parameters @Parameters} in the mixin class are added to the options and positional parameters of this command.
     * A {@link DuplicateOptionAnnotationsException} is thrown if any of the options in the mixin has the same name as
     * an option in this command.
     * </p><p>
     * The {@code Mixin} annotation provides a way to reuse common options and parameters without subclassing. For example:
     * </p><pre>
     * class HelloWorld implements Runnable {
     *
     *     // adds the --help and --version options to this command
     *     &#064;Mixin
     *     private HelpOptions = new HelpOptions();
     *
     *     &#064;Option(names = {"-u", "--userName"}, required = true, description = "The user name")
     *     String userName;
     *
     *     public void run() { System.out.println("Hello, " + userName); }
     * }
     *
     * // Common reusable help options.
     * class HelpOptions {
     *
     *     &#064;Option(names = { "-h", "--help"}, usageHelp = true, description = "Display this help and exit")
     *     private boolean help;
     *
     *     &#064;Option(names = { "-V", "--version"}, versionHelp = true, description = "Display version info and exit")
     *     private boolean versionHelp;
     * }
     * </pre>
     * @since 3.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface Mixin {
        /** Optionally specify a name that the mixin object can be retrieved with from the {@code CommandSpec}.
         * If not specified the name of the annotated field is used.
         * @return a String to register the mixin object with, or an empty String if the name of the annotated field should be used */
        String name() default "";
    }
    /**
     * Fields annotated with {@code @Spec} will be initialized with the {@code CommandSpec} for the command the field is part of. Example usage:
     * <pre>
     * class InjectSpecExample implements Runnable {
     *     &#064;Spec CommandSpec commandSpec;
     *     //...
     *     public void run() {
     *         // do something with the injected objects
     *     }
     * }
     * </pre>
     * @since 3.2
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    public @interface Spec { }

    /**
     * <p>Annotate your class with {@code @Command} when you want more control over the format of the generated help
     * message. From 3.6, methods can also be annotated with {@code @Command}, where the method parameters define the
     * command options and positional parameters.
     * </p><pre>
     * &#064;Command(name              = "Encrypt", mixinStandardHelpOptions = true,
     *        description         = "Encrypt FILE(s), or standard input, to standard output or to the output file.",
     *        version             = "Encrypt version 1.0",
     *        footer              = "Copyright (c) 2017",
     *        exitCodeListHeading = "Exit Codes:%n",
     *        exitCodeList        = { " 0:Successful program execution.",
     *                                "64:Invalid input: an unknown option or invalid parameter was specified.",
     *                                "70:Execution exception: an exception occurred while executing the business logic."}
     *        )
     * public class Encrypt {
     *     &#064;Parameters(paramLabel = "FILE", description = "Any number of input files")
     *     private List&lt;File&gt; files = new ArrayList&lt;File&gt;();
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
     *
     *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbose mode. Helpful for troubleshooting. Multiple -v options increase the verbosity.")
     *     private boolean[] verbose;
     * }</pre>
     * <p>
     * The structure of a help message looks like this:
     * </p><ul>
     *   <li>[header]</li>
     *   <li>[synopsis]: {@code Usage: <commandName> [OPTIONS] [FILE...]}</li>
     *   <li>[description]</li>
     *   <li>[parameter list]: {@code      [FILE...]   Any number of input files}</li>
     *   <li>[option list]: {@code   -h, --help   prints this help message and exits}</li>
     *   <li>[exit code list]</li>
     *   <li>[footer]</li>
     * </ul> */
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Target({ElementType.TYPE, ElementType.LOCAL_VARIABLE, ElementType.FIELD, ElementType.PACKAGE, ElementType.METHOD})
    public @interface Command {
        /** Program name to show in the synopsis. If omitted, {@code "<main class>"} is used.
         * For {@linkplain #subcommands() declaratively added} subcommands, this attribute is also used
         * by the parser to recognize subcommands in the command line arguments.
         * @return the program name to show in the synopsis
         * @see CommandSpec#name()
         * @see Help#commandName() */
        String name() default "<main class>";

        /** Alternative command names by which this subcommand is recognized on the command line.
         * @return one or more alternative command names
         * @since 3.1 */
        String[] aliases() default {};

        /** A list of classes to instantiate and register as subcommands. When registering subcommands declaratively
         * like this, you don't need to call the {@link CommandLine#addSubcommand(String, Object)} method. For example, this:
         * <pre>
         * &#064;Command(subcommands = {
         *         GitStatus.class,
         *         GitCommit.class,
         *         GitBranch.class })
         * public class Git { ... }
         *
         * CommandLine commandLine = new CommandLine(new Git());
         * </pre> is equivalent to this:
         * <pre>
         * // alternative: programmatically add subcommands.
         * // NOTE: in this case there should be no `subcommands` attribute on the @Command annotation.
         * &#064;Command public class Git { ... }
         *
         * CommandLine commandLine = new CommandLine(new Git())
         *         .addSubcommand("status",   new GitStatus())
         *         .addSubcommand("commit",   new GitCommit())
         *         .addSubcommand("branch",   new GitBranch());
         * </pre>
         * @return the declaratively registered subcommands of this command, or an empty array if none
         * @see CommandLine#addSubcommand(String, Object)
         * @see HelpCommand
         * @since 0.9.8
         */
        Class<?>[] subcommands() default {};

        /** Specify whether methods annotated with {@code @Command} should be registered as subcommands of their
         * enclosing {@code @Command} class.
         * The default is {@code true}. For example:
         * <pre>
         * &#064;Command
         * public class Git {
         *     &#064;Command
         *     void status() { ... }
         * }
         *
         * CommandLine git = new CommandLine(new Git());
         * </pre> is equivalent to this:
         * <pre>
         * // don't add command methods as subcommands automatically
         * &#064;Command(addMethodSubcommands = false)
         * public class Git {
         *     &#064;Command
         *     void status() { ... }
         * }
         *
         * // add command methods as subcommands programmatically
         * CommandLine git = new CommandLine(new Git());
         * CommandLine status = new CommandLine(CommandLine.getCommandMethods(Git.class, "status").get(0));
         * git.addSubcommand("status", status);
         * </pre>
         * @return whether methods annotated with {@code @Command} should be registered as subcommands
         * @see CommandLine#addSubcommand(String, Object)
         * @see CommandLine#getCommandMethods(Class, String)
         * @see CommandSpec#addMethodSubcommands()
         * @since 3.6.0 */
        boolean addMethodSubcommands() default true;

        /** String that separates options from option parameters. Default is {@code "="}. Spaces are also accepted.
         * @return the string that separates options from option parameters, used both when parsing and when generating usage help
         * @see CommandLine#setSeparator(String) */
        String separator() default "=";

        /** Version information for this command, to print to the console when the user specifies an
         * {@linkplain Option#versionHelp() option} to request version help. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * <p>This is not part of the usage help message.</p>
         *
         * @return a string or an array of strings with version information about this command (each string in the array is displayed on a separate line).
         * @since 0.9.8
         * @see CommandLine#printVersionHelp(PrintStream)
         */
        String[] version() default {};

        /** Class that can provide version information dynamically at runtime. An implementation may return version
         * information obtained from the JAR manifest, a properties file or some other source.
         * @return a Class that can provide version information dynamically at runtime
         * @since 2.2 */
        Class<? extends IVersionProvider> versionProvider() default NoVersionProvider.class;

        /**
         * Adds the standard {@code -h} and {@code --help} {@linkplain Option#usageHelp() usageHelp} options and {@code -V}
         * and {@code --version} {@linkplain Option#versionHelp() versionHelp} options to the options of this command.
         * <p>
         * Note that if no {@link #version()} or {@link #versionProvider()} is specified, the {@code --version} option will not print anything.
         * </p><p>
         * For {@linkplain #resourceBundle() internationalization}: the help option has {@code descriptionKey = "mixinStandardHelpOptions.help"},
         * and the version option has {@code descriptionKey = "mixinStandardHelpOptions.version"}.
         * </p>
         * @return whether the auto-help mixin should be added to this command
         * @since 3.0 */
        boolean mixinStandardHelpOptions() default false;

        /** Set this attribute to {@code true} if this subcommand is a help command, and required options and positional
         * parameters of the parent command should not be validated. If a subcommand marked as {@code helpCommand} is
         * specified on the command line, picocli will not validate the parent arguments (so no "missing required
         * option" errors) and the {@link CommandLine#printHelpIfRequested(List, PrintStream, PrintStream, Help.Ansi)} method will return {@code true}.
         * @return {@code true} if this subcommand is a help command and picocli should not check for missing required
         *      options and positional parameters on the parent command
         * @since 3.0 */
        boolean helpCommand() default false;

        /** Set the heading preceding the header section.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the header section
         * @see UsageMessageSpec#headerHeading()
         * @see Help#headerHeading(Object...)  */
        String headerHeading() default "";

        /** Optional summary description of the command, shown before the synopsis. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return summary description of the command
         * @see UsageMessageSpec#header()
         * @see Help#header(Object...)  */
        String[] header() default {};

        /** Set the heading preceding the synopsis text. The default heading is {@code "Usage: "} (without a line break between the heading and the synopsis text).
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the synopsis text
         * @see Help#synopsisHeading(Object...)  */
        String synopsisHeading() default "Usage: ";

        /** Specify {@code true} to generate an abbreviated synopsis like {@code "<main> [OPTIONS] [PARAMETERS...]"}.
         * By default, a detailed synopsis with individual option names and parameters is generated.
         * @return whether the synopsis should be abbreviated
         * @see Help#abbreviatedSynopsis()
         * @see Help#detailedSynopsis(Comparator, boolean) */
        boolean abbreviateSynopsis() default false;

        /** Specify one or more custom synopsis lines to display instead of an auto-generated synopsis. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return custom synopsis text to replace the auto-generated synopsis
         * @see Help#customSynopsis(Object...) */
        String[] customSynopsis() default {};

        /** Set the heading preceding the description section.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the description section
         * @see Help#descriptionHeading(Object...)  */
        String descriptionHeading() default "";

        /** Optional text to display between the synopsis line(s) and the list of options. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return description of this command
         * @see Help#description(Object...) */
        String[] description() default {};

        /** Set the heading preceding the parameters list.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the parameters list
         * @see Help#parameterListHeading(Object...)  */
        String parameterListHeading() default "";

        /** Set the heading preceding the options list.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the options list
         * @see Help#optionListHeading(Object...)  */
        String optionListHeading() default "";

        /** Specify {@code false} to show Options in declaration order. The default is to sort alphabetically.
         * @return whether options should be shown in alphabetic order. */
        boolean sortOptions() default true;

        /** Prefix required options with this character in the options list. The default is no marker: the synopsis
         * indicates which options and parameters are required.
         * @return the character to show in the options list to mark required options */
        char requiredOptionMarker() default ' ';

        /** Class that can provide default values dynamically at runtime. An implementation may return default
         * value obtained from a configuration file like a properties file or some other source.
         * @return a Class that can provide default values dynamically at runtime
         * @since 3.6 */
        Class<? extends IDefaultValueProvider> defaultValueProvider() default NoDefaultProvider.class;

        /** Specify {@code true} to show default values in the description column of the options list (except for
         * boolean options). False by default.
         * <p>Note that picocli 3.2 allows {@linkplain Option#description() embedding default values} anywhere in the
         * option or positional parameter description that ignores this setting.</p>
         * @return whether the default values for options and parameters should be shown in the description column */
        boolean showDefaultValues() default false;

        /** Set the heading preceding the subcommands list. The default heading is {@code "Commands:%n"} (with a line break at the end).
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the subcommands list
         * @see Help#commandListHeading(Object...)  */
        String commandListHeading() default "Commands:%n";

        /** Set the heading preceding the footer section.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return the heading preceding the footer section
         * @see Help#footerHeading(Object...)  */
        String footerHeading() default "";

        /** Optional text to display after the list of options. Each element of the array is rendered on a separate line.
         * <p>May contain embedded {@linkplain java.util.Formatter format specifiers} like {@code %n} line separators. Literal percent {@code '%'} characters must be escaped with another {@code %}.</p>
         * @return text to display after the list of options
         * @see Help#footer(Object...) */
        String[] footer() default {};

        /**
         * Set {@code hidden=true} if this command should not be included in the list of commands in the usage help of the parent command.
         * @return whether this command should be excluded from the usage message
         * @since 3.0
         */
        boolean hidden() default false;

        /** Set the base name of the ResourceBundle to find option and positional parameters descriptions, as well as
         * usage help message sections and section headings. <p>See {@link Messages} for more details and an example.</p>
         * @return the base name of the ResourceBundle for usage help strings
         * @see ArgSpec#messages()
         * @see UsageMessageSpec#messages()
         * @see CommandSpec#resourceBundle()
         * @see CommandLine#setResourceBundle(ResourceBundle)
         * @since 3.6
         */
        String resourceBundle() default "";

        /** Set the {@link UsageMessageSpec#width(int) usage help message width}. The default is 80.
         * @since 3.7
         */
        int usageHelpWidth() default 80;

        /** Exit code for successful termination. {@value picocli.CommandLine.ExitCode#OK} by default.
         * @see #execute(String...)
         * @since 4.0 */
        int exitCodeOnSuccess() default ExitCode.OK;

        /** Exit code for successful termination after printing usage help on user request. {@value picocli.CommandLine.ExitCode#OK} by default.
         * @see #execute(String...)
         * @since 4.0 */
        int exitCodeOnUsageHelp() default ExitCode.OK;

        /** Exit code for successful termination after printing version help on user request. {@value picocli.CommandLine.ExitCode#OK} by default.
         * @see #execute(String...)
         * @since 4.0 */
        int exitCodeOnVersionHelp() default ExitCode.OK;

        /** Exit code for command line usage error. {@value picocli.CommandLine.ExitCode#USAGE} by default.
         * @see #execute(String...)
         * @since 4.0 */
        int exitCodeOnInvalidInput() default ExitCode.USAGE;

        /** Exit code signifying that an exception occurred when invoking the Runnable, Callable or Method user object of a command.
         * {@value picocli.CommandLine.ExitCode#SOFTWARE} by default.
         * @see #execute(String...)
         * @since 4.0 */
        int exitCodeOnExecutionException() default ExitCode.SOFTWARE;

        /** Set the heading preceding the exit codes section, may contain {@code "%n"} line separators. {@code ""} (empty string) by default.
         * @see Help#exitCodeListHeading(Object...)
         * @since 4.0 */
        String exitCodeListHeading() default "";

        /** Set the values to be displayed in the exit codes section as a list of {@code "key:value"} pairs:
         *  keys are exit codes, values are descriptions. Descriptions may contain {@code "%n"} line separators.
         * <p>For example:</p>
         * <pre>
         * &#064;Command(exitCodeListHeading = "Exit Codes:%n",
         *          exitCodeList = { " 0:Successful program execution.",
         *                           "64:Invalid input: an unknown option or invalid parameter was specified.",
         *                           "70:Execution exception: an exception occurred while executing the business logic."})
         * </pre>
         * @since 4.0 */
        String[] exitCodeList() default {};
    }
    /** A {@code Command} may define one or more {@code ArgGroups}: a group of options, positional parameters or a mixture of the two.
     * Groups can be used to:
     * <ul>
     *     <li>define <b>mutually exclusive</b> arguments. By default, options and positional parameters
     *     in a group are mutually exclusive. This can be controlled with the {@link #exclusive() exclusive} attribute.
     *     Picocli will throw a {@link MutuallyExclusiveArgsException} if the command line contains multiple arguments that are mutually exclusive.</li>
     *     <li>define a set of arguments that <b>must co-occur</b>. Set {@link #exclusive() exclusive = false}
     *     to define a group of options and positional parameters that must always be specified together.
     *     Picocli will throw a {@link MissingParameterException MissingParameterException} if not all the options and positional parameters in a co-occurring group are specified together.</li>
     *     <li>create an <b>option section</b> in the usage help message.
     *     To be shown in the usage help message, a group needs to have a {@link #heading() heading} (which may come from a {@linkplain #headingKey() resource bundle}).
     *     Groups without a heading are only used for validation.
     *     Set {@link #validate() validate = false} for groups whose purpose is only to customize the usage help message.</li>
     *     <li>define <b>composite repeating argument groups</b>. Groups may contain other groups to create composite groups.</li>
     * </ul>
     * <p>Groups may be optional ({@code multiplicity = "0..1"}), required ({@code multiplicity = "1"}), or repeating groups ({@code multiplicity = "0..*"} or {@code multiplicity = "1..*"}).
     * For a group of mutually exclusive arguments, making the group required means that one of the arguments in the group must appear on the command line, or a {@link MissingParameterException MissingParameterException} is thrown.
     * For a group of co-occurring arguments, all arguments in the group must appear on the command line.
     * </p>
     * <p>Groups can be composed for validation purposes:</p>
     * <ul>
     * <li>When the parent group is mutually exclusive, only one of the subgroups may be present.</li>
     * <li>When the parent group is a co-occurring group, all subgroups must be present.</li>
     * <li>When the parent group is required, at least one subgroup must be present.</li>
     * </ul>
     * <p>
     * Below is an example of an {@code ArgGroup} defining a set of dependent options that must occur together.
     * All options are required <em>within the group</em>, while the group itself is optional:</p>
     * <pre>
     * public class DependentOptions {
     *     &#064;ArgGroup(exclusive = false, multiplicity = "0..1")
     *     Dependent group;
     *
     *     static class Dependent {
     *         &#064;Option(names = "-a", required = true) int a;
     *         &#064;Option(names = "-b", required = true) int b;
     *         &#064;Option(names = "-c", required = true) int c;
     *     }
     * }</pre>
     * @see ArgGroupSpec
     * @since 4.0 */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    public @interface ArgGroup {
        /** The heading of this group, used when generating the usage documentation.
         * When neither a {@link #heading() heading} nor a {@link #headingKey() headingKey} are specified,
         * this group is used for validation only and does not change the usage help message. */
        String heading() default "__no_heading__";

        /** ResourceBundle key for this group's usage help message section heading.
         * When neither a {@link #heading() heading} nor a {@link #headingKey() headingKey} are specified,
         * this group is used for validation only and does not change the usage help message. */
        String headingKey() default "__no_heading_key__";
        /** Determines whether this is a mutually exclusive group; {@code true} by default.
         * If {@code false}, this is a co-occurring group. Ignored if {@link #validate()} is {@code false}. */
        boolean exclusive() default true;
        /** Determines how often this group can be specified on the command line; {@code "0..1"} (optional) by default.
         * For a group of mutually exclusive arguments, making the group required {@code multiplicity = "1"} means that
         * one of the arguments in the group must appear on the command line, or a MissingParameterException is thrown.
         * For a group of co-occurring arguments, making the group required means that all arguments in the group must appear on the command line.
         * Ignored if {@link #validate()} is {@code false}. */
        String multiplicity() default "0..1";
        /** Determines whether picocli should validate the rules of this group ({@code true} by default).
         * For a mutually exclusive group validation means verifying that no more than one elements of the group is specified on the command line;
         * for a co-ocurring group validation means verifying that all elements of the group are specified on the command line.
         * Set {@link #validate() validate = false} for groups whose purpose is only to customize the usage help message.
         * @see #multiplicity()
         * @see #heading() */
        boolean validate() default true;
        /** Determines the position in the options list in the usage help message at which this group should be shown.
         * Options with a lower number are shown before options with a higher number.
         * This attribute is only honored if {@link UsageMessageSpec#sortOptions()} is {@code false} for this command.*/
        int order() default -1;
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
     * commandLine.registerConverter(java.nio.file.Path.class, s -&gt; java.nio.file.Paths.get(s));
     * commandLine.registerConverter(java.time.Duration.class, s -&gt; java.time.Duration.parse(s));</pre>
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
         * Converts the specified command line argument value to some domain object.
         * @param value the command line argument String value
         * @return the resulting domain object
         * @throws Exception an exception detailing what went wrong during the conversion
         */
        K convert(String value) throws Exception;
    }

    /**
     * Provides version information for a command. Commands may configure a provider with the
     * {@link Command#versionProvider()} annotation attribute.
     * @since 2.2 */
    public interface IVersionProvider {
        /**
         * Returns version information for a command.
         * @return version information (each string in the array is displayed on a separate line)
         * @throws Exception an exception detailing what went wrong when obtaining version information
         */
        String[] getVersion() throws Exception;
    }
    private static class NoVersionProvider implements IVersionProvider {
        public String[] getVersion() throws Exception { throw new UnsupportedOperationException(); }
    }

    /**
     * Provides default value for a command. Commands may configure a provider with the
     * {@link Command#defaultValueProvider()} annotation attribute.
     * @since 3.6 */
    public interface IDefaultValueProvider {

        /** Returns the default value for an option or positional parameter or {@code null}.
        * The returned value is converted to the type of the option/positional parameter
        * via the same type converter used when populating this option/positional
        * parameter from a command line argument.
        * @param argSpec the option or positional parameter, never {@code null}
        * @return the default value for the option or positional parameter, or {@code null} if
        *       this provider has no default value for the specified option or positional parameter
        * @throws Exception when there was a problem obtaining the default value
        */
        String defaultValue(ArgSpec argSpec) throws Exception;
    }
    private static class NoDefaultProvider implements IDefaultValueProvider {
        public String defaultValue(ArgSpec argSpec) { throw new UnsupportedOperationException(); }
    }

    /**
     * Creates the {@link Help} instance used to render the usage help message.
     * @since 3.9
     */
    public interface IHelpFactory {
        /** Returns a {@code Help} instance to assist in rendering the usage help message
         * @param commandSpec the command to create usage help for
         * @param colorScheme the color scheme to use when rendering usage help
         * @return a {@code Help} instance
         */
        Help create(CommandSpec commandSpec, Help.ColorScheme colorScheme);
    }

    private static class DefaultHelpFactory implements IHelpFactory {
        public Help create(CommandSpec commandSpec, Help.ColorScheme colorScheme) {
            return new Help(commandSpec, colorScheme);
        }
    }

    /**
     * Factory for instantiating classes that are registered declaratively with annotation attributes, like
     * {@link Command#subcommands()}, {@link Option#converter()}, {@link Parameters#converter()} and {@link Command#versionProvider()}.
     * <p>The default factory implementation simply creates a new instance of the specified class when {@link #create(Class)} is invoked.
     * </p><p>
     * You may provide a custom implementation of this interface.
     * For example, a custom factory implementation could delegate to a dependency injection container that provides the requested instance.
     * </p>
     * @see picocli.CommandLine#CommandLine(Object, IFactory)
     * @see #call(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)
     * @see #run(Class, IFactory, PrintStream, PrintStream, Help.Ansi, String...)
     * @since 2.2 */
    public interface IFactory {
        /**
         * Returns an instance of the specified class.
         * @param cls the class of the object to return
         * @param <K> the type of the object to return
         * @return the instance
         * @throws Exception an exception detailing what went wrong when creating or obtaining the instance
         */
        <K> K create(Class<K> cls) throws Exception;
    }
    /** Returns the default {@link IFactory} implementation used if no factory was specified in the {@link #CommandLine(Object) CommandLine constructor}.
     * @since 4.0 */
    public static IFactory defaultFactory() { return new DefaultFactory(); }
    private static class DefaultFactory implements IFactory {
        public <T> T create(Class<T> cls) throws Exception {
            if (cls.isInterface() && Collection.class.isAssignableFrom(cls)) {
                if (List.class.isAssignableFrom(cls)) {
                    return cls.cast(new ArrayList<Object>());
                } else if (SortedSet.class.isAssignableFrom(cls)) {
                    return cls.cast(new TreeSet<Object>());
                } else if (Set.class.isAssignableFrom(cls)) {
                    return cls.cast(new LinkedHashSet<Object>());
                } else if (Queue.class.isAssignableFrom(cls)) {
                    return cls.cast(new LinkedList<Object>()); // ArrayDeque is only available since 1.6
                }
                return cls.cast(new ArrayList<Object>());
            }
            if (Map.class.isAssignableFrom(cls)) {
                try { // if it is an implementation class, instantiate it
                    return cls.cast(cls.getDeclaredConstructor().newInstance());
                } catch (Exception ignored) { }
                return cls.cast(new LinkedHashMap<Object, Object>());
            }
            try {
                return cls.newInstance();
            } catch (Exception ex) {
                Constructor<T> constructor = cls.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        }
        private static ITypeConverter<?>[] createConverter(IFactory factory, Class<? extends ITypeConverter<?>>[] classes) {
            ITypeConverter<?>[] result = new ITypeConverter<?>[classes.length];
            for (int i = 0; i < classes.length; i++) { result[i] = create(factory, classes[i]); }
            return result;
        }
        static IVersionProvider createVersionProvider(IFactory factory, Class<? extends IVersionProvider> cls) {
            return create(factory, cls);
        }
        static IDefaultValueProvider createDefaultValueProvider(IFactory factory, Class<? extends IDefaultValueProvider> cls) {
            return create(factory, cls);
        }
        static Iterable<String> createCompletionCandidates(IFactory factory, Class<? extends Iterable<String>> cls) {
            return create(factory, cls);
        }
        static <T> T create(IFactory factory, Class<T> cls) {
            try { return factory.create(cls); }
            catch (Exception ex) { throw new InitializationException("Could not instantiate " + cls + ": " + ex, ex); }
        }
    }
    /** Describes the number of parameters required and accepted by an option or a positional parameter.
     * @since 0.9.7
     */
    @SuppressWarnings("deprecation")
    public static class Range implements Comparable<Range> {
        /** @deprecated use {@link #min()} instead */
        @Deprecated public final int min;
        /** @deprecated use {@link #max()} instead */
        @Deprecated public final int max;
        /** @deprecated use {@link #isVariable()} instead */
        @Deprecated public final boolean isVariable;
        private final boolean isUnspecified;
        private final String originalValue;

        /** Constructs a new Range object with the specified parameters.
         * @param min minimum number of required parameters
         * @param max maximum number of allowed parameters (or Integer.MAX_VALUE if variable)
         * @param variable {@code true} if any number or parameters is allowed, {@code false} otherwise
         * @param unspecified {@code true} if no arity was specified on the option/parameter (value is based on type)
         * @param originalValue the original value that was specified on the option or parameter
         */
        public Range(int min, int max, boolean variable, boolean unspecified, String originalValue) {
            if (min < 0 || max < 0) { throw new InitializationException("Invalid negative range (min=" + min + ", max=" + max + ")"); }
            if (min > max) { throw new InitializationException("Invalid range (min=" + min + ", max=" + max + ")"); }
            this.min = min;
            this.max = max;
            this.isVariable = variable;
            this.isUnspecified = unspecified;
            this.originalValue = originalValue;
        }
        /** Returns a new {@code Range} based on the {@link Option#arity()} annotation on the specified field,
         * or the field type's default arity if no arity was specified.
         * @param field the field whose Option annotation to inspect
         * @return a new {@code Range} based on the Option arity annotation on the specified field */
        public static Range optionArity(Field field) { return optionArity(new TypedMember(field)); }
        private static Range optionArity(IAnnotatedElement member) {
            return member.isAnnotationPresent(Option.class)
                    ? adjustForType(Range.valueOf(member.getAnnotation(Option.class).arity()), member)
                    : new Range(0, 0, false, true, "0");
        }
        /** Returns a new {@code Range} based on the {@link Parameters#arity()} annotation on the specified field,
         * or the field type's default arity if no arity was specified.
         * @param field the field whose Parameters annotation to inspect
         * @return a new {@code Range} based on the Parameters arity annotation on the specified field */
        public static Range parameterArity(Field field) { return parameterArity(new TypedMember(field)); }
        private static Range parameterArity(IAnnotatedElement member) {
            if (member.isAnnotationPresent(Parameters.class)) {
                return adjustForType(Range.valueOf(member.getAnnotation(Parameters.class).arity()), member);
            } else {
                return member.isMethodParameter()
                        ? adjustForType(Range.valueOf(""), member)
                        : new Range(0, 0, false, true, "0");
            }
        }
        /** Returns a new {@code Range} based on the {@link Parameters#index()} annotation on the specified field.
         * @param field the field whose Parameters annotation to inspect
         * @return a new {@code Range} based on the Parameters index annotation on the specified field */
        public static Range parameterIndex(Field field) { return parameterIndex(new TypedMember(field)); }
        private static Range parameterIndex(IAnnotatedElement member) {
            if (member.isAnnotationPresent(Parameters.class)) {
                Range result = Range.valueOf(member.getAnnotation(Parameters.class).index());
                if (!result.isUnspecified) { return result; }
            }
            if (member.isMethodParameter()) {
                int min = member.getMethodParamPosition();
                int max = member.isMultiValue() ? Integer.MAX_VALUE : min;
                return new Range(min, max, member.isMultiValue(), false, "");
            }
            return Range.valueOf("*"); // the default
        }
        static Range adjustForType(Range result, IAnnotatedElement member) {
            return result.isUnspecified ? defaultArity(member) : result;
        }
        /** Returns the default arity {@code Range}: for interactive options/positional parameters,
         * this is 0; for {@link Option options} this is 0 for booleans and 1 for
         * other types, for {@link Parameters parameters} booleans have arity 0, arrays or Collections have
         * arity "0..*", and other types have arity 1.
         * @param field the field whose default arity to return
         * @return a new {@code Range} indicating the default arity of the specified field
         * @since 2.0 */
        public static Range defaultArity(Field field) { return defaultArity(new TypedMember(field)); }
        private static Range defaultArity(IAnnotatedElement member) {
            if (member.isInteractive()) { return Range.valueOf("0").unspecified(true); }
            ITypeInfo info = member.getTypeInfo();
            if (member.isAnnotationPresent(Option.class)) {
                boolean zeroArgs = info.isBoolean() || (info.isMultiValue() && info.getAuxiliaryTypeInfos().get(0).isBoolean());
                return zeroArgs ? Range.valueOf("0").unspecified(true)
                                : Range.valueOf("1").unspecified(true);
            }
            if (info.isMultiValue()) {
                return Range.valueOf("0..1").unspecified(true);
            }
            return Range.valueOf("1").unspecified(true);// for single-valued fields (incl. boolean positional parameters)
        }
        /** Returns the default arity {@code Range} for {@link Option options}: booleans have arity 0, other types have arity 1.
         * @param type the type whose default arity to return
         * @return a new {@code Range} indicating the default arity of the specified type
         * @deprecated use {@link #defaultArity(Field)} instead */
        @Deprecated public static Range defaultArity(Class<?> type) {
            return isBoolean(type) ? Range.valueOf("0").unspecified(true) : Range.valueOf("1").unspecified(true);
        }
        private int size() { return 1 + max - min; }
        static Range parameterCapacity(IAnnotatedElement member) {
            Range arity = parameterArity(member);
            if (!member.isMultiValue()) { return arity; }
            Range index = parameterIndex(member);
            return parameterCapacity(arity, index);
        }
        private static Range parameterCapacity(Range arity, Range index) {
            if (arity.max == 0)    { return arity; }
            if (index.size() == 1) { return arity; }
            if (index.isVariable)  { return Range.valueOf(arity.min + "..*"); }
            if (arity.size() == 1) { return Range.valueOf(arity.min * index.size() + ""); }
            if (arity.isVariable)  { return Range.valueOf(arity.min * index.size() + "..*"); }
            return Range.valueOf(arity.min * index.size() + ".." + arity.max * index.size());
        }

        /** Leniently parses the specified String as an {@code Range} value and return the result. A range string can
         * be a fixed integer value or a range of the form {@code MIN_VALUE + ".." + MAX_VALUE}. If the
         * {@code MIN_VALUE} string is not numeric, the minimum is zero. If the {@code MAX_VALUE} is not numeric, the
         * range is taken to be variable and the maximum is {@code Integer.MAX_VALUE}.
         * @param range the value range string to parse
         * @return a new {@code Range} value */
        public static Range valueOf(String range) {
            if (range.contains("${")) {
                return new Range(0, 0, false, false, range); // unresolved
            }
            range = range.trim();
            boolean unspecified = range.length() == 0 || range.startsWith(".."); // || range.endsWith("..");
            int min = -1, max = -1;
            boolean variable = false;
            int dots = -1;
            if ((dots = range.indexOf("..")) >= 0) {
                min = parseInt(range.substring(0, dots), 0);
                max = parseInt(range.substring(dots + 2), Integer.MAX_VALUE);
                variable = max == Integer.MAX_VALUE;
            } else {
                max = parseInt(range, Integer.MAX_VALUE);
                variable = max == Integer.MAX_VALUE;
                min = variable ? 0 : max;
            }
            Range result = new Range(min, max, variable, unspecified, range);
            return result;
        }
        private static int parseInt(String str, int defaultValue) {
            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        /** Returns a new Range object with the {@code min} value replaced by the specified value.
         * The {@code max} of the returned Range is guaranteed not to be less than the new {@code min} value.
         * @param newMin the {@code min} value of the returned Range object
         * @return a new Range object with the specified {@code min} value */
        public Range min(int newMin) { return new Range(newMin, Math.max(newMin, max), isVariable, isUnspecified, originalValue); }

        /** Returns a new Range object with the {@code max} value replaced by the specified value.
         * The {@code min} of the returned Range is guaranteed not to be greater than the new {@code max} value.
         * @param newMax the {@code max} value of the returned Range object
         * @return a new Range object with the specified {@code max} value */
        public Range max(int newMax) { return new Range(Math.min(min, newMax), newMax, isVariable, isUnspecified, originalValue); }

        /** Returns a new Range object with the {@code isUnspecified} value replaced by the specified value.
         * @param unspecified the {@code unspecified} value of the returned Range object
         * @return a new Range object with the specified {@code unspecified} value */
        public Range unspecified(boolean unspecified) { return new Range(min, max, isVariable, unspecified, originalValue); }
        /** Returns {@code true} if this Range is a default value, {@code false} if the user specified this value.
         * @since 4.0 */
        public boolean isUnspecified() { return isUnspecified; }
        /** Returns {@code true} if this range contains variables that have not been expanded yet,
         * {@code false} if this Range does not contain any variables.
         * @since 4.0 */
        public boolean isUnresolved() { return originalValue != null && originalValue.contains("${"); }
        /** Returns the lower bound of this range (inclusive).
         * @since 4.0 */
        public int min() { return min; }
        /** Returns the upper bound of this range (inclusive), or {@code Integer.MAX_VALUE} if this range has {@linkplain #isVariable() no upper bound}.
         * @since 4.0 */
        public int max() { return max; }
        /** Returns {@code true} if this range has no fixed upper bound.
         * @since 4.0 */
        public boolean isVariable() { return isVariable; }

        /**
         * Returns {@code true} if this Range includes the specified value, {@code false} otherwise.
         * @param value the value to check
         * @return {@code true} if the specified value is not less than the minimum and not greater than the maximum of this Range
         */
        public boolean contains(int value) { return min <= value && max >= value; }

        public boolean equals(Object object) {
            if (!(object instanceof Range)) { return false; }
            Range other = (Range) object;
            return other.max == this.max && other.min == this.min && other.isVariable == this.isVariable;
        }
        public int hashCode() {
            return ((17 * 37 + max) * 37 + min) * 37 + (isVariable ? 1 : 0);
        }
        public String toString() {
            if (isUnresolved()) { return originalValue; }
            return min == max ? String.valueOf(min) : min + ".." + (isVariable ? "*" : max);
        }
        public int compareTo(Range other) {
            int result = min - other.min;
            return (result == 0) ? max - other.max : result;
        }
        /** Returns true for these ranges: 0 and 0..1. */
        boolean isValidForInteractiveArgs() { return (min == 0 && (max == 0 || max == 1)); }
        boolean overlaps(Range index) {
            return contains(index.min) || contains(index.max) || index.contains(min) || index.contains(max);
        }
    }
    private static void validatePositionalParameters(List<PositionalParamSpec> positionalParametersFields) {
        int min = 0;
        for (PositionalParamSpec positional : positionalParametersFields) {
            Range index = positional.index();
            if (index.min > min) {
                throw new ParameterIndexGapException("Command definition should have a positional parameter with index=" + min +
                        ". Nearest positional parameter '" + positional.paramLabel() + "' has index=" + index.min);
            }
            min = Math.max(min, index.max);
            min = min == Integer.MAX_VALUE ? min : min + 1;
        }
    }
    @SuppressWarnings("unchecked") private static Stack<String> copy(Stack<String> stack) { return (Stack<String>) stack.clone(); }
    private static <T> Stack<T> reverse(Stack<T> stack) {
        Collections.reverse(stack);
        return stack;
    }
    private static <T> List<T> reverseList(List<T> list) {
        Collections.reverse(list);
        return list;
    }

    /** This class provides a namespace for classes and interfaces that model concepts and attributes of command line interfaces in picocli.
     * @since 3.0 */
    public static final class Model {
        private Model() {}

        /** The scope of a binding is the context where the current value should be gotten from or set to.
         * For a field, the scope is the object whose field value to get/set. For a method binding, it is the
         * object on which the method should be invoked.
         * <p>The getter and setter of the scope allow you to change the object onto which the option and positional parameter getters and setters should be applied.</p>
         * @since 4.0
         */
        public interface IScope extends IGetter, ISetter {}

        /** Customizable getter for obtaining the current value of an option or positional parameter.
         * When an option or positional parameter is matched on the command line, its getter or setter is invoked to capture the value.
         * For example, an option can be bound to a field or a method, and when the option is matched on the command line, the
         * field's value is set or the method is invoked with the option parameter value.
         * @since 3.0 */
        public static interface IGetter {
            /** Returns the current value of the binding. For multi-value options and positional parameters,
             * this method returns an array, collection or map to add values to.
             * @throws PicocliException if a problem occurred while obtaining the current value
             * @throws Exception internally, picocli call sites will catch any exceptions thrown from here and rethrow them wrapped in a PicocliException */
            <T> T get() throws Exception;
        }
        /** Customizable setter for modifying the value of an option or positional parameter.
         * When an option or positional parameter is matched on the command line, its setter is invoked to capture the value.
         * For example, an option can be bound to a field or a method, and when the option is matched on the command line, the
         * field's value is set or the method is invoked with the option parameter value.
         * @since 3.0 */
        public static interface ISetter {
            /** Sets the new value of the option or positional parameter.
             *
             * @param value the new value of the option or positional parameter
             * @param <T> type of the value
             * @return the previous value of the binding (if supported by this binding)
             * @throws PicocliException if a problem occurred while setting the new value
             * @throws Exception internally, picocli call sites will catch any exceptions thrown from here and rethrow them wrapped in a PicocliException */
            <T> T set(T value) throws Exception;
        }

        /** The {@code CommandSpec} class models a command specification, including the options, positional parameters and subcommands
         * supported by the command, as well as attributes for the version help message and the usage help message of the command.
         * <p>
         * Picocli views a command line application as a hierarchy of commands: there is a top-level command (usually the Java
         * class with the {@code main} method) with optionally a set of command line options, positional parameters and subcommands.
         * Subcommands themselves can have options, positional parameters and nested sub-subcommands to any level of depth.
         * </p><p>
         * The object model has a corresponding hierarchy of {@code CommandSpec} objects, each with a set of {@link OptionSpec},
         * {@link PositionalParamSpec} and {@linkplain CommandLine subcommands} associated with it.
         * This object model is used by the picocli command line interpreter and help message generator.
         * </p><p>Picocli can construct a {@code CommandSpec} automatically from classes with {@link Command @Command}, {@link Option @Option} and
         * {@link Parameters @Parameters} annotations. Alternatively a {@code CommandSpec} can be constructed programmatically.
         * </p>
         * @since 3.0 */
        public static class CommandSpec {
            /** Constant String holding the default program name: {@code "<main class>" }. */
            static final String DEFAULT_COMMAND_NAME = "<main class>";

            /** Constant Boolean holding the default setting for whether this is a help command: <code>{@value}</code>.*/
            static final Boolean DEFAULT_IS_HELP_COMMAND = false;

            /** Constant Boolean holding the default setting for whether method commands should be added as subcommands: <code>{@value}</code>.*/
            static final Boolean DEFAULT_IS_ADD_METHOD_SUBCOMMANDS = true;

            /** Constant Boolean holding the default setting for whether variables should be interpolated in String values: <code>{@value}</code>.*/
            static final Boolean DEFAULT_INTERPOLATE_VARIABLES = true;

            private final Map<String, CommandLine> commands = new LinkedHashMap<String, CommandLine>();
            private final Map<String, OptionSpec> optionsByNameMap = new LinkedHashMap<String, OptionSpec>();
            private final Map<Character, OptionSpec> posixOptionsByKeyMap = new LinkedHashMap<Character, OptionSpec>();
            private final Map<String, CommandSpec> mixins = new LinkedHashMap<String, CommandSpec>();
            private final List<ArgSpec> requiredArgs = new ArrayList<ArgSpec>();
            private final List<ArgSpec> args = new ArrayList<ArgSpec>();
            private final List<OptionSpec> options = new ArrayList<OptionSpec>();
            private final List<PositionalParamSpec> positionalParameters = new ArrayList<PositionalParamSpec>();
            private final List<UnmatchedArgsBinding> unmatchedArgs = new ArrayList<UnmatchedArgsBinding>();
            private final List<ArgGroupSpec> groups = new ArrayList<ArgGroupSpec>();
            private final ParserSpec parser = new ParserSpec();
            private final Interpolator interpolator = new Interpolator(this);
            private final UsageMessageSpec usageMessage = new UsageMessageSpec(interpolator);

            private final Object userObject;
            private CommandLine commandLine;
            private CommandSpec parent;
            private Boolean isAddMethodSubcommands;
            private Boolean interpolateVariables;

            private String name;
            private Set<String> aliases = new LinkedHashSet<String>();
            private Boolean isHelpCommand;
            private IVersionProvider versionProvider;
            private IDefaultValueProvider defaultValueProvider;
            private String[] version;
            private String toString;

            private Integer exitCodeOnSuccess;
            private Integer exitCodeOnUsageHelp;
            private Integer exitCodeOnVersionHelp;
            private Integer exitCodeOnInvalidInput;
            private Integer exitCodeOnExecutionException;

            private CommandSpec(Object userObject) { this.userObject = userObject; }
    
            /** Creates and returns a new {@code CommandSpec} without any associated user object. */
            public static CommandSpec create() { return wrapWithoutInspection(null); }
    
            /** Creates and returns a new {@code CommandSpec} with the specified associated user object.
             * The specified user object is <em>not</em> inspected for annotations.
             * @param userObject the associated user object. May be any object, may be {@code null}.
             */
            public static CommandSpec wrapWithoutInspection(Object userObject) { return new CommandSpec(userObject); }

            /** Creates and returns a new {@code CommandSpec} initialized from the specified associated user object. The specified
             * user object must have at least one {@link Command}, {@link Option} or {@link Parameters} annotation.
             * @param userObject the user object annotated with {@link Command}, {@link Option} and/or {@link Parameters} annotations.
             * @throws InitializationException if the specified object has no picocli annotations or has invalid annotations
             */
            public static CommandSpec forAnnotatedObject(Object userObject) { return forAnnotatedObject(userObject, new DefaultFactory()); }

            /** Creates and returns a new {@code CommandSpec} initialized from the specified associated user object. The specified
             * user object must have at least one {@link Command}, {@link Option} or {@link Parameters} annotation.
             * @param userObject the user object annotated with {@link Command}, {@link Option} and/or {@link Parameters} annotations.
             * @param factory the factory used to create instances of {@linkplain Command#subcommands() subcommands}, {@linkplain Option#converter() converters}, etc., that are registered declaratively with annotation attributes
             * @throws InitializationException if the specified object has no picocli annotations or has invalid annotations
             */
            public static CommandSpec forAnnotatedObject(Object userObject, IFactory factory) { return CommandReflection.extractCommandSpec(userObject, factory, true); }

            /** Creates and returns a new {@code CommandSpec} initialized from the specified associated user object. If the specified
             * user object has no {@link Command}, {@link Option} or {@link Parameters} annotations, an empty {@code CommandSpec} is returned.
             * @param userObject the user object annotated with {@link Command}, {@link Option} and/or {@link Parameters} annotations.
             * @throws InitializationException if the specified object has invalid annotations
             */
            public static CommandSpec forAnnotatedObjectLenient(Object userObject) { return forAnnotatedObjectLenient(userObject, new DefaultFactory()); }

            /** Creates and returns a new {@code CommandSpec} initialized from the specified associated user object. If the specified
             * user object has no {@link Command}, {@link Option} or {@link Parameters} annotations, an empty {@code CommandSpec} is returned.
             * @param userObject the user object annotated with {@link Command}, {@link Option} and/or {@link Parameters} annotations.
             * @param factory the factory used to create instances of {@linkplain Command#subcommands() subcommands}, {@linkplain Option#converter() converters}, etc., that are registered declaratively with annotation attributes
             * @throws InitializationException if the specified object has invalid annotations
             */
            public static CommandSpec forAnnotatedObjectLenient(Object userObject, IFactory factory) { return CommandReflection.extractCommandSpec(userObject, factory, false); }

            /** Ensures all attributes of this {@code CommandSpec} have a valid value; throws an {@link InitializationException} if this cannot be achieved. */
            void validate() {
                Collections.sort(positionalParameters, new PositionalParametersSorter());
                validatePositionalParameters(positionalParameters);
                List<String> wrongUsageHelpAttr = new ArrayList<String>();
                List<String> wrongVersionHelpAttr = new ArrayList<String>();
                List<String> usageHelpAttr = new ArrayList<String>();
                List<String> versionHelpAttr = new ArrayList<String>();
                for (OptionSpec option : options()) {
                    if (option.usageHelp()) {
                        usageHelpAttr.add(option.longestName());
                        if (!isBoolean(option.type())) { wrongUsageHelpAttr.add(option.longestName()); }
                    }
                    if (option.versionHelp()) {
                        versionHelpAttr.add(option.longestName());
                        if (!isBoolean(option.type())) { wrongVersionHelpAttr.add(option.longestName()); }
                    }
                }
                String wrongType = "Non-boolean options like %s should not be marked as '%s=true'. Usually a command has one %s boolean flag that triggers display of the %s. Alternatively, consider using @Command(mixinStandardHelpOptions = true) on your command instead.";
                String multiple = "Multiple options %s are marked as '%s=true'. Usually a command has only one %s option that triggers display of the %s. Alternatively, consider using @Command(mixinStandardHelpOptions = true) on your command instead.%n";
                if (!wrongUsageHelpAttr.isEmpty()) {
                    throw new InitializationException(String.format(wrongType, wrongUsageHelpAttr, "usageHelp", "--help", "usage help message"));
                }
                if (!wrongVersionHelpAttr.isEmpty()) {
                    throw new InitializationException(String.format(wrongType, wrongVersionHelpAttr, "versionHelp", "--version", "version information"));
                }
                if (usageHelpAttr.size() > 1)   { new Tracer().warn(multiple, usageHelpAttr, "usageHelp", "--help", "usage help message"); }
                if (versionHelpAttr.size() > 1) { new Tracer().warn(multiple, versionHelpAttr, "versionHelp", "--version", "version information"); }
            }
    
            /** Returns the user object associated with this command.
             * @see CommandLine#getCommand() */
            public Object userObject() { return userObject; }
    
            /** Returns the CommandLine constructed with this {@code CommandSpec} model. */
            public CommandLine commandLine() { return commandLine;}
    
            /** Sets the CommandLine constructed with this {@code CommandSpec} model. */
            protected CommandSpec commandLine(CommandLine commandLine) {
                this.commandLine = commandLine;
                for (CommandSpec mixedInSpec : mixins.values()) {
                    mixedInSpec.commandLine(commandLine);
                }
                for (CommandLine sub : commands.values()) {
                    sub.getCommandSpec().parent(this);
                }
                return this;
            }

            /** Returns the parser specification for this command. */
            public ParserSpec parser() { return parser; }
            /** Initializes the parser specification for this command from the specified settings and returns this commandSpec.*/
            public CommandSpec parser(ParserSpec settings) { parser.initFrom(settings); return this; }

            /** Returns the usage help message specification for this command. */
            public UsageMessageSpec usageMessage() { return usageMessage; }
            /** Initializes the usageMessage specification for this command from the specified settings and returns this commandSpec.*/
            public CommandSpec usageMessage(UsageMessageSpec settings) { usageMessage.initFrom(settings, this); return this; }

            /** Returns the resource bundle base name for this command.
             * @return the resource bundle base name from the {@linkplain UsageMessageSpec#messages()}
             * @since 4.0 */
            public String resourceBundleBaseName() { return Messages.resourceBundleBaseName(usageMessage.messages()); }
            /** Initializes the resource bundle for this command: sets the {@link UsageMessageSpec#messages(Messages) UsageMessageSpec.messages} to
             * a {@link Messages Messages} object created from this command spec and the specified bundle, and then sets the
             * {@link ArgSpec#messages(Messages) ArgSpec.messages} of all options and positional parameters in this command
             * to the same {@code Messages} instance. Subcommands are not modified.
             * <p>This method is preferable to {@link #resourceBundle(ResourceBundle)} for pre-Java 8</p>
             * @param resourceBundleBaseName the base name of the ResourceBundle to set, may be {@code null}
             * @return this commandSpec
             * @see #addSubcommand(String, CommandLine)
             * @since 4.0 */
            public CommandSpec resourceBundleBaseName(String resourceBundleBaseName) {
                ResourceBundle bundle = empty(resourceBundleBaseName) ? null : ResourceBundle.getBundle(resourceBundleBaseName);
                setBundle(resourceBundleBaseName, bundle);
                return this;
            }
            /** Returns the resource bundle for this command.
             * @return the resource bundle from the {@linkplain UsageMessageSpec#messages()}
             * @since 3.6 */
            public ResourceBundle resourceBundle() { return Messages.resourceBundle(usageMessage.messages()); }
            /** Initializes the resource bundle for this command: sets the {@link UsageMessageSpec#messages(Messages) UsageMessageSpec.messages} to
             * a {@link Messages Messages} object created from this command spec and the specified bundle, and then sets the
             * {@link ArgSpec#messages(Messages) ArgSpec.messages} of all options and positional parameters in this command
             * to the same {@code Messages} instance. Subcommands are not modified.
             * @param bundle the ResourceBundle to set, may be {@code null}
             * @return this commandSpec
             * @see #addSubcommand(String, CommandLine)
             * @since 3.6 */
            public CommandSpec resourceBundle(ResourceBundle bundle) {
                setBundle(Messages.extractName(bundle), bundle);
                return this;
            }
            private void setBundle(String bundleBaseName, ResourceBundle bundle) {
                usageMessage().messages(new Messages(this, bundleBaseName, bundle));
                updateArgSpecMessages();
            }
            private void updateArgSpecMessages() {
                for (OptionSpec opt : options()) { opt.messages(usageMessage().messages()); }
                for (PositionalParamSpec pos : positionalParameters()) { pos.messages(usageMessage().messages()); }
                for (ArgGroupSpec group : argGroups()) { group.messages(usageMessage().messages()); }
            }

            /** Returns a read-only view of the subcommand map. */
            public Map<String, CommandLine> subcommands() { return Collections.unmodifiableMap(commands); }
    
            /** Adds the specified subcommand with the specified name.
             * If the specified subcommand does not have a ResourceBundle set, it is initialized to the ResourceBundle of this command spec.
             * @param name subcommand name - the preferred subcommand name to register the subcommand under.
             *             If {@code null}, the {@linkplain CommandSpec#name() name} of the specified subcommand is used;
             *             if this is also {@code null}, the first {@linkplain CommandSpec#aliases() alias} is used.
             *             When this String is encountered in the command line arguments, the subcommand is invoked.
             * @param subcommand describes the subcommand to envoke when the name is encountered on the command line
             * @return this {@code CommandSpec} object for method chaining
             * @throws InitializationException if the specified name is {@code null}, and no alternative name could be found,
             *          or if another subcommand was already registered under the same name, or if one of the aliases
             *          of the specified subcommand was already used by another subcommand.
             */
            public CommandSpec addSubcommand(String name, CommandSpec subcommand) {
                return addSubcommand(name, new CommandLine(subcommand));
            }
    
            /** Adds the specified subcommand with the specified name.
             * If the specified subcommand does not have a ResourceBundle set, it is initialized to the ResourceBundle of this command spec.
             * @param name subcommand name - the preferred subcommand name to register the subcommand under.
             *             If {@code null}, the {@linkplain CommandLine#getCommandName() name} of the specified subcommand is used;
             *             if this is also {@code null}, the first {@linkplain CommandSpec#aliases() alias} is used.
             *             When this String is encountered in the command line arguments, the subcommand is invoked.
             * @param subCommandLine the subcommand to envoke when the name is encountered on the command line
             * @return this {@code CommandSpec} object for method chaining
             * @throws InitializationException if the specified name is {@code null}, and no alternative name could be found,
             *          or if another subcommand was already registered under the same name, or if one of the aliases
             *          of the specified subcommand was already used by another subcommand.
             */
            public CommandSpec addSubcommand(String name, CommandLine subCommandLine) {
                CommandSpec subSpec = subCommandLine.getCommandSpec();
                String actualName = validateSubcommandName(name, subSpec);
                Tracer t = new Tracer();
                if (t.isDebug()) {t.debug("Adding subcommand '%s' to '%s'%n", actualName, this.qualifiedName());}
                CommandLine previous = commands.put(actualName, subCommandLine);
                if (previous != null && previous != subCommandLine) { throw new InitializationException("Another subcommand named '" + actualName + "' already exists for command '" + this.name() + "'"); }
                if (subSpec.name == null) { subSpec.name(actualName); }
                subSpec.parent(this);
                for (String alias : subSpec.aliases()) {
                    if (t.isDebug()) {t.debug("Adding alias '%s' for subcommand '%s' to '%s'%n", alias, actualName, this.qualifiedName());}
                    previous = commands.put(alias, subCommandLine);
                    if (previous != null && previous != subCommandLine) { throw new InitializationException("Alias '" + alias + "' for subcommand '" + actualName + "' is already used by another subcommand of '" + this.name() + "'"); }
                }
                subSpec.initCommandHierarchyWithResourceBundle(resourceBundleBaseName(), resourceBundle());
                return this;
            }

            private String validateSubcommandName(String name, CommandSpec subSpec) {
                String result = name == null ? subSpec.name : name; // NOTE: check subSpec.name field, not subSpec.name()!
                if (result == null && !subSpec.aliases.isEmpty()) {
                    Iterator<String> iter = subSpec.aliases.iterator();
                    result = iter.next();
                    iter.remove();
                }
                if (result == null) {
                    throw new InitializationException("Cannot add subcommand with null name to " + this.qualifiedName());
                }
                return result;
            }

            private void initCommandHierarchyWithResourceBundle(String bundleBaseName, ResourceBundle rb) {
                if (resourceBundle() == null) {
                    setBundle(bundleBaseName, rb);
                }
                for (CommandLine sub : commands.values()) { // percolate down the hierarchy
                    sub.getCommandSpec().initCommandHierarchyWithResourceBundle(bundleBaseName, rb);
                }
            }

            /** Returns whether method commands should be added as subcommands. True by default. Used by the annotation processor.
             * @since 4.0 */
            public boolean isAddMethodSubcommands() { return (isAddMethodSubcommands == null) ? DEFAULT_IS_ADD_METHOD_SUBCOMMANDS : isAddMethodSubcommands; }
            /** Sets whether method commands should be added as subcommands. True by default. Used by the annotation processor.
             * @since 4.0 */
            public CommandSpec setAddMethodSubcommands(Boolean addMethodSubcommands) { isAddMethodSubcommands = addMethodSubcommands; return this; }

            /** Returns whether whether variables should be interpolated in String values. {@value #DEFAULT_INTERPOLATE_VARIABLES} by default.
             * @since 4.0 */
            public boolean interpolateVariables() { return (interpolateVariables == null) ? DEFAULT_INTERPOLATE_VARIABLES : interpolateVariables; }
            /** Sets whether whether variables should be interpolated in String values. {@value #DEFAULT_INTERPOLATE_VARIABLES} by default.
             * @since 4.0 */
            public CommandSpec interpolateVariables(Boolean interpolate) { interpolateVariables = interpolate; return this; }

            /** Reflects on the class of the {@linkplain #userObject() user object} and registers any command methods
             * (class methods annotated with {@code @Command}) as subcommands.
             *
             * @return this {@link CommandSpec} object for method chaining
             * @see #addMethodSubcommands(IFactory)
             * @see #addSubcommand(String, CommandLine)
             * @since 3.6.0
             */
            public CommandSpec addMethodSubcommands() { return addMethodSubcommands(new DefaultFactory()); }

            /** Reflects on the class of the {@linkplain #userObject() user object} and registers any command methods
             * (class methods annotated with {@code @Command}) as subcommands.
             * @param factory the factory used to create instances of subcommands, converters, etc., that are registered declaratively with annotation attributes
             * @return this {@link CommandSpec} object for method chaining
             * @see #addSubcommand(String, CommandLine)
             * @since 3.7.0
             */
            public CommandSpec addMethodSubcommands(IFactory factory) {
                if (userObject() instanceof Method) {
                     throw new InitializationException("Cannot discover subcommand methods of this Command Method: " + userObject());
                }
                for (CommandLine sub : createMethodSubcommands(userObject().getClass(), factory)) {
                    addSubcommand(sub.getCommandName(), sub);
                }
                isAddMethodSubcommands = true;
                return this;
            }
            static List<CommandLine> createMethodSubcommands(Class<?> cls, IFactory factory) {
                List<CommandLine> result = new ArrayList<CommandLine>();
                for (Method method : getCommandMethods(cls, null)) {
                    result.add(new CommandLine(method, factory));
                }
                return result;
            }

            /** Returns the parent command of this subcommand, or {@code null} if this is a top-level command. */
            public CommandSpec parent() { return parent; }
    
            /** Sets the parent command of this subcommand.
             * @return this CommandSpec for method chaining */
            public CommandSpec parent(CommandSpec parent) { this.parent = parent; return this; }
    
            /** Adds the specified option spec or positional parameter spec to the list of configured arguments to expect.
             * @param arg the option spec or positional parameter spec to add
             * @return this CommandSpec for method chaining */
            public CommandSpec add(ArgSpec arg) { return arg.isOption() ? addOption((OptionSpec) arg) : addPositional((PositionalParamSpec) arg); }
    
            /** Adds the specified option spec to the list of configured arguments to expect.
             * The option's {@linkplain OptionSpec#description()} may now return Strings from this
             * CommandSpec's {@linkplain UsageMessageSpec#messages() messages}.
             * The option parameter's {@linkplain OptionSpec#defaultValueString()} may
             * now return Strings from this CommandSpec's {@link CommandSpec#defaultValueProvider()} IDefaultValueProvider}.
             * @param option the option spec to add
             * @return this CommandSpec for method chaining
             * @throws DuplicateOptionAnnotationsException if any of the names of the specified option is the same as the name of another option */
            public CommandSpec addOption(OptionSpec option) {
                for (String name : interpolator.interpolate(option.names())) { // cannot be null or empty
                    OptionSpec existing = optionsByNameMap.put(name, option);
                    if (existing != null) { /* was: && !existing.equals(option)) {*/ // since 4.0 ArgGroups: an option cannot be in multiple groups
                        throw DuplicateOptionAnnotationsException.create(name, option, existing);
                    }
                    if (name.length() == 2 && name.startsWith("-")) { posixOptionsByKeyMap.put(name.charAt(1), option); }
                }
                options.add(option);
                return addArg(option);
            }
            /** Adds the specified positional parameter spec to the list of configured arguments to expect.
             * The positional parameter's {@linkplain PositionalParamSpec#description()} may
             * now return Strings from this CommandSpec's {@linkplain UsageMessageSpec#messages() messages}.
             * The positional parameter's {@linkplain PositionalParamSpec#defaultValueString()} may
             * now return Strings from this CommandSpec's {@link CommandSpec#defaultValueProvider()} IDefaultValueProvider}.
             * @param positional the positional parameter spec to add
             * @return this CommandSpec for method chaining */
            public CommandSpec addPositional(PositionalParamSpec positional) {
                positionalParameters.add(positional);
                addArg(positional);
                if (positional.index().isUnresolved()) {
                    positional.index = Range.valueOf(interpolator.interpolate(positional.index().originalValue));
                    positional.initCapacity();
                }
                return this;
            }
            private CommandSpec addArg(ArgSpec arg) {
                args.add(arg);
                arg.messages(usageMessage().messages());
                arg.commandSpec = this;
                if (arg.arity().isUnresolved()) {
                    arg.arity = Range.valueOf(interpolator.interpolate(arg.arity().originalValue));
                }
                // do this last: arg.required() needs to resolve variables in arg.defaultValue()
                if (arg.required() && arg.group() == null) { requiredArgs.add(arg); }
                return this;
            }

            /** Adds the specified {@linkplain ArgGroupSpec argument group} to the groups in this command.
             * @param group the group spec to add
             * @return this CommandSpec for method chaining
             * @throws InitializationException if the specified group or one of its {@linkplain ArgGroupSpec#parentGroup() ancestors} has already been added
             * @since 4.0 */
            public CommandSpec addArgGroup(ArgGroupSpec group) {
                Assert.notNull(group, "group");
                if (group.parentGroup() != null) {
                    throw new InitializationException("Groups that are part of another group should not be added to a command. Add only the top-level group.");
                }
                check(group, flatten(groups, new HashSet<ArgGroupSpec>()));
                this.groups.add(group);
                addGroupArgsToCommand(group, new HashMap<String, ArgGroupSpec>());
                return this;
            }
            private void addGroupArgsToCommand(ArgGroupSpec group, Map<String, ArgGroupSpec> added) {
                for (ArgSpec arg : group.args()) {
                    if (arg.isOption()) {
                        String[] names = interpolator.interpolate(((OptionSpec) arg).names());
                        for (String name : names) {
                            if (added.containsKey(name)) {
                                throw new DuplicateNameException("An option cannot be in multiple groups but " + name + " is in " + group.synopsis() + " and " + added.get(name).synopsis() + ". Refactor to avoid this. For example, (-a | (-a -b)) can be rewritten as (-a [-b]), and (-a -b | -a -c) can be rewritten as (-a (-b | -c)).");
                            }
                        }
                        for (String name : names) { added.put(name, group); }
                    }
                    add(arg);
                }
                for (ArgGroupSpec sub : group.subgroups()) { addGroupArgsToCommand(sub, added); }
            }
            private Set<ArgGroupSpec> flatten(Collection<ArgGroupSpec> groups, Set<ArgGroupSpec> result) {
                for (ArgGroupSpec group : groups) { flatten(group, result); } return result;
            }
            private Set<ArgGroupSpec> flatten(ArgGroupSpec group, Set<ArgGroupSpec> result) {
                result.add(group);
                for (ArgGroupSpec sub : group.subgroups()) { flatten(sub, result); }
                return result;
            }
            private void check(ArgGroupSpec group, Set<ArgGroupSpec> existing) {
                if (existing.contains(group)) {
                    throw new InitializationException("The specified group " + group.synopsis() + " has already been added to the " + qualifiedName() + " command.");
                }
                for (ArgGroupSpec sub : group.subgroups()) { check(sub, existing); }
            }

            /** Adds the specified mixin {@code CommandSpec} object to the map of mixins for this command.
             * @param name the name that can be used to later retrieve the mixin
             * @param mixin the mixin whose options and positional parameters and other attributes to add to this command
             * @return this CommandSpec for method chaining */
            public CommandSpec addMixin(String name, CommandSpec mixin) {
                mixins.put(interpolator.interpolate(name), mixin);

                initExitCodeOnSuccess(mixin.exitCodeOnSuccess());
                initExitCodeOnUsageHelp(mixin.exitCodeOnUsageHelp());
                initExitCodeOnVersionHelp(mixin.exitCodeOnVersionHelp());
                initExitCodeOnInvalidInput(mixin.exitCodeOnInvalidInput());
                initExitCodeOnExecutionException(mixin.exitCodeOnExecutionException());

                parser.initSeparator(mixin.parser.separator());
                initName(interpolator.interpolateCommandName(mixin.name()));
                initVersion(mixin.version());
                initHelpCommand(mixin.helpCommand());
                initVersionProvider(mixin.versionProvider());
                initDefaultValueProvider(mixin.defaultValueProvider());
                usageMessage.initFromMixin(mixin.usageMessage, this);

                for (Map.Entry<String, CommandLine> entry : mixin.subcommands().entrySet()) {
                    addSubcommand(entry.getKey(), entry.getValue());
                }
                for (OptionSpec optionSpec         : mixin.options())              { addOption(optionSpec); }
                for (PositionalParamSpec paramSpec : mixin.positionalParameters()) { addPositional(paramSpec); }
                return this;
            }

            /** Adds the specified {@code UnmatchedArgsBinding} to the list of model objects to capture unmatched arguments for this command.
             * @param spec the unmatched arguments binding to capture unmatched arguments
             * @return this CommandSpec for method chaining */
            public CommandSpec addUnmatchedArgsBinding(UnmatchedArgsBinding spec) { unmatchedArgs.add(spec); parser().unmatchedArgumentsAllowed(true); return this; }
    
            /** Returns a map of the mixin names to mixin {@code CommandSpec} objects configured for this command.
             * @return an immutable map of mixins added to this command. */
            public Map<String, CommandSpec> mixins() { return Collections.unmodifiableMap(mixins); }
    
            /** Returns the list of options configured for this command.
             * @return an immutable list of options that this command recognizes. */
            public List<OptionSpec> options() { return Collections.unmodifiableList(options); }
    
            /** Returns the list of positional parameters configured for this command.
             * @return an immutable list of positional parameters that this command recognizes. */
            public List<PositionalParamSpec> positionalParameters() { return Collections.unmodifiableList(positionalParameters); }

            /** Returns the {@linkplain ArgGroupSpec argument groups} in this command.
             * @return an immutable list of groups of options and positional parameters in this command
             * @since 4.0 */
            public List<ArgGroupSpec> argGroups() { return Collections.unmodifiableList(groups); }

            /** Returns a map of the option names to option spec objects configured for this command.
             * @return an immutable map of options that this command recognizes. */
            public Map<String, OptionSpec> optionsMap() { return Collections.unmodifiableMap(optionsByNameMap); }
    
            /** Returns a map of the short (single character) option names to option spec objects configured for this command.
             * @return an immutable map of options that this command recognizes. */
            public Map<Character, OptionSpec> posixOptionsMap() { return Collections.unmodifiableMap(posixOptionsByKeyMap); }

            /** Returns the list of required options and positional parameters configured for this command.
             * This does not include options and positional parameters that are part of a {@linkplain ArgGroupSpec group}.
             * @return an immutable list of the required options and positional parameters for this command. */
            public List<ArgSpec> requiredArgs() { return Collections.unmodifiableList(requiredArgs); }

            /** Returns the list of {@link UnmatchedArgsBinding UnmatchedArgumentsBindings} configured for this command;
             * each {@code UnmatchedArgsBinding} captures the arguments that could not be matched to any options or positional parameters. */
            public List<UnmatchedArgsBinding> unmatchedArgsBindings() { return Collections.unmodifiableList(unmatchedArgs); }
    
            /** Returns name of this command. Used in the synopsis line of the help message.
             * {@link #DEFAULT_COMMAND_NAME} by default, initialized from {@link Command#name()} if defined.
             * @see #qualifiedName() */
            public String name() { return interpolator.interpolateCommandName((name == null) ? DEFAULT_COMMAND_NAME : name); }

            /** Returns the alias command names of this subcommand.
             * @since 3.1 */
            public String[] aliases() { return interpolator.interpolate(aliases.toArray(new String[0])); }

            /** Returns all names of this command, including {@link #name()} and {@link #aliases()}.
             * @since 3.9 */
            public Set<String> names() {
                Set<String> result = new LinkedHashSet<String>();
                result.add(name());
                result.addAll(Arrays.asList(aliases()));
                return result;
            }

            /** Returns the list of all options and positional parameters configured for this command.
             * @return an immutable list of all options and positional parameters for this command. */
            public List<ArgSpec> args() { return Collections.unmodifiableList(args); }
            Object[] argValues() {
                Map<Class<?>, CommandSpec> allMixins = null;
                int argsLength = args.size();
                int shift = 0;
                for (Map.Entry<String, CommandSpec> mixinEntry : mixins.entrySet()) {
                    if (mixinEntry.getKey().equals(AutoHelpMixin.KEY)) {
                        shift = 2;
                        argsLength -= shift;
                        continue;
                    }
                    CommandSpec mixin = mixinEntry.getValue();
                    int mixinArgs = mixin.args.size();
                    argsLength -= (mixinArgs - 1); // subtract 1 because that's the mixin
                    if (allMixins == null) {
                        allMixins = new IdentityHashMap<Class<?>, CommandSpec>(mixins.size());
                    }
                    allMixins.put(mixin.userObject.getClass(), mixin);
                }

                Object[] values = new Object[argsLength];
                if (allMixins == null) {
                    for (int i = 0; i < values.length; i++) { values[i] = args.get(i + shift).getValue(); }
                } else {
                    int argIndex = shift;
                    Class<?>[] methodParams = ((Method) userObject).getParameterTypes();
                    for (int i = 0; i < methodParams.length; i++) {
                        final Class<?> param = methodParams[i];
                        CommandSpec mixin = allMixins.remove(param);
                        if (mixin == null) {
                            values[i] = args.get(argIndex++).getValue();
                        } else {
                            values[i] = mixin.userObject;
                            argIndex += mixin.args.size();
                        }
                    }
                }
                return values;
            }

            /** Returns the String to use as the program name in the synopsis line of the help message:
             * this command's {@link #name() name}, preceded by the qualified name of the parent command, if any, separated by a space.
             * @return {@link #DEFAULT_COMMAND_NAME} by default, initialized from {@link Command#name()} and the parent command if defined.
             * @since 3.0.1 */
            public String qualifiedName() { return qualifiedName(" "); }
            /** Returns this command's fully qualified name, which is its {@link #name() name}, preceded by the qualified name of the parent command, if this command has a parent command.
             * @return {@link #DEFAULT_COMMAND_NAME} by default, initialized from {@link Command#name()} and the parent command if any.
             * @param separator the string to put between the names of the commands in the hierarchy
             * @since 3.6 */
            public String qualifiedName(String separator) {
                String result = name();
                if (parent() != null) { result = parent().qualifiedName(separator) + separator + result; }
                return result;
            }

            /** Returns version information for this command, to print to the console when the user specifies an
             * {@linkplain OptionSpec#versionHelp() option} to request version help. This is not part of the usage help message.
             * @return the version strings generated by the {@link #versionProvider() version provider} if one is set, otherwise the {@linkplain #version(String...) version literals}*/
            public String[] version() {
                if (versionProvider != null) {
                    try {
                        return interpolator.interpolate(versionProvider.getVersion());
                    } catch (Exception ex) {
                        String msg = "Could not get version info from " + versionProvider + ": " + ex;
                        throw new ExecutionException(this.commandLine, msg, ex);
                    }
                }
                return interpolator.interpolate(version == null ? UsageMessageSpec.DEFAULT_MULTI_LINE : version);
            }
    
            /** Returns the version provider for this command, to generate the {@link #version()} strings.
             * @return the version provider or {@code null} if the version strings should be returned from the {@linkplain #version(String...) version literals}.*/
            public IVersionProvider versionProvider() { return versionProvider; }

            /** Returns whether this subcommand is a help command, and required options and positional
             * parameters of the parent command should not be validated.
             * @return {@code true} if this subcommand is a help command and picocli should not check for missing required
             *      options and positional parameters on the parent command
             * @see Command#helpCommand() */
            public boolean helpCommand() { return (isHelpCommand == null) ? DEFAULT_IS_HELP_COMMAND : isHelpCommand; }

            /** Returns exit code for successful termination. {@value picocli.CommandLine.ExitCode#OK} by default, may be set programmatically or via the {@link Command#exitCodeOnSuccess() exitCodeOnSuccess} annotation.
             * @see #execute(String...)
             * @since 4.0 */
            public int exitCodeOnSuccess() { return exitCodeOnSuccess == null ? ExitCode.OK : exitCodeOnSuccess; }
            /** Returns exit code for successful termination after printing usage help on user request. {@value picocli.CommandLine.ExitCode#OK} by default, may be set programmatically or via the {@link Command#exitCodeOnVersionHelp() exitCodeOnVersionHelp} annotation.
             * @see #execute(String...)
             * @since 4.0 */
            public int exitCodeOnUsageHelp() { return exitCodeOnUsageHelp == null ? ExitCode.OK : exitCodeOnUsageHelp; }
            /** Returns exit code for successful termination after printing version help on user request. {@value picocli.CommandLine.ExitCode#OK} by default, may be set programmatically or via the {@link Command#exitCodeOnUsageHelp() exitCodeOnUsageHelp} annotation.
             * @see #execute(String...)
             * @since 4.0 */
            public int exitCodeOnVersionHelp() { return exitCodeOnVersionHelp == null ? ExitCode.OK : exitCodeOnVersionHelp; }
            /** Returns exit code for command line usage error. {@value picocli.CommandLine.ExitCode#USAGE} by default, may be set programmatically or via the {@link Command#exitCodeOnInvalidInput() exitCodeOnInvalidInput} annotation.
             * @see #execute(String...)
             * @since 4.0 */
            public int exitCodeOnInvalidInput() { return exitCodeOnInvalidInput == null ? ExitCode.USAGE : exitCodeOnInvalidInput; }
            /** Returns exit code signifying that an exception occurred when invoking the Runnable, Callable or Method user object of a command.
             * {@value picocli.CommandLine.ExitCode#SOFTWARE} by default, may be set programmatically or via the {@link Command#exitCodeOnExecutionException() exitCodeOnExecutionException} annotation.
             * @see #execute(String...)
             * @since 4.0 */
            public int exitCodeOnExecutionException() { return exitCodeOnExecutionException == null ? ExitCode.SOFTWARE : exitCodeOnExecutionException; }

            /** Returns {@code true} if the standard help options have been mixed in with this command, {@code false} otherwise. */
            public boolean mixinStandardHelpOptions() { return mixins.containsKey(AutoHelpMixin.KEY); }

            /** Returns a string representation of this command, used in error messages and trace messages. */
            public String toString() { return toString == null ? String.valueOf(userObject) : toString; }

            /** Sets the String to use as the program name in the synopsis line of the help message.
             * @return this CommandSpec for method chaining */
            public CommandSpec name(String name) { this.name = name; return this; }

            /** Sets the alternative names by which this subcommand is recognized on the command line.
             * @return this CommandSpec for method chaining
             * @since 3.1 */
            public CommandSpec aliases(String... aliases) {
                this.aliases = new LinkedHashSet<String>(Arrays.asList(aliases == null ? new String[0] : aliases));
                return this;
            }

            /** Returns the default value provider for this command.
             * @return the default value provider or {@code null}
             * @since 3.6 */
            public IDefaultValueProvider defaultValueProvider() { return defaultValueProvider; }

            /** Sets default value provider for this command.
             * @param defaultValueProvider the default value provider to use, or {@code null}.
             * @return this CommandSpec for method chaining
             * @since 3.6 */
            public CommandSpec defaultValueProvider(IDefaultValueProvider  defaultValueProvider) { this.defaultValueProvider = defaultValueProvider; return this; }

            /** Sets version information literals for this command, to print to the console when the user specifies an
             * {@linkplain OptionSpec#versionHelp() option} to request version help. Only used if no {@link #versionProvider() versionProvider} is set.
             * @return this CommandSpec for method chaining */
            public CommandSpec version(String... version) { this.version = version; return this; }
    
            /** Sets version provider for this command, to generate the {@link #version()} strings.
             * @param versionProvider the version provider to use to generate the version strings, or {@code null} if the {@linkplain #version(String...) version literals} should be used.
             * @return this CommandSpec for method chaining */
            public CommandSpec versionProvider(IVersionProvider versionProvider) { this.versionProvider = versionProvider; return this; }

            /** Sets whether this is a help command and required parameter checking should be suspended.
             * @return this CommandSpec for method chaining
             * @see Command#helpCommand() */
            public CommandSpec helpCommand(boolean newValue) {isHelpCommand = newValue; return this;}

            /** Sets exit code for successful termination. {@value picocli.CommandLine.ExitCode#OK} by default.
             * @see #execute(String...)
             * @since 4.0 */
            public CommandSpec exitCodeOnSuccess(int newValue) { exitCodeOnSuccess = newValue; return this; }
            /** Sets exit code for successful termination after printing usage help on user request. {@value picocli.CommandLine.ExitCode#OK} by default.
             * @see #execute(String...)
             * @since 4.0 */
            public CommandSpec exitCodeOnUsageHelp(int newValue) { exitCodeOnUsageHelp = newValue; return this; }
            /** Sets exit code for successful termination after printing version help on user request. {@value picocli.CommandLine.ExitCode#OK} by default.
             * @see #execute(String...)
             * @since 4.0 */
            public CommandSpec exitCodeOnVersionHelp(int newValue) { exitCodeOnVersionHelp = newValue; return this; }
            /** Sets exit code for command line usage error. {@value picocli.CommandLine.ExitCode#USAGE} by default.
             * @see #execute(String...)
             * @since 4.0 */
            public CommandSpec exitCodeOnInvalidInput(int newValue) { exitCodeOnInvalidInput = newValue; return this; }
            /** Sets exit code signifying that an exception occurred when invoking the Runnable, Callable or Method user object of a command.
             * {@value picocli.CommandLine.ExitCode#SOFTWARE} by default.
             * @see #execute(String...)
             * @since 4.0 */
            public CommandSpec exitCodeOnExecutionException(int newValue) { exitCodeOnExecutionException = newValue; return this; }

            /** Sets whether the standard help options should be mixed in with this command.
             * @return this CommandSpec for method chaining
             * @see Command#mixinStandardHelpOptions() */
            public CommandSpec mixinStandardHelpOptions(boolean newValue) {
                if (newValue) {
                    CommandSpec mixin = CommandSpec.forAnnotatedObject(new AutoHelpMixin(), new DefaultFactory());
                    addMixin(AutoHelpMixin.KEY, mixin);
                } else {
                    CommandSpec helpMixin = mixins.remove(AutoHelpMixin.KEY);
                    if (helpMixin != null) {
                        options.removeAll(helpMixin.options);
                        for (OptionSpec option : helpMixin.options()) {
                            for (String name : interpolator.interpolate(option.names())) {
                                optionsByNameMap.remove(name);
                                if (name.length() == 2 && name.startsWith("-")) { posixOptionsByKeyMap.remove(name.charAt(1)); }
                            }
                        }
                    }
                }
                return this;
            }

            /** Sets the string representation of this command, used in error messages and trace messages.
             * @param newValue the string representation
             * @return this CommandSpec for method chaining */
            public CommandSpec withToString(String newValue) { this.toString = newValue; return this; }

            /**
             * Updates the following attributes from the specified {@code @Command} annotation:
             * aliases, {@link ParserSpec#separator() parser separator}, command name, version, help command,
             * version provider, default provider and {@link UsageMessageSpec usage message spec}.
             * @param cmd the {@code @Command} annotation to get attribute values from
             * @param factory factory used to instantiate classes
             * @since 3.7
             */
            public void updateCommandAttributes(Command cmd, IFactory factory) {
                parser().updateSeparator(interpolator.interpolate(cmd.separator()));

                updateExitCodeOnSuccess(cmd.exitCodeOnSuccess());
                updateExitCodeOnUsageHelp(cmd.exitCodeOnUsageHelp());
                updateExitCodeOnVersionHelp(cmd.exitCodeOnVersionHelp());
                updateExitCodeOnInvalidInput(cmd.exitCodeOnInvalidInput());
                updateExitCodeOnExecutionException(cmd.exitCodeOnExecutionException());

                aliases(cmd.aliases());
                updateName(cmd.name());
                updateVersion(cmd.version());
                updateHelpCommand(cmd.helpCommand());
                updateAddMethodSubcommands(cmd.addMethodSubcommands());
                usageMessage().updateFromCommand(cmd, this);

                if (factory != null) {
                    updateVersionProvider(cmd.versionProvider(), factory);
                    initDefaultValueProvider(cmd.defaultValueProvider(), factory);
                }
            }

            void initName(String value)                 { if (initializable(name, value, DEFAULT_COMMAND_NAME))                           {name = value;} }
            void initHelpCommand(boolean value)         { if (initializable(isHelpCommand, value, DEFAULT_IS_HELP_COMMAND))               {isHelpCommand = value;} }
            void initVersion(String[] value)            { if (initializable(version, value, UsageMessageSpec.DEFAULT_MULTI_LINE))         {version = value.clone();} }
            void initVersionProvider(IVersionProvider value) { if (versionProvider == null) { versionProvider = value; } }
            void initDefaultValueProvider(IDefaultValueProvider value) { if (defaultValueProvider == null) { defaultValueProvider = value; } }
            void initDefaultValueProvider(Class<? extends IDefaultValueProvider> value, IFactory factory) {
                if (initializable(defaultValueProvider, value, NoDefaultProvider.class)) { defaultValueProvider = (DefaultFactory.createDefaultValueProvider(factory, value)); }
            }
            void initExitCodeOnSuccess(int exitCode)            { if (initializable(exitCodeOnSuccess, exitCode, ExitCode.OK)) { exitCodeOnSuccess = exitCode; } }
            void initExitCodeOnUsageHelp(int exitCode)          { if (initializable(exitCodeOnUsageHelp, exitCode, ExitCode.OK)) { exitCodeOnUsageHelp = exitCode; } }
            void initExitCodeOnVersionHelp(int exitCode)        { if (initializable(exitCodeOnVersionHelp, exitCode, ExitCode.OK)) { exitCodeOnVersionHelp = exitCode; } }
            void initExitCodeOnInvalidInput(int exitCode)       { if (initializable(exitCodeOnInvalidInput, exitCode, ExitCode.USAGE)) { exitCodeOnInvalidInput = exitCode; } }
            void initExitCodeOnExecutionException(int exitCode) { if (initializable(exitCodeOnExecutionException, exitCode, ExitCode.SOFTWARE)) { exitCodeOnExecutionException = exitCode; } }
            void updateName(String value)               { if (isNonDefault(value, DEFAULT_COMMAND_NAME))                 {name = value;} }
            void updateHelpCommand(boolean value)       { if (isNonDefault(value, DEFAULT_IS_HELP_COMMAND))              {isHelpCommand = value;} }
            void updateAddMethodSubcommands(boolean value) { if (isNonDefault(value, DEFAULT_IS_ADD_METHOD_SUBCOMMANDS)) {isAddMethodSubcommands = value;} }
            void updateVersion(String[] value)          { if (isNonDefault(value, UsageMessageSpec.DEFAULT_MULTI_LINE))  {version = value.clone();} }
            void updateVersionProvider(Class<? extends IVersionProvider> value, IFactory factory) {
                if (isNonDefault(value, NoVersionProvider.class)) { versionProvider = (DefaultFactory.createVersionProvider(factory, value)); }
            }
            void updateExitCodeOnSuccess(int exitCode)            { if (isNonDefault(exitCode, ExitCode.OK))       { exitCodeOnSuccess = exitCode; } }
            void updateExitCodeOnUsageHelp(int exitCode)          { if (isNonDefault(exitCode, ExitCode.OK))       { exitCodeOnUsageHelp = exitCode; } }
            void updateExitCodeOnVersionHelp(int exitCode)        { if (isNonDefault(exitCode, ExitCode.OK))       { exitCodeOnVersionHelp = exitCode; } }
            void updateExitCodeOnInvalidInput(int exitCode)       { if (isNonDefault(exitCode, ExitCode.USAGE))    { exitCodeOnInvalidInput = exitCode; } }
            void updateExitCodeOnExecutionException(int exitCode) { if (isNonDefault(exitCode, ExitCode.SOFTWARE)) { exitCodeOnExecutionException = exitCode; } }

            /** Returns the option with the specified short name, or {@code null} if no option with that name is defined for this command. */
            public OptionSpec findOption(char shortName) { return findOption(shortName, options()); }
            /** Returns the option with the specified name, or {@code null} if no option with that name is defined for this command.
             * @param name used to search the options. May include option name prefix characters or not. */
            public OptionSpec findOption(String name) { return findOption(name, options()); }

            static OptionSpec findOption(char shortName, Iterable<OptionSpec> options) {
                for (OptionSpec option : options) {
                    for (String name : option.names()) {
                        if (name.length() == 2 && name.charAt(0) == '-' && name.charAt(1) == shortName) { return option; }
                        if (name.length() == 1 && name.charAt(0) == shortName) { return option; }
                    }
                }
                return null;
            }
            static OptionSpec findOption(String name, List<OptionSpec> options) {
                for (OptionSpec option : options) {
                    for (String prefixed : option.names()) {
                        if (prefixed.equals(name) || stripPrefix(prefixed).equals(name)) { return option; }
                    }
                }
                return null;
            }
            static String stripPrefix(String prefixed) {
                for (int i = 0; i < prefixed.length(); i++) {
                    if (Character.isJavaIdentifierPart(prefixed.charAt(i))) { return prefixed.substring(i); }
                }
                return prefixed;
            }
            List<String> findOptionNamesWithPrefix(String prefix) {
                List<String> result = new ArrayList<String>();
                for (OptionSpec option : options()) {
                    for (String name : option.names()) {
                        if (stripPrefix(name).startsWith(prefix)) { result.add(name); }
                    }
                }
                return result;
            }

            boolean resemblesOption(String arg, Tracer tracer) {
                if (parser().unmatchedOptionsArePositionalParams()) {
                    if (tracer != null && tracer.isDebug()) {tracer.debug("Parser is configured to treat all unmatched options as positional parameter%n", arg);}
                    return false;
                }
                if (arg.length() == 1) {
                    if (tracer != null && tracer.isDebug()) {tracer.debug("Single-character arguments that don't match known options are considered positional parameters%n", arg);}
                    return false;
                }
                if (options().isEmpty()) {
                    boolean result = arg.startsWith("-");
                    if (tracer != null && tracer.isDebug()) {tracer.debug("'%s' %s an option%n", arg, (result ? "resembles" : "doesn't resemble"));}
                    return result;
                }
                int count = 0;
                for (String optionName : optionsMap().keySet()) {
                    for (int i = 0; i < arg.length(); i++) {
                        if (optionName.length() > i && arg.charAt(i) == optionName.charAt(i)) { count++; } else { break; }
                    }
                }
                boolean result = count > 0 && count * 10 >= optionsMap().size() * 9; // at least one prefix char in common with 9 out of 10 options
                if (tracer != null && tracer.isDebug()) {tracer.debug("'%s' %s an option: %d matching prefix chars out of %d option names%n", arg, (result ? "resembles" : "doesn't resemble"), count, optionsMap().size());}
                return result;
            }
        }
        private static boolean initializable(Object current, Object candidate, Object defaultValue) {
            return current == null && isNonDefault(candidate, defaultValue);
        }
        private static boolean initializable(Object current, Object[] candidate, Object[] defaultValue) {
            return current == null && isNonDefault(candidate, defaultValue);
        }
        private static boolean isNonDefault(Object candidate, Object defaultValue) {
            return !Assert.notNull(defaultValue, "defaultValue").equals(candidate);
        }
        private static boolean isNonDefault(Object[] candidate, Object[] defaultValue) {
            return !Arrays.equals(Assert.notNull(defaultValue, "defaultValue"), candidate);
        }
        /** Models the usage help message specification and can be used to customize the usage help message.
         * <p>
         * This class provides two ways to customize the usage help message:
         * </p>
         * <ul>
         *     <li>Change the text of the predefined sections (this may also be done declaratively using the annotations)</li>
         *     <li>Add custom sections, or remove or re-order predefined sections</li>
         * </ul>
         * <p>
         * The pre-defined sections have getters and setters that return a String (or array of Strings). For example:
         * {@link #description()} and {@link #description(String...)} or {@link #header()} and {@link #header(String...)}.
         * </p><p>
         * Changing the section order, or adding custom sections can be accomplished with {@link #sectionKeys(List)} and {@link #sectionMap(Map)}.
         * This gives complete freedom on how a usage help message section is rendered, but it also means that the {@linkplain IHelpSectionRenderer section renderer}
         * is responsible for all aspects of rendering the section, including layout and emitting ANSI escape codes.
         * The {@link Help.TextTable} and {@link Help.Ansi.Text} classes, and the {@link CommandLine.Help.Ansi#string(String)} and {@link CommandLine.Help.Ansi#text(String)} methods may be useful.
         * </p><p>
         * The usage help message is created more or less like this:
         * </p>
         * <pre>
         * // CommandLine.usage(...) or CommandLine.getUsageMessage(...)
         * Help.ColorScheme colorScheme = Help.defaultColorScheme(Help.Ansi.AUTO);
         * Help help = getHelpFactory().create(getCommandSpec(), colorScheme)
         * StringBuilder result = new StringBuilder();
         * for (String key : getHelpSectionKeys()) {
         *     IHelpSectionRenderer renderer = getHelpSectionMap().get(key);
         *     if (renderer != null) { result.append(renderer.render(help)); }
         * }
         * // return or print result
         * </pre>
         * <p>
         * Where the default {@linkplain #sectionMap() help section map} is constructed like this:</p>
         * <pre>{@code
         * // The default section renderers delegate to methods in Help for their implementation
         * // (using Java 8 lambda notation for brevity):
         * Map<String, IHelpSectionRenderer> sectionMap = new HashMap<>();
         * sectionMap.put(SECTION_KEY_HEADER_HEADING,         help -> help.headerHeading());
         * sectionMap.put(SECTION_KEY_HEADER,                 help -> help.header());
         * sectionMap.put(SECTION_KEY_SYNOPSIS_HEADING,       help -> help.synopsisHeading());      //e.g. Usage:
         * sectionMap.put(SECTION_KEY_SYNOPSIS,               help -> help.synopsis(help.synopsisHeadingLength())); //e.g. <cmd> [OPTIONS] <subcmd> [COMMAND-OPTIONS] [ARGUMENTS]
         * sectionMap.put(SECTION_KEY_DESCRIPTION_HEADING,    help -> help.descriptionHeading());   //e.g. %nDescription:%n%n
         * sectionMap.put(SECTION_KEY_DESCRIPTION,            help -> help.description());          //e.g. {"Converts foos to bars.", "Use options to control conversion mode."}
         * sectionMap.put(SECTION_KEY_PARAMETER_LIST_HEADING, help -> help.parameterListHeading()); //e.g. %nPositional parameters:%n%n
         * sectionMap.put(SECTION_KEY_PARAMETER_LIST,         help -> help.parameterList());        //e.g. [FILE...] the files to convert
         * sectionMap.put(SECTION_KEY_OPTION_LIST_HEADING,    help -> help.optionListHeading());    //e.g. %nOptions:%n%n
         * sectionMap.put(SECTION_KEY_OPTION_LIST,            help -> help.optionList());           //e.g. -h, --help   displays this help and exits
         * sectionMap.put(SECTION_KEY_COMMAND_LIST_HEADING,   help -> help.commandListHeading());   //e.g. %nCommands:%n%n
         * sectionMap.put(SECTION_KEY_COMMAND_LIST,           help -> help.commandList());          //e.g.    add       adds the frup to the frooble
         * sectionMap.put(SECTION_KEY_EXIT_CODE_LIST_HEADING, help -> help.exitCodeListHeading());
         * sectionMap.put(SECTION_KEY_EXIT_CODE_LIST,         help -> help.exitCodeList());
         * sectionMap.put(SECTION_KEY_FOOTER_HEADING,         help -> help.footerHeading());
         * sectionMap.put(SECTION_KEY_FOOTER,                 help -> help.footer());
         * }</pre>
         *
         * @since 3.0 */
        public static class UsageMessageSpec {

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Header Heading section.
             * The default renderer for this section calls {@link Help#headerHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_HEADER_HEADING = "headerHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Header section.
             * The default renderer for this section calls {@link Help#header(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_HEADER = "header";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Synopsis Heading section.
             * The default renderer for this section calls {@link Help#synopsisHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_SYNOPSIS_HEADING = "synopsisHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Synopsis section.
             * The default renderer for this section calls {@link Help#synopsis(int)}.
             * @since 3.9 */
            public static final String SECTION_KEY_SYNOPSIS = "synopsis";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Description Heading section.
             * The default renderer for this section calls {@link Help#descriptionHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_DESCRIPTION_HEADING = "descriptionHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Description section.
             * The default renderer for this section calls {@link Help#description(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_DESCRIPTION = "description";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Parameter List Heading section.
             * The default renderer for this section calls {@link Help#parameterListHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_PARAMETER_LIST_HEADING = "parameterListHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Parameter List section.
             * The default renderer for this section calls {@link Help#parameterList()}.
             * @since 3.9 */
            public static final String SECTION_KEY_PARAMETER_LIST = "parameterList";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Option List Heading section.
             * The default renderer for this section calls {@link Help#optionListHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_OPTION_LIST_HEADING = "optionListHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Option List section.
             * The default renderer for this section calls {@link Help#optionList()}.
             * @since 3.9 */
            public static final String SECTION_KEY_OPTION_LIST = "optionList";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Subcommand List Heading section.
             * The default renderer for this section calls {@link Help#commandListHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_COMMAND_LIST_HEADING = "commandListHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Subcommand List section.
             * The default renderer for this section calls {@link Help#commandList()}.
             * @since 3.9 */
            public static final String SECTION_KEY_COMMAND_LIST = "commandList";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Exit Code List Heading section.
             * The default renderer for this section calls {@link Help#exitCodeListHeading(Object...)}.
             * @since 4.0 */
            public static final String SECTION_KEY_EXIT_CODE_LIST_HEADING = "exitCodeListHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Exit Code List section.
             * The default renderer for this section calls {@link Help#exitCodeList()}.
             * @since 4.0 */
            public static final String SECTION_KEY_EXIT_CODE_LIST = "exitCodeList";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Footer Heading section.
             * The default renderer for this section calls {@link Help#footerHeading(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_FOOTER_HEADING = "footerHeading";

            /** {@linkplain #sectionKeys() Section key} to {@linkplain #sectionMap() control} the {@linkplain IHelpSectionRenderer section renderer} for the Footer section.
             * The default renderer for this section calls {@link Help#footer(Object...)}.
             * @since 3.9 */
            public static final String SECTION_KEY_FOOTER = "footer";

            /** Constant holding the default usage message width: <code>{@value}</code>. */
            public  final static int DEFAULT_USAGE_WIDTH = 80;
            private final static int MINIMUM_USAGE_WIDTH = 55;

            /** Constant String holding the default synopsis heading: <code>{@value}</code>. */
            static final String DEFAULT_SYNOPSIS_HEADING = "Usage: ";

            /** Constant String holding the default command list heading: <code>{@value}</code>. */
            static final String DEFAULT_COMMAND_LIST_HEADING = "Commands:%n";

            /** Constant String holding the default string that separates options from option parameters: {@code ' '} ({@value}). */
            static final char DEFAULT_REQUIRED_OPTION_MARKER = ' ';

            /** Constant Boolean holding the default setting for whether to abbreviate the synopsis: <code>{@value}</code>.*/
            static final Boolean DEFAULT_ABBREVIATE_SYNOPSIS = Boolean.FALSE;

            /** Constant Boolean holding the default setting for whether to sort the options alphabetically: <code>{@value}</code>.*/
            static final Boolean DEFAULT_SORT_OPTIONS = Boolean.TRUE;

            /** Constant Boolean holding the default setting for whether to show default values in the usage help message: <code>{@value}</code>.*/
            static final Boolean DEFAULT_SHOW_DEFAULT_VALUES = Boolean.FALSE;

            /** Constant Boolean holding the default setting for whether this command should be listed in the usage help of the parent command: <code>{@value}</code>.*/
            static final Boolean DEFAULT_HIDDEN = Boolean.FALSE;

            /** Constant Boolean holding the default setting for whether line breaks should take wide CJK characters into account: <code>{@value}</code>.*/
            static final Boolean DEFAULT_ADJUST_CJK = Boolean.TRUE;

            static final String DEFAULT_SINGLE_VALUE = "";
            static final String[] DEFAULT_MULTI_LINE = {};

            private IHelpFactory helpFactory;

            private List<String> sectionKeys = Collections.unmodifiableList(Arrays.asList(
                    SECTION_KEY_HEADER_HEADING,
                    SECTION_KEY_HEADER,
                    SECTION_KEY_SYNOPSIS_HEADING,
                    SECTION_KEY_SYNOPSIS,
                    SECTION_KEY_DESCRIPTION_HEADING,
                    SECTION_KEY_DESCRIPTION,
                    SECTION_KEY_PARAMETER_LIST_HEADING,
                    SECTION_KEY_PARAMETER_LIST,
                    SECTION_KEY_OPTION_LIST_HEADING,
                    SECTION_KEY_OPTION_LIST,
                    SECTION_KEY_COMMAND_LIST_HEADING,
                    SECTION_KEY_COMMAND_LIST,
                    SECTION_KEY_EXIT_CODE_LIST_HEADING,
                    SECTION_KEY_EXIT_CODE_LIST,
                    SECTION_KEY_FOOTER_HEADING,
                    SECTION_KEY_FOOTER));

            private Map<String, IHelpSectionRenderer> helpSectionRendererMap = createHelpSectionRendererMap();

            private String[] description;
            private String[] customSynopsis;
            private String[] header;
            private String[] footer;
            private Boolean abbreviateSynopsis;
            private Boolean sortOptions;
            private Boolean showDefaultValues;
            private Boolean hidden;
            private Character requiredOptionMarker;
            private String headerHeading;
            private String synopsisHeading;
            private String descriptionHeading;
            private String parameterListHeading;
            private String optionListHeading;
            private String commandListHeading;
            private String footerHeading;
            private String exitCodeListHeading;
            private String[] exitCodeListStrings;
            private Map<String, String> exitCodeList;
            private int width = DEFAULT_USAGE_WIDTH;

            private final Interpolator interpolator;
            private Messages messages;
            private Boolean adjustLineBreaksForWideCJKCharacters;

            public UsageMessageSpec() { this(null); }
            UsageMessageSpec(Interpolator interpolator) { this.interpolator = interpolator; }

            /**
             * Sets the maximum usage help message width to the specified value. Longer values are wrapped.
             * @param newValue the new maximum usage help message width. Must be 55 or greater.
             * @return this {@code UsageMessageSpec} for method chaining
             * @throws IllegalArgumentException if the specified width is less than 55
             */
            public UsageMessageSpec width(int newValue) {
                if (newValue < MINIMUM_USAGE_WIDTH) {
                    throw new InitializationException("Invalid usage message width " + newValue + ". Minimum value is " + MINIMUM_USAGE_WIDTH);
                }
                width = newValue; return this;
            }

            private static int getSysPropertyWidthOrDefault(int defaultWidth) {
                String userValue = System.getProperty("picocli.usage.width");
                if (userValue == null) { return defaultWidth; }
                try {
                    int width = Integer.parseInt(userValue);
                    if (width < MINIMUM_USAGE_WIDTH) {
                        new Tracer().warn("Invalid picocli.usage.width value %d. Using minimum usage width %d.%n", width, MINIMUM_USAGE_WIDTH);
                        return MINIMUM_USAGE_WIDTH;
                    }
                    return width;
                } catch (NumberFormatException ex) {
                    new Tracer().warn("Invalid picocli.usage.width value '%s'. Using usage width %d.%n", userValue, defaultWidth);
                    return defaultWidth;
                }
            }

            /** Returns the maximum usage help message width. Derived from system property {@code "picocli.usage.width"}
             * if set, otherwise returns the value set via the {@link #width(int)} method, or if not set, the {@linkplain #DEFAULT_USAGE_WIDTH default width}.
             * @return the maximum usage help message width. Never returns less than 55. */
            public int width() { return getSysPropertyWidthOrDefault(width); }

            /** Returns the help section renderers for the predefined section keys. see: {@link #sectionKeys()} */
            private Map<String, IHelpSectionRenderer> createHelpSectionRendererMap() {
                Map<String, IHelpSectionRenderer> result = new HashMap<String, IHelpSectionRenderer>();

                result.put(SECTION_KEY_HEADER_HEADING,         new IHelpSectionRenderer() { public String render(Help help) { return help.headerHeading(); } });
                result.put(SECTION_KEY_HEADER,                 new IHelpSectionRenderer() { public String render(Help help) { return help.header(); } });
                //e.g. Usage:
                result.put(SECTION_KEY_SYNOPSIS_HEADING,       new IHelpSectionRenderer() { public String render(Help help) { return help.synopsisHeading(); } });
                //e.g. &lt;main class&gt; [OPTIONS] &lt;command&gt; [COMMAND-OPTIONS] [ARGUMENTS]
                result.put(SECTION_KEY_SYNOPSIS,               new IHelpSectionRenderer() { public String render(Help help) { return help.synopsis(help.synopsisHeadingLength()); } });
                //e.g. %nDescription:%n%n
                result.put(SECTION_KEY_DESCRIPTION_HEADING,    new IHelpSectionRenderer() { public String render(Help help) { return help.descriptionHeading(); } });
                //e.g. {"Converts foos to bars.", "Use options to control conversion mode."}
                result.put(SECTION_KEY_DESCRIPTION,            new IHelpSectionRenderer() { public String render(Help help) { return help.description(); } });
                //e.g. %nPositional parameters:%n%n
                result.put(SECTION_KEY_PARAMETER_LIST_HEADING, new IHelpSectionRenderer() { public String render(Help help) { return help.parameterListHeading(); } });
                //e.g. [FILE...] the files to convert
                result.put(SECTION_KEY_PARAMETER_LIST,         new IHelpSectionRenderer() { public String render(Help help) { return help.parameterList(); } });
                //e.g. %nOptions:%n%n
                result.put(SECTION_KEY_OPTION_LIST_HEADING,    new IHelpSectionRenderer() { public String render(Help help) { return help.optionListHeading(); } });
                //e.g. -h, --help   displays this help and exits
                result.put(SECTION_KEY_OPTION_LIST,            new IHelpSectionRenderer() { public String render(Help help) { return help.optionList(); } });
                //e.g. %nCommands:%n%n
                result.put(SECTION_KEY_COMMAND_LIST_HEADING,   new IHelpSectionRenderer() { public String render(Help help) { return help.commandListHeading(); } });
                //e.g.    add       adds the frup to the frooble
                result.put(SECTION_KEY_COMMAND_LIST,           new IHelpSectionRenderer() { public String render(Help help) { return help.commandList(); } });
                result.put(SECTION_KEY_EXIT_CODE_LIST_HEADING, new IHelpSectionRenderer() { public String render(Help help) { return help.exitCodeListHeading(); } });
                result.put(SECTION_KEY_EXIT_CODE_LIST,         new IHelpSectionRenderer() { public String render(Help help) { return help.exitCodeList(); } });
                result.put(SECTION_KEY_FOOTER_HEADING,         new IHelpSectionRenderer() { public String render(Help help) { return help.footerHeading(); } });
                result.put(SECTION_KEY_FOOTER,                 new IHelpSectionRenderer() { public String render(Help help) { return help.footer(); } });
                return result;
            }

            /**
             * Returns the section keys in the order that the usage help message should render the sections.
             * This ordering may be modified with the {@link #sectionKeys(List) sectionKeys setter}. The default keys are (in order):
             * <ol start="0">
             *   <li>{@link UsageMessageSpec#SECTION_KEY_HEADER_HEADING SECTION_KEY_HEADER_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_HEADER SECTION_KEY_HEADER}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_SYNOPSIS_HEADING SECTION_KEY_SYNOPSIS_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_SYNOPSIS SECTION_KEY_SYNOPSIS}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_DESCRIPTION_HEADING SECTION_KEY_DESCRIPTION_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_DESCRIPTION SECTION_KEY_DESCRIPTION}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_PARAMETER_LIST_HEADING SECTION_KEY_PARAMETER_LIST_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_PARAMETER_LIST SECTION_KEY_PARAMETER_LIST}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_OPTION_LIST_HEADING SECTION_KEY_OPTION_LIST_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_OPTION_LIST SECTION_KEY_OPTION_LIST}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_COMMAND_LIST_HEADING SECTION_KEY_COMMAND_LIST_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_COMMAND_LIST SECTION_KEY_COMMAND_LIST}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_EXIT_CODE_LIST_HEADING SECTION_KEY_EXIT_CODE_LIST_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_EXIT_CODE_LIST SECTION_KEY_EXIT_CODE_LIST}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_FOOTER_HEADING SECTION_KEY_FOOTER_HEADING}</li>
             *   <li>{@link UsageMessageSpec#SECTION_KEY_FOOTER SECTION_KEY_FOOTER}</li>
             * </ol>
             * @since 3.9
             */
            public List<String> sectionKeys() { return sectionKeys; }

            /**
             * Sets the section keys in the order that the usage help message should render the sections.
             * @see #sectionKeys
             * @since 3.9
             */
            public UsageMessageSpec sectionKeys(List<String> keys) { sectionKeys = Collections.unmodifiableList(new ArrayList<String>(keys)); return this; }

            /**
             * Returns the map of section keys and renderers used to construct the usage help message.
             * The usage help message can be customized by adding, replacing and removing section renderers from this map.
             * Sections can be reordered with the {@link #sectionKeys(List) sectionKeys setter}.
             * Sections that are either not in this map or not in the list returned by {@link #sectionKeys() sectionKeys} are omitted.
             * @see #sectionKeys
             * @since 3.9
             */
            public Map<String, IHelpSectionRenderer> sectionMap() { return helpSectionRendererMap; }

            /**
             * Sets the map of section keys and renderers used to construct the usage help message to a copy of the specified map.
             * @param map the mapping of section keys to their renderers, must be non-{@code null}.
             * @return this UsageMessageSpec for method chaining
             * @see #sectionKeys
             * @see #setHelpSectionMap(Map)
             * @since 3.9
             */
            public UsageMessageSpec sectionMap(Map<String, IHelpSectionRenderer> map) { this.helpSectionRendererMap = new LinkedHashMap<String, IHelpSectionRenderer>(map); return this; }

            /** Returns the {@code IHelpFactory} that is used to construct the usage help message.
             * @see #setHelpFactory(IHelpFactory)
             * @since 3.9
             */
            public IHelpFactory helpFactory() {
                if (helpFactory == null) {
                    helpFactory = new DefaultHelpFactory();
                }
                return helpFactory;
            }

            /** Sets a new {@code IHelpFactory} to customize the usage help message.
             * @param helpFactory the new help factory. Must be non-{@code null}.
             * @return this {@code UsageMessageSpec} object, to allow method chaining
             */
            public UsageMessageSpec helpFactory(IHelpFactory helpFactory) {
                this.helpFactory = Assert.notNull(helpFactory, "helpFactory");
                return this;
            }

            private String   interpolate(String value)    { return interpolator == null ? value  : interpolator.interpolate(value); }
            private String[] interpolate(String[] values) { return interpolator == null ? values : interpolator.interpolate(values); }
            private String str(String localized, String value, String defaultValue) {
                return interpolate(localized != null ? localized : (value != null ? value : defaultValue));
            }
            private String[] arr(String[] localized, String[] value, String[] defaultValue) {
                return interpolate(localized != null ? localized : (value != null ? value.clone() : defaultValue));
            }
            private String   resourceStr(String key) { return messages == null ? null : messages.getString(key, null); }
            private String[] resourceArr(String key) { return messages == null ? null : messages.getStringArray(key, null); }

            /** Returns the optional heading preceding the header section. Initialized from {@link Command#headerHeading()}, or {@code ""} (empty string). */
            public String headerHeading() { return str(resourceStr("usage.headerHeading"), headerHeading, DEFAULT_SINGLE_VALUE); }

            /** Returns the optional header lines displayed at the top of the help message. For subcommands, the first header line is
             * displayed in the list of commands. Values are initialized from {@link Command#header()}
             * if the {@code Command} annotation is present, otherwise this is an empty array and the help message has no
             * header. Applications may programmatically set this field to create a custom help message. */
            public String[] header() { return arr(resourceArr("usage.header"), header, DEFAULT_MULTI_LINE); }

            /** Returns the optional heading preceding the synopsis. Initialized from {@link Command#synopsisHeading()}, {@code "Usage: "} by default. */
            public String synopsisHeading() { return str(resourceStr("usage.synopsisHeading"), synopsisHeading, DEFAULT_SYNOPSIS_HEADING); }

            /** Returns whether the synopsis line(s) should show an abbreviated synopsis without detailed option names. */
            public boolean abbreviateSynopsis() { return (abbreviateSynopsis == null) ? DEFAULT_ABBREVIATE_SYNOPSIS : abbreviateSynopsis; }

            /** Returns the optional custom synopsis lines to use instead of the auto-generated synopsis.
             * Initialized from {@link Command#customSynopsis()} if the {@code Command} annotation is present,
             * otherwise this is an empty array and the synopsis is generated.
             * Applications may programmatically set this field to create a custom help message. */
            public String[] customSynopsis() { return arr(resourceArr("usage.customSynopsis"), customSynopsis, DEFAULT_MULTI_LINE); }

            /** Returns the optional heading preceding the description section. Initialized from {@link Command#descriptionHeading()}, or null. */
            public String descriptionHeading() { return str(resourceStr("usage.descriptionHeading"), descriptionHeading, DEFAULT_SINGLE_VALUE); }

            /** Returns the optional text lines to use as the description of the help message, displayed between the synopsis and the
             * options list. Initialized from {@link Command#description()} if the {@code Command} annotation is present,
             * otherwise this is an empty array and the help message has no description.
             * Applications may programmatically set this field to create a custom help message. */
            public String[] description() { return arr(resourceArr("usage.description"), description, DEFAULT_MULTI_LINE); }

            /** Returns the optional heading preceding the parameter list. Initialized from {@link Command#parameterListHeading()}, or null. */
            public String parameterListHeading() { return str(resourceStr("usage.parameterListHeading"), parameterListHeading, DEFAULT_SINGLE_VALUE); }

            /** Returns the optional heading preceding the options list. Initialized from {@link Command#optionListHeading()}, or null. */
            public String optionListHeading() { return str(resourceStr("usage.optionListHeading"), optionListHeading, DEFAULT_SINGLE_VALUE); }

            /** Returns whether the options list in the usage help message should be sorted alphabetically. */
            public boolean sortOptions() { return (sortOptions == null) ? DEFAULT_SORT_OPTIONS : sortOptions; }

            /** Returns the character used to prefix required options in the options list. */
            public char requiredOptionMarker() { return (requiredOptionMarker == null) ? DEFAULT_REQUIRED_OPTION_MARKER : requiredOptionMarker; }

            /** Returns whether the options list in the usage help message should show default values for all non-boolean options. */
            public boolean showDefaultValues() { return (showDefaultValues == null) ? DEFAULT_SHOW_DEFAULT_VALUES : showDefaultValues; }

            /**
             * Returns whether this command should be hidden from the usage help message of the parent command.
             * @return {@code true} if this command should not appear in the usage help message of the parent command
             */
            public boolean hidden() { return (hidden == null) ? DEFAULT_HIDDEN : hidden; }

            /** Returns the optional heading preceding the subcommand list. Initialized from {@link Command#commandListHeading()}. {@code "Commands:%n"} by default. */
            public String commandListHeading() { return str(resourceStr("usage.commandListHeading"), commandListHeading, DEFAULT_COMMAND_LIST_HEADING); }

            /** Returns the optional heading preceding the exit codes section, may contain {@code "%n"} line separators. {@code ""} (empty string) by default. */
            public String exitCodeListHeading() { return str(resourceStr("usage.exitCodeListHeading"), exitCodeListHeading, DEFAULT_SINGLE_VALUE); }

            /** Returns an unmodifiable map with values to be displayed in the exit codes section: keys are exit codes, values are descriptions.
             * Descriptions may contain {@code "%n"} line separators.
             * Callers may be interested in the {@link UsageMessageSpec#keyValuesMap(String...) keyValuesMap} method for creating a map from a list of {@code "key:value"} Strings.
             * <p>This may be configured in a resource bundle by listing up multiple {@code "key:value"} pairs. For example:</p>
             * <pre>
             * usage.exitCodeList.0 = 0:Successful program execution.
             * usage.exitCodeList.1 = 64:Invalid input: an unknown option or invalid parameter was specified.
             * usage.exitCodeList.2 = 70:Execution exception: an exception occurred while executing the business logic.
             * </pre>
             * @return an unmodifiable map with values to be displayed in the exit codes section, or an empty map if no exit codes are {@linkplain #exitCodeList(Map) registered}.
             * @see #keyValuesMap(String...)
             * @since 4.0 */
            public Map<String, String> exitCodeList() {
                String[] bundleValues = resourceArr("usage.exitCodeList");
                if (bundleValues == null && exitCodeList != null) { return exitCodeList; }
                Map<String, String> result = keyValuesMap(arr(bundleValues, exitCodeListStrings, DEFAULT_MULTI_LINE));
                return result == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(result);
            }

            /** Creates and returns a {@code Map} that contains an entry for each specified String that is in {@code "key:value"} format.
             * @param entries the strings to process; values that are not in {@code "key:value"} format are ignored
             * @return a {@code Map} with an entry for each line, preserving the input order
             * @since 4.0 */
            public static Map<String, String> keyValuesMap(String... entries) {
                Map<String, String> result = new LinkedHashMap<String, String>();
                if (entries == null) { return result; }
                for (int i = 0; i < entries.length; i++) {
                    int pos = entries[i].indexOf(':');
                    if (pos >= 0) {
                        result.put(entries[i].substring(0, pos), entries[i].substring(pos + 1));
                    } else {
                        new Tracer().info("Ignoring line at index %d: cannot split '%s' into 'key:value'%n", i, entries[i]);
                    }
                }
                return result;
            }

            /** Returns the optional heading preceding the footer section. Initialized from {@link Command#footerHeading()}, or {@code ""} (empty string). */
            public String footerHeading() { return str(resourceStr("usage.footerHeading"), footerHeading, DEFAULT_SINGLE_VALUE); }

            /** Returns the optional footer text lines displayed at the bottom of the help message. Initialized from
             * {@link Command#footer()} if the {@code Command} annotation is present, otherwise this is an empty array and
             * the help message has no footer.
             * Applications may programmatically set this field to create a custom help message. */
            public String[] footer() { return arr(resourceArr("usage.footer"), footer, DEFAULT_MULTI_LINE); }

            /** Sets the heading preceding the header section. Initialized from {@link Command#headerHeading()}, or null.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec headerHeading(String headerHeading) { this.headerHeading = headerHeading; return this; }

            /** Sets the optional header lines displayed at the top of the help message. For subcommands, the first header line is
             * displayed in the list of commands.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec header(String... header) { this.header = header; return this; }

            /** Sets the optional heading preceding the synopsis.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec synopsisHeading(String newValue) {synopsisHeading = newValue; return this;}

            /** Sets whether the synopsis line(s) should show an abbreviated synopsis without detailed option names.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec abbreviateSynopsis(boolean newValue) {abbreviateSynopsis = newValue; return this;}

            /** Sets the optional custom synopsis lines to use instead of the auto-generated synopsis.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec customSynopsis(String... customSynopsis) { this.customSynopsis = customSynopsis; return this; }

            /** Sets the heading preceding the description section.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec descriptionHeading(String newValue) {descriptionHeading = newValue; return this;}

            /** Sets the optional text lines to use as the description of the help message, displayed between the synopsis and the
             * options list.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec description(String... description) { this.description = description; return this; }

            /** Sets the optional heading preceding the parameter list.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec parameterListHeading(String newValue) {parameterListHeading = newValue; return this;}

            /** Sets the heading preceding the options list.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec optionListHeading(String newValue) {optionListHeading = newValue; return this;}

            /** Sets whether the options list in the usage help message should be sorted alphabetically.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec sortOptions(boolean newValue) {sortOptions = newValue; return this;}

            /** Sets the character used to prefix required options in the options list.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec requiredOptionMarker(char newValue) {requiredOptionMarker = newValue; return this;}

            /** Sets whether the options list in the usage help message should show default values for all non-boolean options.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec showDefaultValues(boolean newValue) {showDefaultValues = newValue; return this;}

            /**
             * Set the hidden flag on this command to control whether to show or hide it in the help usage text of the parent command.
             * @param value enable or disable the hidden flag
             * @return this UsageMessageSpec for method chaining
             * @see Command#hidden() */
            public UsageMessageSpec hidden(boolean value) { hidden = value; return this; }

            /** Sets the optional heading preceding the subcommand list.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec commandListHeading(String newValue) {commandListHeading = newValue; return this;}

            /** Sets the optional heading preceding the exit codes section, may contain {@code "%n"} line separators. {@code ""} (empty string) by default.
             * @since 4.0 */
            public UsageMessageSpec exitCodeListHeading(String newValue) { exitCodeListHeading = newValue; return this;}

            /** Sets the values to be displayed in the exit codes section: keys are exit codes, values are descriptions.
             * Descriptions may contain {@code "%n"} line separators.
             * <p>This may be configured in a resource bundle by listing up multiple {@code "key:value"} pairs. For example:</p>
             * <pre>
             * usage.exitCodeList.0 = 0:Successful program execution.
             * usage.exitCodeList.1 = 64:Invalid input: an unknown option or invalid parameter was specified.
             * usage.exitCodeList.2 = 70:Execution exception: an exception occurred while executing the business logic.
             * </pre>
             * @newValue a map with values to be displayed in the exit codes section
             * @see #keyValuesMap(String...)
             * @since 4.0 */
            public UsageMessageSpec exitCodeList(Map<String, String> newValue) { exitCodeList = newValue == null ? null : Collections.unmodifiableMap(new LinkedHashMap<String, String>(newValue)); return this;}

            /** Sets the optional heading preceding the footer section.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec footerHeading(String newValue) {footerHeading = newValue; return this;}

            /** Sets the optional footer text lines displayed at the bottom of the help message.
             * @return this UsageMessageSpec for method chaining */
            public UsageMessageSpec footer(String... footer) { this.footer = footer; return this; }
            /** Returns the Messages for this usage help message specification, or {@code null}.
             * @return the Messages object that encapsulates this {@linkplain CommandSpec#resourceBundle() command's resource bundle}
             * @since 3.6 */
            public Messages messages() { return messages; }
            /** Sets the Messages for this usageMessage specification, and returns this UsageMessageSpec.
             * @param msgs the new Messages value that encapsulates this {@linkplain CommandSpec#resourceBundle() command's resource bundle}, may be {@code null}
             * @since 3.6 */
            public UsageMessageSpec messages(Messages msgs) { messages = msgs; return this; }
            /**
             * Returns whether line breaks should take wide Chinese, Japanese and Korean characters into account for line-breaking purposes.
             * @return true if wide Chinese, Japanese and Korean characters are counted as double the size of other characters for line-breaking purposes
             * @since 4.0 */
            public boolean adjustLineBreaksForWideCJKCharacters() { return adjustLineBreaksForWideCJKCharacters == null ? DEFAULT_ADJUST_CJK : adjustLineBreaksForWideCJKCharacters; }
            /** Sets whether line breaks should take wide Chinese, Japanese and Korean characters into account, and returns this UsageMessageSpec.
             * @param adjustForWideChars if true, wide Chinese, Japanese and Korean characters are counted as double the size of other characters for line-breaking purposes
             * @since 4.0 */
            public UsageMessageSpec adjustLineBreaksForWideCJKCharacters(boolean adjustForWideChars) { adjustLineBreaksForWideCJKCharacters = adjustForWideChars; return this; }

            void updateFromCommand(Command cmd, CommandSpec commandSpec) {
                if (!empty(cmd.resourceBundle())) { // else preserve superclass bundle
                    messages(new Messages(commandSpec, cmd.resourceBundle()));
                }
                if (isNonDefault(cmd.synopsisHeading(), DEFAULT_SYNOPSIS_HEADING))            {synopsisHeading = cmd.synopsisHeading();}
                if (isNonDefault(cmd.commandListHeading(), DEFAULT_COMMAND_LIST_HEADING))     {commandListHeading = cmd.commandListHeading();}
                if (isNonDefault(cmd.requiredOptionMarker(), DEFAULT_REQUIRED_OPTION_MARKER)) {requiredOptionMarker = cmd.requiredOptionMarker();}
                if (isNonDefault(cmd.abbreviateSynopsis(), DEFAULT_ABBREVIATE_SYNOPSIS))      {abbreviateSynopsis = cmd.abbreviateSynopsis();}
                if (isNonDefault(cmd.sortOptions(), DEFAULT_SORT_OPTIONS))                    {sortOptions = cmd.sortOptions();}
                if (isNonDefault(cmd.showDefaultValues(), DEFAULT_SHOW_DEFAULT_VALUES))       {showDefaultValues = cmd.showDefaultValues();}
                if (isNonDefault(cmd.hidden(), DEFAULT_HIDDEN))                               {hidden = cmd.hidden();}
                if (isNonDefault(cmd.customSynopsis(), DEFAULT_MULTI_LINE))                   {customSynopsis = cmd.customSynopsis().clone();}
                if (isNonDefault(cmd.description(), DEFAULT_MULTI_LINE))                      {description = cmd.description().clone();}
                if (isNonDefault(cmd.descriptionHeading(), DEFAULT_SINGLE_VALUE))             {descriptionHeading = cmd.descriptionHeading();}
                if (isNonDefault(cmd.header(), DEFAULT_MULTI_LINE))                           {header = cmd.header().clone();}
                if (isNonDefault(cmd.headerHeading(), DEFAULT_SINGLE_VALUE))                  {headerHeading = cmd.headerHeading();}
                if (isNonDefault(cmd.exitCodeList(), DEFAULT_MULTI_LINE))                     {exitCodeListStrings = cmd.exitCodeList().clone();}
                if (isNonDefault(cmd.exitCodeListHeading(), DEFAULT_SINGLE_VALUE))            {exitCodeListHeading = cmd.exitCodeListHeading();}
                if (isNonDefault(cmd.footer(), DEFAULT_MULTI_LINE))                           {footer = cmd.footer().clone();}
                if (isNonDefault(cmd.footerHeading(), DEFAULT_SINGLE_VALUE))                  {footerHeading = cmd.footerHeading();}
                if (isNonDefault(cmd.parameterListHeading(), DEFAULT_SINGLE_VALUE))           {parameterListHeading = cmd.parameterListHeading();}
                if (isNonDefault(cmd.optionListHeading(), DEFAULT_SINGLE_VALUE))              {optionListHeading = cmd.optionListHeading();}
                if (isNonDefault(cmd.usageHelpWidth(), DEFAULT_USAGE_WIDTH))                  {width(cmd.usageHelpWidth());} // validate
            }
            void initFromMixin(UsageMessageSpec mixin, CommandSpec commandSpec) {
                if (initializable(synopsisHeading, mixin.synopsisHeading(), DEFAULT_SYNOPSIS_HEADING))                 {synopsisHeading = mixin.synopsisHeading();}
                if (initializable(commandListHeading, mixin.commandListHeading(), DEFAULT_COMMAND_LIST_HEADING))       {commandListHeading = mixin.commandListHeading();}
                if (initializable(requiredOptionMarker, mixin.requiredOptionMarker(), DEFAULT_REQUIRED_OPTION_MARKER)) {requiredOptionMarker = mixin.requiredOptionMarker();}
                if (initializable(abbreviateSynopsis, mixin.abbreviateSynopsis(), DEFAULT_ABBREVIATE_SYNOPSIS))        {abbreviateSynopsis = mixin.abbreviateSynopsis();}
                if (initializable(sortOptions, mixin.sortOptions(), DEFAULT_SORT_OPTIONS))                             {sortOptions = mixin.sortOptions();}
                if (initializable(showDefaultValues, mixin.showDefaultValues(), DEFAULT_SHOW_DEFAULT_VALUES))          {showDefaultValues = mixin.showDefaultValues();}
                if (initializable(hidden, mixin.hidden(), DEFAULT_HIDDEN))                                             {hidden = mixin.hidden();}
                if (initializable(customSynopsis, mixin.customSynopsis(), DEFAULT_MULTI_LINE))                         {customSynopsis = mixin.customSynopsis().clone();}
                if (initializable(description, mixin.description(), DEFAULT_MULTI_LINE))                               {description = mixin.description().clone();}
                if (initializable(descriptionHeading, mixin.descriptionHeading(), DEFAULT_SINGLE_VALUE))               {descriptionHeading = mixin.descriptionHeading();}
                if (initializable(header, mixin.header(), DEFAULT_MULTI_LINE))                                         {header = mixin.header().clone();}
                if (initializable(headerHeading, mixin.headerHeading(), DEFAULT_SINGLE_VALUE))                         {headerHeading = mixin.headerHeading();}
                if (initializable(exitCodeList, mixin.exitCodeList(), Collections.emptyMap()) && exitCodeListStrings == null) {exitCodeList = Collections.unmodifiableMap(new LinkedHashMap<String, String>(mixin.exitCodeList()));}
                if (initializable(exitCodeListHeading, mixin.exitCodeListHeading(), DEFAULT_SINGLE_VALUE))             {exitCodeListHeading = mixin.exitCodeListHeading();}
                if (initializable(footer, mixin.footer(), DEFAULT_MULTI_LINE))                                         {footer = mixin.footer().clone();}
                if (initializable(footerHeading, mixin.footerHeading(), DEFAULT_SINGLE_VALUE))                         {footerHeading = mixin.footerHeading();}
                if (initializable(parameterListHeading, mixin.parameterListHeading(), DEFAULT_SINGLE_VALUE))           {parameterListHeading = mixin.parameterListHeading();}
                if (initializable(optionListHeading, mixin.optionListHeading(), DEFAULT_SINGLE_VALUE))                 {optionListHeading = mixin.optionListHeading();}
                if (Messages.empty(messages)) { messages(Messages.copy(commandSpec, mixin.messages())); }
                if (initializable(adjustLineBreaksForWideCJKCharacters, mixin.adjustLineBreaksForWideCJKCharacters(), DEFAULT_ADJUST_CJK)) {adjustLineBreaksForWideCJKCharacters = mixin.adjustLineBreaksForWideCJKCharacters();}
            }
            void initFrom(UsageMessageSpec settings, CommandSpec commandSpec) {
                description = settings.description;
                customSynopsis = settings.customSynopsis;
                header = settings.header;
                footer = settings.footer;
                abbreviateSynopsis = settings.abbreviateSynopsis;
                sortOptions = settings.sortOptions;
                showDefaultValues = settings.showDefaultValues;
                hidden = settings.hidden;
                requiredOptionMarker = settings.requiredOptionMarker;
                headerHeading = settings.headerHeading;
                synopsisHeading = settings.synopsisHeading;
                descriptionHeading = settings.descriptionHeading;
                parameterListHeading = settings.parameterListHeading;
                optionListHeading = settings.optionListHeading;
                commandListHeading = settings.commandListHeading;
                footerHeading = settings.footerHeading;
                width = settings.width;
                messages = Messages.copy(commandSpec, settings.messages());
                adjustLineBreaksForWideCJKCharacters = settings.adjustLineBreaksForWideCJKCharacters;
            }
        }
        /** Models parser configuration specification.
         * @since 3.0 */
        public static class ParserSpec {

            /** Constant String holding the default separator between options and option parameters: <code>{@value}</code>.*/
            static final String DEFAULT_SEPARATOR = "=";
            private String separator;
            private boolean stopAtUnmatched = false;
            private boolean stopAtPositional = false;
            private String endOfOptionsDelimiter = "--";
            private boolean toggleBooleanFlags = true;
            private boolean overwrittenOptionsAllowed = false;
            private boolean unmatchedArgumentsAllowed = false;
            private boolean expandAtFiles = true;
            private boolean useSimplifiedAtFiles = false;
            private Character atFileCommentChar = '#';
            private boolean posixClusteredShortOptionsAllowed = true;
            private boolean unmatchedOptionsArePositionalParams = false;
            private boolean limitSplit = false;
            private boolean aritySatisfiedByAttachedOptionParam = false;
            private boolean collectErrors = false;
            private boolean caseInsensitiveEnumValuesAllowed = false;
            private boolean trimQuotes = shouldTrimQuotes();
            private boolean splitQuotedStrings = false;

            /** Returns the String to use as the separator between options and option parameters. {@code "="} by default,
             * initialized from {@link Command#separator()} if defined.*/
            public String separator() { return (separator == null) ? DEFAULT_SEPARATOR : separator; }

            /** @see CommandLine#isStopAtUnmatched() */
            public boolean stopAtUnmatched()                   { return stopAtUnmatched; }
            /** @see CommandLine#isStopAtPositional() */
            public boolean stopAtPositional()                  { return stopAtPositional; }
            /** @see CommandLine#getEndOfOptionsDelimiter()
             * @since 3.5 */
            public String endOfOptionsDelimiter()             { return endOfOptionsDelimiter; }
            /** @see CommandLine#isToggleBooleanFlags() */
            public boolean toggleBooleanFlags()                { return toggleBooleanFlags; }
            /** @see CommandLine#isOverwrittenOptionsAllowed() */
            public boolean overwrittenOptionsAllowed()         { return overwrittenOptionsAllowed; }
            /** @see CommandLine#isUnmatchedArgumentsAllowed() */
            public boolean unmatchedArgumentsAllowed()         { return unmatchedArgumentsAllowed; }
            /** @see CommandLine#isExpandAtFiles() */
            public boolean expandAtFiles()                     { return expandAtFiles; }
            /** @see CommandLine#getAtFileCommentChar()
             * @since 3.5 */
            public Character atFileCommentChar()               { return atFileCommentChar; }
            /** @see CommandLine#isUseSimplifiedAtFiles()
             * @since 3.9 */
            public boolean useSimplifiedAtFiles()              {
                String value = System.getProperty("picocli.useSimplifiedAtFiles");
                if (value != null) {
                    return "".equals(value) || Boolean.valueOf(value);
                }
                return useSimplifiedAtFiles;
            }
            /** @see CommandLine#isPosixClusteredShortOptionsAllowed() */
            public boolean posixClusteredShortOptionsAllowed() { return posixClusteredShortOptionsAllowed; }
            /** @see CommandLine#isCaseInsensitiveEnumValuesAllowed()
             * @since 3.4 */
            public boolean caseInsensitiveEnumValuesAllowed()  { return caseInsensitiveEnumValuesAllowed; }
            /** @see CommandLine#isTrimQuotes()
             * @since 3.7 */
            public boolean trimQuotes()  { return trimQuotes; }
            /** @see CommandLine#isSplitQuotedStrings()
             * @since 3.7 */
            public boolean splitQuotedStrings()  { return splitQuotedStrings; }
            /** @see CommandLine#isUnmatchedOptionsArePositionalParams() */
            public boolean unmatchedOptionsArePositionalParams() { return unmatchedOptionsArePositionalParams; }
            private boolean splitFirst()                       { return limitSplit(); }
            /** Returns true if arguments should be split first before any further processing and the number of
             * parts resulting from the split is limited to the max arity of the argument. */
            public boolean limitSplit()                        { return limitSplit; }
            /** Returns true if options with attached arguments should not consume subsequent arguments and should not validate arity. The default is {@code false}. */
            public boolean aritySatisfiedByAttachedOptionParam() { return aritySatisfiedByAttachedOptionParam; }
            /** Returns true if exceptions during parsing should be collected instead of thrown.
             * Multiple errors may be encountered during parsing. These can be obtained from {@link ParseResult#errors()}.
             * @since 3.2 */
            public boolean collectErrors()                     { return collectErrors; }

            /** Sets the String to use as the separator between options and option parameters.
             * @return this ParserSpec for method chaining */
            public ParserSpec separator(String separator)                                  { this.separator = separator; return this; }
            /** @see CommandLine#setStopAtUnmatched(boolean) */
            public ParserSpec stopAtUnmatched(boolean stopAtUnmatched)                     { this.stopAtUnmatched = stopAtUnmatched; return this; }
            /** @see CommandLine#setStopAtPositional(boolean) */
            public ParserSpec stopAtPositional(boolean stopAtPositional)                   { this.stopAtPositional = stopAtPositional; return this; }
            /** @see CommandLine#setEndOfOptionsDelimiter(String)
             * @since 3.5 */
            public ParserSpec endOfOptionsDelimiter(String delimiter)                      { this.endOfOptionsDelimiter = Assert.notNull(delimiter, "end-of-options delimiter"); return this; }
            /** @see CommandLine#setToggleBooleanFlags(boolean) */
            public ParserSpec toggleBooleanFlags(boolean toggleBooleanFlags)               { this.toggleBooleanFlags = toggleBooleanFlags; return this; }
            /** @see CommandLine#setOverwrittenOptionsAllowed(boolean) */
            public ParserSpec overwrittenOptionsAllowed(boolean overwrittenOptionsAllowed) { this.overwrittenOptionsAllowed = overwrittenOptionsAllowed; return this; }
            /** @see CommandLine#setUnmatchedArgumentsAllowed(boolean) */
            public ParserSpec unmatchedArgumentsAllowed(boolean unmatchedArgumentsAllowed) { this.unmatchedArgumentsAllowed = unmatchedArgumentsAllowed; return this; }
            /** @see CommandLine#setExpandAtFiles(boolean) */
            public ParserSpec expandAtFiles(boolean expandAtFiles)                         { this.expandAtFiles = expandAtFiles; return this; }
            /** @see CommandLine#setAtFileCommentChar(Character)
             * @since 3.5 */
            public ParserSpec atFileCommentChar(Character atFileCommentChar)               { this.atFileCommentChar = atFileCommentChar; return this; }
            /** @see CommandLine#setUseSimplifiedAtFiles(boolean)
             * @since 3.9 */
            public ParserSpec useSimplifiedAtFiles(boolean useSimplifiedAtFiles)           { this.useSimplifiedAtFiles = useSimplifiedAtFiles; return this; }
            /** @see CommandLine#setPosixClusteredShortOptionsAllowed(boolean) */
            public ParserSpec posixClusteredShortOptionsAllowed(boolean posixClusteredShortOptionsAllowed) { this.posixClusteredShortOptionsAllowed = posixClusteredShortOptionsAllowed; return this; }
            /** @see CommandLine#setCaseInsensitiveEnumValuesAllowed(boolean)
             * @since 3.4 */
            public ParserSpec caseInsensitiveEnumValuesAllowed(boolean caseInsensitiveEnumValuesAllowed) { this.caseInsensitiveEnumValuesAllowed = caseInsensitiveEnumValuesAllowed; return this; }
            /** @see CommandLine#setTrimQuotes(boolean)
             * @since 3.7 */
            public ParserSpec trimQuotes(boolean trimQuotes) { this.trimQuotes = trimQuotes; return this; }
            /** @see CommandLine#setSplitQuotedStrings(boolean)
             * @since 3.7 */
            public ParserSpec splitQuotedStrings(boolean splitQuotedStrings)  { this.splitQuotedStrings = splitQuotedStrings; return this; }
            /** @see CommandLine#setUnmatchedOptionsArePositionalParams(boolean) */
            public ParserSpec unmatchedOptionsArePositionalParams(boolean unmatchedOptionsArePositionalParams) { this.unmatchedOptionsArePositionalParams = unmatchedOptionsArePositionalParams; return this; }
            /** Sets whether exceptions during parsing should be collected instead of thrown.
             * Multiple errors may be encountered during parsing. These can be obtained from {@link ParseResult#errors()}.
             * @since 3.2 */
            public ParserSpec collectErrors(boolean collectErrors)                         { this.collectErrors = collectErrors; return this; }

            /** Returns true if options with attached arguments should not consume subsequent arguments and should not validate arity. The default is {@code false}.*/
            public ParserSpec aritySatisfiedByAttachedOptionParam(boolean newValue) { aritySatisfiedByAttachedOptionParam = newValue; return this; }

            /** Sets whether arguments should be {@linkplain ArgSpec#splitRegex() split} first before any further processing.
             * If true, the original argument will only be split into as many parts as allowed by max arity. */
            public ParserSpec limitSplit(boolean limitSplit)                               { this.limitSplit = limitSplit; return this; }

            private boolean shouldTrimQuotes() {
                String value = System.getProperty("picocli.trimQuotes");
                if ("".equals(value)) { value = "true"; }
                return Boolean.valueOf(value);
            }

            void initSeparator(String value)   { if (initializable(separator, value, DEFAULT_SEPARATOR)) {separator = value;} }
            void updateSeparator(String value) { if (isNonDefault(value, DEFAULT_SEPARATOR))             {separator = value;} }
            public String toString() {
                return String.format("posixClusteredShortOptionsAllowed=%s, stopAtPositional=%s, stopAtUnmatched=%s, " +
                                "separator=%s, overwrittenOptionsAllowed=%s, unmatchedArgumentsAllowed=%s, expandAtFiles=%s, " +
                                "atFileCommentChar=%s, useSimplifiedAtFiles=%s, endOfOptionsDelimiter=%s, limitSplit=%s, aritySatisfiedByAttachedOptionParam=%s, " +
                                "toggleBooleanFlags=%s, unmatchedOptionsArePositionalParams=%s, collectErrors=%s," +
                                "caseInsensitiveEnumValuesAllowed=%s, trimQuotes=%s, splitQuotedStrings=%s",
                        posixClusteredShortOptionsAllowed, stopAtPositional, stopAtUnmatched,
                        separator, overwrittenOptionsAllowed, unmatchedArgumentsAllowed, expandAtFiles,
                        atFileCommentChar, useSimplifiedAtFiles, endOfOptionsDelimiter, limitSplit, aritySatisfiedByAttachedOptionParam,
                        toggleBooleanFlags, unmatchedOptionsArePositionalParams, collectErrors,
                        caseInsensitiveEnumValuesAllowed, trimQuotes, splitQuotedStrings);
            }

            void initFrom(ParserSpec settings) {
                separator = settings.separator;
                stopAtUnmatched = settings.stopAtUnmatched;
                stopAtPositional = settings.stopAtPositional;
                endOfOptionsDelimiter = settings.endOfOptionsDelimiter;
                toggleBooleanFlags = settings.toggleBooleanFlags;
                overwrittenOptionsAllowed = settings.overwrittenOptionsAllowed;
                unmatchedArgumentsAllowed = settings.unmatchedArgumentsAllowed;
                expandAtFiles = settings.expandAtFiles;
                atFileCommentChar = settings.atFileCommentChar;
                posixClusteredShortOptionsAllowed = settings.posixClusteredShortOptionsAllowed;
                unmatchedOptionsArePositionalParams = settings.unmatchedOptionsArePositionalParams;
                limitSplit = settings.limitSplit;
                aritySatisfiedByAttachedOptionParam = settings.aritySatisfiedByAttachedOptionParam;
                collectErrors = settings.collectErrors;
                caseInsensitiveEnumValuesAllowed = settings.caseInsensitiveEnumValuesAllowed;
                trimQuotes = settings.trimQuotes;
                splitQuotedStrings = settings.splitQuotedStrings;
            }
        }
        /** Models the shared attributes of {@link OptionSpec} and {@link PositionalParamSpec}.
         * @since 3.0 */
        public abstract static class ArgSpec {
            static final String DESCRIPTION_VARIABLE_DEFAULT_VALUE = "${DEFAULT-VALUE}";
            static final String DESCRIPTION_VARIABLE_COMPLETION_CANDIDATES = "${COMPLETION-CANDIDATES}";
            private static final String NO_DEFAULT_VALUE = "__no_default_value__";

            // help-related fields
            private final boolean hidden;
            private final String paramLabel;
            private final boolean hideParamSyntax;
            private final String[] description;
            private final String descriptionKey;
            private final Help.Visibility showDefaultValue;
            private Messages messages;
            CommandSpec commandSpec;
            private ArgGroupSpec group;
            private final Object userObject;

            // parser fields
            private final boolean interactive;
            private final boolean required;
            private final String splitRegex;
            private final ITypeInfo typeInfo;
            private final ITypeConverter<?>[] converters;
            private final Iterable<String> completionCandidates;
            private final String defaultValue;
            private final Object initialValue;
            private final boolean hasInitialValue;
            private final IGetter getter;
            private final ISetter setter;
            private final IScope scope;
            private Range arity;
            private List<String> stringValues = new ArrayList<String>();
            private List<String> originalStringValues = new ArrayList<String>();
            protected String toString;
            private List<Object> typedValues = new ArrayList<Object>();
            Map<Integer, Object> typedValueAtPosition = new TreeMap<Integer, Object>();

            /** Constructs a new {@code ArgSpec}. */
            private <T extends Builder<T>> ArgSpec(Builder<T> builder) {
                userObject = builder.userObject;
                description = builder.description == null ? new String[0] : builder.description;
                descriptionKey = builder.descriptionKey;
                splitRegex = builder.splitRegex == null ? "" : builder.splitRegex;
                paramLabel = empty(builder.paramLabel) ? "PARAM" : builder.paramLabel;
                hideParamSyntax = builder.hideParamSyntax;
                converters = builder.converters == null ? new ITypeConverter<?>[0] : builder.converters;
                showDefaultValue = builder.showDefaultValue == null ? Help.Visibility.ON_DEMAND : builder.showDefaultValue;
                hidden = builder.hidden;
                interactive = builder.interactive;
                initialValue = builder.initialValue;
                hasInitialValue = builder.hasInitialValue;
                defaultValue = NO_DEFAULT_VALUE.equals(builder.defaultValue) ? null : builder.defaultValue;
                required = builder.required;
                toString = builder.toString;
                getter = builder.getter;
                setter = builder.setter;
                scope  = builder.scope;

                Range tempArity = builder.arity;
                if (tempArity == null) {
                    if (interactive) {
                        tempArity = Range.valueOf("0");
                    } else if (isOption()) {
                        tempArity = (builder.type == null || isBoolean(builder.type)) ? Range.valueOf("0") : Range.valueOf("1");
                    } else {
                        tempArity = Range.valueOf("1");
                    }
                    tempArity = tempArity.unspecified(true);
                }
                arity = tempArity;

                if (builder.typeInfo == null) {
                    this.typeInfo = RuntimeTypeInfo.create(builder.type, builder.auxiliaryTypes,
                            Collections.<String>emptyList(), arity, (isOption() ? boolean.class : String.class), interactive);
                } else {
                    this.typeInfo = builder.typeInfo;
                }

                if (builder.completionCandidates == null && typeInfo.isEnum()) {
                    List<String> list = new ArrayList<String>();
                    for (Object c : typeInfo.getEnumConstantNames()) { list.add(c.toString()); }
                    completionCandidates = Collections.unmodifiableList(list);
                } else {
                    completionCandidates = builder.completionCandidates;
                }
                if (interactive && !arity.isValidForInteractiveArgs()) {
                    throw new InitializationException("Interactive options and positional parameters are only supported for arity=0 and arity=0..1; not for arity=" + arity);
                }
            }
            void applyInitialValue(Tracer tracer) {
                if (hasInitialValue()) {
                    try {
                        setter().set(initialValue());
                        tracer.debug("Set initial value for %s of type %s to %s.%n", this, type(), String.valueOf(initialValue()));
                    } catch (Exception ex) {
                        tracer.warn("Could not set initial value for %s of type %s to %s: %s%n", this, type(), String.valueOf(initialValue()), ex);
                    }
                } else {
                    tracer.debug("Initial value not available for %s%n", this);
                }
            }

            /** Returns whether this is a required option or positional parameter without a default value.
             * If this argument is part of a {@linkplain ArgGroup group}, this method returns whether this argument is required <em>within the group</em> (so it is not necessarily a required argument for the command).
             * @see Option#required() */
            public boolean required() {
                //#261 not required if it has a default; #676 default value may be a variable
                return required && defaultValue() == null && defaultValueFromProvider() == null;
            }
            /** Returns whether this option will prompt the user to enter a value on the command line.
             * @see Option#interactive() */
            public boolean interactive()   { return interactive; }

            /** Returns the description of this option or positional parameter, after all variables have been rendered,
             * including the {@code ${DEFAULT-VALUE}} and {@code ${COMPLETION-CANDIDATES}} variables.
             * Use {@link CommandSpec#interpolateVariables(Boolean)} to switch off variable expansion if needed.
             * <p>
             * If a resource bundle has been {@linkplain ArgSpec#messages(Messages) set}, this method will first try to find a value in the resource bundle:
             * If the resource bundle has no entry for the {@code fully qualified commandName + "." + descriptionKey} or for the unqualified {@code descriptionKey},
             * an attempt is made to find the option or positional parameter description using any of the
             * {@linkplain #getAdditionalDescriptionKeys() additional description keys}, first with the {@code fully qualified commandName + "."} prefix, then without.
             * </p>
             * @see CommandSpec#qualifiedName(String)
             * @see #getAdditionalDescriptionKeys()
             * @see Parameters#description()
             * @see Option#description() */
            public String[] description()  {
                String[] result = description.clone();
                if (messages() != null) { // localize if possible
                    String[] newValue = messages().getStringArray(descriptionKey(), null);
                    if (newValue == null) {
                        for (String name : getAdditionalDescriptionKeys()) {
                            newValue = messages().getStringArray(name, null);
                            if (newValue != null) { result = newValue; break; }
                        }
                    } else {
                        result = newValue;
                    }
                }
                if (commandSpec == null || commandSpec.interpolateVariables()) { // expand variables
                    result = expandVariables(result);
                }
                return result;
            }

            /** Subclasses should override to return a collection of additional description keys that may be used to find
             * description text for this option or positional parameter in the resource bundle.
             * @see OptionSpec#getAdditionalDescriptionKeys()
             * @see PositionalParamSpec#getAdditionalDescriptionKeys()
             * @since 4.0 */
            protected abstract Collection<String> getAdditionalDescriptionKeys();

            /** Returns the description key of this arg spec, used to get the description from a resource bundle.
             * @see Option#descriptionKey()
             * @see Parameters#descriptionKey()
             * @since 3.6 */
            public String descriptionKey()  { return interpolate(descriptionKey); }

            private String[] expandVariables(String[] desc) {
                if (desc.length == 0) { return desc; }
                StringBuilder candidates = new StringBuilder();
                if (completionCandidates() != null) {
                    for (String c : completionCandidates()) {
                        if (candidates.length() > 0) { candidates.append(", "); }
                        candidates.append(c);
                    }
                }
                String defaultValueString = defaultValueString();
                String[] result = new String[desc.length];
                for (int i = 0; i < desc.length; i++) {
                    result[i] = format(desc[i].replace(DESCRIPTION_VARIABLE_DEFAULT_VALUE, defaultValueString.replace("%", "%%"))
                            .replace(DESCRIPTION_VARIABLE_COMPLETION_CANDIDATES, candidates.toString()));
                }
                return interpolate(result);
            }

            /** @deprecated Use {@link #description()} instead */
            @Deprecated public String[] renderedDescription()  { return description(); }

            /** Returns how many arguments this option or positional parameter requires.
             * @see Option#arity() */
            public Range arity()           { return arity; }
    
            /** Returns the name of the option or positional parameter used in the usage help message.
             * @see Option#paramLabel() {@link Parameters#paramLabel()} */
            public String paramLabel()     { return interpolate(paramLabel); }
    
            /** Returns whether usage syntax decorations around the {@linkplain #paramLabel() paramLabel} should be suppressed.
             * The default is {@code false}: by default, the paramLabel is surrounded with {@code '['} and {@code ']'} characters
             * if the value is optional and followed by ellipses ("...") when multiple values can be specified.
             * @since 3.6.0 */
            public boolean hideParamSyntax()     { return hideParamSyntax; }
    
            /** Returns auxiliary type information used when the {@link #type()} is a generic {@code Collection}, {@code Map} or an abstract class.
             * @see Option#type() */
            public Class<?>[] auxiliaryTypes() { return typeInfo.getAuxiliaryTypes(); }
    
            /** Returns one or more {@link CommandLine.ITypeConverter type converters} to use to convert the command line
             * argument into a strongly typed value (or key-value pair for map fields). This is useful when a particular
             * option or positional parameter should use a custom conversion that is different from the normal conversion for the arg spec's type.
             * @see Option#converter() */
            public ITypeConverter<?>[] converters() { return converters.clone(); }
    
            /** Returns a regular expression to split option parameter values or {@code ""} if the value should not be split.
             * @see Option#split() */
            public String splitRegex()     { return interpolate(splitRegex); }
    
            /** Returns whether this option should be excluded from the usage message.
             * @see Option#hidden() */
            public boolean hidden()        { return hidden; }
    
            /** Returns the type to convert the option or positional parameter to before {@linkplain #setValue(Object) setting} the value. */
            public Class<?> type()         { return typeInfo.getType(); }

            /** Returns the {@code ITypeInfo} that can be used both at compile time (by annotation processors) and at runtime.
             * @since 4.0 */
            public ITypeInfo typeInfo()    { return typeInfo; }

            /** Returns the user object associated with this option or positional parameters.
             * @return may return the annotated program element, or some other useful object
             * @since 4.0 */
            public Object userObject()     { return userObject; }
    
            /** Returns the default value of this option or positional parameter, before splitting and type conversion.
             * This method returns the programmatically set value; this may differ from the default value that is actually used:
             * if this ArgSpec is part of a CommandSpec with a {@link IDefaultValueProvider}, picocli will first try to obtain
             * the default value from the default value provider, and this method is only called if the default provider is
             * {@code null} or returned a {@code null} value.
             * @return the programmatically set default value of this option/positional parameter,
             *      returning {@code null} means this option or positional parameter does not have a default
             * @see CommandSpec#defaultValueProvider()
             */
            public String defaultValue()   { return interpolate(defaultValue); }
            /** Returns the initial value this option or positional parameter. If {@link #hasInitialValue()} is true,
             * the option will be reset to the initial value before parsing (regardless of whether a default value exists),
             * to clear values that would otherwise remain from parsing previous input. */
            public Object initialValue()     { return initialValue; }
            /** Determines whether the option or positional parameter will be reset to the {@link #initialValue()}
             * before parsing new input.*/
            public boolean hasInitialValue() { return hasInitialValue; }
    
            /** Returns whether this option or positional parameter's default value should be shown in the usage help. */
            public Help.Visibility showDefaultValue() { return showDefaultValue; }

            /** Returns the default value String displayed in the description. If this ArgSpec is part of a
             * CommandSpec with a {@link IDefaultValueProvider}, this method will first try to obtain
             * the default value from the default value provider; if the provider is {@code null} or if it
             * returns a {@code null} value, then next any value set to {@link ArgSpec#defaultValue()}
             * is returned, and if this is also {@code null}, finally the {@linkplain ArgSpec#initialValue() initial value} is returned.
             * @see CommandSpec#defaultValueProvider()
             * @see ArgSpec#defaultValue() */
            public String defaultValueString() {
                String fromProvider = defaultValueFromProvider();
                // implementation note: don't call this.defaultValue(), that will interpolate variables too soon!
                String defaultVal = fromProvider == null ? this.defaultValue : fromProvider;
                Object value = defaultVal == null ? initialValue() : defaultVal;
                if (value != null && value.getClass().isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Array.getLength(value); i++) {
                        sb.append(i > 0 ? ", " : "").append(Array.get(value, i));
                    }
                    return sb.insert(0, "[").append("]").toString();
                }
                return String.valueOf(value);
            }

            private String defaultValueFromProvider() {
                String fromProvider = null;
                IDefaultValueProvider defaultValueProvider = null;
                try {
                    defaultValueProvider = commandSpec.defaultValueProvider();
                    fromProvider = defaultValueProvider == null ? null : defaultValueProvider.defaultValue(this);
                } catch (Exception ex) {
                    new Tracer().info("Error getting default value for %s from %s: %s", this, defaultValueProvider, ex);
                }
                return fromProvider;
            }

            /** Returns the explicitly set completion candidates for this option or positional parameter, valid enum
             * constant names, or {@code null} if this option or positional parameter does not have any completion
             * candidates and its type is not an enum.
             * @return the completion candidates for this option or positional parameter, valid enum constant names,
             * or {@code null}
             * @since 3.2 */
            public Iterable<String> completionCandidates() { return completionCandidates; }

            /** Returns the {@link IGetter} that is responsible for supplying the value of this argument. */
            public IGetter getter()        { return getter; }
            /** Returns the {@link ISetter} that is responsible for modifying the value of this argument. */
            public ISetter setter()        { return setter; }
            /** Returns the {@link IScope} that determines on which object to set the value (or from which object to get the value) of this argument. */
            public IScope scope()          { return scope; }

            /** Returns the current value of this argument. Delegates to the current {@link #getter()}. */
            public <T> T getValue() throws PicocliException {
                try {
                    return getter.get();
                } catch (PicocliException ex) { throw ex;
                } catch (Exception ex) {        throw new PicocliException("Could not get value for " + this + ": " + ex, ex);
                }
            }
            /** Sets the value of this argument to the specified value and returns the previous value. Delegates to the current {@link #setter()}. */
            public <T> T setValue(T newValue) throws PicocliException {
                try {
                    return setter.set(newValue);
                } catch (PicocliException ex) { throw ex;
                } catch (Exception ex) {        throw new PicocliException("Could not set value (" + newValue + ") for " + this + ": " + ex, ex);
                }
            }
            /** Sets the value of this argument to the specified value and returns the previous value. Delegates to the current {@link #setter()}.
             * @deprecated use {@link #setValue(Object)} instead. This was a design mistake.
             * @since 3.5 */
            @Deprecated public <T> T setValue(T newValue, CommandLine commandLine) throws PicocliException {
                return setValue(newValue);
            }

            /** Returns {@code true} if this argument's {@link #type()} is an array, a {@code Collection} or a {@code Map}, {@code false} otherwise. */
            public boolean isMultiValue()     { return typeInfo.isMultiValue(); }
            /** Returns {@code true} if this argument is a named option, {@code false} otherwise. */
            public abstract boolean isOption();
            /** Returns {@code true} if this argument is a positional parameter, {@code false} otherwise. */
            public abstract boolean isPositional();

            /** Returns the groups this option or positional parameter belongs to, or {@code null} if this option is not part of a group.
             * @since 4.0 */
            public ArgGroupSpec group() { return group; }

            /** Returns the untyped command line arguments matched by this option or positional parameter spec.
             * @return the matched arguments after {@linkplain #splitRegex() splitting}, but before type conversion.
             *      For map properties, {@code "key=value"} values are split into the key and the value part. */
            public List<String> stringValues() { return Collections.unmodifiableList(stringValues); }

            /** Returns the typed command line arguments matched by this option or positional parameter spec.
             * @return the matched arguments after {@linkplain #splitRegex() splitting} and type conversion.
             *      For map properties, {@code "key=value"} values are split into the key and the value part. */
            public List<Object> typedValues() { return Collections.unmodifiableList(typedValues); }

            /** Sets the {@code stringValues} to a new list instance. */
            protected void resetStringValues() { stringValues = new ArrayList<String>(); }

            /** Returns the original command line arguments matched by this option or positional parameter spec.
             * @return the matched arguments as found on the command line: empty Strings for options without value, the
             *      values have not been {@linkplain #splitRegex() split}, and for map properties values may look like {@code "key=value"}*/
            public List<String> originalStringValues() { return Collections.unmodifiableList(originalStringValues); }

            /** Sets the {@code originalStringValues} to a new list instance. */
            protected void resetOriginalStringValues() { originalStringValues = new ArrayList<String>(); }

            /** Returns whether the default for this option or positional parameter should be shown, potentially overriding the specified global setting.
             * @param usageHelpShowDefaults whether the command's UsageMessageSpec is configured to show default values. */
            protected boolean internalShowDefaultValue(boolean usageHelpShowDefaults) {
                if (showDefaultValue() == Help.Visibility.ALWAYS)   { return true; }  // override global usage help setting
                if (showDefaultValue() == Help.Visibility.NEVER)    { return false; } // override global usage help setting
                if (initialValue == null && defaultValue() == null && defaultValueFromProvider() == null) { return false; } // no default value to show
                return usageHelpShowDefaults && !isBoolean(type());
            }
            /** Returns the Messages for this arg specification, or {@code null}.
             * @since 3.6 */
            public Messages messages() { return messages; }
            /** Sets the Messages for this ArgSpec, and returns this ArgSpec.
             * @param msgs the new Messages value, may be {@code null}
             * @see Command#resourceBundle()
             * @see OptionSpec#description()
             * @see PositionalParamSpec#description()
             * @since 3.6 */
            public ArgSpec messages(Messages msgs) { messages = msgs; return this; }

            /** Returns a string respresentation of this option or positional parameter. */
            public String toString() { return toString; }
    
            String[] splitValue(String value, ParserSpec parser, Range arity, int consumed) {
                if (splitRegex().length() == 0) { return new String[] {value}; }
                int limit = parser.limitSplit() ? Math.max(arity.max - consumed, 0) : 0;
                if (parser.splitQuotedStrings()) {
                    return debug(value.split(splitRegex(), limit), "Split (ignoring quotes)", value);
                }
                return debug(splitRespectingQuotedStrings(value, limit, parser, this, splitRegex()), "Split", value);
            }
            private String[] debug(String[] result, String msg, String value) {
                Tracer t = new Tracer();
                if (t.isDebug()) {t.debug("%s with regex '%s' resulted in %s parts: %s%n", msg, splitRegex(), result.length, Arrays.asList(result));}
                return result;
            }
            // @since 3.7
            private static String[] splitRespectingQuotedStrings(String value, int limit, ParserSpec parser, ArgSpec argSpec, String splitRegex) {
                StringBuilder splittable = new StringBuilder();
                StringBuilder temp = new StringBuilder();
                StringBuilder current = splittable;
                Queue<String> quotedValues = new LinkedList<String>();
                boolean escaping = false, inQuote = false;
                for (int ch = 0, i = 0; i < value.length(); i += Character.charCount(ch)) {
                    ch = value.codePointAt(i);
                    switch (ch) {
                        case '\\': escaping = !escaping; break;
                        case '\"':
                            if (!escaping) {
                                inQuote = !inQuote;
                                current = inQuote ? temp : splittable;
                                if (inQuote) {
                                    splittable.appendCodePoint(ch);
                                    continue;
                                } else {
                                    quotedValues.add(temp.toString());
                                    temp.setLength(0);
                                }
                            }
                            break;
                        default: escaping = false; break;
                    }
                    current.appendCodePoint(ch);
                }
                if (temp.length() > 0) {
                    new Tracer().warn("Unbalanced quotes in [%s] for %s (value=%s)%n", temp, argSpec, value);
                    quotedValues.add(temp.toString());
                    temp.setLength(0);
                }
                String[] result = splittable.toString().split(splitRegex, limit);
                for (int i = 0; i < result.length; i++) {
                    result[i] = restoreQuotedValues(result[i], quotedValues, parser);
                }
                if (!quotedValues.isEmpty()) {
                    new Tracer().warn("Unable to respect quotes while splitting value %s for %s (unprocessed remainder: %s)%n", value, argSpec, quotedValues);
                    return value.split(splitRegex, limit);
                }
                return result;
            }

            private static String restoreQuotedValues(String part, Queue<String> quotedValues, ParserSpec parser) {
                StringBuilder result = new StringBuilder();
                boolean escaping = false, inQuote = false, skip = false;
                for (int ch = 0, i = 0; i < part.length(); i += Character.charCount(ch)) {
                    ch = part.codePointAt(i);
                    switch (ch) {
                        case '\\': escaping = !escaping; break;
                        case '\"':
                            if (!escaping) {
                                inQuote = !inQuote;
                                if (!inQuote) { result.append(quotedValues.remove()); }
                                skip = parser.trimQuotes();
                            }
                            break;
                        default: escaping = false; break;
                    }
                    if (!skip) { result.appendCodePoint(ch); }
                    skip = false;
                }
                return result.toString();
            }

            protected boolean equalsImpl(ArgSpec other) {
                boolean result = Assert.equals(this.defaultValue, other.defaultValue)
                        && Assert.equals(this.arity, other.arity)
                        && Assert.equals(this.hidden, other.hidden)
                        && Assert.equals(this.paramLabel, other.paramLabel)
                        && Assert.equals(this.hideParamSyntax, other.hideParamSyntax)
                        && Assert.equals(this.required, other.required)
                        && Assert.equals(this.splitRegex, other.splitRegex)
                        && Arrays.equals(this.description, other.description)
                        && Assert.equals(this.descriptionKey, other.descriptionKey)
                        && this.typeInfo.equals(other.typeInfo)
                        ;
                return result;
            }
            protected int hashCodeImpl() {
                return 17
                        + 37 * Assert.hashCode(defaultValue)
                        + 37 * Assert.hashCode(arity)
                        + 37 * Assert.hashCode(hidden)
                        + 37 * Assert.hashCode(paramLabel)
                        + 37 * Assert.hashCode(hideParamSyntax)
                        + 37 * Assert.hashCode(required)
                        + 37 * Assert.hashCode(splitRegex)
                        + 37 * Arrays.hashCode(description)
                        + 37 * Assert.hashCode(descriptionKey)
                        + 37 * typeInfo.hashCode()
                        ;
            }

            private static String describe(Collection<ArgSpec> args) { return describe(args, ", "); }
            private static String describe(Collection<ArgSpec> args, String separator) {
                StringBuilder sb = new StringBuilder();
                for (ArgSpec arg : args) {
                    if (sb.length() > 0) { sb.append(separator); }
                    sb.append(describe(arg, "="));
                }
                return sb.toString();
            }
            /** Returns a description of the option or positional arg, e.g. {@code -a=<a>}
             * @param separator separator between arg and arg parameter label, usually '=' */
            private static String describe(ArgSpec argSpec, String separator) {
                return describe(argSpec, separator, argSpec.paramLabel());
            }
            /** Returns a description of the option or positional arg
             * @param separator separator between arg and arg parameter value, usually '='
             * @param value the value to append after the separator*/
            private static String describe(ArgSpec argSpec, String separator, String value) {
                String prefix = (argSpec.isOption())
                        ? ((OptionSpec) argSpec).longestName()
                        : "params[" + ((PositionalParamSpec) argSpec).index() + "]";
                return argSpec.arity().min > 0 ? prefix + separator + value : prefix;
            }
            String interpolate(String value)      { return commandSpec == null ? value  : commandSpec.interpolator.interpolate(value); }
            String[] interpolate(String[] values) { return commandSpec == null ? values : commandSpec.interpolator.interpolate(values); }
            abstract static class Builder<T extends Builder<T>> {
                private Object userObject;
                private Range arity;
                private String[] description;
                private String descriptionKey;
                private boolean required;
                private boolean interactive;
                private String paramLabel;
                private boolean hideParamSyntax;
                private String splitRegex;
                private boolean hidden;
                private Class<?> type;
                private Class<?>[] auxiliaryTypes;
                private ITypeInfo typeInfo;
                private ITypeConverter<?>[] converters;
                private String defaultValue;
                private Object initialValue;
                private boolean hasInitialValue = true;
                private Help.Visibility showDefaultValue;
                private Iterable<String> completionCandidates;
                private String toString;
                private IGetter getter = new ObjectBinding();
                private ISetter setter = (ISetter) getter;
                private IScope scope = new ObjectScope(null);

                Builder() {}
                Builder(ArgSpec original) {
                    userObject = original.userObject;
                    arity = original.arity;
                    converters = original.converters;
                    defaultValue = original.defaultValue;
                    description = original.description;
                    getter = original.getter;
                    setter = original.setter;
                    hidden = original.hidden;
                    paramLabel = original.paramLabel;
                    hideParamSyntax = original.hideParamSyntax;
                    required = original.required;
                    interactive = original.interactive;
                    showDefaultValue = original.showDefaultValue;
                    completionCandidates = original.completionCandidates;
                    splitRegex = original.splitRegex;
                    toString = original.toString;
                    descriptionKey = original.descriptionKey;
                    setTypeInfo(original.typeInfo);
                }
                Builder(IAnnotatedElement source) {
                    userObject = source.userObject();
                    setTypeInfo(source.getTypeInfo());
                    toString = source.getToString();
                    getter = source.getter();
                    setter = source.setter();
                    scope = source.scope();
                    hasInitialValue = source.hasInitialValue();
                    try { initialValue = source.getter().get(); } catch (Exception ex) { initialValue = null; hasInitialValue = false; }
                }
                Builder(Option option, IAnnotatedElement source, IFactory factory) {
                    this(source);
                    arity = Range.optionArity(source);
                    required = option.required();

                    paramLabel = inferLabel(option.paramLabel(), source.getName(), source.getTypeInfo());

                    hideParamSyntax = option.hideParamSyntax();
                    interactive = option.interactive();
                    description = option.description();
                    descriptionKey = option.descriptionKey();
                    splitRegex = option.split();
                    hidden = option.hidden();
                    defaultValue = option.defaultValue();
                    showDefaultValue = option.showDefaultValue();
                    if (factory != null) {
                        converters = DefaultFactory.createConverter(factory, option.converter());
                        if (!NoCompletionCandidates.class.equals(option.completionCandidates())) {
                            completionCandidates = DefaultFactory.createCompletionCandidates(factory, option.completionCandidates());
                        }
                    }
                }
                Builder(Parameters parameters, IAnnotatedElement source, IFactory factory) {
                    this(source);
                    arity = Range.parameterArity(source);
                    required = arity.min > 0;

                    // method parameters may be positional parameters without @Parameters annotation
                    if (parameters == null) {
                        paramLabel = inferLabel(null, source.getName(), source.getTypeInfo());
                    } else {
                        paramLabel = inferLabel(parameters.paramLabel(), source.getName(), source.getTypeInfo());

                        hideParamSyntax = parameters.hideParamSyntax();
                        interactive = parameters.interactive();
                        description = parameters.description();
                        descriptionKey = parameters.descriptionKey();
                        splitRegex = parameters.split();
                        hidden = parameters.hidden();
                        defaultValue = parameters.defaultValue();
                        showDefaultValue = parameters.showDefaultValue();
                        if (factory != null) { // annotation processors will pass a null factory
                            converters = DefaultFactory.createConverter(factory, parameters.converter());
                            if (!NoCompletionCandidates.class.equals(parameters.completionCandidates())) {
                                completionCandidates = DefaultFactory.createCompletionCandidates(factory, parameters.completionCandidates());
                            }
                        }
                    }
                }
                private static String inferLabel(String label, String fieldName, ITypeInfo typeInfo) {
                    if (!empty(label)) { return label.trim(); }
                    String name = fieldName;
                    if (typeInfo.isMap()) { // #195 better param labels for map fields
                        List<ITypeInfo> aux = typeInfo.getAuxiliaryTypeInfos();
                        if (aux.size() < 2 || aux.get(0) == null || aux.get(1) == null) {
                            name = "String=String";
                        } else { name = aux.get(0).getClassSimpleName() + "=" + aux.get(1).getClassSimpleName(); }
                    }
                    return "<" + name + ">";
                }

                public    abstract ArgSpec build();
                protected abstract T self(); // subclasses must override to return "this"
                /** Returns whether this is a required option or positional parameter.
                 * @see Option#required() */
                public boolean required()      { return required; }
                /** Returns whether this option prompts the user to enter a value on the command line.
                 * @see Option#interactive() */
                public boolean interactive()   { return interactive; }

                /** Returns the description of this option, used when generating the usage documentation.
                 * @see Option#description() */
                public String[] description()  { return description; }

                /** Returns the description key of this arg spec, used to get the description from a resource bundle.
                 * @see Option#descriptionKey()
                 * @see Parameters#descriptionKey()
                 * @since 3.6 */
                public String descriptionKey()  { return descriptionKey; }

                /** Returns how many arguments this option or positional parameter requires.
                 * @see Option#arity() */
                public Range arity()           { return arity; }
    
                /** Returns the name of the option or positional parameter used in the usage help message.
                 * @see Option#paramLabel() {@link Parameters#paramLabel()} */
                public String paramLabel()     { return paramLabel; }

                /** Returns whether usage syntax decorations around the {@linkplain #paramLabel() paramLabel} should be suppressed.
                 * The default is {@code false}: by default, the paramLabel is surrounded with {@code '['} and {@code ']'} characters
                 * if the value is optional and followed by ellipses ("...") when multiple values can be specified.
                 * @since 3.6.0 */
                public boolean hideParamSyntax()     { return hideParamSyntax; }

                /** Returns auxiliary type information used when the {@link #type()} is a generic {@code Collection}, {@code Map} or an abstract class.
                 * @see Option#type() */
                public Class<?>[] auxiliaryTypes() { return auxiliaryTypes; }

                /** Returns one or more {@link CommandLine.ITypeConverter type converters} to use to convert the command line
                 * argument into a strongly typed value (or key-value pair for map fields). This is useful when a particular
                 * option or positional parameter should use a custom conversion that is different from the normal conversion for the arg spec's type.
                 * @see Option#converter() */
                public ITypeConverter<?>[] converters() { return converters; }

                /** Returns a regular expression to split option parameter values or {@code ""} if the value should not be split.
                 * @see Option#split() */
                public String splitRegex()     { return splitRegex; }

                /** Returns whether this option should be excluded from the usage message.
                 * @see Option#hidden() */
                public boolean hidden()        { return hidden; }

                /** Returns the type to convert the option or positional parameter to before {@linkplain #setValue(Object) setting} the value. */
                public Class<?> type()         { return type; }

                /** Returns the type info for this option or positional parameter.
                 * @return type information that does not require {@code Class} objects and be constructed both at runtime and compile time
                 * @since 4.0
                 */
                public ITypeInfo typeInfo()    { return typeInfo; }

                /** Returns the user object associated with this option or positional parameters.
                 * @return may return the annotated program element, or some other useful object
                 * @since 4.0 */
                public Object userObject()     { return userObject; }

                /** Returns the default value of this option or positional parameter, before splitting and type conversion.
                 * A value of {@code null} means this option or positional parameter does not have a default. */
                public String defaultValue()   { return defaultValue; }
                /** Returns the initial value this option or positional parameter. If {@link #hasInitialValue()} is true,
                 * the option will be reset to the initial value before parsing (regardless of whether a default value exists),
                 * to clear values that would otherwise remain from parsing previous input. */
                public Object initialValue()     { return initialValue; }
                /** Determines whether the option or positional parameter will be reset to the {@link #initialValue()}
                 * before parsing new input.*/
                public boolean hasInitialValue() { return hasInitialValue; }

                /** Returns whether this option or positional parameter's default value should be shown in the usage help. */
                public Help.Visibility showDefaultValue() { return showDefaultValue; }

                /** Returns the completion candidates for this option or positional parameter, or {@code null}.
                 * @since 3.2 */
                public Iterable<String> completionCandidates() { return completionCandidates; }

                /** Returns the {@link IGetter} that is responsible for supplying the value of this argument. */
                public IGetter getter()        { return getter; }
                /** Returns the {@link ISetter} that is responsible for modifying the value of this argument. */
                public ISetter setter()        { return setter; }
                /** Returns the {@link IScope} that determines where the setter sets the value (or the getter gets the value) of this argument. */
                public IScope scope()          { return scope; }

                public String toString() { return toString; }

                /** Sets whether this is a required option or positional parameter, and returns this builder. */
                public T required(boolean required)          { this.required = required; return self(); }

                /** Sets whether this option prompts the user to enter a value on the command line, and returns this builder. */
                public T interactive(boolean interactive)    { this.interactive = interactive; return self(); }

                /** Sets the description of this option, used when generating the usage documentation, and returns this builder.
                 * @see Option#description() */
                public T description(String... description)  { this.description = Assert.notNull(description, "description").clone(); return self(); }

                /** Sets the description key that is used to look up the description in a resource bundle, and returns this builder.
                 * @see Option#descriptionKey()
                 * @see Parameters#descriptionKey()
                 * @since 3.6 */
                public T descriptionKey(String descriptionKey) { this.descriptionKey = descriptionKey; return self(); }

                /** Sets how many arguments this option or positional parameter requires, and returns this builder. */
                public T arity(String range)                 { return arity(Range.valueOf(range)); }
    
                /** Sets how many arguments this option or positional parameter requires, and returns this builder. */
                public T arity(Range arity)                  { this.arity = Assert.notNull(arity, "arity"); return self(); }
    
                /** Sets the name of the option or positional parameter used in the usage help message, and returns this builder. */
                public T paramLabel(String paramLabel)       { this.paramLabel = Assert.notNull(paramLabel, "paramLabel"); return self(); }

                /** Sets whether usage syntax decorations around the {@linkplain #paramLabel() paramLabel} should be suppressed.
                 * The default is {@code false}: by default, the paramLabel is surrounded with {@code '['} and {@code ']'} characters
                 * if the value is optional and followed by ellipses ("...") when multiple values can be specified.
                 * @since 3.6.0 */
                public T hideParamSyntax(boolean hideParamSyntax) { this.hideParamSyntax = hideParamSyntax; return self(); }
    
                /** Sets auxiliary type information, and returns this builder.
                 * @param types  the element type(s) when the {@link #type()} is a generic {@code Collection} or a {@code Map};
                 * or the concrete type when the {@link #type()} is an abstract class. */
                public T auxiliaryTypes(Class<?>... types)   { this.auxiliaryTypes = Assert.notNull(types, "types").clone(); return self(); }
    
                /** Sets option/positional param-specific converter (or converters for Maps), and returns this builder. */
                public T converters(ITypeConverter<?>... cs) { this.converters = Assert.notNull(cs, "type converters").clone(); return self(); }
    
                /** Sets a regular expression to split option parameter values or {@code ""} if the value should not be split, and returns this builder. */
                public T splitRegex(String splitRegex)       { this.splitRegex = Assert.notNull(splitRegex, "splitRegex"); return self(); }
    
                /** Sets whether this option or positional parameter's default value should be shown in the usage help, and returns this builder. */
                public T showDefaultValue(Help.Visibility visibility) { showDefaultValue = Assert.notNull(visibility, "visibility"); return self(); }

                /** Sets the completion candidates for this option or positional parameter, and returns this builder.
                 * @since 3.2 */
                public T completionCandidates(Iterable<String> completionCandidates) { this.completionCandidates = completionCandidates; return self(); }

                /** Sets whether this option should be excluded from the usage message, and returns this builder. */
                public T hidden(boolean hidden)              { this.hidden = hidden; return self(); }
    
                /** Sets the type to convert the option or positional parameter to before {@linkplain #setValue(Object) setting} the value, and returns this builder.
                 * @param propertyType the type of this option or parameter. For multi-value options and positional parameters this can be an array, or a (sub-type of) Collection or Map. */
                public T type(Class<?> propertyType)         { this.type = Assert.notNull(propertyType, "type"); return self(); }

                /** Sets the type info for this option or positional parameter, and returns this builder.
                 * @param typeInfo type information that does not require {@code Class} objects and be constructed both at runtime and compile time
                 * @since 4.0 */
                public T typeInfo(ITypeInfo typeInfo) {
                    setTypeInfo(Assert.notNull(typeInfo, "typeInfo"));
                    return self();
                }
                private void setTypeInfo(ITypeInfo newValue) {
                    this.typeInfo = newValue;
                    if (typeInfo != null) {
                        type = typeInfo.getType();
                        auxiliaryTypes = typeInfo.getAuxiliaryTypes();
                    }
                }

                /** Sets the user object associated with this option or positional parameters, and returns this builder.
                 * @param userObject may be the annotated program element, or some other useful object
                 * @since 4.0 */
                public T userObject(Object userObject)     { this.userObject = Assert.notNull(userObject, "userObject"); return self(); }

                /** Sets the default value of this option or positional parameter to the specified value, and returns this builder.
                 * Before parsing the command line, the result of {@linkplain #splitRegex() splitting} and {@linkplain #converters() type converting}
                 * this default value is applied to the option or positional parameter. A value of {@code null} or {@code "__no_default_value__"} means no default. */
                public T defaultValue(String defaultValue)   { this.defaultValue = defaultValue; return self(); }

                /** Sets the initial value of this option or positional parameter to the specified value, and returns this builder.
                 * If {@link #hasInitialValue()} is true, the option will be reset to the initial value before parsing (regardless
                 * of whether a default value exists), to clear values that would otherwise remain from parsing previous input. */
                public T initialValue(Object initialValue)   { this.initialValue = initialValue; return self(); }

                /** Determines whether the option or positional parameter will be reset to the {@link #initialValue()}
                 * before parsing new input.*/
                public T hasInitialValue(boolean hasInitialValue)   { this.hasInitialValue = hasInitialValue; return self(); }

                /** Sets the {@link IGetter} that is responsible for getting the value of this argument, and returns this builder. */
                public T getter(IGetter getter)              { this.getter = getter; return self(); }
                /** Sets the {@link ISetter} that is responsible for modifying the value of this argument, and returns this builder. */
                public T setter(ISetter setter)              { this.setter = setter; return self(); }
                /** Sets the {@link IScope} that targets where the setter sets the value, and returns this builder. */
                public T scope(IScope scope)                 { this.scope = scope; return self(); }

                /** Sets the string respresentation of this option or positional parameter to the specified value, and returns this builder. */
                public T withToString(String toString)       { this.toString = toString; return self(); }
            }
        }
        /** The {@code OptionSpec} class models aspects of a <em>named option</em> of a {@linkplain CommandSpec command}, including whether
         * it is required or optional, the option parameters supported (or required) by the option,
         * and attributes for the usage help message describing the option.
         * <p>
         * An option has one or more names. The option is matched when the parser encounters one of the option names in the command line arguments.
         * Depending on the option's {@link #arity() arity},
         * the parser may expect it to have option parameters. The parser will call {@link #setValue(Object) setValue} on
         * the matched option for each of the option parameters encountered.
         * </p><p>
         * For multi-value options, the {@code type} may be an array, a {@code Collection} or a {@code Map}. In this case
         * the parser will get the data structure by calling {@link #getValue() getValue} and modify the contents of this data structure.
         * (In the case of arrays, the array is replaced with a new instance with additional elements.)
         * </p><p>
         * Before calling the setter, picocli converts the option parameter value from a String to the option parameter's type.
         * </p>
         * <ul>
         *   <li>If a option-specific {@link #converters() converter} is configured, this will be used for type conversion.
         *   If the option's type is a {@code Map}, the map may have different types for its keys and its values, so
         *   {@link #converters() converters} should provide two converters: one for the map keys and one for the map values.</li>
         *   <li>Otherwise, the option's {@link #type() type} is used to look up a converter in the list of
         *   {@linkplain CommandLine#registerConverter(Class, ITypeConverter) registered converters}.
         *   For multi-value options,
         *   the {@code type} may be an array, or a {@code Collection} or a {@code Map}. In that case the elements are converted
         *   based on the option's {@link #auxiliaryTypes() auxiliaryTypes}. The auxiliaryType is used to look up
         *   the converter(s) to use to convert the individual parameter values.
         *   Maps may have different types for its keys and its values, so {@link #auxiliaryTypes() auxiliaryTypes}
         *   should provide two types: one for the map keys and one for the map values.</li>
         * </ul>
         * <p>
         * {@code OptionSpec} objects are used by the picocli command line interpreter and help message generator.
         * Picocli can construct an {@code OptionSpec} automatically from fields and methods with {@link Option @Option}
         * annotations. Alternatively an {@code OptionSpec} can be constructed programmatically.
         * </p><p>
         * When an {@code OptionSpec} is created from an {@link Option @Option} -annotated field or method, it is "bound"
         * to that field or method: this field is set (or the method is invoked) when the option is matched and
         * {@link #setValue(Object) setValue} is called.
         * Programmatically constructed {@code OptionSpec} instances will remember the value passed to the
         * {@link #setValue(Object) setValue} method so it can be retrieved with the {@link #getValue() getValue} method.
         * This behaviour can be customized by installing a custom {@link IGetter} and {@link ISetter} on the {@code OptionSpec}.
         * </p>
         * @since 3.0 */
        public static class OptionSpec extends ArgSpec implements IOrdered {
            static final int DEFAULT_ORDER = -1;
            private String[] names;
            private boolean help;
            private boolean usageHelp;
            private boolean versionHelp;
            private int order;

            public static OptionSpec.Builder builder(String name, String... names) {
                String[] copy = new String[Assert.notNull(names, "names").length + 1];
                copy[0] = Assert.notNull(name, "name");
                System.arraycopy(names, 0, copy, 1, names.length);
                return new Builder(copy);
            }
            public static OptionSpec.Builder builder(String[] names) { return new Builder(names); }
            public static OptionSpec.Builder builder(IAnnotatedElement source, IFactory factory) { return new Builder(source, factory); }

            /** Ensures all attributes of this {@code OptionSpec} have a valid value; throws an {@link InitializationException} if this cannot be achieved. */
            private OptionSpec(Builder builder) {
                super(builder);
                if (builder.names == null) {
                    throw new InitializationException("OptionSpec names cannot be null. Specify at least one option name.");
                }
                names = builder.names.clone();
                help = builder.help;
                usageHelp = builder.usageHelp;
                versionHelp = builder.versionHelp;
                order = builder.order;

                if (names.length == 0 || Arrays.asList(names).contains("")) {
                    throw new InitializationException("Invalid names: " + Arrays.toString(names));
                }
                if (toString() == null) { toString = "option " + longestName(); }

//                if (arity().max == 0 && !(isBoolean(type()) || (isMultiValue() && isBoolean(auxiliaryTypes()[0])))) {
//                    throw new InitializationException("Option " + longestName() + " is not a boolean so should not be defined with arity=" + arity());
//                }
            }
    
            /** Returns a new Builder initialized with the attributes from this {@code OptionSpec}. Calling {@code build} immediately will return a copy of this {@code OptionSpec}.
             * @return a builder that can create a copy of this spec
             */
            public Builder toBuilder()    { return new Builder(this); }
            @Override public boolean isOption()     { return true; }
            @Override public boolean isPositional() { return false; }

            protected boolean internalShowDefaultValue(boolean usageMessageShowDefaults) {
                return super.internalShowDefaultValue(usageMessageShowDefaults) && !help() && !versionHelp() && !usageHelp();
            }

            /** Returns the additional lookup keys for finding description lines in the resource bundle for this option.
             * @return option names (after variable interpolation), without leading hyphens, slashes and other non-Java identifier characters.
             * @since 4.0 */
            @Override protected Collection<String> getAdditionalDescriptionKeys() {
                Set<String> result = new LinkedHashSet<String>();
                for (String name : names()) { result.add(CommandSpec.stripPrefix(name)); }
                return result;
            }

            /** Returns one or more option names. The returned array will contain at least one option name.
             * @see Option#names() */
            public String[] names() { return interpolate(names.clone()); }

            /** Returns the longest {@linkplain #names() option name}. */
            public String longestName() { return Help.ShortestFirst.longestFirst(names())[0]; }

            /** Returns the shortest {@linkplain #names() option name}.
             * @since 3.8 */
            public String shortestName() { return Help.ShortestFirst.sort(names())[0]; }

            /** Returns the position in the options list in the usage help message at which this option should be shown.
             * Options with a lower number are shown before options with a higher number.
             * This attribute is only honored if {@link UsageMessageSpec#sortOptions()} is {@code false} for this command.
             * @see Option#order()
             * @since 3.9 */
            public int order() { return this.order; }

            /** Returns whether this option disables validation of the other arguments.
             * @see Option#help()
             * @deprecated Use {@link #usageHelp()} and {@link #versionHelp()} instead. */
            @Deprecated public boolean help() { return help; }
    
            /** Returns whether this option allows the user to request usage help.
             * @see Option#usageHelp()  */
            public boolean usageHelp()    { return usageHelp; }
    
            /** Returns whether this option allows the user to request version information.
             * @see Option#versionHelp()  */
            public boolean versionHelp()  { return versionHelp; }
            public boolean equals(Object obj) {
                if (obj == this) { return true; }
                if (!(obj instanceof OptionSpec)) { return false; }
                OptionSpec other = (OptionSpec) obj;
                boolean result = super.equalsImpl(other)
                        && help == other.help
                        && usageHelp == other.usageHelp
                        && versionHelp == other.versionHelp
                        && order == other.order
                        && new HashSet<String>(Arrays.asList(names)).equals(new HashSet<String>(Arrays.asList(other.names)));
                return result;
            }
            public int hashCode() {
                return super.hashCodeImpl()
                        + 37 * Assert.hashCode(help)
                        + 37 * Assert.hashCode(usageHelp)
                        + 37 * Assert.hashCode(versionHelp)
                        + 37 * Arrays.hashCode(names)
                        + 37 * order;
            }
    
            /** Builder responsible for creating valid {@code OptionSpec} objects.
             * @since 3.0
             */
            public static class Builder extends ArgSpec.Builder<Builder> {
                private String[] names;
                private boolean help;
                private boolean usageHelp;
                private boolean versionHelp;
                private int order = DEFAULT_ORDER;

                private Builder(String[] names) { this.names = names; }
                private Builder(OptionSpec original) {
                    super(original);
                    names = original.names;
                    help = original.help;
                    usageHelp = original.usageHelp;
                    versionHelp = original.versionHelp;
                    order = original.order;
                }
                private Builder(IAnnotatedElement member, IFactory factory) {
                    super(member.getAnnotation(Option.class), member, factory);
                    Option option = member.getAnnotation(Option.class);
                    names = option.names();
                    help = option.help();
                    usageHelp = option.usageHelp();
                    versionHelp = option.versionHelp();
                    order = option.order();
                }

                /** Returns a valid {@code OptionSpec} instance. */
                @Override public OptionSpec build() { return new OptionSpec(this); }
                /** Returns this builder. */
                @Override protected Builder self() { return this; }

                /** Returns one or more option names. At least one option name is required.
                 * @see Option#names() */
                public String[] names()       { return names; }

                /** Returns whether this option disables validation of the other arguments.
                 * @see Option#help()
                 * @deprecated Use {@link #usageHelp()} and {@link #versionHelp()} instead. */
                @Deprecated public boolean help() { return help; }

                /** Returns whether this option allows the user to request usage help.
                 * @see Option#usageHelp()  */
                public boolean usageHelp()    { return usageHelp; }

                /** Returns whether this option allows the user to request version information.
                 * @see Option#versionHelp()  */
                public boolean versionHelp()  { return versionHelp; }

                /** Returns the position in the options list in the usage help message at which this option should be shown.
                 * Options with a lower number are shown before options with a higher number.
                 * This attribute is only honored if {@link UsageMessageSpec#sortOptions()} is {@code false} for this command.
                 * @see Option#order()
                 * @since 3.9 */
                public int order()  { return order; }

                /** Replaces the option names with the specified values. At least one option name is required, and returns this builder.
                 * @return this builder instance to provide a fluent interface */
                public Builder names(String... names)           { this.names = Assert.notNull(names, "names").clone(); return self(); }
    
                /** Sets whether this option disables validation of the other arguments, and returns this builder. */
                public Builder help(boolean help)               { this.help = help; return self(); }
    
                /** Sets whether this option allows the user to request usage help, and returns this builder. */
                public Builder usageHelp(boolean usageHelp)     { this.usageHelp = usageHelp; return self(); }
    
                /** Sets whether this option allows the user to request version information, and returns this builder.*/
                public Builder versionHelp(boolean versionHelp) { this.versionHelp = versionHelp; return self(); }

                /** Sets the position in the options list in the usage help message at which this option should be shown, and returns this builder.
                 * @since 3.9 */
                public Builder order(int order) { this.order = order; return self(); }
            }
        }
        /** The {@code PositionalParamSpec} class models aspects of a <em>positional parameter</em> of a {@linkplain CommandSpec command}, including whether
         * it is required or optional, and attributes for the usage help message describing the positional parameter.
         * <p>
         * Positional parameters have an {@link #index() index} (or a range of indices). A positional parameter is matched when the parser
         * encounters a command line argument at that index. Named options and their parameters do not change the index counter,
         * so the command line can contain a mixture of positional parameters and named options.
         * </p><p>
         * Depending on the positional parameter's {@link #arity() arity}, the parser may consume multiple command line
         * arguments starting from the current index. The parser will call {@link #setValue(Object) setValue} on
         * the {@code PositionalParamSpec} for each of the parameters encountered.
         * For multi-value positional parameters, the {@code type} may be an array, a {@code Collection} or a {@code Map}. In this case
         * the parser will get the data structure by calling {@link #getValue() getValue} and modify the contents of this data structure.
         * (In the case of arrays, the array is replaced with a new instance with additional elements.)
         * </p><p>
         * Before calling the setter, picocli converts the positional parameter value from a String to the parameter's type.
         * </p>
         * <ul>
         *   <li>If a positional parameter-specific {@link #converters() converter} is configured, this will be used for type conversion.
         *   If the positional parameter's type is a {@code Map}, the map may have different types for its keys and its values, so
         *   {@link #converters() converters} should provide two converters: one for the map keys and one for the map values.</li>
         *   <li>Otherwise, the positional parameter's {@link #type() type} is used to look up a converter in the list of
         *   {@linkplain CommandLine#registerConverter(Class, ITypeConverter) registered converters}. For multi-value positional parameters,
         *   the {@code type} may be an array, or a {@code Collection} or a {@code Map}. In that case the elements are converted
         *   based on the positional parameter's {@link #auxiliaryTypes() auxiliaryTypes}. The auxiliaryType is used to look up
         *   the converter(s) to use to convert the individual parameter values.
         *   Maps may have different types for its keys and its values, so {@link #auxiliaryTypes() auxiliaryTypes}
         *   should provide two types: one for the map keys and one for the map values.</li>
         * </ul>
         * <p>
         * {@code PositionalParamSpec} objects are used by the picocli command line interpreter and help message generator.
         * Picocli can construct a {@code PositionalParamSpec} automatically from fields and methods with {@link Parameters @Parameters}
         * annotations. Alternatively a {@code PositionalParamSpec} can be constructed programmatically.
         * </p><p>
         * When a {@code PositionalParamSpec} is created from a {@link Parameters @Parameters} -annotated field or method,
         * it is "bound" to that field or method: this field is set (or the method is invoked) when the position is matched
         * and {@link #setValue(Object) setValue} is called.
         * Programmatically constructed {@code PositionalParamSpec} instances will remember the value passed to the
         * {@link #setValue(Object) setValue} method so it can be retrieved with the {@link #getValue() getValue} method.
         * This behaviour can be customized by installing a custom {@link IGetter} and {@link ISetter} on the {@code PositionalParamSpec}.
         * </p>
         * @since 3.0 */
        public static class PositionalParamSpec extends ArgSpec {
            private Range index;
            private Range capacity;
            private Range builderCapacity;

            /** Ensures all attributes of this {@code PositionalParamSpec} have a valid value; throws an {@link InitializationException} if this cannot be achieved. */
            private PositionalParamSpec(Builder builder) {
                super(builder);
                index = builder.index == null ? Range.valueOf("*") : builder.index;
                builderCapacity = builder.capacity;
                initCapacity();
                if (toString == null) { toString = "positional parameter[" + index() + "]"; }
            }
            private void initCapacity() {
                capacity = builderCapacity == null ? Range.parameterCapacity(arity(), index) : builderCapacity;
            }
            public static Builder builder() { return new Builder(); }
            public static Builder builder(IAnnotatedElement source, IFactory factory) { return new Builder(source, factory); }
            /** Returns a new Builder initialized with the attributes from this {@code PositionalParamSpec}. Calling {@code build} immediately will return a copy of this {@code PositionalParamSpec}.
             * @return a builder that can create a copy of this spec
             */
            public Builder toBuilder()    { return new Builder(this); }
            @Override public boolean isOption()     { return false; }
            @Override public boolean isPositional() { return true; }

            /** Returns the additional lookup keys for finding description lines in the resource bundle for this positional parameter.
             * @return a collection with the following single value: {@code paramLabel() + "[" + index() + "]"}.
             * @since 4.0 */
            @Override protected Collection<String> getAdditionalDescriptionKeys() {
                return Arrays.asList(paramLabel() + "[" + index() + "]");
            }

            /** Returns an index or range specifying which of the command line arguments should be assigned to this positional parameter.
             * @see Parameters#index() */
            public Range index()            { return index; }
            private Range capacity()        { return capacity; }

            public int hashCode() {
                return super.hashCodeImpl()
                        + 37 * Assert.hashCode(capacity)
                        + 37 * Assert.hashCode(index);
            }
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (!(obj instanceof PositionalParamSpec)) {
                    return false;
                }
                PositionalParamSpec other = (PositionalParamSpec) obj;
                return super.equalsImpl(other)
                        && Assert.equals(this.capacity, other.capacity)
                        && Assert.equals(this.index, other.index);
            }
    
            /** Builder responsible for creating valid {@code PositionalParamSpec} objects.
             * @since 3.0
             */
            public static class Builder extends ArgSpec.Builder<Builder> {
                private Range capacity;
                private Range index;
                private Builder() {}
                private Builder(PositionalParamSpec original) {
                    super(original);
                    index = original.index;
                    capacity = original.capacity;
                }
                private Builder(IAnnotatedElement member, IFactory factory) {
                    super(member.getAnnotation(Parameters.class), member, factory);
                    index = Range.parameterIndex(member);
                    capacity = Range.parameterCapacity(member);
                }
                /** Returns a valid {@code PositionalParamSpec} instance. */
                @Override public PositionalParamSpec build() { return new PositionalParamSpec(this); }
                /** Returns this builder. */
                @Override protected Builder self()  { return this; }

                /** Returns an index or range specifying which of the command line arguments should be assigned to this positional parameter.
                 * @see Parameters#index() */
                public Range index()            { return index; }

                /** Sets the index or range specifying which of the command line arguments should be assigned to this positional parameter, and returns this builder. */
                public Builder index(String range)  { return index(Range.valueOf(range)); }
    
                /** Sets the index or range specifying which of the command line arguments should be assigned to this positional parameter, and returns this builder. */
                public Builder index(Range index)   { this.index = index; return self(); }

                Range capacity()                   { return capacity; }
                Builder capacity(Range capacity)   { this.capacity = capacity; return self(); }
            }
        }

        /** Interface for sorting {@link OptionSpec options} and {@link ArgGroupSpec groups} together.
         * @since 4.0 */
        public interface IOrdered {
            /** Returns the position in the options list in the usage help message at which this element should be shown.
             * Elements with a lower number are shown before elements with a higher number.
             * This attribute is only honored if {@link UsageMessageSpec#sortOptions()} is {@code false} for this command. */
            int order();
        }

        /** The {@code ArgGroupSpec} class models a {@link ArgGroup group} of arguments (options, positional parameters or a mixture of the two).
         * @see ArgGroup
         * @since 4.0 */
        public static class ArgGroupSpec implements IOrdered {
            static final int DEFAULT_ORDER = -1;
            private static final String NO_HEADING = "__no_heading__";
            private static final String NO_HEADING_KEY = "__no_heading_key__";
            private final String heading;
            private final String headingKey;
            private final boolean exclusive;
            private final Range multiplicity;
            private final boolean validate;
            private final int order;
            private final IGetter getter;
            private final ISetter setter;
            private final IScope scope;
            private final ITypeInfo typeInfo;
            private final List<ArgGroupSpec> subgroups;
            private final Set<ArgSpec> args;
            private Messages messages;
            private ArgGroupSpec parentGroup;
            private String id = "1";

            ArgGroupSpec(ArgGroupSpec.Builder builder) {
                heading          = NO_HEADING    .equals(builder.heading)    ? null : builder.heading;
                headingKey       = NO_HEADING_KEY.equals(builder.headingKey) ? null : builder.headingKey;
                exclusive        = builder.exclusive;
                multiplicity     = builder.multiplicity;
                validate         = builder.validate;
                order            = builder.order;
                typeInfo         = builder.typeInfo;
                getter           = builder.getter;
                setter           = builder.setter;
                scope            = builder.scope;

                args      = Collections.unmodifiableSet(new LinkedHashSet<ArgSpec>(builder.args()));
                subgroups = Collections.unmodifiableList(new ArrayList<ArgGroupSpec>(builder.subgroups()));
                if (args.isEmpty() && subgroups.isEmpty()) { throw new InitializationException("ArgGroup has no options or positional parameters, and no subgroups"); }

                int i = 1;
                for (ArgGroupSpec sub : subgroups) { sub.parentGroup = this; sub.id = id + "." + i++; }
                for (ArgSpec arg : args)           { arg.group = this; }
            }

            /** Returns a new {@link Builder}.
             * @return a new ArgGroupSpec.Builder instance */
            public static Builder builder() { return new Builder(); }

            /** Returns a new {@link Builder} associated with the specified annotated element.
             * @param annotatedElement the annotated element containing {@code @Option} and {@code @Parameters}
             * @return a new ArgGroupSpec.Builder instance */
            public static Builder builder(IAnnotatedElement annotatedElement) { return new Builder(Assert.notNull(annotatedElement, "annotatedElement")); }

            /** Returns whether this is a mutually exclusive group; {@code true} by default.
             * If {@code false}, this is a co-occurring group. Ignored if {@link #validate()} is {@code false}.
             * @see ArgGroup#exclusive() */
            public boolean exclusive() { return exclusive; }

            /** Returns the multiplicity of this group: how many occurrences it may have on the command line; {@code "0..1"} (optional) by default.
             * A group can be made required by specifying a multiplicity of {@code "1"}. For a group of mutually exclusive arguments,
             * being required means that one of the arguments in the group must appear on the command line, or a MissingParameterException is thrown.
             * For a group of co-occurring arguments, being required means that all arguments in the group must appear on the command line.
             * Ignored if {@link #validate()} is {@code false}.
             * @see ArgGroup#multiplicity() */
            public Range multiplicity() { return multiplicity; }

            /** Returns whether picocli should validate the rules of this group:
             * for a mutually exclusive group this means that no more than one arguments in the group is specified on the command line;
             * for a co-ocurring group this means that all arguments in the group are specified on the command line.
             * {@code true} by default.
             * @see ArgGroup#validate() */
            public boolean validate() { return validate; }

            /** Returns the position in the options list in the usage help message at which this group should be shown.
             * Options with a lower number are shown before options with a higher number.
             * This attribute is only honored if {@link UsageMessageSpec#sortOptions()} is {@code false} for this command. */
            public int order() { return this.order; }

            /** Returns the heading of this group (may be {@code null}), used when generating the usage documentation.
             * @see ArgGroup#heading() */
            public String heading()  {
                if (messages() == null) { return heading; }
                String newValue = messages().getString(headingKey(), null);
                if (newValue != null) { return newValue; }
                return heading;
            }

            /** Returns the heading key of this group (may be {@code null}), used to get the heading from a resource bundle.
             * @see ArgGroup#headingKey()  */
            public String headingKey() { return headingKey; }

            /**
             * Returns the parent group that this group is part of, or {@code null} if this group is not part of a composite.
             */
            public ArgGroupSpec parentGroup() { return parentGroup; }

            /** Return the subgroups that this group is composed of; may be empty but not {@code null}.
             * @return immutable list of subgroups that this group is composed of. */
            public List<ArgGroupSpec> subgroups() { return subgroups; }

            /**
             * Returns {@code true} if this group is a subgroup (or a nested sub-subgroup, to any level of depth)
             * of the specified group, {@code false} otherwise.
             * @param group the group to check if it contains this group
             * @return {@code true} if this group is a subgroup or a nested sub-subgroup of the specified group
             */
            public boolean isSubgroupOf(ArgGroupSpec group) {
                for (ArgGroupSpec sub : group.subgroups) {
                    if (this == sub) { return true; }
                    if (isSubgroupOf(sub)) { return true; }
                }
                return false;
            }
            /** Returns the type info for the annotated program element associated with this group.
             * @return type information that does not require {@code Class} objects and be constructed both at runtime and compile time
             */
            public ITypeInfo typeInfo()    { return typeInfo; }

            /** Returns the {@link IGetter} that is responsible for supplying the value of the annotated program element associated with this group. */
            public IGetter getter()        { return getter; }
            /** Returns the {@link ISetter} that is responsible for modifying the value of the annotated program element associated with this group. */
            public ISetter setter()        { return setter; }
            /** Returns the {@link IScope} that determines where the setter sets the value (or the getter gets the value) of the annotated program element associated with this group. */
            public IScope scope()          { return scope; }

            Object userObject() { try { return getter.get(); } catch (Exception ex) { return ex.toString(); } }
            String id() { return id; }

            /** Returns the options and positional parameters in this group; may be empty but not {@code null}. */
            public Set<ArgSpec> args() { return args; }
            /** Returns the required options and positional parameters in this group; may be empty but not {@code null}. */
            public Set<ArgSpec> requiredArgs() {
                Set<ArgSpec> result = new LinkedHashSet<ArgSpec>(args);
                for (Iterator<ArgSpec> iter = result.iterator(); iter.hasNext(); ) {
                    if (!iter.next().required()) { iter.remove(); }
                }
                return Collections.unmodifiableSet(result);
            }

            /** Returns the list of positional parameters configured for this group.
             * @return an immutable list of positional parameters in this group. */
            public List<PositionalParamSpec> positionalParameters() {
                List<PositionalParamSpec> result = new ArrayList<PositionalParamSpec>();
                for (ArgSpec arg : args()) { if (arg instanceof PositionalParamSpec) { result.add((PositionalParamSpec) arg); } }
                return Collections.unmodifiableList(result);
            }
            /** Returns the list of options configured for this group.
             * @return an immutable list of options in this group. */
            public List<OptionSpec> options() {
                List<OptionSpec> result = new ArrayList<OptionSpec>();
                for (ArgSpec arg : args()) { if (arg instanceof OptionSpec) { result.add((OptionSpec) arg); } }
                return Collections.unmodifiableList(result);
            }

            public String synopsis() {
                return synopsisText(new Help.ColorScheme.Builder(Help.Ansi.OFF).build()).toString();
            }

            public Text synopsisText(Help.ColorScheme colorScheme) {
                String infix = exclusive() ? " | " : " ";
                Text synopsis = colorScheme.ansi().new Text(0);
                for (ArgSpec arg : args()) {
                    if (synopsis.length > 0) { synopsis = synopsis.concat(infix); }
                    if (arg instanceof OptionSpec) {
                        synopsis = concatOptionText(synopsis, colorScheme, (OptionSpec) arg);
                    } else {
                        synopsis = concatPositionalText(synopsis, colorScheme, (PositionalParamSpec) arg);
                    }
                }
                for (ArgGroupSpec subgroup : subgroups()) {
                    if (synopsis.length > 0) { synopsis = synopsis.concat(infix); }
                    synopsis = synopsis.concat(subgroup.synopsisText(colorScheme));
                }
                String prefix = multiplicity().min > 0 ? "(" : "[";
                String postfix = multiplicity().min > 0 ? ")" : "]";
                Text result = colorScheme.ansi().text(prefix).concat(synopsis).concat(postfix);
                int i = 1;
                for (; i < multiplicity.min; i++) {
                    result = result.concat(" (").concat(synopsis).concat(")");
                }
                if (multiplicity().isVariable) {
                    result = result.concat("...");
                } else {
                    for (; i < multiplicity.max; i++) {
                        result = result.concat(" [").concat(synopsis).concat("]");
                    }
                }
                return result;
            }

            private Text concatOptionText(Text text, Help.ColorScheme colorScheme, OptionSpec option) {
                if (!option.hidden()) {
                    Text name = colorScheme.optionText(option.shortestName());
                    Text param = createLabelRenderer(option.commandSpec).renderParameterLabel(option, colorScheme.ansi(), colorScheme.optionParamStyles);
                    text = text.concat(open(option)).concat(name).concat(param).concat(close(option));
                    if (option.isMultiValue()) { // e.g., -x=VAL [-x=VAL]...
                        text = text.concat(" [").concat(name).concat(param).concat("]...");
                    }
                }
                return text;
            }

            private Text concatPositionalText(Text text, Help.ColorScheme colorScheme, PositionalParamSpec positionalParam) {
                if (!positionalParam.hidden()) {
                    Text label = createLabelRenderer(positionalParam.commandSpec).renderParameterLabel(positionalParam, colorScheme.ansi(), colorScheme.parameterStyles);
                    text = text.concat(open(positionalParam)).concat(label).concat(close(positionalParam));
                }
                return text;
            }
            private String open(ArgSpec argSpec)  { return argSpec.required() ? "" : "["; }
            private String close(ArgSpec argSpec) { return argSpec.required() ? "" : "]"; }

            public Help.IParamLabelRenderer createLabelRenderer(CommandSpec commandSpec) {
                return new Help.DefaultParamLabelRenderer(commandSpec == null ? CommandSpec.create() : commandSpec);
            }
            /** Returns the Messages for this argument group specification, or {@code null}. */
            public Messages messages() { return messages; }
            /** Sets the Messages for this ArgGroupSpec, and returns this ArgGroupSpec.
             * @param msgs the new Messages value, may be {@code null}
             * @see Command#resourceBundle()
             * @see #headingKey()
             */
            public ArgGroupSpec messages(Messages msgs) {
                messages = msgs;
                for (ArgGroupSpec sub : subgroups()) { sub.messages(msgs); }
                return this;
            }

            @Override public boolean equals(Object obj) {
                if (obj == this) { return true; }
                if (!(obj instanceof ArgGroupSpec)) { return false; }
                ArgGroupSpec other = (ArgGroupSpec) obj;
                return exclusive == other.exclusive
                        && Assert.equals(multiplicity, other.multiplicity)
                        && validate == other.validate
                        && order == other.order
                        && Assert.equals(heading, other.heading)
                        && Assert.equals(headingKey, other.headingKey)
                        && Assert.equals(subgroups, other.subgroups)
                        && Assert.equals(args, other.args);
            }

            @Override public int hashCode() {
                int result = 17;
                result += 37 * result + Assert.hashCode(exclusive);
                result += 37 * result + Assert.hashCode(multiplicity);
                result += 37 * result + Assert.hashCode(validate);
                result += 37 * result + order;
                result += 37 * result + Assert.hashCode(heading);
                result += 37 * result + Assert.hashCode(headingKey);
                result += 37 * result + Assert.hashCode(subgroups);
                result += 37 * result + Assert.hashCode(args);
                return result;
            }

            @Override public String toString() {
                List<String> argNames = new ArrayList<String>();
                for (ArgSpec arg : args()) {
                    if (arg instanceof OptionSpec) {
                        argNames.add(((OptionSpec) arg).shortestName());
                    } else {
                        PositionalParamSpec p = (PositionalParamSpec) arg;
                        argNames.add(p.index() + " (" + p.paramLabel() + ")");
                    }
                }
                return "ArgGroup[exclusive=" + exclusive + ", multiplicity=" + multiplicity +
                        ", validate=" + validate + ", order=" + order + ", args=[" + ArgSpec.describe(args()) +
                        "], headingKey=" + quote(headingKey) + ", heading=" + quote(heading) +
                        ", subgroups=" + subgroups + "]";
            }
            private static String quote(String s) { return s == null ? "null" : "'" + s + "'"; }

            void initUserObject(CommandLine commandLine) {
                if (commandLine == null) { new Tracer().debug("Could not create user object for %s with null CommandLine%n.", this); }
                try {
                    tryInitUserObject(commandLine);
                } catch (PicocliException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InitializationException("Could not create user object for " + this, ex);
                }
            }
            void tryInitUserObject(CommandLine commandLine) throws Exception {
                Tracer tracer = commandLine.tracer;
                if (typeInfo() != null) {
                    tracer.debug("Creating new user object of type %s for group %s%n", typeInfo().getAuxiliaryTypes()[0], synopsis());
                    Object userObject = DefaultFactory.create(commandLine.factory, typeInfo().getAuxiliaryTypes()[0]);
                    tracer.debug("Created %s, invoking setter %s with scope %s%n", userObject, setter(), scope());
                    setUserObject(userObject, commandLine.factory);
                    for (ArgSpec arg : args()) {
                        tracer.debug("Initializing %s in group %s: setting scope to user object %s and initializing initial and default values%n", ArgSpec.describe(arg, "="), synopsis(), userObject);
                        arg.scope().set(userObject); // flip the actual user object for the arg (and all other args in this group; they share the same IScope instance)
                        commandLine.interpreter.parseResultBuilder.isInitializingDefaultValues = true;
                        arg.applyInitialValue(tracer);
                        commandLine.interpreter.applyDefault(commandLine.getCommandSpec().defaultValueProvider(), arg);
                        commandLine.interpreter.parseResultBuilder.isInitializingDefaultValues = false;
                    }
                    for (ArgGroupSpec subgroup : subgroups()) {
                        tracer.debug("Setting scope for subgroup %s with setter=%s in group %s to user object %s%n", subgroup.synopsis(), subgroup.setter(), synopsis(), userObject);
                        subgroup.scope().set(userObject); // flip the actual user object for the arg (and all other args in this group; they share the same IScope instance)
                    }
                } else {
                    tracer.debug("No type information available for group %s: cannot create new user object. Scope for arg setters is not changed.%n", synopsis());
                }
                tracer.debug("Initialization complete for group %s%n", synopsis());
            }

            void setUserObject(Object userObject, IFactory factory) throws Exception {
                if (typeInfo().isCollection()) {
                    @SuppressWarnings("unchecked") Collection<Object> c = (Collection<Object>) getter().get();
                    if (c == null) {
                        @SuppressWarnings("unchecked")
                        Collection<Object> c2 = (Collection<Object>) DefaultFactory.create(factory, typeInfo.getType());
                        setter().set(c = c2);
                    }
                    (c).add(userObject);
                } else if (typeInfo().isArray()) {
                    Object old = getter().get();
                    int oldSize = old == null ? 0 : Array.getLength(old);
                    Object array = Array.newInstance(typeInfo().getAuxiliaryTypes()[0], oldSize + 1);
                    for (int i = 0; i < oldSize; i++) {
                        Array.set(array, i, Array.get(old, i));
                    }
                    Array.set(array, oldSize, userObject);
                    setter().set(array);
                } else {
                    setter().set(userObject);
                }
            }

            ParseResult.GroupValidationResult validateArgs(CommandLine commandLine, Collection<ArgSpec> matchedArgs) {
                Set<ArgSpec> intersection = new LinkedHashSet<ArgSpec>(args());
                Set<ArgSpec> missing = new LinkedHashSet<ArgSpec>(requiredArgs());
                Set<ArgSpec> found = new LinkedHashSet<ArgSpec>();
                found.addAll(matchedArgs);
                missing.removeAll(matchedArgs);
                intersection.retainAll(found);
                int presentCount = intersection.size();
                boolean haveMissing = !missing.isEmpty();
                boolean someButNotAllSpecified = haveMissing && !intersection.isEmpty();
                String exclusiveElements = ArgSpec.describe(intersection);
                String requiredElements = ArgSpec.describe(requiredArgs());
                String missingElements = ArgSpec.describe(missing);

                return validate(commandLine, presentCount, haveMissing, someButNotAllSpecified, exclusiveElements, requiredElements, missingElements);
            }

            private ParseResult.GroupValidationResult validate(CommandLine commandLine, int presentCount, boolean haveMissing, boolean someButNotAllSpecified, String exclusiveElements, String requiredElements, String missingElements) {
                if (exclusive()) {
                    if (presentCount > 1) {
                        return new ParseResult.GroupValidationResult(
                                ParseResult.GroupValidationResult.Type.FAILURE_PRESENT,
                                new MutuallyExclusiveArgsException(commandLine,
                                        "Error: " + exclusiveElements + " are mutually exclusive (specify only one)"));
                    }
                    // check that exactly one member was matched
                    if (multiplicity().min > 0 && haveMissing) {
                        return new ParseResult.GroupValidationResult(
                                ParseResult.GroupValidationResult.Type.FAILURE_ABSENT,
                                new MissingParameterException(commandLine, args(),
                                        "Error: Missing required argument (specify one of these): " + requiredElements));
                    }
                } else { // co-occurring group
                    if (someButNotAllSpecified) {
                        return new ParseResult.GroupValidationResult(
                                ParseResult.GroupValidationResult.Type.FAILURE_PARTIAL,
                                new MissingParameterException(commandLine, args(),
                                        "Error: Missing required argument(s): " + missingElements));
                    }
                    if ((multiplicity().min > 0 && haveMissing)) {
                        return new ParseResult.GroupValidationResult(
                                ParseResult.GroupValidationResult.Type.FAILURE_ABSENT,
                                new MissingParameterException(commandLine, args(),
                                        "Error: Missing required argument(s): " + missingElements));
                    }
                }
                return presentCount > 0 ? ParseResult.GroupValidationResult.SUCCESS_PRESENT : ParseResult.GroupValidationResult.SUCCESS_ABSENT;
            }

            /** Builder responsible for creating valid {@code ArgGroupSpec} objects.
             * @since 4.0 */
            public static class Builder {
                private IGetter getter;
                private ISetter setter;
                private IScope scope;
                private ITypeInfo typeInfo;
                private String heading;
                private String headingKey;
                private boolean exclusive  = true;
                private Range multiplicity = Range.valueOf("0..1");
                private boolean validate   = true;
                private int order          = DEFAULT_ORDER;
                private List<ArgSpec> args = new ArrayList<ArgSpec>();
                private List<ArgGroupSpec> subgroups = new ArrayList<ArgGroupSpec>();

                // for topological sorting; private only
                private Boolean topologicalSortDone;
                private List<Builder> compositesReferencingMe = new ArrayList<Builder>();

                Builder() { }
                Builder(IAnnotatedElement source) {
                    typeInfo = source.getTypeInfo();
                    getter = source.getter();
                    setter = source.setter();
                    scope = source.scope();
                }

                /** Updates this builder from the specified annotation values.
                 * @param group annotation values
                 * @return this builder for method chaining */
                public Builder updateArgGroupAttributes(ArgGroup group) {
                    return this
                            .heading(group.heading())
                            .headingKey(group.headingKey())
                            .exclusive(group.exclusive())
                            .multiplicity(group.multiplicity())
                            .validate(group.validate())
                            .order(group.order());
                }

                /** Returns a valid {@code ArgGroupSpec} instance. */
                public ArgGroupSpec build() { return new ArgGroupSpec(this); }

                /** Returns whether this is a mutually exclusive group; {@code true} by default.
                 * If {@code false}, this is a co-occurring group. Ignored if {@link #validate()} is {@code false}.
                 * @see ArgGroup#exclusive() */
                public boolean exclusive() { return exclusive; }
                /** Sets whether this is a mutually exclusive group; {@code true} by default.
                 * If {@code false}, this is a co-occurring group. Ignored if {@link #validate()} is {@code false}.
                 * @see ArgGroup#exclusive() */
                public Builder exclusive(boolean newValue) { exclusive = newValue; return this; }

                /** Returns the multiplicity of this group: how many occurrences it may have on the command line; {@code "0..1"} (optional) by default.
                 * A group can be made required by specifying a multiplicity of {@code "1"}. For a group of mutually exclusive arguments,
                 * being required means that one of the arguments in the group must appear on the command line, or a MissingParameterException is thrown.
                 * For a group of co-occurring arguments, being required means that all arguments in the group must appear on the command line.
                 * Ignored if {@link #validate()} is {@code false}.
                 * @see ArgGroup#multiplicity() */
                public Range multiplicity() { return multiplicity; }
                /** Sets the multiplicity of this group: how many occurrences it may have on the command line; {@code "0..1"} (optional) by default.
                 * A group can be made required by specifying a multiplicity of {@code "1"}. For a group of mutually exclusive arguments,
                 * being required means that one of the arguments in the group must appear on the command line, or a MissingParameterException is thrown.
                 * For a group of co-occurring arguments, being required means that all arguments in the group must appear on the command line.
                 * Ignored if {@link #validate()} is {@code false}.
                 * @see ArgGroup#multiplicity() */
                public Builder multiplicity(String newValue) { return multiplicity(Range.valueOf(newValue)); }
                /** Sets the multiplicity of this group: how many occurrences it may have on the command line; {@code "0..1"} (optional) by default.
                 * A group can be made required by specifying a multiplicity of {@code "1"}. For a group of mutually exclusive arguments,
                 * being required means that one of the arguments in the group must appear on the command line, or a MissingParameterException is thrown.
                 * For a group of co-occurring arguments, being required means that all arguments in the group must appear on the command line.
                 * Ignored if {@link #validate()} is {@code false}.
                 * @see ArgGroup#multiplicity() */
                public Builder multiplicity(Range newValue) { multiplicity = newValue; return this; }

                /** Returns whether picocli should validate the rules of this group:
                 * for a mutually exclusive group this means that no more than one arguments in the group is specified on the command line;
                 * for a co-ocurring group this means that all arguments in the group are specified on the command line.
                 * {@code true} by default.
                 * @see ArgGroup#validate() */
                public boolean validate() { return validate; }
                /** Sets whether picocli should validate the rules of this group:
                 * for a mutually exclusive group this means that no more than one arguments in the group is specified on the command line;
                 * for a co-ocurring group this means that all arguments in the group are specified on the command line.
                 * {@code true} by default.
                 * @see ArgGroup#validate() */
                public Builder validate(boolean newValue) { validate = newValue; return this; }

                /** Returns the position in the options list in the usage help message at which this group should be shown.
                 * Options with a lower number are shown before options with a higher number.
                 * This attribute is only honored if {@link UsageMessageSpec#sortOptions()} is {@code false} for this command.*/
                public int order() { return order; }

                /** Sets the position in the options list in the usage help message at which this group should be shown, and returns this builder. */
                public Builder order(int order) { this.order = order; return this; }

                /** Returns the heading of this group, used when generating the usage documentation.
                 * @see ArgGroup#heading() */
                public String heading() { return heading; }

                /** Sets the heading of this group (may be {@code null}), used when generating the usage documentation.
                 * @see ArgGroup#heading() */
                public Builder heading(String newValue) { this.heading = newValue; return this; }

                /** Returns the heading key of this group, used to get the heading from a resource bundle.
                 * @see ArgGroup#headingKey()  */
                public String headingKey() { return headingKey; }
                /** Sets the heading key of this group, used to get the heading from a resource bundle.
                 * @see ArgGroup#headingKey()  */
                public Builder headingKey(String newValue) { this.headingKey = newValue; return this; }

                /** Returns the type info for the annotated program element associated with this group.
                 * @return type information that does not require {@code Class} objects and be constructed both at runtime and compile time
                 */
                public ITypeInfo typeInfo()    { return typeInfo; }
                /** Sets the type info for the annotated program element associated with this group, and returns this builder.
                 * @param newValue type information that does not require {@code Class} objects and be constructed both at runtime and compile time
                 */
                public Builder typeInfo(ITypeInfo newValue) { this.typeInfo = newValue; return this; }

                /** Returns the {@link IGetter} that is responsible for supplying the value of the annotated program element associated with this group. */
                public IGetter getter()        { return getter; }
                /** Sets the {@link IGetter} that is responsible for getting the value of the annotated program element associated with this group, and returns this builder. */
                public Builder getter(IGetter getter)       { this.getter = getter; return this; }

                /** Returns the {@link ISetter} that is responsible for modifying the value of the annotated program element associated with this group. */
                public ISetter setter()        { return setter; }
                /** Sets the {@link ISetter} that is responsible for modifying the value of the annotated program element associated with this group, and returns this builder. */
                public Builder setter(ISetter setter)       { this.setter = setter; return this; }

                /** Returns the {@link IScope} that determines where the setter sets the value (or the getter gets the value) of the annotated program element associated with this group. */
                public IScope scope()          { return scope; }
                /** Sets the {@link IScope} that targets where the setter sets the value of the annotated program element associated with this group, and returns this builder. */
                public Builder scope(IScope scope)          { this.scope = scope; return this; }

                /** Adds the specified argument to the list of options and positional parameters that depend on this group. */
                public Builder addArg(ArgSpec arg) { args.add(arg); return this; }

                /** Returns the list of options and positional parameters that depend on this group.*/
                public List<ArgSpec> args() { return args; }

                /** Adds the specified group to the list of subgroups that this group is composed of. */
                public Builder addSubgroup(ArgGroupSpec group) { subgroups.add(group); return this; }

                /** Returns the list of subgroups that this group is composed of.*/
                public List<ArgGroupSpec> subgroups() { return subgroups; }
            }
        }

        /** This class allows applications to specify a custom binding that will be invoked for unmatched arguments.
         * A binding can be created with a {@code ISetter} that consumes the unmatched arguments {@code String[]}, or with a
         * {@code IGetter} that produces a {@code Collection<String>} that the unmatched arguments can be added to.
         * @since 3.0 */
        public static class UnmatchedArgsBinding {
            private final IGetter getter;
            private final ISetter setter;

            /** Creates a {@code UnmatchedArgsBinding} for a setter that consumes {@code String[]} objects.
             * @param setter consumes the String[] array with unmatched arguments. */
            public static UnmatchedArgsBinding forStringArrayConsumer(ISetter setter) { return new UnmatchedArgsBinding(null, setter); }

            /** Creates a {@code UnmatchedArgsBinding} for a getter that produces a {@code Collection<String>} that the unmatched arguments can be added to.
             * @param getter supplies a {@code Collection<String>} that the unmatched arguments can be added to. */
            public static UnmatchedArgsBinding forStringCollectionSupplier(IGetter getter) { return new UnmatchedArgsBinding(getter, null); }

            private UnmatchedArgsBinding(IGetter getter, ISetter setter) {
                if (getter == null && setter == null) { throw new IllegalArgumentException("Getter and setter cannot both be null"); }
                this.setter = setter;
                this.getter = getter;
            }
            /** Returns the getter responsible for producing a {@code Collection} that the unmatched arguments can be added to. */
            public IGetter getter() { return getter; }
            /** Returns the setter responsible for consuming the unmatched arguments. */
            public ISetter setter() { return setter; }
            void addAll(String[] unmatched) {
                if (setter != null) {
                    try {
                        setter.set(unmatched);
                    } catch (Exception ex) {
                        throw new PicocliException(String.format("Could not invoke setter (%s) with unmatched argument array '%s': %s", setter, Arrays.toString(unmatched), ex), ex);
                    }
                }
                if (getter != null) {
                    try {
                        Collection<String> collection = getter.get();
                        Assert.notNull(collection, "getter returned null Collection");
                        collection.addAll(Arrays.asList(unmatched));
                    } catch (Exception ex) {
                        throw new PicocliException(String.format("Could not add unmatched argument array '%s' to collection returned by getter (%s): %s",
                                Arrays.toString(unmatched), getter, ex), ex);
                    }
                }
            }
        }
        /** Command method parameter, similar to java.lang.reflect.Parameter (not available before Java 8).
         * @since 4.0 */
        public static class MethodParam extends AccessibleObject {
            final Method method;
            final int paramIndex;
            final String name;
            int position;

            public MethodParam(Method method, int paramIndex) {
                this.method = method;
                this.paramIndex = paramIndex;
                String tmp = "arg" + paramIndex;
                try {
                    Method getParameters = Method.class.getMethod("getParameters");
                    Object parameters = getParameters.invoke(method);
                    Object parameter = Array.get(parameters, paramIndex);
                    tmp = (String) Class.forName("java.lang.reflect.Parameter").getDeclaredMethod("getName").invoke(parameter);
                } catch (Exception ignored) {}
                this.name = tmp;
            }
            public Type getParameterizedType() { return method.getGenericParameterTypes()[paramIndex]; }
            public String getName() { return name; }
            public Class<?> getType() { return method.getParameterTypes()[paramIndex]; }
            public Method getDeclaringExecutable() { return method; }
            @Override public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                for (Annotation annotation : getDeclaredAnnotations()) {
                    if (annotationClass.isAssignableFrom(annotation.getClass())) { return annotationClass.cast(annotation); }
                }
                return null;
            }
            @Override public Annotation[] getDeclaredAnnotations() { return method.getParameterAnnotations()[paramIndex]; }
            @Override public void setAccessible(boolean flag) throws SecurityException { method.setAccessible(flag); }
            @Override public boolean isAccessible() throws SecurityException { return method.isAccessible(); }
            @Override public String toString() { return method.toString() + ":" + getName(); }
        }

        /** Encapculates type information for an option or parameter to make this information available both at runtime
         * and at compile time (when {@code Class} values are not available).
         * Most of the methods in this interface (but not all!) are safe to use by annotation processors.
         * @since 4.0
         */
        public interface ITypeInfo {
            /** Returns {@code true} if {@link #getType()} is {@code boolean} or {@code java.lang.Boolean}. */
            boolean isBoolean();
            /** Returns {@code true} if {@link #getType()} is an array, map or collection. */
            boolean isMultiValue();
            boolean isArray();
            boolean isCollection();
            boolean isMap();
            /** Returns {@code true} if {@link #getType()} is an enum. */
            boolean isEnum();
            List<String> getEnumConstantNames();
            String getClassName();
            String getClassSimpleName();
            /** Returns type information of components or elements of a {@link #isMultiValue() multivalue} type. */
            List<ITypeInfo> getAuxiliaryTypeInfos();
            /** Returns the names of the type arguments if this is a generic type. For example, returns {@code ["java.lang.String"]} if this type is {@code List<String>}. */
            List<String> getActualGenericTypeArguments();

            /** Returns the class that the option or parameter value should be converted to when matched on the command
             * line. This method is <em>not</em> safe for annotation processors to use.
             * @return the class that the option or parameter value should be converted to
             */
            Class<?> getType();
            /** Returns the component class of an array, or the parameter type of a generic Collection, or the parameter
             * types of the key and the value of a generic Map.
             * This method is <em>not</em> safe for annotation processors to use.
             * @return the component type or types of an array, Collection or Map type
             */
            Class<?>[] getAuxiliaryTypes();
        }
        static class RuntimeTypeInfo implements ITypeInfo {
            private final Class<?> type;
            private final Class<?>[] auxiliaryTypes;
            private final List<String> actualGenericTypeArguments;

            RuntimeTypeInfo(Class<?> type, Class<?>[] auxiliaryTypes, List<String> actualGenericTypeArguments) {
                this.type = Assert.notNull(type, "type");
                this.auxiliaryTypes = Assert.notNull(auxiliaryTypes, "auxiliaryTypes").clone();
                this.actualGenericTypeArguments = actualGenericTypeArguments == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(actualGenericTypeArguments));
            }

            static ITypeInfo createForAuxType(Class<?> type) {
                return create(type, new Class[0], (Type) null, Range.valueOf("1"), String.class, false);
            }
            public static ITypeInfo create(Class<?> type,
                                           Class<?>[] annotationTypes,
                                           Type genericType,
                                           Range arity,
                                           Class<?> defaultType,
                                           boolean interactive) {
                Class<?>[] auxiliaryTypes = RuntimeTypeInfo.inferTypes(type, annotationTypes, genericType);
                List<String> actualGenericTypeArguments = new ArrayList<String>();
                if (genericType instanceof ParameterizedType)  {
                    Class[] declaredTypeParameters = extractTypeParameters((ParameterizedType) genericType);
                    for (Class<?> c : declaredTypeParameters) { actualGenericTypeArguments.add(c.getName()); }
                }
                return create(type, auxiliaryTypes, actualGenericTypeArguments, arity, defaultType, interactive);
            }

            public static ITypeInfo create(Class<?> type, Class<?>[] auxiliaryTypes, List<String> actualGenericTypeArguments, Range arity, Class<?> defaultType, boolean interactive) {
                if (type == null) {
                    if (auxiliaryTypes == null || auxiliaryTypes.length == 0) {
                        if (interactive) {
                            type = char[].class;
                        } else if (arity.isVariable || arity.max > 1) {
                            type = String[].class;
                        } else if (arity.max == 1) {
                            type = String.class;
                        } else {
                            type = defaultType;
                        }
                    } else {
                        type = auxiliaryTypes[0];
                    }
                }
                if (auxiliaryTypes == null || auxiliaryTypes.length == 0) {
                    if (type.isArray()) {
                        if (interactive && type.equals(char[].class)) {
                            auxiliaryTypes = new Class<?>[]{char[].class};
                        } else {
                            auxiliaryTypes = new Class<?>[]{type.getComponentType()};
                        }
                    } else if (Collection.class.isAssignableFrom(type)) { // type is a collection but element type is unspecified
                        auxiliaryTypes = new Class<?>[] {interactive ? char[].class : String.class}; // use String elements
                    } else if (Map.class.isAssignableFrom(type)) { // type is a map but element type is unspecified
                        auxiliaryTypes = new Class<?>[] {String.class, String.class}; // use String keys and String values
                    } else {
                        auxiliaryTypes = new Class<?>[] {type};
                    }
                }
                return new RuntimeTypeInfo(type, auxiliaryTypes, actualGenericTypeArguments);
            }
            static Class<?>[] inferTypes(Class<?> propertyType, Class<?>[] annotationTypes, Type genericType) {
                if (annotationTypes != null && annotationTypes.length > 0) { return annotationTypes; }
                if (propertyType.isArray()) { return new Class<?>[] { propertyType.getComponentType() }; }
                if (CommandLine.isMultiValue(propertyType)) {
                    if (genericType instanceof ParameterizedType) {// e.g. Map<Long, ? extends Number>
                        return extractTypeParameters((ParameterizedType) genericType);
                    }
                    return new Class<?>[] {String.class, String.class}; // field is multi-value but not ParameterizedType
                }
                return new Class<?>[] {propertyType}; // not a multi-value field
            }

            static Class<?>[] extractTypeParameters(ParameterizedType genericType) {
                ParameterizedType parameterizedType = genericType;
                Type[] paramTypes = parameterizedType.getActualTypeArguments(); // e.g. ? extends Number
                Class<?>[] result = new Class<?>[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    if (paramTypes[i] instanceof Class) { result[i] = (Class<?>) paramTypes[i]; continue; } // e.g. Long
                    else if (paramTypes[i] instanceof WildcardType) { // e.g. ? extends Number
                        WildcardType wildcardType = (WildcardType) paramTypes[i];
                        Type[] lower = wildcardType.getLowerBounds(); // e.g. []
                        if (lower.length > 0 && lower[0] instanceof Class) { result[i] = (Class<?>) lower[0]; continue; }
                        Type[] upper = wildcardType.getUpperBounds(); // e.g. Number
                        if (upper.length > 0 && upper[0] instanceof Class) { result[i] = (Class<?>) upper[0]; continue; }
                    } else if (paramTypes[i] instanceof GenericArrayType) {
                        GenericArrayType gat = (GenericArrayType) paramTypes[i];
                        if (char.class.equals(gat.getGenericComponentType())) {
                            result[i] = char[].class; continue;
                        }
                    }
                    Arrays.fill(result, String.class); return result; // too convoluted generic type, giving up
                }
                return result; // we inferred all types from ParameterizedType
            }

            public boolean isBoolean()            { return auxiliaryTypes[0] == boolean.class || auxiliaryTypes[0] == Boolean.class; }
            public boolean isMultiValue()         { return CommandLine.isMultiValue(type); }
            public boolean isArray()              { return type.isArray(); }
            public boolean isCollection()         { return Collection.class.isAssignableFrom(type); }
            public boolean isMap()                { return Map.class.isAssignableFrom(type); }
            public boolean isEnum()               { return auxiliaryTypes[0].isEnum(); }
            public String getClassName()          { return type.getName(); }
            public String getClassSimpleName()    { return type.getSimpleName(); }
            public Class<?> getType()             { return type; }
            public Class<?>[] getAuxiliaryTypes() { return auxiliaryTypes; }
            public List<String> getActualGenericTypeArguments() { return actualGenericTypeArguments; }

            public List<ITypeInfo> getAuxiliaryTypeInfos()  {
                List<ITypeInfo> result = new ArrayList<ITypeInfo>();
                for (Class<?> c : auxiliaryTypes) { result.add(createForAuxType(c)); }
                return result;
            }
            public List<String> getEnumConstantNames() {
                if (!isEnum()) { return Collections.emptyList(); }
                List<String> result = new ArrayList<String>();
                for (Object c : auxiliaryTypes[0].getEnumConstants()) { result.add(c.toString()); }
                return result;
            }

            public boolean equals(Object obj) {
                if (obj == this) { return true; }
                if (!(obj instanceof RuntimeTypeInfo)) { return false; }
                RuntimeTypeInfo other = (RuntimeTypeInfo) obj;
                return Arrays.equals(other.auxiliaryTypes, auxiliaryTypes) && type.equals(other.type);
            }
            public int hashCode() {
                return Arrays.hashCode(auxiliaryTypes) + 37 * Assert.hashCode(type);
            }
            public String toString() {
                return String.format("RuntimeTypeInfo(%s, aux=%s, collection=%s, map=%s)",
                        type.getCanonicalName(), Arrays.toString(auxiliaryTypes), isCollection(), isMap());
            }
        }
        /** Internal interface to allow annotation processors to construct a command model at compile time.
         * @since 4.0 */
        public interface IAnnotatedElement {
            Object userObject();
            boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
            <T extends Annotation> T getAnnotation(Class<T> annotationClass);
            String getName();
            String getMixinName();
            boolean isArgSpec();
            boolean isOption();
            boolean isParameter();
            boolean isArgGroup();
            boolean isMixin();
            boolean isUnmatched();
            boolean isInjectSpec();
            boolean isMultiValue();
            boolean isInteractive();
            boolean hasInitialValue();
            boolean isMethodParameter();
            int getMethodParamPosition();
            CommandLine.Model.IScope scope();
            CommandLine.Model.IGetter getter();
            CommandLine.Model.ISetter setter();
            ITypeInfo getTypeInfo();
            String getToString();
        }

        static class TypedMember implements IAnnotatedElement {
            final AccessibleObject accessible;
            final String name;
            final ITypeInfo typeInfo;
            boolean hasInitialValue;
            private IScope scope;
            private IGetter getter;
            private ISetter setter;
            static TypedMember createIfAnnotated(Field field, IScope scope) {
                return isAnnotated(field) ? new TypedMember(field, scope) : null;
            }
            static boolean isAnnotated(AnnotatedElement e) {
                return false
                        || e.isAnnotationPresent(Option.class)
                        || e.isAnnotationPresent(Parameters.class)
                        || e.isAnnotationPresent(ArgGroup.class)
                        || e.isAnnotationPresent(Unmatched.class)
                        || e.isAnnotationPresent(Mixin.class)
                        || e.isAnnotationPresent(Spec.class)
                        || e.isAnnotationPresent(ParentCommand.class);
            }
            TypedMember(Field field) {
                accessible = Assert.notNull(field, "field");
                accessible.setAccessible(true);
                name = field.getName();
                typeInfo = createTypeInfo(field.getType(), field.getGenericType());
                hasInitialValue = true;
            }
            private TypedMember(Field field, IScope scope) {
                this(field);
                Object obj = ObjectScope.tryGet(scope);
                if (obj != null && Proxy.isProxyClass(obj.getClass())) {
                    throw new InitializationException("Invalid picocli annotation on interface field");
                }
                FieldBinding binding = new FieldBinding(scope, field);
                getter = binding; setter = binding;
                this.scope = scope;
                hasInitialValue &= obj != null ;
            }
            static TypedMember createIfAnnotated(Method method, IScope scope, CommandSpec spec) {
                return isAnnotated(method) ? new TypedMember(method, scope, spec) : null;
            }
            private TypedMember(Method method, IScope scope, CommandSpec spec) {
                accessible = Assert.notNull(method, "method");
                accessible.setAccessible(true);
                name = propertyName(method.getName());
                Class<?>[] parameterTypes = method.getParameterTypes();
                boolean isGetter = parameterTypes.length == 0 && method.getReturnType() != Void.TYPE && method.getReturnType() != Void.class;
                boolean isSetter = parameterTypes.length > 0;
                if (isSetter == isGetter) { throw new InitializationException("Invalid method, must be either getter or setter: " + method); }
                if (isGetter) {
                    hasInitialValue = true;
                    typeInfo = createTypeInfo(method.getReturnType(), method.getGenericReturnType());
                    Object proxy = ObjectScope.tryGet(scope);
                    if (Proxy.isProxyClass(proxy.getClass())) {
                        PicocliInvocationHandler handler = (PicocliInvocationHandler) Proxy.getInvocationHandler(proxy);
                        PicocliInvocationHandler.ProxyBinding binding = handler.new ProxyBinding(method);
                        getter = binding; setter = binding;
                        initializeInitialValue(method);
                    } else {
                        //throw new IllegalArgumentException("Getter method but not a proxy: " + scope + ": " + method);
                        MethodBinding binding = new MethodBinding(scope, method, spec);
                        getter = binding; setter = binding;
                    }
                } else {
                    hasInitialValue = false;
                    typeInfo = createTypeInfo(parameterTypes[0], method.getGenericParameterTypes()[0]);
                    MethodBinding binding = new MethodBinding(scope, method, spec);
                    getter = binding; setter = binding;
                }
            }
            TypedMember(MethodParam param, IScope scope) {
                accessible = Assert.notNull(param, "command method parameter");
                accessible.setAccessible(true);
                name = param.getName();
                typeInfo = createTypeInfo(param.getType(), param.getParameterizedType());
                // bind parameter
                ObjectBinding binding = new ObjectBinding();
                getter = binding; setter = binding;
                initializeInitialValue(param);
                hasInitialValue = true;
            }

            private ITypeInfo createTypeInfo(Class<?> type, Type genericType) {
                Range arity = null;
                if (isOption())    { arity = Range.valueOf(getAnnotation(Option.class).arity()); }
                if (isParameter()) { arity = Range.valueOf(getAnnotation(Parameters.class).arity()); }
                if (arity == null || arity.isUnspecified) {
                    if (isOption()) {
                        arity = (type == null || isBoolean(type)) ? Range.valueOf("0") : Range.valueOf("1");
                    } else {
                        arity = Range.valueOf("1");
                    }
                    arity = arity.unspecified(true);
                }
                return RuntimeTypeInfo.create(type, annotationTypes(), genericType, arity, (isOption() ? boolean.class : String.class), isInteractive());
            }

            private void initializeInitialValue(Object arg) {
                Class<?> type = typeInfo.getType();
                try {
                    if      (type == Boolean.TYPE  ) { setter.set(false); }
                    else if (type == Byte.TYPE     ) { setter.set(Byte.valueOf((byte) 0)); }
                    else if (type == Character.TYPE) { setter.set(Character.valueOf((char) 0)); }
                    else if (type == Short.TYPE    ) { setter.set(Short.valueOf((short) 0)); }
                    else if (type == Integer.TYPE  ) { setter.set(Integer.valueOf(0)); }
                    else if (type == Long.TYPE     ) { setter.set(Long.valueOf(0L)); }
                    else if (type == Float.TYPE    ) { setter.set(Float.valueOf(0f)); }
                    else if (type == Double.TYPE   ) { setter.set(Double.valueOf(0d)); }
                    else {                             setter.set(null); }
                } catch (Exception ex) {
                    throw new InitializationException("Could not set initial value for " + arg + ": " + ex.toString(), ex);
                }
            }
            public Object userObject()      { return accessible; }
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return accessible.isAnnotationPresent(annotationClass); }
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass) { return accessible.getAnnotation(annotationClass); }
            public String getName()         { return name; }
            public boolean isArgSpec()      { return isOption() || isParameter() || (isMethodParameter() && !isMixin()); }
            public boolean isOption()       { return isAnnotationPresent(Option.class); }
            public boolean isParameter()    { return isAnnotationPresent(Parameters.class); }
            public boolean isArgGroup()     { return isAnnotationPresent(ArgGroup.class); }
            public boolean isMixin()        { return isAnnotationPresent(Mixin.class); }
            public boolean isUnmatched()    { return isAnnotationPresent(Unmatched.class); }
            public boolean isInjectSpec()   { return isAnnotationPresent(Spec.class); }
            public boolean isMultiValue()   { return CommandLine.isMultiValue(getType()); }
            public boolean isInteractive()  { return (isOption() && getAnnotation(Option.class).interactive()) || (isParameter() && getAnnotation(Parameters.class).interactive()); }
            public IScope  scope()          { return scope; }
            public IGetter getter()         { return getter; }
            public ISetter setter()         { return setter; }
            public ITypeInfo getTypeInfo()  { return typeInfo; }
            public Class<?> getType()       { return typeInfo.getType(); }
            public Class<?>[] getAuxiliaryTypes() { return typeInfo.getAuxiliaryTypes(); }
            private Class<?>[] annotationTypes() {
                if (isOption())    { return getAnnotation(Option.class).type(); }
                if (isParameter()) { return getAnnotation(Parameters.class).type(); }
                return new Class[0];
            }
            public String toString() { return accessible.toString(); }
            public String getToString()  {
                if (isMixin()) { return abbreviate("mixin from member " + toGenericString()); }
                return (accessible instanceof Field ? "field " : accessible instanceof Method ? "method " : accessible.getClass().getSimpleName() + " ") + abbreviate(toGenericString());
            }
            public String toGenericString() { return accessible instanceof Field ? ((Field) accessible).toGenericString() : accessible instanceof Method ? ((Method) accessible).toGenericString() : ((MethodParam)accessible).toString(); }
            public boolean hasInitialValue()    { return hasInitialValue; }
            public boolean isMethodParameter()  { return accessible instanceof MethodParam; }
            public int getMethodParamPosition() { return isMethodParameter() ? ((MethodParam) accessible).position : -1; }
            public String getMixinName()    {
                String annotationName = getAnnotation(Mixin.class).name();
                return empty(annotationName) ? getName() : annotationName;
            }
            static String propertyName(String methodName) {
                if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set"))) { return decapitalize(methodName.substring(3)); }
                return decapitalize(methodName);
            }
            private static String decapitalize(String name) {
                if (name == null || name.length() == 0) { return name; }
                char[] chars = name.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
            static String abbreviate(String text) {
                return text.replace("private ", "")
                        .replace("protected ", "")
                        .replace("public ", "")
                        .replace("java.lang.", "");
            }
        }

        /** Utility class for getting resource bundle strings.
         * Enhances the standard <a href="https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html">ResourceBundle</a>
         * with support for String arrays and qualified keys: keys that may or may not be prefixed with the fully qualified command name.
         * <p>Example properties resource bundle:</p><pre>
         * # Usage Help Message Sections
         * # ---------------------------
         * # Numbered resource keys can be used to create multi-line sections.
         * usage.headerHeading = This is my app. There are other apps like it but this one is mine.%n
         * usage.header   = header first line
         * usage.header.0 = header second line
         * usage.descriptionHeading = Description:%n
         * usage.description.0 = first line
         * usage.description.1 = second line
         * usage.description.2 = third line
         * usage.synopsisHeading = Usage:&#92;u0020
         * # Leading whitespace is removed by default. Start with &#92;u0020 to keep the leading whitespace.
         * usage.customSynopsis.0 =      Usage: ln [OPTION]... [-T] TARGET LINK_NAME   (1st form)
         * usage.customSynopsis.1 = &#92;u0020 or:  ln [OPTION]... TARGET                  (2nd form)
         * usage.customSynopsis.2 = &#92;u0020 or:  ln [OPTION]... TARGET... DIRECTORY     (3rd form)
         * # Headings can contain the %n character to create multi-line values.
         * usage.parameterListHeading = %nPositional parameters:%n
         * usage.optionListHeading = %nOptions:%n
         * usage.commandListHeading = %nCommands:%n
         * usage.footerHeading = Powered by picocli%n
         * usage.footer = footer
         *
         * # Option Descriptions
         * # -------------------
         * # Use numbered keys to create multi-line descriptions.
         * help = Show this help message and exit.
         * version = Print version information and exit.
         * </pre>
         * <p>Resources for multiple commands can be specified in a single ResourceBundle. Keys and their value can be
         * shared by multiple commands (so you don't need to repeat them for every command), but keys can be prefixed with
         * {@code fully qualified command name + "."} to specify different values for different commands.
         * The most specific key wins. For example: </p>
         * <pre>
         * jfrog.rt.usage.header = Artifactory commands
         * jfrog.rt.config.usage.header = Configure Artifactory details.
         * jfrog.rt.upload.usage.header = Upload files.
         *
         * jfrog.bt.usage.header = Bintray commands
         * jfrog.bt.config.usage.header = Configure Bintray details.
         * jfrog.bt.upload.usage.header = Upload files.
         *
         * # shared between all commands
         * usage.footerHeading = Environment Variables:
         * usage.footer.0 = footer line 0
         * usage.footer.1 = footer line 1
         * </pre>
         * @see Command#resourceBundle()
         * @see Option#descriptionKey()
         * @see OptionSpec#description()
         * @see PositionalParamSpec#description()
         * @see CommandSpec#qualifiedName(String)
         * @since 3.6 */
        public static class Messages {
            private final CommandSpec spec;
            private final String bundleBaseName;
            private final ResourceBundle rb;
            private final Set<String> keys;
            public Messages(CommandSpec spec, String baseName) {
                this(spec, baseName, createBundle(baseName));
            }
            public Messages(CommandSpec spec, ResourceBundle rb) {
                this(spec, extractName(rb), rb);
            }
            public Messages(CommandSpec spec, String baseName, ResourceBundle rb) {
                this.spec = Assert.notNull(spec, "CommandSpec");
                this.bundleBaseName = baseName;
                this.rb = rb;
                this.keys = keys(rb);
                new Tracer().debug("Created Messages from resourceBundle[base=%s] for command '%s' (%s)%n", baseName, spec.name(), spec);
            }
            private static ResourceBundle createBundle(String baseName) {
                return ResourceBundle.getBundle(baseName);
            }
            private static String extractName(ResourceBundle rb) {
                try { // ResourceBundle.getBaseBundleName was introduced in Java 8
                    return (String) ResourceBundle.class.getDeclaredMethod("getBaseBundleName").invoke(rb);
                } catch (Exception ignored) { return ""; }
            }
            private static Set<String> keys(ResourceBundle rb) {
                if (rb == null) { return Collections.emptySet(); }
                Set<String> keys = new LinkedHashSet<String>();
                for (Enumeration<String> k = rb.getKeys(); k.hasMoreElements(); keys.add(k.nextElement()));
                return keys;
            }

            /** Returns a copy of the specified Messages object with the CommandSpec replaced by the specified one.
             * @param spec the CommandSpec of the returned Messages
             * @param original the Messages object whose ResourceBundle to reference
             * @return a Messages object with the specified CommandSpec and the ResourceBundle of the specified Messages object
             */
            public static Messages copy(CommandSpec spec, Messages original) {
                return original == null ? null : new Messages(spec, original.bundleBaseName, original.rb);
            }
            /** Returns {@code true} if the specified {@code Messages} is {@code null} or has a {@code null ResourceBundle}. */
            public static boolean empty(Messages messages) { return messages == null || messages.rb == null; }

            /** Returns the String value found in the resource bundle for the specified key, or the specified default value if not found.
             * @param key unqualified resource bundle key. This method will first try to find a value by qualifying the key with the command's fully qualified name,
             *             and if not found, it will try with the unqualified key.
             * @param defaultValue value to return if the resource bundle is null or empty, or if no value was found by the qualified or unqualified key
             * @return the String value found in the resource bundle for the specified key, or the specified default value
             */
            public String getString(String key, String defaultValue) {
                if (isEmpty()) { return defaultValue; }
                String cmd = spec.qualifiedName(".");
                if (keys.contains(cmd + "." + key)) { return rb.getString(cmd + "." + key); }
                if (keys.contains(key)) { return rb.getString(key); }
                return defaultValue;
            }

            boolean isEmpty() { return rb == null || keys.isEmpty(); }

            /** Returns the String array value found in the resource bundle for the specified key, or the specified default value if not found.
             * Multi-line strings can be specified in the resource bundle with {@code key.0}, {@code key.1}, {@code key.2}, etc.
             * @param key unqualified resource bundle key. This method will first try to find a value by qualifying the key with the command's fully qualified name,
             *            and if not found, it will try with the unqualified key.
             * @param defaultValues value to return if the resource bundle is null or empty, or if no value was found by the qualified or unqualified key
             * @return the String array value found in the resource bundle for the specified key, or the specified default value
             */
            public String[] getStringArray(String key, String[] defaultValues) {
                if (isEmpty()) { return defaultValues; }
                String cmd = spec.qualifiedName(".");
                List<String> result = addAllWithPrefix(rb, cmd + "." + key, keys, new ArrayList<String>());
                if (!result.isEmpty()) { return result.toArray(new String[0]); }
                addAllWithPrefix(rb, key, keys, result);
                return result.isEmpty() ? defaultValues : result.toArray(new String[0]);
            }
            private static List<String> addAllWithPrefix(ResourceBundle rb, String key, Set<String> keys, List<String> result) {
                if (keys.contains(key)) { result.add(rb.getString(key)); }
                for (int i = 0; true; i++) {
                    String elementKey = key + "." + i;
                    if (keys.contains(elementKey)) {
                        result.add(rb.getString(elementKey));
                    } else {
                        return result;
                    }
                }
            }
            /** Returns the ResourceBundle of the specified Messages object or {@code null} if the specified Messages object is {@code null}.
             * @since 4.0 */
            public static String resourceBundleBaseName(Messages messages) { return messages == null ? null : messages.resourceBundleBaseName(); }
            /** Returns the ResourceBundle of the specified Messages object or {@code null} if the specified Messages object is {@code null}. */
            public static ResourceBundle resourceBundle(Messages messages) { return messages == null ? null : messages.resourceBundle(); }
            /** Returns the base name of the ResourceBundle of this object or {@code null}.
             * @since 4.0 */
            public String resourceBundleBaseName() { return bundleBaseName; }
            /** Returns the ResourceBundle of this object or {@code null}. */
            public ResourceBundle resourceBundle() { return rb; }
            /** Returns the CommandSpec of this object, never {@code null}. */
            public CommandSpec commandSpec() { return spec; }
        }
        private static class CommandReflection {
            static ArgGroupSpec extractArgGroupSpec(IAnnotatedElement member, IFactory factory, CommandSpec commandSpec, boolean annotationsAreMandatory) throws Exception {
                Object instance = null;
                try { instance = member.getter().get(); } catch (Exception ignored) {}
                Class<?> cls = instance == null ? member.getTypeInfo().getType() : instance.getClass();
                Tracer t = new Tracer();

                if (member.isMultiValue()) {
                    cls = member.getTypeInfo().getAuxiliaryTypes()[0];
                }
                IScope scope = new ObjectScope(instance);
                ArgGroupSpec.Builder builder = ArgGroupSpec.builder(member);
                builder.updateArgGroupAttributes(member.getAnnotation(ArgGroup.class));
                if (member.isOption() || member.isParameter()) {
                    if (member instanceof TypedMember) { validateArgSpecMember((TypedMember) member); }
                    builder.addArg(buildArgForMember(member, factory));
                }

                Stack<Class<?>> hierarchy = new Stack<Class<?>>();
                while (cls != null) { hierarchy.add(cls); cls = cls.getSuperclass(); }
                boolean hasArgAnnotation = false;
                while (!hierarchy.isEmpty()) {
                    cls = hierarchy.pop();
                    hasArgAnnotation |= initFromAnnotatedFields(scope, cls, commandSpec, builder, factory);
                }
                ArgGroupSpec result = builder.build();
                if (annotationsAreMandatory) {validateArgGroupSpec(result, hasArgAnnotation, cls.getName()); }
                return result;
            }
            static CommandSpec extractCommandSpec(Object command, IFactory factory, boolean annotationsAreMandatory) {
                Class<?> cls = command.getClass();
                Tracer t = new Tracer();
                t.debug("Creating CommandSpec for object of class %s with factory %s%n", cls.getName(), factory.getClass().getName());
                if (command instanceof CommandSpec) { return (CommandSpec) command; }

                Object[] tmp = getOrCreateInstance(cls, command, factory, t);
                cls = (Class<?>) tmp[0];
                Object instance = tmp[1];
                String commandClassName = (String) tmp[2];

                CommandSpec result = CommandSpec.wrapWithoutInspection(Assert.notNull(instance, "command"));
                ObjectScope scope = new ObjectScope(instance);

                Stack<Class<?>> hierarchy = new Stack<Class<?>>();
                while (cls != null) { hierarchy.add(cls); cls = cls.getSuperclass(); }
                boolean hasCommandAnnotation = false;
                boolean mixinStandardHelpOptions = false;
                while (!hierarchy.isEmpty()) {
                    cls = hierarchy.pop();
                    Command cmd = cls.getAnnotation(Command.class);
                    if (cmd != null) {
                        result.updateCommandAttributes(cmd, factory);
                        initSubcommands(cmd, cls, result, factory);
                        // addGroups(cmd, groupBuilders); // TODO delete
                        hasCommandAnnotation = true;
                    }
                    hasCommandAnnotation |= initFromAnnotatedFields(scope, cls, result, null, factory);
                    if (cls.isAnnotationPresent(Command.class)) {
                        mixinStandardHelpOptions |= cls.getAnnotation(Command.class).mixinStandardHelpOptions();
                    }
                }
                result.mixinStandardHelpOptions(mixinStandardHelpOptions); //#377 Standard help options should be added last
                if (command instanceof Method) {
                    Method method = (Method) command;
                    t.debug("Using method %s as command %n", method);
                    commandClassName = method.toString();
                    Command cmd = method.getAnnotation(Command.class);
                    result.updateCommandAttributes(cmd, factory);
                    result.setAddMethodSubcommands(false); // method commands don't have method subcommands
                    initSubcommands(cmd, null, result, factory);
                    hasCommandAnnotation = true;
                    result.mixinStandardHelpOptions(method.getAnnotation(Command.class).mixinStandardHelpOptions());
                    initFromMethodParameters(scope, method, result, null, factory);
                    // set command name to method name, unless @Command#name is set
                    result.initName(((Method)command).getName());
                }
                result.updateArgSpecMessages();

                if (annotationsAreMandatory) {validateCommandSpec(result, hasCommandAnnotation, commandClassName); }
                result.withToString(commandClassName).validate();
                return result;
            }

            private static Object[] getOrCreateInstance(Class<?> cls, Object command, IFactory factory, Tracer t) {
                Object instance = command;
                String commandClassName = cls.getName();
                if (command instanceof Class) {
                    cls = (Class) command;
                    commandClassName = cls.getName();
                    try {
                        t.debug("Getting a %s instance from the factory%n", cls.getName());
                        instance = DefaultFactory.create(factory, cls);
                        cls = instance.getClass();
                        commandClassName = cls.getName();
                        t.debug("Factory returned a %s instance%n", commandClassName);
                    } catch (InitializationException ex) {
                        if (cls.isInterface()) {
                            t.debug("%s. Creating Proxy for interface %s%n", ex.getCause(), cls.getName());
                            instance = Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new PicocliInvocationHandler());
                        } else {
                            throw ex;
                        }
                    }
                } else if (command instanceof Method) {
                    cls = null; // don't mix in options/positional params from outer class @Command
                } else if (instance == null) {
                    t.debug("Getting a %s instance from the factory%n", cls.getName());
                    instance = DefaultFactory.create(factory, cls);
                    t.debug("Factory returned a %s instance%n", instance.getClass().getName());
                }
                return new Object[] { cls, instance, commandClassName };
            }
            private static void initSubcommands(Command cmd, Class<?> cls, CommandSpec parent, IFactory factory) {
                for (Class<?> sub : cmd.subcommands()) {
                    try {
                        if (Help.class == sub) { throw new InitializationException(Help.class.getName() + " is not a valid subcommand. Did you mean " + HelpCommand.class.getName() + "?"); }
                        CommandLine subcommandLine = toCommandLine(factory.create(sub), factory);
                        parent.addSubcommand(subcommandName(sub), subcommandLine);
                        initParentCommand(subcommandLine.getCommandSpec().userObject(), parent.userObject());
                    }
                    catch (InitializationException ex) { throw ex; }
                    catch (NoSuchMethodException ex) { throw new InitializationException("Cannot instantiate subcommand " +
                            sub.getName() + ": the class has no constructor", ex); }
                    catch (Exception ex) {
                        throw new InitializationException("Could not instantiate and add subcommand " +
                                sub.getName() + ": " + ex, ex);
                    }
                }
                if (cmd.addMethodSubcommands() && cls != null) {
                    for (CommandLine sub : CommandSpec.createMethodSubcommands(cls, factory)) {
                        parent.addSubcommand(sub.getCommandName(), sub);
                    }
                }
            }
            static void initParentCommand(Object subcommand, Object parent) {
                if (subcommand == null) { return; }
                try {
                    Class<?> cls = subcommand.getClass();
                    while (cls != null) {
                        for (Field f : cls.getDeclaredFields()) {
                            if (f.isAnnotationPresent(ParentCommand.class)) {
                                f.setAccessible(true);
                                f.set(subcommand, parent);
                            }
                        }
                        cls = cls.getSuperclass();
                    }
                } catch (Exception ex) {
                    throw new InitializationException("Unable to initialize @ParentCommand field: " + ex, ex);
                }
            }
            private static String subcommandName(Class<?> sub) {
                Command subCommand = sub.getAnnotation(Command.class);
                if (subCommand == null || Help.DEFAULT_COMMAND_NAME.equals(subCommand.name())) {
                    throw new InitializationException("Subcommand " + sub.getName() +
                            " is missing the mandatory @Command annotation with a 'name' attribute");
                }
                return subCommand.name();
            }
            private static boolean initFromAnnotatedFields(IScope scope, Class<?> cls, CommandSpec receiver, ArgGroupSpec.Builder groupBuilder, IFactory factory) {
                boolean result = false;
                for (Field field : cls.getDeclaredFields()) {
                    result |= initFromAnnotatedTypedMembers(TypedMember.createIfAnnotated(field, scope), receiver, groupBuilder, factory);
                }
                for (Method method : cls.getDeclaredMethods()) {
                    result |= initFromAnnotatedTypedMembers(TypedMember.createIfAnnotated(method, scope, receiver), receiver, groupBuilder, factory);
                }
                return result;
            }
            @SuppressWarnings("unchecked")
            private static boolean initFromAnnotatedTypedMembers(TypedMember member,
                                                                 CommandSpec commandSpec,
                                                                 ArgGroupSpec.Builder groupBuilder,
                                                                 IFactory factory) {
                boolean result = false;
                if (member == null) { return result; }
                if (member.isMixin()) {
                    assertNoDuplicateAnnotations(member, Mixin.class, Option.class, Parameters.class, Unmatched.class, Spec.class, ArgGroup.class);
                    if (groupBuilder != null) {
                        throw new InitializationException("@Mixins are not supported on @ArgGroups");
                        // TODO groupBuilder.addMixin(member.getMixinName(), buildMixinForMember(member, factory));
                    } else {
                        commandSpec.addMixin(member.getMixinName(), buildMixinForMember(member, factory));
                    }
                    result = true;
                }
                if (member.isArgGroup()) {
                    assertNoDuplicateAnnotations(member, ArgGroup.class, Spec.class, Parameters.class, Option.class, Unmatched.class, Mixin.class);
                    if (groupBuilder != null) {
                        groupBuilder.addSubgroup(buildArgGroupForMember(member, factory, commandSpec));
                    } else {
                        commandSpec.addArgGroup(buildArgGroupForMember(member, factory, commandSpec));
                    }
                    return true;
                }
                if (member.isUnmatched()) {
                    assertNoDuplicateAnnotations(member, Unmatched.class, Mixin.class, Option.class, Parameters.class, Spec.class, ArgGroup.class);
                    if (groupBuilder != null) {
                        // we don't support @Unmatched on @ArgGroup class members...
                        throw new InitializationException("@Unmatched are not supported on @ArgGroups");
                    } else {
                        commandSpec.addUnmatchedArgsBinding(buildUnmatchedForMember(member));
                    }
                }
                if (member.isArgSpec()) {
                    validateArgSpecMember(member);
                    if (groupBuilder != null) {
                        groupBuilder.addArg(buildArgForMember(member, factory));
                    } else {
                        commandSpec.add(buildArgForMember(member, factory));
                    }
                    result = true;
                }
                if (member.isInjectSpec()) {
                    validateInjectSpec(member);
                    try { member.setter().set(commandSpec); } catch (Exception ex) { throw new InitializationException("Could not inject spec", ex); }
                }
                return result;
            }
            private static boolean initFromMethodParameters(IScope scope, Method method, CommandSpec receiver, ArgGroupSpec.Builder groupBuilder, IFactory factory) {
                boolean result = false;
                int optionCount = 0;
                for (int i = 0, count = method.getParameterTypes().length; i < count; i++) {
                    MethodParam param = new MethodParam(method, i);
                    if (param.isAnnotationPresent(Option.class) || param.isAnnotationPresent(Mixin.class)) {
                        optionCount++;
                    } else {
                        param.position = i - optionCount;
                    }
                    result |= initFromAnnotatedTypedMembers(new TypedMember(param, scope), receiver, groupBuilder, factory);
                }
                return result;
            }
            @SuppressWarnings("unchecked")
            private static void validateArgSpecMember(TypedMember member) {
                if (!member.isArgSpec()) { throw new IllegalStateException("Bug: validateArgSpecMember() should only be called with an @Option or @Parameters member"); }
                if (member.isOption()) {
                    assertNoDuplicateAnnotations(member, Option.class, Unmatched.class, Mixin.class, Parameters.class, Spec.class, ArgGroup.class);
                } else {
                    assertNoDuplicateAnnotations(member, Parameters.class, Option.class, Unmatched.class, Mixin.class, Spec.class, ArgGroup.class);
                }
                if (!(member.accessible instanceof Field)) { return; }
                Field field = (Field) member.accessible;
                if (Modifier.isFinal(field.getModifiers()) && (field.getType().isPrimitive() || String.class.isAssignableFrom(field.getType()))) {
                    throw new InitializationException("Constant (final) primitive and String fields like " + field + " cannot be used as " +
                            (member.isOption() ? "an @Option" : "a @Parameter") + ": compile-time constant inlining may hide new values written to it.");
                }
            }
            private static void validateCommandSpec(CommandSpec result, boolean hasCommandAnnotation, String commandClassName) {
                if (!hasCommandAnnotation && result.positionalParameters.isEmpty() && result.optionsByNameMap.isEmpty() && result.unmatchedArgs.isEmpty()) {
                    throw new InitializationException(commandClassName + " is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations");
                }
            }
            private static void validateArgGroupSpec(ArgGroupSpec result, boolean hasArgAnnotation, String className) {
                if (!hasArgAnnotation && result.args().isEmpty()) {
                    throw new InitializationException(className + " is not a group: it has no @Option or @Parameters annotations");
                }
            }
            @SuppressWarnings("unchecked")
            private static void validateInjectSpec(TypedMember member) {
                if (!member.isInjectSpec()) { throw new IllegalStateException("Bug: validateInjectSpec() should only be called with @Spec members"); }
                assertNoDuplicateAnnotations(member, Spec.class, Parameters.class, Option.class, Unmatched.class, Mixin.class, ArgGroup.class);
                if (!CommandSpec.class.getName().equals(member.getTypeInfo().getClassName())) {
                    throw new InitializationException("@picocli.CommandLine.Spec annotation is only supported on fields of type " + CommandSpec.class.getName());
                }
            }
            private static void assertNoDuplicateAnnotations(TypedMember member, Class<? extends Annotation> myAnnotation, Class<? extends Annotation>... forbidden) {
                for (Class<? extends Annotation> annotation : forbidden) {
                    if (member.isAnnotationPresent(annotation)) {
                        throw new DuplicateOptionAnnotationsException("A member cannot have both @" + myAnnotation.getSimpleName() + " and @" + annotation.getSimpleName() + " annotations, but '" + member + "' has both.");
                    }
                }
            }
            private static CommandSpec buildMixinForMember(IAnnotatedElement member, IFactory factory) {
                try {
                    Object userObject = member.getter().get();
                    if (userObject == null) {
                        userObject = factory.create(member.getTypeInfo().getType());
                        member.setter().set(userObject);
                    }
                    CommandSpec result = CommandSpec.forAnnotatedObject(userObject, factory);
                    return result.withToString(member.getToString());
                } catch (InitializationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InitializationException("Could not access or modify mixin member " + member + ": " + ex, ex);
                }
            }
            private static ArgSpec buildArgForMember(IAnnotatedElement member, IFactory factory) {
                if (member.isOption())         { return OptionSpec.builder(member, factory).build(); }
                else if (member.isParameter()) { return PositionalParamSpec.builder(member, factory).build(); }
                else                           { return PositionalParamSpec.builder(member, factory).build(); }
            }
            private static ArgGroupSpec buildArgGroupForMember(IAnnotatedElement member, IFactory factory, CommandSpec commandSpec) {
                try {
                    return extractArgGroupSpec(member, factory, commandSpec, true);
                } catch (InitializationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InitializationException("Could not access or modify ArgGroup member " + member + ": " + ex, ex);
                }
            }
            private static UnmatchedArgsBinding buildUnmatchedForMember(final IAnnotatedElement member) {
                ITypeInfo info = member.getTypeInfo();
                if (!(info.getClassName().equals(String[].class.getName()) ||
                        (info.isCollection() && info.getActualGenericTypeArguments().equals(Arrays.asList(String.class.getName()))))) {
                    throw new InitializationException("Invalid type for " + member + ": must be either String[] or List<String>");
                }
                if (info.getClassName().equals(String[].class.getName())) {
                    return UnmatchedArgsBinding.forStringArrayConsumer(member.setter());
                } else {
                    return UnmatchedArgsBinding.forStringCollectionSupplier(new IGetter() {
                        @SuppressWarnings("unchecked") public <T> T get() throws Exception {
                            List<String> result = (List<String>) member.getter().get();
                            if (result == null) {
                                result = new ArrayList<String>();
                                member.setter().set(result);
                            }
                            return (T) result;
                        }
                    });
                }
            }
        }

        static class FieldBinding implements IGetter, ISetter {
            private final IScope scope;
            private final Field field;
            private static IScope asScope(Object scope) { return scope instanceof IScope ? ((IScope) scope) : new ObjectScope(scope); }
            FieldBinding(Object scope, Field field) { this(asScope(scope), field); }
            FieldBinding(IScope scope, Field field) { this.scope = scope; this.field = field; }
            public <T> T get() throws PicocliException {
                Object obj = null;
                try { obj = scope.get(); }
                catch (Exception ex) { throw new PicocliException("Could not get scope for field " + field, ex); }
                try {
                    @SuppressWarnings("unchecked") T result = (T) field.get(obj);
                    return result;
                } catch (Exception ex) {
                    throw new PicocliException("Could not get value for field " + field, ex);
                }
            }
            public <T> T set(T value) throws PicocliException {
                Object obj = null;
                try { obj = scope.get(); }
                catch (Exception ex) { throw new PicocliException("Could not get scope for field " + field, ex); }
                try {
                    @SuppressWarnings("unchecked") T result = (T) field.get(obj);
                    field.set(obj, value);
                    return result;
                } catch (Exception ex) {
                    throw new PicocliException("Could not set value for field " + field + " to " + value, ex);
                }
            }
            public String toString() {
                return String.format("%s(%s %s.%s)", getClass().getSimpleName(), field.getType().getName(),
                        field.getDeclaringClass().getName(), field.getName());
            }
        }
        static class MethodBinding implements IGetter, ISetter {
            private final IScope scope;
            private final Method method;
            private final CommandSpec spec;
            private Object currentValue;
            MethodBinding(IScope scope, Method method, CommandSpec spec) {
                this.scope = scope;
                this.method = method;
                this.spec = spec;
            }
            @SuppressWarnings("unchecked") public <T> T get() { return (T) currentValue; }
            public <T> T set(T value) throws PicocliException {
                Object obj = null;
                try { obj = scope.get(); }
                catch (Exception ex) { throw new PicocliException("Could not get scope for method " + method, ex); }
                try {
                    @SuppressWarnings("unchecked") T result = (T) currentValue;
                    method.invoke(obj, value);
                    currentValue = value;
                    return result;
                } catch (InvocationTargetException ex) {
                    if (ex.getCause() instanceof PicocliException) { throw (PicocliException) ex.getCause(); }
                    throw createParameterException(value, ex.getCause());
                } catch (Exception ex) {
                    throw createParameterException(value, ex);
                }
            }
            private ParameterException createParameterException(Object value, Throwable t) {
                CommandLine cmd = spec.commandLine() == null ? new CommandLine(spec) : spec.commandLine();
                return new ParameterException(cmd, "Could not invoke " + method + " with " + value, t);
            }
            public String toString() {
                return String.format("%s(%s)", getClass().getSimpleName(), method);
            }
        }
        private static class PicocliInvocationHandler implements InvocationHandler {
            final Map<String, Object> map = new HashMap<String, Object>();
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return map.get(method.getName());
            }
            class ProxyBinding implements IGetter, ISetter {
                private final Method method;
                ProxyBinding(Method method) { this.method = Assert.notNull(method, "method"); }
                @SuppressWarnings("unchecked") public <T> T get() { return (T) map.get(method.getName()); }
                public <T> T set(T value) {
                    T result = get();
                    map.put(method.getName(), value);
                    return result;
                }
            }
        }
        private static class ObjectBinding implements IGetter, ISetter {
            private Object value;
            @SuppressWarnings("unchecked") public <T> T get() { return (T) value; }
            public <T> T set(T value) {
                @SuppressWarnings("unchecked") T result = value;
                this.value = value;
                return result;
            }
            public String toString() {
                return String.format("%s(value=%s)", getClass().getSimpleName(), value);
            }
        }
        static class ObjectScope implements IScope {
            private Object value;
            public ObjectScope(Object value) { this.value = value; }
            @SuppressWarnings("unchecked") public <T> T get() { return (T) value; }
            @SuppressWarnings("unchecked") public <T> T set(T value) { T old = (T) this.value; this.value = value; return old; }
            public static Object tryGet(IScope scope) {
                try {
                    return scope.get();
                } catch (Exception e) {
                    throw new InitializationException("Could not get scope value", e);
                }
            }
            public String toString() { return String.format("Scope(value=%s)", value); }
        }
        static class Interpolator {
            private final CommandSpec commandSpec;
            private final Map<String, ILookup> lookups = new LinkedHashMap<String, ILookup>();

            public Interpolator(final CommandSpec commandSpec) {
                this.commandSpec = commandSpec;
                lookups.put("sys:", new ILookup() { public String get(String key) { return System.getProperty(key); } });
                lookups.put("env:", new ILookup() { public String get(String key) { return System.getenv(key); } });
                lookups.put("bundle:", new ILookup() {
                    public String get(String key) {
                        //commandSpec.usageMessage().messages().
                        return bundleValue(commandSpec.resourceBundle(), key);
                    }
                });
                lookups.put("", new ILookup() {
                    public String get(String key) {
                        String result = System.getProperty(key);
                        if (result == null) { result = System.getenv(key); }
                        if (result == null) { result = bundleValue(commandSpec.resourceBundle(), key); }
                        return result;
                    }
                });
            }
            private static String bundleValue(ResourceBundle rb, String key) {
                if (rb != null) {
                    try {return rb.getString(key);} catch (MissingResourceException ex) { return null; }
                }
                return null;
            }

            public String[] interpolate(String[] values) {
                if (values == null || values.length == 0) { return values; }
                String[] result = new String[values.length];
                for (int i = 0; i < result.length; i++) { result[i] = interpolate(values[i]); }
                return result;
            }
            public String interpolate(String original) {
                if (original == null || !commandSpec.interpolateVariables()) { return original; }
                // TODO don't expand escaped vars, like $${COMMAND-NAME}
                String result1 = original.replaceAll("\\$\\{COMMAND-NAME}", commandSpec.name());
                String result2 = result1.replaceAll("\\$\\{COMMAND-FULL-NAME}", commandSpec.qualifiedName());
                if (commandSpec.parent() != null) {
                    String tmp = result2.replaceAll("\\$\\{PARENT-COMMAND-NAME}", commandSpec.parent().name());
                    result2 = tmp.replaceAll("\\$\\{PARENT-COMMAND-FULL-NAME}", commandSpec.parent().qualifiedName());
                }
                String result = resolveLookups(result2, new HashSet<String>(), new HashMap<String, String>());
                return result;
            }
            public String interpolateCommandName(String original) {
                if (original == null || !commandSpec.interpolateVariables()) { return original; }
                return resolveLookups(original, new HashSet<String>(), new HashMap<String, String>());
            }

            private String resolveLookups(String text, Set<String> visited, Map<String, String> resolved) {
                if (text == null) { return null; }
                for (String lookupKey : lookups.keySet()) {
                    ILookup lookup = lookups.get(lookupKey);
                    String prefix = "${" + lookupKey;
                    int startPos = 0;
                    while ((startPos = findOpeningDollar(text, prefix, startPos)) >= 0) {
                        int endPos = findClosingBrace(text, startPos + prefix.length());
                        if (endPos < 0) { endPos = text.length() - 1; }
                        String fullKey = text.substring(startPos + prefix.length(), endPos);
                        String actualKey = fullKey;

                        int defaultStartPos = fullKey.indexOf(":-");
                        if (defaultStartPos >= 0) { actualKey = fullKey.substring(0, defaultStartPos); }
                        String value = resolved.containsKey(prefix + actualKey)
                                ? resolved.get(prefix + actualKey)
                                : lookup.get(actualKey);
                        if (visited.contains(prefix + actualKey) && !resolved.containsKey(prefix + actualKey)) {
                            throw new InitializationException("Lookup '" + prefix + actualKey + "' has a circular reference.");
                        }
                        visited.add(prefix + actualKey);
                        if (value == null && defaultStartPos >= 0) {
                            String defaultValue = fullKey.substring(defaultStartPos + 2);
                            value = resolveLookups(defaultValue, visited, resolved);
                        }
                        resolved.put(prefix + actualKey, value);
                        if (value == null && startPos == 0 && endPos == text.length() - 1) {
                            return null; // #676 x="${var}" should resolve to x=null if not found (not x="null")
                        }

                        // interpolate
                        text = text.substring(0, startPos) + value + text.substring(endPos + 1);
                        startPos += value == null ? "null".length() : value.length();
                    }
                }
                return text.replace("$$", "$");
            }

            private int findOpeningDollar(String text, String prefix, int start) {
                int open = -1;
                boolean escaping = false;
                for (int ch = 0, i = start; i < text.length(); i += Character.charCount(ch)) {
                    ch = text.codePointAt(i);
                    switch (ch) {
                        case '$':
                            open = escaping ? -1 : i;
                            escaping = !escaping;
                            break;
                        default:
                            escaping = false;
                            break;
                    }
                    if (open != -1 && ch != prefix.codePointAt(i - open)) {
                        open = -1;
                    }
                    if (open != -1 && i - open == prefix.length() - 1) {
                        return open;
                    }
                }
                return -1;
            }

            private int findClosingBrace(String text, int start) {
                int open = 1;
                boolean escaping = false;
                for (int ch = 0, i = start; i < text.length(); i += Character.charCount(ch)) {
                    ch = text.codePointAt(i);
                    switch (ch) {
                        case '\\':
                            escaping = !escaping;
                            break;
                        case '}':
                            if (!escaping) { open--; }
                            if (open == 0) { return i; }
                            escaping = false;
                            break;
                        case '{':
                            if (!escaping) { open++; }
                            escaping = false;
                            break;
                        default:
                            escaping = false;
                            break;
                    }
                }
                return -1;
            }

            interface ILookup {
                String get(String key);
            }
        }
    }

    /** Encapsulates the result of parsing an array of command line arguments.
     * @since 3.0 */
    public static class ParseResult {
        private final CommandSpec commandSpec;
        private final List<OptionSpec> matchedOptions;
        private final List<PositionalParamSpec> matchedUniquePositionals;
        private final List<String> originalArgs;
        private final List<String> unmatched;
        private final List<List<PositionalParamSpec>> matchedPositionalParams;
        private final List<Exception> errors;
        private final GroupMatchContainer groupMatchContainer;
        final List<Object> tentativeMatch;

        private final ParseResult subcommand;
        private final boolean usageHelpRequested;
        private final boolean versionHelpRequested;

        private ParseResult(ParseResult.Builder builder) {
            commandSpec = builder.commandSpec;
            subcommand = builder.subcommand;
            matchedOptions = new ArrayList<OptionSpec>(builder.options);
            unmatched = new ArrayList<String>(builder.unmatched);
            originalArgs = new ArrayList<String>(builder.originalArgList);
            matchedUniquePositionals = new ArrayList<PositionalParamSpec>(builder.positionals);
            matchedPositionalParams = new ArrayList<List<PositionalParamSpec>>(builder.positionalParams);
            errors = new ArrayList<Exception>(builder.errors);
            usageHelpRequested = builder.usageHelpRequested;
            versionHelpRequested = builder.versionHelpRequested;
            tentativeMatch = builder.nowProcessing;
            groupMatchContainer = builder.groupMatchContainer.trim();
        }
        /** Creates and returns a new {@code ParseResult.Builder} for the specified command spec. */
        public static Builder builder(CommandSpec commandSpec) { return new Builder(commandSpec); }

        /**
         * Returns the matches for the specified argument group.
         * @since 4.0 */
        public List<GroupMatchContainer> findMatches(ArgGroupSpec group) {
            return groupMatchContainer.findMatchContainers(group, new ArrayList<GroupMatchContainer>());
        }

        /**
         * Returns the top-level container for the {@link ArgGroupSpec ArgGroupSpec} match or matches found.
         * <p>
         * If the user input was a valid combination of group arguments, the returned list should contain a single
         * {@linkplain GroupMatch match}. Details of the {@linkplain GroupMatchContainer matched groups} encountered
         * on the command line can be obtained via its {@link GroupMatch#matchedSubgroups() matchedSubgroups()} method.
         * The top-level match returned by this method contains no {@linkplain GroupMatch#matchedValues(ArgSpec) matched arguments}.
         * </p><p>
         * If the returned list contains more than one {@linkplain GroupMatch match}, the user input was invalid:
         * the maximum {@linkplain ArgGroup#multiplicity() multiplicity} of a group was exceeded, and the parser created an extra
         * {@code match} to capture the values. Usually this results in a {@link ParameterException ParameterException}
         * being thrown by the {@code parse} method, unless the parser is configured to {@linkplain ParserSpec#collectErrors() collect errors}.
         * </p>
         * @since 4.0 */
        public List<GroupMatch> getGroupMatches() {
            return groupMatchContainer.matches();
        }
        /** Returns the option with the specified short name, or {@code null} if no option with that name was matched
         * on the command line.
         * <p>Use {@link OptionSpec#getValue() getValue} on the returned {@code OptionSpec} to get the matched value (or values),
         * converted to the type of the option. Alternatively, use {@link OptionSpec#stringValues() stringValues}
         * to get the matched String values after they were {@linkplain OptionSpec#splitRegex() split} into parts, or
         * {@link OptionSpec#originalStringValues() originalStringValues} to get the original String values that were
         * matched on the command line, before any processing.
         * </p><p>To get the {@linkplain OptionSpec#defaultValue() default value} of an option that was
         * {@linkplain #hasMatchedOption(char) <em>not</em> matched} on the command line, use
         * {@code parseResult.commandSpec().findOption(shortName).getValue()}. </p>
         * @see CommandSpec#findOption(char)  */
        public OptionSpec matchedOption(char shortName) { return CommandSpec.findOption(shortName, matchedOptions); }

        /** Returns the option with the specified name, or {@code null} if no option with that name was matched on the command line.
         * <p>Use {@link OptionSpec#getValue() getValue} on the returned {@code OptionSpec} to get the matched value (or values),
         * converted to the type of the option. Alternatively, use {@link OptionSpec#stringValues() stringValues}
         * to get the matched String values after they were {@linkplain OptionSpec#splitRegex() split} into parts, or
         * {@link OptionSpec#originalStringValues() originalStringValues} to get the original String values that were
         * matched on the command line, before any processing.
         * </p><p>To get the {@linkplain OptionSpec#defaultValue() default value} of an option that was
         * {@linkplain #hasMatchedOption(String) <em>not</em> matched} on the command line, use
         * {@code parseResult.commandSpec().findOption(String).getValue()}. </p>
         * @see CommandSpec#findOption(String)
         * @param name used to search the matched options. May be an alias of the option name that was actually specified on the command line.
         *      The specified name may include option name prefix characters or not. */
        public OptionSpec matchedOption(String name) { return CommandSpec.findOption(name, matchedOptions); }

        /** Returns the first {@code PositionalParamSpec} that matched an argument at the specified position, or {@code null} if no positional parameters were matched at that position. */
        public PositionalParamSpec matchedPositional(int position) {
            if (matchedPositionalParams.size() <= position || matchedPositionalParams.get(position).isEmpty()) { return null; }
            return matchedPositionalParams.get(position).get(0);
        }

        /** Returns all {@code PositionalParamSpec} objects that matched an argument at the specified position, or an empty list if no positional parameters were matched at that position. */
        public List<PositionalParamSpec> matchedPositionals(int position) {
            if (matchedPositionalParams.size() <= position) { return Collections.emptyList(); }
            return matchedPositionalParams.get(position) == null ? Collections.<PositionalParamSpec>emptyList() : matchedPositionalParams.get(position);
        }
        /** Returns the {@code CommandSpec} for the matched command. */
        public CommandSpec commandSpec()                    { return commandSpec; }

        /** Returns whether an option whose aliases include the specified short name was matched on the command line.
         * @param shortName used to search the matched options. May be an alias of the option name that was actually specified on the command line. */
        public boolean hasMatchedOption(char shortName)     { return matchedOption(shortName) != null; }
        /** Returns whether an option whose aliases include the specified name was matched on the command line.
         * @param name used to search the matched options. May be an alias of the option name that was actually specified on the command line.
         *      The specified name may include option name prefix characters or not. */
        public boolean hasMatchedOption(String name)        { return matchedOption(name) != null; }
        /** Returns whether the specified option was matched on the command line. */
        public boolean hasMatchedOption(OptionSpec option)  { return matchedOptions.contains(option); }

        /** Returns whether a positional parameter was matched at the specified position. */
        public boolean hasMatchedPositional(int position)   { return matchedPositional(position) != null; }
        /** Returns whether the specified positional parameter was matched on the command line. */
        public boolean hasMatchedPositional(PositionalParamSpec positional) { return matchedUniquePositionals.contains(positional); }

        /** Returns a list of matched options, in the order they were found on the command line. */
        public List<OptionSpec> matchedOptions()            { return Collections.unmodifiableList(matchedOptions); }

        /** Returns a list of matched positional parameters. */
        public List<PositionalParamSpec> matchedPositionals() { return Collections.unmodifiableList(matchedUniquePositionals); }

        /** Returns a list of command line arguments that did not match any options or positional parameters. */
        public List<String> unmatched()                     { return Collections.unmodifiableList(unmatched); }

        /** Returns the command line arguments that were parsed. */
        public List<String> originalArgs()                  { return Collections.unmodifiableList(originalArgs); }

        /** If {@link ParserSpec#collectErrors} is {@code true}, returns the list of exceptions that were encountered during parsing, otherwise, returns an empty list.
         * @since 3.2 */
        public List<Exception> errors()                     { return Collections.unmodifiableList(errors); }

        /** Returns the command line argument value of the option with the specified name, converted to the {@linkplain OptionSpec#type() type} of the option, or the specified default value if no option with the specified name was matched. */
        public <T> T matchedOptionValue(char shortName, T defaultValue)    { return matchedOptionValue(matchedOption(shortName), defaultValue); }
        /** Returns the command line argument value of the option with the specified name, converted to the {@linkplain OptionSpec#type() type} of the option, or the specified default value if no option with the specified name was matched. */
        public <T> T matchedOptionValue(String name, T defaultValue)       { return matchedOptionValue(matchedOption(name), defaultValue); }
        /** Returns the command line argument value of the specified option, converted to the {@linkplain OptionSpec#type() type} of the option, or the specified default value if the specified option is {@code null}. */
        @SuppressWarnings("unchecked")
        private <T> T matchedOptionValue(OptionSpec option, T defaultValue) { return option == null ? defaultValue : (T) option.getValue(); }

        /** Returns the command line argument value of the positional parameter at the specified position, converted to the {@linkplain PositionalParamSpec#type() type} of the positional parameter, or the specified default value if no positional parameter was matched at that position. */
        public <T> T matchedPositionalValue(int position, T defaultValue)  { return matchedPositionalValue(matchedPositional(position), defaultValue); }
        /** Returns the command line argument value of the specified positional parameter, converted to the {@linkplain PositionalParamSpec#type() type} of the positional parameter, or the specified default value if the specified positional parameter is {@code null}. */
        @SuppressWarnings("unchecked")
        private <T> T matchedPositionalValue(PositionalParamSpec positional, T defaultValue) { return positional == null ? defaultValue : (T) positional.getValue(); }

        /** Returns {@code true} if a subcommand was matched on the command line, {@code false} otherwise. */
        public boolean hasSubcommand()          { return subcommand != null; }

        /** Returns the {@code ParseResult} for the subcommand of this command that was matched on the command line, or {@code null} if no subcommand was matched. */
        public ParseResult subcommand()         { return subcommand; }

        /** Returns {@code true} if one of the options that was matched on the command line is a {@link OptionSpec#usageHelp() usageHelp} option. */
        public boolean isUsageHelpRequested()   { return usageHelpRequested; }

        /** Returns {@code true} if one of the options that was matched on the command line is a {@link OptionSpec#versionHelp() versionHelp} option. */
        public boolean isVersionHelpRequested() { return versionHelpRequested; }

        /** Returns this {@code ParseResult} as a list of {@code CommandLine} objects, one for each matched command/subcommand.
         * For backwards compatibility with pre-3.0 methods. */
        public List<CommandLine> asCommandLineList() {
            List<CommandLine> result = new ArrayList<CommandLine>();
            ParseResult pr = this;
            while (pr != null) { result.add(pr.commandSpec().commandLine()); pr = pr.hasSubcommand() ? pr.subcommand() : null; }
            return result;
        }

        void validateGroups() {
            for (ArgGroupSpec group : commandSpec.argGroups()) {
                groupMatchContainer.updateUnmatchedGroups(group);
            }
            groupMatchContainer.validate(commandSpec.commandLine());
        }

        /** Builds immutable {@code ParseResult} instances. */
        public static class Builder {
            private final CommandSpec commandSpec;
            private final Set<OptionSpec> options = new LinkedHashSet<OptionSpec>();
            private final Set<PositionalParamSpec> positionals = new LinkedHashSet<PositionalParamSpec>();
            private final List<String> unmatched = new ArrayList<String>();
            private final List<String> originalArgList = new ArrayList<String>();
            private final List<List<PositionalParamSpec>> positionalParams = new ArrayList<List<PositionalParamSpec>>();
            private ParseResult subcommand;
            private boolean usageHelpRequested;
            private boolean versionHelpRequested;
            boolean isInitializingDefaultValues;
            private List<Exception> errors = new ArrayList<Exception>(1);
            private List<Object> nowProcessing;
            private GroupMatchContainer groupMatchContainer = new GroupMatchContainer(null, null);

            private Builder(CommandSpec spec) { commandSpec = Assert.notNull(spec, "commandSpec"); }
            /** Creates and returns a new {@code ParseResult} instance for this builder's configuration. */
            public ParseResult build() {
                return new ParseResult(this);
            }

            private void nowProcessing(ArgSpec spec, Object value) {
                if (nowProcessing != null && !isInitializingDefaultValues) {
                    nowProcessing.add(spec.isPositional() ? spec : value);
                }
            }

            /** Adds the specified {@code OptionSpec} or {@code PositionalParamSpec} to the list of options and parameters
             * that were matched on the command line.
             * @param arg the matched {@code OptionSpec} or {@code PositionalParamSpec}
             * @param position the command line position at which the  {@code PositionalParamSpec} was matched. Ignored for {@code OptionSpec}s.
             * @return this builder for method chaining */
            public Builder add(ArgSpec arg, int position) {
                if (arg.isOption()) {
                    addOption((OptionSpec) arg);
                } else {
                    addPositionalParam((PositionalParamSpec) arg, position);
                }
                afterMatchingGroupElement(arg, position);
                return this;
            }

            /** Adds the specified {@code OptionSpec} to the list of options that were matched on the command line. */
            public Builder addOption(OptionSpec option) { if (!isInitializingDefaultValues) {options.add(option);} return this; }
            /** Adds the specified {@code PositionalParamSpec} to the list of parameters that were matched on the command line.
             * @param positionalParam the matched {@code PositionalParamSpec}
             * @param position the command line position at which the  {@code PositionalParamSpec} was matched.
             * @return this builder for method chaining */
            public Builder addPositionalParam(PositionalParamSpec positionalParam, int position) {
                if (isInitializingDefaultValues) { return this; }
                positionals.add(positionalParam);
                while (positionalParams.size() <= position) { positionalParams.add(new ArrayList<PositionalParamSpec>()); }
                positionalParams.get(position).add(positionalParam);
                return this;
            }
            /** Adds the specified command line argument to the list of unmatched command line arguments. */
            public Builder addUnmatched(String arg) { unmatched.add(arg); return this; }
            /** Adds all elements of the specified command line arguments stack to the list of unmatched command line arguments. */
            public Builder addUnmatched(Stack<String> args) { while (!args.isEmpty()) { addUnmatched(args.pop()); } return this; }
            /** Sets the specified {@code ParseResult} for a subcommand that was matched on the command line. */
            public Builder subcommand(ParseResult subcommand) { this.subcommand = subcommand; return this; }
            /** Sets the specified command line arguments that were parsed. */
            public Builder originalArgs(String[] originalArgs) { originalArgList.addAll(Arrays.asList(originalArgs)); return this;}

            void addStringValue        (ArgSpec argSpec, String value) { if (!isInitializingDefaultValues) { argSpec.stringValues.add(value);} }
            void addOriginalStringValue(ArgSpec argSpec, String value) {
                if (!isInitializingDefaultValues) {
                    argSpec.originalStringValues.add(value);
                    if (argSpec.group() != null) {
                        GroupMatchContainer groupMatchContainer = this.groupMatchContainer.findLastMatchContainer(argSpec.group());
                        groupMatchContainer.lastMatch().addOriginalStringValue(argSpec, value);
                    }
                }
            }

            void addTypedValues(ArgSpec argSpec, int position, Object typedValue) {
                if (!isInitializingDefaultValues) {
                    argSpec.typedValues.add(typedValue);
                    if (argSpec.group() == null) {
                        argSpec.typedValueAtPosition.put(position, typedValue);
                    } else {
                        GroupMatchContainer groupMatchContainer = this.groupMatchContainer.findLastMatchContainer(argSpec.group());
                        groupMatchContainer.lastMatch().addMatchedValue(argSpec, position, typedValue, commandSpec.commandLine.tracer);
                    }
                }
            }

            public void addError(PicocliException ex) {
                errors.add(Assert.notNull(ex, "exception"));
            }

            void beforeMatchingGroupElement(ArgSpec argSpec) throws Exception {
                ArgGroupSpec group = argSpec.group();
                if (group == null || isInitializingDefaultValues) { return; }
                GroupMatchContainer foundGroupMatchContainer = this.groupMatchContainer.findOrCreateMatchingGroup(argSpec, commandSpec.commandLine);
                if (foundGroupMatchContainer.lastMatch().matchedMinElements() && argSpec.required()) {
                    // we need to create a new match; if maxMultiplicity has been reached, we need to add a new GroupMatchContainer.
                    String elementDescription = ArgSpec.describe(argSpec, "=");
                    Tracer tracer = commandSpec.commandLine.tracer;
                    tracer.info("GroupMatch %s is complete: its mandatory elements are all matched. (User object: %s.) %s is required in the group, so it starts a new GroupMatch.%n", foundGroupMatchContainer.lastMatch(), foundGroupMatchContainer.group.userObject(), elementDescription);
                    foundGroupMatchContainer.addMatch(commandSpec.commandLine);
                    this.groupMatchContainer.findOrCreateMatchingGroup(argSpec, commandSpec.commandLine);
                }
            }

            private void afterMatchingGroupElement(ArgSpec argSpec, int position) {
//                ArgGroupSpec group = argSpec.group();
//                if (group == null || isInitializingDefaultValues) { return; }
//                GroupMatchContainer groupMatchContainer = this.groupMatchContainer.findOrCreateMatchingGroup(argSpec, commandSpec.commandLine);
//                promotePartiallyMatchedGroupToMatched(group, groupMatchContainer, true);
            }

            private void promotePartiallyMatchedGroupToMatched(ArgGroupSpec group, GroupMatchContainer groupMatchContainer, boolean allRequired) {
                if (!groupMatchContainer.matchedFully(allRequired)) { return; }

                // FIXME: before promoting the child group, check to see if the parent is matched, given the child group

                Tracer tracer = commandSpec.commandLine.tracer;
                if (groupMatchContainer.matchedMaxElements()) {
                    tracer.info("Marking matched group %s as complete: max elements reached. User object: %s%n", groupMatchContainer, groupMatchContainer.group.userObject());
                    groupMatchContainer.complete(commandSpec.commandLine());
                }
            }
        }

        static class GroupValidationResult {
            static final GroupValidationResult SUCCESS_PRESENT = new GroupValidationResult(Type.SUCCESS_PRESENT);
            static final GroupValidationResult SUCCESS_ABSENT = new GroupValidationResult(Type.SUCCESS_ABSENT);
            enum Type {
                SUCCESS_PRESENT, SUCCESS_ABSENT,
                FAILURE_PRESENT, FAILURE_ABSENT, FAILURE_PARTIAL;
            }
            Type type;
            ParameterException exception;

            GroupValidationResult(Type type) { this.type = type; }
            GroupValidationResult(Type type, ParameterException exception) {
                this.type = type;
                this.exception = exception;
            }
            static GroupValidationResult extractBlockingFailure(List<GroupValidationResult> set) {
                for (GroupValidationResult element : set) {
                    if (element.blockingFailure()) { return element; }
                }
                return null;
            }
            /** FAILURE_PRESENT or FAILURE_PARTIAL */
            boolean blockingFailure() { return type == Type.FAILURE_PRESENT || type == Type.FAILURE_PARTIAL; }
            boolean present()         { return type == Type.SUCCESS_PRESENT /*|| this == Type.FAILURE_PRESENT*/; }
            boolean success()         { return type == Type.SUCCESS_ABSENT  || type == Type.SUCCESS_PRESENT; }
            public String toString()  { return type + (exception == null ? "" : ": " + exception.getMessage());}
        }

        /** Provides information about an {@link ArgGroup} that was matched on the command line.
         * <p>
         * The {@code ParseResult} may have more than one {@code GroupMatchContainer} for an {@code ArgGroupSpec}, when the
         * group was matched more often than its maximum {@linkplain ArgGroup#multiplicity() multiplicity}.
         * This is not necessarily a problem: the parser will add a match to the parent matched group
         * until the maximum multiplicity of the parent group is exceeded, in which case parser will add a match to the parent's parent group, etc.
         * </p><p>
         * Ultimately, as long as the {@link ParseResult#getGroupMatches()} method does not return more than one match, the maximum number of elements is not exceeded.
         * </p>
         * @since 4.0 */
        public static class GroupMatchContainer {
            private final ArgGroupSpec group;
            private GroupMatchContainer parentContainer;
            private List<ArgGroupSpec> unmatchedSubgroups = new ArrayList<ArgGroupSpec>();
            private List<GroupMatch> matches = new ArrayList<GroupMatch>();
            private GroupValidationResult validationResult;

            GroupMatchContainer(ArgGroupSpec group, CommandLine cmd) { this.group = group; addMatch(cmd);}

            /** Returns the {@code ArgGroupSpec} whose matches are captured in this {@code GroupMatchContainer}. */
            public ArgGroupSpec group() { return group; }
//            /** Returns the {@code GroupMatchContainer} of the parent {@code ArgGroupSpec}, or {@code null} if this group has no parent. */
//            public GroupMatchContainer parentContainer() { return parentContainer; }
            /** Returns the list of {@code GroupMatch} instances: {@code ArgGroupSpec}s with a multiplicity greater than one may be matched multiple times. */
            public List<GroupMatch> matches() { return Collections.unmodifiableList(matches); }

            void addMatch(CommandLine commandLine) {
                Tracer tracer = commandLine == null ? new Tracer() : commandLine.tracer;
                if (group != null && isMaxMultiplicityReached()) {
                    tracer.info("Completing GroupMatchContainer %s: max multiplicity is reached.%n", this);
                    complete(commandLine);
                } else {
                    if (group != null) {
                        tracer.info("Adding match to GroupMatchContainer %s (group=%s %s).%n", this, group == null ? "?" : group.id(), group == null ? "ROOT" : group.synopsis());
                    }
                    matches.add(new GroupMatch(this));
                    if (group == null) { return; }
                }
                group.initUserObject(commandLine);
            }
            void complete(CommandLine commandLine) {
                if (parentContainer == null) {
                    addMatch(commandLine); // we have no choice but to potentially exceed the max multiplicity of this group...
                } else {
                    parentContainer.addMatch(commandLine);
                }
            }
            /** Returns the "active" multiple of this GroupMatchContainer. */
            GroupMatch lastMatch()   { return matches.get(matches.size() - 1); }
            /** Returns {@code true} if no more {@code MatchedGroupMultiples} can be added to this {@code GroupMatchContainer}. Each multiple may be a complete or an incomplete match.*/
            boolean isMaxMultiplicityReached() { return matches.size() >= group.multiplicity.max; }
            /** Returns {@code true} if this {@code GroupMatchContainer} has at least the minimum number of {@code MatchedGroupMultiples}. Each multiple may be a complete or an incomplete match. */
            boolean isMinMultiplicityReached() { return matches.size() >= group.multiplicity.min; }

            /** Returns {@code true} if the minimum number of multiples has been matched for the multiplicity of this group,
             * and each multiple has matched at least the {@linkplain GroupMatch#matchedMinElements() minimum number of elements}.*/
            boolean matchedMinElements() { return matchedFully(false); }
            /** Returns {@code true} if the maximum number of multiples has been matched for the multiplicity of this group,
             * and the last multiple has {@linkplain GroupMatch#matchedMaxElements() matched the maximum number of elements},
             * while all other multiples have matched at least the {@linkplain GroupMatch#matchedMinElements() minimum number of elements}.*/
            boolean matchedMaxElements() { return matchedFully(true); }
            private boolean matchedFully(boolean allRequired) {
                for (GroupMatch multiple : matches) {
                    boolean actuallyAllRequired = allRequired && multiple == lastMatch();
                    if (!multiple.matchedFully(actuallyAllRequired)) { return false; }
                }
                return allRequired ? isMaxMultiplicityReached() : isMinMultiplicityReached();
            }

            private GroupMatchContainer findOrCreateMatchingGroup(ArgSpec argSpec, CommandLine commandLine) {
                ArgGroupSpec searchGroup = Assert.notNull(argSpec.group(), "group for " + argSpec);
                GroupMatchContainer container = this;
                if (searchGroup == container.group()) { return container; }
                List<ArgGroupSpec> keys = new ArrayList<ArgGroupSpec>();
                while (searchGroup != null) {
                    keys.add(searchGroup);
                    searchGroup = searchGroup.parentGroup();
                }
                Collections.reverse(keys);
                for (ArgGroupSpec key : keys) {
                    GroupMatchContainer sub = container.lastMatch().matchedSubgroups().get(key);
                    if (sub == null) {
                        sub = createGroupMatchContainer(key, container, commandLine);
                    }
                    container = sub;
                }
                return container;
            }
            private GroupMatchContainer createGroupMatchContainer(ArgGroupSpec group, GroupMatchContainer parent, CommandLine commandLine) {
                GroupMatchContainer result = new GroupMatchContainer(group, commandLine);
                result.parentContainer = parent;
                parent.lastMatch().matchedSubgroups.put(group, result);
                return result;
            }
            GroupMatchContainer trim() {
                for (Iterator<GroupMatch> iter = matches.iterator(); iter.hasNext(); ) {
                    GroupMatch multiple = iter.next();
                    if (multiple.isEmpty()) { iter.remove(); }
                    for (GroupMatchContainer sub : multiple.matchedSubgroups.values()) { sub.trim(); }
                }
                return this;
            }

            List<GroupMatchContainer> findMatchContainers(ArgGroupSpec group, List<GroupMatchContainer> result) {
                if (this.group == group) { result.add(this); return result; }
                for (GroupMatch multiple : matches()) {
                    for (GroupMatchContainer mg : multiple.matchedSubgroups.values()) {
                        mg.findMatchContainers(group, result);
                    }
                }
                return result;
            }
            GroupMatchContainer findLastMatchContainer(ArgGroupSpec group) {
                List<GroupMatchContainer> all = findMatchContainers(group, new ArrayList<GroupMatchContainer>());
                return all.isEmpty() ? null : all.get(all.size() - 1);
            }

            @Override public String toString() {
                return toString(new StringBuilder()).toString();
            }

            private StringBuilder toString(StringBuilder result) {
                String prefix = result.length() == 0 ? "={" : "";
                String suffix = result.length() == 0 ? "}" : "";
                if (group != null && result.length() == 0) {
                    result.append(group.synopsis());
                }
                result.append(prefix);
                String infix = "";
                for (GroupMatch occurrence : matches) {
                    result.append(infix);
                    occurrence.toString(result);
                    infix = " ";
                }
                return result.append(suffix);

            }
            void updateUnmatchedGroups(final ArgGroupSpec group) {
                Assert.assertTrue(Assert.equals(group(), group.parentGroup()), new IHelpSectionRenderer() {public String render(Help h) {
                    return "Internal error: expected " + group.parentGroup() + " (the parent of " + group + "), but was " + group(); }});

                List<GroupMatchContainer> groupMatchContainers = findMatchContainers(group, new ArrayList<GroupMatchContainer>());
                if (groupMatchContainers.isEmpty()) {
                    this.unmatchedSubgroups.add(group);
                }
                for (GroupMatchContainer groupMatchContainer : groupMatchContainers) {
                    for (ArgGroupSpec subGroup : group.subgroups()) {
                        groupMatchContainer.updateUnmatchedGroups(subGroup);
                    }
                }
            }
            void validate(CommandLine commandLine) {
                // first, validate the top-level GroupMatchContainer:
                // Even if cmd has more than one group that each have matches,
                // we should have a *single* top-level GroupMatch, with a subgroup for each GroupMatchContainer.
                // If we have more than one top-level GroupMatch, it means that the parser
                // was forced to "spill over" matches into additional GroupMatches because max multiplicity was exceeded.
                if (group() == null && matches.size() > 1) {
                    failGroupMultiplicityExceeded(matches, commandLine);
                }

                validationResult = matches.isEmpty() ? GroupValidationResult.SUCCESS_ABSENT : GroupValidationResult.SUCCESS_PRESENT;
                for (ArgGroupSpec missing : unmatchedSubgroups) {
                    if (missing.validate() && missing.multiplicity().min > 0) {
//                        if (missing.subgroups().isEmpty()) {
                            int presentCount = 0;
                            boolean haveMissing = true;
                            boolean someButNotAllSpecified = false;
                            String exclusiveElements = missing.synopsis();
                            String missingElements = missing.synopsis(); //ArgSpec.describe(missing.requiredArgs());
                            validationResult = missing.validate(commandLine, presentCount, haveMissing, someButNotAllSpecified, exclusiveElements, missingElements, missingElements);
//                        } else {
//                            validationResult = new ParseResult.GroupValidationResult(
//                                    ParseResult.GroupValidationResult.Type.FAILURE_ABSENT,
//                                    new MissingParameterException(commandLine, missing.args(),
//                                            "Error: Group: " + missing.synopsis() + " must be specified " + missing.multiplicity().min + " times but was missing")
//                            );
//                        }
                    }
                }
                validateGroupMultiplicity(commandLine);
                if (validationResult.blockingFailure()) {
                    commandLine.interpreter.maybeThrow(validationResult.exception); // composite parent validations cannot succeed anyway
                }
                for (GroupMatch match : matches()) {
                    match.validate(commandLine);
                    if (match.validationResult.blockingFailure()) {
                        validationResult = match.validationResult; // potentially overwrites existing blocking failure with subgroup's!
                        break;
                    }
                }
                if (validationResult.blockingFailure()) {
                    commandLine.interpreter.maybeThrow(validationResult.exception); // composite parent validations cannot succeed anyway
                }
                if (group() == null) {
                    if (!validationResult.success()) {
                        commandLine.interpreter.maybeThrow(validationResult.exception);
                    }
                }
            }

            private void failGroupMultiplicityExceeded(List<ParseResult.GroupMatch> groupMatches, CommandLine commandLine) {
                Map<ArgGroupSpec, List<List<ParseResult.GroupMatch>>> matchesPerGroup = new LinkedHashMap<ArgGroupSpec, List<List<GroupMatch>>>();
                String msg = "";
                for (ParseResult.GroupMatch match : groupMatches) {
                    if (msg.length() > 0) { msg += " and "; }
                    msg += match.toString();
                    Map<ArgGroupSpec, GroupMatchContainer> subgroups = match.matchedSubgroups();
                    for (ArgGroupSpec group : subgroups.keySet()) {
                        addValueToListInMap(matchesPerGroup, group, subgroups.get(group).matches());
                    }
                }
                if (!simplifyErrorMessageForSingleGroup(matchesPerGroup, commandLine)) {
                    commandLine.interpreter.maybeThrow(new MaxValuesExceededException(commandLine, "Error: expected only one match but got " + msg));
                }
            }

            private boolean simplifyErrorMessageForSingleGroup(Map<ArgGroupSpec, List<List<ParseResult.GroupMatch>>> matchesPerGroup, CommandLine commandLine) {
                for (ArgGroupSpec group : matchesPerGroup.keySet()) {
                    List<ParseResult.GroupMatch> flat = flatList(matchesPerGroup.get(group));
                    Set<ArgSpec> matchedArgs = new LinkedHashSet<ArgSpec>();
                    for (ParseResult.GroupMatch match : flat) {
                        if (!match.matchedSubgroups().isEmpty()) { return false; }
                        matchedArgs.addAll(match.matchedValues.keySet());
                    }
                    ParseResult.GroupValidationResult validationResult = group.validateArgs(commandLine, matchedArgs);
                    if (validationResult.exception != null) {
                        commandLine.interpreter.maybeThrow(validationResult.exception); // there may be multiple failures, just throw on the first one for now
                        return true;
                    }
                }
                return false;
            }

            private void validateGroupMultiplicity(CommandLine commandLine) {
                if (group == null || !group.validate()) { return; }
                int matchCount = matches().size();
                // note: matchCount == 0 if only subgroup(s) are matched for a group without args (subgroups-only) // TODO really?
                boolean checkMinimum = matchCount > 0 || !group.args().isEmpty();
                if (checkMinimum && matchCount < group.multiplicity().min) {
                    if (validationResult.success()) {
                        validationResult = new ParseResult.GroupValidationResult(
                                matchCount == 0 ? ParseResult.GroupValidationResult.Type.FAILURE_ABSENT : ParseResult.GroupValidationResult.Type.FAILURE_PARTIAL,
                                new MissingParameterException(commandLine, group.args(),
                                        "Error: Group: " + group.synopsis() + " must be specified " + group.multiplicity().min + " times but was matched " + matchCount + " times")
                        );
                    }
                } else if (matchCount > group.multiplicity().max) {
                    if (!validationResult.blockingFailure()) {
                        validationResult = new ParseResult.GroupValidationResult(
                                ParseResult.GroupValidationResult.Type.FAILURE_PRESENT,
                                new MaxValuesExceededException(commandLine,
                                        "Error: Group: " + group.synopsis() + " can only be specified " + group.multiplicity().max + " times but was matched " + matchCount + " times.")
                        );
                    }
                }
            }
        }

        /** A group's {@linkplain ArgGroup#multiplicity() multiplicity} specifies how many matches of a group may
         * appear on the command line. This class models a single "match".
         * For example, this group: {@code (-a -b) (-a -b)} requires two matches of its arguments to fully match.
         * @since 4.0
         */
        public static class GroupMatch {
            int position;
            final GroupMatchContainer container;

            Map<ArgGroupSpec, GroupMatchContainer> matchedSubgroups = new LinkedHashMap<ArgGroupSpec, GroupMatchContainer>(2); // preserve order: used in toString()
            Map<ArgSpec, List<Object>> matchedValues         = new IdentityHashMap<ArgSpec, List<Object>>(); // identity map for performance
            Map<ArgSpec, List<String>> originalStringValues  = new LinkedHashMap<ArgSpec, List<String>>(); // preserve order: used in toString()
            Map<ArgSpec, Map<Integer, List<Object>>> matchedValuesAtPosition = new IdentityHashMap<ArgSpec, Map<Integer, List<Object>>>();
            private GroupValidationResult validationResult;

            GroupMatch(GroupMatchContainer container) { this.container = container; }

            /** Returns {@code true} if this match has no matched arguments and no matched subgroups. */
            public boolean isEmpty() { return originalStringValues.isEmpty() && matchedSubgroups.isEmpty(); }
            /** Returns the {@code ArgGroupSpec} of the container {@code GroupMatchContainer} of this match. */
            public ArgGroupSpec group() { return container.group; }
            /** Returns the container {@code GroupMatchContainer} of this match. */
            public GroupMatchContainer container() { return container; }
            /** Returns matches for the subgroups, if any. */
            public Map<ArgGroupSpec, GroupMatchContainer> matchedSubgroups() { return Collections.unmodifiableMap(matchedSubgroups); }
            int matchCount(ArgSpec argSpec)                    { return matchedValues.get(argSpec) == null ? 0 : matchedValues.get(argSpec).size(); }
            /** Returns the values matched for the specified argument, converted to the type of the argument. */
            public List<Object> matchedValues(ArgSpec argSpec) { return matchedValues.get(argSpec) == null ? Collections.emptyList() : Collections.unmodifiableList(matchedValues.get(argSpec)); }
            void addOriginalStringValue(ArgSpec argSpec, String value) {
                addValueToListInMap(originalStringValues, argSpec, value);
            }
            void addMatchedValue(ArgSpec argSpec, int matchPosition, Object stronglyTypedValue, Tracer tracer) {
                addValueToListInMap(matchedValues, argSpec, stronglyTypedValue);

                Map<Integer, List<Object>> positionalValues = matchedValuesAtPosition.get(argSpec);
                if (positionalValues == null) {
                    positionalValues = new TreeMap<Integer, List<Object>>();
                    matchedValuesAtPosition.put(argSpec, positionalValues);
                }
                addValueToListInMap(positionalValues, matchPosition, stronglyTypedValue);
            }
            boolean hasMatchedValueAtPosition(ArgSpec arg, int position) { Map<Integer, List<Object>> atPos = matchedValuesAtPosition.get(arg); return atPos != null && atPos.containsKey(position); }

            /** Returns {@code true} if the minimum number of elements have been reached for this match:
             * all required arguments have been matched, and for each subgroup,
             * the {@linkplain GroupMatchContainer#matchedMinElements() minimum number of elements have been matched}.*/
            boolean matchedMinElements() { return matchedFully(false); }
            /** Returns {@code true} if the maximum number of matches has been reached for this match:
             * all arguments (required or not) have been matched, and for each subgroup,
             * the {@linkplain GroupMatchContainer#matchedMaxElements() maximum number of elements have been matched}.*/
            boolean matchedMaxElements() { return matchedFully(true); }
            private boolean matchedFully(boolean allRequired) {
                if (group().exclusive()) { return !matchedValues.isEmpty() || hasFullyMatchedSubgroup(allRequired); }
                for (ArgSpec arg : group().args()) {
                    if (matchedValues.get(arg) == null && (arg.required() || allRequired)) { return false; }
                }
                for (ArgGroupSpec subgroup : group().subgroups()) {
                    GroupMatchContainer groupMatchContainer = matchedSubgroups.get(subgroup);
                    if (groupMatchContainer != null) {
                        if (!groupMatchContainer.matchedFully(allRequired)) { return false; }
                    } else {
                        if (allRequired || subgroup.multiplicity().min > 0) { return false; }
                    }
                }
                return true;
            }
            private boolean hasFullyMatchedSubgroup(boolean allRequired) {
                for (GroupMatchContainer sub : matchedSubgroups.values()) { if (sub.matchedFully(allRequired)) { return true; } }
                return false;
            }
            @Override public String toString() {
                return toString(new StringBuilder()).toString();
            }
            private StringBuilder toString(StringBuilder result) {
                int originalLength = result.length();
                for (ArgSpec arg : originalStringValues.keySet()) {
                    List<String> values = originalStringValues.get(arg);
                    for (String value : values) {
                        if (result.length() != originalLength) { result.append(" "); }
                        result.append(ArgSpec.describe(arg, "=", value));
                    }
                }
                for (GroupMatchContainer sub : matchedSubgroups.values()) {
                    if (result.length() != originalLength) { result.append(" "); }
                    if (originalLength == 0) {
                        result.append(sub.toString()); // include synopsis
                    } else {
                        sub.toString(result); // without synopsis
                    }
                }
                return result;
            }

            void validate(CommandLine commandLine) {
                validationResult = GroupValidationResult.SUCCESS_PRESENT; // we matched _something_ or this object would not exist...
                for (GroupMatchContainer sub : matchedSubgroups.values()) {
                    sub.validate(commandLine);
                    if (sub.validationResult.blockingFailure()) {
                        this.validationResult = sub.validationResult;
                        return;
                    }
                }
                // finally, validate that the combination of matched args and matched subgroups is valid
                if (group() != null) {
                    Set<ArgSpec> intersection = new LinkedHashSet<ArgSpec>(group().args());
                    Set<ArgSpec> missing = new LinkedHashSet<ArgSpec>(group().requiredArgs());
                    Set<ArgSpec> found = new LinkedHashSet<ArgSpec>();
                    found.addAll(matchedValues.keySet());
                    missing.removeAll(matchedValues.keySet());
                    intersection.retainAll(found);

                    String exclusiveElements = ArgSpec.describe(intersection, ", ");
                    String requiredElements = ArgSpec.describe(group().requiredArgs(), ", ");
                    String missingElements = ArgSpec.describe(missing, ", ");

                    Set<ArgGroupSpec> missingSubgroups = new LinkedHashSet<ArgGroupSpec>(group().subgroups());
                    missingSubgroups.removeAll(matchedSubgroups.keySet());
                    for (ArgGroupSpec missingSubgroup : missingSubgroups) {
                        if (missingElements.length() > 0) { missingElements += " and "; }
                        missingElements += missingSubgroup.synopsis();
                    }

                    int requiredSubgroupCount = 0;
                    for (ArgGroupSpec subgroup : group().subgroups()) {
                        if (exclusiveElements.length() > 0) { exclusiveElements += " and "; }
                        exclusiveElements += subgroup.synopsis();
                        if (subgroup.multiplicity().min > 0) {
                            requiredSubgroupCount++;
                            if (requiredElements.length() > 0) { requiredElements += " and "; }
                            requiredElements += subgroup.synopsis();
                        }
                    }
                    int requiredCount = group().requiredArgs().size() + requiredSubgroupCount;
                    int presentCount = matchedValues.size() + matchedSubgroups.size();
                    boolean haveMissing = presentCount < requiredCount;
                    validationResult = group().validate(commandLine, presentCount, haveMissing,
                            presentCount > 0 && haveMissing, exclusiveElements, requiredElements, missingElements);
                }
            }
        }
    }
    static <K, T> void addValueToListInMap(Map<K, List<T>> map, K key, T value) {
        List<T> values = map.get(key);
        if (values == null) { values = new ArrayList<T>(); map.put(key, values); }
        values.add(value);
    }
    static <T> List<T> flatList(Collection<? extends Collection<T>> collection) {
        List<T> result = new ArrayList<T>();
        for (Collection<T> sub : collection) { result.addAll(sub); }
        return result;
    }
    private enum LookBehind { SEPARATE, ATTACHED, ATTACHED_WITH_SEPARATOR;
        public boolean isAttached() { return this != LookBehind.SEPARATE; }
    }
    /**
     * Helper class responsible for processing command line arguments.
     */
    private class Interpreter {
        private final Map<Class<?>, ITypeConverter<?>> converterRegistry = new HashMap<Class<?>, ITypeConverter<?>>();
        private boolean isHelpRequested;
        private int position;
        private int interactiveCount;
        private boolean endOfOptions;
        private ParseResult.Builder parseResultBuilder;

        Interpreter() { registerBuiltInConverters(); }

        private void registerBuiltInConverters() {
            converterRegistry.put(Object.class,        new BuiltIn.StringConverter());
            converterRegistry.put(String.class,        new BuiltIn.StringConverter());
            converterRegistry.put(StringBuilder.class, new BuiltIn.StringBuilderConverter());
            converterRegistry.put(char[].class,        new BuiltIn.CharArrayConverter());
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
            converterRegistry.put(BigDecimal.class,    new BuiltIn.BigDecimalConverter());
            converterRegistry.put(BigInteger.class,    new BuiltIn.BigIntegerConverter());
            converterRegistry.put(Charset.class,       new BuiltIn.CharsetConverter());
            converterRegistry.put(InetAddress.class,   new BuiltIn.InetAddressConverter());
            converterRegistry.put(Pattern.class,       new BuiltIn.PatternConverter());
            converterRegistry.put(UUID.class,          new BuiltIn.UUIDConverter());
            converterRegistry.put(Currency.class,      new BuiltIn.CurrencyConverter());
            converterRegistry.put(TimeZone.class,      new BuiltIn.TimeZoneConverter());
            converterRegistry.put(ByteOrder.class,     new BuiltIn.ByteOrderConverter());
            converterRegistry.put(Class.class,         new BuiltIn.ClassConverter());
            converterRegistry.put(NetworkInterface.class, new BuiltIn.NetworkInterfaceConverter());

            BuiltIn.ISO8601TimeConverter.registerIfAvailable(converterRegistry, tracer);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.sql.Connection", "java.sql.DriverManager","getConnection", String.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.sql.Driver", "java.sql.DriverManager","getDriver", String.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.sql.Timestamp", "java.sql.Timestamp","valueOf", String.class);

            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.Duration", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.Instant", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.LocalDate", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.LocalDateTime", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.LocalTime", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.MonthDay", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.OffsetDateTime", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.OffsetTime", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.Period", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.Year", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.YearMonth", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.ZonedDateTime", "parse", CharSequence.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.ZoneId", "of", String.class);
            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.time.ZoneOffset", "of", String.class);

            BuiltIn.registerIfAvailable(converterRegistry, tracer, "java.nio.file.Path", "java.nio.file.Paths", "get", String.class, String[].class);
        }
        private ParserSpec config() { return commandSpec.parser(); }
        /**
         * Entry point into parsing command line arguments.
         * @param args the command line arguments
         * @return a list with all commands and subcommands initialized by this method
         * @throws ParameterException if the specified command line arguments are invalid
         */
        List<CommandLine> parse(String... args) {
            Assert.notNull(args, "argument array");
            if (tracer.isInfo()) {tracer.info("Picocli version: %s%n", versionString());}
            if (tracer.isInfo()) {tracer.info("Parsing %d command line args %s%n", args.length, Arrays.toString(args));}
            if (tracer.isDebug()){tracer.debug("Parser configuration: %s%n", config());}
            if (tracer.isDebug()){tracer.debug("(ANSI is %s by default: isatty=%s, XTERM=%s, OSTYPE=%s, isWindows=%s, JansiConsoleInstalled=%s, ANSICON=%s, ConEmuANSI=%s, NO_COLOR=%s, CLICOLOR=%s, CLICOLOR_FORCE=%s)%n",
                    Help.Ansi.ansiPossible() ? "enabled" : "disabled", Help.Ansi.isTTY(), System.getenv("XTERM"), System.getenv("OSTYPE"), Help.Ansi.isWindows(), Help.Ansi.isJansiConsoleInstalled(), System.getenv("ANSICON"), System.getenv("ConEmuANSI"), System.getenv("NO_COLOR"), System.getenv("CLICOLOR"), System.getenv("CLICOLOR_FORCE"));}
            List<String> expanded = new ArrayList<String>();
            for (String arg : args) { addOrExpand(arg, expanded, new LinkedHashSet<String>()); }
            Stack<String> arguments = new Stack<String>();
            arguments.addAll(reverseList(expanded));
            List<CommandLine> result = new ArrayList<CommandLine>();
            parse(result, arguments, args, new ArrayList<Object>());
            return result;
        }

        private void addOrExpand(String arg, List<String> arguments, Set<String> visited) {
            if (config().expandAtFiles() && !arg.equals("@") && arg.startsWith("@")) {
                arg = arg.substring(1);
                if (arg.startsWith("@")) {
                    if (tracer.isInfo()) { tracer.info("Not expanding @-escaped argument %s (trimmed leading '@' char)%n", arg); }
                } else {
                    if (tracer.isInfo()) { tracer.info("Expanding argument file @%s%n", arg); }
                    expandArgumentFile(arg, arguments, visited);
                    return;
                }
            }
            arguments.add(arg);
        }
        private void expandArgumentFile(String fileName, List<String> arguments, Set<String> visited) {
            File file = new File(fileName);
            if (!file.canRead()) {
                if (tracer.isInfo()) {tracer.info("File %s does not exist or cannot be read; treating argument literally%n", fileName);}
                arguments.add("@" + fileName);
            } else if (visited.contains(file.getAbsolutePath())) {
                if (tracer.isInfo()) {tracer.info("Already visited file %s; ignoring...%n", file.getAbsolutePath());}
            } else {
                expandValidArgumentFile(fileName, file, arguments, visited);
            }
        }
        private void expandValidArgumentFile(String fileName, File file, List<String> arguments, Set<String> visited) {
            List<String> result = new ArrayList<String>();
            LineNumberReader reader = null;
            try {
                visited.add(file.getAbsolutePath());
                reader = new LineNumberReader(new FileReader(file));
                if (commandSpec.parser().useSimplifiedAtFiles()) {
                    String token;
                    while ((token = reader.readLine()) != null) {
                        if (token.length() > 0 && !token.trim().startsWith(String.valueOf(commandSpec.parser().atFileCommentChar()))) {
                            addOrExpand(token, result, visited);
                        }
                    }
                } else {
                    StreamTokenizer tok = new StreamTokenizer(reader);
                    tok.resetSyntax();
                    tok.wordChars(' ', 255);
                    tok.whitespaceChars(0, ' ');
                    tok.quoteChar('"');
                    tok.quoteChar('\'');
                    if (commandSpec.parser().atFileCommentChar() != null) {
                        tok.commentChar(commandSpec.parser().atFileCommentChar());
                    }
                    while (tok.nextToken() != StreamTokenizer.TT_EOF) {
                        addOrExpand(tok.sval, result, visited);
                    }
                }
            } catch (Exception ex) {
                throw new InitializationException("Could not read argument file @" + fileName, ex);
            } finally {
                if (reader != null) { try {reader.close();} catch (Exception ignored) {} }
            }
            if (tracer.isInfo()) {tracer.info("Expanded file @%s to arguments %s%n", fileName, result);}
            arguments.addAll(result);
        }
        private void clear() {
            position = 0;
            endOfOptions = false;
            isHelpRequested = false;
            parseResultBuilder = ParseResult.builder(getCommandSpec());
            for (OptionSpec option : getCommandSpec().options())                           { clear(option); }
            for (PositionalParamSpec positional : getCommandSpec().positionalParameters()) { clear(positional); }
        }
        private void clear(ArgSpec argSpec) {
            argSpec.resetStringValues();
            argSpec.resetOriginalStringValues();
            argSpec.typedValues.clear();
            argSpec.typedValueAtPosition.clear();
            if (argSpec.group() == null) { argSpec.applyInitialValue(tracer); } // groups do their own initialization
        }

        void maybeThrow(PicocliException ex) throws PicocliException {
            if (commandSpec.parser().collectErrors) {
                parseResultBuilder.addError(ex);
            } else {
                throw ex;
            }
        }

        private void parse(List<CommandLine> parsedCommands, Stack<String> argumentStack, String[] originalArgs, List<Object> nowProcessing) {
            clear(); // first reset any state in case this CommandLine instance is being reused
            if (tracer.isDebug()) {
                tracer.debug("Initializing %s: %d options, %d positional parameters, %d required, %d groups, %d subcommands.%n",
                        commandSpec.toString(), new HashSet<ArgSpec>(commandSpec.optionsMap().values()).size(),
                        commandSpec.positionalParameters().size(), commandSpec.requiredArgs().size(),
                        commandSpec.argGroups().size(), commandSpec.subcommands().size());
            }
            parsedCommands.add(CommandLine.this);
            List<ArgSpec> required = new ArrayList<ArgSpec>(commandSpec.requiredArgs());
            Set<ArgSpec> initialized = new LinkedHashSet<ArgSpec>();
            Collections.sort(required, new PositionalParametersSorter());
            boolean continueOnError = commandSpec.parser().collectErrors();
            do {
                int stackSize = argumentStack.size();
                try {
                    applyDefaultValues(required);
                    processArguments(parsedCommands, argumentStack, required, initialized, originalArgs, nowProcessing);
                } catch (ParameterException ex) {
                    maybeThrow(ex);
                } catch (Exception ex) {
                    int offendingArgIndex = originalArgs.length - argumentStack.size() - 1;
                    String arg = offendingArgIndex >= 0 && offendingArgIndex < originalArgs.length ? originalArgs[offendingArgIndex] : "?";
                    maybeThrow(ParameterException.create(CommandLine.this, ex, arg, offendingArgIndex, originalArgs));
                }
                if (continueOnError && stackSize == argumentStack.size() && stackSize > 0) {
                    parseResultBuilder.unmatched.add(argumentStack.pop());
                }
            } while (!argumentStack.isEmpty() && continueOnError);

            if (!isAnyHelpRequested()) {
                validateConstraints(argumentStack, required, initialized);
            }
        }

        private void validateConstraints(Stack<String> argumentStack, List<ArgSpec> required, Set<ArgSpec> matched) {
            if (!required.isEmpty()) {
                for (ArgSpec missing : required) {
                    Assert.assertTrue(missing.group() == null, "Arguments in a group are not necessarily required for the command");
                    if (missing.isOption()) {
                        maybeThrow(MissingParameterException.create(CommandLine.this, required, config().separator()));
                    } else {
                        assertNoMissingParameters(missing, missing.arity(), argumentStack);
                    }
                }
            }
            if (!parseResultBuilder.unmatched.isEmpty()) {
                String[] unmatched = parseResultBuilder.unmatched.toArray(new String[0]);
                for (UnmatchedArgsBinding unmatchedArgsBinding : getCommandSpec().unmatchedArgsBindings()) {
                    unmatchedArgsBinding.addAll(unmatched.clone());
                }
                if (!isUnmatchedArgumentsAllowed()) { maybeThrow(new UnmatchedArgumentException(CommandLine.this, Collections.unmodifiableList(parseResultBuilder.unmatched))); }
                if (tracer.isInfo()) { tracer.info("Unmatched arguments: %s%n", parseResultBuilder.unmatched); }
            }
            ParseResult pr = parseResultBuilder.build();
            pr.validateGroups();
        }

        private void applyDefaultValues(List<ArgSpec> required) throws Exception {
            parseResultBuilder.isInitializingDefaultValues = true;
            for (ArgSpec arg : commandSpec.args()) {
                if (arg.group() == null) {
                    if (applyDefault(commandSpec.defaultValueProvider(), arg)) { required.remove(arg); }
                }
            }
            parseResultBuilder.isInitializingDefaultValues = false;
        }

        private boolean applyDefault(IDefaultValueProvider defaultValueProvider, ArgSpec arg) throws Exception {

            // Default value provider return value is only used if provider exists and if value
            // is not null otherwise the original default or initial value are used
            String fromProvider = defaultValueProvider == null ? null : defaultValueProvider.defaultValue(arg);
            String defaultValue = fromProvider == null ? arg.defaultValue() : fromProvider;

            if (defaultValue != null) {
                if (tracer.isDebug()) {tracer.debug("Applying defaultValue (%s) to %s%n", defaultValue, arg);}
                Range arity = arg.arity().min(Math.max(1, arg.arity().min));
                applyOption(arg, LookBehind.SEPARATE, arity, stack(defaultValue), new HashSet<ArgSpec>(), arg.toString);
            }
            return defaultValue != null;
        }

        private Stack<String> stack(String value) {Stack<String> result = new Stack<String>(); result.push(value); return result;}

        private void processArguments(List<CommandLine> parsedCommands,
                                      Stack<String> args,
                                      Collection<ArgSpec> required,
                                      Set<ArgSpec> initialized,
                                      String[] originalArgs,
                                      List<Object> nowProcessing) throws Exception {
            // arg must be one of:
            // 1. the "--" double dash separating options from positional arguments
            // 1. a stand-alone flag, like "-v" or "--verbose": no value required, must map to boolean or Boolean field
            // 2. a short option followed by an argument, like "-f file" or "-ffile": may map to any type of field
            // 3. a long option followed by an argument, like "-file out.txt" or "-file=out.txt"
            // 3. one or more remaining arguments without any associated options. Must be the last in the list.
            // 4. a combination of stand-alone options, like "-vxr". Equivalent to "-v -x -r", "-v true -x true -r true"
            // 5. a combination of stand-alone options and one option with an argument, like "-vxrffile"

            parseResultBuilder.originalArgs(originalArgs);
            parseResultBuilder.nowProcessing = nowProcessing;
            String separator = config().separator();
            while (!args.isEmpty()) {
                if (endOfOptions) {
                    processRemainderAsPositionalParameters(required, initialized, args);
                    return;
                }
                String arg = args.pop();
                if (tracer.isDebug()) {tracer.debug("Processing argument '%s'. Remainder=%s%n", arg, reverse(copy(args)));}

                // Double-dash separates options from positional arguments.
                // If found, then interpret the remaining args as positional parameters.
                if (commandSpec.parser.endOfOptionsDelimiter().equals(arg)) {
                    tracer.info("Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n");
                    endOfOptions = true;
                    processRemainderAsPositionalParameters(required, initialized, args);
                    return; // we are done
                }

                // if we find another command, we are done with the current command
                if (commandSpec.subcommands().containsKey(arg)) {
                    CommandLine subcommand = commandSpec.subcommands().get(arg);
                    nowProcessing.add(subcommand.commandSpec);
                    updateHelpRequested(subcommand.commandSpec);
                    if (!isAnyHelpRequested() && !required.isEmpty()) { // ensure current command portion is valid
                        throw MissingParameterException.create(CommandLine.this, required, separator);
                    }
                    if (tracer.isDebug()) {tracer.debug("Found subcommand '%s' (%s)%n", arg, subcommand.commandSpec.toString());}
                    subcommand.interpreter.parse(parsedCommands, args, originalArgs, nowProcessing);
                    parseResultBuilder.subcommand(subcommand.interpreter.parseResultBuilder.build());
                    return; // remainder done by the command
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
                    if (commandSpec.optionsMap().containsKey(key) && commandSpec.optionsMap().containsKey(arg)) {
                        tracer.warn("Both '%s' and '%s' are valid option names in %s. Using '%s'...%n", arg, key, getCommandName(), arg);
                    } else if (commandSpec.optionsMap().containsKey(key)) {
                        paramAttachedToOption = true;
                        String optionParam = arg.substring(separatorIndex + separator.length());
                        args.push(optionParam);
                        arg = key;
                        if (tracer.isDebug()) {tracer.debug("Separated '%s' option from '%s' option parameter%n", key, optionParam);}
                    } else {
                        if (tracer.isDebug()) {tracer.debug("'%s' contains separator '%s' but '%s' is not a known option%n", arg, separator, key);}
                    }
                } else {
                    if (tracer.isDebug()) {tracer.debug("'%s' cannot be separated into <option>%s<option-parameter>%n", arg, separator);}
                }
                if (isStandaloneOption(arg)) {
                    processStandaloneOption(required, initialized, arg, args, paramAttachedToOption);
                }
                // Compact (single-letter) options can be grouped with other options or with an argument.
                // only single-letter options can be combined with other options or with an argument
                else if (config().posixClusteredShortOptionsAllowed() && arg.length() > 2 && arg.startsWith("-")) {
                    if (tracer.isDebug()) {tracer.debug("Trying to process '%s' as clustered short options%n", arg, args);}
                    processClusteredShortOptions(required, initialized, arg, args);
                }
                // The argument could not be interpreted as an option: process it as a positional argument
                else {
                    args.push(arg);
                    if (tracer.isDebug()) {tracer.debug("Could not find option '%s', deciding whether to treat as unmatched option or positional parameter...%n", arg);}
                    if (commandSpec.resemblesOption(arg, tracer)) { handleUnmatchedArgument(args); continue; } // #149
                    if (tracer.isDebug()) {tracer.debug("No option named '%s' found. Processing as positional parameter%n", arg);}
                    processPositionalParameter(required, initialized, args);
                }
            }
        }

        private boolean isStandaloneOption(String arg) {
            return commandSpec.optionsMap().containsKey(arg);
        }
        private void handleUnmatchedArgument(Stack<String> args) throws Exception {
            if (!args.isEmpty()) { handleUnmatchedArgument(args.pop()); }
            if (config().stopAtUnmatched()) {
                // addAll would give args in reverse order
                while (!args.isEmpty()) { handleUnmatchedArgument(args.pop()); }
            }
        }
        private void handleUnmatchedArgument(String arg) {
            parseResultBuilder.unmatched.add(arg);
        }

        private void processRemainderAsPositionalParameters(Collection<ArgSpec> required, Set<ArgSpec> initialized, Stack<String> args) throws Exception {
            while (!args.empty()) {
                processPositionalParameter(required, initialized, args);
            }
        }
        private void processPositionalParameter(Collection<ArgSpec> required, Set<ArgSpec> initialized, Stack<String> args) throws Exception {
            if (tracer.isDebug()) {tracer.debug("Processing next arg as a positional parameter. Command-local position=%d. Remainder=%s%n", position, reverse(copy(args)));}
            if (config().stopAtPositional()) {
                if (!endOfOptions && tracer.isDebug()) {tracer.debug("Parser was configured with stopAtPositional=true, treating remaining arguments as positional parameters.%n");}
                endOfOptions = true;
            }
            int originalInteractiveCount = this.interactiveCount;
            int consumedByGroup = 0;
            int argsConsumed = 0;
            int interactiveConsumed = 0;
            int originalNowProcessingSize = parseResultBuilder.nowProcessing.size();
            Map<PositionalParamSpec, Integer> newPositions = new IdentityHashMap<PositionalParamSpec, Integer>();
            for (PositionalParamSpec positionalParam : commandSpec.positionalParameters()) {
                Range indexRange = positionalParam.index();
                int localPosition = getPosition(positionalParam);
                if (positionalParam.group() != null) { // does the positionalParam's index range contain the current position in the currently matching group
                    GroupMatchContainer groupMatchContainer = parseResultBuilder.groupMatchContainer.findOrCreateMatchingGroup(positionalParam, commandSpec.commandLine());
                    if (!indexRange.contains(localPosition) || (groupMatchContainer != null && groupMatchContainer.lastMatch().hasMatchedValueAtPosition(positionalParam, localPosition))) {
                        continue;
                    }
                } else {
                    if (!indexRange.contains(localPosition) || positionalParam.typedValueAtPosition.get(localPosition) != null) {
                        continue;
                    }
                }
                Stack<String> argsCopy = copy(args);
                Range arity = positionalParam.arity();
                if (tracer.isDebug()) {tracer.debug("Position %s is in index range %s. Trying to assign args to %s, arity=%s%n", positionDesc(positionalParam), indexRange, positionalParam, arity);}
                if (!assertNoMissingParameters(positionalParam, arity, argsCopy)) { break; } // #389 collectErrors parsing
                int originalSize = argsCopy.size();
                int actuallyConsumed = applyOption(positionalParam, LookBehind.SEPARATE, arity, argsCopy, initialized, "args[" + indexRange + "] at position " + localPosition);
                int count = originalSize - argsCopy.size();
                if (count > 0 || actuallyConsumed > 0) {
                    required.remove(positionalParam);
                    interactiveConsumed = this.interactiveCount - originalInteractiveCount;
                }
                if (positionalParam.group() == null) { // don't update the command-level position for group args
                    argsConsumed = Math.max(argsConsumed, count);
                } else {
                    newPositions.put(positionalParam, localPosition + count);
                    consumedByGroup = Math.max(consumedByGroup, count);
                }
                while (parseResultBuilder.nowProcessing.size() > originalNowProcessingSize + count) {
                    parseResultBuilder.nowProcessing.remove(parseResultBuilder.nowProcessing.size() - 1);
                }
            }
            // remove processed args from the stack
            int maxConsumed = Math.max(consumedByGroup, argsConsumed);
            for (int i = 0; i < maxConsumed; i++) { args.pop(); }
            position += argsConsumed + interactiveConsumed;
            if (tracer.isDebug()) {tracer.debug("Consumed %d arguments and %d interactive values, moving command-local position to index %d.%n", argsConsumed, interactiveConsumed, position);}
            for (PositionalParamSpec positional : newPositions.keySet()) {
                GroupMatchContainer inProgress = parseResultBuilder.groupMatchContainer.findOrCreateMatchingGroup(positional, commandSpec.commandLine());
                if (inProgress != null) {
                    inProgress.lastMatch().position = newPositions.get(positional);
                    if (tracer.isDebug()) {tracer.debug("Updated group position to %s for group %s.%n", inProgress.lastMatch().position, inProgress);}
                }
            }
            if (consumedByGroup == 0 && argsConsumed == 0 && interactiveConsumed == 0 && !args.isEmpty()) {
                handleUnmatchedArgument(args);
            }
        }

        private void processStandaloneOption(Collection<ArgSpec> required,
                                             Set<ArgSpec> initialized,
                                             String arg,
                                             Stack<String> args,
                                             boolean paramAttachedToKey) throws Exception {
            ArgSpec argSpec = commandSpec.optionsMap().get(arg);
            required.remove(argSpec);
            Range arity = argSpec.arity();
            if (paramAttachedToKey) {
                arity = arity.min(Math.max(1, arity.min)); // if key=value, minimum arity is at least 1
            }
            LookBehind lookBehind = paramAttachedToKey ? LookBehind.ATTACHED_WITH_SEPARATOR : LookBehind.SEPARATE;
            if (tracer.isDebug()) {tracer.debug("Found option named '%s': %s, arity=%s%n", arg, argSpec, arity);}
            parseResultBuilder.nowProcessing.add(argSpec);
            applyOption(argSpec, lookBehind, arity, args, initialized, "option " + arg);
        }

        private void processClusteredShortOptions(Collection<ArgSpec> required,
                                                  Set<ArgSpec> initialized,
                                                  String arg,
                                                  Stack<String> args) throws Exception {
            String prefix = arg.substring(0, 1);
            String cluster = arg.substring(1);
            boolean paramAttachedToOption = true;
            boolean first = true;
            do {
                if (cluster.length() > 0 && commandSpec.posixOptionsMap().containsKey(cluster.charAt(0))) {
                    ArgSpec argSpec = commandSpec.posixOptionsMap().get(cluster.charAt(0));
                    Range arity = argSpec.arity();
                    String argDescription = "option " + prefix + cluster.charAt(0);
                    if (tracer.isDebug()) {tracer.debug("Found option '%s%s' in %s: %s, arity=%s%n", prefix, cluster.charAt(0), arg,
                            argSpec, arity);}
                    required.remove(argSpec);
                    cluster = cluster.substring(1);
                    paramAttachedToOption = cluster.length() > 0;
                    LookBehind lookBehind = paramAttachedToOption ? LookBehind.ATTACHED : LookBehind.SEPARATE;
                    if (cluster.startsWith(config().separator())) {// attached with separator, like -f=FILE or -v=true
                        lookBehind = LookBehind.ATTACHED_WITH_SEPARATOR;
                        cluster = cluster.substring(config().separator().length());
                        arity = arity.min(Math.max(1, arity.min)); // if key=value, minimum arity is at least 1
                    }
                    if (arity.min > 0 && !empty(cluster)) {
                        if (tracer.isDebug()) {tracer.debug("Trying to process '%s' as option parameter%n", cluster);}
                    }
                    // arity may be >= 1, or
                    // arity <= 0 && !cluster.startsWith(separator)
                    // e.g., boolean @Option("-v", arity=0, varargs=true); arg "-rvTRUE", remainder cluster="TRUE"
                    if (!empty(cluster)) {
                        args.push(cluster); // interpret remainder as option parameter (CAUTION: may be empty string!)
                    }
                    if (first) {
                        parseResultBuilder.nowProcessing.add(argSpec);
                        first = false;
                    } else {
                        parseResultBuilder.nowProcessing.set(parseResultBuilder.nowProcessing.size() - 1, argSpec); // replace
                    }
                    int argCount = args.size();
                    int consumed = applyOption(argSpec, lookBehind, arity, args, initialized, argDescription);
                    // if cluster was consumed as a parameter or if this field was the last in the cluster we're done; otherwise continue do-while loop
                    if (empty(cluster) || args.isEmpty() || args.size() < argCount) {
                        return;
                    }
                    cluster = args.pop();
                } else { // cluster is empty || cluster.charAt(0) is not a short option key
                    if (cluster.length() == 0) { // we finished parsing a group of short options like -rxv
                        return; // return normally and parse the next arg
                    }
                    // We get here when the remainder of the cluster group is neither an option,
                    // nor a parameter that the last option could consume.
                    if (arg.endsWith(cluster)) {
                        args.push(paramAttachedToOption ? prefix + cluster : cluster);
                        if (args.peek().equals(arg)) { // #149 be consistent between unmatched short and long options
                            if (tracer.isDebug()) {tracer.debug("Could not match any short options in %s, deciding whether to treat as unmatched option or positional parameter...%n", arg);}
                            if (commandSpec.resemblesOption(arg, tracer)) { handleUnmatchedArgument(args); return; } // #149
                            processPositionalParameter(required, initialized, args);
                            return;
                        }
                        // remainder was part of a clustered group that could not be completely parsed
                        if (tracer.isDebug()) {tracer.debug("No option found for %s in %s%n", cluster, arg);}
                        String tmp = args.pop();
                        tmp = tmp + " (while processing option: '" + arg + "')";
                        args.push(tmp);
                        handleUnmatchedArgument(args);
                    } else {
                        args.push(cluster);
                        if (tracer.isDebug()) {tracer.debug("%s is not an option parameter for %s%n", cluster, arg);}
                        processPositionalParameter(required, initialized, args);
                    }
                    return;
                }
            } while (true);
        }

        private int applyOption(ArgSpec argSpec,
                                LookBehind lookBehind,
                                Range arity,
                                Stack<String> args,
                                Set<ArgSpec> initialized,
                                String argDescription) throws Exception {
            updateHelpRequested(argSpec);
            boolean consumeOnlyOne = commandSpec.parser().aritySatisfiedByAttachedOptionParam() && lookBehind.isAttached();
            Stack<String> workingStack = args;
            if (consumeOnlyOne) {
                workingStack = args.isEmpty() ? args : stack(args.pop());
            } else {
                if (!assertNoMissingParameters(argSpec, arity, args)) { return 0; } // #389 collectErrors parsing
            }

            parseResultBuilder.beforeMatchingGroupElement(argSpec);

            int result;
            if (argSpec.type().isArray() && !(argSpec.interactive() && argSpec.type() == char[].class)) {
                result = applyValuesToArrayField(argSpec, lookBehind, arity, workingStack, initialized, argDescription);
            } else if (Collection.class.isAssignableFrom(argSpec.type())) {
                result = applyValuesToCollectionField(argSpec, lookBehind, arity, workingStack, initialized, argDescription);
            } else if (Map.class.isAssignableFrom(argSpec.type())) {
                result = applyValuesToMapField(argSpec, lookBehind, arity, workingStack, initialized, argDescription);
            } else {
                result = applyValueToSingleValuedField(argSpec, lookBehind, arity, workingStack, initialized, argDescription);
            }
            if (workingStack != args && !workingStack.isEmpty()) {
                args.push(workingStack.pop());
                Assert.assertTrue(workingStack.isEmpty(), "Working stack should be empty but was " + new ArrayList<String>(workingStack));
            }
            return result;
        }

        private int applyValueToSingleValuedField(ArgSpec argSpec,
                                                  LookBehind lookBehind,
                                                  Range derivedArity,
                                                  Stack<String> args,
                                                  Set<ArgSpec> initialized,
                                                  String argDescription) throws Exception {
            boolean noMoreValues = args.isEmpty();
            String value = args.isEmpty() ? null : trim(args.pop()); // unquote the value
            Range arity = argSpec.arity().isUnspecified ? derivedArity : argSpec.arity(); // #509
            if (arity.max == 0 && !arity.isUnspecified && lookBehind == LookBehind.ATTACHED_WITH_SEPARATOR) { // #509
                throw new MaxValuesExceededException(CommandLine.this, optionDescription("", argSpec, 0) +
                        " should be specified without '" + value + "' parameter");
            }
            int consumed = arity.min; // the number or args we need to consume

            String actualValue = value;
            char[] interactiveValue = null;
            Class<?> cls = argSpec.auxiliaryTypes()[0]; // field may be interface/abstract type, use annotation to get concrete type
            if (arity.min <= 0) { // value may be optional
                boolean optionalValueExists = true; // assume we will use the command line value
                consumed = 1;

                // special logic for booleans: BooleanConverter accepts only "true" or "false".
                if (cls == Boolean.class || cls == Boolean.TYPE) {

                    // boolean option with arity = 0..1 or 0..*: value MAY be a param
                    boolean optionalWithBooleanValue = arity.max > 0 && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value));
                    if (!optionalWithBooleanValue && lookBehind != LookBehind.ATTACHED_WITH_SEPARATOR) { // if attached, try converting the value to boolean (and fail if invalid value)
                        // don't process cmdline arg: it's okay to ignore value if not attached to option
                        if (commandSpec.parser().toggleBooleanFlags()) {
                            Boolean currentValue = (Boolean) argSpec.getValue();
                            actualValue = String.valueOf(currentValue == null || !currentValue); // #147 toggle existing boolean value
                        } else {
                            actualValue = "true";
                        }
                        optionalValueExists = false;
                        consumed = 0;
                    }
                } else { // non-boolean option with optional value #325, #279
                    if (isOption(value)) { // value is not a parameter
                        actualValue = "";
                        optionalValueExists = false;
                        consumed = 0;
                    } else if (value == null) { // stack is empty, option with arity=0..1 was the last arg
                        actualValue = "";
                        optionalValueExists = false;
                        consumed = 0;
                    }
                }
                // if argSpec is interactive, we may need to read the password from the console:
                // - if arity = 0   : ALWAYS read from console
                // - if arity = 0..1: ONLY read from console if user specified a non-option value
                if (argSpec.interactive() && (arity.max == 0 || !optionalValueExists)) {
                    interactiveValue = readPassword(argSpec);
                    consumed = 0;
                }
            }
            if (consumed == 0) { // optional value was not specified on command line, we made something up
                if (value != null) {
                    args.push(value); // we don't consume the command line value
                }
            } else { // value was non-optional or optional value was actually specified
                // process the command line value
                if (!lookBehind.isAttached()) { parseResultBuilder.nowProcessing(argSpec, value); } // update position for Completers
            }
            if (noMoreValues && actualValue == null && interactiveValue == null) {
                return 0;
            }
            Object newValue = interactiveValue;
            String initValueMessage = "Setting %s to *** (masked interactive value) for %4$s%n";
            String overwriteValueMessage = "Overwriting %s value with *** (masked interactive value) for %s%n";
            if (!char[].class.equals(cls) && !char[].class.equals(argSpec.type())) {
                if (interactiveValue != null) {
                    actualValue = new String(interactiveValue);
                }
                ITypeConverter<?> converter = getTypeConverter(cls, argSpec, 0);
                newValue = tryConvert(argSpec, -1, converter, actualValue, cls);
                initValueMessage = "Setting %s to '%3$s' (was '%2$s') for %4$s%n";
                overwriteValueMessage = "Overwriting %s value '%s' with '%s' for %s%n";
            } else {
                if (interactiveValue == null) { // setting command line arg to char[] field
                    newValue = actualValue.toCharArray();
                } else {
                    actualValue = "***"; // mask interactive value
                }
            }
            Object oldValue = argSpec.getValue();
            String traceMessage = initValueMessage;
            if (argSpec.group() == null && initialized.contains(argSpec)) {
                if (!isOverwrittenOptionsAllowed()) {
                    throw new OverwrittenOptionException(CommandLine.this, argSpec, optionDescription("", argSpec, 0) +  " should be specified only once");
                }
                traceMessage = overwriteValueMessage;
            }
            initialized.add(argSpec);

            if (tracer.isInfo()) { tracer.info(traceMessage, argSpec.toString(), String.valueOf(oldValue), String.valueOf(newValue), argDescription); }
            int pos = getPosition(argSpec);
            argSpec.setValue(newValue);
            parseResultBuilder.addOriginalStringValue(argSpec, actualValue);// #279 track empty string value if no command line argument was consumed
            parseResultBuilder.addStringValue(argSpec, actualValue);
            parseResultBuilder.addTypedValues(argSpec, pos, newValue);
            parseResultBuilder.add(argSpec, pos);
            return 1;
        }
        private int applyValuesToMapField(ArgSpec argSpec,
                                          LookBehind lookBehind,
                                          Range arity,
                                          Stack<String> args,
                                          Set<ArgSpec> initialized,
                                          String argDescription) throws Exception {
            Class<?>[] classes = argSpec.auxiliaryTypes();
            if (classes.length < 2) { throw new ParameterException(CommandLine.this, argSpec.toString() + " needs two types (one for the map key, one for the value) but only has " + classes.length + " types configured.",argSpec, null); }
            ITypeConverter<?> keyConverter   = getTypeConverter(classes[0], argSpec, 0);
            ITypeConverter<?> valueConverter = getTypeConverter(classes[1], argSpec, 1);
            @SuppressWarnings("unchecked") Map<Object, Object> map = (Map<Object, Object>) argSpec.getValue();
            if (map == null || (!map.isEmpty() && !initialized.contains(argSpec))) {
                tracer.debug("Initializing binding for %s with empty %s%n", optionDescription("", argSpec, 0), argSpec.type().getSimpleName());
                map = createMap(argSpec.type()); // map class
                argSpec.setValue(map);
            }
            initialized.add(argSpec);
            int originalSize = map.size();
            int pos = getPosition(argSpec);
            consumeMapArguments(argSpec, lookBehind, arity, args, classes, keyConverter, valueConverter, map, argDescription);
            parseResultBuilder.add(argSpec, pos);
            argSpec.setValue(map);
            return map.size() - originalSize;
        }

        private void consumeMapArguments(ArgSpec argSpec,
                                         LookBehind lookBehind,
                                         Range arity,
                                         Stack<String> args,
                                         Class<?>[] classes,
                                         ITypeConverter<?> keyConverter,
                                         ITypeConverter<?> valueConverter,
                                         Map<Object, Object> result,
                                         String argDescription) throws Exception {

            // don't modify Interpreter.position: same position may be consumed by multiple ArgSpec objects
            int currentPosition = getPosition(argSpec);

            // first do the arity.min mandatory parameters
            int initialSize = argSpec.stringValues().size();
            int consumed = consumedCountMap(0, initialSize, argSpec);
            for (int i = 0; consumed < arity.min && !args.isEmpty(); i++) {
                Map<Object, Object> typedValuesAtPosition = new LinkedHashMap<Object, Object>();
                parseResultBuilder.addTypedValues(argSpec, currentPosition++, typedValuesAtPosition);
                assertNoMissingMandatoryParameter(argSpec, args, i, arity);
                consumeOneMapArgument(argSpec, lookBehind, arity, consumed, args.pop(), classes, keyConverter, valueConverter, typedValuesAtPosition, i, argDescription);
                result.putAll(typedValuesAtPosition);
                consumed = consumedCountMap(i + 1, initialSize, argSpec);
                lookBehind = LookBehind.SEPARATE;
            }
            // now process the varargs if any
            for (int i = consumed; consumed < arity.max && !args.isEmpty(); i++) {
                if (!varargCanConsumeNextValue(argSpec, args.peek())) { break; }

                Map<Object, Object> typedValuesAtPosition = new LinkedHashMap<Object, Object>();
                parseResultBuilder.addTypedValues(argSpec, currentPosition++, typedValuesAtPosition);
                if (!canConsumeOneMapArgument(argSpec, arity, consumed, args.peek(), classes, keyConverter, valueConverter, argDescription)) {
                    break; // leave empty map at argSpec.typedValueAtPosition[currentPosition] so we won't try to consume that position again
                }
                consumeOneMapArgument(argSpec, lookBehind, arity, consumed, args.pop(), classes, keyConverter, valueConverter, typedValuesAtPosition, i, argDescription);
                result.putAll(typedValuesAtPosition);
                consumed = consumedCountMap(i + 1, initialSize, argSpec);
                lookBehind = LookBehind.SEPARATE;
            }
        }

        private void consumeOneMapArgument(ArgSpec argSpec,
                                           LookBehind lookBehind,
                                           Range arity, int consumed,
                                           String arg,
                                           Class<?>[] classes,
                                           ITypeConverter<?> keyConverter, ITypeConverter<?> valueConverter,
                                           Map<Object, Object> result,
                                           int index,
                                           String argDescription) throws Exception {
            if (!lookBehind.isAttached()) { parseResultBuilder.nowProcessing(argSpec, arg); }
            String raw = trim(arg);
            String[] values = argSpec.splitValue(raw, commandSpec.parser(), arity, consumed);
            for (String value : values) {
                String[] keyValue = splitKeyValue(argSpec, value);
                Object mapKey =   tryConvert(argSpec, index, keyConverter,   keyValue[0], classes[0]);
                Object mapValue = tryConvert(argSpec, index, valueConverter, keyValue[1], classes[1]);
                result.put(mapKey, mapValue);
                if (tracer.isInfo()) { tracer.info("Putting [%s : %s] in %s<%s, %s> %s for %s%n", String.valueOf(mapKey), String.valueOf(mapValue),
                        result.getClass().getSimpleName(), classes[0].getSimpleName(), classes[1].getSimpleName(), argSpec.toString(), argDescription); }
                parseResultBuilder.addStringValue(argSpec, keyValue[0]);
                parseResultBuilder.addStringValue(argSpec, keyValue[1]);
            }
            parseResultBuilder.addOriginalStringValue(argSpec, raw);
        }

        private boolean canConsumeOneMapArgument(ArgSpec argSpec, Range arity, int consumed,
                                                 String raw, Class<?>[] classes,
                                                 ITypeConverter<?> keyConverter, ITypeConverter<?> valueConverter,
                                                 String argDescription) {
            String[] values = argSpec.splitValue(raw, commandSpec.parser(), arity, consumed);
            try {
                for (String value : values) {
                    String[] keyValue = splitKeyValue(argSpec, value);
                    tryConvert(argSpec, -1, keyConverter, keyValue[0], classes[0]);
                    tryConvert(argSpec, -1, valueConverter, keyValue[1], classes[1]);
                }
                return true;
            } catch (PicocliException ex) {
                tracer.debug("$s cannot be assigned to %s: type conversion fails: %s.%n", raw, argDescription, ex.getMessage());
                return false;
            }
        }

        private String[] splitKeyValue(ArgSpec argSpec, String value) {
            String[] keyValue = ArgSpec.splitRespectingQuotedStrings(value, 2, config(), argSpec, "=");

                if (keyValue.length < 2) {
                String splitRegex = argSpec.splitRegex();
                if (splitRegex.length() == 0) {
                    throw new ParameterException(CommandLine.this, "Value for option " + optionDescription("",
                            argSpec, 0) + " should be in KEY=VALUE format but was " + value, argSpec, value);
                } else {
                    throw new ParameterException(CommandLine.this, "Value for option " + optionDescription("",
                            argSpec, 0) + " should be in KEY=VALUE[" + splitRegex + "KEY=VALUE]... format but was " + value, argSpec, value);
                }
            }
            return keyValue;
        }

        private void assertNoMissingMandatoryParameter(ArgSpec argSpec, Stack<String> args, int i, Range arity) {
            if (!varargCanConsumeNextValue(argSpec, args.peek())) {
                String desc = arity.min > 1 ? (i + 1) + " (of " + arity.min + " mandatory parameters) " : "";
                throw new MissingParameterException(CommandLine.this, argSpec, "Expected parameter " + desc + "for " + optionDescription("", argSpec, -1) + " but found '" + args.peek() + "'");
            }
        }
        private int applyValuesToArrayField(ArgSpec argSpec,
                                            LookBehind lookBehind,
                                            Range arity,
                                            Stack<String> args,
                                            Set<ArgSpec> initialized,
                                            String argDescription) throws Exception {
            Object existing = argSpec.getValue();
            int length = existing == null ? 0 : Array.getLength(existing);
            Class<?> type = argSpec.auxiliaryTypes()[0];
            int pos = getPosition(argSpec);
            List<Object> converted = consumeArguments(argSpec, lookBehind, arity, args, type, argDescription);
            List<Object> newValues = new ArrayList<Object>();
            if (initialized.contains(argSpec)) { // existing values are default values if initialized does NOT contain argsSpec
                for (int i = 0; i < length; i++) {
                    newValues.add(Array.get(existing, i)); // keep non-default values
                }
            }
            initialized.add(argSpec);
            for (Object obj : converted) {
                if (obj instanceof Collection<?>) {
                    newValues.addAll((Collection<?>) obj);
                } else {
                    newValues.add(obj);
                }
            }
            Object array = Array.newInstance(type, newValues.size());
            for (int i = 0; i < newValues.size(); i++) {
                Array.set(array, i, newValues.get(i));
            }
            argSpec.setValue(array);
            parseResultBuilder.add(argSpec, pos);
            return converted.size(); // return how many args were consumed
        }

        @SuppressWarnings("unchecked")
        private int applyValuesToCollectionField(ArgSpec argSpec,
                                                 LookBehind lookBehind,
                                                 Range arity,
                                                 Stack<String> args,
                                                 Set<ArgSpec> initialized,
                                                 String argDescription) throws Exception {
            Collection<Object> collection = (Collection<Object>) argSpec.getValue();
            Class<?> type = argSpec.auxiliaryTypes()[0];
            int pos = getPosition(argSpec);
            List<Object> converted = consumeArguments(argSpec, lookBehind, arity, args, type, argDescription);
            if (collection == null || (!collection.isEmpty() && !initialized.contains(argSpec))) {
                tracer.debug("Initializing binding for %s with empty %s%n", optionDescription("", argSpec, 0), argSpec.type().getSimpleName());
                collection = createCollection(argSpec.type(), type); // collection type, element type
                argSpec.setValue(collection);
            }
            initialized.add(argSpec);
            for (Object element : converted) {
                if (element instanceof Collection<?>) {
                    collection.addAll((Collection<?>) element);
                } else {
                    collection.add(element);
                }
            }
            parseResultBuilder.add(argSpec, pos);
            argSpec.setValue(collection);
            return converted.size();
        }

        private List<Object> consumeArguments(ArgSpec argSpec,
                                              LookBehind lookBehind,
                                              Range arity,
                                              Stack<String> args,
                                              Class<?> type,
                                              String argDescription) throws Exception {
            List<Object> result = new ArrayList<Object>();

            // don't modify Interpreter.position: same position may be consumed by multiple ArgSpec objects
            int currentPosition = getPosition(argSpec);

            // first do the arity.min mandatory parameters
            int initialSize = argSpec.stringValues().size();
            int consumed = consumedCount(0, initialSize, argSpec);
            for (int i = 0; consumed < arity.min && !args.isEmpty(); i++) {
                List<Object> typedValuesAtPosition = new ArrayList<Object>();
                parseResultBuilder.addTypedValues(argSpec, currentPosition++, typedValuesAtPosition);
                assertNoMissingMandatoryParameter(argSpec, args, i, arity);
                consumeOneArgument(argSpec, lookBehind, arity, consumed, args.pop(), type, typedValuesAtPosition, i, argDescription);
                result.addAll(typedValuesAtPosition);
                consumed = consumedCount(i + 1, initialSize, argSpec);
                lookBehind = LookBehind.SEPARATE;
            }
            if (argSpec.interactive() && argSpec.arity().max == 0) {
                consumed = addPasswordToList(argSpec, type, result, consumed, argDescription);
            }
            // now process the varargs if any
            for (int i = consumed; consumed < arity.max && !args.isEmpty(); i++) {
                if (argSpec.interactive() && argSpec.arity().max == 1 && !varargCanConsumeNextValue(argSpec, args.peek())) {
                    // if interactive and arity = 0..1, we consume from command line if possible (if next arg not an option or subcommand)
                    consumed = addPasswordToList(argSpec, type, result, consumed, argDescription);
                } else {
                    if (!varargCanConsumeNextValue(argSpec, args.peek())) { break; }
                    List<Object> typedValuesAtPosition = new ArrayList<Object>();
                    parseResultBuilder.addTypedValues(argSpec, currentPosition++, typedValuesAtPosition);
                    if (!canConsumeOneArgument(argSpec, arity, consumed, args.peek(), type, argDescription)) {
                        break; // leave empty list at argSpec.typedValueAtPosition[currentPosition] so we won't try to consume that position again
                    }
                    consumeOneArgument(argSpec, lookBehind, arity, consumed, args.pop(), type, typedValuesAtPosition, i, argDescription);
                    result.addAll(typedValuesAtPosition);
                    consumed = consumedCount(i + 1, initialSize, argSpec);
                    lookBehind = LookBehind.SEPARATE;
                }
            }
            if (result.isEmpty() && arity.min == 0 && arity.max <= 1 && isBoolean(type)) {
                return Arrays.asList((Object) Boolean.TRUE);
            }
            return result;
        }

        private int consumedCount(int i, int initialSize, ArgSpec arg) {
            return commandSpec.parser().splitFirst() ? arg.stringValues().size() - initialSize : i;
        }

        private int consumedCountMap(int i, int initialSize, ArgSpec arg) {
            return commandSpec.parser().splitFirst() ? (arg.stringValues().size() - initialSize) / 2 : i;
        }

        private int addPasswordToList(ArgSpec argSpec, Class<?> type, List<Object> result, int consumed, String argDescription) {
            char[] password = readPassword(argSpec);
            if (tracer.isInfo()) {
                tracer.info("Adding *** (masked interactive value) to %s for %s%n", argSpec.toString(), argDescription);
            }
            parseResultBuilder.addStringValue(argSpec, "***");
            parseResultBuilder.addOriginalStringValue(argSpec, "***");
            if (!char[].class.equals(argSpec.auxiliaryTypes()[0]) && !char[].class.equals(argSpec.type())) {
                Object value = tryConvert(argSpec, consumed, getTypeConverter(type, argSpec, consumed), new String(password), type);
                result.add(value);
            } else {
                result.add(password);
            }
            consumed++;
            return consumed;
        }
        private int consumeOneArgument(ArgSpec argSpec,
                                       LookBehind lookBehind,
                                       Range arity,
                                       int consumed,
                                       String arg,
                                       Class<?> type,
                                       List<Object> result,
                                       int index,
                                       String argDescription) {
            if (!lookBehind.isAttached()) { parseResultBuilder.nowProcessing(argSpec, arg); }
            String raw = trim(arg);
            String[] values = argSpec.splitValue(raw, commandSpec.parser(), arity, consumed);
            ITypeConverter<?> converter = getTypeConverter(type, argSpec, 0);
            for (int j = 0; j < values.length; j++) {
                Object stronglyTypedValue = tryConvert(argSpec, index, converter, values[j], type);
                result.add(stronglyTypedValue);
                if (tracer.isInfo()) {
                    tracer.info("Adding [%s] to %s for %s%n", String.valueOf(result.get(result.size() - 1)), argSpec.toString(), argDescription);
                }
                parseResultBuilder.addStringValue(argSpec, values[j]);
            }
            parseResultBuilder.addOriginalStringValue(argSpec, raw);
            return ++index;
        }
        private boolean canConsumeOneArgument(ArgSpec argSpec, Range arity, int consumed, String arg, Class<?> type, String argDescription) {
            if (char[].class.equals(argSpec.auxiliaryTypes()[0]) || char[].class.equals(argSpec.type())) { return true; }
            ITypeConverter<?> converter = getTypeConverter(type, argSpec, 0);
            try {
                String[] values = argSpec.splitValue(trim(arg), commandSpec.parser(), arity, consumed);
//                if (!argSpec.acceptsValues(values.length, commandSpec.parser())) {
//                    tracer.debug("$s would split into %s values but %s cannot accept that many values.%n", arg, values.length, argDescription);
//                    return false;
//                }
                for (String value : values) {
                    tryConvert(argSpec, -1, converter, value, type);
                }
                return true;
            } catch (PicocliException ex) {
                tracer.debug("$s cannot be assigned to %s: type conversion fails: %s.%n", arg, argDescription, ex.getMessage());
                return false;
            }
        }

        /** Returns whether the next argument can be assigned to a vararg option/positional parameter.
         * <p>
         * Usually, we stop if we encounter '--', a command, or another option.
         * However, if end-of-options has been reached, positional parameters may consume all remaining arguments. </p>*/
        private boolean varargCanConsumeNextValue(ArgSpec argSpec, String nextValue) {
            if (endOfOptions && argSpec.isPositional()) { return true; }
            boolean isCommand = commandSpec.subcommands().containsKey(nextValue);
            return !isCommand && !isOption(nextValue);
        }

        /** Returns true if the specified arg is "--", a registered option, or potentially a clustered POSIX option.
         * Called when parsing varargs parameters for a multi-value option.
         * When an option is encountered, the remainder should not be interpreted as vararg elements.
         * @param arg the string to determine whether it is an option or not
         * @return true if it is an option, false otherwise
         */
        private boolean isOption(String arg) {
            if (arg == null)      { return false; }
            if ("--".equals(arg)) { return true; }

            // not just arg prefix: we may be in the middle of parsing -xrvfFILE
            if (commandSpec.optionsMap().containsKey(arg)) { // -v or -f or --file (not attached to param or other option)
                return true;
            }
            int separatorIndex = arg.indexOf(config().separator());
            if (separatorIndex > 0) { // -f=FILE or --file==FILE (attached to param via separator)
                if (commandSpec.optionsMap().containsKey(arg.substring(0, separatorIndex))) {
                    return true;
                }
            }
            return (arg.length() > 2 && arg.startsWith("-") && commandSpec.posixOptionsMap().containsKey(arg.charAt(1)));
        }
        private Object tryConvert(ArgSpec argSpec, int index, ITypeConverter<?> converter, String value, Class<?> type)
                throws ParameterException {
            try {
                return converter.convert(value);
            } catch (TypeConversionException ex) {
                String msg = String.format("Invalid value for %s: %s", optionDescription("", argSpec, index), ex.getMessage());
                throw new ParameterException(CommandLine.this, msg, argSpec, value);
            } catch (Exception other) {
                String desc = optionDescription("", argSpec, index);
                String msg = String.format("Invalid value for %s: cannot convert '%s' to %s (%s)", desc, value, type.getSimpleName(), other);
                throw new ParameterException(CommandLine.this, msg, other, argSpec, value);
            }
        }

        private String optionDescription(String prefix, ArgSpec argSpec, int index) {
            String desc = "";
            if (argSpec.isOption()) {
                desc = prefix + "option '" + ((OptionSpec) argSpec).longestName() + "'";
                if (index >= 0) {
                    if (argSpec.arity().max > 1) {
                        desc += " at index " + index;
                    }
                    desc += " (" + argSpec.paramLabel() + ")";
                }
            } else {
                desc = prefix + "positional parameter at index " + ((PositionalParamSpec) argSpec).index() + " (" + argSpec.paramLabel() + ")";
            }
            return desc;
        }

        private boolean isAnyHelpRequested() { return isHelpRequested || parseResultBuilder.versionHelpRequested || parseResultBuilder.usageHelpRequested; }

        private void updateHelpRequested(CommandSpec command) {
            isHelpRequested |= command.helpCommand();
        }
        private void updateHelpRequested(ArgSpec argSpec) {
            if (!parseResultBuilder.isInitializingDefaultValues && argSpec.isOption()) {
                OptionSpec option = (OptionSpec) argSpec;
                isHelpRequested                  |= is(argSpec, "help", option.help());
                parseResultBuilder.versionHelpRequested |= is(argSpec, "versionHelp", option.versionHelp());
                parseResultBuilder.usageHelpRequested   |= is(argSpec, "usageHelp", option.usageHelp());
            }
        }
        private boolean is(ArgSpec p, String attribute, boolean value) {
            if (value) { if (tracer.isInfo()) {tracer.info("%s has '%s' annotation: not validating required fields%n", p.toString(), attribute); }}
            return value;
        }
        @SuppressWarnings("unchecked")
        private Collection<Object> createCollection(Class<?> collectionClass, Class<?> elementType) throws Exception {
            if (EnumSet.class.isAssignableFrom(collectionClass) && Enum.class.isAssignableFrom(elementType)) {
                Object enumSet = EnumSet.noneOf((Class<Enum>) elementType);
                return (Collection<Object>) enumSet;
            }
            // custom Collection implementation class must have default constructor
            return (Collection<Object>) factory.create(collectionClass);
        }
        @SuppressWarnings("unchecked") private Map<Object, Object> createMap(Class<?> mapClass) throws Exception {
            return (Map<Object, Object>) factory.create(mapClass);
        }
        private ITypeConverter<?> getTypeConverter(final Class<?> type, ArgSpec argSpec, int index) {
            if (argSpec.converters().length > index) { return argSpec.converters()[index]; }
            // https://github.com/remkop/picocli/pull/648
            // consider adding ParserSpec.charArraysCanCaptureStrings() to allow non-interactive options to capture multi-char values in a char[] array
            // Note that this will require special logic for char[] types in CommandLine$Interpreter.applyValuesToArrayField;
            // TBD: what to do with multiple values? Append or overwrite?
            if (char[].class.equals(argSpec.type()) && argSpec.interactive()) { return converterRegistry.get(char[].class); }
            if (converterRegistry.containsKey(type)) { return converterRegistry.get(type); }
            if (type.isEnum()) {
                return new ITypeConverter<Object>() {
                    @SuppressWarnings("unchecked")
                    public Object convert(String value) throws Exception {
                        String sensitivity = "case-sensitive";
                        if (commandSpec.parser().caseInsensitiveEnumValuesAllowed()) {
                            String upper = value.toUpperCase();
                            for (Object enumConstant : type.getEnumConstants()) {
                                if (upper.equals(String.valueOf(enumConstant).toUpperCase())) { return enumConstant; }
                            }
                            sensitivity = "case-insensitive";
                        }
                        try { return Enum.valueOf((Class<Enum>) type, value); }
                        catch (Exception ex) {
                            Enum<?>[] constants = ((Class<Enum<?>>) type).getEnumConstants();
                            String[] names = new String[constants.length];
                            for (int i = 0; i < names.length; i++) { names[i] = constants[i].name(); }
                            throw new TypeConversionException(
                                String.format("expected one of %s (%s) but was '%s'", Arrays.asList(names), sensitivity, value)); }
                    }
                };
            }
            throw new MissingTypeConverterException(CommandLine.this, "No TypeConverter registered for " + type.getName() + " of " + argSpec);
        }

        private boolean assertNoMissingParameters(ArgSpec argSpec, Range arity, Stack<String> args) {
            if (argSpec.interactive()) { return true; }
            int available = args.size();
            if (available > 0 && commandSpec.parser().splitFirst() && argSpec.splitRegex().length() > 0) {
                available += argSpec.splitValue(args.peek(), commandSpec.parser(), arity, 0).length - 1;
            }
            if (arity.min > available) {
                if (arity.min == 1) {
                    if (argSpec.isOption()) {
                        maybeThrow(new MissingParameterException(CommandLine.this, argSpec, "Missing required parameter for " +
                                optionDescription("", argSpec, 0)));
                        return false;
                    }
                    Range indexRange = ((PositionalParamSpec) argSpec).index();
                    String sep = "";
                    String names = ": ";
                    int count = 0;
                    List<PositionalParamSpec> positionalParameters = commandSpec.positionalParameters();
                    for (int i = indexRange.min; i < positionalParameters.size(); i++) {
                        if (positionalParameters.get(i).arity().min > 0) {
                            names += sep + positionalParameters.get(i).paramLabel();
                            sep = ", ";
                            count++;
                        }
                    }
                    String msg = "Missing required parameter";
                    Range paramArity = argSpec.arity();
                    if (count > 1 || arity.min - available > 1) {
                        msg += "s";
                    }
                    maybeThrow(new MissingParameterException(CommandLine.this, argSpec, msg + names));
                } else if (args.isEmpty()) {
                    maybeThrow(new MissingParameterException(CommandLine.this, argSpec, optionDescription("", argSpec, 0) +
                            " requires at least " + arity.min + " values, but none were specified."));
                } else {
                    maybeThrow(new MissingParameterException(CommandLine.this, argSpec, optionDescription("", argSpec, 0) +
                            " requires at least " + arity.min + " values, but only " + available + " were specified: " + reverse(args)));
                }
                return false;
            }
            return true;
        }
        private String trim(String value) {
            return unquote(value);
        }

        private String unquote(String value) {
            if (!commandSpec.parser().trimQuotes()) { return value; }
            return value == null
                    ? null
                    : (value.length() > 1 && value.startsWith("\"") && value.endsWith("\""))
                        ? value.substring(1, value.length() - 1)
                        : value;
        }

        char[] readPassword(ArgSpec argSpec) {
            String name = argSpec.isOption() ? ((OptionSpec) argSpec).longestName() : "position " + position;
            String prompt = String.format("Enter value for %s (%s): ", name, str(argSpec.description(), 0));
            if (tracer.isDebug()) {tracer.debug("Reading value for %s from console...%n", name);}
            char[] result = readPassword(prompt);
            if (tracer.isDebug()) {tracer.debug("User entered %d characters for %s.%n", result.length, name);}
            return result;
        }
        char[] readPassword(String prompt) {
            try {
                Object console = System.class.getDeclaredMethod("console").invoke(null);
                Method method = Class.forName("java.io.Console").getDeclaredMethod("readPassword", String.class, Object[].class);
                return (char[]) method.invoke(console, prompt, new Object[0]);
            } catch (Exception e) {
                System.out.print(prompt);
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader in = new BufferedReader(isr);
                try {
                    return in.readLine().toCharArray();
                } catch (IOException ex2) {
                    throw new IllegalStateException(ex2);
                }
            } finally {
                interactiveCount++;
            }
        }
        int getPosition(ArgSpec arg) {
            if (arg.group() == null) { return position; }
            GroupMatchContainer container = parseResultBuilder.groupMatchContainer.findLastMatchContainer(arg.group());
            return container == null ? 0 : container.lastMatch().position;
        }
        String positionDesc(ArgSpec arg) {
            int pos = getPosition(arg);
            return (arg.group() == null) ? pos + " (command-local)" : pos + " (in group " + arg.group().synopsis() + ")";
        }
    }
    private static class PositionalParametersSorter implements Comparator<ArgSpec> {
        private static final Range OPTION_INDEX = new Range(0, 0, false, true, "0");
        public int compare(ArgSpec p1, ArgSpec p2) {
            int result = index(p1).compareTo(index(p2));
            return (result == 0) ? p1.arity().compareTo(p2.arity()) : result;
        }
        private Range index(ArgSpec arg) { return arg.isOption() ? OPTION_INDEX : ((PositionalParamSpec) arg).index(); }
    }
    /**
     * Inner class to group the built-in {@link ITypeConverter} implementations.
     */
    private static class BuiltIn {
        static class CharArrayConverter implements ITypeConverter<char[]> {
            public char[] convert(String value) { return value.toCharArray(); }
        }
        static class StringConverter implements ITypeConverter<String> {
            public String convert(String value) { return value; }
        }
        static class StringBuilderConverter implements ITypeConverter<StringBuilder> {
            public StringBuilder convert(String value) { return new StringBuilder(value); }
        }
        static class CharSequenceConverter implements ITypeConverter<CharSequence> {
            public String convert(String value) { return value; }
        }
        /** Converts {@code "true"} or {@code "false"} to a {@code Boolean}. Other values result in a ParameterException.*/
        static class BooleanConverter implements ITypeConverter<Boolean> {
            public Boolean convert(String value) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    return Boolean.parseBoolean(value);
                } else {
                    throw new TypeConversionException("'" + value + "' is not a boolean");
                }
            }
        }
        static class CharacterConverter implements ITypeConverter<Character> {
            public Character convert(String value) {
                if (value.length() > 1) {
                    throw new TypeConversionException("'" + value + "' is not a single character");
                }
                return value.charAt(0);
            }
        }
        private static TypeConversionException fail(String value, Class<?> c) { return fail(value, c, "'%s' is not a %s"); }
        private static TypeConversionException fail(String value, Class<?> c, String template) {
            return new TypeConversionException(String.format(template, value, c.getSimpleName()));
        }
        /** Converts text to a {@code Byte} by delegating to {@link Byte#valueOf(String)}.*/
        static class ByteConverter implements ITypeConverter<Byte> {
            public Byte convert(String value) { try {return Byte.valueOf(value);} catch (Exception ex) {throw fail(value, Byte.TYPE);} }
        }
        /** Converts text to a {@code Short} by delegating to {@link Short#valueOf(String)}.*/
        static class ShortConverter implements ITypeConverter<Short> {
            public Short convert(String value) { try {return Short.valueOf(value);} catch (Exception ex) {throw fail(value, Short.TYPE);}  }
        }
        /** Converts text to an {@code Integer} by delegating to {@link Integer#valueOf(String)}.*/
        static class IntegerConverter implements ITypeConverter<Integer> {
            public Integer convert(String value) { try {return Integer.valueOf(value);} catch (Exception ex) {throw fail(value, Integer.TYPE, "'%s' is not an %s");}  }
        }
        /** Converts text to a {@code Long} by delegating to {@link Long#valueOf(String)}.*/
        static class LongConverter implements ITypeConverter<Long> {
            public Long convert(String value) { try {return Long.valueOf(value);} catch (Exception ex) {throw fail(value, Long.TYPE);}  }
        }
        static class FloatConverter implements ITypeConverter<Float> {
            public Float convert(String value) { try {return Float.valueOf(value);} catch (Exception ex) {throw fail(value, Float.TYPE);}  }
        }
        static class DoubleConverter implements ITypeConverter<Double> {
            public Double convert(String value) { try {return Double.valueOf(value);} catch (Exception ex) {throw fail(value, Double.TYPE);}  }
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
                    throw new TypeConversionException("'" + value + "' is not a yyyy-MM-dd date");
                }
            }
        }
        /** Converts text in any of the following formats to a {@code java.sql.Time}: {@code HH:mm}, {@code HH:mm:ss},
         * {@code HH:mm:ss.SSS}, {@code HH:mm:ss,SSS}. Other formats result in a ParameterException. */
        static class ISO8601TimeConverter implements ITypeConverter<Object> {
            // Implementation note: use reflection so that picocli only requires the java.base module in Java 9.
            private static /*final*/ String FQCN = "java.sql.Time"; // non-final for testing
            public Object convert(String value) {
                try {
                    if (value.length() <= 5) {
                        return createTime(new SimpleDateFormat("HH:mm").parse(value).getTime());
                    } else if (value.length() <= 8) {
                        return createTime(new SimpleDateFormat("HH:mm:ss").parse(value).getTime());
                    } else if (value.length() <= 12) {
                        try {
                            return createTime(new SimpleDateFormat("HH:mm:ss.SSS").parse(value).getTime());
                        } catch (ParseException e2) {
                            return createTime(new SimpleDateFormat("HH:mm:ss,SSS").parse(value).getTime());
                        }
                    }
                } catch (ParseException ignored) {
                    // ignored because we throw a ParameterException below
                }
                throw new TypeConversionException("'" + value + "' is not a HH:mm[:ss[.SSS]] time");
            }

            private Object createTime(long epochMillis) {
                try {
                    Class<?> timeClass = Class.forName(FQCN);
                    Constructor<?> constructor = timeClass.getDeclaredConstructor(long.class);
                    return constructor.newInstance(epochMillis);
                } catch (Exception e) {
                    throw new TypeConversionException("Unable to create new java.sql.Time with long value " + epochMillis + ": " + e.getMessage());
                }
            }

            public static void registerIfAvailable(Map<Class<?>, ITypeConverter<?>> registry, Tracer tracer) {
                if (excluded(FQCN, tracer)) { return; }
                try {
                    registry.put(Class.forName(FQCN), new ISO8601TimeConverter());
                } catch (Exception e) {
                    if (!traced.contains(FQCN)) {
                        tracer.debug("Could not register converter for %s: %s%n", FQCN, e.toString());
                    }
                    traced.add(FQCN);
                }
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
        static class CurrencyConverter implements ITypeConverter<Currency> {
            public Currency convert(String s) throws Exception { return Currency.getInstance(s); }
        }
        static class TimeZoneConverter implements ITypeConverter<TimeZone> {
            public TimeZone convert(String s) throws Exception { return TimeZone.getTimeZone(s); }
        }
        static class ByteOrderConverter implements ITypeConverter<ByteOrder> {
            public ByteOrder convert(String s) throws Exception {
                if (s.equalsIgnoreCase(ByteOrder.BIG_ENDIAN.toString())) { return ByteOrder.BIG_ENDIAN; }
                if (s.equalsIgnoreCase(ByteOrder.LITTLE_ENDIAN.toString())) { return ByteOrder.LITTLE_ENDIAN; }
                throw new TypeConversionException("'" + s + "' is not a valid ByteOrder");
            }
        }
        static class ClassConverter implements ITypeConverter<Class<?>> {
            public Class<?> convert(String s) throws Exception { return Class.forName(s); }
        }
        static class NetworkInterfaceConverter implements ITypeConverter<NetworkInterface> {
            public NetworkInterface convert(String s) throws Exception {
                try {
                    InetAddress addr = new InetAddressConverter().convert(s);
                    return NetworkInterface.getByInetAddress(addr);
                } catch (Exception ex) {
                    try { return NetworkInterface.getByName(s);
                    } catch (Exception ex2) {
                        throw new TypeConversionException("'" + s + "' is not an InetAddress or NetworkInterface name");
                    }
                }
            }
        }
        static void registerIfAvailable(Map<Class<?>, ITypeConverter<?>> registry, Tracer tracer, String fqcn, String factoryMethodName, Class<?>... paramTypes) {
            registerIfAvailable(registry, tracer, fqcn, fqcn, factoryMethodName, paramTypes);
        }
        static void registerIfAvailable(Map<Class<?>, ITypeConverter<?>> registry, Tracer tracer, String fqcn, String factoryClass, String factoryMethodName, Class<?>... paramTypes) {
            if (excluded(fqcn, tracer)) { return; }
            try {
                Class<?> cls = Class.forName(fqcn);
                Class<?> factory = Class.forName(factoryClass);
                Method method = factory.getDeclaredMethod(factoryMethodName, paramTypes);
                registry.put(cls, new ReflectionConverter(method, paramTypes));
            } catch (Exception e) {
                if (!traced.contains(fqcn)) {
                    tracer.debug("Could not register converter for %s: %s%n", fqcn, e.toString());
                }
                traced.add(fqcn);
            }
        }
        static boolean excluded(String fqcn, Tracer tracer) {
            String[] excludes = System.getProperty("picocli.converters.excludes", "").split(",");
            for (String regex : excludes) {
                if (fqcn.matches(regex)) {
                    tracer.debug("BuiltIn type converter for %s is not loaded: (picocli.converters.excludes=%s)%n", fqcn, System.getProperty("picocli.converters.excludes"));
                    return true;
                }
            }
            return false;
        }
        static Set<String> traced = new HashSet<String>();
        static class ReflectionConverter implements ITypeConverter<Object> {
            private final Method method;
            private Class<?>[] paramTypes;

            public ReflectionConverter(Method method, Class<?>... paramTypes) {
                this.method = Assert.notNull(method, "method");
                this.paramTypes = Assert.notNull(paramTypes, "paramTypes");
            }

            public Object convert(String s) {
                try {
                    if (paramTypes.length > 1) {
                        return method.invoke(null, s, new String[0]);
                    } else {
                        return method.invoke(null, s);
                    }
                } catch (InvocationTargetException e) {
                    throw new TypeConversionException(String.format("cannot convert '%s' to %s (%s)", s, method.getReturnType(), e.getTargetException()));
                } catch (Exception e) {
                    throw new TypeConversionException(String.format("Internal error converting '%s' to %s (%s)", s, method.getReturnType(), e));
                }
            }
        }
        private BuiltIn() {} // private constructor: never instantiate
    }

    static class AutoHelpMixin {
        private static final String KEY = "mixinStandardHelpOptions";

        @Option(names = {"${picocli.help.name.0:--h}", "${picocli.help.name.1:---help}"}, usageHelp = true, descriptionKey = "mixinStandardHelpOptions.help",
                description = "Show this help message and exit.")
        private boolean helpRequested;

        @Option(names = {"${picocli.version.name.0:--V}", "${picocli.version.name.1:---version}"}, versionHelp = true, descriptionKey = "mixinStandardHelpOptions.version",
                description = "Print version information and exit.")
        private boolean versionRequested;
    }

    /** Help command that can be installed as a subcommand on all application commands. When invoked with a subcommand
     * argument, it prints usage help for the specified subcommand. For example:<pre>
     *
     * // print help for subcommand
     * command help subcommand
     * </pre><p>
     * When invoked without additional parameters, it prints usage help for the parent command. For example:
     * </p><pre>
     *
     * // print help for command
     * command help
     * </pre>
     * For {@linkplain Messages internationalization}: this command has a {@code --help} option with {@code descriptionKey = "helpCommand.help"},
     * and a {@code COMMAND} positional parameter with {@code descriptionKey = "helpCommand.command"}.
     * @since 3.0
     */
    @Command(name = "help", header = "Displays help information about the specified command",
            synopsisHeading = "%nUsage: ", helpCommand = true,
            description = {"%nWhen no COMMAND is given, the usage help for the main command is displayed.",
                    "If a COMMAND is specified, the help for that command is shown.%n"})
    public static final class HelpCommand implements IHelpCommandInitializable, IHelpCommandInitializable2, Runnable {

        @Option(names = {"-h", "--help"}, usageHelp = true, descriptionKey = "helpCommand.help",
                description = "Show usage help for the help command and exit.")
        private boolean helpRequested;

        @Parameters(paramLabel = "COMMAND", descriptionKey = "helpCommand.command",
                    description = "The COMMAND to display the usage help message for.")
        private String[] commands = new String[0];

        private CommandLine self;
        private PrintStream out;
        private PrintStream err;
        private PrintWriter outWriter;
        private PrintWriter errWriter;
        private Help.Ansi ansi; // for backwards compatibility with pre-4.0
        private Help.ColorScheme colorScheme;

        /** Invokes {@link #usage(PrintStream, Help.ColorScheme) usage} for the specified command, or for the parent command. */
        public void run() {
            CommandLine parent = self == null ? null : self.getParent();
            if (parent == null) { return; }
            Help.ColorScheme colors = colorScheme != null ? colorScheme : Help.defaultColorScheme(ansi);
            if (commands.length > 0) {
                CommandLine subcommand = parent.getSubcommands().get(commands[0]);
                if (subcommand != null) {
                    if (outWriter != null) {
                        subcommand.usage(outWriter, colors);
                    } else {
                        subcommand.usage(out, colors); // for compatibility with pre-4.0 clients
                    }
                } else {
                    throw new ParameterException(parent, "Unknown subcommand '" + commands[0] + "'.", null, commands[0]);
                }
            } else {
                if (outWriter != null) {
                    parent.usage(outWriter, colors);
                } else {
                    parent.usage(out, colors); // for compatibility with pre-4.0 clients
                }
            }
        }
        /** {@inheritDoc} */
        @Deprecated public void init(CommandLine helpCommandLine, Help.Ansi ansi, PrintStream out, PrintStream err) {
            this.self = Assert.notNull(helpCommandLine, "helpCommandLine");
            this.ansi = Assert.notNull(ansi, "ansi");
            this.out  = Assert.notNull(out, "out");
            this.err  = Assert.notNull(err, "err");
        }
        /** {@inheritDoc} */
        public void init(CommandLine helpCommandLine, Help.ColorScheme colorScheme, PrintWriter out, PrintWriter err) {
            this.self        = Assert.notNull(helpCommandLine, "helpCommandLine");
            this.colorScheme = Assert.notNull(colorScheme, "colorScheme");
            this.outWriter   = Assert.notNull(out, "outWriter");
            this.errWriter   = Assert.notNull(err, "errWriter");
        }
    }

    /** Help commands that provide usage help for other commands can implement this interface to be initialized with the information they need.
     * <p>The {@link #printHelpIfRequested(List, PrintStream, PrintStream, Help.Ansi) CommandLine::printHelpIfRequested} method calls the
     * {@link #init(CommandLine, picocli.CommandLine.Help.Ansi, PrintStream, PrintStream) init} method on commands marked as {@link Command#helpCommand() helpCommand}
     * before the help command's {@code run} or {@code call} method is called.</p>
     * <p><b>Implementation note:</b></p><p>
     * If an error occurs in the {@code run} or {@code call} method while processing the help request, it is recommended custom Help
     * commands throw a {@link ParameterException ParameterException} with a reference to the parent command. The {@link DefaultExceptionHandler DefaultExceptionHandler} will print
     * the error message and the usage for the parent command, and will terminate with the exit code of the exception handler if one was set.
     * </p>
     * @deprecated use {@link IHelpCommandInitializable2} instead
     * @since 3.0 */
    @Deprecated public static interface IHelpCommandInitializable {
        /** Initializes this object with the information needed to implement a help command that provides usage help for other commands.
         * @param helpCommandLine the {@code CommandLine} object associated with this help command. Implementors can use
         *                        this to walk the command hierarchy and get access to the help command's parent and sibling commands.
         * @param ansi whether to use Ansi colors or not
         * @param out the stream to print the usage help message to
         * @param err the error stream to print any diagnostic messages to, in addition to the output from the exception handler
         */
        @Deprecated void init(CommandLine helpCommandLine, Help.Ansi ansi, PrintStream out, PrintStream err);
    }

    /** Help commands that provide usage help for other commands can implement this interface to be initialized with the information they need.
     * <p>The {@link #executeHelpRequest(List) CommandLine::printHelpIfRequested} method calls the
     * {@link #init(CommandLine, picocli.CommandLine.Help.ColorScheme, PrintWriter, PrintWriter) init} method on commands marked as {@link Command#helpCommand() helpCommand}
     * before the help command's {@code run} or {@code call} method is called.</p>
     * <p><b>Implementation note:</b></p><p>
     * If an error occurs in the {@code run} or {@code call} method while processing the help request, it is recommended custom Help
     * commands throw a {@link ParameterException ParameterException} with a reference to the parent command.
     * The {@link DefaultExceptionHandler default ParameterException handler} will print the error message and the usage for the parent command.
     * </p>
     * @since 4.0 */
    public static interface IHelpCommandInitializable2 {
        /** Initializes this object with the information needed to implement a help command that provides usage help for other commands.
         * @param helpCommandLine the {@code CommandLine} object associated with this help command. Implementors can use
         *                        this to walk the command hierarchy and get access to the help command's parent and sibling commands.
         * @param colorScheme the color scheme to use when printing help, including whether to use Ansi colors or not
         * @param outWriter the output writer to print the usage help message to
         * @param errWriter the error writer to print any diagnostic messages to, in addition to the output from the exception handler
         */
        void init(CommandLine helpCommandLine, Help.ColorScheme colorScheme, PrintWriter outWriter, PrintWriter errWriter);
    }

    /**
     * Renders a section of the usage help message. The usage help message can be customized:
     * use the {@link #setHelpSectionKeys(List)} and {@link #setHelpSectionMap(Map)} to change the order of sections,
     * delete standard sections, add custom sections or replace the renderer of a standard sections with a custom one.
     * <p>
     * This gives complete freedom on how a usage help message section is rendered, but it also means that the section renderer
     * is responsible for all aspects of rendering the section, including layout and emitting ANSI escape codes.
     * The {@link Help.TextTable} and {@link Help.Ansi.Text} classes, and the {@link CommandLine.Help.Ansi#string(String)} and {@link CommandLine.Help.Ansi#text(String)} methods may be useful.
     * </p>
     * @see UsageMessageSpec
     * @since 3.9
     */
    public interface IHelpSectionRenderer {
        /**
         * Renders a section of the usage help, like header heading, header, synopsis heading,
         * synopsis, description heading, description, etc.
         * @param help the {@code Help} instance for which to render a section
         * @return the text for this section; may contain {@linkplain Help.Ansi ANSI} escape codes
         * @since 3.9
         */
        String render(Help help);
    }

    /**
     * A collection of methods and inner classes that provide fine-grained control over the contents and layout of
     * the usage help message to display to end users when help is requested or invalid input values were specified.
     * <h2>Class Diagram of the CommandLine.Help API</h2>
     * <p>
     * <img src="doc-files/class-diagram-help-api.png" alt="Class Diagram of the CommandLine.Help API">
     * </p>
     * <h2>Layered API</h2>
     * <p>The {@link Command} annotation and the {@link UsageMessageSpec} programmatic API equivalent
     * provide the easiest way to configure the usage help message. See
     * the <a href="https://remkop.github.io/picocli/index.html#_usage_help">Manual</a> for details.</p>
     * <p>This Help class provides high-level functions to create sections of the usage help message and headings
     * for these sections. Instead of calling the {@link CommandLine#usage(PrintStream, CommandLine.Help.ColorScheme)}
     * method, application authors may want to create a custom usage help message by reorganizing sections in a
     * different order and/or adding custom sections.</p>
     * <p>Finally, the Help class contains inner classes and interfaces that can be used to create custom help messages.</p>
     * <h3>IOptionRenderer and IParameterRenderer</h3>
     * <p>Renders a field annotated with {@link Option} or {@link Parameters} to an array of {@link Text} values.
     * By default, these values are</p><ul>
     * <li>mandatory marker character (if the option/parameter is {@link Option#required() required})</li>
     * <li>short option name (empty for parameters)</li>
     * <li>comma or empty (empty for parameters)</li>
     * <li>long option names (the parameter {@link IParamLabelRenderer label} for parameters)</li>
     * <li>description</li>
     * </ul>
     * <p>Other components rely on this ordering.</p>
     * <h3>Layout</h3>
     * <p>Delegates to the renderers to create {@link Text} values for the annotated fields, and uses a
     * {@link TextTable} to display these values in tabular format. Layout is responsible for deciding which values
     * to display where in the table. By default, Layout shows one option or parameter per table row.</p>
     * <h3>TextTable</h3>
     * <p>Responsible for spacing out {@link Text} values according to the {@link Column} definitions the table was
     * created with. Columns have a width, indentation, and an overflow policy that decides what to do if a value is
     * longer than the column's width.</p>
     * <h3>Text</h3>
     * <p>Encapsulates rich text with styles and colors in a way that other components like {@link TextTable} are
     * unaware of the embedded ANSI escape codes.</p>
     */
    public static class Help {

        /** Constant String holding the default program name, value defined in {@link CommandSpec#DEFAULT_COMMAND_NAME}. */
        protected static final String DEFAULT_COMMAND_NAME = CommandSpec.DEFAULT_COMMAND_NAME;

        /** Constant String holding the default string that separates options from option parameters, value defined in {@link ParserSpec#DEFAULT_SEPARATOR}. */
        protected static final String DEFAULT_SEPARATOR = ParserSpec.DEFAULT_SEPARATOR;

        private final static int defaultOptionsColumnWidth = 24;
        private final CommandSpec commandSpec;
        private final ColorScheme colorScheme;
        private final Map<String, Help> commands = new LinkedHashMap<String, Help>();
        private List<String> aliases = Collections.emptyList();

        private IParamLabelRenderer parameterLabelRenderer;

        /** Constructs a new {@code Help} instance with a default color scheme, initialized from annotatations
         * on the specified class and superclasses.
         * @param command the annotated object to create usage help for */
        public Help(Object command) {
            this(command, Ansi.AUTO);
        }

        /** Constructs a new {@code Help} instance with a default color scheme, initialized from annotatations
         * on the specified class and superclasses.
         * @param command the annotated object to create usage help for
         * @param ansi whether to emit ANSI escape codes or not */
        public Help(Object command, Ansi ansi) {
            this(command, defaultColorScheme(ansi));
        }
        /** Constructs a new {@code Help} instance with the specified color scheme, initialized from annotatations
         * on the specified class and superclasses.
         * @param command the annotated object to create usage help for
         * @param colorScheme the color scheme to use
         * @deprecated use {@link picocli.CommandLine.Help#Help(picocli.CommandLine.Model.CommandSpec, picocli.CommandLine.Help.ColorScheme)}  */
        @Deprecated public Help(Object command, ColorScheme colorScheme) {
            this(CommandSpec.forAnnotatedObject(command, new DefaultFactory()), colorScheme);
        }
        /** Constructs a new {@code Help} instance with the specified color scheme, initialized from annotatations
         * on the specified class and superclasses.
         * @param commandSpec the command model to create usage help for
         * @param colorScheme the color scheme to use */
        public Help(CommandSpec commandSpec, ColorScheme colorScheme) {
            this.commandSpec = Assert.notNull(commandSpec, "commandSpec");
            this.aliases = new ArrayList<String>(Arrays.asList(commandSpec.aliases()));
            this.aliases.add(0, commandSpec.name());
            this.colorScheme = new ColorScheme.Builder(colorScheme).applySystemProperties().build();
            parameterLabelRenderer = createDefaultParamLabelRenderer(); // uses help separator

            this.addAllSubcommands(commandSpec.subcommands());
        }

        Help withCommandNames(List<String> aliases) { this.aliases = aliases; return this; }

        /** Returns the {@code CommandSpec} model that this Help was constructed with.
         * @since 3.9 */
        public CommandSpec commandSpec() { return commandSpec; }

        /** Returns the {@code ColorScheme} model that this Help was constructed with.
         * @since 3.0 */
        public ColorScheme colorScheme() { return colorScheme; }

        /** Returns the {@code IHelpFactory} that this Help was constructed with.
         * @since 3.9 */
        private IHelpFactory getHelpFactory() { return commandSpec.usageMessage().helpFactory(); }

        /** Returns the map of subcommand {@code Help} instances for this command Help.
         * @since 3.9 */
        protected Map<String, Help> subcommands() { return Collections.unmodifiableMap(commands); }

        /** Returns the list of aliases for the command in this Help.
         * @since 3.9 */
        protected List<String> aliases() { return Collections.unmodifiableList(aliases); }

        /** Option and positional parameter value label renderer used for the synopsis line(s) and the option list.
         * By default initialized to the result of {@link #createDefaultParamLabelRenderer()}, which takes a snapshot
         * of the {@link ParserSpec#separator()} at construction time. If the separator is modified after Help construction, you
         * may need to re-initialize this field by calling {@link #createDefaultParamLabelRenderer()} again. */
        public IParamLabelRenderer parameterLabelRenderer() {return parameterLabelRenderer;}

        /** Registers all specified subcommands with this Help.
         * @param commands maps the command names to the associated CommandLine object
         * @return this Help instance (for method chaining)
         * @see CommandLine#getSubcommands()
         */
        public Help addAllSubcommands(Map<String, CommandLine> commands) {
            if (commands != null) {
                // first collect aliases
                Map<CommandLine, List<String>> done = new IdentityHashMap<CommandLine, List<String>>();
                for (CommandLine cmd : commands.values()) {
                    if (!done.containsKey(cmd)) {
                        done.put(cmd, new ArrayList<String>(Arrays.asList(cmd.commandSpec.aliases())));
                    }
                }
                // then loop over all names that the command was registered with and add this name to the front of the list (if it isn't already in the list)
                for (Map.Entry<String, CommandLine> entry : commands.entrySet()) {
                    List<String> aliases = done.get(entry.getValue());
                    if (!aliases.contains(entry.getKey())) { aliases.add(0, entry.getKey()); }
                }
                // The aliases list for each command now has at least one entry, with the main name at the front.
                // Now we loop over the commands in the order that they were registered on their parent command.
                for (Map.Entry<String, CommandLine> entry : commands.entrySet()) {
                    // not registering hidden commands is easier than suppressing display in Help.commandList():
                    // if all subcommands are hidden, help should not show command list header
                    if (!entry.getValue().getCommandSpec().usageMessage().hidden()) {
                        List<String> aliases = done.remove(entry.getValue());
                        if (aliases != null) { // otherwise we already processed this command by another alias
                            addSubcommand(aliases, entry.getValue());
                        }
                    }
                }
            }
            return this;
        }

        /** Registers the specified subcommand with this Help.
         * @param commandNames the name and aliases of the subcommand to display in the usage message
         * @param commandLine the {@code CommandLine} object to get more information from
         * @return this Help instance (for method chaining) */
        Help addSubcommand(List<String> commandNames, CommandLine commandLine) {
            String all = commandNames.toString();
            commands.put(all.substring(1, all.length() - 1), getHelpFactory().create(commandLine.commandSpec, colorScheme).withCommandNames(commandNames));
            return this;
        }

        /** Registers the specified subcommand with this Help.
         * @param commandName the name of the subcommand to display in the usage message
         * @param command the {@code CommandSpec} or {@code @Command} annotated object to get more information from
         * @return this Help instance (for method chaining)
         * @deprecated
         */
        @Deprecated public Help addSubcommand(String commandName, Object command) {
            commands.put(commandName,
                    getHelpFactory().create(CommandSpec.forAnnotatedObject(command, commandSpec.commandLine().factory), defaultColorScheme(Ansi.AUTO)));
            return this;
        }

        List<OptionSpec> options() { return commandSpec.options(); }
        List<PositionalParamSpec> positionalParameters() { return commandSpec.positionalParameters(); }
        String commandName() { return commandSpec.name(); }

        /** Returns a synopsis for the command without reserving space for the synopsis heading.
         * @return a synopsis
         * @see #abbreviatedSynopsis()
         * @see #detailedSynopsis(Comparator, boolean)
         * @deprecated use {@link #synopsis(int)} instead
         */
        @Deprecated public String synopsis() { return synopsis(0); }

        /**
         * Returns a synopsis for the command, reserving the specified space for the synopsis heading.
         * @param synopsisHeadingLength the length of the synopsis heading that will be displayed on the same line
         * @return a synopsis
         * @see #abbreviatedSynopsis()
         * @see #detailedSynopsis(Comparator, boolean)
         * @see #synopsisHeading
         */
        public String synopsis(int synopsisHeadingLength) {
            if (!empty(commandSpec.usageMessage().customSynopsis())) { return customSynopsis(); }
            return commandSpec.usageMessage().abbreviateSynopsis() ? abbreviatedSynopsis()
                    : detailedSynopsis(synopsisHeadingLength, createShortOptionArityAndNameComparator(), true);
        }

        /** Generates a generic synopsis like {@code <command name> [OPTIONS] [PARAM1 [PARAM2]...]}, omitting parts
         * that don't apply to the command (e.g., does not show [OPTIONS] if the command has no options).
         * @return a generic synopsis */
        public String abbreviatedSynopsis() {
            StringBuilder sb = new StringBuilder();
            if (!commandSpec.optionsMap().isEmpty()) { // only show if annotated object actually has options
                sb.append(" [OPTIONS]");
            }
            // sb.append(" [--] "); // implied
            for (PositionalParamSpec positionalParam : commandSpec.positionalParameters()) {
                if (!positionalParam.hidden()) {
                    sb.append(' ').append(parameterLabelRenderer().renderParameterLabel(positionalParam, ansi(), colorScheme.parameterStyles));
                }
            }

            // only show if object has subcommands
            if (!commandSpec.subcommands().isEmpty()) {
                sb.append(" [COMMAND]");
            }

            return colorScheme.commandText(commandSpec.qualifiedName()).toString()
                    + (sb.toString()) + System.getProperty("line.separator");
        }
        /** Generates a detailed synopsis message showing all options and parameters. Follows the unix convention of
         * showing optional options and parameters in square brackets ({@code [ ]}).
         * @param optionSort comparator to sort options or {@code null} if options should not be sorted
         * @param clusterBooleanOptions {@code true} if boolean short options should be clustered into a single string
         * @return a detailed synopsis
         * @deprecated use {@link #detailedSynopsis(int, Comparator, boolean)} instead. */
        @Deprecated public String detailedSynopsis(Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
            return detailedSynopsis(0, optionSort, clusterBooleanOptions);
        }

        /** Generates a detailed synopsis message showing all options and parameters. Follows the unix convention of
         * showing optional options and parameters in square brackets ({@code [ ]}).
         * @param synopsisHeadingLength the length of the synopsis heading that will be displayed on the same line
         * @param optionSort comparator to sort options or {@code null} if options should not be sorted
         * @param clusterBooleanOptions {@code true} if boolean short options should be clustered into a single string
         * @return a detailed synopsis
         * @since 3.0 */
        public String detailedSynopsis(int synopsisHeadingLength, Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
            Set<ArgSpec> argsInGroups = new HashSet<ArgSpec>();
            Text groupsText = createDetailedSynopsisGroupsText(argsInGroups);
            Text optionText = createDetailedSynopsisOptionsText(argsInGroups, optionSort, clusterBooleanOptions);
            Text positionalParamText = createDetailedSynopsisPositionalsText(argsInGroups);
            Text commandText = createDetailedSynopsisCommandText();

            Text text = groupsText.concat(optionText).concat(positionalParamText).concat(commandText);

            return insertSynopsisCommandName(synopsisHeadingLength, text);
        }

        /** Returns a Text object containing a partial detailed synopsis showing only the options and positional parameters in
         * the specified {@linkplain ArgGroup#validate() validating} {@linkplain ArgGroup groups}, starting with a {@code " "} space.
         * @param outparam_groupArgs all options and positional parameters in the groups this method generates a synopsis for;
         *                           these options and positional parameters should be excluded from appearing elsewhere in the synopsis
         * @return the formatted groups synopsis elements, starting with a {@code " "} space, or an empty Text if this command has no validating groups
         * @since 4.0 */
        protected Text createDetailedSynopsisGroupsText(Set<ArgSpec> outparam_groupArgs) {
            Set<ArgGroupSpec> remove = new HashSet<ArgGroupSpec>();
            List<ArgGroupSpec> groups = new ArrayList<ArgGroupSpec>(commandSpec().argGroups());
            for (ArgGroupSpec group : groups) {
                if (group.validate()) {
                    // remove subgroups
                    remove.addAll(group.subgroups());

                    // exclude options and positional parameters in this group
                    outparam_groupArgs.addAll(group.args());

                    // exclude options and positional parameters in the subgroups
                    for (ArgGroupSpec subgroup : group.subgroups()) {
                        outparam_groupArgs.addAll(subgroup.args());
                    }
                } else {
                    remove.add(group); // non-validating groups should not impact synopsis
                }
            }
            groups.removeAll(remove);
            Text groupText = ansi().new Text(0);
            for (ArgGroupSpec group : groups) {
                groupText = groupText.concat(" ").concat(group.synopsisText(colorScheme()));
            }
            return groupText;
        }
        /** Returns a Text object containing a partial detailed synopsis showing only the options, starting with a {@code " "} space.
         * Follows the unix convention of showing optional options and parameters in square brackets ({@code [ ]}).
         * @param done the list of options and positional parameters for which a synopsis was already generated. Options in this set should be excluded.
         * @param optionSort comparator to sort options or {@code null} if options should not be sorted
         * @param clusterBooleanOptions {@code true} if boolean short options should be clustered into a single string
         * @return the formatted options, starting with a {@code " "} space, or an empty Text if this command has no named options
         * @since 3.9 */
        protected Text createDetailedSynopsisOptionsText(Collection<ArgSpec> done, Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
            Text optionText = ansi().new Text(0);
            List<OptionSpec> options = new ArrayList<OptionSpec>(commandSpec.options()); // iterate in declaration order
            if (optionSort != null) {
                Collections.sort(options, optionSort);// iterate in specified sort order
            }
            options.removeAll(done);
            if (clusterBooleanOptions) { // cluster all short boolean options into a single string
                List<OptionSpec> booleanOptions = new ArrayList<OptionSpec>();
                StringBuilder clusteredRequired = new StringBuilder("-");
                StringBuilder clusteredOptional = new StringBuilder("-");
                for (OptionSpec option : options) {
                    if (option.hidden()) { continue; }
                    boolean isFlagOption = option.typeInfo().isBoolean();
                    if (isFlagOption && option.arity().max <= 0) { // #612 consider arity: boolean options may require a parameter
                        String shortestName = option.shortestName();
                        if (shortestName.length() == 2 && shortestName.startsWith("-")) {
                            booleanOptions.add(option);
                            if (option.required()) {
                                clusteredRequired.append(shortestName.substring(1));
                            } else {
                                clusteredOptional.append(shortestName.substring(1));
                            }
                        }
                    }
                }
                options.removeAll(booleanOptions);
                if (clusteredRequired.length() > 1) { // initial length was 1
                    optionText = optionText.concat(" ").concat(colorScheme.optionText(clusteredRequired.toString()));
                }
                if (clusteredOptional.length() > 1) { // initial length was 1
                    optionText = optionText.concat(" [").concat(colorScheme.optionText(clusteredOptional.toString())).concat("]");
                }
            }
            for (OptionSpec option : options) {
                if (!option.hidden()) {
                    Text name = colorScheme.optionText(option.shortestName());
                    Text param = parameterLabelRenderer().renderParameterLabel(option, colorScheme.ansi(), colorScheme.optionParamStyles);
                    if (option.required()) { // e.g., -x=VAL
                        optionText = optionText.concat(" ").concat(name).concat(param).concat("");
                        if (option.isMultiValue()) { // e.g., -x=VAL [-x=VAL]...
                            optionText = optionText.concat(" [").concat(name).concat(param).concat("]...");
                        }
                    } else {
                        optionText = optionText.concat(" [").concat(name).concat(param).concat("]");
                        if (option.isMultiValue()) { // add ellipsis to show option is repeatable
                            optionText = optionText.concat("...");
                        }
                    }
                }
            }
            return optionText;
        }

        /** Returns a Text object containing a partial detailed synopsis showing only the positional parameters, starting with a {@code " "} space.
         * Follows the unix convention of showing optional options and parameters in square brackets ({@code [ ]}).
         * @param done the list of options and positional parameters for which a synopsis was already generated. Positional parameters in this set should be excluded.
         * @return the formatted positional parameters, starting with a {@code " "} space, or an empty Text if this command has no positional parameters
         * @since 3.9 */
        protected Text createDetailedSynopsisPositionalsText(Collection<ArgSpec> done) {
            Text positionalParamText = ansi().new Text(0);
            List<PositionalParamSpec> positionals = new ArrayList<PositionalParamSpec>(commandSpec.positionalParameters()); // iterate in declaration order
            positionals.removeAll(done);
            for (PositionalParamSpec positionalParam : positionals) {
                if (!positionalParam.hidden()) {
                    positionalParamText = positionalParamText.concat(" ");
                    Text label = parameterLabelRenderer().renderParameterLabel(positionalParam, colorScheme.ansi(), colorScheme.parameterStyles);
                    positionalParamText = positionalParamText.concat(label);
                }
            }
            return positionalParamText;
        }

        /** Returns a Text object containing a partial detailed synopsis showing only the subcommands, starting with a {@code " "} space.
         * Follows the unix convention of showing optional elements in square brackets ({@code [ ]}).
         * @return this implementation returns a hard-coded string {@code " [COMMAND]"} if this command has subcommands, an empty Text otherwise
         * @since 3.9 */
        protected Text createDetailedSynopsisCommandText() {
            Text commandText = ansi().new Text(0);
            if (!commandSpec.subcommands().isEmpty()){
                commandText = commandText.concat(" [")
                        .concat("COMMAND")
                        .concat("]");
            }
            return commandText;
        }

        /**
         * Returns the detailed synopsis text by inserting the command name before the specified text with options and positional parameters details.
         * @param synopsisHeadingLength length of the synopsis heading string to be displayed on the same line as the first synopsis line.
         *                              For example, if the synopsis heading is {@code "Usage: "}, this value is 7.
         * @param optionsAndPositionalsAndCommandsDetails formatted string with options, positional parameters and subcommands.
         *          Follows the unix convention of showing optional options and parameters in square brackets ({@code [ ]}).
         * @return the detailed synopsis text, in multiple lines if the length exceeds the usage width
         */
        protected String insertSynopsisCommandName(int synopsisHeadingLength, Text optionsAndPositionalsAndCommandsDetails) {
            // Fix for #142: first line of synopsis overshoots max. characters
            String commandName = commandSpec.qualifiedName();
            int firstColumnLength = commandName.length() + synopsisHeadingLength;

            // synopsis heading ("Usage: ") may be on the same line, so adjust column width
            TextTable textTable = TextTable.forColumnWidths(ansi(), firstColumnLength, width() - firstColumnLength);
            textTable.setAdjustLineBreaksForWideCJKCharacters(commandSpec.usageMessage().adjustLineBreaksForWideCJKCharacters());
            textTable.indentWrappedLines = 1; // don't worry about first line: options (2nd column) always start with a space

            // right-adjust the command name by length of synopsis heading
            Text PADDING = Ansi.OFF.new Text(stringOf('X', synopsisHeadingLength));
            textTable.addRowValues(PADDING.concat(colorScheme.commandText(commandName)), optionsAndPositionalsAndCommandsDetails);
            return textTable.toString().substring(synopsisHeadingLength); // cut off leading synopsis heading spaces
        }

        /** Returns the number of characters the synopsis heading will take on the same line as the synopsis.
         * @return the number of characters the synopsis heading will take on the same line as the synopsis.
         * @see #detailedSynopsis(int, Comparator, boolean)
         */
        public int synopsisHeadingLength() {
            String[] lines = Ansi.OFF.new Text(commandSpec.usageMessage().synopsisHeading()).toString().split("\\r?\\n|\\r|%n", -1);
            return lines[lines.length - 1].length();
        }
        /**
         * <p>Returns a description of the {@linkplain Option options} supported by the application.
         * This implementation {@linkplain #createShortOptionNameComparator() sorts options alphabetically}, and shows
         * only the {@linkplain Option#hidden() non-hidden} options in a {@linkplain TextTable tabular format}
         * using the {@linkplain #createDefaultOptionRenderer() default renderer} and {@linkplain Layout default layout}.</p>
         * @return the fully formatted option list
         * @see #optionList(Layout, Comparator, IParamLabelRenderer)
         */
        public String optionList() {
            Comparator<OptionSpec> sortOrder = commandSpec.usageMessage().sortOptions()
                    ? createShortOptionNameComparator()
                    : createOrderComparatorIfNecessary(commandSpec.options());

            return optionList(createLayout(calcLongOptionColumnWidth()), sortOrder, parameterLabelRenderer());
        }

        private static Comparator<OptionSpec> createOrderComparatorIfNecessary(List<OptionSpec> options) {
            for (OptionSpec option : options) { if (option.order() != OptionSpec.DEFAULT_ORDER) { return createOrderComparator(); } }
            return null;
        }

        private int calcLongOptionColumnWidth() {
            int max = 0;
            IOptionRenderer optionRenderer = new DefaultOptionRenderer(false, " ");
            for (OptionSpec option : commandSpec.options()) {
                Text[][] values = optionRenderer.render(option, parameterLabelRenderer(), colorScheme);
                int len = values[0][3].length;
                if (len < Help.defaultOptionsColumnWidth - 3) { max = Math.max(max, len); }
            }
            IParameterRenderer paramRenderer = new DefaultParameterRenderer(false, " ");
            for (PositionalParamSpec positional : commandSpec.positionalParameters()) {
                Text[][] values = paramRenderer.render(positional, parameterLabelRenderer(), colorScheme);
                int len = values[0][3].length;
                if (len < Help.defaultOptionsColumnWidth - 3) { max = Math.max(max, len); }
            }
            return max + 3;
        }

        /** Sorts all {@code Options} with the specified {@code comparator} (if the comparator is non-{@code null}),
         * then {@linkplain Layout#addOption(CommandLine.Model.OptionSpec, CommandLine.Help.IParamLabelRenderer) adds} all non-hidden options to the
         * specified TextTable and returns the result of TextTable.toString().
         * @param layout responsible for rendering the option list
         * @param valueLabelRenderer used for options with a parameter
         * @return the fully formatted option list
         * @since 3.0 */
        public String optionList(Layout layout, Comparator<OptionSpec> optionSort, IParamLabelRenderer valueLabelRenderer) {
            List<OptionSpec> options = new ArrayList<OptionSpec>(commandSpec.options()); // options are stored in order of declaration
            if (optionSort != null) {
                Collections.sort(options, optionSort); // default: sort options ABC
            }
            List<ArgGroupSpec> groups = optionListGroups();
            for (ArgGroupSpec group : groups) { options.removeAll(group.options()); }

            StringBuilder sb = new StringBuilder();
            layout.addOptions(options, valueLabelRenderer);
            sb.append(layout.toString());

            int longOptionColumnWidth = calcLongOptionColumnWidth();
            Collections.sort(groups, new SortByOrder<ArgGroupSpec>());
            for (ArgGroupSpec group : groups) {
                sb.append(heading(ansi(), width(), adjustCJK(), group.heading()));

                Layout groupLayout = createLayout(longOptionColumnWidth);
                groupLayout.addPositionalParameters(group.positionalParameters(), valueLabelRenderer);
                List<OptionSpec> groupOptions = new ArrayList<OptionSpec>(group.options());
                if (optionSort != null) {
                    Collections.sort(groupOptions, optionSort);
                }
                groupLayout.addOptions(groupOptions, valueLabelRenderer);
                sb.append(groupLayout);
            }
            return sb.toString();
        }

        /** Returns the list of {@code ArgGroupSpec}s with a non-{@code null} heading. */
        private List<ArgGroupSpec> optionListGroups() {
            List<ArgGroupSpec> result = new ArrayList<ArgGroupSpec>();
            optionListGroups(commandSpec.argGroups(), result);
            return result;
        }
        private static void optionListGroups(List<ArgGroupSpec> groups, List<ArgGroupSpec> result) {
            for (ArgGroupSpec group : groups) {
                optionListGroups(group.subgroups(), result);
                if (group.heading() != null) { result.add(group); }
            }
        }

        /**
         * Returns the section of the usage help message that lists the parameters with their descriptions.
         * @return the section of the usage help message that lists the parameters
         */
        public String parameterList() {
            return parameterList(createLayout(calcLongOptionColumnWidth()), parameterLabelRenderer());
        }
        /**
         * Returns the section of the usage help message that lists the parameters with their descriptions.
         * @param layout the layout to use
         * @param paramLabelRenderer for rendering parameter names
         * @return the section of the usage help message that lists the parameters
         */
        public String parameterList(Layout layout, IParamLabelRenderer paramLabelRenderer) {
            List<PositionalParamSpec> positionals = new ArrayList<PositionalParamSpec>(commandSpec.positionalParameters());
            List<ArgGroupSpec> groups = optionListGroups();
            for (ArgGroupSpec group : groups) { positionals.removeAll(group.positionalParameters()); }

            layout.addPositionalParameters(positionals, paramLabelRenderer);
            return layout.toString();
        }

        private static String heading(Ansi ansi, int usageWidth, boolean adjustCJK, String values, Object... params) {
            StringBuilder sb = join(ansi, usageWidth, adjustCJK, new String[] {values}, new StringBuilder(), params);
            return trimLineSeparator(sb.toString()) + new String(spaces(countTrailingSpaces(values)));
        }
        static String trimLineSeparator(String result) {
            return result.endsWith(System.getProperty("line.separator"))
                    ? result.substring(0, result.length() - System.getProperty("line.separator").length()) : result;
        }

        private static char[] spaces(int length) { char[] result = new char[length]; Arrays.fill(result, ' '); return result; }
        private static int countTrailingSpaces(String str) {
            if (str == null) {return 0;}
            int trailingSpaces = 0;
            for (int i = str.length() - 1; i >= 0 && str.charAt(i) == ' '; i--) { trailingSpaces++; }
            return trailingSpaces;
        }

        /**
         * @deprecated Use {@link #join(String[], int, int, String)} instead
         */
        @Deprecated public static StringBuilder join(Ansi ansi, int usageHelpWidth, String[] values, StringBuilder sb, Object... params) {
            return join(ansi, usageHelpWidth, UsageMessageSpec.DEFAULT_ADJUST_CJK, values, sb, params);
        }

        /** Formats each of the specified values and appends it to the specified StringBuilder.
         * @param ansi whether the result should contain ANSI escape codes or not
         * @param usageHelpWidth the width of the usage help message
         * @param adjustCJK true if wide Chinese, Japanese and Korean characters should be counted as double the size of other characters for line-breaking purposes
         * @param values the values to format and append to the StringBuilder
         * @param sb the StringBuilder to collect the formatted strings
         * @param params the parameters to pass to the format method when formatting each value
         * @return the specified StringBuilder
         * @since 4.0 */
        public static StringBuilder join(Ansi ansi, int usageHelpWidth, boolean adjustCJK, String[] values, StringBuilder sb, Object... params) {
            if (values != null) {
                TextTable table = TextTable.forColumnWidths(ansi, usageHelpWidth);
                table.setAdjustLineBreaksForWideCJKCharacters(adjustCJK);
                table.indentWrappedLines = 0;
                for (String summaryLine : values) {
                    Text[] lines = ansi.new Text(format(summaryLine, params)).splitLines();
                    for (Text line : lines) {  table.addRowValues(line); }
                }
                table.toString(sb);
            }
            return sb;
        }
        private int width() { return commandSpec.usageMessage().width(); }
        private boolean adjustCJK() { return commandSpec.usageMessage().adjustLineBreaksForWideCJKCharacters(); }
        /** Returns command custom synopsis as a string. A custom synopsis can be zero or more lines, and can be
         * specified declaratively with the {@link Command#customSynopsis()} annotation attribute or programmatically
         * by setting the Help instance's {@link Help#customSynopsis} field.
         * @param params Arguments referenced by the format specifiers in the synopsis strings
         * @return the custom synopsis lines combined into a single String (which may be empty)
         */
        public String customSynopsis(Object... params) {
            return join(ansi(), width(), adjustCJK(), commandSpec.usageMessage().customSynopsis(), new StringBuilder(), params).toString();
        }
        /** Returns command description text as a string. Description text can be zero or more lines, and can be specified
         * declaratively with the {@link Command#description()} annotation attribute or programmatically by
         * setting the Help instance's {@link Help#description} field.
         * @param params Arguments referenced by the format specifiers in the description strings
         * @return the description lines combined into a single String (which may be empty)
         */
        public String description(Object... params) {
            return join(ansi(), width(), adjustCJK(), commandSpec.usageMessage().description(), new StringBuilder(), params).toString();
        }
        /** Returns the command header text as a string. Header text can be zero or more lines, and can be specified
         * declaratively with the {@link Command#header()} annotation attribute or programmatically by
         * setting the Help instance's {@link Help#header} field.
         * @param params Arguments referenced by the format specifiers in the header strings
         * @return the header lines combined into a single String (which may be empty)
         */
        public String header(Object... params) {
            return join(ansi(), width(), adjustCJK(), commandSpec.usageMessage().header(), new StringBuilder(), params).toString();
        }
        /** Returns command footer text as a string. Footer text can be zero or more lines, and can be specified
         * declaratively with the {@link Command#footer()} annotation attribute or programmatically by
         * setting the Help instance's {@link Help#footer} field.
         * @param params Arguments referenced by the format specifiers in the footer strings
         * @return the footer lines combined into a single String (which may be empty)
         */
        public String footer(Object... params) {
            return join(ansi(), width(), adjustCJK(), commandSpec.usageMessage().footer(), new StringBuilder(), params).toString();
        }

        /** Returns the text displayed before the header text; the result of {@code String.format(headerHeading, params)}.
         * @param params the parameters to use to format the header heading
         * @return the formatted header heading */
        public String headerHeading(Object... params) {
            return heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().headerHeading(), params);
        }

        /** Returns the text displayed before the synopsis text; the result of {@code String.format(synopsisHeading, params)}.
         * @param params the parameters to use to format the synopsis heading
         * @return the formatted synopsis heading */
        public String synopsisHeading(Object... params) {
            return heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().synopsisHeading(), params);
        }

        /** Returns the text displayed before the description text; an empty string if there is no description,
         * otherwise the result of {@code String.format(descriptionHeading, params)}.
         * @param params the parameters to use to format the description heading
         * @return the formatted description heading */
        public String descriptionHeading(Object... params) {
            return empty(commandSpec.usageMessage().descriptionHeading()) ? "" : heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().descriptionHeading(), params);
        }

        /** Returns the text displayed before the positional parameter list; an empty string if there are no positional
         * parameters, otherwise the result of {@code String.format(parameterListHeading, params)}.
         * @param params the parameters to use to format the parameter list heading
         * @return the formatted parameter list heading */
        public String parameterListHeading(Object... params) {
            return commandSpec.positionalParameters().isEmpty() ? "" : heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().parameterListHeading(), params);
        }

        /** Returns the text displayed before the option list; an empty string if there are no options,
         * otherwise the result of {@code String.format(optionListHeading, params)}.
         * @param params the parameters to use to format the option list heading
         * @return the formatted option list heading */
        public String optionListHeading(Object... params) {
            return commandSpec.optionsMap().isEmpty() ? "" : heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().optionListHeading(), params);
        }

        /** Returns the text displayed before the command list; an empty string if there are no commands,
         * otherwise the result of {@code String.format(commandListHeading, params)}.
         * @param params the parameters to use to format the command list heading
         * @return the formatted command list heading */
        public String commandListHeading(Object... params) {
            return commands.isEmpty() ? "" : heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().commandListHeading(), params);
        }

        /** Returns the text displayed before the footer text; the result of {@code String.format(footerHeading, params)}.
         * @param params the parameters to use to format the footer heading
         * @return the formatted footer heading */
        public String footerHeading(Object... params) {
            return heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().footerHeading(), params);
        }

        /** Returns the text displayed before the exit code list text; the result of {@code String.format(exitCodeHeading, params)}.
         * @param params the parameters to use to format the exit code heading
         * @return the formatted heading of the exit code section of the usage help message
         * @since 4.0 */
        public String exitCodeListHeading(Object... params) {
            return heading(ansi(), width(), adjustCJK(), commandSpec.usageMessage().exitCodeListHeading(), params);
        }
        /** Returns a 2-column list with exit codes and their description. Descriptions containing {@code "%n"} line separators are broken up into multiple lines.
         * @return a usage help section describing the exit codes
         * @since 4.0 */
        public String exitCodeList() {
            Map<String, String> map = commandSpec.usageMessage().exitCodeList();
            if (map.isEmpty()) { return ""; }
            int keyLength = maxLength(map.keySet());
            Help.TextTable textTable = Help.TextTable.forColumns(ansi(),
                    new Help.Column(keyLength + 3, 2, Help.Column.Overflow.SPAN),
                    new Help.Column(width() - (keyLength + 3), 2, Help.Column.Overflow.WRAP));
            textTable.setAdjustLineBreaksForWideCJKCharacters(adjustCJK());

            for (Map.Entry<String, String> entry : map.entrySet()) {
                Text[] keys = ansi().text(format(entry.getKey())).splitLines();
                Text[] values = ansi().text(format(entry.getValue())).splitLines();
                for (int i = 0; i < Math.max(keys.length, values.length); i++) {
                    Text key = i < keys.length ? keys[i] : Ansi.EMPTY_TEXT;
                    Text value = i < values.length ? values[i] : Ansi.EMPTY_TEXT;
                    textTable.addRowValues(key, value);
                }
            }
            return textTable.toString();
        }
        /** Returns a 2-column list with command names and the first line of their header or (if absent) description.
         * @return a usage help section describing the added commands */
        public String commandList() {
            if (subcommands().isEmpty()) { return ""; }
            int commandLength = maxLength(subcommands().keySet());
            Help.TextTable textTable = Help.TextTable.forColumns(ansi(),
                    new Help.Column(commandLength + 2, 2, Help.Column.Overflow.SPAN),
                    new Help.Column(width() - (commandLength + 2), 2, Help.Column.Overflow.WRAP));
            textTable.setAdjustLineBreaksForWideCJKCharacters(adjustCJK());

            for (Map.Entry<String, Help> entry : subcommands().entrySet()) {
                Help help = entry.getValue();
                UsageMessageSpec usage = help.commandSpec().usageMessage();
                String header = !empty(usage.header())
                        ? usage.header()[0]
                        : (!empty(usage.description()) ? usage.description()[0] : "");
                Text[] lines = ansi().text(format(header)).splitLines();
                for (int i = 0; i < lines.length; i++) {
                    textTable.addRowValues(i == 0 ? help.commandNamesText(", ") : Ansi.EMPTY_TEXT, lines[i]);
                }
            }
            return textTable.toString();
        }
        private static int maxLength(Collection<String> any) {
            List<String> strings = new ArrayList<String>(any);
            Collections.sort(strings, Collections.reverseOrder(Help.shortestFirst()));
            return strings.get(0).length();
        }

        /** Returns a {@code Text} object containing the command name and all aliases, separated with the specified separator.
         * Command names will use the {@link ColorScheme#commandText(String) command style} for the color scheme of this Help.
         * @since 3.9 */
        public Text commandNamesText(String separator) {
            Text result = colorScheme().commandText(aliases().get(0));
            for (int i = 1; i < aliases().size(); i++) {
                result = result.concat(separator).concat(colorScheme().commandText(aliases().get(i)));
            }
            return result;
        }
        private static String join(String[] names, int offset, int length, String separator) {
            if (names == null) { return ""; }
            StringBuilder result = new StringBuilder();
            for (int i = offset; i < offset + length; i++) {
                result.append((i > offset) ? separator : "").append(names[i]);
            }
            return result.toString();
        }
        private static String stringOf(char chr, int length) {
                             char[] buff = new char[length];
            Arrays.fill(buff, chr);
            return new String(buff);
        }

        /** Returns a {@code Layout} instance configured with the user preferences captured in this Help instance.
         * @return a Layout */
        public Layout createDefaultLayout() {
            return createLayout(Help.defaultOptionsColumnWidth);
        }

        private Layout createLayout(int longOptionsColumnWidth) {
            TextTable tt = TextTable.forDefaultColumns(colorScheme.ansi(), longOptionsColumnWidth, width());
            tt.setAdjustLineBreaksForWideCJKCharacters(commandSpec.usageMessage().adjustLineBreaksForWideCJKCharacters());
            return new Layout(colorScheme, tt, createDefaultOptionRenderer(), createDefaultParameterRenderer());
        }

        /** Returns a new default OptionRenderer which converts {@link OptionSpec Options} to five columns of text to match
         *  the default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>the required option marker</li>
         * <li>2-character short option name (or empty string if no short option exists)</li>
         * <li>comma separator (only if both short option and long option exist, empty string otherwise)</li>
         * <li>comma-separated string with long option name(s)</li>
         * <li>first element of the {@link OptionSpec#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   OptionSpec#description()} array, and these rows look like {@code {"", "", "", "", option.description()[i]}}.</p>
         * <p>If configured, this option renderer adds an additional row to display the default field value.</p>
         * @return a new default OptionRenderer
         */
        public IOptionRenderer createDefaultOptionRenderer() {
            return new DefaultOptionRenderer(commandSpec.usageMessage.showDefaultValues(), "" +commandSpec.usageMessage().requiredOptionMarker());
        }
        /** Returns a new minimal OptionRenderer which converts {@link OptionSpec Options} to a single row with two columns
         * of text: an option name and a description. If multiple names or descriptions exist, the first value is used.
         * @return a new minimal OptionRenderer */
        public static IOptionRenderer createMinimalOptionRenderer() {
            return new MinimalOptionRenderer();
        }

        /** Returns a new default ParameterRenderer which converts {@linkplain PositionalParamSpec positional parameters} to four columns of
         * text to match the default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>empty string </li>
         * <li>empty string </li>
         * <li>parameter(s) label as rendered by the {@link IParamLabelRenderer}</li>
         * <li>first element of the {@link PositionalParamSpec#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   PositionalParamSpec#description()} array, and these rows look like {@code {"", "", "", param.description()[i]}}.</p>
         * <p>If configured, this parameter renderer adds an additional row to display the default field value.</p>
         * @return a new default ParameterRenderer
         */
        public IParameterRenderer createDefaultParameterRenderer() {
            return new DefaultParameterRenderer(commandSpec.usageMessage.showDefaultValues(), "" + commandSpec.usageMessage().requiredOptionMarker());
        }
        /** Returns a new minimal ParameterRenderer which converts {@linkplain PositionalParamSpec positional parameters}
         * to a single row with two columns of text: an option name and a description. If multiple descriptions exist, the first value is used.
         * @return a new minimal ParameterRenderer */
        public static IParameterRenderer createMinimalParameterRenderer() {
            return new MinimalParameterRenderer();
        }

        /** Returns a value renderer that returns the {@code paramLabel} if defined or the field name otherwise.
         * @return a new minimal ParamLabelRenderer */
        public static IParamLabelRenderer createMinimalParamLabelRenderer() {
            return new IParamLabelRenderer() {
                public Text renderParameterLabel(ArgSpec argSpec, Ansi ansi, List<IStyle> styles) {
                    return ansi.apply(argSpec.paramLabel(), styles);
                }
                public String separator() { return ""; }
            };
        }
        /** Returns a new default param label renderer that separates option parameters from their option name
         * with the specified separator string, and, unless {@link ArgSpec#hideParamSyntax()} is true,
         * surrounds optional parameters with {@code '['} and {@code ']'}
         * characters and uses ellipses ("...") to indicate that any number of a parameter are allowed.
         * @return a new default ParamLabelRenderer
         */
        public IParamLabelRenderer createDefaultParamLabelRenderer() {
            return new DefaultParamLabelRenderer(commandSpec);
        }
        /** Sorts {@link OptionSpec OptionSpecs} by their option name in case-insensitive alphabetic order. If an
         * option has multiple names, the shortest name is used for the sorting. Help options follow non-help options.
         * @return a comparator that sorts OptionSpecs by their option name in case-insensitive alphabetic order */
        public static Comparator<OptionSpec> createShortOptionNameComparator() {
            return new SortByShortestOptionNameAlphabetically();
        }
        /** Sorts {@link OptionSpec OptionSpecs} by their option {@linkplain Range#max max arity} first, by
         * {@linkplain Range#min min arity} next, and by {@linkplain #createShortOptionNameComparator() option name} last.
         * @return a comparator that sorts OptionSpecs by arity first, then their option name */
        public static Comparator<OptionSpec> createShortOptionArityAndNameComparator() {
            return new SortByOptionArityAndNameAlphabetically();
        }
        /** Sorts short strings before longer strings.
         * @return a comparators that sorts short strings before longer strings */
        public static Comparator<String> shortestFirst() { return new ShortestFirst(); }
        /** Sorts {@link OptionSpec options} by their option {@linkplain IOrdered#order() order}, lowest first, highest last.
         * @return a comparator that sorts OptionSpecs by their order
         * @since 3.9*/
        static Comparator<OptionSpec> createOrderComparator() {
            return new SortByOrder<OptionSpec>();
        }

        /** Returns whether ANSI escape codes are enabled or not.
         * @return whether ANSI escape codes are enabled or not
         */
        public Ansi ansi() { return colorScheme.ansi; }

        /** Controls the visibility of certain aspects of the usage help message. */
        public enum Visibility { ALWAYS, NEVER, ON_DEMAND }

        /** When customizing online help for {@link OptionSpec Option} details, a custom {@code IOptionRenderer} can be
         * used to create textual representation of an Option in a tabular format: one or more rows, each containing
         * one or more columns. The {@link Layout Layout} is responsible for placing these text values in the
         * {@link TextTable TextTable}. */
        public interface IOptionRenderer {
            /**
             * Returns a text representation of the specified option and its parameter(s) if any.
             * @param option the command line option to show online usage help for
             * @param parameterLabelRenderer responsible for rendering option parameters to text
             * @param scheme color scheme for applying ansi color styles to options and option parameters
             * @return a 2-dimensional array of text values: one or more rows, each containing one or more columns
             * @since 3.0
             */
            Text[][] render(OptionSpec option, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme);
        }
        /** The DefaultOptionRenderer converts {@link OptionSpec Options} to five columns of text to match the default
         * {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>the required option marker (if the option is required)</li>
         * <li>2-character short option name (or empty string if no short option exists)</li>
         * <li>comma separator (only if both short option and long option exist, empty string otherwise)</li>
         * <li>comma-separated string with long option name(s)</li>
         * <li>first element of the {@link OptionSpec#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   OptionSpec#description()} array, and these rows look like {@code {"", "", "", option.description()[i]}}.</p>
         */
        static class DefaultOptionRenderer implements IOptionRenderer {
            private String requiredMarker = " ";
            private boolean showDefaultValues;
            private String sep;
            public DefaultOptionRenderer(boolean showDefaultValues, String requiredMarker) {
                this.showDefaultValues = showDefaultValues;
                this.requiredMarker = Assert.notNull(requiredMarker, "requiredMarker");
            }
            public Text[][] render(OptionSpec option, IParamLabelRenderer paramLabelRenderer, ColorScheme scheme) {
                String[] names = ShortestFirst.sort(option.names());
                int shortOptionCount = names[0].length() == 2 ? 1 : 0;
                String shortOption = shortOptionCount > 0 ? names[0] : "";
                sep = shortOptionCount > 0 && names.length > 1 ? "," : "";

                String longOption = join(names, shortOptionCount, names.length - shortOptionCount, ", ");
                Text longOptionText = createLongOptionText(option, paramLabelRenderer, scheme, longOption);

                String requiredOption = option.required() ? requiredMarker : "";
                return renderDescriptionLines(option, scheme, requiredOption, shortOption, longOptionText);
            }

            private Text createLongOptionText(OptionSpec option, IParamLabelRenderer renderer, ColorScheme scheme, String longOption) {
                Text paramLabelText = renderer.renderParameterLabel(option, scheme.ansi(), scheme.optionParamStyles);

                // if no long option, fill in the space between the short option name and the param label value
                if (paramLabelText.length > 0 && longOption.length() == 0) {
                    sep = renderer.separator();
                    // #181 paramLabelText may be =LABEL or [=LABEL...]
                    int sepStart = paramLabelText.plainString().indexOf(sep);
                    Text prefix = paramLabelText.substring(0, sepStart);
                    paramLabelText = prefix.concat(paramLabelText.substring(sepStart + sep.length()));
                }
                Text longOptionText = scheme.optionText(longOption);
                longOptionText = longOptionText.concat(paramLabelText);
                return longOptionText;
            }

            private Text[][] renderDescriptionLines(OptionSpec option,
                                                    ColorScheme scheme,
                                                    String requiredOption,
                                                    String shortOption,
                                                    Text longOptionText) {
                Text EMPTY = Ansi.EMPTY_TEXT;
                boolean[] showDefault = {option.internalShowDefaultValue(showDefaultValues)};
                List<Text[]> result = new ArrayList<Text[]>();
                String[] description = option.description();
                Text[] descriptionFirstLines = createDescriptionFirstLines(scheme, option, description, showDefault);
                result.add(new Text[] { scheme.optionText(requiredOption), scheme.optionText(shortOption),
                        scheme.ansi().new Text(sep), longOptionText, descriptionFirstLines[0] });
                for (int i = 1; i < descriptionFirstLines.length; i++) {
                    result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, descriptionFirstLines[i] });
                }
                for (int i = 1; i < description.length; i++) {
                    Text[] descriptionNextLines = scheme.ansi().new Text(description[i]).splitLines();
                    for (Text line : descriptionNextLines) {
                        result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, line });
                    }
                }
                if (showDefault[0]) { addTrailingDefaultLine(result, option, scheme); }
                return result.toArray(new Text[result.size()][]);
            }
        }
        /** The MinimalOptionRenderer converts {@link OptionSpec Options} to a single row with two columns of text: an
         * option name and a description. If multiple names or description lines exist, the first value is used. */
        static class MinimalOptionRenderer implements IOptionRenderer {
            public Text[][] render(OptionSpec option, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme) {
                Text optionText = scheme.optionText(option.names()[0]);
                Text paramLabelText = parameterLabelRenderer.renderParameterLabel(option, scheme.ansi(), scheme.optionParamStyles);
                optionText = optionText.concat(paramLabelText);
                return new Text[][] {{ optionText,
                                        scheme.ansi().new Text(option.description().length == 0 ? "" : option.description()[0]) }};
            }
        }
        /** The MinimalParameterRenderer converts {@linkplain PositionalParamSpec positional parameters} to a single row with two columns of
         * text: the parameters label and a description. If multiple description lines exist, the first value is used. */
        static class MinimalParameterRenderer implements IParameterRenderer {
            public Text[][] render(PositionalParamSpec param, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme) {
                return new Text[][] {{ parameterLabelRenderer.renderParameterLabel(param, scheme.ansi(), scheme.parameterStyles),
                        scheme.ansi().new Text(param.description().length == 0 ? "" : param.description()[0]) }};
            }
        }
        /** When customizing online help for {@linkplain PositionalParamSpec positional parameters} details, a custom {@code IParameterRenderer}
         * can be used to create textual representation of a Parameters field in a tabular format: one or more rows,
         * each containing one or more columns. The {@link Layout Layout} is responsible for placing these text
         * values in the {@link TextTable TextTable}. */
        public interface IParameterRenderer {
            /**
             * Returns a text representation of the specified positional parameter.
             * @param param the positional parameter to show online usage help for
             * @param parameterLabelRenderer responsible for rendering parameter labels to text
             * @param scheme color scheme for applying ansi color styles to positional parameters
             * @return a 2-dimensional array of text values: one or more rows, each containing one or more columns
             * @since 3.0
             */
            Text[][] render(PositionalParamSpec param, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme);
        }
        /** The DefaultParameterRenderer converts {@linkplain PositionalParamSpec positional parameters} to five columns of text to match the
         * default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>the required option marker (if the parameter's arity is to have at least one value)</li>
         * <li>empty string </li>
         * <li>empty string </li>
         * <li>parameter(s) label as rendered by the {@link IParamLabelRenderer}</li>
         * <li>first element of the {@link PositionalParamSpec#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   PositionalParamSpec#description()} array, and these rows look like {@code {"", "", "", param.description()[i]}}.</p>
         */
        static class DefaultParameterRenderer implements IParameterRenderer {
            private String requiredMarker = " ";
            private boolean showDefaultValues;
            public DefaultParameterRenderer(boolean showDefaultValues, String requiredMarker) {
                this.showDefaultValues = showDefaultValues;
                this.requiredMarker = Assert.notNull(requiredMarker, "requiredMarker");
            }
            public Text[][] render(PositionalParamSpec param, IParamLabelRenderer paramLabelRenderer, ColorScheme scheme) {
                Text label = paramLabelRenderer.renderParameterLabel(param, scheme.ansi(), scheme.parameterStyles);
                Text requiredParameter = scheme.parameterText(param.arity().min > 0 ? requiredMarker : "");

                Text EMPTY = Ansi.EMPTY_TEXT;
                boolean[] showDefault = {param.internalShowDefaultValue(showDefaultValues)};
                List<Text[]> result = new ArrayList<Text[]>();
                String[] description = param.description();
                Text[] descriptionFirstLines = createDescriptionFirstLines(scheme, param, description, showDefault);
                result.add(new Text[] { requiredParameter, EMPTY, EMPTY, label, descriptionFirstLines[0] });
                for (int i = 1; i < descriptionFirstLines.length; i++) {
                    result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, descriptionFirstLines[i] });
                }
                for (int i = 1; i < description.length; i++) {
                    Text[] descriptionNextLines = scheme.ansi().new Text(description[i]).splitLines();
                    for (Text line : descriptionNextLines) {
                        result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, line });
                    }
                }
                if (showDefault[0]) { addTrailingDefaultLine(result, param, scheme); }
                return result.toArray(new Text[result.size()][]);
            }
        }

        private static void addTrailingDefaultLine(List<Text[]> result, ArgSpec arg, ColorScheme scheme) {
            Text EMPTY = Ansi.EMPTY_TEXT;
            result.add(new Text[]{EMPTY, EMPTY, EMPTY, EMPTY, scheme.ansi().new Text("  Default: " + arg.defaultValueString())});
        }

        private static Text[] createDescriptionFirstLines(ColorScheme scheme, ArgSpec arg, String[] description, boolean[] showDefault) {
            Text[] result = scheme.ansi().new Text(str(description, 0)).splitLines();
            if (result.length == 0 || (result.length == 1 && result[0].plain.length() == 0)) {
                if (showDefault[0]) {
                    result = new Text[]{scheme.ansi().new Text("  Default: " + arg.defaultValueString())};
                    showDefault[0] = false; // don't show the default value twice
                } else {
                    result = new Text[]{ Ansi.EMPTY_TEXT };
                }
            }
            return result;
        }

        /** When customizing online usage help for an option parameter or a positional parameter, a custom
         * {@code IParamLabelRenderer} can be used to render the parameter name or label to a String. */
        public interface IParamLabelRenderer {

            /** Returns a text rendering of the option parameter or positional parameter; returns an empty string
             * {@code ""} if the option is a boolean and does not take a parameter.
             * @param argSpec the named or positional parameter with a parameter label
             * @param ansi determines whether ANSI escape codes should be emitted or not
             * @param styles the styles to apply to the parameter label
             * @return a text rendering of the Option parameter or positional parameter
             * @since 3.0 */
            Text renderParameterLabel(ArgSpec argSpec, Ansi ansi, List<IStyle> styles);

            /** Returns the separator between option name and param label.
             * @return the separator between option name and param label */
            String separator();
        }
        /**
         * DefaultParamLabelRenderer separates option parameters from their {@linkplain OptionSpec option names} with a
         * {@linkplain CommandLine.Model.ParserSpec#separator() separator} string, and, unless
         * {@link ArgSpec#hideParamSyntax()} is true, surrounds optional values with {@code '['} and {@code ']'} characters
         * and uses ellipses ("...") to indicate that any number of values is allowed for options or parameters with variable arity.
         */
        static class DefaultParamLabelRenderer implements IParamLabelRenderer {
            private final CommandSpec commandSpec;
            /** Constructs a new DefaultParamLabelRenderer with the specified separator string. */
            public DefaultParamLabelRenderer(CommandSpec commandSpec) {
                this.commandSpec = Assert.notNull(commandSpec, "commandSpec");
            }
            public String separator() { return commandSpec.parser().separator(); }
            public Text renderParameterLabel(ArgSpec argSpec, Ansi ansi, List<IStyle> styles) {
                Range capacity = argSpec.isOption() ? argSpec.arity() : ((PositionalParamSpec)argSpec).capacity();
                if (capacity.max == 0) { return ansi.new Text(""); }
                if (argSpec.hideParamSyntax()) { return ansi.apply((argSpec.isOption() ? separator() : "") + argSpec.paramLabel(), styles); }
                
                Text paramName = ansi.apply(argSpec.paramLabel(), styles);
                String split = argSpec.splitRegex();
                String mandatorySep = empty(split) ? " "  : split;
                String optionalSep  = empty(split) ? " [" : "[" + split;

                boolean unlimitedSplit = !empty(split) && !commandSpec.parser().limitSplit();
                boolean limitedSplit =   !empty(split) &&  commandSpec.parser().limitSplit();
                Text repeating = paramName;
                int paramCount = 1;
                if (unlimitedSplit) {
                    repeating = paramName.concat("[" + split).concat(paramName).concat("...]");
                    paramCount++;
                    mandatorySep = " ";
                    optionalSep = " [";
                }
                Text result = repeating;

                int done = 1;
                for (; done < capacity.min; done++) {
                    result = result.concat(mandatorySep).concat(repeating); // " PARAM" or ",PARAM"
                    paramCount += paramCount;
                }
                if (!capacity.isVariable) {
                    for (int i = done; i < capacity.max; i++) {
                        result = result.concat(optionalSep).concat(paramName); // " [PARAM" or "[,PARAM"
                        paramCount++;
                    }
                    for (int i = done; i < capacity.max; i++) {
                        result = result.concat("]");
                    }
                }
                // show an extra trailing "[,PARAM]" if split and either max=* or splitting is not restricted to max
                boolean effectivelyVariable = capacity.isVariable || (limitedSplit && paramCount == 1);
                if (limitedSplit && effectivelyVariable && paramCount == 1) {
                    result = result.concat(optionalSep).concat(repeating).concat("]"); // PARAM[,PARAM]...
                }
                if (effectivelyVariable) {
                    if (!argSpec.arity().isVariable && argSpec.arity().min > 1) {
                        result = ansi.new Text("(").concat(result).concat(")"); // repeating group
                    }
                    result = result.concat("..."); // PARAM...
                }
                String optionSeparator = argSpec.isOption() ? separator() : "";
                if (capacity.min == 0) { // optional
                    String sep2 = empty(optionSeparator.trim()) ? optionSeparator + "[" : "[" + optionSeparator;
                    result = ansi.new Text(sep2).concat(result).concat("]");
                } else {
                    result = ansi.new Text(optionSeparator).concat(result);
                }
                return result;
            }
        }
        /** Use a Layout to format usage help text for options and parameters in tabular format.
         * <p>Delegates to the renderers to create {@link Text} values for the annotated fields, and uses a
         * {@link TextTable} to display these values in tabular format. Layout is responsible for deciding which values
         * to display where in the table. By default, Layout shows one option or parameter per table row.</p>
         * <p>Customize by overriding the {@link #layout(CommandLine.Model.ArgSpec, CommandLine.Help.Ansi.Text[][])} method.</p>
         * @see IOptionRenderer rendering options to text
         * @see IParameterRenderer rendering parameters to text
         * @see TextTable showing values in a tabular format
         */
        public static class Layout {
            protected final ColorScheme colorScheme;
            protected final TextTable table;
            protected IOptionRenderer optionRenderer;
            protected IParameterRenderer parameterRenderer;

            /** Constructs a Layout with the specified color scheme, a new default TextTable, the
             * {@linkplain Help#createDefaultOptionRenderer() default option renderer}, and the
             * {@linkplain Help#createDefaultParameterRenderer() default parameter renderer}.
             * @param colorScheme the color scheme to use for common, auto-generated parts of the usage help message */
            public Layout(ColorScheme colorScheme, int tableWidth) { this(colorScheme,
                    TextTable.forDefaultColumns(colorScheme.ansi(), tableWidth)); }

            /** Constructs a Layout with the specified color scheme, the specified TextTable, the
             * {@linkplain Help#createDefaultOptionRenderer() default option renderer}, and the
             * {@linkplain Help#createDefaultParameterRenderer() default parameter renderer}.
             * @param colorScheme the color scheme to use for common, auto-generated parts of the usage help message
             * @param textTable the TextTable to lay out parts of the usage help message in tabular format */
            public Layout(ColorScheme colorScheme, TextTable textTable) {
                this(colorScheme, textTable, new DefaultOptionRenderer(false, " "), new DefaultParameterRenderer(false, " "));
            }
            /** Constructs a Layout with the specified color scheme, the specified TextTable, the
             * specified option renderer and the specified parameter renderer.
             * @param colorScheme the color scheme to use for common, auto-generated parts of the usage help message
             * @param optionRenderer the object responsible for rendering Options to Text
             * @param parameterRenderer the object responsible for rendering Parameters to Text
             * @param textTable the TextTable to lay out parts of the usage help message in tabular format */
            public Layout(ColorScheme colorScheme, TextTable textTable, IOptionRenderer optionRenderer, IParameterRenderer parameterRenderer) {
                this.colorScheme       = Assert.notNull(colorScheme, "colorScheme");
                this.table             = Assert.notNull(textTable, "textTable");
                this.optionRenderer    = Assert.notNull(optionRenderer, "optionRenderer");
                this.parameterRenderer = Assert.notNull(parameterRenderer, "parameterRenderer");
            }
            /**
             * Copies the specified text values into the correct cells in the {@link TextTable}. This implementation
             * delegates to {@link TextTable#addRowValues(CommandLine.Help.Ansi.Text...)} for each row of values.
             * <p>Subclasses may override.</p>
             * @param argSpec the Option or Parameters
             * @param cellValues the text values representing the Option/Parameters, to be displayed in tabular form
             * @since 3.0 */
            public void layout(ArgSpec argSpec, Text[][] cellValues) {
                for (Text[] oneRow : cellValues) {
                    table.addRowValues(oneRow);
                }
            }
            /** Calls {@link #addOption(CommandLine.Model.OptionSpec, CommandLine.Help.IParamLabelRenderer)} for all non-hidden Options in the list.
             * @param options options to add usage descriptions for
             * @param paramLabelRenderer object that knows how to render option parameters
             * @since 3.0 */
            public void addOptions(List<OptionSpec> options, IParamLabelRenderer paramLabelRenderer) {
                for (OptionSpec option : options) {
                    if (!option.hidden()) {
                        addOption(option, paramLabelRenderer);
                    }
                }
            }
            /**
             * Delegates to the {@link #optionRenderer option renderer} of this layout to obtain
             * text values for the specified {@link OptionSpec}, and then calls the {@link #layout(CommandLine.Model.ArgSpec, CommandLine.Help.Ansi.Text[][])}
             * method to write these text values into the correct cells in the TextTable.
             * @param option the option argument
             * @param paramLabelRenderer knows how to render option parameters
             * @since 3.0 */
            public void addOption(OptionSpec option, IParamLabelRenderer paramLabelRenderer) {
                Text[][] values = optionRenderer.render(option, paramLabelRenderer, colorScheme);
                layout(option, values);
            }
            /** Calls {@link #addPositionalParameter(CommandLine.Model.PositionalParamSpec, CommandLine.Help.IParamLabelRenderer)} for all non-hidden Parameters in the list.
             * @param params positional parameters to add usage descriptions for
             * @param paramLabelRenderer knows how to render option parameters
             * @since 3.0 */
            public void addPositionalParameters(List<PositionalParamSpec> params, IParamLabelRenderer paramLabelRenderer) {
                for (PositionalParamSpec param : params) {
                    if (!param.hidden()) {
                        addPositionalParameter(param, paramLabelRenderer);
                    }
                }
            }
            /**
             * Delegates to the {@link #parameterRenderer parameter renderer} of this layout
             * to obtain text values for the specified {@linkplain PositionalParamSpec positional parameter}, and then calls
             * {@link #layout(CommandLine.Model.ArgSpec, CommandLine.Help.Ansi.Text[][])} to write these text values into the correct cells in the TextTable.
             * @param param the positional parameter
             * @param paramLabelRenderer knows how to render option parameters
             * @since 3.0 */
            public void addPositionalParameter(PositionalParamSpec param, IParamLabelRenderer paramLabelRenderer) {
                Text[][] values = parameterRenderer.render(param, paramLabelRenderer, colorScheme);
                layout(param, values);
            }
            /** Returns the section of the usage help message accumulated in the TextTable owned by this layout. */
            @Override public String toString() { return table.toString(); }
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
            /** Sorts the specified array of Strings longest-first and returns it. */
            public static String[] longestFirst(String[] names) {
                Arrays.sort(names, Collections.reverseOrder(new ShortestFirst()));
                return names;
            }
        }
        /** Sorts {@code OptionSpec} instances by their name in case-insensitive alphabetic order. If an option has
         * multiple names, the shortest name is used for the sorting. Help options follow non-help options. */
        static class SortByShortestOptionNameAlphabetically implements Comparator<OptionSpec> {
            public int compare(OptionSpec o1, OptionSpec o2) {
                if (o1 == null) { return 1; } else if (o2 == null) { return -1; } // options before params
                String[] names1 = ShortestFirst.sort(o1.names());
                String[] names2 = ShortestFirst.sort(o2.names());
                int result = names1[0].toUpperCase().compareTo(names2[0].toUpperCase()); // case insensitive sort
                result = result == 0 ? -names1[0].compareTo(names2[0]) : result; // lower case before upper case
                return o1.help() == o2.help() ? result : o2.help() ? -1 : 1; // help options come last
            }
        }
        /** Sorts {@code OptionSpec} instances by their max arity first, then their min arity, then delegates to super class. */
        static class SortByOptionArityAndNameAlphabetically extends SortByShortestOptionNameAlphabetically {
            public int compare(OptionSpec o1, OptionSpec o2) {
                Range arity1 = o1.arity();
                Range arity2 = o2.arity();
                int result = arity1.max - arity2.max;
                if (result == 0) {
                    result = arity1.min - arity2.min;
                }
                if (result == 0) { // arity is same
                    if (o1.isMultiValue() && !o2.isMultiValue()) { result = 1; } // f1 > f2
                    if (!o1.isMultiValue() && o2.isMultiValue()) { result = -1; } // f1 < f2
                }
                return result == 0 ? super.compare(o1, o2) : result;
            }
        }
        static class SortByOrder<T extends IOrdered> implements Comparator<T> {
            public int compare(T o1, T o2) {
                return Integer.signum(o1.order() - o2.order());
            }
        }
        /**
         * <p>Responsible for spacing out {@link Text} values according to the {@link Column} definitions the table was
         * created with. Columns have a width, indentation, and an overflow policy that decides what to do if a value is
         * longer than the column's width.</p>
         */
        public static class TextTable {
            /**
             * Helper class to index positions in a {@code Help.TextTable}.
             * @since 2.0
             */
            public static class Cell {
                /** Table column index (zero based). */
                public final int column;
                /** Table row index (zero based). */
                public final int row;
                /** Constructs a new Cell with the specified coordinates in the table.
                 * @param column the zero-based table column
                 * @param row the zero-based table row */
                public Cell(int column, int row) { this.column = column; this.row = row; }
            }

            private static final int OPTION_SEPARATOR_COLUMN = 2;
            private static final int LONG_OPTION_COLUMN = 3;

            /** The column definitions of this table. */
            private final Column[] columns;

            /** The {@code char[]} slots of the {@code TextTable} to copy text values into. */
            protected final List<Text> columnValues = new ArrayList<Text>();

            /** By default, indent wrapped lines by 2 spaces. */
            public int indentWrappedLines = 2;

            private final Ansi ansi;
            private final int tableWidth;
            private boolean adjustLineBreaksForWideCJKCharacters = true;

            /** Constructs a TextTable with five columns as follows:
             * <ol>
             * <li>required option/parameter marker (width: 2, indent: 0, TRUNCATE on overflow)</li>
             * <li>short option name (width: 2, indent: 0, TRUNCATE on overflow)</li>
             * <li>comma separator (width: 1, indent: 0, TRUNCATE on overflow)</li>
             * <li>long option name(s) (width: 24, indent: 1, SPAN multiple columns on overflow)</li>
             * <li>description line(s) (width: 51, indent: 1, WRAP to next row on overflow)</li>
             * </ol>
             * @param ansi whether to emit ANSI escape codes or not
             * @param usageHelpWidth the total width of the columns combined
             */
            public static TextTable forDefaultColumns(Ansi ansi, int usageHelpWidth) {
                return forDefaultColumns(ansi, defaultOptionsColumnWidth, usageHelpWidth);
            }

            /** Constructs a TextTable with five columns as follows:
             * <ol>
             * <li>required option/parameter marker (width: 2, indent: 0, TRUNCATE on overflow)</li>
             * <li>short option name (width: 2, indent: 0, TRUNCATE on overflow)</li>
             * <li>comma separator (width: 1, indent: 0, TRUNCATE on overflow)</li>
             * <li>long option name(s) (width: 24, indent: 1, SPAN multiple columns on overflow)</li>
             * <li>description line(s) (width: 51, indent: 1, WRAP to next row on overflow)</li>
             * </ol>
             * @param ansi whether to emit ANSI escape codes or not
             * @param longOptionsColumnWidth the width of the long options column
             * @param usageHelpWidth the total width of the columns combined
             */
            public static TextTable forDefaultColumns(Ansi ansi, int longOptionsColumnWidth, int usageHelpWidth) {
                // "* -c, --create                Creates a ...."
                return forColumns(ansi,
                        new Column(2,                                       0, TRUNCATE), // "*"
                        new Column(2,                                       0, TRUNCATE), // "-c"
                        new Column(1,                                       0, TRUNCATE), // ","
                        new Column(longOptionsColumnWidth,                         1, SPAN),  // " --create"
                        new Column(usageHelpWidth - longOptionsColumnWidth, 1, WRAP)); // " Creates a ..."
            }

            /** Constructs a new TextTable with columns with the specified width, all SPANning  multiple columns on
             * overflow except the last column which WRAPS to the next row.
             * @param ansi whether to emit ANSI escape codes or not
             * @param columnWidths the width of each table column (all columns have zero indent)
             */
            public static TextTable forColumnWidths(Ansi ansi, int... columnWidths) {
                Column[] columns = new Column[columnWidths.length];
                for (int i = 0; i < columnWidths.length; i++) {
                    columns[i] = new Column(columnWidths[i], 0, i == columnWidths.length - 1 ? WRAP : SPAN);
                }
                return new TextTable(ansi, columns);
            }
            /** Constructs a {@code TextTable} with the specified columns.
             * @param ansi whether to emit ANSI escape codes or not
             * @param columns columns to construct this TextTable with */
            public static TextTable forColumns(Ansi ansi, Column... columns) { return new TextTable(ansi, columns); }
            protected TextTable(Ansi ansi, Column[] columns) {
                this.ansi = Assert.notNull(ansi, "ansi");
                this.columns = Assert.notNull(columns, "columns").clone();
                if (columns.length == 0) { throw new IllegalArgumentException("At least one column is required"); }
                int totalWidth = 0;
                for (Column col : columns) { totalWidth += col.width; }
                tableWidth = totalWidth;
            }
            /** @see UsageMessageSpec#adjustLineBreaksForWideCJKCharacters()
             * @since 4.0 */
            public boolean isAdjustLineBreaksForWideCJKCharacters() { return adjustLineBreaksForWideCJKCharacters; }
            /** @see UsageMessageSpec#adjustLineBreaksForWideCJKCharacters(boolean)
             * @since 4.0 */
            public TextTable setAdjustLineBreaksForWideCJKCharacters(boolean adjustLineBreaksForWideCJKCharacters) {
                this.adjustLineBreaksForWideCJKCharacters = adjustLineBreaksForWideCJKCharacters;
                return this;
            }
            /** The column definitions of this table. */
            public Column[] columns() { return columns.clone(); }
            /** Returns the {@code Text} slot at the specified row and column to write a text value into.
             * @param row the row of the cell whose Text to return
             * @param col the column of the cell whose Text to return
             * @return the Text object at the specified row and column
             * @since 2.0 */
            public Text textAt(int row, int col) { return columnValues.get(col + (row * columns.length)); }

            /** Returns the {@code Text} slot at the specified row and column to write a text value into.
             * @param row the row of the cell whose Text to return
             * @param col the column of the cell whose Text to return
             * @return the Text object at the specified row and column
             * @deprecated use {@link #textAt(int, int)} instead */
            @Deprecated public Text cellAt(int row, int col) { return textAt(row, col); }

            /** Returns the current number of rows of this {@code TextTable}.
             * @return the current number of rows in this TextTable */
            public int rowCount() { return columnValues.size() / columns.length; }

            /** Adds the required {@code char[]} slots for a new row to the {@link #columnValues} field. */
            public void addEmptyRow() {
                for (int i = 0; i < columns.length; i++) {
                    columnValues.add(ansi.new Text(columns[i].width));
                }
            }

            /** Delegates to {@link #addRowValues(CommandLine.Help.Ansi.Text...)}.
             * @param values the text values to display in each column of the current row */
            public void addRowValues(String... values) {
                Text[] array = new Text[values.length];
                for (int i = 0; i < array.length; i++) {
                    array[i] = values[i] == null ? Ansi.EMPTY_TEXT : ansi.new Text(values[i]);
                }
                addRowValues(array);
            }
            /**
             * Adds a new {@linkplain TextTable#addEmptyRow() empty row}, then calls {@link
             * TextTable#putValue(int, int, CommandLine.Help.Ansi.Text) putValue} for each of the specified values, adding more empty rows
             * if the return value indicates that the value spanned multiple columns or was wrapped to multiple rows.
             * @param values the values to write into a new row in this TextTable
             * @throws IllegalArgumentException if the number of values exceeds the number of Columns in this table
             */
            public void addRowValues(Text... values) {
                if (values.length > columns.length) {
                    throw new IllegalArgumentException(values.length + " values don't fit in " +
                            columns.length + " columns");
                }
                addEmptyRow();
                int oldIndent = unindent(values);
                for (int col = 0; col < values.length; col++) {
                    int row = rowCount() - 1;// write to last row: previous value may have wrapped to next row
                    Cell cell = putValue(row, col, values[col]);

                    // add row if a value spanned/wrapped and there are still remaining values
                    if ((cell.row != row || cell.column != col) && col != values.length - 1) {
                        addEmptyRow();
                    }
                }
                reindent(oldIndent);
            }
            private int unindent(Text[] values) {
                if (columns.length <= LONG_OPTION_COLUMN) { return 0; }
                int oldIndent = columns[LONG_OPTION_COLUMN].indent;
                if ("=".equals(values[OPTION_SEPARATOR_COLUMN].toString())) {
                    columns[LONG_OPTION_COLUMN].indent = 0;
                }
                return oldIndent;
            }
            private void reindent(int oldIndent) {
                if (columns.length <= LONG_OPTION_COLUMN) { return; }
                columns[LONG_OPTION_COLUMN].indent = oldIndent;
            }

            /**
             * Writes the specified value into the cell at the specified row and column and returns the last row and
             * column written to. Depending on the Column's {@link Column#overflow Overflow} policy, the value may span
             * multiple columns or wrap to multiple rows when larger than the column width.
             * @param row the target row in the table
             * @param col the target column in the table to write to
             * @param value the value to write
             * @return a Cell indicating the position in the table that was last written to (since 2.0)
             * @throws IllegalArgumentException if the specified row exceeds the table's {@linkplain
             *          TextTable#rowCount() row count}
             * @since 2.0 (previous versions returned a {@code java.awt.Point} object)
             */
            public Cell putValue(int row, int col, Text value) {
                if (row > rowCount() - 1) {
                    throw new IllegalArgumentException("Cannot write to row " + row + ": rowCount=" + rowCount());
                }
                if (value == null || value.plain.length() == 0) { return new Cell(col, row); }
                Column column = columns[col];
                int indent = column.indent;
                switch (column.overflow) {
                    case TRUNCATE:
                        copy(value, textAt(row, col), indent);
                        return new Cell(col, row);
                    case SPAN:
                        int startColumn = col;
                        do {
                            boolean lastColumn = col == columns.length - 1;
                            int charsWritten = lastColumn
                                    ? copy(BreakIterator.getLineInstance(), value, textAt(row, col), indent)
                                    : copy(value, textAt(row, col), indent);
                            value = value.substring(charsWritten);
                            indent = 0;
                            if (value.length > 0) { // value did not fit in column
                                ++col;                // write remainder of value in next column
                            }
                            if (value.length > 0 && col >= columns.length) { // we filled up all columns on this row
                                addEmptyRow();
                                row++;
                                col = startColumn;
                                indent = column.indent + indentWrappedLines;
                            }
                        } while (value.length > 0);
                        return new Cell(col, row);
                    case WRAP:
                        BreakIterator lineBreakIterator = BreakIterator.getLineInstance();
                        do {
                            int charsWritten = copy(lineBreakIterator, value, textAt(row, col), indent);
                            value = value.substring(charsWritten);
                            indent = column.indent + indentWrappedLines;
                            if (value.length > 0) {  // value did not fit in column
                                ++row;                 // write remainder of value in next row
                                addEmptyRow();
                            }
                        } while (value.length > 0);
                        return new Cell(col, row);
                }
                throw new IllegalStateException(column.overflow.toString());
            }
            private int length(Text str) {
                return length(str, str.from, str.length);
            }
            private int length(Text str, int from, int length) {
                int result = 0;
                for (int i = from; i < str.from + length; i++) {
                    result += isCharCJK(str.plain.charAt(i)) ? 2 : 1;
                }
                return result;
            }

            /**
             * Given a character, is this character considered to be a CJK character?
             * Shamelessly stolen from
             * <a href="http://stackoverflow.com/questions/1499804/how-can-i-detect-japanese-text-in-a-java-string">StackOverflow</a>
             * where it was contributed by user Rakesh N. (Upvote! :-) )
             * @param c Character to test
             * @return {@code true} if the character is a CJK character
             */
            boolean isCharCJK(char c) {
                if (!adjustLineBreaksForWideCJKCharacters) { return false; }
                Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
                return (unicodeBlock == Character.UnicodeBlock.HIRAGANA)
                        || (unicodeBlock == Character.UnicodeBlock.KATAKANA)
                        || (unicodeBlock == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS)
                        || (unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO)
                        || (unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO)
                        || (unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
                        || (unicodeBlock == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
                        || (unicodeBlock == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS)
                        //The magic number here is the separating index between full-width and half-width
                        || (unicodeBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS && c < 0xFF61);
            }

            static class Count {
                int charCount;
                int columnCount;
            }
            private int copy(BreakIterator line, Text text, Text columnValue, int offset) {
                // Deceive the BreakIterator to ensure no line breaks after '-' character
                line.setText(text.plainString().replace("-", "\u00ff"));
                Count count = new Count();
                for (int start = line.first(), end = line.next(); end != BreakIterator.DONE; start = end, end = line.next()) {
                    Text word = text.substring(start, end); //.replace("\u00ff", "-"); // not needed
                    if (columnValue.maxLength >= offset + count.columnCount + length(word)) {
                        copy(word, columnValue, offset + count.charCount, count);
                    } else {
                        break;
                    }
                }
                if (count.charCount == 0 && length(text) + offset > columnValue.maxLength) {
                    // The value is a single word that is too big to be written to the column. Write as much as we can.
                    copy(text, columnValue, offset, count);
                }
                return count.charCount;
            }
            private int copy(Text value, Text destination, int offset) {
                Count count = new Count();
                copy(value, destination, offset, count);
                return count.charCount;
            }
            private void copy(Text value, Text destination, int offset, Count count) {
                int length = Math.min(value.length, destination.maxLength - offset);
                value.getStyledChars(value.from, length, destination, offset);
                count.columnCount += length(value, value.from, length);
                count.charCount += length;
            }

            /** Copies the text representation that we built up from the options into the specified StringBuilder.
             * @param text the StringBuilder to write into
             * @return the specified StringBuilder object (to allow method chaining and a more fluid API) */
            public StringBuilder toString(StringBuilder text) {
                int columnCount = this.columns.length;
                StringBuilder row = new StringBuilder(tableWidth);
                for (int i = 0; i < columnValues.size(); i++) {
                    Text column = columnValues.get(i);
                    row.append(column.toString());
                    row.append(new String(spaces(columns[i % columnCount].width - column.length)));
                    if (i % columnCount == columnCount - 1) {
                        int lastChar = row.length() - 1;
                        while (lastChar >= 0 && row.charAt(lastChar) == ' ') {lastChar--;} // rtrim
                        row.setLength(lastChar + 1);
                        text.append(row.toString()).append(System.getProperty("line.separator"));
                        row.setLength(0);
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
            public int indent;

            /** Policy that determines how to handle values larger than the column width. */
            public final Overflow overflow;
            public Column(int width, int indent, Overflow overflow) {
                this.width = width;
                this.indent = indent;
                this.overflow = Assert.notNull(overflow, "overflow");
            }
        }

        /** All usage help message are generated with a color scheme that assigns certain styles and colors to common
         * parts of a usage message: the command name, options, positional parameters and option parameters.
         * Users may customize these styles by creating Help with a custom color scheme.
         * <p>Note that these options and styles may not be rendered if ANSI escape codes are not
         * {@linkplain Ansi#enabled() enabled}.</p>
         * <p>From 4.0, instances of this class are immutable.</p>
         * @see Builder
         * @see Help#defaultColorScheme(Ansi)
         */
        public static class ColorScheme {
            private final List<IStyle> commandStyles;
            private final List<IStyle> optionStyles;
            private final List<IStyle> parameterStyles;
            private final List<IStyle> optionParamStyles;
            private final Ansi ansi;

            /** Constructs a new empty ColorScheme with the specified Ansi enabled mode.
             * @see Help#defaultColorScheme(Ansi)
             * @param builder contains the color scheme attributes to use
             */
            ColorScheme(ColorScheme.Builder builder) {
                Assert.notNull(builder, "builder");
                this.ansi         = Assert.notNull(builder.ansi(), "ansi");
                commandStyles     = Collections.unmodifiableList(new ArrayList<IStyle>(builder.commandStyles()));
                optionStyles      = Collections.unmodifiableList(new ArrayList<IStyle>(builder.optionStyles()));
                parameterStyles   = Collections.unmodifiableList(new ArrayList<IStyle>(builder.parameterStyles()));
                optionParamStyles = Collections.unmodifiableList(new ArrayList<IStyle>(builder.optionParamStyles()));
            }
            /** Returns a Text with all command styles applied to the specified command string.
             * @param command the command string to apply the registered command styles to
             * @return a Text with all command styles applied to the specified command string */
            public Ansi.Text commandText(String command)         { return ansi().apply(command,     commandStyles); }
            /** Returns a Text with all option styles applied to the specified option string.
             * @param option the option string to apply the registered option styles to
             * @return a Text with all option styles applied to the specified option string */
            public Ansi.Text optionText(String option)           { return ansi().apply(option,      optionStyles); }
            /** Returns a Text with all parameter styles applied to the specified parameter string.
             * @param parameter the parameter string to apply the registered parameter styles to
             * @return a Text with all parameter styles applied to the specified parameter string */
            public Ansi.Text parameterText(String parameter)     { return ansi().apply(parameter,   parameterStyles); }
            /** Returns a Text with all optionParam styles applied to the specified optionParam string.
             * @param optionParam the option parameter string to apply the registered option parameter styles to
             * @return a Text with all option parameter styles applied to the specified option parameter string */
            public Ansi.Text optionParamText(String optionParam) { return ansi().apply(optionParam, optionParamStyles); }

            /** Returns the {@code Ansi} setting of this color scheme. */
            public Ansi ansi() { return ansi; }
            /** Returns the registered styles for commands in this color scheme.
             * @since 4.0 */
            public List<IStyle> commandStyles()     { return commandStyles; }
            /** Returns the registered styles for options in this color scheme.
             * @since 4.0 */
            public List<IStyle> optionStyles()      { return optionStyles; }
            /** Returns the registered styles for positional parameters in this color scheme.
             * @since 4.0 */
            public List<IStyle> parameterStyles()   { return parameterStyles; }
            /** Returns the registered styles for option parameters in this color scheme.
             * @since 4.0 */
            public List<IStyle> optionParamStyles() { return optionParamStyles; }

            @Override public boolean equals(Object obj) {
                if (this == obj) { return true; }
                if (!(obj instanceof ColorScheme)) { return false; }
                ColorScheme other = (ColorScheme) obj;
                return ansi.equals(other.ansi)
                        && commandStyles.equals(other.commandStyles)
                        && optionStyles.equals(other.optionStyles)
                        && parameterStyles.equals(other.parameterStyles)
                        && optionParamStyles.equals(other.optionParamStyles);
            }
            @Override public int hashCode() {
                int result = 17;
                result = result * 37 + ansi.hashCode();
                result = result * 37 + commandStyles.hashCode();
                result = result * 37 + optionStyles.hashCode();
                result = result * 37 + parameterStyles.hashCode();
                result = result * 37 + optionParamStyles.hashCode();
                return result;
            }

            /** Builder class to create {@code ColorScheme} instances.
             * @since 4.0 */
            public static class Builder {
                private final List<IStyle> commandStyles = new ArrayList<IStyle>();
                private final List<IStyle> optionStyles = new ArrayList<IStyle>();
                private final List<IStyle> parameterStyles = new ArrayList<IStyle>();
                private final List<IStyle> optionParamStyles = new ArrayList<IStyle>();
                private Ansi ansi = Ansi.AUTO;

                /** Constructs an empty color scheme builder with Ansi.AUTO. */
                public Builder() { }
                /** Constructs an empty color scheme builder with the specified Ansi value. */
                public Builder(Ansi ansi) { this.ansi = Assert.notNull(ansi, "ansi"); }
                /** Constructs a color scheme builder with all attributes copied from the specified color scheme. */
                public Builder(ColorScheme existing) {
                    Assert.notNull(existing, "colorScheme");
                    this.ansi = Assert.notNull(existing.ansi(), "ansi");
                    this.commandStyles.addAll(existing.commandStyles());
                    this.optionStyles.addAll(existing.optionStyles());
                    this.parameterStyles.addAll(existing.parameterStyles());
                    this.optionParamStyles.addAll(existing.optionParamStyles());
                }
                /** Returns the {@code Ansi} setting of this color scheme builder. */
                public Ansi ansi() { return ansi; }
                /** Returns the {@code Ansi} setting of this color scheme builder. */
                public ColorScheme.Builder ansi(Ansi ansi) { this.ansi = Assert.notNull(ansi, "ansi"); return this; }
                /** Returns the registered styles for commands in this color scheme builder. */
                public List<IStyle> commandStyles()     { return commandStyles; }
                /** Returns the registered styles for options in this color scheme builder. */
                public List<IStyle> optionStyles()      { return optionStyles; }
                /** Returns the registered styles for positional parameters in this color scheme builder. */
                public List<IStyle> parameterStyles()   { return parameterStyles; }
                /** Returns the registered styles for option parameters in this color scheme builder. */
                public List<IStyle> optionParamStyles() { return optionParamStyles; }

                /** Adds the specified styles to the registered styles for commands in this color scheme builder and returns this builder.
                 * @param styles the styles to add to the registered styles for commands in this color scheme builder
                 * @return this color scheme builder to enable method chaining for a more fluent API */
                public ColorScheme.Builder commands(IStyle... styles)     { return addAll(commandStyles, styles); }
                /** Adds the specified styles to the registered styles for options in this color scheme and returns this color scheme.
                 * @param styles the styles to add to registered the styles for options in this color scheme builder
                 * @return this color scheme builder to enable method chaining for a more fluent API */
                public ColorScheme.Builder options(IStyle... styles)      { return addAll(optionStyles, styles);}
                /** Adds the specified styles to the registered styles for positional parameters in this color scheme builder and returns this builder.
                 * @param styles the styles to add to registered the styles for parameters in this color scheme builder
                 * @return this color scheme builder to enable method chaining for a more fluent API */
                public ColorScheme.Builder parameters(IStyle... styles)   { return addAll(parameterStyles, styles);}
                /** Adds the specified styles to the registered styles for option parameters in this color scheme builder and returns this builder.
                 * @param styles the styles to add to the registered styles for option parameters in this color scheme builder
                 * @return this color scheme builder to enable method chaining for a more fluent API */
                public ColorScheme.Builder optionParams(IStyle... styles) { return addAll(optionParamStyles, styles);}

                /** Replaces colors and styles in this scheme builder with ones specified in system properties, and returns this builder.
                 * Supported property names:<ul>
                 *     <li>{@code picocli.color.commands}</li>
                 *     <li>{@code picocli.color.options}</li>
                 *     <li>{@code picocli.color.parameters}</li>
                 *     <li>{@code picocli.color.optionParams}</li>
                 * </ul><p>Property values can be anything that {@link Help.Ansi.Style#parse(String)} can handle.</p>
                 * @return this ColorScheme builder
                 */
                public ColorScheme.Builder applySystemProperties() {
                    replace(commandStyles,     System.getProperty("picocli.color.commands"));
                    replace(optionStyles,      System.getProperty("picocli.color.options"));
                    replace(parameterStyles,   System.getProperty("picocli.color.parameters"));
                    replace(optionParamStyles, System.getProperty("picocli.color.optionParams"));
                    return this;
                }
                private void replace(List<IStyle> styles, String property) {
                    if (property != null) {
                        styles.clear();
                        addAll(styles, Style.parse(property));
                    }
                }
                private ColorScheme.Builder addAll(List<IStyle> styles, IStyle... add) {
                    styles.addAll(Arrays.asList(add));
                    return this;
                }
                /** Creates and returns a new {@code ColorScheme} with the values configured on this builder. */
                public ColorScheme build() { return new ColorScheme(this); }
            }
        }

        /** Creates and returns a new {@link ColorScheme} initialized with picocli default values: commands are bold,
         *  options and parameters use a yellow foreground, and option parameters use italic.
         * @param ansi whether the usage help message should contain ANSI escape codes or not
         * @return a new default color scheme
         */
        public static ColorScheme defaultColorScheme(Ansi ansi) {
            return new ColorScheme.Builder(ansi)
                    .commands(Style.bold)
                    .options(Style.fg_yellow)
                    .parameters(Style.fg_yellow)
                    .optionParams(Style.italic).build();
        }

        /** Provides methods and inner classes to support using ANSI escape codes in usage help messages. */
        public enum Ansi {
            /** Only emit ANSI escape codes if the platform supports it and system property {@code "picocli.ansi"}
             * is not set to any value other than {@code "true"} (case insensitive). */
            AUTO,
            /** Forced ON: always emit ANSI escape code regardless of the platform. */
            ON,
            /** Forced OFF: never emit ANSI escape code regardless of the platform. */
            OFF;
            static Text EMPTY_TEXT = OFF.new Text(0);

            static Boolean tty;
            static boolean isTTY() {
                if (tty == null) { tty = calcTTY(); }
                return tty;
            }
            static final boolean isWindows()    { return System.getProperty("os.name").startsWith("Windows"); }
            static final boolean isXterm()      { return System.getenv("TERM") != null && System.getenv("TERM").startsWith("xterm"); }
            // null on Windows unless on Cygwin or MSYS
            static final boolean hasOsType()    { return System.getenv("OSTYPE") != null; }

            // see Jan Niklas Hasse's https://bixense.com/clicolors/ proposal
            // https://conemu.github.io/en/AnsiEscapeCodes.html#Environment_variable
            static final boolean hintDisabled() { return "0".equals(System.getenv("CLICOLOR"))
                                               || "OFF".equals(System.getenv("ConEmuANSI")); }

            /** https://github.com/adoxa/ansicon/blob/master/readme.txt,
             * Jan Niklas Hasse's https://bixense.com/clicolors/ proposal,
             * https://conemu.github.io/en/AnsiEscapeCodes.html#Environment_variable */
            static final boolean hintEnabled() { return System.getenv("ANSICON") != null
                                               || "1".equals(System.getenv("CLICOLOR"))
                                               || "ON".equals(System.getenv("ConEmuANSI")); }
            /** https://no-color.org/ */
            static final boolean forceDisabled() { return System.getenv("NO_COLOR") != null; }

            /** Jan Niklas Hasse's https://bixense.com/clicolors/ proposal */
            static final boolean forceEnabled() { return System.getenv("CLICOLOR_FORCE") != null
                                               && !"0".equals(System.getenv("CLICOLOR_FORCE"));}
            /** http://stackoverflow.com/questions/1403772/how-can-i-check-if-a-java-programs-input-output-streams-are-connected-to-a-term */
            static boolean calcTTY() {
                try { return System.class.getDeclaredMethod("console").invoke(null) != null; }
                catch (Throwable reflectionFailed) { return true; }
            }
            /** Cygwin and MSYS use pseudo-tty and console is always null... */
            static boolean isPseudoTTY() { return isWindows() && (isXterm() || hasOsType()); }

            static boolean ansiPossible() {
                if (forceDisabled())                          { return false; }
                if (forceEnabled())                           { return true; }
                if (isWindows() && isJansiConsoleInstalled()) { return true; } // #630 JVM crash loading jansi.AnsiConsole on Linux
                if (hintDisabled())                           { return false; }
                if (!isTTY() && !isPseudoTTY())               { return false; }
                return hintEnabled() || !isWindows() || isXterm() || hasOsType();
            }
            static boolean isJansiConsoleInstalled() {
                try {
                    Class<?> ansiConsole = Class.forName("org.fusesource.jansi.AnsiConsole");
                    Field out = ansiConsole.getField("out");
                    return out.get(null) == System.out;
                } catch (Exception reflectionFailed) {
                    return false;
                }
            }
            
            /** Returns {@code true} if ANSI escape codes should be emitted, {@code false} otherwise.
             * @return ON: {@code true}, OFF: {@code false}, AUTO: if system property {@code "picocli.ansi"} is
             *      defined then return its boolean value, otherwise return whether the platform supports ANSI escape codes */
            public boolean enabled() {
                if (this == ON)  { return true; }
                if (this == OFF) { return false; }
                String ansi = System.getProperty("picocli.ansi");
                boolean auto = ansi == null || "AUTO".equalsIgnoreCase(ansi);
                return auto ? ansiPossible() : Boolean.getBoolean("picocli.ansi");
            }
            /**
             * Returns a new Text object for this Ansi mode, encapsulating the specified string
             * which may contain markup like {@code @|bg(red),white,underline some text|@}.
             * <p>
             * Calling {@code toString()} on the returned Text will either include ANSI escape codes
             * (if this Ansi mode is ON), or suppress ANSI escape codes (if this Ansi mode is OFF).
             * <p>
             * Equivalent to {@code this.new Text(stringWithMarkup)}.
             * @since 3.4 */
            public Text text(String stringWithMarkup) { return this.new Text(stringWithMarkup); }

            /**
             * Returns a String where any markup like
             * {@code @|bg(red),white,underline some text|@} is converted to ANSI escape codes
             * if this Ansi is ON, or suppressed if this Ansi is OFF.
             * <p>
             * Equivalent to {@code this.new Text(stringWithMarkup).toString()}.
             * @since 3.4 */
            public String string(String stringWithMarkup) { return this.new Text(stringWithMarkup).toString(); }

            /** Returns Ansi.ON if the specified {@code enabled} flag is true, Ansi.OFF otherwise.
             * @since 3.4 */
            public static Ansi valueOf(boolean enabled) {return enabled ? ON : OFF; }

            /** Defines the interface for an ANSI escape sequence. */
            public interface IStyle {

                /** The Control Sequence Introducer (CSI) escape sequence {@value}. */
                String CSI = "\u001B[";

                /** Returns the ANSI escape code for turning this style on.
                 * @return the ANSI escape code for turning this style on */
                String on();

                /** Returns the ANSI escape code for turning this style off.
                 * @return the ANSI escape code for turning this style off */
                String off();
            }

            /**
             * A set of pre-defined ANSI escape code styles and colors, and a set of convenience methods for parsing
             * text with embedded markup style names, as well as convenience methods for converting
             * styles to strings with embedded escape codes.
             */
            public enum Style implements IStyle {
                reset(0, 0), bold(1, 21), faint(2, 22), italic(3, 23), underline(4, 24), blink(5, 25), reverse(7, 27),
                fg_black(30, 39), fg_red(31, 39), fg_green(32, 39), fg_yellow(33, 39), fg_blue(34, 39), fg_magenta(35, 39), fg_cyan(36, 39), fg_white(37, 39),
                bg_black(40, 49), bg_red(41, 49), bg_green(42, 49), bg_yellow(43, 49), bg_blue(44, 49), bg_magenta(45, 49), bg_cyan(46, 49), bg_white(47, 49),
                ;
                private final int startCode;
                private final int endCode;

                Style(int startCode, int endCode) {this.startCode = startCode; this.endCode = endCode; }
                public String on() { return CSI + startCode + "m"; }
                public String off() { return CSI + endCode + "m"; }

                /** Returns the concatenated ANSI escape codes for turning all specified styles on.
                 * @param styles the styles to generate ANSI escape codes for
                 * @return the concatenated ANSI escape codes for turning all specified styles on */
                public static String on(IStyle... styles) {
                    StringBuilder result = new StringBuilder();
                    for (IStyle style : styles) {
                        result.append(style.on());
                    }
                    return result.toString();
                }
                /** Returns the concatenated ANSI escape codes for turning all specified styles off.
                 * @param styles the styles to generate ANSI escape codes for
                 * @return the concatenated ANSI escape codes for turning all specified styles off */
                public static String off(IStyle... styles) {
                    StringBuilder result = new StringBuilder();
                    for (IStyle style : styles) {
                        result.append(style.off());
                    }
                    return result.toString();
                }
                /** Parses the specified style markup and returns the associated style.
                 *  The markup may be one of the Style enum value names, or it may be one of the Style enum value
                 *  names when {@code "fg_"} is prepended, or it may be one of the indexed colors in the 256 color palette.
                 * @param str the case-insensitive style markup to convert, e.g. {@code "blue"} or {@code "fg_blue"},
                 *          or {@code "46"} (indexed color) or {@code "0;5;0"} (RGB components of an indexed color)
                 * @return the IStyle for the specified converter
                 */
                public static IStyle fg(String str) {
                    try { return Style.valueOf(str.toLowerCase(ENGLISH)); } catch (Exception ignored) {}
                    try { return Style.valueOf("fg_" + str.toLowerCase(ENGLISH)); } catch (Exception ignored) {}
                    return new Palette256Color(true, str);
                }
                /** Parses the specified style markup and returns the associated style.
                 *  The markup may be one of the Style enum value names, or it may be one of the Style enum value
                 *  names when {@code "bg_"} is prepended, or it may be one of the indexed colors in the 256 color palette.
                 * @param str the case-insensitive style markup to convert, e.g. {@code "blue"} or {@code "bg_blue"},
                 *          or {@code "46"} (indexed color) or {@code "0;5;0"} (RGB components of an indexed color)
                 * @return the IStyle for the specified converter
                 */
                public static IStyle bg(String str) {
                    try { return Style.valueOf(str.toLowerCase(ENGLISH)); } catch (Exception ignored) {}
                    try { return Style.valueOf("bg_" + str.toLowerCase(ENGLISH)); } catch (Exception ignored) {}
                    return new Palette256Color(false, str);
                }
                /** Parses the specified comma-separated sequence of style descriptors and returns the associated
                 *  styles. For each markup, strings starting with {@code "bg("} are delegated to
                 *  {@link #bg(String)}, others are delegated to {@link #bg(String)}.
                 * @param commaSeparatedCodes one or more descriptors, e.g. {@code "bg(blue),underline,red"}
                 * @return an array with all styles for the specified descriptors
                 */
                public static IStyle[] parse(String commaSeparatedCodes) {
                    String[] codes = commaSeparatedCodes.split(",");
                    IStyle[] styles = new IStyle[codes.length];
                    for(int i = 0; i < codes.length; ++i) {
                        if (codes[i].toLowerCase(ENGLISH).startsWith("fg(")) {
                            int end = codes[i].indexOf(')');
                            styles[i] = Style.fg(codes[i].substring(3, end < 0 ? codes[i].length() : end));
                        } else if (codes[i].toLowerCase(ENGLISH).startsWith("bg(")) {
                            int end = codes[i].indexOf(')');
                            styles[i] = Style.bg(codes[i].substring(3, end < 0 ? codes[i].length() : end));
                        } else {
                            styles[i] = Style.fg(codes[i]);
                        }
                    }
                    return styles;
                }
            }

            /** Defines a palette map of 216 colors: 6 * 6 * 6 cube (216 colors):
             * 16 + 36 * r + 6 * g + b (0 &lt;= r, g, b &lt;= 5). */
            static class Palette256Color implements IStyle {
                private final int fgbg;
                private final int color;

                Palette256Color(boolean foreground, String color) {
                    this.fgbg = foreground ? 38 : 48;
                    String[] rgb = color.split(";");
                    if (rgb.length == 3) {
                        this.color = 16 + 36 * Integer.decode(rgb[0]) + 6 * Integer.decode(rgb[1]) + Integer.decode(rgb[2]);
                    } else {
                        this.color = Integer.decode(color);
                    }
                }
                public String on() { return String.format(CSI + "%d;5;%dm", fgbg, color); }
                public String off() { return CSI + (fgbg + 1) + "m"; }
                public boolean equals(Object obj) {
                    if (obj == this) { return true; }
                    if (!(obj instanceof Palette256Color)) { return false; }
                    Palette256Color other = (Palette256Color) obj;
                    return other.fgbg == this.fgbg && other.color == this.color;
                }
                public int hashCode() {
                    return (17 + fgbg) * 37 + color;
                }
            }
            private static class StyledSection {
                int startIndex, length;
                String startStyles, endStyles;
                StyledSection(int start, int len, String style1, String style2) {
                    startIndex = start; length = len; startStyles = style1; endStyles = style2;
                }
                StyledSection withStartIndex(int newStart) {
                    return new StyledSection(newStart, length, startStyles, endStyles);
                }
            }

            /**
             * Returns a new Text object where all the specified styles are applied to the full length of the
             * specified plain text.
             * @param plainText the string to apply all styles to. Must not contain markup!
             * @param styles the styles to apply to the full plain text
             * @return a new Text object
             */
            public Text apply(String plainText, List<IStyle> styles) {
                if (plainText.length() == 0) { return new Text(0); }
                Text result = new Text(plainText.length());
                IStyle[] all = styles.toArray(new IStyle[styles.size()]);
                result.sections.add(new StyledSection(
                        0, plainText.length(), Style.on(all), Style.off(reverse(all)) + Style.reset.off()));
                result.plain.append(plainText);
                result.length = result.plain.length();
                return result;
            }

            private static <T> T[] reverse(T[] all) {
                for (int i = 0; i < all.length / 2; i++) {
                    T temp = all[i];
                    all[i] = all[all.length - i - 1];
                    all[all.length - i - 1] = temp;
                }
                return all;
            }
            /** Encapsulates rich text with styles and colors. Text objects may be constructed with Strings containing
             * markup like {@code @|bg(red),white,underline some text|@}, and this class converts the markup to ANSI
             * escape codes.
             * <p>
             * Internally keeps both an enriched and a plain text representation to allow layout components to calculate
             * text width while remaining unaware of the embedded ANSI escape codes.</p> */
            public class Text implements Cloneable {
                private final int maxLength;
                private int from;
                private int length;
                private StringBuilder plain = new StringBuilder();
                private List<StyledSection> sections = new ArrayList<StyledSection>();

                /** Constructs a Text with the specified max length (for use in a TextTable Column).
                 * @param maxLength max length of this text */
                public Text(int maxLength) { this.maxLength = maxLength; }

                /** Copy constructor.
                 * @since 3.9 */
                public Text(Text other) {
                    this.maxLength = other.maxLength;
                    this.from = other.from;
                    this.length = other.length;
                    this.plain = new StringBuilder(other.plain);
                    this.sections = new ArrayList<StyledSection>(other.sections);
                }
                /**
                 * Constructs a Text with the specified String, which may contain markup like
                 * {@code @|bg(red),white,underline some text|@}.
                 * @param input the string with markup to parse
                 */
                public Text(String input) {
                    maxLength = -1;
                    plain.setLength(0);
                    int i = 0;

                    while (true) {
                        int j = input.indexOf("@|", i);
                        if (j == -1) {
                            if (i == 0) {
                                plain.append(input);
                                length = plain.length();
                                return;
                            }
                            plain.append(input.substring(i, input.length()));
                            length = plain.length();
                            return;
                        }
                        plain.append(input.substring(i, j));
                        int k = input.indexOf("|@", j);
                        if (k == -1) {
                            plain.append(input);
                            length = plain.length();
                            return;
                        }

                        j += 2;
                        String spec = input.substring(j, k);
                        String[] items = spec.split(" ", 2);
                        if (items.length == 1) {
                            plain.append(input);
                            length = plain.length();
                            return;
                        }

                        IStyle[] styles = Style.parse(items[0]);
                        addStyledSection(plain.length(), items[1].length(),
                                Style.on(styles), Style.off(reverse(styles)) + Style.reset.off());
                        plain.append(items[1]);
                        i = k + 2;
                    }
                }
                private void addStyledSection(int start, int length, String startStyle, String endStyle) {
                    sections.add(new StyledSection(start, length, startStyle, endStyle));
                }
                public Object clone() { return new Text(this); }

                public Text[] splitLines() {
                    List<Text> result = new ArrayList<Text>();
                    int start = 0, end = 0;
                    for (int i = 0; i < plain.length(); i++, end = i) {
                        char c = plain.charAt(i);
                        boolean eol = c == '\n';
                        if (c == '\r' && i + 1 < plain.length() && plain.charAt(i + 1) == '\n') { eol = true; i++; } // \r\n
                        eol |= c == '\r';
                        if (eol) {
                            result.add(this.substring(start, end));
                            start = i + 1;
                        }
                    }
                    // add remainder (may be empty string)
                    result.add(this.substring(start, plain.length()));
                    return result.toArray(new Text[result.size()]);
                }

                /** Returns a new {@code Text} instance that is a substring of this Text. Does not modify this instance!
                 * @param start index in the plain text where to start the substring
                 * @return a new Text instance that is a substring of this Text */
                public Text substring(int start) {
                    return substring(start, length);
                }

                /** Returns a new {@code Text} instance that is a substring of this Text. Does not modify this instance!
                 * @param start index in the plain text where to start the substring
                 * @param end index in the plain text where to end the substring
                 * @return a new Text instance that is a substring of this Text */
                public Text substring(int start, int end) {
                    Text result = (Text) clone();
                    result.from = from + start;
                    result.length = end - start;
                    return result;
                }
                /** @deprecated use {@link #concat(String)} instead */
                @Deprecated public Text append(String string) { return concat(string); }
                /** @deprecated use {@link #concat(Text)} instead */
                @Deprecated public Text append(Text text) { return concat(text); }

                /** Returns a copy of this {@code Text} instance with the specified text concatenated to the end. Does not modify this instance!
                 * @param string the text to concatenate to the end of this Text
                 * @return a new Text instance
                 * @since 3.0 */
                public Text concat(String string) { return concat(new Text(string)); }

                /** Returns a copy of this {@code Text} instance with the specified text concatenated to the end. Does not modify this instance!
                 * @param other the text to concatenate to the end of this Text
                 * @return a new Text instance
                 * @since 3.0 */
                public Text concat(Text other) {
                    Text result = (Text) clone();
                    result.plain = new StringBuilder(plain.toString().substring(from, from + length));
                    result.from = 0;
                    result.sections = new ArrayList<StyledSection>();
                    for (StyledSection section : sections) {
                        result.sections.add(section.withStartIndex(section.startIndex - from));
                    }
                    result.plain.append(other.plain.toString().substring(other.from, other.from + other.length));
                    for (StyledSection section : other.sections) {
                        int index = result.length + section.startIndex - other.from;
                        result.sections.add(section.withStartIndex(index));
                    }
                    result.length = result.plain.length();
                    return result;
                }

                /**
                 * Copies the specified substring of this Text into the specified destination, preserving the markup.
                 * @param from start of the substring
                 * @param length length of the substring
                 * @param destination destination Text to modify
                 * @param offset indentation (padding)
                 */
                public void getStyledChars(int from, int length, Text destination, int offset) {
                    if (destination.length < offset) {
                        for (int i = destination.length; i < offset; i++) {
                            destination.plain.append(' ');
                        }
                        destination.length = offset;
                    }
                    for (StyledSection section : sections) {
                        destination.sections.add(section.withStartIndex(section.startIndex - from + destination.length));
                    }
                    destination.plain.append(plain.toString().substring(from, from + length));
                    destination.length = destination.plain.length();
                }
                /** Returns the plain text without any formatting.
                 * @return the plain text without any formatting */
                public String plainString() {  return plain.toString().substring(from, from + length); }

                public boolean equals(Object obj) { return toString().equals(String.valueOf(obj)); }
                public int hashCode() { return toString().hashCode(); }

                /** Returns a String representation of the text with ANSI escape codes embedded, unless ANSI is
                 * {@linkplain Ansi#enabled()} not enabled}, in which case the plain text is returned.
                 * @return a String representation of the text with ANSI escape codes embedded (if enabled) */
                public String toString() {
                    if (!Ansi.this.enabled()) {
                        return plain.toString().substring(from, from + length);
                    }
                    if (length == 0) { return ""; }
                    StringBuilder sb = new StringBuilder(plain.length() + 20 * sections.size());
                    StyledSection current = null;
                    int end = Math.min(from + length, plain.length());
                    for (int i = from; i < end; i++) {
                        StyledSection section = findSectionContaining(i);
                        if (section != current) {
                            if (current != null) { sb.append(current.endStyles); }
                            if (section != null) { sb.append(section.startStyles); }
                            current = section;
                        }
                        sb.append(plain.charAt(i));
                    }
                    if (current != null) { sb.append(current.endStyles); }
                    return sb.toString();
                }

                private StyledSection findSectionContaining(int index) {
                    for (StyledSection section : sections) {
                        if (index >= section.startIndex && index < section.startIndex + section.length) {
                            return section;
                        }
                    }
                    return null;
                }
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
        static boolean equals(Object obj1, Object obj2) { return obj1 == null ? obj2 == null : obj1.equals(obj2); }
        static int hashCode(Object obj) {return obj == null ? 0 : obj.hashCode(); }
        static int hashCode(boolean bool) {return bool ? 1 : 0; }
        static void assertTrue(boolean condition, String message) {
            if (!condition) throw new IllegalStateException(message);
        }
        static void assertTrue(boolean condition, IHelpSectionRenderer producer) {
            if (!condition) throw new IllegalStateException(producer.render(null));
        }
        private Assert() {} // private constructor: never instantiate
    }
    private enum TraceLevel { OFF, WARN, INFO, DEBUG;
        public boolean isEnabled(TraceLevel other) { return ordinal() >= other.ordinal(); }
        private void print(Tracer tracer, String msg, Object... params) {
            if (tracer.level.isEnabled(this)) { tracer.stream.printf(prefix(msg), params); }
        }
        private String prefix(String msg) { return "[picocli " + this + "] " + msg; }
        static TraceLevel lookup(String key) { return key == null ? WARN : empty(key) || "true".equalsIgnoreCase(key) ? INFO : valueOf(key); }
    }
    static class Tracer {
        TraceLevel level = TraceLevel.lookup(System.getProperty("picocli.trace"));
        PrintStream stream = System.err;
        void warn (String msg, Object... params) { TraceLevel.WARN.print(this, msg, params); }
        void info (String msg, Object... params) { TraceLevel.INFO.print(this, msg, params); }
        void debug(String msg, Object... params) { TraceLevel.DEBUG.print(this, msg, params); }
        boolean isWarn()  { return level.isEnabled(TraceLevel.WARN); }
        boolean isInfo()  { return level.isEnabled(TraceLevel.INFO); }
        boolean isDebug() { return level.isEnabled(TraceLevel.DEBUG); }
    }
    /**
     * Uses cosine similarity to find matches from a candidate set for a specified input.
     * Based on code from http://www.nearinfinity.com/blogs/seth_schroeder/groovy_cosine_similarity_in_grails.html
     *
     * @author Burt Beckwith
     */
    private static class CosineSimilarity {
        static List<String> mostSimilar(String pattern, Iterable<String> candidates) { return mostSimilar(pattern, candidates, 0); }
        static List<String> mostSimilar(String pattern, Iterable<String> candidates, double threshold) {
            pattern = pattern.toLowerCase();
            SortedMap<Double, String> sorted = new TreeMap<Double, String>();
            for (String candidate : candidates) {
                double score = similarity(pattern, candidate.toLowerCase(), 2);
                if (score > threshold) { sorted.put(score, candidate); }
            }
            return reverseList(new ArrayList<String>(sorted.values()));
        }

        private static double similarity(String sequence1, String sequence2, int degree) {
            Map<String, Integer> m1 = countNgramFrequency(sequence1, degree);
            Map<String, Integer> m2 = countNgramFrequency(sequence2, degree);
            return dotProduct(m1, m2) / Math.sqrt(dotProduct(m1, m1) * dotProduct(m2, m2));
        }

        private static Map<String, Integer> countNgramFrequency(String sequence, int degree) {
            Map<String, Integer> m = new HashMap<String, Integer>();
            for (int i = 0; i + degree <= sequence.length(); i++) {
                String gram = sequence.substring(i, i + degree);
                m.put(gram, 1 + (m.containsKey(gram) ? m.get(gram) : 0));
            }
            return m;
        }

        private static double dotProduct(Map<String, Integer> m1, Map<String, Integer> m2) {
            double result = 0;
            for (String key : m1.keySet()) { result += m1.get(key) * (m2.containsKey(key) ? m2.get(key) : 0); }
            return result;
        }
    }
    /** Base class of all exceptions thrown by {@code picocli.CommandLine}.
     * <h2>Class Diagram of the Picocli Exceptions</h2>
     * <p>
     * <img src="doc-files/class-diagram-exceptions.png" alt="Class Diagram of the Picocli Exceptions">
     * </p>
     * @since 2.0 */
    public static class PicocliException extends RuntimeException {
        private static final long serialVersionUID = -2574128880125050818L;
        public PicocliException(String msg) { super(msg); }
        public PicocliException(String msg, Throwable t) { super(msg, t); }
    }
    /** Exception indicating a problem during {@code CommandLine} initialization.
     * @since 2.0 */
    public static class InitializationException extends PicocliException {
        private static final long serialVersionUID = 8423014001666638895L;
        public InitializationException(String msg) { super(msg); }
        public InitializationException(String msg, Exception ex) { super(msg, ex); }
    }
    /** Exception indicating a problem while invoking a command or subcommand.
     * Keeps a reference to the {@code CommandLine} object where the cause exception occurred,
     * so that client code can tailor their handling for the specific command (print the command's usage help message, for example).
     * @since 2.0 */
    public static class ExecutionException extends PicocliException {
        private static final long serialVersionUID = 7764539594267007998L;
        private final CommandLine commandLine;
        public ExecutionException(CommandLine commandLine, String msg) {
            super(msg);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }
        public ExecutionException(CommandLine commandLine, String msg, Throwable t) {
            super(msg, t);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }
        /** Returns the {@code CommandLine} object for the (sub)command that could not be invoked.
         * @return the {@code CommandLine} object for the (sub)command where invocation failed.
         */
        public CommandLine getCommandLine() { return commandLine; }
    }

    /** Exception thrown by {@link ITypeConverter} implementations to indicate a String could not be converted. */
    public static class TypeConversionException extends PicocliException {
        private static final long serialVersionUID = 4251973913816346114L;
        public TypeConversionException(String msg) { super(msg); }
    }
    /** Exception indicating something went wrong while parsing command line options. */
    public static class ParameterException extends PicocliException {
        private static final long serialVersionUID = 1477112829129763139L;
        private final CommandLine commandLine;
        private ArgSpec argSpec = null;
        private String value = null;

        /** Constructs a new ParameterException with the specified CommandLine and error message.
         * @param commandLine the command or subcommand whose input was invalid
         * @param msg describes the problem
         * @since 2.0 */
        public ParameterException(CommandLine commandLine, String msg) {
            super(msg);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        /** Constructs a new ParameterException with the specified CommandLine and error message.
         * @param commandLine the command or subcommand whose input was invalid
         * @param msg describes the problem
         * @param t the throwable that caused this ParameterException
         * @since 2.0 */
        public ParameterException(CommandLine commandLine, String msg, Throwable t) {
            super(msg, t);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        /** Constructs a new ParameterException with the specified CommandLine and error message.
         * @param commandLine the command or subcommand whose input was invalid
         * @param msg describes the problem
         * @param t the throwable that caused this ParameterException
         * @param argSpec the argSpec that caused this ParameterException
         * @param value the value that caused this ParameterException
         * @since 3.2 */
        public ParameterException(CommandLine commandLine, String msg, Throwable t, ArgSpec argSpec, String value) {
            super(msg, t);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
            if (argSpec == null && value == null) { throw new IllegalArgumentException("ArgSpec and value cannot both be null"); }
            this.argSpec = argSpec;
            this.value = value;
        }

        /** Constructs a new ParameterException with the specified CommandLine and error message.
         * @param commandLine the command or subcommand whose input was invalid
         * @param msg describes the problem
         * @param argSpec the argSpec that caused this ParameterException
         * @param value the value that caused this ParameterException
         * @since 3.2 */
        public ParameterException(CommandLine commandLine, String msg, ArgSpec argSpec, String value) {
            super(msg);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
            if (argSpec == null && value == null) { throw new IllegalArgumentException("ArgSpec and value cannot both be null"); }
            this.argSpec = argSpec;
            this.value = value;
        }


        /** Returns the {@code CommandLine} object for the (sub)command whose input could not be parsed.
         * @return the {@code CommandLine} object for the (sub)command where parsing failed.
         * @since 2.0
         */
        public CommandLine getCommandLine() { return commandLine; }

        /** Returns the {@code ArgSpec} object for the (sub)command whose input could not be parsed.
         * @return the {@code ArgSpec} object for the (sub)command where parsing failed.
         * @since 3.2
         */
        public ArgSpec getArgSpec() { return argSpec; }

        /** Returns the {@code String} value for the (sub)command whose input could not be parsed.
         * @return the {@code String} value for the (sub)command where parsing failed.
         * @since 3.2
         */
        public String getValue() { return value; }

        private static ParameterException create(CommandLine cmd, Exception ex, String arg, int i, String[] args) {
            String msg = ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage()
                    + " while processing argument at or before arg[" + i + "] '" + arg + "' in " + Arrays.toString(args) + ": " + ex.toString();
            return new ParameterException(cmd, msg, ex, null, arg);
        }
    }
    /**
     * Exception indicating that a required parameter was not specified.
     */
    public static class MissingParameterException extends ParameterException {
        private static final long serialVersionUID = 5075678535706338753L;
        private final List<ArgSpec> missing;
        public MissingParameterException(CommandLine commandLine, ArgSpec missing, String msg) { this(commandLine, Arrays.asList(missing), msg); }
        public MissingParameterException(CommandLine commandLine, Collection<ArgSpec> missing, String msg) {
            super(commandLine, msg);
            this.missing = Collections.unmodifiableList(new ArrayList<ArgSpec>(missing));
        }
        public List<ArgSpec> getMissing() { return missing; }
        private static MissingParameterException create(CommandLine cmd, Collection<ArgSpec> missing, String separator) {
            if (missing.size() == 1) {
                return new MissingParameterException(cmd, missing, "Missing required option '"
                        + ArgSpec.describe(missing.iterator().next(), separator) + "'");
            }
            List<String> names = new ArrayList<String>(missing.size());
            for (ArgSpec argSpec : missing) {
                names.add(ArgSpec.describe(argSpec, separator));
            }
            return new MissingParameterException(cmd, missing, "Missing required options " + names.toString());
        }
    }
    /** Exception indicating that the user input included multiple arguments from a mutually exclusive group.
     * @since 4.0 */
    public static class MutuallyExclusiveArgsException extends ParameterException {
        private static final long serialVersionUID = -5557715106221420986L;
        public MutuallyExclusiveArgsException(CommandLine commandLine, String msg) { super(commandLine, msg); }
    }

    /** Exception indicating that multiple named elements have incorrectly used the same name.
     * @since 4.0 */
    public static class DuplicateNameException extends InitializationException {
        private static final long serialVersionUID = -4126747467955626054L;
        public DuplicateNameException(String msg) { super(msg); }
    }
    /**
     * Exception indicating that multiple fields have been annotated with the same Option name.
     */
    public static class DuplicateOptionAnnotationsException extends DuplicateNameException {
        private static final long serialVersionUID = -3355128012575075641L;
        public DuplicateOptionAnnotationsException(String msg) { super(msg); }

        private static DuplicateOptionAnnotationsException create(String name, ArgSpec argSpec1, ArgSpec argSpec2) {
            return new DuplicateOptionAnnotationsException("Option name '" + name + "' is used by both " +
                    argSpec1.toString() + " and " + argSpec2.toString());
        }
    }
    /** Exception indicating that there was a gap in the indices of the fields annotated with {@link Parameters}. */
    public static class ParameterIndexGapException extends InitializationException {
        private static final long serialVersionUID = -1520981133257618319L;
        public ParameterIndexGapException(String msg) { super(msg); }
    }
    /** Exception indicating that a command line argument could not be mapped to any of the fields annotated with
     * {@link Option} or {@link Parameters}. */
    public static class UnmatchedArgumentException extends ParameterException {
        private static final long serialVersionUID = -8700426380701452440L;
        private List<String> unmatched = Collections.<String>emptyList();
        public UnmatchedArgumentException(CommandLine commandLine, String msg) { super(commandLine, msg); }
        public UnmatchedArgumentException(CommandLine commandLine, Stack<String> args) { this(commandLine, new ArrayList<String>(reverse(args))); }
        public UnmatchedArgumentException(CommandLine commandLine, List<String> args) {
            this(commandLine, describe(Assert.notNull(args, "unmatched list"), commandLine) +
                    (args.size() == 1 ? ": " : "s: ") + str(args));
            unmatched = new ArrayList<String>(args);
        }
        /** Returns {@code true} and prints suggested solutions to the specified stream if such solutions exist, otherwise returns {@code false}.
         * @since 3.3.0 */
        public static boolean printSuggestions(ParameterException ex, PrintStream out) {
            return ex instanceof UnmatchedArgumentException && ((UnmatchedArgumentException) ex).printSuggestions(out);
        }
        /** Returns {@code true} and prints suggested solutions to the specified writer if such solutions exist, otherwise returns {@code false}.
         * @since 4.0 */
        public static boolean printSuggestions(ParameterException ex, PrintWriter writer) {
            return ex instanceof UnmatchedArgumentException && ((UnmatchedArgumentException) ex).printSuggestions(writer);
        }
        /** Returns the unmatched command line arguments.
         * @since 3.3.0 */
        public List<String> getUnmatched() { return stripErrorMessage(unmatched); }
        static List<String> stripErrorMessage(List<String> unmatched) {
            List<String> result = new ArrayList<String>();
            for (String s : unmatched) {
                if (s == null) { result.add(null); }
                int pos = s.indexOf(" (while processing option:");
                result.add(pos < 0 ? s : s.substring(0, pos));
            }
            return Collections.unmodifiableList(result);
        }
        /** Returns {@code true} if the first unmatched command line arguments resembles an option, {@code false} otherwise.
         * @since 3.3.0 */
        public boolean isUnknownOption() { return isUnknownOption(unmatched, getCommandLine()); }
        /** Returns {@code true} and prints suggested solutions to the specified stream if such solutions exist, otherwise returns {@code false}.
         * @since 3.3.0 */
        public boolean printSuggestions(PrintStream out) { return printSuggestions(new PrintWriter(out, true)); }
        /** Returns {@code true} and prints suggested solutions to the specified stream if such solutions exist, otherwise returns {@code false}.
         * @since 4.0 */
        public boolean printSuggestions(PrintWriter writer) {
            List<String> suggestions = getSuggestions();
            if (!suggestions.isEmpty()) {
                writer.println(isUnknownOption()
                        ? "Possible solutions: " + str(suggestions)
                        : "Did you mean: " + str(suggestions).replace(", ", " or ") + "?");
            }
            return !suggestions.isEmpty();
        }
        /** Returns suggested solutions if such solutions exist, otherwise returns an empty list.
         * @since 3.3.0 */
        public List<String> getSuggestions() {
            if (unmatched.isEmpty()) { return Collections.emptyList(); }
            String arg = unmatched.get(0);
            String stripped = CommandSpec.stripPrefix(arg);
            CommandSpec spec = getCommandLine().getCommandSpec();
            if (spec.resemblesOption(arg, null)) {
                return spec.findOptionNamesWithPrefix(stripped.substring(0, Math.min(2, stripped.length())));
            } else if (!spec.subcommands().isEmpty()) {
                List<String> mostSimilar = CosineSimilarity.mostSimilar(arg, spec.subcommands().keySet());
                return mostSimilar.subList(0, Math.min(3, mostSimilar.size()));
            }
            return Collections.emptyList();
        }
        private static boolean isUnknownOption(List<String> unmatch, CommandLine cmd) {
            return unmatch != null && !unmatch.isEmpty() && cmd.getCommandSpec().resemblesOption(unmatch.get(0), null);
        }
        private static String describe(List<String> unmatch, CommandLine cmd) {
            return isUnknownOption(unmatch, cmd) ? "Unknown option" : "Unmatched argument";
        }
        static String str(List<String> list) {
            String s = list.toString();
            return s.substring(0, s.length() - 1).substring(1);
        }
    }
    /** Exception indicating that more values were specified for an option or parameter than its {@link Option#arity() arity} allows. */
    public static class MaxValuesExceededException extends ParameterException {
        private static final long serialVersionUID = 6536145439570100641L;
        public MaxValuesExceededException(CommandLine commandLine, String msg) { super(commandLine, msg); }
    }
    /** Exception indicating that an option for a single-value option field has been specified multiple times on the command line. */
    public static class OverwrittenOptionException extends ParameterException {
        private static final long serialVersionUID = 1338029208271055776L;
        private final ArgSpec overwrittenArg;
        public OverwrittenOptionException(CommandLine commandLine, ArgSpec overwritten, String msg) {
            super(commandLine, msg);
            overwrittenArg = overwritten;
        }
        /** Returns the {@link ArgSpec} for the option which was being overwritten.
         * @since 3.8 */
        public ArgSpec getOverwritten() { return overwrittenArg; }
    }
    /**
     * Exception indicating that an annotated field had a type for which no {@link ITypeConverter} was
     * {@linkplain #registerConverter(Class, ITypeConverter) registered}.
     */
    public static class MissingTypeConverterException extends ParameterException {
        private static final long serialVersionUID = -6050931703233083760L;
        public MissingTypeConverterException(CommandLine commandLine, String msg) { super(commandLine, msg); }
    }
}
