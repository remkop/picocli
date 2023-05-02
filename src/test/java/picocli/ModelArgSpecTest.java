package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.IScope;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.ITypeInfo;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.OptionSpec.Builder;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.RuntimeTypeInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.junit.Assert.*;

public class ModelArgSpecTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testArgSpecConstructorWithEmptyAuxTypes() {
        PositionalParamSpec positional = PositionalParamSpec.builder().auxiliaryTypes(new Class[0]).build();
        assertEquals(CommandLine.Range.valueOf("1"), positional.arity());
        assertEquals(String.class, positional.type());
        assertArrayEquals(new Class[] {String.class}, positional.auxiliaryTypes());
    }

    @Test
    @Deprecated public void testArgSpecRenderedDescriptionInitial() {
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
        positional.setValue("abc", new CommandLine(CommandSpec.create()));
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
                .type(Map.class)
                .auxiliaryTypes(Integer.class, Double.class)
                .inherited(true)
                .scopeType(CommandLine.ScopeType.INHERIT);

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
        assertNotEquals(p1, positional.inherited(false).build());
        assertEquals(p1, positional.inherited(true).build());
        assertNotEquals(p1, positional.scopeType(CommandLine.ScopeType.LOCAL).build());
        assertEquals(p1, positional.scopeType(CommandLine.ScopeType.INHERIT).build());
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

    private static class AnnotatedImpl implements CommandLine.Model.IAnnotatedElement {
        Object userObject;
        String name;
        ITypeInfo typeInfo;

        AnnotatedImpl() {}
        AnnotatedImpl(String name, ITypeInfo typeInfo) {
            this(null, name, typeInfo);
        }
        AnnotatedImpl(Object userObject, String name, ITypeInfo typeInfo) {
            this.userObject = userObject;
            this.name = name;
            this.typeInfo = typeInfo;
        }

        public Object userObject() { return userObject; }
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return false;}
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) { return null;}
        public String getName() {return name;}
        public String getMixinName() {return null;}
        public boolean isArgSpec() {return false;}
        public boolean isOption() {return false;}
        public boolean isParameter() {return false;}
        public boolean isArgGroup() {return false;}
        public boolean isMixin() {return false;}
        public boolean isUnmatched() { return false;}
        public boolean isSpec() {return false;}
        public boolean isParentCommand() {return false;}
        public boolean isMultiValue() {return false;}
        public boolean isInteractive() {return false;}
        public boolean hasInitialValue() {return false;}
        public boolean isMethodParameter() {return false;}
        public int getMethodParamPosition() {return 0;}
        public IScope scope() {return null;}
        public IGetter getter() {return null;}
        public ISetter setter() {return null;}
        public ITypeInfo getTypeInfo() {return typeInfo;}
        public String getToString() {return null;}
    }

    @Test
    public void testArgSpecBuilderInferLabel() throws Exception{
        Method m = ArgSpec.Builder.class.getDeclaredMethod("inferLabel", String.class, CommandLine.Model.IAnnotatedElement.class);
        m.setAccessible(true);

        assertEquals("<String=String>", m.invoke(null, "",   new AnnotatedImpl("fieldName", typeInfo(new Class[0]))));
        assertEquals("<String=String>", m.invoke(null, "",   new AnnotatedImpl("fieldName", typeInfo(new Class[]{Integer.class}))));
        assertEquals("<String=String>", m.invoke(null, "",   new AnnotatedImpl("fieldName", typeInfo(new Class[]{null, Integer.class}))));
        assertEquals("<String=String>", m.invoke(null, "",   new AnnotatedImpl("fieldName", typeInfo(new Class[]{Integer.class, null}))));
        assertEquals("<Integer=Integer>", m.invoke(null, "", new AnnotatedImpl("fieldName", typeInfo(new Class[]{Integer.class, Integer.class}))));

        assertEquals("<prefix>", m.invoke(null, null, new AnnotatedImpl(m,"prefix$postfix", new TypeInfoAdapter())));

    }

    private ITypeInfo typeInfo(final Class<?>[] aux) {
        return new TypeInfoAdapter() {
            public boolean isMap() { return true; }
            public List<ITypeInfo> getAuxiliaryTypeInfos() {
                List<ITypeInfo> result = new ArrayList<ITypeInfo>();
                for (final Class<?> c : aux) {
                    if (c == null) { result.add(null); }
                    result.add(new TypeInfoAdapter() {
                        public String getClassSimpleName() { return c.getSimpleName(); }
                    });
                }
                return result;
            }
        };
    }
    static class TypeInfoAdapter implements ITypeInfo {
        public boolean isMap() { return false; }
        public List<ITypeInfo> getAuxiliaryTypeInfos() { return null; }
        public List<String> getActualGenericTypeArguments() { return null; }
        public boolean isBoolean() { return false; }
        public boolean isMultiValue() { return false; }
        public boolean isArray() { return false; }
        public boolean isCollection() { return false; }
        public boolean isOptional() { return false; }
        public boolean isEnum() { return false; }
        public List<String> getEnumConstantNames() { return null; }
        public String getClassName() { return null; }
        public String getClassSimpleName() { return null; }
        public Class<?> getType() { return null; }
        public Class<?>[] getAuxiliaryTypes() { return new Class[0]; }
    }

    @Test
    public void testArgSpecUserObject() {
        class App {
            @CommandLine.Parameters
            List<String> args;
            @CommandLine.Option(names = "-x") String x;
        }
        CommandLine cmd =  new CommandLine(new App());
        Object x = cmd.getCommandSpec().findOption("x").userObject();
        assertEquals(Field.class, x.getClass());
        assertEquals("x", ((Field) x).getName());

        Object args = cmd.getCommandSpec().positionalParameters().get(0).userObject();
        assertEquals(Field.class, args.getClass());
        assertEquals("args", ((Field) args).getName());
    }

    @Test
    public void testArgSpecBuilderUserObject() {
        assertNull(OptionSpec.builder("-x").userObject());
        assertEquals("aaa", OptionSpec.builder("-x").userObject("aaa").userObject());

        assertNull(PositionalParamSpec.builder().userObject());
        assertEquals("aaa", PositionalParamSpec.builder().userObject("aaa").userObject());
    }

    @Test
    public void testArgSpecBuilderTypeInfo() {
        ITypeInfo typeInfo = OptionSpec.builder("-x").typeInfo();
        assertNull(typeInfo);
    }

    @Test
    public void testArgSpecBuilderSetTypeInfo() {
        ITypeInfo typeInfo = new RuntimeTypeInfo(List.class, new Class[]{String.class}, Arrays.asList("boo"));
        Builder builder = OptionSpec.builder("-x").typeInfo(typeInfo);
        assertSame(typeInfo, builder.typeInfo());
        assertEquals(List.class, builder.type());
        assertArrayEquals(new Class[]{String.class}, builder.auxiliaryTypes());

        try {
            builder.typeInfo(null);
            fail("Expected NPE");
        } catch (NullPointerException ok) {
        }
    }

    @Test
    public void testArgSpecBuilderObjectBindingToString() {
        Builder builder = OptionSpec.builder("-x");
        assertEquals("ObjectBinding(value=null)", builder.getter().toString());
    }

    @Test(expected = NullPointerException.class)
    public void testOptionSpecBuilderSplitRegexDisallowsNull() {
        OptionSpec.builder("-x").splitRegex(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPositionalParamSpecBuilderSplitRegexDisallowsNull() {
        PositionalParamSpec.builder().splitRegex(null);
    }

    @Test(expected = NullPointerException.class)
    public void testOptionSpecBuilderSplitRegexSynopsisLabelDisallowsNull() {
        OptionSpec.builder("-x").splitRegexSynopsisLabel(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPositionalParamSpecBuilderSplitRegexSynopsisLabelDisallowsNull() {
        PositionalParamSpec.builder().splitRegexSynopsisLabel(null);
    }

    @Test
    public void testArgSpecBuilderSplitRegex() {
        Builder builder = OptionSpec.builder("-x").type(String[].class).splitRegex(";");
        assertEquals("split regex", ";", builder.splitRegex());
        assertEquals("split regex", ";", builder.build().splitRegex());

        PositionalParamSpec.Builder pb = PositionalParamSpec.builder().type(String[].class).splitRegex(";");
        assertEquals("split regex", ";", pb.splitRegex());
        assertEquals("split regex", ";", pb.build().splitRegex());
    }

    @Test
    public void testArgSpecBuilderSplitRegexSynopsisLabel() {
        Builder builder = OptionSpec.builder("-x").splitRegexSynopsisLabel(";");
        assertEquals("split regex synopsis label", ";", builder.splitRegexSynopsisLabel());
        assertEquals("split regex synopsis label", ";", builder.build().splitRegexSynopsisLabel());

        PositionalParamSpec.Builder pb = PositionalParamSpec.builder().splitRegexSynopsisLabel(";");
        assertEquals("split regex synopsis label", ";", pb.splitRegexSynopsisLabel());
        assertEquals("split regex synopsis label", ";", pb.build().splitRegexSynopsisLabel());
    }

    @Test
    public void testArgSpecBuilderSplitRegexAndSplitRegexSynopsisLabel() {
        Builder builder = OptionSpec.builder("-x").type(String[].class).splitRegex("#").splitRegexSynopsisLabel(";");
        assertEquals("split regex synopsis label", ";", builder.splitRegexSynopsisLabel());
        assertEquals("split regex synopsis label", ";", builder.build().splitRegexSynopsisLabel());

        PositionalParamSpec.Builder pb = PositionalParamSpec.builder().type(String[].class).splitRegex("#").splitRegexSynopsisLabel(";");
        assertEquals("split regex synopsis label", ";", pb.splitRegexSynopsisLabel());
        assertEquals("split regex synopsis label", ";", pb.build().splitRegexSynopsisLabel());
    }

    @Test
    public void testPositionalParamSpecBuilderCapacity() {
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
        assertNull(builder.capacity());

        CommandLine.Range value = CommandLine.Range.valueOf("1..2");
        builder.capacity(value);
        assertSame(value, builder.capacity());
    }

    @Test
    public void testArgSpec_defaultValueString() {
        PositionalParamSpec spec = PositionalParamSpec.builder().defaultValue("Hi ${user.name}").build();
        assertEquals("Hi ${user.name}", spec.defaultValueString());
    }

    @Test
    public void testArgSpecBuilder_parameterConsumerGetter() {
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
        assertNull(builder.parameterConsumer());
    }

    @Test
    public void testArgSpecBuilder_parameterConsumerSetter() {
        IParameterConsumer consumer = new IParameterConsumer() {
            public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
            }
        };
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder().parameterConsumer(consumer);
        assertSame(consumer, builder.parameterConsumer());
    }

    @Test
    public void testArgSpecBuilder_scopeGetter() {
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder();
        assertNotNull(builder.scope());
    }

    @Test
    public void testArgSpecBuilder_scopeSetter() {
        IScope scope = new IScope() {
            public <T> T get() { return null; }
            public <T> T set(T value) { return null; }
        };
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder().scope(scope);
        assertSame(scope, builder.scope());
    }
}
