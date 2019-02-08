package picocli;

import org.junit.Test;
import picocli.CommandLine.Group;
import picocli.CommandLine.Command;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.Model.GroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;

import java.util.Arrays;
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
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("AAA");
        builder.addArg(OPTION);

        assertEquals("AAA", builder.name());
        assertEquals("AAA", builder.build().name());
    }

    @Test
    public void testGroupSpecBuilderNameMutable() {
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("AAA");
        assertEquals("AAA", builder.name());
        builder.name("BBB");
        assertEquals("BBB", builder.name());

        builder.addArg(OPTION);
        assertEquals("BBB", builder.build().name());
    }

    @Test
    public void testGroupSpecBuilderNullNameDisallowed() {
        try {
            CommandLine.Model.GroupSpec.builder((String) null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }

        GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder(""); // TODO empty name ok?
        try {
            builder.name(null);
            fail("Expected exception");
        } catch (NullPointerException ok) {
        }
    }

    @Test
    public void testGroupSpecBuilderNullAnnotationDisallowed() {
        try {
            CommandLine.Model.GroupSpec.builder((CommandLine.Group) null);
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
            CommandLine.Model.GroupSpec.builder(annotation).build();
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
            @Option(names = "-x", groups = "abc") int x;
        }

        CommandLine commandLine = new CommandLine(new App());
        assertEquals(1, commandLine.getCommandSpec().groups().size());
        CommandLine.Model.GroupSpec group = commandLine.getCommandSpec().groups().get("abc");
        assertNotNull(group);

        assertEquals("abc", group.name());
        assertEquals(false, group.exclusive());
        assertEquals(false, group.validate());
        assertEquals(true, group.required());
        assertEquals("headingKeyXXX", group.headingKey());
        assertEquals("headingXXX", group.heading());
        assertEquals(123, group.order());

        assertTrue(group.groups().isEmpty());
        assertTrue(group.positionalParameters().isEmpty());
        assertEquals(1, group.options().size());
        OptionSpec option = group.options().get(0);
        assertEquals("-x", option.shortestName());
        assertEquals(Arrays.asList("abc"), option.groupNames());
        assertEquals(1, option.groupNames().size());
        assertEquals(Arrays.asList("abc"), option.groupNames());
    }

    @Test
    public void testGroupSpecBuilderExclusiveTrueByDefault() {
        assertTrue(CommandLine.Model.GroupSpec.builder("A").exclusive());
    }

    @Test
    public void testGroupSpecBuilderExclusiveMutable() {
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        assertTrue(builder.exclusive());
        builder.exclusive(false);
        assertFalse(builder.exclusive());
    }

    @Test
    public void testGroupSpecBuilderRequiredFalseByDefault() {
        assertFalse(CommandLine.Model.GroupSpec.builder("A").required());
    }

    @Test
    public void testGroupSpecBuilderRequiredMutable() {
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
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
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        assertTrue(builder.validate());
        builder.validate(false);
        assertFalse(builder.validate());
    }

    @Test
    public void testGroupSpecBuilderGroupNamesEmptyByDefault() {
        assertTrue(CommandLine.Model.GroupSpec.builder("A").groupNames().isEmpty());
    }

    @Test
    public void testGroupSpecBuilderGroupNamesMutable() {
        CommandLine.Model.GroupSpec.Builder builder = GroupSpec.builder("A");
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
        assertEquals(CommandLine.Model.GroupSpec.DEFAULT_ORDER, CommandLine.Model.GroupSpec.builder("A").order());
    }

    @Test
    public void testGroupSpecBuilderOrderMutable() {
        GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        assertEquals(CommandLine.Model.GroupSpec.DEFAULT_ORDER, builder.order());
        builder.order(34);
        assertEquals(34, builder.order());
    }

    @Test
    public void testGroupSpecBuilderHeadingNullByDefault() {
        assertNull(CommandLine.Model.GroupSpec.builder("A").heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingMutable() {
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        assertNull(builder.heading());
        builder.heading("This is a header");
        assertEquals("This is a header", builder.heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyNullByDefault() {
        assertNull(CommandLine.Model.GroupSpec.builder("A").headingKey());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyMutable() {
        GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
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
        GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        builder.addArg(OPTION);
        CommandLine.Model.GroupSpec group = builder.build();

        assertEquals("A", group.name());
        assertEquals(builder.name(), group.name());

        assertTrue(group.exclusive());
        assertEquals(builder.exclusive(), group.exclusive());

        assertFalse(group.required());
        assertEquals(builder.required(), group.required());

        assertTrue(group.validate());
        assertEquals(builder.validate(), group.validate());

        assertEquals(CommandLine.Model.GroupSpec.DEFAULT_ORDER, group.order());
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
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.required(true);
        builder.addGroup(CommandLine.Model.GroupSpec.builder("B").addArg(OPTION).groupNames("A").build());

        builder.addArg(OPTION);
        CommandLine.Model.GroupSpec group = builder.build();

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
        String expected = "Group[A, exclusive=true, required=false, validate=true, order=-1, options=[-x], positionals=[], groups=[], headingKey=null, heading=null]";
        assertEquals(expected, CommandLine.Model.GroupSpec.builder("A").addArg(OPTION).build().toString());

        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.required(true);
        builder.addGroup(CommandLine.Model.GroupSpec.builder("B").groupNames("A").addArg(OPTION).build());
        builder.addArg(PositionalParamSpec.builder().index("0..1").paramLabel("FILE").groupNames("A").build());

        String expected2 = "Group[A, exclusive=false, required=true, validate=false, order=123, options=[], positionals=[0..1 (FILE)], groups=[B], headingKey='my headingKey', heading='my heading']";
        assertEquals(expected2, builder.build().toString());
    }

    @Test
    public void testGroupSpecEquals() {
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        builder.addArg(OPTION);
        CommandLine.Model.GroupSpec a = builder.build();
        assertEquals(a, a);
        assertNotSame(a, GroupSpec.builder("A").addArg(OPTION).build());
        assertEquals(a, CommandLine.Model.GroupSpec.builder("A").addArg(OPTION).build());

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
        CommandLine.Model.GroupSpec.Builder builder = CommandLine.Model.GroupSpec.builder("A");
        builder.addArg(OPTION);
        CommandLine.Model.GroupSpec a = builder.build();
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), CommandLine.Model.GroupSpec.builder("A").addArg(OPTION).build().hashCode());

        OptionSpec otherOption = OptionSpec.builder("-y").groupNames("A").build();
        assertNotEquals(a.hashCode(), CommandLine.Model.GroupSpec.builder("A").addArg(OPTION).addArg(otherOption).build().hashCode());

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

        builder.groups().add(CommandLine.Model.GroupSpec.builder("B").groupNames("A").addArg(OPTION).build());
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
        Map<String, CommandLine.Model.GroupSpec> groups = spec.groups();
        assertEquals(1, groups.size());

        CommandLine.Model.GroupSpec group = groups.get("AAA");
        assertNotNull(group);

        assertTrue(group.positionalParameters().isEmpty());

        List<OptionSpec> options = group.options();
        assertEquals(2, options.size());
        assertEquals("-x", options.get(0).shortestName());
        assertEquals("-y", options.get(1).shortestName());

        assertEquals(1, options.get(0).groupNames().size());
        assertEquals(Arrays.asList("AAA"), options.get(0).groupNames());

        assertEquals(1, options.get(1).groupNames().size());
        assertEquals(Arrays.asList("AAA"), options.get(1).groupNames());
    }

    @Test
    public void testProgrammatic1() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-x").groupNames("EXCL").build());
        spec.addOption(OptionSpec.builder("-y").groupNames("EXCL").build());
        spec.addOption(OptionSpec.builder("-z").groupNames("ALL").build());

        CommandLine.Model.GroupSpec exclusive = CommandLine.Model.GroupSpec.builder("EXCL")
                .groupNames("ALL")
                .addArg(spec.findOption("-x"))
                .addArg(spec.findOption("-y")).build();
        CommandLine.Model.GroupSpec cooccur = CommandLine.Model.GroupSpec.builder("ALL")
                .addArg(spec.findOption("-z"))
                .addGroup(exclusive).build();
        spec.addGroup(exclusive);
        spec.addGroup(cooccur);

    }
} // TODO test MutuallyExclusiveArgsException