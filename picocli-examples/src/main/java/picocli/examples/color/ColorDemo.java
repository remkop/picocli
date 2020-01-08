package picocli.examples.color;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;

@Command(name = "colordemo")
public class ColorDemo implements Runnable {

    @Override
    public void run() {
        String str = Ansi.AUTO.string("@|bold,green,underline Hello, colored world!|@");
        System.out.println(str);
    }

    public static void main(String[] args) {
        new CommandLine(new ColorDemo()).execute(args);
    }
}
