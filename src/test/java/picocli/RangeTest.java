package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        assertEquals(1, positionals.get(0).index().min());
        assertEquals(4, positionals.get(1).index().min());
        assertEquals(7, positionals.get(2).index().min());
        assertEquals(1, positionals.get(0).index().max());
        assertEquals(4, positionals.get(1).index().max());
        assertEquals(7, positionals.get(2).index().max());
    }

//    @junitparams.Parameters({
//    })
    @Test
    public void testRelativeRangeWithAnchorMixedWithAbsoluteIndex() {
        class App {
            @Parameters(index = "0+", descriptionKey = "a1") String a1;
            @Parameters(index = "1",  descriptionKey = "a0") String a0;
            @Parameters(index = "1+", descriptionKey = "a2") String a2;
            @Parameters(index = "2",  descriptionKey = "b0") String b0;
            @Parameters(index = "4+", descriptionKey = "c") String c;
            @Parameters(index = "+",  descriptionKey = "d") String e;
            @Parameters(index = "7+", descriptionKey = "e") String f;
        }

        Object[][] expect = {
                {"a1", "0 (0+)", 0, 0},
                {"a0", "1",      1, 1},
                {"a2", "2 (1+)", 2, 2},
                {"b0", "2",      2, 2},
                {"c",  "4 (4+)", 4, 4},
                {"e",  "7 (7+)", 7, 7},
                {"d",  "8 (+)",  8, 8},
        };

        CommandLine cmd = new CommandLine(new App());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        for (int i = 0; i < expect.length; i++) {
            PositionalParamSpec positional = positionals.get(i);
            CommandLine.Range index = positional.index();
            assertEquals(i + ": " + index.originalValue(), expect[i][0], positional.descriptionKey());
            assertEquals(i + ": " + index.originalValue(), expect[i][1], positional.index().toString());
            assertEquals(i + ": " + index.originalValue(), expect[i][2], index.min());
            assertEquals(i + ": " + index.originalValue(), expect[i][3], index.max());
        }
    }

    @Test
    public void testGapExceptionIfAnyAbsoluteIndex() {
        class App {
            @Parameters(index = "1+", descriptionKey = "a1") String a1;
            @Parameters(index = "1",  descriptionKey = "a0") String a0;
            @Parameters(index = "1+", descriptionKey = "a2") String a2;
            @Parameters(index = "2",  descriptionKey = "b0") String b0;
            @Parameters(index = "4+", descriptionKey = "c") String c;
            @Parameters(index = "+",  descriptionKey = "d") String e;
            @Parameters(index = "7+", descriptionKey = "e") String f;
        }
        try {
            new CommandLine(new App());
        } catch (CommandLine.ParameterIndexGapException ex) {
            assertEquals("Command definition should have a positional parameter with index=0. Nearest positional parameter '<a0>' has index=1", ex.getMessage());
        }

    }

    //@Ignore // TODO
    @Test
    public void testRelativeRangeWithAnchorOverlappingWithAbsoluteIndex() {
        class App {
            @Parameters(index = "1+", descriptionKey = "1+(1)") String a;
            @Parameters(index = "1",  descriptionKey = "1")     String b;
            @Parameters(index = "0",  descriptionKey = "0")     String c;
            @Parameters(index = "2",  descriptionKey = "2")     String d;
            @Parameters(index = "1+", descriptionKey = "1+(2)") String a2;
            @Parameters(index = "0+", descriptionKey = "0+(1)") String e;
            @Parameters(index = "+",  descriptionKey = "+(1)")  String f;
            @Parameters(index = "7+", descriptionKey = "7+")    String g;
            @Parameters(index = "+",  descriptionKey = "+(2)")  String h;
            @Parameters(index = "0+", descriptionKey = "0+(2)") String i;
        }

        Object[][] expect = {
                {"0",     0, 0},
                {"0+(1)", 1, 1},
                {"0+(2)", 2, 2},
                {"1",     1, 1},
                {"1+(1)", 2, 2},
                {"1+(2)", 3, 3},
                {"2",     2, 2},
                {"7+",    7, 7},
                {"+(1)",  8, 8},
                {"+(2)",  9, 9},
        };

        CommandLine cmd = new CommandLine(new App());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        for (int i = 0; i < expect.length; i++) {
            PositionalParamSpec positional = positionals.get(i);
            CommandLine.Range index = positional.index();
            assertEquals(i + ": " + index.originalValue(), expect[i][0], positional.descriptionKey());
            assertEquals(i + ": " + positional.descriptionKey(), expect[i][1], index.min());
            assertEquals(i + ": " + positional.descriptionKey(), expect[i][2], index.max());
        }
    }

    @Test
    public void testPositionalParametersSorter() {
        String[] indices = new String[] {
                "1+",
                "1",
                "0",
                "2",
                "1+",
                "0+",
                "+",
                "7+",
                "+",
                "0+",};

        int i = 0;
        List<PositionalParamSpec> positionals = new ArrayList<PositionalParamSpec>();
        for (String index : indices) {
            positionals.add(PositionalParamSpec.builder().index(index).descriptionKey(String.valueOf(i++)).build());
        }
        Collections.sort(positionals, new CommandLine.PositionalParametersSorter());
        List<String> after = new ArrayList<String>();
        for (PositionalParamSpec positional : positionals) {
            after.add(positional.index().toString() + " @" + positional.descriptionKey());
        }
        List<String> expected = Arrays.asList(
                "0 @2",
                "0 (0+) @5",
                "0 (0+) @9",
                "1 @1",
                "1 (1+) @0",
                "1 (1+) @4",
                "2 @3",
                "7 (7+) @7",
                "0..* (+) @6",
                "0..* (+) @8"
        );
        assertEquals(expected, after);
    }
}
