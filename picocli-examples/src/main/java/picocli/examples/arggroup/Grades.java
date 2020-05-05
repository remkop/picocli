package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Command(name = "grades", mixinStandardHelpOptions = true, version = "grades 1.0")
public class Grades implements Runnable {

    // unfortunately this does not work
//    static class StudentGrade {
//        @Parameters(index = "0") String name;
//        @Parameters(index = "1") BigDecimal grade;
//    }
//
//    @ArgGroup(exclusive = false, multiplicity = "1..*")
//    List<StudentGrade> gradeList;

    @Parameters(arity = "2",
            description = "Each pair must have a name and a grade.",
            paramLabel = "(NAME GRADE)...", hideParamSyntax = true)
    List<String> gradeList;

    @Override
    public void run() {
        System.out.println(gradeList);
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 0; i < gradeList.size(); i += 2) {
            map.put(gradeList.get(i), new BigDecimal(gradeList.get(i + 1)));
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Grades()).execute(args);
        System.exit(exitCode);
    }
}
