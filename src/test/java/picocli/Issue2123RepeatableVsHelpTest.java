package picocli;

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

    private final CommandLine commandLine = new CommandLine(new Tool())
            .addSubcommand(new Subcommand())
            .addSubcommand(new HelpCommand())
            .setOut(new PrintWriter(out))
            .setErr(new PrintWriter(out));

    @Test
    public void testToolHelpShowsToolUsage() {
        final int result = commandLine.execute("help");

        assertThat(out, hasToString(containsString("Usage: tool [-hV]")));
        assertThat(result, equalTo(OK));
    }

    @Test
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
    public void testToolSubHelpShowsToolSubUsage() {
        final int result = commandLine.execute("sub", "help");

        // With subcommandsRepeatable = false, "help" wasn't allowed so returns USAGE showing usage for "sub"
        // With subcommandsRepeatable = true, "help" is processed alone so returns OK showing usage for "tool"
        // It would be helpful to note that "sub" is the latest command in use so return OK showing usage for "sub"
        assertThat(out, hasToString(containsString("Usage: tool [-hV]")));
        assertThat(result, equalTo(OK));
    }

    @Test
    public void testToolSubDashHShowsToolSubUsage() {
        final int result = commandLine.execute("sub", "-h");

        assertThat(out, hasToString(containsString("Usage: tool sub")));
        assertThat(result, equalTo(OK));
    }
}
