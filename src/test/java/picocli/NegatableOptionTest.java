package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class NegatableOptionTest {

    @Test
    public void testRegexTransformDefault() {
        CommandLine.INegatableOptionTransformer transformer = CommandLine.RegexTransformer.createDefault();

        CommandSpec dummy = CommandSpec.create();
        assertEquals("-X:-option", transformer.makeNegative("-X:+option", dummy));
        assertEquals("-X:\u00b1option", transformer.makeSynopsis("-X:+option", dummy));
        assertEquals("-X:+option", transformer.makeNegative("-X:-option", dummy));
        assertEquals("-X:\u00b1option", transformer.makeSynopsis("-X:-option", dummy));

        assertEquals("--no-verbose", transformer.makeNegative("--verbose", dummy));
        assertEquals("--[no-]verbose", transformer.makeSynopsis("--verbose", dummy));
        assertEquals("--verbose", transformer.makeNegative("--no-verbose", dummy));
        assertEquals("--[no-]verbose", transformer.makeSynopsis("--no-verbose", dummy));
    }

    @Test
    public void testRegexTransformDefaultDisabledForShortOptions() {
        CommandLine.RegexTransformer transformer = CommandLine.RegexTransformer.createDefault();
        CommandSpec dummy = CommandSpec.create();

        assertEquals("+x", transformer.makeNegative("+x", dummy));
        assertEquals("+x", transformer.makeSynopsis("+x", dummy));
        assertEquals("-x", transformer.makeNegative("-x", dummy));
        assertEquals("-x", transformer.makeSynopsis("-x", dummy));
    }

    @Test
    public void testRegexTransformCustomForShortOptions() {
        CommandLine.RegexTransformer transformer = createNegatableShortOptionsTransformer();

        CommandSpec dummy = CommandSpec.create();
        assertEquals("-x", transformer.makeNegative("+x", dummy));
        assertEquals("\u00b1x", transformer.makeSynopsis("+x", dummy));
        assertEquals("+x", transformer.makeNegative("-x", dummy));
        assertEquals("\u00b1x", transformer.makeSynopsis("-x", dummy));
    }

    static CommandLine.RegexTransformer createNegatableShortOptionsTransformer() {
        CommandLine.RegexTransformer transformer = new CommandLine.RegexTransformer.Builder(CommandLine.RegexTransformer.createDefault())
                .addPattern("^-(\\w)$", "+$1", "\u00b1$1") // TBD include short option transforms by default?
                .addPattern("^\\+(\\w)$", "-$1", "\u00b1$1") // (same: transform +x to -x)
                .build();
        return transformer;
    }

    @Test
    public void testModelFromReflection() {
        class App {
            @Option(names = "-a", negatable = true)
            boolean a;

            @Option(names = "-b", negatable = false)
            boolean b;
        }
        CommandLine cmd = new CommandLine(new App());
        assertTrue(cmd.getCommandSpec().findOption("-a").negatable());
        assertFalse(cmd.getCommandSpec().findOption("-b").negatable());
    }

    @Test
    public void testUsage() {
        @Command(usageHelpWidth = 90)
        class App {
            @Option(names = "-a",                     negatable = true, description = "...") boolean a;
            @Option(names = "--long",                 negatable = true, description = "...") boolean longWithoutNo;
            @Option(names = {"-b", "--long-b"},       negatable = true, description = "...") boolean shortAndLong;
            @Option(names = "--no-verbose",           negatable = true, description = "...") boolean longInitiallyNo;
            @Option(names = {"-n", "--no-verbose-b"}, negatable = true, description = "...") boolean shortAndLongInitiallyNo;
            @Option(names = {"-c", "--X:-java1"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle1;
            @Option(names = {      "-X:-java2"},      negatable = true, description = "...", defaultValue = "true") boolean javaStyle2;
            @Option(names = {      "-XX:-java3"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle3;
            @Option(names = {"-f", "--X:+java4"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle4;
            @Option(names = {"-g", "-X:+java5"},      negatable = true, description = "...", defaultValue = "true") boolean javaStyle5;
            @Option(names = {"-h", "-XX:+java6"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle6;
        }

        String expected = String.format("" +
                "Usage: <main class> [-abcfghn] [--[no-]long] [--[no-]verbose] [-X:\u00b1java2] [-XX:\u00b1java3]%n" +
                "  -a                     ...%n" +
                "  -b, --[no-]long-b      ...%n" +
                "  -c, --X:\u00b1java1         ...%n" +
                "  -f, --X:\u00b1java4         ...%n" +
                "  -g, -X:\u00b1java5          ...%n" +
                "  -h, -XX:\u00b1java6         ...%n" +
                "      --[no-]long        ...%n" +
                "  -n, --[no-]verbose-b   ...%n" +
                "      --[no-]verbose     ...%n" +
                "      -X:\u00b1java2          ...%n" +
                "      -XX:\u00b1java3         ...%n");
        String actual = new CommandLine(new App()).getUsageMessage(Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testPlusMinusWidth() {
        @Command(name = "negatable-options-demo", mixinStandardHelpOptions = true)
        class Demo {
            @Option(names = "--verbose", negatable = true, description = "Show verbose output")
            boolean verbose;

            @Option(names = "-XX:+PrintGCDetails", negatable = true, description = "Prints GC details")
            boolean printGCDetails;

            @Option(names = "-XX:-UseG1GC", negatable = true, description = "Use G1 algorithm for GC")
            boolean useG1GC = true;
        }
        String expected = String.format("" +
                "Usage: negatable-options-demo [-hV] [--[no-]verbose] [-XX:\u00b1PrintGCDetails]%n" +
                "                              [-XX:\u00b1UseG1GC]%n" +
                "  -h, --help                 Show this help message and exit.%n" +
                "  -V, --version              Print version information and exit.%n" +
                "      --[no-]verbose         Show verbose output%n" +
                "      -XX:\u00b1PrintGCDetails    Prints GC details%n" +
                "      -XX:\u00b1UseG1GC           Use G1 algorithm for GC%n");
        String actual = new CommandLine(new Demo()).getUsageMessage();
        assertEquals(expected, actual);
    }

    @Test
    public void testParser() {
        class App {
            @Option(names = "-a",                     negatable = true, description = "...") boolean a;
            @Option(names = "--long",                 negatable = true, description = "...") boolean longWithoutNo;
            @Option(names = {"-b", "--long-b"},       negatable = true, description = "...") boolean longB;
            @Option(names = "--no-verbose",           negatable = true, description = "...") boolean noVerbose;
            @Option(names = {"-n", "--no-verbose-b"}, negatable = true, description = "...") boolean noVerboseB;
            @Option(names = {"-c", "--X:-java1"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle1;
            @Option(names = {      "-X:-java2"},      negatable = true, description = "...", defaultValue = "true") boolean javaStyle2;
            @Option(names = {      "-XX:-java3"},     negatable = true, description = "...", defaultValue = "true") boolean javaStyle3;
            @Option(names = {"-f", "--X:+java4"},     negatable = true, description = "...", defaultValue = "false") boolean javaStyle4;
            @Option(names = {"-g", "-X:+java5"},      negatable = true, description = "...", defaultValue = "false") boolean javaStyle5;
            @Option(names = {"-h", "-XX:+java6"},     negatable = true, description = "...", defaultValue = "false") boolean javaStyle6;
        }

        String[] args = { "-a", "--no-long", "--no-long-b", "--verbose", "--verbose-b", "--X:+java1",
                        "-X:+java2", "-XX:+java3", "--X:-java4", "-X:-java5", "-XX:-java6", };
        App app = new App();
        assertFalse(app.a);

        new CommandLine(app).parse(args);
        assertTrue(app.a);
        assertFalse(app.longWithoutNo);
        assertFalse(app.longB);
        assertFalse(app.noVerbose);
        assertFalse(app.noVerboseB);
        assertTrue(app.javaStyle1);
        assertTrue(app.javaStyle2);
        assertTrue(app.javaStyle3);
        assertFalse(app.javaStyle4);
        assertFalse(app.javaStyle5);
        assertFalse(app.javaStyle6);
    }

    @Test
    public void testNegatableDisallowedForNonBooleanOptions() {
        class App {
            @Option(names = "--a", negatable = true, description = "...") int a;
        }
        App app = new App();
        try {
            //HelpTestUtil.setTraceLevel("DEBUG");
            new CommandLine(app);
            fail("Expected exception");
        } catch (CommandLine.InitializationException ex) {
            String cls = app.getClass().getName();
            assertEquals("Only boolean options can be negatable, but field int " + cls  + ".a is of type int", ex.getMessage());
        }
    }

    @Test
    public void testNegatableMultivalueOptions() {
        class App {
            @Option(names = "--a", negatable = true, description = "...") boolean[] a;
        }
        App app = new App();
        new CommandLine(app).parseArgs("--no-a", "--no-a", "--no-a");
        assertArrayEquals(new boolean[]{false, false, false}, app.a);
    }

    @Test
    public void testCustomizeRegexTransformer() {
        class App {
            @Option(names = "-a",                     negatable = true, description = "...") boolean a;
            @Option(names = {"-b", "--long-b"},       negatable = true, description = "...") boolean b;
            @Option(names = "+c",                     negatable = true, description = "...") boolean c;
            @Option(names = {"+n", "--no-verbose"},   negatable = true, description = "...") boolean noVerbose;
        }

        App app = new App();
        assertFalse(app.a);
        assertFalse(app.b);
        assertFalse(app.c);
        assertFalse(app.noVerbose);

        //HelpTestUtil.setTraceLevel("DEBUG");
        CommandLine cmd = new CommandLine(app);
        cmd.setNegatableOptionTransformer(createNegatableShortOptionsTransformer());

        String[] args = { "-a", "+b", "-c", "+n" };
        cmd.parse(args);
        assertTrue(app.a);
        assertFalse(app.b);
        assertFalse(app.c);
        assertTrue(app.noVerbose);

        String expected = String.format("" +
                "Usage: <main class> [\u00b1a] [\u00b1b] [\u00b1c] [\u00b1n]%n" +
                "  \u00b1a                   ...%n" +
                "  \u00b1b, --[no-]long-b    ...%n" +
                "  \u00b1c                   ...%n" +
                "  \u00b1n, --[no-]verbose   ...%n");
        String actual = cmd.getUsageMessage(Ansi.OFF);
        //cmd.usage(System.out);
        assertEquals(expected, actual);
    }

    @Test
    public void testCustomizeRegexTransformerShortOptionsBecomeLong() {
        class App {
            @Option(names = "-a",                     negatable = true, description = "...") boolean a;
            @Option(names = {"-b", "--long-b"},       negatable = true, description = "...") boolean b;
            @Option(names = "+c",                     negatable = true, description = "...") boolean c;
            @Option(names = {"+n", "--no-verbose"},   negatable = true, description = "...") boolean noVerbose;
        }

        //HelpTestUtil.setTraceLevel("DEBUG");
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        CommandLine.RegexTransformer transformer = new CommandLine.RegexTransformer.Builder(CommandLine.RegexTransformer.createDefault())
                .addPattern("^-(\\w)$", "+$1", "(+|-)$1") // TBD include short option transforms by default?
                .addPattern("^\\+(\\w)$", "-$1", "(+|-)$1") // (same: transform +x to -x)
                .build();
        cmd.setNegatableOptionTransformer(transformer);

        String[] args = { "-a", "+b", "-c", "+n" };
        cmd.parse(args);
        String expected = String.format("" +
                "Usage: <main class> [(+|-)a] [(+|-)b] [(+|-)c] [(+|-)n]%n" +
                "  (+|-)a%n" +
                "                       ...%n" +
                "  (+|-)b%n" +
                "    , --[no-]long-b    ...%n" +
                "  (+|-)c%n" +
                "                       ...%n" +
                "  (+|-)n%n" +
                "    , --[no-]verbose   ...%n");
        String actual = cmd.getUsageMessage(Ansi.OFF);
        //cmd.usage(System.out);
        assertEquals(expected, actual);
    }

}
