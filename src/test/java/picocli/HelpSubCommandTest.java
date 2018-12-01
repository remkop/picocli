package picocli;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;
import static picocli.HelpTestUtil.usageString;
import static picocli.PicocliTestUtil.setOf;

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
                "  -h, --help   show help and exit%n" +
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
                "  -h, --help   show help and exit%n" +
                "Commands:%n" +
                "  subcommand%n");
        assertEquals(expected, actual);
    }

    @Command(name = "top", aliases = {"t", "tp"}, subcommands = {SubCommand1.class, SubCommand2.class}, description = "top level command")
    static class TopLevelCommand { }

    @Command(name = "sub", aliases = {"s", "sb"}, subcommands = {SubSubCommand.class}, description = "I'm subcommand No. 1!")
    static class SubCommand1 {}

    @Command(name = "sub2", aliases = {"s2", "sb2"}, subcommands = {SubSubCommand.class}, description = "I'm subcommand 2 but pretty good still")
    static class SubCommand2 {}

    @Command(name = "subsub", aliases = {"ss", "sbsb"}, description = "I'm like a 3rd rate command but great bang for your buck")
    static class SubSubCommand {}

    @Test
    public void testCommandAliasRegistrationByAnnotation() {
        CommandLine commandLine = new CommandLine(new TopLevelCommand());
        assertEquals(setOf("sub", "s", "sb", "sub2", "s2", "sb2"), commandLine.getSubcommands().keySet());

        CommandLine sub1 = commandLine.getSubcommands().get("sub");
        assertEquals(setOf("subsub", "ss", "sbsb"), sub1.getSubcommands().keySet());

        CommandLine sub2 = commandLine.getSubcommands().get("sub2");
        assertEquals(setOf("subsub", "ss", "sbsb"), sub2.getSubcommands().keySet());
    }

    @Test
    public void testCommandAliasAnnotationUsageHelp() {
        CommandLine commandLine = new CommandLine(new TopLevelCommand());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos), CommandLine.Help.defaultColorScheme(Help.Ansi.ON).commands(Help.Ansi.Style.underline)); // add underline
        
        String expected = Help.Ansi.ON.new Text(String.format("" +
                "Usage: @|bold,underline top|@ [COMMAND]%n" +
                "top level command%n" +
                "Commands:%n" +
                "  @|bold,underline sub|@, @|bold,underline s|@, @|bold,underline sb|@     I'm subcommand No. 1!%n" +
                "  @|bold,underline sub2|@, @|bold,underline s2|@, @|bold,underline sb2|@  I'm subcommand 2 but pretty good still%n")).toString();
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testCommandAliasAnnotationSubcommandUsageHelp() {
        CommandLine commandLine = new CommandLine(new TopLevelCommand());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.getSubcommands().get("sub").usage(new PrintStream(baos), CommandLine.Help.defaultColorScheme(Help.Ansi.ON).commands(Help.Ansi.Style.underline)); // add underline

        String expected = Help.Ansi.ON.new Text(String.format("" +
                "Usage: @|bold,underline top sub|@ [COMMAND]%n" +
                "I'm subcommand No. 1!%n" +
                "Commands:%n" +
                "  @|bold,underline subsub|@, @|bold,underline ss|@, @|bold,underline sbsb|@  I'm like a 3rd rate command but great bang for your buck%n")).toString();
        assertEquals(expected, baos.toString());
    }
}