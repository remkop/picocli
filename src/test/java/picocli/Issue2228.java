package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

import static org.junit.Assert.*;

public class Issue2228 {

    @Command(subcommands = Issue2228.SubCommand.class)
    static class TestCommand {
    }

    @Command(name = "subsub")
    static class SubCommand implements Runnable {

        @Option(names = "-y")
        public boolean y;

        public void run() {
            throw new IllegalStateException("Y failing, just for fun");
        }
    }

    @Test
    public void testParseResult() {
        final CommandLine commandLine = new CommandLine(new Issue2228.TestCommand());
        final boolean[] handled = new boolean[] {false};
        commandLine.setExecutionExceptionHandler(new CommandLine.IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex, CommandLine exCmdLine, ParseResult fullParseResult) throws Exception {
                handled[0] = true;

                ParseResult subResult = commandLine.getSubcommands().get("subsub").getParseResult();
                //printParseResult(subResult, "subsub subcommand");
                assertFalse(subResult.matchedArgs().isEmpty());
                assertFalse(subResult.matchedOptions().isEmpty());

                //printParseResult(fullParseResult, "ExecutionExceptionHandler method arg");
                assertNotNull(fullParseResult);
                assertFalse(fullParseResult.subcommand().matchedArgs().isEmpty());
                assertFalse(fullParseResult.subcommand().matchedOptions().isEmpty());

                assertTrue(fullParseResult.matchedArgs().isEmpty());
                assertTrue(fullParseResult.matchedOptions().isEmpty());
                return 0;
            }
        });
        commandLine.execute("subsub", "-y");
        assertTrue("ExecutionExceptionHandler tests were executed", handled[0]);
    }

    private static void printParseResult(ParseResult parseResult, String origin) {
        System.out.println("\tParseResult from " + origin + ": " + parseResult);
        System.out.println("\t\tmatchedArgs(): " + parseResult.matchedArgs());
        System.out.println("\t\tmatchedOptions(): " + parseResult.matchedOptions());
    }
}
