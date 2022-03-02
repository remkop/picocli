package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.FieldBinding;
import picocli.CommandLine.PicocliException;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.*;

public class ModelFieldBindingTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testFieldBindingDoesNotSetAccessible() throws Exception {
        Field f = ModelMethodBindingBean.class.getDeclaredField("x");
        FieldBinding binding = new FieldBinding(new ModelMethodBindingBean(), f);
        try {
            binding.get();
            fail("Expected exception");
        } catch (PicocliException ok) {
            assertThat("not accessible", ok.getCause() instanceof IllegalAccessException);
        }
    }

    @Test
    public void testFieldBindingGetterGivesValue() throws Exception {
        Field f = ModelMethodBindingBean.class.getDeclaredField("x");
        f.setAccessible(true);

        FieldBinding binding = new FieldBinding(new ModelMethodBindingBean(), f);
        assertEquals("initial value", Integer.valueOf(7), binding.get());
    }

    @Test
    public void testFieldBindingSetterModifiesValue() throws Exception {
        Field f = ModelMethodBindingBean.class.getDeclaredField("x");
        f.setAccessible(true);

        ModelMethodBindingBean value = new ModelMethodBindingBean();
        FieldBinding binding = new FieldBinding(value, f);

        binding.set(987);
        assertEquals(987, value.publicGetX());
        assertEquals(Integer.valueOf(987), binding.get());
    }

    @Test
    public void testFieldBindingToString() throws Exception {
        Field f = ModelMethodBindingBean.class.getDeclaredField("x");
        f.setAccessible(true);

        ModelMethodBindingBean value = new ModelMethodBindingBean();
        FieldBinding binding = new FieldBinding(value, f);

        assertEquals("FieldBinding(int picocli.ModelMethodBindingBean.x)", binding.toString());
    }
}
