package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.MutuallyExclusiveArgsException;
import picocli.CommandLine.Option;

/**
 * <p>
 * The example below defines a command with mutually exclusive options `-a`, `-b` and `-c`.
 * </p><p>
 * Note that the options are defined as {@code required = true};
 * this means required <em>within the group</em>, not required within the command.
 * </p><p>
 * The group itself has a `multiplicity` attribute that defines how many times
 * the group may be specified within the command.
 * The default is {@code multiplicity = "0..1"}, meaning that by default a group
 * may be omitted or specified once. In this example the group has {@code multiplicity = "1"},
 * so one of the options must occur on the command line.
 * </p><p>
 * The synopsis of this command is rendered as {@code <main class> (-a=<a> | -b=<b> | -c=<c>)}.
 * </p>
 */
public class MutuallyExclusiveOptionsDemo {

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @Option(names = "-a", required = true) int a;
        @Option(names = "-b", required = true) int b;
        @Option(names = "-c", required = true) int c;
    }

    public static void main(String[] args) {
        MutuallyExclusiveOptionsDemo example = new MutuallyExclusiveOptionsDemo();
        CommandLine cmd = new CommandLine(example);

        try {
            cmd.parseArgs("-a=1", "-b=2");
        } catch (MutuallyExclusiveArgsException ex) {
            assert "Error: -a=<a>, -b=<b> are mutually dependent (specify only one)".equals(ex.getMessage());
        }
    }
}
