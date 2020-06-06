package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * Demonstrates how to use a custom {@link IHelpFactory} and subclassing {@code Help}
 * to hide INHERIT-ed options in the option list and synopsis of subcommands.
 */
public class CustomOptionListAndSynopsisWithInheritance {

    @Command(description = "Inheritance example")
    static class App {
        @Option(names = "-a", scope = INHERIT, description = "a option") boolean a;
        @Option(names = "-b", scope = INHERIT, description = "b option") boolean b;
        @Option(names = "-c", scope = INHERIT, description = "c option") boolean c;

        @Command(description = "This is the `sub` subcommand...")
        void sub() {}
    }

    static class MyHelpFactory implements IHelpFactory {
        @Override
        public Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
            return new Help(commandSpec, colorScheme) {
                @Override
                public String optionListExcludingGroups(List<OptionSpec> optionList, Layout layout, Comparator<OptionSpec> optionSort, IParamLabelRenderer valueLabelRenderer) {
                    return super.optionListExcludingGroups(filter(optionList), layout, optionSort, valueLabelRenderer);
                }

                @Override
                protected Text createDetailedSynopsisOptionsText(Collection<ArgSpec> done, List<OptionSpec> optionList, Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
                    return super.createDetailedSynopsisOptionsText(done, filter(optionList), optionSort, clusterBooleanOptions);
                }

                private List<OptionSpec> filter(List<OptionSpec> optionList) {
                    List<OptionSpec> shown = new ArrayList<OptionSpec>();
                    for (OptionSpec option : optionList) {
                        if (!option.inherited() || option.shortestName().equals("-b")) {
                            shown.add(option);
                        }
                    }
                    return shown;
                }
            };
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new App());
        cmd.setHelpFactory(new MyHelpFactory());

        String expected = String.format("" +
                "Usage: <main class> sub [-b]%n" +
                "This is the `sub` subcommand...%n" +
                "  -b     b option%n");
        String actual = cmd.getSubcommands().get("sub").getUsageMessage(Help.Ansi.OFF);
        if (!expected.equals(actual)) {
            throw new IllegalStateException(expected + " != " + actual);
        }
    }
}