package picocli.groovy;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>
 * Base script class that provides picocli declarative (annotation-based) command line argument processing for Groovy scripts.
 * </p><p>
 * Scripts may install this base script via the {@link PicocliScript2} annotation or via the standard Groovy
 * {@code @groovy.transform.BaseScript(picocli.groovy.PicocliBaseScript2)} annotation, but
 * the {@code @PicocliScript2} annotation is preferred since it enables scripts to use the
 * {@code @Command} annotation. Example usage:
 * </p>
 * <pre>
 * &#64;Command(name = "myCommand", description = "does something special")
 * &#64;PicocliScript2
 * import picocli.groovy.PicocliScript2
 * import picocli.CommandLine.Command
 * ...
 * </pre>
 * <p>
 * Before the script body is executed,
 * the {@code PicocliBaseScript2} base class parses the command line and initializes {@code @Field} variables
 * annotated with {@code @Option} or {@code @Parameters}.
 * It also takes care of error handling and common use cases like requests for usage help.
 * </p><p>
 * See the {@link #run()} method for a detailed break-down of the steps the base class takes
 * before the statements in the script body are executed.
 * </p><p>
 * This class has the following improvements over {@link PicocliBaseScript}:
 * </p>
 * <ul>
 *   <li>Adds support for {@code @Command}-annotated methods to define subcommands in scripts.</li>
 *   <li>Adds support for {@code help} subcommands (both the built-in {@code CommandLine.HelpCommand} and custom implementations).</li>
 *   <li>Adds support for exit codes. The return value of the script becomes the exit code.</li>
 *   <li>Consistency with Java picocli. This implementation delegates to the {@code CommandLine} class,
 *     while {@code PicocliBaseScript} re-implemented features, and this re-implementation got out of sync over time.</li>
 * </ul>
 *
 * @author Remko Popma
 * @since 4.6
 */
public abstract class PicocliBaseScript2 extends Script implements Callable<Object> {
    /**
     * Name of the property that holds the CommandLine instance for this script ({@value}).
     */
    public final static String COMMAND_LINE = "commandLine";

    /** The exit code resulting from executing this script. */
    int exitCode;

    /** Any exception caught while executing this script. */
    Exception exception;

    /**
     * The script body.
     * @return The result of the script evaluation.
     */
    public abstract Object call();

    /**
     * <p>
     * Parses the command line and initializes {@code @Field} variables
     * annotated with {@code @Option} or {@code @Parameters} before executing the script body.
     * Also takes care of error handling and common use cases like requests for usage help.
     * </p>
     * <h1>Execution</h1>
     * <p>
     * Here is a break-down of the steps the base class takes before the statements in the script body are executed:
     * </p>
     * <ol>
     *   <li>A new {@link CommandLine} is created with this script instance as the annotated command object.
     *     The {@code CommandLine} instance is cached in the {@code commandLine} property
     *     (so it can be referred to in script code with
     *     {@code this.commandLine}). {@code CommandLine} creation and initialization may be
     *     customized by overriding {@link #createCommandLine()} or {@link #customize(CommandLine)}.</li>
     *   <li>The {@link CommandLine#execute(String...)} method is called with the script arguments.
     *     This initialises all {@code @Field} variables annotated with {@link CommandLine.Option} or
     *     {@link CommandLine.Parameters}, unless the user input was invalid.</li>
     *   <li>If the user input was invalid, the command line arguments are printed to standard err, followed by,
     *     an error message and the usage message.
     *     Then, the script exits.
     *     This may be customized by overriding {@link #customize(CommandLine)} and setting a custom
     *     {@link IParameterExceptionHandler} on the {@code CommandLine} instance.</li>
     *   <li>Otherwise, if the user input requested version help or usage help, the version string or usage help message is
     *     printed to standard err and the script exits.</li>
     *   <li>The script may support subcommands. In that case only the last specified subcommand is invoked.
     *     This may be customized by overriding
     *     {@link #customize(CommandLine)} and setting a custom
     *     {@link picocli.CommandLine.IExecutionStrategy} on the {@code CommandLine} instance.</li>
     *   <li>If no subcommand was specified, the script body is executed.</li>
     *   <li>If an exception occurs during execution, this exception is rethrown.</li>
     * </ol>
     * <h1>Exit Code</h1>
     * <p>
     *     Scripts that want to control the exit code need to override the {@link #run()} method and call {@code System.exit}.
     *     For example:
     * </p><pre>{@code
     * @Override
     * public Object run() {
     *     try {
     *         super.run();
     *     } finally {
     *         System.exit(exitCode);
     *     }
     * }
     * }</pre>
     * @return The result of the script evaluation.
     */
    @Override
    public Object run() {
        String[] args = getScriptArguments();
        CommandLine commandLine = getOrCreateCommandLine();
        exitCode = commandLine.execute(args);
        if (exception != null) {
            throw new GroovyRuntimeException(exception);
        }

        List<Object> result = new ArrayList<Object>();
        List<CommandLine> commandLines = commandLine.getParseResult().asCommandLineList();
        for (CommandLine cl : commandLines) {
            if (cl.getExecutionResult() != null || !result.isEmpty()) {
                result.add(cl.getExecutionResult());
            }
        }
        return result.isEmpty() ? null : result.size() == 1 ? result.get(0) : result;
    }

    /**
     * Return the script arguments as an array of strings.
     * The default implementation is to get the "args" property.
     *
     * @return the script arguments as an array of strings.
     */
    public String[] getScriptArguments() {
        return (String[]) getProperty("args");
    }

    /**
     * Return the CommandLine for this script.
     * If there isn't one already, then create it using {@link #createCommandLine()}.
     *
     * @return the CommandLine for this script.
     */
    protected CommandLine getOrCreateCommandLine() {
        try {
            CommandLine commandLine = (CommandLine) getProperty(COMMAND_LINE);
            if (commandLine == null) {
                commandLine = createCommandLine();
                // The script has a real property (a field or getter) but if we let Script.setProperty handle
                // this then it just gets stuffed into a binding that shadows the property.
                // This is somewhat related to other bugged behavior in Script wrt properties and bindings.
                // See http://jira.codehaus.org/browse/GROOVY-6582 for example.
                // The correct behavior for Script.setProperty would be to check whether
                // the property has a setter before creating a new script binding.
                this.getMetaClass().setProperty(this, COMMAND_LINE, commandLine);
            }
            return customize(commandLine);
        } catch (MissingPropertyException mpe) {
            CommandLine commandLine = createCommandLine();
            // Since no property or binding already exists, we can use plain old setProperty here.
            setProperty(COMMAND_LINE, commandLine);
            return customize(commandLine);
        }
    }

    protected CommandLine customize(final CommandLine customizable) {
        final IParameterExceptionHandler original = customizable.getParameterExceptionHandler();
        customizable.setParameterExceptionHandler(new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                customizable.getErr().printf("args: %s%n", Arrays.toString(args));
                return original.handleParseException(ex, args);
            }
        });
        customizable.setExecutionExceptionHandler(new IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
                exception = ex; // any exception will be rethrown from run()
                return CommandLine.ExitCode.SOFTWARE;
            }
        });
        return customizable;
    }

    /**
     * Create and returns a new CommandLine instance.
     * This method sets the command name in the usage help message to the script's class simple name (unless
     * annotated with some other command name with the {@code @Command(name = "...")} annotation).
     * <p>
     * Subclasses may override to register custom type converters or programmatically add subcommands.
     * </p>
     *
     * @return A CommandLine instance.
     */
    public CommandLine createCommandLine() {
        CommandLine commandLine = new CommandLine(this);
        if (commandLine.getCommandName().equals("<main class>")) { // only if user did not specify @Command(name) attribute
            commandLine.setCommandName(this.getClass().getSimpleName());
        }
        return commandLine;
    }

}
