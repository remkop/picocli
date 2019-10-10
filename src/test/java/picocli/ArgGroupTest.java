package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult.GroupMatchContainer;
import picocli.CommandLine.ParseResult.GroupMatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ArgGroupTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    static final OptionSpec OPTION   = OptionSpec.builder("-x").required(true).build();
    static final OptionSpec OPTION_A = OptionSpec.builder("-a").required(true).build();
    static final OptionSpec OPTION_B = OptionSpec.builder("-b").required(true).build();
    static final OptionSpec OPTION_C = OptionSpec.builder("-c").required(true).build();

    @Test
    public void testArgSpecHaveNoGroupsByDefault() {
        assertNull(OptionSpec.builder("-x").build().group());
        assertNull(PositionalParamSpec.builder().build().group());
    }

    @Ignore
    @Test
    public void testArgSpecBuilderHasNoExcludesByDefault() {
//        assertTrue(OptionSpec.builder("-x").excludes().isEmpty());
//        assertTrue(PositionalParamSpec.builder().excludes().isEmpty());
        fail(); // TODO
    }

    @Ignore
    @Test
    public void testOptionSpecBuilderExcludesMutable() {
//        OptionSpec.Builder builder = OptionSpec.builder("-x");
//        assertTrue(builder.excludes().isEmpty());
//
//        builder.excludes("AAA").build();
//        assertEquals(1, builder.excludes().size());
//        assertEquals("AAA", builder.excludes().get(0));
        fail(); // TODO
    }

    @Ignore
    @Test
    public void testPositionalParamSpecBuilderExcludesMutable() {
//        PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
//        assertTrue(builder.excludes().isEmpty());
//
//        builder.excludes("AAA").build();
//        assertEquals(1, builder.excludes().size());
//        assertEquals("AAA", builder.excludes().get(0));
        fail();
    }

    @Test
    public void testGroupSpecBuilderFromAnnotation() {
        class Args {
            @Option(names = "-x") int x;
        }
        class App {
            @ArgGroup(exclusive = false, validate = false, multiplicity = "1",
                    headingKey = "headingKeyXXX", heading = "headingXXX", order = 123)
            Args args;
        }

        CommandLine commandLine = new CommandLine(new App(), new InnerClassFactory(this));
        assertEquals(1, commandLine.getCommandSpec().argGroups().size());
        ArgGroupSpec group = commandLine.getCommandSpec().argGroups().get(0);
        assertNotNull(group);

        assertEquals(false, group.exclusive());
        assertEquals(false, group.validate());
        assertEquals(CommandLine.Range.valueOf("1"), group.multiplicity());
        assertEquals("headingKeyXXX", group.headingKey());
        assertEquals("headingXXX", group.heading());
        assertEquals(123, group.order());

        assertTrue(group.subgroups().isEmpty());
        assertEquals(1, group.args().size());
        OptionSpec option = (OptionSpec) group.args().iterator().next();
        assertEquals("-x", option.shortestName());
        assertSame(group, option.group());
        assertSame(option, commandLine.getCommandSpec().findOption("-x"));
    }

    @Test
    public void testGroupSpecBuilderExclusiveTrueByDefault() {
        assertTrue(ArgGroupSpec.builder().exclusive());
    }

    @Test
    public void testGroupSpecBuilderExclusiveMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertTrue(builder.exclusive());
        builder.exclusive(false);
        assertFalse(builder.exclusive());
    }

    @Test
    public void testGroupSpecBuilderRequiredFalseByDefault() {
        assertEquals(CommandLine.Range.valueOf("0..1"), ArgGroupSpec.builder().multiplicity());
    }

    @Test
    public void testGroupSpecBuilderRequiredMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertEquals(CommandLine.Range.valueOf("0..1"), builder.multiplicity());
        builder.multiplicity("1");
        assertEquals(CommandLine.Range.valueOf("1"), builder.multiplicity());
    }

    @Test
    public void testGroupSpecBuilderValidatesTrueByDefault() {
        assertTrue(ArgGroupSpec.builder().validate());
    }

    @Test
    public void testGroupSpecBuilderValidateMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertTrue(builder.validate());
        builder.validate(false);
        assertFalse(builder.validate());
    }

    @Test
    public void testGroupSpecBuilderSubgroupsEmptyByDefault() {
        assertTrue(ArgGroupSpec.builder().subgroups().isEmpty());
    }

    @Test
    public void testGroupSpecBuilderSubgroupsMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertTrue(builder.subgroups().isEmpty());

        ArgGroupSpec b = ArgGroupSpec.builder().addArg(PositionalParamSpec.builder().build()).build();
        ArgGroupSpec c = ArgGroupSpec.builder().addArg(OptionSpec.builder("-t").build()).build();

        builder.subgroups().add(b);
        builder.subgroups().add(c);
        assertEquals(Arrays.asList(b, c), builder.subgroups());

        ArgGroupSpec x = ArgGroupSpec.builder().addArg(PositionalParamSpec.builder().build()).build();
        ArgGroupSpec y = ArgGroupSpec.builder().addArg(OptionSpec.builder("-y").build()).build();

        builder.subgroups().clear();
        builder.addSubgroup(x).addSubgroup(y);
        assertEquals(Arrays.asList(x, y), builder.subgroups());
    }

    @Test
    public void testGroupSpecBuilderOrderMinusOneByDefault() {
        assertEquals(ArgGroupSpec.DEFAULT_ORDER, ArgGroupSpec.builder().order());
    }

    @Test
    public void testGroupSpecBuilderOrderMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertEquals(ArgGroupSpec.DEFAULT_ORDER, builder.order());
        builder.order(34);
        assertEquals(34, builder.order());
    }

    @Test
    public void testGroupSpecBuilderHeadingNullByDefault() {
        assertNull(ArgGroupSpec.builder().heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertNull(builder.heading());
        builder.heading("This is a header");
        assertEquals("This is a header", builder.heading());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyNullByDefault() {
        assertNull(ArgGroupSpec.builder().headingKey());
    }

    @Test
    public void testGroupSpecBuilderHeadingKeyMutable() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertNull(builder.headingKey());
        builder.headingKey("KEY");
        assertEquals("KEY", builder.headingKey());
    }

    @Test
    public void testGroupSpecBuilderBuildDisallowsEmptyGroups() {
        try {
            ArgGroupSpec.builder().build();
        } catch (InitializationException ok) {
            assertEquals("ArgGroup has no options or positional parameters, and no subgroups: null null", ok.getMessage());
        }
    }

    @Test
    public void testAnOptionCannotBeInMultipleGroups() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        builder.subgroups().add(ArgGroupSpec.builder().addArg(OPTION).build());
        builder.subgroups().add(ArgGroupSpec.builder().multiplicity("1..*").addArg(OPTION).build());

        ArgGroupSpec group = builder.build();
        assertEquals(2, group.subgroups().size());
        CommandSpec spec = CommandSpec.create();
        try {
            spec.addArgGroup(group);
            fail("Expected exception");
        } catch (CommandLine.DuplicateNameException ex) {
            assertEquals("An option cannot be in multiple groups but -x is in (-x) and [-x]. " +
                    "Refactor to avoid this. For example, (-a | (-a -b)) can be rewritten " +
                    "as (-a [-b]), and (-a -b | -a -c) can be rewritten as (-a (-b | -c)).", ex.getMessage());
        }
    }

    @Test
    public void testGroupSpecBuilderBuildCopiesBuilderAttributes() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        builder.addArg(OPTION);
        ArgGroupSpec group = builder.build();

        assertTrue(group.exclusive());
        assertEquals(builder.exclusive(), group.exclusive());

        assertEquals(CommandLine.Range.valueOf("0..1"), group.multiplicity());
        assertEquals(builder.multiplicity(), group.multiplicity());

        assertTrue(group.validate());
        assertEquals(builder.validate(), group.validate());

        assertEquals(ArgGroupSpec.DEFAULT_ORDER, group.order());
        assertEquals(builder.order(), group.order());

        assertNull(group.heading());
        assertEquals(builder.heading(), group.heading());

        assertNull(group.headingKey());
        assertEquals(builder.headingKey(), group.headingKey());

        assertTrue(group.subgroups().isEmpty());
        assertEquals(builder.subgroups(), group.subgroups());

        assertNull(group.parentGroup());
    }

    @Test
    public void testGroupSpecBuilderBuildCopiesBuilderAttributesNonDefault() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.multiplicity("1");
        builder.addSubgroup(ArgGroupSpec.builder().addArg(OPTION).build());

        builder.addArg(OPTION);
        ArgGroupSpec group = builder.build();

        assertFalse(group.exclusive());
        assertEquals(builder.exclusive(), group.exclusive());

        assertEquals(CommandLine.Range.valueOf("1"), group.multiplicity());
        assertEquals(builder.multiplicity(), group.multiplicity());

        assertFalse(group.validate());
        assertEquals(builder.validate(), group.validate());

        assertEquals(123, group.order());
        assertEquals(builder.order(), group.order());

        assertEquals("my heading", group.heading());
        assertEquals(builder.heading(), group.heading());

        assertEquals("my headingKey", group.headingKey());
        assertEquals(builder.headingKey(), group.headingKey());

        assertEquals(1, group.subgroups().size());
        assertEquals(builder.subgroups(), group.subgroups());
    }

    @Test
    public void testGroupSpecToString() {
        String expected = "ArgGroup[exclusive=true, multiplicity=0..1, validate=true, order=-1, args=[-x], headingKey=null, heading=null, subgroups=[]]";
        ArgGroupSpec b = ArgGroupSpec.builder().addArg(OPTION).build();
        assertEquals(expected, b.toString());

        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        builder.heading("my heading");
        builder.headingKey("my headingKey");
        builder.order(123);
        builder.exclusive(false);
        builder.validate(false);
        builder.multiplicity("1");
        builder.addSubgroup(ArgGroupSpec.builder().addSubgroup(b).addArg(OPTION_A).build());
        builder.addArg(PositionalParamSpec.builder().index("0..1").paramLabel("FILE").build());

        String expected2 = "ArgGroup[exclusive=false, multiplicity=1, validate=false, order=123, args=[params[0..1]=FILE], headingKey='my headingKey', heading='my heading'," +
                " subgroups=[ArgGroup[exclusive=true, multiplicity=0..1, validate=true, order=-1, args=[-a], headingKey=null, heading=null," +
                " subgroups=[ArgGroup[exclusive=true, multiplicity=0..1, validate=true, order=-1, args=[-x], headingKey=null, heading=null, subgroups=[]]]" +
                "]]" +
                "]";
        assertEquals(expected2, builder.build().toString());
    }

    @Test
    public void testGroupSpecEquals() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        builder.addArg(OPTION);
        ArgGroupSpec a = builder.build();
        assertEquals(a, a);
        assertNotSame(a, ArgGroupSpec.builder().addArg(OPTION).build());
        assertEquals(a, ArgGroupSpec.builder().addArg(OPTION).build());

        OptionSpec otherOption = OptionSpec.builder("-y").build();
        assertNotEquals(a, ArgGroupSpec.builder().addArg(OPTION).addArg(otherOption).build());

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

        builder.multiplicity("1");
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());

        builder.addSubgroup(ArgGroupSpec.builder().addArg(OPTION).build());
        assertNotEquals(a, builder.build());
        assertEquals(builder.build(), builder.build());
    }

    @Test
    public void testGroupSpecHashCode() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        builder.addArg(OPTION);
        ArgGroupSpec a = builder.build();
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), ArgGroupSpec.builder().addArg(OPTION).build().hashCode());

        OptionSpec otherOption = OptionSpec.builder("-y").build();
        assertNotEquals(a.hashCode(), ArgGroupSpec.builder().addArg(OPTION).addArg(otherOption).build().hashCode());

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

        builder.multiplicity("1");
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());

        builder.subgroups().add(ArgGroupSpec.builder().addArg(OPTION).build());
        assertNotEquals(a.hashCode(), builder.build().hashCode());
        assertEquals(builder.build().hashCode(), builder.build().hashCode());
    }

    @Test
    public void testReflection() {
        class All {
            @Option(names = "-x", required = true) int x;
            @Option(names = "-y", required = true) int y;
        }
        class App {
            @ArgGroup All all;
        }
        CommandLine cmd = new CommandLine(new App(), new InnerClassFactory(this));
        CommandSpec spec = cmd.getCommandSpec();
        List<ArgGroupSpec> groups = spec.argGroups();
        assertEquals(1, groups.size());

        ArgGroupSpec group = groups.get(0);
        assertNotNull(group);

        List<ArgSpec> options = new ArrayList<ArgSpec>(group.args());
        assertEquals(2, options.size());
        assertEquals("-x", ((OptionSpec) options.get(0)).shortestName());
        assertEquals("-y", ((OptionSpec) options.get(1)).shortestName());

        assertNotNull(options.get(0).group());
        assertSame(group, options.get(0).group());

        assertNotNull(options.get(1).group());
        assertSame(group, options.get(1).group());
    }

    @Test
    public void testReflectionRequiresNonEmpty() {
        class Invalid {}
        class App {
            @ArgGroup Invalid invalid;
        }
        App app = new App();
        try {
            new CommandLine(app, new InnerClassFactory(this));
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("ArgGroup has no options or positional parameters, and no subgroups: " +
                    "Scope(value=" + app.toString() +
                    ") FieldBinding(" + Invalid.class.getName() + " " + app.getClass().getName() + ".invalid)", ex.getMessage());
        }
    }

    @Test
    public void testProgrammatic() {
        CommandSpec spec = CommandSpec.create();

        ArgGroupSpec exclusiveSub = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-x").required(true).build())
                .addArg(OptionSpec.builder("-y").required(true).build()).build();
        ArgGroupSpec cooccur = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build())
                .addSubgroup(exclusiveSub).build();
        spec.addArgGroup(cooccur);
        assertNull(cooccur.parentGroup());
        assertEquals(1, cooccur.subgroups().size());
        assertSame(exclusiveSub, cooccur.subgroups().get(0));
        assertEquals(1, cooccur.args().size());

        assertNotNull(exclusiveSub.parentGroup());
        assertSame(cooccur, exclusiveSub.parentGroup());
        assertEquals(0, exclusiveSub.subgroups().size());
        assertEquals(2, exclusiveSub.args().size());

        ArgGroupSpec exclusive = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(true).build()).build();
        spec.addArgGroup(exclusive);
        assertNull(exclusive.parentGroup());
        assertTrue(exclusive.subgroups().isEmpty());
        assertEquals(2, exclusive.args().size());

        List<ArgGroupSpec> groups = spec.argGroups();
        assertEquals(2, groups.size());
        assertSame(cooccur, groups.get(0));
        assertSame(exclusive, groups.get(1));
    }

    @Test
    public void testCannotAddSubgroupToCommand() {
        CommandSpec spec = CommandSpec.create();

        ArgGroupSpec exclusiveSub = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-x").required(true).build())
                .addArg(OptionSpec.builder("-y").required(true).build()).build();
        ArgGroupSpec cooccur = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build())
                .addSubgroup(exclusiveSub).build();

        try {
            spec.addArgGroup(exclusiveSub);
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Groups that are part of another group should not be added to a command. Add only the top-level group.", ex.getMessage());
        }
    }

    @Test
    public void testCannotAddSameGroupToCommandMultipleTimes() {
        CommandSpec spec = CommandSpec.create();

        ArgGroupSpec cooccur = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build()).build();

        spec.addArgGroup(cooccur);
        try {
            spec.addArgGroup(cooccur);
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("The specified group [-z] has already been added to the <main class> command.", ex.getMessage());
        }
    }

    @Test
    public void testIsSubgroupOf_FalseIfUnrelated() {
        ArgGroupSpec other = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build()).build();

        ArgGroupSpec exclusiveSub = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-x").required(true).build())
                .addArg(OptionSpec.builder("-y").required(true).build()).build();
        ArgGroupSpec cooccur = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build())
                .addSubgroup(exclusiveSub).build();

        assertFalse(other.isSubgroupOf(exclusiveSub));
        assertFalse(other.isSubgroupOf(cooccur));

        assertFalse(exclusiveSub.isSubgroupOf(other));
        assertFalse(cooccur.isSubgroupOf(other));
    }

    @Test
    public void testIsSubgroupOf_FalseIfSame() {
        ArgGroupSpec other = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build()).build();

        assertFalse(other.isSubgroupOf(other));
    }

    @Test
    public void testIsSubgroupOf_TrueIfChild() {
        ArgGroupSpec subsub = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build()).build();
        ArgGroupSpec sub = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-x").required(true).build())
                .addArg(OptionSpec.builder("-y").required(true).build())
                .addSubgroup(subsub).build();
        ArgGroupSpec top = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-z").required(true).build())
                .addSubgroup(sub).build();

        assertTrue(sub.isSubgroupOf(top));
        assertTrue(subsub.isSubgroupOf(sub));
        assertTrue(subsub.isSubgroupOf(top));

        assertFalse(top.isSubgroupOf(sub));
        assertFalse(top.isSubgroupOf(subsub));
        assertFalse(sub.isSubgroupOf(subsub));
    }

    @Test
    public void testValidationExclusiveMultiplicity0_1_ActualTwo() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            All all;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-a", "-b");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -a, -b are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    @Test
    public void testValidationGroups2Violation1ExclusiveMultiplicity0_1_ActualTwo() {
        class Group1 {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
        }
        class Group2 {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Group1 g1;
            
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Group2 g2;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-x", "-a", "-b");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -a, -b are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    @Test
    public void testValidationGroups2Violations2BothExclusiveMultiplicity0_1_ActualTwo() {
        class Group1 {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
        }
        class Group2 {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Group1 g1;

            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Group2 g2;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-x", "-y", "-a", "-b");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -x, -y are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    @Test
    public void testValidationGroups2Violations0() {
        class Group1 {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
        }
        class Group2 {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Group1 g1;

            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Group2 g2;
        }
        // no error
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-x", "-a");
    }

    @Test
    public void testValidationExclusiveMultiplicity0_1_ActualZero() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            All all;
        }
        // no errors
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs();
    }

    @Test
    public void testValidationExclusiveMultiplicity1_ActualZero() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "1")
            All all;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs();
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument (specify one of these): (-a | -b)", ex.getMessage());
        }
    }

    @Test
    public void testValidationDependentAllRequiredMultiplicity0_1_All() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false)
            All all;
        }
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-a", "-b", "-c");
    }

    @Test
    public void testValidationDependentSomeOptionalMultiplicity0_1_All() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = false) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false)
            All all;
        }
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-a", "-b", "-c");
    }

    @Test
    public void testValidationDependentSomeOptionalMultiplicity0_1_OptionalOmitted() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = false) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false)
            All all;
        }
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-a", "-c");
    }

    @Test
    public void testValidationDependentMultiplicity0_1_Partial() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false)
            All all;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-a", "-b");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -c", ex.getMessage());
        }
    }

    @Test
    public void testValidationDependentMultiplicity0_1_Zero() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false)
            All all;
        }
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs();
    }

    @Test
    public void testValidationDependentMultiplicity1_All() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1")
            All all;
        }
        new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-a", "-b", "-c");
    }

    @Test
    public void testValidationDependentMultiplicity1_Partial() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1")
            All all;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs("-b");
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): -a, -c", ex.getMessage());
        }
    }

    @Test
    public void testValidationDependentMultiplicity1_Zero() {
        class All {
            @Option(names = "-a", required = true) boolean a;
            @Option(names = "-b", required = true) boolean b;
            @Option(names = "-c", required = true) boolean c;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1")
            All all;
        }
        try {
            new CommandLine(new App(), new InnerClassFactory(this)).parseArgs();
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): (-a -b -c)", ex.getMessage());
        }
    }

    static class Excl {
        @Option(names = "-x", required = true) boolean x;
        @Option(names = "-y", required = true) boolean y;
    }
    static class All {
        @Option(names = "-a", required = true) boolean a;
        @Option(names = "-b", required = true) boolean b;
    }
    static class Composite {
        @ArgGroup(exclusive = false, multiplicity = "0..1")
        All all = new All();

        @ArgGroup(exclusive = true, multiplicity = "1")
        Excl excl = new Excl();
    }
    static class CompositeApp {
        // SYNOPSIS: ([-a -b] | (-x | -y))
        @ArgGroup(exclusive = true, multiplicity = "1")
        Composite composite = new Composite();
    }
    static class OptionalCompositeApp {
        @ArgGroup(exclusive = true, multiplicity = "0..1")
        Composite composite = new Composite();
    }
    @Test
    public void testValidationCompositeMultiplicity1() {
        validateInput(new CompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -b",                                "-a");
        validateInput(new CompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -a",                                "-b");
        validateInput(new CompositeApp(), MaxValuesExceededException.class,     "Error: expected only one match but got ([-a -b] | (-x | -y))={-x} and ([-a -b] | (-x | -y))={-y}",                "-x", "-y");
        validateInput(new CompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -b",                                "-x", "-a");
        validateInput(new CompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -a",                                "-x", "-b");
        validateInput(new CompositeApp(), MutuallyExclusiveArgsException.class, "Error: [-a -b] and (-x | -y) are mutually exclusive (specify only one)", "-a", "-x", "-b");
        validateInput(new CompositeApp(), MutuallyExclusiveArgsException.class, "Error: [-a -b] and (-x | -y) are mutually exclusive (specify only one)", "-a", "-y", "-b");

        // no input
        validateInput(new CompositeApp(), MissingParameterException.class,      "Error: Missing required argument (specify one of these): ([-a -b] | (-x | -y))");

        // no error
        validateInput(new CompositeApp(), null, null, "-a", "-b");
        validateInput(new CompositeApp(), null, null, "-x");
        validateInput(new CompositeApp(), null, null, "-y");
    }
    @Test
    public void testValidationCompositeMultiplicity0_1() {
        validateInput(new OptionalCompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -b",                                "-a");
        validateInput(new OptionalCompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -a",                                "-b");
        validateInput(new OptionalCompositeApp(), MaxValuesExceededException.class,     "Error: expected only one match but got [[-a -b] | (-x | -y)]={-x} and [[-a -b] | (-x | -y)]={-y}",                "-x", "-y");
        validateInput(new OptionalCompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -b",                                "-x", "-a");
        validateInput(new OptionalCompositeApp(), MissingParameterException.class,      "Error: Missing required argument(s): -a",                                "-x", "-b");
        validateInput(new OptionalCompositeApp(), MutuallyExclusiveArgsException.class, "Error: [-a -b] and (-x | -y) are mutually exclusive (specify only one)", "-a", "-x", "-b");
        validateInput(new OptionalCompositeApp(), MutuallyExclusiveArgsException.class, "Error: [-a -b] and (-x | -y) are mutually exclusive (specify only one)", "-a", "-y", "-b");

        // no input: ok because composite as a whole is optional
        validateInput(new OptionalCompositeApp(), null, null);

        // no error
        validateInput(new OptionalCompositeApp(), null, null, "-a", "-b");
        validateInput(new OptionalCompositeApp(), null, null, "-x");
        validateInput(new OptionalCompositeApp(), null, null, "-y");
    }

    private void validateInput(Object userObject, Class<? extends Exception> exceptionClass, String errorMessage, String... args) {
        try {
            CommandLine.populateCommand(userObject, args);
            if (exceptionClass != null) {
                fail("Expected " + exceptionClass.getSimpleName() + " for " + Arrays.asList(args));
            }
        } catch (Exception ex) {
            assertEquals(errorMessage, ex.getMessage());
            assertEquals("Exception for input " + Arrays.asList(args), exceptionClass, ex.getClass());
        }
    }

    @Test
    public void testSynopsisOnlyOptions() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(true).build())
                .addArg(OptionSpec.builder("-c").required(true).build());
        assertEquals("[-a | -b | -c]", builder.build().synopsis());

        builder.multiplicity("1");
        assertEquals("(-a | -b | -c)", builder.build().synopsis());

        builder.multiplicity("2");
        assertEquals("(-a | -b | -c) (-a | -b | -c)", builder.build().synopsis());

        builder.multiplicity("1..3");
        assertEquals("(-a | -b | -c) [-a | -b | -c] [-a | -b | -c]", builder.build().synopsis());

        builder.multiplicity("1..*");
        assertEquals("(-a | -b | -c)...", builder.build().synopsis());

        builder.multiplicity("1");
        builder.exclusive(false);
        assertEquals("(-a -b -c)", builder.build().synopsis());

        builder.multiplicity("0..1");
        assertEquals("[-a -b -c]", builder.build().synopsis());

        builder.multiplicity("0..2");
        assertEquals("[-a -b -c] [-a -b -c]", builder.build().synopsis());

        builder.multiplicity("0..3");
        assertEquals("[-a -b -c] [-a -b -c] [-a -b -c]", builder.build().synopsis());

        builder.multiplicity("0..*");
        assertEquals("[-a -b -c]...", builder.build().synopsis());
    }

    @Test
    public void testSynopsisOnlyPositionals() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder()
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG1").required(true).required(true).build())
                .addArg(PositionalParamSpec.builder().index("1").paramLabel("ARG2").required(true).required(true).build())
                .addArg(PositionalParamSpec.builder().index("2").paramLabel("ARG3").required(true).required(true).build());
        assertEquals("[ARG1 | ARG2 | ARG3]", builder.build().synopsis());

        builder.multiplicity("1");
        assertEquals("(ARG1 | ARG2 | ARG3)", builder.build().synopsis());

        builder.multiplicity("2");
        assertEquals("(ARG1 | ARG2 | ARG3) (ARG1 | ARG2 | ARG3)", builder.build().synopsis());

        builder.multiplicity("1..3");
        assertEquals("(ARG1 | ARG2 | ARG3) [ARG1 | ARG2 | ARG3] [ARG1 | ARG2 | ARG3]", builder.build().synopsis());

        builder.multiplicity("1..*");
        assertEquals("(ARG1 | ARG2 | ARG3)...", builder.build().synopsis());

        builder.multiplicity("1");
        builder.exclusive(false);
        assertEquals("(ARG1 ARG2 ARG3)", builder.build().synopsis());

        builder.multiplicity("0..1");
        assertEquals("[ARG1 ARG2 ARG3]", builder.build().synopsis());

        builder.multiplicity("0..2");
        assertEquals("[ARG1 ARG2 ARG3] [ARG1 ARG2 ARG3]", builder.build().synopsis());

        builder.multiplicity("0..*");
        assertEquals("[ARG1 ARG2 ARG3]...", builder.build().synopsis());
    }

    @Test
    public void testSynopsisMixOptionsPositionals() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder()
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG1").required(true).build())
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG2").required(true).build())
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG3").required(true).build())
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(true).build())
                .addArg(OptionSpec.builder("-c").required(true).build());
        assertEquals("[ARG1 | ARG2 | ARG3 | -a | -b | -c]", builder.build().synopsis());

        builder.multiplicity("1");
        assertEquals("(ARG1 | ARG2 | ARG3 | -a | -b | -c)", builder.build().synopsis());

        builder.exclusive(false);
        assertEquals("(ARG1 ARG2 ARG3 -a -b -c)", builder.build().synopsis());

        builder.multiplicity("0..1");
        assertEquals("[ARG1 ARG2 ARG3 -a -b -c]", builder.build().synopsis());
    }

    @Test
    public void testSynopsisOnlyGroups() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(true).build())
                .addArg(OptionSpec.builder("-c").required(true).build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-e").required(true).build())
                .addArg(OptionSpec.builder("-e").required(true).build())
                .addArg(OptionSpec.builder("-f").required(true).build())
                .multiplicity("1");

        ArgGroupSpec.Builder b3 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-g").required(true).build())
                .addArg(OptionSpec.builder("-h").required(true).build())
                .addArg(OptionSpec.builder("-i").required(true).type(List.class).build())
                .multiplicity("1")
                .exclusive(false);

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder()
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addSubgroup(b3.build());

        assertEquals("[[-a | -b | -c] | (-e | -f) | (-g -h -i=PARAM [-i=PARAM]...)]", composite.build().synopsis());

        composite.multiplicity("1");
        assertEquals("([-a | -b | -c] | (-e | -f) | (-g -h -i=PARAM [-i=PARAM]...))", composite.build().synopsis());

        composite.multiplicity("1..*");
        assertEquals("([-a | -b | -c] | (-e | -f) | (-g -h -i=PARAM [-i=PARAM]...))...", composite.build().synopsis());

        composite.multiplicity("1");
        composite.exclusive(false);
        assertEquals("([-a | -b | -c] (-e | -f) (-g -h -i=PARAM [-i=PARAM]...))", composite.build().synopsis());

        composite.multiplicity("0..1");
        assertEquals("[[-a | -b | -c] (-e | -f) (-g -h -i=PARAM [-i=PARAM]...)]", composite.build().synopsis());

        composite.multiplicity("0..*");
        assertEquals("[[-a | -b | -c] (-e | -f) (-g -h -i=PARAM [-i=PARAM]...)]...", composite.build().synopsis());
    }

    @Test
    public void testSynopsisMixGroupsOptions() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(true).build())
                .addArg(OptionSpec.builder("-c").required(true).build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-e").required(true).build())
                .addArg(OptionSpec.builder("-e").required(true).build())
                .addArg(OptionSpec.builder("-f").required(true).build())
                .multiplicity("1");

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder()
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addArg(OptionSpec.builder("-x").required(true).build())
                .addArg(OptionSpec.builder("-y").required(true).build())
                .addArg(OptionSpec.builder("-z").required(true).build());

        assertEquals("[-x | -y | -z | [-a | -b | -c] | (-e | -f)]", composite.build().synopsis());

        composite.multiplicity("1");
        assertEquals("(-x | -y | -z | [-a | -b | -c] | (-e | -f))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("(-x -y -z [-a | -b | -c] (-e | -f))", composite.build().synopsis());

        composite.multiplicity("0..1");
        assertEquals("[-x -y -z [-a | -b | -c] (-e | -f)]", composite.build().synopsis());
    }

    @Test
    public void testSynopsisMixGroupsPositionals() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(false).build())
                .addArg(OptionSpec.builder("-c").required(true).build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-e").required(false).build())
                .addArg(OptionSpec.builder("-f").required(true).build())
                .multiplicity("1");

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder()
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG1").required(true).build())
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG2").required(true).build())
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG3").required(true).build());

        assertEquals("[ARG1 | ARG2 | ARG3 | [-a | [-b] | -c] | ([-e] | -f)]", composite.build().synopsis());

        composite.multiplicity("1");
        assertEquals("(ARG1 | ARG2 | ARG3 | [-a | [-b] | -c] | ([-e] | -f))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("(ARG1 ARG2 ARG3 [-a | [-b] | -c] ([-e] | -f))", composite.build().synopsis());

        composite.multiplicity("0..1");
        assertEquals("[ARG1 ARG2 ARG3 [-a | [-b] | -c] ([-e] | -f)]", composite.build().synopsis());
    }

    @Test
    public void testSynopsisMixGroupsOptionsPositionals() {
        ArgGroupSpec.Builder b1 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-a").required(true).build())
                .addArg(OptionSpec.builder("-b").required(true).build())
                .addArg(OptionSpec.builder("-c").required(true).build());

        ArgGroupSpec.Builder b2 = ArgGroupSpec.builder()
                .addArg(OptionSpec.builder("-e").required(true).build())
                .addArg(OptionSpec.builder("-e").required(true).build())
                .addArg(OptionSpec.builder("-f").required(true).build())
                .multiplicity("1");

        ArgGroupSpec.Builder composite = ArgGroupSpec.builder()
                .addSubgroup(b1.build())
                .addSubgroup(b2.build())
                .addArg(OptionSpec.builder("-x").required(true).build())
                .addArg(OptionSpec.builder("-y").required(true).build())
                .addArg(OptionSpec.builder("-z").required(true).build())
                .addArg(PositionalParamSpec.builder().index("0").paramLabel("ARG1").required(true).build())
                .addArg(PositionalParamSpec.builder().index("1").paramLabel("ARG2").required(true).build())
                .addArg(PositionalParamSpec.builder().index("2").paramLabel("ARG3").required(true).build());

        assertEquals("[-x | -y | -z | ARG1 | ARG2 | ARG3 | [-a | -b | -c] | (-e | -f)]", composite.build().synopsis());

        composite.multiplicity("1");
        assertEquals("(-x | -y | -z | ARG1 | ARG2 | ARG3 | [-a | -b | -c] | (-e | -f))", composite.build().synopsis());

        composite.exclusive(false);
        assertEquals("(-x -y -z ARG1 ARG2 ARG3 [-a | -b | -c] (-e | -f))", composite.build().synopsis());

        composite.multiplicity("0..1");
        assertEquals("[-x -y -z ARG1 ARG2 ARG3 [-a | -b | -c] (-e | -f)]", composite.build().synopsis());
    }

    static class BiGroup {
        @Option(names = "-x") int x;
        @Option(names = "-y") int y;
    }
    static class SetterMethodApp {
        private BiGroup biGroup;

        @ArgGroup(exclusive = false)
        public void setBiGroup(BiGroup biGroup) {
            this.biGroup = biGroup;
        }
    }
    @Test
    public void testGroupAnnotationOnSetterMethod() {
        SetterMethodApp app = new SetterMethodApp();
        CommandLine cmd = new CommandLine(app, new InnerClassFactory(this));
        assertNull("before parsing", app.biGroup);

        cmd.parseArgs("-x=1", "-y=2");
        assertEquals(1, app.biGroup.x);
        assertEquals(2, app.biGroup.y);
    }

    interface AnnotatedGetterInterface {
        @ArgGroup(exclusive = false) BiGroup getGroup();
    }
    @Test
    public void testGroupAnnotationOnGetterMethod() {
        CommandLine cmd = new CommandLine(AnnotatedGetterInterface.class);
        AnnotatedGetterInterface app = cmd.getCommand();
        assertNull("before parsing", app.getGroup());

        cmd.parseArgs("-x=1", "-y=2");
        assertEquals(1, app.getGroup().x);
        assertEquals(2, app.getGroup().y);
    }

    @Test
    public void testUsageHelpRequiredExclusiveGroup() {
        class Excl {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "1") Excl excl;
        }
        String expected = String.format("" +
                "Usage: <main class> (-x | -y)%n" +
                "  -x%n" +
                "  -y%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpNonRequiredExclusiveGroup() {
        class All {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            All all;
        }
        String expected = String.format("" +
                "Usage: <main class> [-x | -y]%n" +
                "  -x%n" +
                "  -y%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpRequiredNonExclusiveGroup() {
        class All {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1")
            All all;
        }
        String expected = String.format("" +
                "Usage: <main class> (-x -y)%n" +
                "  -x%n" +
                "  -y%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpNonRequiredNonExclusiveGroup() {
        class All {
            @Option(names = "-x", required = true) boolean x;
            @Option(names = "-y", required = true) boolean y;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "0..1")
            All all;
        }
        String expected = String.format("" +
                "Usage: <main class> [-x -y]%n" +
                "  -x%n" +
                "  -y%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpNonValidatingGroupDoesNotImpactSynopsis() {
        class All {
            @Option(names = "-x") boolean x;
            @Option(names = "-y") boolean y;
        }
        class App {
            @ArgGroup(validate = false)
            All all;
        }
        String expected = String.format("" +
                "Usage: <main class> [-xy]%n" +
                "  -x%n" +
                "  -y%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testCompositeGroupSynopsis() {
        class IntXY {
            @Option(names = "-x", required = true) int x;
            @Option(names = "-y", required = true) int y;
        }
        class IntABC {
            @Option(names = "-a", required = true) int a;
            @Option(names = "-b", required = true) int b;
            @Option(names = "-c", required = true) int c;
        }
        class Composite {
            @ArgGroup(exclusive = false, multiplicity = "0..1")
            IntABC all;

            @ArgGroup(exclusive = true, multiplicity = "1")
            IntXY excl;
        }
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Composite composite;
        }
        String expected = String.format("" +
                "Usage: <main class> [[-a=<a> -b=<b> -c=<c>] | (-x=<x> | -y=<y>)]%n" +
                "  -a=<a>%n" +
                "  -b=<b>%n" +
                "  -c=<c>%n" +
                "  -x=<x>%n" +
                "  -y=<y>%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testCompositeGroupSynopsisAnsi() {
        class IntXY {
            @Option(names = "-x", required = true) int x;
            @Option(names = "-y", required = true) int y;
        }
        class IntABC {
            @Option(names = "-a", required = true) int a;
            @Option(names = "-b", required = true) int b;
            @Option(names = "-c", required = true) int c;
        }
        class Composite {
            @ArgGroup(exclusive = false, multiplicity = "0..1")
            IntABC all;

            @ArgGroup(exclusive = true, multiplicity = "1")
            IntXY exclusive;
        }
        @Command
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Composite composite;
        }
        String expected = String.format("" +
                "Usage: @|bold <main class>|@ [[@|yellow -a|@=@|italic <a>|@ @|yellow -b|@=@|italic <b>|@ @|yellow -c|@=@|italic <c>|@] | (@|yellow -x|@=@|italic <x>|@ | @|yellow -y|@=@|italic <y>|@)]%n" +
                "@|yellow  |@ @|yellow -a|@=@|italic <|@@|italic a>|@%n" +
                "@|yellow  |@ @|yellow -b|@=@|italic <|@@|italic b>|@%n" +
                "@|yellow  |@ @|yellow -c|@=@|italic <|@@|italic c>|@%n" +
                "@|yellow  |@ @|yellow -x|@=@|italic <|@@|italic x>|@%n" +
                "@|yellow  |@ @|yellow -y|@=@|italic <|@@|italic y>|@%n");
        expected = Help.Ansi.ON.string(expected);
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.ON);
        assertEquals(expected, actual);
    }

    @Test
    public void testGroupUsageHelpOptionListOptionsWithoutGroupsPrecedeGroups() {
        class IntXY {
            @Option(names = "-x", required = true) int x;
            @Option(names = "-y", required = true) int y;
        }
        class IntABC {
            @Option(names = "-a", required = true) int a;
            @Option(names = "-b", required = true) int b;
            @Option(names = "-c", required = true) int c;
        }
        class Composite {
            @ArgGroup(exclusive = false, multiplicity = "0..1", order = 10,
                    heading = "Co-occurring options:%nThese options must appear together, or not at all.%n")
            IntABC all;

            @ArgGroup(exclusive = true, multiplicity = "1", order = 20, heading = "Exclusive options:%n")
            IntXY excl;
        }
        class App {
            @Option(names = "-A") int A;
            @Option(names = "-B") boolean b;
            @Option(names = "-C") boolean c;

            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Composite composite;

            @ArgGroup(validate = false, heading = "Remaining options:%n", order = 100)
            Object remainder = new Object() {
                @Option(names = "-D") int D;
                @Option(names = "-E") boolean E;
                @Option(names = "-F") boolean F;
            };
        }
        String expected = String.format("" +
                "Usage: <main class> [[-a=<a> -b=<b> -c=<c>] | (-x=<x> | -y=<y>)] [-BCEF]%n" +
                "                    [-A=<A>] [-D=<D>]%n" +
                "  -A=<A>%n" +
                "  -B%n" +
                "  -C%n" +
                "Co-occurring options:%n" +
                "These options must appear together, or not at all.%n" +
                "  -a=<a>%n" +
                "  -b=<b>%n" +
                "  -c=<c>%n" +
                "Exclusive options:%n" +
                "  -x=<x>%n" +
                "  -y=<y>%n" +
                "Remaining options:%n" +
                "  -D=<D>%n" +
                "  -E%n" +
                "  -F%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Ignore("Requires support for positionals with same index in group and in command (outside the group)")
    @Test
    public void testGroupWithOptionsAndPositionals_multiplicity0_1() {
        class Remainder {
            @Option(names = "-a"   , required = true) int a;
            @Parameters(index = "0") File f0;
            @Parameters(index = "1") File f1;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "0..1", order = 10,
                    heading = "Co-occurring args:%nThese options must appear together, or not at all.%n")
            Remainder remainder = new Remainder();

            @Parameters(index = "0") File f2;
        }
        String expected = String.format("" +
                "Usage: <main class> [-a=<a> <f0> <f1>] <f2>%n" +
                "      <f2>%n" +
                "Co-occurring args:%n" +
                "These options must appear together, or not at all.%n" +
                "      <f0>%n" +
                "      <f1>%n" +
                "  -a=<a>%n");

        //TestUtil.setTraceLevel("DEBUG");
        CommandLine cmd = new CommandLine(new App(), new InnerClassFactory(this));
        String actual = cmd.getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);

        ParseResult parseResult = cmd.parseArgs("FILE2");
        assertTrue(parseResult.hasMatchedPositional(0));
        assertEquals("<f2>", parseResult.matchedPositional(0).paramLabel());
    }

    @Ignore("Requires support for positionals with same index in group and in command (outside the group)")
    @Test
    public void testGroupWithOptionsAndPositionals_multiplicity1_2() {
        class Remainder {
            @Option(names = "-a"   , required = true) int a;
            @Parameters(index = "0") File f0;
            @Parameters(index = "1") File f1;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1..2", order = 10,
                    heading = "Co-occurring args:%nThese options must appear together, or not at all.%n")
            Remainder remainder = new Remainder();

            @Parameters(index = "0") File f2;
        }
        String expected = String.format("" +
                "Usage: <main class> (-a=<a> <f0> <f1>) [-a=<a> <f0> <f1>] <f2>%n" +
                "      <f2>%n" +
                "Co-occurring args:%n" +
                "These options must appear together, or not at all.%n" +
                "      <f0>%n" +
                "      <f1>%n" +
                "  -a=<a>%n");

        TestUtil.setTraceLevel("DEBUG");
        CommandLine cmd = new CommandLine(new App(), new InnerClassFactory(this));
        String actual = cmd.getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);

        ParseResult parseResult = cmd.parseArgs("-a=1", "F0", "F1", "FILE2");
        assertTrue(parseResult.hasMatchedPositional(0));
        assertEquals("<f2>", parseResult.matchedPositional(0).paramLabel());
    }

    @Test
    public void testPositionalsInGroupAndInCommand() {
        class Remainder {
            @Option(names = "-a"   , required = true) int a;
            @Parameters(index = "0") File f0;
            @Parameters(index = "1") File f1;
        }
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1..2", order = 10,
                    heading = "Co-occurring args:%nThese options must appear together, or not at all.%n")
            Remainder remainder = new Remainder();

            @Parameters(index = "0..*") List<String> allPositionals;
        }
        String expected = String.format("" +
                "Usage: <main class> (-a=<a> <f0> <f1>) [-a=<a> <f0> <f1>] [<allPositionals>...]%n" +
                "      [<allPositionals>...]%n%n" +
                "Co-occurring args:%n" +
                "These options must appear together, or not at all.%n" +
                "      <f0>%n" +
                "      <f1>%n" +
                "  -a=<a>%n");

        //TestUtil.setTraceLevel("INFO");
        CommandLine cmd = new CommandLine(new App(), new InnerClassFactory(this));
        String actual = cmd.getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);

        ParseResult parseResult = cmd.parseArgs("-a=1", "F0", "F1", "FILE2");
        assertTrue(parseResult.hasMatchedPositional(0));
        assertEquals("<f0>", parseResult.matchedPositional(0).paramLabel());
        //assertEquals("<f1>", parseResult.matchedPositional(1).paramLabel());
        assertEquals("<allPositionals>", parseResult.matchedPositional(2).paramLabel());

        CommandSpec spec = cmd.getCommandSpec();
        PositionalParamSpec pos0 = spec.positionalParameters().get(0);
        PositionalParamSpec pos1 = spec.positionalParameters().get(1);
        PositionalParamSpec pos2 = spec.positionalParameters().get(2);

        assertEquals("<f0>", pos0.paramLabel());
        assertEquals("<allPositionals>", pos1.paramLabel());
        assertEquals("<f1>", pos2.paramLabel());

        assertEquals(Arrays.asList("F0", "F1", "FILE2"), pos1.stringValues());
    }

    @Test
    public void testGroupWithMixedOptionsAndPositionals() {
        class IntXY {
            @Option(names = "-x", required = true) int x;
            @Option(names = "-y", required = true) int y;
        }
        class IntABC {
            @Option(names = "-a", required = true) int a;
            @Parameters(index = "0") File f0;
            @Parameters(index = "1") File f1;
        }
        class Composite {
            @ArgGroup(exclusive = false, multiplicity = "0..2", order = 10,
                    heading = "Co-occurring options:%nThese options must appear together, or not at all.%n")
            List<IntABC> all;

            @ArgGroup(exclusive = true, multiplicity = "1", order = 20, heading = "Exclusive options:%n")
            IntXY excl;
        }
        class App {
            @Option(names = "-A") int A;
            @Option(names = "-B") boolean B;
            @Option(names = "-C") boolean C;

            @ArgGroup(exclusive = true, multiplicity = "0..3")
            Composite[] composite;
        }
        //TestUtil.setTraceLevel("DEBUG");
        App app = new App();
        CommandLine cmd = new CommandLine(app, new InnerClassFactory(this));
        String synopsis = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF)).synopsis(0);
        String expected = String.format("" +
                "<main class> [[-a=<a> <f0> <f1>] [-a=<a> <f0> <f1>] | (-x=<x> | -y=<y>)]%n" +
                "             [[-a=<a> <f0> <f1>] [-a=<a> <f0> <f1>] | (-x=<x> | -y=<y>)]%n" +
                "             [[-a=<a> <f0> <f1>] [-a=<a> <f0> <f1>] | (-x=<x> | -y=<y>)] [-BC]%n" +
                "             [-A=<A>]");
        assertEquals(expected, synopsis.trim());

        try {
            cmd.parseArgs("-x=1", "-a=1", "file0", "file1", "-y=2", "-x=3");
            fail("Expected exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: [-a=<a> <f0> <f1>] and (-x=<x> | -y=<y>) are mutually exclusive (specify only one)", ex.getMessage());
        }
        try {
            cmd.parseArgs("-x=1", "-a=1", "file0", "file1", "-x=2", "-x=3");
            fail("Expected exception");
        //} catch (CommandLine.MaxValuesExceededException ex) {
            //assertEquals("Error: Group: (-x=<x> | -y=<y>) can only be specified 1 times but was matched 3 times.", ex.getMessage());
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: [-a=<a> <f0> <f1>] and (-x=<x> | -y=<y>) are mutually exclusive (specify only one)", ex.getMessage());
        }
        try {
            cmd.parseArgs("-x=1", "-a=1", "file0", "file1");
            fail("Expected exception");
        } catch (CommandLine.MutuallyExclusiveArgsException ex) {
            assertEquals("Error: [-a=<a> <f0> <f1>] and (-x=<x> | -y=<y>) are mutually exclusive (specify only one)", ex.getMessage());
        }

        ArgGroupSpec topLevelGroup = cmd.getCommandSpec().argGroups().get(0);
        assertEquals(Composite[].class, topLevelGroup.userObject().getClass());
        assertSame(app.composite, topLevelGroup.userObject());

        ParseResult parseResult = cmd.parseArgs("-a=1", "file0", "file1", "-a=2", "file2", "file3");
        List<ParseResult.GroupMatch> multiples = parseResult.getGroupMatches();
        assertEquals(1, multiples.size());

        Map<ArgGroupSpec, GroupMatchContainer> matchedTopGroups = multiples.get(0).matchedSubgroups();
        assertEquals(1, matchedTopGroups.size());
        GroupMatchContainer topGroupMatch = matchedTopGroups.entrySet().iterator().next().getValue();
        List<ParseResult.GroupMatch> topGroupMultiples = topGroupMatch.matches();
        assertEquals(1, topGroupMultiples.size());
        ParseResult.GroupMatch topGroupMultiple = topGroupMultiples.get(0);

        Map<ArgGroupSpec, GroupMatchContainer> matchedSubGroups = topGroupMultiple.matchedSubgroups();
        assertEquals(1, matchedSubGroups.size());
        GroupMatchContainer subGroupMatch = matchedSubGroups.entrySet().iterator().next().getValue();
        List<GroupMatch> subGroupMultiples = subGroupMatch.matches();
        assertEquals(2, subGroupMultiples.size());
        ParseResult.GroupMatch subMGM1 = subGroupMultiples.get(0);

        assertFalse(subMGM1.isEmpty());
        assertTrue(subMGM1.matchedSubgroups().isEmpty());
        CommandSpec spec = cmd.getCommandSpec();
        assertEquals(Arrays.asList(1), subMGM1.matchedValues(spec.findOption("-a")));
        assertEquals(Arrays.asList(new File("file0")), subMGM1.matchedValues(spec.positionalParameters().get(0)));
        assertEquals(Arrays.asList(new File("file1")), subMGM1.matchedValues(spec.positionalParameters().get(1)));

        ParseResult.GroupMatch subMGM2 = subGroupMultiples.get(1);
        assertFalse(subMGM2.isEmpty());
        assertTrue(subMGM2.matchedSubgroups().isEmpty());
        assertEquals(Arrays.asList(2), subMGM2.matchedValues(spec.findOption("-a")));
        assertEquals(Arrays.asList(new File("file2")), subMGM2.matchedValues(spec.positionalParameters().get(0)));
        assertEquals(Arrays.asList(new File("file3")), subMGM2.matchedValues(spec.positionalParameters().get(1)));

        List<GroupMatchContainer> found = parseResult.findMatches(topLevelGroup);
        assertSame(topGroupMatch, found.get(0));

        ArgGroupSpec sub = topLevelGroup.subgroups().get(0);
        assertEquals("Co-occurring options:%nThese options must appear together, or not at all.%n", sub.heading());
        List<GroupMatchContainer> foundSub = parseResult.findMatches(sub);
        assertEquals(1, foundSub.size());
    }

    @Test
    public void testGroupUsageHelpOptionListOptionsGroupWithMixedOptionsAndPositionals() {
        class IntXY {
            @Option(names = "-x", required = true) int x;
            @Option(names = "-y", required = true) int y;
        }
        class IntABC {
            @Option(names = "-a", required = true) int a;
            @Parameters(index = "0") File f0;
            @Parameters(index = "1") File f1;
        }
        class Composite {
            @ArgGroup(exclusive = false, multiplicity = "0..1", order = 10,
                    heading = "Co-occurring options:%nThese options must appear together, or not at all.%n")
            IntABC all;

            @ArgGroup(exclusive = true, multiplicity = "1", order = 20, heading = "Exclusive options:%n")
            IntXY excl;
        }
        class App {
            @Option(names = "-A") int A;
            @Option(names = "-B") boolean B;
            @Option(names = "-C") boolean C;

            @Parameters(index = "2") File f2;

            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Composite composite;

            @ArgGroup(validate = false, heading = "Remaining options:%n", order = 100)
            Object remainder = new Object() {
                @Option(names = "-D") int D;
                @Option(names = "-E") boolean E;
                @Option(names = "-F") boolean F;
            };
        }
        String expected = String.format("" +
                "Usage: <main class> [[-a=<a> <f0> <f1>] | (-x=<x> | -y=<y>)] [-BCEF] [-A=<A>]%n" +
                "                    [-D=<D>] <f2>%n" +
                "      <f2>%n" +
                "  -A=<A>%n" +
                "  -B%n" +
                "  -C%n" +
                "Co-occurring options:%n" +
                "These options must appear together, or not at all.%n" +
                "      <f0>%n" +
                "      <f1>%n" +
                "  -a=<a>%n" +
                "Exclusive options:%n" +
                "  -x=<x>%n" +
                "  -y=<y>%n" +
                "Remaining options:%n" +
                "  -D=<D>%n" +
                "  -E%n" +
                "  -F%n");
        String actual = new CommandLine(new App(), new InnerClassFactory(this)).getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testRequiredArgsInAGroupAreNotValidated() {
        class App {
            @ArgGroup(exclusive = true, multiplicity = "0..1")
            Object exclusive = new Object() {
                @Option(names = "-x", required = true) boolean x;

                @ArgGroup(exclusive = false, multiplicity = "1")
                Object all = new Object() {
                    @Option(names = "-a", required = true) int a;
                    @Parameters(index = "0") File f0;
                };
            };
        }
        String expected = String.format("" +
                "Usage: <main class> [-x | (-a=<a> <f0>)]%n" +
                "      <f0>%n" +
                "  -a=<a>%n" +
                "  -x%n");

        CommandLine cmd = new CommandLine(new App());
        String actual = cmd.getUsageMessage(Help.Ansi.OFF);
        assertEquals(expected, actual);

        cmd.parseArgs(); // no error
    }

    static class Mono {
        @Option(names = "-a", required = true) int a;
    }
    static class RepeatingApp {
        @ArgGroup(multiplicity = "2") List<Mono> monos;
    }
    @Test
    public void testRepeatingGroupsSimple() {
        RepeatingApp app = new RepeatingApp();
        CommandLine cmd = new CommandLine(app);
        ParseResult parseResult = cmd.parseArgs("-a", "1", "-a", "2");

        assertEquals(2, app.monos.size());
        assertEquals(1, app.monos.get(0).a);
        assertEquals(2, app.monos.get(1).a);

        GroupMatchContainer groupMatchContainer = parseResult.findMatches(cmd.getCommandSpec().argGroups().get(0)).get(0);
        assertEquals(2, groupMatchContainer.matches().size());

        ArgSpec a = cmd.getCommandSpec().findOption("-a");
        ParseResult.GroupMatch multiple1 = groupMatchContainer.matches().get(0);
        assertEquals(1, multiple1.matchCount(a));
        List<Object> values1 = multiple1.matchedValues(a);
        assertEquals(1, values1.size());
        assertEquals(Integer.class, values1.get(0).getClass());
        assertEquals(1, values1.get(0));

        ParseResult.GroupMatch multiple2 = groupMatchContainer.matches().get(1);
        assertEquals(1, multiple2.matchCount(a));
        List<Object> values2 = multiple2.matchedValues(a);
        assertEquals(1, values2.size());
        assertEquals(Integer.class, values2.get(0).getClass());
        assertEquals(2, values2.get(0));
    }

    @Test
    public void testRepeatingGroupsValidation() {
        //TestUtil.setTraceLevel("DEBUG");

        RepeatingApp app = new RepeatingApp();
        CommandLine cmd = new CommandLine(app);
        try {
            cmd.parseArgs("-a", "1");
            fail("Expected exception");
        } catch (CommandLine.ParameterException ex) {
            assertEquals("Error: Group: (-a=<a>) must be specified 2 times but was matched 1 times", ex.getMessage());
        }
    }

    static class RepeatingGroupWithOptionalElements635 {
        @Option(names = {"-a", "--add-dataset"}, required = true) boolean add;
        @Option(names = {"-d", "--dataset"}    , required = true) String dataset;
        @Option(names = {"-c", "--container"}  , required = false) String container;
        @Option(names = {"-t", "--type"}       , required = false) String type;
    }
    @Command(name = "viewer", usageHelpWidth = 100)
    static class RepeatingCompositeWithOptionalApp635 {
        // SYNOPSIS: viewer (-a -d=DATASET [-c=CONTAINER] [-t=TYPE])... [-f=FALLBACK] <positional>
        @ArgGroup(exclusive = false, multiplicity = "1..*")
        List<RepeatingGroupWithOptionalElements635> composites;

        @Option(names = "-f")
        String fallback;

        @Parameters(index = "0")
        String positional;
    }
    @Test
    public void testRepeatingCompositeGroupWithOptionalElements_Issue635() {
        RepeatingCompositeWithOptionalApp635 app = new RepeatingCompositeWithOptionalApp635();
        CommandLine cmd = new CommandLine(app);

        String synopsis = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF)).synopsis(0);
        assertEquals("viewer (-a -d=<dataset> [-c=<container>] [-t=<type>])... [-f=<fallback>] <positional>", synopsis.trim());

        ParseResult parseResult = cmd.parseArgs("-a", "-d", "data1", "-a", "-d=data2", "-c=contain2", "-t=typ2", "pos", "-a", "-d=data3", "-c=contain3");
        assertEquals("pos", parseResult.matchedPositionalValue(0, ""));
        assertFalse(parseResult.hasMatchedOption("-f"));

        GroupMatchContainer groupMatchContainer = parseResult.findMatches(cmd.getCommandSpec().argGroups().get(0)).get(0);
        assertEquals(3, groupMatchContainer.matches().size());

        CommandSpec spec = cmd.getCommandSpec();
        List<String> data =    Arrays.asList("data1",  "data2",    "data3");
        List<String> contain = Arrays.asList(null,     "contain2", "contain3");
        List<String> type =    Arrays.asList(null,     "typ2",     null);
        for (int i = 0; i < 3; i++) {
            ParseResult.GroupMatch multiple = groupMatchContainer.matches().get(i);
            assertEquals(Arrays.asList(data.get(i)),  multiple.matchedValues(spec.findOption("-d")));
            if (contain.get(i) == null) {
                assertEquals(Collections.emptyList(), multiple.matchedValues(spec.findOption("-c")));
            } else {
                assertEquals(Arrays.asList(contain.get(i)), multiple.matchedValues(spec.findOption("-c")));
            }
            if (type.get(i) == null) {
                assertEquals(Collections.emptyList(), multiple.matchedValues(spec.findOption("-t")));
            } else {
                assertEquals(Arrays.asList(type.get(i)), multiple.matchedValues(spec.findOption("-t")));
            }
        }

        assertEquals(3, app.composites.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(data.get(i),    app.composites.get(i).dataset);
            assertEquals(contain.get(i), app.composites.get(i).container);
            assertEquals(type.get(i),    app.composites.get(i).type);
        }
        assertNull(app.fallback);
        assertEquals("pos", app.positional);
    }

    static class RepeatingGroup635 {
        @Option(names = {"-a", "--add-dataset"}, required = true) boolean add;
        @Option(names = {"-c", "--container"}  , required = true) String container;
        @Option(names = {"-d", "--dataset"}    , required = true) String dataset;
        @Option(names = {"-t", "--type"}       , required = true) String type;
    }
    @Command(name = "viewer", usageHelpWidth = 100)
    static class RepeatingCompositeApp635 {
        // SYNOPSIS: viewer (-a -d=DATASET -c=CONTAINER -t=TYPE)... [-f=FALLBACK] <positional>
        @ArgGroup(exclusive = false, multiplicity = "1..*")
        List<RepeatingGroup635> composites;

        @Option(names = "-f")
        String fallback;

        @Parameters(index = "0")
        String positional;
    }
    @Test
    public void testRepeatingCompositeGroup_Issue635() {
        RepeatingCompositeApp635 app = new RepeatingCompositeApp635();
        CommandLine cmd = new CommandLine(app);

        String synopsis = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF)).synopsis(0);
        assertEquals("viewer (-a -c=<container> -d=<dataset> -t=<type>)... [-f=<fallback>] <positional>", synopsis.trim());

        ParseResult parseResult = cmd.parseArgs("-a", "-d", "data1", "-c=contain1", "-t=typ1", "-a", "-d=data2", "-c=contain2", "-t=typ2", "pos", "-a", "-d=data3", "-c=contain3", "-t=type3");
        assertEquals("pos", parseResult.matchedPositionalValue(0, ""));
        assertFalse(parseResult.hasMatchedOption("-f"));

        ParseResult.GroupMatchContainer groupMatchContainer = parseResult.findMatches(cmd.getCommandSpec().argGroups().get(0)).get(0);
        assertEquals(3, groupMatchContainer.matches().size());

        CommandSpec spec = cmd.getCommandSpec();
        List<String> data =    Arrays.asList("data1",    "data2",    "data3");
        List<String> contain = Arrays.asList("contain1", "contain2", "contain3");
        List<String> type =    Arrays.asList("typ1",     "typ2",     "type3");
        for (int i = 0; i < 3; i++) {
            ParseResult.GroupMatch multiple = groupMatchContainer.matches().get(i);
            assertEquals(Arrays.asList(data.get(i)),    multiple.matchedValues(spec.findOption("-d")));
            assertEquals(Arrays.asList(contain.get(i)), multiple.matchedValues(spec.findOption("-c")));
            assertEquals(Arrays.asList(type.get(i)),    multiple.matchedValues(spec.findOption("-t")));
        }

        assertEquals(3, app.composites.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(data.get(i),    app.composites.get(i).dataset);
            assertEquals(contain.get(i), app.composites.get(i).container);
            assertEquals(type.get(i),    app.composites.get(i).type);
        }
        assertNull(app.fallback);
        assertEquals("pos", app.positional);
    }

    @Command(name = "abc")
    static class OptionPositionalCompositeApp {
        @ArgGroup(exclusive = false, validate = true, multiplicity = "1..*",
                heading = "This is the options list heading (See #450)", order = 1)
        List<OptionPositionalComposite> compositeArguments;
    }
    static class OptionPositionalComposite {
        @Option(names = "--mode", required = true) String mode;
        @Parameters(index = "0") String file;
    }

    @Test
    public void testOptionPositionalComposite() {
        OptionPositionalCompositeApp app = new OptionPositionalCompositeApp();
        CommandLine cmd = new CommandLine(app);

        String synopsis = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF)).synopsis(0);
        assertEquals("abc (--mode=<mode> <file>)...", synopsis.trim());

        ParseResult parseResult = cmd.parseArgs("--mode", "mode1", "/file/1", "--mode", "mode2", "/file/2", "--mode=mode3", "/file/3");

        List<String> data = Arrays.asList("mode1",    "mode2",    "mode3");
        List<String> file = Arrays.asList("/file/1",  "/file/2",  "/file/3");
        assertEquals(3, app.compositeArguments.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(data.get(i), app.compositeArguments.get(i).mode);
            assertEquals(file.get(i), app.compositeArguments.get(i).file);
        }

        ParseResult.GroupMatchContainer groupMatchContainer = parseResult.findMatches(cmd.getCommandSpec().argGroups().get(0)).get(0);
        assertEquals(3, groupMatchContainer.matches().size());

        CommandSpec spec = cmd.getCommandSpec();
        for (int i = 0; i < 3; i++) {
            assertEquals(Arrays.asList(data.get(i)), groupMatchContainer.matches().get(i).matchedValues(spec.findOption("--mode")));
            assertEquals(Arrays.asList(file.get(i)), groupMatchContainer.matches().get(i).matchedValues(spec.positionalParameters().get(0)));
        }
    }

    @Test
    public void testMultipleGroups() {
        class MultiGroup {
            // SYNOPSIS: [--mode=<mode> <file>]...
            @ArgGroup(exclusive = false, multiplicity = "*")
            OptionPositionalComposite[] optPos;

            // SYNOPSIS: [-a -d=DATASET -c=CONTAINER -t=TYPE]
            @ArgGroup(exclusive = false)
            List<RepeatingGroup635> composites;
        }
        MultiGroup app = new MultiGroup();
        CommandLine cmd = new CommandLine(app);

        String synopsis = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF)).synopsis(0);
        String expectedSynopsis = String.format("" +
                "<main class> [--mode=<mode> <file>]... [-a -c=<container> -d=<dataset>%n" +
                "             -t=<type>]");
        assertEquals(expectedSynopsis, synopsis.trim());

        ParseResult parseResult = cmd.parseArgs("--mode=mode1", "/file/1", "-a", "-d=data1", "-c=contain1", "-t=typ1", "--mode=mode2", "/file/2");
        List<ParseResult.GroupMatch> multiples = parseResult.getGroupMatches();
        assertEquals(1, multiples.size());
        GroupMatch groupMatch = multiples.get(0);

        assertEquals(2, groupMatch.matchedSubgroups().size());
        List<ArgGroupSpec> argGroups = cmd.getCommandSpec().argGroups();
        ArgGroupSpec modeGroup = argGroups.get(0);
        ArgGroupSpec addDatasetGroup = argGroups.get(1);

        GroupMatchContainer modeGroupMatchContainer = groupMatch.matchedSubgroups().get(modeGroup);
        assertEquals(2, modeGroupMatchContainer.matches().size());

        GroupMatchContainer addDatasetGroupMatchContainer = groupMatch.matchedSubgroups().get(addDatasetGroup);
        assertEquals(1, addDatasetGroupMatchContainer.matches().size());
    }

    static class CompositeGroupDemo {

        @ArgGroup(exclusive = false, multiplicity = "1..*")
        List<Composite> composites;

        static class Composite {
            @ArgGroup(exclusive = false, multiplicity = "1")
            Dependent dependent;

            @ArgGroup(exclusive = true, multiplicity = "1")
            Exclusive exclusive;
        }

        static class Dependent {
            @Option(names = "-a", required = true)
            int a;
            @Option(names = "-b", required = true)
            int b;
            @Option(names = "-c", required = true)
            int c;
        }

        static class Exclusive {
            @Option(names = "-x", required = true)
            boolean x;
            @Option(names = "-y", required = true)
            boolean y;
            @Option(names = "-z", required = true)
            boolean z;
        }
    }
    @Test
    public void testCompositeDemo() {
        CompositeGroupDemo example = new CompositeGroupDemo();
        CommandLine cmd = new CommandLine(example);

        cmd.parseArgs("-x", "-a=1", "-b=1", "-c=1", "-a=2", "-b=2", "-c=2", "-y");
        assertEquals(2, example.composites.size());

        CompositeGroupDemo.Composite c1 = example.composites.get(0);
        assertTrue(c1.exclusive.x);
        assertEquals(1, c1.dependent.a);
        assertEquals(1, c1.dependent.b);
        assertEquals(1, c1.dependent.c);

        CompositeGroupDemo.Composite c2 = example.composites.get(1);
        assertTrue(c2.exclusive.y);
        assertEquals(2, c2.dependent.a);
        assertEquals(2, c2.dependent.b);
        assertEquals(2, c2.dependent.c);
    }

    static class CompositeGroupSynopsisDemo {

        @ArgGroup(exclusive = false, multiplicity = "2..*")
        List<Composite> composites;

        static class Composite {
            @ArgGroup(exclusive = false, multiplicity = "1")
            Dependent dependent;

            @ArgGroup(exclusive = true, multiplicity = "1")
            Exclusive exclusive;
        }

        static class Dependent {
            @Option(names = "-a", required = true)
            int a;
            @Option(names = "-b", required = true)
            int b;
            @Option(names = "-c", required = true)
            int c;
        }

        static class Exclusive {
            @Option(names = "-x", required = true)
            boolean x;
            @Option(names = "-y", required = true)
            boolean y;
            @Option(names = "-z", required = true)
            boolean z;
        }
    }
    @Test
    public void testCompositeSynopsisDemo() {
        CompositeGroupSynopsisDemo example = new CompositeGroupSynopsisDemo();
        CommandLine cmd = new CommandLine(example);
        String synopsis = cmd.getCommandSpec().argGroups().get(0).synopsis();
        assertEquals("((-a=<a> -b=<b> -c=<c>) (-x | -y | -z)) ((-a=<a> -b=<b> -c=<c>) (-x | -y | -z))...", synopsis);
    }

    // https://github.com/remkop/picocli/issues/635
    @Command(name = "test-composite")
    static class TestComposite {

        @ArgGroup(exclusive = false, multiplicity = "0..*")
        List<OuterGroup> outerList;

        static class OuterGroup {
            @Option(names = "--add-group", required = true) boolean addGroup;

            @ArgGroup(exclusive = false, multiplicity = "1")
            Inner inner;

            static class Inner {
                @Option(names = "--option1", required = true) String option1;
                @Option(names = "--option2", required = true) String option2;
            }
        }
    }

    @Test // https://github.com/remkop/picocli/issues/655
    public void testCompositeValidation() {
        TestComposite app = new TestComposite();
        CommandLine cmd = new CommandLine(app);
        try {
            cmd.parseArgs("--add-group", "--option2=1");
            fail("Expect exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): --option1=<option1>", ex.getMessage());
        }
        try {
            cmd.parseArgs("--add-group");
            fail("Expect exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): (--option1=<option1> --option2=<option2>)", ex.getMessage());
        }
        try {
            cmd.parseArgs("--add-group", "--option2=1", "--option2=1");
            fail("Expect exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): --option1=<option1>", ex.getMessage());
        }
        try {
            cmd.parseArgs("--add-group", "--option2=1", "--option1=1", "--add-group", "--option2=1");
            fail("Expect exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): --option1=<option1>", ex.getMessage());
        }
        try {
            ParseResult parseResult = cmd.parseArgs("--add-group", "--option2=1", "--option1=1", "--add-group");
            fail("Expect exception");
        } catch (MissingParameterException ex) {
            assertEquals("Error: Missing required argument(s): (--option1=<option1> --option2=<option2>)", ex.getMessage());
        }
    }

    @Test
    public void testHelp() {
        @Command(name = "abc", mixinStandardHelpOptions = true)
        class App {
            @ArgGroup(multiplicity = "1")
            Composite composite;
        }
        CommandLine commandLine = new CommandLine(new App());
        ParseResult parseResult = commandLine.parseArgs("--help");
        assertTrue(parseResult.isUsageHelpRequested());
    }

    @Command(name = "ArgGroupsTest", resourceBundle = "picocli.arggroup-localization")
    static class LocalizedCommand {

        @Option(names = {"-q", "--quiet"}, required = true)
        static boolean quiet;

        @ArgGroup(exclusive = true, multiplicity = "1", headingKey = "myKey")
        LocalizedGroup datasource;

        static class LocalizedGroup {
            @Option(names = "-a", required = true)
            static boolean isA;

            @Option(names = "-b", required = true)
            static File dataFile;
        }
    }
    @Test
    public void testArgGroupHeaderLocalization() {
        CommandLine cmd = new CommandLine(new LocalizedCommand());
        String expected = String.format("" +
                "Usage: ArgGroupsTest (-a | -b=<dataFile>) -q%n" +
                "  -q, --quiet      My description for option quiet%n" +
                "My heading text%n" +
                "  -a               My description for exclusive option a%n" +
                "  -b=<dataFile>%n");
        String actual = cmd.getUsageMessage();
        assertEquals(expected, actual);
    }

    @Command(name = "ArgGroupsTest")
    static class TestCommand implements Runnable {

        @ArgGroup( exclusive = true)
        DataSource datasource;

        static class DataSource {
            @Option(names = "-a", required = true, defaultValue = "Strings.gxl")
            static String aString;
        }

        public void run() { }
    }

    @Test
    public void testIssue661StackOverflow() {
        CommandLine cmd = new CommandLine(new TestCommand());
        cmd.parseArgs("-a=Foo");
        cmd.setExecutionStrategy(new CommandLine.RunAll()).execute();
    }

    // TODO add tests with positional interactive params in group
    // TODO add tests with positional params in multiple groups

    static class Issue722 {
        static class Level1Argument {
            @Option(names = "--level-1", required = true)
            private String l1;

            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level2aArgument level2a;

            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level2bArgument level2b;
        }

        static class Level2aArgument {
            @Option(names = "--level-2a", required = true)
            private String l2a;
        }

        static class Level2bArgument {
            @Option(names = "--level-2b", required = true)
            private String l2b;

            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level3aArgument level3a;

            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level3bArgument level3b;
        }

        static class Level3aArgument {
            @Option(names = "--level-3a", required = true)
            private String l3a;
        }

        static class Level3bArgument {
            @Option(names = { "--level-3b"}, required = true)
            private String l3b;
        }

        @Command(name = "arg-group-test", separator = " ", subcommands = {CreateCommand.class, CommandLine.HelpCommand.class})
        public static class ArgGroupCommand implements Runnable {
            public void run() {
            }
        }

        @Command(name = "create", separator = " ", helpCommand = true)
        public static class CreateCommand implements Runnable {
            @Option(names = "--level-0", required = true)
            private String l0;
            
            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level1Argument level1 = new Level1Argument();

            public void run() {
            }
        }
    }
    // https://github.com/remkop/picocli/issues/722
    @Test
    public void testIssue722() {
        String expected = String.format("" +
                "create (--level-1 <l1> (--level-2a <l2a>) (--level-2b <l2b> (--level-3a <l3a>)%n" +
                "       (--level-3b <l3b>))) --level-0 <l0>%n");

        CommandLine cmd = new CommandLine(new Issue722.CreateCommand());
        Help help = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF));
        String actual = help.synopsis(0);

        assertEquals(expected, actual);
    }

    @Command(name = "ArgGroupsTest")
    static class CommandWithSplitGroup {

        @ArgGroup(exclusive = false)
        DataSource datasource;

        static class DataSource {
            @Option(names = "-single", split = ",")
            int single;

            @Option(names = "-array", split = ",")
            int[] array;
        }
    }

    @Test
    // https://github.com/remkop/picocli/issues/745
    public void testIssue745SplitErrorMessageIfValidationDisabled() {
        CommandWithSplitGroup bean = new CommandWithSplitGroup();
        System.setProperty("picocli.ignore.invalid.split", "");
        CommandLine cmd = new CommandLine(bean);

        // split attribute is honoured if option type is multi-value (array, Collection, Map)
        cmd.parseArgs("-array=1,2");
        assertArrayEquals(new int[] {1, 2}, bean.datasource.array);

        // split attribute is ignored if option type is single value
        // error because value cannot be assigned to type `int`
        try {
            cmd.parseArgs("-single=1,2");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Invalid value for option '-single': '1,2' is not an int", ex.getMessage());
        }

        // split attribute ignored for simple commands without argument groups
        try {
            new CommandLine(new CommandWithSplitGroup.DataSource()).parseArgs("-single=1,2");
            fail("Expected exception");
        } catch (ParameterException ex) {
            assertEquals("Invalid value for option '-single': '1,2' is not an int", ex.getMessage());
        }
    }

    @Test
    // https://github.com/remkop/picocli/issues/745
    public void testIssue745SplitDisallowedForSingleValuedOption() {
        CommandWithSplitGroup bean = new CommandWithSplitGroup();
        try {
            new CommandLine(bean);
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Only multi-value options and positional parameters should have a split regex (this check can be disabled by setting system property 'picocli.ignore.invalid.split')", ex.getMessage());
        }
        try {
            new CommandLine(new CommandWithSplitGroup.DataSource());
            fail("Expected initialization exception");
        } catch (InitializationException ex) {
            assertEquals("Only multi-value options and positional parameters should have a split regex (this check can be disabled by setting system property 'picocli.ignore.invalid.split')", ex.getMessage());
        }
    }

    @Command(name = "ArgGroupsTest")
    static class CommandWithDefaultValue {

        @ArgGroup(exclusive = false)
        InitializedGroup initializedGroup = new InitializedGroup();

        @ArgGroup(exclusive = false)
        DeclaredGroup declaredGroup;

        static class InitializedGroup {
            @Option(names = "-staticX", arity = "0..1", defaultValue = "999", fallbackValue = "-88" )
            static int staticX;

            @Option(names = "-instanceX", arity = "0..1", defaultValue = "999", fallbackValue = "-88" )
            int instanceX;
        }

        static class DeclaredGroup {
            @Option(names = "-staticY", arity = "0..1", defaultValue = "999", fallbackValue = "-88" )
            static Integer staticY;

            @Option(names = "-instanceY", arity = "0..1", defaultValue = "999", fallbackValue = "-88" )
            Integer instanceY;
        }
    }

    @Test
    // https://github.com/remkop/picocli/issues/746
    public void test746DefaultValue() {
        //TestUtil.setTraceLevel("DEBUG");
        CommandWithDefaultValue bean = new CommandWithDefaultValue();
        CommandLine cmd = new CommandLine(bean);

        cmd.parseArgs();
        assertEquals(999, bean.initializedGroup.instanceX);
        assertEquals(999, CommandWithDefaultValue.InitializedGroup.staticX);

        assertNull(bean.declaredGroup);
        assertNull(CommandWithDefaultValue.DeclaredGroup.staticY);
    }

    static class Issue746 {
        static class Level1Argument {
            @Option(names = "--l1a", required = true, defaultValue = "l1a")
            private String l1a;

            @Option(names = "--l1b", required = true)
            private String l1b;

            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level2Argument level2;
        }

        static class Level2Argument {
            @Option(names = "--l2a", required = true, defaultValue = "l2a")
            private String l2a;
            @Option(names = "--l2b", required = true)
            private String l2b;

            @ArgGroup(exclusive = false, multiplicity = "1")
            private Level3Argument level3;
        }

        static class Level3Argument {
            @Option(names = "--l3a", required = true)
            private String l3a;

            @Option(names = { "--l3b"}, required = true, defaultValue = "l3b")
            private String l3b;
        }

        @Command(name = "arg-group-test", subcommands = {CreateCommand.class, CommandLine.HelpCommand.class})
        public static class ArgGroupCommand implements Runnable {
            public void run() { }
        }

        @Command(name = "create", helpCommand = true)
        public static class CreateCommand implements Runnable {
            @Option(names = "--l0", required = true, defaultValue = "l0")
            private String l0;

            @ArgGroup(exclusive = false, multiplicity = "0..1")
            private Level1Argument level1 = new Level1Argument();

            public void run() { }
        }
    }

    @Test
    // https://github.com/remkop/picocli/issues/746
    public void testIssue746DefaultValueWithNestedArgGroups() {
        Issue746.CreateCommand bean = new Issue746.CreateCommand();
        CommandLine cmd = new CommandLine(bean);
        cmd.parseArgs();
        assertEquals("l0", bean.l0);
        assertEquals("l1a", bean.level1.l1a);
        assertNull(bean.level1.l1b);
        assertNull(bean.level1.level2);
    }

    @Test
    // https://github.com/remkop/picocli/issues/746
    public void testIssue746ArgGroupWithDefaultValuesSynopsis() {
        String expected = String.format("" +
                "create [[--l1a=<l1a>] --l1b=<l1b> ([--l2a=<l2a>] --l2b=<l2b> (--l3a=<l3a>%n" +
                "       [--l3b=<l3b>]))] [--l0=<l0>]%n");

        CommandLine cmd = new CommandLine(new Issue746.CreateCommand());
        Help help = new Help(cmd.getCommandSpec(), Help.defaultColorScheme(Help.Ansi.OFF));
        String actual = help.synopsis(0);

        assertEquals(expected, actual);
    }

    @Test
    // https://github.com/remkop/picocli/issues/746
    public void testIssue746ArgGroupWithDefaultValuesParsing() {
        Issue746.CreateCommand bean = new Issue746.CreateCommand();
        CommandLine cmd = new CommandLine(bean);

        cmd.parseArgs("--l1b=L1B --l2b=L2B --l3a=L3A".split(" "));
        assertEquals("default value", "l0", bean.l0);
        assertNotNull(bean.level1);
        assertEquals("default value", "l1a", bean.level1.l1a);
        assertEquals("specified value", "L1B", bean.level1.l1b);
        assertNotNull(bean.level1.level2);
        assertEquals("default value", "l2a", bean.level1.level2.l2a);
        assertEquals("specified value", "L2B", bean.level1.level2.l2b);
        assertNotNull(bean.level1.level2.level3);
        assertEquals("default value", "l3b", bean.level1.level2.level3.l3b);
        assertEquals("specified value", "L3A", bean.level1.level2.level3.l3a);
    }

    @Command(name = "Issue742")
    static class Issue742 {

        @ArgGroup(exclusive = false, multiplicity = "2")
        List<DataSource> datasource;

        static class DataSource {
            @Option(names = "-g", required = true, defaultValue = "e")
            String aString;
        }
    }

    @Test
    // https://github.com/remkop/picocli/issues/742
    public void testIssue742FalseErrorMessage() {
        //TestUtil.setTraceLevel("DEBUG");
        CommandLine cmd = new CommandLine(new Issue742());
        ParseResult parseResult = cmd.parseArgs("-g=2", "-g=3");
        List<ParseResult.GroupMatch> multiples = parseResult.getGroupMatches();
        assertEquals(1, multiples.size());
        GroupMatch groupMatch = multiples.get(0);

        assertEquals(1, groupMatch.matchedSubgroups().size());
        ArgGroupSpec dsGroup = cmd.getCommandSpec().argGroups().get(0);
        @SuppressWarnings("unchecked")
        List<Issue742.DataSource> datasources = (List<Issue742.DataSource>) dsGroup.userObject();
        assertEquals(2, datasources.size());

        Issue742.DataSource ds1 = datasources.get(0);
        assertEquals("2", ds1.aString);

        Issue742.DataSource ds2 = datasources.get(1);
        assertEquals("3", ds2.aString);

        GroupMatchContainer modeGroupMatchContainer = groupMatch.matchedSubgroups().get(dsGroup);
        assertEquals(2, modeGroupMatchContainer.matches().size());

        GroupMatch dsGroupMatch1 = modeGroupMatchContainer.matches().get(0);
        assertEquals(0, dsGroupMatch1.matchedSubgroups().size());
        assertEquals(Arrays.asList("2"), dsGroupMatch1.matchedValues(dsGroup.args().iterator().next()));

        GroupMatch dsGroupMatch2 = modeGroupMatchContainer.matches().get(1);
        assertEquals(0, dsGroupMatch2.matchedSubgroups().size());
        assertEquals(Arrays.asList("3"), dsGroupMatch2.matchedValues(dsGroup.args().iterator().next()));
    }
    @Command(name = "ExecuteTest")
    static class Issue758 {

        @ArgGroup(exclusive = false)
        static D d;

        static class D {

            @Option(names = "p")
            static int p1;

            @Option(names = "p")
            static int p2;
        }
    }
    @Test
    public void testIssue758() {
        Issue758 app = new Issue758();
        try {
            new CommandLine(app);
        } catch (DuplicateOptionAnnotationsException ex) {
            String name = Issue758.D.class.getName();
            String msg = "Option name 'p' is used by both field static int " + name + ".p2 and field static int " + name + ".p1";
            assertEquals(msg, ex.getMessage());
        }
    }

    static class Issue807Command {
        @ArgGroup(validate = false, heading = "%nGlobal options:%n")
        protected GlobalOptions globalOptions = new GlobalOptions();

        static class GlobalOptions {
            @Option(names = "-s", description = "ssss")
            boolean slowClock = false;

            @Option(names = "-v", description = "vvvv")
            boolean verbose = false;
        }
    }

    @Test
    public void testIssue807Validation() {
        // should not throw MutuallyExclusiveArgsException
        new CommandLine(new Issue807Command()).parseArgs("-s", "-v");
    }

    static class Issue807SiblingCommand {
        @ArgGroup(validate = true, heading = "%nValidating subgroup options:%n")
        protected ValidatingOptions validatingOptions = new ValidatingOptions();

        @ArgGroup(validate = false, heading = "%nGlobal options:%n")
        protected Issue807Command globalOptions = new Issue807Command();

        static class ValidatingOptions {
            @Option(names = "-x", description = "xxx")
            boolean x = false;

            @Option(names = "-y", description = "yyy")
            boolean y = false;
        }
    }

    @Test
    public void testIssue807SiblingValidation() {
        try {
            new CommandLine(new Issue807SiblingCommand()).parseArgs("-s", "-v", "-x", "-y");
            fail("Expected mutually exclusive args exception");
        } catch (MutuallyExclusiveArgsException ex) {
            assertEquals("Error: -x, -y are mutually exclusive (specify only one)", ex.getMessage());
        }
    }

    static class Issue807NestedCommand {

        @ArgGroup(validate = false, heading = "%nNon-validating over-arching group:%n")
        protected Combo nonValidating = new Combo();

        static class Combo {
            @ArgGroup(validate = true, heading = "%nValidating subgroup options:%n")
            protected ValidatingOptions validatingOptions = new ValidatingOptions();

            @ArgGroup(validate = true, heading = "%nGlobal options:%n")
            protected Issue807Command globalOptions = new Issue807Command();
        }

        static class ValidatingOptions {
            @Option(names = "-x", description = "xxx")
            boolean x = false;

            @Option(names = "-y", description = "yyy")
            boolean y = false;
        }
    }
    @Test
    public void testIssue807NestedValidation() {
        // should not throw MutuallyExclusiveArgsException
        new CommandLine(new Issue807NestedCommand()).parseArgs("-s", "-v", "-x", "-y");
    }

    static class Issue829Group {
        int x;
        int y;
        @Option(names = "-x") void x(int x) {this.x = x;}
        @Option(names = "-y") void y(int y) {this.y = y;}
    }
    @Command(subcommands = Issue829Subcommand.class)
    static class Issue829TopCommand {
        @ArgGroup Issue829Group group;

        @Command
        void sub2(@ArgGroup Issue829Group group) {
            assertEquals(0, group.x);
            assertEquals(3, group.y);
        }
    }
    @Command(name = "sub")
    static class Issue829Subcommand {
        @ArgGroup List<Issue829Group> group;
    }

    @Test
    public void testIssue829NPE_inSubcommandWithArgGroup() {
        //TestUtil.setTraceLevel("DEBUG");
        ParseResult parseResult = new CommandLine(new Issue829TopCommand()).parseArgs("-x=1", "sub", "-y=2");
        assertEquals(1, ((Issue829TopCommand)parseResult.commandSpec().userObject()).group.x);

        Issue829Subcommand sub = (Issue829Subcommand) parseResult.subcommand().commandSpec().userObject();
        assertEquals(0, sub.group.get(0).x);
        assertEquals(2, sub.group.get(0).y);

        new CommandLine(new Issue829TopCommand()).parseArgs("sub2", "-y=3");
    }

    static class Issue815Group {
        @Option(names = {"--age"})
        Integer age;

        @Option(names = {"--id"})
        List<String> id;
    }
    static class Issue815 {
        @ArgGroup(exclusive = false, multiplicity = "1")
        Issue815Group group;
    }
    @Test
    public void testIssue815() {
        //TestUtil.setTraceLevel("DEBUG");
        Issue815 userObject = new Issue815();
        new CommandLine(userObject).parseArgs("--id=123", "--id=456");
        assertNotNull(userObject.group);
        assertNull(userObject.group.age);
        assertNotNull(userObject.group.id);
        assertEquals(Arrays.asList("123", "456"), userObject.group.id);
    }

    static class Issue810Command {
        @ArgGroup(validate = false, heading = "%nGrouped options:%n")
        MyGroup myGroup = new MyGroup();

        static class MyGroup {
            @Option(names = "-s", description = "ssss", required = true, defaultValue = "false")
            boolean s = false;

            @Option(names = "-v", description = "vvvv", required = true, defaultValue = "false")
            boolean v = false;
        }
    }
    @Test
    public void testIssue810Validation() {
        // should not throw MutuallyExclusiveArgsException
        Issue810Command app = new Issue810Command();
        new CommandLine(app).parseArgs("-s", "-v");
        assertTrue(app.myGroup.s);
        assertTrue(app.myGroup.v);

//        app = new Issue810Command();
        new CommandLine(app).parseArgs("-s");
        assertTrue(app.myGroup.s);
        assertFalse(app.myGroup.v);

        new CommandLine(app).parseArgs("-v");
        assertFalse(app.myGroup.s);
        assertTrue(app.myGroup.v);
    }


    static class Issue810WithExplicitExclusiveGroup {
        @ArgGroup(exclusive = true, validate = false, heading = "%nGrouped options:%n")
        MyGroup myGroup = new MyGroup();

        static class MyGroup {
            @Option(names = "-s", required = true)
            boolean s = false;

            @Option(names = "-v", required = true)
            boolean v = false;
        }
    }
    @Test
    public void testNonValidatingOptionsAreNotExclusive() {
        CommandSpec spec = CommandSpec.forAnnotatedObject(new Issue810Command());
        assertFalse(spec.argGroups().get(0).exclusive());

        CommandSpec spec2 = CommandSpec.forAnnotatedObject(new Issue810WithExplicitExclusiveGroup());
        assertFalse(spec2.argGroups().get(0).exclusive());
    }
}
