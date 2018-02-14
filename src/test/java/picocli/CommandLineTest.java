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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;
import static picocli.CommandLine.*;

/**
 * Tests for the CommandLine argument parsing interpreter functionality.
 */
// DONE arrays
// DONE collection fields
// DONE all built-in types
// DONE private fields, public fields (TODO methods?)
// DONE arity 2, 3
// DONE arity -1, -2, -3
// TODO arity ignored for single-value types (non-array, non-collection)
// DONE positional arguments with '--' separator (where positional arguments look like options)
// DONE positional arguments without '--' separator (based on arity of last option?)
// DONE positional arguments based on arity of last option
// TODO ambiguous options: writing --input ARG (as opposed to --input=ARG) is ambiguous,
// meaning it is not possible to tell whether ARG is option's argument or a positional argument.
// In usage patterns this will be interpreted as an option with argument only if a description (covered below)
// for that option is provided.
// Otherwise it will be interpreted as an option and separate positional argument.
// TODO ambiguous short options: ambiguity with the -f FILE and -fFILE notation.
// In the latter case it is not possible to tell whether it is a number of stacked short options,
// or an option with an argument. These notations will be interpreted as an option with argument only if a
// description for the option is provided.
// DONE compact flags
// DONE compact flags where last option has an argument, separate by space
// DONE compact flags where last option has an argument attached to the option character
// DONE long options with argument separate by space
// DONE long options with argument separated by '=' (no spaces)
// TODO document that if arity>1 and args="-opt=val1 val2", arity overrules the "=": both values are assigned
// TODO test superclass bean and child class bean where child class field shadows super class and have same annotation Option name
// TODO test superclass bean and child class bean where child class field shadows super class and have different annotation Option name
// DONE -vrx, -vro outputFile, -vrooutputFile, -vro=outputFile, -vro:outputFile, -vro=, -vro:, -vro
// DONE --out outputFile, --out=outputFile, --out:outputFile, --out=, --out:, --out
public class CommandLineTest {
    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }

    private static void setTraceLevel(String level) {
        System.setProperty("picocli.trace", level);
    }
    @Test
    public void testVersion() {
        assertEquals("2.0.4-SNAPSHOT", CommandLine.VERSION);
    }

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
        SupportedTypes bean = CommandLine.populateCommand(new SupportedTypes());
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
        SupportedTypes bean = CommandLine.populateCommand(new SupportedTypes(),
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
    public void testByteFieldsAreDecimal() {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-byte", "0x1F", "-Byte", "0x0F");
            fail("Should fail on hex input");
        } catch (ParameterException expected) {
            assertEquals("Could not convert '0x1F' to byte for option '-byte'" +
                    ": java.lang.NumberFormatException: For input string: \"0x1F\"", expected.getMessage());
        }
    }
    @Test
    public void testCustomByteConverterAcceptsHexadecimalDecimalAndOctal() {
        SupportedTypes bean = new SupportedTypes();
        CommandLine commandLine = new CommandLine(bean);
        ITypeConverter<Byte> converter = new ITypeConverter<Byte>() {
            public Byte convert(String s) {
                return Byte.decode(s);
            }
        };
        commandLine.registerConverter(Byte.class, converter);
        commandLine.registerConverter(Byte.TYPE, converter);
        commandLine.parse("-byte", "0x1F", "-Byte", "0x0F");
        assertEquals(0x1F, bean.byteField);
        assertEquals(Byte.valueOf((byte) 0x0F), bean.aByteField);

        commandLine.parse("-byte", "010", "-Byte", "010");
        assertEquals(8, bean.byteField);
        assertEquals(Byte.valueOf((byte) 8), bean.aByteField);

        commandLine.parse("-byte", "34", "-Byte", "34");
        assertEquals(34, bean.byteField);
        assertEquals(Byte.valueOf((byte) 34), bean.aByteField);
    }
    @Test
    public void testShortFieldsAreDecimal() {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-short", "0xFF", "-Short", "0x6FFE");
            fail("Should fail on hex input");
        } catch (ParameterException expected) {
            assertEquals("Could not convert '0xFF' to short for option '-short'" +
                    ": java.lang.NumberFormatException: For input string: \"0xFF\"", expected.getMessage());
        }
    }
    @Test
    public void testCustomShortConverterAcceptsHexadecimalDecimalAndOctal() {
        SupportedTypes bean = new SupportedTypes();
        CommandLine commandLine = new CommandLine(bean);
        ITypeConverter<Short> shortConverter = new ITypeConverter<Short>() {
            public Short convert(String s) {
                return Short.decode(s);
            }
        };
        commandLine.registerConverter(Short.class, shortConverter);
        commandLine.registerConverter(Short.TYPE, shortConverter);
        commandLine.parse("-short", "0xFF", "-Short", "0x6FFE");
        assertEquals(0xFF, bean.shortField);
        assertEquals(Short.valueOf((short) 0x6FFE), bean.aShortField);

        commandLine.parse("-short", "010", "-Short", "010");
        assertEquals(8, bean.shortField);
        assertEquals(Short.valueOf((short) 8), bean.aShortField);

        commandLine.parse("-short", "34", "-Short", "34");
        assertEquals(34, bean.shortField);
        assertEquals(Short.valueOf((short) 34), bean.aShortField);
    }
    @Test
    public void testIntFieldsAreDecimal() {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-int", "0xFF", "-Integer", "0xFFFF");
            fail("Should fail on hex input");
        } catch (ParameterException expected) {
            assertEquals("Could not convert '0xFF' to int for option '-int'" +
                    ": java.lang.NumberFormatException: For input string: \"0xFF\"", expected.getMessage());
        }
    }
    @Test
    public void testCustomIntConverterAcceptsHexadecimalDecimalAndOctal() {
        SupportedTypes bean = new SupportedTypes();
        CommandLine commandLine = new CommandLine(bean);
        ITypeConverter<Integer> intConverter = new ITypeConverter<Integer>() {
            public Integer convert(String s) {
                return Integer.decode(s);
            }
        };
        commandLine.registerConverter(Integer.class, intConverter);
        commandLine.registerConverter(Integer.TYPE, intConverter);
        commandLine.parse("-int", "0xFF", "-Integer", "0xFFFF");
        assertEquals(255, bean.intField);
        assertEquals(Integer.valueOf(0xFFFF), bean.anIntegerField);

        commandLine.parse("-int", "010", "-Integer", "010");
        assertEquals(8, bean.intField);
        assertEquals(Integer.valueOf(8), bean.anIntegerField);

        commandLine.parse("-int", "34", "-Integer", "34");
        assertEquals(34, bean.intField);
        assertEquals(Integer.valueOf(34), bean.anIntegerField);
    }
    @Test
    public void testLongFieldsAreDecimal() {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-long", "0xAABBCC", "-Long", "0xAABBCCDD");
            fail("Should fail on hex input");
        } catch (ParameterException expected) {
            assertEquals("Could not convert '0xAABBCC' to long for option '-long'" +
                    ": java.lang.NumberFormatException: For input string: \"0xAABBCC\"", expected.getMessage());
        }
    }
    @Test
    public void testCustomLongConverterAcceptsHexadecimalDecimalAndOctal() {
        SupportedTypes bean = new SupportedTypes();
        CommandLine commandLine = new CommandLine(bean);
        ITypeConverter<Long> longConverter = new ITypeConverter<Long>() {
            public Long convert(String s) {
                return Long.decode(s);
            }
        };
        commandLine.registerConverter(Long.class, longConverter);
        commandLine.registerConverter(Long.TYPE, longConverter);
        commandLine.parse("-long", "0xAABBCC", "-Long", "0xAABBCCDD");
        assertEquals(0xAABBCC, bean.longField);
        assertEquals(Long.valueOf(0xAABBCCDDL), bean.aLongField);

        commandLine.parse("-long", "010", "-Long", "010");
        assertEquals(8, bean.longField);
        assertEquals(Long.valueOf(8), bean.aLongField);

        commandLine.parse("-long", "34", "-Long", "34");
        assertEquals(34, bean.longField);
        assertEquals(Long.valueOf(34), bean.aLongField);
    }
    @Test
    public void testTimeFormatHHmmSupported() throws ParseException {
        SupportedTypes bean = CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm").parse("23:59").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssSupported() throws ParseException {
        SupportedTypes bean = CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:58");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss").parse("23:59:58").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssDotSSSSupported() throws ParseException {
        SupportedTypes bean = CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:58.123");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss.SSS").parse("23:59:58.123").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssCommaSSSSupported() throws ParseException {
        SupportedTypes bean = CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:58,123");
        assertEquals("Time", new Time(new SimpleDateFormat("HH:mm:ss,SSS").parse("23:59:58,123").getTime()), bean.aTimeField);
    }
    @Test
    public void testTimeFormatHHmmssSSSInvalidError() throws ParseException {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:58;123");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:58;123' is not a HH:mm[:ss[.SSS]] time for option '-Time'", expected.getMessage());
        }
    }
    @Test
    public void testTimeFormatHHmmssDotInvalidError() throws ParseException {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:58.");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:58.' is not a HH:mm[:ss[.SSS]] time for option '-Time'", expected.getMessage());
        }
    }
    @Test
    public void testTimeFormatHHmmsssInvalidError() throws ParseException {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:587");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:587' is not a HH:mm[:ss[.SSS]] time for option '-Time'", expected.getMessage());
        }
    }
    @Test
    public void testTimeFormatHHmmssColonInvalidError() throws ParseException {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-Time", "23:59:");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'23:59:' is not a HH:mm[:ss[.SSS]] time for option '-Time'", expected.getMessage());
        }
    }
    @Test
    public void testDateFormatYYYYmmddInvalidError() throws ParseException {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-Date", "20170131");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'20170131' is not a yyyy-MM-dd date for option '-Date'", expected.getMessage());
        }
    }
    @Test
    public void testCharConverterInvalidError() throws ParseException {
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-Character", "aa");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'aa' is not a single character for option '-Character'", expected.getMessage());
        }
        try {
            CommandLine.populateCommand(new SupportedTypes(), "-char", "aa");
            fail("Invalid format was accepted");
        } catch (ParameterException expected) {
            assertEquals("'aa' is not a single character for option '-char'", expected.getMessage());
        }
    }
    @Test
    public void testNumberConvertersInvalidError() {
        parseInvalidValue("-Byte", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-byte", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-Short", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-short", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-Integer", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-int", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-Long", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-long", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-Float", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-float", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-Double", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-double", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
        parseInvalidValue("-BigDecimal", "aa", ": java.lang.NumberFormatException");
        parseInvalidValue("-BigInteger", "aa", ": java.lang.NumberFormatException: For input string: \"aa\"");
    }
    @Test
    public void testURLConvertersInvalidError() {
        parseInvalidValue("-URL", ":::", ": java.net.MalformedURLException: no protocol: :::");
    }
    @Test
    public void testURIConvertersInvalidError() {
        parseInvalidValue("-URI", ":::", ": java.net.URISyntaxException: Expected scheme name at index 0: :::");
    }
    @Test
    public void testCharsetConvertersInvalidError() {
        parseInvalidValue("-Charset", "aa", ": java.nio.charset.UnsupportedCharsetException: aa");
    }
    @Test
    public void testInetAddressConvertersInvalidError() {
        parseInvalidValue("-InetAddress", "%$::a?*!a", ": java.net.UnknownHostException: %$::a?*!a");
    }
    @Test
    public void testUUIDConvertersInvalidError() {
        parseInvalidValue("-UUID", "aa", ": java.lang.IllegalArgumentException: Invalid UUID string: aa");
    }
    @Test
    public void testRegexPatternConverterInvalidError() {
        parseInvalidValue("-Pattern", "[[(aa", String.format(": java.util.regex.PatternSyntaxException: Unclosed character class near index 4%n" +
                "[[(aa%n" +
                "    ^"));
    }

    private void parseInvalidValue(String option, String value, String errorMessage) {
        try {
            CommandLine.populateCommand(new SupportedTypes(), option, value);
            fail("Invalid format " + value + " was accepted for " + option);
        } catch (ParameterException actual) {
            String type = option.substring(1);
            String expected = "Could not convert '" + value + "' to " + type + " for option '" + option + "'" + errorMessage;
            assertTrue("expected:<" + expected + "> but was:<" + actual.getMessage() + ">",
                    actual.getMessage().startsWith(actual.getMessage()));
        }
    }

    @Test
    public void testCustomConverter() {
        class Glob {
            public final String glob;
            public Glob(String glob) { this.glob = glob; }
        }
        class App {
            @Parameters Glob globField;
        }
        class GlobConverter implements ITypeConverter<Glob> {
            public Glob convert(String value) throws Exception { return new Glob(value); }
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.registerConverter(Glob.class, new GlobConverter());

        String[] args = {"a*glob*pattern"};
        List<CommandLine> parsed = commandLine.parse(args);
        assertEquals("not empty", 1, parsed.size());
        assertTrue(parsed.get(0).getCommand() instanceof App);
        App app = (App) parsed.get(0).getCommand();
        assertEquals(args[0], app.globField.glob);
    }

    private static class EnumParams {
        @Option(names = "-timeUnit") TimeUnit timeUnit;
        @Option(names = "-timeUnitArray", arity = "2") TimeUnit[] timeUnitArray;
        @Option(names = "-timeUnitList", type = TimeUnit.class, arity = "3") List<TimeUnit> timeUnitList;
    }
    @Test
    public void testEnumTypeConversionSuceedsForValidInput() {
        EnumParams params = CommandLine.populateCommand(new EnumParams(),
                "-timeUnit SECONDS -timeUnitArray MILLISECONDS SECONDS -timeUnitList SECONDS MICROSECONDS NANOSECONDS".split(" "));
        assertEquals(SECONDS, params.timeUnit);
        assertArrayEquals(new TimeUnit[]{MILLISECONDS, TimeUnit.SECONDS}, params.timeUnitArray);
        List<TimeUnit> expected = new ArrayList<TimeUnit>(Arrays.asList(TimeUnit.SECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS));
        assertEquals(expected, params.timeUnitList);
    }
    @Test
    public void testEnumTypeConversionFailsForInvalidInput() {
        try {
            CommandLine.populateCommand(new EnumParams(), "-timeUnit", "xyz");
            fail("Accepted invalid timeunit");
        } catch (Exception ex) {
            String prefix = "Could not convert 'xyz' to TimeUnit for option '-timeUnit'" +
                    ": java.lang.IllegalArgumentException: No enum cons";
            String suffix = " java.util.concurrent.TimeUnit.xyz";
            assertEquals(prefix, ex.getMessage().substring(0, prefix.length()));
            assertEquals(suffix, ex.getMessage().substring(ex.getMessage().length() - suffix.length(), ex.getMessage().length()));
        }
    }
    @Ignore("Requires #14 case-insensitive enum parsing")
    @Test
    public void testEnumTypeConversionIsCaseInsensitive() {
        EnumParams params = CommandLine.populateCommand(new EnumParams(),
                "-timeUnit sEcONds -timeUnitArray milliSeconds miCroSeConds -timeUnitList SEConds MiCROsEconds nanoSEConds".split(" "));
        assertEquals(SECONDS, params.timeUnit);
        assertArrayEquals(new TimeUnit[]{MILLISECONDS, TimeUnit.MICROSECONDS}, params.timeUnitArray);
        List<TimeUnit> expected = new ArrayList<TimeUnit>(Arrays.asList(TimeUnit.SECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS));
        assertEquals(expected, params.timeUnitList);
    }
    @Test
    public void testEnumArrayTypeConversionFailsForInvalidInput() {
        try {
            CommandLine.populateCommand(new EnumParams(), "-timeUnitArray", "a", "b");
            fail("Accepted invalid timeunit");
        } catch (Exception ex) {
            String prefix = "Could not convert 'a' to TimeUnit for option '-timeUnitArray' at index 0 (<timeUnitArray>)" +
                    ": java.lang.IllegalArgumentException: No enum const";
            String suffix = " java.util.concurrent.TimeUnit.a";
            assertEquals(prefix, ex.getMessage().substring(0, prefix.length()));
            assertEquals(suffix, ex.getMessage().substring(ex.getMessage().length() - suffix.length(), ex.getMessage().length()));
        }
    }
    @Test
    public void testEnumListTypeConversionFailsForInvalidInput() {
        try {
            CommandLine.populateCommand(new EnumParams(), "-timeUnitList", "SECONDS", "b", "c");
            fail("Accepted invalid timeunit");
        } catch (Exception ex) {
            String prefix = "Could not convert 'b' to TimeUnit for option '-timeUnitList' at index 1 (<timeUnitList>)" +
                    ": java.lang.IllegalArgumentException: No enum const";
            String suffix = " java.util.concurrent.TimeUnit.b";
            assertEquals(prefix, ex.getMessage().substring(0, prefix.length()));
            assertEquals(suffix, ex.getMessage().substring(ex.getMessage().length() - suffix.length(), ex.getMessage().length()));
        }
    }

    @Test
    public void testArrayOptionParametersAreAlwaysInstantiated() {
        EnumParams params = new EnumParams();
        TimeUnit[] array = params.timeUnitArray;
        new CommandLine(params).parse("-timeUnitArray", "SECONDS", "MILLISECONDS");
        assertNotSame(array, params.timeUnitArray);
    }
    @Test
    public void testListOptionParametersAreInstantiatedIfNull() {
        EnumParams params = new EnumParams();
        assertNull(params.timeUnitList);
        new CommandLine(params).parse("-timeUnitList", "SECONDS", "MICROSECONDS", "MILLISECONDS");
        assertEquals(Arrays.asList(SECONDS, MICROSECONDS, MILLISECONDS), params.timeUnitList);
    }
    @Test
    public void testListOptionParametersAreReusedInstantiatedIfNonNull() {
        EnumParams params = new EnumParams();
        List<TimeUnit> list = new ArrayList<TimeUnit>();
        params.timeUnitList = list;
        new CommandLine(params).parse("-timeUnitList", "SECONDS", "MICROSECONDS", "SECONDS");
        assertEquals(Arrays.asList(SECONDS, MICROSECONDS, SECONDS), params.timeUnitList);
        assertSame(list, params.timeUnitList);
    }
    @Test
    public void testArrayPositionalParametersAreAppendedNotReplaced() {
        class ArrayPositionalParams {
            @Parameters() int[] array;
        }
        ArrayPositionalParams params = new ArrayPositionalParams();
        params.array = new int[3];
        int[] array = params.array;
        new CommandLine(params).parse("3", "2", "1");
        assertNotSame(array, params.array);
        assertArrayEquals(new int[]{0, 0, 0, 3, 2, 1}, params.array);
    }
    private class ListPositionalParams {
        @Parameters(type = Integer.class) List<Integer> list;
    }
    @Test
    public void testListPositionalParametersAreInstantiatedIfNull() {
        ListPositionalParams params = new ListPositionalParams();
        assertNull(params.list);
        new CommandLine(params).parse("3", "2", "1");
        assertNotNull(params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }
    @Test
    public void testListPositionalParametersAreReusedIfNonNull() {
        ListPositionalParams params = new ListPositionalParams();
        params.list = new ArrayList<Integer>();
        List<Integer> list = params.list;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }
    @Test
    public void testListPositionalParametersAreAppendedToIfNonNull() {
        ListPositionalParams params = new ListPositionalParams();
        params.list = new ArrayList<Integer>();
        params.list.add(234);
        List<Integer> list = params.list;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.list);
        assertEquals(Arrays.asList(234, 3, 2, 1), params.list);
    }
    class SortedSetPositionalParams {
        @Parameters(type = Integer.class) SortedSet<Integer> sortedSet;
    }
    @Test
    public void testSortedSetPositionalParametersAreInstantiatedIfNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        assertNull(params.sortedSet);
        new CommandLine(params).parse("3", "2", "1");
        assertNotNull(params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<Integer>(params.sortedSet));
    }
    @Test
    public void testSortedSetPositionalParametersAreReusedIfNonNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        params.sortedSet = new TreeSet<Integer>();
        SortedSet<Integer> list = params.sortedSet;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<Integer>(params.sortedSet));
    }
    @Test
    public void testSortedSetPositionalParametersAreAppendedToIfNonNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        params.sortedSet = new TreeSet<Integer>();
        params.sortedSet.add(234);
        SortedSet<Integer> list = params.sortedSet;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3, 234), new ArrayList<Integer>(params.sortedSet));
    }
    class SetPositionalParams {
        @Parameters(type = Integer.class) Set<Integer> set;
    }
    @Test
    public void testSetPositionalParametersAreInstantiatedIfNull() {
        SetPositionalParams params = new SetPositionalParams();
        assertNull(params.set);
        new CommandLine(params).parse("3", "2", "1");
        assertNotNull(params.set);
        assertEquals(new HashSet(Arrays.asList(1, 2, 3)), params.set);
    }
    @Test
    public void testSetPositionalParametersAreReusedIfNonNull() {
        SetPositionalParams params = new SetPositionalParams();
        params.set = new TreeSet<Integer>();
        Set<Integer> list = params.set;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.set);
        assertEquals(new HashSet(Arrays.asList(1, 2, 3)), params.set);
    }
    @Test
    public void testSetPositionalParametersAreAppendedToIfNonNull() {
        SetPositionalParams params = new SetPositionalParams();
        params.set = new TreeSet<Integer>();
        params.set.add(234);
        Set<Integer> list = params.set;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.set);
        assertEquals(new HashSet(Arrays.asList(1, 2, 3, 234)), params.set);
    }
    class QueuePositionalParams {
        @Parameters(type = Integer.class) Queue<Integer> queue;
    }
    @Test
    public void testQueuePositionalParametersAreInstantiatedIfNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        assertNull(params.queue);
        new CommandLine(params).parse("3", "2", "1");
        assertNotNull(params.queue);
        assertEquals(new LinkedList(Arrays.asList(3, 2, 1)), params.queue);
    }
    @Test
    public void testQueuePositionalParametersAreReusedIfNonNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        params.queue = new LinkedList<Integer>();
        Queue<Integer> list = params.queue;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.queue);
        assertEquals(new LinkedList(Arrays.asList(3, 2, 1)), params.queue);
    }
    @Test
    public void testQueuePositionalParametersAreAppendedToIfNonNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        params.queue = new LinkedList<Integer>();
        params.queue.add(234);
        Queue<Integer> list = params.queue;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.queue);
        assertEquals(new LinkedList(Arrays.asList(234, 3, 2, 1)), params.queue);
    }
    class CollectionPositionalParams {
        @Parameters(type = Integer.class) Collection<Integer> collection;
    }
    @Test
    public void testCollectionPositionalParametersAreInstantiatedIfNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        assertNull(params.collection);
        new CommandLine(params).parse("3", "2", "1");
        assertNotNull(params.collection);
        assertEquals(Arrays.asList(3, 2, 1), params.collection);
    }
    @Test
    public void testCollectionPositionalParametersAreReusedIfNonNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        params.collection = new ArrayList<Integer>();
        Collection<Integer> list = params.collection;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.collection);
        assertEquals(Arrays.asList(3, 2, 1), params.collection);
    }
    @Test
    public void testCollectionPositionalParametersAreAppendedToIfNonNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        params.collection = new ArrayList<Integer>();
        params.collection.add(234);
        Collection<Integer> list = params.collection;
        new CommandLine(params).parse("3", "2", "1");
        assertSame(list, params.collection);
        assertEquals(Arrays.asList(234, 3, 2, 1), params.collection);
    }

    @Test(expected = DuplicateOptionAnnotationsException.class)
    public void testDuplicateOptionsAreRejected() {
        /** Duplicate parameter names are invalid. */
        class DuplicateOptions {
            @Option(names = "-duplicate") public int value1;
            @Option(names = "-duplicate") public int value2;
        }
        new CommandLine(new DuplicateOptions());
    }

    @Test(expected = DuplicateOptionAnnotationsException.class)
    public void testClashingAnnotationsAreRejected() {
        class ClashingAnnotation {
            @Option(names = "-o")
            @Parameters
            public String[] bothOptionAndParameters;
        }
        new CommandLine(new ClashingAnnotation());
    }

    private static class PrivateFinalOptionFields {
        @Option(names = "-f") private final String field = null;
        @Option(names = "-p") private final int primitive = 43;
    }
    @Test
    public void testCanInitializePrivateFinalFields() {
        PrivateFinalOptionFields ff = CommandLine.populateCommand(new PrivateFinalOptionFields(), "-f", "reference value");
        assertEquals("reference value", ff.field);
    }
    @Ignore("Needs Reject final primitive fields annotated with @Option or @Parameters #68")
    @Test
    public void testCanInitializeFinalPrimitiveFields() {
        PrivateFinalOptionFields ff = CommandLine.populateCommand(new PrivateFinalOptionFields(), "-p", "12");
        assertEquals("primitive value", 12, ff.primitive);
    }
    @Test
    public void testLastValueSelectedIfOptionSpecifiedMultipleTimes() {
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new PrivateFinalOptionFields()).setOverwrittenOptionsAllowed(true);
        cmd.parse("-f", "111", "-f", "222");
        PrivateFinalOptionFields ff = (PrivateFinalOptionFields) cmd.getCommand();
        assertEquals("222", ff.field);
    }

    private static class PrivateFinalParameterFields {
        @Parameters(index = "0") private final String field = null;
        @Parameters(index = "1", arity = "0..1") private final int primitive = 43;
    }
    @Test
    public void testCanInitializePrivateFinalParameterFields() {
        PrivateFinalParameterFields ff = CommandLine.populateCommand(new PrivateFinalParameterFields(), "ref value");
        assertEquals("ref value", ff.field);
    }
    @Ignore("Needs Reject final primitive fields annotated with @Option or @Parameters #68")
    @Test
    public void testCannotInitializePrivateFinalPrimitiveParameterFields() {
        PrivateFinalParameterFields ff = CommandLine.populateCommand(new PrivateFinalParameterFields(), "ref value", "12");
        assertEquals("ref value", ff.field);
        assertEquals("primitive value", 12, ff.primitive);
    }

    private static class RequiredField {
        @Option(names = {"-?", "/?"},        help = true)       boolean isHelpRequested;
        @Option(names = {"-V", "--version"}, versionHelp= true) boolean versionHelp;
        @Option(names = {"-h", "--help"},    usageHelp = true)  boolean usageHelp;
        @Option(names = "--required", required = true) private String required;
        @Parameters private String[] remainder;
    }
    @Test
    public void testErrorIfRequiredOptionNotSpecified() {
        try {
            CommandLine.populateCommand(new RequiredField(), "arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '--required=<required>'", ex.getMessage());
        }
    }
    @Test
    public void testNoErrorIfRequiredOptionSpecified() {
        CommandLine.populateCommand(new RequiredField(), "--required", "arg1", "arg2");
    }
    @Test
    public void testNoErrorIfRequiredOptionNotSpecifiedWhenHelpRequested() {
        RequiredField requiredField = CommandLine.populateCommand(new RequiredField(), "-?");
        assertTrue("help requested", requiredField.isHelpRequested);
    }
    @Test
    public void testNoErrorIfRequiredOptionNotSpecifiedWhenUsageHelpRequested() {
        RequiredField requiredField = CommandLine.populateCommand(new RequiredField(), "--help");
        assertTrue("usage help requested", requiredField.usageHelp);
    }
    @Test
    public void testNoErrorIfRequiredOptionNotSpecifiedWhenVersionHelpRequested() {
        RequiredField requiredField = CommandLine.populateCommand(new RequiredField(), "--version");
        assertTrue("version info requested", requiredField.versionHelp);
    }
    @Test
    public void testCommandLine_isUsageHelpRequested_trueWhenSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--help");
        assertTrue("usage help requested", parsedCommands.get(0).isUsageHelpRequested());
    }
    @Test
    public void testCommandLine_isVersionHelpRequested_trueWhenSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--version");
        assertTrue("version info requested", parsedCommands.get(0).isVersionHelpRequested());
    }
    @Test
    public void testCommandLine_isUsageHelpRequested_falseWhenNotSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--version");
        assertFalse("usage help requested", parsedCommands.get(0).isUsageHelpRequested());
    }
    @Test
    public void testCommandLine_isVersionHelpRequested_falseWhenNotSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--help");
        assertFalse("version info requested", parsedCommands.get(0).isVersionHelpRequested());
    }
    @Test
    public void testHelpRequestedFlagResetWhenParsing_staticMethod() {
        RequiredField requiredField = CommandLine.populateCommand(new RequiredField(), "-?");
        assertTrue("help requested", requiredField.isHelpRequested);

        requiredField.isHelpRequested = false;

        // should throw error again on second pass (no help was requested here...)
        try {
            CommandLine.populateCommand(requiredField, "arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '--required=<required>'", ex.getMessage());
        }
    }
    @Test
    public void testHelpRequestedFlagResetWhenParsing_instanceMethod() {
        RequiredField requiredField = new RequiredField();
        CommandLine commandLine = new CommandLine(requiredField);
        commandLine.parse("-?");
        assertTrue("help requested", requiredField.isHelpRequested);

        requiredField.isHelpRequested = false;

        // should throw error again on second pass (no help was requested here...)
        try {
            commandLine.parse("arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '--required=<required>'", ex.getMessage());
        }
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
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvoout");
        verifyCompact(compact, true, true, "out", null);

        // change order within compact group
        compact = CommandLine.populateCommand(new CompactFields(), "-vroout");
        verifyCompact(compact, true, true, "out", null);

        // compact group with separator
        compact = CommandLine.populateCommand(new CompactFields(), "-vro=out");
        verifyCompact(compact, true, true, "out", null);

        compact = CommandLine.populateCommand(new CompactFields(), "-rv p1 p2".split(" "));
        verifyCompact(compact, true, true, null, fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-voout p1 p2".split(" "));
        verifyCompact(compact, true, false, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-voout -r p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-r -v -oout p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-rv -o out p1 p2".split(" ")); //#233
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-oout -r -v p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-rvo out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        try {
            CommandLine.populateCommand(new CompactFields(), "-oout -r -vp1 p2".split(" "));
            fail("should fail: -v does not take an argument");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [-p1]", ex.getMessage());
        }
    }

    @Test
    public void testCompactFieldsWithUnmatchedArguments() {
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new CompactFields()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("-oout -r -vp1 p2".split(" "));
        assertEquals(Arrays.asList("-p1"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testCompactWithOptionParamSeparatePlusParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-r -v -o out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }

    @Test
    public void testCompactWithOptionParamAttachedEqualsSeparatorChar() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvo=out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }

    @Test
    public void testCompactWithOptionParamAttachedColonSeparatorChar() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setSeparator(":");
        cmd.parse("-rvo:out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }

    /** See {@link #testGnuLongOptionsWithVariousSeparators()}  */
    @Test
    public void testDefaultSeparatorIsEquals() {
        assertEquals("=", new CommandLine(new CompactFields()).getSeparator());
    }

    @Test
    public void testOptionsMixedWithParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-r -v p1 -o out p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueAssignsEmptyStringEvenIfNotLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-ro= -v".split(" "));
        verifyCompact(compact, false, true, "-v", null);
    }
    @Test
    public void testShortOptionsWithColonSeparatorButNoValueAssignsEmptyStringEvenIfNotLast() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setSeparator(":");
        cmd.parse("-ro: -v".split(" "));
        verifyCompact(compact, false, true, "-v", null);
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueFailsIfValueRequired() {
        try {
            CommandLine.populateCommand(new CompactFields(), "-rvo=".split(" "));
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required parameter for option '-o' (<outputFile>)", ex.getMessage());
        }
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueAssignsQuotedEmptyStringEvenIfNotLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-ro=\"\" -v".split(" "));
        verifyCompact(compact, true, true, "", null);
    }
    @Test
    public void testShortOptionsWithColonSeparatorButNoValueAssignsQuotedEmptyStringEvenIfNotLast() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setSeparator(":");
        cmd.parse("-ro:\"\" -v".split(" "));
        verifyCompact(compact, true, true, "", null);
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueAssignsEmptyQuotedStringIfLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvo=\"\"".split(" "));
        verifyCompact(compact, true, true, "", null);
    }

    @Test
    public void testDoubleDashSeparatesPositionalParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-oout -- -r -v p1 p2".split(" "));
        verifyCompact(compact, false, false, "out", fileArray("-r", "-v", "p1", "p2"));
    }

    @Test
    public void testDebugOutputForDoubleDashSeparatesPositionalParameters() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "DEBUG");
        CommandLine.populateCommand(new CompactFields(), "-oout -- -r -v p1 p2".split(" "));
        System.setErr(originalErr);
        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
        String expected = String.format("" +
                        "[picocli INFO] Parsing 6 command line args [-oout, --, -r, -v, p1, p2]%n" +
                        "[picocli DEBUG] Initializing %1$s$CompactFields: 3 options, 1 positional parameters, 0 required, 0 subcommands.%n" +
                        "[picocli DEBUG] Processing argument '-oout'. Remainder=[--, -r, -v, p1, p2]%n" +
                        "[picocli DEBUG] '-oout' cannot be separated into <option>=<option-parameter>%n" +
                        "[picocli DEBUG] Trying to process '-oout' as clustered short options%n" +
                        "[picocli DEBUG] Found option '-o' in -oout: field java.io.File %1$s$CompactFields.outputFile, arity=1%n" +
                        "[picocli DEBUG] Trying to process 'out' as option parameter%n" +
                        "[picocli INFO] Setting File field 'CompactFields.outputFile' to 'out' (was 'null') for option -o%n" +
                        "[picocli DEBUG] Processing argument '--'. Remainder=[-r, -v, p1, p2]%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=0. Remainder=[-r, -v, p1, p2]%n" +
                        "[picocli DEBUG] Position 0 is in index range 0..*. Trying to assign args to java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [-r] to File[] field 'CompactFields.inputFiles' for args[0..*] at position 0%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 1.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=1. Remainder=[-v, p1, p2]%n" +
                        "[picocli DEBUG] Position 1 is in index range 0..*. Trying to assign args to java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [-v] to File[] field 'CompactFields.inputFiles' for args[0..*] at position 1%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 2.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=2. Remainder=[p1, p2]%n" +
                        "[picocli DEBUG] Position 2 is in index range 0..*. Trying to assign args to java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [p1] to File[] field 'CompactFields.inputFiles' for args[0..*] at position 2%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 3.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=3. Remainder=[p2]%n" +
                        "[picocli DEBUG] Position 3 is in index range 0..*. Trying to assign args to java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [p2] to File[] field 'CompactFields.inputFiles' for args[0..*] at position 3%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 4.%n",
                CommandLineTest.class.getName(), new File("/home/rpopma/picocli"));
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
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
            assertArrayEquals("args=" + Arrays.toString(compact.inputFiles), params, compact.inputFiles);
        }
    }

    @Test
    public void testNonSpacedOptions() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvo arg path path".split(" "));
        assertTrue("-r", compact.recursive);
        assertTrue("-v", compact.verbose);
        assertEquals("-o", new File("arg"), compact.outputFile);
        assertArrayEquals("args", new File[]{new File("path"), new File("path")}, compact.inputFiles);
    }

    @Test
    public void testPrimitiveParameters() {
        class PrimitiveIntParameters {
            @Parameters int[] intParams;
        }
        PrimitiveIntParameters params = CommandLine.populateCommand(new PrimitiveIntParameters(), "1 2 3 4".split(" "));
        assertArrayEquals(new int[] {1, 2, 3, 4}, params.intParams);
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testMissingTypeConverter() {
        class MissingConverter {
            @Option(names = "--socket") Socket socket;
        }
        CommandLine.populateCommand(new MissingConverter(), "--socket anyString".split(" "));
    }

    @Test
    public void testParametersDeclaredOutOfOrderWithNoArgs() {
        class WithParams {
            @Parameters(index = "1") String param1;
            @Parameters(index = "0") String param0;
        }
        try {
            CommandLine.populateCommand(new WithParams(), new String[0]);
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: <param0>, <param1>", ex.getMessage());
        }
    }

    class VariousPrefixCharacters {
        @Option(names = {"-d", "--dash"}) int dash;
        @Option(names = {"/S"}) int slashS;
        @Option(names = {"/T"}) int slashT;
        @Option(names = {"/4"}) boolean fourDigit;
        @Option(names = {"/Owner", "--owner"}) String owner;
        @Option(names = {"-SingleDash"}) boolean singleDash;
        @Option(names = {"[CPM"}) String cpm;
        @Option(names = {"(CMS"}) String cms;
    }
    @Test
    public void testOptionsMayDefineAnyPrefixChar() {
        VariousPrefixCharacters params = CommandLine.populateCommand(new VariousPrefixCharacters(),
                "-d 123 /4 /S 765 /T=98 /Owner=xyz -SingleDash [CPM CP/M (CMS=cmsVal".split(" "));
        assertEquals("-d", 123, params.dash);
        assertEquals("/S", 765, params.slashS);
        assertEquals("/T", 98, params.slashT);
        assertTrue("/4", params.fourDigit);
        assertTrue("-SingleDash", params.singleDash);
        assertEquals("/Owner", "xyz", params.owner);
        assertEquals("[CPM", "CP/M", params.cpm);
        assertEquals("(CMS", "cmsVal", params.cms);
    }
    @Test
    public void testGnuLongOptionsWithVariousSeparators() {
        VariousPrefixCharacters params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash 123".split(" "));
        assertEquals("--dash val", 123, params.dash);

        params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash=234 --owner=x".split(" "));
        assertEquals("--dash=val", 234, params.dash);
        assertEquals("--owner=x", "x", params.owner);

        params = new VariousPrefixCharacters();
        CommandLine cmd = new CommandLine(params);
        cmd.setSeparator(":");
        cmd.parse("--dash:345");
        assertEquals("--dash:val", 345, params.dash);

        params = new VariousPrefixCharacters();
        cmd = new CommandLine(params);
        cmd.setSeparator(":");
        cmd.parse("--dash:345 --owner:y".split(" "));
        assertEquals("--dash:val", 345, params.dash);
        assertEquals("--owner:y", "y", params.owner);
    }
    @Test
    public void testSeparatorCanBeSetDeclaratively() {
        @Command(separator = ":")
        class App {
            @Option(names = "--opt", required = true) String opt;
        }
        try {
            CommandLine.populateCommand(new App(), "--opt=abc");
            fail("Expected failure with unknown separator");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required option '--opt:<opt>'", ok.getMessage());
        }
    }
    @Test
    public void testIfSeparatorSetTheDefaultSeparatorIsNotRecognized() {
        @Command(separator = ":")
        class App {
            @Option(names = "--opt", required = true) String opt;
        }
        try {
            CommandLine.populateCommand(new App(), "--opt=abc");
            fail("Expected failure with unknown separator");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required option '--opt:<opt>'", ok.getMessage());
        }
    }
    @Test
    public void testIfSeparatorSetTheDefaultSeparatorIsNotRecognizedWithUnmatchedArgsAllowed() {
        @Command(separator = ":")
        class App {
            @Option(names = "--opt", required = true) String opt;
        }
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        try {
            cmd.parse("--opt=abc");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required option '--opt:<opt>'", ok.getMessage());
            assertEquals(Arrays.asList("--opt=abc"), cmd.getUnmatchedArguments());
        }
    }
    @Test
    public void testGnuLongOptionsWithVariousSeparatorsOnlyAndNoValue() {
        VariousPrefixCharacters params;
        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash".split(" "));
            fail("int option needs arg");
        } catch (ParameterException ex) {
            assertEquals("Missing required parameter for option '-d' (<dash>)", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--owner".split(" "));
        } catch (ParameterException ex) {
            assertEquals("Missing required parameter for option '/Owner' (<owner>)", ex.getMessage());
        }

        params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--owner=".split(" "));
        assertEquals("--owner= (at end)", "", params.owner);

        params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--owner= /4".split(" "));
        assertEquals("--owner= (in middle)", "", params.owner);
        assertEquals("/4", true, params.fourDigit);

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash=".split(" "));
            fail("int option (with sep but no value) needs arg");
        } catch (ParameterException ex) {
            assertEquals("Could not convert '' to int for option '-d'" +
                    ": java.lang.NumberFormatException: For input string: \"\"", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash= /4".split(" "));
            fail("int option (with sep but no value, followed by other option) needs arg");
        } catch (ParameterException ex) {
            assertEquals("Could not convert '' to int for option '-d'" +
                    ": java.lang.NumberFormatException: For input string: \"\"", ex.getMessage());
        }
    }

    @Test
    public void testOptionParameterSeparatorIsCustomizable() {
        VariousPrefixCharacters params = new VariousPrefixCharacters();
        CommandLine cmd = new CommandLine(params);
        cmd.setSeparator(":");
        cmd.parse("-d 123 /4 /S 765 /T:98 /Owner:xyz -SingleDash [CPM CP/M (CMS:cmsVal".split(" "));
        assertEquals("-d", 123, params.dash);
        assertEquals("/S", 765, params.slashS);
        assertEquals("/T", 98, params.slashT);
        assertTrue("/4", params.fourDigit);
        assertTrue("-SingleDash", params.singleDash);
        assertEquals("/Owner", "xyz", params.owner);
        assertEquals("[CPM", "CP/M", params.cpm);
        assertEquals("(CMS", "cmsVal", params.cms);
    }
    @Test(expected = NullPointerException.class)
    public void testOptionParameterSeparatorCannotBeSetToNull() {
        CommandLine cmd = new CommandLine(new VariousPrefixCharacters());
        cmd.setSeparator(null);
    }

    @Test
    public void testPotentiallyNestedOptionParsedCorrectly() {
        class MyOption {
            @CommandLine.Option(names = "-p") String path;
        }
        MyOption opt = CommandLine.populateCommand(new MyOption(), "-pa-p");
        assertEquals("a-p", opt.path);

        opt = CommandLine.populateCommand(new MyOption(), "-p-ap");
        assertEquals("-ap", opt.path);
    }

    @Test
    public void testArityGreaterThanOneForSingleValuedFields() {
        class Arity2 {
            @CommandLine.Option(names = "-p", arity="2") String path;
            @CommandLine.Option(names = "-o", arity="2") String[] otherPath;
        }
        Arity2 opt = CommandLine.populateCommand(new Arity2(), "-o a b".split(" "));

        try {
            opt = CommandLine.populateCommand(new Arity2(), "-p a b".split(" "));
            fail("expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [b]", ex.getMessage());
        }
    }

    @Test
    public void testOptionParameterQuotesRemovedFromValue() {
        class TextOption {
            @CommandLine.Option(names = "-t") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testLongOptionAttachedQuotedParameterQuotesRemovedFromValue() {
        class TextOption {
            @CommandLine.Option(names = "--text") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "--text=\"a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testShortOptionAttachedQuotedParameterQuotesRemovedFromValue() {
        class TextOption {
            @CommandLine.Option(names = "-t") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t\"a text\"");
        assertEquals("a text", opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testShortOptionQuotedParameterTypeConversion() {
        class TextOption {
            @CommandLine.Option(names = "-t") int[] number;
            @CommandLine.Option(names = "-v", arity = "1") boolean verbose;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"123\"", "-v", "\"true\"");
        assertEquals(123, opt.number[0]);
        assertTrue(opt.verbose);

        opt = CommandLine.populateCommand(new TextOption(), "-t\"123\"", "-v\"true\"");
        assertEquals(123, opt.number[0]);
        assertTrue(opt.verbose);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"345\"", "-v=\"true\"");
        assertEquals(345, opt.number[0]);
        assertTrue(opt.verbose);
    }

    @Test
    public void testOptionMultiParameterQuotesRemovedFromValue() {
        class TextOption {
            @CommandLine.Option(names = "-t") String[] text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"a text\"", "-t", "\"another text\"", "-t", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t\"a text\"", "-t\"another text\"", "-t\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"", "-t=\"another text\"", "-t=\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        try {
            opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"", "-t=\"another text\"", "\"x z\"");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [\"x z\"]", ok.getMessage());
        }
    }

    @Test
    public void testPositionalParameterQuotesRemovedFromValue() {
        class TextParams {
            @CommandLine.Parameters() String[] text;
        }
        TextParams opt = CommandLine.populateCommand(new TextParams(), "\"a text\"");
        assertEquals("a text", opt.text[0]);
    }

    @Test
    public void testPositionalMultiParameterQuotesRemovedFromValue() {
        class TextParams {
            @CommandLine.Parameters() String[] text;
        }
        TextParams opt = CommandLine.populateCommand(new TextParams(), "\"a text\"", "\"another text\"", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);
    }

    @Test
    public void testPositionalMultiQuotedParameterTypeConversion() {
        class TextParams {
            @CommandLine.Parameters() int[] numbers;
        }
        TextParams opt = CommandLine.populateCommand(new TextParams(), "\"123\"", "\"456\"", "\"999\"");
        assertArrayEquals(new int[]{123, 456, 999}, opt.numbers);
    }

    @Test
    public void testSubclassedOptions() {
        class ParentOption {
            @CommandLine.Option(names = "-p") String path;
        }
        class ChildOption extends ParentOption {
            @CommandLine.Option(names = "-t") String text;
        }
        ChildOption opt = CommandLine.populateCommand(new ChildOption(), "-p", "somePath", "-t", "\"a text\"");
        assertEquals("somePath", opt.path);
        assertEquals("a text", opt.text);
    }

    @Test
    public void testSubclassedOptionsWithShadowedOptionNameThrowsDuplicateOptionAnnotationsException() {
        class ParentOption {
            @CommandLine.Option(names = "-p") String path;
        }
        class ChildOption extends ParentOption {
            @CommandLine.Option(names = "-p") String text;
        }
        try {
            CommandLine.populateCommand(new ChildOption(), "");
            fail("expected CommandLine$DuplicateOptionAnnotationsException");
        } catch (DuplicateOptionAnnotationsException ex) {
            String expected = String.format("Option name '-p' is used by both %s.path and %s.text",
                    ParentOption.class.getName(), ChildOption.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testSubclassedOptionsWithShadowedFieldInitializesChildField() {
        class ParentOption {
            @CommandLine.Option(names = "-parentPath") String path;
        }
        class ChildOption extends ParentOption {
            @CommandLine.Option(names = "-childPath") String path;
        }
        ChildOption opt = CommandLine.populateCommand(new ChildOption(), "-childPath", "somePath");
        assertEquals("somePath", opt.path);

        opt = CommandLine.populateCommand(new ChildOption(), "-parentPath", "somePath");
        assertNull(opt.path);
    }

    @Test
    public void testPositionalParamWithAbsoluteIndex() {
        class App {
            @Parameters(index = "0") File file0;
            @Parameters(index = "1") File file1;
            @Parameters(index = "2", arity = "0..1") File file2;
            @Parameters List<String> all;
        }
        App app1 = CommandLine.populateCommand(new App(), "000", "111", "222", "333");
        assertEquals("arg[0]", new File("000"), app1.file0);
        assertEquals("arg[1]", new File("111"), app1.file1);
        assertEquals("arg[2]", new File("222"), app1.file2);
        assertEquals("args", Arrays.asList("000", "111", "222", "333"), app1.all);

        App app2 = CommandLine.populateCommand(new App(), "000", "111");
        assertEquals("arg[0]", new File("000"), app2.file0);
        assertEquals("arg[1]", new File("111"), app2.file1);
        assertEquals("arg[2]", null, app2.file2);
        assertEquals("args", Arrays.asList("000", "111"), app2.all);

        try {
            CommandLine.populateCommand(new App(), "000");
            fail("Should fail with missingParamException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <file1>", ex.getMessage());
        }
    }

    @Test
    public void testPositionalParamWithFixedIndexRange() {
        System.setProperty("picocli.trace", "OFF");
        class App {
            @Parameters(index = "0..1") File file0_1;
            @Parameters(index = "1..2", type = File.class) List<File> fileList1_2;
            @Parameters(index = "0..3") File[] fileArray0_3 = new File[4];
            @Parameters List<String> all;
        }
        App app1 = new App();
        new CommandLine(app1).setOverwrittenOptionsAllowed(true).parse("000", "111", "222", "333");
        assertEquals("field initialized with arg[0]", new File("111"), app1.file0_1);
        assertEquals("arg[1] and arg[2]", Arrays.asList(
                new File("111"),
                new File("222")), app1.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                null, null, null, null, // existing values
                new File("000"),
                new File("111"),
                new File("222"),
                new File("333")}, app1.fileArray0_3);
        assertEquals("args", Arrays.asList("000", "111", "222", "333"), app1.all);

        App app2 = new App();
        new CommandLine(app2).setOverwrittenOptionsAllowed(true).parse("000", "111");
        assertEquals("field initialized with arg[0]", new File("111"), app2.file0_1);
        assertEquals("arg[1]", Arrays.asList(new File("111")), app2.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                null, null, null, null, // existing values
                new File("000"),
                new File("111"),}, app2.fileArray0_3);
        assertEquals("args", Arrays.asList("000", "111"), app2.all);

        App app3 = CommandLine.populateCommand(new App(), "000");
        assertEquals("field initialized with arg[0]", new File("000"), app3.file0_1);
        assertEquals("arg[1]", null, app3.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                null, null, null, null, // existing values
                new File("000")}, app3.fileArray0_3);
        assertEquals("args", Arrays.asList("000"), app3.all);

        try {
            CommandLine.populateCommand(new App());
            fail("Should fail with missingParamException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <file0_1>", ex.getMessage());
        }
    }

    @Test
    public void testPositionalParamWithFixedAndVariableIndexRanges() throws Exception {
        class App {
            @Parameters(index = "0") InetAddress host1;
            @Parameters(index = "1") int port1;
            @Parameters(index = "2") InetAddress host2;
            @Parameters(index = "3..4", arity = "1..2") int[] port2range;
            @Parameters(index = "4..*") String[] files;
        }
        App app1 = CommandLine.populateCommand(new App(), "localhost", "1111", "localhost", "2222", "3333", "file1", "file2");
        assertEquals(InetAddress.getByName("localhost"), app1.host1);
        assertEquals(1111, app1.port1);
        assertEquals(InetAddress.getByName("localhost"), app1.host2);
        assertArrayEquals(new int[]{2222, 3333}, app1.port2range);
        assertArrayEquals(new String[]{"file1", "file2"}, app1.files);
    }

    @Ignore("Requires #70 support for variable arity in positional parameters")
    @Test
    public void testPositionalParamWithFixedIndexRangeAndVariableArity() throws Exception {
        class App {
            @Parameters(index = "0") InetAddress host1;
            @Parameters(index = "1") int port1;
            @Parameters(index = "2") InetAddress host2;
            @Parameters(index = "3..4", arity = "1..2") int[] port2range;
            @Parameters(index = "4..*") String[] files;
        }
        // now with only one arg in port2range
        App app2 = CommandLine.populateCommand(new App(), "localhost", "1111", "localhost", "2222", "file1", "file2");
        assertEquals(InetAddress.getByName("localhost"), app2.host1);
        assertEquals(1111, app2.port1);
        assertEquals(InetAddress.getByName("localhost"), app2.host2);
        assertArrayEquals(new int[]{2222}, app2.port2range);
        assertArrayEquals(new String[]{"file1", "file2"}, app2.files);
    }

    @Test(expected = ParameterIndexGapException.class)
    public void testPositionalParamWithIndexGap_SkipZero() throws Exception {
        class SkipZero { @Parameters(index = "1") String str; }
        CommandLine.populateCommand(new SkipZero(),"val1", "val2");
    }

    @Test(expected = ParameterIndexGapException.class)
    public void testPositionalParamWithIndexGap_RangeSkipZero() throws Exception {
        class SkipZero { @Parameters(index = "1..*") String str; }
        CommandLine.populateCommand(new SkipZero(),"val1", "val2");
    }

    @Test(expected = ParameterIndexGapException.class)
    public void testPositionalParamWithIndexGap_FixedIndexGap() throws Exception {
        class SkipOne {
            @Parameters(index = "0") String str0;
            @Parameters(index = "2") String str2;
        }
        CommandLine.populateCommand(new SkipOne(),"val1", "val2");
    }

    @Test(expected = ParameterIndexGapException.class)
    public void testPositionalParamWithIndexGap_RangeIndexGap() throws Exception {
        class SkipTwo {
            @Parameters(index = "0..1") String str0;
            @Parameters(index = "3") String str2;
        }
        CommandLine.populateCommand(new SkipTwo(),"val0", "val1", "val2", "val3");
    }

    @Test
    public void testPositionalParamWithIndexGap_VariableRangeIndexNoGap() throws Exception {
        class NoGap {
            @Parameters(index = "0..*") String[] str0;
            @Parameters(index = "3") String str2;
        }
        NoGap noGap = CommandLine.populateCommand(new NoGap(),"val0", "val1", "val2", "val3");
        assertArrayEquals(new String[] {"val0", "val1", "val2", "val3"}, noGap.str0);
        assertEquals("val3", noGap.str2);
    }

    @Test
    public void testPositionalParamWithIndexGap_RangeIndexNoGap() throws Exception {
        class NoGap {
            @Parameters(index = "0..1") String[] str0;
            @Parameters(index = "2") String str2;
        }
        NoGap noGap = CommandLine.populateCommand(new NoGap(),"val0", "val1", "val2");
        assertArrayEquals(new String[] {"val0", "val1"}, noGap.str0);
        assertEquals("val2", noGap.str2);
    }

    @Test(expected = UnmatchedArgumentException.class)
    public void testPositionalParamsDisallowUnknownArgumentSingleValue() throws Exception {
        class SingleValue {
            @Parameters(index = "0") String str;
        }
        SingleValue single = CommandLine.populateCommand(new SingleValue(),"val1", "val2");
        assertEquals("val1", single.str);
    }

    @Test(expected = UnmatchedArgumentException.class)
    public void testPositionalParamsDisallowUnknownArgumentMultiValue() throws Exception {
        class SingleValue {
            @Parameters(index = "0..2") String[] str;
        }
        CommandLine.populateCommand(new SingleValue(),"val0", "val1", "val2", "val3");
    }
    @Test
    public void testPositionalParamsUnknownArgumentSingleValueWithUnmatchedArgsAllowed() throws Exception {
        class SingleValue {
            @Parameters(index = "0") String str;
        }
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("val1", "val2");
        assertEquals("val1", ((SingleValue)cmd.getCommand()).str);
        assertEquals(Arrays.asList("val2"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testPositionalParamsUnknownArgumentMultiValueWithUnmatchedArgsAllowed() throws Exception {
        class SingleValue {
            @Parameters(index = "0..2") String[] str;
        }
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("val0", "val1", "val2", "val3");
        assertArrayEquals(new String[]{"val0", "val1", "val2"}, ((SingleValue)cmd.getCommand()).str);
        assertEquals(Arrays.asList("val3"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testPositionalParamSingleValueButWithoutIndex() throws Exception {
        class SingleValue {
            @Parameters String str;
        }
        try {
            CommandLine.populateCommand(new SingleValue(),"val1", "val2");
            fail("Expected OverwrittenOptionException");
        } catch (OverwrittenOptionException ex) {
            assertEquals("positional parameter at index 0..* (<str>) should be specified only once", ex.getMessage());
        }
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new SingleValue()).setOverwrittenOptionsAllowed(true);
        cmd.parse("val1", "val2");
        assertEquals("val2", ((SingleValue) cmd.getCommand()).str);
    }

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
            args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments [B, C]", ok.getMessage());
        }
        try {
            args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "-a=C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [B]", ok.getMessage());
        }
    }

    @Test
    public void testSplitInOptionArrayWithSpaces() {
        class Args {
            @Option(names = "-a", split = " ") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=\"a b c\"");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a b c", "-a", "B", "-a", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "\"a b c\"", "-a=B", "-a=C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=\"a b c\"", "-a=B", "-a", "C", "-a=D E F");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C", "D", "E", "F"}, args.values);

        try {
            args = CommandLine.populateCommand(new Args(), "-a=a b c", "B", "C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments [B, C]", ok.getMessage());
        }
        try {
            args = CommandLine.populateCommand(new Args(), "-a=a b c", "B", "-a=C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [B]", ok.getMessage());
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
            @Option(names = "-a", split = ",") List<String> values;
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
            args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments [B, C]", ok.getMessage());
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
        }
        try {
            CommandLine.populateCommand(new Args()); // 0 arg: should fail
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<values>) requires at least 2 values, but none were specified.", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new Args(), "a,b,c", "B,C", "d", "e", "f,g"); // 5 args
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (<values>) requires at least 2 values, but only 1 were specified: [f,g]", ex.getMessage());
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
    public void testSplitIgnoredInOptionSingleValueField() {
        class Args {
            @Option(names = "-a", split = ",") String value;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertEquals("a,b,c", args.value);
    }

    @Test
    public void testSplitIgnoredInParameterSingleValueField() {
        class Args {
            @Parameters(split = ",") String value;
        }
        Args args = CommandLine.populateCommand(new Args(), "a,b,c");
        assertEquals("a,b,c", args.value);
    }

    @Test
    public void testParseSubCommands() {
        CommandLine commandLine = Demo.mainCommand();

        List<CommandLine> parsed = commandLine.parse("--git-dir=/home/rpopma/picocli status -sbuno".split(" "));
        assertEquals("command count", 2, parsed.size());

        assertEquals(Demo.Git.class,       parsed.get(0).getCommand().getClass());
        assertEquals(Demo.GitStatus.class, parsed.get(1).getCommand().getClass());

        Demo.Git git = (Demo.Git) parsed.get(0).getCommand();
        assertEquals(new File("/home/rpopma/picocli"), git.gitDir);

        Demo.GitStatus status = (Demo.GitStatus) parsed.get(1).getCommand();
        assertTrue("status -s", status.shortFormat);
        assertTrue("status -b", status.branchInfo);
        assertFalse("NOT status --showIgnored", status.showIgnored);
        assertEquals("status -u=no", Demo.GitStatusMode.no, status.mode);
    }
    @Test
    public void testTracingInfoWithSubCommands() throws Exception {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "");
        CommandLine commandLine = Demo.mainCommand();
        commandLine.parse("--git-dir=/home/rpopma/picocli", "commit", "-m", "\"Fixed typos\"", "--", "src1.java", "src2.java", "src3.java");
        System.setErr(originalErr);
        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
        String expected = String.format("" +
                        "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli INFO] Setting File field 'Git.gitDir' to '%s' (was 'null') for option --git-dir%n" +
                        "[picocli INFO] Adding [Fixed typos] to List<String> field 'GitCommit.message' for option -m%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli INFO] Adding [src1.java] to List<File> field 'GitCommit.files' for args[0..*] at position 0%n" +
                        "[picocli INFO] Adding [src2.java] to List<File> field 'GitCommit.files' for args[0..*] at position 1%n" +
                        "[picocli INFO] Adding [src3.java] to List<File> field 'GitCommit.files' for args[0..*] at position 2%n",
                new File("/home/rpopma/picocli"));
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
    }
    @Test
    public void testTracingDebugWithSubCommands() throws Exception {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "DEBUG");
        CommandLine commandLine = Demo.mainCommand();
        commandLine.parse("--git-dir=/home/rpopma/picocli", "commit", "-m", "\"Fixed typos\"", "--", "src1.java", "src2.java", "src3.java");
        System.setErr(originalErr);
        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
        String expected = String.format("" +
                        "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Initializing %1$s$Git: 3 options, 0 positional parameters, 0 required, 11 subcommands.%n" +
                        "[picocli DEBUG] Processing argument '--git-dir=/home/rpopma/picocli'. Remainder=[commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Separated '--git-dir' option from '/home/rpopma/picocli' option parameter%n" +
                        "[picocli DEBUG] Found option named '--git-dir': field java.io.File %1$s$Git.gitDir, arity=1%n" +
                        "[picocli INFO] Setting File field 'Git.gitDir' to '%2$s' (was 'null') for option --git-dir%n" +
                        "[picocli DEBUG] Processing argument 'commit'. Remainder=[-m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Found subcommand 'commit' (%1$s$GitCommit)%n" +
                        "[picocli DEBUG] Initializing %1$s$GitCommit: 8 options, 1 positional parameters, 0 required, 0 subcommands.%n" +
                        "[picocli DEBUG] Processing argument '-m'. Remainder=[\"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] '-m' cannot be separated into <option>=<option-parameter>%n" +
                        "[picocli DEBUG] Found option named '-m': field java.util.List %1$s$GitCommit.message, arity=1%n" +
                        "[picocli INFO] Adding [Fixed typos] to List<String> field 'GitCommit.message' for option -m%n" +
                        "[picocli DEBUG] Processing argument '--'. Remainder=[src1.java, src2.java, src3.java]%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=0. Remainder=[src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Position 0 is in index range 0..*. Trying to assign args to java.util.List %1$s$GitCommit.files, arity=0..1%n" +
                        "[picocli INFO] Adding [src1.java] to List<File> field 'GitCommit.files' for args[0..*] at position 0%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 1.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=1. Remainder=[src2.java, src3.java]%n" +
                        "[picocli DEBUG] Position 1 is in index range 0..*. Trying to assign args to java.util.List %1$s$GitCommit.files, arity=0..1%n" +
                        "[picocli INFO] Adding [src2.java] to List<File> field 'GitCommit.files' for args[0..*] at position 1%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 2.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=2. Remainder=[src3.java]%n" +
                        "[picocli DEBUG] Position 2 is in index range 0..*. Trying to assign args to java.util.List %1$s$GitCommit.files, arity=0..1%n" +
                        "[picocli INFO] Adding [src3.java] to List<File> field 'GitCommit.files' for args[0..*] at position 2%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 3.%n",
                Demo.class.getName(), new File("/home/rpopma/picocli"));
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
    }
    @Test
    public void testTraceWarningIfOptionOverwrittenWhenOverwrittenOptionsAllowed() throws Exception {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        CommandLine cmd = new CommandLine(new PrivateFinalOptionFields()).setOverwrittenOptionsAllowed(true);
        cmd.parse("-f", "111", "-f", "222", "-f", "333");
        PrivateFinalOptionFields ff = (PrivateFinalOptionFields) cmd.getCommand();
        assertEquals("333", ff.field);
        System.setErr(originalErr);

        String expected = String.format("" +
                        "[picocli WARN] Overwriting String field 'PrivateFinalOptionFields.field' value '111' with '222' for option -f%n" +
                        "[picocli WARN] Overwriting String field 'PrivateFinalOptionFields.field' value '222' with '333' for option -f%n"
        );
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
    }
    @Test
    public void testTraceWarningIfUnmatchedArgsWhenUnmatchedArgumentsAllowed() throws Exception {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        class App {
            @Parameters(index = "0", arity = "2", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true).parse("1=a", "2=b", "3=c", "4=d").get(0);
        assertEquals(Arrays.asList("3=c", "4=d"), cmd.getUnmatchedArguments());
        System.setErr(originalErr);

        String expected = String.format("[picocli WARN] Unmatched arguments: [3=c, 4=d]%n");
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testCommandListReturnsRegisteredCommands() {
        @Command class MainCommand {}
        @Command class Command1 {}
        @Command class Command2 {}
        CommandLine commandLine = new CommandLine(new MainCommand());
        commandLine.addSubcommand("cmd1", new Command1()).addSubcommand("cmd2", new Command2());

        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertEquals(2, commandMap.size());
        assertTrue("cmd1", commandMap.get("cmd1").getCommand() instanceof Command1);
        assertTrue("cmd2", commandMap.get("cmd2").getCommand() instanceof Command2);
    }

    static class MainCommand { @Option(names = "-a") boolean a; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class ChildCommand1 { @Option(names = "-b") boolean b; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class ChildCommand2 { @Option(names = "-c") boolean c; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class GrandChild1Command1 { @Option(names = "-d") boolean d; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class GrandChild1Command2 { @Option(names = "-e") CustomType e; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class GrandChild2Command1 { @Option(names = "-f") boolean f; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class GrandChild2Command2 { @Option(names = "-g") boolean g; public boolean equals(Object o) { return getClass().equals(o.getClass()); }}
    static class GreatGrandChild2Command2_1 {
        @Option(names = "-h") boolean h;
        @Option(names = {"-t", "--type"}) CustomType customType;
        public boolean equals(Object o) { return getClass().equals(o.getClass()); }
    }

    static class CustomType implements ITypeConverter<CustomType> {
        private final String val;
        private CustomType(String val) { this.val = val; }
        public CustomType convert(String value) { return new CustomType(value); }
    }
    private static CommandLine createNestedCommand() {
        CommandLine commandLine = new CommandLine(new MainCommand());
        commandLine
                .addSubcommand("cmd1", new CommandLine(new ChildCommand1())
                        .addSubcommand("sub11", new GrandChild1Command1())
                        .addSubcommand("sub12", new GrandChild1Command2())
                )
                .addSubcommand("cmd2", new CommandLine(new ChildCommand2())
                        .addSubcommand("sub21", new GrandChild2Command1())
                        .addSubcommand("sub22", new CommandLine(new GrandChild2Command2())
                                .addSubcommand("sub22sub1", new GreatGrandChild2Command2_1())
                        )
                );
        return commandLine;
    }

    @Test
    public void testCommandListReturnsOnlyCommandsRegisteredOnInstance() {
        CommandLine commandLine = createNestedCommand();

        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertEquals(2, commandMap.size());
        assertTrue("cmd1", commandMap.get("cmd1").getCommand() instanceof ChildCommand1);
        assertTrue("cmd2", commandMap.get("cmd2").getCommand() instanceof ChildCommand2);
    }

    @Test
    public void testParseNestedSubCommands() {
        // valid
        List<CommandLine> main = createNestedCommand().parse("cmd1");
        assertEquals(2, main.size());
        assertFalse(((MainCommand)   main.get(0).getCommand()).a);
        assertFalse(((ChildCommand1) main.get(1).getCommand()).b);

        List<CommandLine> mainWithOptions = createNestedCommand().parse("-a", "cmd1", "-b");
        assertEquals(2, mainWithOptions.size());
        assertTrue(((MainCommand)   mainWithOptions.get(0).getCommand()).a);
        assertTrue(((ChildCommand1) mainWithOptions.get(1).getCommand()).b);

        List<CommandLine> sub1 = createNestedCommand().parse("cmd1", "sub11");
        assertEquals(3, sub1.size());
        assertFalse(((MainCommand)         sub1.get(0).getCommand()).a);
        assertFalse(((ChildCommand1)       sub1.get(1).getCommand()).b);
        assertFalse(((GrandChild1Command1) sub1.get(2).getCommand()).d);

        List<CommandLine> sub1WithOptions = createNestedCommand().parse("-a", "cmd1", "-b", "sub11", "-d");
        assertEquals(3, sub1WithOptions.size());
        assertTrue(((MainCommand)         sub1WithOptions.get(0).getCommand()).a);
        assertTrue(((ChildCommand1)       sub1WithOptions.get(1).getCommand()).b);
        assertTrue(((GrandChild1Command1) sub1WithOptions.get(2).getCommand()).d);

        // sub12 is not nested under sub11 so is not recognized
        try {
            createNestedCommand().parse("cmd1", "sub11", "sub12");
            fail("Expected exception for sub12");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [sub12]", ex.getMessage());
        }
        List<CommandLine> sub22sub1 = createNestedCommand().parse("cmd2", "sub22", "sub22sub1");
        assertEquals(4, sub22sub1.size());
        assertFalse(((MainCommand)                sub22sub1.get(0).getCommand()).a);
        assertFalse(((ChildCommand2)              sub22sub1.get(1).getCommand()).c);
        assertFalse(((GrandChild2Command2)        sub22sub1.get(2).getCommand()).g);
        assertFalse(((GreatGrandChild2Command2_1) sub22sub1.get(3).getCommand()).h);

        List<CommandLine> sub22sub1WithOptions = createNestedCommand().parse("-a", "cmd2", "-c", "sub22", "-g", "sub22sub1", "-h");
        assertEquals(4, sub22sub1WithOptions.size());
        assertTrue(((MainCommand)                sub22sub1WithOptions.get(0).getCommand()).a);
        assertTrue(((ChildCommand2)              sub22sub1WithOptions.get(1).getCommand()).c);
        assertTrue(((GrandChild2Command2)        sub22sub1WithOptions.get(2).getCommand()).g);
        assertTrue(((GreatGrandChild2Command2_1) sub22sub1WithOptions.get(3).getCommand()).h);

        // invalid
        try {
            createNestedCommand().parse("-a", "-b", "cmd1");
            fail("unmatched option should prevents remainder to be parsed as command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [-b]", ex.getMessage());
        }
        try {
            createNestedCommand().parse("cmd1", "sub21");
            fail("sub-commands for different parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [sub21]", ex.getMessage());
        }
        try {
            createNestedCommand().parse("cmd1", "sub22sub1");
            fail("sub-sub-commands for different parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [sub22sub1]", ex.getMessage());
        }
        try {
            createNestedCommand().parse("sub11");
            fail("sub-commands without preceding parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [sub11]", ex.getMessage());
        }
        try {
            createNestedCommand().parse("sub21");
            fail("sub-commands without preceding parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [sub21]", ex.getMessage());
        }
        try {
            createNestedCommand().parse("sub22sub1");
            fail("sub-sub-commands without preceding parent/grandparent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [sub22sub1]", ex.getMessage());
        }
    }

    @Test
    public void testParseNestedSubCommandsAllowingUnmatchedArguments() {
        setTraceLevel("OFF");
        List<CommandLine> result1 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("-a", "-b", "cmd1");
        assertEquals(Arrays.asList("-b"), result1.get(0).getUnmatchedArguments());

        List<CommandLine> result2 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("cmd1", "sub21");
        assertEquals(Arrays.asList("sub21"), result2.get(1).getUnmatchedArguments());

        List<CommandLine> result3 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("cmd1", "sub22sub1");
        assertEquals(Arrays.asList("sub22sub1"), result3.get(1).getUnmatchedArguments());

        List<CommandLine> result4 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("sub11");
        assertEquals(Arrays.asList("sub11"), result4.get(0).getUnmatchedArguments());

        List<CommandLine> result5 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("sub21");
        assertEquals(Arrays.asList("sub21"), result5.get(0).getUnmatchedArguments());

        List<CommandLine> result6 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("sub22sub1");
        assertEquals(Arrays.asList("sub22sub1"), result6.get(0).getUnmatchedArguments());
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testCustomTypeConverterNotRegisteredAtAll() {
        CommandLine commandLine = createNestedCommand();
        commandLine.parse("cmd1", "sub12", "-e", "TXT");
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testCustomTypeConverterRegisteredBeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.registerConverter(CustomType.class, new CustomType(null));

        commandLine.addSubcommand("main", createNestedCommand());
        commandLine.parse("main", "cmd1", "sub12", "-e", "TXT");
    }

    @Test
    public void testCustomTypeConverterRegisteredAfterSubcommandsAdded() {
        @Command class TopLevel { public boolean equals(Object o) {return getClass().equals(o.getClass());}}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        commandLine.registerConverter(CustomType.class, new CustomType(null));
        List<CommandLine> parsed = commandLine.parse("main", "cmd1", "sub12", "-e", "TXT");
        assertEquals(4, parsed.size());
        assertEquals(TopLevel.class, parsed.get(0).getCommand().getClass());
        assertFalse(((MainCommand)   parsed.get(1).getCommand()).a);
        assertFalse(((ChildCommand1) parsed.get(2).getCommand()).b);
        assertEquals("TXT", ((GrandChild1Command2) parsed.get(3).getCommand()).e.val);
    }

    @Test
    public void testRunCallsRunnableIfParseSucceeds() {
        final boolean[] runWasCalled = {false};
        @Command class App implements Runnable {
            public void run() {
                runWasCalled[0] = true;
            }
        }
        CommandLine.run(new App(), System.err);
        assertTrue(runWasCalled[0]);
    }

    @Test
    public void testRunPrintsErrorIfParseFails() throws UnsupportedEncodingException {
        final boolean[] runWasCalled = {false};
        class App implements Runnable {
            @Option(names = "-number") int number;
            public void run() {
                runWasCalled[0] = true;
            }
        }
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos, true, "UTF8"));
        CommandLine.run(new App(), System.err, "-number", "not a number");
        System.setErr(oldErr);

        String result = baos.toString("UTF8");
        assertFalse(runWasCalled[0]);
        assertEquals(String.format(
                "Could not convert 'not a number' to int for option '-number': java.lang.NumberFormatException: For input string: \"not a number\"%n" +
                "Usage: <main class> [-number=<number>]%n" +
                "      -number=<number>%n"), result);
    }

    @Test(expected = InitializationException.class)
    public void testRunRequiresAnnotatedCommand() {
        class App implements Runnable {
            public void run() { }
        }
        CommandLine.run(new App(), System.err);
    }

    @Test
    public void testCallReturnsCallableResultParseSucceeds() throws Exception {
        @Command class App implements Callable<Boolean> {
            public Boolean call() { return true; }
        }
        assertTrue(CommandLine.call(new App(), System.err));
    }

    @Test
    public void testCallReturnsNullAndPrintsErrorIfParseFails() throws Exception {
        class App implements Callable<Boolean> {
            @Option(names = "-number") int number;
            public Boolean call() { return true; }
        }
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos, true, "UTF8"));
        Boolean callResult = CommandLine.call(new App(), System.err, "-number", "not a number");
        System.setErr(oldErr);

        String result = baos.toString("UTF8");
        assertNull(callResult);
        assertEquals(String.format(
                "Could not convert 'not a number' to int for option '-number': java.lang.NumberFormatException: For input string: \"not a number\"%n" +
                        "Usage: <main class> [-number=<number>]%n" +
                        "      -number=<number>%n"), result);
    }

    @Test(expected = InitializationException.class)
    public void testCallRequiresAnnotatedCommand() throws Exception {
        class App implements Callable<Object> {
            public Object call() { return null; }
        }
        CommandLine.call(new App(), System.err);
    }

    @Test(expected = InitializationException.class)
    public void testPopulateCommandRequiresAnnotatedCommand() {
        class App { }
        CommandLine.populateCommand(new App());
    }

    @Test(expected = InitializationException.class)
    public void testUsageObjectPrintstreamRequiresAnnotatedCommand() {
        class App { }
        CommandLine.usage(new App(), System.out);
    }

    @Test(expected = InitializationException.class)
    public void testUsageObjectPrintstreamAnsiRequiresAnnotatedCommand() {
        class App { }
        CommandLine.usage(new App(), System.out, Help.Ansi.OFF);
    }

    @Test(expected = InitializationException.class)
    public void testUsageObjectPrintstreamColorschemeRequiresAnnotatedCommand() {
        class App { }
        CommandLine.usage(new App(), System.out, Help.defaultColorScheme(Help.Ansi.OFF));
    }

    @Test(expected = InitializationException.class)
    public void testConstructorRequiresAnnotatedCommand() {
        class App { }
        new CommandLine(new App());
    }

    @Test
    public void testOverwrittenOptionDisallowedByDefault() {
        class App {
            @Option(names = "-s") String string;
            @Option(names = "-v") boolean bool;
        }
        try {
            CommandLine.populateCommand(new App(), "-s", "1", "-s", "2");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-s' (<string>) should be specified only once", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "-v");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-v' (<bool>) should be specified only once", ex.getMessage());
        }
    }

    @Test
    public void testOverwrittenOptionDisallowedByDefaultRegardlessOfAlias() {
        class App {
            @Option(names = {"-s", "--str"})      String string;
            @Option(names = {"-v", "--verbose"}) boolean bool;
        }
        try {
            CommandLine.populateCommand(new App(), "-s", "1", "--str", "2");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-s' (<string>) should be specified only once", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "--verbose");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-v' (<bool>) should be specified only once", ex.getMessage());
        }
    }

    @Test
    public void testOverwrittenOptionSetsLastValueIfAllowed() {
        class App {
            @Option(names = {"-s", "--str"})      String string;
            @Option(names = {"-v", "--verbose"}) boolean bool;
        }
        setTraceLevel("OFF");
        CommandLine commandLine = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-s", "1", "--str", "2");
        assertEquals("2", ((App) commandLine.getCommand()).string);

        commandLine = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-v", "--verbose", "-v"); // F -> T -> F -> T
        assertEquals(true, ((App) commandLine.getCommand()).bool);
    }

    @Test
    public void testIssue141Npe() {
        class A {
            @Option(names = { "-u", "--user" }, required = true, description = "user id")
            private String user;

            @Option(names = { "-p", "--password" }, required = true, description = "password")
            private String password;
        }
        A a = new A();
        CommandLine commandLine = new CommandLine(a);
        try {
            commandLine.parse("-u", "foo");
            fail("expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '-p=<password>'", ex.getLocalizedMessage());
        }
        commandLine.parse("-u", "foo", "-p", "abc");
    }

    @Test
    public void testToggleBooleanValue() {
        class App {
            @Option(names = "-a") boolean primitiveFalse = false;
            @Option(names = "-b") boolean primitiveTrue = true;
            @Option(names = "-c") Boolean objectFalse = false;
            @Option(names = "-d") Boolean objectTrue = true;
            @Option(names = "-e") Boolean objectNull = null;
        }
        App app = CommandLine.populateCommand(new App(), "-a -b -c -d -e".split(" "));
        assertTrue(app.primitiveFalse);
        assertFalse(app.primitiveTrue);
        assertTrue(app.objectFalse);
        assertFalse(app.objectTrue);
        assertTrue(app.objectNull);
    }

    @Test(timeout = 15000)
    public void testIssue148InfiniteLoop() throws Exception {
        @Command(showDefaultValues = true)
        class App {
            @Option(names = "--foo-bar-baz")
            String foo = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            // Default value needs to be at least 1 character larger than the "WRAP" column in TextTable(Ansi), which is
            // currently 51 characters. Going with 81 to be safe.
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output);
        CommandLine.usage(new App(), printStream);

        String content = new String(output.toByteArray(), "UTF-8")
                .replaceAll("\r\n", "\n"); // Normalize line endings.

        String expectedOutput =
                "Usage: <main class> [--foo-bar-baz=<foo>]\n" +
                "      --foo-bar-baz=<foo>       Default:\n" +
                "                                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "                                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n";

        assertEquals(expectedOutput, content);
    }
    @Command(name = "subsub1")
    static class SubSub1_testDeclarativelyAddSubcommands {private SubSub1_testDeclarativelyAddSubcommands(){}}

    @Command(name = "sub1", subcommands = {SubSub1_testDeclarativelyAddSubcommands.class})
    static class Sub1_testDeclarativelyAddSubcommands {public Sub1_testDeclarativelyAddSubcommands(){}}

    @Command(subcommands = {Sub1_testDeclarativelyAddSubcommands.class})
    static class MainCommand_testDeclarativelyAddSubcommands {}
    @Test
    public void testDeclarativelyAddSubcommands() {
        CommandLine main = new CommandLine(new MainCommand_testDeclarativelyAddSubcommands());
        assertEquals(1, main.getSubcommands().size());

        CommandLine sub1 = main.getSubcommands().get("sub1");
        assertEquals(Sub1_testDeclarativelyAddSubcommands.class, sub1.getCommand().getClass());

        assertEquals(1, sub1.getSubcommands().size());
        CommandLine subsub1 = sub1.getSubcommands().get("subsub1");
        assertEquals(SubSub1_testDeclarativelyAddSubcommands.class, subsub1.getCommand().getClass());
    }
    @Test
    public void testGetParentForDeclarativelyAddedSubcommands() {
        CommandLine main = new CommandLine(new MainCommand_testDeclarativelyAddSubcommands());
        assertEquals(1, main.getSubcommands().size());

        CommandLine sub1 = main.getSubcommands().get("sub1");
        assertSame(main, sub1.getParent());
        assertEquals(Sub1_testDeclarativelyAddSubcommands.class, sub1.getCommand().getClass());

        assertEquals(1, sub1.getSubcommands().size());
        CommandLine subsub1 = sub1.getSubcommands().get("subsub1");
        assertSame(sub1, subsub1.getParent());
        assertEquals(SubSub1_testDeclarativelyAddSubcommands.class, subsub1.getCommand().getClass());
    }
    @Test
    public void testGetParentForProgrammaticallyAddedSubcommands() {
        CommandLine main = createNestedCommand();
        for (CommandLine child : main.getSubcommands().values()) {
            assertSame(main, child.getParent());
            for (CommandLine grandChild : child.getSubcommands().values()) {
                assertSame(child, grandChild.getParent());
            }
        }
    }
    @Test
    public void testGetParentIsNullForTopLevelCommands() {
        @Command class Top {}
        assertNull(new CommandLine(new Top()).getParent());
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutNoArgConstructor() {
        @Command(name = "sub1") class ABC {}
        @Command(subcommands = {ABC.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand());
        } catch (InitializationException ex) {
            String expected = String.format("Cannot instantiate subcommand %s: the class has no constructor", ABC.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutAnnotation() {
        class MissingCommandAnnotation { public MissingCommandAnnotation() {} }
        @Command(subcommands = {MissingCommandAnnotation.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand());
        } catch (InitializationException ex) {
            String expected = String.format("Subcommand %s is missing the mandatory @Command annotation with a 'name' attribute", MissingCommandAnnotation.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutNameOnCommandAnnotation() {
        @Command class MissingNameAttribute{ public MissingNameAttribute() {} }
        @Command(subcommands = {MissingNameAttribute.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand());
        } catch (InitializationException ex) {
            String expected = String.format("Subcommand %s is missing the mandatory @Command annotation with a 'name' attribute", MissingNameAttribute.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }
    @Test
    public void testMapFieldHappyCase() {
        class App {
            @Option(names = {"-P", "-map"}, type = {String.class, String.class}) Map<String, String> map = new HashMap<String, String>();
            private void validateMapField() {
                assertEquals(1, map.size());
                assertEquals(HashMap.class, map.getClass());
                assertEquals("BBB", map.get("AAA"));
            }
        }
        CommandLine.populateCommand(new App(), "-map", "AAA=BBB").validateMapField();
        CommandLine.populateCommand(new App(), "-map=AAA=BBB").validateMapField();
        CommandLine.populateCommand(new App(), "-P=AAA=BBB").validateMapField();
        CommandLine.populateCommand(new App(), "-PAAA=BBB").validateMapField();
        CommandLine.populateCommand(new App(), "-P", "AAA=BBB").validateMapField();
    }
    @Test
    public void testMapFieldHappyCaseWithMultipleValues() {
        class App {
            @Option(names = {"-P", "-map"}, split = ",", type = {String.class, String.class}) Map<String, String> map;
            private void validateMapField3Values() {
                assertEquals(3, map.size());
                assertEquals(LinkedHashMap.class, map.getClass());
                assertEquals("BBB", map.get("AAA"));
                assertEquals("DDD", map.get("CCC"));
                assertEquals("FFF", map.get("EEE"));
            }
        }
        CommandLine.populateCommand(new App(), "-map=AAA=BBB,CCC=DDD,EEE=FFF").validateMapField3Values();
        CommandLine.populateCommand(new App(), "-PAAA=BBB,CCC=DDD,EEE=FFF").validateMapField3Values();
        CommandLine.populateCommand(new App(), "-P", "AAA=BBB,CCC=DDD,EEE=FFF").validateMapField3Values();
        CommandLine.populateCommand(new App(), "-map=AAA=BBB", "-map=CCC=DDD", "-map=EEE=FFF").validateMapField3Values();
        CommandLine.populateCommand(new App(), "-PAAA=BBB", "-PCCC=DDD", "-PEEE=FFF").validateMapField3Values();
        CommandLine.populateCommand(new App(), "-P", "AAA=BBB", "-P", "CCC=DDD", "-P", "EEE=FFF").validateMapField3Values();

        try {
            CommandLine.populateCommand(new App(), "-P", "AAA=BBB", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments [CCC=DDD, EEE=FFF]", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-map=AAA=BBB", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments [CCC=DDD, EEE=FFF]", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-PAAA=BBB", "-PCCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [EEE=FFF]", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-P", "AAA=BBB", "-P", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [EEE=FFF]", ok.getMessage());
        }
    }

    @Test
    public void testMapField_InstantiatesConcreteMap() {
        class App {
            @Option(names = "-map", type = {String.class, String.class}) TreeMap<String, String> map;
        }
        App app = CommandLine.populateCommand(new App(), "-map=AAA=BBB");
        assertEquals(1, app.map.size());
        assertEquals(TreeMap.class, app.map.getClass());
        assertEquals("BBB", app.map.get("AAA"));
    }
    @Test
    public void testMapFieldMissingTypeAttribute() {
        class App {
            @Option(names = "-map") TreeMap<String, String> map;
        }
        try {
            CommandLine.populateCommand(new App(), "-map=AAA=BBB");
        } catch (ParameterException ex) {
            assertEquals("Field java.util.TreeMap " + App.class.getName() +
                    ".map needs two types (one for the map key, one for the value) but only has 1 types configured.", ex.getMessage());
        }
    }
    @Test
    public void testMapFieldMissingTypeConverter() {
        class App {
            @Option(names = "-map", type = {Thread.class, Thread.class}) TreeMap<String, String> map;
        }
        try {
            CommandLine.populateCommand(new App(), "-map=AAA=BBB");
        } catch (ParameterException ex) {
            assertEquals("No TypeConverter registered for java.lang.Thread of field java.util.TreeMap " +
                    App.class.getName() + ".map", ex.getMessage());
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
            assertEquals("Unmatched argument [3=c|4=d]", ex.getMessage());
        }
    }
    @Test
    public void testMapPositionalParameterFieldMaxArity() {
        class App {
            @Parameters(index = "0", arity = "2", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        try {
            CommandLine.populateCommand(new App(), "1=a", "2=b", "3=c", "4=d");
            fail("UnmatchedArgumentsException expected");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [3=c, 4=d]", ex.getMessage());
        }
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("1=a", "2=b", "3=c", "4=d");
        assertEquals(Arrays.asList("3=c", "4=d"), cmd.getUnmatchedArguments());
    }
    @Test
    public void testMapPositionalParameterFieldArity3() {
        class App {
            @Parameters(index = "0", arity = "3", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        try {
            CommandLine.populateCommand(new App(), "1=a", "2=b", "3=c", "4=d");
            fail("UnmatchedArgumentsException expected");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [4=d]", ex.getMessage());
        }
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("1=a", "2=b", "3=c", "4=d");
        assertEquals(Arrays.asList("4=d"), cmd.getUnmatchedArguments());
    }
    @Test
    public void testMapAndCollectionFieldTypeInference() {
        class App {
            @Option(names = "-a") Map<Integer, URI> a;
            @Option(names = "-b") Map<TimeUnit, StringBuilder> b;
            @SuppressWarnings("unchecked")
            @Option(names = "-c") Map c;
            @Option(names = "-d") List<File> d;
            @Option(names = "-e") Map<? extends Integer, ? super Long> e;
            @Option(names = "-f", type = {Long.class, Float.class}) Map<? extends Number, ? super Number> f;
            @SuppressWarnings("unchecked")
            @Option(names = "-g", type = {TimeUnit.class, Float.class}) Map g;
        }
        App app = CommandLine.populateCommand(new App(),
                "-a", "8=/path", "-a", "98765432=/path/to/resource",
                "-b", "SECONDS=abc",
                "-c", "123=ABC",
                "-d", "/path/to/file",
                "-e", "12345=67890",
                "-f", "12345=67.89",
                "-g", "MILLISECONDS=12.34");
        assertEquals(app.a.size(), 2);
        assertEquals(URI.create("/path"), app.a.get(8));
        assertEquals(URI.create("/path/to/resource"), app.a.get(98765432));

        assertEquals(app.b.size(), 1);
        assertEquals(new StringBuilder("abc").toString(), app.b.get(TimeUnit.SECONDS).toString());

        assertEquals(app.c.size(), 1);
        assertEquals("ABC", app.c.get("123"));

        assertEquals(app.d.size(), 1);
        assertEquals(new File("/path/to/file"), app.d.get(0));

        assertEquals(app.e.size(), 1);
        assertEquals(new Long(67890), app.e.get(12345));

        assertEquals(app.f.size(), 1);
        assertEquals(67.89f, app.f.get(new Long(12345)));

        assertEquals(app.g.size(), 1);
        assertEquals(12.34f, app.g.get(TimeUnit.MILLISECONDS));
    }
    @Test
    public void testUseTypeAttributeInsteadOfFieldType() {
        class App {
            @Option(names = "--num", type = BigDecimal.class) // subclass of field type
            Number[] number; // array type with abstract component class

            @Parameters(type = StringBuilder.class) // concrete impl class
            Appendable address; // type declared as interface
        }
        App app = CommandLine.populateCommand(new App(), "--num", "123.456", "ABC");
        assertEquals(1, app.number.length);
        assertEquals(new BigDecimal("123.456"), app.number[0]);

        assertEquals("ABC", app.address.toString());
        assertTrue(app.address instanceof StringBuilder);
    }
    @Test
    public void testMultipleMissingOptions() {
        class App {
            @Option(names = "-a", required = true) String first;
            @Option(names = "-b", required = true) String second;
            @Option(names = "-c", required = true) String third;
        }
        try {
            CommandLine.populateCommand(new App());
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required options [-a=<first>, -b=<second>, -c=<third>]", ex.getMessage());
        }
    }
    @Test
    public void test185MissingOptionsShouldUseLabel() {
        class App {
            @Parameters(arity = "1", paramLabel = "IN_FILE", description = "The input file")
            File foo;
            @Option(names = "-o", paramLabel = "OUT_FILE", description = "The output file", required = true)
            File bar;
        }
        try {
            CommandLine.populateCommand(new App());
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required options [-o=OUT_FILE, params[*]=IN_FILE]", ex.getMessage());
        }
    }
    @Test
    public void test185MissingMapOptionsShouldUseLabel() {
        class App {
            @Parameters(arity = "1", type = {Long.class, File.class}, description = "The input file mapping")
            Map<Long, File> foo;
            @Option(names = "-o", description = "The output file mapping", required = true)
            Map<String, String> bar;
            @Option(names = "-x", paramLabel = "KEY=VAL", description = "Some other mapping", required = true)
            Map<String, String> xxx;
        }
        try {
            CommandLine.populateCommand(new App());
            fail("MissingParameterException expected");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required options [-o=<String=String>, -x=KEY=VAL, params[*]=<Long=File>]", ex.getMessage());
        }
    }
    @Test
    public void testAnyExceptionWrappedInParameterException() {
        class App {
            @Option(names = "-queue", type = String.class, split = ",")
            ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(2);
        }
        try {
            CommandLine.populateCommand(new App(), "-queue a,b,c".split(" "));
            fail("ParameterException expected");
        } catch (ParameterException ex) {
            assertEquals("IllegalStateException: Queue full while processing argument at or before arg[1] 'a,b,c' in [-queue, a,b,c]: java.lang.IllegalStateException: Queue full", ex.getMessage());
        }
    }
    @Test
    public void test149UnmatchedShortOptionsAreMisinterpretedAsOperands() {
        class App {
            @Option(names = "-a") String first;
            @Option(names = "-b") String second;
            @Option(names = {"-c", "--ccc"}) String third;
            @Parameters String[] positional;
        }
        //System.setProperty("picocli.trace", "DEBUG");
        try {
            CommandLine.populateCommand(new App(), "-xx", "-a", "aValue");
            fail("UnmatchedArgumentException expected for -xx");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [-xx]", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-x", "-a", "aValue");
            fail("UnmatchedArgumentException expected for -x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [-x]", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "--x", "-a", "aValue");
            fail("UnmatchedArgumentException expected for --x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [--x]", ex.getMessage());
        }
    }
    @Test
    public void test149NonOptionArgsShouldBeTreatedAsOperands() {
        class App {
            @Option(names = "/a") String first;
            @Option(names = "/b") String second;
            @Option(names = {"/c", "--ccc"}) String third;
            @Parameters String[] positional;
        }
        //System.setProperty("picocli.trace", "DEBUG");
        App app = CommandLine.populateCommand(new App(), "-yy", "-a");
        assertArrayEquals(new String[] {"-yy", "-a"}, app.positional);

        app = CommandLine.populateCommand(new App(), "-y", "-a");
        assertArrayEquals(new String[] {"-y", "-a"}, app.positional);

        app = CommandLine.populateCommand(new App(), "--y", "-a");
        assertArrayEquals(new String[] {"--y", "-a"}, app.positional);
    }
    @Test
    public void test149LongMatchWeighsWhenDeterminingOptionResemblance() {
        class App {
            @Option(names = "/a") String first;
            @Option(names = "/b") String second;
            @Option(names = {"/c", "--ccc"}) String third;
            @Parameters String[] positional;
        }
        //System.setProperty("picocli.trace", "DEBUG");
        try {
            CommandLine.populateCommand(new App(), "--ccd", "-a");
            fail("UnmatchedArgumentException expected for --x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument [--ccd]", ex.getMessage());
        }
    }

    @Test
    public void test149OnlyUnmatchedOptionStoredOthersParsed() throws Exception {
        class App {
            @Option(names = "-a") String first;
            @Option(names = "-b") String second;
            @Option(names = {"-c", "--ccc"}) String third;
            @Parameters String[] positional;
        }
        //System.setProperty("picocli.trace", "DEBUG");
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true).parse("-yy", "-a=A").get(0);
        assertEquals(Arrays.asList("-yy"), cmd.getUnmatchedArguments());
        assertEquals("A", ((App) cmd.getCommand()).first);

        cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true).parse("-y", "-b=B").get(0);
        assertEquals(Arrays.asList("-y"), cmd.getUnmatchedArguments());
        assertEquals("B", ((App) cmd.getCommand()).second);

        cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true).parse("--y", "-c=C").get(0);
        assertEquals(Arrays.asList("--y"), cmd.getUnmatchedArguments());
        assertEquals("C", ((App) cmd.getCommand()).third);

        String expected = String.format("" +
                "[picocli WARN] Unmatched arguments: [-yy]%n" +
                "[picocli WARN] Unmatched arguments: [-y]%n" +
                "[picocli WARN] Unmatched arguments: [--y]%n");
        String actual = new String(baos.toByteArray(), "UTF8");
        assertEquals(expected, actual);
    }

    @Test
    public void test176OptionModifier() {
        class Args {
            @Option(names = "-option", description = "the option value")
            String option;

            @Option(names = "-option:env", description = "the environment variable to look up for the actual value")
            String optionEnvKey;

            @Option(names = "-option:file", description = "path to the file containing the option value")
            File optionFile;
        }
        Args args = CommandLine.populateCommand(new Args(), "-option", "VAL", "-option:env", "KEY", "-option:file", "/path/to/file");
        assertEquals("VAL", args.option);
        assertEquals("KEY", args.optionEnvKey);
        assertEquals(new File("/path/to/file"), args.optionFile);
        //CommandLine.usage(new Args(), System.out);
    }

    @Test
    public void test187GetCommandNameReturnsMainClassByDefault() {
        class Args { @Parameters String[] args; }
        assertEquals("<main class>", new CommandLine(new Args()).getCommandName());
        assertEquals("<main class>", Help.DEFAULT_COMMAND_NAME);
    }

    @Test
    public void test187GetCommandNameReturnsCommandAnnotationNameAttribute() {
        @Command(name = "someCommand")
        class Args { @Parameters String[] args; }
        assertEquals("someCommand", new CommandLine(new Args()).getCommandName());
    }

    @Test
    public void test187SetCommandNameOverwritesCommandAnnotationNameAttribute() {
        @Command(name = "someCommand")
        class Args { @Parameters String[] args; }
        assertEquals("someCommand", new CommandLine(new Args()).getCommandName());

        String OTHER = "a different name";
        assertEquals(OTHER, new CommandLine(new Args()).setCommandName(OTHER).getCommandName());
    }

    @Test
    public void test187GetCommandReturnsSubclassName() {
        @Command(name = "parent") class Parent { }
        @Command(name = "child")  class Child extends Parent { }
        assertEquals("child", new CommandLine(new Child()).getCommandName());
    }

    @Test
    public void testIssue203InconsistentExceptions() {
        class Example {
            @Option(names = {"-h", "--help"}, help = true, // NOTE: this should be usageHelp = true
                    description = "Displays this help message and quits.")
            private boolean helpRequested;

            @Option(names = {"-o", "--out-dir"}, required = true, description = "The output directory"
                    /*usageHelp = true, NOTE: I'm guessing this usageHelp=true was a copy-paste mistake. */ )
            private File outputDir;

            @Parameters(arity = "1..*", description = "The input files")
            private File[] inputFiles;
        }

        try {
            // Comment from AshwinJay : "Should've failed as inputFiles were not provided".
            //
            // RP: After removing `usageHelp = true`, the "-o /tmp" argument is parsed as '-o'
            // with attached option value ' /tmp' (note the leading space).
            // A MissingParameterException is thrown for the missing <inputFiles>, as expected.
            new CommandLine(new Example()).parse("-o /tmp");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters at positions 0..*: <inputFiles>", ex.getMessage());
        }
        try {
            // Comment from AshwinJay : "Should've failed as inputFiles were not provided"
            //
            // RP: After removing `usageHelp = true`, the ["-o", " /tmp"] arguments are parsed and
            // a MissingParameterException is thrown for the missing <inputFiles>, as expected.
            new CommandLine(new Example()).parse("-o", " /tmp");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters at positions 0..*: <inputFiles>", ex.getMessage());
        }
        try {
            // a MissingParameterException is thrown for missing required option -o, as expected
            new CommandLine(new Example()).parse("inputfile1", "inputfile2");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '-o=<outputDir>'", ex.getMessage());
        }

        // a single empty string parameter was specified: this becomes an <inputFile> value
        try {
            new CommandLine(new Example()).parse("");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '-o=<outputDir>'", ex.getMessage());
        }

        // no parameters were specified
        try {
            new CommandLine(new Example()).parse();
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required options [-o=<outputDir>, params[*]=<inputFiles>]", ex.getMessage());
        }

        // finally, let's test the success scenario
        Example example = new Example();
        new CommandLine(example).parse("-o", "/tmp","inputfile1", "inputfile2");
        assertEquals(new File("/tmp"), example.outputDir);
        assertEquals(2, example.inputFiles.length);
        assertEquals(new File("inputfile1"), example.inputFiles[0]);
        assertEquals(new File("inputfile2"), example.inputFiles[1]);
    }
    @Test
    public void testIssue207ParameterExceptionProvidesAccessToFailedCommand_Programmatic() {
        class Top {
            @Option(names = "-o", required = true) String option;
        }
        class Sub1 {
            @Option(names = "-x", required = true) String x;
        }
        class Sub2 {
            @Option(names = "-y", required = true) String y;
        }
        try {
            new CommandLine(new Top()).
                    addSubcommand("sub1", new Sub1()).
                    addSubcommand("sub2", new Sub2()).
                    parse("sub1 -x abc".split(" "));
        } catch (ParameterException ex) {
            assertTrue(ex.getCommandLine().getCommand() instanceof Top);
        }
        try {
            new CommandLine(new Top()).
                    addSubcommand("sub1", new Sub1()).
                    addSubcommand("sub2", new Sub2()).
                    parse("-o OPT sub1 -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(ex.getCommandLine().getCommand() instanceof Sub1);
        }
        try {
            new CommandLine(new Top()).
                    addSubcommand("sub1", new Sub1()).
                    addSubcommand("sub2", new Sub2()).
                    parse("-o OPT sub2 -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(ex.getCommandLine().getCommand() instanceof Sub2);
        }
        List<CommandLine> parsed = new CommandLine(new Top()).
                addSubcommand("sub1", new Sub1()).
                addSubcommand("sub2", new Sub2()).
                parse("-o OPT sub1 -x ABC".split(" "));
        assertEquals(2, parsed.size());
        assertEquals("OPT", ((Top) parsed.get(0).getCommand()).option);
        assertEquals("ABC", ((Sub1) parsed.get(1).getCommand()).x);
    }
    @Command(name = "sub207A")
    private static class Sub207A { @Option(names = "-x", required = true) String x;  }
    @Command(name = "sub207B")
    private static class Sub207B { @Option(names = "-y", required = true) String y;  }
    @Test
    public void testIssue207ParameterExceptionProvidesAccessToFailedCommand_Declarative() {
        @Command(subcommands = {Sub207A.class, Sub207B.class})
        class Top {
            @Option(names = "-o", required = true) String option;
        }
        try {
            new CommandLine(new Top()).parse("sub207A -x abc".split(" "));
        } catch (ParameterException ex) {
            assertTrue(ex.getCommandLine().getCommand() instanceof Top);
        }
        try {
            new CommandLine(new Top()).parse("-o OPT sub207A -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(ex.getCommandLine().getCommand() instanceof Sub207A);
        }
        try {
            new CommandLine(new Top()).parse("-o OPT sub207B -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(ex.getCommandLine().getCommand() instanceof Sub207B);
        }
        List<CommandLine> parsed = new CommandLine(new Top()).
                parse("-o OPT sub207A -x ABC".split(" "));
        assertEquals(2, parsed.size());
        assertEquals("OPT", ((Top) parsed.get(0).getCommand()).option);
        assertEquals("ABC", ((Sub207A) parsed.get(1).getCommand()).x);
    }
    @Test
    public void testIssue226EmptyStackWithClusteredOptions() {
        class Options {
            @Option(names = "-b")
            private boolean buffered = false;

            @Option(names = "-o")
            private boolean overwriteOutput = true;

            @Option(names = "-v")
            private boolean verbose = false;
        }
        Options options = CommandLine.populateCommand(new Options(), "-bov");
        assertTrue(options.buffered);
        assertFalse(options.overwriteOutput);
        assertTrue(options.verbose);
    }
}
