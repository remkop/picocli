package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This test is located in the `picocli-examples` module because it uses the Java 8 `java.util.Optional` API.
 * See src/test/java/picocli/MapOptionsTest.java for the remaining tests.
 */
public class MapOptionsOptionalTest {

    final static String UNSUPPORTED_TYPE_ERRORMSG = CommandLine.Model.RuntimeTypeInfo.ERRORMSG;

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testOptionalIfNoValue() {
        class App {
            @Option(names = "-D", mapFallbackValue = "") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals(Optional.of(""), app.map.get("key"));
    }

    @Test
    public void testOptionalEmptyIfNoValueWithFallbackNull() {
        class App {
            @Option(names = "-D", mapFallbackValue = Option.NULL_VALUE) Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals(Optional.empty(), app.map.get("key"));
    }

    @Test
    public void testOptionalWithValue() {
        class App {
            @Option(names = "-D") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey=value");
        assertEquals(1, app.map.size());
        assertEquals(Optional.of("value"), app.map.get("key"));
    }

    @Test
    public void testOptionalIfNoValueMultiple() {
        class App {
            @Option(names = "-D", mapFallbackValue = "") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2");
        assertEquals(2, app.map.size());
        assertEquals(Optional.of(""), app.map.get("key1"));
        assertEquals(Optional.of(""), app.map.get("key2"));
    }

    @Test
    public void testOptionalIfNoValueMultipleWithFallbackNull() {
        class App {
            @Option(names = "-D", mapFallbackValue = Option.NULL_VALUE) Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2");
        assertEquals(2, app.map.size());
        assertEquals(Optional.empty(), app.map.get("key1"));
        assertEquals(Optional.empty(), app.map.get("key2"));
    }

    @Test
    public void testOptionalWithValueMultiple() {
        class App {
            @Option(names = "-D") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1=val1", "-Dkey2=val2");
        assertEquals(2, app.map.size());
        assertEquals(Optional.of("val1"), app.map.get("key1"));
        assertEquals(Optional.of("val2"), app.map.get("key2"));
    }
    @Test
    public void testBooleanIfNoValueMultiple() {
        class App {
            @Option(names = "-E", mapFallbackValue = "true") Map<String, Boolean> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Ekey1", "-Ekey2");
        assertEquals(2, app.map.size());
        assertEquals(Boolean.TRUE, app.map.get("key1"));
        assertEquals(Boolean.TRUE, app.map.get("key2"));
    }

    @Test
    public void testOptionalIntegerIfNoValueMultiple() {
        class App {
            @Option(names = "-D", mapFallbackValue = Option.NULL_VALUE) Map<String, Optional<Integer>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2");
        assertEquals(2, app.map.size());
        assertEquals(Optional.empty(), app.map.get("key1"));
        assertEquals(Optional.empty(), app.map.get("key2"));
    }

    @Test
    public void testOptionalIntegerWithValueMultiple() {
        class App {
            @Option(names = "-D") Map<String, Optional<Integer>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1=123", "-Dkey2=456");
        assertEquals(2, app.map.size());
        assertEquals(Optional.of(123), app.map.get("key1"));
        assertEquals(Optional.of(456), app.map.get("key2"));
    }

    @Test
    public void testMapWithOptionalKeysNotSupported() {
        class App {
            @Option(names = "-D") Map<Optional<String>, Optional<Integer>> map;
        }
        try {
            App app = CommandLine.populateCommand(new App(), "-Dkey1=123", "-Dkey2=456");
            fail("Expected exception");
        } catch (Exception ex) {
            //String msg = String.format(UNSUPPORTED_TYPE_ERRORMSG, "java.util.Map<java.util.Optional<java.lang.String>, java.util.Optional<java.lang.Integer>>");
            String msg = "No TypeConverter registered for java.util.Optional of field java.util.Map<java.util.Optional<String>, java.util.Optional<Integer>> picocli.MapOptionsOptionalTest$10App.map";
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public void testListOfOptionalsNotSupported() {
        class App {
            @Option(names = "-X") List<Optional<Integer>> list;
        }
        try {
            App app = CommandLine.populateCommand(new App(), "-X123", "-X456");
            fail("Expected exception");
        } catch (Exception ex) {
            //String msg = String.format(UNSUPPORTED_TYPE_ERRORMSG, "java.util.List<java.util.Optional<java.lang.Integer>>");
            String msg = "PicocliException: Cannot create converter for types [class java.util.Optional] for field java.util.List<java.util.Optional<Integer>> picocli.MapOptionsOptionalTest$11App.list while processing argument at or before arg[0] '-X123' in [-X123, -X456]";
            assertTrue(ex.getMessage(), ex.getMessage().startsWith(msg));
        }
    }

    @Test
    public void testArrayOfOptionalsNotSupported() {
        class App {
            @Option(names = "-X") Optional<Integer>[] array;
        }
        try {
            App app = CommandLine.populateCommand(new App(), "-X123", "-X456");
            fail("Expected exception");
        } catch (Exception ex) {
            String msg = String.format(UNSUPPORTED_TYPE_ERRORMSG, "java.util.Optional<java.lang.Integer>[]");
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public void testMapFallbackValueDescriptionVariable() {
        class App {
            @CommandLine.Parameters(mapFallbackValue = Option.NULL_VALUE, description = "Positional ${MAP-FALLBACK-VALUE} blah.")
            Map<String, Optional<String>> map;

            @Option(names = "-D", mapFallbackValue = "", description = "Option '${MAP-FALLBACK-VALUE}' blah.")
            Map<String, Optional<String>> properties;

            @Option(names = "-X", mapFallbackValue = "xx", description = "XX '${MAP-FALLBACK-VALUE}' blah.")
            Map<String, Optional<Integer>> x;
        }
        String usage = new CommandLine(new App()).getUsageMessage();
        String expected = String.format("" +
                "Usage: <main class> [-D=<String=Optional>]... [-X=<String=Optional>]...%n" +
                "                    [<String=Optional>...]%n" +
                "      [<String=Optional>...]%n" +
                "                          Positional null blah.%n" +
                "  -D=<String=Optional>    Option '' blah.%n" +
                "  -X=<String=Optional>    XX 'xx' blah.%n" +
                "");
        assertEquals(expected, usage);
    }
}
