package picocli.groovy;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>
 * Base script class that provides picocli declarative (annotation-based) command line argument
 * processing for Groovy scripts, updated for picocli version 4 and greater.
 * </p><p>
 * Scripts may install this base script via the {@link PicocliScript2} annotation or via the standard Groovy
 * {@code @groovy.transform.BaseScript(picocli.groovy.PicocliBaseScript2)} annotation, but
 * the {@code @PicocliScript2} annotation is preferred since it enables scripts to use the
 * {@code @Command} annotation.
 * </p>
 * <h1>Example usage</h1>
 * <pre>
 * &#64;Command(name = "greet", description = "Says hello.", mixinStandardHelpOptions = true, version = "greet 0.1")
 * &#64;PicocliScript2
 * import picocli.groovy.PicocliScript2
 * import picocli.CommandLine.Command
 * import picocli.CommandLine.Option
 * import groovy.transform.Field
 *
 * &#64;Option(names = ['-g', '--greeting'], description = 'Type of greeting')
 * &#64;Field String greeting = 'Hello'
 *
 * println "${greeting} world!"
 * </pre>
 * <p>
 * Before the script body is executed,
 * the {@code PicocliBaseScript2} base class parses the command line and initializes {@code @Field} variables
 * annotated with {@code @Option} or {@code @Parameters}.
 * It also takes care of error handling and common use cases like requests for usage help.
 * </p><p>
 * See the {@link #run()} method for a detailed break-down of the steps the base class takes
 * before and after the statements in the script body are executed.
 * </p>
 * <h1>Customization</h1>
 * <p>
 *   Scripts can override {@link #beforeParseArgs(CommandLine)} to
 *   change the parser configuration, or set custom exception handlers etc.
 * </p>
 * <p>
 *   Scripts can override {@link #afterExecution(CommandLine, int, Exception)} to
 *   call {@code System.exit} or return a custom result.
 * </p>
 * <h1>PicocliBaseScript2 vs PicocliBaseScript</h1>
 * <p>
 * This class has the following improvements over {@link PicocliBaseScript}:
 * </p>
 * <ul>
 *   <li>Adds support for {@code @Command}-annotated methods to define subcommands in scripts.</li>
 *   <li>Adds support for {@code help} subcommands (both the built-in {@code CommandLine.HelpCommand} and custom implementations).</li>
 *   <li>Adds support for exit codes.</li>
 *   <li>Consistency with Java picocli. The new {@code PicocliBaseScript2} base class delegates to the
 *     {@code CommandLine::execute} method introduced in picocli 4.0. This allows scripts to
 *     leverage new features of the picocli library, as well as future enhancements, more easily.
 *     By contrast, the old {@code PicocliBaseScript} base class implemented its own execution strategy,
 *     which over time fell behind the core picocli library.
 *   </li>
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
     *   <li>Call {@link #getOrCreateCommandLine()} to create a new {@link CommandLine} with this
     *     script instance as the annotated command object.
     *     The {@code CommandLine} instance is cached in the {@code commandLine} property
     *     (so it can be referred to in script code with
     *     {@code this.commandLine}). Scripts may override.</li>
     *   <li>Call {@link #beforeParseArgs(CommandLine)} to install custom handlers for
     *     invalid user input or exceptions are installed.
     *     Scripts may override.
     *   </li>
     *   <li>Call {@link CommandLine#execute(String...)} method with the script arguments.
     *     This results in the following:
     *     <ul>
     *       <li>Assuming the user input was valid, this initialises all {@code @Field}
     *         variables annotated with {@link CommandLine.Option} or {@link CommandLine.Parameters}.
     *       </li>
     *       <li>Otherwise, if the user input was invalid, the command line arguments are printed
     *         to standard err, followed by an error message and the usage message.
     *         Then, the script exits (see step 4 below).
     *         This may be customized by overriding {@link #beforeParseArgs(CommandLine)} and setting a custom
     *         {@link IParameterExceptionHandler} on the {@code CommandLine} instance.</li>
     *       <li>Otherwise, if the user input requested version help or usage help, the version string or usage help message is
     *         printed to standard err and the script exits (see step 4 below).</li>
     *       <li>The script may define subcommands. In that case only the last specified subcommand is invoked.
     *         This may be customized by overriding
     *         {@link #beforeParseArgs(CommandLine)} and setting a custom
     *         {@link picocli.CommandLine.IExecutionStrategy} on the {@code CommandLine} instance.</li>
     *       <li>If no subcommand was specified, the script body is executed.</li>
     *     </ul>
     *   </li>
     *   <li>Store the exit code returned by {@code CommandLine.execute} in the {@code exitCode} property of this script.</li>
     *   <li>Call {@link #afterExecution(CommandLine, int, Exception)} to handle script exceptions and return the script result:
     *     If an exception occurred during execution, this exception is rethrown, wrapped in a {@code GroovyRuntimeException}.
     *     Otherwise, the result of the script is returned, either as a list (in case of multiple results), or as a single object.
     *   </li>
     * </ol>
     * <h1>Exit Code</h1>
     * <p>
     *     Scripts that want to control the exit code of the process that executed the script
     *     can override the {@link #afterExecution(CommandLine, int, Exception)} method and call {@code System.exit}.
     *     For example:
     * </p><pre>
     * &#64;Override
     * protected Object afterExecution(CommandLine commandLine, int exitCode, Exception exception) {
     *     exception?.printStackTrace();
     *     System.exit(exitCode);
     * }</pre>
     * @return The result of the script evaluation.
     */
    @Override
    public Object run() {
        String[] args = getScriptArguments();
        CommandLine commandLine = beforeParseArgs(getOrCreateCommandLine());
        exitCode = commandLine.execute(args);
        return afterExecution(commandLine, exitCode, exception);
    }

    /**
     * This method is called after the script has been executed, and may do one of three things:
     * <ul>
     *   <li>Call {@code System.exit} with the specified exit code.</li>
     *   <li>Throw a {@code GroovyRuntimeException} with the specified exception.</li>
     *   <li>Return the result of the script execution.</li>
     * </ul>
     * <p>By default, this method will throw a {@code GroovyRuntimeException} if the specified
     *   exception is not {@code null}, and otherwise returns the result of the script execution.
     * </p><p>
     *   Scripts may override this method to call {@code System.exit} or do something else.
     * </p>
     * @param commandLine encapsulates the picocli model of the command and subcommands
     * @param exitCode the exit code that resulted from executing the script's command and/or subcommand(s)
     * @param exception {@code null} if the script executed successfully without throwing any exceptions,
     *                              otherwise this is the exception thrown by the script
     * @return the script result; this may be a list of results, a single object, or {@code null}.
     *         If multiple commands/subcommands were executed, this method may return a {@code List},
     *         which may contain some {@code null} values.
     *         For a single command, this method will return the return value of the script,
     *         which is often the value of the last expression in the script.
     */
    protected Object afterExecution(CommandLine commandLine, int exitCode, Exception exception) {
        if (exception != null) {
            throw new GroovyRuntimeException(exception);
        }
        return scriptResult(commandLine);
    }

    /**
     * Returns the script result; this may be a list of results, a single object, or {@code null}.
     * If multiple commands/subcommands were executed, this method may return a {@code List}, which may contain some {@code null} values.
     * For a single command, this method will return the return value of the script,
     * which is often the value of the last expression in the script.
     */
    private Object scriptResult(CommandLine commandLine) {
        List<Object> result = new ArrayList<Object>();
        List<CommandLine> commandLines = commandLine.getParseResult().asCommandLineList();
        for (CommandLine cl : commandLines) {
            if (cl.getExecutionResult() != null || !result.isEmpty()) { // if multiple results, some may be null
                result.add(cl.getExecutionResult());
            }
        }
        return result.isEmpty() ? null : result.size() == 1 ? result.get(0) : result;
    }

    /**
     * Returns the script arguments as an array of strings.
     * The default implementation is to get the "args" property.
     *
     * @return the script arguments as an array of strings.
     */
    public String[] getScriptArguments() {
        return (String[]) getProperty("args");
    }

    /**
     * Returns the CommandLine for this script.
     * If there isn't one already, then this method returns the result of the {@link #createCommandLine()} method.
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
            return commandLine;
        } catch (MissingPropertyException mpe) {
            CommandLine commandLine = createCommandLine();
            // Since no property or binding already exists, we can use plain old setProperty here.
            setProperty(COMMAND_LINE, commandLine);
            return commandLine;
        }
    }

    /**
     * Customizes the specified {@code CommandLine} instance to set a custom
     * {@code IParameterExceptionHandler} and a custom {@code IExecutionExceptionHandler},
     * subclasses can override to customize further.
     * <p>
     *   This method replaces the default {@code IParameterExceptionHandler} with a custom
     *   one that prints the command line arguments before calling the default parameter exception handler.
     * </p>
     * <p>
     *   This method replaces the default {@code IExecutionExceptionHandler} with a custom
     *   one that stores any exception that occurred during execution into the {@code exception}
     *   property of this script, and returns the
     *   {@link CommandSpec#exitCodeOnExecutionException() configured exit code}.
     *   This exception is later passed to the {@link #afterExecution(CommandLine, int, Exception)} method.
     * </p>
     * @param customizable the {@code CommandLine} instance that models this command and its subcommands
     * @return the customized {@code CommandLine} instance (usually, but not necessarily,
     *          the same instance as the method parameter)
     */
    protected CommandLine beforeParseArgs(final CommandLine customizable) {
        final IParameterExceptionHandler original = customizable.getParameterExceptionHandler();
        customizable.setParameterExceptionHandler(new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                customizable.getErr().printf("args: %s%n", Arrays.toString(args));
                return original.handleParseException(ex, args);
            }
        });
        customizable.setExecutionExceptionHandler(new IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
                exception = ex; // any exception will be rethrown from afterExecution()
                return customizable.getCommandSpec().exitCodeOnExecutionException();
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
        if (CommandSpec.DEFAULT_COMMAND_NAME.equals(commandLine.getCommandName())) { // only if user did not specify @Command(name) attribute
            commandLine.setCommandName(this.getClass().getSimpleName());
        }
        return commandLine;
    }

}
