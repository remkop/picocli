package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "example")
public class Issue930Example implements Runnable {
    String needy = "needs";

    public Issue930Example(String need) {
        needy += " "+need;
    }
    public static void main(String[] args) {new CommandLine(new Issue930Example("a test")).execute(args);}

    @Override
    public void run() {
        System.out.println(needy);
    }
}