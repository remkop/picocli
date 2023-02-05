package picocli.issue793;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@CommandLine.Command
public class Issue793 {
    static class Mode {
        @CommandLine.Option(names = {"-e", "--encrypt"}, required = true) boolean encrypt;
        @CommandLine.Option(names = {"-d", "--decrypt"}, required = true) boolean decrypt;
    }

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    Mode mode;

    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = true) Exclusive exclusive;
        @CommandLine.ArgGroup(exclusive = false) Dependent dependent;
    }
    static class Exclusive {
        @CommandLine.Option(names = {"-a"}, required = true) boolean a;
        @CommandLine.Option(names = {"-b"}, required = true) boolean b;
    }
    static class Dependent {
        @CommandLine.Option(names = {"-f"}, required = true) boolean f;
        @CommandLine.Option(names = {"-g"}, required = true) boolean g;
    }

    @CommandLine.Command(name = "nested")
    int method(@CommandLine.ArgGroup Composite composite) {
        return 0;
    }
}
