package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

// https://github.com/remkop/picocli/issues/1531
public class Issue1531ResetOptionMethods {

    @Rule
    // Allow tests to set any kind of properties they like, without having to individually roll them back.
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command
    static class MyCommand {
        List<String> modules;

        @Option(names = "-c", description = "comps", split = ",")
        List<String> comps;

        @Option(names = "-m", defaultValue = Option.NULL_VALUE, description = "modules", split = ",")
        public void setModules(final List<String> modules) {
            this.modules = modules;
        }
    }

    //@Ignore("https://github.com/remkop/picocli/issues/1531")
    @Test
    public void testResetOptionMethodsOnReuse() {

        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);

        MyCommand myCommand = new MyCommand();
        CommandLine cmd = new CommandLine(myCommand);

        cmd.parseArgs(); // first, invoke with no args
        assertNull(myCommand.comps);
        assertNull(myCommand.modules);

        // now invoke with some arguments
        cmd.parseArgs("-c c1 -m m1".split(" "));
        assertEquals(Collections.singletonList("c1"), myCommand.comps);
        assertEquals(Collections.singletonList("m1"), myCommand.modules);

        //System.err.println("-------------");

        cmd.parseArgs(); // finally, invoke with no args again
        assertNull(myCommand.comps);
        assertNull(myCommand.modules);
    }
}
