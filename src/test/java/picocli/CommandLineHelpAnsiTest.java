package picocli;

import org.fusesource.jansi.AnsiConsole;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.*;
import static picocli.HelpTestUtil.usageString;

public class CommandLineHelpAnsiTest {
    private static final String LINESEP = System.getProperty("line.separator");

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @After
    public void after() {
        System.getProperties().remove("picocli.color.commands");
        System.getProperties().remove("picocli.color.options");
        System.getProperties().remove("picocli.color.parameters");
        System.getProperties().remove("picocli.color.optionParams");
    }

    @Test
    public void testTextWithMultipleStyledSections() {
        assertEquals("\u001B[1m<main class>\u001B[21m\u001B[0m [\u001B[33m-v\u001B[39m\u001B[0m] [\u001B[33m-c\u001B[39m\u001B[0m [\u001B[3m<count>\u001B[23m\u001B[0m]]",
                CommandLine.Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@ [@|italic <count>|@]]").toString());
    }

    @Test
    public void testTextAdjacentStyles() {
        assertEquals("\u001B[3m<commit\u001B[23m\u001B[0m\u001B[3m>\u001B[23m\u001B[0m%n",
                CommandLine.Help.Ansi.ON.new Text("@|italic <commit|@@|italic >|@%n").toString());
    }

    @Test
    public void testTextNoConversionWithoutClosingTag() {
        assertEquals("\u001B[3mabc\u001B[23m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|italic abc|@").toString());
        assertEquals("@|italic abc",                    CommandLine.Help.Ansi.ON.new Text("@|italic abc").toString());
    }

    @Test
    public void testTextNoConversionWithoutSpaceSeparator() {
        assertEquals("\u001B[3ma\u001B[23m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|italic a|@").toString());
        assertEquals("@|italic|@",                    CommandLine.Help.Ansi.ON.new Text("@|italic|@").toString());
        assertEquals("",                              CommandLine.Help.Ansi.ON.new Text("@|italic |@").toString());
    }

    @Test
    public void testPalette236ColorForegroundIndex() {
        assertEquals("\u001B[38;5;45mabc\u001B[39m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|fg(45) abc|@").toString());
    }

    @Test
    public void testPalette236ColorForegroundRgb() {
        int num = 16 + 36 * 5 + 6 * 5 + 5;
        assertEquals("\u001B[38;5;" + num + "mabc\u001B[39m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|fg(5;5;5) abc|@").toString());
    }

    @Test
    public void testPalette236ColorBackgroundIndex() {
        assertEquals("\u001B[48;5;77mabc\u001B[49m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|bg(77) abc|@").toString());
    }

    @Test
    public void testPalette236ColorBackgroundRgb() {
        int num = 16 + 36 * 3 + 6 * 3 + 3;
        assertEquals("\u001B[48;5;" + num + "mabc\u001B[49m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|bg(3;3;3) abc|@").toString());
    }

    @Test
    public void testAnsiEnabled() {
        assertTrue(CommandLine.Help.Ansi.ON.enabled());
        assertFalse(CommandLine.Help.Ansi.OFF.enabled());

        System.setProperty("picocli.ansi", "true");
        assertEquals(true, CommandLine.Help.Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "false");
        assertEquals(false, CommandLine.Help.Ansi.AUTO.enabled());

        System.clearProperty("picocli.ansi");
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        boolean isXterm   = System.getenv("TERM") != null && System.getenv("TERM").startsWith("xterm");
        boolean hasOsType = System.getenv("OSTYPE") != null; // null on Windows unless on Cygwin or MSYS
        boolean isAtty    = (isWindows && (isXterm || hasOsType)) // cygwin pseudo-tty
                || hasConsole();
        assertEquals((isAtty && (!isWindows || isXterm || hasOsType)) || isJansiConsoleInstalled(), CommandLine.Help.Ansi.AUTO.enabled());

        if (isWindows && !CommandLine.Help.Ansi.AUTO.enabled()) {
            AnsiConsole.systemInstall();
            assertTrue(CommandLine.Help.Ansi.AUTO.enabled());
            AnsiConsole.systemUninstall();
        }
    }

    private boolean hasConsole() {
        try { return System.class.getDeclaredMethod("console").invoke(null) != null; }
        catch (Throwable reflectionFailed) { return true; }
    }
    private static boolean isJansiConsoleInstalled() {
        try {
            Class<?> ansiConsole = Class.forName("org.fusesource.jansi.AnsiConsole");
            Field out = ansiConsole.getField("out");
            return out.get(null) == System.out;
        } catch (Exception reflectionFailed) {
            return false;
        }
    }

    @Test
    public void testSystemPropertiesOverrideDefaultColorScheme() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        // default color scheme
        assertEquals(ansi.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@..." + LINESEP),
                new CommandLine.Help(new App(), ansi).synopsis(0));

        System.setProperty("picocli.color.commands", "blue");
        System.setProperty("picocli.color.options", "green");
        System.setProperty("picocli.color.parameters", "cyan");
        System.setProperty("picocli.color.optionParams", "magenta");
        assertEquals(ansi.new Text("@|blue <main class>|@ [@|green -v|@] [@|green -c|@=@|magenta <count>|@] @|cyan FILE|@..." + LINESEP),
                new CommandLine.Help(new App(), ansi).synopsis(0));
    }

    @Test
    public void testSystemPropertiesOverrideExplicitColorScheme() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.ColorScheme explicit = new CommandLine.Help.ColorScheme(ansi)
                .commands(CommandLine.Help.Ansi.Style.faint, CommandLine.Help.Ansi.Style.bg_magenta)
                .options(CommandLine.Help.Ansi.Style.bg_red)
                .parameters(CommandLine.Help.Ansi.Style.reverse)
                .optionParams(CommandLine.Help.Ansi.Style.bg_green);
        // default color scheme
        assertEquals(ansi.new Text("@|faint,bg(magenta) <main class>|@ [@|bg(red) -v|@] [@|bg(red) -c|@=@|bg(green) <count>|@] @|reverse FILE|@..." + LINESEP),
                new CommandLine.Help(CommandLine.Model.CommandSpec.forAnnotatedObject(new App(), CommandLine.defaultFactory()), explicit).synopsis(0));

        System.setProperty("picocli.color.commands", "blue");
        System.setProperty("picocli.color.options", "blink");
        System.setProperty("picocli.color.parameters", "red");
        System.setProperty("picocli.color.optionParams", "magenta");
        assertEquals(ansi.new Text("@|blue <main class>|@ [@|blink -v|@] [@|blink -c|@=@|magenta <count>|@] @|red FILE|@..." + LINESEP),
                new CommandLine.Help(CommandLine.Model.CommandSpec.forAnnotatedObject(new App(), CommandLine.defaultFactory()), explicit).synopsis(0));
    }
    @Test
    public void testUsageWithCustomColorScheme() throws UnsupportedEncodingException {
        CommandLine.Help.ColorScheme scheme = new CommandLine.Help.ColorScheme(CommandLine.Help.Ansi.ON)
                .options(CommandLine.Help.Ansi.Style.bg_magenta).parameters(CommandLine.Help.Ansi.Style.bg_cyan).optionParams(CommandLine.Help.Ansi.Style.bg_yellow).commands(CommandLine.Help.Ansi.Style.reverse);
        class Args {
            @CommandLine.Parameters(description = "param desc") String[] params;
            @CommandLine.Option(names = "-x", description = "option desc") String[] options;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(new Args(), new PrintStream(baos, true, "UTF8"), scheme);
        String actual = baos.toString("UTF8");

        String expected = String.format("" +
                "Usage: @|reverse <main class>|@ [@|bg_magenta -x|@=@|bg_yellow <options>|@]... [@|bg_cyan <params>|@...]%n" +
                "      [@|bg_cyan <params>|@...]   param desc%n" +
                "  @|bg_magenta -x|@=@|bg_yellow <|@@|bg_yellow options>|@        option desc%n");
        assertEquals(CommandLine.Help.Ansi.ON.new Text(expected).toString(), actual);
    }

    @Test
    public void testAbreviatedSynopsis_withParameters() {
        @CommandLine.Command(abbreviateSynopsis = true)
        class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters
            File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals("<main class> [OPTIONS] [<files>...]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testAbreviatedSynopsis_withParameters_ANSI() {
        @CommandLine.Command(abbreviateSynopsis = true)
        class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters
            File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS] [@|yellow <files>|@...]" + LINESEP).toString(), help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withParameters() {
        @CommandLine.Command(separator = ":") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters
            File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c:<count>] [<files>...]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withParameters_ANSI() {
        @CommandLine.Command(separator = ":") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters
            File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@:@|italic <count>|@] [@|yellow <files>|@...]" + LINESEP),
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledParameters() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters(paramLabel = "FILE") File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>] [FILE...]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledParameters_ANSI() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters(paramLabel = "FILE") File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] [@|yellow FILE|@...]" + LINESEP),
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>] FILE..." + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters_ANSI() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--count", "-c"}) int count;
            @CommandLine.Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @CommandLine.Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@..." + LINESEP),
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptionsSeparately() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @CommandLine.Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @CommandLine.Option(names = {"--Verbose", "-V"}, required = true) boolean requiredVerbose;
            @CommandLine.Option(names = {"--Aaaa", "-A"}, required = true) boolean requiredABoolean;
            @CommandLine.Option(names = {"--Xxxx", "-X"}, required = true) Boolean requiredXBoolean;
            @CommandLine.Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals("<main class> -AVX [-avx] [-c=COUNT]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptionsSeparately_ANSI() {
        @CommandLine.Command(separator = "=") class App {
            @CommandLine.Option(names = {"--verbose", "-v"}) boolean verbose;
            @CommandLine.Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @CommandLine.Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @CommandLine.Option(names = {"--Verbose", "-V"}, required = true) boolean requiredVerbose;
            @CommandLine.Option(names = {"--Aaaa", "-A"}, required = true) boolean requiredABoolean;
            @CommandLine.Option(names = {"--Xxxx", "-X"}, required = true) Boolean requiredXBoolean;
            @CommandLine.Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text("@|bold <main class>|@ @|yellow -AVX|@ [@|yellow -avx|@] [@|yellow -c|@=@|italic COUNT|@]" + LINESEP),
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_firstLineLengthAdjustedForSynopsisHeading() {
        //Usage: small-test-program [-acorv!?] [--version] [-h <number>] [-p <file>|<folder>] [-d
//                 <folder> [<folder>]] [-i <includePattern>
//                 [<includePattern>...]]
        @CommandLine.Command(name="small-test-program", sortOptions = false, separator = " ")
        class App {
            @CommandLine.Option(names = "-a") boolean a;
            @CommandLine.Option(names = "-c") boolean c;
            @CommandLine.Option(names = "-o") boolean o;
            @CommandLine.Option(names = "-r") boolean r;
            @CommandLine.Option(names = "-v") boolean v;
            @CommandLine.Option(names = "-!") boolean exclamation;
            @CommandLine.Option(names = "-?") boolean question;
            @CommandLine.Option(names = {"--version"}) boolean version;
            @CommandLine.Option(names = {"--handle", "-h"}) int number;
            @CommandLine.Option(names = {"--ppp", "-p"}, paramLabel = "<file>|<folder>") File f;
            @CommandLine.Option(names = {"--ddd", "-d"}, paramLabel = "<folder>", arity="1..2") File[] d;
            @CommandLine.Option(names = {"--include", "-i"}, paramLabel = "<includePattern>") String pattern;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        String expected = "" +
                "Usage: small-test-program [-!?acorv] [--version] [-h <number>] [-i" + LINESEP +
                "                          <includePattern>] [-p <file>|<folder>] [-d <folder>" + LINESEP +
                "                          [<folder>]]..." + LINESEP;
        assertEquals(expected, help.synopsisHeading() + help.synopsis(help.synopsisHeadingLength()));

        help.commandSpec().usageMessage().synopsisHeading("Usage:%n");
        expected = "" +
                "Usage:" + LINESEP +
                "small-test-program [-!?acorv] [--version] [-h <number>] [-i <includePattern>]" + LINESEP +
                "                   [-p <file>|<folder>] [-d <folder> [<folder>]]..." + LINESEP;
        assertEquals(expected, help.synopsisHeading() + help.synopsis(help.synopsisHeadingLength()));
    }

    @Test
    public void testLongMultiLineSynopsisIndented() {
        @CommandLine.Command(name = "<best-app-ever>")
        class App {
            @CommandLine.Option(names = "--long-option-name", paramLabel = "<long-option-value>") int a;
            @CommandLine.Option(names = "--another-long-option-name", paramLabel = "<another-long-option-value>") int b;
            @CommandLine.Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
            @CommandLine.Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format(
                "<best-app-ever> [--another-long-option-name=<another-long-option-value>]%n" +
                        "                [--fourth-long-option-name=<fourth-long-option-value>]%n" +
                        "                [--long-option-name=<long-option-value>]%n" +
                        "                [--third-long-option-name=<third-long-option-value>]%n"),
                help.synopsis(0));
    }

    @Test
    public void testLongMultiLineSynopsisWithAtMarkIndented() {
        @CommandLine.Command(name = "<best-app-ever>")
        class App {
            @CommandLine.Option(names = "--long-option@-name", paramLabel = "<long-option-valu@@e>") int a;
            @CommandLine.Option(names = "--another-long-option-name", paramLabel = "^[<another-long-option-value>]") int b;
            @CommandLine.Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
            @CommandLine.Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format(
                "<best-app-ever> [--another-long-option-name=^[<another-long-option-value>]]%n" +
                        "                [--fourth-long-option-name=<fourth-long-option-value>]%n" +
                        "                [--long-option@-name=<long-option-valu@@e>]%n" +
                        "                [--third-long-option-name=<third-long-option-value>]%n"),
                help.synopsis(0));
    }

    @Test
    public void testLongMultiLineSynopsisWithAtMarkIndented_ANSI() {
        @CommandLine.Command(name = "<best-app-ever>")
        class App {
            @CommandLine.Option(names = "--long-option@-name", paramLabel = "<long-option-valu@@e>") int a;
            @CommandLine.Option(names = "--another-long-option-name", paramLabel = "^[<another-long-option-value>]") int b;
            @CommandLine.Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
            @CommandLine.Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
        }
        CommandLine.Help help = new CommandLine.Help(new App(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text(String.format(
                "@|bold <best-app-ever>|@ [@|yellow --another-long-option-name|@=@|italic ^[<another-long-option-value>]|@]%n" +
                        "                [@|yellow --fourth-long-option-name|@=@|italic <fourth-long-option-value>|@]%n" +
                        "                [@|yellow --long-option@-name|@=@|italic <long-option-valu@@e>|@]%n" +
                        "                [@|yellow --third-long-option-name|@=@|italic <third-long-option-value>|@]%n")),
                help.synopsis(0));
    }

    @Test
    public void testUsageMainCommand_NoAnsi() {
        String actual = usageString(Demo.mainCommand(), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_MAIN), actual);
    }

    @Test
    public void testUsageMainCommand_ANSI() {
        String actual = usageString(Demo.mainCommand(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_MAIN_ANSI)), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_NoAnsi() {
        String actual = usageString(new Demo.GitStatus(), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_GITSTATUS), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_ANSI() {
        String actual = usageString(new Demo.GitStatus(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_GITSTATUS_ANSI)), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_NoAnsi() {
        String actual = usageString(new Demo.GitCommit(), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_GITCOMMIT), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_ANSI() {
        String actual = usageString(new Demo.GitCommit(), CommandLine.Help.Ansi.ON);
        assertEquals(CommandLine.Help.Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_GITCOMMIT_ANSI)), actual);
    }

    @Test
    public void testTextConstructorPlain() {
        assertEquals("--NoAnsiFormat", CommandLine.Help.Ansi.ON.new Text("--NoAnsiFormat").toString());
    }

    @Test
    public void testTextConstructorWithStyle() {
        assertEquals("\u001B[1m--NoAnsiFormat\u001B[21m\u001B[0m", CommandLine.Help.Ansi.ON.new Text("@|bold --NoAnsiFormat|@").toString());
    }

    @Test
    public void testTextApply() {
        CommandLine.Help.Ansi.Text txt = CommandLine.Help.Ansi.ON.apply("--p", Arrays.<CommandLine.Help.Ansi.IStyle>asList(CommandLine.Help.Ansi.Style.fg_red, CommandLine.Help.Ansi.Style.bold));
        assertEquals(CommandLine.Help.Ansi.ON.new Text("@|fg(red),bold --p|@"), txt);
    }

    @Test
    public void testTextDefaultColorScheme() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.ColorScheme scheme = CommandLine.Help.defaultColorScheme(ansi);
        assertEquals(scheme.ansi().new Text("@|yellow -p|@"),      scheme.optionText("-p"));
        assertEquals(scheme.ansi().new Text("@|bold command|@"),  scheme.commandText("command"));
        assertEquals(scheme.ansi().new Text("@|yellow FILE|@"),   scheme.parameterText("FILE"));
        assertEquals(scheme.ansi().new Text("@|italic NUMBER|@"), scheme.optionParamText("NUMBER"));
    }

    @Test
    public void testTextSubString() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.Ansi.Text txt =   ansi.new Text("@|bold 01234|@").concat("56").concat("@|underline 7890|@");
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 7890|@"), txt.substring(0));
        assertEquals(ansi.new Text("@|bold 1234|@56@|underline 7890|@"), txt.substring(1));
        assertEquals(ansi.new Text("@|bold 234|@56@|underline 7890|@"), txt.substring(2));
        assertEquals(ansi.new Text("@|bold 34|@56@|underline 7890|@"), txt.substring(3));
        assertEquals(ansi.new Text("@|bold 4|@56@|underline 7890|@"), txt.substring(4));
        assertEquals(ansi.new Text("56@|underline 7890|@"), txt.substring(5));
        assertEquals(ansi.new Text("6@|underline 7890|@"), txt.substring(6));
        assertEquals(ansi.new Text("@|underline 7890|@"), txt.substring(7));
        assertEquals(ansi.new Text("@|underline 890|@"), txt.substring(8));
        assertEquals(ansi.new Text("@|underline 90|@"), txt.substring(9));
        assertEquals(ansi.new Text("@|underline 0|@"), txt.substring(10));
        assertEquals(ansi.new Text(""), txt.substring(11));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 7890|@"), txt.substring(0, 11));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 789|@"), txt.substring(0, 10));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 78|@"), txt.substring(0, 9));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 7|@"), txt.substring(0, 8));
        assertEquals(ansi.new Text("@|bold 01234|@56"), txt.substring(0, 7));
        assertEquals(ansi.new Text("@|bold 01234|@5"), txt.substring(0, 6));
        assertEquals(ansi.new Text("@|bold 01234|@"), txt.substring(0, 5));
        assertEquals(ansi.new Text("@|bold 0123|@"), txt.substring(0, 4));
        assertEquals(ansi.new Text("@|bold 012|@"), txt.substring(0, 3));
        assertEquals(ansi.new Text("@|bold 01|@"), txt.substring(0, 2));
        assertEquals(ansi.new Text("@|bold 0|@"), txt.substring(0, 1));
        assertEquals(ansi.new Text(""), txt.substring(0, 0));
        assertEquals(ansi.new Text("@|bold 1234|@56@|underline 789|@"), txt.substring(1, 10));
        assertEquals(ansi.new Text("@|bold 234|@56@|underline 78|@"), txt.substring(2, 9));
        assertEquals(ansi.new Text("@|bold 34|@56@|underline 7|@"), txt.substring(3, 8));
        assertEquals(ansi.new Text("@|bold 4|@56"), txt.substring(4, 7));
        assertEquals(ansi.new Text("5"), txt.substring(5, 6));
        assertEquals(ansi.new Text("@|bold 2|@"), txt.substring(2, 3));
        assertEquals(ansi.new Text("@|underline 8|@"), txt.substring(8, 9));

        CommandLine.Help.Ansi.Text txt2 =  ansi.new Text("@|bold abc|@@|underline DEF|@");
        assertEquals(ansi.new Text("@|bold abc|@@|underline DEF|@"), txt2.substring(0));
        assertEquals(ansi.new Text("@|bold bc|@@|underline DEF|@"), txt2.substring(1));
        assertEquals(ansi.new Text("@|bold abc|@@|underline DE|@"), txt2.substring(0,5));
        assertEquals(ansi.new Text("@|bold bc|@@|underline DE|@"), txt2.substring(1,5));
    }
    @Test
    public void testTextSplitLines() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.Ansi.Text[] all = {
                ansi.new Text("@|bold 012\n34|@").concat("5\nAA\n6").concat("@|underline 78\n90|@"),
                ansi.new Text("@|bold 012\r34|@").concat("5\rAA\r6").concat("@|underline 78\r90|@"),
                ansi.new Text("@|bold 012\r\n34|@").concat("5\r\nAA\r\n6").concat("@|underline 78\r\n90|@"),
        };
        for (CommandLine.Help.Ansi.Text text : all) {
            CommandLine.Help.Ansi.Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
        }
    }
    @Test
    public void testTextSplitLinesStartEnd() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.Ansi.Text[] all = {
                ansi.new Text("\n@|bold 012\n34|@").concat("5\nAA\n6").concat("@|underline 78\n90|@\n"),
                ansi.new Text("\r@|bold 012\r34|@").concat("5\rAA\r6").concat("@|underline 78\r90|@\r"),
                ansi.new Text("\r\n@|bold 012\r\n34|@").concat("5\r\nAA\r\n6").concat("@|underline 78\r\n90|@\r\n"),
        };
        for (CommandLine.Help.Ansi.Text text : all) {
            CommandLine.Help.Ansi.Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
        }
    }
    @Test
    public void testTextSplitLinesStartEndIntermediate() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.Ansi.Text[] all = {
                ansi.new Text("\n@|bold 012\n\n\n34|@").concat("5\n\n\nAA\n\n\n6").concat("@|underline 78\n90|@\n"),
                ansi.new Text("\r@|bold 012\r\r\r34|@").concat("5\r\r\rAA\r\r\r6").concat("@|underline 78\r90|@\r"),
                ansi.new Text("\r\n@|bold 012\r\n\r\n\r\n34|@").concat("5\r\n\r\n\r\nAA\r\n\r\n\r\n6").concat("@|underline 78\r\n90|@\r\n"),
        };
        for (CommandLine.Help.Ansi.Text text : all) {
            CommandLine.Help.Ansi.Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
        }
    }

    @Test
    public void testTextHashCode() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        assertEquals(ansi.new Text("a").hashCode(), ansi.new Text("a").hashCode());
        assertNotEquals(ansi.new Text("a").hashCode(), ansi.new Text("b").hashCode());
    }

    @Test
    public void testTextAppendString() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        assertEquals(ansi.new Text("a").append("xyz"), ansi.new Text("a").concat("xyz"));
    }

    @Test
    public void testTextAppendText() {
        CommandLine.Help.Ansi ansi = CommandLine.Help.Ansi.ON;
        CommandLine.Help.Ansi.Text xyz = ansi.new Text("xyz");
        assertEquals(ansi.new Text("a").append(xyz), ansi.new Text("a").concat(xyz));
    }

    @Test
    public void testStyleParseAllowsMissingClosingBrackets() {
        CommandLine.Help.Ansi.IStyle whiteBg = CommandLine.Help.Ansi.Style.parse("bg(white")[0];
        assertEquals(CommandLine.Help.Ansi.Style.bg_white.on(), whiteBg.on());

        CommandLine.Help.Ansi.IStyle blackFg = CommandLine.Help.Ansi.Style.parse("fg(black")[0];
        assertEquals(CommandLine.Help.Ansi.Style.fg_black.on(), blackFg.on());
    }

    @Test
    public void testColorSchemeDefaultConstructorHasAnsiAuto() {
        CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme();
        assertEquals(CommandLine.Help.Ansi.AUTO, colorScheme.ansi());
    }

    @Test
    public void testCommandLine_printVersionInfo_printsArrayOfPlainTextStrings() {
        @CommandLine.Command(version = {"Versioned Command 1.0", "512-bit superdeluxe", "(c) 2017"}) class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.OFF);
        String result = baos.toString();
        assertEquals(String.format("Versioned Command 1.0%n512-bit superdeluxe%n(c) 2017%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsSingleStringWithMarkup() {
        @CommandLine.Command(version = "@|red 1.0|@") class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.ON);
        String result = baos.toString();
        assertEquals(String.format("\u001B[31m1.0\u001B[39m\u001B[0m%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsArrayOfStringsWithMarkup() {
        @CommandLine.Command(version = {
                "@|yellow Versioned Command 1.0|@",
                "@|blue Build 12345|@",
                "@|red,bg(white) (c) 2017|@" })
        class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.ON);
        String result = baos.toString();
        assertEquals(String.format("" +
                "\u001B[33mVersioned Command 1.0\u001B[39m\u001B[0m%n" +
                "\u001B[34mBuild 12345\u001B[39m\u001B[0m%n" +
                "\u001B[31m\u001B[47m(c) 2017\u001B[49m\u001B[39m\u001B[0m%n"), result);
    }
    @Test
    public void testCommandLine_printVersionInfo_formatsArguments() {
        @CommandLine.Command(version = {"First line %1$s", "Second line %2$s", "Third line %s %s"}) class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        new CommandLine(new Versioned()).printVersionHelp(ps, CommandLine.Help.Ansi.OFF, "VALUE1", "VALUE2", "VALUE3");
        String result = baos.toString();
        assertEquals(String.format("First line VALUE1%nSecond line VALUE2%nThird line VALUE1 VALUE2%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_withMarkupAndParameterContainingMarkup() {
        @CommandLine.Command(version = {
                "@|yellow Versioned Command 1.0|@",
                "@|blue Build 12345|@%1$s",
                "@|red,bg(white) (c) 2017|@%2$s" })
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    static class MarkupVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() {
            return new String[] {
                    "@|yellow Versioned Command 1.0|@",
                    "@|blue Build 12345|@%1$s",
                    "@|red,bg(white) (c) 2017|@%2$s" };
        }
    }

    @Test
    public void testCommandLine_printVersionInfo_fromAnnotation_withMarkupAndParameterContainingMarkup() {
        @CommandLine.Command(versionProvider = MarkupVersionProvider.class)
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    @Test
    public void testCommandLine_printVersionInfo_usesProviderIfBothProviderAndStaticVersionInfoExist() {
        @CommandLine.Command(versionProvider = MarkupVersionProvider.class, version = "static version is ignored")
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    private void verifyVersionWithMarkup(CommandLine commandLine) {
        String[] args = {"@|bold VALUE1|@", "@|underline VALUE2|@", "VALUE3"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        commandLine.printVersionHelp(ps, CommandLine.Help.Ansi.ON, (Object[]) args);
        String result = baos.toString();
        assertEquals(String.format("" +
                "\u001B[33mVersioned Command 1.0\u001B[39m\u001B[0m%n" +
                "\u001B[34mBuild 12345\u001B[39m\u001B[0m\u001B[1mVALUE1\u001B[21m\u001B[0m%n" +
                "\u001B[31m\u001B[47m(c) 2017\u001B[49m\u001B[39m\u001B[0m\u001B[4mVALUE2\u001B[24m\u001B[0m%n"), result);
    }

}
