package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.IHelpSectionRenderer;
import picocli.CommandLine.Model.UsageMessageSpec;

import java.util.LinkedHashMap;

/**
 * Example showing how to show/hide commands on specific conditions.
 *
 * @see <a href="https://github.com/remkop/picocli/issues/1052">https://github.com/remkop/picocli/issues/1052</a>
 */
public class ConditionallyShowSubcommands {

    @Command(name = "myApp")
    static class App {

        @Command(name = "do-something", description = "Do something useful.")
        void doSomething() {
        }

        @Command(name = "generate-cmd-launcher",
                description = "Generate a command prompt launcher (${PARENT-COMMAND-NAME}.cmd).")
        void generateCmdLauncher() {
        }
    }

    public static void main(String[] args) {

        IHelpSectionRenderer renderer = (Help help) -> {
            LinkedHashMap<String, Help> map = new LinkedHashMap<>(help.subcommands());
            if (!isWindows()) {
                map.remove("generate-cmd-launcher");
            }
            return help.commandList(map);
        };

        CommandLine cmd = new CommandLine(new App());
        cmd.getHelpSectionMap().put(UsageMessageSpec.SECTION_KEY_COMMAND_LIST, renderer);
        System.out.println(cmd.getUsageMessage());
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}