package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class NegatableOptionTest {

    @Test
    public void testRegex() {
        CommandLine.INegatableOptionTransformer transformer = CommandLine.RegexTransformer.createDefault();

        CommandSpec dummy = CommandSpec.create();
        assertEquals("-X:-option", transformer.makeNegative("-X:+option", dummy));
        assertEquals("-X:\u00b1option", transformer.makeSynopsis("-X:+option", dummy));
        assertEquals("-X:+option", transformer.makeNegative("-X:-option", dummy));
        assertEquals("-X:\u00b1option", transformer.makeSynopsis("-X:-option", dummy));

        assertEquals("+x", transformer.makeNegative("-x", dummy));
        assertEquals("\u00b1x", transformer.makeSynopsis("-x", dummy));

        assertEquals("--no-verbose", transformer.makeNegative("--verbose", dummy));
        assertEquals("--[no-]verbose", transformer.makeSynopsis("--verbose", dummy));

        assertEquals("--verbose", transformer.makeNegative("--no-verbose", dummy));
        assertEquals("--[no-]verbose", transformer.makeSynopsis("--no-verbose", dummy));
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

    @Ignore
    @Test
    public void testNegatableDoesNothingForNonBooleanOptions() {
        fail();
    }

    @Ignore
    @Test
    public void testNegatableMultivalueOptions() {
        fail();
    }

    @Ignore
    @Test
    public void testCustomNegatableOptionTransformer() {
        fail();
    }

    @Ignore
    @Test
    public void testCustomizeRegexTransformer() {
        // short options +x
        fail();
    }

}
