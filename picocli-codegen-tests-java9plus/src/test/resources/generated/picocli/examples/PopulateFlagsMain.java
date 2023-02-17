package generated.picocli.examples;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

public class PopulateFlagsMain {
    @Command
    private static class Options {
        @Option(names = "-b")
        private boolean buffered;

        @Option(names = "-o")
        private boolean overwriteOutput;

        @Option(names = "-v")
        private boolean verbose;
    }
}
