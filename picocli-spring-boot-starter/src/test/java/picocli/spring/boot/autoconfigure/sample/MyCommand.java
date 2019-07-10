package picocli.spring.boot.autoconfigure.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

// NOTE: inner classes and fields are public for testing

@Component
@Command(name = "mycommand", mixinStandardHelpOptions = true, subcommands = MyCommand.Sub.class)
public class MyCommand implements Callable<Integer> {
    @Option(names = "-x", description = "optional option")
    public String x;

    @Parameters(description = "positional params")
    public List<String> positionals;

    @Override
    public Integer call() {
        System.out.printf("mycommand was called with -x=%s and positionals: %s%n", x, positionals);
        return 23;
    }

    @Component
    @Command(name = "sub", mixinStandardHelpOptions = true, subcommands = MyCommand.SubSub.class,
            exitCodeOnExecutionException = 34)
    public static class Sub implements Callable<Integer> {
        @Option(names = "-y", description = "optional option")
        public String y;

        @Parameters(description = "positional params")
        public List<String> positionals;

        @Override
        public Integer call() {
            System.out.printf("mycommand sub was called with -y=%s and positionals: %s%n", y, positionals);
            throw new RuntimeException("mycommand sub failing on purpose");
            //return 33;
        }
    }

    @Component
    @Command(name = "subsub", mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 44)
    public static class SubSub implements Callable<Integer> {
        @Option(names = "-z", description = "optional option")
        public String z;

        @Autowired
        public SomeService service;

        @Override
        public Integer call() {
            System.out.printf("mycommand sub subsub was called with -z=%s. Service says: '%s'%n", z, service.service());
            throw new RuntimeException("mycommand sub subsub failing on purpose");
            //return 43;
        }
    }
}
