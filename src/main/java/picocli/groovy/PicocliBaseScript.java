/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package picocli.groovy;

import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>
 * Base script that provides picocli declarative (annotation-based) argument processing for scripts.
 * </p><p>
 * Scripts may install this base script via the standard Groovy
 * {@code @groovy.transform.BaseScript(picocli.groovy.PicocliBaseScript)} annotation, but
 * the {@link PicocliScript} annotation is preferred since it is more compact and enables scripts to use the
 * {@code @Command} annotation:
 * </p>
 * <pre>
 * &#64;Command(name = "myCommand", description = "does something special")
 * &#64;PicocliScript
 * import picocli.groovy.PicocliScript
 * import picocli.CommandLine.Command
 * ...
 * </pre>
 * <p>
 * Before the script body is executed,
 * the {@code PicocliBaseScript} base class parses the command line and initializes {@code @Field} variables
 * annotated with {@code @Option} or {@code @Parameters}.
 * It also takes care of error handling and common use cases like requests for usage help.
 * </p><p>
 * Here is a break-down of the steps the base class takes before the statements in the script body are executed:
 * </p>
 * <ol>
 *   <li>A new {@link CommandLine} is created with this script instance as the annotated command object.
 *     The {@code CommandLine} instance is cached in the {@code scriptCommandLine} property
 *     (so it can be referred to in script code with
 *     {@code this.scriptCommandLine}). {@code CommandLine} creation and initialization may be
 *     customized by overriding {@link #createScriptCommandLine()}.</li>
 *   <li>The {@link CommandLine#parse(String...)} method is called with the script arguments.
 *     This initialises all {@code @Field} variables annotated with {@link CommandLine.Option} or
 *     {@link CommandLine.Parameters}, unless the user input was invalid.</li>
 *   <li>If the user input was invalid, an error message and the usage message are printed to standard err and the
 *     script exits. This may be customized by overriding
 *     {@link #handleParameterException(CommandLine, String[], ParameterException)}.</li>
 *   <li>Otherwise, if the user input requested version help or usage help, the version string or usage help message is
 *     printed to standard err and the script exits.</li>
 *   <li>If the script implements {@code Runnable} or {@code Callable}, its {@code run} (or {@code call}) method
 *     is called. The script may support subcommands. In that case only the last specified subcommand is run or called
 *     if it implements {@code Runnable} or {@code Callable}. This may be customized by overriding
 *     {@link #runRunnableSubcommand(List)}.</li>
 *   <li>Finally, the script body is executed.</li>
 * </ol>
 *
 * @author Jim White
 * @author Remko Popma
 * @since 2.0
 */
abstract public class PicocliBaseScript extends Script {
    /**
     * Name of the property that holds the CommandLine instance for this script ({@value}).
     */
    public final static String SCRIPT_COMMAND_LINE = "scriptCommandLine";

    /**
     * The script body
     * @return The result of the script evaluation.
     */
    protected abstract Object runScriptBody();

    @Override
    public Object run() {
        String[] args = getScriptArguments();
        CommandLine commandLine = getScriptCommandLineWithInit();
        List<CommandLine> parsedCommands = null;
        try {
            parsedCommands = parseScriptArguments(commandLine, args);
        } catch (ParameterException pe) {
            return handleParameterException(commandLine, args, pe);
        }
        try {
            // check if the user requested help for the top-level command or any of the subcommands
            for (CommandLine parsed : parsedCommands) {
                if (parsed.isUsageHelpRequested()) {
                    return printHelpMessage(parsed);
                }
                if (parsed.isVersionHelpRequested()) {
                    return printVersionHelpMessage(parsed);
                }
            }
            runRunnableSubcommand(parsedCommands);
            return runScriptBody();
        } catch (Exception ex) {
            return handleExecutionException(commandLine, args, ex);
        }
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
     * If there isn't one already, then create it using createScriptCommandLine.
     *
     * @return the CommandLine for this script.
     */
    protected CommandLine getScriptCommandLineWithInit() {
        try {
            CommandLine commandLine = (CommandLine) getProperty(SCRIPT_COMMAND_LINE);
            if (commandLine == null) {
                commandLine = createScriptCommandLine();
                // The script has a real property (a field or getter) but if we let Script.setProperty handle
                // this then it just gets stuffed into a binding that shadows the property.
                // This is somewhat related to other bugged behavior in Script wrt properties and bindings.
                // See http://jira.codehaus.org/browse/GROOVY-6582 for example.
                // The correct behavior for Script.setProperty would be to check whether
                // the property has a setter before creating a new script binding.
                this.getMetaClass().setProperty(this, SCRIPT_COMMAND_LINE, commandLine);
            }
            return commandLine;
        } catch (MissingPropertyException mpe) {
            CommandLine commandLine = createScriptCommandLine();
            // Since no property or binding already exists, we can use plain old setProperty here.
            setProperty(SCRIPT_COMMAND_LINE, commandLine);
            return commandLine;
        }
    }

    /**
     * Create a new CommandLine instance.
     * The default name for the command name in the usage help message is the script's class simple name (unless
     * annotated with some other command name with the {@code @Command(name = "...")} annotation).
     * <p>
     * Subclasses may override to register custom type converters or programmatically add subcommands.
     * </p>
     *
     * @return A CommandLine instance.
     */
    public CommandLine createScriptCommandLine() {
        CommandLine commandLine = new CommandLine(this);
        if (commandLine.getCommandName().equals("<main class>")) { // only if user did not specify @Command(name) attribute
            commandLine.setCommandName(this.getClass().getSimpleName());
        }
        return commandLine;
    }

    /**
     * Returns the result of calling {@link CommandLine#parse(String...)} with the given arguments.
     * <p>
     * Subclasses may override if any action should be taken before the Runnable/Callable commands are run.
     * </p>
     * @param commandLine The CommandLine instance for this script instance.
     * @param args  The argument array.
     * @return the list of {@code CommandLine} objects that result from parsing the user input
     */
    public List<CommandLine> parseScriptArguments(CommandLine commandLine, String[] args) {
        return commandLine.parse(args);
    }

    /**
     * If the most specific subcommand (the last {@code CommandLine} object in the list) implements Runnable or Callable,
     * then run it.
     * This method will not run the main script {@link #runScriptBody()} method; it will be called from {@code {@link #run()}}.
     *
     * @param parsedCommands the list of {@code CommandLine} objects returns from the {@code CommandLine.parse} method
     * @throws Exception if the Callable throws an exception
     */
    public void runRunnableSubcommand(List<CommandLine> parsedCommands) throws Exception {
        CommandLine deepestSubcommand = parsedCommands.get(parsedCommands.size() - 1);
        Object commandObject = deepestSubcommand.getCommand();
        if (commandObject instanceof Runnable) {
            Runnable runnableCommand = (Runnable) commandObject;
            if (runnableCommand != this) {
                runnableCommand.run();
            }
        } else if (commandObject instanceof Callable<?>) {
            Callable<?> callableCommand = (Callable<?>) commandObject;
            if (callableCommand != this) {
                callableCommand.call();
            }
        }
    }

    /**
     * Error messages that arise from command line processing call this.
     * The default is to print to System.err.
     * If you want to use System.out, a logger, or something else, this is the method to override.
     *
     * @param message the error message to print
     */
    public void printErrorMessage(String message) {
        System.err.println(message);
    }

    /**
     * If a ParameterException occurs during {@link #parseScriptArguments(CommandLine, String[])}
     * then this method gets called to report the problem.
     * The default behavior is to show the exception message using {@link #printErrorMessage(String)},
     * then call {@link #printHelpMessage(CommandLine)}.
     * The return value becomes the return value for the Script.run which will be the exit code
     * if we've been called from the command line.
     *
     * @param commandLine The CommandLine instance
     * @param args The argument array
     * @param pe The ParameterException that occurred
     * @return The value that Script.run should return (1 by default).
     */
    public Object handleParameterException(CommandLine commandLine, String[] args, ParameterException pe) {
        printErrorMessage(String.format("args: %s%n%s", Arrays.toString(args), pe.getMessage()));
        printHelpMessage(commandLine);
        return 1;
    }

    /**
     * If an Exception occurs during {@link #runRunnableSubcommand(List)}, or {@link #runScriptBody()}
     * then this gets called to report the problem.
     * The default behavior is to throw a new {@code RuntimeException} wrapping the specified exception.
     *
     * @param commandLine The CommandLine instance
     * @param args The argument array
     * @param ex The Exception that occurred
     * @return The value that Script.run should return when overriding this method
     * @throws RuntimeException wrapping the specified exception by default
     */
    public Object handleExecutionException(CommandLine commandLine, String[] args, Exception ex) {
        throw new RuntimeException(ex);
    }

    /**
     * If an &#064;Option whose {@code usageHelp} attribute is annotated as true appears in the arguments.
     * then the script body is not run and this {@code printHelpMessage} method is called instead.
     * The default behavior is to print the {@link CommandLine#usage(PrintStream)} to {@code System.err}.
     * The return value becomes the return value for the Script.run which will be the exit code
     * if we've been called from the command line.
     *
     * @param commandLine The CommandLine instance
     * @return The value that Script.run should return ({@code null} by default).
     */
    public Object printHelpMessage(CommandLine commandLine) {
        commandLine.usage(System.err);
        return null;
    }

    /**
     * If an &#064;Option whose {@code versionHelp} attribute is annotated as true appears in the arguments.
     * then the script body is not run and this printVersionHelpMessage method is called instead.
     * The default behavior is to print the {@link CommandLine#printVersionHelp(PrintStream)} to {@code System.err}.
     * The return value becomes the return value for the Script.run which will be the exit code
     * if we've been called from the command line.
     *
     * @param commandLine The CommandLine instance
     * @return The value that Script.run should return ({@code null} by default).
     */
    public Object printVersionHelpMessage(CommandLine commandLine) {
        commandLine.printVersionHelp(System.err);
        return null;
    }
}
