package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.DuplicateOptionAnnotationsException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

import java.util.*;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;
import static picocli.CommandLine.ScopeType.LOCAL;

public class InheritedOptionTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    // allows tests to set any kind of properties they like, without having to individually roll them back
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Command(subcommands = Sub.class)
    static class Top {
        @Option(names = "--verbose", scope = INHERIT)
        boolean verbose;
    }
    @Command(name = "sub", subcommands = SubSub.class)
    static class Sub implements Runnable {
        public void run() {
        }
    }

    @Command(name = "subsub")
    static class SubSub implements Runnable {
        public void run() {
        }
        @Command
        public void subsubsub() {
        }
    }

    @Test
    public void testGlobalOptionIsAddedToSubcommand() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);
        cmd.parseArgs("sub", "--verbose");
        assertTrue(top.verbose);
    }

    @Test
    public void testGlobalOptionIsAddedToSubSubcommand() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);
        cmd.parseArgs("sub", "subsub", "--verbose");
        assertTrue(top.verbose);
    }

    @Test
    public void testGlobalOptionDisallowedIfSubcommandAlreadyHasGlobalOptionWithSameName() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);

        class Other {
            @Option(names = "--verbose", scope = INHERIT)
            boolean verbose;
        }
        Other other = new Other();
        try {
            cmd.addSubcommand("other", other);
            fail("Expected exception");
        } catch (DuplicateOptionAnnotationsException ex) {
            String msg = String.format("Option name '--verbose' is used by both field boolean %s.verbose and field boolean %s.verbose",
                    top.getClass().getName(), other.getClass().getName());
            assertEquals(msg, ex.getMessage());
        }
        //cmd.parseArgs("other", "--verbose");
        //assertTrue(top.verbose);
        //assertFalse(other.verbose);
    }

    @Test
    public void testGlobalOptionDisallowedIfSubcommandAlreadyHasNonGlobalOptionWithSameName() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);

        class Other {
            @Option(names = "--verbose") // local
            boolean verbose;
        }
        Other other = new Other();
        try {
            cmd.addSubcommand("other", other);
            fail("Expected exception");
        } catch (DuplicateOptionAnnotationsException ex) {
            String msg = String.format("Option name '--verbose' is used by both field boolean %s.verbose and field boolean %s.verbose",
                    top.getClass().getName(), other.getClass().getName());
            assertEquals(msg, ex.getMessage());
        }
    }

    static class Base {
        @Option(names = "--verbose", scope = INHERIT)
        boolean verbose;
    }

    @Command(name = "ext",
            subcommands = ExtSub.class,
            resourceBundle = "picocli.InheritedOptionTest$MyBundle") //MyBundle.class.getName()
    static class Ext extends Base{
    }

    @Command(name = "sub")
    static class ExtSub {
        @Command void subsub() {}
    }

    @Test
    public void testGlobalOptionInBaseClass() {
        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        // both top-level command and subcommand extend from a base class where global option is defined
        Ext ext = new Ext();
        CommandLine cmd = new CommandLine(ext);
        cmd.parseArgs("sub", "subsub", "--verbose");
        assertTrue(ext.verbose);
    }

    public static class MyBundle extends ListResourceBundle { // used in Ext
        protected Object[][] getContents() {
            return new Object[][] {
                    {"verbose", "VERBOSE DESCRIPTION"},
                    {"sub.verbose", "SUB CUSTOM VERBOSE DESCRIPTION IS IGNORED"}
            };
        }
    }

    @Test
    public void testGlobalOptionDescriptionFromResourceBundle() {
        CommandLine cmd = new CommandLine(new Ext());
        String top = cmd.getUsageMessage();
        String expected = String.format("" +
                "Usage: ext [--verbose] [COMMAND]%n" +
                "      --verbose   VERBOSE DESCRIPTION%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expected, top);

        CommandLine subCmd = cmd.getSubcommands().get("sub");
        String sub = subCmd.getUsageMessage();
        String expectedSub = String.format("" +
                "Usage: ext sub [--verbose] [COMMAND]%n" +
                "      --verbose   VERBOSE DESCRIPTION%n" +
                "Commands:%n" +
                "  subsub%n");
        assertEquals(expectedSub, sub);

        CommandLine subsubCmd = subCmd.getSubcommands().get("subsub");
        String subsub = subsubCmd.getUsageMessage();
        String expectedSubSub = String.format("" +
                "Usage: ext sub subsub [--verbose]%n" +
                "      --verbose   VERBOSE DESCRIPTION%n");
        assertEquals(expectedSubSub, subsub);
    }

    @Test
    public void testProgrammaticOptionBuilderScopeLocalByDefault() {
        assertEquals(LOCAL, OptionSpec.builder("-a").scopeType());
    }

    @Test
    public void testProgrammaticOptionBuilderScopeMutable() {
        assertEquals(INHERIT, OptionSpec.builder("-a").scopeType(INHERIT).scopeType());
        assertEquals(INHERIT, OptionSpec.builder("-a").scopeType(INHERIT).build().scopeType());
    }

    @Test
    public void testProgrammaticOptionLocalByDefault() {
        assertEquals(LOCAL, OptionSpec.builder("-a").build().scopeType());
    }

    @Test
    public void testProgrammaticOptionBuilderInheritedFalseByDefault() {
        assertFalse(OptionSpec.builder("-a").inherited());
    }

    @Test
    public void testProgrammaticOptionBuilderInheritedMutable() {
        assertTrue(OptionSpec.builder("-a").inherited(true).inherited());
        assertTrue(OptionSpec.builder("-a").inherited(true).build().inherited());
    }

    @Test
    public void testProgrammaticOptionInheritedFalseByDefault() {
        assertFalse(OptionSpec.builder("-a").build().inherited());
    }

    @Test
    public void testProgrammaticAddOptionBeforeSub() {
        OptionSpec optA = OptionSpec.builder("-a").scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        spec.add(optA);
        assertFalse(optA.inherited());

        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        assertNotNull(spec.findOption("-a"));
        assertNotNull(sub.findOption("-a"));

        assertFalse(spec.findOption("-a").inherited());
        assertTrue(sub.findOption("-a").inherited());
    }

    @Test
    public void testProgrammaticAddOptionAfterSub() {
        OptionSpec optA = OptionSpec.builder("-a").scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        spec.add(optA);
        assertFalse(optA.inherited());

        assertNotNull(spec.findOption("-a"));
        assertNotNull(sub.findOption("-a"));

        assertFalse(spec.findOption("-a").inherited());
        assertTrue(sub.findOption("-a").inherited());
    }

    @Test
    public void testProgrammaticPositionalParamBuilderScopeLocalByDefault() {
        assertEquals(LOCAL, PositionalParamSpec.builder().scopeType());
    }

    @Test
    public void testProgrammaticPositionalParamBuilderScopeMutable() {
        assertEquals(INHERIT, PositionalParamSpec.builder().scopeType(INHERIT).scopeType());
        assertEquals(INHERIT, PositionalParamSpec.builder().scopeType(INHERIT).build().scopeType());
    }

    @Test
    public void testProgrammaticPositionalParamLocalByDefault() {
        assertEquals(LOCAL, PositionalParamSpec.builder().build().scopeType());
    }

    @Test
    public void testProgrammaticPositionalParamBuilderInheritedFalseByDefault() {
        assertFalse(PositionalParamSpec.builder().inherited());
    }

    @Test
    public void testProgrammaticPositionalParamBuilderInheritedMutable() {
        assertTrue(PositionalParamSpec.builder().inherited(true).inherited());
        assertTrue(PositionalParamSpec.builder().inherited(true).build().inherited());
    }

    @Test
    public void testProgrammaticPositionalParamInheritedFalseByDefault() {
        assertFalse(PositionalParamSpec.builder().build().inherited());
    }

    @Test
    public void testProgrammaticAddPositionalParamBeforeSub() {
        PositionalParamSpec positional = PositionalParamSpec.builder().scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        spec.add(positional);
        assertFalse(positional.inherited());

        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        assertFalse(spec.positionalParameters().isEmpty());
        assertFalse(sub.positionalParameters().isEmpty());

        assertFalse(spec.positionalParameters().get(0).inherited());
        assertTrue(sub.positionalParameters().get(0).inherited());
    }

    @Test
    public void testProgrammaticAddPositionalParamAfterSub() {
        PositionalParamSpec positional = PositionalParamSpec.builder().scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        spec.add(positional);
        assertFalse(positional.inherited());

        assertFalse(spec.positionalParameters().isEmpty());
        assertFalse(sub.positionalParameters().isEmpty());

        assertFalse(spec.positionalParameters().get(0).inherited());
        assertTrue(sub.positionalParameters().get(0).inherited());
    }


    @Command(name = "TopWithDefault", subcommands = SubWithDefault.class)
    static class TopWithDefault {
        List<String> xvalues = new ArrayList<String>();
        @Option(names = "-x", defaultValue = "xxx", scope = INHERIT)
        public void setX(String x) {
            xvalues.add(x);
        }

        @Option(names = "-y", scope = INHERIT)
        String y = "yyy";

        @Option(names = "-z", defaultValue = "zzz", scope = INHERIT)
        String z;
    }
    @Command(name = "sub", subcommands = SubSubWithDefault.class)
    static class SubWithDefault { }

    @Command(name = "subsub")
    static class SubSubWithDefault { }

    @Test
    public void testInheritedOptionsWithDefault() {
        TopWithDefault bean = new TopWithDefault();
        CommandLine cmd = new CommandLine(bean);
        cmd.parseArgs();

        assertEquals(Arrays.asList("xxx"), bean.xvalues);
        assertEquals("yyy", bean.y);
        assertEquals("zzz", bean.z);

        cmd.parseArgs("-y=1", "-z=2", "sub");

        assertEquals("1", bean.y);
        assertEquals("2", bean.z);
        assertEquals(Arrays.asList("xxx", "xxx"), bean.xvalues); // setters cannot be initialized

        cmd.parseArgs("sub", "subsub");

        assertEquals("zzz", bean.z);
        assertEquals(Arrays.asList("xxx", "xxx", "xxx"), bean.xvalues); // setters cannot be initialized
        assertEquals("yyy", bean.y);
    }

    @Test // https://github.com/remkop/picocli/issues/1001
    public void testInheritedRequiredArgs() {
        //System.setProperty("picocli.trace", "DEBUG");
        @Command
        class App {
            @Option(names = "-x", required = true, scope = INHERIT) int x;
            @Command void sub() {}
        }
        CommandLine cmd0 = new CommandLine(new App());
        cmd0.parseArgs("-x=2");

        CommandLine cmd1 = new CommandLine(new App());
        cmd1.parseArgs("sub", "-x=2");

        CommandLine cmd2 = new CommandLine(new App());
        try {
            cmd2.parseArgs();
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required option: '-x=<x>'", ex.getMessage());
        }

        CommandLine cmd3 = new CommandLine(new App());
        try {
            cmd3.parseArgs("sub");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required option: '-x=<x>'", ex.getMessage());
        }
    }

    @Command(subcommands = Sub.class)
    static class TopWithRequiredArg implements Runnable {
        @Option(names = "-x", required = true, scope = INHERIT)
        int x;

        @Parameters(index = "0", scope = INHERIT)
        String p0;

        @Parameters(index = "1", scope = INHERIT)
        String p1;

        @Parameters(index = "2..*", scope = INHERIT)
        String[] remainder;

        public void run() {
        }
    }
    @Test // https://github.com/remkop/picocli/issues/1001
    public void testInheritedRequiredArgsDeepNesting() {
        //System.setProperty("picocli.trace", "DEBUG");
        CommandLine cmdx = new CommandLine(new TopWithRequiredArg());
        cmdx.parseArgs("-x=2", "0", "1");

        CommandLine cmdSubX = new CommandLine(new TopWithRequiredArg());
        cmdSubX.parseArgs("sub", "-x=2", "0", "1");

        CommandLine cmdSubSubX = new CommandLine(new TopWithRequiredArg());
        cmdSubSubX.parseArgs("sub", "subsub", "-x=2", "0", "1");

        TopWithRequiredArg top4 = new TopWithRequiredArg();
        CommandLine cmdSubSubSubX = new CommandLine(top4);
        cmdSubSubSubX.parseArgs("sub", "subsub", "subsubsub", "-x=2", "0", "1", "2", "3");
        assertEquals(2, top4.x);
        assertEquals("0", top4.p0);
        assertEquals("1", top4.p1);
        assertArrayEquals(new String[]{"2", "3"}, top4.remainder);

        CommandLine cmd2 = new CommandLine(new TopWithRequiredArg());
        try {
            cmd2.parseArgs();
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required options and parameters: '-x=<x>', '<p0>', '<p1>'", ex.getMessage());
        }

        CommandLine cmd3 = new CommandLine(new TopWithRequiredArg());
        try {
            cmd3.parseArgs("sub");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required options and parameters: '-x=<x>', '<p0>', '<p1>'", ex.getMessage());
        }

        CommandLine cmd4 = new CommandLine(new TopWithRequiredArg());
        try {
            cmd4.parseArgs("sub", "subsub");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required options and parameters: '-x=<x>', '<p0>', '<p1>'", ex.getMessage());
        }

        CommandLine cmd5 = new CommandLine(new TopWithRequiredArg());
        try {
            cmd5.parseArgs("sub", "subsub", "subsubsub");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Missing required options and parameters: '-x=<x>', '<p0>', '<p1>'", ex.getMessage());
        }
    }

    @Test
    public void testIssue1042InheritedOptionsWithCommandMethods() {
        @Command(name = "issue1042")
        class Issue1042 implements Runnable {
            boolean debug;
            boolean run;
            boolean subRun;

            @Option(names = {"--debug", "-d"}, scope = INHERIT)
            protected void setDebug(boolean debug) {
                this.debug = debug;
            }

            public void run() { run = true; }

            @Command
            void sub() { subRun = true; }
        }

        Issue1042 bean = new Issue1042();
        new CommandLine(bean).execute("sub", "--debug");
        assertTrue(bean.debug);
        assertTrue(bean.subRun);
        assertFalse(bean.run);
    }

    @Command(name = "Issue1159")
    static class Issue1159 {
        @Option(names = "--xxx-yyy", scope = INHERIT)
        int x;

        @Command
        void sub() {
        }
    }

    @Test
    public void testIssue1159() {
        Issue1159 bean = new Issue1159();
        CommandLine cmd = new CommandLine(bean);
        cmd.setAbbreviatedOptionsAllowed(true);
        cmd.parseArgs("--x-y", "123");
        assertEquals(123, bean.x);

        cmd.parseArgs("sub", "--x-y", "345");
        assertEquals(345, bean.x);
    }

    @Test
    public void testIssue1159WithEquals() {
        Issue1159 bean = new Issue1159();
        CommandLine cmd = new CommandLine(bean);
        cmd.setAbbreviatedOptionsAllowed(true);
        cmd.parseArgs("--x-y=123");
        assertEquals(123, bean.x);

        cmd.parseArgs("sub", "--x-y=345");
        assertEquals(345, bean.x);
    }

    @Test
    public void testIssue1159ParseResult() {
        Issue1159 bean = new Issue1159();
        CommandLine cmd = new CommandLine(bean).setAbbreviatedOptionsAllowed(true);
        ParseResult parseResult = cmd.parseArgs("sub", "--x", "345");
        assertEquals(345, bean.x);

        assertTrue(parseResult.hasSubcommand());
        ParseResult subResult = parseResult.subcommand();

        assertTrue("Matched on the subcommand", subResult.hasMatchedOption("--xxx-yyy"));
        Object subValue = subResult.matchedOption("--xxx-yyy").getValue();
        assertEquals(345, subValue);

        assertFalse("Abbreviated options not supported in ParseResult...",
                subResult.hasMatchedOption("--x")); // this does not work

        assertFalse("Not matched on the parent command",
                parseResult.hasMatchedOption("--xxx-yyy"));
    }


    @Test
    public void testIssue1303InheritCommandWithMethodSubcommands() {
        @Command(scope = INHERIT, mixinStandardHelpOptions = true)
        class InheritApp  {
            int subCalled;

            @Command()
            void sub(@Option(names = "-foo") int foo) {
                subCalled = foo;
                //System.out.printf("Foo: %d", foo);
            }
        }
        InheritApp app = new InheritApp();
        new CommandLine(app).execute("sub", "-foo", "42" );
        assertEquals(42, app.subCalled);
    }

    @Test
    public void testIssue1316() {
        @CommandLine.Command(
                name = "example",
                mixinStandardHelpOptions = true,
                scope = CommandLine.ScopeType.INHERIT,
                subcommands = AutoComplete.GenerateCompletion.class
        )
        class Example { }

        // see also picocli-annotation-processing-tests/src/test/java/picocli/annotation/processing/tests/Issue1316Test.java
        new CommandLine(new Example()); // succeeds without error
    }

    @Test
    public void testIssue1319() {
        @Command(scope = CommandLine.ScopeType.INHERIT
                , mixinStandardHelpOptions = true
                , subcommands = { CommandLine.HelpCommand.class }
                )
        class InheritHelpApp  {
            int subFoo;

            @Command()
            void sub(@Option(names = "-foo") int foo) {
                subFoo = foo;
            }
        }
        InheritHelpApp app = new InheritHelpApp();
        new CommandLine(app).execute("sub", "-foo", "42" );
        assertEquals(42, app.subFoo);
    }

    @Test
    public void testIssue1331() {
        @Command(scope = CommandLine.ScopeType.INHERIT)
        class InheritHelpApp  {
            int subFoo;

            @Command(mixinStandardHelpOptions = true)
            void sub(@Option(names = "-foo") int foo) {
                System.out.printf("Foo: %d", foo);
                subFoo = foo;
            }
        }
        InheritHelpApp app = new InheritHelpApp();
        new CommandLine(app).execute("sub", "-foo", "42" );
        assertEquals(42, app.subFoo);
    }
}
