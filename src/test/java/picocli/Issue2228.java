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
        final ParseResult[] caughtParseResult = new ParseResult[1];
        commandLine.setExecutionExceptionHandler(new CommandLine.IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine exCmdLine, ParseResult parseResult) throws Exception {
                assertSame(commandLine, exCmdLine);
                assertNotNull(parseResult);
                caughtParseResult[0] = parseResult;
                return 0;
            }
        });
        commandLine.execute("-x");
        assertNotNull(caughtParseResult[0]);

        ParseResult after = commandLine.getParseResult();
        assertFalse(after.matchedArgs().isEmpty());
        assertFalse(after.matchedOptions().isEmpty());

        assertFalse(caughtParseResult[0].matchedArgs().isEmpty());
        assertFalse(caughtParseResult[0].matchedOptions().isEmpty());
    }
}
