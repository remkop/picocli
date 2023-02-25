package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;
import static picocli.TestUtil.usageString;
import static picocli.TestUtil.setOf;

public class HelpSubCommandTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

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
        commandLine.usage(new PrintStream(baos),
                new Help.ColorScheme.Builder(CommandLine.Help.defaultColorScheme(Help.Ansi.ON))
                        .commands(Help.Ansi.Style.underline)
                        .build()); // add underline

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
        commandLine.getSubcommands().get("sub").usage(new PrintStream(baos),
                new CommandLine.Help.ColorScheme.Builder(CommandLine.Help.defaultColorScheme(Help.Ansi.ON))
                        .commands(Help.Ansi.Style.underline)
                        .build()); // add underline

        String expected = Help.Ansi.ON.new Text(String.format("" +
                "Usage: @|bold,underline top sub|@ [COMMAND]%n" +
                "I'm subcommand No. 1!%n" +
                "Commands:%n" +
                "  @|bold,underline subsub|@, @|bold,underline ss|@, @|bold,underline sbsb|@  I'm like a 3rd rate command but great bang for your buck%n")).toString();
        assertEquals(expected, baos.toString());
    }

    @Command(name = "customHelp", helpCommand = true)
    @SuppressWarnings("deprecation")
    static class LegacyCustomHelpCommand implements IHelpCommandInitializable, Runnable {
        private CommandLine helpCommandLine;
        private Help.Ansi ansi;
        private PrintStream out;
        private PrintStream err;

        public void init(CommandLine helpCommandLine, Help.Ansi ansi, PrintStream out, PrintStream err) {
            this.helpCommandLine = helpCommandLine;
            this.ansi = ansi;
            this.out = out;
            this.err = err;
        }

        public void run() {
            out.println("Hi, ansi is " + ansi);
            err.println("Hello, ansi is " + ansi);
        }
    }
    @Test
    public void testLegacyCustomHelpCommand() {
        @Command(subcommands = LegacyCustomHelpCommand.class)
        class App implements Runnable {
            public void run() { }
        }

        int exitCode = new CommandLine(new App())
                .setExecutionStrategy(new RunLast())
                .setOut(CommandLine.newPrintWriter(System.out, getStdoutEncoding()))
                .setErr(CommandLine.newPrintWriter(System.err, getStderrEncoding()))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("customHelp");

        assertEquals(0, exitCode);
        assertEquals(String.format("Hi, ansi is OFF%n"), systemOutRule.getLog());
        assertEquals(String.format("Hello, ansi is OFF%n"), systemErrRule.getLog());
    }

    @Command(name = "newCustomHelp", helpCommand = true)
    static class CustomHelpCommand implements IHelpCommandInitializable2, Runnable {
        private CommandLine helpCommandLine;
        private Help.ColorScheme colorScheme;
        private PrintWriter outWriter;
        private PrintWriter errWriter;

        public void init(CommandLine helpCommandLine, Help.ColorScheme colorScheme, PrintWriter outWriter, PrintWriter errWriter) {
            this.helpCommandLine = helpCommandLine;
            this.colorScheme = colorScheme;
            this.outWriter = outWriter;
            this.errWriter = errWriter;
        }

        public void run() {
            outWriter.println("Hi, colorScheme.ansi is " + colorScheme.ansi());
            errWriter.println("Hello, colorScheme.ansi is " + colorScheme.ansi());
        }
    }
    @Test
    public void testCustomHelpCommand() {
        @Command(subcommands = CustomHelpCommand.class)
        class App implements Runnable {
            public void run() { }
        }

        int exitCode = new CommandLine(new App())
                .setExecutionStrategy(new RunLast())
                .setOut(CommandLine.newPrintWriter(System.out, getStdoutEncoding()))
                .setErr(CommandLine.newPrintWriter(System.err, getStderrEncoding()))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("newCustomHelp");

        assertEquals(0, exitCode);
        assertEquals(String.format("Hi, colorScheme.ansi is OFF%n"), systemOutRule.getLog());
        assertEquals(String.format("Hello, colorScheme.ansi is OFF%n"), systemErrRule.getLog());
    }

    @Test
    public void testHelpSubcommandWithValidCommand() {
        @Command(subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("help", "sub");

        String expected = String.format("" +
                "Usage: <main class> sub%n" +
                "This is a subcommand%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithCaseInsensitiveValidCommand() {
        @Command(subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .setSubcommandsCaseInsensitive(true)
                .execute("help", "SUB");

        String expected = String.format("" +
                "Usage: <main class> sub%n" +
                "This is a subcommand%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithAbbreviatedValidCommand() {
        @Command(subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .setAbbreviatedSubcommandsAllowed(true)
                .execute("help", "s");

        String expected = String.format("" +
                "Usage: <main class> sub%n" +
                "This is a subcommand%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithAbbreviatedCaseInsensitiveValidCommand() {
        @Command(subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .setAbbreviatedSubcommandsAllowed(true)
                .setSubcommandsCaseInsensitive(true)
                .execute("help", "S");

        String expected = String.format("" +
                "Usage: <main class> sub%n" +
                "This is a subcommand%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithInvalidCommand() {
        @Command(mixinStandardHelpOptions = true, subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setErr(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("help", "abcd");

        String expected = String.format("" +
                "Unknown subcommand 'abcd'.%n" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Display help information about the specified command.%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithCaseSensitiveInvalidCommand() {
        @Command(mixinStandardHelpOptions = true, subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setErr(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("help", "SUB");

        String expected = String.format("" +
                "Unknown subcommand 'SUB'.%n" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Display help information about the specified command.%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithHelpOption() {
        @Command(subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("help", "-h");

        String expected = String.format("" +
                "Display help information about the specified command.%n" +
                "%n" +
                "Usage: <main class> help [-h] [COMMAND]%n" +
                "%n" +
                "When no COMMAND is given, the usage help for the main command is displayed.%n" +
                "If a COMMAND is specified, the help for that command is shown.%n" +
                "%n" +
                "      [COMMAND]   The COMMAND to display the usage help message for.%n" +
                "  -h, --help      Show usage help for the help command and exit.%n");
        assertEquals(expected, sw.toString());

        sw = new StringWriter();
        new CommandLine(new App()).getSubcommands().get("help").usage(new PrintWriter(sw));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithoutCommand() {
        @Command(mixinStandardHelpOptions = true, subcommands = {HelpTest.Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        StringWriter sw = new StringWriter();
        new CommandLine(new App())
                .setOut(new PrintWriter(sw))
                .setColorScheme(Help.defaultColorScheme(Help.Ansi.OFF))
                .execute("help");

        String expected = String.format("" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Display help information about the specified command.%n");
        assertEquals(expected, sw.toString());

        sw = new StringWriter();
        new CommandLine(new App()).usage(new PrintWriter(sw), Help.Ansi.OFF);
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandRunDoesNothingIfParentNotSet() {
        HelpCommand cmd = new HelpCommand();
        cmd.run();
        assertEquals("", this.systemOutRule.getLog());
    }

    @SuppressWarnings({"deprecation"})
    @Test
    public void testHelpSubcommandRunPrintsParentUsageIfParentSet() {
        HelpCommand cmd = new HelpCommand();
        CommandLine help = new CommandLine(cmd);
        CommandSpec spec = CommandSpec.create().name("parent");
        spec.usageMessage().description("the parent command");
        spec.addSubcommand("parent", help);
        new CommandLine(spec); // make sure parent spec has a CommandLine

        cmd.init(help, Help.Ansi.OFF, System.out, System.err);
        cmd.run();
        String expected = String.format("" +
                "Usage: parent [COMMAND]%n" +
                "the parent command%n" +
                "Commands:%n" +
                "  parent  Display help information about the specified command.%n");
        assertEquals(expected, this.systemOutRule.getLog());
    }

    @Test
    public void testHelpSubcommand2RunPrintsParentUsageIfParentSet() {
        HelpCommand cmd = new HelpCommand();
        CommandLine help = new CommandLine(cmd);
        CommandSpec spec = CommandSpec.create().name("parent");
        spec.usageMessage().description("the parent command");
        spec.addSubcommand("parent", help);
        new CommandLine(spec); // make sure parent spec has a CommandLine

        cmd.init(help, Help.defaultColorScheme(Help.Ansi.OFF), new PrintWriter(System.out), new PrintWriter(System.err));
        cmd.run();
        String expected = String.format("" +
                "Usage: parent [COMMAND]%n" +
                "the parent command%n" +
                "Commands:%n" +
                "  parent  Display help information about the specified command.%n");
        assertEquals(expected, this.systemOutRule.getLog());
    }

    @Test
    public void testUsageHelpForNestedSubcommands() {
        @Command(name = "subsub", mixinStandardHelpOptions = true) class SubSub { }
        @Command(name = "sub", subcommands = {SubSub.class}) class Sub { }
        @Command(name = "main", subcommands = {Sub.class}) class App { }

        CommandLine app = new CommandLine(new App(), new InnerClassFactory(this));
        //ParseResult result = app.parseArgs("sub", "subsub", "--help");
        //CommandLine.printHelpIfRequested(result);
        CommandLine subsub = app.getSubcommands().get("sub").getSubcommands().get("subsub");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        subsub.usage(new PrintStream(baos), Help.Ansi.OFF);

        String expected = String.format("" +
                "Usage: main sub subsub [-hV]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testUsageTextWithHiddenSubcommand() {
        @Command(name = "foo", description = "This is a visible subcommand") class Foo { }
        @Command(name = "bar", description = "This is a hidden subcommand", hidden = true) class Bar { }
        @Command(name = "app", subcommands = {Foo.class, Bar.class}) class App { }

        CommandLine app = new CommandLine(new App(), new InnerClassFactory(this));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.usage(new PrintStream(baos));

        String expected = format("" +
                "Usage: app [COMMAND]%n" +
                "Commands:%n" +
                "  foo  This is a visible subcommand%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testUsageNoHeaderIfAllSubcommandHidden() {
        @Command(name = "foo", description = "This is a foo sub-command", hidden = true) class Foo { }
        @Command(name = "bar", description = "This is a foo sub-command", hidden = true) class Bar { }
        @Command(name = "app", abbreviateSynopsis = true) class App { }

        CommandLine app = new CommandLine(new App())
                .addSubcommand("foo", new Foo())
                .addSubcommand("bar", new Bar());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.usage(new PrintStream(baos));

        String expected = format("" +
                "Usage: app [COMMAND]%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testHelpAllSubcommands() {
        @Command(name = "foo", description = "This is a visible subcommand") class Foo { }
        @Command(name = "bar", description = "This is a hidden subcommand", hidden = true) class Bar { }
        @Command(name = "app", subcommands = {Foo.class, Bar.class}) class App { }

        CommandLine app = new CommandLine(new App(), new InnerClassFactory(this));
        Help help = app.getHelp();
        assertEquals(2, help.allSubcommands().size());
        assertEquals(new HashSet<String>(Arrays.asList("foo", "bar")), help.allSubcommands().keySet());

        assertEquals(1, help.subcommands().size());
        assertEquals(new HashSet<String>(Arrays.asList("foo")), help.subcommands().keySet());
    }

    @Command(name = "import-from-excel",
        aliases = { "import-from-xls", "import-from-xlsx", "import-from-csv", "import-from-txt" },
        description = "Imports the data from various excel files (xls, xlsx, csv or even txt).")
    static class ImportCommand implements Runnable {

        @Parameters(arity = "1..*")
        public List<String> files;

        public void run() {
            System.out.println("ImportCommand.run()");
        }
    }
    @Test
    public void testIssue1870StringIndexOutOfBounds() {
        @Command(name = "top", subcommands = ImportCommand.class) class Top { }
        String actual = usageString(new CommandLine(new Top()), Help.Ansi.OFF);
        String expected = String.format("" +
            "Usage: top [COMMAND]%n" +
            "Commands:%n" +
            "  import-from-excel, import-from-xls, import-from-xlsx, import-from-csv,%n" +
            "    import-from-txt%n" +
            "                                            Imports the data from various excel%n" +
            "                                              files (xls, xlsx, csv or even%n" +
            "                                              txt).%n");
        assertEquals(expected, actual);
    }
}
