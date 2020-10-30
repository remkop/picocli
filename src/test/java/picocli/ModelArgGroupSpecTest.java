package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.IScope;
import picocli.CommandLine.Model.ISetter;

import java.util.List;

import static org.junit.Assert.*;

public class ModelArgGroupSpecTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testArgGroupSpecBuilder_typeInfoGetter() { //9152
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertNull(builder.typeInfo());
    }

    @Test
    public void testArgGroupSpecBuilder_typeInfoSetter() {
        CommandLine.Model.ITypeInfo typeInfo = new CommandLine.Model.ITypeInfo() {
            public boolean isBoolean() { return false; }
            public boolean isMultiValue() { return false; }
            public boolean isArray() { return false; }
            public boolean isCollection() { return false; }
            public boolean isMap() { return false; }
            public boolean isEnum() { return false; }
            public boolean isOptional() { return false; }
            public List<String> getEnumConstantNames() { return null; }
            public String getClassName() { return null; }
            public String getClassSimpleName() { return null; }
            public List<CommandLine.Model.ITypeInfo> getAuxiliaryTypeInfos() { return null; }
            public List<String> getActualGenericTypeArguments() { return null; }
            public Class<?> getType() { return null; }
            public Class<?>[] getAuxiliaryTypes() { return new Class[0]; }
        };
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder().typeInfo(typeInfo);
        assertSame(typeInfo, builder.typeInfo());
    }

    @Test
    public void testArgGroupSpecBuilder_getterGetter() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertNull(builder.getter());
    }

    @Test
    public void testArgGroupSpecBuilder_getterSetter() {
        IGetter getter = new IGetter() {
            public <T> T get() { return null; }
        };
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder().getter(getter);
        assertSame(getter, builder.getter());
    }

    @Test
    public void testArgGroupSpecBuilder_setterGetter() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertNull(builder.setter());
    }

    @Test
    public void testArgGroupSpecBuilder_setterSetter() {
        ISetter setter = new ISetter() {
            public <T> T set(T value) { return null; }
        };
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder().setter(setter);
        assertSame(setter, builder.setter());
    }

    @Test
    public void testArgGroupSpecBuilder_scopeGetter() {
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder();
        assertNull(builder.scope());
    }

    @Test
    public void testArgGroupSpecBuilder_scopeSetter() {
        IScope scope = new IScope() {
            public <T> T get() { return null; }
            public <T> T set(T value) { return null; }
        };
        ArgGroupSpec.Builder builder = ArgGroupSpec.builder().scope(scope);
        assertSame(scope, builder.scope());
    }
}
