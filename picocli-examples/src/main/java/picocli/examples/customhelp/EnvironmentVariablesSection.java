package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.IHelpSectionRenderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This example demonstrates how to add a section to the usage help message.
 *
 * As requested in https://github.com/remkop/picocli/issues/856
 */
@CommandLine.Command(name = "showenv", mixinStandardHelpOptions = true,
        version = "showenv 1.0",
        description = "Demonstrates a usage help message with " +
                "an additional section for environment variables.")
public class EnvironmentVariablesSection {

    static final String SECTION_KEY_ENV_HEADING = "environmentVariablesHeading";
    static final String SECTION_KEY_ENV_DETAILS = "environmentVariables";

    public static void main(String[] args) {
    Map<String, String> env = new LinkedHashMap<>();
    env.put("FOO_CREATOR", "The foo's creator");
    env.put("BAR_CREATOR", "The bar's creator");
    env.put("XYZ", "xxxx yyyy zzz");

    CommandLine cmd = new CommandLine(new EnvironmentVariablesSection());
        installRenderers(env, cmd);
        cmd.usage(System.out);
    }

    private static void installRenderers(final Map<String, String> env, CommandLine cmd) {
        cmd.getHelpSectionMap().put(SECTION_KEY_ENV_HEADING, new IHelpSectionRenderer() {
            @Override public String render(CommandLine.Help help) {
                return String.format("Environment Variables:%n");
            }
        });
        cmd.getHelpSectionMap().put(SECTION_KEY_ENV_DETAILS, new EnvironmentVariablesRenderer(env));

        // @since 4.1: replace the above with this:
        //cmd.getHelpSectionMap().put(SECTION_KEY_ENV_HEADING, help -> help.createHeading("Environment Variables:%n"));
        //cmd.getHelpSectionMap().put(SECTION_KEY_ENV_DETAILS, help -> help.createTextTable(env).toString());

        // finally, register the section keys
        cmd.setHelpSectionKeys(insertKey(cmd.getHelpSectionKeys()));
    }

    private static List<String> insertKey(List<String> helpSectionKeys) {
        // find the place to insert the new sections: before the footer heading
        int index = helpSectionKeys.indexOf(CommandLine.Model.UsageMessageSpec.SECTION_KEY_FOOTER_HEADING);
        List<String> result = new ArrayList<>(helpSectionKeys);
        result.add(index, SECTION_KEY_ENV_HEADING);
        result.add(index + 1, SECTION_KEY_ENV_DETAILS);
        return result;
    }
}

class EnvironmentVariablesRenderer implements IHelpSectionRenderer {
    private final Map<String, String> env;

    public EnvironmentVariablesRenderer(Map<String, String> env) {
        this.env = env;
    }

    //@Override
    public String render(CommandLine.Help help) {
        if (env.isEmpty()) { return ""; }
        int keyLength = maxLength(env.keySet());
        TextTable textTable = TextTable.forColumns(help.colorScheme(),
                new Column(keyLength + 3, 2, Column.Overflow.SPAN),
                new Column(width(help) - (keyLength + 3), 2, Column.Overflow.WRAP));
        textTable.setAdjustLineBreaksForWideCJKCharacters(adjustCJK(help));

        for (Map.Entry<String, String> entry : env.entrySet()) {
            textTable.addRowValues(String.format(entry.getKey()), String.format(entry.getValue()));
        }
        return textTable.toString();
    }

    private boolean adjustCJK(CommandLine.Help help) {
        return help.commandSpec().usageMessage().adjustLineBreaksForWideCJKCharacters();
    }

    private int width(CommandLine.Help help) {
        return help.commandSpec().usageMessage().width();
    }

    private int maxLength(Set<String> keySet) {
        int result = 0;
        for (String k : keySet) { result = Math.max(result, k.length()); }
        return result;
    }
}
