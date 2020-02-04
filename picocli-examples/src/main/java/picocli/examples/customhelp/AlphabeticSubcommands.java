package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class has subcommands that are not declared alphabetically.
 * We want the help for this class to show the subcommands alphabetically.
 */
@Command(name = "alphabetic",
        description = "a command that shows subcommands sorted alphabetically",
        subcommands = {Charlie.class, Bravo.class, Alpha.class, HelpCommand.class})
public class AlphabeticSubcommands implements Runnable {

    @Spec CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new AlphabeticSubcommands());
        commandLine.setHelpFactory(new IHelpFactory() {
            @Override
            public Help create(CommandSpec commandSpec, Help.ColorScheme colorScheme) {
                return new Help(commandSpec, colorScheme) {
                    /**
                     * Returns a sorted map of the subcommands.
                     */
                    @Override
                    public Map<String, Help> subcommands() {
                        return new TreeMap<>(super.subcommands());
                    }
                };
            }
        });
        commandLine.execute(args);
    }
}

@Command(name = "charlie", description = "my name starts with C")
class Charlie implements Runnable {
    public void run() {}
}

@Command(name = "bravo", description = "my name starts with B")
class Bravo implements Runnable {
    public void run() {}
}

@Command(name = "alpha", description = "my name starts with A")
class Alpha implements Runnable {
    public void run() {}
}
