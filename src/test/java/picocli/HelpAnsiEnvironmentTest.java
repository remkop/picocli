package picocli;

import org.fusesource.jansi.AnsiConsole;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.Help.Ansi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.*;
import static picocli.TestUtil.usageString;

public class HelpAnsiEnvironmentTest {
    private static final String LINESEP = System.getProperty("line.separator");

    private static final String[] ANSI_ENVIRONMENT_VARIABLES = new String[] {
            "TERM", "OSTYPE", "NO_COLOR", "ANSICON", "CLICOLOR", "ConEmuANSI", "CLICOLOR_FORCE"
    };

    @Rule
    // allows tests to set any kind of properties they like, without having to individually roll them back
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

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
                Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@ [@|italic <count>|@]]").toString());
    }

    @Test
    public void testTextAdjacentStyles() {
        assertEquals("\u001B[3m<commit\u001B[23m\u001B[0m\u001B[3m>\u001B[23m\u001B[0m%n",
                Ansi.ON.new Text("@|italic <commit|@@|italic >|@%n").toString());
    }

    @Test
    public void testTextNoConversionWithoutClosingTag() {
        assertEquals("\u001B[3mabc\u001B[23m\u001B[0m", Ansi.ON.new Text("@|italic abc|@").toString());
        assertEquals("@|italic abc",                    Ansi.ON.new Text("@|italic abc").toString());
    }

    @Test
    public void testTextNoConversionWithoutSpaceSeparator() {
        assertEquals("\u001B[3ma\u001B[23m\u001B[0m", Ansi.ON.new Text("@|italic a|@").toString());
        assertEquals("@|italic|@",                    Ansi.ON.new Text("@|italic|@").toString());
        assertEquals("",                              Ansi.ON.new Text("@|italic |@").toString());
    }

    @Test
    public void testPalette236ColorForegroundIndex() {
        assertEquals("\u001B[38;5;45mabc\u001B[39m\u001B[0m", Ansi.ON.new Text("@|fg(45) abc|@").toString());
    }

    @Test
    public void testPalette236ColorForegroundRgb() {
        int num = 16 + 36 * 5 + 6 * 5 + 5;
        assertEquals("\u001B[38;5;" + num + "mabc\u001B[39m\u001B[0m", Ansi.ON.new Text("@|fg(5;5;5) abc|@").toString());
    }

    @Test
    public void testPalette236ColorBackgroundIndex() {
        assertEquals("\u001B[48;5;77mabc\u001B[49m\u001B[0m", Ansi.ON.new Text("@|bg(77) abc|@").toString());
    }

    @Test
    public void testPalette236ColorBackgroundRgb() {
        int num = 16 + 36 * 3 + 6 * 3 + 3;
        assertEquals("\u001B[48;5;" + num + "mabc\u001B[49m\u001B[0m", Ansi.ON.new Text("@|bg(3;3;3) abc|@").toString());
    }

    @Test
    public void testAnsiEnabled() {
        assertTrue(Ansi.ON.enabled());
        assertFalse(Ansi.OFF.enabled());

        System.setProperty("picocli.ansi", "tty");
        boolean hasConsole = Ansi.calcTTY() || Ansi.isPseudoTTY();
        assertEquals(hasConsole, Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "true");
        assertEquals(true, Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "false");
        assertEquals(false, Ansi.AUTO.enabled());

        System.clearProperty("picocli.ansi");
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        boolean isXterm   = System.getenv("TERM") != null && System.getenv("TERM").startsWith("xterm");
        boolean isCygwin  = System.getenv("TERM") != null && System.getenv("TERM").toLowerCase(Locale.ENGLISH).contains("cygwin");
        boolean hasOsType = System.getenv("OSTYPE") != null; // null on Windows unless on Cygwin or MSYS
        boolean isAtty    = (isWindows && (isXterm || hasOsType)) // cygwin pseudo-tty
                || hasConsole();
        assertEquals((isAtty && (!isWindows || isXterm || isCygwin || hasOsType)) || isJansiConsoleInstalled(), Ansi.AUTO.enabled());

        if (isWindows && !Ansi.AUTO.enabled()) {
            AnsiConsole.systemInstall();
            try {
                assertTrue(Ansi.AUTO.enabled());
            } finally {
                AnsiConsole.systemUninstall();
            }
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
        Ansi ansi = Ansi.ON;
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
        Ansi ansi = Ansi.ON;
        CommandLine.Help.ColorScheme explicit = new CommandLine.Help.ColorScheme.Builder(ansi)
                .commands(Ansi.Style.faint, Ansi.Style.bg_magenta)
                .options(Ansi.Style.bg_red)
                .parameters(Ansi.Style.reverse)
                .optionParams(Ansi.Style.bg_green).build();
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
        CommandLine.Help.ColorScheme scheme = new CommandLine.Help.ColorScheme.Builder(Ansi.ON)
                .options(Ansi.Style.bg_magenta).parameters(Ansi.Style.bg_cyan).optionParams(Ansi.Style.bg_yellow).commands(Ansi.Style.reverse).build();
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
                "  @|bg_magenta -x|@=@|bg_yellow <options>|@        option desc%n");
        assertEquals(Ansi.ON.new Text(expected).toString(), actual);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.ON);
        assertEquals(Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS] [@|yellow <files>|@...]" + LINESEP).toString(), help.synopsis(0));
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.ON);
        assertEquals(Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@:@|italic <count>|@] [@|yellow <files>|@...]" + LINESEP),
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.ON);
        assertEquals(Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] [@|yellow FILE|@...]" + LINESEP),
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.ON);
        assertEquals(Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@..." + LINESEP),
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.ON);
        assertEquals(Ansi.ON.new Text("@|bold <main class>|@ @|yellow -AVX|@ [@|yellow -avx|@] [@|yellow -c|@=@|italic COUNT|@]" + LINESEP),
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.OFF);
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
        CommandLine.Help help = new CommandLine.Help(new App(), Ansi.ON);
        assertEquals(Ansi.ON.new Text(String.format(
                "@|bold <best-app-ever>|@ [@|yellow --another-long-option-name|@=@|italic ^[<another-long-option-value>]|@]%n" +
                        "                [@|yellow --fourth-long-option-name|@=@|italic <fourth-long-option-value>|@]%n" +
                        "                [@|yellow --long-option@-name|@=@|italic <long-option-valu@@e>|@]%n" +
                        "                [@|yellow --third-long-option-name|@=@|italic <third-long-option-value>|@]%n")),
                help.synopsis(0));
    }

    @Test
    public void testUsageMainCommand_NoAnsi() {
        String actual = usageString(Demo.mainCommand(), Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_MAIN), actual);
    }

    @Test
    public void testUsageMainCommand_ANSI() {
        String actual = usageString(Demo.mainCommand(), Ansi.ON);
        assertEquals(Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_MAIN_ANSI)), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_NoAnsi() {
        String actual = usageString(new Demo.GitStatus(), Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_GITSTATUS), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_ANSI() {
        String actual = usageString(new Demo.GitStatus(), Ansi.ON);
        assertEquals(Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_GITSTATUS_ANSI)), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_NoAnsi() {
        String actual = usageString(new Demo.GitCommit(), Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_GITCOMMIT), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_ANSI() {
        String actual = usageString(new Demo.GitCommit(), Ansi.ON);
        assertEquals(Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_GITCOMMIT_ANSI)), actual);
    }

    @Test
    public void testTextConstructorPlain() {
        assertEquals("--NoAnsiFormat", Ansi.ON.new Text("--NoAnsiFormat").toString());
    }

    @Test
    public void testTextConstructorWithStyle() {
        assertEquals("\u001B[1m--NoAnsiFormat\u001B[21m\u001B[0m", Ansi.ON.new Text("@|bold --NoAnsiFormat|@").toString());
    }

    @Test
    public void testTextApply() {
        @SuppressWarnings("deprecation")
        Ansi.Text txt = Ansi.ON.apply("--p", Arrays.<Ansi.IStyle>asList(Ansi.Style.fg_red, Ansi.Style.bold));
        assertEquals(Ansi.ON.new Text("@|fg(red),bold --p|@"), txt);
    }

    @Test
    public void testTextDefaultColorScheme() {
        Ansi ansi = Ansi.ON;
        CommandLine.Help.ColorScheme scheme = CommandLine.Help.defaultColorScheme(ansi);
        assertEquals(scheme.ansi().new Text("@|yellow -p|@"),      scheme.optionText("-p"));
        assertEquals(scheme.ansi().new Text("@|bold command|@"),  scheme.commandText("command"));
        assertEquals(scheme.ansi().new Text("@|yellow FILE|@"),   scheme.parameterText("FILE"));
        assertEquals(scheme.ansi().new Text("@|italic NUMBER|@"), scheme.optionParamText("NUMBER"));
    }

    @Test
    public void testTextSubString() {
        Ansi ansi = Ansi.ON;
        Ansi.Text txt =   ansi.new Text("@|bold 01234|@").concat("56").concat("@|underline 7890|@");
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

        Ansi.Text txt2 =  ansi.new Text("@|bold abc|@@|underline DEF|@");
        assertEquals(ansi.new Text("@|bold abc|@@|underline DEF|@"), txt2.substring(0));
        assertEquals(ansi.new Text("@|bold bc|@@|underline DEF|@"), txt2.substring(1));
        assertEquals(ansi.new Text("@|bold abc|@@|underline DE|@"), txt2.substring(0,5));
        assertEquals(ansi.new Text("@|bold bc|@@|underline DE|@"), txt2.substring(1,5));
    }
    @Test
    public void testTextSplitLines() {
        Ansi ansi = Ansi.ON;
        Ansi.Text[] all = {
                ansi.new Text("@|bold 012\n34|@").concat("5\nAA\n6").concat("@|underline 78\n90|@"),
                ansi.new Text("@|bold 012\r34|@").concat("5\rAA\r6").concat("@|underline 78\r90|@"),
                ansi.new Text("@|bold 012\r\n34|@").concat("5\r\nAA\r\n6").concat("@|underline 78\r\n90|@"),
        };
        for (Ansi.Text text : all) {
            Ansi.Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
        }
    }
    @Test
    public void testTextSplitLinesEmpty() {
        Ansi ansi = Ansi.ON;
        Ansi.Text text = ansi.new Text("abc\n\n\n");
        Ansi.Text[] lines = text.splitLines();
        assertEquals(4, lines.length);
        assertEquals(ansi.new Text("abc"), lines[0]);
        assertEquals(ansi.new Text(""), lines[1]);
        assertEquals(ansi.new Text(""), lines[2]);
        assertEquals(ansi.new Text(""), lines[3]);
    }
    @Test
    public void testTextSplitLinesStartEnd() {
        Ansi ansi = Ansi.ON;
        Ansi.Text[] all = {
                ansi.new Text("\n@|bold 012\n34|@").concat("5\nAA\n6").concat("@|underline 78\n90|@\n"),
                ansi.new Text("\r@|bold 012\r34|@").concat("5\rAA\r6").concat("@|underline 78\r90|@\r"),
                ansi.new Text("\r\n@|bold 012\r\n34|@").concat("5\r\nAA\r\n6").concat("@|underline 78\r\n90|@\r\n"),
        };
        for (Ansi.Text text : all) {
            Ansi.Text[] lines = text.splitLines();
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
        Ansi ansi = Ansi.ON;
        Ansi.Text[] all = {
                ansi.new Text("\n@|bold 012\n\n\n34|@").concat("5\n\n\nAA\n\n\n6").concat("@|underline 78\n90|@\n"),
                ansi.new Text("\r@|bold 012\r\r\r34|@").concat("5\r\r\rAA\r\r\r6").concat("@|underline 78\r90|@\r"),
                ansi.new Text("\r\n@|bold 012\r\n\r\n\r\n34|@").concat("5\r\n\r\n\r\nAA\r\n\r\n\r\n6").concat("@|underline 78\r\n90|@\r\n"),
        };
        for (Ansi.Text text : all) {
            Ansi.Text[] lines = text.splitLines();
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
        Ansi ansi = Ansi.ON;
        assertEquals(ansi.new Text("a").hashCode(), ansi.new Text("a").hashCode());
        assertNotEquals(ansi.new Text("a").hashCode(), ansi.new Text("b").hashCode());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTextAppendString() {
        Ansi ansi = Ansi.ON;
        assertEquals(ansi.new Text("a").append("xyz"), ansi.new Text("a").concat("xyz"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTextAppendText() {
        Ansi ansi = Ansi.ON;
        Ansi.Text xyz = ansi.new Text("xyz");
        assertEquals(ansi.new Text("a").append(xyz), ansi.new Text("a").concat(xyz));
    }

    @Test
    public void testStyleParseAllowsMissingClosingBrackets() {
        Ansi.IStyle whiteBg = Ansi.Style.parse("bg(white")[0];
        assertEquals(Ansi.Style.bg_white.on(), whiteBg.on());

        Ansi.IStyle blackFg = Ansi.Style.parse("fg(black")[0];
        assertEquals(Ansi.Style.fg_black.on(), blackFg.on());
    }

    @Test
    public void testColorSchemeDefaultConstructorHasAnsiAuto() {
        CommandLine.Help.ColorScheme colorScheme = new CommandLine.Help.ColorScheme.Builder().build();
        assertEquals(Ansi.AUTO, colorScheme.ansi());
    }

    @Test
    public void testAnsiIsWindowsDependsOnSystemProperty() {
        System.setProperty("os.name", "MMIX");
        assertFalse(Ansi.isWindows());

        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());

        System.setProperty("os.name", "Windows 10 build 12345");
        assertTrue(Ansi.isWindows());
    }

    @Test
    public void testAnsiIsXtermDependsOnEnvironmentVariable() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse(Ansi.isXterm());

        environmentVariables.set("TERM", "random value");
        assertFalse(Ansi.isXterm());

        environmentVariables.set("TERM", "xterm");
        assertTrue(Ansi.isXterm());

        environmentVariables.set("TERM", "xterm asfasfasf");
        assertTrue(Ansi.isXterm());
    }

    @Test
    public void testAnsiIsCygwinDependsOnEnvironmentVariable() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse(Ansi.isCygwin());

        environmentVariables.set("TERM", "random value");
        assertFalse(Ansi.isCygwin());

        environmentVariables.set("TERM", "xterm");
        assertFalse(Ansi.isCygwin());

        environmentVariables.set("TERM", "xterm cygwin");
        assertTrue(Ansi.isCygwin());

        environmentVariables.set("TERM", "cygwin");
        assertTrue(Ansi.isCygwin());
    }

    @Test
    public void testAnsiHasOstypeDependsOnEnvironmentVariable() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse(Ansi.hasOsType());

        environmentVariables.set("OSTYPE", "");
        assertTrue(Ansi.hasOsType());

        environmentVariables.set("OSTYPE", "42");
        assertTrue(Ansi.hasOsType());
    }

    @Test
    public void testAnsiIsPseudoTtyDependsOnWindowsXtermOrCygwinOrOsType() {
        System.setProperty("os.name", "MMIX");
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("OSTYPE and XTERM are not set", Ansi.isPseudoTTY());

        System.setProperty("os.name", "Windows 10 build 12345");
        environmentVariables.set("OSTYPE", "222");
        environmentVariables.set("TERM", "xterm");
        assertTrue(Ansi.isPseudoTTY());

        System.setProperty("os.name", "MMIX");
        assertFalse("Not Windows", Ansi.isPseudoTTY());

        System.setProperty("os.name", "Windows 10 build 12345"); // restore
        assertTrue("restored", Ansi.isPseudoTTY());
        environmentVariables.clear("OSTYPE");
        assertTrue("Missing OSTYPE, but TERM=xterm", Ansi.isPseudoTTY());
        environmentVariables.set("TERM", "abcygwinxyz");
        assertTrue("Missing OSTYPE, but TERM=cygwin", Ansi.isPseudoTTY());

        environmentVariables.set("OSTYPE", "anything");
        assertTrue("restored", Ansi.isPseudoTTY());
        environmentVariables.clear("XTERM");
        assertTrue("Missing XTERM, but OSTYPE defined", Ansi.isPseudoTTY());
    }

    @Test
    public void testAnsiHintDisabledTrueIfCLICOLORZero() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "");
        assertFalse("Just defining CLICOLOR is not enough", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "1");
        assertFalse("CLICOLOR=1 is not enough", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "false");
        assertFalse("CLICOLOR=false is not enough", Ansi.hintDisabled());

        environmentVariables.set("CLICOLOR", "0");
        assertTrue("CLICOLOR=0 disables", Ansi.hintDisabled());
    }

    @Test
    public void testAnsiHintDisabledTrueIfConEmuANSIisOFF() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "");
        assertFalse("Just defining ConEmuANSI is not enough", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "0");
        assertFalse("ConEmuANSI=0 is not enough", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "false");
        assertFalse("ConEmuANSI=false is not enough", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "off");
        assertFalse("ConEmuANSI=off does not disable", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "Off");
        assertFalse("ConEmuANSI=Off does not disable", Ansi.hintDisabled());

        environmentVariables.set("ConEmuANSI", "OFF");
        assertTrue("ConEmuANSI=OFF disables", Ansi.hintDisabled());
    }


    @Test
    public void testAnsiHintEnbledTrueIfANSICONDefined() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintEnabled());

        environmentVariables.set("ANSICON", "");
        assertTrue("ANSICON defined without value", Ansi.hintEnabled());

        environmentVariables.set("ANSICON", "abc");
        assertTrue("ANSICON defined any value", Ansi.hintEnabled());
    }

    @Test
    public void testAnsiHintEnbledTrueIfCLICOLOROne() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "");
        assertFalse("Just defining CLICOLOR is not enough", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "0");
        assertFalse("CLICOLOR=0 is not enough", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "true");
        assertFalse("CLICOLOR=true is not enough", Ansi.hintEnabled());

        environmentVariables.set("CLICOLOR", "1");
        assertTrue("CLICOLOR=1 enables", Ansi.hintEnabled());
    }

    @Test
    public void testAnsiHintEnabledTrueIfConEmuANSIisON() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "");
        assertFalse("Just defining ConEmuANSI is not enough", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "1");
        assertFalse("ConEmuANSI=1 is not enough", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "true");
        assertFalse("ConEmuANSI=true is not enough", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "on");
        assertFalse("ConEmuANSI=on does not enables", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "On");
        assertFalse("ConEmuANSI=On does not enables", Ansi.hintEnabled());

        environmentVariables.set("ConEmuANSI", "ON");
        assertTrue("ConEmuANSI=ON enables", Ansi.hintEnabled());
    }

    @Test
    public void testAnsiForceEnabledTrueIfCLICOLOR_FORCEisDefinedAndNonZero() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.forceEnabled());

        environmentVariables.set("CLICOLOR_FORCE", "");
        assertTrue("Just defining CLICOLOR_FORCE is enough", Ansi.forceEnabled());

        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertTrue("CLICOLOR_FORCE=1 is enough", Ansi.forceEnabled());

        environmentVariables.set("CLICOLOR_FORCE", "0");
        assertFalse("CLICOLOR_FORCE=0 is not forced", Ansi.forceEnabled());
    }

    @Test
    public void testAnsiForceDisabledTrueIfNO_COLORDefined() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        assertFalse("no env vars set", Ansi.forceDisabled());

        environmentVariables.set("NO_COLOR", "");
        assertTrue("NO_COLOR defined without value", Ansi.forceDisabled());

        environmentVariables.set("NO_COLOR", "abc");
        assertTrue("NO_COLOR defined any value", Ansi.forceDisabled());
    }

    @Test
    public void testAnsiOnEnabled() {
        assertTrue(Ansi.ON.enabled());
    }

    @Test
    public void testAnsiOffDisabled() {
        assertFalse(Ansi.OFF.enabled());
    }

    @Test
    public void testAnsiAutoIfSystemPropertyPicocliAnsiCleared() {
        environmentVariables.set("CLICOLOR_FORCE", "1");

        System.clearProperty("picocli.ansi");
        assertTrue(Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoIfSystemPropertyPicocliAnsiIsAuto() {
        environmentVariables.set("CLICOLOR_FORCE", "1");

        System.setProperty("picocli.ansi", "auto");
        assertTrue(Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "Auto");
        assertTrue(Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "AUTO");
        assertTrue(Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiOffIfSystemPropertyPicocliAnsiIsNotAuto() {
        System.setProperty("picocli.ansi", "auto1");

        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertFalse(Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesForceEnabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("NO_COLOR", "");
        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertTrue(Ansi.forceDisabled());
        assertTrue(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());
        assertFalse("forceDisabled overrides forceEnabled", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesHintEnabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("NO_COLOR", "");
        environmentVariables.set("CLICOLOR", "1");
        assertTrue(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertTrue(Ansi.hintEnabled());
        assertFalse("forceDisabled overrides hintEnabled", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoForcedEnabledOverridesHintDisabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("CLICOLOR", "0");
        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertFalse(Ansi.forceDisabled());
        assertTrue(Ansi.hintDisabled());
        assertTrue(Ansi.forceEnabled());
        assertFalse(Ansi.hintEnabled());
        assertTrue("forceEnabled overrides hintDisabled", Ansi.AUTO.enabled());

        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("ConEmuANSI", "OFF");
        environmentVariables.set("CLICOLOR_FORCE", "1");
        assertFalse(Ansi.forceDisabled());
        assertTrue(Ansi.hintDisabled());
        assertTrue(Ansi.forceEnabled());
        assertFalse(Ansi.hintEnabled());
        assertTrue("forceEnabled overrides hintDisabled 2", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoJansiConsoleInstalledOverridesHintDisabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("CLICOLOR", "0"); // hint disabled
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        assertFalse(Ansi.isPseudoTTY());
        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertTrue(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        assertFalse(Ansi.isJansiConsoleInstalled());
        AnsiConsole.systemInstall();
        try {
            assertTrue(Ansi.isJansiConsoleInstalled());
            assertTrue(Ansi.AUTO.enabled());
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    @Test
    public void testAnsiAutoHintDisabledOverridesHintEnabled() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("CLICOLOR", "0"); // hint disabled
        environmentVariables.set("ANSICON", "1"); // hint enabled
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        environmentVariables.set("TERM", "xterm"); // fake Cygwin
        assertTrue(Ansi.isPseudoTTY());
        environmentVariables.set("TERM", "cygwin"); // fake Cygwin
        assertTrue(Ansi.isPseudoTTY());

        assertFalse(Ansi.isJansiConsoleInstalled());

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertTrue(Ansi.hintDisabled());
        assertTrue(Ansi.hintEnabled());

        assertFalse("Disabled overrides enabled", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoDisabledIfNoTty() {
        if (Ansi.isTTY()) { return; } //
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        assertFalse(Ansi.isPseudoTTY());
        assertFalse(Ansi.isJansiConsoleInstalled());

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        assertFalse("Must have TTY if no JAnsi", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoEnabledIfNotWindows() {
        if (!Ansi.isTTY()) { return; } // needs TTY for this test
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        System.setProperty("os.name", "MMIX");
        assertFalse(Ansi.isWindows());
        assertFalse(Ansi.isPseudoTTY()); // TODO Mock this?
        assertFalse(Ansi.isJansiConsoleInstalled());

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        assertTrue("If have TTY, enabled on non-Windows", Ansi.AUTO.enabled());
    }

    @Test
    public void testAnsiAutoEnabledIfWindowsPseudoTTY() {
        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        System.setProperty("os.name", "Windows");
        assertTrue(Ansi.isWindows());
        assertFalse(Ansi.isJansiConsoleInstalled());

        assertFalse(Ansi.forceDisabled());
        assertFalse(Ansi.forceEnabled());
        assertFalse(Ansi.hintDisabled());
        assertFalse(Ansi.hintEnabled());

        environmentVariables.set("TERM", "xterm");
        assertTrue(Ansi.isPseudoTTY());
        assertTrue("If have Cygwin pseudo-TTY, enabled on Windows", Ansi.AUTO.enabled());

        environmentVariables.set("TERM", "cygwin");
        assertTrue(Ansi.isPseudoTTY());
        assertTrue("If have Cygwin pseudo-TTY, enabled on Windows", Ansi.AUTO.enabled());

        environmentVariables.clear(ANSI_ENVIRONMENT_VARIABLES);
        environmentVariables.set("OSTYPE", "Windows");
        assertTrue(Ansi.isPseudoTTY());
        assertTrue("If have MSYS pseudo-TTY, enabled on Windows", Ansi.AUTO.enabled());
    }

    @Test
    public void Palette256ColorEquals() {
        Ansi.Palette256Color palette256Color = new Ansi.Palette256Color(true, "255;255;255");
        assertEquals(palette256Color, new Ansi.Palette256Color(true, "255;255;255"));
    }

    @Test
    public void Palette256ColorEqualHashCode() {
        Ansi.Palette256Color palette256Color = new Ansi.Palette256Color(true, "255;255;255");
        assertEquals(palette256Color.hashCode(), new Ansi.Palette256Color(true, "255;255;255").hashCode());
    }
}
