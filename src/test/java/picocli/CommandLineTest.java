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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.IHelpSectionRenderer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Range;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.DuplicateOptionAnnotationsException;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.HelpCommand;
import static picocli.CommandLine.ITypeConverter;
import static picocli.CommandLine.InitializationException;
import static picocli.CommandLine.MissingParameterException;
import static picocli.CommandLine.MissingTypeConverterException;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.OverwrittenOptionException;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.ParameterIndexGapException;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.ParseResult;
import static picocli.CommandLine.Unmatched;
import static picocli.CommandLine.UnmatchedArgumentException;
import static picocli.TestUtil.setTraceLevel;
import static picocli.TestUtil.stripAnsiTrace;
import static picocli.TestUtil.stripHashcodes;

/**
 * Tests for the CommandLine argument parsing interpreter functionality.
 */
// TODO arity ignored for single-value types (non-array, non-collection)
// TODO document that if arity>1 and args="-opt=val1 val2", arity overrules the "=": both values are assigned
// TODO test superclass bean and child class bean where child class field shadows super class and have same annotation Option name
// TODO test superclass bean and child class bean where child class field shadows super class and have different annotation Option name
public class CommandLineTest {

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Before
    public void setUp() {
        System.clearProperty("picocli.trace");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullObject() {
        new CommandLine(null);
    }
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullFactory() {
        new CommandLine(new CompactFields(), null);
    }
    static class BadConverter implements ITypeConverter<Long> {
        public BadConverter() {
            throw new IllegalStateException("bad class");
        }
        public Long convert(String value) throws Exception { return null; }
    }
    @Test
    public void testFailingConverterWithDefaultFactory() {
        class App {
            @Option(names = "-x", converter = BadConverter.class) long bad;
        }
        try {
            new CommandLine(new App());
        } catch (InitializationException ex) {
            assertEquals("Could not instantiate class " +
                    "picocli.CommandLineTest$BadConverter: java.lang.reflect.InvocationTargetException", ex.getMessage());
        }
    }
    @Test
    public void testVersion() {
        assertEquals("4.7.6-SNAPSHOT", CommandLine.VERSION);
    }
    @Test
    public void testArrayPositionalParametersAreReplacedNotAppendedTo() {
        class ArrayPositionalParams {
            @Parameters() int[] array;
        }
        ArrayPositionalParams params = new ArrayPositionalParams();
        params.array = new int[3];
        int[] array = params.array;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(array, params.array);
        assertArrayEquals(new int[]{3, 2, 1}, params.array);
    }
    @Test
    public void testArrayPositionalParametersAreFilledWithValues() {
        @Command
        class ArrayPositionalParams {
            String string;
            @Parameters()
            void setString( String[] array) {
                StringBuilder sb = new StringBuilder();
                for (String s : array) { sb.append(s); }
                string = sb.toString();
            }
        }
        ArrayPositionalParams params = new ArrayPositionalParams();
        new CommandLine(params).parseArgs("foo", "bar", "baz");
        assertEquals("foobarbaz", params.string);
    }
    @Test
    public void testArrayOptionsAreFilledWithValues() {
        @Command
        class ArrayPositionalParams {
            String string;
            @Option(names="-s")
            void setString( String[] array) {
                StringBuilder sb = new StringBuilder();
                for (String s : array) { sb.append(s); }
                string = sb.toString();
            }
        }
        ArrayPositionalParams params = new ArrayPositionalParams();
        new CommandLine(params).parseArgs("-s", "foo", "-s", "bar", "-s", "baz");
        assertEquals("foobarbaz", params.string);
    }
    private static class ListPositionalParams {
        @Parameters(type = Integer.class) List<Integer> list;
    }
    @Test
    public void testListPositionalParametersAreInstantiatedIfNull() {
        ListPositionalParams params = new ListPositionalParams();
        assertNull(params.list);
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotNull(params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }
    @Test
    public void testListPositionalParametersAreReusedIfNonNull() {
        ListPositionalParams params = new ListPositionalParams();
        params.list = new ArrayList<Integer>();
        List<Integer> list = params.list;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }
    @Test
    public void testListPositionalParametersAreReplacedIfNonNull() {
        ListPositionalParams params = new ListPositionalParams();
        params.list = new ArrayList<Integer>();
        params.list.add(234);
        List<Integer> list = params.list;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }
    @Test
    public void testListPositionalParametersAreClearedOnReuse() {
        ListPositionalParams params = new ListPositionalParams();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertEquals(Arrays.asList(3, 2, 1), params.list);

        cmd.parseArgs();
        assertNull(params.list);
    }
    private static class PositionalParamsListWithInitialValue {
        @Parameters(type = Integer.class) List<Integer> list = new ArrayList<Integer>();
    }
    @Test
    public void testListPositionalParametersWithInitialValueAreReusedIfNonNull() {
        PositionalParamsListWithInitialValue params = new PositionalParamsListWithInitialValue();
        params.list = new ArrayList<Integer>();
        List<Integer> list = params.list;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }
    @Test
    public void testListPositionalParametersWithInitialValueAreReplacedIfNonNull() {
        PositionalParamsListWithInitialValue params = new PositionalParamsListWithInitialValue();
        params.list = new ArrayList<Integer>();
        params.list.add(234);
        List<Integer> list = params.list;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
    }

//    @Ignore("Requires https://github.com/remkop/picocli/issues/995")
    @Test
    public void testIssue995ListPositionalParametersWithInitialValueAreClearedOnReuse() {
        PositionalParamsListWithInitialValue params = new PositionalParamsListWithInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertEquals(Arrays.asList(3, 2, 1), params.list);

        cmd.parseArgs("4", "5", "6");
        assertEquals(Arrays.asList(4, 5, 6), params.list);

        /* Related to https://github.com/remkop/picocli/issues/990

        Picocli currently clears (actually creates a new instance of) collections and arrays:
        see comment "existing values are default values if `initialized` does NOT contain argsSpec"  ::applyValuesToArrayField

        But only if the user actually specifies a value...
        The original default value is not preserved and applied if the user input omits the option/positional.
         */
        cmd.parseArgs();
        assertEquals(Collections.emptyList(), params.list);

        params.list = new ArrayList<Integer>();
        params.list.add(233);
        params.list.add(666);
        cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertEquals(Arrays.asList(3, 2, 1), params.list);

        cmd.parseArgs();
        assertEquals(Arrays.asList(233,666), params.list);
    }

    static class PositionalParamsListWithNonEmptyInitialValue {
        @Parameters
        List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 4, 5));
    }
    @Test
    public void testIssue995PositionalParamsListWithNonEmptyInitialValueAreResetOnReuse() {
        PositionalParamsListWithNonEmptyInitialValue params = new PositionalParamsListWithNonEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertEquals(Arrays.asList(3, 2, 1), params.list);

        cmd.parseArgs(); // should be reset to original initial value
        assertEquals(Arrays.asList(3, 4, 5), params.list);
    }

    static class PositionalParametersArrayWithEmptyInitialValue {
        @Parameters
        int[] array = new int[0];
    }
    @Test
    public void testIssue995PositionalParametersArrayWithEmptyInitialValueAreResetOnReuse() {
        PositionalParametersArrayWithEmptyInitialValue params = new PositionalParametersArrayWithEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertArrayEquals(new int[] {3, 2, 1}, params.array);

        cmd.parseArgs(); // should be reset to original empty initial value
        assertArrayEquals(new int[0], params.array);
    }

    static class PositionalParametersArrayWithNonEmptyInitialValue {
        @Parameters
        int[] array = new int[] {3, 4, 5};
    }
    @Test
    public void testIssue995PositionalParametersArrayWithNonEmptyInitialValueAreResetOnReuse() {
        PositionalParametersArrayWithNonEmptyInitialValue params = new PositionalParametersArrayWithNonEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertArrayEquals(new int[] {3, 2, 1}, params.array);

        cmd.parseArgs(); // should be reset to original initial value
        assertArrayEquals(new int[] {3, 4, 5}, params.array);
    }

    static class PositionalParametersSetWithEmptyInitialValue {
        @Parameters
        Set<Integer> set = new TreeSet<Integer>();
    }
    //@Ignore("Requires https://github.com/remkop/picocli/issues/995")
    @Test
    public void testIssue995PositionalParametersSetWithEmptyInitialValueAreResetOnReuse() {
        PositionalParametersSetWithEmptyInitialValue params = new PositionalParametersSetWithEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertEquals(new HashSet<Integer>(Arrays.asList(3, 2, 1)), params.set);

        cmd.parseArgs(); // should be reset to original empty initial value
        assertEquals(new HashSet<Integer>(), params.set);
    }

    static class PositionalParametersSetWithNonEmptyInitialValue {
        @Parameters
        Set<Integer> set = new TreeSet<Integer>(Arrays.asList(3, 4, 5));
    }
    @Test
    public void testIssue995PositionalParametersSetWithNonEmptyInitialValueAreResetOnReuse() {
        PositionalParametersSetWithNonEmptyInitialValue params = new PositionalParametersSetWithNonEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("3", "2", "1");
        assertEquals(new HashSet<Integer>(Arrays.asList(3, 2, 1)), params.set);

        cmd.parseArgs(); // should be reset to original initial value
        assertEquals(new HashSet<Integer>(Arrays.asList(3, 4, 5)), params.set);
    }

    static class PositionalParametersEnumSetWithEmptyInitialValue {
        @Parameters
        EnumSet<TimeUnit> enumSet = EnumSet.noneOf(TimeUnit.class);
    }
    //@Ignore("Requires https://github.com/remkop/picocli/issues/995")
    @Test
    public void testIssue995PositionalParametersEnumSetWithEmptyInitialValueAreResetOnReuse() {
        PositionalParametersEnumSetWithEmptyInitialValue params = new PositionalParametersEnumSetWithEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("MILLISECONDS", "SECONDS");
        assertEquals(EnumSet.of(TimeUnit.MILLISECONDS, TimeUnit.SECONDS), params.enumSet);

        cmd.parseArgs(); // should be reset to original empty initial value
        assertEquals(EnumSet.noneOf(TimeUnit.class), params.enumSet);
    }

    static class PositionalParametersEnumSetWithNonEmptyInitialValue {
        @Parameters
        EnumSet<TimeUnit> enumSet = EnumSet.of(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS);
    }
    @Test
    public void testIssue995PositionalParametersEnumSetWithNonEmptyInitialValueAreResetOnReuse() {
        PositionalParametersEnumSetWithNonEmptyInitialValue params = new PositionalParametersEnumSetWithNonEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("MILLISECONDS", "SECONDS");
        assertEquals(EnumSet.of(TimeUnit.MILLISECONDS, TimeUnit.SECONDS), params.enumSet);

        cmd.parseArgs(); // should be reset to original initial value
        assertEquals(EnumSet.of(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS), params.enumSet);
    }

    static class PositionalParametersMapWithEmptyInitialValue {
        @Parameters
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
    }
    //@Ignore("Requires https://github.com/remkop/picocli/issues/995")
    @Test
    public void testIssue995PositionalParametersMapWithEmptyInitialValueAreResetOnReuse() {
        PositionalParametersMapWithEmptyInitialValue params = new PositionalParametersMapWithEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("1=1", "2=2", "3=3");
        assertEquals(TestUtil.mapOf(1, 1, 2, 2, 3, 3), params.map);

        cmd.parseArgs(); // should be reset to original initial value
        assertEquals(new LinkedHashMap<Integer, Integer>(), params.map);
    }

    static class PositionalParametersMapWithNonEmptyInitialValue {
        @Parameters
        Map<Integer, Integer> map = TestUtil.mapOf(5, 5, 6, 6, 7, 7);
    }
    @Test
    public void testIssue995PositionalParametersMapWithNonEmptyInitialValueAreResetOnReuse() {
        PositionalParametersMapWithNonEmptyInitialValue params = new PositionalParametersMapWithNonEmptyInitialValue();
        CommandLine cmd = new CommandLine(params);

        cmd.parseArgs("1=1", "2=2", "3=3");
        assertEquals(TestUtil.mapOf(1, 1, 2, 2, 3, 3), params.map);

        cmd.parseArgs(); // should be reset to original initial value
        assertEquals(TestUtil.mapOf(5, 5, 6, 6, 7, 7), params.map);
    }

    static class SortedSetPositionalParams {
        @Parameters(type = Integer.class) SortedSet<Integer> sortedSet;
    }
    @Test
    public void testSortedSetPositionalParametersAreInstantiatedIfNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        assertNull(params.sortedSet);
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotNull(params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<Integer>(params.sortedSet));
    }
    @Test
    public void testSortedSetPositionalParametersAreReusedIfNonNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        params.sortedSet = new TreeSet<Integer>();
        SortedSet<Integer> list = params.sortedSet;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<Integer>(params.sortedSet));
    }
    @Test
    public void testSortedSetPositionalParametersAreReplacedIfNonNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        params.sortedSet = new TreeSet<Integer>();
        params.sortedSet.add(234);
        SortedSet<Integer> list = params.sortedSet;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<Integer>(params.sortedSet));
    }
    static class SetPositionalParams {
        @Parameters(type = Integer.class) Set<Integer> set;
    }
    @Test
    public void testSetPositionalParametersAreInstantiatedIfNull() {
        SetPositionalParams params = new SetPositionalParams();
        assertNull(params.set);
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotNull(params.set);
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 3)), params.set);
    }
    @Test
    public void testSetPositionalParametersAreReusedIfNonNull() {
        SetPositionalParams params = new SetPositionalParams();
        params.set = new TreeSet<Integer>();
        Set<Integer> list = params.set;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.set);
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2, 3)), params.set);
    }
    @Test
    public void testSetPositionalParametersAreReplacedIfNonNull() {
        SetPositionalParams params = new SetPositionalParams();
        params.set = new TreeSet<Integer>();
        params.set.add(234);
        Set<Integer> list = params.set;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.set);
        assertEquals(new HashSet<Integer>(Arrays.asList(3, 2, 1)), params.set);
    }
    static class QueuePositionalParams {
        @Parameters(type = Integer.class) Queue<Integer> queue;
    }
    @Test
    public void testQueuePositionalParametersAreInstantiatedIfNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        assertNull(params.queue);
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotNull(params.queue);
        assertEquals(new LinkedList<Integer>(Arrays.asList(3, 2, 1)), params.queue);
    }
    @Test
    public void testQueuePositionalParametersAreReusedIfNonNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        params.queue = new LinkedList<Integer>();
        Queue<Integer> list = params.queue;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.queue);
        assertEquals(new LinkedList<Integer>(Arrays.asList(3, 2, 1)), params.queue);
    }
    @Test
    public void testQueuePositionalParametersAreReplacedIfNonNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        params.queue = new LinkedList<Integer>();
        params.queue.add(234);
        Queue<Integer> list = params.queue;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.queue);
        assertEquals(new LinkedList<Integer>(Arrays.asList(3, 2, 1)), params.queue);
    }
    static class CollectionPositionalParams {
        @Parameters(type = Integer.class) Collection<Integer> collection;
    }
    @Test
    public void testCollectionPositionalParametersAreInstantiatedIfNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        assertNull(params.collection);
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotNull(params.collection);
        assertEquals(Arrays.asList(3, 2, 1), params.collection);
    }
    @Test
    public void testCollectionPositionalParametersAreReusedIfNonNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        params.collection = new ArrayList<Integer>();
        Collection<Integer> list = params.collection;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.collection);
        assertEquals(Arrays.asList(3, 2, 1), params.collection);
    }
    @Test
    public void testCollectionPositionalParametersAreReplacedIfNonNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        params.collection = new ArrayList<Integer>();
        params.collection.add(234);
        Collection<Integer> list = params.collection;
        new CommandLine(params).parseArgs("3", "2", "1");
        assertNotSame(list, params.collection);
        assertEquals(Arrays.asList(3, 2, 1), params.collection);
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
    @Test(expected = InitializationException.class)
    public void testPopulateRejectsPrivateFinalFields() {
        CommandLine.populateCommand(new PrivateFinalOptionFields(), "-f", "reference value");
    }
    @Test(expected = InitializationException.class)
    public void testConstructorRejectsPrivateFinalFields() {
        new CommandLine(new PrivateFinalOptionFields());
    }
    @Test
    public void testLastValueSelectedIfOptionSpecifiedMultipleTimes() {
        class App {
            @Option(names = "-f") String field = null;
            @Option(names = "-p") int primitive = 43;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        cmd.parseArgs("-f", "111", "-f", "222");
        App ff = cmd.getCommand();
        assertEquals("222", ff.field);
    }

    private static class PrivateFinalParameterFields {
        @Parameters(index = "0") private final String field = null;
        @Parameters(index = "1", arity = "0..1") private final int primitive = 43;
    }
    @Test(expected = InitializationException.class)
    public void testPopulateRejectsInitializePrivateFinalParameterFields() {
        CommandLine.populateCommand(new PrivateFinalParameterFields(), "ref value");
    }
    @Test(expected = InitializationException.class)
    public void testConstructorRejectsPrivateFinalPrimitiveParameterFields() {
        new CommandLine(new PrivateFinalParameterFields());
    }

    private static class PrivateFinalAllowedFields {
        @Option(names = "-d") private final Date date = null;
        @Option(names = "-u") private final TimeUnit enumValue = TimeUnit.SECONDS;
        @Parameters(index = "0") private final Integer integer = null;
        @Parameters(index = "1") private final Long longValue = Long.valueOf(9876L);
    }
    @Test
    public void testPrivateFinalNonPrimitiveNonStringFieldsAreAllowed() throws Exception {
        PrivateFinalAllowedFields fields = new PrivateFinalAllowedFields();
        new CommandLine(fields).parseArgs("-d=2017-11-02", "-u=MILLISECONDS", "123", "123456");
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2017-11-02"), fields.date);
        assertSame(TimeUnit.MILLISECONDS, fields.enumValue);
        assertEquals(Integer.valueOf(123), fields.integer);
        assertEquals(Long.valueOf(123456), fields.longValue);
    }

    private static class PojoWithEnumOptions {
        @Option(names = "-u") private final TimeUnit enumValue = TimeUnit.SECONDS;
    }
    @Test
    public void testParserCaseInsensitiveEnumValuesAllowed_falseByDefault() throws Exception {
        PojoWithEnumOptions fields = new PojoWithEnumOptions();
        CommandLine cmd = new CommandLine(fields);
        assertFalse(cmd.isCaseInsensitiveEnumValuesAllowed());

        try {
            cmd.parseArgs("-u=milliseconds");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Invalid value for option '-u': expected one of "));
        }
    }
    @Test
    public void testParserCaseInsensitiveEnumValuesAllowed_enabled() throws Exception {
        PojoWithEnumOptions fields = new PojoWithEnumOptions();
        new CommandLine(fields).setCaseInsensitiveEnumValuesAllowed(true).parseArgs("-u=milliseconds");
        assertSame(TimeUnit.MILLISECONDS, fields.enumValue);
    }
    @Test
    public void testParserCaseInsensitiveEnumValuesAllowed_invalidInput() throws Exception {
        PojoWithEnumOptions fields = new PojoWithEnumOptions();
        CommandLine cmd = new CommandLine(fields).setCaseInsensitiveEnumValuesAllowed(true);

        try {
            cmd.parseArgs("-u=millisecondINVALID");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Invalid value for option '-u': expected one of "));
        }
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
            assertEquals("Missing required option: '--required=<required>'", ex.getMessage());
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
    @SuppressWarnings("deprecation")
    @Test
    public void testCommandLine_isUsageHelpRequested_trueWhenSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--help");
        assertTrue("usage help requested", parsedCommands.get(0).isUsageHelpRequested());
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testCommandLine_isVersionHelpRequested_trueWhenSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--version");
        assertTrue("version info requested", parsedCommands.get(0).isVersionHelpRequested());
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testCommandLine_isUsageHelpRequested_falseWhenNotSpecified() {
        List<CommandLine> parsedCommands = new CommandLine(new RequiredField()).parse("--version");
        assertFalse("usage help requested", parsedCommands.get(0).isUsageHelpRequested());
    }
    @SuppressWarnings("deprecation")
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
            assertEquals("Missing required option: '--required=<required>'", ex.getMessage());
        }
    }
    @Test
    public void testHelpRequestedFlagResetWhenParsing_instanceMethod() {
        RequiredField requiredField = new RequiredField();
        CommandLine commandLine = new CommandLine(requiredField);
        commandLine.parseArgs("-?");
        assertTrue("help requested", requiredField.isHelpRequested);

        requiredField.isHelpRequested = false;

        // should throw error again on second pass (no help was requested here...)
        try {
            commandLine.parseArgs("arg1", "arg2");
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '--required=<required>'", ex.getMessage());
        }
    }

    static class CompactFields {
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
            assertEquals("Unknown option: '-p1' (while processing option: '-vp1')", ex.getMessage());
        }
    }

    @Test
    public void testCompactFieldsWithUnmatchedArguments() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new CompactFields()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("-oout -r -vp1 p2".split(" "));
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
        cmd.parseArgs("-rvo:out p1 p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }

    /** See {@link #testGnuLongOptionsWithVariousSeparators()}  */
    @Test
    public void testDefaultSeparatorIsEquals() {
        assertEquals("=", new CommandLine(new CompactFields()).getSeparator());
    }

    @Test
    public void testTrimQuotesWhenPropertyTrue() {
        System.setProperty("picocli.trimQuotes", "true");
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(true, commandLine.isTrimQuotes());
    }

    @Test
    public void testTrimQuotesWhenPropertyEmpty() {
        System.setProperty("picocli.trimQuotes", "");
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(true, commandLine.isTrimQuotes());
    }

    @Test
    public void testTrimQuotesWhenPropertyFalse() {
        System.setProperty("picocli.trimQuotes", "false");
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isTrimQuotes());
    }

    @Test
    public void testParserUnmatchedOptionsArePositionalParams_False_unmatchedOptionThrowsUnmatchedArgumentException() {
        class App {
            @Option(names = "-a") String alpha;
            @Parameters String[] remainder;
        }
        CommandLine app = new CommandLine(new App());
        try {
            app.parseArgs("-x", "-a", "AAA");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unknown option: '-x'", ok.getMessage());
        }
    }

    @Test
    public void testParserUnmatchedOptionsArePositionalParams_True_unmatchedOptionIsPositionalParam() {
        class App {
            @Option(names = "-a") String alpha;
            @Parameters String[] remainder;
        }
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        cmd.setUnmatchedOptionsArePositionalParams(true);
        ParseResult parseResult = cmd.parseArgs("-x", "-a", "AAA");
        assertTrue(parseResult.hasMatchedPositional(0));
        assertArrayEquals(new String[]{"-x"}, parseResult.matchedPositionalValue(0, new String[0]));
        assertTrue(parseResult.hasMatchedOption("a"));
        assertEquals("AAA", parseResult.matchedOptionValue("a", null));

        assertArrayEquals(new String[]{"-x"}, app.remainder);
        assertEquals("AAA", app.alpha);
    }

    @Test
    public void testUnmatchedWithEmptyPositional() {
        @Command class App {
            @Option(names = "-x") List<Object> unmatched;
        }
        CommandLine cmd = new CommandLine(new App());
        try {
            cmd.parseArgs("");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 0: ''", ex.getMessage());
        }
    }

    @Test
    public void testOptionsMixedWithParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-r -v p1 -o out p2".split(" "));
        verifyCompact(compact, true, true, "out", fileArray("p1", "p2"));
    }
    @Test
    public void testShortOptionsWithSeparatorButNoValueAssignsEmptyStringEvenIfNotLast() {
        try {
            CommandLine.populateCommand(new CompactFields(), "-ro= -v".split(" "));
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Expected parameter for option '-o' but found '-v'", ex.getMessage());
        }
    }
    @Test
    public void testShortOptionsWithColonSeparatorButNoValueAssignsEmptyStringEvenIfNotLast() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setSeparator(":");
        try {
            cmd.parseArgs("-ro: -v".split(" "));
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Expected parameter for option '-o' but found '-v'", ex.getMessage());
        }
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
    public void testShortOptionsWithSeparatorAndQuotedEmptyStringValueNotLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-ro=\"\" -v".split(" "));
        verifyCompact(compact, true, true, "\"\"", null);
    }
    @Test
    public void testShortOptionsWithColonSeparatorAndQuotedEmptyStringValueNotLast() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setSeparator(":");
        cmd.parseArgs("-ro:\"\" -v".split(" "));
        verifyCompact(compact, true, true, "\"\"", null);
    }
    @Test
    public void testShortOptionsWithSeparatorQuotedEmptyStringValueIfLast() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvo=\"\"".split(" "));
        verifyCompact(compact, true, true, "\"\"", null);
    }
    @Test
    public void testParserPosixClustedShortOptions_false_resultsInShortClusteredOptionsNotRecognized() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvoFILE");
        verifyCompact(compact, true, true, "FILE", null);

        CommandLine cmd = new CommandLine(new CompactFields());
        cmd.getCommandSpec().parser().posixClusteredShortOptionsAllowed(false);
        try {
            cmd.parseArgs("-rvoFILE");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-rvoFILE'", ex.getMessage());
        }
    }
    @Test
    public void testParserPosixClustedShortOptions_false_disallowsShortOptionsAttachedToOptionParam() {
        String[] args = {"-oFILE"};
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), args);
        verifyCompact(compact, false, false, "FILE", null);

        CompactFields unclustered = new CompactFields();
        CommandLine cmd = new CommandLine(unclustered);
        cmd.getCommandSpec().parser().posixClusteredShortOptionsAllowed(false);
        try {
            cmd.parseArgs(args);
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-oFILE'", ex.getMessage());
        }
    }
    @Test
    public void testParserPosixClustedShortOptions_false_allowsUnclusteredShortOptions() {
        String[] args = "-r -v -o FILE".split(" ");
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), args);
        verifyCompact(compact, true, true, "FILE", null);

        CompactFields unclustered = new CompactFields();
        CommandLine cmd = new CommandLine(unclustered);
        cmd.getCommandSpec().parser().posixClusteredShortOptionsAllowed(false);
        cmd.parseArgs(args);
        verifyCompact(unclustered, true, true, "FILE", null);
    }

    @Test
    public void testDoubleDashSeparatesPositionalParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-oout -- -r -v p1 p2".split(" "));
        verifyCompact(compact, false, false, "out", fileArray("-r", "-v", "p1", "p2"));
    }

    @Test
    public void testEndOfOptionsSeparatorConfigurable() {
        CompactFields compact = new CompactFields();
        CommandLine cmd = new CommandLine(compact);
        cmd.setEndOfOptionsDelimiter(";;");
        cmd.parseArgs("-oout ;; ;; -- -r -v p1 p2".split(" "));
        verifyCompact(compact, false, false, "out", fileArray(";;", "--","-r", "-v", "p1", "p2"));
    }

    @Test
    public void testEndOfOptionsSeparatorCannotBeNull() {
        try {
            new CommandLine(new CompactFields()).setEndOfOptionsDelimiter(null);
            fail("Expected exception");
        } catch (Exception ok) {
            assertEquals("java.lang.NullPointerException: end-of-options delimiter", ok.toString());
        }
    }

    @Test
    public void testAssertEquals() throws Exception {
        Method m = Class.forName("picocli.CommandLine$Assert").getDeclaredMethod("equals", Object.class, Object.class);
        m.setAccessible(true);
        assertTrue("null equals null", (Boolean) m.invoke(null, null, null));
        assertFalse("String !equals null", (Boolean) m.invoke(null, "", null));
        assertFalse("null !equals String", (Boolean) m.invoke(null, null, ""));
        assertTrue("String equals String", (Boolean) m.invoke(null, "", ""));
    }

    @Test
    public void testAssertAssertTrue() throws Exception {
        Method m = Class.forName("picocli.CommandLine$Assert").getDeclaredMethod("assertTrue", boolean.class, String.class);
        m.setAccessible(true);

        m.invoke(null, true, "not thrown");
        try {
            m.invoke(null, false, "thrown");
        } catch (InvocationTargetException ex) {
            IllegalStateException actual = (IllegalStateException) ex.getTargetException();
            assertEquals("thrown", actual.getMessage());
        }
    }
    @Test
    public void testAssertAssertTrue2() throws Exception {
        Method m = Class.forName("picocli.CommandLine$Assert").getDeclaredMethod("assertTrue", boolean.class, IHelpSectionRenderer.class);
        m.setAccessible(true);

        IHelpSectionRenderer renderer = new IHelpSectionRenderer() {
            public String render(CommandLine.Help h) {
                return "Internal error: blah";
            }
        };
        m.invoke(null, true, renderer);
        try {
            m.invoke(null, false, renderer);
        } catch (InvocationTargetException ex) {
            IllegalStateException actual = (IllegalStateException) ex.getTargetException();
            assertEquals("Internal error: blah", actual.getMessage());
        }
    }

    private File[] fileArray(final String ... paths) {
        File[] result = new File[paths.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new File(paths[i]);
        }
        return result;
    }

    static void verifyCompact(CompactFields compact, boolean verbose, boolean recursive, String out, File[] params) {
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
            assertEquals("Missing required parameters: '<param0>', '<param1>'", ex.getMessage());
        }
    }

    static class VariousPrefixCharacters {
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
        cmd.parseArgs("--dash:345");
        assertEquals("--dash:val", 345, params.dash);

        params = new VariousPrefixCharacters();
        cmd = new CommandLine(params);
        cmd.setSeparator(":");
        cmd.parseArgs("--dash:345 --owner:y".split(" "));
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
            assertEquals("Missing required option: '--opt:<opt>'", ok.getMessage());
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
            assertEquals("Missing required option: '--opt:<opt>'", ok.getMessage());
        }
    }
    @Test
    public void testIfSeparatorSetTheDefaultSeparatorIsNotRecognizedWithUnmatchedArgsAllowed() {
        @Command(separator = ":")
        class App {
            @Option(names = "--opt", required = true) String opt;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        try {
            cmd.parseArgs("--opt=abc");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ok) {
            assertEquals("Missing required option: '--opt:<opt>'", ok.getMessage());
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
            assertEquals("Missing required parameter for option '--dash' (<dash>)", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--owner".split(" "));
        } catch (ParameterException ex) {
            assertEquals("Missing required parameter for option '--owner' (<owner>)", ex.getMessage());
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
            assertEquals("Invalid value for option '--dash': '' is not an int", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash= /4".split(" "));
            fail("int option (with sep but no value, followed by other option) needs arg");
        } catch (ParameterException ex) {
            assertEquals("Invalid value for option '--dash': '' is not an int", ex.getMessage());
        }
    }

    @Test
    public void testOptionParameterSeparatorIsCustomizable() {
        VariousPrefixCharacters params = new VariousPrefixCharacters();
        CommandLine cmd = new CommandLine(params);
        cmd.setSeparator(":");
        cmd.parseArgs("-d 123 /4 /S 765 /T:98 /Owner:xyz -SingleDash [CPM CP/M (CMS:cmsVal".split(" "));
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
            @Option(names = "-p") String path;
        }
        MyOption opt = CommandLine.populateCommand(new MyOption(), "-pa-p");
        assertEquals("a-p", opt.path);

        opt = CommandLine.populateCommand(new MyOption(), "-p-ap");
        assertEquals("-ap", opt.path);
    }

    @Test
    public void testArityGreaterThanOneForSingleValuedFields() {
        class Arity2 {
            @Option(names = "-p", arity="2") String path;
            @Option(names = "-o", arity="2") String[] otherPath;
        }
        Arity2 opt = CommandLine.populateCommand(new Arity2(), "-o a b".split(" "));

        try {
            opt = CommandLine.populateCommand(new Arity2(), "-p a b".split(" "));
            fail("expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 2: 'b'", ex.getMessage());
        }
    }

    @Test
    public void testOptionParameterQuotesNotRemovedFromValue() {
        class TextOption {
            @Option(names = "-t") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"a text\"");
        assertEquals("\"a text\"", opt.text);
    }

    @Test
    public void testLongOptionAttachedQuotedParameterQuotesNotRemovedFromValue() {
        class TextOption {
            @Option(names = "--text") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "--text=\"a text\"");
        assertEquals("\"a text\"", opt.text);
    }

    @Test
    public void testShortOptionAttachedQuotedParameterQuotesNotRemovedFromValue() {
        class TextOption {
            @Option(names = "-t") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t\"a text\"");
        assertEquals("\"a text\"", opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"");
        assertEquals("\"a text\"", opt.text);
    }

    @Test
    public void testShortOptionAttachedQuotedParameterQuotesTrimmedIfRequested() {
        class TextOption {
            @Option(names = "-t") String text;
        }
        TextOption opt = new TextOption();
        new CommandLine(opt).parseArgs("-t\"a text\"");
        assertEquals("Not trimmed by default","\"a text\"", opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t\"a text\"");
        assertEquals("trimmed if requested","a text", opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t=\"a text\"");
        assertEquals("a text", opt.text);

        opt = new TextOption();
        new CommandLine(opt).parseArgs("-t=\"a text\"");
        assertEquals("Not trimmed by default", "\"a text\"", opt.text);
    }

    @Test
    public void testShortOptionQuotedParameterTypeConversion() {
        class TextOption {
            @Option(names = "-t") int[] number;
            @Option(names = "-v", arity = "1") boolean verbose;
        }
        TextOption opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t", "\"123\"", "-v", "\"true\"");
        assertEquals(123, opt.number[0]);
        assertTrue(opt.verbose);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"-t123\"", "\"-vtrue\"");
        assertEquals(123, opt.number[0]);
        assertTrue(opt.verbose);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t\"123\"", "-v\"true\"");
        assertEquals(123, opt.number[0]);
        assertTrue(opt.verbose);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"-t=345\"", "\"-v=true\"");
        assertEquals(345, opt.number[0]);
        assertTrue(opt.verbose);
    }

    @Test
    public void testOptionMultiParameterQuotesNotRemovedFromValue() {
        class TextOption {
            @Option(names = "-t") String[] text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"a text\"", "-t", "\"another text\"", "-t", "\"x z\"");
        assertArrayEquals(new String[]{"\"a text\"", "\"another text\"", "\"x z\""}, opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t\"a text\"", "-t\"another text\"", "-t\"x z\"");
        assertArrayEquals(new String[]{"\"a text\"", "\"another text\"", "\"x z\""}, opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"", "-t=\"another text\"", "-t=\"x z\"");
        assertArrayEquals(new String[]{"\"a text\"", "\"another text\"", "\"x z\""}, opt.text);

        try {
            opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"", "-t=\"another text\"", "\"x z\"");
            fail("Expected UnmatchedArgumentException");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument at index 2: '\"x z\"'", ok.getMessage());
        }
    }

    @Test
    public void testOptionSingleParameterQuotesTrimmedIfRequested() {
        class TextOption {
            @Option(names = "-t") String text;
        }
        TextOption opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t", "\"a text\"");
        assertEquals("a text", opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"-ta text\"");
        assertEquals("a text", opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t\"a text\"");
        assertEquals("a text", opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"-t=a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testOptionMultiParameterQuotesTrimmedIfRequested() {
        class TextOption {
            @Option(names = "-t") String[] text;
        }
        TextOption opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t", "\"a text\"", "-t", "\"another text\"", "-t", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"-ta text\"", "\"-tanother text\"", "\"-tx z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("-t\"a text\"", "-t\"another text\"", "-t\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);

        opt = new TextOption();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"-t=a text\"", "\"-t=another text\"", "\"-t=x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);
    }

    @Test
    public void testPositionalParameterQuotesNotRemovedFromValue() {
        class TextParams {
            @Parameters() String[] text;
        }
        TextParams opt = CommandLine.populateCommand(new TextParams(), "\"a text\"");
        assertEquals("\"a text\"", opt.text[0]);
    }

    @Test
    public void testPositionalParameterQuotesTrimmedIfRequested() {
        class TextParams {
            @Parameters() String[] text;
        }
        TextParams opt = new TextParams();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"a text\"");
        assertEquals("a text", opt.text[0]);
    }

    @Test
    public void testPositionalMultiParameterQuotesNotRemovedFromValue() {
        class TextParams {
            @Parameters() String[] text;
        }
        TextParams opt = CommandLine.populateCommand(new TextParams(), "\"a text\"", "\"another text\"", "\"x z\"");
        assertArrayEquals(new String[]{"\"a text\"", "\"another text\"", "\"x z\""}, opt.text);
    }

    @Test
    public void testPositionalMultiParameterQuotesTrimmedIfRequested() {
        class TextParams {
            @Parameters() String[] text;
        }
        TextParams opt = new TextParams();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"a text\"", "\"another text\"", "\"x z\"");
        assertArrayEquals(new String[]{"a text", "another text", "x z"}, opt.text);
    }

    @Test
    public void testPositionalMultiQuotedParameterTypeConversion() {
        class TextParams {
            @Parameters() int[] numbers;
        }
        TextParams opt = new TextParams();
        new CommandLine(opt).setTrimQuotes(true).parseArgs("\"123\"", "\"456\"", "\"999\"");
        assertArrayEquals(new int[]{123, 456, 999}, opt.numbers);
    }

    @Test
    public void testSubclassedOptions() {
        class ParentOption {
            @Option(names = "-p") String path;
        }
        class ChildOption extends ParentOption {
            @Option(names = "-t") String text;
        }
        ChildOption opt = CommandLine.populateCommand(new ChildOption(), "-p", "somePath", "-t", "\"a text\"");
        assertEquals("somePath", opt.path);
        assertEquals("\"a text\"", opt.text);
    }

    @Test
    public void testSubclassedOptionsWithShadowedOptionNameThrowsDuplicateOptionAnnotationsException() {
        class ParentOption {
            @Option(names = "-p") String path;
        }
        class ChildOption extends ParentOption {
            @Option(names = "-p") String text;
        }
        try {
            CommandLine.populateCommand(new ChildOption(), "");
            fail("expected CommandLine$DuplicateOptionAnnotationsException");
        } catch (DuplicateOptionAnnotationsException ex) {
            String expected = format("Option name '-p' is used by both field String %s.text and field String %s.path",
                    ChildOption.class.getName(), ParentOption.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testSubclassedOptionsWithShadowedFieldInitializesChildField() {
        class ParentOption {
            @Option(names = "-parentPath") String path;
        }
        class ChildOption extends ParentOption {
            @Option(names = "-childPath") String path;
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
            assertEquals("Missing required parameter: '<file1>'", ex.getMessage());
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
        new CommandLine(app1).setOverwrittenOptionsAllowed(true).parseArgs("000", "111", "222", "333");
        assertEquals("field initialized with arg[0]", new File("111"), app1.file0_1);
        assertEquals("arg[1] and arg[2]", Arrays.asList(
                new File("111"),
                new File("222")), app1.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                //null, null, null, null, // #216 default values are replaced
                new File("000"),
                new File("111"),
                new File("222"),
                new File("333")}, app1.fileArray0_3);
        assertEquals("args", Arrays.asList("000", "111", "222", "333"), app1.all);

        App app2 = new App();
        new CommandLine(app2).setOverwrittenOptionsAllowed(true).parseArgs("000", "111");
        assertEquals("field initialized with arg[0]", new File("111"), app2.file0_1);
        assertEquals("arg[1]", Arrays.asList(new File("111")), app2.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                //null, null, null, null,  // #216 default values are replaced
                new File("000"),
                new File("111"),}, app2.fileArray0_3);
        assertEquals("args", Arrays.asList("000", "111"), app2.all);

        App app3 = CommandLine.populateCommand(new App(), "000");
        assertEquals("field initialized with arg[0]", new File("000"), app3.file0_1);
        assertEquals("arg[1]", null, app3.fileList1_2);
        assertArrayEquals("arg[0-3]", new File[]{
                //null, null, null, null,  // #216 default values are replaced
                new File("000")}, app3.fileArray0_3);
        assertEquals("args", Arrays.asList("000"), app3.all);

        try {
            CommandLine.populateCommand(new App());
            fail("Should fail with missingParamException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: '<file0_1>'", ex.getMessage());
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

    @Test
    public void testPositionalParamWithFixedIndexRangeAndVariableArity() throws Exception { // #70
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

    @Test
    public void test70MultiplePositionalsConsumeSamePosition() {
        class App {
            @Parameters(index = "0..3") String[] posA;
            @Parameters(index = "2..4") String[] posB;
            @Unmatched String[] unmatched;
        }
        App app = CommandLine.populateCommand(new App(), "A B C D E F".split(" "));
        assertArrayEquals(new String[]{"A", "B", "C", "D"}, app.posA);
        assertArrayEquals(new String[]{"C", "D", "E"}, app.posB);
        assertArrayEquals(new String[]{"F"}, app.unmatched);
    }

    @Test
    public void test70PositionalOnlyConsumesPositionWhenTypeConversionSucceeds() {
        class App {
            @Parameters(index = "0..3") int[] posA;
            @Parameters(index = "2..4") String[] posB;
            @Unmatched String[] unmatched;
        }
        App app = CommandLine.populateCommand(new App(), "11 22 C D E F".split(" "));
        assertArrayEquals("posA cannot consume positions 2 and 3", new int[]{11, 22}, app.posA);
        assertArrayEquals(new String[]{"C", "D", "E"}, app.posB);
        assertArrayEquals(new String[]{"F"}, app.unmatched);
    }

    @Test
    public void test70PositionalOnlyConsumesPositionWhenTypeConversionSucceeds2() {
        class App {
            @Parameters(index = "0..3") String[] posA;
            @Parameters(index = "2..4") int[] posB;
            @Unmatched String[] unmatched;
        }
        App app = CommandLine.populateCommand(new App(), "A B C 33 44 55".split(" "));
        assertArrayEquals(new String[]{"A", "B", "C", "33"}, app.posA);
        assertArrayEquals(new int[]{33, 44}, app.posB);
        assertArrayEquals(new String[]{"55"}, app.unmatched);
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
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("val1", "val2");
        assertEquals("val1", ((SingleValue)cmd.getCommand()).str);
        assertEquals(Arrays.asList("val2"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testPositionalParamsUnknownArgumentMultiValueWithUnmatchedArgsAllowed() throws Exception {
        class SingleValue {
            @Parameters(index = "0..2") String[] str;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("val0", "val1", "val2", "val3");
        assertArrayEquals(new String[]{"val0", "val1", "val2"}, ((SingleValue)cmd.getCommand()).str);
        assertEquals(Arrays.asList("val3"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testUnmatchedArgsInitiallyEmpty() throws Exception {
        class SingleValue {
            @Parameters(index = "0..2") String[] str;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new SingleValue());
        assertTrue(cmd.getUnmatchedArguments().isEmpty());

        CommandLine cmd2 = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        assertTrue(cmd2.getUnmatchedArguments().isEmpty());
    }

    @Test
    public void testPositionalParamSingleValueButWithoutIndex() throws Exception {
        class SingleValue {
            @Parameters String str;
        }
        try {
            CommandLine.populateCommand(new SingleValue(),"val1", "val2");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 1: 'val2'", ex.getMessage());
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new SingleValue()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("val1", "val2");
        assertEquals("val1", ((SingleValue) cmd.getCommand()).str);
        assertEquals(Arrays.asList("val2"), cmd.getUnmatchedArguments());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testParseSubCommands() {
        CommandLine commandLine = Demo.mainCommand();

        List<CommandLine> parsed = commandLine.parse("--git-dir=/home/rpopma/picocli status -sbuno".split(" "));
        assertEquals("command count", 2, parsed.size());

        assertEquals(Demo.Git.class,       ((Object) parsed.get(0).getCommand()).getClass());
        assertEquals(Demo.GitStatus.class, ((Object) parsed.get(1).getCommand()).getClass());

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
        assertTrue("cmd1", ((Object) commandMap.get("cmd1").getCommand()) instanceof Command1);
        assertTrue("cmd2", ((Object) commandMap.get("cmd2").getCommand()) instanceof Command2);
    }

    @Test
    public void testCommandListReturnsCaseInsensitiveRegisteredCommands() {
        @Command class MainCommand {}
        @Command class Command1 {}
        @Command class Command2 {}
        CommandLine commandLine = new CommandLine(new MainCommand());
        commandLine.addSubcommand("cmd1", new Command1()).addSubcommand("cmd2", new Command2());
        commandLine.setSubcommandsCaseInsensitive(true);

        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertEquals(2, commandMap.size());
        assertTrue("cmd1", ((Object) commandMap.get("CMD1").getCommand()) instanceof Command1);
        assertTrue("cmd2", ((Object) commandMap.get("CMD2").getCommand()) instanceof Command2);
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
            assertEquals("option '-v' should be specified only once", ex.getMessage());
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
            assertEquals("option '--str' (<string>) should be specified only once", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "--verbose");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '--verbose' should be specified only once", ex.getMessage());
        }
    }

    @Test
    public void testOverwrittenOptionExceptionContainsCorrectArgSpec() {
        class App {
            @Option(names = "-s") String string;
            @Option(names = "-v") boolean bool;
        }
        try {
            CommandLine.populateCommand(new App(), "-s", "1", "-s", "2");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals(ex.getCommandLine().getCommandSpec().optionsMap().get("-s"), ex.getOverwritten());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "-v");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals(ex.getCommandLine().getCommandSpec().optionsMap().get("-v"), ex.getOverwritten());
        }
    }

    @Test
    public void testOverwrittenOptionSetsLastValueIfAllowed() {
        class App {
            @Option(names = {"-s", "--str"})      String string;
            @Option(names = {"-v", "--verbose"}) boolean bool;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine commandLine = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        commandLine.parseArgs("-s", "1", "--str", "2");
        assertEquals("2", ((App) commandLine.getCommand()).string);

        commandLine = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        commandLine.parseArgs("-v", "--verbose", "-v"); // F -> T -> F -> T
        assertEquals(true, ((App) commandLine.getCommand()).bool);
    }

    @Test
    public void testOverwrittenOptionAppliesToRegisteredSubcommands() {
        @Command(name = "parent")
        class Parent {
            public Parent() {}
            @Option(names = "--parent") String parentString;
        }
        @Command()
        class App {
            @Option(names = {"-s", "--str"})      String string;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine commandLine = new CommandLine(new App())
                .addSubcommand("parent", new Parent())
                .setOverwrittenOptionsAllowed(true);
        commandLine.parseArgs("-s", "1", "--str", "2", "parent", "--parent", "parentVal", "--parent", "2ndVal");

        App app = commandLine.getCommand();
        assertEquals("2", app.string);

        Parent parent = commandLine.getSubcommands().get("parent").getCommand();
        assertEquals("2ndVal", parent.parentString);
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
            commandLine.parseArgs("-u", "foo");
            fail("expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '--password=<password>'", ex.getLocalizedMessage());
        }
        commandLine.parseArgs("-u", "foo", "-p", "abc");
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
                        "      --foo-bar-baz=<foo>     Default:\n" +
                        "                              aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                        "                              aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n";

        assertEquals(expectedOutput, content);
    }
    @Test
    public void testMapFieldHappyCase() {
        class App {
            @Option(names = {"-P", "-map"}, type = {String.class, String.class}) Map<String, String> map = new HashMap<String, String>();
            private void validateMapField() {
                assertEquals(1, map.size());
                assertEquals(LinkedHashMap.class, map.getClass());
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
            assertEquals("Unmatched arguments from index 2: 'CCC=DDD', 'EEE=FFF'", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-map=AAA=BBB", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments from index 1: 'CCC=DDD', 'EEE=FFF'", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-PAAA=BBB", "-PCCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument at index 2: 'EEE=FFF'", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-P", "AAA=BBB", "-P", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument at index 4: 'EEE=FFF'", ok.getMessage());
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
            assertEquals("No TypeConverter registered for java.lang.Thread of field java.util.TreeMap<String, String> " +
                    App.class.getName() + ".map", ex.getMessage());
        }
    }
    @Test
    public void testMapPositionalParameterFieldMaxArity() {
        class App {
            @Parameters(index = "0", arity = "2", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        try {
            CommandLine.populateCommand(new App(), "1=a", "2=b", "3=c", "4=d");
            fail("UnmatchedArgumentsException expected");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments from index 2: '3=c', '4=d'", ex.getMessage());
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("1=a", "2=b", "3=c", "4=d");
        assertEquals(Arrays.asList("3=c", "4=d"), cmd.getUnmatchedArguments());
    }
    @Test
    public void testMapPositionalParameterFieldArity3() {
        class App {
            @Parameters(index = "0", arity = "3", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        try {
            CommandLine.populateCommand(new App(), "1=a", "2=b", "3=c", "4=d");
            fail("UnmatchedArgumentsException expected");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 3: '4=d'", ex.getMessage());
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true);
        cmd.parseArgs("1=a", "2=b", "3=c", "4=d");
        assertEquals(Arrays.asList("4=d"), cmd.getUnmatchedArguments());
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
            assertEquals("Missing required options: '-a=<first>', '-b=<second>', '-c=<third>'", ex.getMessage());
        }
    }
    @Test
    public void testUnmatchedArgumentDoesNotUseLabel() {
        class App {
            @Parameters(arity = "1", paramLabel = "IN_FILE", description = "The input file")
            File foo;
            @Option(names = "-o", paramLabel = "OUT_FILE", description = "The output file", required = true)
            File bar;
        }
        try {
            CommandLine.populateCommand(new App(), "a", "b", "-o=1");
            fail("UnmatchedArgumentException expected");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 1: 'b'", ex.getMessage());
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
            assertEquals("Missing required options and parameters: '-o=OUT_FILE', 'IN_FILE'", ex.getMessage());
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
            assertEquals("Missing required options and parameters: '-o=<String=String>', '-x=KEY=VAL', '<Long=File>'", ex.getMessage());
        }
    }
    @Ignore("That fails because there is public no-arg constructor")
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
    public void testParameterExceptionDisallowsArgSpecAndValueBothNull() {
        CommandLine cmd = new CommandLine(CommandSpec.create());

        try {
            new ParameterException(cmd, "", null, null);
        } catch (IllegalArgumentException ex) {
            assertEquals("ArgSpec and value cannot both be null", ex.getMessage());
        }
        try {
            new ParameterException(cmd, "", new Throwable(), null, null);
        } catch (IllegalArgumentException ex) {
            assertEquals("ArgSpec and value cannot both be null", ex.getMessage());
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
            assertEquals("Unknown option: '-xx'", ex.getMessage());
            assertEquals(Arrays.asList("-xx"), ex.getUnmatched());
        }
        try {
            CommandLine.populateCommand(new App(), "-x", "-a", "aValue");
            fail("UnmatchedArgumentException expected for -x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-x'", ex.getMessage());
            assertEquals(Arrays.asList("-x"), ex.getUnmatched());
        }
        try {
            CommandLine.populateCommand(new App(), "--x", "-a", "aValue");
            fail("UnmatchedArgumentException expected for --x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '--x'", ex.getMessage());
            assertEquals(Arrays.asList("--x"), ex.getUnmatched());
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
            assertEquals("Unknown option: '--ccd'", ex.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void test149OnlyUnmatchedOptionStoredOthersParsed() throws Exception {
        class App {
            @Option(names = "-a") String first;
            @Option(names = "-b") String second;
            @Option(names = {"-c", "--ccc"}) String third;
            @Parameters String[] positional;
        }
        setTraceLevel(CommandLine.TraceLevel.INFO);
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

        String expected = format("" +
                "[picocli INFO] Picocli version: %2$s%n" +
                "[picocli INFO] Parsing 2 command line args [-yy, -a=A]%n" +
                "[picocli INFO] Setting field String %1$s.first to 'A' (was 'null') for option -a%n" +
                "[picocli INFO] Unmatched arguments: [-yy]%n" +
                "[picocli INFO] Picocli version: %2$s%n" +
                "[picocli INFO] Parsing 2 command line args [-y, -b=B]%n" +
                "[picocli INFO] Setting field String %1$s.second to 'B' (was 'null') for option -b%n" +
                "[picocli INFO] Unmatched arguments: [-y]%n" +
                "[picocli INFO] Picocli version: %2$s%n" +
                "[picocli INFO] Parsing 2 command line args [--y, -c=C]%n" +
                "[picocli INFO] Setting field String %1$s.third to 'C' (was 'null') for option -c%n" +
                "[picocli INFO] Unmatched arguments: [--y]%n",
                App.class.getName(),
                CommandLine.versionString());
        String actual = new String(baos.toByteArray(), "UTF8");
        assertEquals(stripAnsiTrace(expected), stripAnsiTrace(actual));
        setTraceLevel(CommandLine.TraceLevel.WARN);
    }

    @Test
    public void testIsStopAtUnmatched_FalseByDefault() {
        @Command class A {}
        assertFalse(new CommandLine(new A()).isStopAtUnmatched());
    }

    @Test
    public void testSetStopAtUnmatched_True_SetsUnmatchedOptionsAllowedToTrue() {
        @Command class A {}
        CommandLine commandLine = new CommandLine(new A());
        assertFalse(commandLine.isUnmatchedArgumentsAllowed());
        commandLine.setStopAtUnmatched(true);
        assertTrue(commandLine.isUnmatchedArgumentsAllowed());
    }

    @Test
    public void testSetStopAtUnmatched_False_LeavesUnmatchedOptionsAllowedUnchanged() {
        @Command class A {}
        CommandLine commandLine = new CommandLine(new A());
        assertFalse(commandLine.isUnmatchedArgumentsAllowed());
        commandLine.setStopAtUnmatched(false);
        assertFalse(commandLine.isUnmatchedArgumentsAllowed());
    }


    @Test
    public void testStopAtUnmatched_UnmatchedOption() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class App {
            @Option(names = "-a") String first;
            @Parameters String[] positional;
        }
        App cmd1 = new App();
        CommandLine commandLine1 = new CommandLine(cmd1).setStopAtUnmatched(true);
        commandLine1.parseArgs("--y", "-a=abc", "positional");
        assertEquals(Arrays.asList("--y", "-a=abc", "positional"), commandLine1.getUnmatchedArguments());
        assertNull(cmd1.first);
        assertNull(cmd1.positional);

        try {
            // StopAtUnmatched=false, UnmatchedArgumentsAllowed=false
            new CommandLine(new App()).parseArgs("--y", "-a=abc", "positional");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '--y'", ex.getMessage());
        }
        App cmd2 = new App();
        CommandLine commandLine2 = new CommandLine(cmd2).setStopAtUnmatched(false).setUnmatchedArgumentsAllowed(true);
        commandLine2.parseArgs("--y", "-a=abc", "positional");
        assertEquals(Arrays.asList("--y"), commandLine2.getUnmatchedArguments());
        assertEquals("abc", cmd2.first);
        assertArrayEquals(new String[]{"positional"}, cmd2.positional);
        setTraceLevel(CommandLine.TraceLevel.WARN);

    }

    @Test
    public void testIsStopAtPositional_FalseByDefault() {
        @Command class A {}
        assertFalse(new CommandLine(new A()).isStopAtPositional());
    }

    @Test
    public void testSetStopAtPositional_True_SetsStopAtPositionalToTrue() {
        @Command class A {}
        CommandLine commandLine = new CommandLine(new A());
        assertFalse(commandLine.isStopAtPositional());
        commandLine.setStopAtPositional(true);
        assertTrue(commandLine.isStopAtPositional());
    }

    @Test
    public void testStopAtPositional_TreatsOptionsAfterPositionalAsPositional() {
        class App {
            @Option(names = "-a") String first;
            @Parameters String[] positional;
        }
        App cmd1 = new App();
        CommandLine commandLine1 = new CommandLine(cmd1).setStopAtPositional(true);
        commandLine1.parseArgs("positional", "-a=abc", "positional");
        assertArrayEquals(new String[]{"positional", "-a=abc", "positional"}, cmd1.positional);
        assertNull(cmd1.first);
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
            new CommandLine(new Example()).parseArgs("-o /tmp");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: '<inputFiles>'", ex.getMessage());
        }
        try {
            // Comment from AshwinJay : "Should've failed as inputFiles were not provided"
            //
            // RP: After removing `usageHelp = true`, the ["-o", " /tmp"] arguments are parsed and
            // a MissingParameterException is thrown for the missing <inputFiles>, as expected.
            new CommandLine(new Example()).parseArgs("-o", " /tmp");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: '<inputFiles>'", ex.getMessage());
        }
        try {
            // a MissingParameterException is thrown for missing required option -o, as expected
            new CommandLine(new Example()).parseArgs("inputfile1", "inputfile2");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '--out-dir=<outputDir>'", ex.getMessage());
        }

        // a single empty string parameter was specified: this becomes an <inputFile> value
        try {
            new CommandLine(new Example()).parseArgs("");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '--out-dir=<outputDir>'", ex.getMessage());
        }

        // no parameters were specified
        try {
            new CommandLine(new Example()).parseArgs();
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required options and parameters: '--out-dir=<outputDir>', '<inputFiles>'", ex.getMessage());
        }

        // finally, let's test the success scenario
        Example example = new Example();
        new CommandLine(example).parseArgs("-o", "/tmp","inputfile1", "inputfile2");
        assertEquals(new File("/tmp"), example.outputDir);
        assertEquals(2, example.inputFiles.length);
        assertEquals(new File("inputfile1"), example.inputFiles[0]);
        assertEquals(new File("inputfile2"), example.inputFiles[1]);
    }
    @SuppressWarnings("deprecation")
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
                    parseArgs("sub1 -x abc".split(" "));
        } catch (ParameterException ex) {
            assertTrue(((Object) ex.getCommandLine().getCommand()) instanceof Top);
        }
        try {
            new CommandLine(new Top()).
                    addSubcommand("sub1", new Sub1()).
                    addSubcommand("sub2", new Sub2()).
                    parseArgs("-o OPT sub1 -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(((Object) ex.getCommandLine().getCommand()) instanceof Sub1);
        }
        try {
            new CommandLine(new Top()).
                    addSubcommand("sub1", new Sub1()).
                    addSubcommand("sub2", new Sub2()).
                    parseArgs("-o OPT sub2 -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(((Object) ex.getCommandLine().getCommand()) instanceof Sub2);
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

    @SuppressWarnings("deprecation")
    @Test
    public void testIssue207ParameterExceptionProvidesAccessToFailedCommand_Declarative() {
        @Command(subcommands = {Sub207A.class, Sub207B.class})
        class Top {
            @Option(names = "-o", required = true) String option;
        }
        try {
            new CommandLine(new Top()).parseArgs("sub207A -x abc".split(" "));
        } catch (ParameterException ex) {
            assertTrue(((Object) ex.getCommandLine().getCommand()) instanceof Top);
        }
        try {
            new CommandLine(new Top()).parseArgs("-o OPT sub207A -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(((Object) ex.getCommandLine().getCommand()) instanceof Sub207A);
        }
        try {
            new CommandLine(new Top()).parseArgs("-o OPT sub207B -wrong ABC".split(" "));
        } catch (ParameterException ex) {
            assertTrue(((Object) ex.getCommandLine().getCommand()) instanceof Sub207B);
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
    @Test
    public void testIssue217BooleanOptionArrayWithParameter() {
        class App {
            @Option(names = "-a", split = ",")
            private boolean[] array;
        }
        App app;

        app = CommandLine.populateCommand(new App(), "-a=true");
        assertArrayEquals(new boolean[]{true}, app.array);

        app = CommandLine.populateCommand(new App(), "-a=true", "-a=true", "-a=true");
        assertArrayEquals(new boolean[]{true, true, true}, app.array);

        app = CommandLine.populateCommand(new App(), "-a=true,true,true");
        assertArrayEquals(new boolean[]{true, true, true}, app.array);
    }
    @Test
    public void testIssue217BooleanOptionArray() {
        class App {
            @Option(names = "-a")
            private boolean[] array;
        }
        App app;

        app = CommandLine.populateCommand(new App(), "-a");
        assertArrayEquals(new boolean[]{true}, app.array);

        app = CommandLine.populateCommand(new App(), "-a", "-a", "-a");
        assertArrayEquals(new boolean[]{true, true, true}, app.array);

        app = CommandLine.populateCommand(new App(), "-aaa");
        assertArrayEquals(new boolean[]{true, true, true}, app.array);
    }
    @Test
    public void testIssue217BooleanOptionArrayExplicitArity() {
        class App {
            @Option(names = "-a", arity = "0")
            private boolean[] array;
        }
        App app;

        app = CommandLine.populateCommand(new App(), "-a");
        assertArrayEquals(new boolean[]{true}, app.array);

        app = CommandLine.populateCommand(new App(), "-a", "-a", "-a");
        assertArrayEquals(new boolean[]{true, true, true}, app.array);

        setTraceLevel(CommandLine.TraceLevel.DEBUG);
        app = CommandLine.populateCommand(new App(), "-aaa");
        assertArrayEquals(new boolean[]{true, true, true}, app.array);
    }
    @Test
    public void testIssue217BooleanOptionList() {
        class App {
            @Option(names = "-a")
            private List<Boolean> list;
        }
        App app;

        app = CommandLine.populateCommand(new App(), "-a");
        assertEquals(Arrays.asList(true), app.list);

        app = CommandLine.populateCommand(new App(), "-a", "-a", "-a");
        assertEquals(Arrays.asList(true, true, true), app.list);

        app = CommandLine.populateCommand(new App(), "-aaa");
        assertEquals(Arrays.asList(true, true, true), app.list);
    }

    @Test
    public void testUnmatchedAnnotationWithInstantiatedList() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class App {
            @Unmatched List<String> unmatched = new ArrayList<String>();
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parseArgs("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched);
    }

    @Test
    public void testUnmatchedAnnotationInstantiatesList() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class App {
            @Unmatched List<String> unmatched;
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parseArgs("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched);
    }

    @Test
    public void testUnmatchedAnnotationInstantiatesArray() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class App {
            @Unmatched String[] unmatched;
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parseArgs("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertArrayEquals(new String[]{"-t", "-x", "abc"}, app.unmatched);
    }

    @Test
    public void testMultipleUnmatchedAnnotations() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class App {
            @Unmatched String[] unmatched1;
            @Unmatched String[] unmatched2;
            @Unmatched List<String> unmatched3;
            @Unmatched List<String> unmatched4;
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parseArgs("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertArrayEquals(new String[]{"-t", "-x", "abc"}, app.unmatched1);
        assertArrayEquals(new String[]{"-t", "-x", "abc"}, app.unmatched2);
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched3);
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched4);
    }

    @Test
    public void testCommandAllowsOnlyUnmatchedAnnotation() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class App {
            @Unmatched String[] unmatched;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.parseArgs("a", "b");
        assertEquals(Arrays.asList("a", "b"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testUnmatchedAnnotationWithInvalidType_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched String unmatched;
        }
        Object app = new App();
        Field f = App.class.getDeclaredField("unmatched");
        assertInvalidUnmatchedFieldType(app, f);
    }

    @Test
    public void testUnmatchedAnnotationWithInvalidGenericType_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched List<Object> unmatched;
        }
        Object app = new App();
        Field f = App.class.getDeclaredField("unmatched");
        assertInvalidUnmatchedFieldType(app, f);
    }

    private void assertInvalidUnmatchedFieldType(Object app, Field f) {
        try {
            new CommandLine(app);
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "Invalid type for %s: must be either String[] or List<String>";
            assertEquals(format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testUnmatchedAndOptionAnnotation_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched @Option(names = "-x") List<Object> unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "A member cannot have both @Unmatched and @Option annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testUnmatchedAndParametersAnnotation_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched @Parameters List<Object> unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "A member cannot have both @Unmatched and @Parameters annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testUnmatchedAndMixinAnnotation_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched @Mixin List<Object> unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "A member cannot have both @Mixin and @Unmatched annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testMixinAndOptionAnnotation_ThrowsException() throws Exception {
        @Command class App {
            @Mixin @Option(names = "-x") List<Object> unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "A member cannot have both @Mixin and @Option annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testMixinAndParametersAnnotation_ThrowsException() throws Exception {
        @Command class App {
            @Mixin @Parameters List<Object> unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "A member cannot have both @Mixin and @Parameters annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testAnyHelpCommandMakesRequiredOptionsOptional() {
        @Command(name = "help", helpCommand = true)
        class MyHelpCommand {
            @Option(names = "-o")
            String option;
        }
        @Command(subcommands = MyHelpCommand.class)
        class Parent {
            @Option(names = "-m", required = true)
            String mandatory;
        }
        CommandLine commandLine = new CommandLine(new Parent(), new InnerClassFactory(this));
        commandLine.parseArgs("help");
    }

    @Test
    public void testBuiltInHelpCommandMakesRequiredOptionsOptional() {
        @Command(subcommands = HelpCommand.class)
        class Parent {
            @Option(names = "-m", required = true)
            String mandatory;
        }
        CommandLine commandLine = new CommandLine(new Parent(), new InnerClassFactory(this));
        commandLine.parseArgs("help");
    }

    @Test
    public void testAutoHelpOptionMakesRequiredOptionsOptional() {
        @Command(mixinStandardHelpOptions = true)
        class Parent {
            @Option(names = "-m", required = true)
            String mandatory;
        }
        CommandLine commandLine = new CommandLine(new Parent(), new InnerClassFactory(this));
        commandLine.parseArgs("--help");
        assertTrue("No exceptions", true);
    }

    @Test
    public void testToggleBooleanFlagsWhenEnabled() {
        class Flags {
            @Option(names = "-a") boolean a;
            @Option(names = "-b") boolean b = true;
            @Parameters(index = "0") boolean p0;
            @Parameters(index = "1") boolean p1 = true;
        }
        Flags flags = new Flags();
        CommandLine commandLine = new CommandLine(flags);
        commandLine.setToggleBooleanFlags(true);
        commandLine.setOverwrittenOptionsAllowed(true);

        assertFalse(flags.a);
        assertFalse(flags.p0);
        assertTrue (flags.b);
        assertTrue (flags.p1);
        commandLine.parseArgs("-a", "-b", "true", "false");
        assertFalse(!flags.a);
        assertTrue (!flags.b);
        assertFalse(!flags.p0);
        assertTrue (!flags.p1);
        commandLine.parseArgs("-a", "-b", "true", "false");
        assertFalse(!flags.a);
        assertTrue (!flags.b);
        assertFalse(!flags.p0);
        assertTrue (!flags.p1);

        commandLine.parseArgs("-a", "-a", "-b", "-b", "true", "false");
        // multiple occurrences DO cancel each other out
        assertFalse(flags.a);
        assertTrue (flags.b);
    }

    @Test
    public void testNoToggleBooleanFlagsByDefault() {
        class Flags {
            @Option(names = "-a") boolean a;
            @Option(names = "-b") boolean b = true;
            @Parameters(index = "0") boolean p0;
            @Parameters(index = "1") boolean p1 = true;
        }
        Flags flags = new Flags();
        CommandLine commandLine = new CommandLine(flags);
        commandLine.setOverwrittenOptionsAllowed(true);
        assertFalse(commandLine.isToggleBooleanFlags());

        // initial/default values
        assertFalse(flags.a);
        assertTrue (flags.b);
        assertFalse(flags.p0);
        assertTrue (flags.p1);
        commandLine.parseArgs("-a", "-b", "true", "false");
        // specified flags now opposite of default
        assertTrue (flags.a);
        assertFalse(flags.b);
        assertTrue (flags.p0);
        assertFalse(flags.p1);
        commandLine.parseArgs("-a", "-b", "true", "false");
        // specified flags again opposite of default
        assertTrue (flags.a);
        assertFalse(flags.b);
        assertTrue (flags.p0);
        assertFalse(flags.p1);

        commandLine.parseArgs("-a", "-a", "-b", "-b", "true", "false");
        // multiple occurrences do NOT cancel each other out
        assertTrue (flags.a);
        assertFalse(flags.b);
    }

    @Test
    public void testMapValuesContainingSeparator() {
        class MyCommand {
            @Option(names = {"-p", "--parameter"})
            Map<String, String> parameters;
        }
        String[] args = new String[] {"-p", "AppOptions=\"-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io\""};
        MyCommand c = CommandLine.populateCommand(new MyCommand(), args);
        assertEquals("\"-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io\"", c.parameters.get("AppOptions"));

        c = new MyCommand();
        new CommandLine(c).setTrimQuotes(true).parseArgs(args);
        assertEquals("-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io", c.parameters.get("AppOptions"));

        args = new String[] {"-p", "\"AppOptions=-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io\""};
        try {
            c = CommandLine.populateCommand(new MyCommand(), args);
            fail("Expected exception"); // superceded by #1214
        } catch (ParameterException ex) {
            assertEquals("Value for option option '--parameter' (<String=String>) should be in KEY=VALUE format but was \"AppOptions=-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io\"", ex.getMessage());
        }

        c = new MyCommand();
        new CommandLine(c).setTrimQuotes(true).parseArgs(args);
        assertEquals("-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io", c.parameters.get("AppOptions"));
    }

    // Enum required for testIssue402, can't be local
    public enum Choices {
        CHOICE1,
        CHOICE2
    }
    @Test
    public void testIssue402() {
        class AppWithEnum {
            @Parameters(type = Choices.class)
            private Choices choice;
        }
        AppWithEnum app;
        try {
            app = CommandLine.populateCommand(new AppWithEnum(), "CHOICE3");
        } catch (ParameterException e) {
            assertEquals("<choice>", e.getArgSpec().paramLabel());
            assertEquals(2, e.getArgSpec().type().getEnumConstants().length);
            assertEquals(Choices.CHOICE1, e.getArgSpec().type().getEnumConstants()[0]);
            assertEquals(Choices.CHOICE2, e.getArgSpec().type().getEnumConstants()[1]);
            assertEquals("CHOICE3", e.getValue());
        }
    }

    @Test
    public void testEmptyObjectArray() throws Exception {
        Method m = CommandLine.class.getDeclaredMethod("empty", new Class[] {Object[].class});
        m.setAccessible(true);

        assertTrue((Boolean) m.invoke(null, new Object[] {null}));
        assertTrue((Boolean) m.invoke(null, new Object[] {new String[0]}));
    }

    @Test
    public void testStr() throws Exception {
        Method m = CommandLine.class.getDeclaredMethod("str", String[].class, int.class);
        m.setAccessible(true);

        assertEquals("", m.invoke(null, null, 0));
        assertEquals("", m.invoke(null, new String[0], 1));
    }

    @Test
    public void testParseAmbiguousKeyValueOption() {
        class App {
            @Option(names = "-x") String x;
            @Option(names = "-x=abc") String xabc;
        }
        try {
            CommandLine.populateCommand(new App(), "-x=abc");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter for option '-x=abc' (<xabc>)", ex.getMessage());
        }
        assertEquals(format("[picocli WARN] Both '-x=abc' and '-x' are valid option names in <main class>. Using '-x=abc'...%n"), systemErrRule.getLog());
    }

    @Test
    public void testParseAmbiguousKeyValueOption2() {
        class App {
            @Option(names = "-x") String x;
            @Option(names = "-x=abc") String xabc;
        }
        App app = CommandLine.populateCommand(new App(), "-x=abc=xyz");
        assertNull(app.xabc);
        assertEquals("abc=xyz", app.x);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSmartUnquote() {
        CommandSpec spec = CommandSpec.create();
        spec.parser().trimQuotes(true);
        CommandLine cmd = new CommandLine(spec);

        assertNull(cmd.smartUnquoteIfEnabled(null));
        assertEquals("abc", cmd.smartUnquoteIfEnabled("\"abc\""));
        assertEquals("", cmd.smartUnquoteIfEnabled("\"\""));
        assertEquals("only balanced quotes 1", "\"abc", cmd.smartUnquoteIfEnabled("\"abc"));
        assertEquals("only balanced quotes 2", "abc\"", cmd.smartUnquoteIfEnabled("abc\""));
        assertEquals("only balanced quotes 3", "\"", cmd.smartUnquoteIfEnabled("\""));
        assertEquals("no quotes", "X", cmd.smartUnquoteIfEnabled("X"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInterpreterApplyValueToSingleValuedField() throws Exception {
        Class c = Class.forName("picocli.CommandLine$Interpreter");
        Class lookBehindClass = Class.forName("picocli.CommandLine$LookBehind");
        Method applyValueToSingleValuedField = c.getDeclaredMethod("applyValueToSingleValuedField",
                ArgSpec.class,
                boolean.class,
                lookBehindClass,
                boolean.class,
                Range.class,
                Stack.class, Set.class, String.class);
        applyValueToSingleValuedField.setAccessible(true);

        CommandSpec spec = CommandSpec.create();
        spec.parser().trimQuotes(true);
        CommandLine cmd = new CommandLine(spec);
        Object interpreter = TestUtil.interpreter(cmd);
        Method clear = c.getDeclaredMethod("clear");
        clear.setAccessible(true);
        clear.invoke(interpreter); // initializes the interpreter instance

        PositionalParamSpec arg = PositionalParamSpec.builder().arity("1").build();
        Object SEPARATE = lookBehindClass.getDeclaredField("SEPARATE").get(null);

        int value = (Integer) applyValueToSingleValuedField.invoke(interpreter,
                arg, false, SEPARATE, false, Range.valueOf("1"), new Stack<String>(), new HashSet<String>(), "");
        assertEquals(0, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInterpreterProcessClusteredShortOptions() throws Exception {
        Class c = Class.forName("picocli.CommandLine$Interpreter");
        Method processClusteredShortOptions = c.getDeclaredMethod("processClusteredShortOptions",
                Collection.class, Set.class, String.class, boolean.class, Stack.class);
        processClusteredShortOptions.setAccessible(true);

        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-x").arity("0").build());
        spec.parser().trimQuotes(true);
        CommandLine cmd = new CommandLine(spec);
        Object interpreter = TestUtil.interpreter(cmd);

        Stack<String> stack = new Stack<String>();
        String arg = "-xa";
        processClusteredShortOptions.invoke(interpreter, new ArrayList<ArgSpec>(), new HashSet<String>(), arg, true, stack);
        assertEquals(true, cmd.getParseResult().matchedOptionValue('x', null));
    }

    @Test
    public void testInterpreterProcessClusteredShortOptions_complex() {
        class App {
            @Option(names = "-x", arity = "1", split = ",") String x;
            @Parameters List<String> remainder;
        }
        App app = new App();
        System.setProperty("picocli.ignore.invalid.split", "");
        CommandLine cmd = new CommandLine(app);
        cmd.getCommandSpec().parser().aritySatisfiedByAttachedOptionParam(true);
        cmd.parseArgs("-xa,b,c", "d", "e");
        assertEquals("a,b,c", app.x);
        assertEquals(Arrays.asList("d", "e"), app.remainder);
    }

    @Test
    public void testAssertNoMissingParametersOption() {
        class App {
            @Option(names = "-x") int x;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.getCommandSpec().parser().collectErrors(true);
        ParseResult parseResult = cmd.parseArgs("-x");
        List<Exception> errors = parseResult.errors();
        assertEquals(1, errors.size());
        assertEquals("Missing required parameter for option '-x' (<x>)", errors.get(0).getMessage());
    }

    @Test
    public void testAssertNoMissingParametersPositional() {
        class App {
            @Parameters(arity = "1") int x;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.getCommandSpec().parser().collectErrors(true);
        ParseResult parseResult = cmd.parseArgs();
        List<Exception> errors = parseResult.errors();
        assertEquals(1, errors.size());
        assertEquals("Missing required parameter: '<x>'", errors.get(0).getMessage());
    }

    @Test
    public void testUpdateHelpRequested() {
        class App {
            @Option(names = "-x", help = true) boolean x;
        }
        System.setProperty("picocli.trace", "INFO");
        new CommandLine(new App()).parseArgs("-x");
        assertTrue(systemErrRule.getLog().contains("App.x has 'help' annotation: not validating required fields"));
    }

    @Test
    public void testVarargCanConsumeNextValue() {
        class App {
            @Parameters(arity = "*") List<String> all;
        }
        App app1 = CommandLine.populateCommand(new App(), "--", "a", "b");
        assertEquals(Arrays.asList("a", "b"), app1.all);
    }

    @Test
    public void testVarargCanConsumeNextValue2() {
        @Command(subcommands = HelpCommand.class)
        class App {
            @Option(names = "-x", arity = "*") List<String> x;
            @Option(names = "-y", arity = "*") List<Integer> y;
            @Unmatched List<String> unmatched;
        }

        App app = CommandLine.populateCommand(new App(), "--", "-x", "3", "a", "b");
        assertEquals(Arrays.asList("-x", "3", "a", "b"), app.unmatched);

        app = CommandLine.populateCommand(new App(), "-x", "3", "a", "b");
        assertEquals(Arrays.asList("3", "a", "b"), app.x);

        app = CommandLine.populateCommand(new App(), "-y", "3", "a", "b");
        assertNull(app.x);
        assertEquals(Arrays.asList(3), app.y);
        assertEquals(Arrays.asList("a", "b"), app.unmatched);

        app = CommandLine.populateCommand(new App(), "-y", "3", "-x", "a", "b");
        assertEquals(Arrays.asList("a", "b"), app.x);
        assertEquals(Arrays.asList(3), app.y);
        assertNull(app.unmatched);

        app = CommandLine.populateCommand(new App(), "-y", "3", "help", "a", "b");
        assertNull(app.x);
        assertEquals(Arrays.asList(3), app.y);
        assertNull(app.unmatched);
    }

    @Test(expected = MissingParameterException.class)
    public void testBooleanOptionDefaulting() {
        class App {
            @Option(names = "-h", usageHelp = true, defaultValue = "false")
            boolean helpAsked;

            @Option(names = "-V", versionHelp = true, defaultValue = "false")
            boolean versionAsked;

            @Parameters
            String compulsoryParameter;
        }
        //System.setProperty("picocli.trace", "DEBUG");
        CommandLine commandLine = new CommandLine(new App());
        commandLine.parseArgs(new String[0]);
    }

    @Test
    public void testIssue613SingleDashPositionalParam() {
        @Command(name = "dashtest", mixinStandardHelpOptions = true)
        class App {
            @Parameters(index = "0")
            private String json;
            @Parameters(index = "1")
            private String template;
        }
        System.setProperty("picocli.trace", "DEBUG");
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        //commandLine.setUnmatchedOptionsArePositionalParams(true);

        commandLine.parseArgs("-", "~/hello.mustache");
        assertEquals("-", app.json);
        assertEquals("~/hello.mustache", app.template);
        assertTrue(systemErrRule.getLog().contains("Single-character arguments that don't match known options are considered positional parameters"));
    }

    @Test
    public void testCaseInsensitiveToggle() {
        @Command
        class App {
            @Command(name = "help")
            public void helpCommand() {}

            @Option(names = "-h")
            public boolean helpMessage;
        }
        CommandLine commandLine = new CommandLine(new App());
        // Assert default behaviour. (defaults to false)
        assertFalse(commandLine.isSubcommandsCaseInsensitive());
        assertFalse(commandLine.isOptionsCaseInsensitive());
        commandLine.setSubcommandsCaseInsensitive(true);
        // Test set case-insensitivity twice.
        commandLine.setSubcommandsCaseInsensitive(true);
        assertTrue(commandLine.isSubcommandsCaseInsensitive());
        assertFalse(commandLine.isOptionsCaseInsensitive());
        commandLine.setOptionsCaseInsensitive(true);
        // Test set case-insensitivity twice.
        commandLine.setOptionsCaseInsensitive(true);
        assertTrue(commandLine.isSubcommandsCaseInsensitive());
        assertTrue(commandLine.isOptionsCaseInsensitive());
        commandLine.setSubcommandsCaseInsensitive(false);
        commandLine.setOptionsCaseInsensitive(false);
        assertFalse(commandLine.isSubcommandsCaseInsensitive());
        assertFalse(commandLine.isOptionsCaseInsensitive());
    }

    @Test
    public void testAddCaseInsensitiveDuplicateSubcommands() {
        @Command
        class App {
            @Command(name = "help")
            public void helpCommand() {}
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.setSubcommandsCaseInsensitive(true);
        try {
            commandLine.getCommandSpec().addSubcommand("HELP", CommandSpec.create());
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Another subcommand named 'help' already exists for command '<main class>'", ex.getMessage());
        }
    }

    @Test
    public void testToggleCaseInsensitiveDuplicateSubcommands() {
        @Command
        class App {
            @Command(name = "help")
            public void helpCommand() {}

            @Command(name = "HELP")
            public void helpCommand2() {}
        }
        CommandLine commandLine = new CommandLine(new App());
        try {
            commandLine.setSubcommandsCaseInsensitive(true);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Duplicated keys: help and HELP", ex.getMessage());
        }
    }


    @Test
    public void testAddCaseInsensitiveDuplicateOptions() {
        class App {
            @Option(names = "-h")
            boolean helpMessage;
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.setOptionsCaseInsensitive(true);
        try {
            commandLine.getCommandSpec().addOption(OptionSpec.builder("-H").build());
            fail("Expected exception");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().startsWith("Option name '-h' is used by both option -H and field boolean "));
        }
    }

    @Test
    public void testToggleCaseInsensitiveDuplicateOptions() {
        class App {
            @Option(names = "-h")
            boolean helpMessage;

            @Option(names = "-H")
            boolean highlight;
        }
        CommandLine commandLine = new CommandLine(new App());
        try {
            commandLine.setOptionsCaseInsensitive(true);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals("Duplicated keys: -h and -H", ex.getMessage());
        }
    }

    @Test
    public void testCaseInsensitiveParseResult() {
        @Command
        class App {
            @Option(names = "-h")
            boolean helpMessage;

            @Option(names = "-hElLo")
            boolean helloWorld;

            @Command(name = "help")
            public int helpCommand() { return 0; }
        }
        @Command(name = "ver")
        class VerCommand implements Callable<Integer> {
            public Integer call() { return 42; }
        }
        CommandLine commandLine = new CommandLine(new App());
        commandLine.getSubcommands().get("help").addSubcommand("ver", new VerCommand());
        commandLine.setSubcommandsCaseInsensitive(true);
        commandLine.setOptionsCaseInsensitive(true);
        ParseResult result = commandLine.parseArgs("-h", "-HeLLO");

        assertTrue(result.hasMatchedOption("-h"));
        assertTrue(result.hasMatchedOption("-hElLo"));
        assertFalse(result.hasMatchedOption("-H"));
        assertFalse(result.hasMatchedOption("-hello"));
        assertFalse(result.hasMatchedOption("-HELLO"));
        assertFalse(result.hasMatchedOption("-HeLLO"));

        int returnCode = commandLine.execute("HeLp", "VER");
        assertEquals(42, returnCode);
    }

    @Test
    public void testClose() {
        setTraceLevel(CommandLine.TraceLevel.WARN);
        CommandLine.close(new Closeable() {
            public void close() throws IOException {
                throw new IllegalStateException("booh!");
            }
        });
        String expected = String.format("[picocli WARN] Could not close picocli.CommandLineTest$2@: java.lang.IllegalStateException: booh!%n");
        assertEquals(expected, systemErrRule.getLog().replaceAll("@.*: java", "@: java"));
    }

    @Test
    public void testGetFactoryDefault1() {
        CommandLine cmd = new CommandLine(CompactFields.class);
        CommandLine.IFactory dflt = CommandLine.defaultFactory();
        assertNotSame(dflt, cmd.getFactory());
        assertEquals(dflt.getClass(), cmd.getFactory().getClass());
    }

    @Test
    public void testGetFactoryDefault2() {
        CommandLine cmd = new CommandLine(new CompactFields());
        CommandLine.IFactory dflt = CommandLine.defaultFactory();
        assertNotSame(dflt, cmd.getFactory());
        assertEquals(dflt.getClass(), cmd.getFactory().getClass());
    }

    @Test
    public void testGetFactoryCustom() {
        CommandLine.IFactory myFactory = new CommandLine.IFactory() {
            public <K> K create(Class<K> cls) {
                return null;
            }
        };
        CommandLine cmd = new CommandLine(CompactFields.class, myFactory);
        assertSame(myFactory, cmd.getFactory());

        CommandLine.IFactory dflt = CommandLine.defaultFactory();
        assertNotSame(dflt, cmd.getFactory());
        assertNotEquals(dflt.getClass(), cmd.getFactory().getClass());
    }
}
