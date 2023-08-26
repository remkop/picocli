<p align="center">
<img src="https://picocli.info/images/spring-boot.png" alt="spring and spring boot logos" height="150px">
<img src="https://picocli.info/images/1x1.png" width="20">
<img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px">
</p>


# Picocli Spring Boot Starter - Enables Spring Dependency Injection in Picocli Commands

Picocli Spring Boot Starter contains components and documentation for building
command line applications with Spring and picocli.


## Dependency Management

Picocli 4.7.5 has been tested with Spring Boot 2.5, 2.6, 2.7, and 3.1 up to 3.1.2.

Add the following dependency:

Maven:
```xml
<dependency>
  <groupId>info.picocli</groupId>
  <artifactId>picocli-spring-boot-starter</artifactId>
  <version>4.7.5</version>
</dependency>
```

Gradle:
```
dependencies {
    implementation "info.picocli:picocli-spring-boot-starter:4.7.5"
}
```

This will bring in the `info.picocli:picocli` and `org.springframework.boot:spring-boot-starter` dependencies.


## Example Application

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MySpringApp {

    @Bean
    ServiceDependency dependency() {
        return new ServiceDependency();
    }

    @Bean
    SomeService someService(ServiceDependency dependency) {
        return new SomeService(dependency);
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(MySpringApp.class, args)));
    }
}
```

```java
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

	private final MyCommand myCommand;

	private final IFactory factory; // auto-configured to inject PicocliSpringFactory

	private int exitCode;

	public MyApplicationRunner(MyCommand myCommand, IFactory factory) {
		this.myCommand = myCommand;
		this.factory = factory;
	}

	@Override
	public void run(String... args) throws Exception {
		exitCode = new CommandLine(myCommand, factory).execute(args);
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
```

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Component
@Command(name = "mycommand", mixinStandardHelpOptions = true, subcommands = MyCommand.Sub.class)
public class MyCommand implements Callable<Integer> {
    @Option(names = "-x", description = "optional option")
    private String x;

    @Parameters(description = "positional params")
    private List<String> positionals;

    @Override
    public Integer call() {
        System.out.printf("mycommand was called with -x=%s and positionals: %s%n", x, positionals);
        return 23;
    }

    @Component
    @Command(name = "sub", mixinStandardHelpOptions = true, subcommands = MyCommand.SubSub.class,
            exitCodeOnExecutionException = 34)
    static class Sub implements Callable<Integer> {
        @Option(names = "-y", description = "optional option")
        private String y;

        @Parameters(description = "positional params")
        private List<String> positionals;

        @Override
        public Integer call() {
            System.out.printf("mycommand sub was called with -y=%s and positionals: %s%n", y, positionals);
            return 33;
        }
    }

    @Component
    @Command(name = "subsub", mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 44)
    static class SubSub implements Callable<Integer> {
        @Option(names = "-z", description = "optional option")
        private String z;

        @Autowired
        private SomeService service;

        @Override
        public Integer call() {
            System.out.printf("mycommand sub subsub was called with -z=%s. Service says: '%s'%n", z, service.service());
            return 43;
        }
    }
}
```

## Testing

See [this example](https://github.com/remkop/picocli/blob/main/picocli-spring-boot-starter/src/test/java/picocli/spring/boot/autoconfigure/example/test/ExampleTest.java).
