package picocli.codegen.aot.graalvm;

import picocli.CommandLine.Command;

@Command(name = "app", subcommands = {Issue622Command1.class, Issue622Command2.class})
public class Issue622App extends Issue622AbstractCommand {
}
