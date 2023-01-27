package generated.picocli.issue769;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.issue769.MyMixin;

@Command(name = "SubCommand")
class SubCommand {
    @Mixin MyMixin someMixin;
}
