package picocli.examples.autocomplete;

import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "mycommand", mixinStandardHelpOptions = true, version = "1.0",
        subcommands = Completion.class)
public class CommandWithCompletionSubcommand implements Runnable {

    @Option(names = {"-x", "--x-long-option"})
    int x;

    @Option(names = {"-y", "--y-long-option"})
    int y;

    @Override
    public void run() {
        System.out.printf("x * y = %s%n", x * y);
    }

    public static void main(String[] args) {
        new CommandLine(new CommandWithCompletionSubcommand()).execute(args);
    }
}

@Command(name = "completion", mixinStandardHelpOptions = true,
        description = "Generate a completion script for the parent command")
class Completion implements Runnable {

    @Spec CommandLine.Model.CommandSpec spec;

    @Option(names = {"-n", "--name"},
            description = "Optionally specify the name of the command to create" +
                    " a completion script for, \"${DEFAULT-VALUE}\" by default.")
    String name = "mycommand";

    @Override
    public void run() {
        System.out.println(
                AutoComplete.bash(name, spec.parent().commandLine()));
    }
}
