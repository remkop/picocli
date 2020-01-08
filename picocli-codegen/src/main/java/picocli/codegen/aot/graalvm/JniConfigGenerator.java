package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;
import picocli.codegen.aot.graalvm.ReflectionConfigGenerator.ReflectedClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class JniConfigGenerator {

    @Command(name = "gen-jni-config",
            description = {"Generates a JSON file with the program elements that will be " +
                    "accessed reflectively from native code. " +
                    "The generated JSON file can be passed to the -H:JNIConfigurationFiles=/path/to/jni-config.json " +
                    "option of the `native-image` GraalVM utility.",
                    "See https://github.com/oracle/graal/blob/master/substratevm/JNI.md"},
            mixinStandardHelpOptions = true,
            version = "picocli-codegen ${COMMAND-NAME} " + CommandLine.VERSION)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1..*", description = "One or more classes to generate a GraalVM JNI configuration for.")
        Class<?>[] classes = new Class<?>[0];

        @Mixin
        OutputFileMixin outputFile = new OutputFileMixin();

        public Integer call() throws Exception {
            String result = generateReflectionConfig(classes).toString();
            outputFile.write(result);
            return 0;
        }
    }

    /**
     * Runs this class as a standalone application, printing the resulting JSON String to a file or to {@code System.out}.
     * @param args one or more fully qualified class names of {@code @Command}-annotated classes.
     */
    public static void main(String... args) {
        new CommandLine(new App()).execute(args);
    }

    private static StringBuilder generateReflectionConfig(Class<?>[] classes) {
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
