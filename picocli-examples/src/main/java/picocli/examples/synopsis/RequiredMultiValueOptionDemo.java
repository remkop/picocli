package picocli.examples.synopsis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

public class RequiredMultiValueOptionDemo {
    @Command(name = "issue688", description = "Synopsis test",
            optionListHeading = "Options%n",
            parameterListHeading = "Positional Parameters%n"
    )
    static class App {
        @Parameters(paramLabel = "POSITIONAL", arity = "1..*", split = ",",
                description = "Specify at least one, each value may be a " +
                        "comma-separated list of values.")
        List<String> positionals;

        @Option(names = {"-a", "--aaa"}, paramLabel = "AAA", required = true,
                description = "This required option may be specified multiple times.")
        List<String> requiredFoos;

        @Option(names = {"-b", "--bbb"}, paramLabel = "BBB", required = true, split = ",",
                description = "This required option may be specified multiple times, " +
                        "and each value may be a comma-separated list of values.")
        List<String> requiredBars;

        @Option(names = {"-c", "--ccc"}, paramLabel = "CCC",
                description = "This option may be specified multiple times.")
        List<String> foos;

        @Option(names = {"-d", "--ddd"}, paramLabel = "DDD", split = ",",
                description = "This option may be specified multiple times, " +
                        "and each value may be a comma-separated list of values.")
        List<String> bars;
    }

    public static void main(String[] args) {
        new CommandLine(new App()).usage(System.out);
    }
    // Expected output:
/*
Usage: issue688 -a=AAA [-a=AAA]... -b=BBB[,BBB...] [-b=BBB[,BBB...]]...
                [-c=CCC]... [-d=DDD[,DDD...]]... POSITIONAL[,POSITIONAL...]...
Synopsis test
Positional Parameters
      POSITIONAL[,POSITIONAL...]...
                           Specify at least one, each value may be a comma-separated
                             list of values.
Options
  -a, --aaa=AAA            This required option may be specified multiple times.
  -b, --bbb=BBB[,BBB...]   This required option may be specified multiple times, and
                             each value may be a comma-separated list of values.
  -c, --ccc=CCC            This option may be specified multiple times.
  -d, --ddd=DDD[,DDD...]   This option may be specified multiple times, and each
                             value may be a comma-separated list of values.
*/
}
