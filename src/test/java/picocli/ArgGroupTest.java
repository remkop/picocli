package picocli;

import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.MutuallyExclusiveArgsException;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ArgGroupTest {
    static OptionSpec OPTION = OptionSpec.builder("-x").groupNames("AAA", "BBB", "A", "B").build();

    @Test
    public void testArgSpecHaveNoGroupsByDefault() {
        assertTrue(OptionSpec.builder("-x").build().groupNames().isEmpty());
        assertTrue(PositionalParamSpec.builder().build().groupNames().isEmpty());
    }

    @Test
    public void testArgSpecBuilderHasNoGroupsByDefault() {
        assertTrue(OptionSpec.builder("-x").groupNames().isEmpty());
        assertTrue(PositionalParamSpec.builder().groupNames().isEmpty());
    }

    @Test
    public void testOptionSpecBuilderGroupNamesMutable() {
        OptionSpec.Builder builder = OptionSpec.builder("-x");
        assertTrue(builder.groupNames().isEmpty());

        builder.groupNames("AAA").build();
        assertEquals(1, builder.groupNames().size());
        assertEquals("AAA", builder.groupNames().get(0));
    }

    @Test
    public void testPositionalParamSpecBuilderGroupNamesMutable() {
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
        assertTrue(builder.groupNames().isEmpty());

        builder.groupNames("AAA").build();
        assertEquals(1, builder.groupNames().size());
        assertEquals("AAA", builder.groupNames().get(0));
    }

    @Test
    public void testGroupSpec_builderFactoryMethodSetsName() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("AAA");
        builder.addArg(OPTION);

        assertEquals("AAA", builder.name());
        assertEquals("AAA", builder.build().name());
    }

    @Test
    public void testGroupSpecBuilderNameMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("AAA");
        assertEquals("AAA", builder.name());
        builder.name("BBB");
        assertEquals("BBB", builder.name());

        builder.addArg(OPTION);
        assertEquals("BBB", builder.build().name());
    }

    @Test
    public void testGroupSpecBuilderNullNameDisallowed() {
        try {
            ArgGroupSpec.builder((String) null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }

        ArgGroupSpec.Builder builder = ArgGroupSpec.builder(""); // TODO empty name ok?
        try {
            builder.name(null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }
    }

    @Test
    public void testGroupSpecBuilderNullAnnotationDisallowed() {
        try {
            ArgGroupSpec.builder((ArgGroup) null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }
    }


    @Test
    public void testGroupSpecBuilderFromAnnotationFailsIfNoOptionsOrSubgroups() {
        @Command(argGroups =
        @ArgGroup(name = "abc",
                exclusive = false, validate = false, required = true,
                headingKey = "headingKeyXXX", heading = "headingXXX", order = 123))
        class App {
        }
        Command command = App.class.getAnnotation(Command.class);
        ArgGroup annotation = command.argGroups()[0];
        try {
            ArgGroupSpec.builder(annotation).build();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("ArgGroup 'abc' has no options or positional parameters, and no subgroups", ex.getMessage());
        }
    }

    @Test
    public void testGroupSpecBuilderFromAnnotation() {
        @Command(argGroups =
        @ArgGroup(name = "abc",
                exclusive = false, validate = false, required = true,
                headingKey = "headingKeyXXX", heading = "headingXXX", order = 123))
        class App {
            @Option(names = "-x", groups = "abc")
            int x;
        }

        CommandLine commandLine = new CommandLine(new App());
        assertEquals(1, commandLine.getCommandSpec().argGroups().size());
        ArgGroupSpec group = commandLine.getCommandSpec().argGroups().get("abc");
        assertNotNull(group);

        assertEquals("abc", group.name());
        assertEquals(false, group.exclusive());
        assertEquals(false, group.validate());
        assertEquals(true, group.required());
        assertEquals("headingKeyXXX", group.headingKey());
        assertEquals("headingXXX", group.heading());
        assertEquals(123, group.order());

        assertTrue(group.subgroups().isEmpty());
        assertEquals(1, group.args().size());
        OptionSpec option = (OptionSpec) group.args().iterator().next();
        assertEquals("-x", option.shortestName());
        assertEquals(Arrays.asList("abc"), option.groupNames());
        assertEquals(1, option.groupNames().size());
        assertEquals(Arrays.asList("abc"), option.groupNames());
    }

    @Test
    public void testGroupSpecBuilderExclusiveTrueByDefault() {
        assertTrue(ArgGroupSpec.builder("A").exclusive());
    }

    @Test
    public void testGroupSpecBuilderExclusiveMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertTrue(builder.exclusive());
        builder.exclusive(false);
        assertFalse(builder.exclusive());
    }

    @Test
    public void testGroupSpecBuilderRequiredFalseByDefault() {
        assertFalse(ArgGroupSpec.builder("A").required());
    }

    @Test
    public void testGroupSpecBuilderRequiredMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertFalse(builder.required());
        builder.required(true);
        assertTrue(builder.required());
    }

    @Test
    public void testGroupSpecBuilderValidatesTrueByDefault() {
        assertTrue(ArgGroupSpec.builder("A").validate());
    }

    @Test
    public void testGroupSpecBuilderValidateMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertTrue(builder.validate());
        builder.validate(false);
        assertFalse(builder.validate());
    }

    @Test
    public void testGroupSpecBuilderGroupNamesEmptyByDefault() {
        assertTrue(ArgGroupSpec.builder("A").subgroupNames().isEmpty());
    }

    @Test
    public void testGroupSpecBuilderGroupNamesMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertTrue(builder.subgroupNames().isEmpty());
        builder.subgroupNames().add("B");
        builder.subgroupNames().add("C");
        assertEquals(Arrays.asList("B", "C"), builder.subgroupNames());

        builder.subgroupNames(Arrays.asList("X", "Y"));
        assertEquals(Arrays.asList("X", "Y"), builder.subgroupNames());
    }

    // TODO
//    @Test
//    public void testGroupSpecBuilderGroupsEmptyByDefault() {
//        assertTrue(ArgGroupSpec.builder("A").groups().isEmpty());
//    }
//
//    @Test
//    public void testGroupSpecBuilderGroupsMutable() {
//        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
//        assertTrue(builder.groups().isEmpty());
//        builder.groups().add(ArgGroupSpec.builder("B").build());
//        builder.groups().add(ArgGroupSpec.builder("C").build());
//        assertEquals(2, builder.groups().size());
//    }

    @Test
    public void testGroupSpecBuilderOrderMinusOneByDefault() {
        assertEquals(ArgGroupSpec.DEFAULT_ORDER, ArgGroupSpec.builder("A").order());
    }

    @Test
    public void testGroupSpecBuilderOrderMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertEquals(ArgGroupSpec.DEFAULT_ORDER, builder.order());
        builder.order(34);
        assertEquals(34, builder.order());
    }

    @Test
    public void testGroupSpecBuilderHeadingNullByDefault() {
        assertNull(ArgGroupSpec.builder("A").heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertNull(builder.heading());
        builder.heading("This is a header");
        assertEquals("This is a header", builder.heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyNullByDefault() {
        assertNull(ArgGroupSpec.builder("A").headingKey());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        assertNull(builder.headingKey());
        builder.headingKey("KEY");
        assertEquals("KEY", builder.headingKey());
    }

    // TODO
//    @Test
//    public void testGroupSpecBuilderBuildDisallowsDuplidateGroups() {
//        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
//        builder.groups().add(ArgGroupSpec.builder("B").build());
//        builder.groups().add(ArgGroupSpec.builder("B").heading("x").build());
//
//        try {
//            builder.build();
//            fail("Expected exception");
//        } catch (DuplicateNameException ex) {
//            assertEquals("Different ArgGroups should not use the same name 'B'", ex.getMessage());
//        }
//    }

    @Test
    public void testGroupSpecBuilderBuildCopiesBuilderAttributes() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        builder.addArg(OPTION);
        ArgGroupSpec group = builder.build();

        assertEquals("A", group.name());
        assertEquals(builder.name(), group.name());

        assertTrue(group.exclusive());
        assertEquals(builder.exclusive(), group.exclusive());

        assertFalse(group.required());
        assertEquals(builder.required(), group.required());

        assertTrue(group.validate());
        assertEquals(builder.validate(), group.validate());

        assertEquals(ArgGroupSpec.DEFAULT_ORDER, group.order());
        assertEquals(builder.order(), group.order());

        assertNull(group.heading());
        assertEquals(builder.heading(), group.heading());

        assertNull(group.headingKey());
        assertEquals(builder.headingKey(), group.headingKey());

        // TODO
//        assertTrue(group.groups().isEmpty());
//        assertEquals(builder.groupNames(), group.groupNames());

        assertTrue(group.subgroups().isEmpty());
        // TODO assertEquals(builder.groups().isEmpty(), group.groups().isEmpty());
    }

    @Test
    public void testGroupSpecBuilderBuildCopiesBuilderAttributesNonDefault() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.required(true);
        builder.addSubgroup(ArgGroupSpec.builder("B").addArg(OPTION).subgroupNames("A").build());

        builder.addArg(OPTION);
        ArgGroupSpec group = builder.build();

        assertEquals("A", group.name());
        assertEquals(builder.name(), group.name());

        assertFalse(group.exclusive());
        assertEquals(builder.exclusive(), group.exclusive());

        assertTrue(group.required());
        assertEquals(builder.required(), group.required());

        assertFalse(group.validate());
        assertEquals(builder.validate(), group.validate());

        assertEquals(123, group.order());
        assertEquals(builder.order(), group.order());

        assertEquals("my heading", group.heading());
        assertEquals(builder.heading(), group.heading());

        assertEquals("my headingKey", group.headingKey());
        assertEquals(builder.headingKey(), group.headingKey());

        assertEquals(1, group.subgroups().size());
        assertEquals("B", group.subgroups().get("B").name());
        // TODO assertEquals(builder.groups(), new ArrayList<ArgGroupSpec>(group.groups().values()));
    }

    @Test
    public void testGroupSpecToString() {
        String expected = "ArgGroup[A, exclusive=true, required=false, validate=true, order=-1, args=[-x], subgroups=[], headingKey=null, heading=null]";
        assertEquals(expected, ArgGroupSpec.builder("A").addArg(OPTION).build().toString());

        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.required(true);
        builder.addSubgroup(ArgGroupSpec.builder("B").subgroupNames("A").addArg(OPTION).build());
        builder.addArg(PositionalParamSpec.builder().index("0..1").paramLabel("FILE").groupNames("A").build());

        String expected2 = "ArgGroup[A, exclusive=false, required=true, validate=false, order=123, args=[params[0..1]=FILE], subgroups=[B], headingKey='my headingKey', heading='my heading']";
        assertEquals(expected2, builder.build().toString());
    }

    @Test
    public void testGroupSpecEquals() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        builder.addArg(OPTION);
        ArgGroupSpec a = builder.build();
        assertEquals(a, a);
        assertNotSame(a, ArgGroupSpec.builder("A").addArg(OPTION).build());
        assertEquals(a, ArgGroupSpec.builder("A").addArg(OPTION).build());

        OptionSpec otherOption = OptionSpec.builder("-y").groupNames("A").build();
        assertNotEquals(a, ArgGroupSpec.builder("A").addArg(OPTION).addArg(otherOption).build());

        builder.heading("my heading");
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.headingKey("my headingKey");
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.order(123);
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.exclusive(false);
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.validate(false);
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.required(true);
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.addSubgroup(ArgGroupSpec.builder("B").addArg(OPTION).build());
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());
    }

    @Test
    public void testGroupSpecHashCode() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("A");
        builder.addArg(OPTION);
        ArgGroupSpec a = builder.build();
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), ArgGroupSpec.builder("A").addArg(OPTION).build().hashCode());

        OptionSpec otherOption = OptionSpec.builder("-y").build();
        assertNotEquals(a.hashCode(), ArgGroupSpec.builder("A").addArg(OPTION).addArg(otherOption).build().hashCode());

        builder.heading("my heading");
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.headingKey("my headingKey");
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.order(123);
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.exclusive(false);
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.validate(false);
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.required(true);
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.subgroups().add(ArgGroupSpec.builder("B").addArg(OPTION).build());
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());
    }

    @Test
    public void testReflection() {
        @Command(argGroups = @ArgGroup(name = "AAA"))
        class App {
            @Option(names = "-x", groups = "AAA")
            int x;
            @Option(names = "-y", groups = "AAA")
            int y;
        }
        CommandLine cmd = new CommandLine(new App());
        CommandSpec spec = cmd.getCommandSpec();
        Map<String, ArgGroupSpec> groups = spec.argGroups();
        assertEquals(1, groups.size());

        ArgGroupSpec group = groups.get("AAA");
        assertNotNull(group);

        List<ArgSpec> options = new ArrayList<ArgSpec>(group.args());
        assertEquals(2, options.size());
        assertEquals("-x", ((OptionSpec) options.get(0)).shortestName());
        assertEquals("-y", ((OptionSpec) options.get(1)).shortestName());

        assertEquals(1, options.get(0).groupNames().size());
        assertEquals(Arrays.asList("AAA"), options.get(0).groupNames());

        assertEquals(1, options.get(1).groupNames().size());
        assertEquals(Arrays.asList("AAA"), options.get(1).groupNames());
    }

    @Test
    public void testProgrammatic() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-x").build());
        spec.addOption(OptionSpec.builder("-y").build());
        spec.addOption(OptionSpec.builder("-z").build());

        ArgGroupSpec exclusive = ArgGroupSpec.builder("EXCL")
                .addArg(spec.findOption("-x"))
                .addArg(spec.findOption("-y")).build();
        ArgGroupSpec cooccur = ArgGroupSpec.builder("ALL")
                .addArg(spec.findOption("-z"))
                .addSubgroup(exclusive).build();
        spec.addArgGroup(exclusive);
        spec.addArgGroup(cooccur);

        Map<String, ArgGroupSpec> groups = spec.argGroups();
    }

    static final OptionSpec OPTION_A = OptionSpec.builder("-a").build();
    static final OptionSpec OPTION_B = OptionSpec.builder("-b").build();
    static final OptionSpec OPTION_C = OptionSpec.builder("-c").build();

    @Test
    public void testValidationNonRequiredExclusive_ActualTwo() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").addArg(OPTION_A).addArg(OPTION_B).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        try {
            group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_A, OPTION_B));
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -a, -b are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    @Test
    public void testReflectionValidationNonRequiredExclusive_ActualTwo() {
        @Command(argGroups = @CommandLine.ArgGroup(name = "X"))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
        }
        try {
            CommandLine.populateCommand(new App(), "-a", "-b");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -a, -b are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    @Test
    public void testValidationNonRequiredExclusive_ActualZero() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").addArg(OPTION_A).addArg(OPTION_B).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        group.validateConstraints(cmd, Collections.<ArgSpec>emptyList());
    }

    @Test
    public void testReflectionValidationNonRequiredExclusive_ActualZero() {
        @Command(argGroups = @ArgGroup(name = "X"))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
        }
        // no errors
        CommandLine.populateCommand(new App());
    }

    @Test
    public void testValidationRequiredExclusive_ActualZero() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").required(true).addArg(OPTION_A).addArg(OPTION_B).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        try {
            group.validateConstraints(cmd, Collections.<ArgSpec>emptyList());
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument (specify one of these): -a, -b", ex.getMessage());
        }
    }

    @Test
    public void testReflectionValidationRequiredExclusive_ActualZero() {
        @Command(argGroups = @CommandLine.ArgGroup(name = "X", required = true))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
        }
        try {
            CommandLine.populateCommand(new App());
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument (specify one of these): -a, -b", ex.getMessage());
        }
    }

    @Test
    public void testValidationNonRequiredNonExclusive_All() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        // no error
        group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_A, OPTION_B, OPTION_C));
    }

    @Test
    public void testReflectionValidationNonRequiredNonExclusive_All() {
        @Command(argGroups = @ArgGroup(name = "X", exclusive = false))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        CommandLine.populateCommand(new App(), "-a", "-b", "-c");
    }

    @Test
    public void testValidationNonRequiredNonExclusive_Partial() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        try {
            group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_A, OPTION_B));
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -c", ex.getMessage());
        }
    }

    @Test
    public void testReflectionValidationNonRequiredNonExclusive_Partial() {
        @Command(argGroups = @ArgGroup(name = "X", exclusive = false))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        try {
            CommandLine.populateCommand(new App(), "-a", "-b");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -c", ex.getMessage());
        }
    }

    @Test
    public void testValidationNonRequiredNonExclusive_Zero() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        // no error
        group.validateConstraints(cmd, Collections.<ArgSpec>emptyList());
    }

    @Test
    public void testReflectionValidationNonRequiredNonExclusive_Zero() {
        @Command(argGroups = @CommandLine.ArgGroup(name = "X", exclusive = false))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        CommandLine.populateCommand(new App());
    }

    @Test
    public void testValidationRequiredNonExclusive_All() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").required(true).exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        // no error
        group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_A, OPTION_B, OPTION_C));
    }

    @Test
    public void testReflectionValidationRequiredNonExclusive_All() {
        @Command(argGroups = @CommandLine.ArgGroup(name = "X", exclusive = false, required = true))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        CommandLine.populateCommand(new App(), "-a", "-b", "-c");
    }

    @Test
    public void testValidationRequiredNonExclusive_Partial() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").required(true).exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        try {
            group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_B));
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -a, -c", ex.getMessage());
        }
    }

    @Test
    public void testReflectionValidationRequiredNonExclusive_Partial() {
        @Command(argGroups = @ArgGroup(name = "X", exclusive = false, required = true))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        try {
            CommandLine.populateCommand(new App(), "-b");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -a, -c", ex.getMessage());
        }
    }

    @Test
    public void testValidationRequiredNonExclusive_Zero() {
        ArgGroupSpec group = ArgGroupSpec.builder("blah").required(true).exclusive(false)
                .addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        try {
            group.validateConstraints(cmd, Collections.<ArgSpec>emptyList());
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -a, -c, -b", ex.getMessage());
        }
    }

    @Test
    public void testReflectionValidationRequiredNonExclusive_Zero() {
        @Command(argGroups = @ArgGroup(name = "X", exclusive = false, required = true))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        try {
            CommandLine.populateCommand(new App());
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -a, -b, -c", ex.getMessage());
        }
    }

    @Test
    public void testReflectionValidationMultipleGroups() {
        @Command(argGroups = {
                @ArgGroup(name = "ALL", exclusive = false, required = false),
                @ArgGroup(name = "EXCL", exclusive = true, required = true),
                @ArgGroup(name = "COMPOSITE", exclusive = true, required = true,
                        subgroups = {"ALL", "EXCL"}),
        })
        class App {
            @Option(names = "-x", groups = "EXCL") boolean x;
            @Option(names = "-y", groups = "EXCL") boolean y;
            @Option(names = "-a", groups = "ALL") boolean a;
            @Option(names = "-b", groups = "ALL") boolean b;
        }
        try {
            CommandLine.populateCommand(new App(), "-a");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -b", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-b");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -a", ex.getMessage());
        }
        // no error
        CommandLine.populateCommand(new App(), "-a", "-b");
        CommandLine.populateCommand(new App(), "-x");
        CommandLine.populateCommand(new App(), "-y");
        try {
            CommandLine.populateCommand(new App(), "-x", "-y");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -x, -y are mutually exclusive (specify only one)", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-x", "-a");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -b", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-a", "-x", "-b");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: ([-a -b] | (-x | -y)) are mutually exclusive (specify only one)", ex.getMessage());
        }
        try {
            CommandLine.populateCommand(new App(), "-a", "-y", "-b");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: ([-a -b] | (-x | -y)) are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    @Test
    public void testTopologicalSortSimple() {
        ArgGroupSpec.Builder[] all = new ArgGroupSpec.Builder[]{
                ArgGroupSpec.builder("A"),
                ArgGroupSpec.builder("B").subgroupNames("A"),
                ArgGroupSpec.builder("C").subgroupNames("B"),
        };

        List<ArgGroupSpec.Builder> builders = ArgGroupSpec.topologicalSort(Arrays.asList(all));
        assertSame(all[0], builders.get(0));
        assertSame(all[1], builders.get(1));
        assertSame(all[2], builders.get(2));

        List<ArgGroupSpec.Builder> randomized = new ArrayList<ArgGroupSpec.Builder>(Arrays.asList(all));
        Collections.shuffle(randomized);
        builders = ArgGroupSpec.topologicalSort(randomized);
        assertSame(all[0], builders.get(0));
        assertSame(all[1], builders.get(1));
        assertSame(all[2], builders.get(2));
    }

    @Test
    public void testTopologicalSortComplex() {
        ArgGroupSpec.Builder[] all = new ArgGroupSpec.Builder[]{
                ArgGroupSpec.builder("5"),
                ArgGroupSpec.builder("11").subgroupNames("5", "7"),
                ArgGroupSpec.builder("2" ).subgroupNames("11"),
                ArgGroupSpec.builder("8" ).subgroupNames("3", "7"),
                ArgGroupSpec.builder("9" ).subgroupNames("8", "11"),
                ArgGroupSpec.builder("7"),
                ArgGroupSpec.builder("3"),
                ArgGroupSpec.builder("10").subgroupNames("3", "11")
        };
        List<ArgGroupSpec.Builder> builders = ArgGroupSpec.topologicalSort(Arrays.asList(all));
        List<String> names = new ArrayList<String>();
        for (ArgGroupSpec.Builder b: builders) { names.add(b.name()); }
        assertEquals(Arrays.asList("7", "5", "3", "8", "11", "10", "9", "2"), names);
    }

    @Test
    public void testTopologicalSortCyclicalGroupIsIllegal() {
        try {
            ArgGroupSpec.topologicalSort(Arrays.asList(
                    ArgGroupSpec.builder("A").subgroupNames("B"),
                    ArgGroupSpec.builder("B").subgroupNames("C"),
                    ArgGroupSpec.builder("C").subgroupNames("D"),
                    ArgGroupSpec.builder("D").subgroupNames("E", "G"),
                    ArgGroupSpec.builder("E").subgroupNames("F", "A"),
                    ArgGroupSpec.builder("G").subgroupNames("H"),
                    ArgGroupSpec.builder("F"),
                    ArgGroupSpec.builder("H")
            ));
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Cyclic group dependency: ArgGroupSpec.Builder[A, -> [B]] in [A, B, C, D, E, A]", ex.getMessage());
        }
    }

    @Test
    public void testTopologicalSortSimpleCyclicalGroupIsIllegal() {
        try {
            ArgGroupSpec.topologicalSort(Arrays.asList(
                    ArgGroupSpec.builder("X").subgroupNames("X")));
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Cyclic group dependency: ArgGroupSpec.Builder[X, -> [X]] in [X, X]", ex.getMessage());
        }
    }

    @Test
    public void testSynopsisOnlyOptions() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("G")
                .addArg(OptionSpec.builder("-a").build())
                .addArg(OptionSpec.builder("-b").build())
                .addArg(OptionSpec.builder("-c").build());
        assertEquals("[-a | -b | -c]", builder.build().synopsis());

        builder.required(true);
        assertEquals("(-a | -b | -c)", builder.build().synopsis());

        builder.exclusive(false);
        assertEquals("(-a -b -c)", builder.build().synopsis());

        builder.required(false);
        assertEquals("[-a -b -c]", builder.build().synopsis());
    }

    @Test
    public void testSynopsisOnlyPositionals() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("G")
                .addArg(PositionalParamSpec.builder().paramLabel("ARG1").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG2").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG3").build());
        assertEquals("[ARG1 | ARG2 | ARG3]", builder.build().synopsis());

        builder.required(true);
        assertEquals("(ARG1 | ARG2 | ARG3)", builder.build().synopsis());

        builder.exclusive(false);
        assertEquals("(ARG1 ARG2 ARG3)", builder.build().synopsis());

        builder.required(false);
        assertEquals("[ARG1 ARG2 ARG3]", builder.build().synopsis());
    }

    @Test
    public void testSynopsisMixOptionsPositionals() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder("G")
                .addArg(PositionalParamSpec.builder().paramLabel("ARG1").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG2").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG3").build())
                .addArg(OptionSpec.builder("-a").build())
                .addArg(OptionSpec.builder("-b").build())
                .addArg(OptionSpec.builder("-c").build());
        assertEquals("[ARG1 | ARG2 | ARG3 | -a | -b | -c]", builder.build().synopsis());

        builder.required(true);
        assertEquals("(ARG1 | ARG2 | ARG3 | -a | -b | -c)", builder.build().synopsis());

        builder.exclusive(false);
        assertEquals("(ARG1 ARG2 ARG3 -a -b -c)", builder.build().synopsis());

        builder.required(false);
        assertEquals("[ARG1 ARG2 ARG3 -a -b -c]", builder.build().synopsis());
    }

    @Test
    public void testSynopsisOnlyGroups() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder("B1")
                .addArg(OptionSpec.builder("-a").build())
                .addArg(OptionSpec.builder("-b").build())
                .addArg(OptionSpec.builder("-c").build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder("B2")
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-f").build())
                .required(true);

        ArgGroupSpec.Builder b3 = ArgGroupSpec.builder("B3")
                .addArg(OptionSpec.builder("-g").build())
                .addArg(OptionSpec.builder("-h").build())
                .addArg(OptionSpec.builder("-i").build())
                .required(true)
                .exclusive(false);

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder("COMPOSITE")
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addSubgroup(b3.build());

        assertEquals("[[-a | -b | -c] | (-e | -f) | (-g -h -i)]", composite.build().synopsis());

        composite.required(true);
        assertEquals("([-a | -b | -c] | (-e | -f) | (-g -h -i))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("([-a | -b | -c] (-e | -f) (-g -h -i))", composite.build().synopsis());

        composite.required(false);
        assertEquals("[[-a | -b | -c] (-e | -f) (-g -h -i)]", composite.build().synopsis());
    }

    @Test
    public void testSynopsisMixGroupsOptions() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder("B1")
                .addArg(OptionSpec.builder("-a").build())
                .addArg(OptionSpec.builder("-b").build())
                .addArg(OptionSpec.builder("-c").build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder("B2")
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-f").build())
                .required(true);

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder("COMPOSITE")
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addArg(OptionSpec.builder("-x").build())
                .addArg(OptionSpec.builder("-y").build())
                .addArg(OptionSpec.builder("-z").build());

        assertEquals("[-x | -y | -z | [-a | -b | -c] | (-e | -f)]", composite.build().synopsis());

        composite.required(true);
        assertEquals("(-x | -y | -z | [-a | -b | -c] | (-e | -f))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("(-x -y -z [-a | -b | -c] (-e | -f))", composite.build().synopsis());

        composite.required(false);
        assertEquals("[-x -y -z [-a | -b | -c] (-e | -f)]", composite.build().synopsis());
    }

    @Test
    public void testSynopsisMixGroupsPositionals() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder("B1")
                .addArg(OptionSpec.builder("-a").build())
                .addArg(OptionSpec.builder("-b").build())
                .addArg(OptionSpec.builder("-c").build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder("B2")
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-f").build())
                .required(true);

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder("COMPOSITE")
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG1").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG2").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG3").build());

        assertEquals("[ARG1 | ARG2 | ARG3 | [-a | -b | -c] | (-e | -f)]", composite.build().synopsis());

        composite.required(true);
        assertEquals("(ARG1 | ARG2 | ARG3 | [-a | -b | -c] | (-e | -f))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("(ARG1 ARG2 ARG3 [-a | -b | -c] (-e | -f))", composite.build().synopsis());

        composite.required(false);
        assertEquals("[ARG1 ARG2 ARG3 [-a | -b | -c] (-e | -f)]", composite.build().synopsis());
    }

    @Test
    public void testSynopsisMixGroupsOptionsPositionals() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder("B1")
                .addArg(OptionSpec.builder("-a").build())
                .addArg(OptionSpec.builder("-b").build())
                .addArg(OptionSpec.builder("-c").build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder("B2")
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-e").build())
                .addArg(OptionSpec.builder("-f").build())
                .required(true);

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder("COMPOSITE")
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addArg(OptionSpec.builder("-x").build())
                .addArg(OptionSpec.builder("-y").build())
                .addArg(OptionSpec.builder("-z").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG1").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG2").build())
                .addArg(PositionalParamSpec.builder().paramLabel("ARG3").build());

        assertEquals("[-x | -y | -z | ARG1 | ARG2 | ARG3 | [-a | -b | -c] | (-e | -f)]", composite.build().synopsis());

        composite.required(true);
        assertEquals("(-x | -y | -z | ARG1 | ARG2 | ARG3 | [-a | -b | -c] | (-e | -f))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("(-x -y -z ARG1 ARG2 ARG3 [-a | -b | -c] (-e | -f))", composite.build().synopsis());

        composite.required(false);
        assertEquals("[-x -y -z ARG1 ARG2 ARG3 [-a | -b | -c] (-e | -f)]", composite.build().synopsis());
    }

    @Test
    public void testRequiredGroupIgnoreDefaults() {
        // TODO what are the semantics?
        // 1. exclusive required group: one must exist. If none specified, fail, even if all members have a default.
        // 2. co-occurring required group: all must exist. If any missing, fail, even if it has a default.

    }
}