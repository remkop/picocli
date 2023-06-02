package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;

public class Issue2029 {
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @CommandLine.Command(name = "test")
    static class TestCommand implements Runnable {

        @CommandLine.Parameters
        String text;

        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        //@Override
        public void run() {
            spec.commandLine().getOut().print(text);
            spec.commandLine().getOut().flush();
            spec.commandLine().getErr().print(text);
            spec.commandLine().getErr().flush();
        }
    }

    @Test
    public void invalidEncodingFallsbackToDefaultEncoding() {
        resetLogs();
        System.setProperty("sun.stdout.encoding", "cp0");
        System.setProperty("sun.stdout.encoding", "cp0");

        assertEquals(CommandLine.ExitCode.OK, new CommandLine(new Issue1320.TestCommand()).execute("test"));
        assertEquals("test", systemOutRule.getLog());
        assertEquals("test", systemErrRule.getLog());
    }

    private void resetLogs() {
        systemOutRule.clearLog();
        systemErrRule.clearLog();
    }
}
