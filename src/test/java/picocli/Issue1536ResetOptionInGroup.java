package picocli;

import org.junit.Ignore;
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
            return "Args{" +
                "opt1=" + opt1 +
                ", opt2=" + opt2 +
                '}';
        }
    }

    @Command(name = "cmd")
     static class Cmd implements Runnable {
        @ArgGroup(exclusive = false) Args args;

        @Option(names = {"-opt3"}) int opt3;

        public void run() {
            System.out.println(args+", opt3="+opt3);
        }
    }

//    enter cmd; the output is null, opt3=0 (group opt1/opt2 unset)
//    enter cmd -opt1=33 -opt2=44 -opt3=55; the output is Args{opt1=33, opt2=44}, opt3=55
//    enter cmd (without options) again; the output is Args{opt1=33, opt2=44}, opt3=0 (opt3 resetted correctly, the argument-group kept)
    @Ignore("see https://github.com/remkop/picocli/issues/1536")
    @Test
    public void testIssue1536() {
        Cmd main = new Cmd();
        CommandLine cmdLine = new CommandLine(main);

        cmdLine.parseArgs();
        assertEquals(0, main.opt3);
        assertEquals(null, main.args);

        cmdLine.parseArgs("-opt1=33 -opt2=44 -opt3=55".split(" "));
        assertEquals(55, main.opt3);
        assertNotNull(main.args);
        assertEquals(33, main.args.opt1);
        assertEquals(44, main.args.opt2);

        cmdLine.parseArgs();
        assertEquals(0, main.opt3);
        assertEquals(null, main.args);
    }

}
