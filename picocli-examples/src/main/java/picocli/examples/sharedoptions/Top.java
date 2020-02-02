package picocli.examples.sharedoptions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(subcommands = Sub2.class)
class Top implements Runnable {
    static String output = "";
    private boolean[] verbosity = new boolean[0];

    @Option(names = "-v")
    public void setVerbose(boolean[] verbosity) {
        this.verbosity = verbosity;
    }

    public void run() {
        verbose("Hello from top%n");
    }

    public void verbose(String pattern, Object... params) {
        if (verbosity.length >= 1) {
            System.out.printf(pattern, params);
            output += String.format(pattern, params);
        }
    }

    // This works for @Command-annotated methods also.
    @Command
    void sub1(@Mixin MyMixin mymixin) {
        verbose("Hello from sub1%n");
    }

    public static void main(String[] args) {
        new CommandLine(new Top()).execute("-v");
        assertEquals("Hello from top%n", output);

        output = "";
        new CommandLine(new Top()).execute("-v", "sub1");
        assertEquals("Hello from sub1%n", output);

        output = "";
        new CommandLine(new Top()).execute("sub1", "-v");
        assertEquals("Hello from sub1%n", output);

        output = "";
        new CommandLine(new Top()).execute("-v", "sub2");
        assertEquals("Hello from sub2%n", output);

        output = "";
        new CommandLine(new Top()).execute("sub2", "-v");
        assertEquals("Hello from sub2%n", output);
    }

    private static void assertEquals(String expected, String actual) {
        if (!String.format(expected).equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but got '" + actual + "'");
        }
    }
}

// Define a mixin that delegates to the parent command.
class MyMixin {
    @ParentCommand
    Top top;

    @Option(names = "-v")
    public void setVerbose(boolean[] verbosity) {
        top.setVerbose(verbosity);
    }
}

// Now subcommands just need to mix in the `MyMixin`
// to get a `-v` option that delegates to the parent command.
@Command(name = "sub2")
class Sub2 implements Runnable{
    @Mixin
    MyMixin mymixin;

    @Override
    public void run() {
        mymixin.top.verbose("Hello from sub2%n");
    }
}
