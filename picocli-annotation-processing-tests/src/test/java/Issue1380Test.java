import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

//Testing Command
@Command(requiredOptionMarker = '*')
class RequiredMarkerDisplayedIncorrectly {

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    public RequiredMarkerDisplayedIncorrectly.ExclusiveOptions exclusive;
    public static class ExclusiveOptions {
        @Option(names = {"-s", "--silent"},
                description = "Silent mode",
                required = true
        )
        public boolean silent;

        @Option(names = {"-v", "--verbose"},
                description = "Verbose mode",
                required = false)
        public boolean verbose;

        @Option(names = {"-h", "--help"},
                description = "Printing help",
                required = false)
        public boolean help;
    }
}

public class Issue1380Test {
    @Test
    public void testingRequiredOptionMark() {

        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(tempOut);
        //Create the testing command
        new CommandLine(new RequiredMarkerDisplayedIncorrectly()).usage(printStream);

        String returnedText = tempOut.toString();
        String expectedText = "Usage: <main class> [-s | -v | -h]\n" +
                "  -h, --help      Printing help\n" +
                "* -s, --silent    Silent mode\n" +
                "  -v, --verbose   Verbose mode\n\n";

        assertEquals(expectedText, returnedText);

    }
}
