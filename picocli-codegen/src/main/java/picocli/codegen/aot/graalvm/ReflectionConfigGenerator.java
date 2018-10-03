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
import picocli.CommandLine.Parameters;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class ReflectionConfigGenerator {

    @Command(name = "ReflectionConfigGenerator",
            description = {"Generates a JSON file with the program elements that will be " +
                    "accessed reflectively for the specified @Command classes. " +
                    "The generated JSON file can be passed to the -H:ReflectionConfigurationFiles=/path/to/reflectconfig " +
                    "option of the `native-image` GraalVM utility.",
                    "See https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md"},
            mixinStandardHelpOptions = true, version = "picocli-codegen 3.7.0")
    private static class App implements Callable<String> {
        @Parameters(arity = "1..*", description = "One or more classes to generate a GraalVM ReflectionConfiguration for.")
        Class<?>[] classes = new Class<?>[0];

        //@Override
        public String call() throws NoSuchFieldException, IllegalAccessException {
            List<CommandSpec> specs = new ArrayList<CommandSpec>();
            for (Class<?> cls : classes) {
                specs.add(new CommandLine(cls).getCommandSpec());
            }
            return new ReflectionConfigGenerator().generateReflectionConfig(specs.toArray(new CommandSpec[0]));
        }
    }
    public static void main(String... args) {
        String result = CommandLine.call(new App(), args);
        if (result != null) {
            System.out.println(result);
        }
    }

    public String generateReflectionConfig(CommandSpec... specs) throws NoSuchFieldException, IllegalAccessException {
        Visitor visitor = new Visitor();
        for (CommandSpec spec : specs) {
            visitor.visit(spec);
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
        Map<String, ReflectedClass> visited = new HashMap<String, ReflectedClass>();

        Visitor() {
            getOrCreate(Method.class.getName()).addMethod("getParameters");
            getOrCreate("java.lang.reflect.Parameter").addMethod("getName");
        }

        void visit(CommandSpec spec) throws NoSuchFieldException, IllegalAccessException {
            if (spec.userObject() != null) {
                if (spec.userObject() instanceof Method) {
                    Method method = (Method) spec.userObject();
                    ReflectedClass cls = getOrCreate(method.getDeclaringClass().getName());
                    cls.addMethod(method.getName(), method.getParameterTypes());
                } else if (Proxy.isProxyClass(spec.userObject().getClass())) {
                    // do nothing
                } else {
                    getOrCreate(spec.userObject().getClass().getName());
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
                visit(mixin);
            }
            for (CommandLine sub : spec.subcommands().values()) {
                visit(sub.getCommandSpec());
            }
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
            if (type != null) { getOrCreate(type.getName()); }
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
            if ("picocli.CommandLine$Model$FieldBinding".equals(getter.getClass().getName())) {
                visitFieldBinding(getter);
            }
            if ("picocli.CommandLine$Model$MethodBinding".equals(getter.getClass().getName())) {
                visitMethodBinding(getter);
            }
        }

        private void visitSetter(ISetter setter) throws NoSuchFieldException, IllegalAccessException {
            if ("picocli.CommandLine$Model$FieldBinding".equals(setter.getClass().getName())) {
                visitFieldBinding(setter);
            }
            if ("picocli.CommandLine$Model$MethodBinding".equals(setter.getClass().getName())) {
                visitMethodBinding(setter);
            }
        }

        private void visitFieldBinding(Object fieldBinding) throws IllegalAccessException, NoSuchFieldException {
            Field field = (Field) accessibleField(fieldBinding.getClass(), "field").get(fieldBinding);
            Object scope = accessibleField(fieldBinding.getClass(),"scope").get(fieldBinding);
            getOrCreate(scope.getClass().getName()).addField(field.getName());
        }

        private void visitMethodBinding(Object methodBinding) throws IllegalAccessException, NoSuchFieldException {
            Method method = (Method) accessibleField(methodBinding.getClass(), "method").get(methodBinding);
            ReflectedClass cls = getOrCreate(method.getDeclaringClass().getName());
            cls.addMethod(method.getName(), method.getParameterTypes());

            Object scope = accessibleField(methodBinding.getClass(),"scope").get(methodBinding);
            ReflectedClass scopeClass = getOrCreate(scope.getClass().getName());
            if (!scope.getClass().equals(method.getDeclaringClass())) {
                scopeClass.addMethod(method.getName(), method.getParameterTypes());
            }
        }

        private static Field accessibleField(Class<?> cls, String fieldName) throws NoSuchFieldException {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }

        ReflectedClass getOrCreate(String name) {
            ReflectedClass result = visited.get(name);
            if (result == null) {
                result = new ReflectedClass(name);
                visited.put(name, result);
            }
            return result;
        }
    }
    static class ReflectedClass {
        private final String name;
        private final Set<ReflectedField> fields = new LinkedHashSet<ReflectedField>();
        private final Set<ReflectedMethod> methods = new LinkedHashSet<ReflectedMethod>();

        ReflectedClass(String name) {
            this.name = name;
        }

        ReflectedClass addField(String fieldName) {
            fields.add(new ReflectedField(fieldName));
            return this;
        }

        ReflectedClass addMethod0(String methodName, String... paramTypes) {
            methods.add(new ReflectedMethod(methodName, paramTypes));
            return this;
        }

        ReflectedClass addMethod(String methodName, Class... paramClasses) {
            String[] paramTypes = new String[paramClasses.length];
            for (int i = 0; i < paramClasses.length; i++) {
                paramTypes[i] = String.valueOf(paramClasses[i]);
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
                    result += String.format("%s    %s", prefix, field);
                    prefix = String.format(",%n");
                }
                result += String.format("%n    ]"); // end JSON array
            }
            if (!methods.isEmpty()) {
                result += String.format(",%n    \"methods\" : ");
                String prefix = String.format("[%n"); // start JSON array
                for (ReflectedMethod method : methods) {
                    result += String.format("%s    %s", prefix, method);
                    prefix = String.format(",%n");
                }
                result += String.format("%n    ]"); // end JSON array
            }
            result += String.format("%n  }");
            return result;
        }
    }
    static class ReflectedMethod {
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
            return String.format("    { \"name\" : \"%s\", \"parameterTypes\" : [%s] }", name, formatParamTypes());
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
    }
    static class ReflectedField {
        private final String name;

        ReflectedField(String name) {
            this.name = name;
        }
        @Override public int hashCode() { return name.hashCode(); }
        @Override public boolean equals(Object o) {
            return o instanceof ReflectedField && ((ReflectedField) o).name.equals(name);
        }

        @Override
        public String toString() {
            return String.format("    { \"name\" : \"%s\" }", name);
        }
    }
}
