package picocli;

import org.junit.Rule;
import org.junit.Test;

import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Range;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ModelOptionSpecTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testOptionIsOption() {
        assertTrue(OptionSpec.builder("-x").build().isOption());
    }

    @Test
    public void testOptionIsNotPositional() {
        assertFalse(OptionSpec.builder("-x").build().isPositional());
    }

    @Test
    public void testOptionDefaultOrderIsMinusOne() {
        assertEquals(OptionSpec.DEFAULT_ORDER, OptionSpec.builder("-x").build().order());
    }

    @Test
    public void testOptionDefaultUsageHelpIsFalse() {
        assertFalse(OptionSpec.builder("-x").build().usageHelp());
    }
    @Test
    public void testOptionDefaultVersionHelpIsFalse() {
        assertFalse(OptionSpec.builder("-x").build().versionHelp());
    }
    @Deprecated
    @Test
    public void testOptionDefaultHelpIsFalse() {
        assertFalse(OptionSpec.builder("-x").build().help());
    }
    @Test
    public void testOptionDefaultHiddenIsFalse() {
        assertFalse(OptionSpec.builder("-x").build().hidden());
    }
    @Test
    public void testPositionalDefaultHiddenIsFalse() {
        assertFalse(CommandLine.Model.PositionalParamSpec.builder().build().hidden());
    }
    @Test
    public void testOptionDefaultRequiredIsFalse() {
        assertFalse(OptionSpec.builder("-x").build().required());
    }

    @Test
    public void testOptionDefaultTypeIsBoolean_withDefaultArity() {
        assertEquals(boolean.class, OptionSpec.builder("-x").build().type());
    }

    @Test
    public void testOptionDefaultTypeIsBoolean_withArityZero() {
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("0").build().type());
    }

    @Test
    public void testOptionDefaultTypeIsString_withArityOne() {
        assertEquals(String.class, OptionSpec.builder("-x").arity("1").build().type());
    }

    @Test
    public void testOptionDefaultTypeIsStringArray_withArityTwo() {
        assertEquals(String[].class, OptionSpec.builder("-x").arity("2").build().type());
    }

    @Test
    public void testOptionDefaultAuxiliaryTypesIsDerivedFromType() {
        assertArrayEquals(new Class[] {boolean.class}, OptionSpec.builder("-x").build().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, OptionSpec.builder("-x").type(int.class).build().auxiliaryTypes());
    }

    @Test
    public void testOptionDefaultTypDependsOnArity() {
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("0").build().type());
        assertEquals(String.class, OptionSpec.builder("-x").arity("1").build().type());
        assertEquals(String.class, OptionSpec.builder("-x").arity("0..1").build().type());
        assertEquals(String[].class, OptionSpec.builder("-x").arity("2").build().type());
        assertEquals(String[].class, OptionSpec.builder("-x").arity("0..2").build().type());
        assertEquals(String[].class, OptionSpec.builder("-x").arity("*").build().type());
    }

    @Test
    public void testOptionAuxiliaryTypeOverridesDefaultType() {
        assertEquals(int.class, OptionSpec.builder("-x").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("0").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("0..1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("0..2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("*").auxiliaryTypes(int.class).build().type());
    }

    @Test
    public void testOptionDefaultArityIsZeroIfUntyped() {
        assertEquals(Range.valueOf("0"), OptionSpec.builder("-x").build().arity());
    }

    @Test
    public void testOptionDefaultArityIsZeroIfTypeBoolean() {
        assertEquals(Range.valueOf("0"), OptionSpec.builder("-x").type(boolean.class).build().arity());
        assertEquals(Range.valueOf("0"), OptionSpec.builder("-x").type(Boolean.class).build().arity());
    }

    @Test
    public void testOptionDefaultArityIsOneIfTypeNonBoolean() {
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(int.class).build().arity());
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(Integer.class).build().arity());
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(Byte.class).build().arity());
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(String.class).build().arity());
    }

    @Test
    public void testOptionDefaultSplitRegexIsEmptyString() {
        assertEquals("", OptionSpec.builder("-x").build().splitRegex());
    }

    @Test
    public void testOptionDefaultDescriptionIsEmptyArray() {
        assertArrayEquals(new String[0], OptionSpec.builder("-x").build().description());
    }

    @Test
    public void testOptionDefaultParamLabel() {
        assertEquals("PARAM", OptionSpec.builder("-x").build().paramLabel());
    }

    @Test
    public void testOptionDefaultConvertersIsEmpty() {
        assertArrayEquals(new ITypeConverter[0], OptionSpec.builder("-x").build().converters());
    }

    @Test
    public void testOptionSpecRequiresNonNullName() {
        try {
            OptionSpec.builder(null, "-s").build();
            fail("Expected exception");
        } catch (NullPointerException ex) {
            assertEquals("name", ex.getMessage());
        }
    }

    @Test()
    public void testOptionSpecRequiresNonNullNameArray() {
        try {
            OptionSpec.builder((String[]) null).build();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("OptionSpec names cannot be null. Specify at least one option name.", ex.getMessage());
        }
    }

    @Test
    public void testOptionSpecRequiresAtLeastOneName() {
        try {
            OptionSpec.builder(new String[0]).build();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid names: []", ex.getMessage());
        }
    }

    @Test
    public void testOptionSpecRequiresNonEmptyName() {
        try {
            OptionSpec.builder("").build();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid names: []", ex.getMessage());
        }
    }

    @Test
    public void testOptionBuilderNamesOverwriteInitialValue() {
        OptionSpec option = OptionSpec.builder("-a", "--aaa").names("-b", "--bbb").build();
        assertArrayEquals(new String[] {"-b", "--bbb"}, option.names());
    }

    @Test
    public void testOptionCopyBuilder() {
        OptionSpec option = OptionSpec.builder("-a", "--aaa").arity("1").type(int.class).description("abc").paramLabel("ABC").build();
        OptionSpec copy = option.toBuilder().build();
        assertEquals(option, copy);
        assertNotSame(option, copy);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGettersOnOptionBuilder() {
        ISetter setter = new ISetter() {
            public <T> T set(T value) {
                return null;
            }
        };
        IGetter getter = new IGetter() {
            public <T> T get() {
                return null;
            }
        };
        ITypeConverter<Integer> converter = new ITypeConverter<Integer>() {
            public Integer convert(String value) {
                return null;
            }
        };
        OptionSpec.Builder builder = OptionSpec.builder("-x");
        builder.auxiliaryTypes(Integer.class, Integer.TYPE)
                .type(Double.TYPE)
                .splitRegex(",,,")
                .required(true)
                .defaultValue("DEF")
                .description("Description")
                .paramLabel("param")
                .arity("1")
                .help(true)
                .versionHelp(true)
                .usageHelp(true)
                .order(123)
                .hidden(true)
                .setter(setter)
                .getter(getter)
                .converters(converter)
                .initialValue("ABC")
                .showDefaultValue(Visibility.NEVER)
                .withToString("TOSTRING");
        assertArrayEquals(new Class[]{Integer.class, Integer.TYPE}, builder.auxiliaryTypes());
        assertEquals(Double.TYPE, builder.type());
        assertEquals(",,,", builder.splitRegex());
        assertTrue(builder.required());
        assertEquals("DEF", builder.defaultValue());
        assertArrayEquals(new String[]{"Description"}, builder.description());
        assertEquals("param", builder.paramLabel());
        assertEquals(Range.valueOf("1"), builder.arity());
        assertTrue(builder.help());
        assertTrue(builder.versionHelp());
        assertTrue(builder.usageHelp());
        assertEquals(123, builder.order());
        assertTrue(builder.hidden());
        assertSame(getter, builder.getter());
        assertSame(setter, builder.setter());
        assertSame(converter, builder.converters()[0]);
        assertEquals("ABC", builder.initialValue());
        assertEquals(Visibility.NEVER, builder.showDefaultValue());
        assertEquals("TOSTRING", builder.toString());

        builder.names("a", "b", "c")
                .type(String.class)
                .auxiliaryTypes(StringWriter.class);
        assertArrayEquals(new String[]{"a", "b", "c"}, builder.names());
        assertArrayEquals(new Class[]{StringWriter.class}, builder.auxiliaryTypes());
        assertEquals(String.class, builder.type());
    }

    @Test
    public void testOptionInteractiveFalseByDefault() {
        assertFalse(OptionSpec.builder("-x").interactive());
        assertFalse(OptionSpec.builder("-x").build().interactive());
    }

    @Test
    public void testOptionInteractiveIfSet() {
        assertTrue(OptionSpec.builder("-x").interactive(true).interactive());
        assertTrue(OptionSpec.builder("-x").arity("0").interactive(true).build().interactive());
    }

    @Test
    public void testOptionInteractiveReadFromAnnotation() {
        class App {
            @Option(names = "-x", interactive = true) int x;
            @Option(names = "-y", interactive = false) int y;
            @Option(names = "-z") int z;
        }

        CommandLine cmd = new CommandLine(new App());
        assertTrue(cmd.getCommandSpec().findOption("x").interactive());
        assertFalse(cmd.getCommandSpec().findOption("y").interactive());
        assertFalse(cmd.getCommandSpec().findOption("z").interactive());
    }

    @Test
    public void testOptionInteractiveNotSupportedForMultiValue() {
        OptionSpec.Builder[] options = new OptionSpec.Builder[]{
                OptionSpec.builder("-x").arity("1").interactive(true),
                OptionSpec.builder("-x").arity("2").interactive(true),
                OptionSpec.builder("-x").arity("3").interactive(true),
                OptionSpec.builder("-x").arity("1..2").interactive(true),
                OptionSpec.builder("-x").arity("1..*").interactive(true),
                OptionSpec.builder("-x").arity("0..*").interactive(true),
        };
        for (OptionSpec.Builder opt : options) {
            try {
                opt.build();
                fail("Expected exception");
            } catch (InitializationException ex) {
                assertEquals("Interactive options and positional parameters are only supported for arity=0 and arity=0..1; not for arity=" + opt.arity(), ex.getMessage());
            }
        }

        // no errors
        OptionSpec.builder("-x").arity("0").interactive(true).build();
        OptionSpec.builder("-x").arity("0..1").interactive(true).build();
    }

    @Test
    public void testOptionHasCommand() {
        class App {
            @Option(names = "-x") int x;
        }

        CommandSpec cmd = new CommandLine(new App()).getCommandSpec();
        CommandSpec cmd2 = new CommandLine(new App()).getCommandSpec();
        OptionSpec optx = cmd.findOption('x');
        assertEquals(cmd, optx.command());
        OptionSpec opty = OptionSpec.builder("-y").arity("1").build();
        assertEquals(null, opty.command());
        cmd.add(opty);
        assertEquals(cmd, opty.command());
        cmd2.add(opty);
        assertEquals(cmd2, opty.command());
        assertEquals(opty, cmd.findOption('y'));
    }

    @Test
    public void testOptionSpecEquals() {
        OptionSpec.Builder option = OptionSpec.builder("-x")
                .arity("1")
                .hideParamSyntax(true)
                .required(true)
                .splitRegex(";")
                .description("desc")
                .descriptionKey("key")
                .type(Map.class)
                .auxiliaryTypes(Integer.class, Double.class)
                .help(true)
                .usageHelp(true)
                .versionHelp(true)
                .order(123);

        OptionSpec p1 = option.build();
        assertEquals(p1, p1);
        assertEquals(p1, option.build());
        assertNotEquals(p1, option.arity("2").build());
        assertNotEquals(p1, option.arity("1").hideParamSyntax(false).build());
        assertNotEquals(p1, option.hideParamSyntax(true).required(false).build());
        assertNotEquals(p1, option.required(true).splitRegex(",").build());
        assertNotEquals(p1, option.splitRegex(";").description("xyz").build());
        assertNotEquals(p1, option.description("desc").descriptionKey("XX").build());
        assertNotEquals(p1, option.descriptionKey("key").type(List.class).build());
        assertNotEquals(p1, option.type(Map.class).auxiliaryTypes(Short.class).build());
        assertEquals(p1, option.auxiliaryTypes(Integer.class, Double.class).build());

        assertNotEquals(p1, option.help(false).build());
        assertNotEquals(p1, option.help(true).usageHelp(false).build());
        assertNotEquals(p1, option.usageHelp(true).versionHelp(false).build());
        assertNotEquals(p1, option.versionHelp(true).order(999).build());
        assertNotEquals(p1, option.order(123).names("-a", "-b", "-c").build());
        assertEquals(p1, option.names("-x").build());
    }

    @Test
    public void testOptionSpecBuilder_negatableGetter() {
        OptionSpec.Builder builder = OptionSpec.builder("-x");
        assertFalse(builder.negatable());
    }

    @Test
    public void testOptionSpec_negatableSetter() {
        OptionSpec.Builder builder = OptionSpec.builder("-x").negatable(true);
        assertTrue(builder.negatable());
    }

    @Test
    public void testOptionSpec_fallbackValueGetter() {
        OptionSpec.Builder builder = OptionSpec.builder("-x");
        assertEquals("", builder.fallbackValue());
    }

    @Test
    public void testOptionSpec_fallbackValueSetter() {
        OptionSpec.Builder builder = OptionSpec.builder("-x").fallbackValue("fallback");
        assertEquals("fallback", builder.fallbackValue());
    }

    @Test
    public void testEmptyUsageSplit() {
        assertEquals("", OptionSpec.builder("-x").build().splitRegexSynopsisLabel());
    }

    @Test
    public void testGetterAndSetterOfUsageSplit() {
        OptionSpec.Builder builder = OptionSpec.builder("-x");
        builder.auxiliaryTypes(Integer.class, Integer.TYPE)
            .splitRegex("\\|")
            .splitRegexSynopsisLabel("|");
        assertEquals("\\|", builder.splitRegex());
        assertEquals("|", builder.splitRegexSynopsisLabel());
    }

    @Test
    public void testUsageSplitEquals() {
        OptionSpec.Builder option = OptionSpec.builder("-x")
            .arity("1")
            .hideParamSyntax(true)
            .required(true)
            .splitRegex("\\|")
            .splitRegexSynopsisLabel("|")
            .description("desc")
            .descriptionKey("key")
            .type(Map.class)
            .auxiliaryTypes(Integer.class, Double.class)
            .help(true)
            .usageHelp(true)
            .versionHelp(true)
            .order(123);

        OptionSpec p1 = option.build();
        assertEquals(p1, p1);
        assertEquals(p1, option.build());
        assertNotEquals(p1, option.splitRegexSynopsisLabel("\\\\?").build());
    }
}
