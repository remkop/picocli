package picocli;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;
import static picocli.HelpTestUtil.usageString;


public class HelpSubCommandTest {

    @Test
    public void testShowSynopsisUsageWithCommandOption() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());

        // adding a subcommand should show "COMMAND" option to the help synopsis
        spec.addSubcommand("subcommand", CommandSpec.create());

        CommandLine commandLine = new CommandLine(spec);

        String actual = usageString(commandLine, Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-h] [COMMAND]%n" +
                "  -h, --help                  show help and exit%n" +
                "Commands:%n" +
                "  subcommand%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowAbbreviatedSynopsisUsageWithCommandOption() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());

        // using abbreviated synopsis
        spec.usageMessage().abbreviateSynopsis(true);

        // adding a subcommand should show "COMMAND" option to the help synopsis
        spec.addSubcommand("subcommand", CommandSpec.create());

        CommandLine commandLine = new CommandLine(spec);

        String actual = usageString(commandLine, Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [OPTIONS] [COMMAND]%n" +
                "  -h, --help                  show help and exit%n" +
                "Commands:%n" +
                "  subcommand%n");
        assertEquals(expected, actual);
    }

}