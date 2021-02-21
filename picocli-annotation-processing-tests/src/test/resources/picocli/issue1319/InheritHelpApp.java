package picocli.issue1319;

import picocli.CommandLine;

@CommandLine.Command(scope = CommandLine.ScopeType.INHERIT
        , mixinStandardHelpOptions = true
        , subcommands = { CommandLine.HelpCommand.class }
)
class InheritHelpApp  {
    int subFoo;

    @CommandLine.Command()
    void sub(@CommandLine.Option(names = "-foo") int foo) {
        subFoo = foo;
    }
}
