package picocli.shell.jline2.example;

import java.io.IOException;
import java.util.Stack;

import jline.console.ConsoleReader;
import picocli.CommandLine;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * <p>A parameter consumer for interactively entering a value (e.g. a password).
 * <p>Similar to {@link Option#interactive()} and {@link Parameters#interactive()}.
 * Picocli's interactive and JLine's {@link ConsoleReader} do not work well together.
 * Thus delegating reading input to {@link ConsoleReader} should be preferred.
 * @since 4.0.1
 */
public class InteractiveParameterConsumer implements IParameterConsumer {

    private final ConsoleReader reader;

    public InteractiveParameterConsumer(ConsoleReader reader) {
        this.reader = reader;
    }

    public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
        try {
            argSpec.setValue(reader.readLine(String
                        .format("Enter %s: ", argSpec.paramLabel()), '\0'));
        } catch (IOException e) {
            throw new CommandLine.ParameterException(commandSpec.commandLine()
                    , "Error while reading interactivly", e, argSpec, "");
        }
    }
}
