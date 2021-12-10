package picocli;

import org.junit.Test;
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
    private TestingClassExclusiveTrue.ExclusiveOptions exclusive;

    /**
     * Added getters to satisfy PMF and findBug requirements
     */
    public ExclusiveOptions getExclusive() {
        return this.exclusive;
    }

    private static class ExclusiveOptions {
        @Option(names = {"-s", "--silent"}, description = "Silent mode", required = false)
        private boolean silent;

        /**
         * Added getters to satisfy PMF and findBug requirements
         */
        public boolean getSilent() {
            return this.silent;
        }

        @Option(names = {"-v", "--verbose"}, description = "Verbose mode", required = false)
        private boolean verbose;

        /**
         * Added getters to satisfy PMF and findBug requirements
         */
        public boolean getVerbose() {
            return this.verbose;
        }

        @Option(names = {"-j", "--json"}, description = "JSON printing", required = false)
        private boolean json;

        /**
         * Added getters to satisfy PMF and findBug requirements
         */
        public boolean getJson(){
            return this.json;
        }

    }
}

/**
 * Testing class for creating a commandline with ArgGroup exclusive false
 */
@Command(requiredOptionMarker = '*')
class TestingClassExclusiveFalse {

    @ArgGroup(exclusive = false, multiplicity = "0..1")
    private TestingClassExclusiveFalse.ExclusiveOptions exclusive;

    /**
     * Added getters to satisfy PMF and findBug requirements
     */
    public ExclusiveOptions getExclusive() {
        return this.exclusive;
    }

    private static class ExclusiveOptions {
        @Option(names = {"-s", "--silent"}, description = "Silent mode", required = false)
        private boolean silent;

        /**
         * Added getters to satisfy PMF and findBug requirements
         */
        public boolean getSilent() {
            return this.silent;
        }

        @Option(names = {"-v", "--verbose"}, description = "Verbose mode", required = false)
        private boolean verbose;

        /**
         * Added getters to satisfy PMF and findBug requirements
         */
        public boolean getVerbose() {
            return this.verbose;
        }

        @Option(names = {"-j", "--json"}, description = "JSON printing", required = false)
        private boolean json;

        /**
         * Added getters to satisfy PMF and findBug requirements
         */
        public boolean getJson(){
            return this.json;
        }
    }
}

/**
 * JUnit testing class for issue 1380 // CS427 https://github.com/remkop/picocli/issues/1380
 */
public class Issue1380Test {

    /**
     * JUnit test class for issue 1380 with exclusive set to true // CS427 https://github.com/remkop/picocli/issues/1380
     */
    @Test
    public void testingWithExclusiveTrue() {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(tempOut);
        new CommandLine(new TestingClassExclusiveTrue()).usage(printStream);

        String returnedText = tempOut.toString();
        String expectedText =
        "Usage: <main class> [-s | -v | -j]\n"+
                "  -j, --json      JSON printing\n"+
                "  -s, --silent    Silent mode\n"+
                "  -v, --verbose   Verbose mode\n";

        assertEquals(expectedText, returnedText);

    }

    /**
     * JUnit test class for issue 1380 with exclusive set to false // CS427 https://github.com/remkop/picocli/issues/1380
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
