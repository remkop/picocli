package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.util.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * {@code ResourceConfigGenerator} generates a JSON String with the resource bundles and other classpath resources
 * that should be included in the Substrate VM native image.
 * <p>
 * The GraalVM native-image builder by default will not integrate any of the
 * <a href="https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/">classpath resources</a> into the image it creates.
 * </p><p>
 * The output of {@code ResourceConfigGenerator} is intended to be passed to the {@code -H:ResourceConfigurationFiles=/path/to/resource-config.json}
 * option of the {@code native-image} <a href="https://www.graalvm.org/latest/reference-manual/native-image/">GraalVM utility</a>.
 * This allows picocli-based native image applications to access these resources.
 * </p><p>
 * Alternatively, the generated <a href="https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildConfiguration/">configuration</a>
 * files can be supplied to the {@code native-image} tool by placing them in a
 * {@code META-INF/native-image/} directory on the class path, for example, in a JAR file used in the image build.
 * This directory (or any of its subdirectories) is searched for files with the names {@code jni-config.json},
 * {@code reflect-config.json}, {@code proxy-config.json} and {@code resource-config.json}, which are then automatically
 * included in the build. Not all of those files must be present.
 * When multiple files with the same name are found, all of them are included.
 * </p>
 *
 * @since 4.0
 */
public class ResourceConfigGenerator {

    @Command(name = "gen-resource-config", showAtFileInUsageHelp = true, sortOptions = false,
            description = {"Generates a JSON file with the resources and resource bundles to include in the native image.",
                    "The generated JSON file can be passed to the `-H:ResourceConfigurationFiles=/path/to/resource-config.json` " +
                    "option of the `native-image` GraalVM utility.",
                    "See https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/"},
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
                            "picocli.codegen.aot.graalvm.ResourceConfigGenerator my.pkg.MyClass"
            },
            mixinStandardHelpOptions = true, version = "picocli-codegen gen-resource-config " + CommandLine.VERSION)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "0..*", description = "Zero or more `@Command` classes with a resource bundle to include in the image.")
        Class<?>[] classes = new Class<?>[0];

        @Option(names = {"-b", "--bundle"}, paramLabel = "<bundle-base-name>",
                description = "Additional resource bundle(s) to be included in the image. " +
                        "This option may be specified multiple times with different regular expression patterns.")
        String[] bundles = new String[0];

        @Option(names = {"-p", "--pattern"}, description = "Java regexp that matches resource(s) to be included in the image. " +
                "This option may be specified multiple times with different regular expression patterns.")
        String[] resourceRegex = new String[0];

        @Option(names = {"-c", "--factory"}, description = "Optionally specify the fully qualified class name of the custom factory to use to instantiate the command class. " +
                "When omitted, the default picocli factory is used.")
        String factoryClass;

        @Mixin
        OutputFileMixin outputFile = new OutputFileMixin();

        @Option(names = "--exit", negatable = true,
                description = "Specify `--exit` if you want the application to call `System.exit` when finished. " +
                "By default, `System.exit` is not called.")
        boolean exit;

        public Integer call() throws Exception {
            List<CommandSpec> specs = Util.getCommandSpecs(factoryClass, classes);
            String result = ResourceConfigGenerator.generateResourceConfig(specs.toArray(new CommandSpec[0]), bundles, resourceRegex);
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

    /**
     * Returns a JSON String with the resources and resource bundles to include for the specified
     * {@code CommandSpec} objects.
     *
     * @param specs one or more {@code CommandSpec} objects to inspect for resource bundles
     * @param bundles base names of additional resource bundles to be included in the image
     * @param resourceRegex one or more Java regular expressions that match resource(s) to be included in the image
     * @return a JSON String in the <a href="https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/#resource-bundles-in-native-image">format</a>
     *       required by the {@code -H:ResourceConfigurationFiles=/path/to/resource-config.json} option of the GraalVM {@code native-image} utility.
     */
    public static String generateResourceConfig(CommandSpec[] specs, String[] bundles, String[] resourceRegex) {
        Visitor visitor = new Visitor();
        visitor.bundles.addAll(Arrays.asList(bundles));
        visitor.resources.addAll(Arrays.asList(resourceRegex));

        for (CommandSpec spec : specs) {
            visitor.visitCommandSpec(spec);
        }
        return visitor.toString();
    }

    static final class Visitor {
        Set<String> resources = new LinkedHashSet<String>();
        Set<String> bundles = new LinkedHashSet<String>();

        void visitCommandSpec(CommandSpec spec) {
            String bundle = spec.resourceBundleBaseName();
            if (bundle != null) {
                bundles.add(bundle);
            }
            for (CommandSpec mixin : spec.mixins().values()) {
                visitCommandSpec(mixin);
            }
            for (CommandLine sub : spec.subcommands().values()) {
                visitCommandSpec(sub.getCommandSpec());
            }
        }

        @Override
        public String toString() {
            return String.format("" +
                    "{%n" +
                    "  \"bundles\" : [%s%n" +
                    "  ],%n" +
                    "  \"resources\" : [%s%n" +
                    "  ]%n" +
                    "}%n", bundlesJson(), resourcesJson());
        }

        private StringBuilder bundlesJson() {
            return json(bundles, "name");
        }

        private StringBuilder resourcesJson() {
            return json(resources, "pattern");
        }

        private static StringBuilder json(Collection<String> strings, String label) {
            StringBuilder result = new StringBuilder(1024);
            for (String str : strings) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(String.format("%n    {\"%s\" : \"%s\"}", label, str));
            }
            return result;
        }
    }
}
