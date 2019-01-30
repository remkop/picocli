package picocli.examples.mixin;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "mixee", description = "This command has a footer and an option mixed in")
public class CommandWithMixin {
    @Mixin
    CommonOption commonOption = new CommonOption();

    @Option(names = "-y", description = "command option")
    int y;

    @Command
    public void doit(@Mixin CommonOption commonOptionParam,
                     @Option(names = "-z") int z,
                     @Parameters String arg0,
                     String arg1) {}

    public static void main(String[] args) {
        CommandWithMixin cmd = new CommandWithMixin();
        new CommandLine(cmd).parseArgs("-x", "3", "-y", "4");

        System.out.printf("x=%s, y=%s%n", cmd.commonOption.x, cmd.y);
    }
}
