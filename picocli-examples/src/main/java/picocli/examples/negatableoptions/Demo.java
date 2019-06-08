package picocli.examples.negatableoptions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "negatable-options-demo", mixinStandardHelpOptions = true,
        version = {"picocli version " + CommandLine.VERSION,
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"})
public class Demo implements Runnable {

    @Option(names = "--verbose", negatable = true, description = "Show verbose output")
    boolean verbose;

    @Option(names = "-XX:+PrintGCDetails", negatable = true, description = "Prints GC details")
    boolean printGCDetails;

    @Option(names = "-XX:-UseG1GC", negatable = true, description = "Use G1 algorithm for GC")
    boolean useG1GC = true;

    @Override
    public void run() {
        System.out.printf("verbose: %s, printGCDetails: %s, useG1GC: %s%n", verbose, printGCDetails, useG1GC);
        System.out.println("System file encoding: " + System.getProperty("file.encoding"));
    }

    public static void main(String[] args) {
        new CommandLine(new Demo()).execute(args);
    }
}
