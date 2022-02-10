package picocli;

import java.util.concurrent.Callable;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class Issue1528 {

    @Command(name = "main", subcommands = SubCommand.class)
    static class MainCommand implements Callable<Integer> {
        public Integer call() throws Exception {
            return null;
        }
    }

    @Command(name = "sub")
    static class SubCommand implements Callable<Integer> {
        public Integer call() throws Exception {
            return null;
        }
    }

    @Test
    public void testSubCommandAliasModification() {
        CommandLine cl = new CommandLine(new MainCommand());
        CommandSpec cs = cl.getSubcommands().get("sub").getCommandSpec();

        assertEquals(0, cs.aliases().length);
        assertEquals(0, cl.execute("sub"));
        assertNotEquals(0, cl.execute("alias1"));

        cs.aliases("alias1");
        assertEquals(1, cs.aliases().length);
        assertEquals("alias1", cs.aliases()[0]);
        assertEquals(0, cl.execute("sub"));
        assertEquals(0, cl.execute("alias1"));

        cs.aliases("alias1", "alias2");
        assertEquals(2, cs.aliases().length);
        assertEquals("alias1", cs.aliases()[0]);
        assertEquals("alias2", cs.aliases()[1]);
        assertEquals(0, cl.execute("sub"));
        assertEquals(0, cl.execute("alias1"));
        assertEquals(0, cl.execute("alias2"));

        cs.aliases("alias2");
        assertEquals(1, cs.aliases().length);
        assertEquals("alias2", cs.aliases()[0]);
        assertEquals(0, cl.execute("sub"));
        assertNotEquals(0, cl.execute("alias1"));
        assertEquals(0, cl.execute("alias2"));

        cs.aliases("alias3");
        assertEquals(1, cs.aliases().length);
        assertEquals("alias3", cs.aliases()[0]);
        assertEquals(0, cl.execute("sub"));
        assertNotEquals(0, cl.execute("alias1"));
        assertNotEquals(0, cl.execute("alias2"));
        assertEquals(0, cl.execute("alias3"));

        cs.aliases(new String[0]);
        assertEquals(0, cs.aliases().length);
        assertEquals(0, cl.execute("sub"));
        assertNotEquals(0, cl.execute("alias1"));
        assertNotEquals(0, cl.execute("alias2"));
        assertNotEquals(0, cl.execute("alias3"));
    }
}
