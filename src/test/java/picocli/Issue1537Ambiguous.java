package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import picocli.CommandLine.Command;

import static org.junit.Assert.*;

public class Issue1537Ambiguous {

    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Command
    static class MyCommand implements Runnable {

        @Command(name = "chemical-files", aliases = {"chem-formats"})
        void sub() {}

        public void run() {}
    }

    @Test
    public void testExecute() {
        int exitCode = new CommandLine(new MyCommand()).setAbbreviatedOptionsAllowed(true).execute("chem");
        assertEquals(2, exitCode);

        String expected = String.format(
            "Unmatched argument at index 0: 'chem'%n" +
            "Did you mean: chem-formats or chemical-files?%n");
        assertEquals(expected, systemErrRule.getLog());
    }

    @Test
    public void testParseArgs() {
        try {
            new CommandLine(new MyCommand()).setAbbreviatedOptionsAllowed(true).parseArgs("chem");
            fail("expected exception");
        } catch (CommandLine.UnmatchedArgumentException ex) {
            assertEquals("Unmatched argument at index 0: 'chem'", ex.getMessage());
        }
    }
}
