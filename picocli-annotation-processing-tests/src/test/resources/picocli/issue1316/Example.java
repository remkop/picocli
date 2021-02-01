package picocli.issue1316;

import picocli.CommandLine;
import picocli.AutoComplete;

@CommandLine.Command(
        name = "example",
        mixinStandardHelpOptions = true,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = AutoComplete.GenerateCompletion.class
)
public class Example {
}