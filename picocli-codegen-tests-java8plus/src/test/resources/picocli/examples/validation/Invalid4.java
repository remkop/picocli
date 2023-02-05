package picocli.examples.validation;

import static picocli.CommandLine.Option;

public class Invalid4 {

    @Option(names = "--usageHelp", usageHelp = true)
    int invalidUsageHelpShouldBeBoolean;

    @Option(names = "--versionHelp", versionHelp = true)
    int invalidVersionHelpShouldBeBoolean;

    @Option(names = "--usageHelp2", usageHelp = true)
    boolean invalidDuplicateUsageHelp;

    @Option(names = "--versionHelp2", versionHelp = true)
    boolean invalidDuplicateVersionHelp;

    @Option(names = "--versionAndUsageHelp", versionHelp = true, usageHelp = true)
    boolean invalidDuplicateUsageAndVersionHelp;

//    @Option(names = "--help", help = true)
//    int invalidHelpShouldBeBoolean;

}
