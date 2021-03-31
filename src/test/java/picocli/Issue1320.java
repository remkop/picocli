package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class Issue1320 {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @Command(name = "test")
    static class TestCommand implements Runnable {

        @Parameters
        String text;

        @Spec
        CommandSpec spec;

        //@Override
        public void run() {
            spec.commandLine().getOut().print(text);
            spec.commandLine().getOut().flush();
            spec.commandLine().getErr().print(text);
            spec.commandLine().getErr().flush();
        }
    }

    @Test
    public void testIssue1320() {
        String unmappable  = "[abcµ]";
        String alt1 = "[abc\u00B5]";
        String alt2 = "[abc\u03BC]";

        resetLogs();
        System.clearProperty(SUN_STDOUT_ENCODING);
        System.clearProperty(SUN_STDERR_ENCODING);
        fixLogPrintStream(Charset.defaultCharset().name());
        assertEquals(CommandLine.ExitCode.OK, new CommandLine(new TestCommand()).execute(unmappable));
//        assertEquals(systemOutRule.getLog(), unmappable, systemOutRule.getLog());
//        assertEquals(systemErrRule.getLog(), unmappable, systemErrRule.getLog());
        assertTrue(systemOutRule.getLog(), alt1.equals(systemOutRule.getLog()) || alt2.equals(systemOutRule.getLog()));
        assertTrue(systemErrRule.getLog(), alt1.equals(systemErrRule.getLog()) || alt2.equals(systemErrRule.getLog()));

        resetLogs();
        System.setProperty(SUN_STDOUT_ENCODING, CP_437);
        System.setProperty(SUN_STDERR_ENCODING, CP_437);
        fixLogPrintStream(CP_437);
        assertEquals(CommandLine.ExitCode.OK, new CommandLine(new TestCommand()).execute(unmappable));
        assertEquals(unmappable, systemOutRule.getLog());
        assertEquals(unmappable, systemErrRule.getLog());
    }

    private void resetLogs() {
        systemOutRule.clearLog();
        systemErrRule.clearLog();
    }

    private void fixLogPrintStream(String encoding) {
        System.setProperty("file.encoding", encoding);
    }

    private static final String CP_437 = "cp437";
    private static final String SUN_STDOUT_ENCODING = "sun.stdout.encoding";
    private static final String SUN_STDERR_ENCODING = "sun.stderr.encoding";
}
