package picocli.examples.casesensitivity;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;

@Command(name = "POSIX Option Resemble Demo",
  description = "Demonstrates multiple POSIX options resembling a long option.")
public class POSIXOptionResembleDemo implements Runnable {

    @Option(names = "-a")
    boolean a;

    @Option(names = "-b")
    boolean b;

    @Option(names = "-c")
    boolean c;

    @Option(names = "-ABC")
    boolean abc;

    @Override
    public void run() {
        System.out.printf("-a is %s%n" +
                "-b is %s%n" +
                "-c is %s%n" +
                "-ABC is %s%n", a, b, c, abc);
    }

    public static void main(String... args) {
        if (args.length == 0) {
            args = new String[]{"-abc"};
        }
        System.out.println("Original args: " + Arrays.toString(args));

        System.out.println();
        System.out.println("Parsing in default mode...");
        new CommandLine(new POSIXOptionResembleDemo())
                .execute(args);

        System.out.println();
        System.out.println("Parsing in case insensitive mode...");
        new CommandLine(new POSIXOptionResembleDemo())
                .setOptionsCaseInsensitive(true)
                .execute(args);
    }
}
