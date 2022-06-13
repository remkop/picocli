package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Mixin;

@Command(name = "i18n-top",
        resourceBundle = "picocli.SharedMessages",
        subcommands = {CommandLine.HelpCommand.class, I18nSubcommand.class, I18nSubcommand2.class },
        mixinStandardHelpOptions = true,
        description = {"top desc 1", "top desc 2", "top desc 3"},
        descriptionHeading = "top desc heading%n",
        header = {"top header 1", "top header 2", "top header 3"},
        headerHeading = "top header heading%n",
        footer = {"top footer 1", "top footer 2", "top footer 3"},
        footerHeading = "top footer heading%n",
        commandListHeading = "top command list heading%n",
        optionListHeading = "top option list heading%n",
        parameterListHeading = "top param list heading%n")
public class I18nCommand {
    @Option(names = {"-x", "--xxx"})
    String x;

    @Option(names = {"-y", "--yyy"}, description = {"top yyy description 1", "top yyy description 2"})
    String y;

    @Option(names = {"-z", "--zzz"}, description = "top zzz description")
    String z;

    @Parameters(index = "0")
    String param0;

    @Parameters(index = "1", description = "top param1 description")
    String param1;

    @Option(
        names = {"--optionWithDescriptionFromParent"},
        descriptionKey = "optionWithDescriptionFromParent",
        scope = CommandLine.ScopeType.INHERIT
    )
    public boolean parentOption1;

    @Override
    public String toString() {
        return getClass().getName();
    }
}
