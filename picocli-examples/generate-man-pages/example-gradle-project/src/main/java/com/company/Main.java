package com.company;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "top", mixinStandardHelpOptions = true, showAtFileInUsageHelp = true,
        version = {"test 1.0", "picocli " + CommandLine.VERSION},
        header = "display Linux processes",
        description = { 
                "The @|bold top|@ program provides a dynamic real-time view of a running " +
                "system.  It can display system summary information as well as a list " +
                "of @|bold processes|@ or @|bold threads|@ currently being managed by the Linux kernel. " +
                "The types of system summary information shown and the types, order " +
                "and size of information displayed for processes are all user " +
                "configurable and that configuration can be made persistent across " +
                "restarts.",
                "",
                "The program provides a limited interactive interface for process " +
                "manipulation as well as a much more extensive interface for personal " +
                "configuration  --  encompassing every aspect of its operation.  And " +
                "while @|bold top|@ is referred to throughout this document, you are free to " +
                "name the program anything you wish.  That new name, possibly an " +
                "alias, will then be reflected on top's display and used when reading " +
                "and writing a configuration file."},
        optionListHeading = "COMMAND-LINE Options%n"
)
public class Main implements Callable<Integer> {

    @Option(names = "-b", description = {"Batch-mode operation.",
            "Starts top in Batch mode, which could be useful for sending " +
            "output from top to other programs or to a file.  In this mode, " +
            "top will not accept input and runs until the iterations limit " +
            "you've set with the `-n` command-line option or until killed."})
    boolean batchMode;

    @Option(names = "-c", description = {"Command-line/Program-name toggle.",
            "Starts top with the last remembered `c` state reversed.  Thus, " +
            "if top was displaying command lines, now that field will show " +
            "program names, and vice versa.  See the `c` interactive command " +
            "for additional information."})
    boolean commandMode;

    @Option(names = "-d", description = {"Delay-time interval as:  `-d ss.t` (secs.tenths)",
            "Specifies the delay between screen updates, and overrides the " +
            "corresponding value in one's personal configuration file or the " +
            "startup default.  Later this can be changed with the `d` or `s` " +
            "interactive commands.",
            "",
            "Fractional seconds are honored, but a negative number is not allowed."})
    String delayTime;

    @Option(names = "-H", description = {"Threads-mode operation.",
            "Instructs top to display individual threads.  Without this " +
            "command-line option a summation of all threads in each process " +
            "is shown.  Later this can be changed with the `H` interactive command."})
    boolean threadsMode;

    @Option(names = "-n", description = {"Number-of-iterations limit as:  `-n number`",
            "Specifies the maximum number of iterations, or frames, top " +
            "should produce before ending."})
    int number;


    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Hi!");
        return 0;
    }
}
