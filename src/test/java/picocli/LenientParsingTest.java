/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.*;

/**
 * Tests collecting errors instead of throwing them.
 */
public class LenientParsingTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();


    @Test
    public void testMultiValueOptionArityAloneIsInsufficient() throws Exception {
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.create();
        CommandLine.Model.OptionSpec option = CommandLine.Model.OptionSpec.builder("-c", "--count").arity("3").type(int.class).build();
        assertFalse(option.isMultiValue());

        spec.addOption(option);
        spec.parser().collectErrors(true);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "1", "2", "3");
        assertEquals(1, commandLine.getParseResult().errors().size());
        assertEquals("Unmatched arguments from index 2: '2', '3'", commandLine.getParseResult().errors().get(0).getMessage());
    }

    @Test
    public void testMultiValuePositionalParamArityAloneIsInsufficient() throws Exception {
        CommandLine.Model.CommandSpec spec = CommandLine.Model.CommandSpec.create();
        CommandLine.Model.PositionalParamSpec positional = CommandLine.Model.PositionalParamSpec.builder().index("0").arity("3").type(int.class).build();
        assertFalse(positional.isMultiValue());

        spec.addPositional(positional);
        spec.parser().collectErrors(true);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("1", "2", "3");
        assertEquals(1, commandLine.getParseResult().errors().size());
        assertEquals("Unmatched arguments from index 1: '2', '3'", commandLine.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testMissingRequiredParams() {
        class Example {
            @Parameters(index = "1", arity = "0..1") String optional;
            @Parameters(index = "0") String mandatory;
        }

        CommandLine cmd = new CommandLine(new Example());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs(new String[0]);

        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Missing required parameter: '<mandatory>'", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testMissingRequiredParams1() {
        class Tricky1 {
            @Parameters(index = "2") String anotherMandatory;
            @Parameters(index = "1", arity = "0..1") String optional;
            @Parameters(index = "0") String mandatory;
        }
        CommandLine cmd = new CommandLine(new Tricky1());
        cmd.getCommandSpec().parser().collectErrors(true);

        cmd.parseArgs(new String[0]);
        assertEquals(2, cmd.getParseResult().errors().size());
        assertEquals("Missing required parameters: '<mandatory>', '<anotherMandatory>'", cmd.getParseResult().errors().get(0).getMessage());
//        assertEquals("Missing required parameters: '<mandatory>', '<anotherMandatory>'", cmd.getParseResult().errors().get(1).getMessage());
        assertEquals("Missing required parameter: '<anotherMandatory>'", cmd.getParseResult().errors().get(1).getMessage());

        cmd.parseArgs(new String[] {"firstonly"});
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Missing required parameter: '<anotherMandatory>'", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testMissingRequiredParams2() {
        class Tricky2 {
            @Parameters(index = "2", arity = "0..1") String anotherOptional;
            @Parameters(index = "1", arity = "0..1") String optional;
            @Parameters(index = "0") String mandatory;
        }
        CommandLine cmd = new CommandLine(new Tricky2());
        cmd.getCommandSpec().parser().collectErrors(true);

        cmd.parseArgs(new String[0]);
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Missing required parameter: '<mandatory>'", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testMissingRequiredParamsWithOptions() {
        class Tricky3 {
            @Option(names="-v", required = true) boolean more;
            @Option(names="-t", required = true) boolean any;
            @Parameters(index = "1") String alsoMandatory;
            @Parameters(index = "0") String mandatory;
        }
        CommandLine cmd = new CommandLine(new Tricky3());
        cmd.getCommandSpec().parser().collectErrors(true);

        cmd.parseArgs(new String[] {"-t", "-v", "mandatory"});
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Missing required parameter: '<alsoMandatory>'", cmd.getParseResult().errors().get(0).getMessage());

        cmd.parseArgs(new String[] {"-t"});
        assertEquals(3, cmd.getParseResult().errors().size());
        assertEquals("Missing required options and parameters: '-v', '<mandatory>', '<alsoMandatory>'", cmd.getParseResult().errors().get(0).getMessage());
//        assertEquals("Missing required options and parameters: '-v', '<mandatory>', '<alsoMandatory>'", cmd.getParseResult().errors().get(1).getMessage());
//        assertEquals("Missing required options and parameters: '-v', '<mandatory>', '<alsoMandatory>'", cmd.getParseResult().errors().get(2).getMessage());
        assertEquals("Missing required parameters: '<mandatory>', '<alsoMandatory>'", cmd.getParseResult().errors().get(1).getMessage());
        assertEquals("Missing required parameter: '<alsoMandatory>'", cmd.getParseResult().errors().get(2).getMessage());
    }
    @Test
    public void testMissingRequiredParamWithOption() {
        class Tricky3 {
            @Option(names="-t") boolean any;
            @Parameters(index = "0") String mandatory;
        }
        CommandLine cmd = new CommandLine(new Tricky3());
        cmd.getCommandSpec().parser().collectErrors(true);

        cmd.parseArgs(new String[] {"-t"});
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Missing required parameter: '<mandatory>'", cmd.getParseResult().errors().get(0).getMessage());
    }

    @Test
    public void testNonVarargArrayParametersWithArity0() {
        class NonVarArgArrayParamsZeroArity {
            @Parameters(arity = "0")
            List<String> params;
        }
        CommandLine cmd = new CommandLine(new NonVarArgArrayParamsZeroArity());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("a", "b", "c");
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Unmatched arguments from index 0: 'a', 'b', 'c'", cmd.getParseResult().errors().get(0).getMessage());

        cmd.parseArgs("a");
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Unmatched argument at index 0: 'a'", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testSingleValueFieldDefaultMinArityIsOne() {
        class App {
            @Option(names = "-boolean")       boolean booleanField;
            @Option(names = "-Long")          Long aLongField;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-Long", "-boolean");
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Expected parameter for option '-Long' but found '-boolean'", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testBooleanOptionsArity0_nFailsIfAttachedParamNotABoolean() { // ignores varargs
        CommandLine cmd = new CommandLine(new ArityTest.BooleanOptionsArity0_nAndParameters());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-bool=123 -other".split(" "));
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Invalid value for option '-bool': '123' is not a boolean", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABoolean() { // ignores varargs
        CommandLine cmd = new CommandLine(new ArityTest.BooleanOptionsArity0_nAndParameters());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-rv234 -bool".split(" "));
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Unknown option: '-234' (while processing option: '-rv234')", cmd.getParseResult().errors().get(0).getMessage());
    }

    @Test
    public void test365_StricterArityValidation() {
        class Cmd {
            @Option(names = "-a", arity = "2") String[] a;
            @Option(names = "-b", arity = "1..2") String[] b;
            @Option(names = "-c", arity = "2..3") String[] c;
            @Option(names = "-v") boolean verbose;
        }
        assertMissing(Arrays.asList("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-a'",
                "option '-a' at index 0 (<a>) requires at least 2 values, but only 1 were specified: [2]",
                "Unmatched argument at index 3: '2'"),
                new Cmd(), "-a", "1", "-a", "2");

        assertMissing(Arrays.asList("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-v'"),
                new Cmd(), "-a", "1", "-v");

        assertMissing(Arrays.asList("Expected parameter for option '-b' but found '-v'"),
                new Cmd(), "-b", "-v");

        assertMissing(Arrays.asList("option '-c' at index 0 (<c>) requires at least 2 values, but only 1 were specified: [-a]",
                "option '-a' at index 0 (<a>) requires at least 2 values, but none were specified."),
                new Cmd(), "-c", "-a");

        assertMissing(Arrays.asList("Expected parameter 1 (of 2 mandatory parameters) for option '-c' but found '-a'"),
                new Cmd(), "-c", "-a", "1", "2");

        assertMissing(Arrays.asList("Expected parameter 2 (of 2 mandatory parameters) for option '-c' but found '-a'",
                "option '-a' at index 0 (<a>) requires at least 2 values, but none were specified."),
                new Cmd(), "-c", "1", "-a");
    }

    @Test
    public void test365_StricterArityValidationWithMaps() {
        class Cmd {
            @Option(names = "-a", arity = "2")
            Map<String,String> a;
            @Option(names = "-b", arity = "1..2") Map<String,String> b;
            @Option(names = "-c", arity = "2..3") Map<String,String> c;
            @Option(names = "-v") boolean verbose;
        }
        assertMissing(Arrays.asList("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-a'",
                "option '-a' at index 0 (<String=String>) requires at least 2 values, but only 1 were specified: [C=D]",
                "Unmatched argument at index 3: 'C=D'"),
                new Cmd(), "-a", "A=B", "-a", "C=D");

        assertMissing(Arrays.asList("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-v'"),
                new Cmd(), "-a", "A=B", "-v");

        assertMissing(Arrays.asList("Expected parameter for option '-b' but found '-v'"),
                new Cmd(), "-b", "-v");

        assertMissing(Arrays.asList("option '-c' at index 0 (<String=String>) requires at least 2 values, but only 1 were specified: [-a]",
                "option '-a' at index 0 (<String=String>) requires at least 2 values, but none were specified."),
                new Cmd(), "-c", "-a");

        assertMissing(Arrays.asList("Expected parameter 1 (of 2 mandatory parameters) for option '-c' but found '-a'"),
                new Cmd(), "-c", "-a", "A=B", "C=D");

        assertMissing(Arrays.asList("Expected parameter 2 (of 2 mandatory parameters) for option '-c' but found '-a'",
                "option '-a' at index 0 (<String=String>) requires at least 2 values, but none were specified."),
                new Cmd(), "-c", "A=B", "-a");
    }
    private void assertMissing(List<String> expected, Object command, String... args) {
        CommandLine cmd = new CommandLine(command);
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs(args);
        List<Exception> errors = cmd.getParseResult().errors();
        assertEquals(errors.toString(), expected.size(), errors.size());
        int i = 0;
        for (String msg : expected) {
            assertEquals(msg, errors.get(i++).getMessage());
        }
    }
    @Test
    public void testEnumListTypeConversionFailsForInvalidInput() {
        CommandLine cmd = new CommandLine(new TypeConversionTest.EnumParams());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-timeUnitList", "SECONDS", "b", "c");
        assertEquals(2, cmd.getParseResult().errors().size());
        Exception ex = cmd.getParseResult().errors().get(0);
        String prefix = "Invalid value for option '-timeUnitList' at index 1 (<timeUnitList>): expected one of ";
        String suffix = " but was 'b'";
        assertEquals(prefix, ex.getMessage().substring(0, prefix.length()));
        assertEquals(suffix, ex.getMessage().substring(ex.getMessage().length() - suffix.length(), ex.getMessage().length()));

        assertEquals("Unmatched argument at index 3: 'c'", cmd.getParseResult().errors().get(1).getMessage());
    }
    @Test
    public void testTimeFormatHHmmssSSSInvalidError() throws ParseException {
        CommandLine cmd = new CommandLine(new TypeConversionTest.SupportedTypes());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-Time", "23:59:58;123");
        assertEquals(1, cmd.getParseResult().errors().size());
        assertEquals("Invalid value for option '-Time': '23:59:58;123' is not a HH:mm[:ss[.SSS]] time", cmd.getParseResult().errors().get(0).getMessage());
    }
    @Test
    public void testByteFieldsAreDecimal() {
        CommandLine cmd = new CommandLine(new TypeConversionTest.SupportedTypes());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-byte", "0x1F", "-Byte", "0x0F");
        assertEquals(2, cmd.getParseResult().errors().size());
        assertEquals("Invalid value for option '-byte': '0x1F' is not a byte", cmd.getParseResult().errors().get(0).getMessage());
        assertEquals("Invalid value for option '-Byte': '0x0F' is not a byte", cmd.getParseResult().errors().get(1).getMessage());
    }
    @Test
    public void testUnknownOption() {
        class App {
            @Option(names = "-x") int x;
            @Parameters(index = "0") int first;
            @Parameters(index = "*") String all;
        }

        CommandLine cmd = new CommandLine(new App());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("NOT_AN_INT", "-x=b", "-unknown", "1", "2", "3");
        ParseResult parseResult = cmd.getParseResult();
        assertEquals(Arrays.asList("NOT_AN_INT", "-unknown", "2", "3"), parseResult.unmatched());
        assertEquals(6, parseResult.errors().size());
        assertEquals("Invalid value for positional parameter at index 0 (<first>): 'NOT_AN_INT' is not an int", parseResult.errors().get(0).getMessage());
        assertEquals("Invalid value for option '-x': 'b' is not an int", parseResult.errors().get(1).getMessage());
        assertEquals("positional parameter at index 0..* (<all>) should be specified only once", parseResult.errors().get(2).getMessage());
        assertEquals("positional parameter at index 0..* (<all>) should be specified only once", parseResult.errors().get(3).getMessage());
        assertEquals("positional parameter at index 0..* (<all>) should be specified only once", parseResult.errors().get(4).getMessage());
        assertEquals("Unmatched arguments from index 0: 'NOT_AN_INT', '-unknown', '2', '3'", parseResult.errors().get(5).getMessage());
    }

//    @Ignore("Requires https://github.com/remkop/picocli/issues/995")
    @Test
    public void testAnyExceptionWrappedInParameterException() {
        class App {
            @Option(names = "-queue", type = String.class, split = ",")
            ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(2);
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.getCommandSpec().parser().collectErrors(true);
        cmd.parseArgs("-queue", "a,b,c");

        ParseResult parseResult = cmd.getParseResult();
        assertTrue(parseResult.unmatched().isEmpty());
        assertEquals(1, parseResult.errors().size());

        assertTrue(parseResult.errors().get(0) instanceof ParameterException);
        assertTrue(parseResult.errors().get(0).getCause() instanceof NoSuchMethodException);

        assertEquals("NoSuchMethodException: java.util.concurrent.ArrayBlockingQueue.<init>() while processing argument at or before arg[1] 'a,b,c' in [-queue, a,b,c]: java.lang.NoSuchMethodException: java.util.concurrent.ArrayBlockingQueue.<init>()",
                parseResult.errors().get(0).getMessage());
        assertEquals("java.util.concurrent.ArrayBlockingQueue.<init>()", parseResult.errors().get(0).getCause().getMessage());
    }

}
