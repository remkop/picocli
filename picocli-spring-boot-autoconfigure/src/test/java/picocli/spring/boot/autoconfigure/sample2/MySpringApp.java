package picocli.spring.boot.autoconfigure.sample2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;

@Configuration
@ComponentScan
@Import(PicocliSpringFactory.class)
public class MySpringApp implements CommandLineRunner, ExitCodeGenerator {
    private int exitCode;

    @Autowired
    PicocliSpringFactory factory;

    @Autowired
    MyCommand myCommand;

    @Bean
    ServiceDependency dependency() {
        return new ServiceDependency();
    }

    @Bean
    SomeService someService(ServiceDependency dependency) {
        return new SomeService(dependency);
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(myCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(MySpringApp.class, args)));
    }
}
