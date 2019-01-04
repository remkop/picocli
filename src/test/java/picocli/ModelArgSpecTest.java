package picocli;

import org.junit.Test;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

public class ModelArgSpecTest {

    @Test
    public void testArgSpecConstructorWithEmptyAuxTypes() {
        PositionalParamSpec positional = PositionalParamSpec.builder().auxiliaryTypes(new Class[0]).build();
        assertEquals(CommandLine.Range.valueOf("1"), positional.arity());
        assertEquals(String.class, positional.type());
        assertArrayEquals(new Class[] {String.class}, positional.auxiliaryTypes());
    }

    @Test
    public void testArgSpecRenderedDescriptionInitial() {
        PositionalParamSpec positional = PositionalParamSpec.builder().build();
        assertArrayEquals(new String[0], positional.renderedDescription());

        PositionalParamSpec positional2 = PositionalParamSpec.builder().description(new String[0]).build();
        assertArrayEquals(new String[0], positional2.renderedDescription());
    }

    @Test
    public void testArgSpecGetter() {
        IGetter getter = new IGetter() {
            public <T> T get() { return null; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().getter(getter).build();
        assertSame(getter, positional.getter());
    }

    @Test
    public void testArgSpecGetterRethrowsPicocliException() {
        final CommandLine.PicocliException expected = new CommandLine.PicocliException("boom");
        IGetter getter = new IGetter() {
            public <T> T get() { throw expected; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().getter(getter).build();
        try {
            positional.getValue();
        } catch (CommandLine.PicocliException ex) {
            assertSame(expected, ex);
        }
    }

    @Test
    public void testArgSpecGetterWrapNonPicocliException() {
        final Exception expected = new Exception("boom");
        IGetter getter = new IGetter() {
            public <T> T get() throws Exception { throw expected; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().getter(getter).build();
        try {
            positional.getValue();
        } catch (CommandLine.PicocliException ex) {
            assertSame(expected, ex.getCause());
        }
    }

    @Test
    public void testArgSpecSetterRethrowsPicocliException() {
        final CommandLine.PicocliException expected = new CommandLine.PicocliException("boom");
        ISetter setter = new ISetter() {
            public <T> T set(T value) throws Exception { throw expected; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().setter(setter).build();
        try {
            positional.setValue("abc");
        } catch (CommandLine.PicocliException ex) {
            assertSame(expected, ex);
        }
    }

    @Test
    public void testArgSpecSetValueCallsSetter() {
        final Object[] newVal = new Object[1];
        ISetter setter = new ISetter() {
            public <T> T set(T value) { newVal[0] = value; return null; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().setter(setter).build();
        positional.setValue("abc");
        assertEquals("abc", newVal[0]);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testArgSpecSetValueWithCommandLineCallsSetter() {
        final Object[] newVal = new Object[1];
        ISetter setter = new ISetter() {
            public <T> T set(T value) { newVal[0] = value; return null; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().setter(setter).build();
        positional.setValue("abc", new CommandLine(CommandLine.Model.CommandSpec.create()));
        assertEquals("abc", newVal[0]);
    }

    @Test
    public void testArgSpecSetterWrapNonPicocliException() {
        final Exception expected = new Exception("boom");
        ISetter setter = new ISetter() {
            public <T> T set(T value) throws Exception { throw expected; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().setter(setter).build();
        try {
            positional.setValue("abc");
        } catch (CommandLine.PicocliException ex) {
            assertSame(expected, ex.getCause());
        }
    }

    @Test
    public void testArgSpecSetter2WrapNonPicocliException() {
        final Exception expected = new Exception("boom");
        ISetter setter = new ISetter() {
            public <T> T set(T value) throws Exception { throw expected; }
        };
        PositionalParamSpec positional = PositionalParamSpec.builder().setter(setter).build();
        try {
            positional.setValue("abc");
        } catch (CommandLine.PicocliException ex) {
            assertSame(expected, ex.getCause());
        }
    }

    @Test
    public void testArgSpecEquals() {
        PositionalParamSpec.Builder positional = PositionalParamSpec.builder()
                .arity("1")
                .hideParamSyntax(true)
                .required(true)
                .splitRegex(";")
                .description("desc")
                .descriptionKey("key")
                .auxiliaryTypes(Integer.class, Double.class);

        PositionalParamSpec p1 = positional.build();
        assertEquals(p1, p1);
        assertEquals(p1, positional.build());
        assertNotEquals(p1, positional.arity("2").build());
        assertNotEquals(p1, positional.arity("1").hideParamSyntax(false).build());
        assertNotEquals(p1, positional.hideParamSyntax(true).required(false).build());
        assertNotEquals(p1, positional.required(true).splitRegex(",").build());
        assertNotEquals(p1, positional.splitRegex(";").description("xyz").build());
        assertNotEquals(p1, positional.description("desc").descriptionKey("XX").build());
        assertNotEquals(p1, positional.descriptionKey("key").auxiliaryTypes(Short.class).build());
        assertEquals(p1, positional.auxiliaryTypes(Integer.class, Double.class).build());
    }

    @Test
    public void testArgSpecBuilderDescriptionKey() {
        PositionalParamSpec.Builder positional = PositionalParamSpec.builder()
                .descriptionKey("key");

        assertEquals("key", positional.descriptionKey());
        assertEquals("xxx", positional.descriptionKey("xxx").descriptionKey());
    }

    @Test
    public void testArgSpecBuilderHideParamSyntax() {
        PositionalParamSpec.Builder positional = PositionalParamSpec.builder()
                .hideParamSyntax(true);

        assertEquals(true, positional.hideParamSyntax());
        assertEquals(false, positional.hideParamSyntax(false).hideParamSyntax());
    }

    @Test
    public void testArgSpecBuilderHasInitialValue() {
        PositionalParamSpec.Builder positional = PositionalParamSpec.builder()
                .hasInitialValue(true);

        assertEquals(true, positional.hasInitialValue());
        assertEquals(false, positional.hasInitialValue(false).hasInitialValue());
    }

    @Test
    public void testArgSpecBuilderCompletionCandidates() {
        List<String> candidates = Arrays.asList("a", "b");
        PositionalParamSpec.Builder positional = PositionalParamSpec.builder()
                .completionCandidates(candidates);

        assertEquals(candidates, positional.completionCandidates());
    }
}
