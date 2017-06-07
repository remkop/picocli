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
import org.junit.Test;

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    @Test
    public void testVersion() {
        assertEquals("0.9.7-SNAPSHOT", CommandLine.VERSION);
    }

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
            assertEquals("Could not convert '0x1F' to byte for option '-byte'", expected.getMessage());
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
            assertEquals("Could not convert '0xFF' to short for option '-short'", expected.getMessage());
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
            assertEquals("Could not convert '0xFF' to int for option '-int'", expected.getMessage());
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
            assertEquals("Could not convert '0xAABBCC' to long for option '-long'", expected.getMessage());
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
    @Test(expected = MissingParameterException.class)
    public void testSingleValueFieldDefaultMinArityIs1() {
        CommandLine.populateCommand(new SupportedTypes(),  "-Long");
    }
    @Test
    public void testSingleValueFieldDefaultMinArityIsOne() {
        try {
            CommandLine.populateCommand(new SupportedTypes(),  "-Long", "-boolean");
            fail("should fail");
        } catch (ParameterException ex) {
            assertEquals("Could not convert '-boolean' to Long for option '-Long'", ex.getMessage());
        }
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
            CommandLine.populateCommand(new SupportedTypes(), option, value);
            fail("Invalid format " + value + " was accepted for " + option);
        } catch (ParameterException expected) {
            String type = option.substring(1);
            assertEquals("Could not convert '" + value + "' to " + type
                    + " for option '" + option + "'", expected.getMessage());
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

    static class EnumParams {
        @Option(names = "-timeUnit") TimeUnit timeUnit;
        @Option(names = "-timeUnitArray", arity = "2") TimeUnit[] timeUnitArray;
        @Option(names = "-timeUnitList", type = TimeUnit.class, arity = "3") List<TimeUnit> timeUnitList;
    }
    @Test
    public void testEnumTypeConversionSuceedsForValidInput() {
        EnumParams params = CommandLine.populateCommand(new EnumParams(),
                "-timeUnit DAYS -timeUnitArray HOURS MINUTES -timeUnitList SECONDS MICROSECONDS NANOSECONDS".split(" "));
        assertEquals(DAYS, params.timeUnit);
        assertArrayEquals(new TimeUnit[]{HOURS, TimeUnit.MINUTES}, params.timeUnitArray);
        List<TimeUnit> expected = new ArrayList<TimeUnit>(Arrays.asList(TimeUnit.SECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS));
        assertEquals(expected, params.timeUnitList);
    }
    @Test
    public void testEnumTypeConversionFailsForInvalidInput() {
        try {
            CommandLine.populateCommand(new EnumParams(), "-timeUnit", "xyz");
            fail("Accepted invalid timeunit");
        } catch (Exception ex) {
            assertEquals("Could not convert 'xyz' to TimeUnit for option '-timeUnit'", ex.getMessage());
        }
    }
    @Ignore("Requires #14 case-insensitive enum parsing")
    @Test
    public void testEnumTypeConversionIsCaseInsensitive() {
        EnumParams params = CommandLine.populateCommand(new EnumParams(),
                "-timeUnit daYS -timeUnitArray hours miNutEs -timeUnitList SEConds MiCROsEconds nanoSEConds".split(" "));
        assertEquals(DAYS, params.timeUnit);
        assertArrayEquals(new TimeUnit[]{HOURS, TimeUnit.MINUTES}, params.timeUnitArray);
        List<TimeUnit> expected = new ArrayList<TimeUnit>(Arrays.asList(TimeUnit.SECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS));
        assertEquals(expected, params.timeUnitList);
    }
    @Test
    public void testEnumArrayTypeConversionFailsForInvalidInput() {
        try {
            CommandLine.populateCommand(new EnumParams(), "-timeUnitArray", "a", "b");
            fail("Accepted invalid timeunit");
        } catch (Exception ex) {
            assertEquals("Could not convert 'a' to TimeUnit[] for option '-timeUnitArray' at index 0 (timeUnitArray)", ex.getMessage());
        }
    }
    @Test
    public void testEnumListTypeConversionFailsForInvalidInput() {
        try {
            CommandLine.populateCommand(new EnumParams(), "-timeUnitList", "DAYS", "b", "c");
            fail("Accepted invalid timeunit");
        } catch (Exception ex) {
            assertEquals("Could not convert 'b' to TimeUnit for option '-timeUnitList' at index 1 (timeUnitList)", ex.getMessage());
        }
    }

    @Test
    public void testArrayOptionParametersAreAlwaysInstantiated() {
        EnumParams params = new EnumParams();
        TimeUnit[] array = params.timeUnitArray;
        new CommandLine(params).parse("-timeUnitArray", "DAYS", "HOURS");
        assertNotSame(array, params.timeUnitArray);
    }
    @Test
    public void testListOptionParametersAreInstantiatedIfNull() {
        EnumParams params = new EnumParams();
        assertNull(params.timeUnitList);
        new CommandLine(params).parse("-timeUnitList", "DAYS", "HOURS", "DAYS");
        assertEquals(Arrays.asList(DAYS, HOURS, DAYS), params.timeUnitList);
    }
    @Test
    public void testListOptionParametersAreReusedInstantiatedIfNonNull() {
        EnumParams params = new EnumParams();
        List<TimeUnit> list = new ArrayList<TimeUnit>();
        params.timeUnitList = list;
        new CommandLine(params).parse("-timeUnitList", "DAYS", "HOURS", "DAYS");
        assertEquals(Arrays.asList(DAYS, HOURS, DAYS), params.timeUnitList);
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
    class ListPositionalParams {
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

    @Test(expected = ParameterException.class)
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
        @Option(names = {"-h", "--help", "-?", "/?"}, help = true) boolean isHelpRequested;
        @Option(names = "--required", required = true) private String required;
        @Parameters private String[] remainder;
    }
    @Test
    public void testErrorIfRequiredOptionNotSpecified() {
        try {
            CommandLine.populateCommand(new RequiredField(), "arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option 'required'", ex.getMessage());
        }
    }
    @Test
    public void testNoErrorIfRequiredOptionSpecified() {
        CommandLine.populateCommand(new RequiredField(), "--required", "arg1", "arg2");
    }
    @Test
    public void testNoErrorIfRequiredOptionNotSpecifiedWhenHelpRequested() {
        RequiredField requiredField = CommandLine.populateCommand(new RequiredField(), "--help");
        assertTrue("help requested", requiredField.isHelpRequested);
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
            assertEquals("Missing required parameter: mandatory", ex.getMessage());
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
            assertEquals("Missing required parameters: mandatory, anotherMandatory", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new Tricky1(), new String[] {"firstonly"});
            fail("Should not accept missing mandatory parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: anotherMandatory", ex.getMessage());
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
            assertEquals("Missing required parameter: mandatory", ex.getMessage());
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
            assertEquals("Missing required parameter: alsoMandatory", ex.getMessage());
        }

        try {
            CommandLine.populateCommand(new Tricky3(), new String[] { "-t", "-v"});
            fail("Should not accept missing two mandatory parameters");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameters: mandatory, alsoMandatory", ex.getMessage());
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
            assertEquals("Missing required parameter: mandatory", ex.getMessage());
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
            assertEquals("Missing required parameters: host, port", ex.getMessage());
        }
    }
    @Test
    public void testHelpRequestedFlagResetWhenParsing_staticMethod() {
        RequiredField requiredField = CommandLine.populateCommand(new RequiredField(), "--help");
        assertTrue("help requested", requiredField.isHelpRequested);

        requiredField.isHelpRequested = false;

        // should throw error again on second pass (no help was requested here...)
        try {
            CommandLine.populateCommand(requiredField, "arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option 'required'", ex.getMessage());
        }
    }
    @Test
    public void testHelpRequestedFlagResetWhenParsing_instanceMethod() {
        RequiredField requiredField = new RequiredField();
        CommandLine commandLine = new CommandLine(requiredField);
        commandLine.parse("--help");
        assertTrue("help requested", requiredField.isHelpRequested);

        requiredField.isHelpRequested = false;

        // should throw error again on second pass (no help was requested here...)
        try {
            commandLine.parse("arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option 'required'", ex.getMessage());
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

        compact = CommandLine.populateCommand(new CompactFields(), "-rv p1 p2".split(" "));
        verifyCompact(compact, true, true, null, fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-voout p1 p2".split(" "));
        verifyCompact(compact, true, false, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-voout -r p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-r -v -oout p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-oout -r -v p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        compact = CommandLine.populateCommand(new CompactFields(), "-rvo out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));

        try {
            CommandLine.populateCommand(new CompactFields(), "-oout -r -vp1 p2".split(" "));
            fail("should fail: -v does not take an argument");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [-p1, p2]", ex.getMessage());
        }
    }

    @Test
    public void testCompactFieldsWithUnmatchedArguments() {
        CommandLine cmd = new CommandLine(new CompactFields()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("-oout -r -vp1 p2".split(" "));
        assertEquals(Arrays.asList("-p1", "p2"), cmd.getUnmatchedArguments());
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
    public void testOptionsAfterParamAreInterpretedAsParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-r -v p1 -o out p2".split(" "));
        verifyCompact(compact, true, true, null, fileArray("p1", "-o", "out", "p2"));
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueAssignsEmptyStringEvenIfNotLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-ro= -v".split(" "));
        verifyCompact(compact, true, true, "", null);
    }
    @Test
    public void testShortOptionsWithColonSeparatorButNoValueAssignsEmptyStringEvenIfNotLast() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setSeparator(":");
        cmd.parse("-ro: -v".split(" "));
        verifyCompact(compact, true, true, "", null);
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueAssignsEmptyStringIfLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvo=".split(" "));
        verifyCompact(compact, true, true, "", null);
    }


    @Test
    public void testDoubleDashSeparatesPositionalParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-oout -- -r -v p1 p2".split(" "));
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
    @Test
    public void testArityForOption_booleanFieldImplicitArity0() throws Exception {
        Range arity = Range.optionArity(SupportedTypes.class.getDeclaredField("booleanField"));
        assertEquals(Range.valueOf("0"), arity);
        assertEquals("0", arity.toString());
    }
    @Test
    public void testArityForOption_intFieldImplicitArity1() throws Exception {
        Range arity = Range.optionArity(SupportedTypes.class.getDeclaredField("intField"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForOption_isExplicitlyDeclaredValue() throws Exception {
        Range arity = Range.optionArity(EnumParams.class.getDeclaredField("timeUnitList"));
        assertEquals(Range.valueOf("3"), arity);
        assertEquals("3", arity.toString());
    }
    @Test
    public void testArityForOption_listFieldImplicitArity0_n() throws Exception {
        class ImplicitList { @Option(names = "-a") List<Integer> listIntegers; }
        Range arity = Range.optionArity(ImplicitList.class.getDeclaredField("listIntegers"));
        assertEquals(Range.valueOf("0..*"), arity);
        assertEquals("0..*", arity.toString());
    }
    @Test
    public void testArityForOption_arrayFieldImplicitArity0_n() throws Exception {
        class ImplicitList { @Option(names = "-a") int[] intArray; }
        Range arity = Range.optionArity(ImplicitList.class.getDeclaredField("intArray"));
        assertEquals(Range.valueOf("0..*"), arity);
        assertEquals("0..*", arity.toString());
    }
    @Test
    public void testArityForParameters_booleanFieldImplicitArity0() throws Exception {
        class ImplicitBoolField { @Parameters boolean boolSingleValue; }
        Range arity = Range.parameterArity(ImplicitBoolField.class.getDeclaredField("boolSingleValue"));
        assertEquals(Range.valueOf("0"), arity);
        assertEquals("0", arity.toString());
    }
    @Test
    public void testArityForParameters_intFieldImplicitArity1() throws Exception {
        class ImplicitSingleField { @Parameters int intSingleValue; }
        Range arity = Range.parameterArity(ImplicitSingleField.class.getDeclaredField("intSingleValue"));
        assertEquals(Range.valueOf("1"), arity);
        assertEquals("1", arity.toString());
    }
    @Test
    public void testArityForParameters_listFieldImplicitArity0_n() throws Exception {
        Range arity = Range.parameterArity(ListPositionalParams.class.getDeclaredField("list"));
        assertEquals(Range.valueOf("0..*"), arity);
        assertEquals("0..*", arity.toString());
    }
    @Test
    public void testArityForParameters_arrayFieldImplicitArity0_n() throws Exception {
        Range arity = Range.parameterArity(CompactFields.class.getDeclaredField("inputFiles"));
        assertEquals(Range.valueOf("0..*"), arity);
        assertEquals("0..*", arity.toString());
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
        assertFalse(params.vOrOther);
        assertArrayEquals(new String[]{ "123", "-other"}, params.params);
    }
    @Test
    public void testBooleanOptionsArity0_nFailsIfAttachedParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-bool=123 -other".split(" "));
            fail("was able to assign 123 to boolean");
        } catch (ParameterException ex) {
            assertEquals("'123' is not a boolean for option '-bool'", ex.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-rv234 -bool".split(" "));
            fail("Expected exception");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments [-234, -bool]", ok.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedParamNotABooleanWithUnmatchedArgsAllowed() { // ignores varargs
        CommandLine cmd = new CommandLine(new BooleanOptionsArity0_nAndParameters()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("-rv234 -bool".split(" "));
        assertEquals(Arrays.asList("-234", "-bool"), cmd.getUnmatchedArguments());
    }
    @Test
    public void testBooleanOptionsArity0_nShortFormFailsIfAttachedWithSepParamNotABoolean() { // ignores varargs
        try {
            CommandLine.populateCommand(new BooleanOptionsArity0_nAndParameters(), "-rv=234 -bool".split(" "));
            fail("was able to assign 234 to boolean");
        } catch (ParameterException ex) {
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
        } catch (ParameterException expected) {
            assertEquals("'abc' is not a boolean for option '-bool'", expected.getMessage());
        }
    }
    @Test
    public void testBooleanOptionsArity1_nErrorIfValueMissing() {
        try {
            CommandLine.populateCommand(new BooleanOptionsArity1_nAndParameters(), "-bool".split(" "));
            fail("Missing param was accepted for boolean with arity=1");
        } catch (ParameterException expected) {
            assertEquals("Missing required parameter for option '-bool' at index 0 (aBoolean)", expected.getMessage());
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
    }
    @Test
    public void testArrayOptionsWithArity2Consume2ArgumentsEvenIfFirstIsAttached() {
        ArrayOptionArity2AndParameters
                params = CommandLine.populateCommand(new ArrayOptionArity2AndParameters(), "-doubles=1.1 2.2 3.3 4.4".split(" "));
        assertArrayEquals(Arrays.toString(params.doubleOptions),
                new double[] {1.1, 2.2, }, params.doubleOptions, 0.000001);
        assertArrayEquals(new double[]{3.3, 4.4}, params.doubleParams, 0.000001);
    }

    @Test
    public void testArrayOptionWithoutArityConsumesAllArguments() {
        class OptionsNoArityAndParameters {
            @Parameters char[] charParams;
            @Option(names = "-chars") char[] charOptions;
        }
        OptionsNoArityAndParameters
                params = CommandLine.populateCommand(new OptionsNoArityAndParameters(), "-chars a b c d".split(" "));
        assertArrayEquals(Arrays.toString(params.charOptions),
                new char[] {'a', 'b', 'c', 'd'}, params.charOptions);
        assertArrayEquals(Arrays.toString(params.charParams), null, params.charParams);
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testMissingTypeConverter() {
        class MissingConverter {
            @Option(names = "--socket") Socket socket;
        }
        CommandLine.populateCommand(new MissingConverter(), "--socket anyString".split(" "));
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
            assertEquals("Missing required parameters at positions 0..*: params", ex.getMessage());
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
            assertEquals("positional parameter at index 0..* (params) requires at least 2 values, but only 1 were specified.", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new ArrayParamsArity2_n());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (params) requires at least 2 values, but only 0 were specified.", ex.getMessage());
        }
    }

    @Test
    public void testNonVarargArrayParametersWithNegativeArityConsumesZeroArguments() {
        class NonVarArgArrayParamsNegativeArity {
            @Parameters(arity = "-1")
            List<String> params;
        }
        NonVarArgArrayParamsNegativeArity params = CommandLine.populateCommand(new NonVarArgArrayParamsNegativeArity(), "a", "b", "c");
        assertEquals(Arrays.asList(), params.params);

        params = CommandLine.populateCommand(new NonVarArgArrayParamsNegativeArity(), "a");
        assertEquals(Arrays.asList(), params.params);

        params = CommandLine.populateCommand(new NonVarArgArrayParamsNegativeArity());
        assertEquals(null, params.params);
    }

    @Test
    public void testNonVarargArrayParametersWithArity0() {
        class NonVarArgArrayParamsZeroArity {
            @Parameters(arity = "0")
            List<String> params;
        }
        NonVarArgArrayParamsZeroArity params = CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity(), "a", "b", "c");
        assertEquals(new ArrayList<String>(), params.params);

        params = CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity(), "a");
        assertEquals(new ArrayList<String>(), params.params);

        params = CommandLine.populateCommand(new NonVarArgArrayParamsZeroArity());
        assertEquals(null, params.params);
    }

    @Test
    public void testNonVarargArrayParametersWithArity1() {
        class NonVarArgArrayParamsArity1 {
            @Parameters(arity = "1")
            List<String> params;
        }
        NonVarArgArrayParamsArity1 params = CommandLine.populateCommand(new NonVarArgArrayParamsArity1(), "a", "b", "c");
        assertEquals(Arrays.asList("a"), params.params);

        params = CommandLine.populateCommand(new NonVarArgArrayParamsArity1(), "a");
        assertEquals(Arrays.asList("a"), params.params);

        try {
            params = CommandLine.populateCommand(new NonVarArgArrayParamsArity1());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: params", ex.getMessage());
        }
    }

    @Test
    public void testNonVarargArrayParametersWithArity2() {
        class NonVarArgArrayParamsArity2 {
            @Parameters(arity = "2")
            List<String> params;
        }
        NonVarArgArrayParamsArity2 params = CommandLine.populateCommand(new NonVarArgArrayParamsArity2(), "a", "b", "c");
        assertEquals(Arrays.asList("a", "b"), params.params);

        try {
            params = CommandLine.populateCommand(new NonVarArgArrayParamsArity2(), "a");
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (params) requires at least 2 values, but only 1 were specified.", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new NonVarArgArrayParamsArity2());
            fail("Should not accept input with missing parameter");
        } catch (MissingParameterException ex) {
            assertEquals("positional parameter at index 0..* (params) requires at least 2 values, but only 0 were specified.", ex.getMessage());
        }
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
            assertEquals("Missing required parameters: param0, param1", ex.getMessage());
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
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [--opt=abc]", ok.getMessage());
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
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument [--opt=abc]", ok.getMessage());
        }
    }
    @Test
    public void testIfSeparatorSetTheDefaultSeparatorIsNotRecognizedWithUnmatchedArgsAllowed() {
        @Command(separator = ":")
        class App {
            @Option(names = "--opt", required = true) String opt;
        }
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        try {
            cmd.parse("--opt=abc");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required option 'opt'", ok.getMessage());
        }
    }
    @Test
    public void testGnuLongOptionsWithVariousSeparatorsOnlyAndNoValue() {
        VariousPrefixCharacters params;
        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash".split(" "));
            fail("int option needs arg");
        } catch (ParameterException ex) {
            assertEquals("Missing required parameter for option '-d' (dash)", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--owner".split(" "));
        } catch (ParameterException ex) {
            assertEquals("Missing required parameter for option '/Owner' (owner)", ex.getMessage());
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
            assertEquals("Could not convert '' to int for option '-d'", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash= /4".split(" "));
            fail("int option (with sep but no value, followed by other option) needs arg");
        } catch (ParameterException ex) {
            assertEquals("Could not convert '' to int for option '-d'", ex.getMessage());
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
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"a text\"", "\"another text\"", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t\"a text\"", "\"another text\"", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"", "\"another text\"", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);
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
            assertEquals("Option name '-p' is used by both picocli.CommandLineTest$2ParentOption.path and picocli.CommandLineTest$2ChildOption.text", ex.getMessage());
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
            assertEquals("Missing required parameter: file1", ex.getMessage());
        }
    }

    @Test
    public void testPositionalParamWithFixedIndexRange() {
        class App {
            @Parameters(index = "0..1") File file0_1;
            @Parameters(index = "1..2", type = File.class) List<File> fileList1_2;
            @Parameters(index = "0..3") File[] fileArray0_3 = new File[4];
            @Parameters List<String> all;
        }
        App app1 = CommandLine.populateCommand(new App(), "000", "111", "222", "333");
        assertEquals("field initialized with arg[0]", new File("000"), app1.file0_1);
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

        App app2 = CommandLine.populateCommand(new App(), "000", "111");
        assertEquals("field initialized with arg[0]", new File("000"), app2.file0_1);
        assertEquals("arg[1]", Arrays.asList(new File("111")), app2.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                null, null, null, null, // existing values
                new File("000"),
                new File("111"),}, app2.fileArray0_3);
        assertEquals("args", Arrays.asList("000", "111"), app2.all);

        App app3 = CommandLine.populateCommand(new App(), "000");
        assertEquals("field initialized with arg[0]", new File("000"), app3.file0_1);
        assertEquals("arg[1]", new ArrayList<File>(), app3.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                null, null, null, null, // existing values
                new File("000")}, app3.fileArray0_3);
        assertEquals("args", Arrays.asList("000"), app3.all);

        try {
            CommandLine.populateCommand(new App());
            fail("Should fail with missingParamException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: file0_1", ex.getMessage());
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
        assertArrayEquals(new String[]{"3333", "file1", "file2"}, app1.files);
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
        CommandLine cmd = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        cmd.parse("val0", "val1", "val2", "val3");
        assertArrayEquals(new String[]{"val0", "val1", "val2"}, ((SingleValue)cmd.getCommand()).str);
        assertEquals(Arrays.asList("val3"), cmd.getUnmatchedArguments());
    }

    @Test // TODO
    public void testPositionalParamSingleValueButWithoutIndex() throws Exception {
        class SingleValue {
            @Parameters String str;
        }
        SingleValue single = CommandLine.populateCommand(new SingleValue(),"val1", "val2");
        assertEquals("val1", single.str);
    }

    @Test
    public void testSplitInOptionArray() {
        class Args {
            @Option(names = "-a", split = ",") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C", "D,E,F");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C", "D", "E", "F"}, args.values);
    }

    @Test
    public void testSplitInOptionArrayWithSpaces() {
        class Args {
            @Option(names = "-a", split = " ") String[] values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=\"a b c\"");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a b c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "\"a b c\"", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=\"a b c\"", "B", "C", "D E F");
        assertArrayEquals(new String[] {"a", "b", "c", "B", "C", "D", "E", "F"}, args.values);
    }

    @Test
    public void testSplitInOptionArrayWithArity() {
        class Args {
            @Option(names = "-a", split = ",", arity = "0..4") String[] values;
            @Parameters() String[] params;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B"}, args.values);
        assertArrayEquals(new String[] {"C"}, args.params);

        args = CommandLine.populateCommand(new Args(), "-a", "a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B"}, args.values);
        assertArrayEquals(new String[] {"C"}, args.params);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C", "D,E,F");
        assertArrayEquals(new String[] {"a", "b", "c", "B"}, args.values);
        assertArrayEquals(new String[] {"C", "D,E,F"}, args.params);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c,d,e", "B", "C", "D,E,F");
        assertArrayEquals(new String[] {"a", "b", "c", "d"}, args.values);
        assertArrayEquals(new String[] {"e", "B", "C", "D,E,F"}, args.params);
    }

    @Test
    public void testSplitInOptionCollection() {
        class Args {
            @Option(names = "-a", split = ",") List<String> values;
        }
        Args args = CommandLine.populateCommand(new Args(), "-a=a,b,c");
        assertEquals(Arrays.asList("a", "b", "c"), args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C"), args.values);

        args = CommandLine.populateCommand(new Args(), "-a", "a,b,c", "B", "C");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C"), args.values);

        args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "C", "D,E,F");
        assertEquals(Arrays.asList("a", "b", "c", "B", "C", "D", "E", "F"), args.values);
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
        Args args = CommandLine.populateCommand(new Args(), "a,b,c");
        assertArrayEquals(new String[] {"a", "b", "c"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B", "C");
        assertArrayEquals(new String[] {"a", "b", "c", "B"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b,c", "B,C");
        assertArrayEquals(new String[] {"a", "b", "c", "B"}, args.values);

        args = CommandLine.populateCommand(new Args(), "a,b", "A,B,C");
        assertArrayEquals(new String[] {"a", "b", "A", "B"}, args.values);
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
        @Override public CustomType convert(String value) { return new CustomType(value); }
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
            assertEquals("Unmatched arguments [-b, cmd1]", ex.getMessage());
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
        List<CommandLine> result1 = createNestedCommand().setUnmatchedArgumentsAllowed(true)
                .parse("-a", "-b", "cmd1");
        assertEquals(Arrays.asList("-b", "cmd1"), result1.get(0).getUnmatchedArguments());

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
                "Could not convert 'not a number' to int for option '-number'%n" +
                "Usage: <main class> [-number=<number>]%n" +
                "      -number=<number>%n"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunRequiresAnnotatedCommand() {
        class App implements Runnable {
            public void run() { }
        }
        CommandLine.run(new App(), System.err);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPopulateCommandRequiresAnnotatedCommand() {
        class App { }
        CommandLine.populateCommand(new App());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUsageObjectPrintstreamRequiresAnnotatedCommand() {
        class App { }
        CommandLine.usage(new App(), System.out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUsageObjectPrintstreamAnsiRequiresAnnotatedCommand() {
        class App { }
        CommandLine.usage(new App(), System.out, Help.Ansi.OFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUsageObjectPrintstreamColorschemeRequiresAnnotatedCommand() {
        class App { }
        CommandLine.usage(new App(), System.out, Help.defaultColorScheme(Help.Ansi.OFF));
    }

    @Test(expected = IllegalArgumentException.class)
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
            assertEquals("option '-s' (string) should be specified only once", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "-v");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-v' (bool) should be specified only once", ex.getMessage());
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
            assertEquals("option '-s' (string) should be specified only once", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "--verbose");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '-v' (bool) should be specified only once", ex.getMessage());
        }
    }

    @Test
    public void testOverwrittenOptionSetsLastValueIfAllowed() {
        class App {
            @Option(names = {"-s", "--str"})      String string;
            @Option(names = {"-v", "--verbose"}) boolean bool;
        }
        CommandLine commandLine = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-s", "1", "--str", "2");
        assertEquals("2", ((App) commandLine.getCommand()).string);

        commandLine = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-v", "--verbose");
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
            assertEquals("Missing required option 'password'", ex.getLocalizedMessage());
        }
        commandLine.parse("-u", "foo", "-p", "abc");
    }
}
