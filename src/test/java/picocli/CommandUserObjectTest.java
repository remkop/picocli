package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.CommandUserObject;
import picocli.CommandLine.Option;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class CommandUserObjectTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    // CommandUserObject tests
    @Test
    public void testCommandSpecWrapWithoutInspection() {
        String userObject = "hello";
        CommandSpec spec = CommandSpec.wrapWithoutInspection(userObject, CommandLine.defaultFactory());
        assertSame(userObject, spec.userObject());
    }

    @Test
    public void testCreate() {
        String userObject = "hello";
        CommandUserObject cuo = CommandUserObject.create(userObject, CommandLine.defaultFactory());
        assertEquals(String.class, cuo.getType());
        assertSame(userObject, cuo.getInstance());
        assertSame(userObject, cuo.get());
        assertFalse("not a method", cuo.isMethod());
        assertFalse("not a proxy", cuo.isProxyClass());
    }

    static class CmdMethodApp {
        @CommandLine.Command public void hello() {}
    }
    @Test
    public void testIsMethod() {
        List<Method> commandMethods = CommandLine.getCommandMethods(CmdMethodApp.class, null);
        CommandUserObject cuo = CommandUserObject.create(commandMethods.get(0), CommandLine.defaultFactory());
        assertNull(cuo.getType()); // TODO javadoc
        assertSame(commandMethods.get(0), cuo.getInstance());
        assertSame(commandMethods.get(0), cuo.get());
        assertTrue("is a method", cuo.isMethod());
        assertFalse("not a proxy", cuo.isProxyClass());
    }

    static interface ProxyApp {
        @Option(names = "--int") int getInt();
    }
    @Test
    public void testIsProxyFromClass() {
        CommandUserObject cuo = CommandUserObject.create(ProxyApp.class, CommandLine.defaultFactory());
        assertSame(ProxyApp.class, cuo.getType());
        assertFalse("is a method", cuo.isMethod());
        assertTrue("not a proxy", cuo.isProxyClass());

        Object instance = cuo.getInstance();
        assertTrue(instance instanceof ProxyApp);

        ProxyApp got = cuo.get();
        assertSame(instance, got);
    }

    @Test
    public void testCopy() {
        String userObject = "hello";
        CommandUserObject cuo = CommandUserObject.create(userObject, CommandLine.defaultFactory());
        CommandUserObject copy = cuo.copy(); // FIXME remove this method?
        assertEquals(String.class, copy.getType());
        assertEquals("", copy.getInstance());
        assertEquals("", copy.get());
        assertFalse("not a method", copy.isMethod());
        assertFalse("not a proxy", copy.isProxyClass());
    }

}

