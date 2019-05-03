package picocli.examples.jsr380.beanvalidation;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Example inspired by https://www.baeldung.com/javax-validation
public class User implements Runnable {

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

    public User() {
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
        new CommandLine(new User()).execute(args);
    }

    @Override
    public void run() {
        validate();

        // ... now run the business logic
    }

    private void validate() {
        System.out.println(spec.commandLine().getParseResult().originalArgs());
        System.out.println(this);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(this);

        if (!violations.isEmpty()) {
            String errorMsg = "";
            for (ConstraintViolation<User> violation : violations) {
                errorMsg += "ERROR: " + violation.getMessage() + "\n";
            }
            throw new ParameterException(spec.commandLine(), errorMsg);
        }
    }
}