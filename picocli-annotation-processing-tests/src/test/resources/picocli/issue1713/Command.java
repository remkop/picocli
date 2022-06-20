package picocli.issue1713;

import java.util.Optional;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "Command",
    description = "Command description")
class Command1 {

    @Spec
    CommandSpec spec;

    @CommandLine.Option(names = "--progress",
        description = "A negatable optional boolean should be allowed",
        negatable = true)
    Optional<Boolean> progress;
}
