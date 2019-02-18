package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UnmatchedArgsBinding;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * {@code ReflectionConfigGenerator} generates a JSON String with the program elements that will be accessed
 * reflectively in a picocli-based application, in order to compile this application ahead-of-time into a native
 * executable with GraalVM.
 * <p>
 * GraalVM has <a href="https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md">limited support for Java
 * reflection</a> and it needs to know ahead of time the reflectively accessed program elements.
 * </p><p>
 * The output of {@code ReflectionConfigGenerator} is intended to be passed to the {@code -H:ReflectionConfigurationFiles=/path/to/reflectconfig}
 * option of the {@code native-image} <a href="https://www.graalvm.org/docs/reference-manual/aot-compilation/">GraalVM utility</a>.
 * This allows picocli-based applications to be compiled to a native image.
 * </p><p>
 * If necessary, it is possible to exclude classes with system property {@code picocli.codegen.excludes},
 * which accepts a comma-separated list of regular expressions of the fully qualified class names that should
 * <em>not</em> be included in the resulting JSON String.
 * </p>
 *
 * @since 3.7.0
 */
public class ReflectionConfigGenerator {

    private static final String SYSPROP_CODEGEN_EXCLUDES = "picocli.codegen.excludes";
    private static final String REFLECTED_FIELD_BINDING_CLASS = "picocli.CommandLine$Model$FieldBinding";
    private static final String REFLECTED_METHOD_BINDING_CLASS = "picocli.CommandLine$Model$MethodBinding";
    private static final String REFLECTED_FIELD_BINDING_FIELD = "field";
    private static final String REFLECTED_METHOD_BINDING_METHOD = "method";
    private static final String REFLECTED_BINDING_FIELD_SCOPE = "scope";

    @Command(name = "ReflectionConfigGenerator",
            description = {"Generates a JSON file with the program elements that will be " +
                    "accessed reflectively for the specified @Command classes. " +
                    "The generated JSON file can be passed to the -H:ReflectionConfigurationFiles=/path/to/reflectconfig " +
                    "option of the `native-image` GraalVM utility.",
                    "See https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md"},
            mixinStandardHelpOptions = true, version = "picocli-codegen ReflectionConfigGenerator 3.7.0")
    private static class App implements Callable<Void> {

        @Parameters(arity = "1..*", description = "One or more classes to generate a GraalVM ReflectionConfiguration for.")
        Class<?>[] classes = new Class<?>[0];

        @Option(names = {"-o", "--output"}, description = "Output file to write the configuration to. " +
                "If not specified, the configuration is written to the standard output stream.")
        File outputFile;

        public Void call() throws NoSuchFieldException, IllegalAccessException, IOException {
            List<CommandSpec> specs = new ArrayList<CommandSpec>();
            for (Class<?> cls : classes) {
                specs.add(new CommandLine(cls).getCommandSpec());
            }
            String result = new ReflectionConfigGenerator().generateReflectionConfig(specs.toArray(new CommandSpec[0]));
            if (result != null) {
                if (outputFile == null) {
                    System.out.print(result);
                } else {
                    writeToFile(result);
                }
            }
            return null;
        }

        private void writeToFile(String result) throws IOException {
            FileWriter writer = null;
            try {
                writer = new FileWriter(outputFile);
                writer.write(result);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    /**
     * Runs this class as a standalone application, printing the resulting JSON String to a file or to {@code System.out}.
     * @param args one or more fully qualified class names of {@code @Command}-annotated classes.
     */
    public static void main(String... args) {
        CommandLine.call(new App(), args);
    }

    /**
     * Returns a JSON String with the program elements that will be accessed reflectively for the specified
     * {@code CommandSpec} objects.
     *
     * @param specs one or more {@code CommandSpec} objects to inspect
     * @return a JSON String in the <a href="https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md#manual-configuration">format</a>
     *       required by the {@code -H:ReflectionConfigurationFiles=/path/to/reflectconfig} option of the GraalVM {@code native-image} utility.
     * @throws NoSuchFieldException if a problem occurs while processing the specified specs
     * @throws IllegalAccessException if a problem occurs while processing the specified specs
     */
    public String generateReflectionConfig(CommandSpec... specs) throws NoSuchFieldException, IllegalAccessException {
        Visitor visitor = new Visitor();
        for (CommandSpec spec : specs) {
            visitor.visitCommandSpec(spec);
        }
        return generateReflectionConfig(visitor).toString();
    }

    StringBuilder generateReflectionConfig(Visitor visited) {
        StringBuilder result = new StringBuilder(1024);
        String prefix = String.format("[%n");
        String suffix = String.format("%n]%n");
        for (ReflectedClass cls : visited.visited.values()) {
            result.append(prefix).append(cls);
            prefix = String.format(",%n");
        }
        return result.append(suffix);
    }

    static final class Visitor {
        Map<String, ReflectedClass> visited = new TreeMap<String, ReflectedClass>();

        Visitor() {
            getOrCreateClass(Method.class);
            getOrCreateClassName("java.lang.reflect.Executable").addMethod("getParameters");
            getOrCreateClassName("java.lang.reflect.Parameter").addMethod("getName");

            // ANSI color enabled detection
            getOrCreateClassName("java.lang.System").addMethod("console");
            getOrCreateClassName("org.fusesource.jansi.AnsiConsole").addField("out", false);

            // picocli 4.0
            getOrCreateClassName("java.util.ResourceBundle").addMethod("getBaseBundleName");

            // type converters registered with reflection
            getOrCreateClassName("java.time.Duration").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.Instant").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.LocalDate").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.LocalDateTime").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.LocalTime").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.MonthDay").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.OffsetDateTime").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.OffsetTime").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.Period").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.Year").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.YearMonth").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.ZonedDateTime").addMethod("parse", CharSequence.class);
            getOrCreateClassName("java.time.ZoneId").addMethod("of", String.class);
            getOrCreateClassName("java.time.ZoneOffset").addMethod("of", String.class);
            getOrCreateClassName("java.nio.file.Path");
            getOrCreateClassName("java.nio.file.Paths").addMethod("get", String.class, String[].class);

            getOrCreateClassName("java.sql.Connection");
            getOrCreateClassName("java.sql.Driver");
            getOrCreateClassName("java.sql.DriverManager")
                    .addMethod("getConnection", String.class)
                    .addMethod("getDriver", String.class);
            getOrCreateClassName("java.sql.Timestamp").addMethod("valueOf", String.class);
        }

        void visitCommandSpec(CommandSpec spec) throws NoSuchFieldException, IllegalAccessException {
            if (spec.userObject() != null) {
                if (spec.userObject() instanceof Method) {
                    Method method = (Method) spec.userObject();
                    ReflectedClass cls = getOrCreateClass(method.getDeclaringClass());
                    cls.addMethod(method.getName(), method.getParameterTypes());
                } else if (Proxy.isProxyClass(spec.userObject().getClass())) {
                    // do nothing
                } else {
                    visitAnnotatedFields(spec.userObject().getClass());
                }
            }
            visitObjectType(spec.versionProvider());
            visitObjectType(spec.defaultValueProvider());

            for (UnmatchedArgsBinding binding : spec.unmatchedArgsBindings()) {
                visitGetter(binding.getter());
                visitSetter(binding.setter());
            }
            for (OptionSpec option : spec.options()) {
                visitArgSpec(option);
            }
            for (PositionalParamSpec positional : spec.positionalParameters()) {
                visitArgSpec(positional);
            }
            for (CommandSpec mixin : spec.mixins().values()) {
                visitCommandSpec(mixin);
            }
            for (CommandLine sub : spec.subcommands().values()) {
                visitCommandSpec(sub.getCommandSpec());
            }
        }

        private void visitAnnotatedFields(Class<?> cls) {
            if (cls == null) {
                return;
            }
            ReflectedClass reflectedClass = getOrCreateClass(cls);
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field f : declaredFields) {
                if (f.isAnnotationPresent(CommandLine.Spec.class)) {
                    reflectedClass.addField(f.getName(), isFinal(f));
                }
                if (f.isAnnotationPresent(CommandLine.ParentCommand.class)) {
                    reflectedClass.addField(f.getName(), isFinal(f));
                }
                if (f.isAnnotationPresent(CommandLine.Mixin.class)) {
                    reflectedClass.addField(f.getName(), isFinal(f));
                }
                if (f.isAnnotationPresent(CommandLine.Unmatched.class)) {
                    reflectedClass.addField(f.getName(), isFinal(f));
                }
            }
            visitAnnotatedFields(cls.getSuperclass());
        }

        private boolean isFinal(Field f) {
            return (f.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
        }

        private void visitArgSpec(ArgSpec argSpec) throws NoSuchFieldException, IllegalAccessException {
            visitGetter(argSpec.getter());
            visitSetter(argSpec.setter());
            visitType(argSpec.type());
            visitTypes(argSpec.auxiliaryTypes());
            visitObjectType(argSpec.completionCandidates());
            visitObjectTypes(argSpec.converters());
        }

        private void visitTypes(Class<?>[] classes) {
            for (Class<?> cls : classes) { visitType(cls); }
        }

        private void visitType(Class<?> type) {
            if (type != null) { getOrCreateClass(type); }
        }

        private void visitObjectType(Object object) {
            if (object != null) { visitType(object.getClass()); }
        }

        private void visitObjectTypes(Object[] array) {
            if (array != null) {
                for (Object element : array) { visitObjectType(element); }
            }
        }

        private void visitGetter(IGetter getter) throws NoSuchFieldException, IllegalAccessException {
            if (getter == null) {
                return;
            }
            if (REFLECTED_FIELD_BINDING_CLASS.equals(getter.getClass().getName())) {
                visitFieldBinding(getter);
            }
            if (REFLECTED_METHOD_BINDING_CLASS.equals(getter.getClass().getName())) {
                visitMethodBinding(getter);
            }
        }

        private void visitSetter(ISetter setter) throws NoSuchFieldException, IllegalAccessException {
            if (setter == null) {
                return;
            }
            if (REFLECTED_FIELD_BINDING_CLASS.equals(setter.getClass().getName())) {
                visitFieldBinding(setter);
            }
            if (REFLECTED_METHOD_BINDING_CLASS.equals(setter.getClass().getName())) {
                visitMethodBinding(setter);
            }
        }

        private void visitFieldBinding(Object fieldBinding) throws IllegalAccessException, NoSuchFieldException {
            Field field = (Field) accessibleField(fieldBinding.getClass(), REFLECTED_FIELD_BINDING_FIELD).get(fieldBinding);
            getOrCreateClass(field.getDeclaringClass())
                    .addField(field.getName(), isFinal(field));

            Object scope = accessibleField(fieldBinding.getClass(), REFLECTED_BINDING_FIELD_SCOPE).get(fieldBinding);
            getOrCreateClass(scope.getClass());
        }

        private void visitMethodBinding(Object methodBinding) throws IllegalAccessException, NoSuchFieldException {
            Method method = (Method) accessibleField(methodBinding.getClass(), REFLECTED_METHOD_BINDING_METHOD).get(methodBinding);
            ReflectedClass cls = getOrCreateClass(method.getDeclaringClass());
            cls.addMethod(method.getName(), method.getParameterTypes());

            Object scope = accessibleField(methodBinding.getClass(), REFLECTED_BINDING_FIELD_SCOPE).get(methodBinding);
            ReflectedClass scopeClass = getOrCreateClass(scope.getClass());
            if (!scope.getClass().equals(method.getDeclaringClass())) {
                scopeClass.addMethod(method.getName(), method.getParameterTypes());
            }
        }

        private static Field accessibleField(Class<?> cls, String fieldName) throws NoSuchFieldException {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }

        ReflectedClass getOrCreateClass(Class<?> cls) {
            if (cls.isPrimitive()) {
                return new ReflectedClass(cls.getName()); // don't store
            }
            return getOrCreateClassName(cls.getName());
        }
        private ReflectedClass getOrCreateClassName(String name) {
            ReflectedClass result = visited.get(name);
            if (result == null) {
                result = new ReflectedClass(name);
                if (!excluded(name)) {
                    visited.put(name, result);
                }
            }
            return result;
        }

        static boolean excluded(String fqcn) {
            String[] excludes = System.getProperty(SYSPROP_CODEGEN_EXCLUDES, "").split(",");
            for (String regex : excludes) {
                if (fqcn.matches(regex)) {
                    System.err.printf("Class %s is excluded: (%s=%s)%n", fqcn, SYSPROP_CODEGEN_EXCLUDES, System.getProperty(SYSPROP_CODEGEN_EXCLUDES));
                    return true;
                }
            }
            return false;
        }
    }
    static class ReflectedClass {
        private final String name;
        private final Set<ReflectedField> fields = new TreeSet<ReflectedField>();
        private final Set<ReflectedMethod> methods = new TreeSet<ReflectedMethod>();

        ReflectedClass(String name) {
            this.name = name;
        }

        ReflectedClass addField(String fieldName, boolean isFinal) {
            fields.add(new ReflectedField(fieldName, isFinal));
            return this;
        }

        ReflectedClass addMethod0(String methodName, String... paramTypes) {
            methods.add(new ReflectedMethod(methodName, paramTypes));
            return this;
        }

        ReflectedClass addMethod(String methodName, Class... paramClasses) {
            String[] paramTypes = new String[paramClasses.length];
            for (int i = 0; i < paramClasses.length; i++) {
                paramTypes[i] = paramClasses[i].getName();
            }
            return addMethod0(methodName, paramTypes);
        }

        @Override
        public String toString() {
            String result = String.format("" +
                    "  {%n" +
                    "    \"name\" : \"%s\",%n" +
                    "    \"allDeclaredConstructors\" : true,%n" +
                    "    \"allPublicConstructors\" : true,%n" +
                    "    \"allDeclaredMethods\" : true,%n" +
                    "    \"allPublicMethods\" : true", name);
            if (!fields.isEmpty()) {
                result += String.format(",%n    \"fields\" : ");
                String prefix = String.format("[%n"); // start JSON array
                for (ReflectedField field : fields) {
                    result += String.format("%s      %s", prefix, field);
                    prefix = String.format(",%n");
                }
                result += String.format("%n    ]"); // end JSON array
            }
            if (!methods.isEmpty()) {
                result += String.format(",%n    \"methods\" : ");
                String prefix = String.format("[%n"); // start JSON array
                for (ReflectedMethod method : methods) {
                    result += String.format("%s      %s", prefix, method);
                    prefix = String.format(",%n");
                }
                result += String.format("%n    ]"); // end JSON array
            }
            result += String.format("%n  }");
            return result;
        }
    }
    static class ReflectedMethod implements Comparable<ReflectedMethod> {
        private final String name;
        private final String[] paramTypes;

        ReflectedMethod(String name, String... paramTypes) {
            this.name = name;
            this.paramTypes = paramTypes.clone();
        }
        @Override public int hashCode() { return name.hashCode() * Arrays.hashCode(paramTypes); }
        @Override public boolean equals(Object o) {
            return o instanceof ReflectedMethod
                    && ((ReflectedMethod) o).name.equals(name)
                    && Arrays.equals(((ReflectedMethod) o).paramTypes, paramTypes);
        }

        @Override
        public String toString() {
            return String.format("{ \"name\" : \"%s\", \"parameterTypes\" : [%s] }", name, formatParamTypes());
        }

        private String formatParamTypes() {
            StringBuilder result = new StringBuilder();
            for (String type : paramTypes) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append('"').append(type).append('"');
            }
            return result.toString();
        }

        public int compareTo(ReflectedMethod o) {
            int result = name.compareTo(o.name);
            if (result == 0) {
                result = Arrays.toString(this.paramTypes).compareTo(Arrays.toString(o.paramTypes));
            }
            return result;
        }
    }
    static class ReflectedField implements Comparable<ReflectedField> {
        private final String name;
        private final boolean isFinal;

        ReflectedField(String name, boolean isFinal) {
            this.name = name;
            this.isFinal = isFinal;
        }
        @Override public int hashCode() { return name.hashCode(); }
        @Override public boolean equals(Object o) {
            return o instanceof ReflectedField && ((ReflectedField) o).name.equals(name);
        }

        @Override
        public String toString() {
            return isFinal
                    ? String.format("{ \"name\" : \"%s\", \"allowWrite\" : true }", name)
                    : String.format("{ \"name\" : \"%s\" }", name);
        }

        public int compareTo(ReflectedField o) {
            return name.compareTo(o.name);
        }
    }
}
