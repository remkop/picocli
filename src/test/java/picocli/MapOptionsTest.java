package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.util.Map;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * Verifies https://github.com/remkop/picocli/issues/1214
 *
 * This test only uses Java 5 API.
 * Additional tests that use the Java 8 `java.util.Optional` API
 * can be found here: picocli-examples/src/test/java/picocli/MapOptionsOptionalTest.java.
 */
public class MapOptionsTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testErrorIfNoMapFallbackValue() {
        class App {
            @Option(names = "-D") Map<String, String> map;
        }
        try {
            CommandLine.populateCommand(new App(), "-Dkey");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Value for option option '-D' (<String=String>) should be in KEY=VALUE format but was key", ex.getMessage());
        }
    }

    @Test
    public void testErrorIfMapFallbackValueIsUnspecified() {
        class App {
            @Option(names = "-D", mapFallbackValue = "__unspecified__") Map<String, String> map;
        }
        try {
            CommandLine.populateCommand(new App(), "-Dkey");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Value for option option '-D' (<String=String>) should be in KEY=VALUE format but was key", ex.getMessage());
        }
    }

    @Test
    public void testMapFallbackValueEmptyString() {
        class App {
            @Option(names = "-D", mapFallbackValue = "") Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals("", app.map.get("key"));
    }

    @Test
    public void testOptionMapFallbackValueNull() {
        class App {
            @Option(names = "-D", mapFallbackValue = Option.NULL_VALUE) Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals(null, app.map.get("key"));
    }

    @Test
    public void testInheritedOptionMapFallbackValueEmptyString() {
        class App {
            @Option(names = "-D", mapFallbackValue = "", scope = INHERIT) Map<String, String> map;
        }
        @Command(name = "sub")
        class Sub {
        }
        App app = new App();
        Sub sub = new Sub();
        new CommandLine(app)
                .addSubcommand(sub)
                .parseArgs("sub", "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals("", app.map.get("key"));
    }

    @Test
    public void testInheritedOptionMapFallbackValueNull() {
        class App {
            @Option(names = "-D", mapFallbackValue = Option.NULL_VALUE, scope = INHERIT) Map<String, String> map;
        }
        @Command(name = "sub")
        class Sub {
        }
        App app = new App();
        Sub sub = new Sub();
        new CommandLine(app)
                .addSubcommand(sub)
                .parseArgs("sub", "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals(null, app.map.get("key"));
    }

    @Test
    public void testPositionalMapFallbackValueNull() {
        class App {
            @Parameters(mapFallbackValue = Option.NULL_VALUE) Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "key");
        assertEquals(1, app.map.size());
        assertEquals(null, app.map.get("key"));
    }

    @Test
    public void testInheritedPositionalMapFallbackValueNull() {
        class App {
            @Parameters(mapFallbackValue = Option.NULL_VALUE, scope = INHERIT) Map<String, String> map;
        }
        @Command(name = "sub")
        class Sub {
        }
        App app = new App();
        Sub sub = new Sub();
        new CommandLine(app)
                .addSubcommand(sub)
                .parseArgs("sub", "key");
        assertEquals(1, app.map.size());
        assertEquals(null, app.map.get("key"));
    }

    @Test
    public void testMapFallbackValueEmptyStringMultiple() {
        class App {
            @Option(names = "-D", mapFallbackValue = "") Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2", "-Dkey3");
        assertEquals(3, app.map.size());
        assertEquals("", app.map.get("key1"));
        assertEquals("", app.map.get("key2"));
        assertEquals("", app.map.get("key3"));
    }

    @Test
    public void testOptionMapFallbackValueNullMultiple() {
        class App {
            @Option(names = "-D", mapFallbackValue = Option.NULL_VALUE) Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2", "-Dkey3");
        assertEquals(3, app.map.size());
        assertEquals(null, app.map.get("key1"));
        assertEquals(null, app.map.get("key2"));
        assertEquals(null, app.map.get("key3"));
    }

    @Test
    public void testParametersMapFallbackValueNullMultiple() {
        class App {
            @Parameters(mapFallbackValue = Option.NULL_VALUE) Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "key1", "key2", "key3");
        assertEquals(3, app.map.size());
        assertEquals(null, app.map.get("key1"));
        assertEquals(null, app.map.get("key2"));
        assertEquals(null, app.map.get("key3"));
    }

    @Test
    public void testTypeConversionErrorIfValueCannotBeConverted() {
        class App {
            @Option(names = "-D", mapFallbackValue = "") Map<String, Integer> map;
        }
        try {
            CommandLine.populateCommand(new App(), "-Dkey");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Invalid value for option '-D' (<String=Integer>): '' is not an int", ex.getMessage());
        }
    }

    @Test
    public void testMapFallbackValueDescriptionVariable() {
        class App {
            @Parameters(mapFallbackValue = Option.NULL_VALUE, description = "Positional ${MAP-FALLBACK-VALUE} blah.")
            Map<String, String> map;

            @Option(names = "-D", mapFallbackValue = "", description = "Option '${MAP-FALLBACK-VALUE}' blah.")
            Map<String, Integer> properties;

            @Option(names = "-X", mapFallbackValue = "xx", description = "XX '${MAP-FALLBACK-VALUE}' blah.")
            Map<String, Integer> x;
        }
        String usage = new CommandLine(new App()).getUsageMessage();
        String expected = String.format("" +
                "Usage: <main class> [-D=<String=Integer>]... [-X=<String=Integer>]...%n" +
                "                    [<String=String>...]%n" +
                "      [<String=String>...]   Positional null blah.%n" +
                "  -D=<String=Integer>        Option '' blah.%n" +
                "  -X=<String=Integer>        XX 'xx' blah.%n" +
                "");
        assertEquals(expected, usage);
    }
}
