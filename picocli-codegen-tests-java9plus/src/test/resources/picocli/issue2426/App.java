package picocli.issue2426;

import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "app", mixinStandardHelpOptions = true, version = "app 1.0")
public class App {

    /** Option name "-h" overlaps with the standard help option added by mixinStandardHelpOptions. */
    @Option(names = {"-h", "--host"}, description = "The host to connect to.")
    String host;
}
