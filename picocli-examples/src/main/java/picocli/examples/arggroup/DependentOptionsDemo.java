package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Option;

/**
 * <p>
 * The example below sets {@code exclusive = false}, and defines a command with
 * dependent options `-a`, `-b` and `-c` that must co-occur.
 * </p><p>
 * Note that the options are defined as {@code required = true}; this means required
 * <em>within the group</em>, not required within the command.
 * </p><p>
 * The group itself has a `multiplicity` attribute that defines how many times
 * the group may be specified within the command. In this example the group has the
 * default multiplicity, {@code multiplicity = "0..1"},
 * meaning that the group may be omitted or specified once.
 * </p><p>
 * The synopsis of this command is rendered as {@code <main class> [-a=<a> -b=<b> -c=<c>]}.
 * </p>
 */
public class DependentOptionsDemo {

    @ArgGroup(exclusive = false)
    Dependent dependent;

    static class Dependent {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }

    public static void main(String[] args) {
        DependentOptionsDemo example = new DependentOptionsDemo();
        CommandLine cmd = new CommandLine(example);

        try {
            cmd.parseArgs("-a=1", "-b=2");
        } catch (MissingParameterException ex) {
            assert "Error: Missing required argument(s): -c=<c>".equals(ex.getMessage());
        }
    }
}
