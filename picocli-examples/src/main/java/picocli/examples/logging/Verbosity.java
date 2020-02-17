package picocli.examples.logging;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import static picocli.CommandLine.Spec.Target.MIXEE;

/**
 * This is a mixin that adds a {@code --verbose} option to a command.
 * If this option is specified, the implementation will delegate
 * the new value to the top-level command.
 */
public class Verbosity {
    @Spec(MIXEE) CommandSpec spec;

    /**
     * Returns the {@link CommandSpec} of the top-level command.
     * @return the top-level command spec
     */
    private CommandSpec topLevelCommand() {
        CommandSpec result = spec;
        while (result.parent() != null) {
            result = result.parent();
        }
        return result;
    }

    /**
     * Delegates the specified verbosity to the top-level command.
     * @param verbosity the new verbosity value
     */
    @Option(names = "-v")
    public void setVerbose(boolean[] verbosity) {
        CommandSpec topLevelCommand = topLevelCommand();
        MyApp myApp = topLevelCommand.commandLine().getCommand();
        myApp.verbosity = verbosity;
    }

}
