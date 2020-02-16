package picocli.examples.exceptionhandler;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

import java.util.concurrent.Callable;

@Command
public class ExecutionExceptionHandlerDemo2 implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"})
    boolean[] verbosity = new boolean[0];

    @Override
    public Integer call() {
        throw new IllegalStateException("Business exception with end user specific message");
    }

    // The default IExecutionExceptionHandler will rethrow the exception;
    // this results in the exception stack trace being printed to STDERR.

    // This example shows how to print the exception message and the usage help message instead.

    public static void main(String[] args) {
        IExecutionExceptionHandler errorHandler = new IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex,
                                                CommandLine commandLine,
                                                ParseResult parseResult) {
                commandLine.getErr().println(ex.getMessage());

                ExecutionExceptionHandlerDemo2 app = commandLine.getCommand();
                System.out.printf("app.verbosity.length=%d%n", app.verbosity.length);
                if (app.verbosity.length >= 2) {
                    ex.printStackTrace();
                }

                // alternatively, we can get the verbosity from the parse result:
                boolean[] verbosity = parseResult.matchedOptionValue("verbose", new boolean[0]);
                System.out.printf("parseResult.matchedOption(verbose).length=%d%n", verbosity.length);
                if (verbosity.length >= 1) {
                    commandLine.usage(commandLine.getErr());
                }
                return commandLine.getCommandSpec().exitCodeOnExecutionException();
            }
        };
        new CommandLine(new ExecutionExceptionHandlerDemo2())
                .setExecutionExceptionHandler(errorHandler)
                .execute("--verbose", "-vvv");
    }
}
