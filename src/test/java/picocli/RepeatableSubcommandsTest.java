package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RepeatableSubcommandsTest {
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Command(name="A",
            subcommandsRepeatable = true,
            subcommands = {B.class, C.class, D.class})
    static class A {
        @Option(names = "-x") String x;
        static AtomicInteger total = new AtomicInteger();
        static AtomicInteger count = new AtomicInteger();
        public A() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    @Command(name="B",
            subcommandsRepeatable = true,
            subcommands = {E.class, F.class, G.class})
    static class B {
        @Option(names = "-x") String x;
        static AtomicInteger count = new AtomicInteger();
        public B() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    @Command(name="C")
    static class C {
        @Option(names = "-x") String x;
        static AtomicInteger count = new AtomicInteger();
        public C() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    @Command(name="D")
    static class D {
        @Option(names = "-x") String x;
        static AtomicInteger count = new AtomicInteger();
        public D() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    @Command(name="E")
    static class E {
        @Option(names = "-x") String x;
        static AtomicInteger count = new AtomicInteger();
        public E() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    @Command(name="F")
    static class F {
        static AtomicInteger count = new AtomicInteger();
        public F() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    @Command(name="G")
    static class G {
        static AtomicInteger count = new AtomicInteger();
        public G() {
            count.incrementAndGet();
            A.total.incrementAndGet();
        }
    }

    //@Ignore("requires #454 repeatable subcommands")
    @Test
    public void testSubommandRepeatable() {
        //TestUtil.setTraceLevel("DEBUG");
        CommandLine cl = new CommandLine(new A());
        ParseResult parseResult = cl.parseArgs("B B C D B E F G E E F F".split(" "));
        StringWriter sw = new StringWriter();
        print("", parseResult, new PrintWriter(sw));
        String expected = String.format("" +
                "A%n" +
                "  B%n" +
                "  B%n" +
                "  C%n" +
                "  D%n" +
                "  B%n" +
                "    E%n" +
                "    F%n" +
                "    G%n" +
                "    E%n" +
                "    E%n" +
                "    F%n" +
                "    F%n");
        assertEquals(expected, sw.toString());
    }

    private void print(String indent, ParseResult parseResult, PrintWriter pw) {
        pw.println(indent + parseResult.commandSpec().name());
        for (ParseResult sub : parseResult.subcommands()) {
            print(indent + "  ", sub, pw);
        }
    }

    @Test
    public void testCommandLazyInstantiation() {
        A.total.set(0);
        A.count.set(0);
        B.count.set(0);
        C.count.set(0);
        D.count.set(0);
        E.count.set(0);
        F.count.set(0);
        G.count.set(0);

        CommandLine cl = new CommandLine(A.class);
        assertEquals(0, A.count.get());
        assertEquals(0, B.count.get());
        assertEquals(0, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(0, A.total.get());

        cl.parseArgs();
        assertEquals(1, A.count.get());
        assertEquals(0, B.count.get());
        assertEquals(0, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(1, A.total.get());

        cl.parseArgs("-x=y");
        assertEquals(1, A.count.get());
        assertEquals(0, B.count.get());
        assertEquals(0, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(1, A.total.get());

        cl.parseArgs("-x=y", "B");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get());
        assertEquals(0, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(2, A.total.get());

        cl.parseArgs("-x=y", "B", "-x=y");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get());
        assertEquals(0, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(2, A.total.get());

        cl.parseArgs("C");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get());
        assertEquals(1, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(3, A.total.get());

        cl.parseArgs("C", "-x=y");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get());
        assertEquals(1, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(0, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(3, A.total.get());

        cl.parseArgs("B", "E");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get());
        assertEquals(1, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(1, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(4, A.total.get());

        cl.parseArgs("B", "E", "-x=y");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get());
        assertEquals(1, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(1, E.count.get());
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(4, A.total.get());

        cl.parseArgs("B", "-x=y", "E", "-x=y");
        assertEquals(1, A.count.get());
        assertEquals(1, B.count.get()); // instance is reused
        assertEquals(1, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(1, E.count.get()); // instance is reused
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(4, A.total.get());

        cl.parseArgs("-x=y", "B", "-x=y", "E", "-x=y");
        assertEquals(1, A.count.get()); // instance is reused
        assertEquals(1, B.count.get()); // instance is reused
        assertEquals(1, C.count.get());
        assertEquals(0, D.count.get());
        assertEquals(1, E.count.get()); // instance is reused
        assertEquals(0, F.count.get());
        assertEquals(0, G.count.get());
        assertEquals(4, A.total.get());
    }
}
