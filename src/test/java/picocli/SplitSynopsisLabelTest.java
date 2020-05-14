package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.junit.Test;
import picocli.CommandLine.Parameters;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.Help.Ansi.OFF;

public class SplitSynopsisLabelTest {
    @Test
    public void testSimple() {
        @Command(name = "UsageSplit")
        class Simple {
            @Option(names = "v", split = "\\|", splitSynopsisLabel = "|")
            String args[] = {};
        }

        String expected = String.format("" +
            "Usage: UsageSplit [v=<args>[|<args>...]]...%n" +
            "      v=<args>[|<args>...]%n");
        String actual = new CommandLine(new Simple()).getUsageMessage(OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testOptionWithoutSplit() {
        @Command(name = "WithoutSplit")
        class WithoutSplit {
            @Option(names = "v", splitSynopsisLabel = "|", description = "xxx yyy zzz")
            String args[] = {};
        }

        String expected = String.format("" +
                "Usage: WithoutSplit [v=<args>]...%n" +
                "      v=<args>   xxx yyy zzz%n");
        String actual = new CommandLine(new WithoutSplit()).getUsageMessage(OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testParameters() {
        @Command(name = "cmd")
        class Simple {
            @Parameters(split = "\\|", splitSynopsisLabel = "|",
                    paramLabel = "VAL", description = "This is the description.")
            List<String> list;
        }

        String expected = String.format("" +
                "Usage: cmd [VAL[|VAL...]...]%n" +
                "      [VAL[|VAL...]...]   This is the description.%n");
        String actual = new CommandLine(new Simple()).getUsageMessage(OFF);
        assertEquals(expected, actual);

    }
}