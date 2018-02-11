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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine.*;

import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CommandLineArityTest {
    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }

    private static void setTraceLevel(String level) {
        System.setProperty("picocli.trace", level);
    }
    @Test
    public void testArityConstructor_fixedRange() {
        Range arity = new Range(1, 23, false, false, null);
        assertEquals("min", 1, arity.min);
        assertEquals("max", 23, arity.max);
        assertEquals("1..23", arity.toString());
        assertEquals(Range.valueOf("1..23"), arity);
    }
    @Test
    public void testArityConstructor_variableRange() {
        Range arity = new Range(1, Integer.MAX_VALUE, true, false, null);
        assertEquals("min", 1, arity.min);
        assertEquals("max", Integer.MAX_VALUE, arity.max);
        assertEquals("1..*", arity.toString());
        assertEquals(Range.valueOf("1..*"), arity);
    }
    private static class SupportedTypes2 {
        @Option(names = "-boolean")       boolean booleanField;
        @Option(names = "-int")           int intField;
    }

     @Test
    public void testArityForOption_booleanFieldImplicitArity0() throws Exception {
        Range arity = Range.optionArity(SupportedTypes2.class.getDeclaredField("booleanField"));
        assertEquals(Range.valueOf("0"), arity);
        assertEquals("0", arity.toString());
    }
    @Test
    public void testArityForOption_intFieldImplicitArity1() throws Exception {
        Range arity = Range.optionArity(SupportedTypes2.class.getDeclaredField("intField"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForOption_isExplicitlyDeclaredValue() throws Exception {
        class Params {
            @Option(names = "-timeUnitList", type = TimeUnit.class, arity = "3") List<TimeUnit> timeUnitList;
        }
        Range arity = Range.optionArity(Params.class.getDeclaredField("timeUnitList"));
        assertEquals(Range.valueOf("3"), arity);
        assertEquals("3", arity.toString());
    }
    @Test
    public void testArityForOption_listFieldImplicitArity1() throws Exception {
        class ImplicitList { @Option(names = "-a") List<Integer> listIntegers; }
        Range arity = Range.optionArity(ImplicitList.class.getDeclaredField("listIntegers"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForOption_arrayFieldImplicitArity1() throws Exception {
        class ImplicitList { @Option(names = "-a") int[] intArray; }
        Range arity = Range.optionArity(ImplicitList.class.getDeclaredField("intArray"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForParameters_booleanFieldImplicitArity1() throws Exception {
        class ImplicitBoolField { @Parameters boolean boolSingleValue; }
        Range arity = Range.parameterArity(ImplicitBoolField.class.getDeclaredField("boolSingleValue"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForParameters_intFieldImplicitArity1() throws Exception {
        class ImplicitSingleField { @Parameters int intSingleValue; }
        Range arity = Range.parameterArity(ImplicitSingleField.class.getDeclaredField("intSingleValue"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForParameters_listFieldImplicitArity0_1() throws Exception {
        class Params {
            @Parameters(type = Integer.class) List<Integer> list;
        }
        Range arity = Range.parameterArity(Params.class.getDeclaredField("list"));
        assertEquals(Range.valueOf("0..1"), arity);
        assertEquals("0..1", arity.toString());
    }
    @Test
    public void testArityForParameters_arrayFieldImplicitArity0_1() throws Exception {
        class Args {
            @Parameters File[] inputFiles;
        }
        Range arity = Range.parameterArity(Args.class.getDeclaredField("inputFiles"));
        assertEquals(Range.valueOf("0..1"), arity);
        assertEquals("0..1", arity.toString());
    }
    @Test
    public void testArrayOptionsWithArity0_nConsumeAllArguments() {
        final double[] DEFAULT_PARAMS = new double[] {1, 2};
        class ArrayOptionsArity0_nAndParameters {
            @Parameters double[] doubleParams = DEFAULT_PARAMS;
            @Option(names = "-doubles", arity = "0..*") double[] doubleOptions;
        }
        ArrayOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionsArity0_nAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(DEFAULT_PARAMS, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionsWithArity1_nConsumeAllArguments() {
        class ArrayOptionsArity1_nAndParameters {
            @Parameters double[] doubleParams;
            @Option(names = "-doubles", arity = "1..*") double[] doubleOptions;
        }
        ArrayOptionsArity1_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionsArity1_nAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(null, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionsWithArity2_nConsumeAllArguments() {
        class ArrayOptionsArity2_nAndParameters {
            @Parameters double[] doubleParams;
            @Option(names = "-doubles", arity = "2..*") double[] doubleOptions;
        }
        ArrayOptionsArity2_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionsArity2_nAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(null, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionArity2_nConsumesAllArgumentsUpToClusteredOption() {
        class ArrayOptionsArity2_nAndParameters {
            @Parameters String[] stringParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionsArity2_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionsArity2_nAndParameters(), "-s 1.1 2.2 3.3 4.4 -vfFILE 5.5".split(" "));
        assertArrayEquals(Arrays.toString(params.stringOptions),
                new String[] {"1.1", "2.2", "3.3", "4.4"}, params.stringOptions);
        assertTrue(params.verbose);
        assertEquals(new File("FILE"), params.file);
        assertArrayEquals(new String[] {"5.5"}, params.stringParams);
    }

    @Test
    public void testArrayOptionArity2_nConsumesAllArgumentIncludingQuotedSimpleOption() {
        class ArrayOptionArity2_nAndParameters {
            @Parameters String[] stringParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionArity2_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2_nAndParameters(), "-s 1.1 2.2 3.3 4.4 \"-v\" \"-f\" \"FILE\" 5.5".split(" "));
        assertArrayEquals(Arrays.toString(params.stringOptions),
                new String[] {"1.1", "2.2", "3.3", "4.4", "-v", "-f", "FILE", "5.5"}, params.stringOptions);
        assertFalse("verbose", params.verbose);
        assertNull("file", params.file);
        assertArrayEquals(null, params.stringParams);
    }

    @Test
    public void testArrayOptionArity2_nConsumesAllArgumentIncludingQuotedClusteredOption() {
        class ArrayOptionArity2_nAndParameters {
            @Parameters String[] stringParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionArity2_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2_nAndParameters(), "-s 1.1 2.2 3.3 4.4 \"-vfFILE\" 5.5".split(" "));
        assertArrayEquals(Arrays.toString(params.stringOptions),
                new String[] {"1.1", "2.2", "3.3", "4.4", "-vfFILE", "5.5"}, params.stringOptions);
        assertFalse("verbose", params.verbose);
        assertNull("file", params.file);
        assertArrayEquals(null, params.stringParams);
    }

    @Test
    public void testArrayOptionArity2_nConsumesAllArgumentsUpToNextSimpleOption() {
        class ArrayOptionArity2_nAndParameters {
            @Parameters double[] doubleParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionArity2_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2_nAndParameters(), "-s 1.1 2.2 3.3 4.4 -v -f=FILE 5.5".split(" "));
        assertArrayEquals(Arrays.toString(params.stringOptions),
                new String[] {"1.1", "2.2", "3.3", "4.4"}, params.stringOptions);
        assertTrue(params.verbose);
        assertEquals(new File("FILE"), params.file);
        assertArrayEquals(new double[] {5.5}, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionArity2_nConsumesAllArgumentsUpToNextOptionWithAttachment() {
        class ArrayOptionArity2_nAndParameters {
            @Parameters double[] doubleParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionArity2_nAndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2_nAndParameters(), "-s 1.1 2.2 3.3 4.4 -f=FILE -v 5.5".split(" "));
        assertArrayEquals(Arrays.toString(params.stringOptions),
                new String[] {"1.1", "2.2", "3.3", "4.4"}, params.stringOptions);
        assertTrue(params.verbose);
        assertEquals(new File("FILE"), params.file);
        assertArrayEquals(new double[] {5.5}, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionArityNConsumeAllArguments() {
        class ArrayOptionArityNAndParameters {
            @Parameters char[] charParams;
            @Option(names = "-chars", arity = "*") char[] charOptions;
        }
        ArrayOptionArityNAndParameters
                params = CommandLine.populateCommand(new ArrayOptionArityNAndParameters(), "-chars a b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
                new char[] {'a', 'b', 'c', 'd'}, params.charOptions);
        assertArrayEquals(null, params.charParams);
    }
    @Test
    public void testMissingRequiredParams() {
        class Example {
            @Parameters(index = "1", arity = "0..1") String optional;
            @Parameters(index = "0") String mandatory;
        }
        try { CommandLine.populateCommand(new Example(), new String[] {"mandatory"}); }
        catch (MissingParameterException ex) { fail(); }

        try {
            CommandLine.populateCommand(new Example(), new String[0]);
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <mandatory>", ex.getMessage());
        }
    }
    @Test
    public void testMissingRequiredParams1() {
        class Tricky1 {
            @Parameters(index = "2") String anotherMandatory;
            @Parameters(index = "1", arity = "0..1") String optional;
            @Parameters(index = "0") String mandatory;
        }
        try {
            CommandLine.populateCommand(new Tricky1(), new String[0]);
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: <mandatory>, <anotherMandatory>", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new Tricky1(), new String[] {"firstonly"});
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <anotherMandatory>", ex.getMessage());
        }
    }
    @Test
    public void testMissingRequiredParams2() {
        class Tricky2 {
            @Parameters(index = "2", arity = "0..1") String anotherOptional;
            @Parameters(index = "1", arity = "0..1") String optional;
            @Parameters(index = "0") String mandatory;
        }
        try { CommandLine.populateCommand(new Tricky2(), new String[] {"mandatory"}); }
        catch (MissingParameterException ex) { fail(); }

        try {
            CommandLine.populateCommand(new Tricky2(), new String[0]);
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <mandatory>", ex.getMessage());
        }
    }
    @Test
    public void testMissingRequiredParamsWithOptions() {
        class Tricky3 {
            @Option(names="-v") boolean more;
            @Option(names="-t") boolean any;
            @Parameters(index = "1") String alsoMandatory;
            @Parameters(index = "0") String mandatory;
        }
        try {
            CommandLine.populateCommand(new Tricky3(), new String[] {"-t", "-v", "mandatory"});
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <alsoMandatory>", ex.getMessage());
        }

        try {
            CommandLine.populateCommand(new Tricky3(), new String[] { "-t", "-v"});
            fail("Should not accept missing two mandatory parameters");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: <mandatory>, <alsoMandatory>", ex.getMessage());
        }
    }
    @Test
    public void testMissingRequiredParamWithOption() {
        class Tricky3 {
            @Option(names="-t") boolean any;
            @Parameters(index = "0") String mandatory;
        }
        try {
            CommandLine.populateCommand(new Tricky3(), new String[] {"-t"});
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <mandatory>", ex.getMessage());
        }
    }
    @Test
    public void testNoMissingRequiredParamErrorIfHelpOptionSpecified() {
        class App {
            @Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
                    List<String> allParameters; // no "index" attribute: captures _all_ arguments (as Strings)

            @Parameters(index = "0")    InetAddress  host;
            @Parameters(index = "1")    int          port;
            @Parameters(index = "2..*") File[]       files;

            @Option(names = "-?", help = true) boolean help;
        }
        CommandLine.populateCommand(new App(), new String[] {"-?"});
        try {
            CommandLine.populateCommand(new App(), new String[0]);
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: <host>, <port>", ex.getMessage());
        }
    }
    @Test
    public void testNoMissingRequiredParamErrorWithLabelIfHelpOptionSpecified() {
        class App {
            @Parameters(hidden = true)  // "hidden": don't show this parameter in usage help message
                    List<String> allParameters; // no "index" attribute: captures _all_ arguments (as Strings)

            @Parameters(index = "0", paramLabel = "HOST")     InetAddress  host;
            @Parameters(index = "1", paramLabel = "PORT")     int          port;
            @Parameters(index = "2..*", paramLabel = "FILES") File[]       files;

            @Option(names = "-?", help = true) boolean help;
        }
        CommandLine.populateCommand(new App(), new String[] {"-?"});
        try {
            CommandLine.populateCommand(new App(), new String[0]);
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: HOST, PORT", ex.getMessage());
        }
    }

    private static class BooleanOptionsArity0_nAndParameters {
        @Parameters String[] params;
        @Option(names = "-bool", arity = "0..*") boolean bool;
        @Option(names = {"-v", "-other"}, arity="0..*") boolean vOrOther;
        @Option(names = "-r") boolean rBoolean;
    }
    @Test
    public void testBooleanOptionsArity0_nConsume1ArgumentIfPossible() { // ignores varargs
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool false false true".split(" "));
        assertFalse(params.bool);
        assertArrayEquals(new String[]{ "false", "true"}, params.params);
    }
    @Test
    public void testBooleanOptionsArity0_nRequiresNoArgument() { // ignores varargs
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool".split(" "));
        assertTrue(params.bool);
    }
    @Test
    public void testBooleanOptionsArity0_nConsume0ArgumentsIfNextArgIsOption() { // ignores varargs
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool -other".split(" "));
        assertTrue(params.bool);
        assertTrue(params.vOrOther);
    }
    @Test
    public void testBooleanOptionsArity0_nConsume0ArgumentsIfNextArgIsParameter() { // ignores varargs
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool 123 -other".split(" "));
        assertTrue(params.bool);
        assertTrue(params.vOrOther);
        assertArrayEquals(new String[]{ "123"}, params.params);
    }
    @Test
    public void testBooleanOptionsArity0_nFailsIfAttachedParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool=123 -other".split(" "));
            fail("was able to assign 123 to boolean");
        } catch (CommandLine.ParameterException ex) {
            assertEquals("'123' is not a boolean for option '-bool'", ex.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-rv234 -bool".split(" "));
            fail("Expected exception");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [-234]", ok.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABooleanWithUnmatchedArgsAllowed() { // ignores varargs
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new BooleanOptionsArity0_nAndParameters()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("-rv234 -bool".split(" "));
        assertEquals(Arrays.asList("-234"), cmd.getUnmatchedArguments());
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedWithSepParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-rv=234 -bool".split(" "));
            fail("was able to assign 234 to boolean");
        } catch (CommandLine.ParameterException ex) {
            assertEquals("'234' is not a boolean for option '-v'", ex.getMessage());
        }
    }

    private static class BooleanOptionsArity1_nAndParameters {
        @Parameters boolean[] boolParams;
        @Option(names = "-bool", arity = "1..*") boolean aBoolean;
    }
    @Test
    public void testBooleanOptionsArity1_nConsume1Argument() { // ignores varargs
        BooleanOptionsArity1_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool false false true".split(" "));
        assertFalse(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);

        params = CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool true false true".split(" "));
        assertTrue(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);
    }
    @Test
    public void testBooleanOptionsArity1_nCaseInsensitive() { // ignores varargs
        BooleanOptionsArity1_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool fAlsE false true".split(" "));
        assertFalse(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);

        params = CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool FaLsE false true".split(" "));
        assertFalse(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);

        params = CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool tRuE false true".split(" "));
        assertTrue(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);
    }
    @Test
    public void testBooleanOptionsArity1_nErrorIfValueNotTrueOrFalse() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool abc".split(" "));
            fail("Invalid format abc was accepted for boolean");
        } catch (CommandLine.ParameterException expected) {
            assertEquals("'abc' is not a boolean for option '-bool'", expected.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity1_nErrorIfValueMissing() {
        try {
            CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool".split(" "));
            fail("Missing param was accepted for boolean with arity=1");
        } catch (CommandLine.ParameterException expected) {
            assertEquals("Missing required parameter for option '-bool' at index 0 (<aBoolean>)", expected.getMessage());
        }
    }

    @Test
    public void testBooleanOptionArity0Consumes0Arguments() {
        class BooleanOptionArity0AndParameters {
            @Parameters boolean[] boolParams;
            @Option(names = "-bool", arity = "0") boolean aBoolean;
        }
        BooleanOptionArity0AndParameters
                params = CommandLine.populateCommand(new BooleanOptionArity0AndParameters(), "-bool true false true".split(" "));
        assertTrue(params.aBoolean);
        assertArrayEquals(new boolean[]{true, false, true}, params.boolParams);
    }
    @Test(expected = MissingParameterException.class)
    public void testSingleValueFieldDefaultMinArityIs1() {
        class App { @Option(names = "-Long")          Long aLongField; }
        CommandLine.populateCommand(new App(),  "-Long");
    }
    @Test
    public void testSingleValueFieldDefaultMinArityIsOne() {
        class App {
            @Option(names = "-boolean")       boolean booleanField;
            @Option(names = "-Long")          Long aLongField;
        }
        try {
            CommandLine.populateCommand(new App(),  "-Long", "-boolean");
            fail("should fail");
        } catch (CommandLine.ParameterException ex) {
            assertEquals("Could not convert '-boolean' to Long for option '-Long'" +
                    ": java.lang.NumberFormatException: For input string: \"-boolean\"", ex.getMessage());
        }
    }

    @Test
    public void testIntOptionArity1_nConsumes1Argument() { // ignores varargs
        class IntOptionArity1_nAndParameters {
            @Parameters int[] intParams;
            @Option(names = "-int", arity = "1..*") int anInt;
        }
        IntOptionArity1_nAndParameters
                params = CommandLine.populateCommand(new IntOptionArity1_nAndParameters(), "-int 23 42 7".split(" "));
        assertEquals(23, params.anInt);
        assertArrayEquals(new int[]{ 42, 7}, params.intParams);
    }

    @Test
    public void testArrayOptionsWithArity0Consume0Arguments() {
        class OptionsArray0ArityAndParameters {
            @Parameters double[] doubleParams;
            @Option(names = "-doubles", arity = "0") double[] doubleOptions;
        }
        OptionsArray0ArityAndParameters
                params = CommandLine.populateCommand(new OptionsArray0ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[0], params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{1.1, 2.2, 3.3, 4.4}, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionWithArity1Consumes1Argument() {
        class Options1ArityAndParameters {
            @Parameters double[] doubleParams;
            @Option(names = "-doubles", arity = "1") double[] doubleOptions;
        }
        Options1ArityAndParameters
                params = CommandLine.populateCommand(new Options1ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1}, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{2.2, 3.3, 4.4}, params.doubleParams, 0.000001);

        // repeated occurrence
        params = CommandLine.populateCommand(new Options1ArityAndParameters(), "-doubles 1.1 -doubles 2.2 -doubles 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3}, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{4.4}, params.doubleParams, 0.000001);

    }

    private static class ArrayOptionArity2AndParameters {
        @Parameters double[] doubleParams;
        @Option(names = "-doubles", arity = "2") double[] doubleOptions;
    }
    @Test
    public void testArrayOptionWithArity2Consumes2Arguments() {
        ArrayOptionArity2AndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2AndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, }, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{3.3, 4.4}, params.doubleParams, 0.000001);

        // repeated occurrence
        params = CommandLine.populateCommand(new ArrayOptionArity2AndParameters(), "-doubles 1.1 2.2 -doubles 3.3 4.4 0".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4 }, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{ 0.0 }, params.doubleParams, 0.000001);
    }
    @Test
    public void testArrayOptionsWithArity2Consume2ArgumentsEvenIfFirstIsAttached() {
        ArrayOptionArity2AndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2AndParameters(), "-doubles=1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, }, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{3.3, 4.4}, params.doubleParams, 0.000001);

        // repeated occurrence
        params = CommandLine.populateCommand(new ArrayOptionArity2AndParameters(), "-doubles=1.1 2.2 -doubles=3.3 4.4 0".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{0}, params.doubleParams, 0.000001);
    }
    /** Arity should not limit the total number of values put in an array or collection #191 */
    @Test
    public void testArrayOptionsWithArity2MayContainMoreThan2Values() {
        ArrayOptionArity2AndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2AndParameters(), "-doubles=1 2 -doubles 3 4 -doubles 5 6".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1, 2, 3, 4, 5, 6 }, params.doubleOptions, 0.000001);
        assertArrayEquals(null, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionWithoutArityConsumesOneArgument() { // #192
        class OptionsNoArityAndParameters {
            @Parameters char[] charParams;
            @Option(names = "-chars") char[] charOptions;
        }
        OptionsNoArityAndParameters
                params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars a b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
                new char[] {'a', }, params.charOptions);
        assertArrayEquals(Arrays.toString(params.charParams), new char[] {'b', 'c', 'd'}, params.charParams);

        // repeated occurrence
        params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars a -chars b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
                new char[] {'a', 'b', }, params.charOptions);
        assertArrayEquals(Arrays.toString(params.charParams), new char[] {'c', 'd'}, params.charParams);

        try {
            CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars".split(" "));
            fail("expected MissingParameterException");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required parameter for option '-chars' (<charOptions>)", ok.getMessage());
        }
    }

    @Test
    public void testArrayParametersWithDefaultArity() {
        class ArrayParamsDefaultArity {
            @Parameters
            List<String> params;
        }
        ArrayParamsDefaultArity params = CommandLine.populateCommand(new ArrayParamsDefaultArity(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsDefaultArity(), "a");
        assertEquals(Arrays.asList("a"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsDefaultArity());
        assertEquals(null, params.params);
    }

    @Test
    public void testArrayParametersWithArityMinusOneToN() {
        class ArrayParamsNegativeArity {
            @Parameters(arity = "-1..*")
            List<String> params;
        }
        ArrayParamsNegativeArity params = CommandLine.populateCommand(new ArrayParamsNegativeArity(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsNegativeArity(), "a");
        assertEquals(Arrays.asList("a"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsNegativeArity());
        assertEquals(null, params.params);
    }

    @Test
    public void testArrayParametersArity0_n() {
        class ArrayParamsArity0_n {
            @Parameters(arity = "0..*")
            List<String> params;
        }
        ArrayParamsArity0_n params = CommandLine.populateCommand(new ArrayParamsArity0_n(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsArity0_n(), "a");
        assertEquals(Arrays.asList("a"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsArity0_n());
        assertEquals(null, params.params);
    }

    @Test
    public void testArrayParametersArity1_n() {
        class ArrayParamsArity1_n {
            @Parameters(arity = "1..*")
            List<String> params;
        }
        ArrayParamsArity1_n params = CommandLine.populateCommand(new ArrayParamsArity1_n(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), params.params);

        params = CommandLine.populateCommand(new ArrayParamsArity1_n(), "a");
        assertEquals(Arrays.asList("a"), params.params);

        try {
            params = CommandLine.populateCommand(new ArrayParamsArity1_n());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters at positions 0..*: <params>", ex.getMessage());
        }
    }

    @Test
    public void testArrayParametersArity2_n() {
        class ArrayParamsArity2_n {
            @Parameters(arity = "2..*")
            List<String> params;
        }
        ArrayParamsArity2_n params = CommandLine.populateCommand(new ArrayParamsArity2_n(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), params.params);

        try {
            params = CommandLine.populateCommand(new ArrayParamsArity2_n(), "a");
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<params>) requires at least 2 values, but only 1 were specified: [a]", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new ArrayParamsArity2_n());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<params>) requires at least 2 values, but none were specified.", ex.getMessage());
        }
    }

    @Test
    public void testNonVarargArrayParametersWithNegativeArityConsumesZeroArguments() {
        class NonVarArgArrayParamsNegativeArity {
            @Parameters(arity = "-1")
            List<String> params;
        }
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsNegativeArity(), "a", "b", "c");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [a, b, c]", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsNegativeArity(), "a");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [a]", ex.getMessage());
        }
        NonVarArgArrayParamsNegativeArity params = CommandLine.populateCommand(new NonVarArgArrayParamsNegativeArity());
        assertEquals(null, params.params);
    }

    @Test
    public void testNonVarargArrayParametersWithArity0() {
        class NonVarArgArrayParamsZeroArity {
            @Parameters(arity = "0")
            List<String> params;
        }
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity(), "a", "b", "c");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [a, b, c]", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity(), "a");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [a]", ex.getMessage());
        }
        NonVarArgArrayParamsZeroArity params = CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity());
        assertEquals(null, params.params);
    }

    @Test
    public void testNonVarargArrayParametersWithArity1() {
        class NonVarArgArrayParamsArity1 {
            @Parameters(arity = "1")
            List<String> params;
        }
        NonVarArgArrayParamsArity1 actual = CommandLine.populateCommand(new NonVarArgArrayParamsArity1(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), actual.params);

        NonVarArgArrayParamsArity1  params = CommandLine.populateCommand(new NonVarArgArrayParamsArity1(), "a");
        assertEquals(Arrays.asList("a"), params.params);

        try {
            params = CommandLine.populateCommand(new NonVarArgArrayParamsArity1());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <params>", ex.getMessage());
        }
    }

    @Test
    public void testNonVarargArrayParametersWithArity2() {
        class NonVarArgArrayParamsArity2 {
            @Parameters(arity = "2")
            List<String> params;
        }
        NonVarArgArrayParamsArity2 params = null;
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsArity2(), "a", "b", "c");
            fail("expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<params>) requires at least 2 values, but only 1 were specified: [c]", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new NonVarArgArrayParamsArity2(), "a");
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<params>) requires at least 2 values, but only 1 were specified: [a]", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new NonVarArgArrayParamsArity2());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<params>) requires at least 2 values, but none were specified.", ex.getMessage());
        }
    }
    @Test
    public void testMixPositionalParamsWithOptions_ParamsUnboundedArity() {
        class Arg {
            @Parameters(arity = "1..*") List<String> parameters;
            @Option(names = "-o")    List<String> options;
        }
        Arg result = CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "p2", "-o", "v2", "p3", "p4");
        assertEquals(Arrays.asList("p1", "p2", "p3", "p4"), result.parameters);
        assertEquals(Arrays.asList("v1", "v2"), result.options);

        Arg result2 = CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "-o", "v2", "p3");
        assertEquals(Arrays.asList("p1", "p3"), result2.parameters);
        assertEquals(Arrays.asList("v1", "v2"), result2.options);

        try {
            CommandLine.populateCommand(new Arg(), "-o", "v1", "-o", "v2");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters at positions 0..*: <parameters>", ex.getMessage());
        }
    }

    @Test
    public void test130MixPositionalParamsWithOptions() {
        @CommandLine.Command(name = "test-command", description = "tests help from a command script")
        class Arg {

            @Parameters(description = "some parameters")
            List<String> parameters;

            @Option(names = {"-cp", "--codepath"}, description = "the codepath")
            List<String> codepath;
        }
        Arg result = CommandLine.populateCommand(new Arg(), "--codepath", "/usr/x.jar", "placeholder", "-cp", "/bin/y.jar", "another");
        assertEquals(Arrays.asList("/usr/x.jar", "/bin/y.jar"), result.codepath);
        assertEquals(Arrays.asList("placeholder", "another"), result.parameters);
    }

    @Test
    public void test130MixPositionalParamsWithOptions1() {
        class Arg {
            @Parameters           List<String> parameters;
            @Option(names = "-o") List<String> options;
        }
        Arg result = CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "p2", "-o", "v2", "p3");
        assertEquals(Arrays.asList("v1", "v2"), result.options);
        assertEquals(Arrays.asList("p1", "p2", "p3"), result.parameters);
    }

    @Test
    public void test130MixPositionalParamsWithOptionsArity() {
        class Arg {
            @Parameters(arity = "2") List<String> parameters;
            @Option(names = "-o")    List<String> options;
        }
        Arg result = CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "p2", "-o", "v2", "p3", "p4");
        assertEquals(Arrays.asList("v1", "v2"), result.options);
        assertEquals(Arrays.asList("p1", "p2", "p3", "p4"), result.parameters);

        Arg result2 = CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "-o", "v2", "p3");
        assertEquals(Arrays.asList("v1"), result2.options);
        assertEquals(Arrays.asList("p1", "-o", "v2", "p3"), result2.parameters);

        try {
            CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "p2", "-o", "v2", "p3");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<parameters>) requires at least 2 values, but only 1 were specified: [p3]", ex.getMessage());
        }
    }

    @Test
    public void test284VarargPositionalShouldNotConsumeOptions() {
        class Cmd {
            @Option(names = "--alpha") String alpha;
            @Parameters(index = "0", arity = "1") String foo;
            @Parameters(index = "1..*", arity = "*") List<String> params;
        }
        Cmd cmd = CommandLine.populateCommand(new Cmd(), "foo", "xx", "--alpha", "--beta");
        assertEquals("foo", cmd.foo);
        assertEquals("--beta", cmd.alpha);
        assertEquals(Arrays.asList("xx"), cmd.params);
    }

    @Test
    public void test284VarargPositionalShouldConsumeOptionsAfterDoubleDash() {
        class Cmd {
            @Option(names = "--alpha") String alpha;
            @Parameters(index = "0", arity = "1") String foo;
            @Parameters(index = "1..*", arity = "*") List<String> params;
        }
        Cmd cmd = CommandLine.populateCommand(new Cmd(), "foo", "--", "xx", "--alpha", "--beta");
        assertEquals("foo", cmd.foo);
        assertEquals(null, cmd.alpha);
        assertEquals(Arrays.asList("xx", "--alpha", "--beta"), cmd.params);
    }

    @Test
    public void testPositionalShouldCaptureDoubleDashAfterDoubleDash() {
        class Cmd {
            @Parameters List<String> params;
        }
        Cmd cmd = CommandLine.populateCommand(new Cmd(), "foo", "--", "--", "--");
        assertEquals(Arrays.asList("foo", "--", "--"), cmd.params);
    }

    @Test
    public void testVarargPositionalShouldCaptureDoubleDashAfterDoubleDash() {
        class Cmd {
            @Parameters(index = "0..*", arity = "*") List<String> params;
        }
        Cmd cmd = CommandLine.populateCommand(new Cmd(), "foo", "--", "--", "--");
        assertEquals(Arrays.asList("foo", "--", "--"), cmd.params);
    }
}
