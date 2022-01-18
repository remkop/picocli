package picocli;

import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class Issue1536ResetOptionInGroup {
    static class Args {
        @Option(names = {"-opt1"}) int opt1;
        @Option(names = {"-opt2"}) int opt2;

        @Override
        public String toString() {
            return String.format("Args{opt1=%s, opt2=%s}", opt1, opt2);
        }
    }
    static class Args2 {
        @Option(names = {"-a"}, defaultValue = "0") int opt1;
        @Option(names = {"-b"}, defaultValue = "0") int opt2;
    }

    @Command(name = "cmd")
     static class Cmd implements Runnable {
        @ArgGroup(exclusive = false) Args args;
        @ArgGroup(exclusive = false) Args2 initialized = new Args2();

        @Option(names = {"-opt3"}) int opt3;

        public void run() {
            System.out.println(args+", opt3="+opt3);
        }
    }

//    enter cmd; the output is null, opt3=0 (group opt1/opt2 unset)
//    enter cmd -opt1=33 -opt2=44 -opt3=55; the output is Args{opt1=33, opt2=44}, opt3=55
//    enter cmd (without options) again; the output is Args{opt1=33, opt2=44}, opt3=0 (opt3 resetted correctly, the argument-group kept)
//    @Ignore("see https://github.com/remkop/picocli/issues/1536")
    @Test
    public void testIssue1536() {
        Cmd main = new Cmd();
        CommandLine cmdLine = new CommandLine(main);

        cmdLine.parseArgs();
        assertEquals(0, main.opt3);
        assertEquals(null, main.args);
        assertNotNull(main.initialized);
        assertEquals(0, main.initialized.opt1);
        assertEquals(0, main.initialized.opt2);

        cmdLine.parseArgs("-opt1=33 -opt2=44 -opt3=55 -a=33 -b=44".split(" "));
        assertEquals(55, main.opt3);
        assertNotNull(main.args);
        assertEquals(33, main.args.opt1);
        assertEquals(44, main.args.opt2);
        assertNotNull(main.initialized);
        assertEquals(33, main.initialized.opt1);
        assertEquals(44, main.initialized.opt2);

        cmdLine.parseArgs();
        assertEquals(0, main.opt3);
        assertNotNull(main.initialized);
        assertEquals(0, main.initialized.opt1);
        assertEquals(0, main.initialized.opt2);

        // the previous values are preserved, not reset...
        assertNotNull(main.args);
        assertEquals(33, main.args.opt1);
        assertEquals(44, main.args.opt2);
    }

}
