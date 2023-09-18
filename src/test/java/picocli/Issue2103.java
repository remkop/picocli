package picocli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Enhancement from issue 2103 enables or disables positional parameters before the EndOfOptions delimiter (such as "--").
 */
public class Issue2103 {

    /**
     * Original behavior allows positional parameters before and after EndOfOptions delimiter.
     */
    @Test
    public void testOriginalBehavior() {
        class App {
            @CommandLine.Option(names = "--optA") String optA;
            @CommandLine.Parameters()
            final List<String> list = new ArrayList<String>();
        }

        App app = CommandLine.populateCommand(new App(), "--optA joe a b -- --optB c d".split(" "));
        assertEquals("joe", app.optA);
        assertEquals(Arrays.asList("a", "b", "--optB", "c", "d"), app.list);
    }

    /**
     * When ParameterAllowedBeforeEndOfOptions is disabled, the exit code for USAGE should be returned
     * when positional parameters are found before EndOfOptions delimiter.
     */
    @Test
    public void testTriggerUsage() {
        class App implements Runnable {
            @CommandLine.Option(names = "--optA") String optA;
            @CommandLine.Parameters()
            final List<String> list = new ArrayList<String>();

            public void run() { }
        }

        App app = new App();
        int exitCode = new CommandLine(app)
            .setParameterAllowedBeforeEndOfOptions(false)
            .execute("--optA joe a b -- --optB c d".split(" "));
        assertEquals(2, exitCode); // Should exit with USAGE since a and b are unmatched arguments
        assertEquals("joe", app.optA);
        assertEquals(Arrays.asList("--optB", "c", "d"), app.list);
    }

    /**
     * Using a valid command line with ParameterAllowedBeforeEndOfOptions disabled, should correctly parse the options
     * after the EndOfOptions delimiter as well as the valid options before the delimiter.
     */
    @Test
    public void testParameterAllowedBeforeEndOfOptions() {
        class App implements Runnable {
            @CommandLine.Option(names = "--optA") String optA;
            @CommandLine.Parameters()
            final List<String> list = new ArrayList<String>();

            public void run() { }
        }

        App app = new App();
        int exitCode = new CommandLine(app)
            .setParameterAllowedBeforeEndOfOptions(false)
            .execute("--optA joe -- --optB c d".split(" "));
        assertEquals(0, exitCode);
        assertEquals("joe", app.optA);
        assertEquals(Arrays.asList("--optB", "c", "d"), app.list);
    }
}
