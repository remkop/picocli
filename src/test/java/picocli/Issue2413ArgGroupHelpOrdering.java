package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

/**
 * Nested Arggroup options are printed in the Outer Arggroup when Nested is ordered after Outer
 */
public class Issue2413ArgGroupHelpOrdering {
    @Command(name = "test", sortOptions = false)
    static class Test {
        @ArgGroup(exclusive = false, heading = "Outer group%n", order = 10)
        private Outer outer;

        private static class Outer {
            @Option(names = "--outer-opt", description = "Outer option")
            private String oo;

            @ArgGroup(exclusive = false, heading = "Nested group%n", order = 20)
            private Nested nested;
        }

        private static class Nested {
            @Option(names = "--nested-opt", description = "Nested option")
            private String no;
        }
    }

    @org.junit.Test
    public void testUsage() {
        String expected = String.format("" +
            "Usage: test [[--outer-opt=<oo>] [[--nested-opt=<no>]]]%n" +
            "Outer group%n" +
            "      --outer-opt=<oo>    Outer option%n" +
            "Nested group%n" +
            "      --nested-opt=<no>   Nested option%n");
        String actual = new CommandLine(new Test()).getUsageMessage(CommandLine.Help.Ansi.OFF);

        assertEquals(expected, actual);
    }
}
