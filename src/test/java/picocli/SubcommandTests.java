package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.MissingTypeConverterException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.RunAll;
import picocli.CommandLine.UnmatchedArgumentException;

import java.util.*;

import static org.junit.Assert.*;
import static picocli.HelpTestUtil.setTraceLevel;
import static picocli.HelpTestUtil.textArray;
import static picocli.PicocliTestUtil.setOf;

public class SubcommandTests {
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

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
        String[] args = {"t"};
        List<Object> result = cmd.parseWithHandler(new RunAll(), args);
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
        @Command
        class Top {}
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
        assertEquals("picocli.SubcommandTests$1ABCD", cmdLine.getSubcommands().get("sub1").getCommand().getClass().getName());
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
        commandLine.parse("cmd1", "sub12", "-e", "TXT");
    }

    @Test(expected = MissingTypeConverterException.class)
    public void testCustomTypeConverterRegisteredBeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
        CommandLine commandLine = new CommandLine(new TopLevel());
        commandLine.registerConverter(CustomType.class, new CustomType(null));

        commandLine.addSubcommand("main", createNestedCommand());
        commandLine.parse("main", "cmd1", "sub12", "-e", "TXT");
    }

    @Test
    public void testCustomTypeConverterRegisteredAfterSubcommandsAdded() {
        @Command
        class TopLevel { public boolean equals(Object o) {return getClass().equals(o.getClass());}}
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
    public void testParserToggleBooleanFlags_BeforeSubcommandsAdded() {
        @Command
        class TopLevel {}
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
        @Command
        class TopLevel {}
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
                "descriptionHeading", "description", "parameterListHeading", "parameterList", "optionListHeading",
                "optionList", "commandListHeading", "commandList", "footerHeading", "footer");
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
                "descriptionHeading", "description", "parameterListHeading", "parameterList", "optionListHeading",
                "optionList", "commandListHeading", "commandList", "footerHeading", "footer");
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

        final Set<String> DEFAULT_KEYS = new HashSet<String>(Arrays.asList("headerHeading", "header", "synopsisHeading", "synopsis",
                "descriptionHeading", "description", "parameterListHeading", "parameterList", "optionListHeading",
                "optionList", "commandListHeading", "commandList", "footerHeading", "footer"));
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

        final Set<String> DEFAULT_KEYS = new HashSet<String>(Arrays.asList("headerHeading", "header", "synopsisHeading", "synopsis",
                "descriptionHeading", "description", "parameterListHeading", "parameterList", "optionListHeading",
                "optionList", "commandListHeading", "commandList", "footerHeading", "footer"));
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

}
