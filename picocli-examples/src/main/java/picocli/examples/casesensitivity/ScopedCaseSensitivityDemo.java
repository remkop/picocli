package picocli.examples.casesensitivity;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "Scoped Case Sensitivity Demo")
public class ScopedCaseSensitivityDemo implements Runnable {

    @Option(names = {"-n", "--name"}, required = false, defaultValue = "User")
    String name;

    @Command(name = "greet")
    public int greet() {
        System.out.println("Have a nice day!");
        return 0;
    }

    @Override
    public void run() {
        System.out.println("Hi, Your name is " + name);
    }

    public static void main(String... args){
        CommandLine commandLine = new CommandLine(new ScopedCaseSensitivityDemo());
        commandLine.addSubcommand("strict", new StrictGreet());
        // globally case insensitive
        commandLine.setOptionsCaseInsensitive(true);
        commandLine.setSubcommandsCaseInsensitive(true);
        // specifically case sensitive
        // accepts `strict GrEET`
        // rejects `strict greet`, `strict GREET`...
        commandLine.getSubcommands().get("strict").setSubcommandsCaseInsensitive(false);
        commandLine.execute(args);
    }
}

@Command(name = "Strict Greet")
class StrictGreet implements Callable<Integer>{

    @Command(name = "GrEET")
    public int greet() {
        System.out.println("I will only GrEET if the case is correct!");
        return 0;
    }

    @Override
    public Integer call() {
        System.out.println("Entering strict zone...");
        return 0;
    }
}