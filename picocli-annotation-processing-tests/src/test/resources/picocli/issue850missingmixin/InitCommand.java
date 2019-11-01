package picocli.issue850missingmixin;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Initializes an existing git repository.
 */
@Command(
    name = "init",
    description = {"Initializes an existing git repository"}
)
class InitCommand implements Callable<Integer> {
    @Option(
        names = {"--name"},
        required = true,
        description = {"The name of the repository"}
    )
    private String name;

    @Mixin
    private ProviderMixin providerMixin;

    @Option(
        names = {"--description"},
        required = true,
        description = {"The description of the repository"}
    )
    private String description;

    @Option(
        names = {"--language"},
        required = true,
        description = {"The language of the repository"}
    )
    private String language;

    @Option(
        names = {"--clone-dir"},
        required = true,
        description = {"The directory in which the repository should be cloned"}
    )
    private String cloneDir;

    @Option(
        names = {"--travis-badge"},
        required = false,
        description = {"Add the Travis badge in the README file"}
    )
    private boolean travisBadge;

    @Override
    public Integer call() {
        System.out.println("hello init");
        return 0;
    }
}
