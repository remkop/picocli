package picocli.examples.defaultprovider;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * This example demonstrates a command that allows users to specify the location of
 * a "profile" file where the default values are stored.
 * <p>
 * We take a two-pass approach to loading defaults, where the user-specified arguments
 * are parsed twice, once to get the profile path, and the next (final) time with
 * a default provider that is initialized from the specified profile path.
 * </p>
 */
public class TwoPass {
    @Command(mixinStandardHelpOptions = true)
    static class FirstPass {
        @Option(names = "--profile", description = "...")
        File profilePath = new File(System.getProperty("user.dir"), "my-command.properties");

        @Unmatched
        List<String> remainder;
    }

    @Command(name = "my-command", mixinStandardHelpOptions = true, version = "...",
            description = "...")
    static class FinalPass implements Runnable {
        @Option(names = "--profile", description = "...")
        File profilePath = new File(System.getProperty("user.dir"), "my-command.properties");

        @Option(names = "-a", description = "...")
        int a;

        @Option(names = "-b", description = "...", descriptionKey = "bbb")
        int b;

        @Option(names = "--long-option")
        String value;

        @Spec CommandLine.Model.CommandSpec spec;

        @Override
        public void run() {
            System.out.println("Executing the business logic...");
            System.out.printf("profile=%s%n", profilePath);
            System.out.printf("a=%s%n", a);
            System.out.printf("b=%s%n", b);
            System.out.printf("long-option=%s%n", value);
            System.out.println("Command line args:");
            System.out.printf("%s%n", spec.commandLine().getParseResult().originalArgs());
        }
    }

    public static void main(String[] args) throws IOException {
        // create some defaults
        Properties profile = new Properties();
        profile.setProperty("a", "111111");
        profile.setProperty("bbb", "99999999");
        profile.setProperty("long-option", "this is the default for long-option");

        // store them in a file
        File path = File.createTempFile("twopass", ".properties");
        profile.store(new FileWriter(path), "Default values for my-command");

        args = ("--profile=" + path.getAbsolutePath() + " -b123 --long-option USER-SPECIFIED").split(" ");

        FirstPass firstPass = new FirstPass();
        CommandLine cmd = new CommandLine(firstPass);
        cmd.parseArgs(args); // first pass
        if (cmd.isUsageHelpRequested()) {
            new CommandLine(new FinalPass()).usage(System.out);
        } else if (cmd.isVersionHelpRequested()) {
            new CommandLine(new FinalPass()).printVersionHelp(System.out);
        } else {
            // final pass
            int exitCode = new CommandLine(new FinalPass()).
                    setDefaultValueProvider(new PropertiesDefaultProvider(firstPass.profilePath))
                    .execute(args);
            System.exit(exitCode);
        }
    }
}
