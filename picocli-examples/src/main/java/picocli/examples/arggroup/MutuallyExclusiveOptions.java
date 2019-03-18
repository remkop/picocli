package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.MutuallyExclusiveArgsException;
import picocli.CommandLine.Option;

public class MutuallyExclusiveOptions {

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = "-a") int a;
        @Option(names = "-b") int b;
        @Option(names = "-c") int c;
    }

    public static void main(String[] args) {
        MutuallyExclusiveOptions example = new MutuallyExclusiveOptions();
        CommandLine cmd = new CommandLine(example);

        try {
            cmd.parseArgs("-a=1", "-b=2");
        } catch (MutuallyExclusiveArgsException ex) {
            assert "Error: -a=<a>, -b=<b> are mutually dependent (specify only one)".equals(ex.getMessage());
        }
    }
}
