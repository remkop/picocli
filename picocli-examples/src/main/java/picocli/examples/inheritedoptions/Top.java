package picocli.examples.inheritedoptions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;

import static picocli.CommandLine.ScopeType.INHERIT;

@Command(name = "top")
public class Top implements Runnable {

    /**
     * Enables or disables debug mode, which is disabled by default.
     * @param debug The new state of debug mode.
     */
    @Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.",
            scope = INHERIT)
    protected void setDebug(final boolean debug) {
        System.out.println("Debug has been set to " + debug);
    }

    @Override
    public void run() {
        System.out.println("Running " + this);
    }

    @Command
    void sub() {
        System.out.println("Running sub");
    }

    public static void main(String[] args) {
        String[][] input = {
                {"--debug"},
                {"sub", "--debug"}
        };
        if (args.length > 0) {
            new CommandLine(new Top()).execute(args);
        } else {
            for (int i = 0; i < input.length; i++) {
                System.out.printf("Executing test input %d: %s%n", i, Arrays.toString(input[i]));
                new CommandLine(new Top()).execute(input[i]);
            }
        }
    }
}
