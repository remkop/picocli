package picocli.codegen.aot.graalvm;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "cmd2", subcommands = Issue622Command2Sub.class)
public class Issue622Command2 extends Issue622AbstractCommand {
    @Option(names = "-x")
    int x;
}
