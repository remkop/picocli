package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Option;
import picocli.CommandLine.OverwrittenOptionException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for
 * <ul>
 *   <li>https://github.com/remkop/picocli/issues/1015</li>>
 *   <li>https://github.com/remkop/picocli/issues/1055</li>>
 *   <li>https://github.com/remkop/picocli/issues/639</li>>
 * </ul>
 */
public class UnmatchedOptionTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    static void expect(Object userObject, String errorMessage, Class<? extends Exception> cls, String... args) {
        expect(new CommandLine(userObject), errorMessage, cls, args);
    }
    static void expect(CommandLine cmd, String errorMessage, Class<? extends Exception> cls, String... args) {
        try {
            cmd.parseArgs(args);
            fail("Expected exception");
        } catch (Exception ex) {
            assertTrue("Wrong exception: " + ex + ", expected " + cls.getName(), cls.isAssignableFrom(ex.getClass()));
            assertEquals(errorMessage, ex.getMessage());
        }
    }

    @Test
    public void testSingleValuePositionalDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String y;
        }

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-x", "3", "-x");
        expect(new App(), "option '-x' (<x>) should be specified only once", OverwrittenOptionException.class, "-x", "3", "-x", "4");

        App app = new App();
        new CommandLine(app).parseArgs("-x", "3", "4");
        assertEquals(3, app.x);
        assertEquals("4", app.y);
    }

    @Test
    public void testMultiValuePositionalDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String[] y;
        }

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-x", "3", "-x");
        expect(new App(), "option '-x' (<x>) should be specified only once", OverwrittenOptionException.class, "-x", "3", "-x", "4");

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-x", "3", "4", "-x");

        App app = new App();
        new CommandLine(app).parseArgs("-x", "3", "4");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"4"}, app.y);
    }

    @Test
    public void testMultiValueVarArgPositionalDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "*") String[] y;
        }

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-x", "3", "-x");
        expect(new App(), "option '-x' (<x>) should be specified only once", OverwrittenOptionException.class, "-x", "3", "-x", "4");

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-x", "3", "4", "-x");

        App app = new App();
        new CommandLine(app).parseArgs("-x", "3", "4");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"4"}, app.y);
    }

    @Test
    public void testMultiValuePositionalArity2_NDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "2..*") String[] y;
        }

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-x", "3", "-x");
        expect(new App(), "option '-x' (<x>) should be specified only once", OverwrittenOptionException.class, "-x", "3", "-x", "4");

        expect(new App(), "Expected parameter 2 (of 2 mandatory parameters) for positional parameter at index 0..* (<y>) but found '-x'",
                MissingParameterException.class, "-x", "3", "4", "-x");

        App app = new App();
        new CommandLine(app).parseArgs("-x", "3", "4", "5");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"4", "5"}, app.y);
    }

    @Test
    public void testSingleValueOptionDoesNotConsumeActualOptionSimple() {
        class App {
            @Option(names = "-x") String x;
        }

        expect(new App(), "Expected parameter for option '-x' but found '-x'", MissingParameterException.class, "-x", "-x");
    }

    @Test
    public void testSingleValueOptionDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String y;
        }

        expect(new App(), "Expected parameter for option '-y' but found '-x'", MissingParameterException.class, "-y", "-x");
    }

    @Test
    public void testSingleValueOptionDoesNotConsumeNegatedActualOption() {
        class App {
            @Option(names = "--x", negatable = true) boolean x;
            @Option(names = "-y") String y;
        }

        expect(new App(), "Expected parameter for option '-y' but found '--no-x'", MissingParameterException.class, "-y", "--no-x");
    }

    @Test
    public void testSingleValueOptionCanConsumeQuotedActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String y;
        }
        App app = new App();
        new CommandLine(app).parseArgs("-y", "\"-x\"");
        assertEquals("\"-x\"", app.y);

        new CommandLine(app).setTrimQuotes(true).parseArgs("-y", "\"-x\"");
        assertEquals("-x", app.y);
    }

    @Test
    public void testMultiValueOptionDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String[] y;
        }

        expect(new App(), "Expected parameter for option '-y' but found '-x'", MissingParameterException.class, "-y", "-x");
        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-y", "3", "-x");
    }

    @Test
    public void testMultiValueOptionArity2DoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2") String[] y;
        }

        expect(new App(), "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-x]",
                MissingParameterException.class, "-y", "-x");

        expect(new App(), "Expected parameter 2 (of 2 mandatory parameters) for option '-y' but found '-x'",
                MissingParameterException.class, "-y", "3", "-x");
    }

    @Test
    public void testMultiValueOptionArity2_NDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2..N") String[] y;
        }

        expect(new App(), "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-x]",
                MissingParameterException.class, "-y", "-x");

        expect(new App(), "Expected parameter 2 (of 2 mandatory parameters) for option '-y' but found '-x'",
                MissingParameterException.class, "-y", "1", "-x");

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-y", "1", "2", "-x");
    }

    @Test
    public void testMultiValueVarArgOptionDoesNotConsumeActualOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "*") String[] y;
        }

        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-y", "-x");
        expect(new App(), "Missing required parameter for option '-x' (<x>)", MissingParameterException.class, "-y", "3", "-x");
    }

    // -----------------------------------

    @Test
    public void testSingleValuePositionalDoesNotConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String y;
        }

        expect(new App(), "Missing required parameter: '<y>'", MissingParameterException.class, "-x", "3", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");
    }
    @Test
    public void testSingleValuePositionalCanBeConfiguredToConsumeUnknownOption() {
        class App {

            @Option(names = "-x") int x;
            @Parameters String y;
        }

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "-z");
        assertEquals(3, app.x);
        assertEquals("-z", app.y);

        CommandLine cmd = new CommandLine(new App()).setUnmatchedOptionsArePositionalParams(true);
        expect(cmd, "Unmatched argument at index 3: '4'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");
    }

    @Test
    public void testMultiValuePositionalDoesNotConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String[] y;
        }

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "4", "-z");
    }
    @Test
    public void testMultiValuePositionalCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String[] y;
        }

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "-z");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"-z"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "-z", "4");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"-z", "4"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "4", "-z");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"4", "-z"}, app.y);
    }

    @Test
    public void testMultiValueVarArgPositionalDoesNotConsumeUnknownOptionSimple() {
        class App {
            @Parameters(arity = "*") String[] y;
        }

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-z", "4");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "4", "-z");
    }

    @Test
    public void testMultiValueVarArgPositionalDoesNotConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "*") String[] y;
        }

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "4", "-z");
    }
    @Test
    public void testMultiValueVarArgPositionalCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "*") String[] y;
        }

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "-z");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"-z"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "-z", "4");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"-z", "4"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "4", "-z");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"4", "-z"}, app.y);
    }

    @Test
    public void testMultiValuePositionalArity2_NDoesNotConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "2..*") String[] y;
        }

        expect(new App(), "positional parameter at index 0..* (<y>) requires at least 2 values, but none were specified.",
                MissingParameterException.class, "-x", "3", "-z");

        expect(new App(), "positional parameter at index 0..* (<y>) requires at least 2 values, but only 1 were specified: [4]",
                MissingParameterException.class, "-x", "3", "-z", "4");

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "4", "-z");
    }
    @Test
    public void testMultiValuePositionalArity2_NCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "2..*") String[] y;
        }

        expect(new App(), "positional parameter at index 0..* (<y>) requires at least 2 values, but none were specified.",
                MissingParameterException.class, "-x", "3", "-z");

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "-z", "4");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"-z", "4"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsArePositionalParams(true).parseArgs("-x", "3", "4", "-z");
        assertEquals(3, app.x);
        assertArrayEquals(new String[]{"4", "-z"}, app.y);
    }

    @Test
    public void testSingleValueOptionDoesNotConsumeUnknownOptionByDefault() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String y;
        }

        CommandLine cmd = new CommandLine(new App());
        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        expect(cmd, "Unknown option: '-z'; Expected parameter for option '-y' but found '-z'", UnmatchedArgumentException.class, "-y", "-z");
    }

    @Test
    public void testSingleValueOptionCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String y;
        }

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "-z");
        assertEquals("-z", app.y);
    }

    @Test
    public void testMultiValueOptionDoesNotConsumeUnknownOptionByDefault() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String[] y;
        }

        CommandLine cmd = new CommandLine(new App());
        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        expect(cmd, "Unknown option: '-z'; Expected parameter for option '-y' but found '-z'", UnmatchedArgumentException.class, "-y", "-z");
        expect(cmd, "Unknown option: '-z'", UnmatchedArgumentException.class, "-y", "3", "-z");
    }

    @Test
    public void testMultiValueOptionCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String[] y;
        }

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "-z");
        assertArrayEquals(new String[]{"-z"}, app.y);

        // arity=1 for multi-value options
        CommandLine cmd = new CommandLine(new App()).setUnmatchedOptionsAllowedAsOptionParameters(true);
        expect(cmd, "Unknown option: '-z'", UnmatchedArgumentException.class, "-y", "3", "-z");
    }

    @Test
    public void testMultiValueOptionArity2DoesNotConsumeUnknownOptionByDefault() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2") String[] y;
        }

        CommandLine cmd = new CommandLine(new App());
        expect(cmd, "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-z]",
                MissingParameterException.class, "-y", "-z");

        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        expect(cmd, "Unknown option: '-z'; Expected parameter 2 (of 2 mandatory parameters) for option '-y' but found '-z'",
                UnmatchedArgumentException.class, "-y", "3", "-z");
    }

    @Test
    public void testMultiValueOptionArity2CanBeConfiguredToConsumeUnknownOptions() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2") String[] y;
        }

        CommandLine cmd = new CommandLine(new App()).setUnmatchedOptionsAllowedAsOptionParameters(true);
        expect(cmd, "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-z]",
                MissingParameterException.class, "-y", "-z");

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "3", "-z");
        assertArrayEquals(new String[]{"3", "-z"}, app.y);
    }

    @Test
    public void testMultiValueOptionArity2_NDoesNotConsumeUnknownOptionsByDefault() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2..N") String[] y;
        }

        CommandLine cmd = new CommandLine(new App());
        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        expect(cmd, "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-z]",
                MissingParameterException.class, "-y", "-z");

        expect(cmd, "Unknown option: '-z'; Expected parameter 2 (of 2 mandatory parameters) for option '-y' but found '-z'",
                UnmatchedArgumentException.class, "-y", "1", "-z");

        expect(cmd, "Unknown option: '-z'; Expected parameter 3 (of 2 mandatory parameters) for option '-y' but found '-z'",
                UnmatchedArgumentException.class, "-y", "1", "2", "-z");
    }

    @Test
    public void testMultiValueOptionArity2_NCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2..N") String[] y;
        }

        CommandLine cmd = new CommandLine(new App()).setUnmatchedOptionsAllowedAsOptionParameters(true);
        expect(cmd, "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-z]",
                MissingParameterException.class, "-y", "-z");

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "1", "-z");
        assertArrayEquals(new String[]{"1", "-z"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "1", "2", "-z");
        assertArrayEquals(new String[]{"1", "2", "-z"}, app.y);
    }

    @Test
    public void testMultiValueVarArgOptionDoesNotConsumeUnknownOptionsByDefault() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "*") String[] y;
        }

        CommandLine cmd = new CommandLine(new App());
        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        expect(cmd, "Unknown option: '-z'; Expected parameter for option '-y' but found '-z'", UnmatchedArgumentException.class, "-y", "-z");
        expect(cmd, "Unknown option: '-z'; Expected parameter for option '-y' but found '-z'", UnmatchedArgumentException.class, "-y", "3", "-z");
    }

    @Test
    public void testMultiValueVarArgOptionCanBeConfiguredToConsumeUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "*") String[] y;
        }

        App app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "-z");
        assertArrayEquals(new String[]{"-z"}, app.y);

        app = new App();
        new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true).parseArgs("-y", "3", "-z");
        assertArrayEquals(new String[]{"3", "-z"}, app.y);
    }

    @Test //#639
    public void testUnknownOptionAsOptionValue() {
        class App {
            @Option(names = {"-x", "--xvalue"})
            String x;

            @Option(names = {"-y", "--yvalues"}, arity = "1")
            List<String> y;
        }
        App app = new App();
        CommandLine cmd = new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true);
        cmd.parseArgs("-x", "-unknown");
        assertEquals("-unknown", app.x);

        cmd.parseArgs("-y", "-unknown");
        assertEquals(Arrays.asList("-unknown"), app.y);

        cmd = new CommandLine(new App());
        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        expect(cmd, "Unknown option: '-unknown'; Expected parameter for option '--xvalue' but found '-unknown'", UnmatchedArgumentException.class, "-x", "-unknown");
        expect(cmd, "Unknown option: '-unknown'; Expected parameter for option '--yvalues' but found '-unknown'", UnmatchedArgumentException.class, "-y", "-unknown");
    }

    static class LenientShortConverter implements CommandLine.ITypeConverter<Short> {
        public Short convert(String value) {
            return Short.decode(value); // allow octal values "-#123"
        }
    }
    static class LenientLongConverter implements CommandLine.ITypeConverter<Long> {
        public Long convert(String value) {
            return Long.decode(value); // allow hex values "-0xABCDEF"
        }
    }
    @Test //#639
    public void testNegativeNumbersAreNotUnknownOption() {
        class App {
            @Option(names = {"-i", "--int"})
            int i;

            @Option(names = {"-s", "--short"}, converter = LenientShortConverter.class)
            short s;

            @Option(names = {"-L", "--long"}, converter = LenientLongConverter.class)
            long L;

            @Option(names = {"-d", "--double"})
            double d;

            @Option(names = {"-f", "--float"})
            float f;

            @Option(names = {"-x", "--xvalue"})
            String x;

            @Option(names = {"-y", "--yvalues"}, arity = "1")
            List<String> y;
        }

        App app = new App();
        CommandLine cmd = new CommandLine(app).setUnmatchedOptionsAllowedAsOptionParameters(true);
        String[] args = {"-i", "-1", "-s", "-#55", "-L", "-0xCAFEBABE", "-d", "-Infinity", "-f", "-NaN", "-x", "-2", "-y", "-3", "-y", "-0", "-y", "-0.0", "-y", "-NaN"};
        cmd.parseArgs(args);
        assertEquals(-1, app.i);
        assertEquals(-0x55, app.s);
        assertEquals(-0xCAFEBABEL, app.L);
        assertEquals(Double.NEGATIVE_INFINITY, app.d, 0);
        assertEquals(Float.NaN, app.f, 0);
        assertEquals("-2", app.x);
        assertEquals(Arrays.asList("-3", "-0", "-0.0", "-NaN"), app.y);

        cmd = new CommandLine(new App());
        cmd.setUnmatchedOptionsAllowedAsOptionParameters(false);
        cmd.parseArgs(args);
        assertEquals(-1, app.i);
        assertEquals(-0x55, app.s);
        assertEquals(-0xCAFEBABEL, app.L);
        assertEquals(Double.NEGATIVE_INFINITY, app.d, 0);
        assertEquals(Float.NaN, app.f, 0);
        assertEquals("-2", app.x);
        assertEquals(Arrays.asList("-3", "-0", "-0.0", "-NaN"), app.y);
    }

    @Ignore("#1125")
    @Test // https://github.com/remkop/picocli/issues/1125
    public void testSubcommandAsOptionValue() {
        @Command(name = "app")
        class App {
            @Option(names = "-x") String x;

            @Command
            public int search() {
                return 123;
            }
        }
        ParseResult parseResult = new CommandLine(new App()).parseArgs("-x", "search", "search");
        assertEquals("search", parseResult.matchedOptionValue("-x", null));
        assertTrue(parseResult.hasSubcommand());
    }
}
