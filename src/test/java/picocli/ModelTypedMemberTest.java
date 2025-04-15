package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class ModelTypedMemberTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Ignore("No longer expected to fail after [#1396][#1401]")
    @Test
    public void testInferTypes() {
        class App {
            @CommandLine.Parameters
            List<Class<? extends Class<? extends String>[]>> list;
        }
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (CommandLine.InitializationException ex) {
            String msg = "Unsupported generic type java.util.List<java.lang.Class<? extends java.lang.Class<? extends java.lang.String>[]>>. Only List<T>, Map<K,V>, Optional<T>, and Map<K, Optional<V>> are supported. Type parameters may be char[], a non-array type, or a wildcard type with an upper or lower bound.";
            assertEquals(msg, ex.getMessage());
        }
    }

    @Test
    public void testTypedMemberGetAuxiliaryTypes() throws Exception {
        class App {
            @CommandLine.Option(names = "-x") public char x;
        }
        Field f = App.class.getDeclaredField("x");
        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(f);
        assertArrayEquals(new Class[]{char.class}, typedMember.getAuxiliaryTypes());

        assertEquals(-1, typedMember.getMethodParamPosition());
    }

    @Test
    public void testTypedMemberGetMethodParamPosition() throws Exception {
        class App {
            @CommandLine.Command
            public void mymethod(@CommandLine.Option(names = "-x") char x) {}
        }
        Method method = App.class.getDeclaredMethod("mymethod", char.class);
        CommandLine.Model.MethodParam param = new CommandLine.Model.MethodParam(method, 0);

        CommandLine.Model.TypedMember typedMember = new CommandLine.Model.TypedMember(param, new CommandLine.Model.ObjectScope(new App()));
        assertEquals(0, typedMember.getMethodParamPosition());
    }
}
