package picocli;

import org.junit.Test;
import picocli.CommandLine.Model.FieldBinding;
import picocli.CommandLine.PicocliException;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.*;

public class ModelFieldBindingTest {
    static class WithField {
        private int x = 23;
    }

    @Test
    public void testFieldBindingDoesNotSetAccessible() throws Exception {
        Field f = WithField.class.getDeclaredField("x");
        FieldBinding binding = new FieldBinding(new WithField(), f);
        try {
            binding.get();
            fail("Expected exception");
        } catch (PicocliException ok) {
            assertThat("not accessible", ok.getCause() instanceof IllegalAccessException);
        }
    }

    @Test
    public void testFieldBindingGetterGivesValue() throws Exception {
        Field f = WithField.class.getDeclaredField("x");
        f.setAccessible(true);

        FieldBinding binding = new FieldBinding(new WithField(), f);
        assertEquals(23, binding.get());
    }

    @Test
    public void testFieldBindingSetterModifiesValue() throws Exception {
        Field f = WithField.class.getDeclaredField("x");
        f.setAccessible(true);

        WithField value = new WithField();
        FieldBinding binding = new FieldBinding(value, f);

        binding.set(987);
        assertEquals(987, value.x);
        assertEquals(987, binding.get());
    }
}
