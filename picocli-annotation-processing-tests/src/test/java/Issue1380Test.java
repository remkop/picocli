import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;


@Command(requiredOptionMarker = '*')
class TestingClassExclusiveTrue {

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    public TestingClassExclusiveTrue.ExclusiveOptions exclusive;

    public static class ExclusiveOptions {
        @Option(names = {"-s", "--silent"}, description = "Silent mode", required = false)
        public boolean silent;

        @Option(names = {"-v", "--verbose"}, description = "Verbose mode", required = false)
        public boolean verbose;

        @Option(names = {"-j", "--json"}, description = "JSON printing", required = true)
        public boolean help;
    }
}

@Command(requiredOptionMarker = '*')
class TestingClassExclusiveFalse {

    @ArgGroup(exclusive = false, multiplicity = "0..1")
    public TestingClassExclusiveFalse.ExclusiveOptions exclusive;

    public static class ExclusiveOptions {
        @Option(names = {"-s", "--silent"}, description = "Silent mode", required = false)
        public boolean silent;

        @Option(names = {"-v", "--verbose"}, description = "Verbose mode", required = false)
        public boolean verbose;

        @Option(names = {"-j", "--json"}, description = "JSON printing", required = false)
        public boolean help;
    }
}

public class Issue1380Test {
    @Test
    public void testingWithExclusiveTrue() {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(tempOut);
        new CommandLine(new TestingClassExclusiveTrue()).usage(printStream);

        String returnedText = tempOut.toString();
        String expectedText = "Usage: <main class> [[-s] | [-v] | -j]\n" +
                "* -j, --json      JSON printing\n" +
                "  -s, --silent    Silent mode\n" +
                "  -v, --verbose   Verbose mode\n";

        assertEquals(expectedText, returnedText);

    }

    @Test
    public void testingWithExclusiveFalse() {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(tempOut);
        new CommandLine(new TestingClassExclusiveFalse()).usage(printStream);

        String returnedText = tempOut.toString();
        String expectedText = "Usage: <main class> [[-s] [-v] [-j]]\n" +
                "  -j, --json      JSON printing\n" +
                "  -s, --silent    Silent mode\n" +
                "  -v, --verbose   Verbose mode\n";

        assertEquals(expectedText, returnedText);

    }
}
