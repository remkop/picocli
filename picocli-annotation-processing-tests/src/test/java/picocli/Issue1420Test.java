package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * Testing class for creating a commandline with resourceBundle
 */
@Command(name = "TestingCommand", resourceBundle = ("picocli.Message"))
class TestingClass {

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    protected TestingClass.ExclusiveOptions exclusive;

    public TestingClass.ExclusiveOptions getExclusive() {
        return this.exclusive;
    }
    private static class ExclusiveOptions {
        @Option(names = {"-h", "--help"}, descriptionKey = "help.message", required = false)
        protected boolean showHelp;

        @Option(names = {"-j", "--json"}, descriptionKey = "json.message", required = false)
        protected boolean isJson;

    }
}

@Ignore
/**
 * JUnit testing class for issue#1420 // CS427 https://github.com/remkop/picocli/issues/1420
 */
public class Issue1420Test {

    /**
     * JUnit test class for issue#1420 with resourceBundle // CS427 https://github.com/remkop/picocli/issues/1420
     */
    @Test
    public void testingWithResourceBundle1() {
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(tempOut);
        new CommandLine(new TestingClass()).usage(printStream);

        String returnedText = tempOut.toString();
        String expectedText = "Usage: TestingCommand [[-h] | [-j]]\n" +
                "  -h, --help   Show help options\n" +
                "  -j, --json   Set json export-on\n";

        assertEquals(expectedText, returnedText);
    }
}
