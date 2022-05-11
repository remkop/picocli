package picocli.examples.inheritedoptions;

import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

/**
 * Demonstrates how to avoid DuplicateOptionAnnotationsException
 * (inspired by https://github.com/remkop/picocli/issues/1673)
 * in this case, an inherited --help option clashes with a
 * subcommand that already has a --help option.
 */
@Command(name = "top" /*, subcommands = AutoComplete.GenerateCompletion.class*/)
public class DuplicateOptionsWorkaround implements Runnable {

    @Option(names = {"-h", "--help"}, scope = CommandLine.ScopeType.INHERIT, description = "Display usage help and exit.")
    boolean usageHelpRequested;

    public void run() {
        System.out.println("Hello, world!");
    }

    public static void main(String[] args) {
        CommandLine top = new CommandLine(new DuplicateOptionsWorkaround());

        // create a CommandSpec for the subcommand
        CommandSpec sub = CommandSpec.forAnnotatedObject(new AutoComplete.GenerateCompletion());

        // remove the duplicate option from the subcommand
        sub.remove(sub.findOption("--help"));

        // add the subcommand manually
        top.addSubcommand(sub);

        // and run the application
        top.execute(args);
    }
}
