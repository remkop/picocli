package picocli.examples.synopsis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.util.Collection;
import java.util.Comparator;

/**
 * See https://stackoverflow.com/questions/66004574/picocli-options-order-in-usage-section
 */
@Command(name = "application", subcommands = { AddSubCommand.class })
public class Unsorted implements Runnable {
    @Spec CommandSpec spec;

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    }

    public static void main(String... args) {
        new CommandLine(new Unsorted())
                .setHelpFactory(new UnsortedSynopsisHelpFactory())
                .execute("add", "-h");
    }
}

@Command(name = "add", sortOptions = false)
class AddSubCommand implements Runnable {
    @Option(names = { "-h" }, usageHelp = true, hidden = true)
    boolean helpFlag;

    @Option(names = { "-i" }, required = true, description = "id")
    int id;

    @Option(names = { "-t" }, required = true, description = "type")
    String type;

    @Option(names = { "-c" }, required = true, description = "config")
    String config;

    public void run() {
        // logic
    }
}

class UnsortedSynopsisHelpFactory implements CommandLine.IHelpFactory {

    @Override
    public CommandLine.Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
        return new CommandLine.Help(commandSpec, colorScheme) {
            @Override
            protected Ansi.Text createDetailedSynopsisOptionsText(
                    Collection<ArgSpec> done,
                    Comparator<OptionSpec> optionSort,
                    boolean clusterBooleanOptions) {

                return super.createDetailedSynopsisOptionsText(
                        done,
                        null,  // do not sort options in synopsis
                        clusterBooleanOptions);
            }
        };
    }
}
