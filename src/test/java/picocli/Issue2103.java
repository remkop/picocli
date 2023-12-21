package picocli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Enhancement from issue 2103 enables or disables positional parameters before the EndOfOptions delimiter (such as "--").
 */
public class Issue2103 {

    static class App implements Runnable {
        @CommandLine.Option(names = "--optA") String optA;
        @CommandLine.Parameters()
        final List<String> list = new ArrayList<String>();
        public void run() { }
    }

    static class SubCommand implements Runnable {
        @CommandLine.Option(names = "--optB") String optB;
        @CommandLine.Parameters()
        final List<String> list = new ArrayList<String>();
        public void run() { }
    }

    /**
     * Original behavior allows positional parameters before and after EndOfOptions delimiter.
     */
    @Test
    public void testOriginalBehavior() {
        App app = CommandLine.populateCommand(new App(), "--optA joe a b -- --optB c d".split(" "));
        assertEquals("joe", app.optA);
        assertEquals(Arrays.asList("a", "b", "--optB", "c", "d"), app.list);
    }

    /**
     * The default value for allowing parameters prior to the End Of Options delimiter should be true
     * in order to maintain backward compatibility with previous releases.
     */
    @Test
    public void testOriginalDefault() {
        App app = new App();
        CommandLine c = new CommandLine(app);
        assertTrue(c.isParameterAllowedBeforeEndOfOptions());
        //verify ParserSpec getter (should return same value as CommandLine setting
        assertTrue(c.getCommandSpec().parser().parameterAllowedBeforeEndOfOptions());

        // Toggle value for setting and verify (tests setter, and verifies getters)
        c.getCommandSpec().parser().parameterAllowedBeforeEndOfOptions(false);
        assertFalse(c.getCommandSpec().parser().parameterAllowedBeforeEndOfOptions());
        assertFalse(c.isParameterAllowedBeforeEndOfOptions());
    }

    /**
     * When ParameterAllowedBeforeEndOfOptions is disabled, the exit code for USAGE should be returned
     * when positional parameters are found before EndOfOptions delimiter.
     */
    @Test
    public void testTriggerUsage() {
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
        App app = new App();
        int exitCode = new CommandLine(app)
            .setParameterAllowedBeforeEndOfOptions(false)
            .execute("--optA joe -- --optB c d".split(" "));
        assertEquals(0, exitCode);
        assertEquals("joe", app.optA);
        assertEquals(Arrays.asList("--optB", "c", "d"), app.list);
    }

    /**
     * Subcommand tests for ParameterAllowedBeforeEndOfOptions.
     */
    @Test
    public void testParameterAllowedBeforeEndOfOptionsSubCommand1() {
        class SubCommandZ implements Runnable {
            @CommandLine.Option(names = "--optZ") String optZ;
            @CommandLine.Parameters()
            final List<String> list = new ArrayList<String>();
            public void run() { }
        }

        CommandLine cl = new CommandLine(new App())
            .addSubcommand("cmdA", new SubCommand())
            .setParameterAllowedBeforeEndOfOptions(false)
            .addSubcommand("cmdZ", new SubCommandZ());

        // The ParameterAllowedBeforeEndOfOptions should apply to both main command and subcommands
        // The extra "a1" after "jack" should be rejected.
        assertEquals(2, cl.execute("--optA jill cmdA --optB jack a1 -- --optC c d".split(" ")));
        // The extra "a2" after "jill" should be rejected
        assertEquals(2, cl.execute("--optA jill a2 cmdA --optB jack -- --optC c d".split(" ")));
        /* The extra "a3" after "hill" should NOT be rejected since the setParameterAllowedBeforeEndOfOptions was
           called before the subcommand was added. */
        assertEquals(0, cl.execute("--optA jill cmdZ --optZ hill a3 -- --optC c d".split(" ")));
    }

    /**
     * Subcommand tests for ParameterAllowedBeforeEndOfOptions.
     */
    @Test
    public void testParameterAllowedBeforeEndOfOptionsSubCommand2() {
        App app = new App();
        SubCommand sub = new SubCommand();
        int exitCode = new CommandLine(app)
            .addSubcommand("cmdA", sub)
            .execute("--optA jill cmdA --optB jack a -- --optC c d".split(" "));
        // the extra "a" after "jack" should be accepted, because ParameterAllowedBeforeEndOfOptions was not set
        assertEquals(0, exitCode);
        assertEquals("jill", app.optA);
        assertEquals("jack", sub.optB);
        assertTrue(app.list.isEmpty());
        assertEquals(Arrays.asList("a", "--optC", "c", "d"), sub.list);
    }

}
