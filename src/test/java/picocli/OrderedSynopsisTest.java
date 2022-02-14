package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

// https://github.com/remkop/picocli/issues/1408
public class OrderedSynopsisTest {

//    @Ignore("#1408")
    @Test
    public void testIssue1408() {
        @Command(name = "myCommand",
            mixinStandardHelpOptions = true,
            description = "A command with explicitly ordered options.",
            sortSynopsis = false,
            sortOptions = false)
        class Example {
            @Option(names = { "-d", "--option-d" }, order = -10, description = "Should be first")
            private void setD(String value) {
                this.d = value;
            }
            private String d;

            @Option(names = { "-c", "--option-c" }, order = -9, description = "Should be second")
            private void setC(String value) {
                this.c = value;
            }
            private String c;

            @Option(names = { "-b", "--option-b" }, order = -8, description = "Should be third")
            private String b;

            @Option(names = { "-a", "--option-a" }, order = -7, description = "Should be fourth")
            private String a;
        }
        String actual = new CommandLine(new Example()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
            "Usage: myCommand [-hV] [-d=<d>] [-c=<c>] [-b=<b>] [-a=<a>]%n" +
            "A command with explicitly ordered options.%n" +
            "  -d, --option-d=<d>   Should be first%n" +
            "  -c, --option-c=<c>   Should be second%n" +
            "  -b, --option-b=<b>   Should be third%n" +
            "  -a, --option-a=<a>   Should be fourth%n" +
            "  -h, --help           Show this help message and exit.%n" +
            "  -V, --version        Print version information and exit.%n");
        assertEquals(expected, actual);
    }
}
