package picocli;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.*;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * https://github.com/remkop/picocli/issues/1772
 */
public class Issue1772 {
    public CommandLine getTestCommandLine(ParentTestCommand parentCommand, TestCommand testCommand, IFactory factory) {
        CommandLine commandLine = new CommandLine(parentCommand, factory);
        commandLine.addSubcommand(testCommand);
        return commandLine;
    }
    @Command(name = "parentTestCommand", mixinStandardHelpOptions = true, scope = INHERIT)
    static class ParentTestCommand {
    }

    @Command(name = "test")
    static class TestCommand {
        @Option(names = "-r")
        public boolean option1;

        public static String testString;

        @Command(name = "subcommand")
        void start(@Option(names = "-s") boolean option2) {
            testString = "------> -r is " + option1 + " and -s is " + option2 + " <------";
        }
    }

    @Test
    public void testIssue1772() {
        CommandLine commandLine = getTestCommandLine(new ParentTestCommand(), new TestCommand(), CommandLine.defaultFactory());
        commandLine.execute("test", "-r", "subcommand", "-s");
        assertEquals("------> -r is true and -s is true <------", TestCommand.testString);

    }
}
