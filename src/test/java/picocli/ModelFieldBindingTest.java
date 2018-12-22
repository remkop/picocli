package picocli;

import org.junit.Test;
import picocli.CommandLine.Model.FieldBinding;
import picocli.CommandLine.PicocliException;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.*;

public class ModelFieldBindingTest {

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
        assertEquals("initial value", 7, binding.get());
    }

    @Test
    public void testFieldBindingSetterModifiesValue() throws Exception {
        Field f = ModelMethodBindingBean.class.getDeclaredField("x");
        f.setAccessible(true);

        ModelMethodBindingBean value = new ModelMethodBindingBean();
        FieldBinding binding = new FieldBinding(value, f);

        binding.set(987);
        assertEquals(987, value.publicGetX());
        assertEquals(987, binding.get());
    }
}
