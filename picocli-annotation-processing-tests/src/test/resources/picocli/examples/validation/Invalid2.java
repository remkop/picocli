package picocli.examples.validation;

import static picocli.CommandLine.Option;

public class Invalid2 {

    @Option(names = "--negatable", negatable = true)
    int invalidNegatableShouldBeBoolean;
}
