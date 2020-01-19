package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParseResult;

public class RepeatableSubcommandsTest {
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Command(name="A",
            //subcommandsRepeatable = true,
            subcommands = {B.class, C.class, D.class})
    static class A {}

    @Command(name="B",
            //subcommandsRepeatable = true,
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

    @Ignore("requires #454 repeatable subcommands")
    @Test
    public void testSubommandRepeatable() {
        TestUtil.setTraceLevel("DEBUG");
        CommandLine cl = new CommandLine(new A());
        ParseResult parseResult = cl.parseArgs("B B C D B E F G E E F F".split(" "));
//        print("", parseResult);
    }

    private void print(String indent, ParseResult parseResult) {
        System.out.println(indent + parseResult.commandSpec().name());
        indent += "  ";
        for (ParseResult sub : parseResult.subcommands()) {
            print(indent, sub);
        }
    }
}
