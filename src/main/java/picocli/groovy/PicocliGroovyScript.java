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
 * Base script that provides CommandLine declarative (annotation-based) argument processing for scripts.
 *
 * @author Jim White
 * @author Remko Popma
 */
abstract public class PicocliGroovyScript extends Script {
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
     * The default name for the command name in the usage help message is the script's class simple name.
     * <p>
     * Subclasses may override to register custom type converters or programmatically add subcommands.
     * </p>
     *
     * @return A CommandLine instance with the commands (if any) initialized.
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
     * @param message
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
