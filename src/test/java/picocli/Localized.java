package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "i18l",
        subcommands = CommandLine.HelpCommand.class,
        mixinStandardHelpOptions = true,
        description = {"orig desc 1", "orig desc 2", "orig desc 3"},
        descriptionHeading = "orig desc heading",
        header = {"orig header 1", "orig header 2", "orig header 3"},
        headerHeading = "orig header heading",
        footer = {"orig footer 1", "orig footer 2", "orig footer 3"},
        footerHeading = "orig footer heading",
        commandListHeading = "Orig command list heading",
        optionListHeading = "Orig option list heading",
        parameterListHeading = "Orig param list heading")
public class Localized {
    @Option(names = {"-x", "--xxx"})
    String x;

    @Option(names = {"-y", "--yyy"}, description = {"orig yyy description 1", "orig yyy description 2"})
    String y;

    @Option(names = {"-z", "--zzz"}, description = "orig zzz description")
    String z;

    @Parameters(index = "0")
    String param0;

    @Parameters(index = "1", description = "orig param1 description")
    String param1;
}
