package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FallbackTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command
    static class MyCommand implements Runnable {
        @Option(names = {"-r", "--reader"}, arity = "0..1",
                description = "Use reader", paramLabel = "<reader>", fallbackValue = "")
        String reader;

        @Option(names = "-x") String arity1;
        @Parameters String[] params = {};

        public void run() { }

        private String subApp;
        private String[] subParams;

        @Command(name = "run", description = "Run specified application.")
        public int runApp(@Parameters(paramLabel = "<app>", index = "0") String app,
                          @Parameters(index = "1..*") String[] args) {
            subApp = app;
            subParams = args;
            return 123;
        }
    }

    @Test
    public void testIssue828SubcommandAssignedToOptionWithFallback() {
        MyCommand cmd = new MyCommand();
        int exitCode = new CommandLine(cmd)
                .setUnmatchedOptionsArePositionalParams(true)
                .execute("-r run app arg1 arg2 --something".split(" "));

        assertNotEquals("run", cmd.reader);
        assertEquals(123, exitCode);
        assertEquals("app", cmd.subApp);
        assertArrayEquals(new String[]{"arg1", "arg2", "--something"}, cmd.subParams);
    }

    @Ignore
    @Test
    public void testQuotedSubcommandAssignedToOptionWithFallback() {
        MyCommand cmd = new MyCommand();
        int exitCode = new CommandLine(cmd)
                .setTrimQuotes(true)
                .setUnmatchedOptionsArePositionalParams(true)
                .execute("-r \"run\" app arg1 arg2 --something".split(" "));

        assertEquals("run", cmd.reader);
        assertEquals(0, exitCode);
        assertNull(cmd.subApp);
        assertArrayEquals(new String[]{"app", "arg1", "arg2", "--something"}, cmd.params);
    }

    @Test
    public void testSubcommandAssignedToOption() {
        class MyBadParams implements CommandLine.IParameterExceptionHandler {
            ParameterException ex;
            public int handleParseException(ParameterException ex, String[] args) throws Exception {
                this.ex = ex;
                return 2;
            }
        }
        MyBadParams handler = new MyBadParams();
        MyCommand cmd = new MyCommand();
        int exitCode = new CommandLine(cmd)
                .setUnmatchedOptionsArePositionalParams(true)
                .setParameterExceptionHandler(handler)
                .execute("-x run app a".split(" "));

        assertEquals(2, exitCode);
        assertEquals(null, cmd.arity1);
        assertNull(cmd.subApp);
        assertNotNull(handler.ex);
        assertEquals("Expected parameter for option '-x' but found 'run'", handler.ex.getMessage());
    }

    @Test
    public void testNullFallbackValue() {
        class App {
            @Option(names = "-x", arity = "0..1", fallbackValue = Option.NULL_VALUE)
            Integer x = 3;
        }
        App missing = CommandLine.populateCommand(new App()); // no args specified
        assertEquals(Integer.valueOf(3), missing.x);

        App app = CommandLine.populateCommand(new App(), "-x");
        assertNull(app.x);
    }
    static class Issue1904 {
        enum DebugFacility { FOO, BAR, BAZ, ALL, DEFAULT, FALLBACK }

        @Option(
            names = { "--debug" },
            paramLabel = "FACILITY",
            split = ",",
            arity = "0..*",
            defaultValue = "DEFAULT",
            fallbackValue = "FALLBACK")
        Collection<DebugFacility> facilities;

        @Option(names = { "--map" }, arity = "0..*",
            defaultValue = "DEFAULT=0",
            fallbackValue = "FALLBACK=1")
        Map<DebugFacility, String> map;

        @Option(names = "-x")
        String x;

        @Parameters
        List<String> remainder;
    }

    @Test
    public void testIssue1904CollectionFallback_NoOptions() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904()); // options are not specified
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.DEFAULT), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.DEFAULT, "0"), obj.map);
    }

    @Test
    public void testIssue1904CollectionFallback_NoArgs() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904(), "--debug"); // no args specified
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.FALLBACK), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.DEFAULT, "0"), obj.map);
    }

    @Test
    public void testIssue1904CollectionFallback_NoArgs_map() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904(), "--map"); // no args specified
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.DEFAULT), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.FALLBACK, "1"), obj.map);
    }

    @Test
    public void testIssue1904CollectionFallback_otherOption() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904(), "--debug", "-x", "xarg"); // other option
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.FALLBACK), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.DEFAULT, "0"), obj.map);
    }

    @Test
    public void testIssue1904CollectionFallback_otherOption_map() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904(), "--map", "-x", "xarg"); // other option
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.DEFAULT), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.FALLBACK, "1"), obj.map);
    }

    @Test
    public void testIssue1904CollectionFallback_positional() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904(), "--debug", "123"); // positional
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.FALLBACK), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.DEFAULT, "0"), obj.map);
    }

    @Test
    public void testIssue1904CollectionFallback_positional_map() {
        Issue1904 obj = CommandLine.populateCommand(new Issue1904(), "--map", "123"); // positional
        assertEquals(Collections.singletonList(Issue1904.DebugFacility.DEFAULT), obj.facilities);
        assertEquals(Collections.singletonMap(Issue1904.DebugFacility.FALLBACK, "1"), obj.map);
    }
}
