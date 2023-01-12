package picocli.examples.jsr380.beanvalidation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Example inspired by https://www.baeldung.com/javax-validation
// This example uses a custom execution strategy
public class User2 implements Runnable {

    @NotNull(message = "Name cannot be null")
    @Option(names = {"-n", "--name"}, description = "mandatory")
    private String name;

    @AssertTrue(message = "working must be true")
    @Option(names = {"-w", "--working"}, description = "Must be true")
    private boolean working;

    @Size(min = 10, max = 200, message
            = "About Me must be between 10 and 200 characters")
    @Option(names = {"-m", "--aboutMe"}, description = "between 10-200 chars")
    private String aboutMe;

    @Min(value = 18, message = "Age should not be less than 18")
    @Max(value = 150, message = "Age should not be greater than 150")
    @Option(names = {"-a", "--age"}, description = "between 18-150")
    private int age;

    @Email(message = "Email should be valid")
    @Option(names = {"-e", "--email"}, description = "valid email")
    private String email;

    @Option(names = {"-p", "--preferences"}, description = "not blank")
    List<@NotBlank String> preferences;

    @Option(names = {"-d", "--dateOfBirth"}, description = "past")
    private LocalDate dateOfBirth;

    @Spec
    CommandSpec spec;

    public User2() {
    }

    public Optional<@Past LocalDate> getDateOfBirth() {
        return Optional.ofNullable(dateOfBirth);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", working=" + working +
                ", aboutMe='" + aboutMe + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", preferences=" + preferences +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }

    public static void main(String... args) {
        args = "-d 2019-03-01 -n Remko -p \"\" -p a -w -e me@mail@com --aboutMe about".split(" ");
        new CommandLine(new User2())
            .setExecutionStrategy(new ValidatingExecutionStrategy())
            .execute(args);
    }

    @Override
    public void run() {
        // ... now run the business logic
        System.out.println("hello " + this);
    }
}
