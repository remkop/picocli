package picocli;


import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class Issue2341 {
    @CommandLine.Command
    static class TestCommand implements Callable<Integer> {
        //@CommandLine.Mixin MyMixIn myMixIn; // added programmatically in main

        public Integer call() throws Exception {
            return 0;
        }
    }

    static class MyMixIn {
        @CommandLine.ArgGroup(validate = false, heading = "Heading\n")
        private OptionGroup group;
    }

    public class OptionGroup {
        @CommandLine.Option(names = "--double", description = "This option is doubled")
        boolean option;
    }

    @Test
    public void testIssue2341() {
        TestCommand testCommand = new TestCommand();
        CommandLine commandLine = new CommandLine(testCommand);
        commandLine.addMixin("myMixin", new MyMixIn());
        String usageMessage = commandLine.getUsageMessage(CommandLine.Help.Ansi.OFF);

        String expected = String.format("" +
            "Usage: <main class> [--double]%n" +
            "Heading%n" +
            "      --double   This option is doubled%n");
        assertEquals(expected, usageMessage);
    }
}
