package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParseResult;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class RepeatableSubcommandsTest {
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Command(name="A",
            subcommandsRepeatable = true,
            subcommands = {B.class, C.class, D.class})
    static class A {}

    @Command(name="B",
            subcommandsRepeatable = true,
            subcommands = {E.class, F.class, G.class})
    static class B {}

    @Command(name="C")
    static class C {}

    @Command(name="D")
    static class D {}

    @Command(name="E")
    static class E {}

    @Command(name="F")
    static class F {}

    @Command(name="G")
    static class G {}

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
}
