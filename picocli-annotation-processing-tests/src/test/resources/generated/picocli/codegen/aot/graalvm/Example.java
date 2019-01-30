package generated.picocli.codegen.aot.graalvm;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.aot.graalvm.Example.ExampleMixin;

@Command(name = "example",
         mixinStandardHelpOptions = true,
         subcommands = HelpCommand.class,
         version = "3.7.0")
public class Example {
    @Mixin ExampleMixin mixin;

    @Option(names = "-t")
    TimeUnit timeUnit;

    @Option(names = "--minimum")
    public void setMinimum(int min) {
        // TODO replace the stored value with the new value
    }

    @Parameters(index = "0")
    File file;

    @Parameters(index = "1..*",
                type = File.class)
    public void setOtherFiles(List<File> otherFiles) {
        // TODO replace the stored value with the new value
    }

    @Command
    public static class ExampleMixin {
        @Option(names = "-l")
        int length;
    }

    @Command(name = "multiply")
    int multiply(
        @Option(names = "--count") int count,
        @Parameters(index = "0") int multiplier) {
        // TODO implement commandSpec
    }
}
