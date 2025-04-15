package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


import static org.junit.Assert.assertEquals;

public class Issue2309 {
    @Test
    public void testIssue2309OptionsForArgGroupInMixinsAreNotDuplicatedInHelp() {
        String output = new CommandLine(new Issue2309MutuallyExclusiveOptionsDemo()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        String expected = String.format(
            "Usage: exclusivedemo [-hV] (-a=<a> | -b=<b> | -c=<c>)%n" +
                "  -a=<a>          Use A.%n" +
                "  -b=<b>          Use B.%n" +
                "  -c=<c>          Use C.%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");

        assertEquals(expected, output);
    }

    @Command
    static class Issue2309MyMixin {
        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        class Exclusive {
            @Option(names = "-a", required = true, description = "Use A.")
            int a;
            @Option(names = "-b", required = true, description = "Use B.")
            int b;
            @Option(names = "-c", required = true, description = "Use C.")
            int c;
        }
    }

    @Command(mixinStandardHelpOptions = true, name = "exclusivedemo")
    class Issue2309MutuallyExclusiveOptionsDemo {
        @CommandLine.Mixin
        Issue2309MyMixin mixin;
    }
}
