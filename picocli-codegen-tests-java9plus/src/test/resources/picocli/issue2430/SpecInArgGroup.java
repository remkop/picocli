package picocli.issue2430;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "spec-in-arggroup")
class SpecInArgGroup {

    static class Options {
        @Spec
        CommandSpec spec;

        @Option(names = "-x")
        int x;
    }

    @ArgGroup(exclusive = false)
    Options options = new Options();
}
