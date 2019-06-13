package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


public class RepeatableSubcommandTest {

    @Command(name = "print", subcommands = File.class)
    static class Print implements Runnable {
        enum Paper {A1, A2, A3, A4, A5, B1, B2, B3, B4, B5}
        @Option(names = "--paper") Paper paper;

        public void run() {
        }
    }
    @Command(name = "file")
    static class File implements Runnable {
        @Parameters(index = "0", paramLabel = "FILE")
        java.io.File file;

        @Option(names = "--count") int count = 1;

        enum Rotate {left, right}
        @Option(names = "--rotate") Rotate rotate;

        public void run() {
        }
    }

    @Ignore
    @Test
    public void testSimple() {
        Print print = new Print();
        CommandLine cmd = new CommandLine(print);
        cmd.execute((
                "print --paper A4" +
                " file A.pdf" +
                " file B.pdf --count 3" +
                " file C.pdf --count 3 --rotate left" +
                " file D.pdf" +
                " file E.pdf --rotate right").split(" "));
    }
}
