package picocli.codegen.aot.graalvm;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElement;
import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.IScope;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UnmatchedArgsBinding;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.annotation.processing.ITypeMetaData;
import picocli.codegen.annotation.processing.AnnotatedElementHolder;
import picocli.codegen.util.Util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * {@code ReflectionConfigGenerator} generates a JSON String with the program elements that will be accessed
 * reflectively in a picocli-based application, in order to compile this application ahead-of-time into a native
 * executable with GraalVM.
 * <p>
 * GraalVM has <a href="https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/">limited support for Java
 * reflection</a> and it needs to know ahead of time the reflectively accessed program elements.
 * </p><p>
 * The output of {@code ReflectionConfigGenerator} is intended to be passed to the {@code -H:ReflectionConfigurationFiles=/path/to/reflect-config.json}
 * option of the {@code native-image} <a href="https://www.graalvm.org/latest/reference-manual/native-image/">GraalVM utility</a>.
 * This allows picocli-based applications to be compiled to a native image.
 * </p><p>
 * Alternatively, the generated <a href="https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildConfiguration/">configuration</a>
 * files can be supplied to the {@code native-image} tool by placing them in a
 * {@code META-INF/native-image/} directory on the class path, for example, in a JAR file used in the image build.
 * This directory (or any of its subdirectories) is searched for files with the names {@code jni-config.json},
 * {@code reflect-config.json}, {@code proxy-config.json} and {@code resource-config.json}, which are then automatically
 * included in the build. Not all of those files must be present.
 * When multiple files with the same name are found, all of them are included.
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
    private static final String REFLECTED_PROXY_METHOD_BINDING_CLASS = "picocli.CommandLine$Model$PicocliInvocationHandler$ProxyBinding";
    private static final String REFLECTED_FIELD_BINDING_FIELD = "field";
    private static final String REFLECTED_METHOD_BINDING_METHOD = "method";
    private static final String REFLECTED_BINDING_FIELD_SCOPE = "scope";

    @Command(name = "gen-reflect-config", showAtFileInUsageHelp = true,
            description = {"Generates a JSON file with the program elements that will be " +
                    "accessed reflectively for the specified `@Command` classes.",
                    "The generated JSON file can be passed to the `-H:ReflectionConfigurationFiles=/path/to/reflect-config.json` " +
                    "option of the `native-image` GraalVM utility.",
                    "See https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/"},
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
                            "picocli.codegen.aot.graalvm.ReflectionConfigGenerator my.pkg.MyClass"
            },
            mixinStandardHelpOptions = true, sortOptions = false,
            version = "picocli-codegen ${COMMAND-NAME} " + CommandLine.VERSION)
    private static class App implements Callable<Integer> {

        @Parameters(arity = "1..*", description = "One or more classes to generate a GraalVM ReflectionConfiguration for.")
        Class<?>[] classes = new Class<?>[0];

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
            String result = ReflectionConfigGenerator.generateReflectionConfig(specs.toArray(new CommandSpec[0]));
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
     * Returns a JSON String with the program elements that will be accessed reflectively for the specified
     * {@code CommandSpec} objects.
     *
     * @param specs one or more {@code CommandSpec} objects to inspect
     * @return a JSON String in the <a href="https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/#manual-configuration">format</a>
     *       required by the {@code -H:ReflectionConfigurationFiles=/path/to/reflect-config.json} option of the GraalVM {@code native-image} utility.
     * @throws NoSuchFieldException if a problem occurs while processing the specified specs
     * @throws IllegalAccessException if a problem occurs while processing the specified specs
     */
    public static String generateReflectionConfig(CommandSpec... specs) throws Exception {
        Visitor visitor = new Visitor();
        for (CommandSpec spec : specs) {
            visitor.visitCommandSpec(spec);
        }
        return generateReflectionConfig(visitor).toString();
    }

    private static StringBuilder generateReflectionConfig(Visitor visited) {
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
        static Set<String> excluded = new HashSet<String>(Arrays.asList(
                boolean.class.getName(), boolean[].class.getName(), boolean[].class.getCanonicalName(),
                byte.class.getName(),    byte[].class.getName(),    byte[].class.getCanonicalName(),
                char.class.getName(),    char[].class.getName(),    char[].class.getCanonicalName(),
                double.class.getName(),  double[].class.getName(),  double[].class.getCanonicalName(),
                float.class.getName(),   float[].class.getName(),   float[].class.getCanonicalName(),
                int.class.getName(),     int[].class.getName(),     int[].class.getCanonicalName(),
                long.class.getName(),    long[].class.getName(),    long[].class.getCanonicalName(),
                short.class.getName(),   short[].class.getName(),   short[].class.getCanonicalName(),
                CommandSpec.class.getName(),
                Method.class.getName(),
                Object.class.getName(),
                String.class.getName(),
                String[].class.getName(), String[].class.getCanonicalName(),
                File.class.getName(),
                File[].class.getName(), File[].class.getCanonicalName(),
                List.class.getName(),
                Set.class.getName(),
                Map.class.getName(),
                Class.class.getName(), Class[].class.getName(), Class[].class.getCanonicalName(), "java.lang.Class<?>", "java.lang.Class<?>[]",
                "java.lang.reflect.Executable", // addMethod("getParameters")
                "java.lang.reflect.Parameter", // addMethod("getName");
                "org.fusesource.jansi.AnsiConsole", // addField("out", false);
                "java.util.ResourceBundle", // addMethod("getBaseBundleName");
                "java.time.Duration", // addMethod("parse", CharSequence.class);
                "java.time.Instant", // addMethod("parse", CharSequence.class);
                "java.time.LocalDate", // addMethod("parse", CharSequence.class);
                "java.time.LocalDateTime", // addMethod("parse", CharSequence.class);
                "java.time.LocalTime", // addMethod("parse", CharSequence.class);
                "java.time.MonthDay", // addMethod("parse", CharSequence.class);
                "java.time.OffsetDateTime", // addMethod("parse", CharSequence.class);
                "java.time.OffsetTime", // addMethod("parse", CharSequence.class);
                "java.time.Period", // addMethod("parse", CharSequence.class);
                "java.time.Year", // addMethod("parse", CharSequence.class);
                "java.time.YearMonth", // addMethod("parse", CharSequence.class);
                "java.time.ZonedDateTime", // addMethod("parse", CharSequence.class);
                "java.time.ZoneId", // addMethod("of", String.class);
                "java.time.ZoneOffset", // addMethod("of", String.class);
                "java.nio.file.Path",
                "java.nio.file.Paths", // addMethod("get", String.class, String[].class);

                "java.sql.Connection",
                "java.sql.Driver",
                "java.sql.DriverManager", // .addMethod("getConnection", String.class) .addMethod("getDriver", String.class);
                "java.sql.Time", // constructor long
                "java.sql.Timestamp"  // addMethod("valueOf", String.class);
        ));
        Map<String, ReflectedClass> visited = new TreeMap<String, ReflectedClass>();

        void visitCommandSpec(CommandSpec spec) throws Exception {
            Object userObject = spec.userObject();
            if (userObject != null) {
                if (userObject instanceof Method) {
                    Method method = (Method) spec.userObject();
                    ReflectedClass cls = getOrCreateClass(method.getDeclaringClass());
                    cls.addMethod(method);
                } else if (userObject instanceof Element) {
                    visitElement((Element) userObject);
                } else if (Proxy.isProxyClass(spec.userObject().getClass())) {
                    // do nothing: requires DynamicProxyConfigGenerator
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
            for (IAnnotatedElement specElement : spec.specElements()) {
                visitGetter(specElement.getter());
                visitSetter(specElement.setter());
            }
            for (IAnnotatedElement parentCommandElement : spec.parentCommandElements()) {
                visitGetter(parentCommandElement.getter());
                visitSetter(parentCommandElement.setter());
            }
            for (OptionSpec option : spec.options()) {
                visitArgSpec(option);
            }
            for (PositionalParamSpec positional : spec.positionalParameters()) {
                visitArgSpec(positional);
            }
            for (ArgGroupSpec group : spec.argGroups()) {
                visitGroupSpec(group);
            }
            for (Map.Entry<String, CommandSpec> entry : spec.mixins().entrySet()) {
                CommandSpec mixin = entry.getValue();
                visitCommandSpec(mixin);

                String name = entry.getKey();
                IAnnotatedElement annotatedElement = spec.mixinAnnotatedElements().get(name);
                if (annotatedElement != null) {
                    visitGetter(annotatedElement.getter());
                }
            }
            for (CommandLine sub : spec.subcommands().values()) {
                visitCommandSpec(sub.getCommandSpec());
            }
        }

        @SuppressWarnings("deprecation") // SimpleElementVisitor6 is deprecated in Java 9
        private void visitElement(Element element) {
            element.accept(new SimpleElementVisitor6<Void, Void>() {
                @Override
                public Void visitVariable(VariableElement e, Void aVoid) {
                    if (!e.asType().getKind().isPrimitive() &&
                            !(e.getKind() == ElementKind.INTERFACE && e.asType().toString().startsWith("java"))) {
                        getOrCreateClassByName(elementTypeName(e.asType()));
                    }
                    if (e.getKind() == ElementKind.FIELD) {
                        TypeElement type = (TypeElement) e.getEnclosingElement();
                        ReflectedClass cls = getOrCreateClassByName(elementTypeName(type.asType()));
                        cls.addField(e.getSimpleName().toString(), e.getModifiers().contains(javax.lang.model.element.Modifier.FINAL));
                    }
                    return null;
                }
                @Override public Void visitType(TypeElement e, Void aVoid) {
                    if (!e.asType().getKind().isPrimitive() &&
                            !(e.getKind() == ElementKind.INTERFACE && e.getQualifiedName().toString().startsWith("java"))) {
                        getOrCreateClassByName(elementTypeName(e.asType()));
                    }
                    return null;
                }
                @Override public Void visitExecutable(ExecutableElement method, Void aVoid) {
                    TypeElement type = (TypeElement) method.getEnclosingElement();
                    ReflectedClass cls = getOrCreateClassByName(elementTypeName(type.asType()));
                    List<? extends VariableElement> parameters = method.getParameters();
                    List<String> paramTypeNames = new ArrayList<String>();
                    for (VariableElement param : parameters) {
                        param.asType().accept(new SimpleTypeVisitor6<Void, List<String>>() {
                            @Override
                            public Void visitPrimitive(PrimitiveType t, List<String> collect) {
                                collect.add(t.toString());
                                return null;
                            }
                            @Override
                            public Void visitArray(ArrayType t, List<String> collect) {
                                collect.add(t.toString());
                                return null;
                            }
                            @Override
                            public Void visitDeclared(DeclaredType t, List<String> collect) {
                                collect.add(elementTypeName(t)); // t.toString() would give "java.util.List<java.io.File>"
                                return null;
                            }
                        }, paramTypeNames);
                    }
                    cls.addMethod(method.getSimpleName().toString(), paramTypeNames);
                    return null;
                }
            }, null);
        }

        // convert canonical type names (picocli.AutoComplete.App) to class name (picocli.AutoComplete$App)
        // convert generic (java.util.List<java.io.File>) to raw (java.util.List)
        private String elementTypeName(TypeMirror typeMirror) {
            @SuppressWarnings("deprecation") // SimpleElementVisitor6 is deprecated in Java 9
            String result = typeMirror.accept(new SimpleTypeVisitor6<String, Void>() {
                @Override
                public String visitDeclared(DeclaredType declaredType, Void aVoid) {
                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                    if (typeElement.getNestingKind().isNested()) {
                        return elementTypeName(typeElement.getEnclosingElement().asType()) + "$" + typeElement.getSimpleName();
                    }
                    String raw = typeElement.getQualifiedName().toString();
                    return raw;
                }

                @Override
                public String visitArray(ArrayType arrayType, Void aVoid) {
                    return elementTypeName(arrayType.getComponentType()) + "[]";
                }
            }, null);
            if (result == null) {
                return typeMirror.toString();
            }
            return result;
        }

        private void visitAnnotatedFields(Class<?> cls) {
            if (cls == null) {
                return;
            }
            ReflectedClass reflectedClass = getOrCreateClass(cls);
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field f : declaredFields) {
                if (f.isAnnotationPresent(CommandLine.Spec.class)) {
                    reflectedClass.addField(f);
                }
                if (f.isAnnotationPresent(CommandLine.ParentCommand.class)) {
                    reflectedClass.addField(f);
                }
                if (f.isAnnotationPresent(Mixin.class)) {
                    reflectedClass.addField(f);
                }
                if (f.isAnnotationPresent(CommandLine.Unmatched.class)) {
                    reflectedClass.addField(f);
                }
            }
            visitAnnotatedFields(cls.getSuperclass());
        }

        private void visitArgSpec(ArgSpec argSpec) throws Exception {
            visitGetter(argSpec.getter());
            visitSetter(argSpec.setter());
            visitTypeInfo(argSpec.typeInfo());
            visitObjectType(argSpec.completionCandidates());
            visitObjectType(argSpec.parameterConsumer());
            visitObjectTypes(argSpec.converters());
        }

        private void visitGroupSpec(ArgGroupSpec group) throws Exception {
            IScope scope = group.scope();
            if (scope != null) {
                Object scopeValue = scope.get();
                if (scopeValue != null) {
                    getOrCreateClass(scopeValue.getClass());
                }
            }
            visitGetter(group.getter());
            visitSetter(group.setter());
            for (ArgSpec argSpec : group.args()) {
                visitArgSpec(argSpec);
            }
            for (ArgGroupSpec subGroup : group.subgroups()) {
                visitGroupSpec(subGroup);
            }
        }

        private void visitTypeInfo(CommandLine.Model.ITypeInfo typeInfo) {
            getOrCreateClassByName(typeInfo.getClassName());
            for (CommandLine.Model.ITypeInfo aux : typeInfo.getAuxiliaryTypeInfos()) {
                getOrCreateClassByName(aux.getClassName());
            }
        }

        private void visitType(Class<?> type) {
            if (type != null) { getOrCreateClass(type); }
        }

        private void visitObjectType(Object object) {
            if (object == null) {
                return;
            }
            if (object instanceof ITypeMetaData) {
                visitElement(((ITypeMetaData) object).getTypeElement());
            } else {
                visitType(object.getClass());
            }
        }

        private void visitObjectTypes(Object[] array) {
            if (array != null) {
                for (Object element : array) { visitObjectType(element); }
            }
        }

        private void visitGetter(IGetter getter) throws Exception {
            if (getter == null) {
                return;
            }
            if (getter instanceof AnnotatedElementHolder) {
                AnnotatedElementHolder metaData = (AnnotatedElementHolder) getter;
                visitElement(metaData.getElement());
            } else if (REFLECTED_FIELD_BINDING_CLASS.equals(getter.getClass().getName())) {
                visitFieldBinding(getter);
            } else if (REFLECTED_METHOD_BINDING_CLASS.equals(getter.getClass().getName())) {
                visitMethodBinding(getter);
            } else if (REFLECTED_PROXY_METHOD_BINDING_CLASS.equals(getter.getClass().getName())) {
                visitProxyMethodBinding(getter);
            }
        }

        private void visitSetter(ISetter setter) throws Exception {
            if (setter == null) {
                return;
            }
            if (setter instanceof AnnotatedElementHolder) {
                AnnotatedElementHolder metaData = (AnnotatedElementHolder) setter;
                visitElement(metaData.getElement());
            } else if (REFLECTED_FIELD_BINDING_CLASS.equals(setter.getClass().getName())) {
                visitFieldBinding(setter);
            } else if (REFLECTED_METHOD_BINDING_CLASS.equals(setter.getClass().getName())) {
                visitMethodBinding(setter);
            }
        }

        private void visitFieldBinding(Object fieldBinding) throws Exception {
            Field field = (Field) accessibleField(fieldBinding.getClass(), REFLECTED_FIELD_BINDING_FIELD).get(fieldBinding);
            getOrCreateClass(field.getDeclaringClass())
                    .addField(field);

            IScope scope = (IScope) accessibleField(fieldBinding.getClass(), REFLECTED_BINDING_FIELD_SCOPE).get(fieldBinding);
            Object scopeValue = scope.get();
            if (scopeValue != null) {
                getOrCreateClass(scopeValue.getClass());
            }
        }

        private void visitMethodBinding(Object methodBinding) throws Exception {
            Method method = (Method) accessibleField(methodBinding.getClass(), REFLECTED_METHOD_BINDING_METHOD).get(methodBinding);
            ReflectedClass cls = getOrCreateClass(method.getDeclaringClass());
            cls.addMethod(method);
        }

        private void visitProxyMethodBinding(Object methodBinding) throws Exception {
            Method method = (Method) accessibleField(methodBinding.getClass(), REFLECTED_METHOD_BINDING_METHOD).get(methodBinding);
            ReflectedClass cls = getOrCreateClass(method.getDeclaringClass());
            cls.addMethod(method);
        }

        private static Field accessibleField(Class<?> cls, String fieldName) throws NoSuchFieldException {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }

        ReflectedClass getOrCreateClass(Class<?> cls) {
            if (cls.isPrimitive() || (cls.isInterface() && cls.getName().startsWith("java"))) {
                return new ReflectedClass(cls); // don't store
            }
            return getOrCreateClassByName(cls);
        }
        private ReflectedClass getOrCreateClassByName(Class<?> cls) {
            return getOrCreateClassByName(cls.getName());
        }
        private ReflectedClass getOrCreateClassByName(String name) {
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
            if (excluded.contains(fqcn)) { return true; }
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

        ReflectedClass(Class<?> cls) {
            this(cls.getName());
        }
        ReflectedClass(String name) {
            this.name = name;
        }

        private boolean isFinal(Field f) {
            return (f.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
        }

        ReflectedClass addField(Field field) {
            return addField(field.getName(), isFinal(field));
        }

        ReflectedClass addField(String fieldName, boolean isFinal) {
            fields.add(new ReflectedField(fieldName, isFinal));
            return this;
        }

        ReflectedClass addMethod0(String methodName, String... paramTypes) {
            methods.add(new ReflectedMethod(methodName, paramTypes));
            return this;
        }

        ReflectedClass addMethod(Method method) {
            return addMethod(method.getName(), method.getParameterTypes());
        }

        ReflectedClass addMethod(String methodName, Class... paramClasses) {
            String[] paramTypes = new String[paramClasses.length];
            for (int i = 0; i < paramClasses.length; i++) {
                paramTypes[i] = paramClasses[i].getName();
            }
            return addMethod0(methodName, paramTypes);
        }

        public ReflectedClass addMethod(String methodName, List<String> paramTypeNames) {
            return addMethod0(methodName, paramTypeNames.toArray(new String[0]));
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
