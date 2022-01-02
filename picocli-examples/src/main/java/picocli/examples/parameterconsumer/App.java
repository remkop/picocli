package picocli.examples.parameterconsumer;

import java.util.Stack;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

class App implements Runnable {
    @Option(names = "-x", parameterConsumer = App.CustomConsumer.class)
    String x;

    @Option(names = "-y")
    String y;

    @Command
    public void mySubcommand() {}

    static class CustomConsumer implements IParameterConsumer {
        @Override
        public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec cmdSpec) {
            if (args.isEmpty()) {
                throw new ParameterException(cmdSpec.commandLine(),
                    "Error: option '-x' requires a parameter");
            }
            String arg = args.pop();
            argSpec.setValue(arg);
        }
    }

    public void run() {
        System.out.printf("x='%s', y='%s'%n", x, y);
    }

    public static void main(String... args) {
        new CommandLine(new App()).execute(args);
    }
}
