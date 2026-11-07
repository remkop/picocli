package picocli.issue2426;

import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "app", mixinStandardHelpOptions = true, version = "app 1.0")
public class AppNoConflict {

    /** Option names do not overlap with the standard help option names. */
    @Option(names = {"-H", "--host"}, description = "The host to connect to.")
    String host;
}
