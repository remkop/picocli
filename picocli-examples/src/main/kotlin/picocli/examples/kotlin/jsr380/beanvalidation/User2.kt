package picocli.examples.kotlin.jsr380.beanvalidation

import jakarta.validation.constraints.*
import picocli.CommandLine
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec
import java.time.LocalDate


class User2 : Runnable {

    @NotNull(message = "Name cannot be null")
    @Option(names = ["-n", "--name"], description = ["mandatory"])
    private lateinit var name: String

    @AssertTrue(message = "working must be true")
    @Option(names = ["-w", "--working"], description = ["Must be true"])
    private var working = false

    @Option(names = ["-m", "--aboutMe"], description = ["between 10-200 chars"])
    @Size(
        min = 10,
        max = 200,
        message = "About Me must be between 10 and 200 characters"
    )
    private lateinit var aboutMe:  String

    @Option(names = ["-a", "--age"], description = ["between 18-150"])
    @Min(value = 18, message = "Age should not be less than 18")
    @Max(value = 150, message = "Age should not be greater than 150")
    private var age = 0

    @Email(message = "Email should be valid")
    @Option(names = ["-e", "--email"], description = ["valid email"])
    private lateinit var email: String

    @Option(names = ["-p", "--preferences"], description = ["not blank"])
    lateinit var preferences: List<@NotBlank String>

    @Option(names = ["-d", "--dateOfBirth"], description = ["past"])
    private lateinit var dateOfBirth: LocalDate


    @Spec
    lateinit var spec: CommandSpec
    override fun toString(): String {
        return "User{" +
            "name='$name', working=$working, aboutMe='$aboutMe'" +
            ", age=$age, email='$email', preferences=$preferences" +
            ", dateOfBirth=$dateOfBirth}"
    }

    override fun run() {
        // business logic here
        println("hello $this")
    }
}

fun main(args: Array<String>) {
    val args2 = "-d 2019-03-01 -n Remko -p \"\" -p a -w -e me@mail@com --aboutMe about".split(" ".toRegex())
        .dropLastWhile { it.isEmpty() }
        .toTypedArray()

    CommandLine(User2())
        .setExecutionStrategy(ValidatingExecutionStrategy())
        .execute(*args2)
}
