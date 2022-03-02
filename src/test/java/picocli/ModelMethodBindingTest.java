package picocli;

import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.MethodBinding;
import picocli.CommandLine.Model.ObjectScope;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.PicocliException;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

public class ModelMethodBindingTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testGetDoesNotInvokeMethod() throws Exception {
        Method getX = ModelMethodBindingBean.class.getDeclaredMethod("getX");
        MethodBinding binding = new MethodBinding(new ObjectScope(new ModelMethodBindingBean()), getX, CommandSpec.create());
        binding.get(); // no IllegalAccessException
    }

    @Test
    public void testGetReturnsNullForGetterMethod() throws Exception {
        Method getX = ModelMethodBindingBean.class.getDeclaredMethod("getX");
        getX.setAccessible(true);

        ModelMethodBindingBean bean = new ModelMethodBindingBean();
        MethodBinding binding = new MethodBinding(new ObjectScope(bean), getX, CommandSpec.create());
        assertNull(binding.get());
        assertEquals("actual value returned by getX() method", 7, bean.publicGetX());
    }

    @Test
    public void testSetInvokesMethod_FailsForGetterMethod() throws Exception {
        Method getX = ModelMethodBindingBean.class.getDeclaredMethod("getX");
        getX.setAccessible(true);

        ModelMethodBindingBean bean = new ModelMethodBindingBean();
        CommandSpec spec = CommandSpec.create();
        MethodBinding binding = new MethodBinding(new ObjectScope(bean), getX, spec);

        try {
            binding.set(41);
            fail("Expect exception");
        } catch (Exception ex) {
            ParameterException pex = (ParameterException) ex;
            assertSame(spec, pex.getCommandLine().getCommandSpec());
            assertThat(pex.getCause().getClass().toString(), pex.getCause() instanceof IllegalArgumentException);
            assertThat(pex.getCause().getMessage(), containsString("wrong number of arguments"));
        }
    }

    @Test
    public void testGetReturnsLastSetValue_ForSetterMethod() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        ModelMethodBindingBean bean = new ModelMethodBindingBean();
        MethodBinding binding = new MethodBinding(new ObjectScope(bean), setX, CommandSpec.create());
        assertNull("initial", binding.get());
        assertEquals(7, bean.publicGetX());

        binding.set(41);
        assertEquals(41, bean.publicGetX());
        assertEquals(Integer.valueOf(41), binding.get());
    }

    @Test
    public void testMethodMustBeAccessible() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        MethodBinding binding = new MethodBinding(new ObjectScope(new ModelMethodBindingBean()), setX, CommandSpec.create());
        try {
            binding.set(1);
            fail("Expected exception");
        } catch (PicocliException ok) {
            assertThat("not accessible", ok.getCause() instanceof IllegalAccessException);
        }
    }

    @Test
    public void testSetInvokesMethod_ForSetterMethod() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        ModelMethodBindingBean value = new ModelMethodBindingBean();
        MethodBinding binding = new MethodBinding(new ObjectScope(value), setX, CommandSpec.create());

        binding.set(987);
        assertEquals(987, value.publicGetX());
        assertEquals(Integer.valueOf(987), binding.get());
    }

    @Test
    public void testSetFailsIfObjectNotSet_ForSetterMethod() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        CommandSpec spec = CommandSpec.create();
        MethodBinding binding = new MethodBinding(new ObjectScope(null), setX, spec);

        try {
            binding.set(41);
            fail("Expect exception");
        } catch (Exception ex) {
            ParameterException pex = (ParameterException) ex;
            assertSame(spec, pex.getCommandLine().getCommandSpec());
            assertThat(pex.getCause().getClass().toString(), pex.getCause() instanceof NullPointerException);
        }
    }

    @Test
    public void testExceptionHandlingUsesCommandLineIfAvailable() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        CommandSpec spec = CommandSpec.create();
        CommandLine cmd = new CommandLine(spec);
        spec.commandLine(cmd);
        MethodBinding binding = new MethodBinding(new ObjectScope(null), setX, spec);

        try {
            binding.set(41);
            fail("Expect exception");
        } catch (Exception ex) {
            ParameterException pex = (ParameterException) ex;
            assertSame(cmd, pex.getCommandLine());
        }
    }

    @Test
    public void testExceptionHandlingCreatesCommandLineIfNecessary() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        CommandSpec spec = CommandSpec.create();
        assertNull(spec.commandLine());
        MethodBinding binding = new MethodBinding(new ObjectScope(null), setX, spec);

        try {
            binding.set(41);
            fail("Expect exception");
        } catch (Exception ex) {
            assertNotNull(spec.commandLine()); // has been set

            ParameterException pex = (ParameterException) ex;
            assertSame(pex.getCommandLine(), spec.commandLine());
            assertSame(spec, pex.getCommandLine().getCommandSpec());
        }
    }

    @Test
    public void testToString() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        ModelMethodBindingBean value = new ModelMethodBindingBean();
        MethodBinding binding = new MethodBinding(new ObjectScope(value), setX, CommandSpec.create());

        assertEquals("MethodBinding(private void picocli.ModelMethodBindingBean.setX(int))", binding.toString());
    }
}
