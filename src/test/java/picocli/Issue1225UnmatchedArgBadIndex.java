package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class Issue1225UnmatchedArgBadIndex {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");


    @Command(name = "app", mixinStandardHelpOptions = true, version = "app 1.0",
            description = "....")
    static class App implements Callable<Integer> {

        @Parameters(index = "0", description = "...")
        private String arg1;

        @Parameters(index = "1", description = "...", defaultValue="")
        private String arg2;

        public Integer call()  {
            return 0;
        }
        public static void main(String[] args) {
            args = new String[] {"sdf", "sf", "sdf" };
            CommandLine cmd = new CommandLine(new App());
            cmd.execute(args);

            args = new String[] {"a", "b", "a" };
            cmd.execute(args);
        }
    }

    @Test
    public void testIssue1225() {
        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        CommandLine cmd = new CommandLine(new App());

        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("x", "y", "x"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched argument at index 2: 'x'"));

        sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("a", "b", "c"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched argument at index 2: 'c'"));
    }

    @Test
    public void testIssue1225Longer() {
        CommandLine cmd = new CommandLine(new App());

        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("x", "y", "x", "x"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched arguments from index 2: 'x', 'x'"));
    }
}
