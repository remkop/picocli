package picocli.issue850missingmixin;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * The entry point to the application.
 */
@Command(
    synopsisSubcommandLabel = "COMMAND",
    subcommands = {
//        ActivateBitbucketPipelinesCommand.class,
//        ActivateTravisCommand.class,
//        CreateCommand.class,
//        DeactivateBitbucketPipelinesCommand.class,
//        DeactivateTravisCommand.class,
//        DeleteCommand.class,
//        DownCommand.class,
        InitCommand.class,
//        ListCommand.class,
//        MergePullRequestsCommand.class,
//        UpCommand.class,
//        UpdateLicenseCommand.class
    }
)
public class App implements Callable<Integer> {

    @Command
    public int commandMethod(@Option(names = "-x") int x, @Mixin ProviderMixin pm) {
        return 0;
    }
    /**
     * Runs the application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        System.exit(
            new CommandLine(new App())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args)
        );
    }

    @Override
    public Integer call() {
        System.out.println("Missing sub-command");
        return -1;
    }
}
