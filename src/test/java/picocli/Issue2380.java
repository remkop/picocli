package picocli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Issue2380 {
    static class Cmd2380 {
        @CommandLine.Option(names = "--flag1", arity = "0")
        boolean flag1;

        @CommandLine.Option(names = "--flag2", arity = "0", defaultValue = "false")
        boolean flag2;

        @CommandLine.Option(names = "--flag2a", arity = "0", defaultValue = "true")
        boolean flag2a;

        @CommandLine.Option(names = "--flag3", defaultValue = "false")
        boolean flag3;

        @CommandLine.Option(names = "--flag4", defaultValue = "true")
        boolean flag4;
    }

    @Before
    public void before() {
        //CommandLine.tracer().setLevel(CommandLine.TraceLevel.DEBUG);
    }

    @After
    public void after() {
        //CommandLine.tracer().setLevel(CommandLine.TraceLevel.WARN);
    }

    @Test
    public void testIssue2380() {
        Cmd2380 cmd = new Cmd2380();
        new CommandLine(cmd).parseArgs();
        assertFalse(cmd.flag1);
        assertFalse(cmd.flag2);
        assertTrue(cmd.flag2a);
        assertFalse(cmd.flag3);
        assertTrue(cmd.flag4);
    }
}
