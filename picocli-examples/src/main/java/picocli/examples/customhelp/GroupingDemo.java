package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

@Command(name = "grouping-demo", mixinStandardHelpOptions = true,
        description = "This shows how to group subcommands with separate headings")
public class GroupingDemo {

    public static void main(String[] args) {

        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("%nFirst Group:%n", Arrays.asList("sub2", "sub4", "sub6"));
        sections.put("%nSecond Group:%n", Arrays.asList("sub1", "sub3", "sub5"));
        sections.put("%nThird Group:%n", Arrays.asList("sub7", "sub8"));
        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);

        CommandLine cmd = new CommandLine(new GroupingDemo());
        cmd.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);
        cmd.usage(System.out);
    }

    @Command(aliases = {"s1", "foo"},
            description = "The quick brown fox jumps over the lazy dog.")
    void sub1() {}

    @Command(description = "How quickly daft jumping zebras vex.")
    void sub2() {}

    @Command(description = "The five boxing wizards jump quickly.")
    void sub3() {}

    @Command(description = "Pack my box with five dozen liquor jugs.")
    void sub4() {}

    @Command(description = "A wizard’s job is to vex chumps quickly in fog.")
    void sub5() {}

    @Command(description = "Cwm fjord bank glyphs vext quiz.")
    void sub6() {}

    @Command(description = "Jived fox nymph grabs quick waltz.")
    void sub7() {}

    @Command(description = "Glib jocks quiz nymph to vex dwarf.")
    void sub8() {}
}

class CommandGroupRenderer implements CommandLine.IHelpSectionRenderer {
    private final Map<String, List<String>> sections;

    public CommandGroupRenderer(Map<String, List<String>> sections) {
        this.sections = sections;
    }

    //@Override
    public String render(CommandLine.Help help) {
        if (help.commandSpec().subcommands().isEmpty()) { return ""; }

        StringBuilder result = new StringBuilder();
        sections.forEach((key, value) -> result.append(renderSection(key, value, help)));
        return result.toString();
    }

    private String renderSection(String sectionHeading, List<String> cmdNames, CommandLine.Help help) {
        TextTable textTable = createTextTable(help);

        for (String name : cmdNames) {
            CommandSpec sub = help.commandSpec().subcommands().get(name).getCommandSpec();

            // create comma-separated list of command name and aliases
            String names = sub.names().toString();
            names = names.substring(1, names.length() - 1); // remove leading '[' and trailing ']'

            // description may contain line separators; use Text::splitLines to handle this
            String description = description(sub.usageMessage());
            Text[] lines = help.colorScheme().text(String.format(description)).splitLines();

            for (int i = 0; i < lines.length; i++) {
                Text cmdNamesText = help.colorScheme().commandText(i == 0 ? names : "");
                textTable.addRowValues(cmdNamesText, lines[i]);
            }
        }
        return help.createHeading(sectionHeading) + textTable.toString();
    }

    private TextTable createTextTable(CommandLine.Help help) {
        CommandSpec spec = help.commandSpec();
        // prepare layout: two columns
        // the left column overflows, the right column wraps if text is too long
        int commandLength = maxLength(spec.subcommands(), 37);
        TextTable textTable = TextTable.forColumns(help.colorScheme(),
                new Column(commandLength + 2, 2, Column.Overflow.SPAN),
                new Column(spec.usageMessage().width() - (commandLength + 2), 2, Column.Overflow.WRAP));
        textTable.setAdjustLineBreaksForWideCJKCharacters(spec.usageMessage().adjustLineBreaksForWideCJKCharacters());
        return textTable;
    }

    private int maxLength(Map<String, CommandLine> subcommands, int max) {
        int result = subcommands.values().stream().map(cmd -> cmd.getCommandSpec().names().toString().length() - 2).max(Integer::compareTo).get();
        return Math.min(max, result);
    }

    private String description(UsageMessageSpec usageMessage) {
        if (usageMessage.header().length > 0) {
            return usageMessage.header()[0];
        }
        if (usageMessage.description().length > 0) {
            return usageMessage.description()[0];
        }
        return "";
    }
}
