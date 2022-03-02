package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IExecutionStrategy;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.MissingTypeConverterException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UsageMessageSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.RunAll;
import picocli.CommandLine.Spec;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;
import static picocli.TestUtil.setOf;
import static picocli.TestUtil.setTraceLevel;

public class SubcommandTests {
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "top")
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

    @Test
    public void testAddSubcommandWithoutNameRequiresAnnotationName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        try {
            cmd.addSubcommand(new ChildCommand1());
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Cannot add subcommand with null name to top", ex.getMessage());
        }
    }

    @Command(name = "annotationName")
    static class SubcommandWithAnnotationName {}

    @Test
    public void testAddSubcommandWithoutNameUsesInstanceAnnotationName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        Object userObject = new SubcommandWithAnnotationName();
        cmd.addSubcommand(userObject);
        assertTrue(cmd.getSubcommands().containsKey("annotationName"));
        assertSame(userObject, cmd.getSubcommands().get("annotationName").getCommand());
    }

    @Test
    public void testAddSubcommandWithoutNameUsesClassAnnotationName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand(SubcommandWithAnnotationName.class);
        assertTrue(cmd.getSubcommands().containsKey("annotationName"));
        Object userObject = cmd.getSubcommands().get("annotationName").getCommand();
        assertTrue(userObject instanceof SubcommandWithAnnotationName);
    }

    @Test
    public void testAddSubcommandWithoutNameUsesInstanceSpecName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        Object userObject = new SubcommandWithAnnotationName();
        CommandSpec instanceSpec = CommandSpec.forAnnotatedObject(userObject);
        cmd.addSubcommand(instanceSpec);
        assertTrue(cmd.getSubcommands().containsKey("annotationName"));
        assertSame(userObject, cmd.getSubcommands().get("annotationName").getCommand());
    }

    @Test
    public void testAddSubcommandWithoutNameUsesClassSpecName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        CommandSpec classSpec = CommandSpec.forAnnotatedObject(SubcommandWithAnnotationName.class);
        cmd.addSubcommand(classSpec);
        assertTrue(cmd.getSubcommands().containsKey("annotationName"));
        Object userObject = cmd.getSubcommands().get("annotationName").getCommand();
        assertTrue(userObject instanceof SubcommandWithAnnotationName);
    }

    @Test
    public void testAddSubcommandWithoutNameUsesCustomSpecName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        CommandSpec classSpec = CommandSpec.create().name("random");
        cmd.addSubcommand(classSpec);
        assertTrue(cmd.getSubcommands().containsKey("random"));
        Object userObject = cmd.getSubcommands().get("random").getCommand();
        assertNull(userObject);
    }

    @Test
    public void testAddSubcommandWithoutNameUsesInstanceCommandLineName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        Object userObject = new SubcommandWithAnnotationName();
        CommandLine instanceCmd = new CommandLine(userObject);
        cmd.addSubcommand(instanceCmd);
        assertTrue(cmd.getSubcommands().containsKey("annotationName"));
        assertSame(userObject, cmd.getSubcommands().get("annotationName").getCommand());
    }

    @Test
    public void testAddSubcommandWithoutNameUsesClassCommandLineName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        CommandLine classCmd = new CommandLine(SubcommandWithAnnotationName.class);
        cmd.addSubcommand(classCmd);
        assertTrue(cmd.getSubcommands().containsKey("annotationName"));
        Object userObject = cmd.getSubcommands().get("annotationName").getCommand();
        assertTrue(userObject instanceof SubcommandWithAnnotationName);
    }

    @Command(aliases = {"alias1", "alias2", "bobobo"})
    static class SubcommandWithAliases {}

    @Test
    public void testAddSubcommandWithoutNameUsesInstanceAnnotationAliases() {
        CommandLine cmd = new CommandLine(new MainCommand());
        Object userObject = new SubcommandWithAliases();
        cmd.addSubcommand(userObject);
        assertTrue(cmd.getSubcommands().containsKey("alias1"));
        assertSame(userObject, cmd.getSubcommands().get("alias1").getCommand());
        assertArrayEquals(new String[]{"alias2", "bobobo"}, cmd.getSubcommands().get("alias1").getCommandSpec().aliases());
    }

    @Test
    public void testAddSubcommandWithoutNameUsesClassAnnotationAliases() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand(SubcommandWithAliases.class);
        assertTrue(cmd.getSubcommands().containsKey("alias1"));
        Object userObject = cmd.getSubcommands().get("alias1").getCommand();
        assertTrue(userObject instanceof SubcommandWithAliases);
        assertArrayEquals(new String[]{"alias2", "bobobo"}, cmd.getSubcommands().get("alias1").getCommandSpec().aliases());
    }

    @Test
    public void testRemoveSubcommandByMainName() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("main");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByAlias() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("alias1");

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("alias1");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByMainNameCaseInsensitive() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        cmd.setSubcommandsCaseInsensitive(true);

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("MAIN");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByAliasCaseInsensitive() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        cmd.setSubcommandsCaseInsensitive(true);

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("ALIAS2");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByMainNameAbbreviated() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        cmd.setAbbreviatedSubcommandsAllowed(true);

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("ma");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByAliasAbbreviated() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        cmd.setAbbreviatedSubcommandsAllowed(true);

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("bo");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByMainNameAbbreviatedCaseInsensitive() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        cmd.setSubcommandsCaseInsensitive(true);
        cmd.setAbbreviatedSubcommandsAllowed(true);

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("MA");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);
    }

    @Test
    public void testRemoveSubcommandByAliasAbbreviatedCaseInsensitive() {
        CommandLine cmd = new CommandLine(new MainCommand());
        cmd.addSubcommand("main", new SubcommandWithAliases());
        assertEquals(4, cmd.getSubcommands().size());
        CommandLine sub = cmd.getSubcommands().get("main");

        cmd.setSubcommandsCaseInsensitive(true);
        cmd.setAbbreviatedSubcommandsAllowed(true);

        TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);

        CommandLine removed = cmd.getCommandSpec().removeSubcommand("BO");
        assertEquals(0, cmd.getSubcommands().size());
        assertEquals(0, cmd.getCommandSpec().subcommands().size());
        assertSame(sub, removed);

        String line = String.format("[picocli DEBUG] Removed 4 subcommand entries [alias1, alias2, bobobo, main] for key 'BO' from 'top'%n");
        assertEquals(line, systemErrRule.getLog());
    }

    @Test
    public void testAddSubcommandAliasTrace() {
        TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        MainCommand top = new MainCommand();
        CommandLine cmd = new CommandLine(top);
        SubcommandWithAliases sub = new SubcommandWithAliases();
        cmd.addSubcommand("sub", sub);

        String expected = String.format("" +
            "[picocli DEBUG] Creating CommandSpec for picocli.SubcommandTests$MainCommand@%s with factory picocli.CommandLine$DefaultFactory%n" +
            "[picocli DEBUG] Creating CommandSpec for picocli.SubcommandTests$SubcommandWithAliases@%s with factory picocli.CommandLine$DefaultFactory%n" +
            "[picocli DEBUG] Adding subcommand 'sub' to 'top'%n" +
            "[picocli DEBUG] Adding alias 'top alias1' for 'top sub'%n" +
            "[picocli DEBUG] Adding alias 'top alias2' for 'top sub'%n" +
            "[picocli DEBUG] Adding alias 'top bobobo' for 'top sub'%n",
            Integer.toHexString(top.hashCode()), Integer.toHexString(sub.hashCode()));
        assertEquals(expected, systemErrRule.getLog());
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
        assertTrue("cmd1", ((Object) commandMap.get("cmd1").getCommand()) instanceof ChildCommand1);
        assertTrue("cmd2", ((Object) commandMap.get("cmd2").getCommand()) instanceof ChildCommand2);
    }

    @Test
    public void testCommandListReturnsAliases() {
        CommandLine commandLine = createNestedCommandWithAliases();

        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertEquals(6, commandMap.size());
        assertEquals(setOf("cmd1", "cmd1alias1", "cmd1alias2", "cmd2", "cmd2alias1", "cmd2alias2"), commandMap.keySet());
        assertTrue("cmd1", ((Object) commandMap.get("cmd1").getCommand()) instanceof ChildCommand1);
        assertSame(commandMap.get("cmd1"), commandMap.get("cmd1alias1"));
        assertSame(commandMap.get("cmd1"), commandMap.get("cmd1alias2"));

        assertTrue("cmd2", ((Object) commandMap.get("cmd2").getCommand()) instanceof ChildCommand2);
        assertSame(commandMap.get("cmd2"), commandMap.get("cmd2alias1"));
        assertSame(commandMap.get("cmd2"), commandMap.get("cmd2alias2"));

        CommandLine cmd2 = commandMap.get("cmd2");
        Map<String, CommandLine> subMap = cmd2.getSubcommands();

        assertTrue("cmd2", ((Object) subMap.get("sub21").getCommand()) instanceof GrandChild2Command1);
        assertSame(subMap.get("sub21"), subMap.get("sub21alias1"));
        assertSame(subMap.get("sub21"), subMap.get("sub21alias2"));
    }

    @Command(name = "cb")
    static class Issue443TopLevelCommand implements Runnable  {
        boolean topWasExecuted;
        public void run() {
            topWasExecuted = true;
        }
    }

    @Command(name = "task", aliases = {"t"}, description = "subcommand with alias")
    static class SubCommandWithAlias implements Runnable {
        boolean subWasExecuted;
        public void run() {
            subWasExecuted = true;
        }
    }

    @Test
    public void testIssue443SubcommandWithAliasAnnotation() {
        Issue443TopLevelCommand top = new Issue443TopLevelCommand();
        SubCommandWithAlias sub = new SubCommandWithAlias();
        CommandLine cmd = new CommandLine(top).addSubcommand("task", sub);
        cmd.setExecutionStrategy(new RunAll());
        cmd.execute("t");
        assertTrue("top was executed", top.topWasExecuted);
        assertTrue("sub was executed", sub.subWasExecuted);
    }

    @Test
    public void testIssue444SubcommandWithDuplicateAliases() {
        Issue443TopLevelCommand top = new Issue443TopLevelCommand();
        SubCommandWithAlias sub = new SubCommandWithAlias();
        CommandLine cmd = new CommandLine(top).addSubcommand("task", sub, "t", "t");
        CommandSpec subSpec = cmd.getSubcommands().get("task").getCommandSpec();
        String expected = String.format("" +
                "Usage: cb [COMMAND]%n" +
                "Commands:%n" +
                "  task, t  subcommand with alias%n");
        assertEquals(expected, cmd.getUsageMessage());
        assertArrayEquals(new String[]{"t"}, subSpec.aliases());
    }

    @SuppressWarnings("deprecation")
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
            createNestedCommand().parseArgs("cmd1", "sub11", "sub12");
            fail("Expected exception for sub12");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 2: 'sub12'", ex.getMessage());
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
            createNestedCommand().parseArgs("-a", "-b", "cmd1");
            fail("unmatched option should prevents remainder to be parsed as command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unknown option: '-b'", ex.getMessage());
        }
        try {
            createNestedCommand().parseArgs("cmd1", "sub21");
            fail("sub-commands for different parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 1: 'sub21'", ex.getMessage());
        }
        try {
            createNestedCommand().parseArgs("cmd1", "sub22sub1");
            fail("sub-sub-commands for different parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 1: 'sub22sub1'", ex.getMessage());
        }
        try {
            createNestedCommand().parseArgs("sub11");
            fail("sub-commands without preceding parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 0: 'sub11'", ex.getMessage());
        }
        try {
            createNestedCommand().parseArgs("sub21");
            fail("sub-commands without preceding parent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 0: 'sub21'", ex.getMessage());
        }
        try {
            createNestedCommand().parseArgs("sub22sub1");
            fail("sub-sub-commands without preceding parent/grandparent command");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 0: 'sub22sub1'", ex.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
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
            assertEquals("Unmatched argument at index 2: 'sub12alias1'", ex.getMessage());
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

    @SuppressWarnings("deprecation")
    @Test
    public void testParseNestedSubCommandsAllowingUnmatchedArguments() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
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
    /** Subcommand with default constructor */
    @Command(name = "subsub1")
    static class SubSub1_testDeclarativelyAddSubcommands {}

    @Command(name = "sub1", subcommands = {SubSub1_testDeclarativelyAddSubcommands.class})
    static class Sub1_testDeclarativelyAddSubcommands {public Sub1_testDeclarativelyAddSubcommands(){}}

    @Command(subcommands = {Sub1_testDeclarativelyAddSubcommands.class})
    static class MainCommand_testDeclarativelyAddSubcommands {}
    @Test
    public void testFactory() {
        final Sub1_testDeclarativelyAddSubcommands sub1Command = new Sub1_testDeclarativelyAddSubcommands();
        final SubSub1_testDeclarativelyAddSubcommands subsub1Command = new SubSub1_testDeclarativelyAddSubcommands();
        IFactory factory = new IFactory() {
            @SuppressWarnings("unchecked")
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
                    "picocli.SubcommandTests$Sub1_testDeclarativelyAddSubcommands: " +
                    "java.lang.IllegalStateException: bad class", ex.getMessage());
        }
    }

    @Test
    public void testDeclarativelyAddSubcommands() {
        CommandLine main = new CommandLine(new MainCommand_testDeclarativelyAddSubcommands());
        assertEquals(1, main.getSubcommands().size());

        CommandLine sub1 = main.getSubcommands().get("sub1");
        assertEquals(Sub1_testDeclarativelyAddSubcommands.class, ((Object) sub1.getCommand()).getClass());

        assertEquals(1, sub1.getSubcommands().size());
        CommandLine subsub1 = sub1.getSubcommands().get("subsub1");
        assertEquals(SubSub1_testDeclarativelyAddSubcommands.class, ((Object) subsub1.getCommand()).getClass());
    }
    @Test
    public void testGetParentForDeclarativelyAddedSubcommands() {
        CommandLine main = new CommandLine(new MainCommand_testDeclarativelyAddSubcommands());
        assertEquals(1, main.getSubcommands().size());

        CommandLine sub1 = main.getSubcommands().get("sub1");
        assertSame(main, sub1.getParent());
        assertEquals(Sub1_testDeclarativelyAddSubcommands.class, ((Object) sub1.getCommand()).getClass());

        assertEquals(1, sub1.getSubcommands().size());
        CommandLine subsub1 = sub1.getSubcommands().get("subsub1");
        assertSame(sub1, subsub1.getParent());
        assertEquals(SubSub1_testDeclarativelyAddSubcommands.class, ((Object) subsub1.getCommand()).getClass());
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
        @Command
        class Top {}
        assertNull(new CommandLine(new Top()).getParent());
    }
    @Test
    public void testDeclarativelyAddSubcommandsSucceedsWithDefaultConstructorForDefaultFactory() {
        @Command(subcommands = {SubSub1_testDeclarativelyAddSubcommands.class}) class MainCommand {}
        CommandLine cmdLine = new CommandLine(new MainCommand());
        assertEquals(SubSub1_testDeclarativelyAddSubcommands.class.getName(), ((Object) cmdLine.getSubcommands().get("subsub1").getCommand()).getClass().getName());
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutNoArgConstructor() {
        @Command(name = "sub1") class ABC { public ABC(String constructorParam) {} }
        @Command(subcommands = {ABC.class}) class MainCommand {}
        CommandLine cmd = new CommandLine(new MainCommand(), new InnerClassFactory(this));
        try {
            cmd.parseArgs("sub1");
            fail("Expected exception");
        } catch (InitializationException ex) {
            String prefix = String.format("Could not instantiate %s either with or without construction parameter picocli.SubcommandTests@", ABC.class.getName());
            String suffix = String.format("java.lang.NoSuchMethodException: %s.<init>(picocli.SubcommandTests)", ABC.class.getName());

            assertTrue(ex.getMessage(), ex.getMessage().startsWith(prefix));
            assertTrue(ex.getMessage(), ex.getMessage().endsWith(suffix));
        }
    }
    @Test
    public void testDeclarativelyAddSubcommandsSucceedsWithDefaultConstructor() {
        @Command(name = "sub1") class ABCD {}
        @Command(subcommands = {ABCD.class}) class MainCommand {}
        CommandLine cmdLine = new CommandLine(new MainCommand(), new InnerClassFactory(this));
        assertEquals("picocli.SubcommandTests$1ABCD", ((Object) cmdLine.getSubcommands().get("sub1").getCommand()).getClass().getName());
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutAnnotation() {
        class MissingCommandAnnotation { public MissingCommandAnnotation() {} }
        @Command(subcommands = {MissingCommandAnnotation.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand(), new InnerClassFactory(this));
            fail("Expected exception");
        } catch (InitializationException ex) {
            String expected = String.format("%s is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations", MissingCommandAnnotation.class.toString());
            assertEquals(expected, ex.getMessage());
        }
    }
    @Test
    public void testDeclarativelyAddSubcommandsFailsWithoutNameOnCommandAnnotation() {
        @Command
        class MissingNameAttribute{ public MissingNameAttribute() {} }
        @Command(subcommands = {MissingNameAttribute.class}) class MainCommand {}
        try {
            new CommandLine(new MainCommand(), new InnerClassFactory(this));
            fail("Expected exception");
        } catch (InitializationException ex) {
            String expected = String.format("Subcommand %s is missing the mandatory @Command annotation with a 'name' attribute", MissingNameAttribute.class.getName());
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testCustomTypeConverterNotRegisteredAtAll() {
        CommandLine commandLine = createNestedCommand();
        commandLine.parseArgs("cmd1", "sub12", "-e", "TXT");
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testCustomTypeConverterRegisteredBeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.registerConverter(CustomType.class, new CustomType(null));

        commandLine.addSubcommand("main", createNestedCommand());
        commandLine.parseArgs("main", "cmd1", "sub12", "-e", "TXT");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCustomTypeConverterRegisteredAfterSubcommandsAdded() {
        @Command
        class TopLevel { public boolean equals(Object o) {return getClass().equals(o.getClass());}}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        commandLine.registerConverter(CustomType.class, new CustomType(null));
        List<CommandLine> parsed = commandLine.parse("main", "cmd1", "sub12", "-e", "TXT");
        assertEquals(4, parsed.size());
        assertEquals(TopLevel.class, ((Object) parsed.get(0).getCommand()).getClass());
        assertFalse(((MainCommand)   parsed.get(1).getCommand()).a);
        assertFalse(((ChildCommand1) parsed.get(2).getCommand()).b);
        assertEquals("TXT", ((GrandChild1Command2) parsed.get(3).getCommand()).e.val);
    }

    @Test
    public void testSetSeparator_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
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
        @Command
        class TopLevel {}
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
    public void testSetEndOfOptionsDelimiter_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals("--", commandLine.getEndOfOptionsDelimiter());
        commandLine.setEndOfOptionsDelimiter("@@");
        assertEquals("@@", commandLine.getEndOfOptionsDelimiter());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", "--", sub.getEndOfOptionsDelimiter());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", "--", subsub.getEndOfOptionsDelimiter());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetEndOfOptionsDelimiter_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals("--", commandLine.getEndOfOptionsDelimiter());
        commandLine.setEndOfOptionsDelimiter("@@");
        assertEquals("@@", commandLine.getEndOfOptionsDelimiter());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", "@@", sub.getEndOfOptionsDelimiter());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", "@@", sub.getEndOfOptionsDelimiter());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUsageHelpWidth_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        int DEFAULT = UsageMessageSpec.DEFAULT_USAGE_WIDTH;
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
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        int DEFAULT = UsageMessageSpec.DEFAULT_USAGE_WIDTH;
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
    public void testSetUsageHelpAutoWidth_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        boolean DEFAULT = false;
        assertEquals(DEFAULT, commandLine.isUsageHelpAutoWidth());
        commandLine.setUsageHelpAutoWidth(!DEFAULT);
        assertEquals(!DEFAULT, commandLine.isUsageHelpAutoWidth());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", DEFAULT, sub.isUsageHelpAutoWidth());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", DEFAULT, subsub.isUsageHelpAutoWidth());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUsageHelpAutoWidth_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        boolean DEFAULT = false;
        assertEquals(DEFAULT, commandLine.isUsageHelpAutoWidth());
        commandLine.setUsageHelpAutoWidth(!DEFAULT);
        assertEquals(!DEFAULT, commandLine.isUsageHelpAutoWidth());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", !DEFAULT, sub.isUsageHelpAutoWidth());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", !DEFAULT, sub.isUsageHelpAutoWidth());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUsageHelpLongOptionsMaxWidth_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        int DEFAULT = UsageMessageSpec.DEFAULT_USAGE_LONG_OPTIONS_WIDTH;
        assertEquals(DEFAULT, commandLine.getUsageHelpLongOptionsMaxWidth());
        commandLine.setUsageHelpLongOptionsMaxWidth(50);
        assertEquals(50, commandLine.getUsageHelpLongOptionsMaxWidth());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", DEFAULT, sub.getUsageHelpLongOptionsMaxWidth());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", DEFAULT, subsub.getUsageHelpLongOptionsMaxWidth());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUsageHelpLongOptionsMaxWidth_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        int DEFAULT = UsageMessageSpec.DEFAULT_USAGE_LONG_OPTIONS_WIDTH;
        assertEquals(DEFAULT, commandLine.getUsageHelpLongOptionsMaxWidth());
        commandLine.setUsageHelpLongOptionsMaxWidth(50);
        assertEquals(50, commandLine.getUsageHelpLongOptionsMaxWidth());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", 50, sub.getUsageHelpLongOptionsMaxWidth());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", 50, sub.getUsageHelpLongOptionsMaxWidth());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetAdjustLineBreaksForWideCJKCharacters_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        boolean DEFAULT = UsageMessageSpec.DEFAULT_ADJUST_CJK;
        assertEquals(DEFAULT, commandLine.isAdjustLineBreaksForWideCJKCharacters());
        commandLine.setAdjustLineBreaksForWideCJKCharacters(!DEFAULT);
        assertEquals(!DEFAULT, commandLine.isAdjustLineBreaksForWideCJKCharacters());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", DEFAULT, sub.isAdjustLineBreaksForWideCJKCharacters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", DEFAULT, subsub.isAdjustLineBreaksForWideCJKCharacters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetAdjustLineBreaksForWideCJKCharacters_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        boolean DEFAULT = UsageMessageSpec.DEFAULT_ADJUST_CJK;
        assertEquals(DEFAULT, commandLine.isAdjustLineBreaksForWideCJKCharacters());
        commandLine.setAdjustLineBreaksForWideCJKCharacters(!DEFAULT);
        assertEquals(!DEFAULT, commandLine.isAdjustLineBreaksForWideCJKCharacters());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", !DEFAULT, sub.isAdjustLineBreaksForWideCJKCharacters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", !DEFAULT, sub.isAdjustLineBreaksForWideCJKCharacters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testInterpolateVariables_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(true, commandLine.isInterpolateVariables());
        commandLine.setInterpolateVariables(false);
        assertEquals(false, commandLine.isInterpolateVariables());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", true, sub.isInterpolateVariables());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", true, subsub.isInterpolateVariables());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testInterpolateVariables_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(true, commandLine.isInterpolateVariables());
        commandLine.setInterpolateVariables(false);
        assertEquals(false, commandLine.isInterpolateVariables());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", false, sub.isInterpolateVariables());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", false, sub.isInterpolateVariables());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testExitCodeExceptionMapper_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(null, commandLine.getExitCodeExceptionMapper());
        IExitCodeExceptionMapper mapper = new IExitCodeExceptionMapper() {
            public int getExitCode(Throwable exception) { return 0; }
        };
        commandLine.setExitCodeExceptionMapper(mapper);
        assertEquals(mapper, commandLine.getExitCodeExceptionMapper());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", null, sub.getExitCodeExceptionMapper());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", null, subsub.getExitCodeExceptionMapper());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testExitCodeExceptionMapper_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(null, commandLine.getExitCodeExceptionMapper());
        IExitCodeExceptionMapper mapper = new IExitCodeExceptionMapper() {
            public int getExitCode(Throwable exception) { return 0; }
        };
        commandLine.setExitCodeExceptionMapper(mapper);
        assertEquals(mapper, commandLine.getExitCodeExceptionMapper());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", mapper, sub.getExitCodeExceptionMapper());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", mapper, sub.getExitCodeExceptionMapper());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testExecutionStrategy_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        IExecutionStrategy original = commandLine.getExecutionStrategy();
        assertTrue(original instanceof CommandLine.RunLast);
        IExecutionStrategy strategy = new IExecutionStrategy() {
            public int execute(ParseResult parseResult) throws ExecutionException, ParameterException {
                return 0;
            }
        };
        commandLine.setExecutionStrategy(strategy);
        assertEquals(strategy, commandLine.getExecutionStrategy());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertTrue("subcommand added afterwards is not impacted", sub.getExecutionStrategy() instanceof CommandLine.RunLast);
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertTrue("subcommand added afterwards is not impacted", subsub.getExecutionStrategy() instanceof CommandLine.RunLast);
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testExecutionStrategy_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        IExecutionStrategy original = commandLine.getExecutionStrategy();
        assertTrue(original instanceof CommandLine.RunLast);
        IExecutionStrategy strategy = new IExecutionStrategy() {
            public int execute(ParseResult parseResult) throws ExecutionException, ParameterException {
                return 0;
            }
        };
        commandLine.setExecutionStrategy(strategy);
        assertEquals(strategy, commandLine.getExecutionStrategy());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", strategy, sub.getExecutionStrategy());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", strategy, sub.getExecutionStrategy());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParameterExceptionHandler_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        IParameterExceptionHandler original = commandLine.getParameterExceptionHandler();
        assertNotNull(original);
        IParameterExceptionHandler strategy = new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) { return 0; }
        };
        commandLine.setParameterExceptionHandler(strategy);
        assertEquals(strategy, commandLine.getParameterExceptionHandler());
        assertNotEquals(original, commandLine.getParameterExceptionHandler());
        assertNotEquals(original.getClass(), commandLine.getParameterExceptionHandler().getClass());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", original.getClass(), sub.getParameterExceptionHandler().getClass());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", original.getClass(), subsub.getParameterExceptionHandler().getClass());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParameterExceptionHandler_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        IParameterExceptionHandler original = commandLine.getParameterExceptionHandler();
        assertNotNull(original);
        IParameterExceptionHandler strategy = new IParameterExceptionHandler() {
            public int handleParseException(ParameterException ex, String[] args) { return 0; }
        };
        commandLine.setParameterExceptionHandler(strategy);
        assertEquals(strategy, commandLine.getParameterExceptionHandler());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", strategy, sub.getParameterExceptionHandler());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", strategy, sub.getParameterExceptionHandler());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testExecutionExceptionHandler_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        IExecutionExceptionHandler original = commandLine.getExecutionExceptionHandler();
        assertNotNull(original);
        IExecutionExceptionHandler strategy = new IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) { return 0; }
        };
        commandLine.setExecutionExceptionHandler(strategy);
        assertEquals(strategy, commandLine.getExecutionExceptionHandler());
        assertNotEquals(original, commandLine.getExecutionExceptionHandler());
        assertNotEquals(original.getClass(), commandLine.getExecutionExceptionHandler().getClass());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", original.getClass(), sub.getExecutionExceptionHandler().getClass());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", original.getClass(), subsub.getExecutionExceptionHandler().getClass());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testExecutionExceptionHandler_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        IExecutionExceptionHandler original = commandLine.getExecutionExceptionHandler();
        assertNotNull(original);
        IExecutionExceptionHandler strategy = new IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) { return 0; }
        };
        commandLine.setExecutionExceptionHandler(strategy);
        assertEquals(strategy, commandLine.getExecutionExceptionHandler());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", strategy, sub.getExecutionExceptionHandler());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", strategy, sub.getExecutionExceptionHandler());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testErr_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        PrintWriter original = commandLine.getErr();
        assertNotNull(original);
        PrintWriter err = new PrintWriter(new StringWriter());
        commandLine.setErr(err);
        assertEquals(err, commandLine.getErr());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertNotSame("subcommand added afterwards is not impacted", err, sub.getErr().getClass());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertNotSame("subcommand added afterwards is not impacted", err, subsub.getErr().getClass());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testErr_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        PrintWriter original = commandLine.getErr();
        assertNotNull(original);
        PrintWriter err = new PrintWriter(new StringWriter());
        commandLine.setErr(err);
        assertEquals(err, commandLine.getErr());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertSame("subcommand added before IS impacted", err, sub.getErr());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertSame("subsubcommand added before IS impacted", err, sub.getErr());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testOut_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        PrintWriter original = commandLine.getOut();
        assertNotNull(original);
        PrintWriter out = new PrintWriter(new StringWriter());
        commandLine.setOut(out);
        assertEquals(out, commandLine.getOut());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertNotSame("subcommand added afterwards is not impacted", out, sub.getOut().getClass());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertNotSame("subcommand added afterwards is not impacted", out, subsub.getOut().getClass());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testOut_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        PrintWriter original = commandLine.getOut();
        assertNotNull(original);
        PrintWriter out = new PrintWriter(new StringWriter());
        commandLine.setOut(out);
        assertEquals(out, commandLine.getOut());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertSame("subcommand added before IS impacted", out, sub.getOut());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertSame("subsubcommand added before IS impacted", out, sub.getOut());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testColorScheme_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        CommandLine.Help.ColorScheme original = commandLine.getColorScheme();
        assertEquals(Arrays.asList(CommandLine.Help.Ansi.Style.bold), original.commandStyles());

        CommandLine.Help.ColorScheme scheme = new CommandLine.Help.ColorScheme.Builder()
                .commands(CommandLine.Help.Ansi.Style.fg_black, CommandLine.Help.Ansi.Style.bg_cyan)
                .build();
        commandLine.setColorScheme(scheme);
        assertEquals(scheme, commandLine.getColorScheme());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertNotSame("subcommand added afterwards is not impacted", scheme, sub.getColorScheme().getClass());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertNotSame("subcommand added afterwards is not impacted", scheme, subsub.getColorScheme().getClass());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testColorScheme_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        CommandLine.Help.ColorScheme original = commandLine.getColorScheme();
        assertEquals(Arrays.asList(CommandLine.Help.Ansi.Style.bold), original.commandStyles());

        CommandLine.Help.ColorScheme scheme = new CommandLine.Help.ColorScheme.Builder()
                .commands(CommandLine.Help.Ansi.Style.fg_black, CommandLine.Help.Ansi.Style.bg_cyan)
                .build();
        commandLine.setColorScheme(scheme);
        assertEquals(scheme, commandLine.getColorScheme());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertSame("subcommand added before IS impacted", scheme, sub.getColorScheme());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertSame("subsubcommand added before IS impacted", scheme, sub.getColorScheme());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserToggleBooleanFlags_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isToggleBooleanFlags());
        commandLine.setToggleBooleanFlags(true);
        assertEquals(true, commandLine.isToggleBooleanFlags());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isToggleBooleanFlags());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isToggleBooleanFlags());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserToggleBooleanFlags_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isToggleBooleanFlags());
        commandLine.setToggleBooleanFlags(true);
        assertEquals(true, commandLine.isToggleBooleanFlags());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isToggleBooleanFlags());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isToggleBooleanFlags());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserCaseInsensitiveEnumValuesAllowed_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isCaseInsensitiveEnumValuesAllowed());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        assertEquals(true, commandLine.isCaseInsensitiveEnumValuesAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isCaseInsensitiveEnumValuesAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isCaseInsensitiveEnumValuesAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserCaseInsensitiveEnumValuesAllowed_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isCaseInsensitiveEnumValuesAllowed());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        assertEquals(true, commandLine.isCaseInsensitiveEnumValuesAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isCaseInsensitiveEnumValuesAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isCaseInsensitiveEnumValuesAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSubcommandsCaseInsensitive_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isSubcommandsCaseInsensitive());
        commandLine.setSubcommandsCaseInsensitive(true);
        assertEquals(true, commandLine.isSubcommandsCaseInsensitive());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isSubcommandsCaseInsensitive());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isSubcommandsCaseInsensitive());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSubcommandsCaseInsensitive_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isSubcommandsCaseInsensitive());
        commandLine.setSubcommandsCaseInsensitive(true);
        assertEquals(true, commandLine.isSubcommandsCaseInsensitive());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isSubcommandsCaseInsensitive());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isSubcommandsCaseInsensitive());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testOptionsCaseInsensitive_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isOptionsCaseInsensitive());
        commandLine.setOptionsCaseInsensitive(true);
        assertEquals(true, commandLine.isOptionsCaseInsensitive());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isOptionsCaseInsensitive());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isOptionsCaseInsensitive());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testOptionsCaseInsensitive_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isOptionsCaseInsensitive());
        commandLine.setOptionsCaseInsensitive(true);
        assertEquals(true, commandLine.isOptionsCaseInsensitive());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isOptionsCaseInsensitive());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isOptionsCaseInsensitive());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testAbbrevSubcommands_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isAbbreviatedSubcommandsAllowed());
        commandLine.setAbbreviatedSubcommandsAllowed(true);
        assertEquals(true, commandLine.isAbbreviatedSubcommandsAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isAbbreviatedSubcommandsAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isAbbreviatedSubcommandsAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testAbbrevSubcommands_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isAbbreviatedSubcommandsAllowed());
        commandLine.setAbbreviatedSubcommandsAllowed(true);
        assertEquals(true, commandLine.isAbbreviatedSubcommandsAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isAbbreviatedSubcommandsAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isAbbreviatedSubcommandsAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testAbbrevOptions_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isAbbreviatedOptionsAllowed());
        commandLine.setAbbreviatedOptionsAllowed(true);
        assertEquals(true, commandLine.isAbbreviatedOptionsAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isAbbreviatedOptionsAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isAbbreviatedOptionsAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testAbbrevOptions_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isAbbreviatedOptionsAllowed());
        commandLine.setAbbreviatedOptionsAllowed(true);
        assertEquals(true, commandLine.isAbbreviatedOptionsAllowed());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isAbbreviatedOptionsAllowed());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isAbbreviatedOptionsAllowed());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserTrimQuotes_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isTrimQuotes());
        commandLine.setTrimQuotes(true);
        assertEquals(true, commandLine.isTrimQuotes());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isTrimQuotes());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isTrimQuotes());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserTrimQuotes_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isTrimQuotes());
        commandLine.setTrimQuotes(true);
        assertEquals(true, commandLine.isTrimQuotes());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isTrimQuotes());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isTrimQuotes());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testParserSplitQuotedStrings_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(false, commandLine.isSplitQuotedStrings());
        commandLine.setSplitQuotedStrings(true);
        assertEquals(true, commandLine.isSplitQuotedStrings());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isSplitQuotedStrings());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isSplitQuotedStrings());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testParserSplitQuotedStrings_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(false, commandLine.isSplitQuotedStrings());
        commandLine.setSplitQuotedStrings(true);
        assertEquals(true, commandLine.isSplitQuotedStrings());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isSplitQuotedStrings());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isSplitQuotedStrings());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testParserUnmatchedOptionsArePositionalParams_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
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
        @Command
        class TopLevel {}
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
    public void testParserPosixClustedShortOptions_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
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
        @Command
        class TopLevel {}
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
    public void testSetStopAtUnmatched_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
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
        @Command
        class TopLevel {}
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
    public void testSetStopAtPositional_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
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
        @Command
        class TopLevel {}
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
    public void testSetAtFileCommentChar_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals((Character) '#', commandLine.getAtFileCommentChar());
        commandLine.setAtFileCommentChar(';');
        assertEquals((Character) ';', commandLine.getAtFileCommentChar());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", (Character) '#', sub.getAtFileCommentChar());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", (Character) '#', subsub.getAtFileCommentChar());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetAtFileCommentChar_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals((Character) '#', commandLine.getAtFileCommentChar());
        commandLine.setAtFileCommentChar(';');
        assertEquals((Character) ';', commandLine.getAtFileCommentChar());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals((Character) ';', sub.getAtFileCommentChar());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals((Character) ';', subsub.getAtFileCommentChar());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUseSimplifiedAtFiles_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertFalse(commandLine.isUseSimplifiedAtFiles());
        commandLine.setUseSimplifiedAtFiles(true);
        assertTrue(commandLine.isUseSimplifiedAtFiles());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertFalse("subcommand added afterwards is not impacted", sub.isUseSimplifiedAtFiles());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertFalse("subcommand added afterwards is not impacted", subsub.isUseSimplifiedAtFiles());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUseSimplifiedAtFiles_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertFalse(commandLine.isUseSimplifiedAtFiles());
        commandLine.setUseSimplifiedAtFiles(true);
        assertTrue(commandLine.isUseSimplifiedAtFiles());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertTrue(sub.isUseSimplifiedAtFiles());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertTrue(subsub.isUseSimplifiedAtFiles());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Command(mixinStandardHelpOptions = true) class Top563 {}
    @Command(mixinStandardHelpOptions = true) class Sub563 {}

    // test for https://github.com/remkop/picocli/issues/563
    @Test
    public void testSubcommandWithoutAnnotationName() {
        CommandLine top = new CommandLine(new Top563());
        top.addSubcommand("subname", new Sub563());

        CommandLine sub = top.getSubcommands().get("subname");
        assertEquals("subname", sub.getCommandName());
        assertEquals("subname", sub.getCommandSpec().name());
        assertEquals("<main class> subname", sub.getCommandSpec().qualifiedName());

        String expected = String.format("" +
                "Usage: <main class> subname [-hV]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        sub.usage(System.out);
        assertEquals(expected, systemOutRule.getLog());
    }

    static class MyHelpFactory implements CommandLine.IHelpFactory {
        public CommandLine.Help create(CommandSpec commandSpec, CommandLine.Help.ColorScheme colorScheme) {
            return null;
        }
    }

    @Test
    public void testSetHelpFactory_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals("picocli.CommandLine$DefaultHelpFactory", commandLine.getHelpFactory().getClass().getName());
        commandLine.setHelpFactory(new MyHelpFactory());
        assertEquals("picocli.SubcommandTests$MyHelpFactory", commandLine.getHelpFactory().getClass().getName());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", "picocli.CommandLine$DefaultHelpFactory", sub.getHelpFactory().getClass().getName());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", "picocli.CommandLine$DefaultHelpFactory", subsub.getHelpFactory().getClass().getName());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetHelpFactory_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals("picocli.CommandLine$DefaultHelpFactory", commandLine.getHelpFactory().getClass().getName());
        commandLine.setHelpFactory(new MyHelpFactory());
        assertEquals("picocli.SubcommandTests$MyHelpFactory", commandLine.getHelpFactory().getClass().getName());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", "picocli.SubcommandTests$MyHelpFactory", sub.getHelpFactory().getClass().getName());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", "picocli.SubcommandTests$MyHelpFactory", sub.getHelpFactory().getClass().getName());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetHelpSectionKeys_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());

        final List<String> DEFAULT_LIST = Arrays.asList("headerHeading", "header", "synopsisHeading", "synopsis",
                "descriptionHeading", "description", "parameterListHeading", "atFileParameterList", "parameterList", "optionListHeading",
                "optionList", "endOfOptionsList", "commandListHeading", "commandList", "exitCodeListHeading", "exitCodeList", "footerHeading", "footer");
        assertEquals(DEFAULT_LIST, commandLine.getHelpSectionKeys());

        final List<String> NEW_LIST = Arrays.asList("a", "b", "c");
        commandLine.setHelpSectionKeys(NEW_LIST);
        assertEquals(NEW_LIST, commandLine.getHelpSectionKeys());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", DEFAULT_LIST, sub.getHelpSectionKeys());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", DEFAULT_LIST, subsub.getHelpSectionKeys());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetHelpSectionKeys_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());

        final List<String> DEFAULT_LIST = Arrays.asList("headerHeading", "header", "synopsisHeading", "synopsis",
                "descriptionHeading", "description", "parameterListHeading", "atFileParameterList", "parameterList", "optionListHeading",
                "optionList", "endOfOptionsList", "commandListHeading", "commandList", "exitCodeListHeading", "exitCodeList", "footerHeading", "footer");
        assertEquals(DEFAULT_LIST, commandLine.getHelpSectionKeys());

        final List<String> NEW_LIST = Arrays.asList("a", "b", "c");
        commandLine.setHelpSectionKeys(NEW_LIST);
        assertEquals(NEW_LIST, commandLine.getHelpSectionKeys());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", NEW_LIST, sub.getHelpSectionKeys());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", NEW_LIST, sub.getHelpSectionKeys());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetHelpSectionMap_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());

        final Set<String> DEFAULT_KEYS = new HashSet<String>(Arrays.asList("headerHeading", "header", "synopsisHeading", "synopsis", "endOfOptionsList",
                "descriptionHeading", "description", "parameterListHeading", "atFileParameterList", "parameterList", "optionListHeading",
                "optionList", "commandListHeading", "commandList", "exitCodeListHeading", "exitCodeList", "footerHeading", "footer"));
        assertEquals(DEFAULT_KEYS, commandLine.getHelpSectionMap().keySet());

        Map<String, CommandLine.IHelpSectionRenderer> NEW_MAP = new HashMap<String, CommandLine.IHelpSectionRenderer>();
        NEW_MAP.put("a", null);
        NEW_MAP.put("b", null);
        NEW_MAP.put("c", null);
        commandLine.setHelpSectionMap(NEW_MAP);
        assertEquals(NEW_MAP.keySet(), commandLine.getHelpSectionMap().keySet());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", DEFAULT_KEYS, sub.getHelpSectionMap().keySet());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", DEFAULT_KEYS, subsub.getHelpSectionMap().keySet());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetHelpSectionMap_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());

        final Set<String> DEFAULT_KEYS = new HashSet<String>(Arrays.asList("headerHeading", "header", "synopsisHeading", "synopsis", "endOfOptionsList",
                "descriptionHeading", "description", "parameterListHeading", "atFileParameterList", "parameterList", "optionListHeading",
                "optionList", "commandListHeading", "commandList", "exitCodeListHeading", "exitCodeList", "footerHeading", "footer"));
        assertEquals(DEFAULT_KEYS, commandLine.getHelpSectionMap().keySet());

        Map<String, CommandLine.IHelpSectionRenderer> NEW_MAP = new HashMap<String, CommandLine.IHelpSectionRenderer>();
        NEW_MAP.put("a", null);
        NEW_MAP.put("b", null);
        NEW_MAP.put("c", null);
        commandLine.setHelpSectionMap(NEW_MAP);
        assertEquals(NEW_MAP.keySet(), commandLine.getHelpSectionMap().keySet());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", NEW_MAP.keySet(), sub.getHelpSectionMap().keySet());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", NEW_MAP.keySet(), sub.getHelpSectionMap().keySet());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    // https://github.com/remkop/picocli/issues/625
    static class MandatorySubcommand625 {
        @Command(name = "top", subcommands = Sub.class, synopsisSubcommandLabel = "COMMAND")
        static class Top implements Runnable {
            @Spec
            CommandSpec spec;
            public void run() {
                throw new ParameterException(spec.commandLine(), "Missing required subcommand");
            }
        }
        @Command(name = "sub")
        static class Sub implements Runnable {
            public void run() {
            }
        }
    }
    @Test
    public void testMandatorySubcommand625() {
        int exitCode = new CommandLine(new MandatorySubcommand625.Top()).execute();
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);

        String expected = String.format("" +
                "Missing required subcommand%n" +
                "Usage: top COMMAND%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expected, systemErrRule.getLog());
    }
    static class CustomNegatableOptionTransformer implements CommandLine.INegatableOptionTransformer {
        public String makeNegative(String optionName, CommandSpec cmd) { return null; }
        public String makeSynopsis(String optionName, CommandSpec cmd) { return null;}
    }
    @Test
    public void testSetNegatableOptionTransformer_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertEquals(CommandLine.RegexTransformer.class, commandLine.getNegatableOptionTransformer().getClass());
        CustomNegatableOptionTransformer newValue = new CustomNegatableOptionTransformer();
        commandLine.setNegatableOptionTransformer(newValue);
        assertEquals(newValue, commandLine.getNegatableOptionTransformer());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", CommandLine.RegexTransformer.class, sub.getNegatableOptionTransformer().getClass());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", CommandLine.RegexTransformer.class, subsub.getNegatableOptionTransformer().getClass());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetNegatableOptionTransformer_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertEquals(CommandLine.RegexTransformer.class, commandLine.getNegatableOptionTransformer().getClass());
        CustomNegatableOptionTransformer newValue = new CustomNegatableOptionTransformer();
        commandLine.setNegatableOptionTransformer(newValue);
        assertEquals(newValue, commandLine.getNegatableOptionTransformer());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", newValue, sub.getNegatableOptionTransformer());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", newValue, sub.getNegatableOptionTransformer());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }
    @Test
    public void testSetUnmatchedOptionsAllowedAsOptionParameters_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertTrue(commandLine.isUnmatchedOptionsAllowedAsOptionParameters());
        commandLine.setUnmatchedOptionsAllowedAsOptionParameters(false);
        assertFalse(commandLine.isUnmatchedOptionsAllowedAsOptionParameters());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", true, sub.isUnmatchedOptionsAllowedAsOptionParameters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", true, subsub.isUnmatchedOptionsAllowedAsOptionParameters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetUnmatchedOptionsAllowedAsOptionParameters_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertTrue(commandLine.isUnmatchedOptionsAllowedAsOptionParameters());
        commandLine.setUnmatchedOptionsAllowedAsOptionParameters(false);
        assertFalse(commandLine.isUnmatchedOptionsAllowedAsOptionParameters());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", false, sub.isUnmatchedOptionsAllowedAsOptionParameters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", false, sub.isUnmatchedOptionsAllowedAsOptionParameters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }
    @Test
    public void testSetAllowOptionsAsOptionParameters_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertFalse(commandLine.isAllowOptionsAsOptionParameters());
        commandLine.setAllowOptionsAsOptionParameters(true);
        assertTrue(commandLine.isAllowOptionsAsOptionParameters());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isAllowOptionsAsOptionParameters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isAllowOptionsAsOptionParameters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetAllowOptionsAsOptionParameters_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertFalse(commandLine.isAllowOptionsAsOptionParameters());
        commandLine.setAllowOptionsAsOptionParameters(true);
        assertTrue(commandLine.isAllowOptionsAsOptionParameters());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isAllowOptionsAsOptionParameters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isAllowOptionsAsOptionParameters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }
    @Test
    public void testSetAllowSubcommandsAsOptionParameters_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        assertFalse(commandLine.isAllowSubcommandsAsOptionParameters());
        commandLine.setAllowSubcommandsAsOptionParameters(true);
        assertTrue(commandLine.isAllowSubcommandsAsOptionParameters());

        int childCount = 0;
        int grandChildCount = 0;
        commandLine.addSubcommand("main", createNestedCommand());
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added afterwards is not impacted", false, sub.isAllowSubcommandsAsOptionParameters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subcommand added afterwards is not impacted", false, subsub.isAllowSubcommandsAsOptionParameters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testSetAllowSubcommandsAsOptionParameters_AfterSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.addSubcommand("main", createNestedCommand());
        assertFalse(commandLine.isAllowSubcommandsAsOptionParameters());
        commandLine.setAllowSubcommandsAsOptionParameters(true);
        assertTrue(commandLine.isAllowSubcommandsAsOptionParameters());

        int childCount = 0;
        int grandChildCount = 0;
        for (CommandLine sub : commandLine.getSubcommands().values()) {
            childCount++;
            assertEquals("subcommand added before IS impacted", true, sub.isAllowSubcommandsAsOptionParameters());
            for (CommandLine subsub : sub.getSubcommands().values()) {
                grandChildCount++;
                assertEquals("subsubcommand added before IS impacted", true, sub.isAllowSubcommandsAsOptionParameters());
            }
        }
        assertTrue(childCount > 0);
        assertTrue(grandChildCount > 0);
    }

    @Test
    public void testCommandSpecRoot() {
        CommandLine command = createNestedCommand();
        CommandLine sub = command.getSubcommands().get("cmd2").getSubcommands().get("sub22").getSubcommands().get("sub22sub1");
        assertSame(command.getCommandSpec(), sub.getCommandSpec().root());
        assertNotSame(sub.getCommandSpec(), sub.getCommandSpec().root());
        assertSame(sub.getCommandSpec().parent().parent().parent(), sub.getCommandSpec().root());
    }

    @Test
    public void testSubcommandsNotInstantiated() {
        @Command(subcommands = Foo.class)
        class App { }

        CommandLine cmd = new CommandLine(new App());
        StringWriter sw = new StringWriter();
        cmd.usage(new PrintWriter(sw)); // no exception
    }

    @Command(name = "foo", description = "I am ${COMMAND-FULL-NAME}. I don't do much.")
    static class Foo {
        public Foo() {
            throw new IllegalStateException("Don't instantiate me!");
        }

    }

    @Command(name = "playpico",
            description = "play picocli", mixinStandardHelpOptions = true,
            subcommands = Foo.class )
    static class Launcher implements Runnable {
        @Spec CommandSpec spec;
        @Option(names = "-x", defaultValue = "123", description = "X; default=${DEFAULT-VALUE}")
        int x;

        public static void main(String[] args) {
            new CommandLine(new Launcher())
                    .execute(args);
        }
        public void run() {
            spec.commandLine().usage(spec.commandLine().getOut());
        }
    }

    @Test
    public void testPostponeInstantiation_Issue690() {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        new CommandLine(new Launcher())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setUsageHelpLongOptionsMaxWidth(40)
                .setUsageHelpAutoWidth(true)
                .setErr(new PrintWriter(err))
                .setOut(new PrintWriter(out))
                .execute();
        assertEquals("", err.toString());

        assertEquals(String.format("" +
                "Usage: playpico [-hV] [-x=<x>] [COMMAND]%n" +
                "play picocli%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x=<x>          X; default=123%n" +
                "Commands:%n" +
                "  foo  I am playpico foo. I don't do much.%n"), out.toString());
    }

    @Command(name = "top", subcommands = Sub990.class)
    static class Top990 {
        @Option(names = "-a") int a = -11;
    }
    @Command(name = "sub", subcommands = SubSub990.class)
    static class Sub990 {
        @Option(names = "-b") int b = -22;
    }
    @Command(name = "subsub")
    static class SubSub990 {
        @Option(names = "-c") int c = -33;
    }

    //@Ignore("Needs fix for https://github.com/remkop/picocli/issues/990")
    @Test // https://github.com/remkop/picocli/issues/990
    public void testIssue990_OptionsInSubcommandsNotResetToTheirInitialValue() {
        CommandLine cmd = new CommandLine(new Top990());
        ParseResult result1 = cmd.parseArgs("-a=1 sub -b=2 subsub -c=3".split(" "));
        assertEquals(Integer.valueOf(1), result1.matchedOptionValue("-a", 0));
        assertEquals(Integer.valueOf(2), result1.subcommand().matchedOptionValue("-b", 0));
        assertEquals(Integer.valueOf(3), result1.subcommand().subcommand().matchedOptionValue("-c", 0));
        assertEquals(1, ((Top990)result1.commandSpec().userObject()).a);
        assertEquals(2, ((Sub990)result1.subcommand().commandSpec().userObject()).b);
        assertEquals(3, ((SubSub990)result1.subcommand().subcommand().commandSpec().userObject()).c);

        // now check that option values are reset to their default on the 2nd invocation
        ParseResult result2 = cmd.parseArgs("sub subsub".split(" "));
        assertEquals(-11, ((Top990)result2.commandSpec().userObject()).a);
        assertEquals(-22, ((Sub990)result2.subcommand().commandSpec().userObject()).b);
        assertEquals(-33, ((SubSub990)result2.subcommand().subcommand().commandSpec().userObject()).c);
    }

    @Test // https://github.com/remkop/picocli/issues/1083
    public void testIssue1083SubcommandMethodsWithoutCommandAnnotationOnEnclosingClass() {
        class App {
            @Option(names = "-a", scope = INHERIT) boolean a;
            @Command void sub() {}
        }
        CommandLine sub = new CommandLine(new App()).getSubcommands().get("sub");
        assertNotNull(sub);
        assertNotNull(sub.getCommandSpec().findOption("-a"));
        assertTrue(sub.getCommandSpec().findOption("-a").inherited());
    }

    @Command(name="root", subcommands = {Sub.class})
    static class InhRoot {
        @Parameters(defaultValue = "", scope = INHERIT)
        public String parameter = "param";

        @Option(names="--inh", defaultValue = "def", scope = INHERIT)
        String inh;
    }

    @Command(name="sub")
    static class Sub {
        @ParentCommand
        InhRoot parent;

        @Option(names="--opt")
        public String opt = "opt";
    }

    @Test
    public void testInheritedParameterDefaultValueOverride() {
        CommandLine cli = new CommandLine(new InhRoot());
        // we first parse with the parameter at the beginning
        ParseResult parseResult = cli.parseArgs("parameter_val", "sub", "--opt", "something");
        Sub parsedSub = (Sub) parseResult.commandSpec().subcommands().get("sub").getCommandSpec().userObject();
        assertEquals("parameter_val", parsedSub.parent.parameter);

        // we finally parse with the parameter at the end
        parseResult = cli.parseArgs("sub", "--opt", "something", "parameter_val");
        parsedSub = (Sub) parseResult.commandSpec().subcommands().get("sub").getCommandSpec().userObject();
        assertEquals("parameter_val", parsedSub.parent.parameter);
    }

    @Test
    public void testInheritedOptionDefaultValueOverride() {
        CommandLine cli = new CommandLine(new InhRoot());
        // we first parse with the parameter at the beginning
        ParseResult parseResult = cli.parseArgs("--inh", "first_opt", "sub", "--opt", "something");
        Sub parsedSub = (Sub) parseResult.commandSpec().subcommands().get("sub").getCommandSpec().userObject();
        assertEquals("first_opt", parsedSub.parent.inh);

        // we finally parse with the parameter at the end
        parseResult = cli.parseArgs("sub", "--opt", "something", "--inh", "first_opt");
        parsedSub = (Sub) parseResult.commandSpec().subcommands().get("sub").getCommandSpec().userObject();
        assertEquals("first_opt", parsedSub.parent.inh);
    }

    @Test
    public void testInheritedOptionRoot() {
        @Command(subcommands = App.InhSub.class)
        class App {
            @Option(names = "-a", scope = INHERIT) boolean a;
            @Option(names = "-n") boolean n;
            @Command(name = "sub")
            class InhSub {
                @Option(names="-b", scope=INHERIT) boolean b;
                @Command void subsub() {}
            }
        }
        CommandLine root = new CommandLine(new App());
        OptionSpec rootAOption = root.getCommandSpec().findOption("-a");
        assertFalse("An inheritable option was marked as inherited at the root! (should only be marked as inherited for children)",
                rootAOption.inherited());
        assertEquals("An inheritable option's root was not itself!", rootAOption, rootAOption.root());
        OptionSpec rootNOption = root.getCommandSpec().findOption("-n");
        assertFalse("A non-inheritable option was marked as inherited!", rootNOption.inherited());
        assertNull("A non-inheritable option had a non-null root!", rootNOption.root());

        CommandLine sub = root.getSubcommands().get("sub");
        OptionSpec subAOption = sub.getCommandSpec().findOption("-a");
        assertTrue("An inherited option was not marked as such!", subAOption.inherited());
        assertEquals("The root of the inherited option was not the real root option", rootAOption, subAOption.root());
        OptionSpec subBOption = sub.getCommandSpec().findOption("-b");
        assertFalse("An inheritable option rooted in a subcommand was marked wrongly as inherited!", subBOption.inherited());
        assertEquals("An inheritable option rooted in a subcommand's root was not itself!", subBOption, subBOption.root());
        OptionSpec subNOption = sub.getCommandSpec().findOption("-n");
        assertNull("A non-inheritable option was inherited by a subcommand!", subNOption);

        CommandLine subsub = sub.getSubcommands().get("subsub");
        OptionSpec subsubAOption = subsub.getCommandSpec().findOption("-a");
        assertTrue("An inherited option was not marked as such!", subsubAOption.inherited());
        assertEquals("The root of the inherited option was not the real root option", rootAOption, subsubAOption.root());
        OptionSpec subsubBOption = subsub.getCommandSpec().findOption("-b");
        assertTrue("An inherited option was not marked as such!", subsubBOption.inherited());
        assertEquals("An inheritable option rooted in a subcommand's root was not itself!", subBOption, subsubBOption.root());
    }

    @Test
    public void testInheritedParameterRoot() {
        @Command(subcommands = App.InhSub.class)
        class App {
            @Parameters(scope = INHERIT) boolean param;
            @Command(name = "sub")
            class InhSub {
                @Parameters(scope=INHERIT) String subParam;
                @Parameters String nonInherited;
                @Command void subsub() {}
            }
        }
        CommandLine root = new CommandLine(new App());
        List<PositionalParamSpec> rootParams = root.getCommandSpec().positionalParameters();
        assertEquals(1, rootParams.size());
        PositionalParamSpec rootParam = rootParams.get(0);
        assertFalse("An inheritable parameter was marked as inherited at the root! (should only be marked as inherited for children)",
                rootParam.inherited());
        assertEquals("An inheritable option's root was not itself!", rootParam, rootParam.root());

        CommandLine sub = root.getSubcommands().get("sub");
        List<PositionalParamSpec> subParams = sub.getCommandSpec().positionalParameters();
        assertEquals(3, subParams.size());
        // inherited parameters are appended to the list of parameters
        PositionalParamSpec subParam = subParams.get(2);
        assertTrue("An inherited parameter was not marked as such!", subParam.inherited());
        assertEquals("The root of the inherited parameter was not the real root parameter", rootParam, subParam.root());
        PositionalParamSpec subParamRooted = subParams.get(0);
        assertFalse("An inheritable parameter rooted in a subcommand was marked wrongly as inherited!", subParamRooted.inherited());
        assertEquals("An inheritable parameter rooted in a subcommand's root was not itself!", subParamRooted, subParamRooted.root());
        PositionalParamSpec subNonInherited = subParams.get(1);
        assertFalse("A non-inheritable parameter was marked as inherited!", subNonInherited.inherited());
        assertNull("A non-inheritable parameter had a non-null root!", subNonInherited.root());

        CommandLine subsub = sub.getSubcommands().get("subsub");
        List<PositionalParamSpec> subsubParams = subsub.getCommandSpec().positionalParameters();
        assertEquals(2, subsubParams.size());
        // positional parameters are appended going up the inheritance chain
        // obviously, real use cases should not do this, but this helps test the robustness of the .root() method inheritance
        PositionalParamSpec subsubParamRoot = subsubParams.get(1);
        assertTrue("An inherited parameter was not marked as such!", subsubParamRoot.inherited());
        assertEquals("The root of the inherited parameter was not the real root parameter", rootParam, subsubParamRoot.root());
        PositionalParamSpec subsubParamFromSub = subsubParams.get(0);
        assertTrue("An inherited parameter was not marked as such!", subsubParamFromSub.inherited());
        assertEquals("An inheritable parameter rooted in a subcommand's root was not itself!", subParamRooted, subsubParamFromSub.root());
    }

    @Test
    public void testIssue1183_HelpWithSubcommandWithRequiredOptions() {
        @Command(name = "app", mixinStandardHelpOptions = true)
        class App implements Runnable {
            public void run() { throw new IllegalStateException("app"); }

            @Command int sub(@Option(names = "-x", required = true) int x) {
                throw new IllegalStateException("sub");
            }
        }
        int exitCode = new CommandLine(new App()).execute("-h", "sub");
        assertEquals(0, exitCode);
        assertEquals("", systemErrRule.getLog());

        String expected = String.format("" +
                "Usage: app [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expected, systemOutRule.getLog());
    }
    static class MyDefaultValueProvider implements CommandLine.IDefaultValueProvider {
        public String defaultValue(CommandLine.Model.ArgSpec argSpec) throws Exception {
            return null;
        }
    }
    static class MyVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() throws Exception {
            return new String[0];
        }
    }
    @Test
    public void testCommandScopeInherit() {
        @Command(name = "app", scope = INHERIT,
                mixinStandardHelpOptions = true,
                subcommands = InhRoot.class,
                description = "appdescription",
                descriptionHeading = "appDescriptionHeading",
                version = "appversion",
                aliases = {"a", "b", "c"},
                separator = ":", subcommandsRepeatable = true, helpCommand = true,
                headerHeading = "appHeaderHeading", header = "appheader",
                synopsisHeading = "appSynopsisHeading", customSynopsis = "appCustomSynopsis",
                abbreviateSynopsis = true, synopsisSubcommandLabel = "appSynopsisSubcommandLabel",
                optionListHeading = "appOptionListHeading", parameterListHeading = "appParameterListHeading",
                commandListHeading = "appCommandListHeading",
                footerHeading = "appFooterHeading", footer = "appFooter",
                hidden = true,
                exitCodeList = "123:appExitCodeList", exitCodeListHeading = "appExitCodeListHeading",
                exitCodeOnExecutionException = 333,
                exitCodeOnInvalidInput = 444,
                exitCodeOnSuccess = 555,
                exitCodeOnUsageHelp = 666,
                exitCodeOnVersionHelp = 777,
                requiredOptionMarker = '%',
                showAtFileInUsageHelp = true,
                showDefaultValues = true,
                showEndOfOptionsDelimiterInUsageHelp = true,
                sortOptions = false,
                usageHelpAutoWidth = true,
                usageHelpWidth = 88,
                defaultValueProvider = MyDefaultValueProvider.class,
                versionProvider = MyVersionProvider.class
        )
        class App implements Runnable {
            @Option(names = "-x") int x;
            public void run() { }
            @Command int sub(@Option(names = "-y") int y) {
                throw new IllegalStateException("sub");
            }
        }
        CommandLine app = new CommandLine(new App());
        CommandSpec spec = app.getCommandSpec();
        verifyInheritedAttributes(spec, true);

        verifyInheritedAttributes(spec.subcommands().get("sub").getCommandSpec(), false);
        assertEquals("sub", spec.subcommands().get("sub").getCommandSpec().name());

        CommandSpec inhRoot = spec.subcommands().get("root").getCommandSpec();
        assertEquals("root", inhRoot.name());
        verifyInheritedAttributes(inhRoot, false);
        verifyInheritedAttributes(inhRoot.subcommands().get("sub").getCommandSpec(), false);
        assertEquals("sub", inhRoot.subcommands().get("sub").getCommandSpec().name());
    }

    protected void verifyInheritedAttributes(CommandSpec spec, boolean aliasesMustBeEqual) {
        assertTrue(spec.mixinStandardHelpOptions());
        assertNotNull(spec.findOption("--help"));
        assertNotNull(spec.findOption("--version"));
        assertTrue(spec.versionProvider() instanceof MyVersionProvider);
        assertTrue(spec.defaultValueProvider() instanceof MyDefaultValueProvider);
        if (aliasesMustBeEqual) {
            assertArrayEquals(new String[]{"a", "b", "c"}, spec.aliases());
        } else {
            assertArrayEquals(new String[]{}, spec.aliases());
        }
        assertEquals(":", spec.parser().separator());
        assertEquals(true, spec.subcommandsRepeatable());
        assertEquals(true, spec.helpCommand());
        assertEquals(333, spec.exitCodeOnExecutionException());
        assertEquals(444, spec.exitCodeOnInvalidInput());
        assertEquals(555, spec.exitCodeOnSuccess());
        assertEquals(666, spec.exitCodeOnUsageHelp());
        assertEquals(777, spec.exitCodeOnVersionHelp());

        assertEquals("appdescription", spec.usageMessage().description()[0]);
        assertEquals("appDescriptionHeading", spec.usageMessage().descriptionHeading());
        assertEquals("appheader", spec.usageMessage().header()[0]);
        assertEquals("appHeaderHeading", spec.usageMessage().headerHeading());
        assertEquals("appFooter", spec.usageMessage().footer()[0]);
        assertEquals("appFooterHeading", spec.usageMessage().footerHeading());
        assertEquals("appOptionListHeading", spec.usageMessage().optionListHeading());
        assertEquals("appParameterListHeading", spec.usageMessage().parameterListHeading());
        assertEquals("appCommandListHeading", spec.usageMessage().commandListHeading());
        assertEquals("appSynopsisHeading", spec.usageMessage().synopsisHeading());
        assertEquals("appCustomSynopsis", spec.usageMessage().customSynopsis()[0]);
        assertEquals("appSynopsisSubcommandLabel", spec.usageMessage().synopsisSubcommandLabel());
        assertEquals(true, spec.usageMessage().abbreviateSynopsis());
        Map<String, String> exitCodeList = TestUtil.mapOf("123", "appExitCodeList");
        assertEquals(exitCodeList, spec.usageMessage().exitCodeList());
        assertEquals("appExitCodeListHeading", spec.usageMessage().exitCodeListHeading());

        assertEquals('%', spec.usageMessage().requiredOptionMarker());
        assertEquals(true, spec.usageMessage().showDefaultValues());
        assertEquals(false, spec.usageMessage().sortOptions());
        assertEquals(true, spec.usageMessage().autoWidth());
        assertEquals(88, spec.usageMessage().width());
        assertEquals(true, spec.usageMessage().showAtFileInUsageHelp());
        assertEquals(true, spec.usageMessage().showEndOfOptionsDelimiterInUsageHelp());
        assertEquals(true, spec.usageMessage().hidden());
    }

    @Test
    public void testCommandScopeInheritVersion() {
        @Command(name = "app", scope = INHERIT,
                subcommands = InhRoot.class,
                version = "appversion")
        class App {
            @Command void sub() { }
        }
        CommandLine app = new CommandLine(new App());
        CommandSpec spec = app.getCommandSpec();
        assertEquals("appversion", spec.version()[0]);

        CommandSpec sub = spec.subcommands().get("sub").getCommandSpec();
        assertEquals("sub", sub.name());
        assertEquals("appversion", sub.version()[0]);

        CommandSpec root = spec.subcommands().get("root").getCommandSpec();
        assertEquals("root", root.name());
        assertEquals("appversion", root.version()[0]);

        CommandSpec subsub = root.subcommands().get("sub").getCommandSpec();
        assertEquals("sub", subsub.name());
        assertEquals("appversion", subsub.version()[0]);
    }

    @Test
    public void testInheritedHelp() {
        @Command(name = "app", scope = INHERIT,
                mixinStandardHelpOptions = true, version = "app version 1.0",
                header = "App header",
                description = "App description",
                footerHeading = "Copyright%n", footer = "(c) Copyright by the authors",
                showAtFileInUsageHelp = true)
        class App implements Runnable {
            @Option(names = "-x")
            int x;

            public void run() {
                System.out.printf("Hello from app %d%n!", x);
            }

            @Command(header = "Subcommand header", description = "Subcommand description")
            void sub(@Option(names = "-y") int y) {
                System.out.printf("Hello app sub %d%n!", y);
            }
        }
        String expected = String.format("" +
                "Subcommand header%n" +
                "Usage: app sub [-hV] [-y=<arg0>] [@<filename>...]%n" +
                "Subcommand description%n" +
                "      [@<filename>...]   One or more argument files containing options.%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n" +
                "  -y=<arg0>%n" +
                "Copyright%n" +
                "(c) Copyright by the authors%n");

        String usageMessage = new CommandLine(new App()).getSubcommands().get("sub").getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, usageMessage);

        new CommandLine(new App()).execute("sub", "--help");
        assertEquals(expected, systemOutRule.getLog());

        StringWriter sw2 = new StringWriter();
        new CommandLine(new App()).getSubcommands().get("sub").printVersionHelp(new PrintWriter(sw2));
        String expected2 = String.format("app version 1.0%n");
        assertEquals(expected2, sw2.toString());
    }
}
