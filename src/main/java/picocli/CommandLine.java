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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.Ansi.Text;

import static java.util.Locale.ENGLISH;
import static picocli.CommandLine.Help.Column.Overflow.SPAN;
import static picocli.CommandLine.Help.Column.Overflow.TRUNCATE;
import static picocli.CommandLine.Help.Column.Overflow.WRAP;

/**
 * <p>
 * CommandLine interpreter that uses reflection to initialize an annotated domain object with values obtained from the
 * command line arguments.
 * </p><h2>Example</h2>
 * <pre>import static picocli.CommandLine.*;
 *
 * &#064;Command(header = "Encrypt FILE(s), or standard input, to standard output or to the output file.",
 *          version = "v1.2.3")
 * public class Encrypt {
 *
 *     &#064;Parameters(type = File.class, description = "Any number of input files")
 *     private List&lt;File&gt; files = new ArrayList&lt;File&gt;();
 *
 *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
 *     private File outputFile;
 *
 *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely list files processed")
 *     private boolean verbose;
 *
 *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, usageHelp = true, description = "Display this help and exit")
 *     private boolean help;
 *
 *     &#064;Option(names = { "-V", "--version"}, versionHelp = true, description = "Display version info and exit")
 *     private boolean versionHelp;
 * }
 * </pre>
 * <p>
 * Use {@code CommandLine} to initialize a domain object as follows:
 * </p><pre>
 * public static void main(String... args) {
 *     Encrypt encrypt = new Encrypt();
 *     try {
 *         List&lt;CommandLine&gt; parsedCommands = new CommandLine(encrypt).parse(args);
 *         if (!CommandLine.printHelpIfRequested(parsedCommands, System.err, Help.Ansi.AUTO)) {
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
 */
public class CommandLine {
    /** This is picocli version {@value}. */
    public static final String VERSION = "2.0.3-SNAPSHOT";

    private final Tracer tracer = new Tracer();
    private final Interpreter interpreter;
    private String commandName = Help.DEFAULT_COMMAND_NAME;
    private boolean overwrittenOptionsAllowed = false;
    private boolean unmatchedArgumentsAllowed = false;
    private List<String> unmatchedArguments = new ArrayList<String>();
    private CommandLine parent;
    private boolean usageHelpRequested;
    private boolean versionHelpRequested;
    private List<String> versionLines = new ArrayList<String>();

    /**
     * Constructs a new {@code CommandLine} interpreter with the specified annotated object.
     * When the {@link #parse(String...)} method is called, fields of the specified object that are annotated
     * with {@code @Option} or {@code @Parameters} will be initialized based on command line arguments.
     * @param command the object to initialize from the command line arguments
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     */
    public CommandLine(Object command) {
        interpreter = new Interpreter(command);
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
     * @param name the string to recognize on the command line as a subcommand
     * @param command the object to initialize with command line arguments following the subcommand name.
     *          This may be a {@code CommandLine} instance with its own (nested) subcommands
     * @return this CommandLine object, to allow method chaining
     * @see #registerConverter(Class, ITypeConverter)
     * @since 0.9.7
     * @see Command#subcommands()
     */
    public CommandLine addSubcommand(String name, Object command) {
        CommandLine commandLine = toCommandLine(command);
        commandLine.parent = this;
        interpreter.commands.put(name, commandLine);
        return this;
    }
    /** Returns a map with the subcommands {@linkplain #addSubcommand(String, Object) registered} on this instance.
     * @return a map with the registered subcommands
     * @since 0.9.7
     */
    public Map<String, CommandLine> getSubcommands() {
        return new LinkedHashMap<String, CommandLine>(interpreter.commands);
    }
    /**
     * Returns the command that this is a subcommand of, or {@code null} if this is a top-level command.
     * @return the command that this is a subcommand of, or {@code null} if this is a top-level command
     * @see #addSubcommand(String, Object)
     * @see Command#subcommands()
     * @since 0.9.8
     */
    public CommandLine getParent() {
        return parent;
    }

    /** Returns the annotated object that this {@code CommandLine} instance was constructed with.
     * @param <T> the type of the variable that the return value is being assigned to
     * @return the annotated object that this {@code CommandLine} instance was constructed with
     * @since 0.9.7
     */
    public <T> T getCommand() {
        return (T) interpreter.command;
    }

    /** Returns {@code true} if an option annotated with {@link Option#usageHelp()} was specified on the command line.
     * @return whether the parser encountered an option annotated with {@link Option#usageHelp()}.
     * @since 0.9.8 */
    public boolean isUsageHelpRequested() { return usageHelpRequested; }

    /** Returns {@code true} if an option annotated with {@link Option#versionHelp()} was specified on the command line.
     * @return whether the parser encountered an option annotated with {@link Option#versionHelp()}.
     * @since 0.9.8 */
    public boolean isVersionHelpRequested() { return versionHelpRequested; }

    /** Returns whether options for single-value fields can be specified multiple times on the command line.
     * The default is {@code false} and a {@link OverwrittenOptionException} is thrown if this happens.
     * When {@code true}, the last specified value is retained.
     * @return {@code true} if options for single-value fields can be specified multiple times on the command line, {@code false} otherwise
     * @since 0.9.7
     */
    public boolean isOverwrittenOptionsAllowed() {
        return overwrittenOptionsAllowed;
    }

    /** Sets whether options for single-value fields can be specified multiple times on the command line without a {@link OverwrittenOptionException} being thrown.
     * <p>The specified setting will be registered with this {@code CommandLine} and the full hierarchy of its
     * subcommands and nested sub-subcommands <em>at the moment this method is called</em>. Subcommands added
     * later will have the default setting. To ensure a setting is applied to all
     * subcommands, call the setter last, after adding subcommands.</p>
     * @param newValue the new setting
     * @return this {@code CommandLine} object, to allow method chaining
     * @since 0.9.7
     */
    public CommandLine setOverwrittenOptionsAllowed(boolean newValue) {
        this.overwrittenOptionsAllowed = newValue;
        for (CommandLine command : interpreter.commands.values()) {
            command.setOverwrittenOptionsAllowed(newValue);
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
        return unmatchedArgumentsAllowed;
    }

    /** Sets whether the end user may specify unmatched arguments on the command line without a {@link UnmatchedArgumentException} being thrown.
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
        this.unmatchedArgumentsAllowed = newValue;
        for (CommandLine command : interpreter.commands.values()) {
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
        return unmatchedArguments;
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
        CommandLine cli = toCommandLine(command);
        cli.parse(args);
        return command;
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
     * @since 2.0 */
    public static interface IParseResultHandler {
        /** Processes a List of {@code CommandLine} objects resulting from successfully
         * {@linkplain #parse(String...) parsing} the command line arguments and optionally returns a list of results.
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return a list of results, or an empty list if there are no results
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) throws ExecutionException;
    }
    /**
     * Represents a function that can handle a {@code ParameterException} that occurred while
     * {@linkplain #parse(String...) parsing} the command line arguments. This is a
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">functional interface</a>
     * whose functional method is {@link #handleException(CommandLine.ParameterException, PrintStream, CommandLine.Help.Ansi, String...)}.
     * <p>
     * Implementations of this functions can be passed to the {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...) CommandLine::parseWithHandler}
     * methods to handle situations when the command line could not be parsed.
     * </p>
     * @see DefaultExceptionHandler
     * @since 2.0 */
    public static interface IExceptionHandler {
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
     * Default exception handler that prints the exception message to the specified {@code PrintStream}, followed by the
     * usage message for the command or subcommand whose input was invalid.
     * <p>Implementation roughly looks like this:</p>
     * <pre>
     *     System.err.println(paramException.getMessage());
     *     paramException.getCommandLine().usage(System.err);
     * </pre>
     * @since 2.0 */
    public static class DefaultExceptionHandler implements IExceptionHandler {
        public List<Object> handleException(ParameterException ex, PrintStream out, Help.Ansi ansi, String... args) {
            out.println(ex.getMessage());
            ex.getCommandLine().usage(out, ansi);
            return Collections.emptyList();
        }
    }
    /**
     * Helper method that may be useful when processing the list of {@code CommandLine} objects that result from successfully
     * {@linkplain #parse(String...) parsing} command line arguments. This method prints out
     * {@linkplain #usage(PrintStream, Help.Ansi) usage help} if {@linkplain #isUsageHelpRequested() requested}
     * or {@linkplain #printVersionHelp(PrintStream, Help.Ansi) version help} if {@linkplain #isVersionHelpRequested() requested}
     * and returns {@code true}. Otherwise, if none of the specified {@code CommandLine} objects have help requested,
     * this method returns {@code false}.
     * <p>
     * Note that this method <em>only</em> looks at the {@link Option#usageHelp() usageHelp} and
     * {@link Option#versionHelp() versionHelp} attributes. The {@link Option#help() help} attribute is ignored.
     * </p>
     * @param parsedCommands the list of {@code CommandLine} objects to check if help was requested
     * @param out the {@code PrintStream} to print help to if requested
     * @param ansi for printing help messages using ANSI styles and colors
     * @return {@code true} if help was printed, {@code false} otherwise
     * @since 2.0 */
    public static boolean printHelpIfRequested(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
        for (CommandLine parsed : parsedCommands) {
            if (parsed.isUsageHelpRequested()) {
                parsed.usage(out, ansi);
                return true;
            } else if (parsed.isVersionHelpRequested()) {
                parsed.printVersionHelp(out, ansi);
                return true;
            }
        }
        return false;
    }
    private static Object execute(CommandLine parsed) {
        Object command = parsed.getCommand();
        if (command instanceof Runnable) {
            try {
                ((Runnable) command).run();
                return null;
            } catch (Exception ex) {
                throw new ExecutionException(parsed, "Error while running command (" + command + ")", ex);
            }
        } else if (command instanceof Callable) {
            try {
                return ((Callable<Object>) command).call();
            } catch (Exception ex) {
                throw new ExecutionException(parsed, "Error while calling command (" + command + ")", ex);
            }
        }
        throw new ExecutionException(parsed, "Parsed command (" + command + ") is not Runnable or Callable");
    }
    /**
     * Command line parse result handler that prints help if requested, and otherwise executes the top-level
     * {@code Runnable} or {@code Callable} command.
     * For use in the {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...) parseWithHandler} methods.
     * <p>
     * From picocli v2.0, {@code RunFirst} is used to implement the {@link #run(Runnable, PrintStream, Help.Ansi, String...) run}
     * and {@link #call(Callable, PrintStream, Help.Ansi, String...) call} convenience methods.
     * </p>
     * @since 2.0 */
    public static class RunFirst implements IParseResultHandler {
        /** Prints help if requested, and otherwise executes the top-level {@code Runnable} or {@code Callable} command.
         * If the top-level command does not implement either {@code Runnable} or {@code Callable}, a {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return an empty list if help was requested, or a list containing a single element: the result of calling the
         *      {@code Callable}, or a {@code null} element if the top-level command was a {@code Runnable}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
            if (printHelpIfRequested(parsedCommands, out, ansi)) { return Collections.emptyList(); }
            return Arrays.asList(execute(parsedCommands.get(0)));
        }
    }
    /**
     * Command line parse result handler that prints help if requested, and otherwise executes the most specific
     * {@code Runnable} or {@code Callable} subcommand.
     * For use in the {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...) parseWithHandler} methods.
     * <p>
     * Something like this:</p>
     * <pre>
     *     // RunLast implementation: print help if requested, otherwise execute the most specific subcommand
     *     if (CommandLine.printHelpIfRequested(parsedCommands, System.err, Help.Ansi.AUTO)) {
     *         return emptyList();
     *     }
     *     CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
     *     Object command = last.getCommand();
     *     if (command instanceof Runnable) {
     *         try {
     *             ((Runnable) command).run();
     *         } catch (Exception ex) {
     *             throw new ExecutionException(last, "Error in runnable " + command, ex);
     *         }
     *     } else if (command instanceof Callable) {
     *         Object result;
     *         try {
     *             result = ((Callable) command).call();
     *         } catch (Exception ex) {
     *             throw new ExecutionException(last, "Error in callable " + command, ex);
     *         }
     *         // ...do something with result
     *     } else {
     *         throw new ExecutionException(last, "Parsed command (" + command + ") is not Runnable or Callable");
     *     }
     * </pre>
     * @since 2.0 */
    public static class RunLast implements IParseResultHandler {
        /** Prints help if requested, and otherwise executes the most specific {@code Runnable} or {@code Callable} subcommand.
         * If the last (sub)command does not implement either {@code Runnable} or {@code Callable}, a {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return an empty list if help was requested, or a list containing a single element: the result of calling the
         *      {@code Callable}, or a {@code null} element if the last (sub)command was a {@code Runnable}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
            if (printHelpIfRequested(parsedCommands, out, ansi)) { return Collections.emptyList(); }
            CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
            return Arrays.asList(execute(last));
        }
    }
    /**
     * Command line parse result handler that prints help if requested, and otherwise executes the top-level command and
     * all subcommands as {@code Runnable} or {@code Callable}.
     * For use in the {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...) parseWithHandler} methods.
     * @since 2.0 */
    public static class RunAll implements IParseResultHandler {
        /** Prints help if requested, and otherwise executes the top-level command and all subcommands as {@code Runnable}
         * or {@code Callable}. If any of the {@code CommandLine} commands does not implement either
         * {@code Runnable} or {@code Callable}, a {@code ExecutionException}
         * is thrown detailing the problem and capturing the offending {@code CommandLine} object.
         *
         * @param parsedCommands the {@code CommandLine} objects that resulted from successfully parsing the command line arguments
         * @param out the {@code PrintStream} to print help to if requested
         * @param ansi for printing help messages using ANSI styles and colors
         * @return an empty list if help was requested, or a list containing the result of executing all commands:
         *      the return values from calling the {@code Callable} commands, {@code null} elements for commands that implement {@code Runnable}
         * @throws ExecutionException if a problem occurred while processing the parse results; use
         *      {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
         */
        public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, Help.Ansi ansi) {
            if (printHelpIfRequested(parsedCommands, out, ansi)) {
                return null;
            }
            List<Object> result = new ArrayList<Object>();
            for (CommandLine parsed : parsedCommands) {
                result.add(execute(parsed));
            }
            return result;
        }
    }
    /**
     * Returns the result of calling {@link #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...)}
     * with {@code Help.Ansi.AUTO} and a new {@link DefaultExceptionHandler} in addition to the specified parse result handler,
     * {@code PrintStream}, and the specified command line arguments.
     * <p>
     * This is a convenience method intended to offer the same ease of use as the {@link #run(Runnable, PrintStream, Help.Ansi, String...) run}
     * and {@link #call(Callable, PrintStream, Help.Ansi, String...) call} methods, but with more flexibility and better
     * support for nested subcommands.
     * </p>
     * <p>Calling this method roughly expands to:</p>
     * <pre>
     * try {
     *     List&lt;CommandLine&gt; parsedCommands = parse(args);
     *     return parseResultsHandler.handleParseResult(parsedCommands, out, Help.Ansi.AUTO);
     * } catch (ParameterException ex) {
     *     return new DefaultExceptionHandler().handleException(ex, out, ansi, args);
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
     * @param handler the function that will process the result of successfully parsing the command line arguments
     * @param out the {@code PrintStream} to print help to if requested
     * @param args the command line arguments
     * @return a list of results, or an empty list if there are no results
     * @throws ExecutionException if the command line arguments were parsed successfully but a problem occurred while processing the
     *      parse results; use {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
     * @see RunLast
     * @see RunAll
     * @since 2.0 */
    public List<Object> parseWithHandler(IParseResultHandler handler, PrintStream out, String... args) {
        return parseWithHandlers(handler, out, Help.Ansi.AUTO, new DefaultExceptionHandler(), args);
    }
    /**
     * Tries to {@linkplain #parse(String...) parse} the specified command line arguments, and if successful, delegates
     * the processing of the resulting list of {@code CommandLine} objects to the specified {@linkplain IParseResultHandler handler}.
     * If the command line arguments were invalid, the {@code ParameterException} thrown from the {@code parse} method
     * is caught and passed to the specified {@link IExceptionHandler}.
     * <p>
     * This is a convenience method intended to offer the same ease of use as the {@link #run(Runnable, PrintStream, Help.Ansi, String...) run}
     * and {@link #call(Callable, PrintStream, Help.Ansi, String...) call} methods, but with more flexibility and better
     * support for nested subcommands.
     * </p>
     * <p>Calling this method roughly expands to:</p>
     * <pre>
     * try {
     *     List&lt;CommandLine&gt; parsedCommands = parse(args);
     *     return parseResultsHandler.handleParseResult(parsedCommands, out, ansi);
     * } catch (ParameterException ex) {
     *     return new exceptionHandler.handleException(ex, out, ansi, args);
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
     * @param handler the function that will process the result of successfully parsing the command line arguments
     * @param out the {@code PrintStream} to print help to if requested
     * @param ansi for printing help messages using ANSI styles and colors
     * @param exceptionHandler the function that can handle the {@code ParameterException} thrown when the command line arguments are invalid
     * @param args the command line arguments
     * @return a list of results produced by the {@code IParseResultHandler} or the {@code IExceptionHandler}, or an empty list if there are no results
     * @throws ExecutionException if the command line arguments were parsed successfully but a problem occurred while processing the parse
     *      result {@code CommandLine} objects; use {@link ExecutionException#getCommandLine()} to get the command or subcommand where processing failed
     * @see RunLast
     * @see RunAll
     * @see DefaultExceptionHandler
     * @since 2.0 */
    public List<Object> parseWithHandlers(IParseResultHandler handler, PrintStream out, Help.Ansi ansi, IExceptionHandler exceptionHandler, String... args) {
        try {
            List<CommandLine> result = parse(args);
            return handler.handleParseResult(result, out, ansi);
        } catch (ParameterException ex) {
            return exceptionHandler.handleException(ex, out, ansi, args);
        }
    }
    /**
     * Equivalent to {@code new CommandLine(command).usage(out)}. See {@link #usage(PrintStream)} for details.
     * @param command the object annotated with {@link Command}, {@link Option} and {@link Parameters}
     * @param out the print stream to print the help message to
     * @throws IllegalArgumentException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     */
    public static void usage(Object command, PrintStream out) {
        toCommandLine(command).usage(out);
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
        toCommandLine(command).usage(out, ansi);
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
        toCommandLine(command).usage(out, colorScheme);
    }

    /**
     * Delegates to {@link #usage(PrintStream, Help.Ansi)} with the {@linkplain Help.Ansi#AUTO platform default}.
     * @param out the printStream to print to
     * @see #usage(PrintStream, Help.ColorScheme)
     */
    public void usage(PrintStream out) {
        usage(out, Help.Ansi.AUTO);
    }

    /**
     * Delegates to {@link #usage(PrintStream, Help.ColorScheme)} with the {@linkplain Help#defaultColorScheme(CommandLine.Help.Ansi) default color scheme}.
     * @param out the printStream to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @see #usage(PrintStream, Help.ColorScheme)
     */
    public void usage(PrintStream out, Help.Ansi ansi) {
        usage(out, Help.defaultColorScheme(ansi));
    }
    /**
     * Prints a usage help message for the annotated command class to the specified {@code PrintStream}.
     * Delegates construction of the usage help message to the {@link Help} inner class and is equivalent to:
     * <pre>
     * Help help = new Help(command).addAllSubcommands(getSubcommands());
     * StringBuilder sb = new StringBuilder()
     *         .append(help.headerHeading())
     *         .append(help.header())
     *         .append(help.synopsisHeading())      //e.g. Usage:
     *         .append(help.synopsis())             //e.g. &lt;main class&gt; [OPTIONS] &lt;command&gt; [COMMAND-OPTIONS] [ARGUMENTS]
     *         .append(help.descriptionHeading())   //e.g. %nDescription:%n%n
     *         .append(help.description())          //e.g. {"Converts foos to bars.", "Use options to control conversion mode."}
     *         .append(help.parameterListHeading()) //e.g. %nPositional parameters:%n%n
     *         .append(help.parameterList())        //e.g. [FILE...] the files to convert
     *         .append(help.optionListHeading())    //e.g. %nOptions:%n%n
     *         .append(help.optionList())           //e.g. -h, --help   displays this help and exits
     *         .append(help.commandListHeading())   //e.g. %nCommands:%n%n
     *         .append(help.commandList())          //e.g.    add       adds the frup to the frooble
     *         .append(help.footerHeading())
     *         .append(help.footer());
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
     */
    public void usage(PrintStream out, Help.ColorScheme colorScheme) {
        Help help = new Help(interpreter.command, colorScheme).addAllSubcommands(getSubcommands());
        if (!Help.DEFAULT_SEPARATOR.equals(getSeparator())) {
            help.separator = getSeparator();
            help.parameterLabelRenderer = help.createDefaultParamLabelRenderer(); // update for new separator
        }
        if (!Help.DEFAULT_COMMAND_NAME.equals(getCommandName())) {
            help.commandName = getCommandName();
        }
        StringBuilder sb = new StringBuilder()
                .append(help.headerHeading())
                .append(help.header())
                .append(help.synopsisHeading())      //e.g. Usage:
                .append(help.synopsis(help.synopsisHeadingLength())) //e.g. &lt;main class&gt; [OPTIONS] &lt;command&gt; [COMMAND-OPTIONS] [ARGUMENTS]
                .append(help.descriptionHeading())   //e.g. %nDescription:%n%n
                .append(help.description())          //e.g. {"Converts foos to bars.", "Use options to control conversion mode."}
                .append(help.parameterListHeading()) //e.g. %nPositional parameters:%n%n
                .append(help.parameterList())        //e.g. [FILE...] the files to convert
                .append(help.optionListHeading())    //e.g. %nOptions:%n%n
                .append(help.optionList())           //e.g. -h, --help   displays this help and exits
                .append(help.commandListHeading())   //e.g. %nCommands:%n%n
                .append(help.commandList())          //e.g.    add       adds the frup to the frooble
                .append(help.footerHeading())
                .append(help.footer());
        out.print(sb);
    }

    /**
     * Delegates to {@link #printVersionHelp(PrintStream, Help.Ansi)} with the {@linkplain Help.Ansi#AUTO platform default}.
     * @param out the printStream to print to
     * @see #printVersionHelp(PrintStream, Help.Ansi)
     * @since 0.9.8
     */
    public void printVersionHelp(PrintStream out) { printVersionHelp(out, Help.Ansi.AUTO); }

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
        for (String versionInfo : versionLines) {
            out.println(ansi.new Text(versionInfo));
        }
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
        for (String versionInfo : versionLines) {
            out.println(ansi.new Text(String.format(versionInfo, params)));
        }
    }

    /**
     * Delegates to {@link #call(Callable, PrintStream, Help.Ansi, String...)} with {@link Help.Ansi#AUTO}.
     * <p>
     * From picocli v2.0, this method prints usage help or version help if {@linkplain #printHelpIfRequested(List, PrintStream, Help.Ansi) requested},
     * and any exceptions thrown by the {@code Callable} are caught and rethrown wrapped in an {@code ExecutionException}.
     * </p>
     * @param callable the command to call when {@linkplain #parse(String...) parsing} succeeds.
     * @param out the printStream to print to
     * @param args the command line arguments to parse
     * @param <C> the annotated object must implement Callable
     * @param <T> the return type of the most specific command (must implement {@code Callable})
     * @see #call(Callable, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, otherwise returns the result of calling the Callable
     * @see #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...)
     * @see RunFirst
     */
    public static <C extends Callable<T>, T> T call(C callable, PrintStream out, String... args) {
        return call(callable, out, Help.Ansi.AUTO, args);
    }
    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * The annotated object needs to implement {@link Callable}. Calling this method is equivalent to:
     * <pre>
     * CommandLine cmd = new CommandLine(callable);
     * List&lt;CommandLine&gt; parsedCommands;
     * try {
     *     parsedCommands = cmd.parse(args);
     * } catch (ParameterException ex) {
     *     out.println(ex.getMessage());
     *     cmd.usage(out, ansi);
     *     return null;
     * }
     * if (CommandLine.printHelpIfRequested(parsedCommands, out, ansi)) {
     *     return null;
     * }
     * CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
     * try {
     *     Callable&lt;Object&gt; subcommand = last.getCommand();
     *     return subcommand.call();
     * } catch (Exception ex) {
     *     throw new ExecutionException(last, "Error calling " + last.getCommand(), ex);
     * }
     * </pre>
     * <p>
     * If the specified Callable command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed.
     * Commands with subcommands may be interested in calling the {@link #parseWithHandler(IParseResultHandler, PrintStream, String...) parseWithHandler}
     * method with a {@link RunAll} handler or a custom handler.
     * </p><p>
     * From picocli v2.0, this method prints usage help or version help if {@linkplain #printHelpIfRequested(List, PrintStream, Help.Ansi) requested},
     * and any exceptions thrown by the {@code Callable} are caught and rethrown wrapped in an {@code ExecutionException}.
     * </p>
     * @param callable the command to call when {@linkplain #parse(String...) parsing} succeeds.
     * @param out the printStream to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <C> the annotated object must implement Callable
     * @param <T> the return type of the specified {@code Callable}
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Callable throws an exception
     * @return {@code null} if an error occurred while parsing the command line options, or if help was requested and printed. Otherwise returns the result of calling the Callable
     * @see #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...)
     * @see RunLast
     */
    public static <C extends Callable<T>, T> T call(C callable, PrintStream out, Help.Ansi ansi, String... args) {
        CommandLine cmd = new CommandLine(callable); // validate command outside of try-catch
        List<Object> results = cmd.parseWithHandlers(new RunLast(), out, ansi, new DefaultExceptionHandler(), args);
        return results == null || results.isEmpty() ? null : (T) results.get(0);
    }

    /**
     * Delegates to {@link #run(Runnable, PrintStream, Help.Ansi, String...)} with {@link Help.Ansi#AUTO}.
     * <p>
     * From picocli v2.0, this method prints usage help or version help if {@linkplain #printHelpIfRequested(List, PrintStream, Help.Ansi) requested},
     * and any exceptions thrown by the {@code Runnable} are caught and rethrown wrapped in an {@code ExecutionException}.
     * </p>
     * @param runnable the command to run when {@linkplain #parse(String...) parsing} succeeds.
     * @param out the printStream to print to
     * @param args the command line arguments to parse
     * @param <R> the annotated object must implement Runnable
     * @see #run(Runnable, PrintStream, Help.Ansi, String...)
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...)
     * @see RunFirst
     */
    public static <R extends Runnable> void run(R runnable, PrintStream out, String... args) {
        run(runnable, out, Help.Ansi.AUTO, args);
    }
    /**
     * Convenience method to allow command line application authors to avoid some boilerplate code in their application.
     * The annotated object needs to implement {@link Runnable}. Calling this method is equivalent to:
     * <pre>
     * CommandLine cmd = new CommandLine(runnable);
     * List&lt;CommandLine&gt; parsedCommands;
     * try {
     *     parsedCommands = cmd.parse(args);
     * } catch (ParameterException ex) {
     *     out.println(ex.getMessage());
     *     cmd.usage(out, ansi);
     *     return null;
     * }
     * if (CommandLine.printHelpIfRequested(parsedCommands, out, ansi)) {
     *     return null;
     * }
     * CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
     * try {
     *     Runnable subcommand = last.getCommand();
     *     subcommand.run();
     * } catch (Exception ex) {
     *     throw new ExecutionException(last, "Error running " + last.getCommand(), ex);
     * }
     * </pre>
     * <p>
     * If the specified Runnable command has subcommands, the {@linkplain RunLast last} subcommand specified on the
     * command line is executed.
     * Commands with subcommands may be interested in calling the {@link #parseWithHandler(IParseResultHandler, PrintStream, String...) parseWithHandler}
     * method with a {@link RunAll} handler or a custom handler.
     * </p><p>
     * From picocli v2.0, this method prints usage help or version help if {@linkplain #printHelpIfRequested(List, PrintStream, Help.Ansi) requested},
     * and any exceptions thrown by the {@code Runnable} are caught and rethrown wrapped in an {@code ExecutionException}.
     * </p>
     * @param runnable the command to run when {@linkplain #parse(String...) parsing} succeeds.
     * @param out the printStream to print to
     * @param ansi whether the usage message should include ANSI escape codes or not
     * @param args the command line arguments to parse
     * @param <R> the annotated object must implement Runnable
     * @throws InitializationException if the specified command object does not have a {@link Command}, {@link Option} or {@link Parameters} annotation
     * @throws ExecutionException if the Runnable throws an exception
     * @see #parseWithHandlers(IParseResultHandler, PrintStream, Help.Ansi, IExceptionHandler, String...)
     * @see RunLast
     */
    public static <R extends Runnable> void run(R runnable, PrintStream out, Help.Ansi ansi, String... args) {
        CommandLine cmd = new CommandLine(runnable); // validate command outside of try-catch
        cmd.parseWithHandlers(new RunLast(), out, ansi, new DefaultExceptionHandler(), args);
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
        for (CommandLine command : interpreter.commands.values()) {
            command.registerConverter(cls, converter);
        }
        return this;
    }

    /** Returns the String that separates option names from option values when parsing command line options. {@value Help#DEFAULT_SEPARATOR} by default.
     * @return the String the parser uses to separate option names from option values */
    public String getSeparator() {
        return interpreter.separator;
    }

    /** Sets the String the parser uses to separate option names from option values to the specified value.
     * The separator may also be set declaratively with the {@link CommandLine.Command#separator()} annotation attribute.
     * @param separator the String that separates option names from option values
     * @return this {@code CommandLine} object, to allow method chaining */
    public CommandLine setSeparator(String separator) {
        interpreter.separator = Assert.notNull(separator, "separator");
        return this;
    }

    /** Returns the command name (also called program name) displayed in the usage help synopsis. {@value Help#DEFAULT_COMMAND_NAME} by default.
     * @return the command name (also called program name) displayed in the usage */
    public String getCommandName() {
        return commandName;
    }

    /** Sets the command name (also called program name) displayed in the usage help synopsis to the specified value.
     * Note that this method only modifies the usage help message, it does not impact parsing behaviour.
     * The command name may also be set declaratively with the {@link CommandLine.Command#name()} annotation attribute.
     * @param commandName command name (also called program name) displayed in the usage help synopsis
     * @return this {@code CommandLine} object, to allow method chaining */
    public CommandLine setCommandName(String commandName) {
        this.commandName = Assert.notNull(commandName, "commandName");
        return this;
    }
    private static boolean empty(String str) { return str == null || str.trim().length() == 0; }
    private static boolean empty(Object[] array) { return array == null || array.length == 0; }
    private static boolean empty(Text txt) { return txt == null || txt.plain.toString().trim().length() == 0; }
    private static String str(String[] arr, int i) { return (arr == null || arr.length == 0) ? "" : arr[i]; }
    private static boolean isBoolean(Class<?> type) { return type == Boolean.class || type == Boolean.TYPE; }
    private static CommandLine toCommandLine(Object obj) { return obj instanceof CommandLine ? (CommandLine) obj : new CommandLine(obj);}
    private static boolean isMultiValue(Field field) {  return isMultiValue(field.getType()); }
    private static boolean isMultiValue(Class<?> cls) { return cls.isArray() || Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls); }
    private static Class<?>[] getTypeAttribute(Field field) {
        Class<?>[] explicit = field.isAnnotationPresent(Parameters.class) ? field.getAnnotation(Parameters.class).type() : field.getAnnotation(Option.class).type();
        if (explicit.length > 0) { return explicit; }
        if (field.getType().isArray()) { return new Class<?>[] { field.getType().getComponentType() }; }
        if (isMultiValue(field)) {
            Type type = field.getGenericType(); // e.g. Map<Long, ? extends Number>
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] paramTypes = parameterizedType.getActualTypeArguments(); // e.g. ? extends Number
                Class<?>[] result = new Class<?>[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    if (paramTypes[i] instanceof Class) { result[i] = (Class<?>) paramTypes[i]; continue; } // e.g. Long
                    if (paramTypes[i] instanceof WildcardType) { // e.g. ? extends Number
                        WildcardType wildcardType = (WildcardType) paramTypes[i];
                        Type[] lower = wildcardType.getLowerBounds(); // e.g. []
                        if (lower.length > 0 && lower[0] instanceof Class) { result[i] = (Class<?>) lower[0]; continue; }
                        Type[] upper = wildcardType.getUpperBounds(); // e.g. Number
                        if (upper.length > 0 && upper[0] instanceof Class) { result[i] = (Class<?>) upper[0]; continue; }
                    }
                    Arrays.fill(result, String.class); return result; // too convoluted generic type, giving up
                }
                return result; // we inferred all types from ParameterizedType
            }
            return new Class<?>[] {String.class, String.class}; // field is multi-value but not ParameterizedType
        }
        return new Class<?>[] {field.getType()}; // not a multi-value field
    }
    /**
     * <p>
     * Annotate fields in your class with {@code @Option} and picocli will initialize these fields when matching
     * arguments are specified on the command line.
     * </p><p>
     * For example:
     * </p>
     * <pre>import static picocli.CommandLine.*;
     *
     * public class MyClass {
     *     &#064;Parameters(type = File.class, description = "Any number of input files")
     *     private List&lt;File&gt; files = new ArrayList&lt;File&gt;();
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
     *
     *     &#064;Option(names = { "-v", "--verbose"}, description = "Verbosely list files processed")
     *     private boolean verbose;
     *
     *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, usageHelp = true, description = "Display this help and exit")
     *     private boolean help;
     *
     *     &#064;Option(names = { "-V", "--version"}, versionHelp = true, description = "Display version information and exit")
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
         * If an option is required, but a user invokes the program without specifying the required option,
         * a {@link MissingParameterException} is thrown from the {@link #parse(String...)} method.
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
        boolean help() default false;

        /**
         * Set {@code usageHelp=true} if this option allows the user to request usage help. If this option is
         * specified on the command line, picocli will not validate the remaining arguments (so no "missing required
         * option" errors) and the {@link CommandLine#isUsageHelpRequested()} method will return {@code true}.
         * <p>
         * This attribute is useful for special options like help ({@code -h} and {@code --help} on unix,
         * {@code -?} and {@code -Help} on Windows).
         * </p>
         * <p>
         * Note that the {@link #parse(String...)} method will not print usage help documentation. It will only set
         * the value of the annotated field. It is the responsibility of the caller to inspect the annotated fields
         * and take the appropriate action.
         * </p>
         * @return whether this option allows the user to request usage help
         * @since 0.9.8
         */
        boolean usageHelp() default false;

        /**
         * Set {@code versionHelp=true} if this option allows the user to request version information. If this option is
         * specified on the command line, picocli will not validate the remaining arguments (so no "missing required
         * option" errors) and the {@link CommandLine#isVersionHelpRequested()} method will return {@code true}.
         * <p>
         * This attribute is useful for special options like version ({@code -V} and {@code --version} on unix,
         * {@code -Version} on Windows).
         * </p>
         * <p>
         * Note that the {@link #parse(String...)} method will not print version information. It will only set
         * the value of the annotated field. It is the responsibility of the caller to inspect the annotated fields
         * and take the appropriate action.
         * </p>
         * @return whether this option allows the user to request version information
         * @since 0.9.8
         */
        boolean versionHelp() default false;

        /**
         * Description of this option, used when generating the usage documentation.
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
         * Specify a regular expression to use to split option parameter values before applying them to the field.
         * All elements resulting from the split are added to the array or Collection. Ignored for single-value fields.
         * @return a regular expression to split option parameter values or {@code ""} if the value should not be split
         * @see String#split(String)
         */
        String split() default "";

        /**
         * Set {@code hidden=true} if this option should not be included in the usage documentation.
         * @return whether this option should be excluded from the usage message
         */
        boolean hidden() default false;
    }
    /**
     * <p>
     * Fields annotated with {@code @Parameters} will be initialized with positional parameters. By specifying the
     * {@link #index()} attribute you can pick which (or what range) of the positional parameters to apply. If no index
     * is specified, the field will get all positional parameters (so it should be an array or a collection).
     * </p><p>
     * When parsing the command line arguments, picocli first tries to match arguments to {@link Option Options}.
     * Positional parameters are the arguments that follow the options, or the arguments that follow a "--" (double
     * dash) argument on the command line.
     * </p><p>
     * For example:
     * </p>
     * <pre>import static picocli.CommandLine.*;
     *
     * public class MyCalcParameters {
     *     &#064;Parameters(type = BigDecimal.class, description = "Any number of input numbers")
     *     private List&lt;BigDecimal&gt; files = new ArrayList&lt;BigDecimal&gt;();
     *
     *     &#064;Option(names = { "-h", "--help", "-?", "-help"}, help = true, description = "Display this help and exit")
     *     private boolean help;
     * }
     * </pre><p>
     * A field cannot be annotated with both {@code @Parameters} and {@code @Option} or a {@code ParameterException}
     * is thrown.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Parameters {
        /** Specify an index ("0", or "1", etc.) to pick which of the command line arguments should be assigned to this
         * field. For array or Collection fields, you can also specify an index range ("0..3", or "2..*", etc.) to assign
         * a subset of the command line arguments to this field. The default is "*", meaning all command line arguments.
         * @return an index or range specifying which of the command line arguments should be assigned to this field
         */
        String index() default "*";

        /** Description of the parameter(s), used when generating the usage documentation.
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
    }

    /**
     * <p>Annotate your class with {@code @Command} when you want more control over the format of the generated help
     * message.
     * </p><pre>
     * &#064;Command(name      = "Encrypt",
     *        description = "Encrypt FILE(s), or standard input, to standard output or to the output file.",
     *        footer      = "Copyright (c) 2017")
     * public class Encrypt {
     *     &#064;Parameters(paramLabel = "FILE", type = File.class, description = "Any number of input files")
     *     private List&lt;File&gt; files     = new ArrayList&lt;File&gt;();
     *
     *     &#064;Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
     *     private File outputFile;
     * }</pre>
     * <p>
     * The structure of a help message looks like this:
     * </p><ul>
     *   <li>[header]</li>
     *   <li>[synopsis]: {@code Usage: <commandName> [OPTIONS] [FILE...]}</li>
     *   <li>[description]</li>
     *   <li>[parameter list]: {@code      [FILE...]   Any number of input files}</li>
     *   <li>[option list]: {@code   -h, --help   prints this help message and exits}</li>
     *   <li>[footer]</li>
     * </ul> */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE})
    public @interface Command {
        /** Program name to show in the synopsis. If omitted, {@code "<main class>"} is used.
         * For {@linkplain #subcommands() declaratively added} subcommands, this attribute is also used
         * by the parser to recognize subcommands in the command line arguments.
         * @return the program name to show in the synopsis
         * @see Help#commandName */
        String name() default "<main class>";

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
         * @since 0.9.8
         */
        Class<?>[] subcommands() default {};

        /** String that separates options from option parameters. Default is {@code "="}. Spaces are also accepted.
         * @return the string that separates options from option parameters, used both when parsing and when generating usage help
         * @see Help#separator
         * @see CommandLine#setSeparator(String) */
        String separator() default "=";

        /** Version information for this command, to print to the console when the user specifies an
         * {@linkplain Option#versionHelp() option} to request version help. This is not part of the usage help message.
         *
         * @return a string or an array of strings with version information about this command.
         * @since 0.9.8
         * @see CommandLine#printVersionHelp(PrintStream)
         */
        String[] version() default {};

        /** Set the heading preceding the header section. May contain embedded {@linkplain java.util.Formatter format specifiers}.
         * @return the heading preceding the header section
         * @see Help#headerHeading(Object...)  */
        String headerHeading() default "";

        /** Optional summary description of the command, shown before the synopsis.
         * @return summary description of the command
         * @see Help#header
         * @see Help#header(Object...)  */
        String[] header() default {};

        /** Set the heading preceding the synopsis text. May contain embedded
         * {@linkplain java.util.Formatter format specifiers}. The default heading is {@code "Usage: "} (without a line
         * break between the heading and the synopsis text).
         * @return the heading preceding the synopsis text
         * @see Help#synopsisHeading(Object...)  */
        String synopsisHeading() default "Usage: ";

        /** Specify {@code true} to generate an abbreviated synopsis like {@code "<main> [OPTIONS] [PARAMETERS...]"}.
         * By default, a detailed synopsis with individual option names and parameters is generated.
         * @return whether the synopsis should be abbreviated
         * @see Help#abbreviateSynopsis
         * @see Help#abbreviatedSynopsis()
         * @see Help#detailedSynopsis(Comparator, boolean) */
        boolean abbreviateSynopsis() default false;

        /** Specify one or more custom synopsis lines to display instead of an auto-generated synopsis.
         * @return custom synopsis text to replace the auto-generated synopsis
         * @see Help#customSynopsis
         * @see Help#customSynopsis(Object...) */
        String[] customSynopsis() default {};

        /** Set the heading preceding the description section. May contain embedded {@linkplain java.util.Formatter format specifiers}.
         * @return the heading preceding the description section
         * @see Help#descriptionHeading(Object...)  */
        String descriptionHeading() default "";

        /** Optional text to display between the synopsis line(s) and the list of options.
         * @return description of this command
         * @see Help#description
         * @see Help#description(Object...) */
        String[] description() default {};

        /** Set the heading preceding the parameters list. May contain embedded {@linkplain java.util.Formatter format specifiers}.
         * @return the heading preceding the parameters list
         * @see Help#parameterListHeading(Object...)  */
        String parameterListHeading() default "";

        /** Set the heading preceding the options list. May contain embedded {@linkplain java.util.Formatter format specifiers}.
         * @return the heading preceding the options list
         * @see Help#optionListHeading(Object...)  */
        String optionListHeading() default "";

        /** Specify {@code false} to show Options in declaration order. The default is to sort alphabetically.
         * @return whether options should be shown in alphabetic order.
         * @see Help#sortOptions */
        boolean sortOptions() default true;

        /** Prefix required options with this character in the options list. The default is no marker: the synopsis
         * indicates which options and parameters are required.
         * @return the character to show in the options list to mark required options
         * @see Help#requiredOptionMarker */
        char requiredOptionMarker() default ' ';

        /** Specify {@code true} to show default values in the description column of the options list (except for
         * boolean options). False by default.
         * @return whether the default values for options and parameters should be shown in the description column
         * @see Help#showDefaultValues */
        boolean showDefaultValues() default false;

        /** Set the heading preceding the subcommands list. May contain embedded {@linkplain java.util.Formatter format specifiers}.
         * The default heading is {@code "Commands:%n"} (with a line break at the end).
         * @return the heading preceding the subcommands list
         * @see Help#commandListHeading(Object...)  */
        String commandListHeading() default "Commands:%n";

        /** Set the heading preceding the footer section. May contain embedded {@linkplain java.util.Formatter format specifiers}.
         * @return the heading preceding the footer section
         * @see Help#footerHeading(Object...)  */
        String footerHeading() default "";

        /** Optional text to display after the list of options.
         * @return text to display after the list of options
         * @see Help#footer
         * @see Help#footer(Object...) */
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
    /** Describes the number of parameters required and accepted by an option or a positional parameter.
     * @since 0.9.7
     */
    public static class Range implements Comparable<Range> {
        /** Required number of parameters for an option or positional parameter. */
        public final int min;
        /** Maximum accepted number of parameters for an option or positional parameter. */
        public final int max;
        public final boolean isVariable;
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
        public static Range optionArity(Field field) {
            return field.isAnnotationPresent(Option.class)
                    ? adjustForType(Range.valueOf(field.getAnnotation(Option.class).arity()), field)
                    : new Range(0, 0, false, true, "0");
        }
        /** Returns a new {@code Range} based on the {@link Parameters#arity()} annotation on the specified field,
         * or the field type's default arity if no arity was specified.
         * @param field the field whose Parameters annotation to inspect
         * @return a new {@code Range} based on the Parameters arity annotation on the specified field */
        public static Range parameterArity(Field field) {
            return field.isAnnotationPresent(Parameters.class)
                    ? adjustForType(Range.valueOf(field.getAnnotation(Parameters.class).arity()), field)
                    : new Range(0, 0, false, true, "0");
        }
        /** Returns a new {@code Range} based on the {@link Parameters#index()} annotation on the specified field.
         * @param field the field whose Parameters annotation to inspect
         * @return a new {@code Range} based on the Parameters index annotation on the specified field */
        public static Range parameterIndex(Field field) {
            return field.isAnnotationPresent(Parameters.class)
                    ? Range.valueOf(field.getAnnotation(Parameters.class).index())
                    : new Range(0, 0, false, true, "0");
        }
        static Range adjustForType(Range result, Field field) {
            return result.isUnspecified ? defaultArity(field) : result;
        }
        /** Returns the default arity {@code Range}: for {@link Option options} this is 0 for booleans and 1 for
         * other types, for {@link Parameters parameters} booleans have arity 0, arrays or Collections have
         * arity "0..*", and other types have arity 1.
         * @param field the field whose default arity to return
         * @return a new {@code Range} indicating the default arity of the specified field
         * @since 2.0 */
        public static Range defaultArity(Field field) {
            Class<?> type = field.getType();
            if (field.isAnnotationPresent(Option.class)) {
                return defaultArity(type);
            }
            if (isMultiValue(type)) {
                return Range.valueOf("0..1");
            }
            return Range.valueOf("1");// for single-valued fields (incl. boolean positional parameters)
        }
        /** Returns the default arity {@code Range} for {@link Option options}: booleans have arity 0, other types have arity 1.
         * @param type the type whose default arity to return
         * @return a new {@code Range} indicating the default arity of the specified type */
        public static Range defaultArity(Class<?> type) {
            return isBoolean(type) ? Range.valueOf("0") : Range.valueOf("1");
        }
        private int size() { return 1 + max - min; }
        static Range parameterCapacity(Field field) {
            Range arity = parameterArity(field);
            if (!isMultiValue(field)) { return arity; }
            Range index = parameterIndex(field);
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
            return min == max ? String.valueOf(min) : min + ".." + (isVariable ? "*" : max);
        }
        public int compareTo(Range other) {
            int result = min - other.min;
            return (result == 0) ? max - other.max : result;
        }
    }
    static void init(Class<?> cls,
                              List<Field> requiredFields,
                              Map<String, Field> optionName2Field,
                              Map<Character, Field> singleCharOption2Field,
                              List<Field> positionalParametersFields) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Option.class)) {
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
                if (field.isAnnotationPresent(Option.class)) {
                    throw new DuplicateOptionAnnotationsException("A field can be either @Option or @Parameters, but '"
                            + field.getName() + "' is both.");
                }
                positionalParametersFields.add(field);
                Range arity = Range.parameterArity(field);
                if (arity.min > 0) {
                    requiredFields.add(field);
                }
            }
        }
    }
    static void validatePositionalParameters(List<Field> positionalParametersFields) {
        int min = 0;
        for (Field field : positionalParametersFields) {
            Range index = Range.parameterIndex(field);
            if (index.min > min) {
                throw new ParameterIndexGapException("Missing field annotated with @Parameter(index=" + min +
                        "). Nearest field '" + field.getName() + "' has index=" + index.min);
            }
            min = Math.max(min, index.max);
            min = min == Integer.MAX_VALUE ? min : min + 1;
        }
    }
    private static <T> Stack<T> reverse(Stack<T> stack) {
        Collections.reverse(stack);
        return stack;
    }
    /**
     * Helper class responsible for processing command line arguments.
     */
    private class Interpreter {
        private final Map<String, CommandLine> commands                  = new LinkedHashMap<String, CommandLine>();
        private final Map<Class<?>, ITypeConverter<?>> converterRegistry = new HashMap<Class<?>, ITypeConverter<?>>();
        private final Map<String, Field> optionName2Field                = new HashMap<String, Field>();
        private final Map<Character, Field> singleCharOption2Field       = new HashMap<Character, Field>();
        private final List<Field> requiredFields                         = new ArrayList<Field>();
        private final List<Field> positionalParametersFields             = new ArrayList<Field>();
        private final Object command;
        private boolean isHelpRequested;
        private String separator = Help.DEFAULT_SEPARATOR;
        private int position;

        Interpreter(Object command) {
            converterRegistry.put(Object.class,        new BuiltIn.StringConverter());
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

            this.command                 = Assert.notNull(command, "command");
            Class<?> cls                 = command.getClass();
            String declaredName          = null;
            String declaredSeparator     = null;
            boolean hasCommandAnnotation = false;
            while (cls != null) {
                init(cls, requiredFields, optionName2Field, singleCharOption2Field, positionalParametersFields);
                if (cls.isAnnotationPresent(Command.class)) {
                    hasCommandAnnotation = true;
                    Command cmd = cls.getAnnotation(Command.class);
                    declaredSeparator = (declaredSeparator == null) ? cmd.separator() : declaredSeparator;
                    declaredName = (declaredName == null) ? cmd.name() : declaredName;
                    CommandLine.this.versionLines.addAll(Arrays.asList(cmd.version()));

                    for (Class<?> sub : cmd.subcommands()) {
                        Command subCommand = sub.getAnnotation(Command.class);
                        if (subCommand == null || Help.DEFAULT_COMMAND_NAME.equals(subCommand.name())) {
                            throw new InitializationException("Subcommand " + sub.getName() +
                                    " is missing the mandatory @Command annotation with a 'name' attribute");
                        }
                        try {
                            Constructor<?> constructor = sub.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            CommandLine commandLine = toCommandLine(constructor.newInstance());
                            commandLine.parent = CommandLine.this;
                            commands.put(subCommand.name(), commandLine);
                        }
                        catch (InitializationException ex) { throw ex; }
                        catch (NoSuchMethodException ex) { throw new InitializationException("Cannot instantiate subcommand " +
                                sub.getName() + ": the class has no constructor", ex); }
                        catch (Exception ex) {
                            throw new InitializationException("Could not instantiate and add subcommand " +
                                    sub.getName() + ": " + ex, ex);
                        }
                    }
                }
                cls = cls.getSuperclass();
            }
            separator = declaredSeparator != null ? declaredSeparator : separator;
            CommandLine.this.commandName = declaredName != null ? declaredName : CommandLine.this.commandName;
            Collections.sort(positionalParametersFields, new PositionalParametersSorter());
            validatePositionalParameters(positionalParametersFields);

            if (positionalParametersFields.isEmpty() && optionName2Field.isEmpty() && !hasCommandAnnotation) {
                throw new InitializationException(command + " (" + command.getClass() +
                        ") is not a command: it has no @Command, @Option or @Parameters annotations");
            }
        }

        /**
         * Entry point into parsing command line arguments.
         * @param args the command line arguments
         * @return a list with all commands and subcommands initialized by this method
         * @throws ParameterException if the specified command line arguments are invalid
         */
        List<CommandLine> parse(String... args) {
            Assert.notNull(args, "argument array");
            if (tracer.isInfo()) {tracer.info("Parsing %d command line args %s%n", args.length, Arrays.toString(args));}
            Stack<String> arguments = new Stack<String>();
            for (int i = args.length - 1; i >= 0; i--) {
                arguments.push(args[i]);
            }
            List<CommandLine> result = new ArrayList<CommandLine>();
            parse(result, arguments, args);
            return result;
        }

        private void parse(List<CommandLine> parsedCommands, Stack<String> argumentStack, String[] originalArgs) {
            // first reset any state in case this CommandLine instance is being reused
            isHelpRequested = false;
            CommandLine.this.versionHelpRequested = false;
            CommandLine.this.usageHelpRequested = false;

            Class<?> cmdClass = this.command.getClass();
            if (tracer.isDebug()) {tracer.debug("Initializing %s: %d options, %d positional parameters, %d required, %d subcommands.%n", cmdClass.getName(), new HashSet<Field>(optionName2Field.values()).size(), positionalParametersFields.size(), requiredFields.size(), commands.size());}
            parsedCommands.add(CommandLine.this);
            List<Field> required = new ArrayList<Field>(requiredFields);
            Set<Field> initialized = new HashSet<Field>();
            Collections.sort(required, new PositionalParametersSorter());
            try {
                processArguments(parsedCommands, argumentStack, required, initialized, originalArgs);
            } catch (ParameterException ex) {
                throw ex;
            } catch (Exception ex) {
                int offendingArgIndex = originalArgs.length - argumentStack.size() - 1;
                String arg = offendingArgIndex >= 0 && offendingArgIndex < originalArgs.length ? originalArgs[offendingArgIndex] : "?";
                throw ParameterException.create(CommandLine.this, ex, arg, offendingArgIndex, originalArgs);
            }
            if (!isAnyHelpRequested() && !required.isEmpty()) {
                for (Field missing : required) {
                    if (missing.isAnnotationPresent(Option.class)) {
                        throw MissingParameterException.create(CommandLine.this, required, separator);
                    } else {
                        assertNoMissingParameters(missing, Range.parameterArity(missing).min, argumentStack);
                    }
                }
            }
            if (!unmatchedArguments.isEmpty()) {
                if (!isUnmatchedArgumentsAllowed()) { throw new UnmatchedArgumentException(CommandLine.this, unmatchedArguments); }
                if (tracer.isWarn()) { tracer.warn("Unmatched arguments: %s%n", unmatchedArguments); }
            }
        }

        private void processArguments(List<CommandLine> parsedCommands,
                                      Stack<String> args,
                                      Collection<Field> required,
                                      Set<Field> initialized,
                                      String[] originalArgs) throws Exception {
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
                if (tracer.isDebug()) {tracer.debug("Processing argument '%s'. Remainder=%s%n", arg, reverse((Stack<String>) args.clone()));}

                // Double-dash separates options from positional arguments.
                // If found, then interpret the remaining args as positional parameters.
                if ("--".equals(arg)) {
                    tracer.info("Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n");
                    processRemainderAsPositionalParameters(required, initialized, args);
                    return; // we are done
                }

                // if we find another command, we are done with the current command
                if (commands.containsKey(arg)) {
                    if (!isHelpRequested && !required.isEmpty()) { // ensure current command portion is valid
                        throw MissingParameterException.create(CommandLine.this, required, separator);
                    }
                    if (tracer.isDebug()) {tracer.debug("Found subcommand '%s' (%s)%n", arg, commands.get(arg).interpreter.command.getClass().getName());}
                    commands.get(arg).interpreter.parse(parsedCommands, args, originalArgs);
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
                    if (optionName2Field.containsKey(key) && !optionName2Field.containsKey(arg)) {
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
                if (optionName2Field.containsKey(arg)) {
                    processStandaloneOption(required, initialized, arg, args, paramAttachedToOption);
                }
                // Compact (single-letter) options can be grouped with other options or with an argument.
                // only single-letter options can be combined with other options or with an argument
                else if (arg.length() > 2 && arg.startsWith("-")) {
                    if (tracer.isDebug()) {tracer.debug("Trying to process '%s' as clustered short options%n", arg, args);}
                    processClusteredShortOptions(required, initialized, arg, args);
                }
                // The argument could not be interpreted as an option.
                // We take this to mean that the remainder are positional arguments
                else {
                    args.push(arg);
                    if (tracer.isDebug()) {tracer.debug("Could not find option '%s', deciding whether to treat as unmatched option or positional parameter...%n", arg);}
                    if (resemblesOption(arg)) { handleUnmatchedArguments(args.pop()); continue; } // #149
                    if (tracer.isDebug()) {tracer.debug("No option named '%s' found. Processing remainder as positional parameters%n", arg);}
                    processPositionalParameter(required, initialized, args);
                }
            }
        }
        private boolean resemblesOption(String arg) {
            int count = 0;
            for (String optionName : optionName2Field.keySet()) {
                for (int i = 0; i < arg.length(); i++) {
                    if (optionName.length() > i && arg.charAt(i) == optionName.charAt(i)) { count++; } else { break; }
                }
            }
            boolean result = count > 0 && count * 10 >= optionName2Field.size() * 9; // at least one prefix char in common with 9 out of 10 options
            if (tracer.isDebug()) {tracer.debug("%s %s an option: %d matching prefix chars out of %d option names%n", arg, (result ? "resembles" : "doesn't resemble"), count, optionName2Field.size());}
            return result;
        }
        private void handleUnmatchedArguments(String arg) {Stack<String> args = new Stack<String>(); args.add(arg); handleUnmatchedArguments(args);}
        private void handleUnmatchedArguments(Stack<String> args) {
            while (!args.isEmpty()) { unmatchedArguments.add(args.pop()); } // addAll would give args in reverse order
        }

        private void processRemainderAsPositionalParameters(Collection<Field> required, Set<Field> initialized, Stack<String> args) throws Exception {
            while (!args.empty()) {
                processPositionalParameter(required, initialized, args);
            }
        }
        private void processPositionalParameter(Collection<Field> required, Set<Field> initialized, Stack<String> args) throws Exception {
            if (tracer.isDebug()) {tracer.debug("Processing next arg as a positional parameter at index=%d. Remainder=%s%n", position, reverse((Stack<String>) args.clone()));}
            int consumed = 0;
            for (Field positionalParam : positionalParametersFields) {
                Range indexRange = Range.parameterIndex(positionalParam);
                if (!indexRange.contains(position)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Stack<String> argsCopy = (Stack<String>) args.clone();
                Range arity = Range.parameterArity(positionalParam);
                if (tracer.isDebug()) {tracer.debug("Position %d is in index range %s. Trying to assign args to %s, arity=%s%n", position, indexRange, positionalParam, arity);}
                assertNoMissingParameters(positionalParam, arity.min, argsCopy);
                int originalSize = argsCopy.size();
                applyOption(positionalParam, Parameters.class, arity, false, argsCopy, initialized, "args[" + indexRange + "] at position " + position);
                int count = originalSize - argsCopy.size();
                if (count > 0) { required.remove(positionalParam); }
                consumed = Math.max(consumed, count);
            }
            // remove processed args from the stack
            for (int i = 0; i < consumed; i++) { args.pop(); }
            position += consumed;
            if (tracer.isDebug()) {tracer.debug("Consumed %d arguments, moving position to index %d.%n", consumed, position);}
            if (consumed == 0 && !args.isEmpty()) {
                handleUnmatchedArguments(args.pop());
            }
        }

        private void processStandaloneOption(Collection<Field> required,
                                             Set<Field> initialized,
                                             String arg,
                                             Stack<String> args,
                                             boolean paramAttachedToKey) throws Exception {
            Field field = optionName2Field.get(arg);
            required.remove(field);
            Range arity = Range.optionArity(field);
            if (paramAttachedToKey) {
                arity = arity.min(Math.max(1, arity.min)); // if key=value, minimum arity is at least 1
            }
            if (tracer.isDebug()) {tracer.debug("Found option named '%s': field %s, arity=%s%n", arg, field, arity);}
            applyOption(field, Option.class, arity, paramAttachedToKey, args, initialized, "option " + arg);
        }

        private void processClusteredShortOptions(Collection<Field> required,
                                                  Set<Field> initialized,
                                                  String arg,
                                                  Stack<String> args)
                throws Exception {
            String prefix = arg.substring(0, 1);
            String cluster = arg.substring(1);
            boolean paramAttachedToOption = true;
            do {
                if (cluster.length() > 0 && singleCharOption2Field.containsKey(cluster.charAt(0))) {
                    Field field = singleCharOption2Field.get(cluster.charAt(0));
                    Range arity = Range.optionArity(field);
                    String argDescription = "option " + prefix + cluster.charAt(0);
                    if (tracer.isDebug()) {tracer.debug("Found option '%s%s' in %s: field %s, arity=%s%n", prefix, cluster.charAt(0), arg, field, arity);}
                    required.remove(field);
                    cluster = cluster.length() > 0 ? cluster.substring(1) : "";
                    paramAttachedToOption = cluster.length() > 0;
                    if (cluster.startsWith(separator)) {// attached with separator, like -f=FILE or -v=true
                        cluster = cluster.substring(separator.length());
                        arity = arity.min(Math.max(1, arity.min)); // if key=value, minimum arity is at least 1
                    }
                    if (arity.min > 0 && !empty(cluster)) {
                        if (tracer.isDebug()) {tracer.debug("Trying to process '%s' as option parameter%n", cluster);}
                    }
                    // arity may be >= 1, or
                    // arity <= 0 && !cluster.startsWith(separator)
                    // e.g., boolean @Option("-v", arity=0, varargs=true); arg "-rvTRUE", remainder cluster="TRUE"
                    if (!empty(cluster)) {
                        args.push(cluster); // interpret remainder as option parameter
                    }
                    int consumed = applyOption(field, Option.class, arity, paramAttachedToOption, args, initialized, argDescription);
                    // only return if cluster (and maybe more) was consumed, otherwise continue do-while loop
                    if (empty(cluster) || consumed > 0 || args.isEmpty()) {
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
                            if (resemblesOption(arg)) { handleUnmatchedArguments(args.pop()); return; } // #149
                            processPositionalParameter(required, initialized, args);
                            return;
                        }
                        // remainder was part of a clustered group that could not be completely parsed
                        if (tracer.isDebug()) {tracer.debug("No option found for %s in %s%n", cluster, arg);}
                        handleUnmatchedArguments(args.pop());
                    } else {
                        args.push(cluster);
                        if (tracer.isDebug()) {tracer.debug("%s is not an option parameter for %s%n", cluster, arg);}
                        processPositionalParameter(required, initialized, args);
                    }
                    return;
                }
            } while (true);
        }

        private int applyOption(Field field,
                                Class<?> annotation,
                                Range arity,
                                boolean valueAttachedToOption,
                                Stack<String> args,
                                Set<Field> initialized,
                                String argDescription) throws Exception {
            updateHelpRequested(field);
            int length = args.size();
            assertNoMissingParameters(field, arity.min, args);

            Class<?> cls = field.getType();
            if (cls.isArray()) {
                return applyValuesToArrayField(field, annotation, arity, args, cls, argDescription);
            }
            if (Collection.class.isAssignableFrom(cls)) {
                return applyValuesToCollectionField(field, annotation, arity, args, cls, argDescription);
            }
            if (Map.class.isAssignableFrom(cls)) {
                return applyValuesToMapField(field, annotation, arity, args, cls, argDescription);
            }
            cls = getTypeAttribute(field)[0]; // field may be interface/abstract type, use annotation to get concrete type
            return applyValueToSingleValuedField(field, arity, args, cls, initialized, argDescription);
        }

        private int applyValueToSingleValuedField(Field field,
                                                  Range arity,
                                                  Stack<String> args,
                                                  Class<?> cls,
                                                  Set<Field> initialized,
                                                  String argDescription) throws Exception {
            boolean noMoreValues = args.isEmpty();
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
                    Boolean currentValue = (Boolean) field.get(command);
                    value = String.valueOf(currentValue == null ? true : !currentValue); // #147 toggle existing boolean value
                }
            }
            if (noMoreValues && value == null) {
                return 0;
            }
            ITypeConverter<?> converter = getTypeConverter(cls, field);
            Object newValue = tryConvert(field, -1, converter, value, cls);
            Object oldValue = field.get(command);
            TraceLevel level = TraceLevel.INFO;
            String traceMessage = "Setting %s field '%s.%s' to '%5$s' (was '%4$s') for %6$s%n";
            if (initialized != null) {
                if (initialized.contains(field)) {
                    if (!isOverwrittenOptionsAllowed()) {
                        throw new OverwrittenOptionException(CommandLine.this, optionDescription("", field, 0) +  " should be specified only once");
                    }
                    level = TraceLevel.WARN;
                    traceMessage = "Overwriting %s field '%s.%s' value '%s' with '%s' for %s%n";
                }
                initialized.add(field);
            }
            if (tracer.level.isEnabled(level)) { level.print(tracer, traceMessage, field.getType().getSimpleName(),
                        field.getDeclaringClass().getSimpleName(), field.getName(), String.valueOf(oldValue), String.valueOf(newValue), argDescription);
            }
            field.set(command, newValue);
            return result;
        }
        private int applyValuesToMapField(Field field,
                                          Class<?> annotation,
                                          Range arity,
                                          Stack<String> args,
                                          Class<?> cls,
                                          String argDescription) throws Exception {
            Class<?>[] classes = getTypeAttribute(field);
            if (classes.length < 2) { throw new ParameterException(CommandLine.this, "Field " + field + " needs two types (one for the map key, one for the value) but only has " + classes.length + " types configured."); }
            ITypeConverter<?> keyConverter   = getTypeConverter(classes[0], field);
            ITypeConverter<?> valueConverter = getTypeConverter(classes[1], field);
            Map<Object, Object> result = (Map<Object, Object>) field.get(command);
            if (result == null) {
                result = createMap(cls);
                field.set(command, result);
            }
            int originalSize = result.size();
            consumeMapArguments(field, arity, args, classes, keyConverter, valueConverter, result, argDescription);
            return result.size() - originalSize;
        }

        private void consumeMapArguments(Field field,
                                         Range arity,
                                         Stack<String> args,
                                         Class<?>[] classes,
                                         ITypeConverter<?> keyConverter,
                                         ITypeConverter<?> valueConverter,
                                         Map<Object, Object> result,
                                         String argDescription) throws Exception {
            // first do the arity.min mandatory parameters
            for (int i = 0; i < arity.min; i++) {
                consumeOneMapArgument(field, arity, args, classes, keyConverter, valueConverter, result, i, argDescription);
            }
            // now process the varargs if any
            for (int i = arity.min; i < arity.max && !args.isEmpty(); i++) {
                if (!field.isAnnotationPresent(Parameters.class)) {
                    if (commands.containsKey(args.peek()) || isOption(args.peek())) {
                        return;
                    }
                }
                consumeOneMapArgument(field, arity, args, classes, keyConverter, valueConverter, result, i, argDescription);
            }
        }

        private void consumeOneMapArgument(Field field,
                                           Range arity,
                                           Stack<String> args,
                                           Class<?>[] classes,
                                           ITypeConverter<?> keyConverter, ITypeConverter<?> valueConverter,
                                           Map<Object, Object> result,
                                           int index,
                                           String argDescription) throws Exception {
            String[] values = split(trim(args.pop()), field);
            for (String value : values) {
                String[] keyValue = value.split("=");
                if (keyValue.length < 2) {
                    String splitRegex = splitRegex(field);
                    if (splitRegex.length() == 0) {
                        throw new ParameterException(CommandLine.this, "Value for option " + optionDescription("", field,
                                0) + " should be in KEY=VALUE format but was " + value);
                    } else {
                        throw new ParameterException(CommandLine.this, "Value for option " + optionDescription("", field,
                                0) + " should be in KEY=VALUE[" + splitRegex + "KEY=VALUE]... format but was " + value);
                    }
                }
                Object mapKey =   tryConvert(field, index, keyConverter,   keyValue[0], classes[0]);
                Object mapValue = tryConvert(field, index, valueConverter, keyValue[1], classes[1]);
                result.put(mapKey, mapValue);
                if (tracer.isInfo()) {tracer.info("Putting [%s : %s] in %s<%s, %s> field '%s.%s' for %s%n", String.valueOf(mapKey), String.valueOf(mapValue),
                        result.getClass().getSimpleName(), classes[0].getSimpleName(), classes[1].getSimpleName(), field.getDeclaringClass().getSimpleName(), field.getName(), argDescription);}
            }
        }

        private void checkMaxArityExceeded(Range arity, int remainder, Field field, String[] values) {
            if (values.length <= remainder) { return; }
            String desc = arity.max == remainder ? "" + remainder : arity + ", remainder=" + remainder;
            throw new MaxValuesforFieldExceededException(CommandLine.this, optionDescription("", field, -1) +
                    " max number of values (" + arity.max + ") exceeded: remainder is " + remainder + " but " +
                    values.length + " values were specified: " + Arrays.toString(values));
        }

        private int applyValuesToArrayField(Field field,
                                            Class<?> annotation,
                                            Range arity,
                                            Stack<String> args,
                                            Class<?> cls,
                                            String argDescription) throws Exception {
            Object existing = field.get(command);
            int length = existing == null ? 0 : Array.getLength(existing);
            Class<?> type = getTypeAttribute(field)[0];
            List<Object> converted = consumeArguments(field, annotation, arity, args, type, length, argDescription);
            List<Object> newValues = new ArrayList<Object>();
            for (int i = 0; i < length; i++) {
                newValues.add(Array.get(existing, i));
            }
            for (Object obj : converted) {
                if (obj instanceof Collection<?>) {
                    newValues.addAll((Collection<?>) obj);
                } else {
                    newValues.add(obj);
                }
            }
            Object array = Array.newInstance(type, newValues.size());
            field.set(command, array);
            for (int i = 0; i < newValues.size(); i++) {
                Array.set(array, i, newValues.get(i));
            }
            return converted.size(); // return how many args were consumed
        }

        @SuppressWarnings("unchecked")
        private int applyValuesToCollectionField(Field field,
                                                 Class<?> annotation,
                                                 Range arity,
                                                 Stack<String> args,
                                                 Class<?> cls,
                                                 String argDescription) throws Exception {
            Collection<Object> collection = (Collection<Object>) field.get(command);
            Class<?> type = getTypeAttribute(field)[0];
            int length = collection == null ? 0 : collection.size();
            List<Object> converted = consumeArguments(field, annotation, arity, args, type, length, argDescription);
            if (collection == null) {
                collection = createCollection(cls);
                field.set(command, collection);
            }
            for (Object element : converted) {
                if (element instanceof Collection<?>) {
                    collection.addAll((Collection<?>) element);
                } else {
                    collection.add(element);
                }
            }
            return converted.size();
        }

        private List<Object> consumeArguments(Field field,
                                              Class<?> annotation,
                                              Range arity,
                                              Stack<String> args,
                                              Class<?> type,
                                              int originalSize,
                                              String argDescription) throws Exception {
            List<Object> result = new ArrayList<Object>();

            // first do the arity.min mandatory parameters
            for (int i = 0; i < arity.min; i++) {
                consumeOneArgument(field, arity, args, type, result, i, originalSize, argDescription);
            }
            // now process the varargs if any
            for (int i = arity.min; i < arity.max && !args.isEmpty(); i++) {
                if (annotation != Parameters.class) { // for vararg Options, we stop if we encounter '--', a command, or another option
                    if (commands.containsKey(args.peek()) || isOption(args.peek())) {
                        return result;
                    }
                }
                consumeOneArgument(field, arity, args, type, result, i, originalSize, argDescription);
            }
            return result;
        }

        private int consumeOneArgument(Field field,
                                       Range arity,
                                       Stack<String> args,
                                       Class<?> type,
                                       List<Object> result,
                                       int index,
                                       int originalSize,
                                       String argDescription) throws Exception {
            String[] values = split(trim(args.pop()), field);
            ITypeConverter<?> converter = getTypeConverter(type, field);

            for (int j = 0; j < values.length; j++) {
                result.add(tryConvert(field, index, converter, values[j], type));
                if (tracer.isInfo()) {
                    if (field.getType().isArray()) {
                        tracer.info("Adding [%s] to %s[] field '%s.%s' for %s%n", String.valueOf(result.get(result.size() - 1)), type.getSimpleName(), field.getDeclaringClass().getSimpleName(), field.getName(), argDescription);
                    } else {
                        tracer.info("Adding [%s] to %s<%s> field '%s.%s' for %s%n", String.valueOf(result.get(result.size() - 1)), field.getType().getSimpleName(), type.getSimpleName(), field.getDeclaringClass().getSimpleName(), field.getName(), argDescription);
                    }
                }
            }
            //checkMaxArityExceeded(arity, max, field, values);
            return ++index;
        }

        private String splitRegex(Field field) {
            if (field.isAnnotationPresent(Option.class))     { return field.getAnnotation(Option.class).split(); }
            if (field.isAnnotationPresent(Parameters.class)) { return field.getAnnotation(Parameters.class).split(); }
            return "";
        }
        private String[] split(String value, Field field) {
            String regex = splitRegex(field);
            return regex.length() == 0 ? new String[] {value} : value.split(regex);
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
            } catch (TypeConversionException ex) {
                throw new ParameterException(CommandLine.this, ex.getMessage() + optionDescription(" for ", field, index));
            } catch (Exception other) {
                String desc = optionDescription(" for ", field, index) + ": " + other;
                throw new ParameterException(CommandLine.this, "Could not convert '" + value + "' to " + type.getSimpleName() + desc, other);
            }
        }

        private String optionDescription(String prefix, Field field, int index) {
            Help.IParamLabelRenderer labelRenderer = Help.createMinimalParamLabelRenderer();
            String desc = "";
            if (field.isAnnotationPresent(Option.class)) {
                desc = prefix + "option '" + field.getAnnotation(Option.class).names()[0] + "'";
                if (index >= 0) {
                    Range arity = Range.optionArity(field);
                    if (arity.max > 1) {
                        desc += " at index " + index;
                    }
                    desc += " (" + labelRenderer.renderParameterLabel(field, Help.Ansi.OFF, Collections.<IStyle>emptyList()) + ")";
                }
            } else if (field.isAnnotationPresent(Parameters.class)) {
                Range indexRange = Range.parameterIndex(field);
                Text label = labelRenderer.renderParameterLabel(field, Help.Ansi.OFF, Collections.<IStyle>emptyList());
                desc = prefix + "positional parameter at index " + indexRange + " (" + label + ")";
            }
            return desc;
        }

        private boolean isAnyHelpRequested() { return isHelpRequested || versionHelpRequested || usageHelpRequested; }

        private void updateHelpRequested(Field field) {
            if (field.isAnnotationPresent(Option.class)) {
                isHelpRequested                       |= is(field, "help", field.getAnnotation(Option.class).help());
                CommandLine.this.versionHelpRequested |= is(field, "versionHelp", field.getAnnotation(Option.class).versionHelp());
                CommandLine.this.usageHelpRequested   |= is(field, "usageHelp", field.getAnnotation(Option.class).usageHelp());
            }
        }
        private boolean is(Field f, String description, boolean value) {
            if (value) { if (tracer.isInfo()) {tracer.info("Field '%s.%s' has '%s' annotation: not validating required fields%n", f.getDeclaringClass().getSimpleName(), f.getName(), description); }}
            return value;
        }
        @SuppressWarnings("unchecked")
        private Collection<Object> createCollection(Class<?> collectionClass) throws Exception {
            if (collectionClass.isInterface()) {
                if (List.class.isAssignableFrom(collectionClass)) {
                    return new ArrayList<Object>();
                } else if (SortedSet.class.isAssignableFrom(collectionClass)) {
                    return new TreeSet<Object>();
                } else if (Set.class.isAssignableFrom(collectionClass)) {
                    return new LinkedHashSet<Object>();
                } else if (Queue.class.isAssignableFrom(collectionClass)) {
                    return new LinkedList<Object>(); // ArrayDeque is only available since 1.6
                }
                return new ArrayList<Object>();
            }
            // custom Collection implementation class must have default constructor
            return (Collection<Object>) collectionClass.newInstance();
        }
        private Map<Object, Object> createMap(Class<?> mapClass) throws Exception {
            try { // if it is an implementation class, instantiate it
                return (Map<Object, Object>) mapClass.newInstance();
            } catch (Exception ignored) {}
            return new LinkedHashMap<Object, Object>();
        }
        private ITypeConverter<?> getTypeConverter(final Class<?> type, Field field) {
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
            throw new MissingTypeConverterException(CommandLine.this, "No TypeConverter registered for " + type.getName() + " of field " + field);
        }

        private void assertNoMissingParameters(Field field, int arity, Stack<String> args) {
            if (arity > args.size()) {
                if (arity == 1) {
                    if (field.isAnnotationPresent(Option.class)) {
                        throw new MissingParameterException(CommandLine.this, "Missing required parameter for " +
                                optionDescription("", field, 0));
                    }
                    Range indexRange = Range.parameterIndex(field);
                    Help.IParamLabelRenderer labelRenderer = Help.createMinimalParamLabelRenderer();
                    String sep = "";
                    String names = "";
                    int count = 0;
                    for (int i = indexRange.min; i < positionalParametersFields.size(); i++) {
                        if (Range.parameterArity(positionalParametersFields.get(i)).min > 0) {
                            names += sep + labelRenderer.renderParameterLabel(positionalParametersFields.get(i),
                                    Help.Ansi.OFF, Collections.<IStyle>emptyList());
                            sep = ", ";
                            count++;
                        }
                    }
                    String msg = "Missing required parameter";
                    Range paramArity = Range.parameterArity(field);
                    if (paramArity.isVariable) {
                        msg += "s at positions " + indexRange + ": ";
                    } else {
                        msg += (count > 1 ? "s: " : ": ");
                    }
                    throw new MissingParameterException(CommandLine.this, msg + names);
                }
                if (args.isEmpty()) {
                    throw new MissingParameterException(CommandLine.this, optionDescription("", field, 0) +
                            " requires at least " + arity + " values, but none were specified.");
                }
                throw new MissingParameterException(CommandLine.this, optionDescription("", field, 0) +
                        " requires at least " + arity + " values, but only " + args.size() + " were specified: " + reverse(args));
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
    private static class PositionalParametersSorter implements Comparator<Field> {
        public int compare(Field o1, Field o2) {
            int result = Range.parameterIndex(o1).compareTo(Range.parameterIndex(o2));
            return (result == 0) ? Range.parameterArity(o1).compareTo(Range.parameterArity(o2)) : result;
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
        /** Converts text to a {@code Byte} by delegating to {@link Byte#valueOf(String)}.*/
        static class ByteConverter implements ITypeConverter<Byte> {
            public Byte convert(String value) { return Byte.valueOf(value); }
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
        /** Converts text to a {@code Short} by delegating to {@link Short#valueOf(String)}.*/
        static class ShortConverter implements ITypeConverter<Short> {
            public Short convert(String value) { return Short.valueOf(value); }
        }
        /** Converts text to an {@code Integer} by delegating to {@link Integer#valueOf(String)}.*/
        static class IntegerConverter implements ITypeConverter<Integer> {
            public Integer convert(String value) { return Integer.valueOf(value); }
        }
        /** Converts text to a {@code Long} by delegating to {@link Long#valueOf(String)}.*/
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
                throw new TypeConversionException("'" + value + "' is not a HH:mm[:ss[.SSS]] time");
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
        private BuiltIn() {} // private constructor: never instantiate
    }

    /**
     * A collection of methods and inner classes that provide fine-grained control over the contents and layout of
     * the usage help message to display to end users when help is requested or invalid input values were specified.
     * <h3>Layered API</h3>
     * <p>The {@link Command} annotation provides the easiest way to customize usage help messages. See
     * the <a href="https://remkop.github.io/picocli/index.html#_usage_help">Manual</a> for details.</p>
     * <p>This Help class provides high-level functions to create sections of the usage help message and headings
     * for these sections. Instead of calling the {@link CommandLine#usage(PrintStream, CommandLine.Help.ColorScheme)}
     * method, application authors may want to create a custom usage help message by reorganizing sections in a
     * different order and/or adding custom sections.</p>
     * <p>Finally, the Help class contains inner classes and interfaces that can be used to create custom help messages.</p>
     * <h4>IOptionRenderer and IParameterRenderer</h4>
     * <p>Renders a field annotated with {@link Option} or {@link Parameters} to an array of {@link Text} values.
     * By default, these values are</p><ul>
     * <li>mandatory marker character (if the option/parameter is {@link Option#required() required})</li>
     * <li>short option name (empty for parameters)</li>
     * <li>comma or empty (empty for parameters)</li>
     * <li>long option names (the parameter {@link IParamLabelRenderer label} for parameters)</li>
     * <li>description</li>
     * </ul>
     * <p>Other components rely on this ordering.</p>
     * <h4>Layout</h4>
     * <p>Delegates to the renderers to create {@link Text} values for the annotated fields, and uses a
     * {@link TextTable} to display these values in tabular format. Layout is responsible for deciding which values
     * to display where in the table. By default, Layout shows one option or parameter per table row.</p>
     * <h4>TextTable</h4>
     * <p>Responsible for spacing out {@link Text} values according to the {@link Column} definitions the table was
     * created with. Columns have a width, indentation, and an overflow policy that decides what to do if a value is
     * longer than the column's width.</p>
     * <h4>Text</h4>
     * <p>Encapsulates rich text with styles and colors in a way that other components like {@link TextTable} are
     * unaware of the embedded ANSI escape codes.</p>
     */
    public static class Help {
        /** Constant String holding the default program name: {@value} */
        protected static final String DEFAULT_COMMAND_NAME = "<main class>";

        /** Constant String holding the default string that separates options from option parameters: {@value} */
        protected static final String DEFAULT_SEPARATOR = "=";

        private final static int usageHelpWidth = 80;
        private final static int optionsColumnWidth = 2 + 2 + 1 + 24;
        private final Object command;
        private final Map<String, Help> commands = new LinkedHashMap<String, Help>();
        final ColorScheme colorScheme;

        /** Immutable list of fields annotated with {@link Option}, in declaration order. */
        public final List<Field> optionFields;

        /** Immutable list of fields annotated with {@link Parameters}, or an empty list if no such field exists. */
        public final List<Field> positionalParametersFields;

        /** The String to use as the separator between options and option parameters. {@code "="} by default,
         * initialized from {@link Command#separator()} if defined.
         * @see #parameterLabelRenderer */
        public String separator;

        /** The String to use as the program name in the synopsis line of the help message.
         * {@link #DEFAULT_COMMAND_NAME} by default, initialized from {@link Command#name()} if defined. */
        public String commandName = DEFAULT_COMMAND_NAME;

        /** Optional text lines to use as the description of the help message, displayed between the synopsis and the
         * options list. Initialized from {@link Command#description()} if the {@code Command} annotation is present,
         * otherwise this is an empty array and the help message has no description.
         * Applications may programmatically set this field to create a custom help message. */
        public String[] description = {};

        /** Optional custom synopsis lines to use instead of the auto-generated synopsis.
         * Initialized from {@link Command#customSynopsis()} if the {@code Command} annotation is present,
         * otherwise this is an empty array and the synopsis is generated.
         * Applications may programmatically set this field to create a custom help message. */
        public String[] customSynopsis = {};

        /** Optional header lines displayed at the top of the help message. For subcommands, the first header line is
         * displayed in the list of commands. Values are initialized from {@link Command#header()}
         * if the {@code Command} annotation is present, otherwise this is an empty array and the help message has no
         * header. Applications may programmatically set this field to create a custom help message. */
        public String[] header = {};

        /** Optional footer text lines displayed at the bottom of the help message. Initialized from
         * {@link Command#footer()} if the {@code Command} annotation is present, otherwise this is an empty array and
         * the help message has no footer.
         * Applications may programmatically set this field to create a custom help message. */
        public String[] footer = {};

        /** Option and positional parameter value label renderer used for the synopsis line(s) and the option list.
         * By default initialized to the result of {@link #createDefaultParamLabelRenderer()}, which takes a snapshot
         * of the {@link #separator} at construction time. If the separator is modified after Help construction, you
         * may need to re-initialize this field by calling {@link #createDefaultParamLabelRenderer()} again. */
        public IParamLabelRenderer parameterLabelRenderer;

        /** If {@code true}, the synopsis line(s) will show an abbreviated synopsis without detailed option names. */
        public Boolean abbreviateSynopsis;

        /** If {@code true}, the options list is sorted alphabetically. */
        public Boolean sortOptions;

        /** If {@code true}, the options list will show default values for all options except booleans. */
        public Boolean showDefaultValues;

        /** Character used to prefix required options in the options list. */
        public Character requiredOptionMarker;

        /** Optional heading preceding the header section. Initialized from {@link Command#headerHeading()}, or null. */
        public String headerHeading;

        /** Optional heading preceding the synopsis. Initialized from {@link Command#synopsisHeading()}, {@code "Usage: "} by default. */
        public String synopsisHeading;

        /** Optional heading preceding the description section. Initialized from {@link Command#descriptionHeading()}, or null. */
        public String descriptionHeading;

        /** Optional heading preceding the parameter list. Initialized from {@link Command#parameterListHeading()}, or null. */
        public String parameterListHeading;

        /** Optional heading preceding the options list. Initialized from {@link Command#optionListHeading()}, or null. */
        public String optionListHeading;

        /** Optional heading preceding the subcommand list. Initialized from {@link Command#commandListHeading()}. {@code "Commands:%n"} by default. */
        public String commandListHeading;

        /** Optional heading preceding the footer section. Initialized from {@link Command#footerHeading()}, or null. */
        public String footerHeading;

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
         * @param colorScheme the color scheme to use */
        public Help(Object command, ColorScheme colorScheme) {
            this.command = Assert.notNull(command, "command");
            this.colorScheme = Assert.notNull(colorScheme, "colorScheme").applySystemProperties();
            List<Field> options = new ArrayList<Field>();
            List<Field> operands = new ArrayList<Field>();
            Class<?> cls = command.getClass();
            while (cls != null) {
                for (Field field : cls.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Option.class)) {
                        Option option = field.getAnnotation(Option.class);
                        if (!option.hidden()) { // hidden options should not appear in usage help
                            // TODO remember longest concatenated option string length (issue #45)
                            options.add(field);
                        }
                    }
                    if (field.isAnnotationPresent(Parameters.class)) {
                        operands.add(field);
                    }
                }
                // superclass values should not overwrite values if both class and superclass have a @Command annotation
                if (cls.isAnnotationPresent(Command.class)) {
                    Command cmd = cls.getAnnotation(Command.class);
                    if (DEFAULT_COMMAND_NAME.equals(commandName)) {
                        commandName = cmd.name();
                    }
                    separator = (separator == null) ? cmd.separator() : separator;
                    abbreviateSynopsis = (abbreviateSynopsis == null) ? cmd.abbreviateSynopsis() : abbreviateSynopsis;
                    sortOptions = (sortOptions == null) ? cmd.sortOptions() : sortOptions;
                    requiredOptionMarker = (requiredOptionMarker == null) ? cmd.requiredOptionMarker() : requiredOptionMarker;
                    showDefaultValues = (showDefaultValues == null) ? cmd.showDefaultValues() : showDefaultValues;
                    customSynopsis = empty(customSynopsis) ? cmd.customSynopsis() : customSynopsis;
                    description = empty(description) ? cmd.description() : description;
                    header = empty(header) ? cmd.header() : header;
                    footer = empty(footer) ? cmd.footer() : footer;
                    headerHeading = empty(headerHeading) ? cmd.headerHeading() : headerHeading;
                    synopsisHeading = empty(synopsisHeading) || "Usage: ".equals(synopsisHeading) ? cmd.synopsisHeading() : synopsisHeading;
                    descriptionHeading = empty(descriptionHeading) ? cmd.descriptionHeading() : descriptionHeading;
                    parameterListHeading = empty(parameterListHeading) ? cmd.parameterListHeading() : parameterListHeading;
                    optionListHeading = empty(optionListHeading) ? cmd.optionListHeading() : optionListHeading;
                    commandListHeading = empty(commandListHeading) || "Commands:%n".equals(commandListHeading) ? cmd.commandListHeading() : commandListHeading;
                    footerHeading = empty(footerHeading) ? cmd.footerHeading() : footerHeading;
                }
                cls = cls.getSuperclass();
            }
            sortOptions =          (sortOptions == null)          ? true : sortOptions;
            abbreviateSynopsis =   (abbreviateSynopsis == null)   ? false : abbreviateSynopsis;
            requiredOptionMarker = (requiredOptionMarker == null) ? ' ' : requiredOptionMarker;
            showDefaultValues =    (showDefaultValues == null)    ? false : showDefaultValues;
            synopsisHeading =      (synopsisHeading == null)      ? "Usage: " : synopsisHeading;
            commandListHeading =   (commandListHeading == null)   ? "Commands:%n" : commandListHeading;
            separator =            (separator == null)            ? DEFAULT_SEPARATOR : separator;
            parameterLabelRenderer = createDefaultParamLabelRenderer(); // uses help separator
            Collections.sort(operands, new PositionalParametersSorter());
            positionalParametersFields = Collections.unmodifiableList(operands);
            optionFields                 = Collections.unmodifiableList(options);
        }

        /** Registers all specified subcommands with this Help.
         * @param commands maps the command names to the associated CommandLine object
         * @return this Help instance (for method chaining)
         * @see CommandLine#getSubcommands()
         */
        public Help addAllSubcommands(Map<String, CommandLine> commands) {
            if (commands != null) {
                for (Map.Entry<String, CommandLine> entry : commands.entrySet()) {
                    addSubcommand(entry.getKey(), entry.getValue().getCommand());
                }
            }
            return this;
        }

        /** Registers the specified subcommand with this Help.
         * @param commandName the name of the subcommand to display in the usage message
         * @param command the annotated object to get more information from
         * @return this Help instance (for method chaining)
         */
        public Help addSubcommand(String commandName, Object command) {
            commands.put(commandName, new Help(command));
            return this;
        }

        /** Returns a synopsis for the command without reserving space for the synopsis heading.
         * @return a synopsis
         * @see #abbreviatedSynopsis()
         * @see #detailedSynopsis(Comparator, boolean)
         * @deprecated use {@link #synopsis(int)} instead
         */
        public String synopsis() { return synopsis(0); }

        /**
         * Returns a synopsis for the command, reserving the specified space for the synopsis heading.
         * @param synopsisHeadingLength the length of the synopsis heading that will be displayed on the same line
         * @return a synopsis
         * @see #abbreviatedSynopsis()
         * @see #detailedSynopsis(Comparator, boolean)
         * @see #synopsisHeading
         */
        public String synopsis(int synopsisHeadingLength) {
            if (!empty(customSynopsis)) { return customSynopsis(); }
            return abbreviateSynopsis ? abbreviatedSynopsis()
                    : detailedSynopsis(synopsisHeadingLength, createShortOptionArityAndNameComparator(), true);
        }

        /** Generates a generic synopsis like {@code <command name> [OPTIONS] [PARAM1 [PARAM2]...]}, omitting parts
         * that don't apply to the command (e.g., does not show [OPTIONS] if the command has no options).
         * @return a generic synopsis */
        public String abbreviatedSynopsis() {
            StringBuilder sb = new StringBuilder();
            if (!optionFields.isEmpty()) { // only show if annotated object actually has options
                sb.append(" [OPTIONS]");
            }
            // sb.append(" [--] "); // implied
            for (Field positionalParam : positionalParametersFields) {
                if (!positionalParam.getAnnotation(Parameters.class).hidden()) {
                    sb.append(' ').append(parameterLabelRenderer.renderParameterLabel(positionalParam, ansi(), colorScheme.parameterStyles));
                }
            }
            return colorScheme.commandText(commandName).toString()
                    + (sb.toString()) + System.getProperty("line.separator");
        }
        /** Generates a detailed synopsis message showing all options and parameters. Follows the unix convention of
         * showing optional options and parameters in square brackets ({@code [ ]}).
         * @param optionSort comparator to sort options or {@code null} if options should not be sorted
         * @param clusterBooleanOptions {@code true} if boolean short options should be clustered into a single string
         * @return a detailed synopsis
         * @deprecated use {@link #detailedSynopsis(int, Comparator, boolean)} instead. */
        public String detailedSynopsis(Comparator<Field> optionSort, boolean clusterBooleanOptions) {
            return detailedSynopsis(0, optionSort, clusterBooleanOptions);
        }

        /** Generates a detailed synopsis message showing all options and parameters. Follows the unix convention of
         * showing optional options and parameters in square brackets ({@code [ ]}).
         * @param synopsisHeadingLength the length of the synopsis heading that will be displayed on the same line
         * @param optionSort comparator to sort options or {@code null} if options should not be sorted
         * @param clusterBooleanOptions {@code true} if boolean short options should be clustered into a single string
         * @return a detailed synopsis */
        public String detailedSynopsis(int synopsisHeadingLength, Comparator<Field> optionSort, boolean clusterBooleanOptions) {
            Text optionText = ansi().new Text(0);
            List<Field> fields = new ArrayList<Field>(optionFields); // iterate in declaration order
            if (optionSort != null) {
                Collections.sort(fields, optionSort);// iterate in specified sort order
            }
            if (clusterBooleanOptions) { // cluster all short boolean options into a single string
                List<Field> booleanOptions = new ArrayList<Field>();
                StringBuilder clusteredRequired = new StringBuilder("-");
                StringBuilder clusteredOptional = new StringBuilder("-");
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
                if (clusteredRequired.length() > 1) { // initial length was 1
                    optionText = optionText.append(" ").append(colorScheme.optionText(clusteredRequired.toString()));
                }
                if (clusteredOptional.length() > 1) { // initial length was 1
                    optionText = optionText.append(" [").append(colorScheme.optionText(clusteredOptional.toString())).append("]");
                }
            }
            for (Field field : fields) {
                Option option = field.getAnnotation(Option.class);
                if (!option.hidden()) {
                    if (option.required()) {
                        optionText = appendOptionSynopsis(optionText, field, ShortestFirst.sort(option.names())[0], " ", "");
                        if (isMultiValue(field)) {
                            optionText = appendOptionSynopsis(optionText, field, ShortestFirst.sort(option.names())[0], " [", "]...");
                        }
                    } else {
                        optionText = appendOptionSynopsis(optionText, field, ShortestFirst.sort(option.names())[0], " [", "]");
                        if (isMultiValue(field)) {
                            optionText = optionText.append("...");
                        }
                    }
                }
            }
            for (Field positionalParam : positionalParametersFields) {
                if (!positionalParam.getAnnotation(Parameters.class).hidden()) {
                    optionText = optionText.append(" ");
                    Text label = parameterLabelRenderer.renderParameterLabel(positionalParam, colorScheme.ansi(), colorScheme.parameterStyles);
                    optionText = optionText.append(label);
                }
            }
            // Fix for #142: first line of synopsis overshoots max. characters
            int firstColumnLength = commandName.length() + synopsisHeadingLength;

            // synopsis heading ("Usage: ") may be on the same line, so adjust column width
            TextTable textTable = new TextTable(ansi(), firstColumnLength, usageHelpWidth - firstColumnLength);
            textTable.indentWrappedLines = 1; // don't worry about first line: options (2nd column) always start with a space

            // right-adjust the command name by length of synopsis heading
            Text PADDING = Ansi.OFF.new Text(stringOf('X', synopsisHeadingLength));
            textTable.addRowValues(new Text[] {PADDING.append(colorScheme.commandText(commandName)), optionText});
            return textTable.toString().substring(synopsisHeadingLength); // cut off leading synopsis heading spaces
        }

        private Text appendOptionSynopsis(Text optionText, Field field, String optionName, String prefix, String suffix) {
            Text optionParamText = parameterLabelRenderer.renderParameterLabel(field, colorScheme.ansi(), colorScheme.optionParamStyles);
            return optionText.append(prefix)
                    .append(colorScheme.optionText(optionName))
                    .append(optionParamText)
                    .append(suffix);
        }

        /** Returns the number of characters the synopsis heading will take on the same line as the synopsis.
         * @return the number of characters the synopsis heading will take on the same line as the synopsis.
         * @see #detailedSynopsis(int, Comparator, boolean)
         */
        public int synopsisHeadingLength() {
            String[] lines = Ansi.OFF.new Text(synopsisHeading).toString().split("\\r?\\n|\\r|%n", -1);
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
            Comparator<Field> sortOrder = sortOptions == null || sortOptions.booleanValue()
                    ? createShortOptionNameComparator()
                    : null;
            return optionList(createDefaultLayout(), sortOrder, parameterLabelRenderer);
        }

        /** Sorts all {@code Options} with the specified {@code comparator} (if the comparator is non-{@code null}),
         * then {@linkplain Layout#addOption(Field, CommandLine.Help.IParamLabelRenderer) adds} all non-hidden options to the
         * specified TextTable and returns the result of TextTable.toString().
         * @param layout responsible for rendering the option list
         * @param optionSort determines in what order {@code Options} should be listed. Declared order if {@code null}
         * @param valueLabelRenderer used for options with a parameter
         * @return the fully formatted option list
         */
        public String optionList(Layout layout, Comparator<Field> optionSort, IParamLabelRenderer valueLabelRenderer) {
            List<Field> fields = new ArrayList<Field>(optionFields); // options are stored in order of declaration
            if (optionSort != null) {
                Collections.sort(fields, optionSort); // default: sort options ABC
            }
            layout.addOptions(fields, valueLabelRenderer);
            return layout.toString();
        }

        /**
         * Returns the section of the usage help message that lists the parameters with their descriptions.
         * @return the section of the usage help message that lists the parameters
         */
        public String parameterList() {
            return parameterList(createDefaultLayout(), parameterLabelRenderer);
        }
        /**
         * Returns the section of the usage help message that lists the parameters with their descriptions.
         * @param layout the layout to use
         * @param paramLabelRenderer for rendering parameter names
         * @return the section of the usage help message that lists the parameters
         */
        public String parameterList(Layout layout, IParamLabelRenderer paramLabelRenderer) {
            layout.addPositionalParameters(positionalParametersFields, paramLabelRenderer);
            return layout.toString();
        }

        private static String heading(Ansi ansi, String values, Object... params) {
            StringBuilder sb = join(ansi, new String[] {values}, new StringBuilder(), params);
            String result = sb.toString();
            result = result.endsWith(System.getProperty("line.separator"))
                    ? result.substring(0, result.length() - System.getProperty("line.separator").length()) : result;
            return result + new String(spaces(countTrailingSpaces(values)));
        }
        private static char[] spaces(int length) { char[] result = new char[length]; Arrays.fill(result, ' '); return result; }
        private static int countTrailingSpaces(String str) {
            if (str == null) {return 0;}
            int trailingSpaces = 0;
            for (int i = str.length() - 1; i >= 0 && str.charAt(i) == ' '; i--) { trailingSpaces++; }
            return trailingSpaces;
        }

        /** Formats each of the specified values and appends it to the specified StringBuilder.
         * @param ansi whether the result should contain ANSI escape codes or not
         * @param values the values to format and append to the StringBuilder
         * @param sb the StringBuilder to collect the formatted strings
         * @param params the parameters to pass to the format method when formatting each value
         * @return the specified StringBuilder */
        public static StringBuilder join(Ansi ansi, String[] values, StringBuilder sb, Object... params) {
            if (values != null) {
                TextTable table = new TextTable(ansi, usageHelpWidth);
                table.indentWrappedLines = 0;
                for (String summaryLine : values) {
                    Text[] lines = ansi.new Text(format(summaryLine, params)).splitLines();
                    for (Text line : lines) {  table.addRowValues(line); }
                }
                table.toString(sb);
            }
            return sb;
        }
        private static String format(String formatString,  Object... params) {
            return formatString == null ? "" : String.format(formatString, params);
        }
        /** Returns command custom synopsis as a string. A custom synopsis can be zero or more lines, and can be
         * specified declaratively with the {@link Command#customSynopsis()} annotation attribute or programmatically
         * by setting the Help instance's {@link Help#customSynopsis} field.
         * @param params Arguments referenced by the format specifiers in the synopsis strings
         * @return the custom synopsis lines combined into a single String (which may be empty)
         */
        public String customSynopsis(Object... params) {
            return join(ansi(), customSynopsis, new StringBuilder(), params).toString();
        }
        /** Returns command description text as a string. Description text can be zero or more lines, and can be specified
         * declaratively with the {@link Command#description()} annotation attribute or programmatically by
         * setting the Help instance's {@link Help#description} field.
         * @param params Arguments referenced by the format specifiers in the description strings
         * @return the description lines combined into a single String (which may be empty)
         */
        public String description(Object... params) {
            return join(ansi(), description, new StringBuilder(), params).toString();
        }
        /** Returns the command header text as a string. Header text can be zero or more lines, and can be specified
         * declaratively with the {@link Command#header()} annotation attribute or programmatically by
         * setting the Help instance's {@link Help#header} field.
         * @param params Arguments referenced by the format specifiers in the header strings
         * @return the header lines combined into a single String (which may be empty)
         */
        public String header(Object... params) {
            return join(ansi(), header, new StringBuilder(), params).toString();
        }
        /** Returns command footer text as a string. Footer text can be zero or more lines, and can be specified
         * declaratively with the {@link Command#footer()} annotation attribute or programmatically by
         * setting the Help instance's {@link Help#footer} field.
         * @param params Arguments referenced by the format specifiers in the footer strings
         * @return the footer lines combined into a single String (which may be empty)
         */
        public String footer(Object... params) {
            return join(ansi(), footer, new StringBuilder(), params).toString();
        }

        /** Returns the text displayed before the header text; the result of {@code String.format(headerHeading, params)}.
         * @param params the parameters to use to format the header heading
         * @return the formatted header heading */
        public String headerHeading(Object... params) {
            return heading(ansi(), headerHeading, params);
        }

        /** Returns the text displayed before the synopsis text; the result of {@code String.format(synopsisHeading, params)}.
         * @param params the parameters to use to format the synopsis heading
         * @return the formatted synopsis heading */
        public String synopsisHeading(Object... params) {
            return heading(ansi(), synopsisHeading, params);
        }

        /** Returns the text displayed before the description text; an empty string if there is no description,
         * otherwise the result of {@code String.format(descriptionHeading, params)}.
         * @param params the parameters to use to format the description heading
         * @return the formatted description heading */
        public String descriptionHeading(Object... params) {
            return empty(descriptionHeading) ? "" : heading(ansi(), descriptionHeading, params);
        }

        /** Returns the text displayed before the positional parameter list; an empty string if there are no positional
         * parameters, otherwise the result of {@code String.format(parameterListHeading, params)}.
         * @param params the parameters to use to format the parameter list heading
         * @return the formatted parameter list heading */
        public String parameterListHeading(Object... params) {
            return positionalParametersFields.isEmpty() ? "" : heading(ansi(), parameterListHeading, params);
        }

        /** Returns the text displayed before the option list; an empty string if there are no options,
         * otherwise the result of {@code String.format(optionListHeading, params)}.
         * @param params the parameters to use to format the option list heading
         * @return the formatted option list heading */
        public String optionListHeading(Object... params) {
            return optionFields.isEmpty() ? "" : heading(ansi(), optionListHeading, params);
        }

        /** Returns the text displayed before the command list; an empty string if there are no commands,
         * otherwise the result of {@code String.format(commandListHeading, params)}.
         * @param params the parameters to use to format the command list heading
         * @return the formatted command list heading */
        public String commandListHeading(Object... params) {
            return commands.isEmpty() ? "" : heading(ansi(), commandListHeading, params);
        }

        /** Returns the text displayed before the footer text; the result of {@code String.format(footerHeading, params)}.
         * @param params the parameters to use to format the footer heading
         * @return the formatted footer heading */
        public String footerHeading(Object... params) {
            return heading(ansi(), footerHeading, params);
        }
        /** Returns a 2-column list with command names and the first line of their header or (if absent) description.
         * @return a usage help section describing the added commands */
        public String commandList() {
            if (commands.isEmpty()) { return ""; }
            int commandLength = maxLength(commands.keySet());
            Help.TextTable textTable = new Help.TextTable(ansi(),
                    new Help.Column(commandLength + 2, 2, Help.Column.Overflow.SPAN),
                    new Help.Column(usageHelpWidth - (commandLength + 2), 2, Help.Column.Overflow.WRAP));

            for (Map.Entry<String, Help> entry : commands.entrySet()) {
                Help command = entry.getValue();
                String header = command.header != null && command.header.length > 0 ? command.header[0]
                        : (command.description != null && command.description.length > 0 ? command.description[0] : "");
                textTable.addRowValues(colorScheme.commandText(entry.getKey()), ansi().new Text(header));
            }
            return textTable.toString();
        }
        private static int maxLength(Collection<String> any) {
            List<String> strings = new ArrayList<String>(any);
            Collections.sort(strings, Collections.reverseOrder(Help.shortestFirst()));
            return strings.get(0).length();
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
            return new Layout(colorScheme, new TextTable(colorScheme.ansi()), createDefaultOptionRenderer(), createDefaultParameterRenderer());
        }
        /** Returns a new default OptionRenderer which converts {@link Option Options} to five columns of text to match
         *  the default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>the required option marker</li>
         * <li>2-character short option name (or empty string if no short option exists)</li>
         * <li>comma separator (only if both short option and long option exist, empty string otherwise)</li>
         * <li>comma-separated string with long option name(s)</li>
         * <li>first element of the {@link Option#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   Option#description()} array, and these rows look like {@code {"", "", "", "", option.description()[i]}}.</p>
         * <p>If configured, this option renderer adds an additional row to display the default field value.</p>
         * @return a new default OptionRenderer
         */
        public IOptionRenderer createDefaultOptionRenderer() {
            DefaultOptionRenderer result = new DefaultOptionRenderer();
            result.requiredMarker = String.valueOf(requiredOptionMarker);
            if (showDefaultValues != null && showDefaultValues.booleanValue()) {
                result.command = this.command;
            }
            return result;
        }
        /** Returns a new minimal OptionRenderer which converts {@link Option Options} to a single row with two columns
         * of text: an option name and a description. If multiple names or descriptions exist, the first value is used.
         * @return a new minimal OptionRenderer */
        public static IOptionRenderer createMinimalOptionRenderer() {
            return new MinimalOptionRenderer();
        }

        /** Returns a new default ParameterRenderer which converts {@link Parameters Parameters} to four columns of
         * text to match the default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>empty string </li>
         * <li>empty string </li>
         * <li>parameter(s) label as rendered by the {@link IParamLabelRenderer}</li>
         * <li>first element of the {@link Parameters#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   Parameters#description()} array, and these rows look like {@code {"", "", "", param.description()[i]}}.</p>
         * <p>If configured, this parameter renderer adds an additional row to display the default field value.</p>
         * @return a new default ParameterRenderer
         */
        public IParameterRenderer createDefaultParameterRenderer() {
            DefaultParameterRenderer result = new DefaultParameterRenderer();
            result.requiredMarker = String.valueOf(requiredOptionMarker);
            return result;
        }
        /** Returns a new minimal ParameterRenderer which converts {@link Parameters Parameters} to a single row with
         * two columns of text: an option name and a description. If multiple descriptions exist, the first value is used.
         * @return a new minimal ParameterRenderer */
        public static IParameterRenderer createMinimalParameterRenderer() {
            return new MinimalParameterRenderer();
        }

        /** Returns a value renderer that returns the {@code paramLabel} if defined or the field name otherwise.
         * @return a new minimal ParamLabelRenderer */
        public static IParamLabelRenderer createMinimalParamLabelRenderer() {
            return new IParamLabelRenderer() {
                public Text renderParameterLabel(Field field, Ansi ansi, List<IStyle> styles) {
                    String text = DefaultParamLabelRenderer.renderParameterName(field);
                    return ansi.apply(text, styles);
                }
                public String separator() { return ""; }
            };
        }
        /** Returns a new default value renderer that separates option parameters from their {@linkplain Option
         * options} with the specified separator string, surrounds optional parameters with {@code '['} and {@code ']'}
         * characters and uses ellipses ("...") to indicate that any number of a parameter are allowed.
         * @return a new default ParamLabelRenderer
         */
        public IParamLabelRenderer createDefaultParamLabelRenderer() {
            return new DefaultParamLabelRenderer(separator);
        }
        /** Sorts Fields annotated with {@code Option} by their option name in case-insensitive alphabetic order. If an
         * Option has multiple names, the shortest name is used for the sorting. Help options follow non-help options.
         * @return a comparator that sorts fields by their option name in case-insensitive alphabetic order */
        public static Comparator<Field> createShortOptionNameComparator() {
            return new SortByShortestOptionNameAlphabetically();
        }
        /** Sorts Fields annotated with {@code Option} by their option {@linkplain Range#max max arity} first, by
         * {@linkplain Range#min min arity} next, and by {@linkplain #createShortOptionNameComparator() option name} last.
         * @return a comparator that sorts fields by arity first, then their option name */
        public static Comparator<Field> createShortOptionArityAndNameComparator() {
            return new SortByOptionArityAndNameAlphabetically();
        }
        /** Sorts short strings before longer strings.
         * @return a comparators that sorts short strings before longer strings */
        public static Comparator<String> shortestFirst() {
            return new ShortestFirst();
        }

        /** Returns whether ANSI escape codes are enabled or not.
         * @return whether ANSI escape codes are enabled or not
         */
        public Ansi ansi() {
            return colorScheme.ansi;
        }

        /** When customizing online help for {@link Option Option} details, a custom {@code IOptionRenderer} can be
         * used to create textual representation of an Option in a tabular format: one or more rows, each containing
         * one or more columns. The {@link Layout Layout} is responsible for placing these text values in the
         * {@link TextTable TextTable}. */
        public interface IOptionRenderer {
            /**
             * Returns a text representation of the specified Option and the Field that captures the option value.
             * @param option the command line option to show online usage help for
             * @param field the field that will hold the value for the command line option
             * @param parameterLabelRenderer responsible for rendering option parameters to text
             * @param scheme color scheme for applying ansi color styles to options and option parameters
             * @return a 2-dimensional array of text values: one or more rows, each containing one or more columns
             */
            Text[][] render(Option option, Field field, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme);
        }
        /** The DefaultOptionRenderer converts {@link Option Options} to five columns of text to match the default
         * {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>the required option marker (if the option is required)</li>
         * <li>2-character short option name (or empty string if no short option exists)</li>
         * <li>comma separator (only if both short option and long option exist, empty string otherwise)</li>
         * <li>comma-separated string with long option name(s)</li>
         * <li>first element of the {@link Option#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   Option#description()} array, and these rows look like {@code {"", "", "", option.description()[i]}}.</p>
         */
        static class DefaultOptionRenderer implements IOptionRenderer {
            public String requiredMarker = " ";
            public Object command;
            private String sep;
            private boolean showDefault;
            public Text[][] render(Option option, Field field, IParamLabelRenderer paramLabelRenderer, ColorScheme scheme) {
                String[] names = ShortestFirst.sort(option.names());
                int shortOptionCount = names[0].length() == 2 ? 1 : 0;
                String shortOption = shortOptionCount > 0 ? names[0] : "";
                sep = shortOptionCount > 0 && names.length > 1 ? "," : "";

                String longOption = join(names, shortOptionCount, names.length - shortOptionCount, ", ");
                Text longOptionText = createLongOptionText(field, paramLabelRenderer, scheme, longOption);

                showDefault = command != null && !option.help() && !isBoolean(field.getType());
                Object defaultValue = createDefaultValue(field);

                String requiredOption = option.required() ? requiredMarker : "";
                return renderDescriptionLines(option, scheme, requiredOption, shortOption, longOptionText, defaultValue);
            }

            private Object createDefaultValue(Field field) {
                Object defaultValue = null;
                try {
                    defaultValue = field.get(command);
                    if (defaultValue == null) { showDefault = false; } // #201 don't show null default values
                    else if (field.getType().isArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < Array.getLength(defaultValue); i++) {
                            sb.append(i > 0 ? ", " : "").append(Array.get(defaultValue, i));
                        }
                        defaultValue = sb.insert(0, "[").append("]").toString();
                    }
                } catch (Exception ex) {
                    showDefault = false;
                }
                return defaultValue;
            }

            private Text createLongOptionText(Field field, IParamLabelRenderer renderer, ColorScheme scheme, String longOption) {
                Text paramLabelText = renderer.renderParameterLabel(field, scheme.ansi(), scheme.optionParamStyles);

                // if no long option, fill in the space between the short option name and the param label value
                if (paramLabelText.length > 0 && longOption.length() == 0) {
                    sep = renderer.separator();
                    // #181 paramLabelText may be =LABEL or [=LABEL...]
                    int sepStart = paramLabelText.plainString().indexOf(sep);
                    Text prefix = paramLabelText.substring(0, sepStart);
                    paramLabelText = prefix.append(paramLabelText.substring(sepStart + sep.length()));
                }
                Text longOptionText = scheme.optionText(longOption);
                longOptionText = longOptionText.append(paramLabelText);
                return longOptionText;
            }

            private Text[][] renderDescriptionLines(Option option,
                                                    ColorScheme scheme,
                                                    String requiredOption,
                                                    String shortOption,
                                                    Text longOptionText,
                                                    Object defaultValue) {
                Text EMPTY = Ansi.EMPTY_TEXT;
                List<Text[]> result = new ArrayList<Text[]>();
                Text[] descriptionFirstLines = scheme.ansi().new Text(str(option.description(), 0)).splitLines();
                if (descriptionFirstLines.length == 0) {
                    if (showDefault) {
                        descriptionFirstLines = new Text[]{scheme.ansi().new Text("  Default: " + defaultValue)};
                        showDefault = false; // don't show the default value twice
                    } else {
                        descriptionFirstLines = new Text[]{ EMPTY };
                    }
                }
                result.add(new Text[] { scheme.optionText(requiredOption), scheme.optionText(shortOption),
                        scheme.ansi().new Text(sep), longOptionText, descriptionFirstLines[0] });
                for (int i = 1; i < descriptionFirstLines.length; i++) {
                    result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, descriptionFirstLines[i] });
                }
                for (int i = 1; i < option.description().length; i++) {
                    Text[] descriptionNextLines = scheme.ansi().new Text(option.description()[i]).splitLines();
                    for (Text line : descriptionNextLines) {
                        result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, line });
                    }
                }
                if (showDefault) {
                    result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, scheme.ansi().new Text("  Default: " + defaultValue) });
                }
                return result.toArray(new Text[result.size()][]);
            }
        }
        /** The MinimalOptionRenderer converts {@link Option Options} to a single row with two columns of text: an
         * option name and a description. If multiple names or description lines exist, the first value is used. */
        static class MinimalOptionRenderer implements IOptionRenderer {
            public Text[][] render(Option option, Field field, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme) {
                Text optionText = scheme.optionText(option.names()[0]);
                Text paramLabelText = parameterLabelRenderer.renderParameterLabel(field, scheme.ansi(), scheme.optionParamStyles);
                optionText = optionText.append(paramLabelText);
                return new Text[][] {{ optionText,
                                        scheme.ansi().new Text(option.description().length == 0 ? "" : option.description()[0]) }};
            }
        }
        /** The MinimalParameterRenderer converts {@link Parameters Parameters} to a single row with two columns of
         * text: the parameters label and a description. If multiple description lines exist, the first value is used. */
        static class MinimalParameterRenderer implements IParameterRenderer {
            public Text[][] render(Parameters param, Field field, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme) {
                return new Text[][] {{ parameterLabelRenderer.renderParameterLabel(field, scheme.ansi(), scheme.parameterStyles),
                        scheme.ansi().new Text(param.description().length == 0 ? "" : param.description()[0]) }};
            }
        }
        /** When customizing online help for {@link Parameters Parameters} details, a custom {@code IParameterRenderer}
         * can be used to create textual representation of a Parameters field in a tabular format: one or more rows,
         * each containing one or more columns. The {@link Layout Layout} is responsible for placing these text
         * values in the {@link TextTable TextTable}. */
        public interface IParameterRenderer {
            /**
             * Returns a text representation of the specified Parameters and the Field that captures the parameter values.
             * @param parameters the command line parameters to show online usage help for
             * @param field the field that will hold the value for the command line parameters
             * @param parameterLabelRenderer responsible for rendering parameter labels to text
             * @param scheme color scheme for applying ansi color styles to positional parameters
             * @return a 2-dimensional array of text values: one or more rows, each containing one or more columns
             */
            Text[][] render(Parameters parameters, Field field, IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme);
        }
        /** The DefaultParameterRenderer converts {@link Parameters Parameters} to five columns of text to match the
         * default {@linkplain TextTable TextTable} column layout. The first row of values looks like this:
         * <ol>
         * <li>the required option marker (if the parameter's arity is to have at least one value)</li>
         * <li>empty string </li>
         * <li>empty string </li>
         * <li>parameter(s) label as rendered by the {@link IParamLabelRenderer}</li>
         * <li>first element of the {@link Parameters#description()} array</li>
         * </ol>
         * <p>Following this, there will be one row for each of the remaining elements of the {@link
         *   Parameters#description()} array, and these rows look like {@code {"", "", "", param.description()[i]}}.</p>
         */
        static class DefaultParameterRenderer implements IParameterRenderer {
            public String requiredMarker = " ";
            public Text[][] render(Parameters params, Field field, IParamLabelRenderer paramLabelRenderer, ColorScheme scheme) {
                Text label = paramLabelRenderer.renderParameterLabel(field, scheme.ansi(), scheme.parameterStyles);
                Text requiredParameter = scheme.parameterText(Range.parameterArity(field).min > 0 ? requiredMarker : "");

                Text EMPTY = Ansi.EMPTY_TEXT;
                List<Text[]> result = new ArrayList<Text[]>();
                Text[] descriptionFirstLines = scheme.ansi().new Text(str(params.description(), 0)).splitLines();
                if (descriptionFirstLines.length == 0) { descriptionFirstLines = new Text[]{ EMPTY }; }
                result.add(new Text[] { requiredParameter, EMPTY, EMPTY, label, descriptionFirstLines[0] });
                for (int i = 1; i < descriptionFirstLines.length; i++) {
                    result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, descriptionFirstLines[i] });
                }
                for (int i = 1; i < params.description().length; i++) {
                    Text[] descriptionNextLines = scheme.ansi().new Text(params.description()[i]).splitLines();
                    for (Text line : descriptionNextLines) {
                        result.add(new Text[] { EMPTY, EMPTY, EMPTY, EMPTY, line });
                    }
                }
                return result.toArray(new Text[result.size()][]);
            }
        }
        /** When customizing online usage help for an option parameter or a positional parameter, a custom
         * {@code IParamLabelRenderer} can be used to render the parameter name or label to a String. */
        public interface IParamLabelRenderer {

            /** Returns a text rendering of the Option parameter or positional parameter; returns an empty string
             * {@code ""} if the option is a boolean and does not take a parameter.
             * @param field the annotated field with a parameter label
             * @param ansi determines whether ANSI escape codes should be emitted or not
             * @param styles the styles to apply to the parameter label
             * @return a text rendering of the Option parameter or positional parameter */
            Text renderParameterLabel(Field field, Ansi ansi, List<IStyle> styles);

            /** Returns the separator between option name and param label.
             * @return the separator between option name and param label */
            String separator();
        }
        /**
         * DefaultParamLabelRenderer separates option parameters from their {@linkplain Option options} with a
         * {@linkplain DefaultParamLabelRenderer#separator separator} string, surrounds optional values
         * with {@code '['} and {@code ']'} characters and uses ellipses ("...") to indicate that any number of
         * values is allowed for options or parameters with variable arity.
         */
        static class DefaultParamLabelRenderer implements IParamLabelRenderer {
            /** The string to use to separate option parameters from their options. */
            public final String separator;
            /** Constructs a new DefaultParamLabelRenderer with the specified separator string. */
            public DefaultParamLabelRenderer(String separator) {
                this.separator = Assert.notNull(separator, "separator");
            }
            public String separator() { return separator; }
            public Text renderParameterLabel(Field field, Ansi ansi, List<IStyle> styles) {
                boolean isOptionParameter = field.isAnnotationPresent(Option.class);
                Range arity = isOptionParameter ? Range.optionArity(field) : Range.parameterCapacity(field);
                String split = isOptionParameter ? field.getAnnotation(Option.class).split() : field.getAnnotation(Parameters.class).split();
                Text result = ansi.new Text("");
                String sep = isOptionParameter ? separator : "";
                Text paramName = ansi.apply(renderParameterName(field), styles);
                if (!empty(split)) { paramName = paramName.append("[" + split).append(paramName).append("]..."); } // #194
                for (int i = 0; i < arity.min; i++) {
                    result = result.append(sep).append(paramName);
                    sep = " ";
                }
                if (arity.isVariable) {
                    if (result.length == 0) { // arity="*" or arity="0..*"
                        result = result.append(sep + "[").append(paramName).append("]...");
                    } else if (!result.plainString().endsWith("...")) { // split param may already end with "..."
                        result = result.append("...");
                    }
                } else {
                    sep = result.length == 0 ? (isOptionParameter ? separator : "") : " ";
                    for (int i = arity.min; i < arity.max; i++) {
                        if (sep.trim().length() == 0) {
                            result = result.append(sep + "[").append(paramName);
                        } else {
                            result = result.append("[" + sep).append(paramName);
                        }
                        sep  = " ";
                    }
                    for (int i = arity.min; i < arity.max; i++) { result = result.append("]"); }
                }
                return result;
            }
            private static String renderParameterName(Field field) {
                String result = null;
                if (field.isAnnotationPresent(Option.class)) {
                    result = field.getAnnotation(Option.class).paramLabel();
                } else if (field.isAnnotationPresent(Parameters.class)) {
                    result = field.getAnnotation(Parameters.class).paramLabel();
                }
                if (result != null && result.trim().length() > 0) {
                    return result.trim();
                }
                String name = field.getName();
                if (Map.class.isAssignableFrom(field.getType())) { // #195 better param labels for map fields
                    Class<?>[] paramTypes = getTypeAttribute(field);
                    if (paramTypes.length < 2 || paramTypes[0] == null || paramTypes[1] == null) {
                        name = "String=String";
                    } else { name = paramTypes[0].getSimpleName() + "=" + paramTypes[1].getSimpleName(); }
                }
                return "<" + name + ">";
            }
        }
        /** Use a Layout to format usage help text for options and parameters in tabular format.
         * <p>Delegates to the renderers to create {@link Text} values for the annotated fields, and uses a
         * {@link TextTable} to display these values in tabular format. Layout is responsible for deciding which values
         * to display where in the table. By default, Layout shows one option or parameter per table row.</p>
         * <p>Customize by overriding the {@link #layout(Field, CommandLine.Help.Ansi.Text[][])} method.</p>
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
            public Layout(ColorScheme colorScheme) { this(colorScheme, new TextTable(colorScheme.ansi())); }

            /** Constructs a Layout with the specified color scheme, the specified TextTable, the
             * {@linkplain Help#createDefaultOptionRenderer() default option renderer}, and the
             * {@linkplain Help#createDefaultParameterRenderer() default parameter renderer}.
             * @param colorScheme the color scheme to use for common, auto-generated parts of the usage help message
             * @param textTable the TextTable to lay out parts of the usage help message in tabular format */
            public Layout(ColorScheme colorScheme, TextTable textTable) {
                this(colorScheme, textTable, new DefaultOptionRenderer(), new DefaultParameterRenderer());
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
             * @param field the field annotated with the specified Option or Parameters
             * @param cellValues the text values representing the Option/Parameters, to be displayed in tabular form
             */
            public void layout(Field field, Text[][] cellValues) {
                for (Text[] oneRow : cellValues) {
                    table.addRowValues(oneRow);
                }
            }
            /** Calls {@link #addOption(Field, CommandLine.Help.IParamLabelRenderer)} for all non-hidden Options in the list.
             * @param fields fields annotated with {@link Option} to add usage descriptions for
             * @param paramLabelRenderer object that knows how to render option parameters */
            public void addOptions(List<Field> fields, IParamLabelRenderer paramLabelRenderer) {
                for (Field field : fields) {
                    Option option = field.getAnnotation(Option.class);
                    if (!option.hidden()) {
                        addOption(field, paramLabelRenderer);
                    }
                }
            }
            /**
             * Delegates to the {@link #optionRenderer option renderer} of this layout to obtain
             * text values for the specified {@link Option}, and then calls the {@link #layout(Field, CommandLine.Help.Ansi.Text[][])}
             * method to write these text values into the correct cells in the TextTable.
             * @param field the field annotated with the specified Option
             * @param paramLabelRenderer knows how to render option parameters
             */
            public void addOption(Field field, IParamLabelRenderer paramLabelRenderer) {
                Option option = field.getAnnotation(Option.class);
                Text[][] values = optionRenderer.render(option, field, paramLabelRenderer, colorScheme);
                layout(field, values);
            }
            /** Calls {@link #addPositionalParameter(Field, CommandLine.Help.IParamLabelRenderer)} for all non-hidden Parameters in the list.
             * @param fields fields annotated with {@link Parameters} to add usage descriptions for
             * @param paramLabelRenderer knows how to render option parameters */
            public void addPositionalParameters(List<Field> fields, IParamLabelRenderer paramLabelRenderer) {
                for (Field field : fields) {
                    Parameters parameters = field.getAnnotation(Parameters.class);
                    if (!parameters.hidden()) {
                        addPositionalParameter(field, paramLabelRenderer);
                    }
                }
            }
            /**
             * Delegates to the {@link #parameterRenderer parameter renderer} of this layout
             * to obtain text values for the specified {@link Parameters}, and then calls
             * {@link #layout(Field, CommandLine.Help.Ansi.Text[][])} to write these text values into the correct cells in the TextTable.
             * @param field the field annotated with the specified Parameters
             * @param paramLabelRenderer knows how to render option parameters
             */
            public void addPositionalParameter(Field field, IParamLabelRenderer paramLabelRenderer) {
                Parameters option = field.getAnnotation(Parameters.class);
                Text[][] values = parameterRenderer.render(option, field, paramLabelRenderer, colorScheme);
                layout(field, values);
            }
            /** Returns the section of the usage help message accumulated in the TextTable owned by this layout. */
            @Override public String toString() {
                return table.toString();
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
        static class SortByShortestOptionNameAlphabetically implements Comparator<Field> {
            public int compare(Field f1, Field f2) {
                Option o1 = f1.getAnnotation(Option.class);
                Option o2 = f2.getAnnotation(Option.class);
                if (o1 == null) { return 1; } else if (o2 == null) { return -1; } // options before params
                String[] names1 = ShortestFirst.sort(o1.names());
                String[] names2 = ShortestFirst.sort(o2.names());
                int result = names1[0].toUpperCase().compareTo(names2[0].toUpperCase()); // case insensitive sort
                result = result == 0 ? -names1[0].compareTo(names2[0]) : result; // lower case before upper case
                return o1.help() == o2.help() ? result : o2.help() ? -1 : 1; // help options come last
            }
        }
        /** Sorts {@code Option} instances by their max arity first, then their min arity, then delegates to super class. */
        static class SortByOptionArityAndNameAlphabetically extends SortByShortestOptionNameAlphabetically {
            public int compare(Field f1, Field f2) {
                Option o1 = f1.getAnnotation(Option.class);
                Option o2 = f2.getAnnotation(Option.class);
                Range arity1 = Range.optionArity(f1);
                Range arity2 = Range.optionArity(f2);
                int result = arity1.max - arity2.max;
                if (result == 0) {
                    result = arity1.min - arity2.min;
                }
                if (result == 0) { // arity is same
                    if (isMultiValue(f1) && !isMultiValue(f2)) { result = 1; } // f1 > f2
                    if (!isMultiValue(f1) && isMultiValue(f2)) { result = -1; } // f1 < f2
                }
                return result == 0 ? super.compare(f1, f2) : result;
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

            /** The column definitions of this table. */
            public final Column[] columns;

            /** The {@code char[]} slots of the {@code TextTable} to copy text values into. */
            protected final List<Text> columnValues = new ArrayList<Text>();

            /** By default, indent wrapped lines by 2 spaces. */
            public int indentWrappedLines = 2;

            private final Ansi ansi;

            /** Constructs a TextTable with five columns as follows:
             * <ol>
             * <li>required option/parameter marker (width: 2, indent: 0, TRUNCATE on overflow)</li>
             * <li>short option name (width: 2, indent: 0, TRUNCATE on overflow)</li>
             * <li>comma separator (width: 1, indent: 0, TRUNCATE on overflow)</li>
             * <li>long option name(s) (width: 24, indent: 1, SPAN multiple columns on overflow)</li>
             * <li>description line(s) (width: 51, indent: 1, WRAP to next row on overflow)</li>
             * </ol>
             * @param ansi whether to emit ANSI escape codes or not
             */
            public TextTable(Ansi ansi) {
                // "* -c, --create                Creates a ...."
                this(ansi, new Column[] {
                            new Column(2,                                        0, TRUNCATE), // "*"
                            new Column(2,                                        0, TRUNCATE), // "-c"
                            new Column(1,                                        0, TRUNCATE), // ","
                            new Column(optionsColumnWidth - 2 - 2 - 1       , 1, SPAN),  // " --create"
                            new Column(usageHelpWidth - optionsColumnWidth, 1, WRAP) // " Creates a ..."
                    });
            }

            /** Constructs a new TextTable with columns with the specified width, all SPANning  multiple columns on
             * overflow except the last column which WRAPS to the next row.
             * @param ansi whether to emit ANSI escape codes or not
             * @param columnWidths the width of the table columns (all columns have zero indent)
             */
            public TextTable(Ansi ansi, int... columnWidths) {
                this.ansi = Assert.notNull(ansi, "ansi");
                columns = new Column[columnWidths.length];
                for (int i = 0; i < columnWidths.length; i++) {
                    columns[i] = new Column(columnWidths[i], 0, i == columnWidths.length - 1 ? SPAN: WRAP);
                }
            }
            /** Constructs a {@code TextTable} with the specified columns.
             * @param ansi whether to emit ANSI escape codes or not
             * @param columns columns to construct this TextTable with */
            public TextTable(Ansi ansi, Column... columns) {
                this.ansi = Assert.notNull(ansi, "ansi");
                this.columns = Assert.notNull(columns, "columns");
                if (columns.length == 0) { throw new IllegalArgumentException("At least one column is required"); }
            }
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
            public Text cellAt(int row, int col) { return textAt(row, col); }

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
                for (int col = 0; col < values.length; col++) {
                    int row = rowCount() - 1;// write to last row: previous value may have wrapped to next row
                    Cell cell = putValue(row, col, values[col]);

                    // add row if a value spanned/wrapped and there are still remaining values
                    if ((cell.row != row || cell.column != col) && col != values.length - 1) {
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
            private static int length(Text str) {
                return str.length; // TODO count some characters as double length
            }

            private int copy(BreakIterator line, Text text, Text columnValue, int offset) {
                // Deceive the BreakIterator to ensure no line breaks after '-' character
                line.setText(text.plainString().replace("-", "\u00ff"));
                int done = 0;
                for (int start = line.first(), end = line.next(); end != BreakIterator.DONE; start = end, end = line.next()) {
                    Text word = text.substring(start, end); //.replace("\u00ff", "-"); // not needed
                    if (columnValue.maxLength >= offset + done + length(word)) {
                        done += copy(word, columnValue, offset + done); // TODO localized length
                    } else {
                        break;
                    }
                }
                if (done == 0 && length(text) > columnValue.maxLength) {
                    // The value is a single word that is too big to be written to the column. Write as much as we can.
                    done = copy(text, columnValue, offset);
                }
                return done;
            }
            private static int copy(Text value, Text destination, int offset) {
                int length = Math.min(value.length, destination.maxLength - offset);
                value.getStyledChars(value.from, length, destination, offset);
                return length;
            }

            /** Copies the text representation that we built up from the options into the specified StringBuilder.
             * @param text the StringBuilder to write into
             * @return the specified StringBuilder object (to allow method chaining and a more fluid API) */
            public StringBuilder toString(StringBuilder text) {
                int columnCount = this.columns.length;
                StringBuilder row = new StringBuilder(usageHelpWidth);
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
                //if (Ansi.enabled()) { text.append(Style.reset.off()); }
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

        /** All usage help message are generated with a color scheme that assigns certain styles and colors to common
         * parts of a usage message: the command name, options, positional parameters and option parameters.
         * Users may customize these styles by creating Help with a custom color scheme.
         * <p>Note that these options and styles may not be rendered if ANSI escape codes are not
         * {@linkplain Ansi#enabled() enabled}.</p>
         * @see Help#defaultColorScheme(Ansi)
         */
        public static class ColorScheme {
            public final List<IStyle> commandStyles = new ArrayList<IStyle>();
            public final List<IStyle> optionStyles = new ArrayList<IStyle>();
            public final List<IStyle> parameterStyles = new ArrayList<IStyle>();
            public final List<IStyle> optionParamStyles = new ArrayList<IStyle>();
            private final Ansi ansi;

            /** Constructs a new ColorScheme with {@link Help.Ansi#AUTO}. */
            public ColorScheme() { this(Ansi.AUTO); }

            /** Constructs a new ColorScheme with the specified Ansi enabled mode.
             * @param ansi whether to emit ANSI escape codes or not
             */
            public ColorScheme(Ansi ansi) {this.ansi = Assert.notNull(ansi, "ansi"); }

            /** Adds the specified styles to the registered styles for commands in this color scheme and returns this color scheme.
             * @param styles the styles to add to the registered styles for commands in this color scheme
             * @return this color scheme to enable method chaining for a more fluent API */
            public ColorScheme commands(IStyle... styles)     { return addAll(commandStyles, styles); }
            /** Adds the specified styles to the registered styles for options in this color scheme and returns this color scheme.
             * @param styles the styles to add to registered the styles for options in this color scheme
             * @return this color scheme to enable method chaining for a more fluent API */
            public ColorScheme options(IStyle... styles)      { return addAll(optionStyles, styles);}
            /** Adds the specified styles to the registered styles for positional parameters in this color scheme and returns this color scheme.
             * @param styles the styles to add to registered the styles for parameters in this color scheme
             * @return this color scheme to enable method chaining for a more fluent API */
            public ColorScheme parameters(IStyle... styles)   { return addAll(parameterStyles, styles);}
            /** Adds the specified styles to the registered styles for option parameters in this color scheme and returns this color scheme.
             * @param styles the styles to add to the registered styles for option parameters in this color scheme
             * @return this color scheme to enable method chaining for a more fluent API */
            public ColorScheme optionParams(IStyle... styles) { return addAll(optionParamStyles, styles);}
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

            /** Replaces colors and styles in this scheme with ones specified in system properties, and returns this scheme.
             * Supported property names:<ul>
             *     <li>{@code picocli.color.commands}</li>
             *     <li>{@code picocli.color.options}</li>
             *     <li>{@code picocli.color.parameters}</li>
             *     <li>{@code picocli.color.optionParams}</li>
             * </ul><p>Property values can be anything that {@link Help.Ansi.Style#parse(String)} can handle.</p>
             * @return this ColorScheme
             */
            public ColorScheme applySystemProperties() {
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
            private ColorScheme addAll(List<IStyle> styles, IStyle... add) {
                styles.addAll(Arrays.asList(add));
                return this;
            }

            public Ansi ansi() {
                return ansi;
            }
        }

        /** Creates and returns a new {@link ColorScheme} initialized with picocli default values: commands are bold,
         *  options and parameters use a yellow foreground, and option parameters use italic.
         * @param ansi whether the usage help message should contain ANSI escape codes or not
         * @return a new default color scheme
         */
        public static ColorScheme defaultColorScheme(Ansi ansi) {
            return new ColorScheme(ansi)
                    .commands(Style.bold)
                    .options(Style.fg_yellow)
                    .parameters(Style.fg_yellow)
                    .optionParams(Style.italic);
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
            static final boolean isWindows  = System.getProperty("os.name").startsWith("Windows");
            static final boolean isXterm    = System.getenv("TERM") != null && System.getenv("TERM").startsWith("xterm");
            static final boolean ISATTY = calcTTY();

            // http://stackoverflow.com/questions/1403772/how-can-i-check-if-a-java-programs-input-output-streams-are-connected-to-a-term
            static final boolean calcTTY() {
                if (isWindows && isXterm) { return true; } // Cygwin uses pseudo-tty and console is always null...
                try { return System.class.getDeclaredMethod("console").invoke(null) != null; }
                catch (Throwable reflectionFailed) { return true; }
            }
            private static boolean ansiPossible() { return ISATTY && (!isWindows || isXterm); }

            /** Returns {@code true} if ANSI escape codes should be emitted, {@code false} otherwise.
             * @return ON: {@code true}, OFF: {@code false}, AUTO: if system property {@code "picocli.ansi"} is
             *      defined then return its boolean value, otherwise return whether the platform supports ANSI escape codes */
            public boolean enabled() {
                if (this == ON)  { return true; }
                if (this == OFF) { return false; }
                return (System.getProperty("picocli.ansi") == null ? ansiPossible() : Boolean.getBoolean("picocli.ansi"));
            }

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
                public Object clone() {
                    try { return super.clone(); } catch (CloneNotSupportedException e) { throw new IllegalStateException(e); }
                }

                public Text[] splitLines() {
                    List<Text> result = new ArrayList<Text>();
                    boolean trailingEmptyString = false;
                    int start = 0, end = 0;
                    for (int i = 0; i < plain.length(); i++, end = i) {
                        char c = plain.charAt(i);
                        boolean eol = c == '\n';
                        eol |= (c == '\r' && i + 1 < plain.length() && plain.charAt(i + 1) == '\n' && ++i > 0); // \r\n
                        eol |= c == '\r';
                        if (eol) {
                            result.add(this.substring(start, end));
                            trailingEmptyString = i == plain.length() - 1;
                            start = i + 1;
                        }
                    }
                    if (start < plain.length() || trailingEmptyString) {
                        result.add(this.substring(start, plain.length()));
                    }
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
                /** Returns a new {@code Text} instance with the specified text appended. Does not modify this instance!
                 * @param string the text to append
                 * @return a new Text instance */
                public Text append(String string) {
                    return append(new Text(string));
                }

                /** Returns a new {@code Text} instance with the specified text appended. Does not modify this instance!
                 * @param other the text to append
                 * @return a new Text instance */
                public Text append(Text other) {
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
    private static class Tracer {
        TraceLevel level = TraceLevel.lookup(System.getProperty("picocli.trace"));
        PrintStream stream = System.err;
        void warn (String msg, Object... params) { TraceLevel.WARN.print(this, msg, params); }
        void info (String msg, Object... params) { TraceLevel.INFO.print(this, msg, params); }
        void debug(String msg, Object... params) { TraceLevel.DEBUG.print(this, msg, params); }
        boolean isWarn()  { return level.isEnabled(TraceLevel.WARN); }
        boolean isInfo()  { return level.isEnabled(TraceLevel.INFO); }
        boolean isDebug() { return level.isEnabled(TraceLevel.DEBUG); }
    }
    /** Base class of all exceptions thrown by {@code picocli.CommandLine}.
     * @since 2.0 */
    public static class PicocliException extends RuntimeException {
        private static final long serialVersionUID = -2574128880125050818L;
        public PicocliException(String msg) { super(msg); }
        public PicocliException(String msg, Exception ex) { super(msg, ex); }
    }
    /** Exception indicating a problem during {@code CommandLine} initialization.
     * @since 2.0 */
    public static class InitializationException extends PicocliException {
        private static final long serialVersionUID = 8423014001666638895L;
        public InitializationException(String msg) { super(msg); }
        public InitializationException(String msg, Exception ex) { super(msg, ex); }
    }
    /** Exception indicating a problem while invoking a command or subcommand.
     * @since 2.0 */
    public static class ExecutionException extends PicocliException {
        private static final long serialVersionUID = 7764539594267007998L;
        private final CommandLine commandLine;
        public ExecutionException(CommandLine commandLine, String msg) {
            super(msg);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }
        public ExecutionException(CommandLine commandLine, String msg, Exception ex) {
            super(msg, ex);
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
         * @param ex the exception that caused this ParameterException
         * @since 2.0 */
        public ParameterException(CommandLine commandLine, String msg, Exception ex) {
            super(msg, ex);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        /** Returns the {@code CommandLine} object for the (sub)command whose input could not be parsed.
         * @return the {@code CommandLine} object for the (sub)command where parsing failed.
         * @since 2.0
         */
        public CommandLine getCommandLine() { return commandLine; }

        private static ParameterException create(CommandLine cmd, Exception ex, String arg, int i, String[] args) {
            String msg = ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage()
                    + " while processing argument at or before arg[" + i + "] '" + arg + "' in " + Arrays.toString(args) + ": " + ex.toString();
            return new ParameterException(cmd, msg, ex);
        }
    }
    /**
     * Exception indicating that a required parameter was not specified.
     */
    public static class MissingParameterException extends ParameterException {
        private static final long serialVersionUID = 5075678535706338753L;
        public MissingParameterException(CommandLine commandLine, String msg) {
            super(commandLine, msg);
        }

        private static MissingParameterException create(CommandLine cmd, Collection<Field> missing, String separator) {
            if (missing.size() == 1) {
                return new MissingParameterException(cmd, "Missing required option '"
                        + describe(missing.iterator().next(), separator) + "'");
            }
            List<String> names = new ArrayList<String>(missing.size());
            for (Field field : missing) {
                names.add(describe(field, separator));
            }
            return new MissingParameterException(cmd, "Missing required options " + names.toString());
        }
        private static String describe(Field field, String separator) {
            String prefix = (field.isAnnotationPresent(Option.class))
                ? field.getAnnotation(Option.class).names()[0] + separator
                : "params[" + field.getAnnotation(Parameters.class).index() + "]" + separator;
            return prefix + Help.DefaultParamLabelRenderer.renderParameterName(field);
        }
    }

    /**
     * Exception indicating that multiple fields have been annotated with the same Option name.
     */
    public static class DuplicateOptionAnnotationsException extends InitializationException {
        private static final long serialVersionUID = -3355128012575075641L;
        public DuplicateOptionAnnotationsException(String msg) { super(msg); }

        private static DuplicateOptionAnnotationsException create(String name, Field field1, Field field2) {
            return new DuplicateOptionAnnotationsException("Option name '" + name + "' is used by both " +
                    field1.getDeclaringClass().getName() + "." + field1.getName() + " and " +
                    field2.getDeclaringClass().getName() + "." + field2.getName());
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
        public UnmatchedArgumentException(CommandLine commandLine, String msg) { super(commandLine, msg); }
        public UnmatchedArgumentException(CommandLine commandLine, Stack<String> args) { this(commandLine, new ArrayList<String>(reverse(args))); }
        public UnmatchedArgumentException(CommandLine commandLine, List<String> args) { this(commandLine, "Unmatched argument" + (args.size() == 1 ? " " : "s ") + args); }
    }
    /** Exception indicating that more values were specified for an option or parameter than its {@link Option#arity() arity} allows. */
    public static class MaxValuesforFieldExceededException extends ParameterException {
        private static final long serialVersionUID = 6536145439570100641L;
        public MaxValuesforFieldExceededException(CommandLine commandLine, String msg) { super(commandLine, msg); }
    }
    /** Exception indicating that an option for a single-value option field has been specified multiple times on the command line. */
    public static class OverwrittenOptionException extends ParameterException {
        private static final long serialVersionUID = 1338029208271055776L;
        public OverwrittenOptionException(CommandLine commandLine, String msg) { super(commandLine, msg); }
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
