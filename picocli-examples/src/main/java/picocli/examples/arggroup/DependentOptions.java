package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.MutuallyExclusiveArgsException;
import picocli.CommandLine.Option;

public class DependentOptions {

    @ArgGroup(exclusive = false, multiplicity = "1")
    Dependent dependent;

    static class Dependent {
        @Option(names = "-a") int a;
        @Option(names = "-b") int b;
        @Option(names = "-c") int c;
    }

    public static void main(String[] args) {
        DependentOptions example = new DependentOptions();
        CommandLine cmd = new CommandLine(example);

        try {
            cmd.parseArgs("-a=1", "-b=2");
        } catch (MissingParameterException ex) {
            assert "Error: Missing required argument(s): -c=<c>".equals(ex.getMessage());
        }
    }
}
