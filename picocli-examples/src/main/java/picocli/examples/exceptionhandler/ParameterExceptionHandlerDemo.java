package picocli.examples.exceptionhandler;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

@Command
public class ParameterExceptionHandlerDemo implements Runnable {
    @Option(names="--mypath", defaultValue = "${env:MYPATH}", required = true, descriptionKey = "path")
    private String path;

    @Override
    public void run() {
        System.out.println("Path: " + path);
    }

    public static void main(String[] args) {

        final CommandLine commandLine = new CommandLine(new ParameterExceptionHandlerDemo());

        // The default error message if --mypath is not specified and there is no environment variable MYPATH
        // is "Missing required option '--mypath=<path>'".

        // This example shows how to customize the error message
        // to indicate users may also specify an environment variable.

        IParameterExceptionHandler handler = new IParameterExceptionHandler() {
            IParameterExceptionHandler defaultHandler = commandLine.getParameterExceptionHandler();
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                ArgSpec path = commandLine.getCommandSpec().findOption("--mypath");
                if (ex instanceof MissingParameterException && ((MissingParameterException) ex).getMissing().contains(path)) {
                    CommandLine cmd = ex.getCommandLine();
                    cmd.getErr().println(ex.getMessage() + ", alternatively specify environment variable MYPATH");
                    cmd.usage(cmd.getErr());
                    return cmd.getCommandSpec().exitCodeOnInvalidInput();
                }
                return defaultHandler.handleParseException(ex, args);
            }
        };

        int exitCode = commandLine
                .setParameterExceptionHandler(handler)
                .execute(args);
    }
}
