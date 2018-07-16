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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.HelpTestUtil.setTraceLevel;

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

    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }

    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullObject() {
        new CommandLine(null);
    }
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullFactory() {
        new CommandLine(new CompactFields(), null);
    }
    @Test
    public void testFactory() {
        final Sub1_testDeclarativelyAddSubcommands sub1Command = new Sub1_testDeclarativelyAddSubcommands();
        final SubSub1_testDeclarativelyAddSubcommands subsub1Command = new SubSub1_testDeclarativelyAddSubcommands();
        IFactory factory = new IFactory() {
            public <T> T create(Class<T> cls) throws Exception {
                if (cls == Sub1_testDeclarativelyAddSubcommands.class) {
                    return (T) sub1Command;
                }
                if (cls == SubSub1_testDeclarativelyAddSubcommands.class) {
                    return (T) subsub1Command;
                }
                throw new IllegalStateException();
            }
        };
        CommandLine commandLine = new CommandLine(new MainCommand_testDeclarativelyAddSubcommands(), factory);
        CommandLine sub1 = commandLine.getSubcommands().get("sub1");
        assertSame(sub1Command, sub1.getCommand());

        CommandLine subsub1 = sub1.getSubcommands().get("subsub1");
        assertSame(subsub1Command, subsub1.getCommand());
    }
    @Test
    public void testFailingFactory() {
        IFactory factory = new IFactory() {
            public <T> T create(Class<T> cls) throws Exception {
                throw new IllegalStateException("bad class");
            }
        };
        try {
            new CommandLine(new MainCommand_testDeclarativelyAddSubcommands(), factory);
        } catch (InitializationException ex) {
            assertEquals("Could not instantiate and add subcommand " +
                    "picocli.CommandLineTest$Sub1_testDeclarativelyAddSubcommands: " +
                    "java.lang.IllegalStateException: bad class", ex.getMessage());
        }
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
    static class BadVersionProvider implements IVersionProvider {
        public BadVersionProvider() {
            throw new IllegalStateException("bad class");
        }
        public String[] getVersion() throws Exception { return new String[0]; }
    }
    @Test
    public void testFailingVersionProviderWithDefaultFactory() {
        @Command(versionProvider = BadVersionProvider.class)
        class App { }
        try {
            new CommandLine(new App());
        } catch (InitializationException ex) {
            assertEquals("Could not instantiate class " +
                    "picocli.CommandLineTest$BadVersionProvider: java.lang.reflect.InvocationTargetException", ex.getMessage());
        }
    }
    @Test
    public void testVersion() {
        assertEquals("3.3.1-SNAPSHOT", CommandLine.VERSION);
    }
    @Test
    public void testArrayPositionalParametersAreReplacedNotAppendedTo() {
        class ArrayPositionalParams {
            @Parameters() int[] array;
        }
        ArrayPositionalParams params = new ArrayPositionalParams();
        params.array = new int[3];
        int[] array = params.array;
        new CommandLine(params).parse("3", "2", "1");
        assertNotSame(array, params.array);
        assertArrayEquals(new int[]{3, 2, 1}, params.array);
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
    public void testListPositionalParametersAreReplacedIfNonNull() {
        ListPositionalParams params = new ListPositionalParams();
        params.list = new ArrayList<Integer>();
        params.list.add(234);
        List<Integer> list = params.list;
        new CommandLine(params).parse("3", "2", "1");
        assertNotSame(list, params.list);
        assertEquals(Arrays.asList(3, 2, 1), params.list);
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
    public void testSortedSetPositionalParametersAreReplacedIfNonNull() {
        SortedSetPositionalParams params = new SortedSetPositionalParams();
        params.sortedSet = new TreeSet<Integer>();
        params.sortedSet.add(234);
        SortedSet<Integer> list = params.sortedSet;
        new CommandLine(params).parse("3", "2", "1");
        assertNotSame(list, params.sortedSet);
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<Integer>(params.sortedSet));
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
    public void testSetPositionalParametersAreReplacedIfNonNull() {
        SetPositionalParams params = new SetPositionalParams();
        params.set = new TreeSet<Integer>();
        params.set.add(234);
        Set<Integer> list = params.set;
        new CommandLine(params).parse("3", "2", "1");
        assertNotSame(list, params.set);
        assertEquals(new HashSet(Arrays.asList(3, 2, 1)), params.set);
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
    public void testQueuePositionalParametersAreReplacedIfNonNull() {
        QueuePositionalParams params = new QueuePositionalParams();
        params.queue = new LinkedList<Integer>();
        params.queue.add(234);
        Queue<Integer> list = params.queue;
        new CommandLine(params).parse("3", "2", "1");
        assertNotSame(list, params.queue);
        assertEquals(new LinkedList(Arrays.asList(3, 2, 1)), params.queue);
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
    public void testCollectionPositionalParametersAreReplacedIfNonNull() {
        CollectionPositionalParams params = new CollectionPositionalParams();
        params.collection = new ArrayList<Integer>();
        params.collection.add(234);
        Collection<Integer> list = params.collection;
        new CommandLine(params).parse("3", "2", "1");
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
        setTraceLevel("OFF");
        CommandLine cmd = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        cmd.parse("-f", "111", "-f", "222");
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
        new CommandLine(fields).parse("-d=2017-11-02", "-u=MILLISECONDS", "123", "123456");
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2017-11-02"), fields.date);
        assertSame(TimeUnit.MILLISECONDS, fields.enumValue);
        assertEquals(Integer.valueOf(123), fields.integer);
        assertEquals(Long.valueOf(123456), fields.longValue);
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
            assertEquals("Unknown option: -p1", ex.getMessage());
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
    public void testSetSeparator_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals("=", commandLine.getSeparator());
        commandLine.setSeparator(":");
        assertEquals(":", commandLine.getSeparator());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", "=", sub.getSeparator());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", "=", subsub.getSeparator());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetSeparator_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals("=", commandLine.getSeparator());
        commandLine.setSeparator(":");
        assertEquals(":", commandLine.getSeparator());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", ":", sub.getSeparator());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", ":", sub.getSeparator());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUsageHelpWidth_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        int DEFAULT = Model.UsageMessageSpec.DEFAULT_USAGE_WIDTH;
        assertEquals(DEFAULT, commandLine.getUsageHelpWidth());
        commandLine.setUsageHelpWidth(120);
        assertEquals(120, commandLine.getUsageHelpWidth());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", DEFAULT, sub.getUsageHelpWidth());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", DEFAULT, subsub.getUsageHelpWidth());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUsageHelpWidth_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        int DEFAULT = Model.UsageMessageSpec.DEFAULT_USAGE_WIDTH;
        assertEquals(DEFAULT, commandLine.getUsageHelpWidth());
        commandLine.setUsageHelpWidth(120);
        assertEquals(120, commandLine.getUsageHelpWidth());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", 120, sub.getUsageHelpWidth());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", 120, sub.getUsageHelpWidth());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserToggleBooleanFlags_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(true, commandLine.isToggleBooleanFlags());
        commandLine.setToggleBooleanFlags(false);
        assertEquals(false, commandLine.isToggleBooleanFlags());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", true, sub.isToggleBooleanFlags());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", true, subsub.isToggleBooleanFlags());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserToggleBooleanFlags_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(true, commandLine.isToggleBooleanFlags());
        commandLine.setToggleBooleanFlags(false);
        assertEquals(false, commandLine.isToggleBooleanFlags());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", false, sub.isToggleBooleanFlags());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", false, sub.isToggleBooleanFlags());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserUnmatchedOptionsArePositionalParams_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isUnmatchedOptionsArePositionalParams());
        commandLine.setUnmatchedOptionsArePositionalParams(true);
        assertEquals(true, commandLine.isUnmatchedOptionsArePositionalParams());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isUnmatchedOptionsArePositionalParams());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isUnmatchedOptionsArePositionalParams());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserUnmatchedOptionsArePositionalParams_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isUnmatchedOptionsArePositionalParams());
        commandLine.setUnmatchedOptionsArePositionalParams(true);
        assertEquals(true, commandLine.isUnmatchedOptionsArePositionalParams());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isUnmatchedOptionsArePositionalParams());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isUnmatchedOptionsArePositionalParams());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
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
            assertEquals("Unknown option: -x", ok.getMessage());
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
    public void testParserPosixClustedShortOptions_false_resultsInShortClusteredOptionsNotRecognized() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-rvoFILE");
        verifyCompact(compact, true, true, "FILE", null);

        CommandLine cmd = new CommandLine(new CompactFields());
        cmd.getCommandSpec().parser().posixClusteredShortOptionsAllowed(false);
        try {
            cmd.parse("-rvoFILE");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: -rvoFILE", ex.getMessage());
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
            cmd.parse(args);
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: -oFILE", ex.getMessage());
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
        cmd.parse(args);
        verifyCompact(unclustered, true, true, "FILE", null);
    }

    @Test
    public void testParserPosixClustedShortOptions_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(true, commandLine.isPosixClusteredShortOptionsAllowed());
        commandLine.setPosixClusteredShortOptionsAllowed(false);
        assertEquals(false, commandLine.isPosixClusteredShortOptionsAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", true, sub.isPosixClusteredShortOptionsAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", true, subsub.isPosixClusteredShortOptionsAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserPosixClustedShortOptions_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(true, commandLine.isPosixClusteredShortOptionsAllowed());
        commandLine.setPosixClusteredShortOptionsAllowed(false);
        assertEquals(false, commandLine.isPosixClusteredShortOptionsAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", false, sub.isPosixClusteredShortOptionsAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", false, sub.isPosixClusteredShortOptionsAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testDoubleDashSeparatesPositionalParameters() {
        CompactFields compact = CommandLine.populateCommand(new CompactFields(), "-oout -- -r -v p1 p2".split(" "));
        verifyCompact(compact, false, false, "out", fileArray("-r", "-v", "p1", "p2"));
    }

    private static void clearBuiltInTracingCache() throws Exception {
        Field field = Class.forName("picocli.CommandLine$BuiltIn").getDeclaredField("traced");
        field.setAccessible(true);
        Collection<?> collection = (Collection<?>) field.get(null);
        collection.clear();
    }
    @Test
    public void testDebugOutputForDoubleDashSeparatesPositionalParameters() throws Exception {
        clearBuiltInTracingCache();
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
        String prefix8 = String.format("" +
                "[picocli DEBUG] Could not register converter for java.time.Duration: java.lang.ClassNotFoundException: java.time.Duration%n" +
                "[picocli DEBUG] Could not register converter for java.time.Instant: java.lang.ClassNotFoundException: java.time.Instant%n" +
                "[picocli DEBUG] Could not register converter for java.time.LocalDate: java.lang.ClassNotFoundException: java.time.LocalDate%n" +
                "[picocli DEBUG] Could not register converter for java.time.LocalDateTime: java.lang.ClassNotFoundException: java.time.LocalDateTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.LocalTime: java.lang.ClassNotFoundException: java.time.LocalTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.MonthDay: java.lang.ClassNotFoundException: java.time.MonthDay%n" +
                "[picocli DEBUG] Could not register converter for java.time.OffsetDateTime: java.lang.ClassNotFoundException: java.time.OffsetDateTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.OffsetTime: java.lang.ClassNotFoundException: java.time.OffsetTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.Period: java.lang.ClassNotFoundException: java.time.Period%n" +
                "[picocli DEBUG] Could not register converter for java.time.Year: java.lang.ClassNotFoundException: java.time.Year%n" +
                "[picocli DEBUG] Could not register converter for java.time.YearMonth: java.lang.ClassNotFoundException: java.time.YearMonth%n" +
                "[picocli DEBUG] Could not register converter for java.time.ZonedDateTime: java.lang.ClassNotFoundException: java.time.ZonedDateTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.ZoneId: java.lang.ClassNotFoundException: java.time.ZoneId%n" +
                "[picocli DEBUG] Could not register converter for java.time.ZoneOffset: java.lang.ClassNotFoundException: java.time.ZoneOffset%n");
        String prefix7 = String.format("" +
                "[picocli DEBUG] Could not register converter for java.nio.file.Path: java.lang.ClassNotFoundException: java.nio.file.Path%n");
        String expected = String.format("" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.CommandLineTest$CompactFields with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli INFO] Parsing 6 command line args [-oout, --, -r, -v, p1, p2]%n" +
                        "[picocli DEBUG] Parser configuration: posixClusteredShortOptionsAllowed=true, stopAtPositional=false, stopAtUnmatched=false, separator=null, overwrittenOptionsAllowed=false, unmatchedArgumentsAllowed=false, expandAtFiles=true, limitSplit=false, aritySatisfiedByAttachedOptionParam=false%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLineTest$CompactFields.verbose of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLineTest$CompactFields.recursive of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File picocli.CommandLineTest$CompactFields.outputFile of type class java.io.File to null.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles of type class [Ljava.io.File; to null.%n" +
                        "[picocli DEBUG] Initializing %1$s$CompactFields: 3 options, 1 positional parameters, 0 required, 0 subcommands.%n" +
                        "[picocli DEBUG] Processing argument '-oout'. Remainder=[--, -r, -v, p1, p2]%n" +
                        "[picocli DEBUG] '-oout' cannot be separated into <option>=<option-parameter>%n" +
                        "[picocli DEBUG] Trying to process '-oout' as clustered short options%n" +
                        "[picocli DEBUG] Found option '-o' in -oout: field java.io.File %1$s$CompactFields.outputFile, arity=1%n" +
                        "[picocli DEBUG] Trying to process 'out' as option parameter%n" +
                        "[picocli INFO] Setting field java.io.File picocli.CommandLineTest$CompactFields.outputFile to 'out' (was 'null') for option -o%n" +
                        "[picocli DEBUG] Processing argument '--'. Remainder=[-r, -v, p1, p2]%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=0. Remainder=[-r, -v, p1, p2]%n" +
                        "[picocli DEBUG] Position 0 is in index range 0..*. Trying to assign args to field java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [-r] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 0%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 1.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=1. Remainder=[-v, p1, p2]%n" +
                        "[picocli DEBUG] Position 1 is in index range 0..*. Trying to assign args to field java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [-v] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 1%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 2.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=2. Remainder=[p1, p2]%n" +
                        "[picocli DEBUG] Position 2 is in index range 0..*. Trying to assign args to field java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [p1] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 2%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 3.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=3. Remainder=[p2]%n" +
                        "[picocli DEBUG] Position 3 is in index range 0..*. Trying to assign args to field java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli INFO] Adding [p2] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 3%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 4.%n",
                CommandLineTest.class.getName(), new File("/home/rpopma/picocli"));
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        if (System.getProperty("java.version").compareTo("1.7.0") < 0) {
            expected = prefix7 + expected;
        }
        if (System.getProperty("java.version").compareTo("1.8.0") < 0) {
            expected = prefix8 + expected;
        }
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
            assertEquals("Could not convert '' to int for option '--dash'" +
                    ": java.lang.NumberFormatException: For input string: \"\"", ex.getMessage());
        }

        try {
            params = CommandLine.populateCommand(new VariousPrefixCharacters(), "--dash= /4".split(" "));
            fail("int option (with sep but no value, followed by other option) needs arg");
        } catch (ParameterException ex) {
            assertEquals("Could not convert '' to int for option '--dash'" +
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
            assertEquals("Unmatched argument: b", ex.getMessage());
        }
    }

    @Test
    public void testOptionParameterQuotesRemovedFromValue() {
        class TextOption {
            @Option(names = "-t") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t", "\"a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testLongOptionAttachedQuotedParameterQuotesRemovedFromValue() {
        class TextOption {
            @Option(names = "--text") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "--text=\"a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testShortOptionAttachedQuotedParameterQuotesRemovedFromValue() {
        class TextOption {
            @Option(names = "-t") String text;
        }
        TextOption opt = CommandLine.populateCommand(new TextOption(), "-t\"a text\"");
        assertEquals("a text", opt.text);

        opt = CommandLine.populateCommand(new TextOption(), "-t=\"a text\"");
        assertEquals("a text", opt.text);
    }

    @Test
    public void testShortOptionQuotedParameterTypeConversion() {
        class TextOption {
            @Option(names = "-t") int[] number;
            @Option(names = "-v", arity = "1") boolean verbose;
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
            @Option(names = "-t") String[] text;
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
            assertEquals("Unmatched argument: \"x z\"", ok.getMessage());
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
            @Option(names = "-p") String path;
        }
        class ChildOption extends ParentOption {
            @Option(names = "-t") String text;
        }
        ChildOption opt = CommandLine.populateCommand(new ChildOption(), "-p", "somePath", "-t", "\"a text\"");
        assertEquals("somePath", opt.path);
        assertEquals("a text", opt.text);
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
            String expected = String.format("Option name '-p' is used by both field String %s.path and field String %s.text",
                    ParentOption.class.getName(), ChildOption.class.getName());
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
                //null, null, null, null, // #216 default values are replaced
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
            assertEquals("Unmatched arguments: B, C", ok.getMessage());
        }
        try {
            args = CommandLine.populateCommand(new Args(), "-a=a,b,c", "B", "-a=C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument: B", ok.getMessage());
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
            assertEquals("Unmatched arguments: B, C", ok.getMessage());
        }
        try {
            args = CommandLine.populateCommand(new Args(), "-a=a b c", "B", "-a=C");
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument: B", ok.getMessage());
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
            assertEquals("Unmatched arguments: B, C", ok.getMessage());
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
            assertTrue(ex.getMissing().get(0).toString(), ex.getMissing().get(0) instanceof Model.PositionalParamSpec);
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
                        "[picocli INFO] Setting field java.io.File picocli.Demo$Git.gitDir to '%s' (was 'null') for option --git-dir%n" +
                        "[picocli INFO] Adding [Fixed typos] to field java.util.List<String> picocli.Demo$GitCommit.message for option -m%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli INFO] Adding [src1.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 0%n" +
                        "[picocli INFO] Adding [src2.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 1%n" +
                        "[picocli INFO] Adding [src3.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 2%n",
                new File("/home/rpopma/picocli"));
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
    }
    @Test
    public void testTracingDebugWithSubCommands() throws Exception {
        clearBuiltInTracingCache();
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
        String prefix8 = String.format("" +
                "[picocli DEBUG] Could not register converter for java.time.Duration: java.lang.ClassNotFoundException: java.time.Duration%n" +
                "[picocli DEBUG] Could not register converter for java.time.Instant: java.lang.ClassNotFoundException: java.time.Instant%n" +
                "[picocli DEBUG] Could not register converter for java.time.LocalDate: java.lang.ClassNotFoundException: java.time.LocalDate%n" +
                "[picocli DEBUG] Could not register converter for java.time.LocalDateTime: java.lang.ClassNotFoundException: java.time.LocalDateTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.LocalTime: java.lang.ClassNotFoundException: java.time.LocalTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.MonthDay: java.lang.ClassNotFoundException: java.time.MonthDay%n" +
                "[picocli DEBUG] Could not register converter for java.time.OffsetDateTime: java.lang.ClassNotFoundException: java.time.OffsetDateTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.OffsetTime: java.lang.ClassNotFoundException: java.time.OffsetTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.Period: java.lang.ClassNotFoundException: java.time.Period%n" +
                "[picocli DEBUG] Could not register converter for java.time.Year: java.lang.ClassNotFoundException: java.time.Year%n" +
                "[picocli DEBUG] Could not register converter for java.time.YearMonth: java.lang.ClassNotFoundException: java.time.YearMonth%n" +
                "[picocli DEBUG] Could not register converter for java.time.ZonedDateTime: java.lang.ClassNotFoundException: java.time.ZonedDateTime%n" +
                "[picocli DEBUG] Could not register converter for java.time.ZoneId: java.lang.ClassNotFoundException: java.time.ZoneId%n" +
                "[picocli DEBUG] Could not register converter for java.time.ZoneOffset: java.lang.ClassNotFoundException: java.time.ZoneOffset%n");
        String prefix7 = String.format("" +
                "[picocli DEBUG] Could not register converter for java.nio.file.Path: java.lang.ClassNotFoundException: java.nio.file.Path%n");
        String expected = String.format("" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$Git with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.CommandLine$AutoHelpMixin with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.CommandLine$HelpCommand with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitStatus with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitCommit with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitAdd with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitBranch with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitCheckout with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitClone with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitDiff with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitMerge with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitPush with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitRebase with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for object of class picocli.Demo$GitTag with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Parser configuration: posixClusteredShortOptionsAllowed=true, stopAtPositional=false, stopAtUnmatched=false, separator=null, overwrittenOptionsAllowed=false, unmatchedArgumentsAllowed=false, expandAtFiles=true, limitSplit=false, aritySatisfiedByAttachedOptionParam=false%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$Git.gitDir of type class java.io.File to null.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLine$AutoHelpMixin.helpRequested of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLine$AutoHelpMixin.versionRequested of type boolean to false.%n" +
                        "[picocli DEBUG] Initializing %1$s$Git: 3 options, 0 positional parameters, 0 required, 12 subcommands.%n" +
                        "[picocli DEBUG] Processing argument '--git-dir=/home/rpopma/picocli'. Remainder=[commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Separated '--git-dir' option from '/home/rpopma/picocli' option parameter%n" +
                        "[picocli DEBUG] Found option named '--git-dir': field java.io.File %1$s$Git.gitDir, arity=1%n" +
                        "[picocli INFO] Setting field java.io.File picocli.Demo$Git.gitDir to '%2$s' (was 'null') for option --git-dir%n" +
                        "[picocli DEBUG] Processing argument 'commit'. Remainder=[-m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Found subcommand 'commit' (%1$s$GitCommit)%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.Demo$GitCommit.all of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.Demo$GitCommit.patch of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.reuseMessageCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.reEditMessageCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.fixupCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.squashCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$GitCommit.file of type class java.io.File to null.%n" +
                        "[picocli DEBUG] Set initial value for field java.util.List<String> picocli.Demo$GitCommit.message of type interface java.util.List to [].%n" +
                        "[picocli DEBUG] Set initial value for field java.util.List<java.io.File> picocli.Demo$GitCommit.files of type interface java.util.List to [].%n" +
                        "[picocli DEBUG] Initializing %1$s$GitCommit: 8 options, 1 positional parameters, 0 required, 0 subcommands.%n" +
                        "[picocli DEBUG] Processing argument '-m'. Remainder=[\"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] '-m' cannot be separated into <option>=<option-parameter>%n" +
                        "[picocli DEBUG] Found option named '-m': field java.util.List<String> %1$s$GitCommit.message, arity=1%n" +
                        "[picocli INFO] Adding [Fixed typos] to field java.util.List<String> picocli.Demo$GitCommit.message for option -m%n" +
                        "[picocli DEBUG] Processing argument '--'. Remainder=[src1.java, src2.java, src3.java]%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=0. Remainder=[src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Position 0 is in index range 0..*. Trying to assign args to field java.util.List<java.io.File> %1$s$GitCommit.files, arity=0..1%n" +
                        "[picocli INFO] Adding [src1.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 0%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 1.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=1. Remainder=[src2.java, src3.java]%n" +
                        "[picocli DEBUG] Position 1 is in index range 0..*. Trying to assign args to field java.util.List<java.io.File> %1$s$GitCommit.files, arity=0..1%n" +
                        "[picocli INFO] Adding [src2.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 1%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 2.%n" +
                        "[picocli DEBUG] Processing next arg as a positional parameter at index=2. Remainder=[src3.java]%n" +
                        "[picocli DEBUG] Position 2 is in index range 0..*. Trying to assign args to field java.util.List<java.io.File> %1$s$GitCommit.files, arity=0..1%n" +
                        "[picocli INFO] Adding [src3.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 2%n" +
                        "[picocli DEBUG] Consumed 1 arguments, moving position to index 3.%n",
                Demo.class.getName(), new File("/home/rpopma/picocli"));
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        if (System.getProperty("java.version").compareTo("1.7.0") < 0) {
            expected = prefix7 + expected;
        }
        if (System.getProperty("java.version").compareTo("1.8.0") < 0) {
            expected = prefix8 + expected;
        }
        assertEquals(expected, actual);
    }
    @Test
    public void testTraceWarningIfOptionOverwrittenWhenOverwrittenOptionsAllowed() throws Exception {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        setTraceLevel("INFO");
        class App {
            @Option(names = "-f") String field = null;
            @Option(names = "-p") int primitive = 43;
        }
        CommandLine cmd = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        cmd.parse("-f", "111", "-f", "222", "-f", "333");
        App ff = cmd.getCommand();
        assertEquals("333", ff.field);
        System.setErr(originalErr);

        String expected = String.format("" +
                        "[picocli INFO] Parsing 6 command line args [-f, 111, -f, 222, -f, 333]%n" +
                        "[picocli INFO] Setting field String picocli.CommandLineTest$16App.field to '111' (was 'null') for option -f%n" +
                        "[picocli INFO] Overwriting field String picocli.CommandLineTest$16App.field value '111' with '222' for option -f%n" +
                        "[picocli INFO] Overwriting field String picocli.CommandLineTest$16App.field value '222' with '333' for option -f%n",
                App.class.getName());
        String actual = new String(baos.toByteArray(), "UTF8");
        assertEquals(expected, actual);
        setTraceLevel("WARN");
    }
    @Test
    public void testTraceWarningIfUnmatchedArgsWhenUnmatchedArgumentsAllowed() throws Exception {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        setTraceLevel("INFO");
        class App {
            @Parameters(index = "0", arity = "2", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true).parse("1=a", "2=b", "3=c", "4=d").get(0);
        assertEquals(Arrays.asList("3=c", "4=d"), cmd.getUnmatchedArguments());
        System.setErr(originalErr);

        String expected = String.format("[picocli INFO] Parsing 4 command line args [1=a, 2=b, 3=c, 4=d]%n" +
                "[picocli INFO] Putting [1 : a] in LinkedHashMap<Integer, String> field java.util.Map<Integer, String> picocli.CommandLineTest$17App.message for args[0] at position 0%n" +
                "[picocli INFO] Putting [2 : b] in LinkedHashMap<Integer, String> field java.util.Map<Integer, String> picocli.CommandLineTest$17App.message for args[0] at position 0%n" +
                "[picocli INFO] Unmatched arguments: [3=c, 4=d]%n");
        String actual = new String(baos.toByteArray(), "UTF8");
        //System.out.println(actual);
        assertEquals(expected, actual);
        setTraceLevel("WARN");
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

    private static CommandLine createNestedCommandWithAliases() {
        CommandLine commandLine = new CommandLine(new MainCommand());
        commandLine
                .addSubcommand("cmd1", new CommandLine(new ChildCommand1())
                        .addSubcommand("sub11", new GrandChild1Command1(), "sub11alias1", "sub11alias2")
                        .addSubcommand("sub12", new GrandChild1Command2(), "sub12alias1", "sub12alias2")
                        , "cmd1alias1", "cmd1alias2")
                .addSubcommand("cmd2", new CommandLine(new ChildCommand2())
                        .addSubcommand("sub21", new GrandChild2Command1(), "sub21alias1", "sub21alias2")
                        .addSubcommand("sub22", new CommandLine(new GrandChild2Command2())
                                .addSubcommand("sub22sub1", new GreatGrandChild2Command2_1(), "sub22sub1alias1", "sub22sub1alias2"), "sub22alias1", "sub22alias2")
                        , "cmd2alias1", "cmd2alias2");
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
    public void testCommandListReturnsAliases() {
        CommandLine commandLine = createNestedCommandWithAliases();

        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertEquals(6, commandMap.size());
        assertEquals(setOf("cmd1", "cmd1alias1", "cmd1alias2", "cmd2", "cmd2alias1", "cmd2alias2"), commandMap.keySet());
        assertTrue("cmd1", commandMap.get("cmd1").getCommand() instanceof ChildCommand1);
        assertSame(commandMap.get("cmd1"), commandMap.get("cmd1alias1"));
        assertSame(commandMap.get("cmd1"), commandMap.get("cmd1alias2"));

        assertTrue("cmd2", commandMap.get("cmd2").getCommand() instanceof ChildCommand2);
        assertSame(commandMap.get("cmd2"), commandMap.get("cmd2alias1"));
        assertSame(commandMap.get("cmd2"), commandMap.get("cmd2alias2"));

        CommandLine cmd2 = commandMap.get("cmd2");
        Map<String, CommandLine> subMap = cmd2.getSubcommands();

        assertTrue("cmd2", subMap.get("sub21").getCommand() instanceof GrandChild2Command1);
        assertSame(subMap.get("sub21"), subMap.get("sub21alias1"));
        assertSame(subMap.get("sub21"), subMap.get("sub21alias2"));
    }

    public static <T> Set<T> setOf(T... elements) {
        Set<T> result = new HashSet<T>();
        for (T t : elements) { result.add(t); }
        return result;
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
            assertEquals("Unmatched argument: sub12", ex.getMessage());
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
            assertEquals("Unknown option: -b", ex.getMessage());
        }
        try {
            createNestedCommand().parse("cmd1", "sub21");
            fail("sub-commands for different parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument: sub21", ex.getMessage());
        }
        try {
            createNestedCommand().parse("cmd1", "sub22sub1");
            fail("sub-sub-commands for different parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument: sub22sub1", ex.getMessage());
        }
        try {
            createNestedCommand().parse("sub11");
            fail("sub-commands without preceding parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument: sub11", ex.getMessage());
        }
        try {
            createNestedCommand().parse("sub21");
            fail("sub-commands without preceding parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument: sub21", ex.getMessage());
        }
        try {
            createNestedCommand().parse("sub22sub1");
            fail("sub-sub-commands without preceding parent/grandparent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument: sub22sub1", ex.getMessage());
        }
    }

    @Test
    public void testParseNestedSubCommandsWithAliases() {
        // valid
        List<CommandLine> main = createNestedCommandWithAliases().parse("cmd1alias1");
        assertEquals(2, main.size());
        assertFalse(((MainCommand)   main.get(0).getCommand()).a);
        assertFalse(((ChildCommand1) main.get(1).getCommand()).b);

        List<CommandLine> mainWithOptions = createNestedCommandWithAliases().parse("-a", "cmd1alias2", "-b");
        assertEquals(2, mainWithOptions.size());
        assertTrue(((MainCommand)   mainWithOptions.get(0).getCommand()).a);
        assertTrue(((ChildCommand1) mainWithOptions.get(1).getCommand()).b);

        List<CommandLine> sub1 = createNestedCommandWithAliases().parse("cmd1", "sub11");
        assertEquals(3, sub1.size());
        assertFalse(((MainCommand)         sub1.get(0).getCommand()).a);
        assertFalse(((ChildCommand1)       sub1.get(1).getCommand()).b);
        assertFalse(((GrandChild1Command1) sub1.get(2).getCommand()).d);

        List<CommandLine> sub1WithOptions = createNestedCommandWithAliases().parse("-a", "cmd1alias1", "-b", "sub11alias2", "-d");
        assertEquals(3, sub1WithOptions.size());
        assertTrue(((MainCommand)         sub1WithOptions.get(0).getCommand()).a);
        assertTrue(((ChildCommand1)       sub1WithOptions.get(1).getCommand()).b);
        assertTrue(((GrandChild1Command1) sub1WithOptions.get(2).getCommand()).d);

        // sub12 is not nested under sub11 so is not recognized
        try {
            createNestedCommandWithAliases().parse("cmd1alias1", "sub11alias1", "sub12alias1");
            fail("Expected exception for sub12alias1");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument: sub12alias1", ex.getMessage());
        }
        List<CommandLine> sub22sub1 = createNestedCommandWithAliases().parse("cmd2alias1", "sub22alias2", "sub22sub1alias1");
        assertEquals(4, sub22sub1.size());
        assertFalse(((MainCommand)                sub22sub1.get(0).getCommand()).a);
        assertFalse(((ChildCommand2)              sub22sub1.get(1).getCommand()).c);
        assertFalse(((GrandChild2Command2)        sub22sub1.get(2).getCommand()).g);
        assertFalse(((GreatGrandChild2Command2_1) sub22sub1.get(3).getCommand()).h);

        List<CommandLine> sub22sub1WithOptions = createNestedCommandWithAliases().parse("-a", "cmd2alias1", "-c", "sub22alias1", "-g", "sub22sub1alias2", "-h");
        assertEquals(4, sub22sub1WithOptions.size());
        assertTrue(((MainCommand)                sub22sub1WithOptions.get(0).getCommand()).a);
        assertTrue(((ChildCommand2)              sub22sub1WithOptions.get(1).getCommand()).c);
        assertTrue(((GrandChild2Command2)        sub22sub1WithOptions.get(2).getCommand()).g);
        assertTrue(((GreatGrandChild2Command2_1) sub22sub1WithOptions.get(3).getCommand()).h);
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
            assertEquals("option '--str' (<string>) should be specified only once", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-v", "--verbose");
            fail("expected exception");
        } catch (OverwrittenOptionException ex) {
            assertEquals("option '--verbose' (<bool>) should be specified only once", ex.getMessage());
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
        setTraceLevel("OFF");
        CommandLine commandLine = new CommandLine(new App())
                .addSubcommand("parent", new Parent())
                .setOverwrittenOptionsAllowed(true);
        commandLine.parse("-s", "1", "--str", "2", "parent", "--parent", "parentVal", "--parent", "2ndVal");

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
            commandLine.parse("-u", "foo");
            fail("expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '--password=<password>'", ex.getLocalizedMessage());
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
                        "      --foo-bar-baz=<foo>     Default:\n" +
                        "                              aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                        "                              aaaaaaaaaaaaaaaaaaaaaaaaaa\n";

        assertEquals(expectedOutput, content);
    }
    /** Subcommand with default constructor */
    @Command(name = "subsub1")
    static class SubSub1_testDeclarativelyAddSubcommands {}

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
    public void testDeclarativelyAddSubcommandsSucceedsWithDefaultConstructorForDefaultFactory() {
        @Command(subcommands = {SubSub1_testDeclarativelyAddSubcommands.class}) class MainCommand {}
        CommandLine cmdLine = new CommandLine(new MainCommand());
        assertEquals(SubSub1_testDeclarativelyAddSubcommands.class.getName(), cmdLine.getSubcommands().get("subsub1").getCommand().getClass().getName());
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutNoArgConstructor() {
        @Command(name = "sub1") class ABC { public ABC(String constructorParam) {} }
        @Command(subcommands = {ABC.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand(), new InnerClassFactory(this));
            fail("Expected exception");
        } catch (InitializationException ex) {
            String prefix = String.format("Could not instantiate %s either with or without construction parameter picocli.CommandLineTest@", ABC.class.getName());
            String suffix = String.format("java.lang.NoSuchMethodException: %s.<init>(picocli.CommandLineTest)", ABC.class.getName());

            assertTrue(ex.getMessage(), ex.getMessage().startsWith(prefix));
            assertTrue(ex.getMessage(), ex.getMessage().endsWith(suffix));
        }
    }
    @Test
    public void testDeclarativelyAddSubcommandsSucceedsWithDefaultConstructor() {
        @Command(name = "sub1") class ABCD {}
        @Command(subcommands = {ABCD.class}) class MainCommand {}
        CommandLine cmdLine = new CommandLine(new MainCommand(), new InnerClassFactory(this));
        assertEquals("picocli.CommandLineTest$1ABCD", cmdLine.getSubcommands().get("sub1").getCommand().getClass().getName());
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutAnnotation() {
        class MissingCommandAnnotation { public MissingCommandAnnotation() {} }
        @Command(subcommands = {MissingCommandAnnotation.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand(), new InnerClassFactory(this));
            fail("Expected exception");
        } catch (InitializationException ex) {
            String expected = String.format("%s is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations", MissingCommandAnnotation.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutNameOnCommandAnnotation() {
        @Command class MissingNameAttribute{ public MissingNameAttribute() {} }
        @Command(subcommands = {MissingNameAttribute.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand(), new InnerClassFactory(this));
            fail("Expected exception");
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
            assertEquals("Unmatched arguments: CCC=DDD, EEE=FFF", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-map=AAA=BBB", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched arguments: CCC=DDD, EEE=FFF", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-PAAA=BBB", "-PCCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument: EEE=FFF", ok.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-P", "AAA=BBB", "-P", "CCC=DDD", "EEE=FFF").validateMapField3Values();
            fail("Expected UnmatchedArgEx");
        } catch (UnmatchedArgumentException ok) {
            assertEquals("Unmatched argument: EEE=FFF", ok.getMessage());
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
            assertEquals("Unmatched argument: 3=c|4=d", ex.getMessage());
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
            assertEquals("Unmatched arguments: 3=c, 4=d", ex.getMessage());
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
            assertEquals("Unmatched argument: 4=d", ex.getMessage());
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
            assertEquals("Missing required options [-o=OUT_FILE, params[0..*]=IN_FILE]", ex.getMessage());
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
            assertEquals("Missing required options [-o=<String=String>, -x=KEY=VAL, params[0..*]=<Long=File>]", ex.getMessage());
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
            assertEquals("Unknown option: -xx", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-x", "-a", "aValue");
            fail("UnmatchedArgumentException expected for -x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: -x", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "--x", "-a", "aValue");
            fail("UnmatchedArgumentException expected for --x");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: --x", ex.getMessage());
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
            assertEquals("Unknown option: --ccd", ex.getMessage());
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
        setTraceLevel("INFO");
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
                "[picocli INFO] Parsing 2 command line args [-yy, -a=A]%n" +
                "[picocli INFO] Setting field String picocli.CommandLineTest$47App.first to 'A' (was 'null') for option -a%n" +
                "[picocli INFO] Unmatched arguments: [-yy]%n" +
                "[picocli INFO] Parsing 2 command line args [-y, -b=B]%n" +
                "[picocli INFO] Setting field String picocli.CommandLineTest$47App.second to 'B' (was 'null') for option -b%n" +
                "[picocli INFO] Unmatched arguments: [-y]%n" +
                "[picocli INFO] Parsing 2 command line args [--y, -c=C]%n" +
                "[picocli INFO] Setting field String picocli.CommandLineTest$47App.third to 'C' (was 'null') for option -c%n" +
                "[picocli INFO] Unmatched arguments: [--y]%n");
        String actual = new String(baos.toByteArray(), "UTF8");
        assertEquals(expected, actual);
        setTraceLevel("WARN");
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
    public void testSetStopAtUnmatched_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertFalse(commandLine.isStopAtUnmatched());
        commandLine.setStopAtUnmatched(true);
        assertTrue(commandLine.isStopAtUnmatched());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertFalse("subcommand added afterwards is not impacted", sub.isStopAtUnmatched());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertFalse("subcommand added afterwards is not impacted", subsub.isStopAtUnmatched());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetStopAtUnmatched_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertFalse(commandLine.isStopAtUnmatched());
        commandLine.setStopAtUnmatched(true);
        assertTrue(commandLine.isStopAtUnmatched());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertTrue(sub.isStopAtUnmatched());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertTrue(subsub.isStopAtUnmatched());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testStopAtUnmatched_UnmatchedOption() {
        setTraceLevel("OFF");
        class App {
            @Option(names = "-a") String first;
            @Parameters String[] positional;
        }
        App cmd1 = new App();
        CommandLine commandLine1 = new CommandLine(cmd1).setStopAtUnmatched(true);
        commandLine1.parse("--y", "-a=abc", "positional");
        assertEquals(Arrays.asList("--y", "-a=abc", "positional"), commandLine1.getUnmatchedArguments());
        assertNull(cmd1.first);
        assertNull(cmd1.positional);

        try {
            // StopAtUnmatched=false, UnmatchedArgumentsAllowed=false
            new CommandLine(new App()).parse("--y", "-a=abc", "positional");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: --y", ex.getMessage());
        }
        App cmd2 = new App();
        CommandLine commandLine2 = new CommandLine(cmd2).setStopAtUnmatched(false).setUnmatchedArgumentsAllowed(true);
        commandLine2.parse("--y", "-a=abc", "positional");
        assertEquals(Arrays.asList("--y"), commandLine2.getUnmatchedArguments());
        assertEquals("abc", cmd2.first);
        assertArrayEquals(new String[]{"positional"}, cmd2.positional);
        setTraceLevel("WARN");

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
    public void testSetStopAtPositional_BeforeSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertFalse(commandLine.isStopAtPositional());
        commandLine.setStopAtPositional(true);
        assertTrue(commandLine.isStopAtPositional());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertFalse("subcommand added afterwards is not impacted", sub.isStopAtPositional());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertFalse("subcommand added afterwards is not impacted", subsub.isStopAtPositional());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetStopAtPositional_AfterSubcommandsAdded() {
        @Command class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertFalse(commandLine.isStopAtPositional());
        commandLine.setStopAtPositional(true);
        assertTrue(commandLine.isStopAtPositional());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertTrue(sub.isStopAtPositional());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertTrue(subsub.isStopAtPositional());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testStopAtPositional_TreatsOptionsAfterPositionalAsPositional() {
        class App {
            @Option(names = "-a") String first;
            @Parameters String[] positional;
        }
        App cmd1 = new App();
        CommandLine commandLine1 = new CommandLine(cmd1).setStopAtPositional(true);
        commandLine1.parse("positional", "-a=abc", "positional");
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
            new CommandLine(new Example()).parse("-o /tmp");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <inputFiles>", ex.getMessage());
        }
        try {
            // Comment from AshwinJay : "Should've failed as inputFiles were not provided"
            //
            // RP: After removing `usageHelp = true`, the ["-o", " /tmp"] arguments are parsed and
            // a MissingParameterException is thrown for the missing <inputFiles>, as expected.
            new CommandLine(new Example()).parse("-o", " /tmp");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <inputFiles>", ex.getMessage());
        }
        try {
            // a MissingParameterException is thrown for missing required option -o, as expected
            new CommandLine(new Example()).parse("inputfile1", "inputfile2");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '--out-dir=<outputDir>'", ex.getMessage());
        }

        // a single empty string parameter was specified: this becomes an <inputFile> value
        try {
            new CommandLine(new Example()).parse("");
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '--out-dir=<outputDir>'", ex.getMessage());
        }

        // no parameters were specified
        try {
            new CommandLine(new Example()).parse();
            fail("Expected MissingParameterException");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required options [--out-dir=<outputDir>, params[0..*]=<inputFiles>]", ex.getMessage());
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

        setTraceLevel("DEBUG");
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

    private File findFile(String resource) {
        URL url = this.getClass().getResource(resource);
        assertNotNull(resource, url);
        String str = url.toString();
        return new File(str.substring(str.indexOf("file:") + 5));
    }

    @Test
    public void testAtFileExpandedAbsolute() {
        class App {
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "@" + file.getAbsolutePath());
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", "3333"), app.files);
    }

    @Test
    public void testAtFileNotExpandedIfDisabled() {
        class App {
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        assertTrue(file.getAbsoluteFile().exists());
        App app = new App();
        new CommandLine(app).setExpandAtFiles(false).parse("@" + file.getAbsolutePath());
        assertFalse(app.verbose);
        assertEquals(Arrays.asList("@" + file.getAbsolutePath()), app.files);
    }

    @Test
    public void testAtFileExpansionEnabledByDefault() {
        @Command class App { }
        assertTrue(new CommandLine(new App()).isExpandAtFiles());
    }

    @Test
    public void testAtFileExpandedRelative() {
        class App {
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        if (!file.getAbsolutePath().startsWith(System.getProperty("user.dir"))) {
            return;
        }
        String relative = file.getAbsolutePath().substring(System.getProperty("user.dir").length());
        if (relative.startsWith(File.separator)) {
            relative = relative.substring(File.separator.length());
        }
        App app = CommandLine.populateCommand(new App(), "@" + relative);
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", "3333"), app.files);
    }

    @Test
    public void testAtFileExpandedMixedWithOtherParams() {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "-f", "fVal1", "@" + file.getAbsolutePath(), "-x", "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", "3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileWithMultipleValuesPerLine() {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile3-multipleValuesPerLine.txt");
        App app = CommandLine.populateCommand(new App(), "-f", "fVal1", "@" + file.getAbsolutePath(), "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", "3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "FFFF", "F2F2F2", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileWithQuotedValuesContainingWhitespace() {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        setTraceLevel("OFF");
        File file = findFile("/argfile4-quotedValuesContainingWhitespace.txt");
        App app = CommandLine.populateCommand(new App(), "-f", "fVal1", "@" + file.getAbsolutePath(), "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("11 11", "22\n22", "3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "F F F F", "F2 F2 F2", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileWithExcapedAtValues() {
        class App {
            @Parameters
            private List<String> files;
        }
        setTraceLevel("OFF");
        File file = findFile("/argfile5-escapedAtValues.txt");
        App app = CommandLine.populateCommand(new App(), "aa", "@" + file.getAbsolutePath(), "bb");
        assertEquals(Arrays.asList("aa", "@val1", "@argfile5-escapedAtValues.txt", "bb"), app.files);
    }

    @Test
    public void testEscapedAtFileIsUnescapedButNotExpanded() {
        class App {
            @Parameters
            private List<String> files;
        }
        setTraceLevel("OFF");
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "aa", "@@" + file.getAbsolutePath(), "bb");
        assertEquals(Arrays.asList("aa", "@" + file.getAbsolutePath(), "bb"), app.files);
    }

    @Test
    public void testMultipleAtFilesExpandedMixedWithOtherParams() {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        File file2 = findFile("/argfile2.txt");

        setTraceLevel("OFF");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-f", "fVal1", "@" + file.getAbsolutePath(), "-x", "@" + file2.getAbsolutePath(),  "-f", "fVal2");
        assertFalse("invoked twice", app.verbose);
        assertEquals(Arrays.asList("1111", "2222", "3333", "1111", "2222", "3333"), app.files);
        assertFalse("invoked twice", app.xxx);
        assertArrayEquals(new String[]{"fVal1", "FFFF", "F2F2F2", "fVal2"}, app.fff);
    }

    @Test
    public void testNestedAtFile() throws IOException {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-nested-at-file.txt");
        File file2 = findFile("/argfile2.txt");
        File nested = new File("argfile2.txt");
        nested.delete();
        assertFalse("does not exist yet", nested.exists());
        copyFile(file2, nested);

        setTraceLevel("OFF");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-f", "fVal1", "@" + file.getAbsolutePath(),  "-f", "fVal2");
        assertTrue("invoked in argFile2", app.verbose);
        assertEquals(Arrays.asList("abcdefg", "1111", "2222", "3333"), app.files);
        assertTrue("invoked in argFile2", app.xxx);
        assertArrayEquals(new String[]{"fVal1", "FFFF", "F2F2F2", "fVal2"}, app.fff);
        assertTrue("Deleted " + nested, nested.delete());
    }

    @Test
    public void testRecursiveNestedAtFileIgnored() throws IOException {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-recursive-at-file.txt");
        File localCopy = new File("argfile-with-recursive-at-file.txt");
        localCopy.delete();
        assertFalse("does not exist yet", localCopy.exists());
        copyFile(file, localCopy);

        setTraceLevel("OFF");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-f", "fVal1", "@" + localCopy.getAbsolutePath(),  "-f", "fVal2");
        assertEquals(Arrays.asList("abc defg", "xyz"), app.files);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
        assertFalse("not invoked", app.verbose);
        assertFalse("not invoked", app.xxx);
        assertTrue("Deleted " + localCopy, localCopy.delete());
    }

    @Test
    public void testNestedAtFileNotFound() throws IOException {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-nested-at-file.txt");
        File nested = new File("argfile2.txt");
        nested.delete();
        assertFalse(nested + " does not exist", nested.exists());

        setTraceLevel("OFF");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parse("-f", "fVal1", "@" + file.getAbsolutePath(),  "-f", "fVal2");
        assertEquals(Arrays.asList("abcdefg", "@" + nested.getName()), app.files);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
        assertFalse("never invoked", app.verbose);
        assertFalse("never invoked", app.xxx);
    }

    private void copyFile(File source, File destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            byte[] buff = new byte[(int) source.length()];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            if (in != null) { try { in.close(); } catch (Exception ignored) {} }
            if (out != null) { try { out.close(); } catch (Exception ignored) {} }
        }
    }

    @Test
    public void testUnmatchedAnnotationInstantiatesList() {
        setTraceLevel("OFF");
        class App {
            @Unmatched List<String> unmatched;
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parse("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched);
    }

    @Test
    public void testUnmatchedAnnotationInstantiatesArray() {
        setTraceLevel("OFF");
        class App {
            @Unmatched String[] unmatched;
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parse("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertArrayEquals(new String[]{"-t", "-x", "abc"}, app.unmatched);
    }

    @Test
    public void testMultipleUnmatchedAnnotations() {
        setTraceLevel("OFF");
        class App {
            @Unmatched String[] unmatched1;
            @Unmatched String[] unmatched2;
            @Unmatched List<String> unmatched3;
            @Unmatched List<String> unmatched4;
            @Option(names = "-o") String option;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parse("-t", "-x", "abc");
        assertEquals(Arrays.asList("-t", "-x", "abc"), commandLine.getUnmatchedArguments());
        assertArrayEquals(new String[]{"-t", "-x", "abc"}, app.unmatched1);
        assertArrayEquals(new String[]{"-t", "-x", "abc"}, app.unmatched2);
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched3);
        assertEquals(Arrays.asList("-t", "-x", "abc"), app.unmatched4);
    }

    @Test
    public void testCommandAllowsOnlyUnmatchedAnnotation() {
        setTraceLevel("OFF");
        class App {
            @Unmatched String[] unmatched;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.parse("a", "b");
        assertEquals(Arrays.asList("a", "b"), cmd.getUnmatchedArguments());
    }

    @Test
    public void testUnmatchedAnnotationWithInvalidType_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched String unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "Invalid type for %s: must be either String[] or List<String>";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
        }
    }

    @Test
    public void testUnmatchedAnnotationWithInvalidGenericType_ThrowsException() throws Exception {
        @Command class App {
            @Unmatched List<Object> unmatched;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            String pattern = "Invalid type for %s: must be either String[] or List<String>";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
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
            String pattern = "A member cannot have both @Unmatched and @Option or @Parameters annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
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
            String pattern = "A member cannot have both @Unmatched and @Option or @Parameters annotations, but '%s' has both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
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
            String pattern = "A member cannot be both a @Mixin command and an @Unmatched but '%s' is both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
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
            String pattern = "A member cannot be both a @Mixin command and an @Option or @Parameters, but '%s' is both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
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
            String pattern = "A member cannot be both a @Mixin command and an @Option or @Parameters, but '%s' is both.";
            Field f = App.class.getDeclaredField("unmatched");
            assertEquals(String.format(pattern, f), ex.getMessage());
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
        commandLine.parse("help");
    }

    @Test
    public void testBuiltInHelpCommandMakesRequiredOptionsOptional() {
        @Command(subcommands = HelpCommand.class)
        class Parent {
            @Option(names = "-m", required = true)
            String mandatory;
        }
        CommandLine commandLine = new CommandLine(new Parent(), new InnerClassFactory(this));
        commandLine.parse("help");
    }

    @Test
    public void testAutoHelpOptionMakesRequiredOptionsOptional() {
        @Command(mixinStandardHelpOptions = true)
        class Parent {
            @Option(names = "-m", required = true)
            String mandatory;
        }
        CommandLine commandLine = new CommandLine(new Parent(), new InnerClassFactory(this));
        commandLine.parse("--help");
        assertTrue("No exceptions", true);
    }

    @Test
    public void testToggleBooleanFlagsByDefault() {
        class Flags {
            @Option(names = "-a") boolean a;
            @Option(names = "-b") boolean b = true;
            @Parameters(index = "0") boolean p0;
            @Parameters(index = "1") boolean p1 = true;
        }
        Flags flags = new Flags();
        CommandLine commandLine = new CommandLine(flags);
        assertFalse(flags.a);
        assertFalse(flags.p0);
        assertTrue (flags.b);
        assertTrue (flags.p1);
        commandLine.parse("-a", "-b", "true", "false");
        assertFalse(!flags.a);
        assertTrue (!flags.b);
        assertFalse(!flags.p0);
        assertTrue (!flags.p1);
        commandLine.parse("-a", "-b", "true", "false");
        assertFalse(!flags.a);
        assertTrue (!flags.b);
        assertFalse(!flags.p0);
        assertTrue (!flags.p1);
    }

    @Test
    public void testNoToggleBooleanFlagsWhenSwitchedOff() {
        class Flags {
            @Option(names = "-a") boolean a;
            @Option(names = "-b") boolean b = true;
            @Parameters(index = "0") boolean p0;
            @Parameters(index = "1") boolean p1 = true;
        }
        Flags flags = new Flags();
        CommandLine commandLine = new CommandLine(flags);
        commandLine.setToggleBooleanFlags(false);
        // initial
        assertFalse(flags.a);
        assertFalse(flags.p0);
        assertTrue (flags.b);
        assertTrue (flags.p1);
        commandLine.parse("-a", "-b", "true", "false");
        assertTrue(flags.a);
        assertTrue (flags.b);
        assertTrue (flags.p0);
        assertFalse(flags.p1);
        commandLine.parse("-a", "-b", "true", "false");
        assertTrue(flags.a);
        assertTrue (flags.b);
        assertTrue (flags.p0);
        assertFalse(flags.p1);
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

        args = new String[] {"-p", "\"AppOptions=-Dspring.profiles.active=test -Dspring.mail.host=smtp.mailtrap.io\""};
        c = CommandLine.populateCommand(new MyCommand(), args);
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
    public void testUnmatchedArgumentSuggestsSubcommands() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((IParseResultHandler)null), new PrintStream(baos), new String[]{"chekcout"});
        String expected = String.format("" +
                "Unmatched argument: chekcout%n" +
                "Did you mean: checkout or help or branch?%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    public void testUnmatchedArgumentSuggestsSubcommands2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((IParseResultHandler)null), new PrintStream(baos), new String[]{"me"});
        String expected = String.format("" +
                "Unmatched argument: me%n" +
                "Did you mean: merge?%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    public void testUnmatchedArgumentSuggestsOptions() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        cmd.parseWithHandler(((IParseResultHandler)null), new PrintStream(baos), new String[]{"-fi"});
        String expected = String.format("" +
                "Unknown option: -fi%n" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    public void testUnmatchedArgumentDoesNotSuggestOptionsIfNoMatch() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        cmd.parseWithHandler(((IParseResultHandler)null), new PrintStream(baos), new String[]{"-x"});
        String actual = baos.toString();
        assertTrue(actual, actual.startsWith("Unknown option: -x"));
        assertTrue(actual, actual.contains("Usage:"));
        assertFalse(actual, actual.contains("Possible solutions:"));
    }

    @Command(name="method")
    static class MethodApp {
        static final int[] data = {-1};

        @Command(name="run-0")
        void run0() {}

        @Command(name="run-1")
        void run1(int a) {
            data[0] = a;
        }
        @Command(name="run-2")
        void run2(int a, @Option(names="-b") int b) {
            data[0] = a*b;
        }
    }
    @Test
    public void testAnnotateMethod_noArg() throws Exception {
        setTraceLevel("OFF");
        Method m = MethodApp.class.getDeclaredMethod("run0", new Class<?>[] {});
        CommandLine cmd1 = new CommandLine(m);
        assertEquals("run-0", cmd1.getCommandName());
        assertEquals(Arrays.asList(), cmd1.getCommandSpec().args());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cmd1.parseWithHandler(((IParseResultHandler)null), new PrintStream(baos), new String[]{"--y"});
        assertEquals(Arrays.asList("--y"), cmd1.getUnmatchedArguments());
        setTraceLevel("WARN");
    }
    @Test
    public void testAnnotateMethod_unannotatedPositional() throws Exception {
        Method m = MethodApp.class.getDeclaredMethod("run1", new Class<?>[] {int.class});

        // test required
        try {
            CommandLine.populateCommand(m);
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: <arg0>", ex.getMessage());
        }

        // test execute
        MethodApp.data[0] = -1;
        CommandLine.run(m, new PrintStream(new ByteArrayOutputStream()), "42");
        assertEquals(42, MethodApp.data[0]);
    }

    @Test
    public void testAnnotateMethod_annotated() throws Exception {
        Method m = MethodApp.class.getDeclaredMethod("run2", new Class<?>[] {int.class, int.class});

        // test required
        try {
            CommandLine.populateCommand(m, "0");
            fail("Missing required option should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '-b=<arg1>'", ex.getMessage());
        }

        // test execute
        MethodApp.data[0] = -1;
        CommandLine.run(m, new PrintStream(new ByteArrayOutputStream()), "13", "-b", "-1");
        assertEquals(-13, MethodApp.data[0]);
    }

    @Test
    public void testAnnotateMethod_addMethodSubcommands() throws Exception {

        CommandLine cmd = new CommandLine(MethodApp.class);
        assertEquals("method", cmd.getCommandName());
        assertEquals(0, cmd.getSubcommands().size());

        cmd.addMethodSubcommands();
        assertEquals(3, cmd.getSubcommands().size());
        assertEquals(0, cmd.getSubcommands().get("run-0").getCommandSpec().args().size());
        assertEquals(1, cmd.getSubcommands().get("run-1").getCommandSpec().args().size());
        assertEquals(2, cmd.getSubcommands().get("run-2").getCommandSpec().args().size());
    }

}
