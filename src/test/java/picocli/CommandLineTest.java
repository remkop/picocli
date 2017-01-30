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
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.CommandLineTest.InvalidAnnotations.*;

/**
 * CommandLine unit tests.
 */
// TODO arrays
// TODO collection fields
// TODO all built-in types
// TODO private fields, public fields (methods?)
// TODO arity 2, 3
// TODO arity -1, -2, -3
// TODO arity ignored for single-value types (non-array, non-collection)
// TODO positional arguments with '--' separator (where positional arguments look like options)
// TODO positional arguments without '--' separator (based on arity of last option?)
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

    static class SupportedTypes {
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
    public void testTypeConversion()
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
        assertEquals("StringBuilder", new StringBuilder("bcd").toString(), bean.aStringBuilderField.toString());
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

    class FinalFields {
        @Option(names = "-f") private final String field = null;
    }
    @Test
    public void testCanInitializeFinalFields() {
        FinalFields ff = CommandLine.parse(new FinalFields(), "-f", "some value");
        assertEquals("some value", ff.field);
    }

    class RequiredField {
        @Option(names = "--required", required = true) private String required;
    }
    @Test(expected = MissingParameterException.class)
    public void testErrorIfRequiredOptionNotSpecified() {
        CommandLine.parse(new RequiredField(), "arg1", "arg2");
    }

    class CompactFields {
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

    static class PrimitiveParameters {
        @Parameters int[] intParams;
    }
    @Test
    public void testPrimitiveParameters() {
        PrimitiveParameters params = CommandLine.parse(new PrimitiveParameters(), "1 2 3 4".split(" "));
        assertArrayEquals(new int[] {1, 2, 3, 4}, params.intParams);
    }

    static class MissingConverter {
        @Option(names = "--socket") Socket socket;
    }
    @Test(expected = MissingTypeConverterException.class)
    public void testMissingTypeConverter() {
        CommandLine.parse(new MissingConverter(), "--socket anyString".split(" "));
    }
}
