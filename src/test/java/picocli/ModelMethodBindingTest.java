package picocli;

import org.junit.Test;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.MethodBinding;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.PicocliException;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class ModelMethodBindingTest {

    @Test
    public void testGetDoesNotInvokeMethod() throws Exception {
        Method getX = ModelMethodBindingBean.class.getDeclaredMethod("getX");
        MethodBinding binding = new MethodBinding(new ModelMethodBindingBean(), getX, CommandSpec.create());
        binding.get(); // no IllegalAccessException
    }

    @Test
    public void testGetReturnsNullForGetterMethod() throws Exception {
        Method getX = ModelMethodBindingBean.class.getDeclaredMethod("getX");
        getX.setAccessible(true);

        ModelMethodBindingBean bean = new ModelMethodBindingBean();
        MethodBinding binding = new MethodBinding(bean, getX, CommandSpec.create());
        assertNull(binding.get());
        assertEquals("actual value returned by getX() method", 7, bean.publicGetX());
    }

    @Test
    public void testSetInvokesMethod_FailsForGetterMethod() throws Exception {
        Method getX = ModelMethodBindingBean.class.getDeclaredMethod("getX");
        getX.setAccessible(true);

        ModelMethodBindingBean bean = new ModelMethodBindingBean();
        CommandSpec spec = CommandSpec.create();
        MethodBinding binding = new MethodBinding(bean, getX, spec);

        try {
            binding.set(41);
            fail("Expect exception");
        } catch (Exception ex) {
            ParameterException pex = (ParameterException) ex;
            assertSame(spec, pex.getCommandLine().getCommandSpec());
            assertThat(pex.getCause().getClass().toString(), pex.getCause() instanceof IllegalArgumentException);
            assertEquals("wrong number of arguments", pex.getCause().getMessage());
        }
    }

    @Test
    public void testGetReturnsLastSetValue_ForSetterMethod() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        ModelMethodBindingBean bean = new ModelMethodBindingBean();
        MethodBinding binding = new MethodBinding(bean, setX, CommandSpec.create());
        assertNull("initial", binding.get());
        assertEquals(7, bean.publicGetX());

        binding.set(41);
        assertEquals(41, bean.publicGetX());
        assertEquals(41, binding.get());
    }

    @Test
    public void testMethodMustBeAccessible() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        MethodBinding binding = new MethodBinding(new ModelMethodBindingBean(), setX, CommandSpec.create());
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
        MethodBinding binding = new MethodBinding(value, setX, CommandSpec.create());

        binding.set(987);
        assertEquals(987, value.publicGetX());
        assertEquals(987, binding.get());
    }

    @Test
    public void testSetFailsIfObjectNotSet_ForSetterMethod() throws Exception {
        Method setX = ModelMethodBindingBean.class.getDeclaredMethod("setX", int.class);
        setX.setAccessible(true);

        CommandSpec spec = CommandSpec.create();
        MethodBinding binding = new MethodBinding(null, setX, spec);

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
        MethodBinding binding = new MethodBinding(null, setX, spec);

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
        MethodBinding binding = new MethodBinding(null, setX, spec);

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
}
