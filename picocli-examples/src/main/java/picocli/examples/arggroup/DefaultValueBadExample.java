package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "bad", description = "usage help shows the wrong default value")
public class DefaultValueBadExample {
    @ArgGroup BadGroup badGroup;

    public static void main(String[] args) {
        new CommandLine(new DefaultValueBadExample()).usage(System.out);
    }
}
class BadGroup {
    @Option(names = "-x", description = "Default: ${DEFAULT-VALUE}")
    int x = 123; // picocli cannot find this value until `BadGroup` is instantiated
}
