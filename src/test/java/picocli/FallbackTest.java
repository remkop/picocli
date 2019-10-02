package picocli;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

import static org.junit.Assert.*;

public class FallbackTest {
    @Test
    public void testIssue828SubcommandAssignedToOption() {
        class MyCommand {
            @Option(names = {"-r", "--reader"}, arity = "0..1",
                    description = "Use reader", paramLabel = "<reader>", fallbackValue = "")
            String reader;

            @Parameters
            String[] params = {};

            @Command(name = "run", description = "Run specified application.")
            public int runApp(@Parameters(paramLabel = "<app>", index = "0") String app,
                              @Parameters(index = "1..*") String[] args) {
                return 123;
            }
        }

        MyCommand cmd = new MyCommand();
        ParseResult parseResult = new CommandLine(cmd)
                .setUnmatchedOptionsArePositionalParams(true)
                .parseArgs("-r run app arg1 arg2 --something".split(" "));
        assertNotEquals("run", cmd.reader);
    }
}
