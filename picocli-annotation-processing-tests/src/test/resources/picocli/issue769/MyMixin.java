package picocli.issue769;

import picocli.CommandLine;

class MyMixin {
    @CommandLine.Option(names = {"--some-option"})
    public String someOption;
}

@CommandLine.Command(name = "SubCommand")
class SubCommand {
    @CommandLine.Mixin
    public MyMixin someMixin;
}

@CommandLine.Command(name = "Command", subcommands = {SubCommand.class})
class MyCommand {
}
