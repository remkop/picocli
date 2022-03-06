package picocli.examples.module.simple;

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(name = "modular-cli", mixinStandardHelpOptions = true, version = CommandLine.VERSION)
public class ModularCli implements Runnable {

    @Spec Model.CommandSpec spec;

    @Option(names = "--option")
    String option;

    public void run() {
        spec.commandLine().getOut().printf("Hello modular world!%n");
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new ModularCli()).execute(args));
    }
}
