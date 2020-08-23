package picocli.issue1151;

import picocli.CommandLine;
import picocli.codegen.docgen.manpage.ManPageGenerator;

@CommandLine.Command(name = "issue1151", version = "1151",
        subcommands = ManPageGenerator.class,
        description = "ManPageGenerator as subcommand with native-image throws exception")
public class Issue1151CommandWithManPageGeneratorSubcommand implements Runnable {
    @Override
    public void run() {
        System.out.printf("You just executed issue1151%n");
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Issue1151CommandWithManPageGeneratorSubcommand()).execute(args));
    }
}
