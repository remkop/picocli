package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "good", description = "usage help shows the default value")
public class DefaultValueGoodExample {
    @ArgGroup GoodGroup goodGroup;

    public static void main(String[] args) {
        new CommandLine(new DefaultValueGoodExample()).usage(System.out);
    }
}
class GoodGroup {
    @Option(names = "-x", defaultValue = "123", description = "Default: ${DEFAULT-VALUE}")
    int x;
}
