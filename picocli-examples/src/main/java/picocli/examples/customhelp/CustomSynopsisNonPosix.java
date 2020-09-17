package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

import java.util.Comparator;

/**
 * Demonstrates how to use a custom {@link IHelpFactory} and subclassing {@code Help}
 * to create a custom synopsis.
 *
 * See https://github.com/remkop/picocli/issues/1177
 */
public class CustomSynopsisNonPosix {

    @Command(name = "customSynopsis", version = "customSynopsis 1.0", mixinStandardHelpOptions = true,
            description = "Demonstrates how to create a custom synopsis that does not cluster boolean options together.")
    static class App {
        @Option(names = "-a", description = "a option") boolean a;
        @Option(names = "-b", description = "b option") boolean b;
        @Option(names = "-c", description = "c option") boolean c;
    }

    static class MyHelpFactory implements IHelpFactory {
        @Override
        public Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
            return new Help(commandSpec, colorScheme) {
                @Override
                public String detailedSynopsis(int synopsisHeadingLength, Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
                    return super.detailedSynopsis(synopsisHeadingLength, optionSort, false);
                }
            };
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new App());
        cmd.setHelpFactory(new MyHelpFactory());

        String expected = String.format("" +
                "Usage: customSynopsis [-a] [-b] [-c] [-h] [-V]%n" +
                "Demonstrates how to create a custom synopsis that does not cluster boolean%n" +
                "options together.%n" +
                "  -a              a option%n" +
                "  -b              b option%n" +
                "  -c              c option%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        String actual = cmd.getUsageMessage(Help.Ansi.OFF);
        if (!expected.equals(actual)) {
            throw new IllegalStateException(expected + " != " + actual);
        }
    }
}