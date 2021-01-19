package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class Issue1309 {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "test", mixinStandardHelpOptions = true)
    static class TestCommand implements Callable<Integer> {

        @Spec
        CommandSpec spec;

        public Integer call() throws Exception {
            throw new ParameterException(spec.commandLine(), "Missing required subcommand");
        }

        @Command(name = "start", description = "start", mixinStandardHelpOptions = true)
        public int start(
                @Parameters(index = "0", paramLabel = "id") String id) {
            System.out.printf("start was called with %s%n", id);
            return 111;
        }

        @Command(name = "restart", description = "restart", mixinStandardHelpOptions = true)
        public int restart(
                @Parameters(index = "0", paramLabel = "id") String id) {
            System.out.printf("restart was called with %s%n", id);
            return 222;
        }

    }

    @Test
    public void testIssue1309() {
        assertEquals(222, new CommandLine(new TestCommand()).execute("restart", "rest"));
        assertEquals(111, new CommandLine(new TestCommand()).execute("start", "sta"));
    }
}
