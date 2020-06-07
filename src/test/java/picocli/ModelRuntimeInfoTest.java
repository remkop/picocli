package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModelRuntimeInfoTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testRuntimeTypeInfoConstructor() {
        CommandLine.Model.RuntimeTypeInfo runtimeTypeInfo = new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{String.class}, null);
        assertEquals(Collections.emptyList(), runtimeTypeInfo.getActualGenericTypeArguments());
    }

    @Test
    public void testRuntimeTypeInfoIsArray() {
        CommandLine.Model.RuntimeTypeInfo runtimeTypeInfo = new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{String.class}, null);
        assertFalse(runtimeTypeInfo.isArray());

        assertTrue(new CommandLine.Model.RuntimeTypeInfo(String[].class, new Class[]{String[].class}, null).isArray());
    }

    @Test
    public void testRuntimeTypeInfoGetEnumConstantNames() {
        CommandLine.Model.RuntimeTypeInfo runtimeTypeInfo = new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{String.class}, null);
        assertFalse(runtimeTypeInfo.isEnum());

        assertEquals(Collections.emptyList(), runtimeTypeInfo.getEnumConstantNames());
    }

    @Test
    public void testRuntimeTypeInfoEquals() {
        CommandLine.Model.RuntimeTypeInfo runtimeTypeInfo = new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{String.class}, null);
        assertFalse(runtimeTypeInfo.equals("abc"));
        assertTrue(runtimeTypeInfo.equals(new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{String.class}, null)));

        assertFalse(runtimeTypeInfo.equals(new CommandLine.Model.RuntimeTypeInfo(String.class, new Class[]{String.class}, null)));
        assertFalse(runtimeTypeInfo.equals(new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{Integer.class}, null)));
    }

    @Test
    public void testRuntimeTypeToString() {
        CommandLine.Model.RuntimeTypeInfo runtimeTypeInfo = new CommandLine.Model.RuntimeTypeInfo(List.class, new Class[]{String.class}, null);
        String expected = "RuntimeTypeInfo(java.util.List, aux=[class java.lang.String], collection=true, map=false)";
        assertEquals(expected, runtimeTypeInfo.toString());
    }
}
