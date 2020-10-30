package picocli;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This test is located in the `picocli-examples` module because it uses the Java 8
 * `java.util.Optional` API. (The main module requires only Java 5.)
 * <p>
 * See https://github.com/remkop/picocli/issues/1108 for background on this feature.
 * </p>
 */
public class OptionalTest {
    static class SingleOptions {
        @Option(names = "-x")
        Optional<Integer> x;

        @Option(names = "-y")
        Optional<Integer> y;

        @Option(names = {"-z", "--long"}, negatable = true)
        Optional<Boolean> z = Optional.of(Boolean.FALSE);

        @Parameters(arity = "0..*")
        Optional<String> positional = Optional.empty();
    }

    @Test
    public void testTypeInfo() {
        CommandSpec spec = CommandSpec.forAnnotatedObject(new SingleOptions());
        OptionSpec x = spec.findOption("-x");
        assertTrue(x.typeInfo().isOptional());
        assertEquals(Optional.class, x.typeInfo().getType());
        assertEquals(1, x.typeInfo().getAuxiliaryTypes().length);
        assertEquals(Integer.class, x.typeInfo().getAuxiliaryTypes()[0]);

        OptionSpec z = spec.findOption("-z");
        assertTrue(z.typeInfo().isOptional());
        assertEquals(Optional.class, z.typeInfo().getType());
        assertEquals(1, z.typeInfo().getAuxiliaryTypes().length);
        assertEquals(Boolean.class, z.typeInfo().getAuxiliaryTypes()[0]);
    }

    @Test
    public void testOptionalSingleOptions() {
        SingleOptions bean = CommandLine.populateCommand(new SingleOptions(),
                "-x=123", "last", "--no-long");
        assertEquals(Optional.of(123), bean.x);
        assertEquals(Optional.empty(), bean.y);
        assertEquals(Optional.of(false), bean.z);
        assertEquals(Optional.of("last"), bean.positional);
    }

    @Test
    public void testOptionalSingleOptionNegatable() {
        SingleOptions bean = CommandLine.populateCommand(new SingleOptions(),
                "--long");
        assertEquals(Optional.empty(), bean.x);
        assertEquals(Optional.empty(), bean.y);
        assertEquals(Optional.of(true), bean.z);
        assertEquals(Optional.empty(), bean.positional);
    }
}
