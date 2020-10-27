package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Map;

import static org.junit.Assert.*;

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
    public void testEmptyStringIfNoValue() {
        class App {
            @Option(names = "-D") Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals("", app.map.get("key"));
    }

    @Test
    public void testEmptyStringIfNoValueMultiple() {
        class App {
            @Option(names = "-D") Map<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2", "-Dkey3");
        assertEquals(3, app.map.size());
        assertEquals("", app.map.get("key1"));
        assertEquals("", app.map.get("key2"));
        assertEquals("", app.map.get("key3"));
    }

    @Test
    public void testTypeConversionErrorIfNoValue() {
        class App {
            @Option(names = "-D") Map<String, Integer> map;
        }
        try {
            CommandLine.populateCommand(new App(), "-Dkey");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Invalid value for option '-D' (<String=Integer>): '' is not an int", ex.getMessage());
        }
    }
}
