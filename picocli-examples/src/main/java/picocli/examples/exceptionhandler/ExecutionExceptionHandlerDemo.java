package picocli.examples.exceptionhandler;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

import java.util.concurrent.Callable;

@Command
public class ExecutionExceptionHandlerDemo implements Callable<Integer> {
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
                commandLine.usage(commandLine.getErr());
                return commandLine.getCommandSpec().exitCodeOnExecutionException();
            }
        };
        int exitCode = new CommandLine(new ExecutionExceptionHandlerDemo())
                .setExecutionExceptionHandler(errorHandler)
                .execute(args);
    }
}
