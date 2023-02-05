package picocli.examples.validation;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.ParentCommand;
import static picocli.CommandLine.Spec;
import static picocli.CommandLine.Unmatched;

@Command(subcommands = {Invalid.Sub1.class, Invalid.Sub2.class})
public class Invalid {

    @Option(names = "-a")
    @Parameters
    int invalidOptionAndParameters;

    @Mixin
    int invalidPrimitiveMixin;

    @Option(names = "-b")
    @Mixin
    Integer invalidOptionAndMixin;

    @Option(names = "-c")
    @Unmatched
    int invalidOptionAndUnmatched;

    @Option(names = "-d")
    @Spec
    int invalidOptionAndSpec;

    @Option(names = "-e")
    @ParentCommand
    int invalidOptionAndParentCommand;

    // ---
    @Parameters
    @Mixin
    Integer invalidParametersAndMixin;

    @Parameters
    @Unmatched
    int invalidParametersAndUnmatched;

    @Parameters
    @Spec
    int invalidParametersAndSpec;

    @Parameters
    @ParentCommand
    int invalidParametersAndParentCommand;

    // ---
    @Unmatched
    @Mixin
    Integer invalidUnmatchedAndMixin;

    @Unmatched
    @Spec
    int invalidUnmatchedAndSpec;

    @Unmatched
    @ParentCommand
    int invalidUnmatchedAndParentCommand;

    // ---
    @Spec
    @Mixin
    Integer invalidSpecAndMixin;

    @Spec
    @ParentCommand
    int invalidSpecAndParentCommand;

    // ---
    @ParentCommand
    @Mixin
    Integer invalidParentCommandAndMixin;

    static class Sub1 {
        @Parameters String[] params;
    }

    @Command
    static class Sub2 {
        @Parameters String[] params;
    }
}
