package picocli.examples.casesensitivity;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "POSIX Option Resemble Demo",
description = "This shows what will happen if multiple POSIX options resemble a long option")
public class POSIXOptionResembleDemo implements Runnable {

    @Option(names = "-a", required = false)
    boolean a;

    @Option(names = "-b", required = false)
    boolean b;

    @Option(names = "-c", required = false)
    boolean c;

    @Option(names = "-abc", required = false)
    boolean abc;

    @Override
    public void run() {
        System.out.println("a is " + a + "\n" +
                "b is " + b + "\n" +
                "c is " + c + "\n" +
                "abc is " + abc);
    }

    public static void main(String... args){
        new CommandLine(new POSIXOptionResembleDemo())
                .setOptionsCaseInsensitive(true)
                .execute(args);
    }
}
