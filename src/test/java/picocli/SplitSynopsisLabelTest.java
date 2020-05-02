package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.junit.Test;

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

}