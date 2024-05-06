package picocli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

public class Issue2270 {
    static class CmdObjectBoolean {
        @Option(names = "--test-Boolean", defaultValue = "dHJ1ZQ==" /* base64-encoded 'true' */)
        Boolean testBoolean;
    }
    static class CmdPrimitiveBoolean {
        @Option(names = "--test-boolean", defaultValue = "dHJ1ZQ==" /* base64-encoded 'true' */)
        boolean testboolean;
    }

    static class Base64BooleanTypeConverter implements ITypeConverter<Boolean> {

        static List<String> invocations = new ArrayList<>();

        public Boolean convert(String value) {
            invocations.add(value);
            System.out.printf("converter invocation %s: called with value %s%n",
                    invocations.size(), value);
            if (Boolean.parseBoolean(value)) {
                return true;
            }
            return Boolean.parseBoolean(new String(Base64.getDecoder().decode(value)));
        }
    }

    @Before
    public void beforeTests() {
        CommandLine.tracer().setLevel(CommandLine.TraceLevel.DEBUG);
    }

    @After
    public void afterTests() {
        CommandLine.tracer().setLevel(CommandLine.TraceLevel.WARN);
    }

    @Test
    public void testObjectBoolean() {
        Base64BooleanTypeConverter.invocations.clear();
        CmdObjectBoolean cmd = new CmdObjectBoolean();
        new CommandLine(cmd)
            .registerConverter(Boolean.class, new Base64BooleanTypeConverter())
            .parseArgs();
        assertThat(Base64BooleanTypeConverter.invocations.size(), greaterThanOrEqualTo(1));
        assertEquals(Arrays.asList("dHJ1ZQ==", "true"), Base64BooleanTypeConverter.invocations);

        assertTrue(cmd.testBoolean);
    }

    @Test
    public void testPrimitiveBoolean() {
        Base64BooleanTypeConverter.invocations.clear();
        CmdPrimitiveBoolean cmd = new CmdPrimitiveBoolean();
        new CommandLine(cmd)
            .registerConverter(Boolean.TYPE, new Base64BooleanTypeConverter())
            .parseArgs();
        assertThat(Base64BooleanTypeConverter.invocations.size(), greaterThanOrEqualTo(1));
        assertEquals(Arrays.asList("dHJ1ZQ==", "true"), Base64BooleanTypeConverter.invocations);

        assertTrue(cmd.testboolean);
    }
}
