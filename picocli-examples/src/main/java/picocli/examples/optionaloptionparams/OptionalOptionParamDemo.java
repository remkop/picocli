package picocli.examples.optionaloptionparams;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * This class demonstrates how to use a custom IParameterConsumer
 * to handle complex situations where the default picocli parser is insufficient.
 * <p>
 * We want to handle cases like this:
 * </p><pre>
 *  myapp --debug 8080 a.txt b.txt # debug port=8080
 *  myapp --debug a.txt b.txt      # debug port=12345 (the fallback value)
 *  myapp a.txt b.txt              # no debugging
 * </pre>
 * <p>
 * A custom IParamaterConsumer is needed so that input like
 * {@code myapp --debug a.txt} does not result in an error like this:
 * {@code "Invalid value for option '--debug': 'a.txt' is not an int"}.
 * </p>
 */
@Command(name = "myapp")
public class OptionalOptionParamDemo implements Callable<Integer> {
    @Option(names = "--debug", arity = "0..1", fallbackValue = "12345",
            parameterConsumer = CustomConsumer.class,
            description = "The port where to wait for debug connections; " +
                    "if no port is specified, ${FALLBACK-VALUE} is used. " +
                    "If this option is not specified at all, then no debugging port is opened."
    )
    int port;

    @Parameters()
    List<String> files;

    @Override
    public Integer call() {
        System.out.printf("Port=%d, files=%s%n", port, files);
        return 0;
    }

    public static void main(String[] ignored) {
        String[][] cases = new String[][] {
                {"--debug=999", "a.txt", "b.txt"},
                {"--debug", "999", "a.txt", "b.txt"},
                {"--debug", "--", "a.txt", "b.txt"},
                {"--debug=", "a.txt", "b.txt"},
                {"--debug", "a.txt", "b.txt"},
                {"a.txt", "b.txt"},
        };

        for (String[] args: cases) {
            System.out.println(Arrays.toString(args));
            new CommandLine(new OptionalOptionParamDemo()).execute(args);
            System.out.println();
        }
    }

    static class CustomConsumer implements IParameterConsumer {
        @Override
        public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
            String arg = args.pop();
            try {
                int port = Integer.parseInt(arg);
                argSpec.setValue(port);
            } catch (Exception ex) {
                String fallbackValue = (argSpec.isOption()) ? ((OptionSpec) argSpec).fallbackValue() : null;
                try {
                    int fallbackPort = Integer.parseInt(fallbackValue);
                    argSpec.setValue(fallbackPort);
                } catch (Exception badFallbackValue) {
                    throw new InitializationException("FallbackValue for --debug must be an int", badFallbackValue);
                }
                args.push(arg); // put it back
            }
        }
    }
}
