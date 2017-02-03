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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.CommandLineTest.InvalidAnnotations.*;

/**
 * CommandLine unit tests.
 */
// DONE arrays
// TODO collection fields
// DONE all built-in types
// TODO private fields, public fields (methods?)
// TODO arity 2, 3
// TODO arity -1, -2, -3
// TODO arity ignored for single-value types (non-array, non-collection)
// DONE positional arguments with '--' separator (where positional arguments look like options)
// DONE positional arguments without '--' separator (based on arity of last option?)
// TODO positional arguments based on arity of last option
// TODO ambiguous options: writing --input ARG (as opposed to --input=ARG) is ambiguous,
// meaning it is not possible to tell whether ARG is option's argument or a positional argument.
// In usage patterns this will be interpreted as an option with argument only if a description (covered below)
// for that option is provided.
// Otherwise it will be interpreted as an option and separate positional argument.
// TODO ambiguous short options: ambiguity with the -f FILE and -fFILE notation.
// In the latter case it is not possible to tell whether it is a number of stacked short options,
// or an option with an argument. These notations will be interpreted as an option with argument only if a
// description for the option is provided.
// TODO compact flags
// TODO compact flags where last option has an argument, separate by space
// TODO compact flags where last option has an argument attached to the option character
// TODO long options with argument separate by space
// TODO long options with argument separated by '=' (no spaces)
// TODO test superclass bean and child class bean where child class field shadows super class and have same annotation Option name
// TODO test superclass bean and child class bean where child class field shadows super class and have different annotation Option name
// TODO -vrx, -vro outputFile, -vrooutputFile, -vro=outputFile, -vro:outputFile, -vro=, -vro:, -vro
// TODO --out outputFile, --out=outputFile, --out:outputFile, --out=, --out:, --out
public class CommandLineTest {

    private static class SupportedTypes {
        @Option(names = "-boolean")       boolean booleanField;
        @Option(names = "-Boolean")       Boolean aBooleanField;
        @Option(names = "-byte")          byte byteField;
        @Option(names = "-Byte")          Byte aByteField;
        @Option(names = "-char")          char charField;
        @Option(names = "-Character")     Character aCharacterField;
        @Option(names = "-short")         short shortField;
        @Option(names = "-Short")         Short aShortField;
        @Option(names = "-int")           int intField;
        @Option(names = "-Integer")       Integer anIntegerField;
        @Option(names = "-long")          long longField;
        @Option(names = "-Long")          Long aLongField;
        @Option(names = "-float")         float floatField;
        @Option(names = "-Float")         Float aFloatField;
        @Option(names = "-double")        double doubleField;
        @Option(names = "-Double")        Double aDoubleField;
        @Option(names = "-String")        String aStringField;
        @Option(names = "-StringBuilder") StringBuilder aStringBuilderField;
        @Option(names = "-CharSequence")  CharSequence aCharSequenceField;
        @Option(names = "-File")          File aFileField;
        @Option(names = "-URL")           URL anURLField;
        @Option(names = "-URI")           URI anURIField;
        @Option(names = "-Date")          Date aDateField;
        @Option(names = "-Time")          Time aTimeField;
        @Option(names = "-BigDecimal")    BigDecimal aBigDecimalField;
        @Option(names = "-BigInteger")    BigInteger aBigIntegerField;
        @Option(names = "-Charset")       Charset aCharsetField;
        @Option(names = "-InetAddress")   InetAddress anInetAddressField;
        @Option(names = "-Pattern")       Pattern aPatternField;
        @Option(names = "-UUID")          UUID anUUIDField;
    }
    @Test
    public void testDefaults() {
        SupportedTypes bean = CommandLine.parse(new SupportedTypes());
        assertEquals("boolean", false, bean.booleanField);
        assertEquals("Boolean", null, bean.aBooleanField);
        assertEquals("byte", 0, bean.byteField);
        assertEquals("Byte", null, bean.aByteField);
        assertEquals("char", 0, bean.charField);
        assertEquals("Character", null, bean.aCharacterField);
        assertEquals("short", 0, bean.shortField);
        assertEquals("Short", null, bean.aShortField);
        assertEquals("int", 0, bean.intField);
        assertEquals("Integer", null, bean.anIntegerField);
        assertEquals("long", 0, bean.longField);
        assertEquals("Long", null, bean.aLongField);
        assertEquals("float", 0f, bean.floatField, Float.MIN_VALUE);
        assertEquals("Float", null, bean.aFloatField);
        assertEquals("double", 0d, bean.doubleField, Double.MIN_VALUE);
        assertEquals("Double", null, bean.aDoubleField);
        assertEquals("String", null, bean.aStringField);
        assertEquals("StringBuilder", null, bean.aStringBuilderField);
        assertEquals("CharSequence", null, bean.aCharSequenceField);
        assertEquals("File", null, bean.aFileField);
        assertEquals("URL", null, bean.anURLField);
        assertEquals("URI", null, bean.anURIField);
        assertEquals("Date", null, bean.aDateField);
        assertEquals("Time", null, bean.aTimeField);
        assertEquals("BigDecimal", null, bean.aBigDecimalField);
        assertEquals("BigInteger", null, bean.aBigIntegerField);
        assertEquals("Charset", null, bean.aCharsetField);
        assertEquals("InetAddress", null, bean.anInetAddressField);
        assertEquals("Pattern", null, bean.aPatternField);
        assertEquals("UUID", null, bean.anUUIDField);
    }
    @Test
    public void testTypeConversionSucceedsForValidInput()
            throws MalformedURLException, URISyntaxException, UnknownHostException, ParseException {
        SupportedTypes bean = CommandLine.parse(new SupportedTypes(),
                "-boolean", "-Boolean", //
                "-byte", "12", "-Byte", "23", //
                "-char", "p", "-Character", "i", //
                "-short", "34", "-Short", "45", //
                "-int", "56", "-Integer", "67", //
                "-long", "78", "-Long", "89", //
                "-float", "1.23", "-Float", "2.34", //
                "-double", "3.45", "-Double", "4.56", //
                "-String", "abc", "-StringBuilder", "bcd", "-CharSequence", "xyz", //
                "-File", "abc.txt", //
                "-URL", "http://pico-cli.github.io", //
                "-URI", "http://pico-cli.github.io/index.html", //
                "-Date", "2017-01-30", //
                "-Time", "23:59:59", //
                "-BigDecimal", "12345678901234567890.123", //
                "-BigInteger", "123456789012345678901", //
                "-Charset", "UTF8", //
                "-InetAddress", InetAddress.getLocalHost().getHostName(), //
                "-Pattern", "a*b", //
                "-UUID", "c7d51423-bf9d-45dd-a30d-5b16fafe42e2"
        );
        assertEquals("boolean", true, bean.booleanField);
        assertEquals("Boolean", Boolean.TRUE, bean.aBooleanField);
        assertEquals("byte", 12, bean.byteField);
        assertEquals("Byte", Byte.valueOf((byte) 23), bean.aByteField);
        assertEquals("char", 'p', bean.charField);
        assertEquals("Character", Character.valueOf('i'), bean.aCharacterField);
        assertEquals("short", 34, bean.shortField);
        assertEquals("Short", Short.valueOf((short) 45), bean.aShortField);
        assertEquals("int", 56, bean.intField);
        assertEquals("Integer", Integer.valueOf(67), bean.anIntegerField);
        assertEquals("long", 78L, bean.longField);
        assertEquals("Long", Long.valueOf(89L), bean.aLongField);
        assertEquals("float", 1.23f, bean.floatField, Float.MIN_VALUE);
        assertEquals("Float", Float.valueOf(2.34f), bean.aFloatField);
        assertEquals("double", 3.45, bean.doubleField, Double.MIN_VALUE);
        assertEquals("Double", Double.valueOf(4.56), bean.aDoubleField);
        assertEquals("String", "abc", bean.aStringField);
        assertEquals("StringBuilder type", StringBuilder.class, bean.aStringBuilderField.getClass());
        assertEquals("StringBuilder", "bcd", bean.aStringBuilderField.toString());
        assertEquals("CharSequence", "xyz", bean.aCharSequenceField);
        assertEquals("File", new File("abc.txt"), bean.aFileField);
        assertEquals("URL", new URL("http://pico-cli.github.io"), bean.anURLField);
        assertEquals("URI", new URI("http://pico-cli.github.io/index.html"), bean.anURIField);
        assertEquals("Date", new SimpleDateFormat("yyyy-MM-dd").parse("2017-01-30"), bean.aDateField);
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss").parse("23:59:59").getTime()), bean.aTimeField);
        assertEquals("BigDecimal", new BigDecimal("12345678901234567890.123"), bean.aBigDecimalField);
        assertEquals("BigInteger", new BigInteger("123456789012345678901"), bean.aBigIntegerField);
        assertEquals("Charset", Charset.forName("UTF8"), bean.aCharsetField);
        assertEquals("InetAddress", InetAddress.getByName(InetAddress.getLocalHost().getHostName()), bean.anInetAddressField);
        assertEquals("Pattern", Pattern.compile("a*b").pattern(), bean.aPatternField.pattern());
        assertEquals("UUID", UUID.fromString("c7d51423-bf9d-45dd-a30d-5b16fafe42e2"), bean.anUUIDField);
    }
    @Test
    public void testTimeFormatHHmmSupported() throws ParseException {
        SupportedTypes bean = CommandLine.parse(new SupportedTypes(), "-Time", "23:59");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm").parse("23:59").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssSupported() throws ParseException {
        SupportedTypes bean = CommandLine.parse(new SupportedTypes(), "-Time", "23:59:58");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss").parse("23:59:58").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssDotSSSSupported() throws ParseException {
        SupportedTypes bean = CommandLine.parse(new SupportedTypes(), "-Time", "23:59:58.123");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss.SSS").parse("23:59:58.123").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssCommaSSSSupported() throws ParseException {
        SupportedTypes bean = CommandLine.parse(new SupportedTypes(), "-Time", "23:59:58,123");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss,SSS").parse("23:59:58,123").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssSSSInvalidError() throws ParseException {
        try {
            CommandLine.parse(new SupportedTypes(), "-Time", "23:59:58;123");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:58;123' is not a HH:mm[:ss[.SSS]] time", expected.getMessage());
        }
    }
    @Test
    public void testTimeFormatHHmmssDotInvalidError() throws ParseException {
        try {
            CommandLine.parse(new SupportedTypes(), "-Time", "23:59:58.");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:58.' is not a HH:mm[:ss[.SSS]] time", expected.getMessage());
        }
    }
    @Test
    public void testTimeFormatHHmmsssInvalidError() throws ParseException {
        try {
            CommandLine.parse(new SupportedTypes(), "-Time", "23:59:587");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:587' is not a HH:mm[:ss[.SSS]] time", expected.getMessage());
        }
    }
    @Test
    public void testTimeFormatHHmmssColonInvalidError() throws ParseException {
        try {
            CommandLine.parse(new SupportedTypes(), "-Time", "23:59:");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:' is not a HH:mm[:ss[.SSS]] time", expected.getMessage());
        }
    }
    @Test
    public void testDateFormatYYYYmmddInvalidError() throws ParseException {
        try {
            CommandLine.parse(new SupportedTypes(), "-Date", "20170131");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'20170131' is not a yyyy-MM-dd date", expected.getMessage());
        }
    }
    @Test
    public void testCharConverterInvalidError() throws ParseException {
        try {
            CommandLine.parse(new SupportedTypes(), "-Character", "aa");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'aa' is not a single character.", expected.getMessage());
        }
        try {
            CommandLine.parse(new SupportedTypes(), "-char", "aa");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'aa' is not a single character.", expected.getMessage());
        }
    }
    @Test
    public void testNumberConvertersInvalidError() {
        parseInvalidValue("-Byte", "aa");
        parseInvalidValue("-byte", "aa");
        parseInvalidValue("-Short", "aa");
        parseInvalidValue("-short", "aa");
        parseInvalidValue("-Integer", "aa");
        parseInvalidValue("-int", "aa");
        parseInvalidValue("-Long", "aa");
        parseInvalidValue("-long", "aa");
        parseInvalidValue("-Float", "aa");
        parseInvalidValue("-float", "aa");
        parseInvalidValue("-Double", "aa");
        parseInvalidValue("-double", "aa");
        parseInvalidValue("-BigDecimal", "aa");
        parseInvalidValue("-BigInteger", "aa");
    }
    @Test
    public void testDomainObjectConvertersInvalidError() {
        parseInvalidValue("-URL", ":::");
        parseInvalidValue("-URI", ":::");
        parseInvalidValue("-Charset", "aa");
        parseInvalidValue("-InetAddress", "::a?*!a");
        parseInvalidValue("-Pattern", "[[(aa");
        parseInvalidValue("-UUID", "aa");
    }

    private void parseInvalidValue(String option, String value) {
        try {
            CommandLine.parse(new SupportedTypes(), option, value);
            fail("Invalid format " + value + " was accepted for " + option);
        } catch (ParameterException expected) {
            String type = option.substring(1);
            assertEquals("Could not convert '" + value + "' to a " + type, expected.getMessage());
        }
    }

    static class InvalidAnnotations {
        /** Duplicate parameter names are invalid. */
        static class DuplicateOptions {
            @Option(names = "-duplicate") public int value1;
            @Option(names = "-duplicate") public int value2;
        }
        static class ClashingAnnotation {
            @Option(names = "-o")
            @Parameters
            public String[] bothOptionAndParameters;
        }
    }
    @Test(expected = DuplicateOptionAnnotationsException.class)
    public void testDuplicateOptionsAreRejected() {
        new CommandLine(new DuplicateOptions());
    }

    @Test(expected = ParameterException.class)
    public void testClashingAnnotationsAreRejected() {
        new CommandLine(new ClashingAnnotation());
    }

    private static class FinalFields {
        @Option(names = "-f") private final String field = null;
    }
    @Test
    public void testCanInitializeFinalFields() {
        FinalFields ff = CommandLine.parse(new FinalFields(), "-f", "some value");
        assertEquals("some value", ff.field);
    }

    private static class RequiredField {
        @Option(names = "--required", required = true) private String required;
    }
    @Test(expected = MissingParameterException.class)
    public void testErrorIfRequiredOptionNotSpecified() {
        CommandLine.parse(new RequiredField(), "arg1", "arg2");
    }
    @Test
    public void testNoErrorIfRequiredOptionSpecified() {
        CommandLine.parse(new RequiredField(), "--required", "arg1", "arg2");
    }

    private class CompactFields {
        @Option(names = "-v") boolean verbose;
        @Option(names = "-r") boolean recursive;
        @Option(names = "-o") File outputFile;
        @Parameters File[] inputFiles;
    }
    @Test
    public void testCompactFieldsAnyOrder() {
        //cmd -a -o arg path path
        //cmd -o arg -a path path
        //cmd -a -o arg -- path path
        //cmd -a -oarg path path
        //cmd -aoarg path path
        CompactFields compact = CommandLine.parse(new CompactFields(), "-rvoout");
        verifyCompact(compact, true, true, "out", null);

        // change order within compact group
        compact = CommandLine.parse(new CompactFields(), "-vroout");
        verifyCompact(compact, true, true, "out", null);

        compact = CommandLine.parse(new CompactFields(), "-rv p1 p2".split(" "));
        verifyCompact(compact, true, true, null, fileArray("p1", "p2"));

        compact = CommandLine.parse(new CompactFields(), "-voout p1 p2".split(" "));
        verifyCompact(compact, true, false, "out", fileArray("p1", "p2"));

        compact = CommandLine.parse(new CompactFields(), "-voout -r p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.parse(new CompactFields(), "-r -v -oout p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.parse(new CompactFields(), "-oout -r -v p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.parse(new CompactFields(), "-rvo out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }

    @Test
    public void testCompactWithOptionParamSeparatePlusParameters() {
        CompactFields compact = CommandLine.parse(new CompactFields(), "-r -v -o out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }

    @Test
    public void testOptionsAfterParamAreInterpretedAsParameters() {
        CompactFields compact = CommandLine.parse(new CompactFields(), "-r -v p1 -o out p2".split(" "));
        verifyCompact(compact, true, true, null, fileArray("p1", "-o", "out", "p2"));
    }

    @Test
    public void testDoubleDashSeparatesPositionalParameters() {
        CompactFields compact = CommandLine.parse(new CompactFields(), "-oout -- -r -v p1 p2".split(" "));
        verifyCompact(compact, false, false, "out", fileArray("-r", "-v", "p1", "p2"));
    }

    private File[] fileArray(final String ... paths) {
        File[] result = new File[paths.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new File(paths[i]);
        }
        return result;
    }

    private void verifyCompact(CompactFields compact, boolean verbose, boolean recursive, String out, File[] params) {
        assertEquals("-v", verbose, compact.verbose);
        assertEquals("-r", recursive, compact.recursive);
        assertEquals("-o", out == null ? null : new File(out), compact.outputFile);
        if (params == null) {
            assertNull("args", compact.inputFiles);
        } else {
            assertArrayEquals("args", params, compact.inputFiles);
        }
    }

    @Test
    public void testNonSpacedOptions() {
        CompactFields compact = CommandLine.parse(new CompactFields(), "-rvo arg path path".split(" "));
        assertTrue("-r", compact.recursive);
        assertTrue("-v", compact.verbose);
        assertEquals("-o", new File("arg"), compact.outputFile);
        assertArrayEquals("args", new File[]{new File("path"), new File("path")}, compact.inputFiles);
    }

    private static class PrimitiveIntParameters {
        @Parameters int[] intParams;
    }
    @Test
    public void testPrimitiveParameters() {
        PrimitiveIntParameters params = CommandLine.parse(new PrimitiveIntParameters(), "1 2 3 4".split(" "));
        assertArrayEquals(new int[] {1, 2, 3, 4}, params.intParams);
    }

    private static class VarargOptions0ArityAndParameters {
        static final double[] DEFAULT_PARAMS = new double[] {1, 2};
        @Parameters double[] doubleParams = DEFAULT_PARAMS;
        @Option(names = "-doubles", arity = 0, varargs = true) double[] doubleOptions;
    }
    @Test
    public void testVarargsArrayOptionsWithArity0ConsumeAllArguments() {
        VarargOptions0ArityAndParameters
                params = CommandLine.parse(new VarargOptions0ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(VarargOptions0ArityAndParameters.DEFAULT_PARAMS, params.doubleParams, 0.000001);
    }

    private static class VarargOptions1ArityAndParameters {
        @Parameters double[] doubleParams;
        @Option(names = "-doubles", arity = 1, varargs = true) double[] doubleOptions;
    }
    @Test
    public void testVarargsArrayOptionsWithArity1ConsumeAllArguments() {
        VarargOptions1ArityAndParameters
                params = CommandLine.parse(new VarargOptions1ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(null, params.doubleParams, 0.000001);
    }

    private static class VarargOptions2ArityAndParameters {
        @Parameters double[] doubleParams;
        @Option(names = "-doubles", arity = 2, varargs = true) double[] doubleOptions;
    }
    @Test
    public void testVarargsArrayOptionsWithArity2ConsumeAllArguments() {
        VarargOptions2ArityAndParameters
                params = CommandLine.parse(new VarargOptions2ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, 3.3, 4.4}, params.doubleOptions, 0.000001);
        assertArrayEquals(null, params.doubleParams, 0.000001);
    }

    private static class VarargOptionsNoArityAndParameters {
        @Parameters char[] charParams;
        @Option(names = "-chars", varargs = true) char[] charOptions;
    }
    @Test
    public void testVarargsArrayOptionsWithoutArityConsumeAllArguments() {
        VarargOptionsNoArityAndParameters
                params = CommandLine.parse(new VarargOptionsNoArityAndParameters(), "-chars a b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
                new char[] {'a', 'b', 'c', 'd'}, params.charOptions);
        assertArrayEquals(null, params.charParams);
    }

    private static class VarargsBooleanOptions0ArityAndParameters {
        @Parameters String[] params;
        @Option(names = "-bool", arity = 0, varargs = true) boolean bool;
        @Option(names = {"-v", "-other"}, arity=0, varargs = true) boolean vOrOther;
        @Option(names = "-r") boolean rBoolean;
    }
    @Test
    public void testVarargsBooleanOptionsArity0Consume1ArgumentIfPossible() { // ignores varargs
        VarargsBooleanOptions0ArityAndParameters
                params = CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-bool false false true".split(" "));
        assertFalse(params.bool);
        assertArrayEquals(new String[]{ "false", "true"}, params.params);
    }
    @Test
    public void testVarargsBooleanOptionsArity0RequiresNoArgument() { // ignores varargs
        VarargsBooleanOptions0ArityAndParameters
                params = CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-bool".split(" "));
        assertTrue(params.bool);
    }
    @Test
    public void testVarargsBooleanOptionsArity0Consume0ArgumentsIfNextArgIsOption() { // ignores varargs
        VarargsBooleanOptions0ArityAndParameters
                params = CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-bool -other".split(" "));
        assertTrue(params.bool);
        assertTrue(params.vOrOther);
    }
    @Test
    public void testVarargsBooleanOptionsArity0Consume0ArgumentsIfNextArgIsParameter() { // ignores varargs
        VarargsBooleanOptions0ArityAndParameters
                params = CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-bool 123 -other".split(" "));
        assertTrue(params.bool);
        assertFalse(params.vOrOther);
        assertArrayEquals(new String[]{ "123", "-other"}, params.params);
    }
    @Test
    public void testVarargsBooleanOptionsArity0FailsIfAttachedParamNotABoolean() { // ignores varargs
        try {
            CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-bool=123 -other".split(" "));
            fail("was able to assign 123 to boolean");
        } catch (ParameterException ex) {
            assertEquals("'123' is not a boolean.", ex.getMessage());
        }
    }
    @Test
    public void testVarargsBooleanOptionsArity0ShortFormFailsIfAttachedParamNotABoolean() { // ignores varargs
        VarargsBooleanOptions0ArityAndParameters params =
            CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-rv234 -bool".split(" "));
        assertTrue(params.vOrOther);
        assertTrue(params.rBoolean);
        assertArrayEquals(new String[]{ "234", "-bool"}, params.params);
        assertFalse(params.bool);
    }
    @Test
    public void testVarargsBooleanOptionsArity0ShortFormFailsIfAttachedWithSepParamNotABoolean() { // ignores varargs
        try {
            CommandLine.parse(new VarargsBooleanOptions0ArityAndParameters(), "-rv=234 -bool".split(" "));
            fail("was able to assign 234 to boolean");
        } catch (ParameterException ex) {
            assertEquals("'234' is not a boolean.", ex.getMessage());
        }
    }

    private static class VarargsBooleanOptions1ArityAndParameters {
        @Parameters boolean[] boolParams;
        @Option(names = "-bool", arity = 1, varargs = true) boolean aBoolean;
    }
    @Test
    public void testVarargsBooleanOptionsArity1Consume1Argument() { // ignores varargs
        VarargsBooleanOptions1ArityAndParameters
                params = CommandLine.parse(new VarargsBooleanOptions1ArityAndParameters(), "-bool false false true".split(" "));
        assertFalse(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);

        params = CommandLine.parse(new VarargsBooleanOptions1ArityAndParameters(), "-bool true false true".split(" "));
        assertTrue(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);
    }
    @Test
    public void testVarargsBooleanOptionsArity1CaseInsensitive() { // ignores varargs
        VarargsBooleanOptions1ArityAndParameters
                params = CommandLine.parse(new VarargsBooleanOptions1ArityAndParameters(), "-bool fAlsE false true".split(" "));
        assertFalse(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);

        params = CommandLine.parse(new VarargsBooleanOptions1ArityAndParameters(), "-bool FaLsE false true".split(" "));
        assertFalse(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);

        params = CommandLine.parse(new VarargsBooleanOptions1ArityAndParameters(), "-bool tRuE false true".split(" "));
        assertTrue(params.aBoolean);
        assertArrayEquals(new boolean[]{ false, true}, params.boolParams);
    }
    @Test
    public void testBooleanOptionsWithArity1ErrorIfValueNotTrueOrFalse() { // ignores varargs
        try {
            CommandLine.parse(new VarargsBooleanOptions1ArityAndParameters(), "-bool abc".split(" "));
            fail("Invalid format abc was accepted for boolean");
        } catch (ParameterException expected) {
            assertEquals("'abc' is not a boolean.", expected.getMessage());
        }
    }

    private static class BooleanOptions0ArityAndParameters {
        @Parameters boolean[] boolParams;
        @Option(names = "-bool", arity = 0) boolean aBoolean;
    }
    @Test
    public void testBooleanOptionsWithArity0Consume0Arguments() {
        BooleanOptions0ArityAndParameters
                params = CommandLine.parse(new BooleanOptions0ArityAndParameters(), "-bool true false true".split(" "));
        assertTrue(params.aBoolean);
        assertArrayEquals(new boolean[]{true, false, true}, params.boolParams);
    }

    private static class VarargsIntOptionsArity1AndParameters {
        @Parameters int[] intParams;
        @Option(names = "-int", arity = 1, varargs = true) int anInt;
    }
    @Test
    public void testVarargsIntOptionsWithArity1Consume1Argument() { // ignores varargs
        VarargsIntOptionsArity1AndParameters
                params = CommandLine.parse(new VarargsIntOptionsArity1AndParameters(), "-int 23 42 7".split(" "));
        assertEquals(23, params.anInt);
        assertArrayEquals(new int[]{ 42, 7}, params.intParams);
    }

    private static class OptionsArray0ArityAndParameters {
        @Parameters double[] doubleParams;
        @Option(names = "-doubles", arity = 0) double[] doubleOptions;
    }
    @Test
    public void testArrayOptionsWithArity0Consume0Arguments() {
        OptionsArray0ArityAndParameters
                params = CommandLine.parse(new OptionsArray0ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[0], params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{1.1, 2.2, 3.3, 4.4}, params.doubleParams, 0.000001);
    }

    private static class Options1ArityAndParameters {
        @Parameters double[] doubleParams;
        @Option(names = "-doubles", arity = 1) double[] doubleOptions;
    }
    @Test
    public void testArrayOptionsWithArity1Consume1Argument() {
        Options1ArityAndParameters
                params = CommandLine.parse(new Options1ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1}, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{2.2, 3.3, 4.4}, params.doubleParams, 0.000001);
    }

    private static class Options2ArityAndParameters {
        @Parameters double[] doubleParams;
        @Option(names = "-doubles", arity = 2) double[] doubleOptions;
    }
    @Test
    public void testArrayOptionsWithArity2Consume2Arguments() {
        Options2ArityAndParameters
                params = CommandLine.parse(new Options2ArityAndParameters(), "-doubles 1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, }, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{3.3, 4.4}, params.doubleParams, 0.000001);
    }

    private static class OptionsNoArityAndParameters {
        @Parameters char[] charParams;
        @Option(names = "-chars") char[] charOptions;
    }
    @Test
    public void testArrayOptionsWithoutArityConsumeAllArguments() {
        OptionsNoArityAndParameters
                params = CommandLine.parse(new OptionsNoArityAndParameters(), "-chars a b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
                new char[] {'a', 'b', 'c', 'd'}, params.charOptions);
        assertArrayEquals(null, params.charParams);
    }

    private static class MissingConverter {
        @Option(names = "--socket") Socket socket;
    }
    @Test(expected = MissingTypeConverterException.class)
    public void testMissingTypeConverter() {
        CommandLine.parse(new MissingConverter(), "--socket anyString".split(" "));
    }

    static class VarArgArrayParamsNegativeArity {
        @Parameters(arity = -1)
        List<String> params;
    }
    @Test
    public void testVarargArrayParametersWithNegativeArity() {
        VarArgArrayParamsNegativeArity params = CommandLine.parse(new VarArgArrayParamsNegativeArity(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b", "c"), params.params);
    }

    @Ignore
    @Test
    public void testVarargArrayParametersWithArity0() {
        fail();
    }

    @Ignore
    @Test
    public void testVarargArrayParametersWithArity1() {
        fail();
    }

    @Ignore
    @Test
    public void testVarargArrayParametersWithArity2() {
        fail();
    }

    @Ignore
    @Test
    public void testNonVarargArrayParametersWithNegativeArity() {
        fail();
    }

    @Ignore
    @Test
    public void testNonVarargArrayParametersWithArity0() {
        fail();
    }

    @Ignore
    @Test
    public void testNonVarargArrayParametersWithArity1() {
        fail();
    }

    @Ignore
    @Test
    public void testNonVarargArrayParametersWithArity2() {
        fail();
    }
}
