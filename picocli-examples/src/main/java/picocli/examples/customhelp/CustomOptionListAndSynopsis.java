package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.IHelpSectionRenderer;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_SYNOPSIS;
import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * Demonstrates how to use custom {@link IHelpSectionRenderer} implementations
 * to hide INHERIT-ed options in the option list and synopsis of subcommands.
 */
public class CustomOptionListAndSynopsis {

    static IHelpSectionRenderer optionListRenderer = new IHelpSectionRenderer() {
        public String render(CommandLine.Help help) {
            return help.optionListExcludingGroups(filter(help.commandSpec().options()));
        }
    };

    static IHelpSectionRenderer synopsisRenderer = new IHelpSectionRenderer() {
        public String render(CommandLine.Help help) {

            // building a custom synopsis is more complex;
            // we subclass Help here so we can invoke some protected methods
            class HelpHelper extends CommandLine.Help {
                public HelpHelper(CommandSpec commandSpec, ColorScheme colorScheme) {
                    super(commandSpec, colorScheme);
                }
                String buildSynopsis() {
                    // customize the list of options to show in the synopsis
                    List<OptionSpec> myOptions = filter(help.commandSpec().options());

                    // and build up the synopsis text with our customized options list...
                    Set<ArgSpec> argsInGroups = new HashSet<ArgSpec>();
                    Text groupsText = createDetailedSynopsisGroupsText(argsInGroups);
                    Text optionText = createDetailedSynopsisOptionsText(argsInGroups, myOptions, CommandLine.Help.createShortOptionArityAndNameComparator(), true);
                    Text endOfOptionsText = createDetailedSynopsisEndOfOptionsText();
                    Text positionalParamText = createDetailedSynopsisPositionalsText(argsInGroups);
                    Text commandText = createDetailedSynopsisCommandText();

                    return makeSynopsisFromParts(help.synopsisHeadingLength(), optionText, groupsText, endOfOptionsText, positionalParamText, commandText);
                }
            }
            // and delegate the work to our helper subclass
            return new HelpHelper(help.commandSpec(), help.colorScheme()).buildSynopsis();
        }
    };

    private static List<OptionSpec> filter(List<OptionSpec> optionList) {
        List<OptionSpec> shown = new ArrayList<OptionSpec>();
        for (OptionSpec option : optionList) {
            if (!option.inherited() || option.shortestName().equals("-b")) {
                shown.add(option);
            }
        }
        return shown;
    }


    @Command
    static class App {
        @Option(names = "-a", scope = INHERIT, description = "a option") boolean a;
        @Option(names = "-b", scope = INHERIT, description = "b option") boolean b;
        @Option(names = "-c", scope = INHERIT, description = "c option") boolean c;

        @Command(description = "This is the `sub` subcommand...")
        void sub() {}
    }
    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new App());
        for (CommandLine sub : cmd.getSubcommands().values()) {
            sub.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, optionListRenderer);
            sub.getHelpSectionMap().put(SECTION_KEY_SYNOPSIS, synopsisRenderer);
        }
        String expected = String.format("" +
                "Usage: <main class> [-abc] [COMMAND]%n" +
                "  -a     a option%n" +
                "  -b     b option%n" +
                "  -c     c option%n" +
                "Commands:%n" +
                "  sub  This is the `sub` subcommand...%n");
        String actual = cmd.getUsageMessage(CommandLine.Help.Ansi.OFF);
        if (!expected.equals(actual)) {
            throw new IllegalStateException(expected + " != " + actual);
        }

        String expectedSub = String.format("" +
                "Usage: <main class> sub [-b]%n" +
                "This is the `sub` subcommand...%n" +
                "  -b     b option%n");
        String actualSub = cmd.getSubcommands().get("sub").getUsageMessage(CommandLine.Help.Ansi.OFF);
        if (!expectedSub.equals(actualSub)) {
            throw new IllegalStateException(expectedSub + " != " + actualSub);
        }
    }
}