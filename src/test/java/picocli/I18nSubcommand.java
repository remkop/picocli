package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "i18l-sub",
        resourceBundle = "picocli.SharedMessages",
        subcommands = CommandLine.HelpCommand.class,
        mixinStandardHelpOptions = true,
        description = {"orig desc 1", "orig desc 2", "orig desc 3"},
        descriptionHeading = "orig desc heading%n",
        header = {"orig header 1", "orig header 2", "orig header 3"},
        headerHeading = "orig header heading%n",
        footer = {"orig footer 1", "orig footer 2", "orig footer 3"},
        footerHeading = "orig footer heading%n",
        commandListHeading = "Orig command list heading%n",
        optionListHeading = "Orig option list heading%n",
        parameterListHeading = "Orig param list heading%n")
public class I18nSubcommand {
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
