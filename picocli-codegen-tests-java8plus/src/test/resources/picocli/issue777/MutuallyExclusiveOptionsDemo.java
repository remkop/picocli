package picocli.issue777;

import picocli.CommandLine.*;

@Command(name = "exclusivedemo")
public class MutuallyExclusiveOptionsDemo {

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }
}
