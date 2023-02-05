package picocli.codegen.graalvm.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator;

import java.io.File;
import java.nio.file.Path;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This command uses many built-in type converters that use Java 8 and are loaded via
 * reflection, without their classes and methods being registered in the reflect-config.json file.
 */
@Command(name = "example-full",
        mixinStandardHelpOptions = true,
        subcommands = CommandLine.HelpCommand.class,
        resourceBundle = "picocli.codegen.aot.graalvm.exampleResources",
        version = {
                "ExampleFull " + CommandLine.VERSION,
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"
        })
public class ExampleFull implements Callable<Integer> {

    @Spec private CommandSpec spec;

    @Option(names = "--password", interactive = true, arity = "0..1")
    char[] password = null;

    @Option(names = "--duration", description = "PT20.345S")
    Duration duration;

    @Option(names = "--instant", defaultValue = "2019-05-03T10:15:30.00Z")
    Instant instant;

    @Option(names = "--localDate", defaultValue = "2019-05-03")
    LocalDate localDate;

    @Option(names = "--localDateTime", defaultValue = "2019-05-03T10:15:30")
    LocalDateTime localDateTime;

    @Option(names = "--localTime", defaultValue = "10:15:30")
    LocalTime localTime;

    @Option(names = "--monthDay", defaultValue = "--12-03")
    MonthDay monthDay;

    @Option(names = "--offsetDateTime", defaultValue = "2019-05-03T10:15:30+01:00")
    OffsetDateTime offsetDateTime;

    @Option(names = "--offsetTime", defaultValue = "10:15:30+01:00")
    OffsetTime offsetTime;

    @Option(names = "--period", defaultValue = "P1Y2M3W4D")
    Period period;

    @Option(names = "--year", defaultValue = "2019")
    Year year;

    @Option(names = "--yearMonth", defaultValue = "2019-05")
    YearMonth yearMonth;

    @Option(names = "--zonedDateTime", defaultValue = "2019-05-03T10:15:30+01:00[Europe/Paris]")
    ZonedDateTime zonedDateTime;

    @Option(names = "--zoneId")
    ZoneId zoneId = ZoneId.systemDefault();

    @Option(names = "--zoneOffset", defaultValue = "+02:00")
    ZoneOffset zoneOffset;

    @Option(names = "--path", defaultValue = "${sys:user.home}")
    Path path;

    @Option(names = "--time")
    Time time;


    private int exitCode;
    private List<File> files;

    @Option(names = "--exit-code")
    public void setExitCode(int exitCode) {
        if (exitCode < 0) {
            throw new ParameterException(spec.commandLine(), "ExitCode must be a positive integer");
        }
        this.exitCode = exitCode;
    }

    @Parameters(index = "0..*", description = "Must exist if specified.")
    public void setFiles(List<File> otherFiles) {
        for (File f : otherFiles) {
            if (!f.exists()) {
                throw new ParameterException(spec.commandLine(), "File " + f.getAbsolutePath() + " must exist");
            }
        }
        this.files = otherFiles;
    }

    @Command
    public void generateReflectConfig() throws Exception {
        System.out.println(ReflectionConfigGenerator.generateReflectionConfig(spec));
    }

    @Command(resourceBundle = "picocli.codegen.aot.graalvm.exampleMultiplyResources")
    int multiply(@Option(names = "--count") int count,
                 @Parameters int multiplier) {
        System.out.println("Result is " + count * multiplier);
        return count * multiplier;
    }

    public Integer call() {
        System.out.println("Printing current values:");
        System.out.printf("--password=%s --duration=%s --instant=%s --localDate=%s --localDateTime=%s --localTime=%s%n",
                           new String(password), duration, instant, localDate, localDateTime, localTime);
        System.out.printf("--monthDay=%s --offsetDateTime=%s --offsetTime=%s --period=%s --year=%s --yearMonth=%s%n",
                           monthDay, offsetDateTime, offsetTime, period, year, yearMonth);
        System.out.printf("--zonedDateTime=%s --zoneId=%s --path=%s --time=%s --exit-code=%s files=%s%n",
                           zonedDateTime, zoneId, path, time, exitCode, files);
//        System.out.printf("--zonedDateTime=%s --zoneId=%s --time=%s --exit-code=%s files=%s%n",
//                zonedDateTime, zoneId, time, exitCode, files);

        return exitCode;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ExampleFull()).execute(args);
        System.exit(exitCode);
    }
}
