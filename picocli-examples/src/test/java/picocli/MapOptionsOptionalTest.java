package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Option;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This test is located in the `picocli-examples` module because it uses the Java 8 `java.util.Optional` API.
 * See src/test/java/picocli/MapOptionsTest.java for the remaining tests.
 */
public class MapOptionsOptionalTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Ignore("Requires #1108")
    @Test
    public void testOptionalIfNoValue() {
        class App {
            @Option(names = "-D") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey");
        assertEquals(1, app.map.size());
        assertEquals(Optional.empty(), app.map.get("key"));
    }

    @Ignore("Requires #1108")
    @Test
    public void testOptionalWithValue() {
        class App {
            @Option(names = "-D") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey=value");
        assertEquals(1, app.map.size());
        assertEquals(Optional.of("value"), app.map.get("key"));
    }

    @Ignore("Requires #1108")
    @Test
    public void testOptionalIfNoValueMultiple() {
        class App {
            @Option(names = "-D") Map<String, Optional<String>> map;
        }
        App app = CommandLine.populateCommand(new App(), "-Dkey1", "-Dkey2");
        assertEquals(2, app.map.size());
        assertEquals(Optional.empty(), app.map.get("key1"));
        assertEquals(Optional.empty(), app.map.get("key2"));
    }

    @Ignore("Requires #1108")
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
}
