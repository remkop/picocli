package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;

public class Issue1471 {
    public CommandLine getTestCommandLine(ParentTestCommand parentCommand, TestCommand testCommand, CommandLine.IFactory factory) {
        CommandLine commandLine = new CommandLine(parentCommand, factory);
        commandLine.addSubcommand(testCommand);
        return commandLine;
    }
    @Command(name = "parentTestCommand", mixinStandardHelpOptions = true, scope = INHERIT)
    static class ParentTestCommand {
    }

    @Command(name = "sorteotest")
    static class TestCommand {
        @Command(name = "entertest", description = "Start participating in a giveaway")
        void enter(@Parameters(arity = "1") String sentenceType,
                   @Parameters(arity = "1") String tweetUrl) {
            System.out.println("entering giveaway");
        }
    }

    @Ignore
    @Test
    public void testIssue1741() {
        CommandLine commandLine = getTestCommandLine(new ParentTestCommand(), new TestCommand(), CommandLine.defaultFactory());
        assertEquals(0, commandLine.execute("sorteotest", "entertest", "sequenceType", "url"));
    }

}
