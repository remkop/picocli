package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Command(name = "example", version = "4.0.0",
        mixinStandardHelpOptions = true, subcommands = CommandLine.HelpCommand.class)
public interface ExampleInterface {

    @Spec
    CommandSpec spec();

    @Option(names = "-t", defaultValue = "SECONDS")
    TimeUnit timeUnit();

    @Parameters(index = "0")
    File file();

    @Option(names = "--minimum")
    int minimum();

    @Parameters(index = "1..*")
    List<File> otherFiles();
}
