package picocli;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.*;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * https://github.com/remkop/picocli/issues/1774
 */
public class Issue1774 {
    public CommandLine getTestCommandLine(ParentTestCommand parentCommand, TestCommand testCommand, CommandLine.IFactory factory) {
        CommandLine commandLine = new CommandLine(parentCommand, factory);
        commandLine.addSubcommand(testCommand);
        return commandLine;
    }
    @Command(name = "parentTestCommand", mixinStandardHelpOptions = true, scope = INHERIT)
    static class ParentTestCommand {
    }

    @Command(name = "test")
    static class TestCommand {
        @Command(name = "subcommand")
        void start(@Option(names = "-r", arity = "0") boolean optionR,
                   @Option(names = "-s", arity = "0") boolean optionS,
                   @Parameters(arity = "1") String url) {
            System.out.printf("optionR=%s, optionS=%s, url=%s%n", optionR, optionS, url);
        }
    }
    @Test
    public void testIssue1774() {
        CommandLine commandLine = getTestCommandLine(new ParentTestCommand(), new TestCommand(), CommandLine.defaultFactory());
        assertEquals(0, commandLine.execute("test", "subcommand", "-r", "-s", "URL"));
    }
}
