package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

import static org.junit.Assert.*;

public class Issue2228 {

    @Command
    static class TestCommand implements Runnable {

        @Option(names = "-x")
        public boolean x;

        public void run() {
            throw new IllegalStateException("failing, just for fun");
        }
    }

    @Test
    public void testParseResult() {
        final CommandLine commandLine = new CommandLine(new Issue2228.TestCommand());
        final boolean[] handled = new boolean[] {false};
        commandLine.setExecutionExceptionHandler(new CommandLine.IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine exCmdLine, ParseResult parseResult) throws Exception {
                handled[0] = true;
                assertSame(commandLine, exCmdLine);

                ParseResult after = commandLine.getParseResult();
                printParseResult(after, "commandLine.getParseResult()");
                assertFalse(after.matchedArgs().isEmpty());
                assertFalse(after.matchedOptions().isEmpty());

                printParseResult(parseResult, "ExecutionExceptionHandler method arg");
                assertNotNull(parseResult);
                assertFalse(parseResult.matchedArgs().isEmpty());
                assertFalse(parseResult.matchedOptions().isEmpty());
                return 0;
            }
        });
        commandLine.execute("-x");
        assertTrue("ExecutionExceptionHandler tests were executed", handled[0]);
    }

    private static void printParseResult(ParseResult parseResult, String origin) {
        System.out.println("\tParseResult from " + origin + ": " + parseResult);
        System.out.println("\t\tmatchedArgs(): " + parseResult.matchedArgs());
        System.out.println("\t\tmatchedOptions(): " + parseResult.matchedOptions());
    }
}
