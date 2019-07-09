package picocli.examples.validation;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import static picocli.CommandLine.Option;

public class InvalidSplit {

    @Option(names = "-x", split = ",")
    int singleOption;

    @Parameters(split = ",")
    String singlePositional;
}
