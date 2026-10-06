package picocli.issue2430;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(name = "main")
class Main implements Callable<Integer> {

    static class Options {
        @Spec
        CommandSpec spec;

        int option1 = 1;
        int option2 = 2;

        @Option(names = "-o1")
        void setOption1(int value) {
            if (value > 10) {
                throw new ParameterException(spec.commandLine(), "option1 must be <= 10");
            }
            this.option1 = value;
        }

        @Option(names = "-o2")
        void setOption2(int value) {
            if (value > 10) {
                throw new ParameterException(spec.commandLine(), "option2 must be <= 10");
            }
            this.option2 = value;
        }
    }

    static class SharedOptions {

        @ArgGroup(exclusive = false, heading = "Shared options%n")
        Options options = new Options();
    }

    @Mixin
    SharedOptions sharedOptions = new SharedOptions();

    @Override
    public Integer call() throws Exception {
        System.out.printf("Hello world: o1 = %s, o2 = %s%n",
            sharedOptions.options.option1,
            sharedOptions.options.option2
        );
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
