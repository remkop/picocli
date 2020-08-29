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
    int x;

    @Parameters
    List<String> positionalParams;

    @Override
    public void run() {
        ParseResult parseResult = spec.commandLine().getParseResult();

        System.out.println(parseResult.expandedArgs());
        System.out.println(parseResult.originalArgs());
    }

    public static void main(String[] args) {
        new CommandLine(new ParseResultDemo()).execute(args);
    }
}
