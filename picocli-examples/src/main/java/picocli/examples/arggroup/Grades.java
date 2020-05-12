package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.math.BigDecimal;
import java.util.List;

@Command(name = "grades", mixinStandardHelpOptions = true, version = "grades 1.0")
public class Grades implements Runnable {

    // From picocli 4.3, see https://github.com/remkop/picocli/issues/1027
    static class StudentGrade {
        @Parameters(index = "0") String name;
        @Parameters(index = "1") BigDecimal grade;
    }

    @ArgGroup(exclusive = false, multiplicity = "1..*")
    List<StudentGrade> gradeList;

    // workaround for picocli 4.2 and older
//    @Parameters(arity = "2",
//            description = "Each pair must have a name and a grade.",
//            paramLabel = "(NAME GRADE)...", hideParamSyntax = true)
//    List<String> gradeList;

    @Override
    public void run() {
        gradeList.forEach(e -> System.out.println(e.name + ": " + e.grade));
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Grades()).execute(args);
        System.exit(exitCode);
    }
}
