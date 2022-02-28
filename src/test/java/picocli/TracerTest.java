package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.TestUtil.setTraceLevel;
import static picocli.TestUtil.stripAnsiTrace;
import static picocli.TestUtil.stripHashcodes;

public class TracerTest {
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    private static void clearBuiltInTracingCache() throws Exception {
        Field field = Class.forName("picocli.CommandLine$BuiltIn").getDeclaredField("traced");
        field.setAccessible(true);
        Collection<?> collection = (Collection<?>) field.get(null);
        collection.clear();
    }
    @Test
    public void testDebugOutputForDoubleDashSeparatesPositionalParameters() throws Exception {
        clearBuiltInTracingCache();
        TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        CommandLine.populateCommand(new CommandLineTest.CompactFields(), "-oout -- -r -v p1 p2".split(" "));
        String prefix8 = format("" +
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
        String prefix7 = format("" +
                "[picocli DEBUG] Could not register converter for java.nio.file.Path: java.lang.ClassNotFoundException: java.nio.file.Path%n");
        String expected = format("" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.CommandLineTest$CompactFields@20f5239f with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli INFO] Picocli version: %3$s%n" +
                        "[picocli INFO] Parsing 6 command line args [-oout, --, -r, -v, p1, p2]%n" +
                        "[picocli DEBUG] Parser configuration: optionsCaseInsensitive=false, subcommandsCaseInsensitive=false, abbreviatedOptionsAllowed=false, abbreviatedSubcommandsAllowed=false, allowOptionsAsOptionParameters=false, allowSubcommandsAsOptionParameters=false, aritySatisfiedByAttachedOptionParam=false, atFileCommentChar=#, caseInsensitiveEnumValuesAllowed=false, collectErrors=false, endOfOptionsDelimiter=--, expandAtFiles=true, limitSplit=false, overwrittenOptionsAllowed=false, posixClusteredShortOptionsAllowed=true, separator=null, splitQuotedStrings=false, stopAtPositional=false, stopAtUnmatched=false, toggleBooleanFlags=false, trimQuotes=false, unmatchedArgumentsAllowed=false, unmatchedOptionsAllowedAsOptionParameters=true, unmatchedOptionsArePositionalParams=false, useSimplifiedAtFiles=false%n" +
                        "[picocli DEBUG] (ANSI is disabled by default: ...)%n" +
                        "[picocli DEBUG] Initializing command 'null' (user object: picocli.CommandLineTest$CompactFields@20f5239f): 3 options, 1 positional parameters, 0 required, 0 groups, 0 subcommands.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLineTest$CompactFields.verbose of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLineTest$CompactFields.recursive of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File picocli.CommandLineTest$CompactFields.outputFile of type class java.io.File to null.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles of type class [Ljava.io.File; to null.%n" +
                        "[picocli DEBUG] [0] Processing argument '-oout'. Remainder=[--, -r, -v, p1, p2]%n" +
                        "[picocli DEBUG] '-oout' cannot be separated into <option>=<option-parameter>%n" +
                        "[picocli DEBUG] Trying to process '-oout' as clustered short options%n" +
                        "[picocli DEBUG] Found option '-o' in -oout: field java.io.File %1$s$CompactFields.outputFile, arity=1%n" +
                        "[picocli DEBUG] Trying to process 'out' as option parameter%n" +
                        "[picocli DEBUG] 'out' doesn't resemble an option: 0 matching prefix chars out of 3 option names%n" +
                        "[picocli INFO] Setting field java.io.File picocli.CommandLineTest$CompactFields.outputFile to 'out' (was 'null') for option -o%n" +
                        "[picocli DEBUG] [1] Processing argument '--'. Remainder=[-r, -v, p1, p2]%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli DEBUG] [2] Processing next arg as a positional parameter. Command-local position=0. Remainder=[-r, -v, p1, p2]%n" +
                        "[picocli DEBUG] Position 0 (command-local) is in index range 0..*. Trying to assign args to field java.io.File[] %1$s$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli DEBUG] '-r' resembles an option: 4 matching prefix chars out of 3 option names%n" +
                        "[picocli DEBUG] Parser is configured to allow unmatched option '-r' as option or positional parameter.%n" +
                        "[picocli INFO] Adding [-r] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 0%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 1.%n" +
                        "[picocli DEBUG] [3] Processing next arg as a positional parameter. Command-local position=1. Remainder=[-v, p1, p2]%n" +
                        "[picocli DEBUG] Position 1 (command-local) is in index range 0..*. Trying to assign args to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli DEBUG] '-v' resembles an option: 4 matching prefix chars out of 3 option names%n" +
                        "[picocli DEBUG] Parser is configured to allow unmatched option '-v' as option or positional parameter.%n" +
                        "[picocli INFO] Adding [-v] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 1%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 2.%n" +
                        "[picocli DEBUG] [4] Processing next arg as a positional parameter. Command-local position=2. Remainder=[p1, p2]%n" +
                        "[picocli DEBUG] Position 2 (command-local) is in index range 0..*. Trying to assign args to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli DEBUG] 'p1' doesn't resemble an option: 0 matching prefix chars out of 3 option names%n" +
                        "[picocli INFO] Adding [p1] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 2%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 3.%n" +
                        "[picocli DEBUG] [5] Processing next arg as a positional parameter. Command-local position=3. Remainder=[p2]%n" +
                        "[picocli DEBUG] Position 3 (command-local) is in index range 0..*. Trying to assign args to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles, arity=0..1%n" +
                        "[picocli DEBUG] 'p2' doesn't resemble an option: 0 matching prefix chars out of 3 option names%n" +
                        "[picocli INFO] Adding [p2] to field java.io.File[] picocli.CommandLineTest$CompactFields.inputFiles for args[0..*] at position 3%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 4.%n" +
                        "[picocli DEBUG] Applying default values for command '<main class>'%n" +
                        "[picocli DEBUG] defaultValue not defined for field boolean picocli.CommandLineTest$CompactFields.verbose%n" +
                        "[picocli DEBUG] defaultValue not defined for field boolean picocli.CommandLineTest$CompactFields.recursive%n",
                CommandLineTest.class.getName(),
                new File("/home/rpopma/picocli"),
                CommandLine.versionString());
        String actual = systemErrRule.getLog();
        //System.out.println(actual);
        if (System.getProperty("java.version").compareTo("1.7.0") < 0) {
            expected = prefix7 + expected;
        }
        if (System.getProperty("java.version").compareTo("1.8.0") < 0) {
            expected = prefix8 + expected;
        }
        assertEquals(stripAnsiTrace(stripHashcodes(expected)), stripAnsiTrace(stripHashcodes(actual)));
    }

    @Test
    public void testTracerIsWarn() {
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);

        try {
            System.clearProperty(PROPERTY);
            assertTrue("WARN enabled by default", CommandLine.tracer().isWarn());

            System.setProperty(PROPERTY, "OFF");
            assertFalse("WARN can be disabled by setting to OFF", CommandLine.tracer().isWarn());

            System.setProperty(PROPERTY, "WARN");
            assertTrue("WARN can be explicitly enabled", CommandLine.tracer().isWarn());

        } finally {
            if (old == null) {
                System.clearProperty(PROPERTY);
            } else {
                System.setProperty(PROPERTY, old);
            }
        }
    }

    @Test
    public void testTracerLevelsCaseInsensitive() {
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);

        try {
            System.setProperty(PROPERTY, "off");
            assertEquals("OFF", String.valueOf(CommandLine.tracer().getLevel()));
            assertFalse("!debug", CommandLine.tracer().isDebug());
            assertFalse("!info", CommandLine.tracer().isInfo());
            assertFalse("!warn", CommandLine.tracer().isWarn());

            System.setProperty(PROPERTY, "debug");
            assertEquals("DEBUG", String.valueOf(CommandLine.tracer().getLevel()));
            assertTrue("debug", CommandLine.tracer().isDebug());
            assertTrue("info", CommandLine.tracer().isInfo());
            assertTrue("warn", CommandLine.tracer().isWarn());

            System.setProperty(PROPERTY, "info");
            assertEquals("INFO", String.valueOf(CommandLine.tracer().getLevel()));
            assertFalse("!debug", CommandLine.tracer().isDebug());
            assertTrue("info", CommandLine.tracer().isInfo());
            assertTrue("warn", CommandLine.tracer().isWarn());

            System.setProperty(PROPERTY, "warn");
            assertEquals("WARN", String.valueOf(CommandLine.tracer().getLevel()));
            assertFalse("!debug", CommandLine.tracer().isDebug());
            assertFalse("!info", CommandLine.tracer().isInfo());
            assertTrue("warn", CommandLine.tracer().isWarn());

        } finally {
            if (old == null) {
                System.clearProperty(PROPERTY);
            } else {
                System.setProperty(PROPERTY, old);
            }
        }
    }

    @Test
    public void testTraceLevelToInfoViaSetterWithSubCommands() throws Exception {
        CommandLine.TraceLevel old = CommandLine.tracer().getLevel();
        CommandLine.tracer().setLevel(CommandLine.TraceLevel.INFO);
        CommandLine commandLine = Demo.mainCommand();
        commandLine.setEndOfOptionsDelimiter("$$$");
        commandLine.parseArgs("--git-dir=/home/rpopma/picocli", "commit", "-m", "\"Fixed typos\"", "$$$", "src1.java", "src2.java", "src3.java");
        CommandLine.tracer().setLevel(old);
        CommandLine.tracer().modified = false;
        String expected = format("" +
                "[picocli INFO] Picocli version: %s%n" +
                "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", $$$, src1.java, src2.java, src3.java]%n" +
                "[picocli INFO] Setting field java.io.File picocli.Demo$Git.gitDir to '%s' (was 'null') for option --git-dir%n" +
                "[picocli INFO] Adding [\"Fixed typos\"] to field java.util.List<String> picocli.Demo$GitCommit.message for option -m%n" +
                "[picocli INFO] Found end-of-options delimiter '$$$'. Treating remainder as positional parameters.%n" +
                "[picocli INFO] Adding [src1.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 0%n" +
                "[picocli INFO] Adding [src2.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 1%n" +
                "[picocli INFO] Adding [src3.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 2%n",
            CommandLine.versionString(),
            new File("/home/rpopma/picocli"));
        String actual = systemErrRule.getLog();
        //System.out.println(actual);
        assertEquals(stripAnsiTrace(expected), stripAnsiTrace(actual));
    }

    @Test
    public void testTracingInfoWithSubCommands() throws Exception {
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "");
        CommandLine commandLine = Demo.mainCommand();
        commandLine.setEndOfOptionsDelimiter("$$$");
        commandLine.parseArgs("--git-dir=/home/rpopma/picocli", "commit", "-m", "\"Fixed typos\"", "$$$", "src1.java", "src2.java", "src3.java");
        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
        String expected = format("" +
                        "[picocli INFO] Picocli version: %s%n" +
                        "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", $$$, src1.java, src2.java, src3.java]%n" +
                        "[picocli INFO] Setting field java.io.File picocli.Demo$Git.gitDir to '%s' (was 'null') for option --git-dir%n" +
                        "[picocli INFO] Adding [\"Fixed typos\"] to field java.util.List<String> picocli.Demo$GitCommit.message for option -m%n" +
                        "[picocli INFO] Found end-of-options delimiter '$$$'. Treating remainder as positional parameters.%n" +
                        "[picocli INFO] Adding [src1.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 0%n" +
                        "[picocli INFO] Adding [src2.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 1%n" +
                        "[picocli INFO] Adding [src3.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 2%n",
                CommandLine.versionString(),
                new File("/home/rpopma/picocli"));
        String actual = systemErrRule.getLog();
        //System.out.println(actual);
        assertEquals(stripAnsiTrace(expected), stripAnsiTrace(actual));
    }
    @Test
    public void testTracingDebugWithSubCommands() throws Exception {
        clearBuiltInTracingCache();
        final String PROPERTY = "picocli.trace";
        String old = System.getProperty(PROPERTY);
        System.setProperty(PROPERTY, "DEBUG");
        CommandLine commandLine = Demo.mainCommand();
        commandLine.execute("--git-dir=/home/rpopma/picocli", "commit", "-m", "\"Fixed typos\"", "--", "src1.java", "src2.java", "src3.java");
        if (old == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, old);
        }
        String prefix8 = format("" +
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
        String prefix7 = format("" +
                "[picocli DEBUG] Could not register converter for java.nio.file.Path: java.lang.ClassNotFoundException: java.nio.file.Path%n");
        String expected = format("" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$Git@150ede8b with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.CommandLine$AutoHelpMixin@69228e85 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.CommandLine$HelpCommand@332820f4 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'help' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitStatus@4d192aef with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'status' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitCommit@2dfe5525 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'commit' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitAdd@43d38654 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'add' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitBranch@710d89e2 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'branch' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitCheckout@1b9776f5 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'checkout' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitClone@67a3bd51 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'clone' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitDiff@5c534b5b with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'diff' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitMerge@14229fa7 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'merge' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitPush@3936df72 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'push' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitRebase@42714a7 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'rebase' to 'git'%n" +
                        "[picocli DEBUG] Creating CommandSpec for picocli.Demo$GitTag@f679798 with factory picocli.CommandLine$DefaultFactory%n" +
                        "[picocli DEBUG] Adding subcommand 'tag' to 'git'%n" +
                        "[picocli INFO] Picocli version: %3$s%n" +
                        "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Parser configuration: optionsCaseInsensitive=false, subcommandsCaseInsensitive=false, abbreviatedOptionsAllowed=false, abbreviatedSubcommandsAllowed=false, allowOptionsAsOptionParameters=false, allowSubcommandsAsOptionParameters=false, aritySatisfiedByAttachedOptionParam=false, atFileCommentChar=#, caseInsensitiveEnumValuesAllowed=false, collectErrors=false, endOfOptionsDelimiter=--, expandAtFiles=true, limitSplit=false, overwrittenOptionsAllowed=false, posixClusteredShortOptionsAllowed=true, separator=null, splitQuotedStrings=false, stopAtPositional=false, stopAtUnmatched=false, toggleBooleanFlags=false, trimQuotes=false, unmatchedArgumentsAllowed=false, unmatchedOptionsAllowedAsOptionParameters=true, unmatchedOptionsArePositionalParams=false, useSimplifiedAtFiles=false%n" +
                        "[picocli DEBUG] (ANSI is disabled by default: ...)%n" +
                        "[picocli DEBUG] Initializing command 'git' (user object: picocli.Demo$Git@75d4a5c2): 3 options, 0 positional parameters, 0 required, 0 groups, 12 subcommands.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$Git.gitDir of type class java.io.File to null.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLine$AutoHelpMixin.helpRequested of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.CommandLine$AutoHelpMixin.versionRequested of type boolean to false.%n" +
                        "[picocli DEBUG] [0] Processing argument '--git-dir=/home/rpopma/picocli'. Remainder=[commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Separated '--git-dir' option from '/home/rpopma/picocli' option parameter%n" +
                        "[picocli DEBUG] Found option named '--git-dir': field java.io.File %1$s$Git.gitDir, arity=1%n" +
                        "[picocli DEBUG] '/home/rpopma/picocli' doesn't resemble an option: 0 matching prefix chars out of 5 option names%n" +
                        "[picocli INFO] Setting field java.io.File picocli.Demo$Git.gitDir to '%2$s' (was 'null') for option --git-dir%n" +
                        "[picocli DEBUG] [1] Processing argument 'commit'. Remainder=[-m, \"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Found subcommand 'commit' (command 'git-commit' (user object: picocli.Demo$GitCommit@22ff4249))%n" +
                        "[picocli DEBUG] Checking required args for parent command 'git' (user object: picocli.Demo$Git@00000000)...%n" +
                        "[picocli DEBUG] Initializing command 'git-commit' (user object: picocli.Demo$GitCommit@22ff4249): 8 options, 1 positional parameters, 0 required, 0 groups, 0 subcommands.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.Demo$GitCommit.all of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field boolean picocli.Demo$GitCommit.patch of type boolean to false.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.reuseMessageCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.reEditMessageCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.fixupCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field String picocli.Demo$GitCommit.squashCommit of type class java.lang.String to null.%n" +
                        "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$GitCommit.file of type class java.io.File to null.%n" +
                        "[picocli DEBUG] Set initial value for field java.util.List<String> picocli.Demo$GitCommit.message of type interface java.util.List to [].%n" +
                        "[picocli DEBUG] Set initial value for field java.util.List<java.io.File> picocli.Demo$GitCommit.files of type interface java.util.List to [].%n" +
                        "[picocli DEBUG] [2] Processing argument '-m'. Remainder=[\"Fixed typos\", --, src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] '-m' cannot be separated into <option>=<option-parameter>%n" +
                        "[picocli DEBUG] Found option named '-m': field java.util.List<String> picocli.Demo$GitCommit.message, arity=1%n" +
                        "[picocli DEBUG] '\"Fixed typos\"' doesn't resemble an option: 0 matching prefix chars out of 14 option names%n" +
                        "[picocli INFO] Adding [\"Fixed typos\"] to field java.util.List<String> picocli.Demo$GitCommit.message for option -m%n" +
                        "[picocli DEBUG] Initializing binding for option '--message' (<msg>)%n" +
                        "[picocli DEBUG] [4] Processing argument '--'. Remainder=[src1.java, src2.java, src3.java]%n" +
                        "[picocli INFO] Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n" +
                        "[picocli DEBUG] [5] Processing next arg as a positional parameter. Command-local position=0. Remainder=[src1.java, src2.java, src3.java]%n" +
                        "[picocli DEBUG] Position 0 (command-local) is in index range 0..*. Trying to assign args to field java.util.List<java.io.File> picocli.Demo$GitCommit.files, arity=0..1%n" +
                        "[picocli DEBUG] 'src1.java' doesn't resemble an option: 0 matching prefix chars out of 14 option names%n" +
                        "[picocli INFO] Adding [src1.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 0%n" +
                        "[picocli DEBUG] Initializing binding for positional parameter at index 0..* (<files>)%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 1.%n" +
                        "[picocli DEBUG] [6] Processing next arg as a positional parameter. Command-local position=1. Remainder=[src2.java, src3.java]%n" +
                        "[picocli DEBUG] Position 1 (command-local) is in index range 0..*. Trying to assign args to field java.util.List<java.io.File> picocli.Demo$GitCommit.files, arity=0..1%n" +
                        "[picocli DEBUG] 'src2.java' doesn't resemble an option: 0 matching prefix chars out of 14 option names%n" +
                        "[picocli INFO] Adding [src2.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 1%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 2.%n" +
                        "[picocli DEBUG] [7] Processing next arg as a positional parameter. Command-local position=2. Remainder=[src3.java]%n" +
                        "[picocli DEBUG] Position 2 (command-local) is in index range 0..*. Trying to assign args to field java.util.List<java.io.File> picocli.Demo$GitCommit.files, arity=0..1%n" +
                        "[picocli DEBUG] 'src3.java' doesn't resemble an option: 0 matching prefix chars out of 14 option names%n" +
                        "[picocli INFO] Adding [src3.java] to field java.util.List<java.io.File> picocli.Demo$GitCommit.files for args[0..*] at position 2%n" +
                        "[picocli DEBUG] Consumed 1 arguments and 0 interactive values, moving command-local position to index 3.%n" +
                        "[picocli DEBUG] Applying default values for command 'git git-commit'%n" +
                        "[picocli DEBUG] defaultValue not defined for field boolean picocli.Demo$GitCommit.all%n" +
                        "[picocli DEBUG] defaultValue not defined for field boolean picocli.Demo$GitCommit.patch%n" +
                        "[picocli DEBUG] defaultValue not defined for field String picocli.Demo$GitCommit.reuseMessageCommit%n" +
                        "[picocli DEBUG] defaultValue not defined for field String picocli.Demo$GitCommit.reEditMessageCommit%n" +
                        "[picocli DEBUG] defaultValue not defined for field String picocli.Demo$GitCommit.fixupCommit%n" +
                        "[picocli DEBUG] defaultValue not defined for field String picocli.Demo$GitCommit.squashCommit%n" +
                        "[picocli DEBUG] defaultValue not defined for field java.io.File picocli.Demo$GitCommit.file%n" +
                        "[picocli DEBUG] Applying default values for command 'git'%n" +
                        "[picocli DEBUG] defaultValue not defined for field boolean picocli.CommandLine$AutoHelpMixin.helpRequested%n" +
                        "[picocli DEBUG] defaultValue not defined for field boolean picocli.CommandLine$AutoHelpMixin.versionRequested%n" +
                "[picocli DEBUG] Help was not requested. Continuing to process ParseResult...%n" +
                "[picocli DEBUG] RunLast: handling ParseResult...%n" +
                "[picocli DEBUG] RunLast: executing user object for 'git git-commit'...%n" +
                "[picocli DEBUG] Invoking Runnable::run%n" +
                "[picocli DEBUG] RunLast: ParseResult has 0 exit code generators%n" +
                "[picocli DEBUG] resolveExitCode: exit code generators resulted in exit code=0%n" +
                "[picocli DEBUG] resolveExitCode: execution results resulted in exit code=0%n" +
                "[picocli DEBUG] resolveExitCode: returning exit code=0%n",
                Demo.class.getName(),
                new File("/home/rpopma/picocli"),
                CommandLine.versionString());
        String actual = systemErrRule.getLog();
        String actualSafe = stripHashcodes(actual);
        //System.out.println(actual);
        if (System.getProperty("java.version").compareTo("1.7.0") < 0) {
            expected = prefix7 + expected;
        }
        if (System.getProperty("java.version").compareTo("1.8.0") < 0) {
            expected = prefix8 + expected;
        }
        assertEquals(stripAnsiTrace(stripHashcodes(expected)), stripAnsiTrace(stripHashcodes(actual)));
    }

    @Test
    public void testStripAnsiTrace() {
        String original = "[picocli INFO] Picocli version: 4.4.1-SNAPSHOT, JVM: 1.5.0_22 (Sun Microsystems Inc. Java HotSpot(TM) Client VM 1.5.0_22-b03), OS: Windows NT (unknown) 6.2 x86\n" +
                "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]\n" +
                "[picocli DEBUG] Parser configuration: optionsCaseInsensitive=false, subcommandsCaseInsensitive=false, abbreviatedOptionsAllowed=false, abbreviatedSubcommandsAllowed=false, aritySatisfiedByAttachedOptionParam=false, atFileCommentChar=#, caseInsensitiveEnumValuesAllowed=false, collectErrors=false, endOfOptionsDelimiter=--, expandAtFiles=true, limitSplit=false, overwrittenOptionsAllowed=false, posixClusteredShortOptionsAllowed=true, separator=null, splitQuotedStrings=false, stopAtPositional=false, stopAtUnmatched=false, toggleBooleanFlags=false, trimQuotes=false, unmatchedArgumentsAllowed=false, unmatchedOptionsArePositionalParams=false, useSimplifiedAtFiles=false\n" +
                "[picocli DEBUG] (ANSI is disabled by default: systemproperty[picocli.ansi]=false, isatty=true, TERM=null, OSTYPE=null, isWindows=true, JansiConsoleInstalled=false, ANSICON=null, ConEmuANSI=null, NO_COLOR=null, CLICOLOR=null, CLICOLOR_FORCE=null)\n" +
                "[picocli DEBUG] Initializing command 'git' (user object: picocli.Demo$Git@10f8ee4): 3 options, 0 positional parameters, 0 required, 0 groups, 12 subcommands.\n" +
                "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$Git.gitDir of type class java.io.File to null.\n";

        String expected = "[picocli INFO] Picocli version: 4.4.1-SNAPSHOT, JVM: 1.5.0_22 (Sun Microsystems Inc. Java HotSpot(TM) Client VM 1.5.0_22-b03), OS: Windows NT (unknown) 6.2 x86\n" +
                "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]\n" +
                "[picocli DEBUG] Parser configuration: optionsCaseInsensitive=false, subcommandsCaseInsensitive=false, abbreviatedOptionsAllowed=false, abbreviatedSubcommandsAllowed=false, aritySatisfiedByAttachedOptionParam=false, atFileCommentChar=#, caseInsensitiveEnumValuesAllowed=false, collectErrors=false, endOfOptionsDelimiter=--, expandAtFiles=true, limitSplit=false, overwrittenOptionsAllowed=false, posixClusteredShortOptionsAllowed=true, separator=null, splitQuotedStrings=false, stopAtPositional=false, stopAtUnmatched=false, toggleBooleanFlags=false, trimQuotes=false, unmatchedArgumentsAllowed=false, unmatchedOptionsArePositionalParams=false, useSimplifiedAtFiles=false\n" +
                "[picocli DEBUG] (ANSI is disabled ...)\n" +
                "[picocli DEBUG] Initializing command 'git' (user object: picocli.Demo$Git@10f8ee4): 3 options, 0 positional parameters, 0 required, 0 groups, 12 subcommands.\n" +
                "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$Git.gitDir of type class java.io.File to null.\n";

        assertEquals(expected, stripAnsiTrace(original));

        // Now use environment value with closing brace: ANSICON=80x1000 (80x25)
        // https://github.com/remkop/picocli/issues/1103
        String original2 = "[picocli INFO] Picocli version: 4.4.1-SNAPSHOT, JVM: 1.5.0_22 (Sun Microsystems Inc. Java HotSpot(TM) Client VM 1.5.0_22-b03), OS: Windows NT (unknown) 6.2 x86\n" +
                "[picocli INFO] Parsing 8 command line args [--git-dir=/home/rpopma/picocli, commit, -m, \"Fixed typos\", --, src1.java, src2.java, src3.java]\n" +
                "[picocli DEBUG] Parser configuration: optionsCaseInsensitive=false, subcommandsCaseInsensitive=false, abbreviatedOptionsAllowed=false, abbreviatedSubcommandsAllowed=false, aritySatisfiedByAttachedOptionParam=false, atFileCommentChar=#, caseInsensitiveEnumValuesAllowed=false, collectErrors=false, endOfOptionsDelimiter=--, expandAtFiles=true, limitSplit=false, overwrittenOptionsAllowed=false, posixClusteredShortOptionsAllowed=true, separator=null, splitQuotedStrings=false, stopAtPositional=false, stopAtUnmatched=false, toggleBooleanFlags=false, trimQuotes=false, unmatchedArgumentsAllowed=false, unmatchedOptionsArePositionalParams=false, useSimplifiedAtFiles=false\n" +
                "[picocli DEBUG] (ANSI is disabled by default: systemproperty[picocli.ansi]=false, isatty=true, TERM=null, OSTYPE=null, isWindows=true, JansiConsoleInstalled=false, ANSICON=80x1000 (80x25), ConEmuANSI=null, NO_COLOR=null, CLICOLOR=null, CLICOLOR_FORCE=null)\n" +
                "[picocli DEBUG] Initializing command 'git' (user object: picocli.Demo$Git@10f8ee4): 3 options, 0 positional parameters, 0 required, 0 groups, 12 subcommands.\n" +
                "[picocli DEBUG] Set initial value for field java.io.File picocli.Demo$Git.gitDir of type class java.io.File to null.\n";

        assertEquals(expected, stripAnsiTrace(original2));
    }

    @Test
    public void testTraceWarningIfOptionOverwrittenWhenOverwrittenOptionsAllowed() throws Exception {

        setTraceLevel(CommandLine.TraceLevel.INFO);
        class App {
            @CommandLine.Option(names = "-f") String field = null;
            @CommandLine.Option(names = "-p") int primitive = 43;
        }
        CommandLine cmd = new CommandLine(new App()).setOverwrittenOptionsAllowed(true);
        cmd.parseArgs("-f", "111", "-f", "222", "-f", "333");
        App ff = cmd.getCommand();
        assertEquals("333", ff.field);

        String expected = format("" +
                        "[picocli INFO] Picocli version: %s%n" +
                        "[picocli INFO] Parsing 6 command line args [-f, 111, -f, 222, -f, 333]%n" +
                        "[picocli INFO] Setting field String %2$s.field to '111' (was 'null') for option -f%n" +
                        "[picocli INFO] Overwriting field String %2$s.field value '111' with '222' for option -f%n" +
                        "[picocli INFO] Overwriting field String %2$s.field value '222' with '333' for option -f%n",
                CommandLine.versionString(),
                App.class.getName());
        String actual = systemErrRule.getLog();
        assertEquals(stripAnsiTrace(expected), stripAnsiTrace(actual));
        setTraceLevel(CommandLine.TraceLevel.WARN);
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testTraceWarningIfUnmatchedArgsWhenUnmatchedArgumentsAllowed() throws Exception {
        setTraceLevel(CommandLine.TraceLevel.INFO);
        class App {
            @CommandLine.Parameters(index = "0", arity = "2", split = "\\|", type = {Integer.class, String.class})
            Map<Integer,String> message;
        }
        CommandLine cmd = new CommandLine(new App()).setUnmatchedArgumentsAllowed(true).parse("1=a", "2=b", "3=c", "4=d").get(0);
        assertEquals(Arrays.asList("3=c", "4=d"), cmd.getUnmatchedArguments());

        String expected = format("" +
                        "[picocli INFO] Picocli version: %s%n" +
                        "[picocli INFO] Parsing 4 command line args [1=a, 2=b, 3=c, 4=d]%n" +
                        "[picocli INFO] Putting [1 : a] in LinkedHashMap<Integer, String> field java.util.Map<Integer, String> %s.message for args[0] at position 0%n" +
                        "[picocli INFO] Putting [2 : b] in LinkedHashMap<Integer, String> field java.util.Map<Integer, String> %s.message for args[0] at position 0%n" +
                        "[picocli INFO] Unmatched arguments: [3=c, 4=d]%n",
                CommandLine.versionString(),
                App.class.getName(),
                App.class.getName());
        String actual = systemErrRule.getLog();
        //System.out.println(actual);
        assertEquals(stripAnsiTrace(expected), stripAnsiTrace(actual));
        setTraceLevel(CommandLine.TraceLevel.WARN);
    }
}
