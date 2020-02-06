package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.Column.Overflow;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.IHelpSectionRenderer;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;

/**
 * This example demonstrates how to customize a section of the usage help message.
 * It replaces the standard command list with a custom list that displays
 * not just the immediate subcommands but the full hierarchy of subcommands:
 * <pre>
 * Usage: showall [-hV] [COMMAND]
 * Demonstrates a usage help message that shows not just the subcommands of this
 * command, but also the nested sub-subcommands.
 *   -h, --help      Show this help message and exit.
 *   -V, --version   Print version information and exit.
 * Commands:
 *   sub1           subcommand1 of showall
 *     sub1sub1     subcommand1 of subcommand1 of showall
 *     sub1sub2     subcommand2 of subcommand1 of showall
 *   sub2           subcommand2 of showall
 *     sub2sub1     subcommand1 of subcommand2 of showall
 * </pre>
 *
 * As requested in https://github.com/remkop/picocli/issues/566
 */
@Command(name = "showall", mixinStandardHelpOptions = true,
        version = "from picocli 3.9",
        description = "Demonstrates a usage help message that shows " +
                "not just the subcommands of this command, " +
                "but also the nested sub-subcommands.",
        subcommands = {Subcommand1.class, Subcommand2.class} )
public class ShowCommandHierarchy {
    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ShowCommandHierarchy());
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, new MyCommandListRenderer());
        cmd.usage(System.out);
    }
}

class MyCommandListRenderer implements IHelpSectionRenderer {
    //@Override
    public String render(Help help) {
        CommandSpec spec = help.commandSpec();
        if (spec.subcommands().isEmpty()) { return ""; }

        // prepare layout: two columns
        // the left column overflows, the right column wraps if text is too long
        TextTable textTable = TextTable.forColumns(help.colorScheme(),
                new Column(15, 2, Overflow.SPAN),
                new Column(spec.usageMessage().width() - 15, 2, Overflow.WRAP));
        textTable.setAdjustLineBreaksForWideCJKCharacters(spec.usageMessage().adjustLineBreaksForWideCJKCharacters());

        for (CommandLine subcommand : spec.subcommands().values()) {
            addHierarchy(subcommand, textTable, "");
        }
        return textTable.toString();
    }

    private void addHierarchy(CommandLine cmd, TextTable textTable, String indent) {
        // create comma-separated list of command name and aliases
        String names = cmd.getCommandSpec().names().toString();
        names = names.substring(1, names.length() - 1); // remove leading '[' and trailing ']'

        // command description is taken from header or description
        String description = description(cmd.getCommandSpec().usageMessage());

        // add a line for this command to the layout
        textTable.addRowValues(indent + names, description);

        // add its subcommands (if any)
        for (CommandLine sub : cmd.getSubcommands().values()) {
            addHierarchy(sub, textTable, indent + "  ");
        }
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

@Command(name = "sub1", description = "subcommand1 of showall",
        subcommands = {Subcommand1Sub1.class, Subcommand1Sub2.class})
class Subcommand1 {}

@Command(name = "sub2", description = "subcommand2 of showall",
        subcommands = {Subcommand2Sub1.class})
class Subcommand2 {}

@Command(name = "sub1sub1", description = "subcommand1 of subcommand1 of showall")
class Subcommand1Sub1 {}

@Command(name = "sub1sub2", description = "subcommand2 of subcommand1 of showall")
class Subcommand1Sub2 {}

@Command(name = "sub2sub1", description = "subcommand1 of subcommand2 of showall")
class Subcommand2Sub1 {}
