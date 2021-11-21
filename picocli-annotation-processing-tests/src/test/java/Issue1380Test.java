import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * Testing class for creating a commandline with ArgGroup exclusive tree
 */
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

/**
 * Testing class for creating a commandline with ArgGroup exclusive false
 */
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

/**
 * JUnit testing class for issue 1380
 */
public class Issue1380Test {

    /**
     * JUnit test class for issue 1380 with exclusive set to true
     */
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

    /**
     * JUnit test class for issue 1380 with exclusive set to false
     */
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
