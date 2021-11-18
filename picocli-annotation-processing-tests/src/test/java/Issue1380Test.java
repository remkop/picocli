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
        @Option(names = "--silent",
                description = "Silent mode",
                required = false
        )
        public boolean silent;
        @Option(names = {"-v", "--verbose"},
                description = "Verbose mode",
                required = false)
        public boolean verbose;
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
        String expectedText = "Usage: <main class> [--silent | -v]\n" +
                "      --silent    Silent mode\n" +
                "  -v, --verbose   Verbose mode\n";

        assertEquals(expectedText, returnedText);

    }
}
