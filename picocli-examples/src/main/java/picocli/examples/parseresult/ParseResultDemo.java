package picocli.examples.parseresult;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import java.util.List;

@Command(name = "myapp", mixinStandardHelpOptions = true, version = "myapp 0.1")
public class ParseResultDemo implements Runnable {
    @Spec CommandSpec spec;

    @Option(names = "-x")
    int x = 10;

    @Option(names = "-y", defaultValue = "20")
    int y;

    @Parameters
    List<String> positionalParams;

    @Override
    public void run() {
        ParseResult pr = spec.commandLine().getParseResult();

        System.out.println(pr.expandedArgs());
        System.out.println(pr.originalArgs());

        String[] options = {"x", "y"};
        for (String name : options) {
            System.out.printf("%s was specified: %s%n", name, pr.hasMatchedOption(name));
            System.out.printf("%s=%s (-1 means this option was not matched on command line)%n", name, pr.matchedOptionValue(name, -1));
            System.out.printf("%s=%s (arg value or default)%n", name, spec.findOption(name).getValue());
            System.out.println();
        }

        System.out.printf("-x=%s (field value)%n", x);
        System.out.printf("-y=%s (field value)%n", y);
    }

    public static void main(String[] args) {
        new CommandLine(new ParseResultDemo()).execute(args);
    }
}
