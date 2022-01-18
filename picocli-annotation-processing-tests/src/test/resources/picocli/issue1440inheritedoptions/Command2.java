package picocli.issue1440inheritedoptions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "Command2", subcommands =  CommandLine.HelpCommand.class,
    description = "Command 1 description")
class Command2 {
    @Spec
    CommandSpec spec;

    @CommandLine.Option(names = "--option",
        scope = CommandLine.ScopeType.INHERIT,
        description = "If set to true, will not ask questions and configurations, e.g. to overwrite a file. We don't recommend setting this option to true unless you are working in a scripted environment")
    String option = "";
}
