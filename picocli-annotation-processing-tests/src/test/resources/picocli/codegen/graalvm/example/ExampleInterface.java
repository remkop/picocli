package picocli.codegen.graalvm.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.IExecutionStrategy;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Command(name = "example-interface",
        mixinStandardHelpOptions = true,
        subcommands = CommandLine.HelpCommand.class,
        version = {
                "example-interface " + CommandLine.VERSION,
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"
        })
public interface ExampleInterface {

    @Spec
    CommandSpec spec();

    @Option(names = "-t", defaultValue = "SECONDS")
    TimeUnit timeUnit();

    @Parameters(index = "0")
    File file();

    @Option(names = "--minimum")
    int minimum();

    @Parameters(index = "1..*")
    List<File> otherFiles();

    class Runner {
        public static void main(String[] args) {
            System.exit(execute(args));
        }

        private static void businessLogic(ExampleInterface exampleInterface) {
            System.out.printf("timeUnit=%s%n", exampleInterface.timeUnit());
            System.out.printf("minimum=%s%n", exampleInterface.minimum());
            System.out.printf("file (positional arg[0]=%s%n", exampleInterface.file());
            System.out.printf("otherFiles (positional arg[1..*]=%s%n", exampleInterface.otherFiles());
            System.out.printf("spec=%s%n", exampleInterface.spec());
        }

        private static int execute(String[] args) {
            final CommandLine cmd = new CommandLine(ExampleInterface.class);
            cmd.setExecutionStrategy(new IExecutionStrategy() {
                public int execute(ParseResult parseResult) throws ExecutionException, ParameterException {
                    Integer result = CommandLine.executeHelpRequest(parseResult);
                    if (result != null) {
                        return result;
                    }
                    // invoke the business logic
                    ExampleInterface exampleInterface = cmd.getCommand();
                    businessLogic(exampleInterface);
                    return cmd.getCommandSpec().exitCodeOnSuccess();
                }
            });
            return cmd.execute(args);
        }
    }
}
