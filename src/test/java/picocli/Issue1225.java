package picocli;

import org.junit.Test;
import picocli.CommandLine;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class Issue1225 {

    @Command(name = "app", mixinStandardHelpOptions = true, version = "app 1.0",
            description = "....")
    static class App implements Callable<Integer> {

        @Parameters(index = "0", description = "...")
        private String arg1;

        @Parameters(index = "1", description = "...", defaultValue="")
        private String arg2;

        public Integer call() throws Exception {
            return 0;
        }
        public static void main(String[] args) {
            args = new String[] {"sdf", "sf", "sdf2" };
            CommandLine cmd = new CommandLine(new App());
            cmd.execute(args);

            args = new String[] {"a", "b", "c" };
            cmd.execute(args);
        }
    }

    @Test
    public void testIssue1225() {
        CommandLine cmd = new CommandLine(new App());

        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("x", "y", "z"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched argument at index 2: 'z'"));

        sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("a", "b", "c"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched argument at index 2: 'c'"));
    }

    @Test
    public void testIssue1225DifferentInstance() {
        CommandLine cmd = new CommandLine(new App());

        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("x", "y", "z"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched argument at index 2: 'z'"));

        sw = new StringWriter();
        cmd = new CommandLine(new App());
        cmd.setErr(new PrintWriter(sw));
        assertEquals(2, cmd.execute("a", "b", "c"));
        assertTrue(sw.toString(), sw.toString().startsWith("Unmatched argument at index 2: 'c'"));
    }
}
