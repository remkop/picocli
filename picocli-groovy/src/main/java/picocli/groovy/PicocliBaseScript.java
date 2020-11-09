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
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.ParameterException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>
 * Base script class that provides picocli declarative (annotation-based) command line
 * argument processing for Groovy scripts, superseded by {@link PicocliBaseScript2}.
 * </p><p>
 * Scripts may install this base script via the {@link PicocliScript} annotation or via the standard Groovy
 * {@code @groovy.transform.BaseScript(picocli.groovy.PicocliBaseScript)} annotation, but
 * the {@code @PicocliScript} annotation is preferred since it enables scripts to use the
 * {@code @Command} annotation. Example usage:
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
 * See the {@link #run()} method for a detailed break-down of the steps the base class takes
 * before the statements in the script body are executed.
 * </p>
 *
 * @author Jim White
 * @author Remko Popma
 * @since 2.0
 * @see PicocliBaseScript2
 * @deprecated Use {@link PicocliBaseScript2} instead.
 */
abstract public class PicocliBaseScript extends Script {
    /**
     * Name of the property that holds the CommandLine instance for this script ({@value}).
     */
    public final static String COMMAND_LINE = "commandLine";

    /**
     * The script body.
     * @return The result of the script evaluation.
     */
    protected abstract Object runScriptBody();

    /**
     * <p>
     * Parses the command line and initializes {@code @Field} variables
     * annotated with {@code @Option} or {@code @Parameters} before executing the script body.
     * Also takes care of error handling and common use cases like requests for usage help.
     * </p><p>
     * Here is a break-down of the steps the base class takes before the statements in the script body are executed:
     * </p>
     * <ol>
     *   <li>A new {@link CommandLine} is created with this script instance as the annotated command object.
     *     The {@code CommandLine} instance is cached in the {@code commandLine} property
     *     (so it can be referred to in script code with
     *     {@code this.commandLine}). {@code CommandLine} creation and initialization may be
     *     customized by overriding {@link #createCommandLine()}.</li>
     *   <li>The {@link #parseScriptArguments(CommandLine, String[])} method is called with the script arguments.
     *     This initialises all {@code @Field} variables annotated with {@link Option} or
     *     {@link Parameters}, unless the user input was invalid.</li>
     *   <li>If the user input was invalid, an error message and the usage message are printed to standard err and the
     *     script exits. This may be customized by overriding
     *     {@link #handleParameterException(CommandLine.ParameterException, String[])}.</li>
     *   <li>Otherwise, if the user input requested version help or usage help, the version string or usage help message is
     *     printed to standard err and the script exits.</li>
     *   <li>If the script implements {@code Runnable} or {@code Callable}, its {@code run} (or {@code call}) method
     *     is called. The script may support subcommands. In that case only the last specified subcommand is run or called
     *     if it implements {@code Runnable} or {@code Callable}. This may be customized by overriding
     *     {@link #runRunnableSubcommand(List)}.</li>
     *   <li>Finally, the script body is executed.</li>
     * </ol>
     * @return The result of the script evaluation.
     */
    @Override
    public Object run() {
        String[] args = getScriptArguments();
        CommandLine commandLine = getOrCreateCommandLine();
        List<CommandLine> parsedCommands = null;
        try {
            parsedCommands = parseScriptArguments(commandLine, args);
        } catch (ParameterException pe) {
            return handleParameterException(pe, args);
        }
        try {
            // check if the user requested help for the top-level command or any of the subcommands
            for (CommandLine parsed : parsedCommands) {
                if (parsed.isUsageHelpRequested()) {
                    return printHelpMessage(parsed, System.out);
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
            return commandLine;
        } catch (MissingPropertyException mpe) {
            CommandLine commandLine = createCommandLine();
            // Since no property or binding already exists, we can use plain old setProperty here.
            setProperty(COMMAND_LINE, commandLine);
            return commandLine;
        }
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

    /**
     * Returns the result of calling {@link CommandLine#parse(String...)} with the given arguments.
     * <p>
     * Subclasses may override if any action should be taken immediately before or after parsing.
     * </p>
     * @param commandLine The CommandLine instance for this script instance.
     * @param args  The argument array.
     * @return the list of {@code CommandLine} objects that result from parsing the user input
     */
    @SuppressWarnings("deprecation")
    public List<CommandLine> parseScriptArguments(CommandLine commandLine, String[] args) {
        return commandLine.parse(args);
    }

    /**
     * If the most specific subcommand (the last {@code CommandLine} object in the list) implements Runnable or Callable,
     * then run it.
     * This method will not run the main script {@link #runScriptBody()} method; that will be called from {@code {@link #run()}}.
     *
     * @param parsedCommands the list of {@code CommandLine} objects returns from the {@code CommandLine.parse} method
     * @throws Exception if the Callable throws an exception
     */
    public void runRunnableSubcommand(List<CommandLine> parsedCommands) throws Exception {
        CommandLine deepestSubcommand = parsedCommands.get(parsedCommands.size() - 1);
        Object commandObject = deepestSubcommand.getCommand();
        if (commandObject == this) {
            return;
        }
        if (commandObject instanceof Runnable) {
            ((Runnable) commandObject).run();
        } else if (commandObject instanceof Callable<?>) {
            ((Callable<?>) commandObject).call();
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
     * @param pe The ParameterException that occurred
     * @param args The argument array
     * @return The value that Script.run should return. This implementation returns 1, subclasses may override.
     */
    public Object handleParameterException(ParameterException pe, String[] args) {
        printErrorMessage(String.format("args: %s%n%s", Arrays.toString(args), pe.getMessage()));
        printHelpMessage(pe.getCommandLine());
        return 1;
    }

    /**
     * If an Exception occurs during {@link #runRunnableSubcommand(List)}, or {@link #runScriptBody()}
     * then this gets called to report the problem.
     * The default behavior is to throw a new {@code ExecutionException} wrapping the specified exception.
     *
     * @param commandLine The CommandLine instance
     * @param args The argument array
     * @param ex The Exception that occurred
     * @return The value that Script.run should return when overriding this method
     * @throws ExecutionException wrapping the specified exception by default
     */
    public Object handleExecutionException(CommandLine commandLine, String[] args, Exception ex) {
        if (ex instanceof ExecutionException) {
            throw (ExecutionException) ex;
        }
        throw new ExecutionException(commandLine, ex.toString(), ex);
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
        return printHelpMessage(commandLine, System.err);
    }

    /**
     * If an &#064;Option whose {@code usageHelp} attribute is annotated as true appears in the arguments.
     * then the script body is not run and this {@code printHelpMessage} method is called instead.
     * The default behavior is to print the {@link CommandLine#usage(PrintStream)} to the specified stream.
     * The return value becomes the return value for the Script.run which will be the exit code
     * if we've been called from the command line.
     *
     * @param commandLine The CommandLine instance
     * @param stream the stream to print the usage help message to
     * @return The value that Script.run should return ({@code null} by default).
     * @since 3.0
     */
    public Object printHelpMessage(CommandLine commandLine, PrintStream stream) {
        commandLine.usage(stream);
        return null;
    }

    /**
     * If an &#064;Option whose {@code versionHelp} attribute is annotated as true appears in the arguments.
     * then the script body is not run and this printVersionHelpMessage method is called instead.
     * The default behavior is to print the {@link CommandLine#printVersionHelp(PrintStream)} to {@code System.out}.
     * The return value becomes the return value for the Script.run which will be the exit code
     * if we've been called from the command line.
     *
     * @param commandLine The CommandLine instance
     * @return The value that Script.run should return ({@code null} by default).
     */
    public Object printVersionHelpMessage(CommandLine commandLine) {
        commandLine.printVersionHelp(System.out);
        return null;
    }
}
