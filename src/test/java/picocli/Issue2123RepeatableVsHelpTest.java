package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.object.HasToString.hasToString;
import static picocli.CommandLine.ExitCode.OK;
import static picocli.CommandLine.ExitCode.USAGE;

public class Issue2123RepeatableVsHelpTest {

    @Command(name = "tool", mixinStandardHelpOptions = true, subcommandsRepeatable = true)
    static class Tool {
    }

    @Command(name = "sub", mixinStandardHelpOptions = true)
    static class Subcommand implements Runnable {
        @Override
        public void run() {
        }
    }

    private final StringWriter out = new StringWriter();

    private final CommandLine commandLine = new CommandLine(new Tool()).addSubcommand(new Subcommand()).addSubcommand(new HelpCommand()).setOut(new PrintWriter(out)).setErr(new PrintWriter(out));

    @Test
    public void testToolHelpShowsToolUsage() {
        final int result = commandLine.execute("help");

        assertThat(out, hasToString(containsString("Usage: tool [-hV]")));
        assertThat(result, equalTo(OK));
    }

    @Test
    @Ignore("This fails when subcommandsRepeatable = true")
    public void testToolHelpSubShowsToolSubUsage() {
        final int result = commandLine.execute("help", "sub");

        assertThat(out, hasToString(containsString("Usage: tool sub")));
        assertThat(result, equalTo(OK));
    }

    @Test
    public void testToolDashHSubShowsToolSubUsage() {
        final int result = commandLine.execute("-h", "sub");

        assertThat(out, hasToString(containsString("Usage: tool [-hV]")));
        assertThat(result, equalTo(OK));
    }

    @Test
    @Ignore("This fails when subcommandsRepeatable = true")
    public void testToolSubHelpShowsToolSubUsage() {
        final int result = commandLine.execute("sub", "help");

        // Previously "help" wasn't allowed and return USAGE showing usage for "sub"
        // Arguably HelpCommand should see that "sub" is in use and return OK showing usage for "sub"
        assertThat(out, hasToString(containsString("Usage: tool sub")));
        assertThat(result, equalTo(USAGE));
    }

    @Test
    public void testToolSubDashHShowsToolSubUsage() {
        final int result = commandLine.execute("sub", "-h");

        assertThat(out, hasToString(containsString("Usage: tool sub")));
        assertThat(result, equalTo(OK));
    }
}
