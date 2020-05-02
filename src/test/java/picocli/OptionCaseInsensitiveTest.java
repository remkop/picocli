package picocli;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class OptionCaseInsensitiveTest
{
    @CommandLine.Command(name = "adder", aliases = "addr", description = "add two integers and give result")
    public static class Adder implements Callable<Integer>
    {
        @CommandLine.Parameters(paramLabel = "Integers", description = "Integers to add")
        public int[] integers;
        @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "just a simple adder", caseInsensitive = true)
        public boolean help;
        @CommandLine.Option(names = {"-a", "--take_abs"}, arity = "0..1", description = "take abs vals and add, default disabled")
        public boolean abs;
        @CommandLine.Option(names = {"-t", "--test_flag"}, arity = "0..1", description = "just for test", negatable = true, caseInsensitive = true)
        public boolean flag;
        @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        static class Exclusive
        {
            @CommandLine.Option(names = "-x", required = true, caseInsensitive = true)
            int x;
            @CommandLine.Option(names = "-y", required = true)
            int y;
            @CommandLine.Option(names = "-z", required = true)
            int z;
        }


        public Integer call() throws Exception
        {
            if (abs)
            {
                for (int i = 0; i < integers.length; i++)
                    integers[i] = Math.abs(integers[i]);
            }
            int sum = 0;
            for (int i : integers)
                sum += i;
            return sum;
        }
    }

    @Test
    public void testCaseInsensitivePosixOption()
    {
        Adder a = new Adder();
        CommandLine cmd = new CommandLine(a);
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.execute("-H", "1", "2");
        assertTrue(a.help);
    }

    @Test
    public void testCaseInsensitiveNegatble()
    {
        Adder a = new Adder();
        CommandLine cmd = new CommandLine(a);
        cmd.execute("-t", "1", "2");
        assertTrue(a.flag);
        cmd.execute("--no-test_flag", "1", "2");
        assertFalse(a.flag);
        cmd.execute("-t", "1", "2");
        assertTrue(a.flag);
        cmd.execute("--nO-tEst_fLag", "1", "2");
        assertFalse(a.flag);
    }

    @Test
    public void testMixCaseSensitivityPosixOptions1()
    {
        Adder a = new Adder();
        CommandLine cmd = new CommandLine(a);
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.execute("-tH", "1", "2");
        assertTrue(a.help);
        assertTrue(a.flag);
    }

    @Test
    public void testCaseInsensitiveOption()
    {
        Adder a = new Adder();
        CommandLine cmd = new CommandLine(a);
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.execute("--HeLp", "1", "2");
        assertTrue(a.help);
    }

    @Test(expected = CommandLine.DuplicateOptionAnnotationsException.class)
    public void testDuplicateOptionsAreRejected()
    {
        /** Duplicate parameter names are invalid. */
        class DuplicateOptions
        {
            @CommandLine.Option(names = "-dUpLicate", caseInsensitive = true)
            public int value1;
            @CommandLine.Option(names = "-dupliCaTe", caseInsensitive = false)
            public int value2;
        }
        new CommandLine(new DuplicateOptions());
    }

    @Test
    public void argGroupTest()
    {
        Adder a = new Adder();
        CommandLine cmd = new CommandLine(a);
        cmd.execute("-X=1","1","2");
        assertEquals(1, a.exclusive.x);
    }

//    @Test
//    public void testDuplicateNegatedOptionsRejected()
//    {
//        @CommandLine.Command(name = "negatable-options-demo")
//        class NegatableOptionsDemo
//        {
//            @CommandLine.Option(names = "--verbose", negatable = true)
//            boolean verbose= false;
//            @CommandLine.Option(names = "--no-verbose")
//            boolean nverbose = false;
//        }
//        NegatableOptionsDemo d = new NegatableOptionsDemo();
//        CommandLine cmd = new CommandLine(d);
//        cmd.execute("--verbose");
//        assertTrue(d.verbose);
//        cmd.execute("--no-verbose");
//        assertFalse(d.verbose);
//        assertTrue(d.nverbose);
//    }
}
