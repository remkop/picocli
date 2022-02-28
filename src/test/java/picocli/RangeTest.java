package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
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
        assertEquals(0, positionals.get(0).index().min());
        assertEquals(0, positionals.get(0).index().max());

        assertEquals("b", positionals.get(1).descriptionKey());
        assertEquals(1, positionals.get(1).index().min());
        assertEquals(1, positionals.get(1).index().max());

        assertEquals("c", positionals.get(2).descriptionKey());
        assertEquals(2, positionals.get(2).index().min());
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
        assertEquals(1, positionals.get(0).index().min());
        assertEquals(1, positionals.get(0).index().max());

        assertEquals("b", positionals.get(1).descriptionKey());
        assertEquals(4, positionals.get(1).index().min());
        assertEquals(4, positionals.get(1).index().max());

        assertEquals("c", positionals.get(2).descriptionKey());
        assertEquals(7, positionals.get(2).index().min());
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
                {"a1", "0+ (0)", 0, 0},
                {"a0", "1",      1, 1},
                {"a2", "1+ (2)", 2, 2},
                {"b0", "2",      2, 2},
                {"c",  "4+ (4)", 4, 4},
                {"e",  "7+ (7)", 7, 7},
                {"d",  "+ (8)",  8, 8},
        };

        CommandLine cmd = new CommandLine(new App());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        for (int i = 0; i < expect.length; i++) {
            PositionalParamSpec positional = positionals.get(i);
            CommandLine.Range index = positional.index();
            assertEquals(i + ": " + index.originalValue(), expect[i][0], positional.descriptionKey());
            assertEquals(i + ": " + index.originalValue(), expect[i][1], positional.index().internalToString());
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
            assertEquals("Command definition should have a positional parameter with index=0. " +
                    "Nearest positional parameter '<a0>' has index=1. " +
                    "(Full list: [1, 1+ (2), 1+ (3), 2, 4+ (4), 7+ (7), + (8)])", ex.getMessage());
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
                {"0",     "0",      0, 0},
                {"0+(1)", "0+ (1)", 1, 1},
                {"0+(2)", "0+ (2)", 2, 2},
                {"1",     "1",      1, 1},
                {"1+(1)", "1+ (2)", 2, 2},
                {"1+(2)", "1+ (3)", 3, 3},
                {"2",     "2",      2, 2},
                {"7+",    "7+ (7)", 7, 7},
                {"+(1)",  "+ (8)",  8, 8},
                {"+(2)",  "+ (9)",  9, 9},
        };

        CommandLine cmd = new CommandLine(new App());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        for (int i = 0; i < expect.length; i++) {
            PositionalParamSpec positional = positionals.get(i);
            CommandLine.Range index = positional.index();
            assertEquals(i + ": " + index.originalValue(), expect[i][0], positional.descriptionKey());
            assertEquals(i + ": " + index.originalValue(), expect[i][1], index.internalToString());
            assertEquals(i + ": " + positional.descriptionKey(), expect[i][2], index.min());
            assertEquals(i + ": " + positional.descriptionKey(), expect[i][3], index.max());
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
            after.add(positional.index().internalToString() + " @" + positional.descriptionKey());
        }
        List<String> expected = Arrays.asList(
                "0 @2",
                "0+ (0) @5",
                "0+ (0) @9",
                "1 @1",
                "1+ (1) @0",
                "1+ (1) @4",
                "2 @3",
                "7+ (7) @7",
                "+ (+) @6",
                "+ (+) @8"
        );
        assertEquals(expected, after);

        // now verify how the sort order is adjusted when the positionals are added to a CommandSpec
        CommandSpec spec = CommandSpec.create();
        for (PositionalParamSpec positional : positionals) {
            spec.addPositional(positional);
        }
        after.clear();
        for (PositionalParamSpec positional : spec.positionalParameters()) {
            after.add(positional.index().internalToString() + " @" + positional.descriptionKey());
        }
        expected = Arrays.asList(
                "0 @2",
                "0+ (1) @5", // adjusted 0 -> 1
                "0+ (2) @9", // adjusted 0 -> 2
                "1 @1",
                "1+ (2) @0", // adjusted 1 -> 2
                "1+ (3) @4", // adjusted 1 -> 3
                "2 @3",
                "7+ (7) @7",
                "+ (8) @6", // adjusted 0..* -> 8
                "+ (9) @8"  // adjusted 0..* -> 9
        );
        assertEquals(expected, after);
    }

    @Test
    public void testPositionalParametersSorter2() {
        String[] indices = new String[] {
                "+",
                "1",
                "+"};

        int i = 0;
        List<PositionalParamSpec> positionals = new ArrayList<PositionalParamSpec>();
        for (String index : indices) {
            positionals.add(PositionalParamSpec.builder().index(index).descriptionKey(String.valueOf(i++)).build());
        }
        Collections.sort(positionals, new CommandLine.PositionalParametersSorter());
        List<String> after = new ArrayList<String>();
        for (PositionalParamSpec positional : positionals) {
            after.add(positional.index().internalToString() + " @" + positional.descriptionKey());
        }
        // indices are adjusted when positionals are added to a CommandSpec.
        List<String> expected = Arrays.asList(
                "1 @1",
                "+ (+) @0",
                "+ (+) @2"
        );
        assertEquals(expected, after);

        // now verify how the sort order is adjusted when the positionals are added to a CommandSpec
        CommandSpec spec = CommandSpec.create();
        for (PositionalParamSpec positional : positionals) {
            spec.addPositional(positional);
        }
        after.clear();
        for (PositionalParamSpec positional : spec.positionalParameters()) {
            after.add(positional.index().internalToString() + " @" + positional.descriptionKey());
        }
        expected = Arrays.asList(
                "1 @1",
                "+ (2) @0", // adjusted 0..* -> 2
                "+ (3) @2"  // adjusted 0..* -> 3
        );
        assertEquals(expected, after);
    }

    static class CommonMixinOne {
        @Parameters(index = "0+", paramLabel = "COMMON-PARAM-ONE")
        private String commonMixinOneParam;
    }

    static class CommonMixinTwo {
        @Parameters(index = "2+", paramLabel = "COMMON-PARAM-TWO")
        private String commonMixinTwoParam;
    }

    @Test
    // test for https://github.com/remkop/picocli/issues/564
    public void testMixinsWithVariableIndex() {
        @Command(name = "testCommand", description = "Example for issue 564")
        class TestCommand {

            @Mixin private CommonMixinOne myCommonMixinOne;

            @Parameters(index = "1", paramLabel = "TEST-COMMAND-PARAM")
            private String testCommandParam;

            @Mixin private CommonMixinTwo myCommonMixinTwo;
        }

        CommandLine cmd = new CommandLine(new TestCommand());

        String expected = String.format("" +
                "Usage: testCommand COMMON-PARAM-ONE TEST-COMMAND-PARAM COMMON-PARAM-TWO%n" +
                "Example for issue 564%n" +
                "      COMMON-PARAM-ONE%n" +
                "      TEST-COMMAND-PARAM%n" +
                "      COMMON-PARAM-TWO%n");
        String actual = cmd.getUsageMessage();
        assertEquals(expected, actual);
    }

    static class MixinOne {
        @Parameters(paramLabel = "COMMON-PARAM-ONE")
        private String commonMixinOneParam;
    }

    static class MixinTwo {
        @Parameters(index = "+", paramLabel = "COMMON-PARAM-TWO")
        private String commonMixinTwoParam;
    }

    @Test
    public void testDefaultRelativeIndex() {
        @Command(name = "testCommand", description = "Example for issue 564")
        class TestCommand {

            @Mixin private MixinOne myCommonMixinOne;

            @Parameters(index = "1", paramLabel = "TEST-COMMAND-PARAM")
            private String testCommandParam;

            @Mixin private MixinTwo myCommonMixinTwo;
        }

        CommandLine cmd = new CommandLine(new TestCommand());
        List<PositionalParamSpec> positionals = cmd.getCommandSpec().positionalParameters();
        Object[][] expected = new Object[][] {
                {"0", "0+ (0)", 0, 0, true },
                {"1", "1",      1, 1, false},
                {"2", "+ (2)", 2, 2, true },
        };
        for (int i = 0; i < positionals.size(); i++) {
            CommandLine.Range index = positionals.get(i).index();
            String desc = "pos@" + i + " (" + index.internalToString() + ")";
            assertEquals(desc, expected[i][0], index.toString());
            assertEquals(desc, expected[i][1], index.internalToString());
            assertEquals(desc, expected[i][2], index.min());
            assertEquals(desc, expected[i][3], index.max());
            assertEquals(desc, expected[i][4], index.isRelative());
        }

        String expectedUsage = String.format("" +
                "Usage: testCommand COMMON-PARAM-ONE TEST-COMMAND-PARAM COMMON-PARAM-TWO%n" +
                "Example for issue 564%n" +
                "      COMMON-PARAM-ONE%n" +
                "      TEST-COMMAND-PARAM%n" +
                "      COMMON-PARAM-TWO%n");
        String actual = cmd.getUsageMessage();
        assertEquals(expectedUsage, actual);
    }

    @Test
    public void testDefaultRange() {
        class R1 {
            @Parameters int a;
        }
        PositionalParamSpec positional = new CommandLine(new R1()).getCommandSpec().positionalParameters().get(0);
        CommandLine.Range range = positional.index();
        assertEquals(0, range.min());
        assertEquals(0, range.max());
        assertEquals(false, range.isUnspecified());
        assertEquals(true, range.isRelative());
        assertEquals(false, range.isVariable());
        assertEquals(false, range.isUnresolved());
        assertEquals("0", range.toString());
        assertEquals("0+ (0)", range.internalToString());
    }

    @Test
    public void testUserManualAutomaticIndexExample() {
        class AutomaticIndex {
            @Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
            List<String> allParameters; // no "index" attribute: captures _all_ arguments (as Strings)

            @Parameters String group;    // assigned index = "0"
            @Parameters String artifact; // assigned index = "1"
            @Parameters String version;  // assigned index = "2"
        }
        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        String[] args = { "info.picocli", "picocli", "4.3.0" };
        AutomaticIndex params = CommandLine.populateCommand(new AutomaticIndex(), args);

        assertEquals("info.picocli", params.group);
        assertEquals("picocli", params.artifact);
        assertEquals("4.3.0", params.version);
        assertEquals(Arrays.asList("info.picocli", "picocli", "4.3.0"), params.allParameters);
    }

    @Ignore("Methods ordering is not reliable")
    @Test
    public void testUserManualAutomaticIndexWithMethods() {
        class WithMethod {
            @Parameters void group(String s) {}    // assigned index = "0"
            @Parameters void artifact(String s) {} // assigned index = "1"
            @Parameters void version(String s) {}  // assigned index = "2"
        }
        CommandSpec spec = CommandSpec.forAnnotatedObject(new WithMethod());
        for (int i = 0; i < spec.positionalParameters().size(); i++) {
            PositionalParamSpec param = spec.positionalParameters().get(i);
            CommandLine.Range index = param.index();
            switch (i) {
                case 0:
                    assertEquals("<group>", param.paramLabel());
                    assertEquals(0, index.min());
                    assertEquals(0, index.max());
                    assertEquals("0+ (0)", index.internalToString());
                    break;
                case 1:
                    assertEquals("<artifact>", param.paramLabel());
                    assertEquals(1, index.min());
                    assertEquals(1, index.max());
                    assertEquals("0+ (1)", index.internalToString());
                    break;
                case 2:
                    assertEquals("<version>", param.paramLabel());
                    assertEquals(2, index.min());
                    assertEquals(2, index.max());
                    assertEquals("0+ (2)", index.internalToString());
                    break;
            }
        }
    }

    @Test
    public void testUserManualAnchoredIndexExample() {
        class ExplicitAndAutomaticIndexes {
            @Parameters(index = "0") String p0;
            @Parameters(index = "1") String p1;
            @Parameters String pAuto0;
            @Parameters String pAuto1;
            @Parameters(index = "1+") String pAnchored0;
            @Parameters(index = "1+") String pAnchored1;
        }

        CommandSpec spec = CommandSpec.forAnnotatedObject(new ExplicitAndAutomaticIndexes());
        for (int i = 0; i < spec.positionalParameters().size(); i++) {
            PositionalParamSpec param = spec.positionalParameters().get(i);
            CommandLine.Range index = param.index();
            switch (i) {
                case 0:
                    assertEquals("<p0>", param.paramLabel());
                    assertEquals(0, index.min());
                    assertEquals(0, index.max());
                    assertEquals("0", index.internalToString());
                    break;
                case 1:
                    assertEquals("<pAuto0>", param.paramLabel());
                    assertEquals(1, index.min());
                    assertEquals(1, index.max());
                    assertEquals("0+ (1)", index.internalToString());
                    break;
                case 2:
                    assertEquals("<pAuto1>", param.paramLabel());
                    assertEquals(2, index.min());
                    assertEquals(2, index.max());
                    assertEquals("0+ (2)", index.internalToString());
                    break;
                case 3:
                    assertEquals("<p1>", param.paramLabel());
                    assertEquals(1, index.min());
                    assertEquals(1, index.max());
                    assertEquals("1", index.internalToString());
                    break;
                case 4:
                    assertEquals("<pAnchored0>", param.paramLabel());
                    assertEquals(2, index.min());
                    assertEquals(2, index.max());
                    assertEquals("1+ (2)", index.internalToString());
                    break;
                case 5:
                    assertEquals("<pAnchored1>", param.paramLabel());
                    assertEquals(3, index.min());
                    assertEquals(3, index.max());
                    assertEquals("1+ (3)", index.internalToString());
                    break;
            }
        }
    }

    @Test
    public void testUserManualUnanchoredExample() {
        class Unanchored {
            @Parameters(index = "0") String p0;
            @Parameters(index = "1+") String p1; // assigned index = "1"
            @Parameters(index = "1+") String p2; // assigned index = "2"
            @Parameters(index = "3") String p3;
            @Parameters(index = "+") String p4; // assigned index = "4" <-- unanchored
            @Parameters(index = "+") String p5; // assigned index = "5" <-- unanchored
        }

        CommandSpec spec = CommandSpec.forAnnotatedObject(new Unanchored());
        for (int i = 0; i < spec.positionalParameters().size(); i++) {
            PositionalParamSpec param = spec.positionalParameters().get(i);
            CommandLine.Range index = param.index();
            switch (i) {
                case 0:
                    assertEquals("<p0>", param.paramLabel());
                    assertEquals(0, index.min());
                    assertEquals(0, index.max());
                    assertEquals("0", index.internalToString());
                    break;
                case 1:
                    assertEquals("<p1>", param.paramLabel());
                    assertEquals(1, index.min());
                    assertEquals(1, index.max());
                    assertEquals("1+ (1)", index.internalToString());
                    break;
                case 2:
                    assertEquals("<p2>", param.paramLabel());
                    assertEquals(2, index.min());
                    assertEquals(2, index.max());
                    assertEquals("1+ (2)", index.internalToString());
                    break;
                case 3:
                    assertEquals("<p3>", param.paramLabel());
                    assertEquals(3, index.min());
                    assertEquals(3, index.max());
                    assertEquals("3", index.internalToString());
                    break;
                case 4:
                    assertEquals("<p4>", param.paramLabel());
                    assertEquals(4, index.min());
                    assertEquals(4, index.max());
                    assertEquals("+ (4)", index.internalToString());
                    break;
                case 5:
                    assertEquals("<p5>", param.paramLabel());
                    assertEquals(5, index.min());
                    assertEquals(5, index.max());
                    assertEquals("+ (5)", index.internalToString());
                    break;
            }
        }
    }

    @Test
    public void testUnanchoredWithVariable_AssignedIndex() {
        class UnanchoredWithVariable {
            @Parameters(index = "0..*") List<String> all;
            @Parameters(index = "+") String p1;
            @Parameters(index = "+") String p2;
        }

        CommandSpec spec = CommandSpec.forAnnotatedObject(new UnanchoredWithVariable());
        for (int i = 0; i < spec.positionalParameters().size(); i++) {
            PositionalParamSpec param = spec.positionalParameters().get(i);
            CommandLine.Range index = param.index();
            switch (i) {
                case 0:
                    assertEquals("<all>", param.paramLabel());
                    assertEquals(0, index.min());
                    assertEquals(Integer.MAX_VALUE, index.max());
                    assertEquals("0..*", index.internalToString());
                    break;
                case 1:
                    assertEquals("<p1>", param.paramLabel());
                    assertEquals(Integer.MAX_VALUE, index.min());
                    assertEquals(Integer.MAX_VALUE, index.max());
                    assertEquals("+ (+)", index.internalToString());
                    break;
                case 2:
                    assertEquals("<p2>", param.paramLabel());
                    assertEquals(Integer.MAX_VALUE, index.min());
                    assertEquals(Integer.MAX_VALUE, index.max());
                    assertEquals("+ (+)", index.internalToString());
                    break;
            }
        }
    }

    @Test
    public void testUnanchoredWithVariable_ParseResult() {
        class UnanchoredAfterOpenEndedIndex {
            @Parameters(index = "0..*") List<String> all;
            @Parameters(index = "+") String last;
        }

        try {
            CommandLine.populateCommand(new UnanchoredAfterOpenEndedIndex(), "a", "b", "c");
            fail("Expected exception");
        } catch (CommandLine.MissingParameterException ex) {
            assertEquals("Missing required parameter: '<last>'", ex.getMessage());
        }
    }

}
