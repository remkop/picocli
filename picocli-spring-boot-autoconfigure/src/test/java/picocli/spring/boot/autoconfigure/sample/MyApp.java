package picocli.spring.boot.autoconfigure.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.spring.PicocliSpringFactory;

import java.util.List;
import java.util.concurrent.Callable;

@Configuration
@Import(PicocliSpringFactory.class)
@Command(name = "myapp", mixinStandardHelpOptions = true, subcommands = MyApp.Sub.class)
public class MyApp implements Callable<Integer>, CommandLineRunner, ExitCodeGenerator {
    private int exitCode;

    @Autowired
    IFactory factory;

    @Option(names = "-x", description = "optional option")
    String x;

    @Parameters(description = "positional params")
    List<String> positionals;

    @Bean
    ServiceDependency dependency() {
        return new ServiceDependency();
    }

    @Bean
    SomeService someService(ServiceDependency dependency) {
        return new SomeService(dependency);
    }

    @Override
    public Integer call() {
        System.out.printf("myapp was called with -x=%s and positionals: %s%n", x, positionals);
        return 23;
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(this, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(MyApp.class, args)));
    }

    @Component
    @Command(name = "sub", mixinStandardHelpOptions = true, subcommands = MyApp.SubSub.class,
            exitCodeOnExecutionException = 34)
    static class Sub implements Callable<Integer> {
        @Option(names = "-y", description = "optional option")
        String y;

        @Parameters(description = "positional params")
        List<String> positionals;

        @Override
        public Integer call() {
            System.out.printf("myapp sub was called with -y=%s and positionals: %s%n", y, positionals);
            throw new RuntimeException("myapp sub failing on purpose");
            //return 33;
        }
    }

    @Component
    @Command(name = "subsub", mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 44)
    static class SubSub implements Callable<Integer> {
        @Option(names = "-z", description = "optional option")
        String z;

        @Autowired
        SomeService service;

        @Override
        public Integer call() {
            System.out.printf("myapp sub subsub was called with -z=%s. Service says: '%s'%n", z, service.service());
            throw new RuntimeException("myapp sub subsub failing on purpose");
            //return 43;
        }
    }
}
