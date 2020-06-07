package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.ParserSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Range;
import picocli.CommandLine.UnmatchedArgumentException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class ArgSplitTest {
    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testSplitInOptionArray() {
        class Args {
            @Option(names = "-a", split = ",") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "-a=B", "-a", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "a,b,c", "-a", "B", "-a", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "-a", "B", "-a", "C", "-a", "D,E,F");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C", "D", "E", "F"}, args.values);

        try {
            CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments from index 1: 'B', 'C'", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "-a=C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument at index 1: 'B'", ok.getMessage());
        }
    }

    @Test
    public void testSplitInOptionArrayWithSpaces() {
        class Args {
            @Option(names = "-a", split = " ") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=\"a b c\"");
        assertArrayEquals(new String[] {"\"a b c\""}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a b c", "-a", "B", "-a", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "\"a b c\"", "-a=B", "-a=C");
        assertArrayEquals(new String[] {"\"a b c\"", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=\"a b c\"", "-a=B", "-a", "C", "-a=D E F");
        assertArrayEquals(new String[] {"\"a b c\"", "B", "C", "D", "E", "F"}, args.values);

        try {
            CommandLine.populateCommand(new Args(), "-a=a b c", "B", "C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments from index 1: 'B', 'C'", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new Args(), "-a=a b c", "B", "-a=C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument at index 1: 'B'", ok.getMessage());
        }
    }

    @Test
    public void testSplitInOptionArrayWithArity() {
        class Args {
            @Option(names = "-a", split = ",", arity = "0..4") String[] values;
            @Parameters() String[] params;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c"); // 1 args
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a"); // 0 args
        assertArrayEquals(new String[0], args.values);
        assertNull(args.params);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C"); // 3 args
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);
        assertNull(args.params);

        args = CommandLine.populateCommand(new Args(), "-a", "a,b,c", "B", "C"); // 3 args
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);
        assertNull(args.params);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C", "D,E,F"); // 4 args
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C", "D", "E", "F"}, args.values);
        assertNull(args.params);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c,d", "B", "C", "D", "E,F"); // 5 args
        assertArrayEquals(new String[] {"a", "b", "c", "d", "B", "C", "D"}, args.values);
        assertArrayEquals(new String[] {"E,F"}, args.params);
    }

    @Test
    public void testSplitInOptionCollection() {
        class Args {
            @Option(names = "-a", split = ",")
            List<String> values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertEquals(Arrays.asList("a", "b", "c"), args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "-a", "B", "-a=C");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C"), args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "a,b,c", "-a", "B", "-a", "C");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C"), args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "-a", "B", "-a", "C", "-a", "D,E,F");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C", "D", "E", "F"), args.values);

        try {
            CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments from index 1: 'B', 'C'", ok.getMessage());
        }
    }

    @Test
    public void testSplitInParametersArray() {
        class Args {
            @Parameters(split = ",") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "a,b,c");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C", "D,E,F");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C", "D", "E", "F"}, args.values);
    }

    @Test
    public void testSplitInParametersArrayWithArity() {
        class Args {
            @Parameters(arity = "2..4", split = ",") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "a,b", "c,d"); // 2 args
        assertArrayEquals(new String[] {"a", "b", "c", "d"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b", "c,d", "e,f"); // 3 args
        assertArrayEquals(new String[] {"a", "b", "c", "d", "e", "f"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "d", "e,f"); // 4 args
        assertArrayEquals(new String[] {"a", "b", "c", "B", "d", "e", "f"}, args.values);
        try {
            CommandLine.populateCommand(new Args(), "a,b,c,d,e"); // 1 arg: should fail
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<values>) requires at least 2 values, but only 1 were specified: [a,b,c,d,e]", ex.getMessage());
            assertEquals(1, ex.getMissing().size());
            assertTrue(ex.getMissing().get(0).toString(), ex.getMissing().get(0) instanceof CommandLine.Model.PositionalParamSpec);
        }
        try {
            CommandLine.populateCommand(new Args(), "a,b,c", "B,C", "d", "e", "f,g"); // 5 args
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<values>) requires at least 2 values, but only 1 were specified: [f,g]", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new Args()); // 0 arg: should fail
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<values>) requires at least 2 values, but none were specified.", ex.getMessage());
        }
    }

    @Test
    public void testSplitInParametersCollection() {
        class Args {
            @Parameters(split = ",") List<String> values;
        }
        Args args = CommandLine.populateCommand(new Args(), "a,b,c");
        assertEquals(Arrays.asList("a", "b", "c"), args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C"), args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C"), args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C", "D,E,F");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C", "D", "E", "F"), args.values);
    }

    @Test
    public void testSplitIgnoredInOptionSingleValueFieldIfSystemPropertySet() {
        class Args {
            @Option(names = "-a", split = ",") String value;
        }
        System.setProperty("picocli.ignore.invalid.split", "");
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertEquals("a,b,c", args.value);
    }

    @Test
    public void testSplitDisallowedInOptionSingleValueField() {
        class Args {
            @Option(names = "-a", split = ",") String value;
        }
        System.clearProperty("picocli.ignore.invalid.split");
        try {
            new CommandLine(new Args());
            fail("Expected exception");
        } catch (CommandLine.InitializationException ex) {
            assertEquals("Only multi-value options and positional parameters should have a split regex (this check can be disabled by setting system property 'picocli.ignore.invalid.split')", ex.getMessage());
        }
    }

    @Test
    public void testSplitIgnoredInParameterSingleValueFieldIfSystemPropertySet() {
        class Args {
            @Parameters(split = ",") String value;
        }
        System.setProperty("picocli.ignore.invalid.split", "");
        Args args = CommandLine.populateCommand(new Args(), "a,b,c");
        assertEquals("a,b,c", args.value);
    }

    @Test
    public void testSplitDisallowedInParameterSingleValueField() {
        class Args {
            @Parameters(split = ",") String value;
        }
        System.clearProperty("picocli.ignore.invalid.split");
        try {
            new CommandLine(new Args());
            fail("Expected exception");
        } catch (CommandLine.InitializationException ex) {
            assertEquals("Only multi-value options and positional parameters should have a split regex (this check can be disabled by setting system property 'picocli.ignore.invalid.split')", ex.getMessage());
        }
    }

    @Test
    public void testMapFieldWithSplitRegex() {
        class App {
            @Option(names = "-fix", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
            private void validate() {
                assertEquals(10, message.size());
                assertEquals(LinkedHashMap.class, message.getClass());
                assertEquals("FIX.4.4", message.get(8));
                assertEquals("69", message.get(9));
                assertEquals("A", message.get(35));
                assertEquals("MBT", message.get(49));
                assertEquals("TargetCompID", message.get(56));
                assertEquals("9", message.get(34));
                assertEquals("20130625-04:05:32.682", message.get(52));
                assertEquals("0", message.get(98));
                assertEquals("30", message.get(108));
                assertEquals("052", message.get(10));
            }
        }
        CommandLine.populateCommand(new App(), "-fix", "8=FIX.4.4|9=69|35=A|49=MBT|56=TargetCompID|34=9|52=20130625-04:05:32.682|98=0|108=30|10=052").validate();
    }
    @Test
    public void testMapFieldArityWithSplitRegex() {
        class App {
            @Option(names = "-fix", arity = "2", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
            private void validate() {
                assertEquals(message.toString(), 4, message.size());
                assertEquals(LinkedHashMap.class, message.getClass());
                assertEquals("a", message.get(1));
                assertEquals("b", message.get(2));
                assertEquals("c", message.get(3));
                assertEquals("d", message.get(4));
            }
        }
        CommandLine.populateCommand(new App(), "-fix", "1=a", "2=b|3=c|4=d").validate(); // 2 args
        //Arity should not limit the total number of values put in an array or collection #191
        CommandLine.populateCommand(new App(), "-fix", "1=a", "2=b", "-fix", "3=c", "4=d").validate(); // 2 args

        try {
            CommandLine.populateCommand(new App(), "-fix", "1=a|2=b|3=c|4=d"); // 1 arg
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("option '-fix' at index 0 (<Integer=String>) requires at least 2 values, but only 1 were specified: [1=a|2=b|3=c|4=d]", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-fix", "1=a", "2=b", "3=c|4=d"); // 3 args
            fail("UnmatchedArgumentException expected");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 3: '3=c|4=d'", ex.getMessage());
        }
    }

    @Ignore("https://github.com/remkop/picocli/issues/765")
    @Test
    public void testSplitRegexWithEscapedCharacter() {
        class App {
            @Option(names = "x", split = "\\|")
            String[] parts = {};
        }
        String expected = String.format("" +
                "Usage: <main class> [x=<parts>[|<parts>...]]...%n" +
                "      x=<parts>[|<parts>...]%n" +
                "%n");
        String actual = new CommandLine(new App()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Ignore("https://github.com/remkop/picocli/issues/765")
    @Test
    public void testSplitRegexWithEscapedBackslash() {
        class App {
            @Option(names = "x", split = "\\\\")
            String[] parts = {};
        }
        String expected = String.format("" +
                "Usage: <main class> [x=<parts>[\\<parts>...]]...%n" +
                "      x=<parts>[\\<parts>...]%n" +
                "%n");
        String actual = new CommandLine(new App()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testArgSpecSplitValue_SplitsQuotedValuesIfConfigured() {
        ParserSpec parser = new ParserSpec().splitQuotedStrings(true);
        ArgSpec spec = PositionalParamSpec.builder().type(String[].class).splitRegex(",").build();
        String[] actual = spec.splitValue("a,b,\"c,d,e\",f", parser, Range.valueOf("0"), 0);
        assertArrayEquals(new String[]{"a", "b", "\"c", "d" , "e\"", "f"}, actual);
    }

    @Test
    public void testArgSpecSplitValue_RespectsQuotedValuesByDefault() {
        ParserSpec parser = new ParserSpec();
        ArgSpec spec = PositionalParamSpec.builder().type(String[].class).splitRegex(",").build();
        String[] actual = spec.splitValue("a,b,\"c,d,e\",f", parser, Range.valueOf("0"), 0);
        assertArrayEquals(new String[]{"a", "b", "\"c,d,e\"", "f"}, actual);
    }

    @Test
    public void testArgSpecSplitValue_MultipleQuotedValues() {
        ParserSpec parser = new ParserSpec();
        ArgSpec spec = PositionalParamSpec.builder().type(String[].class).splitRegex(",").build();
        String[] actual = spec.splitValue("a,b,\"c,d,e\",f,\"xxx,yyy\"", parser, Range.valueOf("0"), 0);
        assertArrayEquals(new String[]{"a", "b", "\"c,d,e\"", "f", "\"xxx,yyy\""}, actual);
    }

    @Test
    public void testArgSpecSplitValue_MultipleQuotedValues_QuotesTrimmedIfRequested() {
        ParserSpec parser = new ParserSpec().trimQuotes(true);
        ArgSpec spec = PositionalParamSpec.builder().type(String[].class).splitRegex(",").build();
        String[] actual = spec.splitValue("a,b,\"c,d,e\",f,\"xxx,yyy\"", parser, Range.valueOf("0"), 0);
        assertArrayEquals(new String[]{"a", "b", "c,d,e", "f", "xxx,yyy"}, actual);
    }

    @Test
    public void testParseQuotedArgumentWithNestedQuotes() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"-x", "a,b,\"c,d,e\",f,\"xxx,yyy\""};
        Example example = new Example();
        new CommandLine(example).parseArgs(args);
        assertEquals(Arrays.asList("a", "b", "\"c,d,e\"", "f", "\"xxx,yyy\""), example.parts);
    }

    @Test
    public void testParseQuotedArgumentWithNestedQuotesTrimQuotes() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"-x", "a,b,\"c,d,e\",f,\"xxx,yyy\""};
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals(Arrays.asList("a", "b", "c,d,e", "f", "xxx,yyy"), example.parts);
    }

    @Test
    public void testParseQuotedArgumentWithNestedQuotes2() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"-x", "\"-Dvalues=a,b,c\",\"-Dother=1,2\""};
        Example example = new Example();
        new CommandLine(example).parseArgs(args);
        assertEquals(Arrays.asList("\"-Dvalues=a,b,c\"", "\"-Dother=1,2\""), example.parts);
    }

    @Test
    public void testParseQuotedArgumentWithNestedQuotes2TrimQuotes() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"-x", "\"-Dvalues=a,b,c\",\"-Dother=1,2\""};
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals(Arrays.asList("-Dvalues=a,b,c", "-Dother=1,2"), example.parts);
    }

    @Test
    public void testParseQuotedOptionsWithNestedQuotes2TrimQuotes() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x\"", "\"-Dvalues=a,b,c\",\"-Dother=1,2\""};
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals(Arrays.asList("-Dvalues=a,b,c", "-Dother=1,2"), example.parts);
    }

    @Test
    public void testParseUnquotedOptionsWithAttachedUnescapedQuotedValuesTrimQuotes() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"-x=\"-Dvalues=a,b,c\",\"-Dother=1,2\""};
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals(Arrays.asList("-Dvalues=a,b,c", "-Dother=1,2"), example.parts);
    }

    @Test
    public void testParseQuotedOptionsWithAttachedUnescapedNestedQuotedValuesGivesError() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x=\"-Dvalues=a,b,c\",\"-Dother=1,2\"\""}; // "-x="-Dvalues=a,b,c","-Dother=1,2""
        Example example = new Example();
        try {
            new CommandLine(example).setTrimQuotes(true).parseArgs(args);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Unmatched argument at index 0: '\"-x=\"-Dvalues=a,b,c\",\"-Dother=1,2\"\"'", ex.getMessage());
        }
    }

    @Test
    public void testParseQuotedOptionsWithAttachedUnescapedNestedQuotedValuesGivesError2() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x=a,b,\"c,d,e\",f\""}; // "-x=a,b,"c,d,e",f"
        Example example = new Example();
        try {
            new CommandLine(example).setTrimQuotes(true).parseArgs(args);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Unmatched argument at index 0: '\"-x=a,b,\"c,d,e\",f\"'", ex.getMessage());
        }
    }

    @Test
    public void testParseQuotedOptionsWithAttachedEscapedNestedQuotedValues() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x=a,b,\\\"c,d,e\\\",f\""}; // "-x=a,b,\"c,d,e\",f"
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals(Arrays.asList("a", "b", "c,d,e", "f"), example.parts);
    }

    @Test
    public void testParseQuotedOptionsWithAttachedEscapedDoublyNestedQuotedValues_givesSingleValue() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x=\\\"a,b,\\\\\"c,d,e\\\\\",f\\\"\""}; // "-x=\"a,b,\\"c,d,e\\",f\""
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals("One value", Arrays.asList("a,b,\"c,d,e\",f"), example.parts);
    }

    @Test
    public void testParseQuotedOptionsWithEscapedDoublyNestedQuotedValuesMultiArity() {
        class Example {
            @Option(names = "-x", split = ",", arity = "*")
            List<String> parts;
        }
        // "-x=\"a,b,\\"c,d,e\\",f\"" "x,y,z" "\"1,2,3\"" "\\"1,2,3\\""
        // "-x=a,b,\"c,d,e\",f"       -> gives 4 values: 'a'; 'b'; 'c,d,e'; and 'f'
        // "-x=\"a,b,\\"c,d,e\\",f\"" -> gives 1 value : 'a,b,"c,d,e",f'
        // "\"\\"1,2,3\\"\""
        String[] args = {"\"-x=\\\"a,b,\\\\\"c,d,e\\\\\",f\\\"\"", "\"x,y,z\"", "\"\\\"1,2,3\\\"\"", "\"\\\"\\\\\"1,2,3\\\\\"\\\"\""};
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals("One value", Arrays.asList("a,b,\"c,d,e\",f", "x", "y", "z", "1,2,3", "\"1,2,3\""), example.parts);
    }

    @Test
    public void testParseQuotedOptionsWithSeparateEscapedNestedQuotedValues2() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x\"", "\"a,b,\\\"c,d,e\\\",f\""}; // "-x" "a,b,\"c,d,e\",f"
        Example example = new Example();
//        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
//        assertEquals(Arrays.asList("a", "b", "c,d,e", "f"), example.parts);

        args = new String[]{"\"-x\"", "\"\\\"\\\\\"1,2,3\\\\\"\\\"\""}; // "\"\\"1,2,3\\"\""
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals(Arrays.asList("\"1,2,3\""), example.parts);
    }

    @Test
    public void testParseQuotedOptionsWithSeparateQuotedValueWithEscapedNestedQuotedValues2() {
        class Example {
            @Option(names = "-x", split = ",")
            List<String> parts;
        }
        String[] args = {"\"-x\"", "\"\\\"a,b,\\\\\"c,d,e\\\\\",f\\\"\""}; // "-x" "\"a,b,\\"c,d,e\\",f\""
        Example example = new Example();
        new CommandLine(example).setTrimQuotes(true).parseArgs(args);
        assertEquals("One value", Arrays.asList("a,b,\"c,d,e\",f"), example.parts);
    }


    // test https://github.com/remkop/picocli/issues/379
    @Test
    public void test379() {
        String[] args = {
                "-p",
                "AppOptions=\"-Dspring.profiles.active=foo,bar -Dspring.mail.host=smtp.mailtrap.io\",OtherOptions=\"\""
        };
        class App {
            @Option(names = {"-p", "--parameter"}, split = ",")
            Map<String, String> parameters;
        }
        App app = new App();
        new CommandLine(app).parseArgs(args);
        assertEquals(2, app.parameters.size());
        assertEquals("\"-Dspring.profiles.active=foo,bar -Dspring.mail.host=smtp.mailtrap.io\"", app.parameters.get("AppOptions"));
        assertEquals("\"\"", app.parameters.get("OtherOptions"));
    }
    @Test
    public void test379WithTrimQuotes() {
        String[] args = {
                "-p",
                "AppOptions=\"-Dspring.profiles.active=foo,bar -Dspring.mail.host=smtp.mailtrap.io\",OtherOptions=\"\""
        };
        class App {
            @Option(names = {"-p", "--parameter"}, split = ",")
            Map<String, String> parameters;
        }
        App app = new App();
        new CommandLine(app).setTrimQuotes(true).parseArgs(args);
        assertEquals(2, app.parameters.size());
        assertEquals("-Dspring.profiles.active=foo,bar -Dspring.mail.host=smtp.mailtrap.io", app.parameters.get("AppOptions"));
        assertEquals("", app.parameters.get("OtherOptions"));
    }

    @Test
    public void testSmartUnquote() {
        assertEquals("-x=\"a,b,\\\"c,d,e\\\",f\"", CommandLine.smartUnquote("\"-x=\\\"a,b,\\\\\"c,d,e\\\\\",f\\\"\""));
    }

    @Test
    public void testArgSpecSplitValueDebug() {
        PositionalParamSpec positional = PositionalParamSpec.builder().type(String[].class).splitRegex("b").build();

        System.setProperty("picocli.trace", "DEBUG");
        String[] values = positional.splitValue("abc", new CommandLine.Model.ParserSpec().splitQuotedStrings(true), CommandLine.Range.valueOf("1"), 1);
        System.clearProperty("picocli.trace");

        assertArrayEquals(new String[] {"a", "c"}, values);
    }

    @Test
    public void testArgSpecSplitWithEscapedBackslashInsideQuote() {
        PositionalParamSpec positional = PositionalParamSpec.builder().type(String[].class).splitRegex(";").build();

        System.setProperty("picocli.trace", "DEBUG");
        String value = "\"abc\\\\\\\";def\"";
        String[] values = positional.splitValue(value, new CommandLine.Model.ParserSpec().splitQuotedStrings(false), CommandLine.Range.valueOf("1"), 1);
        System.clearProperty("picocli.trace");

        assertArrayEquals(new String[] {"\"abc\\\\\\\";def\""}, values);
    }

    @Test
    public void testArgSpecSplitWithEscapedBackslashOutsideQuote() {
        PositionalParamSpec positional = PositionalParamSpec.builder().type(String[].class).splitRegex(";").build();

        System.setProperty("picocli.trace", "DEBUG");
        String value = "\\\\\"abc\\\";def\";\\\"a\\";
        String[] values = positional.splitValue(value, new CommandLine.Model.ParserSpec().splitQuotedStrings(false), CommandLine.Range.valueOf("1"), 1);
        System.clearProperty("picocli.trace");

        assertArrayEquals(new String[] {"\\\\\"abc\\\";def\"", "\\\"a\\"}, values);
    }

    @Test
    public void testArgSpecSplitBalancedQuotedValueDebug() {
        PositionalParamSpec positional = PositionalParamSpec.builder().type(String[].class).splitRegex(";").build();

        System.setProperty("picocli.trace", "DEBUG");
        String value = "\"abc\\\";def\"";
        String[] values = positional.splitValue(value, new CommandLine.Model.ParserSpec().splitQuotedStrings(false), CommandLine.Range.valueOf("1"), 1);
        System.clearProperty("picocli.trace");

        assertArrayEquals(new String[] {"\"abc\\\";def\""}, values);
    }

    @Test
    public void testArgSpecSplitUnbalancedQuotedValueDebug() {
        PositionalParamSpec positional = PositionalParamSpec.builder().type(String[].class).splitRegex(";").build();

        System.setProperty("picocli.trace", "DEBUG");
        String value = "\"abc\\\";def";
        String[] values = positional.splitValue(value, new CommandLine.Model.ParserSpec().splitQuotedStrings(false), CommandLine.Range.valueOf("1"), 1);
        System.clearProperty("picocli.trace");

        assertArrayEquals(new String[] {"\"abc\\\"", "def"}, values);
    }
    @Test
    public void testQuotedMapKeysDefault() {
        class App {
            @Option(names = "-e")
            Map<String, String> runtimeParams = new HashMap<String, String>();
        }

        App app = new App();
        new CommandLine(app).parseArgs("-e", "\"a=b=c\"=foo");
        assertTrue(app.runtimeParams.containsKey("\"a=b=c\""));
        assertEquals("foo", app.runtimeParams.get("\"a=b=c\""));

        new CommandLine(app).parseArgs("-e", "\"a=b=c\"=\"x=y=z\"");
        assertTrue(app.runtimeParams.containsKey("\"a=b=c\""));
        assertEquals("\"x=y=z\"", app.runtimeParams.get("\"a=b=c\""));
    }
    @Test
    public void testQuotedMapKeysDefaultWithSplit() {
        class App {
            @Option(names = "-e", split = ",")
            Map<String, String> map = new HashMap<String, String>();
        }

        App app = new App();
        new CommandLine(app).parseArgs("-e", "\"a=b=c\"=foo,\"d=e=f\"=bar");
        assertTrue(app.map.containsKey("\"a=b=c\""));
        assertTrue(app.map.containsKey("\"d=e=f\""));
        assertEquals("foo", app.map.get("\"a=b=c\""));
        assertEquals("bar", app.map.get("\"d=e=f\""));

        new CommandLine(app).parseArgs("-e", "\"a=b=c\"=\"x=y=z\",\"d=e=f\"=\"x2=y2\"");
        assertTrue(app.map.containsKey("\"a=b=c\""));
        assertTrue(app.map.containsKey("\"d=e=f\""));
        assertEquals("\"x=y=z\"", app.map.get("\"a=b=c\""));
        assertEquals("\"x2=y2\"", app.map.get("\"d=e=f\""));
    }
    @Test
    public void testQuotedMapKeysTrimQuotes() {
        class App {
            @Option(names = "-e")
            Map<String, String> map = new HashMap<String, String>();
        }

        App app = new App();
        new CommandLine(app).setTrimQuotes(true).parseArgs("-e", "\"a=b=c\"=foo");
        assertTrue(app.map.toString(), app.map.containsKey("a=b=c"));
        assertEquals("foo", app.map.get("a=b=c"));

        new CommandLine(app).setTrimQuotes(true).parseArgs("-e", "\"a=b=c\"=x=y=z");
        assertTrue(app.map.keySet().toString(), app.map.containsKey("a=b=c"));
        assertEquals("x=y=z", app.map.get("a=b=c"));
    }

    @Test
    public void testQuotedMapKeysTrimQuotesWithSplit() {
        class App {
            @Option(names = "-e", split = ",")
            Map<String, String> map = new HashMap<String, String>();
        }

        App app = new App();
        new CommandLine(app).setTrimQuotes(true).parseArgs("-e", "\"\\\"a=b=c\\\"=foo\",\"\\\"d=e=f\\\"=bar\"");
        assertTrue(app.map.containsKey("a=b=c"));
        assertTrue(app.map.containsKey("d=e=f"));
        assertEquals("foo", app.map.get("a=b=c"));
        assertEquals("bar", app.map.get("d=e=f"));

        new CommandLine(app).setTrimQuotes(true).parseArgs("-e", "\"\\\"a=b=c\\\"=x=y=z\",\"\\\"d=e=f\\\"=x2=y2\"");
        assertTrue(app.map.keySet().toString(), app.map.containsKey("a=b=c"));
        assertTrue(app.map.keySet().toString(), app.map.containsKey("d=e=f"));
        assertEquals("x=y=z", app.map.get("a=b=c"));
        assertEquals("x2=y2", app.map.get("d=e=f"));
    }
    @Test
    public void testQuotedMapKeysAndQuotedMapValuesNeedExtraQuotes() {
        class App {
            @Option(names = "-e")
            Map<String, String> map = new HashMap<String, String>();
        }

        App app = new App();
        new CommandLine(app).setTrimQuotes(true).parseArgs("-e", "\"\\\"a=b=c\\\"=\\\"x y z\\\"\"");
        assertTrue(app.map.keySet().toString(), app.map.containsKey("a=b=c"));
        assertEquals("x y z", app.map.get("a=b=c"));
    }

    @Test
    public void testQuotedMapKeysAndQuotedMapValuesNeedExtraQuotesWithSplit() {
        class App {
            @Option(names = "-e", split = ",")
            Map<String, String> map = new HashMap<String, String>();
        }

        App app = new App();
        // -e "\"a=b=c\"=\"x=y,z=0\"","\"d=e=f\"=\"x2 y2\""
        new CommandLine(app).setTrimQuotes(true).parseArgs("-e", "\"\\\"a=b=c\\\"=\\\"x=y,z=0\\\"\",\"\\\"d=e=f\\\"=\\\"x2 y2\\\"\"");
        assertTrue(app.map.keySet().toString(), app.map.containsKey("a=b=c"));
        assertTrue(app.map.keySet().toString(), app.map.containsKey("d=e=f"));
        assertEquals("x=y,z=0", app.map.get("a=b=c"));
        assertEquals("x2 y2", app.map.get("d=e=f"));
    }
}
