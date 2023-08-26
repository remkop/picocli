package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator.ReflectedClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class JniConfigGenerator {

    @Command(name = "gen-jni-config", showAtFileInUsageHelp = true,
            description = {"Generates a JSON file with the program elements that will be " +
                    "accessed reflectively from native code.",
                    "The generated JSON file can be passed to the `-H:JNIConfigurationFiles=/path/to/jni-config.json` " +
                    "option of the `native-image` GraalVM utility.",
                    "See https://www.graalvm.org/reference-manual/native-image/JNI/"},
            exitCodeListHeading = "%nExit Codes (if enabled with `--exit`)%n",
            exitCodeList = {
                    "0:Successful program execution.",
                    "1:A runtime exception occurred while generating man pages.",
                    "2:Usage error: user input for the command was incorrect, " +
                            "e.g., the wrong number of arguments, a bad flag, " +
                            "a bad syntax in a parameter, etc."
            },
            footerHeading = "%nExample%n",
            footer = {
                    "  java -cp \"myapp.jar;picocli-4.7.6-SNAPSHOT.jar;picocli-codegen-4.7.6-SNAPSHOT.jar\" " +
                            "picocli.codegen.aot.graalvm.JniConfigGenerator my.pkg.MyClass"
            },
            mixinStandardHelpOptions = true, sortOptions = false,
            version = "picocli-codegen ${COMMAND-NAME} " + CommandLine.VERSION)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1..*", description = "One or more classes to generate a GraalVM JNI configuration for.")
        Class<?>[] classes = new Class<?>[0];

        @Mixin
        OutputFileMixin outputFile = new OutputFileMixin();

        @Option(names = "--exit", negatable = true,
                description = "Specify `--exit` if you want the application to call `System.exit` when finished. " +
                "By default, `System.exit` is not called.")
        boolean exit;

        public Integer call() throws Exception {
            String result = generateJniConfig(classes).toString();
            outputFile.write(result);
            return 0;
        }
    }

    /**
     * Runs this class as a standalone application, printing the resulting JSON String to a file or to {@code System.out}.
     * @param args one or more fully qualified class names of {@code @Command}-annotated classes.
     */
    public static void main(String... args) {
        App app = new App();
        int exitCode = new CommandLine(app).execute(args);
        if (app.exit) {
            System.exit(exitCode);
        }
    }

    private static StringBuilder generateJniConfig(Class<?>[] classes) {
        List<ReflectedClass> visited = visit(classes);
        StringBuilder result = new StringBuilder(1024);
        String prefix = String.format("[%n");
        String suffix = String.format("%n]%n");
        for (ReflectedClass cls : visited) {
            result.append(prefix).append(cls);
            prefix = String.format(",%n");
        }
        return result.append(suffix);
    }

    private static List<ReflectedClass> visit(Class<?>[] classes) {
        List<ReflectedClass> result = new ArrayList<ReflectedClass>();
        for (Class<?> cls : classes) {
            visit(cls, result);
        }
        return result;
    }

    private static void visit(Class<?> cls, List<ReflectedClass> result) {
        ReflectedClass reflected = new ReflectedClass(cls.getName());
        result.add(reflected);

        for (Method method : cls.getDeclaredMethods()) {
            reflected.addMethod(method);
        }
        for (Field field : cls.getDeclaredFields()) {
            reflected.addField(field);
        }
        for (Class<?> inner : cls.getDeclaredClasses()) {
            visit(inner, result);
        }
    }
}
