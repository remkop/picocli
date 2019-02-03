package picocli.codegen.aot.graalvm;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "sub")
public class Issue622Command2Sub extends Issue622AbstractCommand {
    @Option(names = "-x")
    int x;
}
