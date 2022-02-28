package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.UnmatchedArgsBinding;
import picocli.CommandLine.PicocliException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static picocli.TestUtil.setTraceLevel;

public class ModelUnmatchedArgsBindingTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testUnmatchedArgsBinding_GetterAndSetterBothNull() {
        try {
            UnmatchedArgsBinding.forStringArrayConsumer(null);
        } catch (IllegalArgumentException ex) {
            assertEquals("Getter and setter cannot both be null", ex.getMessage());
        }
    }

    @Test
    public void testUnmatchedArgsBinding_forStringArrayConsumer() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class ArrayBinding implements ISetter {
            String[] array;
            @SuppressWarnings("unchecked") public <T> T set(T value) {
                T old = (T) array;
                array = (String[]) value;
                return old;
            }
        }
        ArrayBinding setter = new ArrayBinding();
        CommandSpec cmd = CommandSpec.create();
        UnmatchedArgsBinding unmatched = UnmatchedArgsBinding.forStringArrayConsumer(setter);
        assertSame(setter, unmatched.setter());

        cmd.addUnmatchedArgsBinding(unmatched);
        cmd.addOption(CommandLine.Model.OptionSpec.builder("-x").build());
        CommandLine.ParseResult result = new CommandLine(cmd).parseArgs("-x", "a", "b", "c");

        assertEquals(Arrays.asList("a", "b", "c"), result.unmatched());
        assertArrayEquals(new String[]{"a", "b", "c"}, setter.array);
        assertSame(unmatched, cmd.unmatchedArgsBindings().get(0));
        assertEquals(1, cmd.unmatchedArgsBindings().size());
    }

    @Test
    public void testUnmatchedArgsBinding_forStringCollectionSupplier() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class ArrayBinding implements IGetter {
            List<String> list = new ArrayList<String>();
            @SuppressWarnings("unchecked") public <T> T get() {
                return (T) list;
            }
        }
        ArrayBinding binding = new ArrayBinding();
        CommandSpec cmd = CommandSpec.create();
        UnmatchedArgsBinding unmatched = UnmatchedArgsBinding.forStringCollectionSupplier(binding);
        assertSame(binding, unmatched.getter());

        cmd.addUnmatchedArgsBinding(unmatched);
        cmd.addOption(CommandLine.Model.OptionSpec.builder("-x").build());
        CommandLine.ParseResult result = new CommandLine(cmd).parseArgs("-x", "a", "b", "c");

        assertEquals(Arrays.asList("a", "b", "c"), result.unmatched());
        assertEquals(Arrays.asList("a", "b", "c"), binding.list);
        assertSame(unmatched, cmd.unmatchedArgsBindings().get(0));
        assertEquals(1, cmd.unmatchedArgsBindings().size());
    }

    @Test
    public void testUnmatchedArgsBinding_forStringArrayConsumer_withInvalidBinding() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class ListBinding implements ISetter {
            List<String> list = new ArrayList<String>();
            @SuppressWarnings("unchecked") public <T> T set(T value) {
                T old = (T) list;
                list = (List<String>) value;
                return old;
            }
        }
        CommandSpec cmd = CommandSpec.create();
        cmd.addUnmatchedArgsBinding(UnmatchedArgsBinding.forStringArrayConsumer(new ListBinding()));
        try {
            new CommandLine(cmd).parseArgs("-x", "a", "b", "c");
        } catch (Exception ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Could not invoke setter ("));
            assertTrue(ex.getMessage(), ex.getMessage().contains("with unmatched argument array '[-x, a, b, c]': java.lang.ClassCastException"));
        }
    }

    @Test
    public void testUnmatchedArgsBinding_forStringCollectionSupplier_withInvalidBinding() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        class ListBinding implements IGetter {
            @SuppressWarnings("unchecked") public <T> T get() {
                return (T) new Object();
            }
        }
        CommandSpec cmd = CommandSpec.create();
        cmd.addUnmatchedArgsBinding(UnmatchedArgsBinding.forStringCollectionSupplier(new ListBinding()));
        try {
            new CommandLine(cmd).parseArgs("-x", "a", "b", "c");
        } catch (Exception ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Could not add unmatched argument array '[-x, a, b, c]' to collection returned by getter ("));
            assertTrue(ex.getMessage(), ex.getMessage().contains("): java.lang.ClassCastException: "));
            assertTrue(ex.getMessage(), ex.getMessage().contains("java.lang.Object"));
        }
    }

    @Test
    public void testUnmatchedArgsBinding_forStringCollectionSupplier_exceptionsRethrownAsPicocliException() {
        class ThrowingGetter implements IGetter {
            public <T> T get() { throw new RuntimeException("test"); }
        }
        try {
            UnmatchedArgsBinding.forStringCollectionSupplier(new ThrowingGetter()).addAll(new String[0]);
            fail("Expected exception");
        } catch (PicocliException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Could not add unmatched argument array '[]' to collection returned by getter ("));
            assertTrue(ex.getMessage(), ex.getMessage().endsWith("): java.lang.RuntimeException: test"));
        }
    }

    @Test
    public void testUnmatchedArgsBinding_forStringArrayConsumer_exceptionsRethrownAsPicocliException() {
        class ThrowingSetter implements ISetter {
            public <T> T set(T value) { throw new RuntimeException("test"); }
        }
        try {
            UnmatchedArgsBinding.forStringArrayConsumer(new ThrowingSetter()).addAll(new String[0]);
            fail("Expected exception");
        } catch (PicocliException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Could not invoke setter "));
            assertTrue(ex.getMessage(), ex.getMessage().contains(") with unmatched argument array '[]': java.lang.RuntimeException: test"));
        }
    }
}
