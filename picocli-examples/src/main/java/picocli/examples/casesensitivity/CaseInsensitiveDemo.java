package picocli.examples.casesensitivity;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Case Insensitive Demo")
public class CaseInsensitiveDemo implements Runnable {

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
        new CommandLine(new CaseInsensitiveDemo())
                .setOptionsCaseInsensitive(true)     // accepts `-N`, `--nAmE`
                .setSubcommandsCaseInsensitive(true) // accepts `gREET`, `GREET`
                .execute(args);
    }
}
