package picocli;

import static org.junit.Assert.*;
import picocli.CommandLine;

import org.junit.Test;

public class Issue1880 {

    @CommandLine.Command
    static class TestCommand implements Runnable {

        @CommandLine.Option(
            names = {"--no-interactive"},
           /* defaultValue = "true",*/
            negatable = true
        )
        public boolean interactive = true;

        public void run() {}
    }

    @Test
    public void optionIsTrueByDefault() {
        final CommandLine commandLine = new CommandLine(new TestCommand());
        final CommandLine.ParseResult parseResult = commandLine.parseArgs();
        final TestCommand command = (TestCommand) parseResult.commandSpec().userObject();
        assertTrue(command.interactive);
    }

    @Test
    public void optionFalseWhenSpecified() {
        final TestCommand command = new TestCommand();
        new CommandLine(command).parseArgs("--no-interactive");
        assertFalse(command.interactive);
    }

    @Test
    public void optionTrueWhenNegatedFormSpecified() {
        final TestCommand command = new TestCommand();
        new CommandLine(command).parseArgs("--interactive");
        assertTrue(command.interactive);
    }

    @Test
    public void optionCanBeSetToFalse() {
        final TestCommand command = new TestCommand();
        //CommandLine.tracer().setLevel(CommandLine.TraceLevel.DEBUG);
        new CommandLine(command).parseArgs("--no-interactive=false");
        assertFalse(command.interactive);
    }

    @Test
    public void optionNegatedFormSetToFalseIsTrue() {
        final TestCommand command = new TestCommand();
        new CommandLine(command).parseArgs("--interactive=false");
        assertTrue(command.interactive);
    }

}
