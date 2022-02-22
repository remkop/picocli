package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

/**
 * Shows how to use a mixin to add an arg group to a command and all subcommands.
 *
 * <p>
 *   Prints out the following:
 * </p>
 * <pre>
 * ---------------
 * Usage: command [-hV] [--global-opt-1=<global1>] [--global-opt-2=<global2>]
 *                [COMMAND]
 *   -h, --help      Show this help message and exit.
 *   -V, --version   Print version information and exit.
 *
 * Shared options that work on every command:
 *       --global-opt-1=<global1>
 *                   I'm nice.
 *       --global-opt-2=<global2>
 *                   I'm nicer.
 * Commands:
 *   sub1
 *   sub2
 * ---------------
 * Usage: command sub1 [-hV] [--global-opt-1=<global1>] [--global-opt-2=<global2>]
 *                     [--sub1-opt-1=<opt1>] [--sub1-opt-2=<opt2>] [COMMAND]
 *   -h, --help                Show this help message and exit.
 *       --sub1-opt-1=<opt1>   I'm ok.
 *       --sub1-opt-2=<opt2>   I'm oker.
 *   -V, --version             Print version information and exit.
 *
 * Shared options that work on every command:
 *       --global-opt-1=<global1>
 *                             I'm nice.
 *       --global-opt-2=<global2>
 *                             I'm nicer.
 * Commands:
 *   sub1sub1
 * ---------------
 * Usage: command sub1 sub1sub1 [-hV] [--global-opt-1=<global1>]
 *                              [--global-opt-2=<global2>]
 *                              [--sub1-sub1-opt-1=<arg1>]
 *                              [--sub1-sub1-opt-2=<arg2>]
 *   -h, --help      Show this help message and exit.
 *       --sub1-sub1-opt-1=<arg1>
 *                   I'm sub ok.
 *       --sub1-sub1-opt-2=<arg2>
 *                   I'm sub oker.
 *   -V, --version   Print version information and exit.
 *
 * Shared options that work on every command:
 *       --global-opt-1=<global1>
 *                   I'm nice.
 *       --global-opt-2=<global2>
 *                   I'm nicer.
 * </pre>
 */
public class ArgGroupMixinDemo {

    @Command(mixinStandardHelpOptions = true) // add --help and --version to all commands that have this mixin
    static class MyMixin {
        @ArgGroup(heading = "%nShared options that work on every command:%n", validate = false)
        MyGroup group; // = new MyGroup(); //https://picocli.info/#_assigning_default_values_in_argument_groups
    }
    static class MyGroup {
        @Option(names = "--global-opt-1", description = "I'm nice.") String global1;
        @Option(names = "--global-opt-2", description = "I'm nicer.") String global2;
    }

    @Command(name = "command", subcommands = {Sub1.class, Sub2.class})
    static class MyCommand implements Runnable {
        @Mixin MyMixin myMixin;

        public void run() { }
    }

    @Command(name = "sub1")
    static class Sub1 implements Runnable {
        @Mixin MyMixin myMixin;

        @Option(names = "--sub1-opt-1", description = "I'm ok.") String opt1;
        @Option(names = "--sub1-opt-2", description = "I'm oker.") String opt2;
        public void run() { }

        @Command
        public void sub1sub1(
            @Mixin MyMixin mixin,
            @Option(names = "--sub1-sub1-opt-1", description = "I'm sub ok.") String subopt1,
            @Option(names = "--sub1-sub1-opt-2", description = "I'm sub oker.") String subopt2
        ) {}
    }

    @Command(name = "sub2")
    static class Sub2 implements Runnable {
        @Mixin MyMixin myMixin;

        @Option(names = "--sub2-opt-1", description = "I'm ok.") String opt1;
        @Option(names = "--sub2-opt-2", description = "I'm oker.") String opt2;
        public void run() { }
    }

    public static void main(String[] args) {
        System.out.println("---------------");
        new CommandLine(new MyCommand()).execute("--help");

        System.out.println("---------------");
        new CommandLine(new MyCommand()).execute("sub1", "--help");

        System.out.println("---------------");
        new CommandLine(new MyCommand()).execute("sub1", "sub1sub1", "--help");
    }
}
