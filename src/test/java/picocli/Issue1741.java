package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;

public class Issue1741 {
    public CommandLine getTestCommandLine(ParentTestCommand parentCommand, TestCommand testCommand, CommandLine.IFactory factory) {
        CommandLine commandLine = new CommandLine(parentCommand, factory);
        commandLine.addSubcommand(testCommand);
        return commandLine;
    }
    @Command(name = "parentTestCommand", mixinStandardHelpOptions = true, scope = INHERIT)
    static class ParentTestCommand {
    }

    @Command(name = "parentTestCommand", mixinStandardHelpOptions = true, scope = INHERIT,
            subcommands = TestCommand.class)
    static class ParentTestCommand2 {
    }

    @Command(name = "sorteotest")
    static class TestCommand {
        @Command(name = "entertest", description = "Start participating in a giveaway")
        void enter(@Parameters(arity = "1") String sentenceType,
                   @Parameters(arity = "1") String tweetUrl) {
            System.out.println("entering giveaway");
        }
    }

    @Test
    public void testIssue1741_subcommandAddedProgrammatically() {
        CommandLine commandLine = getTestCommandLine(new ParentTestCommand(), new TestCommand(), CommandLine.defaultFactory());
        assertEquals(0, commandLine.execute("sorteotest", "entertest", "sequenceType", "url"));
    }

    @Test
    public void testIssue1741_staticHierarchy() {
        CommandLine commandLine = new CommandLine(new ParentTestCommand2());
        assertEquals(0, commandLine.execute("sorteotest", "entertest", "sequenceType", "url"));
    }
}
