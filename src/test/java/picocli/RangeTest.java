package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Parameters;

import java.util.List;

import static org.junit.Assert.*;

public class RangeTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testRelativeRangeWithoutAnchor() {
        class App {
            @Parameters(index = "+", descriptionKey = "a") String a;
            @Parameters(index = "+", descriptionKey = "b") String b;
            @Parameters(index = "+", descriptionKey = "c") String c;
        }

        CommandLine cmd = new CommandLine(new App());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        assertEquals("a", positionals.get(0).descriptionKey());
        assertEquals("b", positionals.get(1).descriptionKey());
        assertEquals("c", positionals.get(2).descriptionKey());
        assertEquals(0, positionals.get(0).index().min());
        assertEquals(1, positionals.get(1).index().min());
        assertEquals(2, positionals.get(2).index().min());
        assertEquals(0, positionals.get(0).index().max());
        assertEquals(1, positionals.get(1).index().max());
        assertEquals(2, positionals.get(2).index().max());
    }

    @Test
    public void testRelativeRangeWithAnchor() {
        class App {
            @Parameters(index = "1+", descriptionKey = "a") String a;
            @Parameters(index = "4+", descriptionKey = "b") String b;
            @Parameters(index = "7+", descriptionKey = "c") String c;
        }

        CommandLine cmd = new CommandLine(new App());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        assertEquals("a", positionals.get(0).descriptionKey());
        assertEquals("b", positionals.get(1).descriptionKey());
        assertEquals("c", positionals.get(2).descriptionKey());
        assertEquals(0, positionals.get(0).index().min());
        assertEquals(1, positionals.get(1).index().min());
        assertEquals(2, positionals.get(2).index().min());
        assertEquals(0, positionals.get(0).index().max());
        assertEquals(1, positionals.get(1).index().max());
        assertEquals(2, positionals.get(2).index().max());
    }
}
