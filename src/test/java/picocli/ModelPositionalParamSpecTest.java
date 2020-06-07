package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Range;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ModelPositionalParamSpecTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testPositionalParamSpecIsNotOption() {
        assertFalse(CommandLine.Model.PositionalParamSpec.builder().build().isOption());
    }

    @Test
    public void testPositionalParamSpecIsPositional() {
        assertTrue(CommandLine.Model.PositionalParamSpec.builder().build().isPositional());
    }
    @Test
    public void testPositionalDefaultRequiredIsFalse() {
        assertFalse(CommandLine.Model.PositionalParamSpec.builder().build().required());
    }
    @Test
    public void testPositionalDefaultFixParamLabelIsFalse() {
        assertFalse(CommandLine.Model.PositionalParamSpec.builder().build().hideParamSyntax());
    }

    @Test
    public void testPositionalDefaultTypeIsString_withDefaultArity() {
        assertEquals(String.class, CommandLine.Model.PositionalParamSpec.builder().build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString_withArityZero() {
        assertEquals(String.class, CommandLine.Model.PositionalParamSpec.builder().arity("0").build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString_withArityOne() {
        assertEquals(String.class, CommandLine.Model.PositionalParamSpec.builder().arity("1").build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsStringArray_withArityTwo() {
        assertEquals(String[].class, CommandLine.Model.PositionalParamSpec.builder().arity("2").build().type());
    }

    @Test
    public void testPositionalWithArityHasDefaultTypeString() {
        assertEquals(String.class, CommandLine.Model.PositionalParamSpec.builder().arity("0").build().type());
        assertEquals(String.class, CommandLine.Model.PositionalParamSpec.builder().arity("1").build().type());
        assertEquals(String.class, CommandLine.Model.PositionalParamSpec.builder().arity("0..1").build().type());
        assertEquals(String[].class, CommandLine.Model.PositionalParamSpec.builder().arity("2").build().type());
        assertEquals(String[].class, CommandLine.Model.PositionalParamSpec.builder().arity("0..2").build().type());
        assertEquals(String[].class, CommandLine.Model.PositionalParamSpec.builder().arity("*").build().type());
    }

    @Test
    public void testPositionalAuxiliaryTypeOverridesDefaultType() {
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().arity("0").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().arity("1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().arity("0..1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().arity("2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().arity("0..2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, CommandLine.Model.PositionalParamSpec.builder().arity("*").auxiliaryTypes(int.class).build().type());
    }
    @Test
    public void testPositionalDefaultAuxiliaryTypesIsDerivedFromType() {
        assertArrayEquals(new Class[] {String.class}, CommandLine.Model.PositionalParamSpec.builder().build().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, CommandLine.Model.PositionalParamSpec.builder().type(int.class).build().auxiliaryTypes());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfUntyped() {
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().build().arity());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfTypeBoolean() {
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().type(boolean.class).build().arity());
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().type(Boolean.class).build().arity());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfTypeNonBoolean() {
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().type(int.class).build().arity());
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().type(Integer.class).build().arity());
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().type(Byte.class).build().arity());
        assertEquals(Range.valueOf("1"), CommandLine.Model.PositionalParamSpec.builder().type(String.class).build().arity());
    }
    @Test
    public void testPositionalDefaultSplitRegexIsEmptyString() {
        assertEquals("", CommandLine.Model.PositionalParamSpec.builder().build().splitRegex());
    }
    @Test
    public void testPositionalDefaultDescriptionIsEmptyArray() {
        assertArrayEquals(new String[0], CommandLine.Model.PositionalParamSpec.builder().build().description());
    }
    @Test
    public void testPositionalDefaultParamLabel() {
        assertEquals("PARAM", PositionalParamSpec.builder().build().paramLabel());
    }

    @Test
    public void testRelativeIndexToString() {
        assertEquals("+", Range.valueOf("+").toString());
        assertEquals("+ (+)", Range.valueOf("+").internalToString());
    }

    @Test
    public void testRelativeAnchoredIndexToString() {
        assertEquals("0", Range.valueOf("0+").toString());
        assertEquals("0+ (0)", Range.valueOf("0+").internalToString());
    }

    @Test
    public void testPositionalDefaultIndexIsNext() {
        assertEquals(Range.valueOf("0+"), PositionalParamSpec.builder().build().index());
    }

    @Test
    public void testPositionalDefaultIndexForSingleValueIsNext() {
        assertEquals(Range.valueOf("0+"), PositionalParamSpec.builder().type(String.class).build().index());
    }

    @Test
    public void testPositionalDefaultIndexForMultiValueIsAll() {
        assertEquals(Range.valueOf("*"), PositionalParamSpec.builder().type(List.class).build().index());
    }

    @Test
    public void testPositionalDefaultArityIsOne() {
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().build().arity());
    }
    @Test
    public void testPositionalDefaultConvertersIsEmpty() {
        assertArrayEquals(new ITypeConverter[0], PositionalParamSpec.builder().build().converters());
    }

    @Test
    public void testPositionalCopyBuilder() {
        PositionalParamSpec option = PositionalParamSpec.builder().index("0..34").arity("1").type(int.class).description("abc").paramLabel("ABC").build();
        PositionalParamSpec copy = option.toBuilder().build();
        assertEquals(option, copy);
        assertNotSame(option, copy);
    }

    @Test
    public void testGettersOnPositionalBuilder() {
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
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
        builder.auxiliaryTypes(Integer.class, Integer.TYPE)
                .type(Double.TYPE)
                .splitRegex(",,,")
                .required(true)
                .defaultValue("DEF")
                .description("Description")
                .paramLabel("param")
                .arity("1")
                .hidden(true)
                .setter(setter)
                .getter(getter)
                .converters(converter)
                .initialValue("ABC")
                .showDefaultValue(Visibility.NEVER)
                .index("3..4")
                .withToString("TOSTRING");
        assertArrayEquals(new Class[]{Integer.class, Integer.TYPE}, builder.auxiliaryTypes());
        assertEquals(Double.TYPE, builder.type());
        assertEquals(",,,", builder.splitRegex());
        assertTrue(builder.required());
        assertEquals("DEF", builder.defaultValue());
        assertArrayEquals(new String[]{"Description"}, builder.description());
        assertEquals("param", builder.paramLabel());
        assertEquals(Range.valueOf("1"), builder.arity());
        assertTrue(builder.hidden());
        assertSame(getter, builder.getter());
        assertSame(setter, builder.setter());
        assertSame(converter, builder.converters()[0]);
        assertEquals("ABC", builder.initialValue());
        assertEquals(Visibility.NEVER, builder.showDefaultValue());
        assertEquals("TOSTRING", builder.toString());
        assertEquals(Range.valueOf("3..4"), builder.index());
    }

    @Test
    public void testPositionalInteractiveFalseByDefault() {
        assertFalse(PositionalParamSpec.builder().interactive());
        assertFalse(PositionalParamSpec.builder().build().interactive());
    }

    @Test
    public void testPositionalInteractiveIfSet() {
        assertTrue(PositionalParamSpec.builder().interactive(true).interactive());
        assertTrue(PositionalParamSpec.builder().interactive(true).build().interactive());
    }

    @Test
    public void testPositionalInteractiveNotSupportedForMultiValue() {
        PositionalParamSpec.Builder[] options = new PositionalParamSpec.Builder[]{
                PositionalParamSpec.builder().arity("1").interactive(true),
                PositionalParamSpec.builder().arity("2").interactive(true),
                PositionalParamSpec.builder().arity("3").interactive(true),
                PositionalParamSpec.builder().arity("1..2").interactive(true),
                PositionalParamSpec.builder().arity("1..*").interactive(true),
                PositionalParamSpec.builder().arity("0..*").interactive(true),
        };
        for (PositionalParamSpec.Builder opt : options) {
            try {
                opt.build();
                fail("Expected exception");
            } catch (CommandLine.InitializationException ex) {
                assertEquals("Interactive options and positional parameters are only supported for arity=0 and arity=0..1; not for arity=" + opt.arity(), ex.getMessage());
            }
        }
        // no errors
        PositionalParamSpec.builder().arity("0").interactive(true).build();
        PositionalParamSpec.builder().arity("0..1").interactive(true).build();
    }

    @Test
    public void testPositionalInteractiveReadFromAnnotation() {
        class App {
            @Parameters(index = "0", interactive = true) int x;
            @Parameters(index = "1", interactive = false) int y;
            @Parameters(index = "2") int z;
        }

        CommandLine cmd = new CommandLine(new App());
        assertTrue(cmd.getCommandSpec().positionalParameters().get(0).interactive());
        assertFalse(cmd.getCommandSpec().positionalParameters().get(1).interactive());
        assertFalse(cmd.getCommandSpec().positionalParameters().get(2).interactive());
    }

    @Test
    public void testParameterHasCommand() {
        class App {
            @Parameters(index="0") int x;
        }

        CommandSpec cmd = new CommandLine(new App()).getCommandSpec();
        CommandSpec cmd2 = new CommandLine(new App()).getCommandSpec();
        PositionalParamSpec param = cmd.positionalParameters().get(0);
        assertEquals(cmd, param.command());
        PositionalParamSpec param1 = PositionalParamSpec.builder().index("1").build();
        assertEquals(null, param1.command());
        cmd.add(param1);
        assertEquals(cmd, param1.command());
        cmd2.add(param1);
        assertEquals(cmd2, param1.command());
        assertEquals(param1, cmd.positionalParameters().get(1));
    }

    @Test
    public void testPositionalParamSpecEquals() {
        PositionalParamSpec.Builder positional = PositionalParamSpec.builder()
                .arity("1")
                .hideParamSyntax(true)
                .required(true)
                .splitRegex(";")
                .description("desc")
                .descriptionKey("key")
                .type(Map.class)
                .auxiliaryTypes(Integer.class, Double.class)
                .index("1..3");

        PositionalParamSpec p1 = positional.build();
        assertEquals(p1, p1);
        assertEquals(p1, positional.build());
        assertNotEquals(p1, positional.arity("2").build());
        assertNotEquals(p1, positional.arity("1").hideParamSyntax(false).build());
        assertNotEquals(p1, positional.hideParamSyntax(true).required(false).build());
        assertNotEquals(p1, positional.required(true).splitRegex(",").build());
        assertNotEquals(p1, positional.splitRegex(";").description("xyz").build());
        assertNotEquals(p1, positional.description("desc").descriptionKey("XX").build());
        assertNotEquals(p1, positional.descriptionKey("key").type(List.class).build());
        assertNotEquals(p1, positional.type(Map.class).auxiliaryTypes(Short.class).build());
        assertEquals(p1, positional.auxiliaryTypes(Integer.class, Double.class).build());

        assertNotEquals(p1, positional.index("0..*").build());
        assertEquals(p1, positional.index("1..3").build());
    }

    @Test
    public void testUnresolvedPositionalParamIndex() {
        class PositionalUnresolvedIndex {
            @Parameters(index = "${index:-0}") String first;
            @Parameters(index = "${index:-1}") String second;
        }
        CommandLine cmd = new CommandLine(new PositionalUnresolvedIndex());
        PositionalParamSpec first = cmd.getCommandSpec().positionalParameters().get(0);
        assertEquals(CommandLine.Range.valueOf("0"), first.index());
        PositionalParamSpec second = cmd.getCommandSpec().positionalParameters().get(1);
        assertEquals(CommandLine.Range.valueOf("1"), second.index());
    }

    @Test
    public void testPositionalParamSpec_builderCopy() {
        PositionalParamSpec original = PositionalParamSpec.builder().index("3..4").build();
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder(original);
        assertEquals(CommandLine.Range.valueOf("3..4"), builder.index());
        assertEquals(original, builder.build());
    }
}
