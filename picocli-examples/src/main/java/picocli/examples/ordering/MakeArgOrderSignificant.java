package picocli.examples.ordering;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * By default, picocli does not care about the order in which arguments are
 * specified on the command line.
 * However, for some applications ordering is significant.
 * This class shows how to accomplish this with picocli's ParseResult API.
 */
@Command(name = "ordered", description = "Argument order is significant in this command")
public class MakeArgOrderSignificant implements Callable<Integer> {
    @Option(names = "--debug")
    boolean debug;

    @Parameters(description = "A list of files")
    List<String> files;

    @Spec
    CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        // Get the ParseResult. This is also the object returned by
        // the CommandLine.parseArgs method, but the CommandLine.execute method
        // is more convenient to use (see https://picocli.info/#_diy_command_execution),
        // so we get the parse result from the @Spec.
        ParseResult pr = spec.commandLine().getParseResult();

        // The ParseResult.matchedArgs method returns the matched options and
        // positional parameters, in order they were matched on the command line.
        List<ArgSpec> argSpecs = pr.matchedArgs();

        if (argSpecs.get(0).isPositional()) {
            System.out.println("The first arg is a positional parameter: treating all args as Strings...");
            // ...
        } else { // argSpecs.get(0).isOption()
            System.out.println("The first arg is an option");
            boolean isDebug = spec.findOption("--debug").equals(argSpecs.get(0));
            System.out.printf("The first arg %s --debug%n", isDebug ? "is" : "is not");
            if (isDebug) {
                // do debuggy stuff...
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        //System.exit(new CommandLine(new MakeArgOrderSignificant()).execute(args));

        // test:
        System.out.println("Test 1: starting with --debug...");
        new CommandLine(new MakeArgOrderSignificant()).execute("--debug", "myfile.java", "myfile2.java");

        System.out.println();
        System.out.println("Test 2: starting with myfile.java...");
        new CommandLine(new MakeArgOrderSignificant()).execute("myfile.java", "--debug", "myfile2.java");
    }
}
