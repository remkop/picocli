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

import org.junit.*;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static picocli.TestUtil.setTraceLevel;

public class ArityTest {
    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testArityConstructor_fixedRange() {
        Range arity = new Range(1, 23, false, false, null);
        assertEquals("min", 1, arity.min());
        assertEquals("max", 23, arity.max());
        assertEquals("1..23", arity.toString());
        assertEquals(Range.valueOf("1..23"), arity);
    }
    @Test
    public void testArityConstructor_variableRange() {
        Range arity = new Range(1, Integer.MAX_VALUE, true, false, null);
        assertEquals("min", 1, arity.min());
        assertEquals("max", Integer.MAX_VALUE, arity.max());
        assertEquals("1..*", arity.toString());
        assertEquals(Range.valueOf("1..*"), arity);
    }

    @Test
    public void testValueOfDisallowsInvalidRange() {
        try {
            Range.valueOf("1..0");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid range (min=1, max=0)", ex.getMessage());
        }
        try {
            Range.valueOf("3..1");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid range (min=3, max=1)", ex.getMessage());
        }
    }

    @Test
    public void testValueOfDisallowsNegativeRange() {
        try {
            Range.valueOf("-1..0");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid negative range (min=-1, max=0)", ex.getMessage());
        }
        try {
            Range.valueOf("-3..1");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid negative range (min=-3, max=1)", ex.getMessage());
        }
    }

    @Test
    public void testConstructorDisallowsNegativeRange() {
        try {
            new Range(-1, 0, true, true, "");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid negative range (min=-1, max=0)", ex.getMessage());
        }
        try {
            new Range(-3, -1, true, true, "");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid negative range (min=-3, max=-1)", ex.getMessage());
        }
    }

    @Test
    public void testConstructorDisallowsInvalidRange() {
        try {
            new Range(1, 0, true, true, "");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid range (min=1, max=0)", ex.getMessage());
        }
        try {
            new Range(3, 1, true, true, "");
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid range (min=3, max=1)", ex.getMessage());
        }
    }

    private static class SupportedTypes2 {
        String nonOptionField;

        @Option(names = "-boolean")       boolean booleanField;
        @Option(names = "-int")           int intField;
    }

    @Test
    public void testOptionArity_forNonAnnotatedField() throws Exception {
        Range arity = Range.optionArity(SupportedTypes2.class.getDeclaredField("nonOptionField"));
        assertEquals(0, arity.max());
        assertEquals(0, arity.min());
        assertEquals(false, arity.isVariable());
        assertEquals("0", arity.toString());
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
    public void testParameterArityWithOptionMember() throws Exception {
        class ImplicitBoolField { @Option(names = "-x") boolean boolSingleValue; }
        Range arity = Range.parameterArity(ImplicitBoolField.class.getDeclaredField("boolSingleValue"));
        assertEquals(0, arity.max());
        assertEquals(0, arity.min());
        assertEquals(false, arity.isVariable());
        assertEquals("0", arity.toString());
    }

    @Test
    public void testParameterIndex_WhenUndefinedSingleValue() throws Exception {
        class ImplicitBoolField { @Parameters boolean boolSingleValue; }
        Range index = Range.parameterIndex(ImplicitBoolField.class.getDeclaredField("boolSingleValue"));
        assertEquals(0, index.max());
        assertEquals(0, index.min());
        assertEquals(false, index.isVariable());
        assertEquals("0", index.toString());
        assertEquals("0+ (0)", index.internalToString());
        assertEquals(true, index.isRelative());
        assertEquals(true, index.isRelativeToAnchor());
        assertEquals(0, index.anchor());
    }

    @Test
    public void testParameterIndex_WhenUndefinedMultiValue() throws Exception {
        class ImplicitBoolArrayField { @Parameters boolean[] boolMultiValue; }
        Range index = Range.parameterIndex(ImplicitBoolArrayField.class.getDeclaredField("boolMultiValue"));
        assertEquals(Integer.MAX_VALUE, index.max());
        assertEquals(0, index.min());
        assertEquals(true, index.isVariable());
        assertEquals("0..*", index.toString());
    }

    @Test
    public void testParameterIndex_WhenDefined() throws Exception {
        class ImplicitBoolField { @Parameters(index = "2..3") boolean boolSingleValue; }
        Range arity = Range.parameterIndex(ImplicitBoolField.class.getDeclaredField("boolSingleValue"));
        assertEquals(3, arity.max());
        assertEquals(2, arity.min());
        assertEquals(false, arity.isVariable());
        assertEquals("2..3", arity.toString());
    }

    @Test
    public void testDefaultArity_Field() throws Exception {
        class ImplicitBoolField {
            @Option(names = "-x") boolean x;
            @Option(names = "-y") int y;
            @Option(names = "-z") List<String> z;
            @Parameters boolean a;
            @Parameters int b;
            @Parameters List<String> c;
        }
        assertEquals(Range.valueOf("0"),    Range.defaultArity(ImplicitBoolField.class.getDeclaredField("x")));
        assertEquals(Range.valueOf("1"),    Range.defaultArity(ImplicitBoolField.class.getDeclaredField("y")));
        assertEquals(Range.valueOf("1"),    Range.defaultArity(ImplicitBoolField.class.getDeclaredField("z")));
        assertEquals(Range.valueOf("1"),    Range.defaultArity(ImplicitBoolField.class.getDeclaredField("a")));
        assertEquals(Range.valueOf("1"),    Range.defaultArity(ImplicitBoolField.class.getDeclaredField("b")));
        assertEquals(Range.valueOf("0..1"), Range.defaultArity(ImplicitBoolField.class.getDeclaredField("c")));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDefaultArity_Class() {
        assertEquals(Range.valueOf("0"), Range.defaultArity(Boolean.TYPE));
        assertEquals(Range.valueOf("0"), Range.defaultArity(Boolean.class));
        assertEquals(Range.valueOf("1"), Range.defaultArity(Integer.TYPE));
        assertEquals(Range.valueOf("1"), Range.defaultArity(Integer.class));
        assertEquals(Range.valueOf("1"), Range.defaultArity(List.class));
        assertEquals(Range.valueOf("1"), Range.defaultArity(String[].class));
        assertEquals(Range.valueOf("1"), Range.defaultArity(Map.class));
    }

    @Test
    public void testParameterCapacity() throws Exception {
        PositionalParamSpec paramSpec = PositionalParamSpec.builder().index(Range.valueOf("1..2")).arity(Range.valueOf("*")).build();
        Method capacity = PositionalParamSpec.class.getDeclaredMethod("capacity");
        capacity.setAccessible(true);
        Range c = (Range) capacity.invoke(paramSpec);
        assertEquals(Range.valueOf("*"), c);
    }

    @Test
    public void testValueOf_EmptyString() throws Exception {
        assertEquals(Range.valueOf("*"), Range.valueOf(""));
    }

    @Test
    public void testValueOf_Invalid() throws Exception {
        assertEquals(Range.valueOf("0..3"), Range.valueOf("..3"));
    }

    @Test
    public void testMaxSetter()  {
        assertEquals(Range.valueOf("0..3"), Range.valueOf("0").max(3));
    }

    @Test
    public void testIsUnspecified()  {
        class App {
            @Parameters List<String> unspecified;
            @Parameters(arity = "2") List<String> specified;
        }
        CommandLine cmd = new CommandLine(new App());
        assertTrue(cmd.getCommandSpec().positionalParameters().get(0).arity().isUnspecified());
        assertFalse(cmd.getCommandSpec().positionalParameters().get(1).arity().isUnspecified());
    }

    @Test
    public void testRangeEquals_OtherType()  {
        assertFalse(Range.valueOf("0").equals(123));
        assertNotEquals(Range.valueOf("0"), 123);
    }

    @Test
    public void testRangeEquals_MinMaxVariable()  {
        assertNotEquals("different max", Range.valueOf("1..1"), Range.valueOf("1..2"));
        assertNotEquals("different min", Range.valueOf("2..2"), Range.valueOf("1..2"));
        assertNotEquals("different isVariable", Range.valueOf("1..*"), Range.valueOf("1..2"));
        assertNotEquals("different min and isVariable", Range.valueOf("1..*"), Range.valueOf("2..2"));
        assertEquals("same", Range.valueOf("1..*"), Range.valueOf("1..*"));
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
    public void test1125_ArrayOptionArity2_nConsumesAllArgumentsWhenAllowOptionsAsOptionParameters() {
        class ArrayOptionsArity2_nAndParameters {
            @Parameters String[] stringParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionsArity2_nAndParameters params = new ArrayOptionsArity2_nAndParameters();
        new CommandLine(params).setAllowOptionsAsOptionParameters(true)
            .parseArgs("-s 1.1 2.2 3.3 4.4 -vfFILE 5.5".split(" "));
        assertArrayEquals(Arrays.toString(params.stringOptions),
            new String[] {"1.1", "2.2", "3.3", "4.4", "-vfFILE", "5.5"}, params.stringOptions);
        assertFalse(params.verbose);
        assertEquals(null, params.file);
        assertNull(params.stringParams);
    }

    @Test
    public void testArrayOptionArity2_nConsumesAllArgumentIncludingQuotedSimpleOption() {
        class ArrayOptionArity2_nAndParameters {
            @Parameters String[] stringParams;
            @Option(names = "-s", arity = "2..*") String[] stringOptions;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
        }
        ArrayOptionArity2_nAndParameters params = new ArrayOptionArity2_nAndParameters();
        new CommandLine(params).setTrimQuotes(true).parseArgs("-s 1.1 2.2 3.3 4.4 \"-v\" \"-f\" \"FILE\" 5.5".split(" "));
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
        ArrayOptionArity2_nAndParameters params = new ArrayOptionArity2_nAndParameters();
        new CommandLine(params).setTrimQuotes(true).parseArgs("-s 1.1 2.2 3.3 4.4 \"-vfFILE\" 5.5".split(" "));
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
            @Parameters int[] intParams;
            @Option(names = "-ints", arity = "*") int[] intOptions;
        }
        ArrayOptionArityNAndParameters
                params = CommandLine.populateCommand(new ArrayOptionArityNAndParameters(), "-ints 1 2 3 4".split(" "));
        assertArrayEquals(Arrays.toString(params.intOptions),
                new int[] {1, 2, 3, 4}, params.intOptions);
        assertArrayEquals(null, params.intParams);
    }

    @Test
    public void testCharacterArrayOptionArityNConsumeAllArguments() {
        class ArrayOptionArityNAndParameters {
            @Parameters Character[] cParams;
            @Option(names = "-chars", arity = "*") Character[] cOptions;
        }
        ArrayOptionArityNAndParameters
            params = CommandLine.populateCommand(new ArrayOptionArityNAndParameters(), "-chars a b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.cOptions),
            new Character[] {'a', 'b', 'c', 'd'}, params.cOptions);
        assertArrayEquals(null, params.cParams);
    }

    @Test
    public void testCharArrayOptionArityNConsumeSingleArgDisallowsMultiArgs() {
        class ArrayOptionArityNAndParameters {
            @Parameters(arity = "0..1") char[] charParams;
            @Option(names = "-chars", arity = "*") char[] charOptions;
        }
        ArrayOptionArityNAndParameters
            params = CommandLine.populateCommand(new ArrayOptionArityNAndParameters(), "-chars abcd".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
            new char[] {'a', 'b', 'c', 'd'}, params.charOptions);
        assertArrayEquals(null, params.charParams);

        try {
            CommandLine.populateCommand(new ArrayOptionArityNAndParameters(), "-chars a b c d".split(" "));
            fail("expected MissingParameterException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments from index 3: 'c', 'd'", ex.getMessage());
        }
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
            assertEquals("Missing required parameter: '<mandatory>'", ex.getMessage());
            assertEquals(1, ex.getMissing().size());
            assertEquals("<mandatory>", ex.getMissing().get(0).paramLabel());
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
            assertEquals("Missing required parameters: '<mandatory>', '<anotherMandatory>'", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new Tricky1(), new String[] {"firstonly"});
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: '<anotherMandatory>'", ex.getMessage());
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
            assertEquals("Missing required parameter: '<mandatory>'", ex.getMessage());
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
            assertEquals("Missing required parameter: '<alsoMandatory>'", ex.getMessage());
        }

        try {
            CommandLine.populateCommand(new Tricky3(), new String[] { "-t", "-v"});
            fail("Should not accept missing two mandatory parameters");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: '<mandatory>', '<alsoMandatory>'", ex.getMessage());
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
            assertEquals("Missing required parameter: '<mandatory>'", ex.getMessage());
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
            assertEquals("Missing required parameters: '<host>', '<port>'", ex.getMessage());
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
            assertEquals("Missing required parameters: 'HOST', 'PORT'", ex.getMessage());
        }
    }

    static class BooleanOptionsArity0_nAndParameters {
        @Parameters String[] params;
        @Option(names = "-bool", arity = "0..*") boolean bool;
        @Option(names = {"-v", "-other"}, arity="0..*") boolean vOrOther;
        @Option(names = "-r") boolean rBoolean;
    }

    @Test
    public void testBooleanOptionsArity0_nFalse() {
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool false".split(" "));
        assertFalse(params.bool);
    }

    @Test
    public void testBooleanOptionsArity0_nTrue() {
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool true".split(" "));
        assertTrue(params.bool);
        assertNull(params.params);
    }

    @Test
    public void testBooleanOptionsArity0_nX() {
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool x".split(" "));
        assertTrue(params.bool);
        assertArrayEquals(new String[]{ "x" }, params.params);
    }
    @Test
    public void testBooleanOptionsArity0_nConsume1ArgumentIfPossible() { // ignores varargs
        BooleanOptionsArity0_nAndParameters
                params = CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool=false false true".split(" "));
        assertFalse(params.bool);
        assertArrayEquals(new String[]{ "false", "true" }, params.params);
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
            assertEquals("Invalid value for option '-bool': '123' is not a boolean", ex.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-rv234 -bool".split(" "));
            fail("Expected exception");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unknown option: '-234' (while processing option: '-rv234')", ok.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABooleanWithUnmatchedArgsAllowed() { // ignores varargs
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new BooleanOptionsArity0_nAndParameters()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("-rv234 -bool".split(" "));
        assertEquals(Arrays.asList("-234"), cmd.getUnmatchedArguments());
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedWithSepParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-rv=234 -bool".split(" "));
            fail("was able to assign 234 to boolean");
        } catch (CommandLine.ParameterException ex) {
            assertEquals("Invalid value for option '-other': '234' is not a boolean", ex.getMessage());
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
            assertEquals("Invalid value for option '-bool': 'abc' is not a boolean", expected.getMessage());
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
            assertEquals("Expected parameter for option '-Long' but found '-boolean'", ex.getMessage());
        }
    }
    /** see <a href="https://github.com/remkop/picocli/issues/279">issue #279</a>  */
    @Test
    public void testSingleValueFieldWithOptionalParameter_279() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..1") String foo;
        }
        Sample sample1 = CommandLine.populateCommand(new Sample()); // not specified
        assertNull("optional option is null when option not specified", sample1.foo);

        Sample sample2 = CommandLine.populateCommand(new Sample(), "--foo"); // no arguments
        assertEquals("optional option is empty string when specified without args", "", sample2.foo);

        Sample sample3 = CommandLine.populateCommand(new Sample(), "--foo", "value"); // no arguments
        assertEquals("optional option has value when specified", "value", sample3.foo);
    }
    /** see <a href="https://github.com/remkop/picocli/issues/280">issue #280</a>  */
    @Test
    public void testSingleValueFieldWithOptionalParameter_280() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..1", fallbackValue = "213") String foo;
        }
        Sample sample1 = CommandLine.populateCommand(new Sample()); // not specified
        assertNull("optional option is null when option not specified", sample1.foo);

        Sample sample2 = CommandLine.populateCommand(new Sample(), "--foo"); // no arguments
        assertEquals("optional option is empty string when specified without args", "213", sample2.foo);

        Sample sample3 = CommandLine.populateCommand(new Sample(), "--foo", "value"); // no arguments
        assertEquals("optional option has value when specified", "value", sample3.foo);
    }
    @Test
    public void testSingleValueFieldWithTypedOptionalParameter_280() {
        @Command(name="sample")
        class Sample {
            @Option(names="--unit", arity="0..1", fallbackValue = "SECONDS") TimeUnit unit;
        }
        Sample sample1 = CommandLine.populateCommand(new Sample()); // not specified
        assertNull("optional option is null when option not specified", sample1.unit);

        Sample sample2 = CommandLine.populateCommand(new Sample(), "--unit"); // no arguments
        assertEquals("optional option is empty string when specified without args", TimeUnit.SECONDS, sample2.unit);

        Sample sample3 = CommandLine.populateCommand(new Sample(), "--unit", "MILLISECONDS"); // no arguments
        assertEquals("optional option has value when specified", TimeUnit.MILLISECONDS, sample3.unit);
    }
    @Test
    public void testListFieldOptionalParamWithFallback_280() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..*", fallbackValue = "213", defaultValue = "567") List<String> foo;

            @Option(names = "-x") boolean x;
        }
        Sample sample1 = CommandLine.populateCommand(new Sample()); // not specified
        assertEquals("option gets default when option not specified", Arrays.asList("567"), sample1.foo);

        Sample sample2 = CommandLine.populateCommand(new Sample(), "--foo"); // no arguments
        assertEquals("optional option is fallback list when specified without args",
                Arrays.asList("213"), sample2.foo);

        Sample sample2a = CommandLine.populateCommand(new Sample(), "--foo", "--foo"); // no arguments, twice
        assertEquals("optional option is fallback list for each specified without args",
                Arrays.asList("213", "213"), sample2a.foo);

        Sample sample2b = CommandLine.populateCommand(new Sample(), "--foo", "-x"); // no arguments
        assertEquals("optional option is fallback list for each specified without args",
                Arrays.asList("213"), sample2b.foo);

        Sample sample3 = CommandLine.populateCommand(new Sample(), "--foo", "value"); // no arguments
        assertEquals("optional option has value when specified", Arrays.asList("value"), sample3.foo);
    }
    @Test
    public void testArrayFieldOptionalParamWithFallback_280() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..*", fallbackValue = "213", defaultValue = "567")
            String[] foo;

            @Option(names = "-x") boolean x;
        }
        Sample sample1 = CommandLine.populateCommand(new Sample()); // not specified
        assertArrayEquals("option gets default when option not specified", new String[]{"567"}, sample1.foo);

        Sample sample2 = CommandLine.populateCommand(new Sample(), "--foo"); // no arguments
        assertArrayEquals("optional option is fallback list when specified without args",
                new String[]{"213"}, sample2.foo);

        Sample sample2a = CommandLine.populateCommand(new Sample(), "--foo", "--foo"); // no arguments, twice
        assertArrayEquals("optional option is fallback list for each specified without args",
                new String[]{"213", "213"}, sample2a.foo);

        Sample sample2b = CommandLine.populateCommand(new Sample(), "--foo", "-x"); // no arguments
        assertArrayEquals("optional option is fallback list for each specified without args",
                new String[]{"213"}, sample2b.foo);

        Sample sample3 = CommandLine.populateCommand(new Sample(), "--foo", "value"); // no arguments
        assertArrayEquals("optional option has value when specified", new String[]{"value"}, sample3.foo);
    }
    @Test
    public void testMapFieldOptionalParamWithFallback_280() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..*", fallbackValue = "a=b", defaultValue = "x=z")
            Map<String,String> foo;

            @Option(names = "-x") boolean x;
        }
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("x", "z");
        Sample sample1 = CommandLine.populateCommand(new Sample()); // not specified
        assertEquals("default when option not specified", expected, sample1.foo);

        expected.clear();
        expected.put("a", "b");
        Sample sample2 = CommandLine.populateCommand(new Sample(), "--foo"); // no arguments
        assertEquals("fallback when specified without args", expected, sample2.foo);

        expected.clear();
        expected.put("a", "b");
        Sample sample2a = CommandLine.populateCommand(new Sample(), "--foo", "--foo"); // no arguments, twice
        assertEquals("fallback when specified without args twice", expected, sample2a.foo);

        expected.clear();
        expected.put("a", "b");
        Sample sample2b = CommandLine.populateCommand(new Sample(), "--foo", "-x"); // no arguments, followed by other option
        assertEquals("fallback when specified without args followed by other option", expected, sample2b.foo);

        expected.clear();
        expected.put("aa", "bb");
        Sample sample3 = CommandLine.populateCommand(new Sample(), "--foo", "aa=bb"); // no arguments
        assertEquals("optional option has value when specified", expected, sample3.foo);
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
        assertArrayEquals(new int[]{ 42, 7 }, params.intParams);
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
            @Parameters int[] intParams;
            @Option(names = "-ints") int[] intOptions;
        }
        OptionsNoArityAndParameters
                params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-ints 1 2 3 4".split(" "));
        assertArrayEquals(Arrays.toString(params.intOptions),
                new int[] {1, }, params.intOptions);
        assertArrayEquals(Arrays.toString(params.intParams), new int[] {2, 3, 4}, params.intParams);

        // repeated occurrence
        params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-ints 1 -ints 2 3 4".split(" "));
        assertArrayEquals(Arrays.toString(params.intOptions),
                new int[] {1, 2, }, params.intOptions);
        assertArrayEquals(Arrays.toString(params.intParams), new int[] {3, 4}, params.intParams);

        try {
            CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-ints".split(" "));
            fail("expected MissingParameterException");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required parameter for option '-ints' (<intOptions>)", ok.getMessage());
            assertEquals(1, ok.getMissing().size());
            assertTrue(ok.getMissing().get(0).toString(), ok.getMissing().get(0) instanceof Model.OptionSpec);
        }
    }

    @Test
    public void testCharacterArrayOptionWithoutArityConsumesOneArgument() { // #192
        class OptionsNoArityAndParameters {
            @Parameters Character[] cParams;
            @Option(names = "-chars") Character[] cOptions;
        }
        OptionsNoArityAndParameters
            params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars 1 2 3 4".split(" "));
        assertArrayEquals(Arrays.toString(params.cOptions),
            new Character[] {'1', }, params.cOptions);
        assertArrayEquals(Arrays.toString(params.cParams), new Character[] {'2', '3', '4'}, params.cParams);

        // repeated occurrence
        params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars 1 -chars 2 3 4".split(" "));
        assertArrayEquals(Arrays.toString(params.cOptions),
            new Character[] {'1', '2', }, params.cOptions);
        assertArrayEquals(Arrays.toString(params.cParams), new Character[] {'3', '4'}, params.cParams);

        try {
            CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars".split(" "));
            fail("expected MissingParameterException");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required parameter for option '-chars' (<cOptions>)", ok.getMessage());
            assertEquals(1, ok.getMissing().size());
            assertTrue(ok.getMissing().get(0).toString(), ok.getMissing().get(0) instanceof Model.OptionSpec);
        }
    }

    @Test
    public void testCharArrayOption() { // #192
        class OptionsNoArityAndParameters {
            @Parameters(arity = "0..1") char[] charParams;
            @Option(names = "-chars") char[] charOptions;
        }
        OptionsNoArityAndParameters
            params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars abcd".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
            new char[] {'a', 'b', 'c', 'd'}, params.charOptions);
        assertArrayEquals(Arrays.toString(params.charParams), null, params.charParams);

        // repeated occurrence disallowed
        try {
            CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars ab -chars cd".split(" "));
            fail("Expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-chars' (<charOptions>) should be specified only once", ex.getMessage());
        }

        params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars ab cd".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
            new char[] {'a', 'b', }, params.charOptions);
        assertArrayEquals(Arrays.toString(params.charParams), new char[] {'c', 'd'}, params.charParams);

        try {
            CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars".split(" "));
            fail("expected MissingParameterException");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required parameter for option '-chars' (<charOptions>)", ok.getMessage());
            assertEquals(1, ok.getMissing().size());
            assertTrue(ok.getMissing().get(0).toString(), ok.getMissing().get(0) instanceof Model.OptionSpec);
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
        try {
            new CommandLine(new ArrayParamsNegativeArity());
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid negative range (min=-1, max=2147483647)", ex.getMessage());
        }
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
            assertEquals("Missing required parameter: '<params>'", ex.getMessage());
        }
    }

    @Test
    public void testMapOptionArity1_n() {
        class MapParamsArity1_n {
            @Option(names = {"-D", "--def"}, arity = "1..*", split = ",")
            Map<String, Integer> params;
        }
        // most verbose
        MapParamsArity1_n params = CommandLine.populateCommand(new MapParamsArity1_n(), "--def", "a=1", "--def", "b=2", "--def", "c=3");
        Map<String, Integer> expected = new LinkedHashMap<String, Integer>();
        expected.put("a", 1);
        expected.put("b", 2);
        expected.put("c", 3);
        assertEquals(expected, params.params);

        // option name once, followed by values
        params = CommandLine.populateCommand(new MapParamsArity1_n(), "--def", "aa=11", "bb=22", "cc=33");
        expected.clear();
        expected.put("aa", 11);
        expected.put("bb", 22);
        expected.put("cc", 33);
        assertEquals(expected, params.params);

        // most compact
        params = CommandLine.populateCommand(new MapParamsArity1_n(), "-Dx=4,y=5,z=6");
        expected.clear();
        expected.put("x", 4);
        expected.put("y", 5);
        expected.put("z", 6);
        assertEquals(expected, params.params);

        try {
            params = CommandLine.populateCommand(new MapParamsArity1_n(), "--def");
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter for option '--def' at index 0 (<String=Integer>)", ex.getMessage());
        }
    }

    @Test
    public void testMissingPositionalParameters() {
        class App {
            @Parameters(index = "0", paramLabel = "PARAM1") String p1;
            @Parameters(index = "1", paramLabel = "PARAM2") String p2;
            @Parameters(index = "2", paramLabel = "PARAM3") String p3;
        }
        try {
            CommandLine.populateCommand(new App(), "a");
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: 'PARAM2', 'PARAM3'", ex.getMessage());
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
    public void testNonVarargArrayParametersWithArity0() {
        class NonVarArgArrayParamsZeroArity {
            @Parameters(arity = "0")
            List<String> params;
        }
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity(), "a", "b", "c");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments from index 0: 'a', 'b', 'c'", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity(), "a");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 0: 'a'", ex.getMessage());
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
            assertEquals("Missing required parameter: '<params>'", ex.getMessage());
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
            assertEquals("Missing required parameter: '<parameters>'", ex.getMessage());
        }
    }

    @Test
    public void test130MixPositionalParamsWithOptions() {
        @Command(name = "test-command", description = "tests help from a command script")
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

        try {
            CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "-o", "v2", "p3");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Expected parameter 2 (of 2 mandatory parameters) for positional parameter at index 0..* (<parameters>) but found '-o'", ex.getMessage());
            assertEquals(1, ex.getMissing().size());
            assertTrue(ex.getMissing().get(0).toString(), ex.getMissing().get(0) instanceof PositionalParamSpec);
        }

        try {
            CommandLine.populateCommand(new Arg(), "-o", "v1", "p1", "p2", "-o", "v2", "p3");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<parameters>) requires at least 2 values, but only 1 were specified: [p3]", ex.getMessage());
            assertEquals(1, ex.getMissing().size());
            assertTrue(ex.getMissing().get(0).toString(), ex.getMissing().get(0) instanceof PositionalParamSpec);
        }
    }

    @Test
    public void test365_StricterArityValidation() {
        class Cmd {
            @Option(names = "-a", arity = "2") String[] a;
            @Option(names = "-b", arity = "1..2") String[] b;
            @Option(names = "-c", arity = "2..3") String[] c;
            @Option(names = "-v") boolean verbose;
        }
        assertMissing("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-a'",
                new Cmd(), "-a", "1", "-a", "2");

        assertMissing("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-v'",
                new Cmd(), "-a", "1", "-v");

        assertMissing("Expected parameter for option '-b' but found '-v'",
                new Cmd(), "-b", "-v");

        assertMissing("option '-c' at index 0 (<c>) requires at least 2 values, but only 1 were specified: [-a]",
                new Cmd(), "-c", "-a");

        assertMissing("Expected parameter 1 (of 2 mandatory parameters) for option '-c' but found '-a'",
                new Cmd(), "-c", "-a", "1", "2");

        assertMissing("Expected parameter 2 (of 2 mandatory parameters) for option '-c' but found '-a'",
                new Cmd(), "-c", "1", "-a");
    }

    @Test
    public void test365_StricterArityValidationWithMaps() {
        class Cmd {
            @Option(names = "-a", arity = "2") Map<String,String> a;
            @Option(names = "-b", arity = "1..2") Map<String,String> b;
            @Option(names = "-c", arity = "2..3") Map<String,String> c;
            @Option(names = "-v") boolean verbose;
        }
        assertMissing("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-a'",
                new Cmd(), "-a", "A=B", "-a", "C=D");

        assertMissing("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-v'",
                new Cmd(), "-a", "A=B", "-v");

        assertMissing("Expected parameter for option '-b' but found '-v'",
                new Cmd(), "-b", "-v");

        assertMissing("option '-c' at index 0 (<String=String>) requires at least 2 values, but only 1 were specified: [-a]",
                new Cmd(), "-c", "-a");

        assertMissing("Expected parameter 1 (of 2 mandatory parameters) for option '-c' but found '-a'",
                new Cmd(), "-c", "-a", "A=B", "C=D");

        assertMissing("Expected parameter 2 (of 2 mandatory parameters) for option '-c' but found '-a'",
                new Cmd(), "-c", "A=B", "-a");
    }
    private void assertMissing(String expected, Object command, String... args) {
        try {
            CommandLine.populateCommand(command, args);
            fail("Expected missing param exception");
        } catch (MissingParameterException ex) {
            assertEquals(expected, ex.getMessage());
        }
    }
    @Test
    public void test1125_ArityValidation() {
        class Cmd {
            @Option(names = "-a", arity = "2") String[] a;
            @Option(names = "-b", arity = "1..2") String[] b;
            @Option(names = "-c", arity = "2..3") String[] c;
            @Option(names = "-v") boolean verbose;
        }
        assertWithAllowOptions("Unmatched argument at index 3: '2'",
            new Cmd(), "-a", "1", "-a", "2");

        Cmd bean = new Cmd();
        new CommandLine(bean).setAllowOptionsAsOptionParameters(true).parseArgs("-a", "1", "-v");
        assertArrayEquals(new String[]{"1", "-v"}, bean.a);

        bean = new Cmd();
        new CommandLine(bean).setAllowOptionsAsOptionParameters(true).parseArgs("-b", "-v");
        assertArrayEquals(new String[]{"-v"}, bean.b);

        assertWithAllowOptions("option '-c' at index 0 (<c>) requires at least 2 values, but only 1 were specified: [-a]",
            new Cmd(), "-c", "-a");

        bean = new Cmd();
        new CommandLine(bean).setAllowOptionsAsOptionParameters(true).parseArgs("-c", "-a", "1", "2");
        assertArrayEquals(new String[]{"-a", "1", "2"}, bean.c);

        bean = new Cmd();
        new CommandLine(bean).setAllowOptionsAsOptionParameters(true).parseArgs("-c", "1", "-a");
        assertArrayEquals(new String[]{"1", "-a"}, bean.c);
    }
    @Test
    public void test1125_ArityValidationWithMaps() {
        class Cmd {
            @Option(names = "-a", arity = "2") Map<String,String> a;
            @Option(names = "-b", arity = "1..2") Map<String,String> b;
            @Option(names = "-c", arity = "2..3") Map<String,String> c;
            @Option(names = "-v") boolean verbose;
        }
        assertWithAllowOptions("Value for option option '-a' at index 0 (<String=String>) should be in KEY=VALUE format but was -a",
            new Cmd(), "-a", "A=B", "-a", "C=D");

        assertWithAllowOptions("Value for option option '-a' at index 0 (<String=String>) should be in KEY=VALUE format but was -v",
            new Cmd(), "-a", "A=B", "-v");

        assertWithAllowOptions("Value for option option '-b' at index 0 (<String=String>) should be in KEY=VALUE format but was -v",
            new Cmd(), "-b", "-v");

        assertWithAllowOptions("Value for option option '-c' at index 0 (<String=String>) should be in KEY=VALUE format but was -a",
            new Cmd(), "-c", "A=B", "-a");
    }
    private void assertWithAllowOptions(String expected, Object command, String... args) {
        try {
            new CommandLine(command).setAllowOptionsAsOptionParameters(true).parseArgs(args);
            fail("Expected unmatched arg exception");
        } catch (ParameterException ex) {
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void test285VarargPositionalShouldNotConsumeOptions() {
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
    public void test285VarargPositionalShouldConsumeOptionsAfterDoubleDash() {
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

    @Test
    public void testIfStopAtPositional_VarargPositionalShouldConsumeOptions() {
        class Cmd {
            @Option(names = "--alpha") String alpha;
            @Parameters(index = "0", arity = "1") String foo;
            @Parameters(index = "1..*", arity = "*") List<String> params;
        }
        Cmd cmd = new Cmd();
        System.setProperty("picocli.trace", "DEBUG");
        new CommandLine(cmd).setStopAtPositional(true).parseArgs("foo", "xx", "--alpha", "--beta");
        assertEquals("foo", cmd.foo);
        assertEquals(null, cmd.alpha);
        assertEquals(Arrays.asList("xx", "--alpha", "--beta"), cmd.params);
        assertTrue(systemErrRule.getLog(), systemErrRule.getLog().contains("Parser was configured with stopAtPositional=true, treating remaining arguments as positional parameters."));
    }

    @Test
    public void testIfStopAtPositional_PositionalShouldConsumeOptions() {
        class Cmd {
            @Option(names = "--alpha") String alpha;
            @Parameters(index = "0") String foo;
            @Parameters(index = "1..*") List<String> params;
        }
        Cmd cmd = new Cmd();
        new CommandLine(cmd).setStopAtPositional(true).parseArgs("foo", "xx", "--alpha", "--beta");
        assertEquals("foo", cmd.foo);
        assertEquals(null, cmd.alpha);
        assertEquals(Arrays.asList("xx", "--alpha", "--beta"), cmd.params);
    }

    @Test
    public void testPosixAttachedOnly1() {
        class ValSepC {
            @Option(names = "-a", arity="2") String[] a;
            @Option(names = "-b", arity="2", split=",") String[] b;
            @Option(names = "-c", arity="*", split=",") String[] c;
            @Option(names = "-d") boolean d;
            @Option(names = "-e", arity="1", split=",") boolean[] e;
            @Unmatched String[] remaining;
        }
        ValSepC val1 = parseCommonsCliCompatible(new ValSepC(), "-a 1 2 3 4".split(" "));
        assertArrayEquals(new String[]{"1", "2"}, val1.a);
        assertArrayEquals(new String[]{"3", "4"}, val1.remaining);

        ValSepC val2 = parseCommonsCliCompatible(new ValSepC(), "-a1 -a2 3".split(" "));
        assertArrayEquals(new String[]{"1", "2"}, val2.a);
        assertArrayEquals(new String[]{"3"}, val2.remaining);

        ValSepC val3 = parseCommonsCliCompatible(new ValSepC(), "-b1,2".split(" "));
        assertArrayEquals(new String[]{"1", "2"}, val3.b);

        ValSepC val4 = parseCommonsCliCompatible(new ValSepC(), "-c 1".split(" "));
        assertArrayEquals(new String[]{"1"}, val4.c);

        ValSepC val5 = parseCommonsCliCompatible(new ValSepC(), "-c1".split(" "));
        assertArrayEquals(new String[]{"1"}, val5.c);

        ValSepC val6 = parseCommonsCliCompatible(new ValSepC(), "-c1,2,3".split(" "));
        assertArrayEquals(new String[]{"1", "2", "3"}, val6.c);

        ValSepC val7 = parseCommonsCliCompatible(new ValSepC(), "-d".split(" "));
        assertTrue(val7.d);
        assertNull(val7.e);

        ValSepC val8 = parseCommonsCliCompatible(new ValSepC(), "-e true".split(" "));
        assertFalse(val8.d);
        assertTrue(val8.e[0]);
    }

    @Test
    public void testPosixClusteredBooleansAttached() {
        class App {
            @Option(names = "-a") boolean a;
            @Option(names = "-b") boolean b;
            @Option(names = "-c") boolean c;
            @Unmatched String[] remaining;
        }

        App app = parseCommonsCliCompatible(new App(), "-abc".split(" "));
        assertTrue("a", app.a);
        assertTrue("b", app.b);
        assertTrue("c", app.c);
        assertNull(app.remaining);

        app = parseCommonsCliCompatible(new App(), "-abc -d".split(" "));
        assertTrue("a", app.a);
        assertTrue("b", app.b);
        assertTrue("c", app.c);
        assertArrayEquals(new String[]{"-d"}, app.remaining);
    }

    @Test
    public void testPosixClusteredBooleanArraysAttached() {
        class App {
            @Option(names = "-a") boolean[] a;
            @Option(names = "-b") boolean[] b;
            @Option(names = "-c") boolean[] c;
            @Unmatched String[] remaining;
        }

        App app = parseCommonsCliCompatible(new App(), "-abc".split(" "));
        assertArrayEquals("a", new boolean[]{true}, app.a);
        assertArrayEquals("b", new boolean[]{true}, app.b);
        assertArrayEquals("c", new boolean[]{true}, app.c);
        assertNull(app.remaining);

        app = parseCommonsCliCompatible(new App(), "-abc -d".split(" "));
        assertArrayEquals("a", new boolean[]{true}, app.a);
        assertArrayEquals("b", new boolean[]{true}, app.b);
        assertArrayEquals("c", new boolean[]{true}, app.c);
        assertArrayEquals(new String[]{"-d"}, app.remaining);

        app = parseCommonsCliCompatible(new App(), "-aaabbccc -d".split(" "));
        assertArrayEquals("a", new boolean[]{true, true, true}, app.a);
        assertArrayEquals("b", new boolean[]{true, true}, app.b);
        assertArrayEquals("c", new boolean[]{true, true, true}, app.c);
        assertArrayEquals(new String[]{"-d"}, app.remaining);
    }

    @Test
    public void testPosixAttachedOnly3() {
        class ValSepC {
            @Option(names = "-a", arity = "2")
            String[] a;
            @Unmatched
            String[] remaining;
        }

        try {
            parseCommonsCliCompatible(new ValSepC(), "-a 1 -a 2".split(" "));
            fail("Expected exception: Arity not satisfied");
        } catch (Exception ok) {
            assertEquals("Expected parameter 2 (of 2 mandatory parameters) for option '-a' but found '-a'", ok.getMessage());
        }
    }

    @Test
    public void testPosixAttachedOnly2() {
        class ValSepC {
            @Option(names = "-a", arity="2") String[] a;
            @Unmatched String[] remaining;
        }
        try {
            parseCommonsCliCompatible(new ValSepC(), "-a 1".split(" "));
            fail();
        } catch (Exception ok) {
        }

        ValSepC val1 = parseCommonsCliCompatible(new ValSepC(), "-a1".split(" "));
        assertArrayEquals(new String[]{"1"}, val1.a);

        val1 = parseCommonsCliCompatible(new ValSepC(), "-a1 -a2".split(" "));
        assertArrayEquals(new String[]{"1", "2"}, val1.a);

        val1 = parseCommonsCliCompatible(new ValSepC(), "-a1 -a2 -a3".split(" "));
        assertArrayEquals(new String[]{"1", "2", "3"}, val1.a);

        try {
            parseCommonsCliCompatible(new ValSepC(), "-a 1 -a 2 -a 3".split(" "));
            fail();
        } catch (Exception ok) {
        }

        val1 = parseCommonsCliCompatible(new ValSepC(), "-a 1 2".split(" "));
        assertArrayEquals(new String[]{"1", "2"}, val1.a);

        val1 = parseCommonsCliCompatible(new ValSepC(), "-a1 2".split(" "));
        assertArrayEquals(new String[]{"1"}, val1.a);
        assertArrayEquals(new String[]{"2"}, val1.remaining);
    }

    @Test
    public void testCommonsCliCompatibleSeparatorHandling() {
        class ValSepC {
            @Option(names = "-a", arity="1..2") String[] a;
            @Option(names = "-b", arity="1..2", split=",") String[] b;
            @Option(names = "-c", arity="1..*", split=",") String[] c;
            @Unmatched String[] remaining;
        }
        ValSepC val3a = parseCommonsCliCompatible(new ValSepC(), "-b1,2,3".split(" "));
        assertArrayEquals(new String[]{"1", "2,3"}, val3a.b);
    }

    @Test
    public void testCommonsCliCompatibleSeparatorHandlingForMaps() {
        class ValSepC {
            @Option(names = "-b", arity="1..2", split=",") Map<String, String> b;
        }
        ValSepC val3a = parseCommonsCliCompatible(new ValSepC(), "-ba=1,b=2,c=3".split(" "));
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("a", "1");
        expected.put("b", "2,c=3");
        assertEquals(expected, val3a.b);
    }

    private <T> T parseCommonsCliCompatible(T obj, String[] args) {
        CommandLine cmd = new CommandLine(obj);
        cmd.getCommandSpec().parser()
                .limitSplit(true)
                .aritySatisfiedByAttachedOptionParam(true);
        cmd.parseArgs(args);
        return obj;
    }

    @Ignore("#370 Needs support for `last position` in index")
    @Test
    public void test370UnboundedArityParam() {
        class App {
            @Parameters(index = "0", description = "Folder")
            private String folder;

            // TODO add support for $-x in index to mean the x-before-last element
            @Parameters(index = "1..$-1", description = "Source paths")
            private String [] sources;

            // TODO add support for $ in index to mean the last element
            @Parameters(index = "$", description = "Destination folder path")
            private String destination;
        }

        //setTraceLevel(CommandLine.TraceLevel.DEBUG);
        App app = new App();
        new CommandLine(app)
                .setOverwrittenOptionsAllowed(true)
                .parseArgs("folder source1 source2 source3 destination".split(" "));

        assertEquals("folder", app.folder);
        assertArrayEquals(new String[]{"source1", "source2", "source2"}, app.sources);
        assertEquals("destination", app.destination);
        //CommandLine.usage(new App(), System.out);
    }

    @Test
    public void testArityZeroForBooleanOption() {
        class App {
            @Option(names = "--explicit", arity = "0") boolean explicit;
            @Option(names = "--implicit") boolean implicit;
        }
        try {
            new CommandLine(new App()).parseArgs("--implicit=false --explicit=false".split(" "));
            fail("--explicit option should not accept parameters");
        } catch (ParameterException ex) {
            assertEquals("option '--explicit' should be specified without 'false' parameter", ex.getMessage());
        }
    }

    @Test(expected = InitializationException.class)
    public void testRangeConstructorDisallowsNegativeMin() {
        new Range(-1, 2, false, false, "");
    }

    @Test(expected = InitializationException.class)
    public void testRangeConstructorDisallowsNegativeMax() {
        new Range(0, -2, false, false, "");
    }

    @Test
    public void testRangeArityMismatchingTypeCount() {
        class InvalidMapTypes {
            @Option(names = "-D", arity = "1..3", type = Integer.class) // invalid: only one type
            TreeMap<Integer, String> map;
        }
        try {
            CommandLine.populateCommand(new InvalidMapTypes(), "-D", "1=a");
            fail("expect exception");
        } catch (ParameterException ex) {
            assertEquals("field java.util.TreeMap<Integer, String> picocli.ArityTest$1InvalidMapTypes.map needs two types (one for the map key, one for the value) but only has 1 types configured.", ex.getMessage());
        }
    }

    @Test
    public void testVariableArityMap() {
        class App {
            @Option(names = "-D", arity = "1..*")
            TreeMap<Integer, String> map;
        }

        App app = CommandLine.populateCommand(new App(), "-D", "1=a", "2=b", "3=c");
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");
        assertEquals(map, app.map);
    }

    @Test
    public void testRangeArity0To3Map0() {
        class App {
            @Option(names = "-D", arity = "0..3")
            TreeMap<Integer, String> map;
        }

        App app = CommandLine.populateCommand(new App(), "-D");
        assertEquals(new TreeMap<Integer, String>(), app.map);
    }

    @Test
    public void testRangeArity1To3Map1() {
        class App {
            @Option(names = "-D", arity = "1..3")
            TreeMap<Integer, String> map;

            @Option(names = "-x")
            int x;
        }

        App app = CommandLine.populateCommand(new App(), "-D", "1=a", "-x", "123");
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        map.put(1, "a");
        assertEquals(map, app.map);
        assertEquals(123, app.x);
    }

    @Test
    public void testRangeArity1To3Map2() {
        class App {
            @Option(names = "-D", arity = "1..3")
            TreeMap<Integer, String> map;
        }

        App app = CommandLine.populateCommand(new App(), "-D", "1=a", "2=b");
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        map.put(1, "a");
        map.put(2, "b");
        assertEquals(map, app.map);
    }

    @Test
    public void testRangeArity1To3Map3() {
        class App {
            @Option(names = "-D", arity = "1..3")
            TreeMap<Integer, String> map;
        }

        App app = CommandLine.populateCommand(new App(), "-D", "1=a", "2=b", "3=c");
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");
        assertEquals(map, app.map);
    }

    @Test
    public void testMapArgumentsArity() {
        class App {
            @Parameters(arity = "2") Map<String, String> map;
        }
        try {
            CommandLine.populateCommand(new App(), "a=c");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<String=String>) requires at least 2 values, but only 1 were specified: [a=c]", ex.getMessage());
        }
    }

    @Test
    public void testOverlap() {
        Range r1 = Range.valueOf("1..5");
        assertTrue(r1.overlaps(Range.valueOf("1..1")));
        assertTrue(r1.overlaps(Range.valueOf("2..2")));
        assertTrue(r1.overlaps(Range.valueOf("2..5")));
        assertTrue(r1.overlaps(Range.valueOf("5")));
        assertTrue(r1.overlaps(Range.valueOf("0..6")));
        assertTrue(r1.overlaps(Range.valueOf("0..*")));
        assertFalse(r1.overlaps(Range.valueOf("6")));
        assertFalse(r1.overlaps(Range.valueOf("0")));
    }

    @Test
    public void testCustomEndOfOptionsDelimiter() {
        class App {
            @Option(names = "-x", arity = "*")
            List<String> option;

            @Unmatched
            List<String> unmatched;
        }

        App app = new App();
        new CommandLine(app).setEndOfOptionsDelimiter(";").parseArgs("-x", "a", "b", ";", "x", "y");
        assertEquals(Arrays.asList("a", "b"), app.option);
        assertEquals(Arrays.asList("x", "y"), app.unmatched);

        app = new App();
        new CommandLine(app).parseArgs("-x", "a", "b", "--", "x", "y");
        assertEquals(Arrays.asList("a", "b"), app.option);
        assertEquals(Arrays.asList("x", "y"), app.unmatched);

        app = new App();
        new CommandLine(app).parseArgs("-x", "a", "b", ";", "x", "y");
        assertEquals(Arrays.asList("a", "b", ";", "x", "y"), app.option);
    }

    @Test
    public void test1125_CustomEndOfOptionsDelimiter() {
        class App {
            @Option(names = "-x", arity = "*")
            List<String> option;

            @Unmatched
            List<String> unmatched;
        }

        App app = new App();
        new CommandLine(app).setAllowOptionsAsOptionParameters(true).setEndOfOptionsDelimiter(";")
            .parseArgs("-x", "a", "b", ";", "x", "y");
        assertEquals(Arrays.asList("a", "b"), app.option);
        assertEquals(Arrays.asList("x", "y"), app.unmatched);

        app = new App();
        new CommandLine(app).setAllowOptionsAsOptionParameters(true).parseArgs("-x", "a", "b", "--", "x", "y");
        assertEquals(Arrays.asList("a", "b"), app.option);
        assertEquals(Arrays.asList("x", "y"), app.unmatched);

        app = new App();
        new CommandLine(app).setAllowOptionsAsOptionParameters(true).parseArgs("-x", "a", "b", ";", "x", "y");
        assertEquals(Arrays.asList("a", "b", ";", "x", "y"), app.option);
    }

    @Test
    public void testUnmatchedListCleared() {
        class App {
            @Unmatched
            List<String> unmatchedList;

            @Unmatched
            String[] unmatchedArray;
        }

        App app = new App();
        CommandLine cmd = new CommandLine(app);
        cmd.parseArgs("--", "a", "b");
        assertEquals(Arrays.asList("a", "b"), app.unmatchedList);
        assertArrayEquals(new String[]{"a", "b"}, app.unmatchedArray);

        cmd.parseArgs("--", "x", "y");
        assertEquals(Arrays.asList("x", "y"), app.unmatchedList);
        assertArrayEquals(new String[]{"x", "y"}, app.unmatchedArray);
    }
}
