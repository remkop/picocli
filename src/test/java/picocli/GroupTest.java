package picocli;

import org.junit.Test;
import picocli.CommandLine.Group;
import picocli.CommandLine.Command;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.GroupSpec;
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

public class GroupTest {
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
        GroupSpec.Builder builder = GroupSpec.builder("AAA");
        builder.addArg(OPTION);

        assertEquals("AAA", builder.name());
        assertEquals("AAA", builder.build().name());
    }

    @Test
    public void testGroupSpecBuilderNameMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("AAA");
        assertEquals("AAA", builder.name());
        builder.name("BBB");
        assertEquals("BBB", builder.name());

        builder.addArg(OPTION);
        assertEquals("BBB", builder.build().name());
    }

    @Test
    public void testGroupSpecBuilderNullNameDisallowed() {
        try {
            GroupSpec.builder((String) null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }

        GroupSpec.Builder builder = GroupSpec.builder(""); // TODO empty name ok?
        try {
            builder.name(null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }
    }

    @Test
    public void testGroupSpecBuilderNullAnnotationDisallowed() {
        try {
            GroupSpec.builder((CommandLine.Group) null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }
    }


    @Test
    public void testGroupSpecBuilderFromAnnotationFailsIfNoOptions() {
        @Command(groups =
        @CommandLine.Group(name = "abc",
                exclusive = false, validate = false, required = true,
                headingKey = "headingKeyXXX", heading = "headingXXX", order = 123))
        class App {
        }
        Command command = App.class.getAnnotation(Command.class);
        CommandLine.Group annotation = command.groups()[0];
        try {
            GroupSpec.builder(annotation).build();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Group 'abc' has no options or positional parameters", ex.getMessage());
        }
    }

    @Test
    public void testGroupSpecBuilderFromAnnotation() {
        @Command(groups =
        @Group(name = "abc",
                exclusive = false, validate = false, required = true,
                headingKey = "headingKeyXXX", heading = "headingXXX", order = 123))
        class App {
            @Option(names = "-x", groups = "abc")
            int x;
        }

        CommandLine commandLine = new CommandLine(new App());
        assertEquals(1, commandLine.getCommandSpec().groups().size());
        GroupSpec group = commandLine.getCommandSpec().groups().get("abc");
        assertNotNull(group);

        assertEquals("abc", group.name());
        assertEquals(false, group.exclusive());
        assertEquals(false, group.validate());
        assertEquals(true, group.required());
        assertEquals("headingKeyXXX", group.headingKey());
        assertEquals("headingXXX", group.heading());
        assertEquals(123, group.order());

        assertTrue(group.groups().isEmpty());
        assertEquals(1, group.args().size());
        OptionSpec option = (OptionSpec) group.args().iterator().next();
        assertEquals("-x", option.shortestName());
        assertEquals(Arrays.asList("abc"), option.groupNames());
        assertEquals(1, option.groupNames().size());
        assertEquals(Arrays.asList("abc"), option.groupNames());
    }

    @Test
    public void testGroupSpecBuilderExclusiveTrueByDefault() {
        assertTrue(GroupSpec.builder("A").exclusive());
    }

    @Test
    public void testGroupSpecBuilderExclusiveMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertTrue(builder.exclusive());
        builder.exclusive(false);
        assertFalse(builder.exclusive());
    }

    @Test
    public void testGroupSpecBuilderRequiredFalseByDefault() {
        assertFalse(GroupSpec.builder("A").required());
    }

    @Test
    public void testGroupSpecBuilderRequiredMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertFalse(builder.required());
        builder.required(true);
        assertTrue(builder.required());
    }

    @Test
    public void testGroupSpecBuilderValidatesTrueByDefault() {
        assertTrue(GroupSpec.builder("A").validate());
    }

    @Test
    public void testGroupSpecBuilderValidateMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertTrue(builder.validate());
        builder.validate(false);
        assertFalse(builder.validate());
    }

    @Test
    public void testGroupSpecBuilderGroupNamesEmptyByDefault() {
        assertTrue(GroupSpec.builder("A").groupNames().isEmpty());
    }

    @Test
    public void testGroupSpecBuilderGroupNamesMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertTrue(builder.groupNames().isEmpty());
        builder.groupNames().add("B");
        builder.groupNames().add("C");
        assertEquals(Arrays.asList("B", "C"), builder.groupNames());

        builder.groupNames(Arrays.asList("X", "Y"));
        assertEquals(Arrays.asList("X", "Y"), builder.groupNames());
    }

    // TODO
//    @Test
//    public void testGroupSpecBuilderGroupsEmptyByDefault() {
//        assertTrue(GroupSpec.builder("A").groups().isEmpty());
//    }
//
//    @Test
//    public void testGroupSpecBuilderGroupsMutable() {
//        GroupSpec.Builder builder = GroupSpec.builder("A");
//        assertTrue(builder.groups().isEmpty());
//        builder.groups().add(GroupSpec.builder("B").build());
//        builder.groups().add(GroupSpec.builder("C").build());
//        assertEquals(2, builder.groups().size());
//    }

    @Test
    public void testGroupSpecBuilderOrderMinusOneByDefault() {
        assertEquals(GroupSpec.DEFAULT_ORDER, GroupSpec.builder("A").order());
    }

    @Test
    public void testGroupSpecBuilderOrderMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertEquals(GroupSpec.DEFAULT_ORDER, builder.order());
        builder.order(34);
        assertEquals(34, builder.order());
    }

    @Test
    public void testGroupSpecBuilderHeadingNullByDefault() {
        assertNull(GroupSpec.builder("A").heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertNull(builder.heading());
        builder.heading("This is a header");
        assertEquals("This is a header", builder.heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyNullByDefault() {
        assertNull(GroupSpec.builder("A").headingKey());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyMutable() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        assertNull(builder.headingKey());
        builder.headingKey("KEY");
        assertEquals("KEY", builder.headingKey());
    }

    // TODO
//    @Test
//    public void testGroupSpecBuilderBuildDisallowsDuplidateGroups() {
//        GroupSpec.Builder builder = GroupSpec.builder("A");
//        builder.groups().add(GroupSpec.builder("B").build());
//        builder.groups().add(GroupSpec.builder("B").heading("x").build());
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
        GroupSpec.Builder builder = GroupSpec.builder("A");
        builder.addArg(OPTION);
        GroupSpec group = builder.build();

        assertEquals("A", group.name());
        assertEquals(builder.name(), group.name());

        assertTrue(group.exclusive());
        assertEquals(builder.exclusive(), group.exclusive());

        assertFalse(group.required());
        assertEquals(builder.required(), group.required());

        assertTrue(group.validate());
        assertEquals(builder.validate(), group.validate());

        assertEquals(GroupSpec.DEFAULT_ORDER, group.order());
        assertEquals(builder.order(), group.order());

        assertNull(group.heading());
        assertEquals(builder.heading(), group.heading());

        assertNull(group.headingKey());
        assertEquals(builder.headingKey(), group.headingKey());

        // TODO
//        assertTrue(group.groups().isEmpty());
//        assertEquals(builder.groupNames(), group.groupNames());

        assertTrue(group.groups().isEmpty());
        // TODO assertEquals(builder.groups().isEmpty(), group.groups().isEmpty());
    }

    @Test
    public void testGroupSpecBuilderBuildCopiesBuilderAttributesNonDefault() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.required(true);
        builder.addGroup(GroupSpec.builder("B").addArg(OPTION).groupNames("A").build());

        builder.addArg(OPTION);
        GroupSpec group = builder.build();

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

        assertEquals(1, group.groups().size());
        assertEquals("B", group.groups().get("B").name());
        // TODO assertEquals(builder.groups(), new ArrayList<GroupSpec>(group.groups().values()));
    }

    @Test
    public void testGroupSpecToString() {
        String expected = "Group[A, exclusive=true, required=false, validate=true, order=-1, args=[-x], groups=[], headingKey=null, heading=null]";
        assertEquals(expected, GroupSpec.builder("A").addArg(OPTION).build().toString());

        GroupSpec.Builder builder = GroupSpec.builder("A");
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.required(true);
        builder.addGroup(GroupSpec.builder("B").groupNames("A").addArg(OPTION).build());
        builder.addArg(PositionalParamSpec.builder().index("0..1").paramLabel("FILE").groupNames("A").build());

        String expected2 = "Group[A, exclusive=false, required=true, validate=false, order=123, args=[params[0..1]=FILE], groups=[B], headingKey='my headingKey', heading='my heading']";
        assertEquals(expected2, builder.build().toString());
    }

    @Test
    public void testGroupSpecEquals() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        builder.addArg(OPTION);
        GroupSpec a = builder.build();
        assertEquals(a, a);
        assertNotSame(a, GroupSpec.builder("A").addArg(OPTION).build());
        assertEquals(a, GroupSpec.builder("A").addArg(OPTION).build());

        OptionSpec otherOption = OptionSpec.builder("-y").groupNames("A").build();
        assertNotEquals(a, GroupSpec.builder("A").addArg(OPTION).addArg(otherOption).build());

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

        builder.addGroup(GroupSpec.builder("B").addArg(OPTION).groupNames("A").build());
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());
    }

    @Test
    public void testGroupSpecHashCode() {
        GroupSpec.Builder builder = GroupSpec.builder("A");
        builder.addArg(OPTION);
        GroupSpec a = builder.build();
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), GroupSpec.builder("A").addArg(OPTION).build().hashCode());

        OptionSpec otherOption = OptionSpec.builder("-y").groupNames("A").build();
        assertNotEquals(a.hashCode(), GroupSpec.builder("A").addArg(OPTION).addArg(otherOption).build().hashCode());

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

        builder.groups().add(GroupSpec.builder("B").groupNames("A").addArg(OPTION).build());
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());
    }

    @Test
    public void testReflection() {
        @Command(groups = @CommandLine.Group(name = "AAA"))
        class App {
            @Option(names = "-x", groups = "AAA")
            int x;
            @Option(names = "-y", groups = "AAA")
            int y;
        }
        CommandLine cmd = new CommandLine(new App());
        CommandSpec spec = cmd.getCommandSpec();
        Map<String, GroupSpec> groups = spec.groups();
        assertEquals(1, groups.size());

        GroupSpec group = groups.get("AAA");
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
        spec.addOption(OptionSpec.builder("-x").groupNames("EXCL").build());
        spec.addOption(OptionSpec.builder("-y").groupNames("EXCL").build());
        spec.addOption(OptionSpec.builder("-z").groupNames("ALL").build());

        GroupSpec exclusive = GroupSpec.builder("EXCL")
                .groupNames("ALL")
                .addArg(spec.findOption("-x"))
                .addArg(spec.findOption("-y")).build();
        GroupSpec cooccur = GroupSpec.builder("ALL")
                .addArg(spec.findOption("-z"))
                .addGroup(exclusive).build();
        spec.addGroup(exclusive);
        spec.addGroup(cooccur);

    }

    static final OptionSpec OPTION_A = OptionSpec.builder("-a").build();
    static final OptionSpec OPTION_B = OptionSpec.builder("-b").build();
    static final OptionSpec OPTION_C = OptionSpec.builder("-c").build();

    @Test
    public void testValidationNonRequiredExclusive_ActualTwo() {
        GroupSpec group = GroupSpec.builder("blah").addArg(OPTION_A).addArg(OPTION_B).build();
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
        @Command(groups = @Group(name = "X"))
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
        GroupSpec group = GroupSpec.builder("blah").addArg(OPTION_A).addArg(OPTION_B).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        group.validateConstraints(cmd, Collections.<ArgSpec>emptyList());
    }

    @Test
    public void testReflectionValidationNonRequiredExclusive_ActualZero() {
        @Command(groups = @Group(name = "X"))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
        }
        // no errors
        CommandLine.populateCommand(new App());
    }

    @Test
    public void testValidationRequiredExclusive_ActualZero() {
        GroupSpec group = GroupSpec.builder("blah").required(true).addArg(OPTION_A).addArg(OPTION_B).build();
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
        @Command(groups = @Group(name = "X", required = true))
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
        GroupSpec group = GroupSpec.builder("blah").exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        // no error
        group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_A, OPTION_B, OPTION_C));
    }

    @Test
    public void testReflectionValidationNonRequiredNonExclusive_All() {
        @Command(groups = @Group(name = "X", exclusive = false))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        CommandLine.populateCommand(new App(), "-a", "-b", "-c");
    }

    @Test
    public void testValidationNonRequiredNonExclusive_Partial() {
        GroupSpec group = GroupSpec.builder("blah").exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
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
        @Command(groups = @Group(name = "X", exclusive = false))
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
        GroupSpec group = GroupSpec.builder("blah").exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        // no error
        group.validateConstraints(cmd, Collections.<ArgSpec>emptyList());
    }

    @Test
    public void testReflectionValidationNonRequiredNonExclusive_Zero() {
        @Command(groups = @Group(name = "X", exclusive = false))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        CommandLine.populateCommand(new App());
    }

    @Test
    public void testValidationRequiredNonExclusive_All() {
        GroupSpec group = GroupSpec.builder("blah").required(true).exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
        CommandLine cmd = new CommandLine(CommandSpec.create());

        // no error
        group.validateConstraints(cmd, Arrays.<ArgSpec>asList(OPTION_A, OPTION_B, OPTION_C));
    }

    @Test
    public void testReflectionValidationRequiredNonExclusive_All() {
        @Command(groups = @Group(name = "X", exclusive = false, required = true))
        class App {
            @Option(names = "-a", groups = "X") boolean a;
            @Option(names = "-b", groups = "X") boolean b;
            @Option(names = "-c", groups = "X") boolean c;
        }
        CommandLine.populateCommand(new App(), "-a", "-b", "-c");
    }

    @Test
    public void testValidationRequiredNonExclusive_Partial() {
        GroupSpec group = GroupSpec.builder("blah").required(true).exclusive(false).addArg(OPTION_A).addArg(OPTION_B).addArg(OPTION_C).build();
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
        @Command(groups = @Group(name = "X", exclusive = false, required = true))
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
        GroupSpec group = GroupSpec.builder("blah").required(true).exclusive(false)
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
        @Command(groups = @Group(name = "X", exclusive = false, required = true))
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
    public void testRequiredGroupIgnoreDefaults() {
        // TODO what are the semantics?
        // 1. exclusive required group: one must exist. If none specified, fail, even if all members have a default.
        // 2. co-occurring required group: all must exist. If any missing, fail, even if it has a default.

    }
}