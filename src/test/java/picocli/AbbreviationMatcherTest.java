package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static picocli.CommandLine.AbbreviationMatcher.match;
import static picocli.CommandLine.AbbreviationMatcher.splitIntoChunks;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Model;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.ParseResult;
import static picocli.CommandLine.UnmatchedArgumentException;

public class AbbreviationMatcherTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    private Set<String> createSet() {
        Set<String> result = new LinkedHashSet<String>();
        result.add("kebab-case-extra");
        result.add("kebab-case-extra-extra");
        result.add("kebab-case");
        result.add("kc"); // alias
        result.add("very-long-kebab-case");
        result.add("camelCase");
        result.add("veryLongCamelCase");
        result.add("super-long-option");
        result.add("extremely-long-option-with-many-components");
        return result;
    }

    @Test
    public void testPrefixMatch() {
        Set<String> set = createSet();
        CommandLine cmd = new CommandLine(Model.CommandSpec.create());

        assertEquals("kebab-case", match(set, "kebab-case", false, cmd));
        assertEquals("kebab-case-extra", match(set, "kebab-case-extra", false, cmd));
        assertEquals("very-long-kebab-case", match(set, "very-long-kebab-case", false, cmd));
        assertEquals("very-long-kebab-case", match(set, "v-l-k-c", false, cmd));
        assertEquals("very-long-kebab-case", match(set, "vLKC", false, cmd));
        assertEquals("camelCase", match(set, "camelCase", false, cmd));
        assertEquals("camelCase", match(set, "cC", false, cmd));
        assertEquals("camelCase", match(set, "c-c", false, cmd));
        assertEquals("camelCase", match(set, "camC", false, cmd));
        assertEquals("camelCase", match(set, "camel", false, cmd));
        assertEquals("camelCase", match(set, "ca", false, cmd));
        assertEquals("super-long-option", match(set, "s-l-o", false, cmd));
        assertEquals("super-long-option", match(set, "s-long-opt", false, cmd));
        assertEquals("super-long-option", match(set, "super", false, cmd));
        assertEquals("super-long-option", match(set, "sup", false, cmd));
        assertEquals("super-long-option", match(set, "sup-long", false, cmd));
        assertNotEquals("super-long-option", match(set, "SLO", false, cmd));
        assertEquals   ("super-long-option", match(set, "sLO", false, cmd));
        assertEquals("super-long-option", match(set, "s-opt", false, cmd));
        assertEquals("veryLongCamelCase", match(set, "veryLongCamelCase", false, cmd));
        assertEquals("veryLongCamelCase", match(set, "vLCC", false, cmd));
        assertEquals("veryLongCamelCase", match(set, "v-l-c-c", false, cmd));
        assertEquals("extremely-long-option-with-many-components", match(set, "extr-opt-comp", false, cmd));
        assertEquals("extremely-long-option-with-many-components", match(set, "extr-long-opt-comp", false, cmd));
        assertEquals("extremely-long-option-with-many-components", match(set, "extr-comp", false, cmd));
        assertNotEquals("extremely-long-option-with-many-components", match(set, "extr-opt-long-comp", false, cmd));
        assertNotEquals("extremely-long-option-with-many-components", match(set, "long", false, cmd));

        try {
            match(set, "vLC", false, cmd);
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Error: 'vLC' is not unique: it matches 'very-long-kebab-case', 'veryLongCamelCase'", ex.getMessage());
        }
        try {
            match(set, "k-c", false, cmd);
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Error: 'k-c' is not unique: it matches 'kebab-case-extra', 'kebab-case-extra-extra', 'kebab-case'", ex.getMessage());
        }
        try {
            match(set, "kC", false, cmd);
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Error: 'kC' is not unique: it matches 'kebab-case-extra', 'kebab-case-extra-extra', 'kebab-case'", ex.getMessage());
        }
        try {
            match(set, "keb-ca", false, cmd);
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Error: 'keb-ca' is not unique: it matches 'kebab-case-extra', 'kebab-case-extra-extra', 'kebab-case'", ex.getMessage());
        }
    }

    //|`--veryLongCamelCase` | `--very`, `--VLCC`, `--vCase`
    //|`--super-long-option` | `--sup`, `--sLO`, `--s-l-o`, `--s-lon`, `--s-opt`, `--sOpt`
    //|`some-long-command` | `so`, sLC`, `s-l-c`, `soLoCo`, `someCom`
    @Test
    public void testUserManualExamples() {
        Set<String> original = new LinkedHashSet<String>();
        original.add("--veryLongCamelCase");
        original.add("--super-long-option");
        original.add("some-long-command");

        CommandLine cmd = new CommandLine(Model.CommandSpec.create());

        assertNotEquals("--veryLongCamelCase", match(original, "--VLCC", false, cmd));
        assertEquals("--veryLongCamelCase", match(original, "--very", false, cmd));
        assertEquals("--veryLongCamelCase", match(original, "--vLCC", false, cmd));
        assertEquals("--veryLongCamelCase", match(original, "--vCase", false, cmd));

        assertEquals("--super-long-option", match(original, "--sup", false, cmd));
        assertNotEquals("--super-long-option", match(original, "--Sup", false, cmd));
        assertEquals("--super-long-option", match(original, "--sLO", false, cmd));
        assertEquals("--super-long-option", match(original, "--s-l-o", false, cmd));
        assertEquals("--super-long-option", match(original, "--s-lon", false, cmd));
        assertEquals("--super-long-option", match(original, "--s-opt", false, cmd));
        assertEquals("--super-long-option", match(original, "--sOpt", false, cmd));

        assertNotEquals("some-long-command", match(original, "So", false, cmd));
        assertEquals("some-long-command", match(original, "so", false, cmd));
        assertEquals("some-long-command", match(original, "sLC", false, cmd));
        assertEquals("some-long-command", match(original, "s-l-c", false, cmd));
        assertEquals("some-long-command", match(original, "soLoCo", false, cmd));
        assertNotEquals("some-long-command", match(original, "SoLoCo", false, cmd));
        assertEquals("some-long-command", match(original, "someCom", false, cmd));
    }

    @Test
    public void testSplitIntoChunks() {
        assertEquals(Arrays.asList("k", "C"), splitIntoChunks("kC", false));
        assertEquals(Arrays.asList("k", "C"), splitIntoChunks("k-c", false));
        assertEquals(Arrays.asList("K", "C"), splitIntoChunks("KC", false));
        assertEquals(Arrays.asList("kebab", "Case"), splitIntoChunks("kebab-case", false));
        assertEquals(Arrays.asList("very", "Long", "Kebab", "Case"), splitIntoChunks("very-long-kebab-case", false));
        assertEquals(Arrays.asList("camel", "Case"), splitIntoChunks("camelCase", false));
        assertEquals(Arrays.asList("very", "Long", "Camel", "Case"), splitIntoChunks("veryLongCamelCase", false));

        assertEquals(Arrays.asList("--", "k", "C"), splitIntoChunks("--kC", false));
        assertEquals(Arrays.asList("--", "k", "C"), splitIntoChunks("--k-c", false));
        assertEquals(Arrays.asList("--", "K", "C"), splitIntoChunks("--KC", false));
        assertEquals(Arrays.asList("--", "kebab", "Case"), splitIntoChunks("--kebab-case", false));
        assertEquals(Arrays.asList("--", "very", "Long", "Kebab", "Case"), splitIntoChunks("--very-long-kebab-case", false));
        assertEquals(Arrays.asList("--", "camel", "Case"), splitIntoChunks("--camelCase", false));
        assertEquals(Arrays.asList("--", "very", "Long", "Camel", "Case"), splitIntoChunks("--veryLongCamelCase", false));
    }

    @Test
    public void testDefaultValues() {
        @Command
        class App {}
        CommandLine commandLine = new CommandLine(new App());
        assertFalse(commandLine.isAbbreviatedSubcommandsAllowed());
        assertFalse(commandLine.isAbbreviatedOptionsAllowed());
        commandLine.setAbbreviatedSubcommandsAllowed(true);
        assertTrue(commandLine.isAbbreviatedSubcommandsAllowed());
        assertFalse(commandLine.isAbbreviatedOptionsAllowed());
        commandLine.setAbbreviatedOptionsAllowed(true);
        assertTrue(commandLine.isAbbreviatedSubcommandsAllowed());
        assertTrue(commandLine.isAbbreviatedOptionsAllowed());
    }

    @Test
    public void testAbbrevSubcommands() throws Exception {
        @Command
        class App {
            @Command(name = "help")
            public int helpCommand() {
                return 1;
            }

            @Command(name = "hello")
            public int helloCommand() {
                return 2;
            }

            @Command(name = "version")
            public int versionCommand() {
                return 3;
            }

            @Command(name = "camelCaseSubcommand")
            public int ccsCommand() {
                return 4;
            }

            @Command(name = "another-style")
            public int asCommand() {
                return 5;
            }
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.setAbbreviatedSubcommandsAllowed(true);

        assertEquals(1, commandLine.execute("help"));
        assertEquals(2, commandLine.execute("hello"));
        assertEquals(3, commandLine.execute("version"));
        assertEquals(4, commandLine.execute("camelCaseSubcommand"));
        assertEquals(5, commandLine.execute("another-style"));

        assertEquals(1, commandLine.execute("help"));
        assertEquals(2, commandLine.execute("hell"));
        assertEquals(3, commandLine.execute("ver"));
        assertEquals(4, commandLine.execute("camel"));
        assertEquals(4, commandLine.execute("cCS"));
        assertEquals(4, commandLine.execute("c-c-s"));
        assertEquals(5, commandLine.execute("aS"));
        assertEquals(5, commandLine.execute("a-s"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output);
        commandLine.setErr(new PrintWriter(printStream));
        commandLine.execute("h");

        String content = new String(output.toByteArray(), "UTF-8")
                .replaceAll("\r\n", "\n"); // Normalize line endings.
        assertEquals("Error: 'h' is not unique: it matches 'hello', 'help'\n"
                + "Usage: <main class> [COMMAND]\n"
                + "Commands:\n"
                + "  another-style\n"
                + "  camelCaseSubcommand\n"
                + "  hello\n"
                + "  help\n"
                + "  version\n", content);
    }

    @Test
    public void testCaseInsensitiveAbbrevSubcommands() throws Exception {
        @Command
        class App {
            @Command(name = "HACKING")
            public int hackingCommand() {
                return -1;
            }

            @Command(name = "help")
            public int helpCommand() {
                return 1;
            }

            @Command(name = "hello")
            public int helloCommand() {
                return 2;
            }

            @Command(name = "version")
            public int versionCommand() {
                return 3;
            }

            @Command(name = "camelCaseSubcommand")
            public int ccsCommand() {
                return 4;
            }

            @Command(name = "another-style")
            public int asCommand() {
                return 5;
            }
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.setAbbreviatedSubcommandsAllowed(true);
        commandLine.setSubcommandsCaseInsensitive(true);

        assertEquals(1, commandLine.execute("HELP"));
        assertEquals(2, commandLine.execute("HELLO"));
        assertEquals(3, commandLine.execute("VERSION"));
        assertEquals(4, commandLine.execute("CAMELCASESUBCOMMAND"));
        assertEquals(5, commandLine.execute("ANOTHER-STYLE"));

        assertEquals(1, commandLine.execute("help"));
        assertEquals(2, commandLine.execute("hell"));
        assertEquals(3, commandLine.execute("ver"));
        assertEquals(4, commandLine.execute("camel"));
        assertEquals(4, commandLine.execute("CAMEL"));
        assertEquals(5, commandLine.execute("a-S"));
        assertEquals(5, commandLine.execute("A-s"));
        assertEquals(5, commandLine.execute("ANOTHER"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output);
        commandLine.setErr(new PrintWriter(printStream));
        commandLine.execute("h");

        String content = new String(output.toByteArray(), "UTF-8")
                .replaceAll("\r\n", "\n"); // Normalize line endings.
        assertEquals("Error: 'h' is not unique: it matches 'HACKING', 'hello', 'help'\n"
                + "Usage: <main class> [COMMAND]\n"
                + "Commands:\n"
                + "  another-style\n"
                + "  camelCaseSubcommand\n"
                + "  HACKING\n"
                + "  hello\n"
                + "  help\n"
                + "  version\n", content);
    }

    @Test
    public void testAbbrevOptions() throws Exception {
        @Command
        class App {
            @Option(names = {"-H", "--help"})
            public boolean help;

            @Option(names = "--hello")
            public boolean hello;

            @Option(names = "--version")
            public boolean version;

            @Option(names = "--camelCaseOption", negatable = true)
            public boolean ccOption;

            @Option(names = "--another-style", negatable = true)
            public boolean anotherStyle;

            @Option(names = "---hi-triple-hyphens", negatable = true)
            public boolean tripleHyphens;
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.setAbbreviatedOptionsAllowed(true);

        ParseResult result = commandLine.parseArgs("--help", "--hello", "--version", "--camelCaseOption", "--another-style", "---hi-triple-hyphens");
        assertTrue(result.hasMatchedOption("--help"));
        assertTrue(result.hasMatchedOption("--hello"));
        assertTrue(result.hasMatchedOption("--version"));
        assertTrue(result.hasMatchedOption("--camelCaseOption"));
        assertTrue(result.hasMatchedOption("--another-style"));
        assertTrue(result.hasMatchedOption("---hi-triple-hyphens"));

        result = commandLine.parseArgs("--help", "--hell", "--ver", "--cCO", "--a-s");
        assertTrue(result.hasMatchedOption("--help"));
        assertTrue(result.hasMatchedOption("--hello"));
        assertTrue(result.hasMatchedOption("--version"));
        assertTrue(result.hasMatchedOption("--camelCaseOption"));
        assertTrue(result.hasMatchedOption("--another-style"));

        result = commandLine.parseArgs("--c-c-o", "--aS");
        assertTrue(result.hasMatchedOption("--camelCaseOption"));
        assertTrue(result.hasMatchedOption("--another-style"));

        result = commandLine.parseArgs("--no-c-c-o", "--no-aS");
        assertTrue(result.hasMatchedOption("--camelCaseOption"));
        assertTrue(result.hasMatchedOption("--another-style"));

        try {
            commandLine.parseArgs("--hi-triple-hyphens");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '--hi-triple-hyphens'", ex.getMessage());
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output);
        commandLine.setErr(new PrintWriter(printStream));
        commandLine.execute("--h");

        String content = new String(output.toByteArray(), "UTF-8")
                .replaceAll("\r\n", "\n"); // Normalize line endings.
        assertEquals("Error: '--h' is not unique: it matches '--help', '--hello'\n"
                + "Usage: <main class> [-H] [--[no-]another-style] [--[no-]camelCaseOption]\n"
                + "                    [--hello] [---hi-triple-hyphens] [--version]\n"
                + "      --[no-]another-style\n"
                + "      --[no-]camelCaseOption\n"
                + "  -H, --help\n"
                + "      --hello\n"
                + "      ---hi-triple-hyphens\n"
                + "      --version\n", content);
    }

    @Test
    public void testAbbrevOptionsCaseInsensitive1() {
        class App {
            @Option(names = {"-CamelCaseOption"}) boolean camelCaseOption;
        }

        try {
            new CommandLine(new App()).setAbbreviatedOptionsAllowed(true).setOptionsCaseInsensitive(true).parseArgs("-CCO");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-CCO'", ex.getMessage());
        }

        try {
            new CommandLine(new App()).setAbbreviatedOptionsAllowed(true).setOptionsCaseInsensitive(true).parseArgs("-c-c-o");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-c-c-o'", ex.getMessage());
        }
        App app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).setOptionsCaseInsensitive(true).parseArgs("-came");
        assertTrue(app.camelCaseOption);
    }

    @Test
    public void testAbbrevOptionsCaseInsensitive2() {
        class App {
            @Option(names = {"-kebab-case-option"}) boolean kebabCaseOption;
        }

        try {
            new CommandLine(new App()).setAbbreviatedOptionsAllowed(true).setOptionsCaseInsensitive(true).parseArgs("-KCO");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-KCO'", ex.getMessage());
        }
        App app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).setOptionsCaseInsensitive(true).parseArgs("-k-c-o");
        assertTrue(app.kebabCaseOption);

        app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).setOptionsCaseInsensitive(true).parseArgs("-kebab");
        assertTrue(app.kebabCaseOption);
    }

    @Test
    public void testPOSIXClusterOptionsWithAbbrevOptions() {
        @Command
        class App {
            @Option(names = "-A")
            public boolean a;

            @Option(names = "-B")
            public boolean b;

            @Option(names = "-AB")
            public boolean ab;
        }

        CommandLine commandLine = new CommandLine(new App());
        commandLine.setAbbreviatedOptionsAllowed(true);

        ParseResult result = commandLine.parseArgs("-AB");
        assertFalse(result.hasMatchedOption("-A"));
        assertFalse(result.hasMatchedOption("-B"));
        assertTrue(result.hasMatchedOption("-AB"));

        result = commandLine.parseArgs("-A");
        assertTrue(result.hasMatchedOption("-A"));
        assertFalse(result.hasMatchedOption("-B"));
        assertFalse(result.hasMatchedOption("-AB"));

        App app = CommandLine.populateCommand(new App(), "-AB");
        assertTrue(app.ab);
        assertFalse(app.a);
        assertFalse(app.b);
    }

    @Test
    public void testPOSIXClusterOptionsWithAbbrevOptions2() {
        @Command
        class App {
            @Option(names = "-A")
            public boolean a;

            @Option(names = "-B")
            public boolean b;

            @Option(names = "-AaaBbb")
            public boolean aaaBbb;
        }

        CommandLine commandLine = new CommandLine(new App());
        commandLine.setAbbreviatedOptionsAllowed(true);

        ParseResult result = commandLine.parseArgs("-AB");
        assertFalse(result.hasMatchedOption("-A"));
        assertFalse(result.hasMatchedOption("-B"));
        assertTrue(result.hasMatchedOption("-AaaBbb"));

        result = commandLine.parseArgs("-A");
        assertTrue(result.hasMatchedOption("-A"));
        assertFalse(result.hasMatchedOption("-B"));
        assertFalse(result.hasMatchedOption("-AaaBbb"));

        App app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("-AB");
        assertTrue(app.aaaBbb);
        assertFalse(app.a);
        assertFalse(app.b);
    }

    @Test
    public void testAbbrevOptionsAmbiguous() {
        @Command
        class App {
            @Option(names = "--a-B")
            public boolean a_b;

            @Option(names = "--aB")
            public boolean aB;

            @Option(names = "-a")
            public boolean a;

            @Option(names = "-B")
            public boolean B;
        }

        App app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("-aB");
        assertFalse(app.a_b);
        assertFalse(app.aB);
        assertTrue(app.a);
        assertTrue(app.B);

        app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("--aB");
        assertFalse(app.a_b);
        assertTrue(app.aB);
        assertFalse(app.a);
        assertFalse(app.B);

        app = new App();
        new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("--aB", "--a-B");
        assertTrue(app.a_b);
        assertTrue(app.aB);
        assertFalse(app.a);
        assertFalse(app.B);

        app = new App();
        try {
            new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("--a-b");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Error: '--a-b' is not unique: it matches '--a-B', '--aB'", ex.getMessage());
        }

        app = new App();
        try {
            new CommandLine(app).setAbbreviatedOptionsAllowed(true).parseArgs("--AB");
            fail("Expected exception");
        } catch (ParameterException ex) {
            //assertEquals("Error: '--AB' is not unique: it matches '--a-B', '--aB'", ex.getMessage());
            assertEquals("Unknown option: '--AB'", ex.getMessage());
        }
    }

    @Test
    public void testAbbreviatedOptionWithAttachedValue() {
        @Command
        class Bean {
            @Option(names = "--xxx-yyy")
            int x;
        }
        Bean bean = new Bean();
        CommandLine cmd = new CommandLine(bean);
        cmd.setAbbreviatedOptionsAllowed(true);
        cmd.parseArgs("--x-y=123");
        assertEquals(123, bean.x);
    }

}
