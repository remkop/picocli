package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class CommandAnnotationInheritedTest {
    static class Example {

        @Command(footer = "Top-level footer")
        abstract static class CommonCommand {
            abstract String getContext();
        }

        @Command(name = "mycommand")
        static class FooSubcommand extends CommonCommand {
            @Option(names = "-x") int x;

            @Override
            String getContext() { return "foo"; }
        }

        @Command(name = "mycommand")
        static class BarSubcommand extends CommonCommand {
            @Override
            String getContext() { return "bar"; }
        }

        @Command(name = "foo", subcommands = {FooSubcommand.class})
        static class Foo { }

        @Command(name = "bar", subcommands = {BarSubcommand.class})
        static class Bar { }

        @Command(name = "top", subcommands = { Foo.class, Bar.class })
        static class TopLevel { }
    }

    @Test
    public void testNoInitializationException1() {
        new CommandLine(new Example.Bar());
    }

    @Test
    public void testNoInitializationException2() {
        new CommandLine(new Example.Foo());
    }

    @Test
    public void testNoInitializationException3() {
        new CommandLine(new Example.TopLevel());
    }

    @Test
    public void testFooterPreserved() {
        String expected = String.format("" +
                "Usage: mycommand [-x=<x>]%n" +
                "  -x=<x>%n" +
                "Top-level footer%n");
        String actual = new CommandLine(new Example.FooSubcommand()).getUsageMessage();
        assertEquals(expected, actual);
    }
}
