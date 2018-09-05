package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "i18n-sub",
        subcommands = CommandLine.HelpCommand.class,
        mixinStandardHelpOptions = true,
        description = {"subcmd desc 1", "subcmd desc 2", "subcmd desc 3"},
        descriptionHeading = "subcmd desc heading%n",
        header = {"subcmd header 1", "subcmd header 2", "subcmd header 3"},
        headerHeading = "subcmd header heading%n",
        footer = {"subcmd footer 1", "subcmd footer 2", "subcmd footer 3"},
        footerHeading = "subcmd footer heading%n",
        commandListHeading = "subcmd command list heading%n",
        optionListHeading = "subcmd option list heading%n",
        parameterListHeading = "subcmd param list heading%n")
public class I18nSubcommand {
    @Option(names = {"-x", "--xxx"})
    String x;

    @Option(names = {"-y", "--yyy"}, description = {"subcmd yyy description 1", "subcmd yyy description 2"})
    String y;

    @Option(names = {"-z", "--zzz"}, description = "subcmd zzz description")
    String z;

    @Parameters(index = "0")
    String param0;

    @Parameters(index = "1", description = "subcmd param1 description")
    String param1;
}
