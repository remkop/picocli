package picocli;

import org.fusesource.jansi.AnsiConsole;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Help.Ansi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpAnsiTest {
    private static final String LINESEP = System.getProperty("line.separator");

    private static final String[] ANSI_ENVIRONMENT_VARIABLES = new String[] {
            "TERM", "OSTYPE", "NO_COLOR", "ANSICON", "CLICOLOR", "ConEmuANSI", "CLICOLOR_FORCE"
    };
    private static final int TERM = 0;
    private static final int OSTYPE = 1;
    private static final int NO_COLOR = 2;
    private static final int ANSICON = 3;
    private static final int CLICOLOR = 4;
    private static final int ConEmuANSI = 5;
    private static final int CLICOLOR_FORCE = 6;

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
    public void testSystemPropertiesOverrideDefaultColorScheme() throws Exception {
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

        restoreSystemProperties(() -> {
            System.setProperty("picocli.color.commands", "blue");
            System.setProperty("picocli.color.options", "green");
            System.setProperty("picocli.color.parameters", "cyan");
            System.setProperty("picocli.color.optionParams", "magenta");
            assertEquals(ansi.new Text("@|blue <main class>|@ [@|green -v|@] [@|green -c|@=@|magenta <count>|@] @|cyan FILE|@..." + LINESEP),
                new CommandLine.Help(new App(), ansi).synopsis(0));
        });
    }

    @Test
    public void testSystemPropertiesOverrideExplicitColorScheme() throws Exception {
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

        restoreSystemProperties(() -> {
            System.setProperty("picocli.color.commands", "blue");
            System.setProperty("picocli.color.options", "blink");
            System.setProperty("picocli.color.parameters", "red");
            System.setProperty("picocli.color.optionParams", "magenta");
            assertEquals(ansi.new Text("@|blue <main class>|@ [@|blink -v|@] [@|blink -c|@=@|magenta <count>|@] @|red FILE|@..." + LINESEP),
                new CommandLine.Help(CommandLine.Model.CommandSpec.forAnnotatedObject(new App(), CommandLine.defaultFactory()), explicit).synopsis(0));
        });
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
    public void testAnsiIsWindowsDependsOnSystemProperty() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty("os.name", "MMIX");
            assertFalse(Ansi.isWindows());

            System.setProperty("os.name", "Windows");
            assertTrue(Ansi.isWindows());

            System.setProperty("os.name", "Windows 10 build 12345");
            assertTrue(Ansi.isWindows());
        });
    }

    @Test
    public void testAnsiIsXtermDependsOnEnvironmentVariable() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.isXterm());
            });

        withEnvironmentVariable("TERM", "random value")
            .execute(() -> {
                assertFalse(Ansi.isXterm());
            });

        withEnvironmentVariable("TERM", "xterm")
            .execute(() -> {
                assertTrue(Ansi.isXterm());
            });

        withEnvironmentVariable("TERM", "xterm asfasfasf")
            .execute(() -> {
                assertTrue(Ansi.isXterm());
            });
    }

    @Test
    public void testAnsiIsCygwinDependsOnEnvironmentVariable() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.isCygwin());
            });

        withEnvironmentVariable("TERM", "random value")
            .execute(() -> {
                assertFalse(Ansi.isCygwin());
            });


        withEnvironmentVariable("TERM", "xterm")
            .execute(() -> {
                assertFalse(Ansi.isCygwin());
            });

        withEnvironmentVariable("TERM", "xterm cygwin")
            .execute(() -> {
                assertTrue(Ansi.isCygwin());
            });

        withEnvironmentVariable("TERM", "cygwin")
            .execute(() -> {
                assertTrue(Ansi.isCygwin());
            });
    }

    @Test
    public void testAnsiHasOstypeDependsOnEnvironmentVariable() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hasOsType());
            });

        withEnvironmentVariable("OSTYPE", "")
            .execute(() -> {
                assertTrue(Ansi.hasOsType());
            });

        withEnvironmentVariable("OSTYPE", "42")
            .execute(() -> {
                assertTrue(Ansi.hasOsType());
            });
    }

    @Test
    public void testAnsiIsPseudoTtyDependsOnWindowsXtermOrCygwinOrOsType() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty("os.name", "MMIX");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                        assertFalse(Ansi.isPseudoTTY(), "OSTYPE and XTERM are not set");
                    });

            System.setProperty("os.name", "Windows 10 build 12345");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "222")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY());
                });

            System.setProperty("os.name", "MMIX");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "222")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertFalse(Ansi.isPseudoTTY(), "Not Windows");
                });

            System.setProperty("os.name", "Windows 10 build 12345"); // restore
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "222")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "restored");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "Missing OSTYPE, but TERM=xterm");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "abcygwinxyz")
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "Missing OSTYPE, but TERM=cygwin");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "abcygwinxyz")
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "anything")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "restored");
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "anything")
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY(), "Missing XTERM, but OSTYPE defined");
                });
        });
    }

    @Test
    public void testAnsiHintDisabledTrueIfCLICOLORZero() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "no env vars set");
            });

        withEnvironmentVariable("CLICOLOR", "")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "Just defining CLICOLOR is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "Just defining CLICOLOR is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "1")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "CLICOLOR=1 is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "false")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "CLICOLOR=false is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "0")
            .execute(() -> {
                assertTrue(Ansi.hintDisabled(), "CLICOLOR=0 disables");
            });
    }

    @Test
    public void testAnsiHintDisabledTrueIfConEmuANSIisOFF() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "no env vars set");
            });

        withEnvironmentVariable("ConEmuANSI", "")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "Just defining ConEmuANSI is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "0")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=0 is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "false")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=false is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "off")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=off does not disable");
            });

        withEnvironmentVariable("ConEmuANSI", "Off")
            .execute(() -> {
                assertFalse(Ansi.hintDisabled(), "ConEmuANSI=Off does not disable");
            });

        withEnvironmentVariable("ConEmuANSI", "OFF")
            .execute(() -> {
                assertTrue(Ansi.hintDisabled(), "ConEmuANSI=OFF disables");
            });
    }


    @Test
    public void testAnsiHintEnbledTrueIfANSICONDefined() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "no env vars set");
            });

        withEnvironmentVariable("ANSICON", "")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "ANSICON defined without value");
            });

        withEnvironmentVariable("ANSICON", "abc")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "ANSICON defined any value");
            });
    }

    @Test
    public void testAnsiHintEnbledTrueIfCLICOLOROne() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "no env vars set");
            });

        withEnvironmentVariable("CLICOLOR", "")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "Just defining CLICOLOR is not enough");
            });

        withEnvironmentVariable("CLICOLOR", "0")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "CLICOLOR=0 is not enough");
            });
        withEnvironmentVariable("CLICOLOR", "true")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "CLICOLOR=true is not enough");
            });
        withEnvironmentVariable("CLICOLOR", "1")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "CLICOLOR=1 enables");
            });
    }

    @Test
    public void testAnsiHintEnabledTrueIfConEmuANSIisON() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "no env vars set");
            });

        withEnvironmentVariable("ConEmuANSI", "")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "Just defining ConEmuANSI is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "1")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=1 is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "true")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=true is not enough");
            });

        withEnvironmentVariable("ConEmuANSI", "on")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=on does not enables");
            });

        withEnvironmentVariable("ConEmuANSI", "On")
            .execute(() -> {
                assertFalse(Ansi.hintEnabled(), "ConEmuANSI=On does not enables");
            });

        withEnvironmentVariable("ConEmuANSI", "ON")
            .execute(() -> {
                assertTrue(Ansi.hintEnabled(), "ConEmuANSI=ON enables");
            });
    }

    @Test
    public void testAnsiForceEnabledTrueIfCLICOLOR_FORCEisDefinedAndNonZero() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.forceEnabled(), "no env vars set");
            });

        withEnvironmentVariable("CLICOLOR_FORCE", "")
            .execute(() -> {
                assertTrue(Ansi.forceEnabled(), "Just defining CLICOLOR_FORCE is enough");
            });

        withEnvironmentVariable("CLICOLOR_FORCE", "1")
            .execute(() -> {
                assertTrue(Ansi.forceEnabled(), "CLICOLOR_FORCE=1 is enough");
            });

        withEnvironmentVariable("CLICOLOR_FORCE", "0")
            .execute(() -> {
                assertFalse(Ansi.forceEnabled(), "CLICOLOR_FORCE=0 is not forced");
            });
    }

    @Test
    public void testAnsiForceDisabledTrueIfNO_COLORDefined() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertFalse(Ansi.forceDisabled(), "no env vars set");
            });

        withEnvironmentVariable("NO_COLOR", "")
            .execute(() -> {
                assertTrue(Ansi.forceDisabled(), "NO_COLOR defined without value");
            });

        withEnvironmentVariable("NO_COLOR", "abc")
            .execute(() -> {
                assertTrue(Ansi.forceDisabled(), "NO_COLOR defined without value");
            });
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
    public void testAnsiAutoIfSystemPropertyPicocliAnsiCleared() throws Exception {
        withEnvironmentVariable("CLICOLOR_FORCE", "1")
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.clearProperty("picocli.ansi");
                    assertTrue(Ansi.AUTO.enabled());
                });
            });
    }

    @Test
    public void testAnsiAutoIfSystemPropertyPicocliAnsiIsAuto() throws Exception {
        withEnvironmentVariable("CLICOLOR_FORCE", "1")
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("picocli.ansi", "auto");
                    assertTrue(Ansi.AUTO.enabled());

                    System.setProperty("picocli.ansi", "Auto");
                    assertTrue(Ansi.AUTO.enabled());

                    System.setProperty("picocli.ansi", "AUTO");
                    assertTrue(Ansi.AUTO.enabled());
                });
            });
    }

    @Test
    public void testAnsiOffIfSystemPropertyPicocliAnsiIsNotAuto() throws Exception {
        restoreSystemProperties(() -> {
            System.setProperty("picocli.ansi", "auto1");

            withEnvironmentVariable("CLICOLOR_FORCE", "1")
                .execute(() -> {
                    assertFalse(Ansi.AUTO.enabled());
                });
        });
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesForceEnabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[NO_COLOR], "")
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR_FORCE], "1")
            .execute(() -> {
                assertTrue(Ansi.forceDisabled());
                assertTrue(Ansi.forceEnabled());
                assertFalse(Ansi.hintDisabled());
                assertFalse(Ansi.hintEnabled());
                assertFalse(Ansi.AUTO.enabled(), "forceDisabled overrides forceEnabled");
            });
    }

    @Test
    public void testAnsiAutoForceDisabledOverridesHintEnabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[NO_COLOR], "")
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "1")
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                assertTrue(Ansi.forceDisabled());
                assertFalse(Ansi.forceEnabled());
                assertFalse(Ansi.hintDisabled());
                assertTrue(Ansi.hintEnabled());
                assertFalse(Ansi.AUTO.enabled(), "forceDisabled overrides hintEnabled");
            });
    }

    @Test
    public void testAnsiAutoForcedEnabledOverridesHintDisabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR_FORCE], "1")
            .execute(() -> {
                assertFalse(Ansi.forceDisabled());
                assertTrue(Ansi.hintDisabled());
                assertTrue(Ansi.forceEnabled());
                assertFalse(Ansi.hintEnabled());
                assertTrue(Ansi.AUTO.enabled(), "forceEnabled overrides hintDisabled");
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[ConEmuANSI], "OFF")
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR_FORCE], "1")
            .execute(() -> {
                assertFalse(Ansi.forceDisabled());
                assertTrue(Ansi.hintDisabled());
                assertTrue(Ansi.forceEnabled());
                assertFalse(Ansi.hintEnabled());
                assertTrue(Ansi.AUTO.enabled(), "forceEnabled overrides hintDisabled 2");
            });
    }

    @Test
    public void testAnsiAutoJansiConsoleInstalledOverridesHintDisabled() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {

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
                });
            });
    }

    @Test
    public void testAnsiAutoHintDisabledOverridesHintEnabled() throws Exception {
        restoreSystemProperties(() -> {

            System.setProperty("os.name", "Windows");
            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[ANSICON], "1") // hint enabled
                .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {

                        assertTrue(Ansi.isWindows());
                    });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")// fake Cygwi
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[ANSICON], "1") // hint enabled
                .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY());
                });

            withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "cygwin")
                .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[ANSICON], "1") // hint enabled
                .and(ANSI_ENVIRONMENT_VARIABLES[CLICOLOR], "0")// hint disabled
                .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
                .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
                .execute(() -> {
                    assertTrue(Ansi.isPseudoTTY());

                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertTrue(Ansi.hintDisabled());
                    assertTrue(Ansi.hintEnabled());

                    assertFalse(Ansi.AUTO.enabled(), "Disabled overrides enabled");
                });
        });
    }

    @Test
    public void testAnsiAutoDisabledIfNoTty() throws Exception {
        if (Ansi.isTTY()) { return; } //
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {

                restoreSystemProperties(() -> {

                    System.setProperty("os.name", "Windows");
                    assertTrue(Ansi.isWindows());
                    assertFalse(Ansi.isPseudoTTY());
                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertFalse(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());

                    assertFalse(Ansi.AUTO.enabled(), "Must have TTY if no JAnsi");
                });
            });
    }

    @Test
    public void testAnsiAutoEnabledIfNotWindows() throws Exception {
        if (!Ansi.isTTY()) { return; } // needs TTY for this test
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {

                    System.setProperty("os.name", "MMIX");
                    assertFalse(Ansi.isWindows());
                    assertFalse(Ansi.isPseudoTTY()); // TODO Mock this?
                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertFalse(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());

                    assertTrue(Ansi.AUTO.enabled(), "If have TTY, enabled on non-Windows");
                });
            });
    }

    @Test
    public void testAnsiAutoEnabledIfWindowsPseudoTTY() throws Exception {
        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[0], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");
                    assertTrue(Ansi.isWindows());
                    assertFalse(Ansi.isJansiConsoleInstalled());

                    assertFalse(Ansi.forceDisabled());
                    assertFalse(Ansi.forceEnabled());
                    assertFalse(Ansi.hintDisabled());
                    assertFalse(Ansi.hintEnabled());
                });
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "xterm")
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");

                    assertTrue(Ansi.isPseudoTTY());
                    assertTrue(Ansi.AUTO.enabled(), "If have Cygwin pseudo-TTY, enabled on Windows");
                });
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], "cygwin")
            .and(ANSI_ENVIRONMENT_VARIABLES[1], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");

                    assertTrue(Ansi.isPseudoTTY());
                    assertTrue(Ansi.AUTO.enabled(), "If have Cygwin pseudo-TTY, enabled on Windows");
                });
            });

        withEnvironmentVariable(ANSI_ENVIRONMENT_VARIABLES[TERM], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[OSTYPE], "Windows")
            .and(ANSI_ENVIRONMENT_VARIABLES[2], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[3], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[4], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[5], null)
            .and(ANSI_ENVIRONMENT_VARIABLES[6], null)
            .execute(() -> {
                restoreSystemProperties(() -> {
                    System.setProperty("os.name", "Windows");

                    assertTrue(Ansi.isPseudoTTY());
                    assertTrue(Ansi.AUTO.enabled(), "If have MSYS pseudo-TTY, enabled on Windows");
                });
            });
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
