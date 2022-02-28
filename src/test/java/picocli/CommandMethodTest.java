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
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IScope;
import picocli.CommandLine.Model.MethodParam;
import picocli.CommandLine.Model.ObjectScope;
import picocli.CommandLine.Model.TypedMember;
import picocli.CommandLineTest.CompactFields;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.IParseResultHandler;
import static picocli.CommandLine.MissingParameterException;
import static picocli.CommandLine.Model;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.UnmatchedArgumentException;
import static picocli.CommandLineTest.verifyCompact;
import static picocli.TestUtil.setTraceLevel;

/**
 * Tests for {@code @Command} methods.
 */
@SuppressWarnings("deprecation")
public class CommandMethodTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Before public void setUp() { System.clearProperty("picocli.trace"); }
    @After public void tearDown() { System.clearProperty("picocli.trace"); }

    static class MethodAppBase {
        @Command(name="run-0")
        public void run0() {}
    }

    @Command(name="method")
    static class MethodApp extends MethodAppBase {

        @Command(name="run-1")
        int run1(int a) {
            return a;
        }

        @Command(name="run-2")
        int run2(int a, @Option(names="-b", required=true) int b) {
            return a*b;
        }
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testAnnotateMethod_noArg() throws Exception {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        Method m = CommandLine.getCommandMethods(MethodApp.class, "run0").get(0);
        CommandLine cmd1 = new CommandLine(m);
        assertEquals("run-0", cmd1.getCommandName());
        assertEquals(Collections.emptyList(), cmd1.getCommandSpec().args());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cmd1.parseWithHandler(((IParseResultHandler) null), new PrintStream(baos), new String[]{"--y"});
        assertEquals(Collections.singletonList("--y"), cmd1.getUnmatchedArguments());

        // test execute
        Object ret = CommandLine.invoke(m.getName(), MethodApp.class, new PrintStream(new ByteArrayOutputStream()));
        assertNull("return value", ret);

        setTraceLevel(CommandLine.TraceLevel.WARN);
    }
    @Test
    public void testAnnotateMethod_unannotatedPositional() throws Exception {
        Method m = CommandLine.getCommandMethods(MethodApp.class, "run1").get(0);

        // test required
        try {
            CommandLine.populateCommand(m);
            fail("Missing required field should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required parameter: '<arg0>'", ex.getMessage());
        }

        // test execute
        Object ret = CommandLine.invoke(m.getName(), MethodApp.class, new PrintStream(new ByteArrayOutputStream()), "42");
        assertEquals("return value", 42, ((Number)ret).intValue());
    }

    @Command
    static class UnannotatedPositional {
        @Command
        public void x(int a, int b, int c, int[] x, String[] y) {}
    }

    @Test
    public void testAnnotateMethod_unannotatedPositional_indexByParameterOrder() throws Exception {
        Method m = CommandLine.getCommandMethods(UnannotatedPositional.class, "x").get(0);
        CommandLine cmd = new CommandLine(m);
        CommandSpec spec = cmd.getCommandSpec();
        List<Model.PositionalParamSpec> positionals = spec.positionalParameters();
        String[] labels = { "<arg0>", "<arg1>", "<arg2>", "<arg3>", "<arg4>"};
        assertEquals(positionals.size(), labels.length);

        String[] ranges = { "0", "1", "2", "3..*", "4..*" };

        for (int i = 0; i < positionals.size(); i++) {
            Model.PositionalParamSpec positional = positionals.get(i);
            assertEquals(positional.paramLabel() + " at index " + i, CommandLine.Range.valueOf(ranges[i]), positional.index());
            assertEquals(labels[i], positional.paramLabel());
        }
    }

    @Command
    static class PositionalsMixedWithOptions {
        @Command
        public void mixed(int a, @Option(names = "-b") int b, @Option(names = "-c") String c, int[] x, String[] y) {}
    }

    @Test
    public void testAnnotateMethod_unannotatedPositionalMixedWithOptions_indexByParameterOrder() throws Exception {
        Method m = CommandLine.getCommandMethods(PositionalsMixedWithOptions.class, "mixed").get(0);
        CommandLine cmd = new CommandLine(m);
        CommandSpec spec = cmd.getCommandSpec();
        List<Model.PositionalParamSpec> positionals = spec.positionalParameters();
        String[] labels = { "<arg0>", "<arg3>", "<arg4>"};
        assertEquals(positionals.size(), labels.length);

        String[] ranges = { "0", "1..*", "2..*" };

        for (int i = 0; i < positionals.size(); i++) {
            Model.PositionalParamSpec positional = positionals.get(i);
            assertEquals(positional.paramLabel() + " at index " + i, CommandLine.Range.valueOf(ranges[i]), positional.index());
            assertEquals(labels[i], positional.paramLabel());
        }

        assertEquals(2, spec.options().size());
        assertEquals(int.class, spec.findOption("-b").type());
        assertEquals(String.class, spec.findOption("-c").type());
    }

    @Command static class SomeMixin {
        @Option(names = "-a") int a;
        @Option(names = "-b") long b;
    }

    static class UnannotatedClassWithMixinParameters {
        @Command
        void withMixin(@Mixin SomeMixin mixin) {
        }

        @Command
        void posAndMixin(int[] x, @Mixin SomeMixin mixin) {
        }

        @Command
        void posAndOptAndMixin(int[] x, @Option(names = "-y") String[] y, @Mixin SomeMixin mixin) {
        }

        @Command
        void mixinFirst(@Mixin SomeMixin mixin, int[] x, @Option(names = "-y") String[] y) {
        }
    }

    @Test
    public void testAnnotateMethod_mixinParameter() {
        Method m = CommandLine.getCommandMethods(UnannotatedClassWithMixinParameters.class, "withMixin").get(0);
        CommandLine cmd = new CommandLine(m);
        CommandSpec spec = cmd.getCommandSpec();
        assertEquals(1, spec.mixins().size());
        spec = spec.mixins().get("arg0");
        assertEquals(SomeMixin.class, spec.userObject().getClass());
    }

    @Test
    public void testAnnotateMethod_positionalAndMixinParameter() {
        Method m = CommandLine.getCommandMethods(UnannotatedClassWithMixinParameters.class, "posAndMixin").get(0);
        CommandLine cmd = new CommandLine(m);
        CommandSpec spec = cmd.getCommandSpec();
        assertEquals(1, spec.mixins().size());
        assertEquals(1, spec.positionalParameters().size());
        spec = spec.mixins().get("arg1");
        assertEquals(SomeMixin.class, spec.userObject().getClass());
    }

    @Test
    public void testAnnotateMethod_positionalAndOptionsAndMixinParameter() {
        Method m = CommandLine.getCommandMethods(UnannotatedClassWithMixinParameters.class, "posAndOptAndMixin").get(0);
        CommandLine cmd = new CommandLine(m);
        CommandSpec spec = cmd.getCommandSpec();
        assertEquals(1, spec.mixins().size());
        assertEquals(1, spec.positionalParameters().size());
        assertEquals(3, spec.options().size());
        spec = spec.mixins().get("arg2");
        assertEquals(SomeMixin.class, spec.userObject().getClass());
    }

    @Test
    public void testAnnotateMethod_mixinParameterFirst() {
        Method m = CommandLine.getCommandMethods(UnannotatedClassWithMixinParameters.class, "mixinFirst").get(0);
        CommandLine cmd = new CommandLine(m);
        CommandSpec spec = cmd.getCommandSpec();
        assertEquals(1, spec.mixins().size());
        assertEquals(1, spec.positionalParameters().size());
        assertEquals(3, spec.options().size());
        spec = spec.mixins().get("arg0");
        assertEquals(SomeMixin.class, spec.userObject().getClass());
    }

    static class UnannotatedClassWithMixinAndOptionsAndPositionals {
        @Command(name="sum")
        long sum(@Option(names = "-y") String[] y, @Mixin SomeMixin subMixin, int[] x) {
            return y.length + subMixin.a + subMixin.b + x.length;
        }
    }

    @Test
    public void testUnannotatedCommandWithMixin() throws Exception {
        Method m = CommandLine.getCommandMethods(UnannotatedClassWithMixinAndOptionsAndPositionals.class, "sum").get(0);
        CommandLine commandLine = new CommandLine(m);
        List<CommandLine> parsed = commandLine.parse("-y foo -y bar -a 7 -b 11 13 42".split(" "));
        assertEquals(1, parsed.size());

        // get method args
        Object[] methodArgValues = parsed.get(0).getCommandSpec().commandMethodParamValues();
        assertNotNull(methodArgValues);

        // verify args
        String[] arg0 = (String[]) methodArgValues[0];
        assertArrayEquals(new String[] {"foo", "bar"}, arg0);
        SomeMixin arg1 = (SomeMixin) methodArgValues[1];
        assertEquals(7, arg1.a);
        assertEquals(11, arg1.b);
        int[] arg2 = (int[]) methodArgValues[2];
        assertArrayEquals(new int[] {13, 42}, arg2);

        // verify method is callable with args
        long result = (Long) m.invoke(new UnannotatedClassWithMixinAndOptionsAndPositionals(), methodArgValues);
        assertEquals(22, result);

        // verify same result with result handler
        List<Object> results = new RunLast().handleParseResult(parsed, System.out, CommandLine.Help.Ansi.OFF);
        assertEquals(1, results.size());
        assertEquals(22L, results.get(0));
    }

    @Command
    static class AnnotatedClassWithMixinParameters {
        @Mixin SomeMixin mixin;

        @Command(name="sum")
        long sum(@Option(names = "-y") String[] y, @Mixin SomeMixin subMixin, int[] x) {
            return mixin.a + mixin.b + y.length + subMixin.a + subMixin.b + x.length;
        }
    }

    @Test
    public void testAnnotatedSubcommandWithDoubleMixin() throws Exception {
        AnnotatedClassWithMixinParameters command = new AnnotatedClassWithMixinParameters();
        CommandLine commandLine = new CommandLine(command);
        List<CommandLine> parsed = commandLine.parse("-a 3 -b 5 sum -y foo -y bar -a 7 -b 11 13 42".split(" "));
        assertEquals(2, parsed.size());

        // get method args
        Object[] methodArgValues = parsed.get(1).getCommandSpec().commandMethodParamValues();
        assertNotNull(methodArgValues);

        // verify args
        String[] arg0 = (String[]) methodArgValues[0];
        assertArrayEquals(new String[] {"foo", "bar"}, arg0);
        SomeMixin arg1 = (SomeMixin) methodArgValues[1];
        assertEquals(7, arg1.a);
        assertEquals(11, arg1.b);
        int[] arg2 = (int[]) methodArgValues[2];
        assertArrayEquals(new int[] {13, 42}, arg2);

        // verify method is callable with args
        Method m = AnnotatedClassWithMixinParameters.class.getDeclaredMethod("sum", String[].class, SomeMixin.class, int[].class);
        long result = (Long) m.invoke(command, methodArgValues);
        assertEquals(30, result);

        // verify same result with result handler
        List<Object> results = new RunLast().handleParseResult(parsed, System.out, CommandLine.Help.Ansi.OFF);
        assertEquals(1, results.size());
        assertEquals(30L, results.get(0));
    }

    @Command static class OtherMixin {
        @Option(names = "-c") int c;
    }

    static class AnnotatedClassWithMultipleMixinParameters {
        @Command(name="sum")
        long sum(@Mixin SomeMixin mixin1, @Mixin OtherMixin mixin2) {
            return mixin1.a + mixin1.b + mixin2.c;
        }
    }

    @Test
    public void testAnnotatedMethodMultipleMixinsSubcommandWithDoubleMixin() throws Exception {
        Method m = CommandLine.getCommandMethods(AnnotatedClassWithMultipleMixinParameters.class, "sum").get(0);
        CommandLine commandLine = new CommandLine(m);
        List<CommandLine> parsed = commandLine.parse("-a 3 -b 5 -c 7".split(" "));
        assertEquals(1, parsed.size());

        // get method args
        Object[] methodArgValues = parsed.get(0).getCommandSpec().commandMethodParamValues();
        assertNotNull(methodArgValues);

        // verify args
        SomeMixin arg0 = (SomeMixin) methodArgValues[0];
        assertEquals(3, arg0.a);
        assertEquals(5, arg0.b);
        OtherMixin arg1 = (OtherMixin) methodArgValues[1];
        assertEquals(7, arg1.c);

        // verify method is callable with args
        long result = (Long) m.invoke(new AnnotatedClassWithMultipleMixinParameters(), methodArgValues);
        assertEquals(15, result);

        // verify same result with result handler
        List<Object> results = new RunLast().handleParseResult(parsed, System.out, CommandLine.Help.Ansi.OFF);
        assertEquals(1, results.size());
        assertEquals(15L, results.get(0));
    }

    @Command static class EmptyMixin {}

    static class AnnotatedClassWithMultipleEmptyParameters {
        @Command(name="sum")
        long sum(@Option(names = "-a") int a, @Mixin EmptyMixin mixin) {
            return a;
        }
    }

    @Test
    public void testAnnotatedMethodMultipleMixinsSubcommandWithEmptyMixin() throws Exception {
        Method m = CommandLine.getCommandMethods(AnnotatedClassWithMultipleEmptyParameters.class, "sum").get(0);
        CommandLine commandLine = new CommandLine(m);
        List<CommandLine> parsed = commandLine.parse("-a 3".split(" "));
        assertEquals(1, parsed.size());

        // get method args
        Object[] methodArgValues = parsed.get(0).getCommandSpec().commandMethodParamValues();
        assertNotNull(methodArgValues);

        // verify args
        int arg0 = (Integer) methodArgValues[0];
        assertEquals(3, arg0);
        EmptyMixin arg1 = (EmptyMixin) methodArgValues[1];

        // verify method is callable with args
        long result = (Long) m.invoke(new AnnotatedClassWithMultipleEmptyParameters(), methodArgValues);
        assertEquals(3, result);

        // verify same result with result handler
        List<Object> results = new RunLast().handleParseResult(parsed, System.out, CommandLine.Help.Ansi.OFF);
        assertEquals(1, results.size());
        assertEquals(3L, results.get(0));
    }

    @Test
    public void testAnnotateMethod_annotated() throws Exception {
        Method m = CommandLine.getCommandMethods(MethodApp.class, "run2").get(0);

        // test required
        try {
            CommandLine.populateCommand(m, "0");
            fail("Missing required option should have thrown exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '-b=<arg1>'", ex.getMessage());
        }

        // test execute
        Object ret = CommandLine.invoke(m.getName(), MethodApp.class, new PrintStream(new ByteArrayOutputStream()), "13", "-b", "-1");
        assertEquals("return value", -13, ((Number)ret).intValue());
    }

    @Test
    public void testCommandMethodsFromSuperclassAddedToSubcommands() throws Exception {

        CommandLine cmd = new CommandLine(MethodApp.class);
        assertEquals("method", cmd.getCommandName());
        assertEquals(3, cmd.getSubcommands().size());
        assertEquals(0, cmd.getSubcommands().get("run-0").getCommandSpec().args().size());
        assertEquals(1, cmd.getSubcommands().get("run-1").getCommandSpec().args().size());
        assertEquals(2, cmd.getSubcommands().get("run-2").getCommandSpec().args().size());

        //CommandLine.usage(cmd.getSubcommands().get("run-2"), System.out);
    }

    /** @see CompactFields */
    private static class CompactFieldsMethod {
        @Command
        public CompactFields run(
            @Option(names = "-v", paramLabel="<verbose>" /* useless, but required for Assert.equals() */) boolean verbose,
            @Option(names = "-r", paramLabel="<recursive>" /* useless, but required for Assert.equals() */) boolean recursive,
            @Option(names = "-o", paramLabel="<outputFile>" /* required only for Assert.equals() */) File outputFile,
            @Parameters(paramLabel="<inputFiles>" /* required only for Assert.equals() */) File[] inputFiles)
        {
            CompactFields ret = new CommandLineTest.CompactFields();
            ret.verbose = verbose;
            ret.recursive = recursive;
            ret.outputFile = outputFile;
            ret.inputFiles = inputFiles;
            return ret;
        }
    }
    @Test
    public void testAnnotateMethod_matchesAnnotatedClass() throws Exception {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandLine classCmd = new CommandLine(new CompactFields());
        Method m = CompactFieldsMethod.class.getDeclaredMethod("run", new Class<?>[] {boolean.class, boolean.class, File.class, File[].class});
        CommandLine methodCmd = new CommandLine(m);
        assertEquals("run", methodCmd.getCommandName());
        assertEquals("argument count", classCmd.getCommandSpec().args().size(), methodCmd.getCommandSpec().args().size());
        for (int i = 0;  i < classCmd.getCommandSpec().args().size(); i++) {
            Model.ArgSpec classArg = classCmd.getCommandSpec().args().get(i);
            Model.ArgSpec methodArg = methodCmd.getCommandSpec().args().get(i);
            assertEquals("arg #" + i, classArg, methodArg);
        }
        setTraceLevel(CommandLine.TraceLevel.WARN);
    }
    /** replicate {@link CommandLineTest#testCompactFieldsAnyOrder()} but using
     * {@link CompactFieldsMethod#run(boolean, boolean, File, File[])}
     * as source of the {@link Command} annotation. */
    @Test
    public void testCompactFieldsAnyOrder_method() throws Exception {
        final Method m = CompactFieldsMethod.class.getDeclaredMethod("run", new Class<?>[] {boolean.class, boolean.class, File.class, File[].class});
        String[] tests = {
                "-rvoout",
                "-vroout",
                "-vro=out",
                "-rv p1 p2",
                "p1 p2",
                "-voout p1 p2",
                "-voout -r p1 p2",
                "-r -v -oout p1 p2",
                "-rv -o out p1 p2",
                "-oout -r -v p1 p2",
                "-rvo out p1 p2",
        };
        for (String test : tests) {
            // parse
            CompactFields compact = CommandLine.populateCommand(new CompactFields(), test.split(" "));
            List<CommandLine> result = new CommandLine(m).parse(test.split(" "));

            // extract arg values
            assertEquals(1, result.size());
            Object[] methodArgValues = result.get(0).getCommandSpec().commandMethodParamValues();
            assertNotNull(methodArgValues);

            // verify parsing had the same result
            verifyCompact(compact, (Boolean)methodArgValues[0], (Boolean)methodArgValues[1], methodArgValues[2] == null ? null : String.valueOf(methodArgValues[2]), (File[])methodArgValues[3]);

            // verify method is callable (args have the correct/assignable type)
            CompactFields methodCompact = (CompactFields) m.invoke(new CompactFieldsMethod(), methodArgValues); // should not throw

            // verify passed args are the same
            assertNotNull(methodCompact);
            assertEquals(compact.verbose, methodCompact.verbose);
            assertEquals(compact.recursive, methodCompact.recursive);
            assertEquals(compact.outputFile, methodCompact.outputFile);
            assertArrayEquals(compact.inputFiles, methodCompact.inputFiles);
        }
        try {
            CommandLine.populateCommand(m, "-oout -r -vp1 p2".split(" "));
            fail("should fail: -v does not take an argument");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-p1' (while processing option: '-vp1')", ex.getMessage());
        }
    }

    static class CommandMethod1 {
        @Command(mixinStandardHelpOptions = true, version = "1.2.3")
        public int times(@Option(names = "-l", defaultValue = "2") int left,
                         @Option(names = "-r", defaultValue = "3") int right) {
            return left * right;
        }
    }

    @Test
    public void testCommandMethodDefaults() {
        Object timesResultBothDefault = CommandLine.invoke("times", CommandMethod1.class);
        assertEquals("both default", 6, ((Integer) timesResultBothDefault).intValue());

        Object timesResultLeftDefault = CommandLine.invoke("times", CommandMethod1.class, "-r", "8");
        assertEquals("right default", 16, ((Integer) timesResultLeftDefault).intValue());

        Object timesResultRightDefault = CommandLine.invoke("times", CommandMethod1.class, "-l", "8");
        assertEquals("left default", 24, ((Integer) timesResultRightDefault).intValue());

        Object timesResultNoDefault = CommandLine.invoke("times", CommandMethod1.class, "-r", "4", "-l", "5");
        assertEquals("no default", 20, ((Integer) timesResultNoDefault).intValue());
    }

    @Test
    public void testCommandMethodMixinHelp() {
        CommandLine.invoke("times", CommandMethod1.class, "-h");
        String expected = String.format("" +
                "Usage: times [-hV] [-l=<arg0>] [-r=<arg1>]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -l=<arg0>%n" +
                "  -r=<arg1>%n" +
                "  -V, --version   Print version information and exit.%n" +
                "");
        assertEquals(expected, systemOutRule.getLog());
    }

    @Test
    public void testCommandMethodMixinVersion() {
        CommandLine.invoke("times", CommandMethod1.class, "--version");
        String expected = String.format("1.2.3%n");
        assertEquals(expected, systemOutRule.getLog());
    }

    static class UnAnnotatedClassWithoutAnnotatedFields {
        @Command public void cmd1(@Option(names = "-x") int x, File f) { }
        @Command public void cmd2(@Option(names = "-x") int x, File f) { }
    }

    @Test
    public void testMethodCommandsAreNotSubcommandsOfNonAnnotatedClass() {
        Object userObject = new UnAnnotatedClassWithoutAnnotatedFields();
        try {
            new CommandLine(userObject);
            fail("expected exception");
        } catch (CommandLine.InitializationException ex) {
            assertEquals(userObject + " " +
                            "is not a command: it has no @Command, @Option, " +
                            "@Parameters or @Unmatched annotations", ex.getMessage());
        }
    }

    static class UnAnnotatedClassWithAnnotatedField {
        @Option(names = "-y") int y;

        @Command public void cmd1(@Option(names = "-x") int x, File f) { }
        @Command public void cmd2(@Option(names = "-x") int x, File f) { }
    }

    @Test
    public void testMethodCommandsAreSubcommandsOfNonAnnotatedClassWithAnnotatedFields() {
        CommandLine cmd = new CommandLine(new UnAnnotatedClassWithAnnotatedField());
        assertNotNull(cmd.getCommandSpec().findOption('y'));

        assertFalse(cmd.getSubcommands().isEmpty());
        assertNotNull(cmd.getSubcommands().get("cmd1").getCommandSpec().findOption('x'));
    }

    @Command
    static class AnnotatedClassWithoutAnnotatedFields {
        @Command public void cmd1(@Option(names = "-x") int x, File f) { }
        @Command public void cmd2(@Option(names = "-x") int x, File f) { }
    }

    @Test
    public void testMethodCommandsAreSubcommandsOfAnnotatedClass() {
        CommandLine cmd = new CommandLine(new AnnotatedClassWithoutAnnotatedFields());
        assertNull(cmd.getCommandSpec().findOption('x'));

        assertEquals(2, cmd.getSubcommands().size());
        assertEquals(set("cmd1", "cmd2"), cmd.getSubcommands().keySet());

        String expected = String.format("" +
                "Usage: <main class> [COMMAND]%n" +
                "Commands:%n" +
                "  cmd1%n" +
                "  cmd2%n");
        assertEquals(expected, cmd.getUsageMessage());
    }

    @Command(addMethodSubcommands = false)
    static class SwitchedOff {
        @Command public void cmd1(@Option(names = "-x") int x, File f) { }
        @Command public void cmd2(@Option(names = "-x") int x, File f) { }
    }

    @Test
    public void testMethodCommandsAreNotAddedAsSubcommandsIfAnnotationSaysSo() {
        CommandLine cmd = new CommandLine(new SwitchedOff());

        assertEquals(0, cmd.getSubcommands().size());

        String expected = String.format("" +
                "Usage: <main class>%n");
        assertEquals(expected, cmd.getUsageMessage());
    }

    /** Exemple from the documentation. */
    static class Cat {
        public static void main(String[] args) {
            CommandLine.invoke("cat", Cat.class, args);
        }

        @Command(description = "Concatenate FILE(s) to standard output.",
                mixinStandardHelpOptions = true, version = "3.6.0")
        void cat(@Option(names = {"-E", "--show-ends"}) boolean showEnds,
                 @Option(names = {"-n", "--number"}) boolean number,
                 @Option(names = {"-T", "--show-tabs"}) boolean showTabs,
                 @Option(names = {"-v", "--show-nonprinting"}) boolean showNonPrinting,
                 @Parameters(paramLabel = "FILE") File[] files) {
            // process files
        }
    }
    @Test
    public void testCatUsageHelpMessage() {
        CommandLine cmd = new CommandLine(CommandLine.getCommandMethods(Cat.class, "cat").get(0));
        String expected = String.format("" +
                "Usage: cat [-EhnTvV] [FILE...]%n" +
                "Concatenate FILE(s) to standard output.%n" +
                "      [FILE...]%n" +
                "  -E, --show-ends%n" +
                "  -h, --help               Show this help message and exit.%n" +
                "  -n, --number%n" +
                "  -T, --show-tabs%n" +
                "  -v, --show-nonprinting%n" +
                "  -V, --version            Print version information and exit.%n");
        assertEquals(expected, cmd.getUsageMessage());
    }

    @Command(name = "git", mixinStandardHelpOptions = true, version = "picocli-3.6.0",
            description = "Version control system.")
    static class Git {
        @Option(names = "--git-dir", description = "Set the path to the repository")
        File path;

        @Command(description = "Clone a repository into a new directory")
        void clone(@Option(names = {"-l", "--local"}) boolean local,
                   @Option(names = "-q", description = "Operate quietly.") boolean quiet,
                   @Option(names = "-v", description = "Run verbosely.") boolean verbose,
                   @Option(names = {"-b", "--branch"}) String branch,
                   @Parameters(paramLabel = "<repository>") String repo) {
            // ... implement business logic
        }

        @Command(description = "Record changes to the repository")
        void commit(@Option(names = {"-m", "--message"}) String commitMessage,
                    @Option(names = "--squash", paramLabel = "<commit>") String squash,
                    @Parameters(paramLabel = "<file>") File[] files) {
            // ... implement business logic
        }

        @Command(description = "Update remote refs along with associated objects")
        void push(@Option(names = {"-f", "--force"}) boolean force,
                  @Option(names = "--tags") boolean tags,
                  @Parameters(paramLabel = "<repository>") String repo) {
            // ... implement business logic
        }
    }
    @Test
    public void testGitUsageHelpMessage() {
        CommandLine cmd = new CommandLine(new Git());
        String expected = String.format("" +
                "Usage: git [-hV] [--git-dir=<path>] [COMMAND]%n" +
                "Version control system.%n" +
                "      --git-dir=<path>   Set the path to the repository%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n" +
                "Commands:%n" +
                "  clone   Clone a repository into a new directory%n" +
                "  commit  Record changes to the repository%n" +
                "  push    Update remote refs along with associated objects%n");
        assertEquals(expected, cmd.getUsageMessage());
    }

    @Test
    public void testParamIndex() {
        CommandLine git = new CommandLine(new Git());
        CommandLine clone = git.getSubcommands().get("clone");
        Model.PositionalParamSpec repo = clone.getCommandSpec().positionalParameters().get(0);
        assertEquals(CommandLine.Range.valueOf("0"), repo.index());
    }

    @Command
    static class AnnotatedParams {
        @Command
        public void method(@Parameters int a,
                           @Parameters int b,
                           @Parameters int c,
                           int x,
                           int y,
                           int z) {}
    }

    @Test
    public void testParamIndexAnnotatedAndUnAnnotated() {
        CommandLine git = new CommandLine(new AnnotatedParams());
        CommandLine method = git.getSubcommands().get("method");
        List<Model.PositionalParamSpec> positionals = method.getCommandSpec().positionalParameters();
        for (int i = 0; i < positionals.size(); i++) {
            assertEquals(CommandLine.Range.valueOf("" + i), positionals.get(i).index());
        }
    }

    /** https://github.com/remkop/picocli/issues/538 */
    static class CommandMethodWithDefaults {
        @Command
        public String cmd(@Option(names = "-a", defaultValue = "2") Integer a,
                          @Option(names = "-b"                    ) Integer b,
                          @Option(names = "-c", defaultValue = "abc") String c,
                          @Option(names = "-d"                      ) String d,
                          @Option(names = "-e", defaultValue = "a=b") Map<String, String> e,
                          @Option(names = "-f"                      ) Map<String, String> f) {
            return String.format("a=%s, b=%s, c=%s, d=%s, e=%s, f=%s", a, b, c, d, e, f);
        }
    }

    @Test // for #538
    public void testCommandMethodObjectDefaults() {
        Object s1 = CommandLine.invoke("cmd", CommandMethodWithDefaults.class);
        assertEquals("nothing matched", "a=2, b=null, c=abc, d=null, e={a=b}, f=null", s1); // fails

        Object s2 = CommandLine.invoke("cmd", CommandMethodWithDefaults.class,
                "-a1", "-b2", "-cX", "-dY", "-eX=Y", "-fA=B");
        assertEquals("all matched", "a=1, b=2, c=X, d=Y, e={X=Y}, f={A=B}", s2);
    }

    private static class PrimitiveWrapper {
        @Option(names = "-0") private boolean aBool;
        @Option(names = "-1") private Boolean boolWrapper;
        @Option(names = "-b") private byte aByte;
        @Option(names = "-B") private Byte byteWrapper;
        @Option(names = "-c") private char aChar;
        @Option(names = "-C") private Character aCharacter;
        @Option(names = "-s") private short aShort;
        @Option(names = "-S") private Short shortWrapper;
        @Option(names = "-i") private int anInt;
        @Option(names = "-I") private Integer intWrapper;
        @Option(names = "-l") private long aLong;
        @Option(names = "-L") private Long longWrapper;
        @Option(names = "-d") private double aDouble;
        @Option(names = "-D") private Double doubleWrapper;
        @Option(names = "-f") private float aFloat;
        @Option(names = "-F") private Float floatWrapper;
    }

    @Test // for #538: check no regression
    public void testPrimitiveWrappersNotInitializedIfNotMatched() {
        PrimitiveWrapper s1 = CommandLine.populateCommand(new PrimitiveWrapper());
        assertEquals(false, s1.aBool);
        assertNull(s1.boolWrapper);
        assertEquals(0, s1.aByte);
        assertNull(s1.byteWrapper);
        assertEquals(0, s1.aChar);
        assertNull(s1.aCharacter);
        assertEquals(0, s1.aShort);
        assertNull(s1.shortWrapper);
        assertEquals(0, s1.anInt);
        assertNull(s1.intWrapper);
        assertEquals(0, s1.aLong);
        assertNull(s1.longWrapper);
        assertEquals(0d, s1.aDouble, 0.00001D);
        assertNull(s1.doubleWrapper);
        assertEquals(0f, s1.aFloat, 0.00001F);
        assertNull(s1.floatWrapper);
    }

    private static Set<String> set(String... elements) {
        return new HashSet<String>(Arrays.asList(elements));
    }

    /** Test for https://github.com/remkop/picocli/issues/554 */
    @Command(name = "maincommand")
    class MainCommand implements Runnable {
        @Spec
        CommandSpec spec;

        public void run() { throw new UnsupportedOperationException("must specify a subcommand"); }

        @Command
        public void subcommand(@Option(names = "-x") String x) {
            System.out.println("x=" + x);
        }

        @Command
        public void explicit(@Option(names = "-v") boolean v) {
            CommandLine commandLine = spec.subcommands().get("explicit");
            throw new CommandLine.ParameterException(commandLine, "Validation failed");
        }
    }

    @Test
    public void testSubcommandMethodInvalidInputHandling() {
        String expected = String.format("" +
                "Unknown option: '-y'%n" +
                "Usage: maincommand subcommand [-x=<arg0>]%n" +
                "  -x=<arg0>%n");

        CommandLine.run(new MainCommand(), "subcommand", "-y");
        assertEquals(expected, this.systemErrRule.getLog());
        assertEquals("", this.systemOutRule.getLog());
    }

    @Test
    public void testSubcommandMethodThrowingParameterException() {
        String expected = String.format("" +
                "Validation failed%n" +
                "Usage: maincommand explicit [-v]%n" +
                "  -v%n");

        CommandLine.run(new MainCommand(), "explicit", "-v");
        assertEquals(expected, this.systemErrRule.getLog());
        assertEquals("", this.systemOutRule.getLog());
    }

    // test (1/2) for https://github.com/remkop/picocli/issues/570
    @Test
    public void testOptionalListParameterInCommandClass() {
        @Command() class TestCommand implements Callable<String> {
            @Parameters(arity="0..*") private List<String> values;
            public String call() throws Exception { return values == null ? "null" : values.toString(); }
        }

        // seems to be working for @Command-class @Parameters
        CommandLine commandLine = new CommandLine(new TestCommand());
        List<Object> firstExecutionResultWithParametersGiven = commandLine.parseWithHandlers(
                new RunLast(),
                new DefaultExceptionHandler<List<Object>>(),
                new String[] {"arg0", "arg1"});
        List<Object> secondExecutionResultWithoutParameters = commandLine.parseWithHandlers(
                new RunLast(),
                new DefaultExceptionHandler<List<Object>>(),
                new String[] {});
        assertEquals("[arg0, arg1]", firstExecutionResultWithParametersGiven.get(0));
        assertEquals("null", secondExecutionResultWithoutParameters.get(0));
    }

    // test (2/2) for https://github.com/remkop/picocli/issues/570
    @Test
    public void testOptionalListParameterShouldNotRememberValuesInCommandMethods() {
        @Command() class TestCommand {
            @Command(name="method")
            public String methodCommand(@Parameters(arity="0..*") List<String> methodValues) {
                return methodValues == null ? "null" : methodValues.toString();
            }
        }
        CommandLine commandLine = new CommandLine(new TestCommand());

        // problematic for @Command-method @Parameters
        List<Object> methodFirstExecutionResultWithParametersGiven = commandLine.parseWithHandlers(
                new RunLast(),
                new DefaultExceptionHandler<List<Object>>(),
                new String[] {"method","arg0", "arg1"});
        List<Object> methodSecondExecutionResultWithoutParameters = commandLine.parseWithHandlers(
                new RunLast(),
                new DefaultExceptionHandler<List<Object>>(),
                new String[] {"method"});

        assertEquals("[arg0, arg1]", methodFirstExecutionResultWithParametersGiven.get(0));
        // fails, still "[arg0, arg1]"
        assertEquals("null", methodSecondExecutionResultWithoutParameters.get(0));
    }

    @Command(addMethodSubcommands = false)
    static class StaticMethodCommand {
        @Spec static CommandSpec spec;

        public StaticMethodCommand(int constructorParam) {}

        @Command
        public static int staticCommand(@Option(names = "-x") int x) {
            return x * 3;
        }

        @Command
        public void cannotBeCalled(@Option(names = "-v") boolean v) {
        }

        @Command
        public static void throwsExecutionException() {
            throw new ExecutionException(new CommandLine(new StaticMethodCommand(8)), "abc");
        }

        @Command
        public static void throwsOtherException() {
            throw new IndexOutOfBoundsException();
        }
    }

    @Test
    public void testStaticCommandMethod() {
        assertEquals(9, CommandLine.invoke("staticCommand", StaticMethodCommand.class, "-x", "3"));
    }

    @Test
    public void testInvokeMethodClassPrintStreamAnsi() {
        assertEquals(9, CommandLine.invoke("staticCommand", StaticMethodCommand.class, System.out, Help.Ansi.OFF, "-x", "3"));
    }

    @Test
    public void testCommandMethodsRequireNonArgConstructor() {
        try {
            CommandLine.invoke("cannotBeCalled", StaticMethodCommand.class);
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof NoSuchMethodException);
        }
    }

    @Test
    public void testCommandMethodsThatThrowsExecutionException() {
        try {
            CommandLine.invoke("throwsExecutionException", StaticMethodCommand.class);
        } catch (ExecutionException ex) {
            assertEquals("abc", ex.getMessage());
        }
    }

    @Test
    public void testCommandMethodsThatThrowsException() {
        try {
            CommandLine.invoke("throwsOtherException", StaticMethodCommand.class);
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof IndexOutOfBoundsException);
        }
    }

    @Command(addMethodSubcommands = false)
    static class ErroringCommand {
        public ErroringCommand() { // InvocationTargetException when invoking constructor
            throw new IllegalStateException("boom");
        }
        @Command
        public void cannotBeCalled() { }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCommandMethodsWhereConstructorThrowsException() {
        try {
            CommandLine.invoke("cannotBeCalled", ErroringCommand.class);
        } catch (ExecutionException ex) { // InvocationTargetException when invoking constructor
            assertTrue(ex.getCause() instanceof IllegalStateException);
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Error while calling command ("));
        }
    }

    @Test
    public void testCommandMethodsUnexpectedError() throws Exception {
        Method method = CommandMethod1.class.getDeclaredMethod("times", int.class, int.class);
        CommandLine cmd = new CommandLine(method);

        Method execute = CommandLine.class.getDeclaredMethod("executeUserObject", CommandLine.class, List.class);
        execute.setAccessible(true);
        try {
            execute.invoke(null, cmd, null);
        } catch (InvocationTargetException ex) {
            ExecutionException actual = (ExecutionException) ex.getCause();
            assertTrue(actual.getMessage(), actual.getMessage().startsWith("Unhandled error while calling command ("));
        }
    }

    static class Duplicate {
        @Command int mycommand() { return 1; }

        @Command int mycommand(String[] args) { return 2;}
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDuplicateCommandMethodNames() {
        try {
            CommandLine.invoke("mycommand", Duplicate.class, System.out, System.out, Help.Ansi.OFF, "abd");
        } catch (InitializationException ex) {
            assertTrue(ex.getMessage().startsWith("Expected exactly one @Command-annotated method for "));
        }
    }

    @Test
    public void testAddMethodSubcommands() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(new StaticMethodCommand(1));
        assertEquals(0, spec.subcommands().size());

        spec.addMethodSubcommands();
        assertEquals(4, spec.subcommands().size());
    }

    @Test
    public void testAddMethodSubcommands_DisallowedIfUserObjectIsMethod() throws Exception {
        Method m = MethodApp.class.getDeclaredMethod("run1", int.class);
        CommandSpec spec = CommandSpec.wrapWithoutInspection(m);

        try {
            spec.addMethodSubcommands();
        } catch (InitializationException ex) {
            assertEquals("Cannot discover subcommand methods of this Command Method: int picocli.CommandMethodTest$MethodApp.run1(int)", ex.getMessage());
        }
    }

    @Test
    public void testMethodParam_getDeclaringExecutable() throws Exception {
        Method m = MethodApp.class.getDeclaredMethod("run1", int.class);
        MethodParam param = new MethodParam(m, 0);
        assertSame(m, param.getDeclaringExecutable());
    }

    @Test
    public void testMethodParam_isAccessible() throws Exception {
        Method m = MethodApp.class.getDeclaredMethod("run1", int.class);
        MethodParam param = new MethodParam(m, 0);
        assertFalse(param.isAccessible());

        m.setAccessible(true);
        assertTrue(param.isAccessible());
    }

    static class TypedMemberObj {
        void getterNorSetter1() {}
        Void getterNorSetter2() {return null;}
        int getter() { return 0; }
        void setter(String str) { throw new IllegalStateException(); }
    }

    @Test
    public void testTypedMemberConstructorRejectsGetterNorSetter() throws Exception {
        Constructor<TypedMember> constructor = TypedMember.class.getDeclaredConstructor(Method.class, IScope.class, CommandSpec.class);
        constructor.setAccessible(true);

        Method getterNorSetter1 = TypedMemberObj.class.getDeclaredMethod("getterNorSetter1");
        Method getterNorSetter2 = TypedMemberObj.class.getDeclaredMethod("getterNorSetter2");

        try {
            constructor.newInstance(getterNorSetter1, new ObjectScope(new TypedMemberObj()), CommandSpec.create());
            fail("expect exception");
        } catch (InvocationTargetException ex) {
            InitializationException ex2 = (InitializationException) ex.getCause();
            assertEquals("Invalid method, must be either getter or setter: void picocli.CommandMethodTest$TypedMemberObj.getterNorSetter1()", ex2.getMessage());
        }
        try {
            constructor.newInstance(getterNorSetter2, new ObjectScope(new TypedMemberObj()), CommandSpec.create());
            fail("expect exception");
        } catch (InvocationTargetException ex) {
            InitializationException ex2 = (InitializationException) ex.getCause();
            assertEquals("Invalid method, must be either getter or setter: java.lang.Void picocli.CommandMethodTest$TypedMemberObj.getterNorSetter2()", ex2.getMessage());
        }
    }

    @Test
    public void testTypedMemberConstructorNonProxyObject() throws Exception {
        Constructor<TypedMember> constructor = TypedMember.class.getDeclaredConstructor(Method.class, IScope.class, CommandSpec.class);
        constructor.setAccessible(true);

        Method getter = TypedMemberObj.class.getDeclaredMethod("getter");
        TypedMember typedMember = constructor.newInstance(getter, new ObjectScope(new TypedMemberObj()), CommandSpec.create());
        assertSame(typedMember.getter(), typedMember.setter());
        assertTrue(typedMember.getter() instanceof Model.MethodBinding);
    }

    @Test
    public void testTypedMemberInitializeInitialValue() throws Exception {
        Constructor<TypedMember> constructor = TypedMember.class.getDeclaredConstructor(Method.class, IScope.class, CommandSpec.class);
        constructor.setAccessible(true);

        Method setter = TypedMemberObj.class.getDeclaredMethod("setter", String.class);
        TypedMember typedMember = constructor.newInstance(setter, new ObjectScope(new TypedMemberObj()), CommandSpec.create());

        Method initializeInitialValue = TypedMember.class.getDeclaredMethod("initializeInitialValue", Object.class);
        initializeInitialValue.setAccessible(true);

        try {
            initializeInitialValue.invoke(typedMember, "boom");
        } catch (InvocationTargetException ite) {
            InitializationException ex = (InitializationException) ite.getCause();
            assertTrue(ex.getMessage().startsWith("Could not set initial value for boom"));
        }
    }

    @Test
    public void testTypedMemberPropertyName() {
        assertEquals("aBC", TypedMember.propertyName("ABC"));
        assertEquals("blah", TypedMember.propertyName("setBlah"));
        assertEquals("blah", TypedMember.propertyName("getBlah"));
        assertEquals("isBlah", TypedMember.propertyName("isBlah"));
        assertEquals("isBlah", TypedMember.propertyName("IsBlah"));
        assertEquals("", TypedMember.propertyName(""));
    }

    @Test
    public void testTypedMemberDecapitalize() throws Exception {
        Method decapitalize = TypedMember.class.getDeclaredMethod("decapitalize", String.class);
        decapitalize.setAccessible(true);

        assertNull(decapitalize.invoke(null, (String) null));
    }

    @Command
    static class Issue905ParentCommand implements Runnable {
        public void run() {}

        @Command
        private int parameterless() {
            return 23;
        }
    }

    @Test
    public void testIssue905ParameterlessCommandMethodsException() {
        int actual = new CommandLine(new Issue905ParentCommand()).execute("parameterless");
        assertEquals(23, actual);
    }
}
