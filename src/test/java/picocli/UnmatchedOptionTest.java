package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Option;
import picocli.CommandLine.OverwrittenOptionException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.UnmatchedArgumentException;

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
    public void testSingleValuePositionalDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String y;
        }

        expect(new App(), "Missing required parameter: '<y>'", MissingParameterException.class, "-x", "3", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");
    }
    @Test
    public void testSingleValuePositionalCanBeConfiguredToConsumeArgResemblingOption() {
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
    public void testMultiValuePositionalDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters String[] y;
        }

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "4", "-z");
    }
    @Test
    public void testMultiValuePositionalCanBeConfiguredToConsumeArgResemblingOption() {
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
    public void testMultiValueVarArgPositionalDoesNotConsumeArgResemblingOptionSimple() {
        class App {
            @Parameters(arity = "*") String[] y;
        }

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-z", "4");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "4", "-z");
    }

    @Test
    public void testMultiValueVarArgPositionalDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(arity = "*") String[] y;
        }

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "-z", "4");

        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-x", "3", "4", "-z");
    }
    @Test
    public void testMultiValueVarArgPositionalCanBeConfiguredToConsumeArgResemblingOption() {
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
    public void testMultiValuePositionalArity2_NDoesNotConsumeArgResemblingOption() {
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
    public void testMultiValuePositionalArity2_NCanBeConfiguredToConsumeArgResemblingOption() {
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

    @Ignore
    @Test
    public void testSingleValueOptionDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String y;
        }

        //FIXME
        expect(new App(), "Unknown option: '-z'; Missing required parameter for option '-y' (<y>)", MissingParameterException.class, "-y", "-z");
    }

    @Ignore
    @Test
    public void testMultiValueOptionDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y") String[] y;
        }

        //FIXME
        expect(new App(), "Unknown option: '-z'; Missing required parameter for option '-y' (<y>)", MissingParameterException.class, "-y", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-y", "3", "-z");
    }

    @Ignore
    @Test
    public void testMultiValueOptionArity2DoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2") String[] y;
        }

        expect(new App(), "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-z]",
                MissingParameterException.class, "-y", "-z");

        //FIXME
        expect(new App(), "Expected parameter 2 (of 2 mandatory parameters) for option '-y' but found '-z'",
                MissingParameterException.class, "-y", "3", "-z");
    }

    @Ignore
    @Test
    public void testMultiValueOptionArity2_NDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "2..N") String[] y;
        }

        expect(new App(), "option '-y' at index 0 (<y>) requires at least 2 values, but only 1 were specified: [-z]",
                MissingParameterException.class, "-y", "-z");

        //FIXME
        expect(new App(), "Expected parameter 2 (of 2 mandatory parameters) for option '-y' but found '-z'",
                MissingParameterException.class, "-y", "1", "-z");

        //FIXME
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-y", "1", "2", "-z");
    }


    @Ignore
    @Test
    public void testMultiValueVarArgOptionDoesNotConsumeArgResemblingOption() {
        class App {
            @Option(names = "-x") int x;
            @Option(names = "-y", arity = "*") String[] y;
        }

        //FIXME
        //expect(new App(), "Missing required parameter for option '-y' (<y>)", MissingParameterException.class, "-y", "-z");
        expect(new App(), "Unknown option: '-z'", UnmatchedArgumentException.class, "-y", "3", "-z");
    }

}
