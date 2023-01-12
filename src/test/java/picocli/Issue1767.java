package picocli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static picocli.CommandLine.ScopeType.INHERIT;

import java.util.concurrent.Callable;

import org.junit.Test;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

public class Issue1767 {
    @Command(name = "test")
    static class TestCommand implements Callable<Integer> {
        @ArgGroup TestArgGroup testArgGroup;
        @Spec CommandSpec spec;

        public static class TestArgGroup {
            @Option(names = "-r")
            public Integer option1;
        }

        public Integer call() throws Exception {
            Integer value = spec.options().get(0).getValue();
            return value==null ? 0 : value;
        }
    }

    @Test
    public void testIssue1772() {
        assertEquals(5, new CommandLine(new TestCommand()).execute("-r", "5"));
        assertEquals(0, new CommandLine(new TestCommand()).execute());
    }
}
