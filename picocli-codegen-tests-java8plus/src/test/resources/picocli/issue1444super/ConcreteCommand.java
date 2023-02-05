package picocli.issue1444super;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

@Command(name = "dummy", description = "Login to MySystem", sortOptions = false)
public class ConcreteCommand extends AbstractCommand {
    @ArgGroup(exclusive = false, multiplicity = "1", heading = "Connection options:%n", order = 1)
    private OtherOptions otherOptions;


    private static class OtherOptions {
        @Option(names = {"--other", "-o"}, required = false, defaultValue = "default")
        protected String option;
    }

}
