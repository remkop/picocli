package picocli.annotation.processing.tests;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.ITypeInfo;
import picocli.CommandLine.Model.MethodParam;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;
import picocli.codegen.annotation.processing.ITypeMetaData;
import picocli.codegen.util.Assert;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class AnnotatedCommandSourceGenerator {

    @Command(name = "<main class>",
            aliases = {},
            mixinStandardHelpOptions = false,
            headerHeading = "",
            header = {},
            descriptionHeading = "",
            description = {},
            synopsisHeading = "Usage: ",
            abbreviateSynopsis = false,
            customSynopsis = {},
            optionListHeading = "",
            parameterListHeading = "",
            commandListHeading = "Commands:%n",
            footerHeading = "",
            footer = {},
            requiredOptionMarker = ' ',
            addMethodSubcommands = true,
            subcommands = {},
            version = {},
//            versionProvider = null,
            showDefaultValues = false,
//            defaultValueProvider = null,
            resourceBundle = "",
            sortOptions = true,
            hidden = false,
            helpCommand = false,
            separator = "=",
            usageHelpWidth = 80)
    class App {
        @ParentCommand
        Object parent;

        @Spec
        CommandSpec spec;

        @Unmatched
        List<String> unmatched;

        @Mixin
        Object mixin;

        @Option(names = {},
                required = false,
                help = false,
                usageHelp = false,
                versionHelp = false,
                description = {},
                arity = "",
                paramLabel = "",
                hideParamSyntax = false,
                type = {},
                converter = {},
                split = "",
                hidden = false,
                defaultValue = "__no_default_value__",
                showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND,
//                completionCandidates = null,
                interactive = false,
                descriptionKey = "")
        Object option;

        @Parameters(index = "",
                description = {},
                arity = "",
                paramLabel = "",
                hideParamSyntax = false,
                type = {},
                converter = {},
                split = "",
                hidden = false,
                defaultValue = "__no_default_value__",
                showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND,
//                completionCandidates = null,
                interactive = false,
                descriptionKey = "")
        Object parameter;
    }

    private final static String INDENT_INCREMENT = "    ";
    private static final String[] EMPTY_ARRAY = new String[0];

    private final CommandSpec commandSpec;
    private TypeImporter importer;
    private String outputPackage;

    public AnnotatedCommandSourceGenerator(CommandSpec commandSpec) {
        this(commandSpec, extractPackageName(commandSpec.userObject()));
    }
    public AnnotatedCommandSourceGenerator(CommandSpec commandSpec, String outputPackage) {
        this.commandSpec = Assert.notNull(commandSpec, "commandSpec");
        this.outputPackage = Assert.notNull(outputPackage, "outputPackage");
        this.importer = new TypeImporter(outputPackage);
    }

    private static String extractPackageName(Object userObject) {
        if (userObject instanceof ExecutableElement) {
            return extractPackageName((TypeElement) ((ExecutableElement) userObject).getEnclosingElement());
        } else if (userObject instanceof TypeElement) {
            return extractPackageName((TypeElement) userObject);
        } else if (userObject instanceof Method) {
            return extractPackageName(((Method) userObject).getDeclaringClass());
        } else if (userObject instanceof Class) {
            return extractPackageName((Class<?>) userObject);
        } else {
            return extractPackageName(userObject.getClass());
        }
    }

    private static String extractPackageName(Class<?> cls) {
        return cls.getPackage().getName();
    }

    private static String extractPackageName(TypeElement typeElement) {
        Element enclosing = typeElement.getEnclosingElement();
        while (enclosing != null) {
            if (enclosing instanceof PackageElement) {
                PackageElement pkg = (PackageElement) enclosing;
                if (pkg.isUnnamed()) {
                    return "";
                }
                String fqcn = pkg.getQualifiedName().toString();
                return fqcn;
            }
            enclosing = enclosing.getEnclosingElement();
        }
        return "";
    }

    public String getOutputPackage() {
        return outputPackage;
    }

    public void setOutputPackage(String outputPackage) {
        this.outputPackage = Assert.notNull(outputPackage, "outputPackage");
        this.importer = new TypeImporter(outputPackage);
    }

    public String generate() {
        StringWriter result = new StringWriter();
        writeTo(new PrintWriter(result), "");
        return result.toString();
    }

    public void writeTo(PrintWriter pw, String indent) {
        StringWriter result = new StringWriter();
        PrintWriter tmp = new PrintWriter(result);
        printCommand(tmp, commandSpec, indent, new HashSet<Object>());

        pw.println("package " + outputPackage + ";");
        pw.println(importer.createImportDeclaration());
        pw.println();
        pw.print(result);
        pw.flush();
    }

    private void printCommand(PrintWriter pw, CommandSpec spec, String indent, Set<Object> visited) {
        Stack<String> after = new Stack<String>();
        Stack<Object> surroundingElements = new Stack<Object>();
        indent = printSurroundingElements(pw, spec.userObject(), indent, surroundingElements, after, visited);

        printCommandAnnotation(pw, spec, indent);
        pw.println();
        printCommandElementDefOpen(pw, spec.userObject(), indent);
        boolean isCommandMethod = isCommandMethod(spec);
        String indent2 = indent + INDENT_INCREMENT;

        for (String mixinName : spec.mixins().keySet()) {
            CommandSpec mixin = spec.mixins().get(mixinName);
            if ("picocli.CommandLine.AutoHelpMixin".equals(mixin.userObject().getClass().getCanonicalName())) {
                continue;
            }
            pw.println();
            pw.printf("%s@%s ", indent2, importer.getImportedName(Mixin.class.getCanonicalName()));
            pw.print(importer.getImportedName(extractClassName(mixin.userObject())));
            pw.println(" " + mixinName + ";");
        }

        String sep = "";
        for (OptionSpec option : spec.options()) {
            if (!isMixedIn(option, spec)) {
                pw.printf(sep);
                pw.println();
                printOptionAnnotation(pw, option, indent2);
                pw.printf(isCommandMethod ? " " : ("%n" + indent2));
                sep = printArgElementDef(pw, option.userObject(), isCommandMethod, indent2);
            }
        }
        for (PositionalParamSpec param : spec.positionalParameters()) {
            if (!isMixedIn(param, spec)) {
                pw.printf(sep);
                pw.println();
                printParametersAnnotation(pw, param, indent2);
                pw.printf(isCommandMethod ? " " : ("%n" + indent2));
                sep = printArgElementDef(pw, param.userObject(), isCommandMethod, indent2);
            }
        }
        if (!isCommandMethod) {
            pw.printf(sep);
        }

        for (String mixinName : spec.mixins().keySet()) {
            CommandSpec mixin = spec.mixins().get(mixinName);
            if (isNestedCommand(mixin, spec) && !isBuiltInMixin(mixin)) {
                pw.println();
                printCommand(pw, mixin, indent + INDENT_INCREMENT, visited);
            }
        }
        for (String subcommandName : spec.subcommands().keySet()) {
            CommandSpec subcommand = spec.subcommands().get(subcommandName).getCommandSpec();
            if (isNestedCommand(subcommand, spec) && !isBuiltInSubcommand(subcommand)) {
                pw.println();
                printCommand(pw, subcommand, indent + INDENT_INCREMENT, visited);
            }
        }
        printCommandElementDefClose(pw, spec.userObject(), indent);

        while (!after.isEmpty()) {
            Object surroundingElement = surroundingElements.pop();
            for (String mixinName : spec.mixins().keySet()) {
                CommandSpec mixin = spec.mixins().get(mixinName);
                if (isNested(mixin.userObject(), surroundingElement) && !isBuiltInMixin(mixin)) {
                    pw.println();
                    printCommand(pw, mixin, indent, visited);
                }
            }
            for (String subcommandName : spec.subcommands().keySet()) {
                CommandSpec subcommand = spec.subcommands().get(subcommandName).getCommandSpec();
                if (isNested(subcommand.userObject(), surroundingElement) && !isBuiltInSubcommand(subcommand)) {
                    pw.println();
                    printCommand(pw, subcommand, indent, visited);
                }
            }
            pw.print(after.pop());
        }
    }

    public static boolean isBuiltInMixin(CommandSpec mixin) {
        String str = mixin.userObject().toString();
        return "picocli.CommandLine.AutoHelpMixin".equals(str)
                || "picocli.CommandLine$AutoHelpMixin".equals(str);
    }

    public static boolean isBuiltInSubcommand(CommandSpec subcommand) {
        String str = subcommand.userObject().toString();
        return "picocli.CommandLine.HelpCommand".equals(str)
                || "picocli.CommandLine$HelpCommand".equals(str);
    }

    @SuppressWarnings("unchecked")
    private String printSurroundingElements(PrintWriter pw,
                                            Object userObject,
                                            String indent,
                                            Stack<Object> surrounding,
                                            Stack<String> after,
                                            Set<Object> visited) {

        collectEnclosingElements(userObject, surrounding, visited);
        Stack<Object> enclosing = (Stack<Object>) surrounding.clone();
        Queue<String> indents = new LinkedList<String>();
        for (int i = 0; i < enclosing.size(); i++) {
            indents.add(indent);
            indent += "    ";
        }

        String currentIndent = indent;
        Stack<String> before = new Stack<String>();
        while (!enclosing.isEmpty()) {
            Object obj = enclosing.pop();
            currentIndent = indents.poll();
            if (obj == userObject) {
                 break;
            }

            StringWriter sw = new StringWriter();
            if (obj instanceof Method || obj instanceof ExecutableElement) {
                printArgElementDef(new PrintWriter(sw), obj, true, currentIndent);
                String definition = sw.toString();
                definition = definition.substring(0, definition.indexOf("{") + 1);
                before.push(String.format("%s%n", definition));
                after.push(String.format("%s}%n", currentIndent));

            } else {
                printCommandElementDefOpen(new PrintWriter(sw), obj, currentIndent);
                before.push(String.format("%s%n", sw.toString()));
                sw.getBuffer().setLength(0);
                printCommandElementDefClose(new PrintWriter(sw), obj, currentIndent);
                after.push(sw.toString());
            }
        }
        while (!before.isEmpty()) {
            pw.print(before.pop());
        }
        return currentIndent;
    }

    private void collectEnclosingElements(Object userObject, Stack<Object> enclosing, Set<Object> visited) {
        if (visited.contains(userObject)) {
            return;
        }
        visited.add(userObject);
        enclosing.add(userObject);
        if (userObject instanceof Method) {
            collectEnclosingElements(((Method) userObject).getDeclaringClass(), enclosing, visited);
        } else if (userObject instanceof ExecutableElement) {
            collectEnclosingElements(((ExecutableElement) userObject).getEnclosingElement(), enclosing, visited);
        } else if (userObject instanceof Class) {
            Class type = (Class) userObject;
            if (type.getEnclosingMethod() != null) {
                collectEnclosingElements(type.getEnclosingMethod(), enclosing, visited);
            } else if (type.getDeclaringClass() != null) {
                collectEnclosingElements(type.getDeclaringClass(), enclosing, visited);
            }
        } else if (userObject instanceof TypeElement) {
            Element enclosingElement = ((TypeElement) userObject).getEnclosingElement();
            if (enclosingElement instanceof TypeElement || enclosingElement instanceof ExecutableElement) {
                collectEnclosingElements(enclosingElement, enclosing, visited);
            }
        }
    }

    public static boolean isNestedCommand(CommandSpec inner, CommandSpec outer) {
        Object innerUserObject = inner.userObject();
        Object outerUserObject = outer.userObject();
        return isNested(innerUserObject, outerUserObject);
    }

    private static boolean isNested(Object innerUserObject, Object outerUserObject) {
        if (innerUserObject instanceof Method) {
            Class<?> cls = ((Method) innerUserObject).getDeclaringClass();
            if (cls.equals(outerUserObject) || cls.equals(outerUserObject.getClass())) {
                return true;
            }
        } else if (innerUserObject instanceof Element) { // ExecutableElement or TypeElement
            Element enclosingElement = ((Element) innerUserObject).getEnclosingElement();
            while (enclosingElement != null) {
                if (enclosingElement.equals(outerUserObject)) {
                    return true;
                }
                enclosingElement = enclosingElement.getEnclosingElement();
            }
            return false;
        } else if (innerUserObject instanceof Class) {
            Class<?> cls = (Class<?>) innerUserObject;
            if (cls.isMemberClass() &&
                    (cls.getEnclosingClass().equals(outerUserObject) || cls.getEnclosingClass().equals(outerUserObject.getClass()))) {
                return true;
            }
        } else {
            Class<?> cls = innerUserObject.getClass();
            if (cls.isMemberClass() &&
                    (cls.getEnclosingClass().equals(outerUserObject) || cls.getEnclosingClass().equals(outerUserObject.getClass()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMixedIn(OptionSpec option, CommandSpec spec) {
        for (CommandSpec mixin : spec.mixins().values()) {
            if (mixin.findOption(option.longestName()) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMixedIn(PositionalParamSpec positional, CommandSpec spec) {
        for (CommandSpec mixin : spec.mixins().values()) {
            for (PositionalParamSpec mixedIn : mixin.positionalParameters()) {
                if (mixedIn.equals(positional)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String argElementName(ArgSpec argSpec) {
        Object userObject = argSpec.userObject();
        if (userObject instanceof Field) {
            return ((Field) userObject).getName();
        } else if (userObject instanceof MethodParam) {
            return ((MethodParam) userObject).getName();
        } else if (userObject instanceof Method) {
            return propertyName(((Method) userObject).getName());
        } else if (userObject instanceof VariableElement) {
            return ((VariableElement) userObject).getSimpleName().toString();
        } else if (userObject instanceof ExecutableElement) {
            return propertyName(((ExecutableElement) userObject).getSimpleName().toString());
        } else {
            return userObject + "";
        }
    }
    static String propertyName(String methodName) {
        if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set"))) { return decapitalize(methodName.substring(3)); }
        return decapitalize(methodName);
    }
    private static String decapitalize(String name) {
        if (name == null || name.length() == 0) { return name; }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private String printArgElementDef(PrintWriter pw,
                                      Object userObject,
                                      boolean enclosedInCommandMethod,
                                      String indent) {
        if (userObject instanceof Field) {
            Field f = (Field) userObject;
            String result = typeName(f.getGenericType()) + " " + f.getName();
            if (f.getModifiers() != 0 && !enclosedInCommandMethod) {
                pw.print(Modifier.toString(f.getModifiers()) + " " + result);
            } else {
                pw.print(result);
            }
            // default return value
        } else if (userObject instanceof MethodParam) {
            MethodParam param = (MethodParam) userObject;
            String result = typeName(param.getParameterizedType()) + " " + param.getName();
            pw.print(result);
            // default return value //return ",%n";
        } else if (userObject instanceof Method) {
            Method m = (Method) userObject;
            StringBuilder sb = new StringBuilder(128);
            sb.append(typeName(m.getGenericReturnType())).append(" ").append(m.getName());
            sb.append('(');
            Type[] params = m.getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = typeName(params[j]);
                if (m.isVarArgs() && (j == params.length - 1)) {// replace T[] with T...
                    param = param.replaceFirst("\\[\\]$", "...");
                }
                sb.append(param);
                sb.append(" ").append(parameterName(m, j));
                if (j < (params.length - 1)) {
                    sb.append(',');
                }
            }
            sb.append(')');
            Type[] exceptions = m.getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)
                            ? importer.getImportedName(((Class) exceptions[k]).getCanonicalName())
                            : importer.getImportedName(exceptions[k].toString())) ;
                    if (k < (exceptions.length - 1)) {
                        sb.append(',');
                    }
                }
            }
            String result = sb.toString();
            if (m.getModifiers() != 0 && !enclosedInCommandMethod) {
                pw.print(Modifier.toString(m.getModifiers()) + " " + result);
            } else {
                pw.print(result);
            }
            if (m.getDeclaringClass().isInterface()) {
                pw.print(";");
            } else {
                pw.println(" {");
                pw.println(indent + "    // TODO replace the stored value with the new value");
                pw.println(indent + "}");
            }
            return "";
        } else if (userObject instanceof VariableElement) {
            VariableElement f = (VariableElement) userObject;
            String result = typeName(f.asType()) + " " + f.getSimpleName();
            if (!f.getModifiers().isEmpty() && !enclosedInCommandMethod) {
                pw.print(modifierString(f.getModifiers()) + "" + result);
            } else {
                pw.print(result);
            }
            // default return value

        } else if (userObject instanceof ExecutableElement) {
            ExecutableElement m = (ExecutableElement) userObject;
            StringBuilder sb = new StringBuilder(128);
            sb.append(typeName(m.getReturnType())).append(" ").append(m.getSimpleName());
            sb.append('(');
            List<? extends VariableElement> parameters = m.getParameters();
            //List<? extends TypeParameterElement> typeParameters = m.getTypeParameters();
            for (int j = 0; j < parameters.size(); j++) {
                String param = typeName(parameters.get(j).asType());
                if (m.isVarArgs() && (j == parameters.size() - 1)) {// replace T[] with T...
                    param = param.replaceFirst("\\[\\]$", "...");
                }
                sb.append(param);
                sb.append(" ").append(parameters.get(j).getSimpleName());
                if (j < (parameters.size() - 1)) {
                    sb.append(',');
                }
            }
            sb.append(')');
            List<? extends TypeMirror> exceptions = m.getThrownTypes();
            if (!exceptions.isEmpty()) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.size(); k++) {
                    sb.append(importer.getImportedName(exceptions.get(k).toString())) ;
                    if (k < (exceptions.size() - 1)) {
                        sb.append(',');
                    }
                }
            }
            String result = sb.toString();
            if (!m.getModifiers().isEmpty() && !enclosedInCommandMethod) {
                pw.print(modifierString(m.getModifiers()) + "" + result);
            } else {
                pw.print(result);
            }
            if (m.getEnclosingElement().getKind() == ElementKind.INTERFACE) {
                pw.print(";");
            } else {
                pw.println(" {");
                pw.println(indent + "    // TODO replace the stored value with the new value");
                pw.println(indent + "}");
            }
            return "";
        } else {
            pw.print("CANNOT RENDER " + userObject);
        }
        return enclosedInCommandMethod ? "," : ";%n";
    }

    private String typeName(Type type) {
        if (type instanceof Class) {
            return importer.getImportedName(((Class<?>) type).getCanonicalName());
        }
        return importer.getImportedName(type.toString());
    }

    private String typeName(TypeMirror type) {
//        StringBuilder sb = new StringBuilder();
//        type.accept(new AbstractTypeVisitor6() {
//
//        }, sb);
        return importer.getImportedName(type.toString());
    }

    private static String parameterName(Method m, int j) {
        try {
            Object parameterArray = Method.class.getDeclaredMethod("getParameters").invoke(m);
            Object parameter = Array.get(parameterArray, j);
            return (String) Class.forName("java.lang.reflect.Parameter").getDeclaredMethod("getName").invoke(parameter);
        } catch (Exception ignored) {}
        return "arg" + j;
    }

    private static boolean isCommandMethod(CommandSpec spec) {
        Object userObject = spec.userObject();
        return userObject instanceof Method || userObject instanceof ExecutableElement;
    }

    private void printCommandElementDefOpen(PrintWriter pw, Object userObject, String indent) {
        if (userObject instanceof Method) {
            Method m = (Method) userObject;
            pw.print(indent);
            if (m.getModifiers() != 0) {
                pw.print(Modifier.toString(m.getModifiers()));
            }
            pw.print(typeName(m.getGenericReturnType()));
            pw.print(" ");
            pw.print(m.getName());
            pw.print("(");
        } else if (userObject instanceof ExecutableElement) {
            ExecutableElement m = (ExecutableElement) userObject;
            pw.print(indent);
            if (!m.getModifiers().isEmpty()) {
                pw.print(modifierString(m.getModifiers()));
            }
            pw.print(typeName(m.getReturnType()));
            pw.print(" ");
            pw.print(m.getSimpleName());
            pw.print("(");
        } else if (userObject instanceof TypeElement) {
            TypeElement type = (TypeElement) userObject;
            String modifiers = modifierString(type.getModifiers());
            String name = type.getSimpleName().toString();
            pw.printf("%s%sclass %s {", indent, modifiers, name);
        } else {
            Class<?> cls = userObject.getClass();
            String modifiers = cls.getModifiers() == 0 ? "" : (Modifier.toString(cls.getModifiers()) + " ");
            String name = importer.getImportedName(userObject.getClass().getCanonicalName());
            pw.printf("%s%sclass %s {", indent, modifiers, name);
        }
    }

    private static String modifierString(Set<javax.lang.model.element.Modifier> modifiers) {
        return modifierString(modifiers, new StringBuilder()).toString();
    }

    private static StringBuilder modifierString(Set<javax.lang.model.element.Modifier> modifiers, StringBuilder sb) {
        for (javax.lang.model.element.Modifier mod : modifiers) {
            sb.append(mod.toString());
            sb.append(" ");
        }
        return sb;
    }

    private static void printCommandElementDefClose(PrintWriter pw, Object userObject, String indent) {
        if (userObject instanceof Method || userObject instanceof ExecutableElement) {
            String full = (userObject).toString();
            pw.print(full.substring(full.indexOf(')')));
            pw.println(" {");
            pw.println(indent + "    // TODO implement commandSpec");
            pw.println(indent + "}");
        } else {
            pw.printf("%s}%n", indent);
        }
    }

    private void printParametersAnnotation(PrintWriter pw, PositionalParamSpec spec, String indent) {
        pw.printf("%s@%s", indent, importer.getImportedName(Parameters.class.getCanonicalName()));
        indent = String.format(",%n%s            ", indent);
        String sep = "(";

        sep = append(pw, sep, indent, "index = \"%s\"", spec.index().toString(), spec.index().isUnspecified());
        sep = appendStringArray(pw, sep, indent, "description = %s", spec.description(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "arity = \"%s\"", spec.arity().toString(), spec.arity().isUnspecified() ? spec.arity().toString() : "");
        sep = append(pw, sep, indent, "paramLabel = \"%s\"", spec.paramLabel(), "<" + argElementName(spec) + ">");
        sep = append(pw, sep, indent, "hideParamSyntax = %s", spec.hideParamSyntax(), false);
        sep = appendTypeInfo(pw, sep, indent, spec.typeInfo());
        sep = appendTypeConverter(pw, sep, indent, spec.converters());
        sep = append(pw, sep, indent, "split = \"%s\"", spec.splitRegex(), "");
        sep = append(pw, sep, indent, "hidden = %s", spec.hidden(), false);
        sep = append(pw, sep, indent, "defaultValue = \"%s\"", spec.defaultValue() == null ? "__no_default_value__" : spec.defaultValue(), "__no_default_value__");
        sep = append(pw, sep, indent, "showDefaultValue = %s", spec.showDefaultValue(), CommandLine.Help.Visibility.ON_DEMAND);
        sep = appendCompletionCandidates(pw, sep, indent, spec);
        sep = append(pw, sep, indent, "interactive = %s", spec.interactive(), false);
        sep = append(pw, sep, indent, "descriptionKey = \"%s\"", spec.descriptionKey(), "");

        if (!"(".equals(sep)) {
            pw.print(")");
        }
    }

    @SuppressWarnings("deprecation")
    private void printOptionAnnotation(PrintWriter pw, OptionSpec spec, String indent) {
        pw.printf("%s@%s", indent, importer.getImportedName(Option.class.getCanonicalName()));
        indent = String.format(",%n%s        ", indent);
        String sep = "(";

        sep = appendStringArray(pw, sep, indent, "names = %s", spec.names(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "required = %s", spec.required(), false);
        sep = append(pw, sep, indent, "help = %s", spec.help(), false);
        sep = append(pw, sep, indent, "usageHelp = %s", spec.usageHelp(), false);
        sep = append(pw, sep, indent, "versionHelp = %s", spec.versionHelp(), false);
        sep = appendStringArray(pw, sep, indent, "description = %s", spec.description(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "arity = \"%s\"", spec.arity().toString(), spec.arity().isUnspecified() ? spec.arity().toString() : "");
        sep = append(pw, sep, indent, "paramLabel = \"%s\"", spec.paramLabel(), "<" + argElementName(spec) + ">");
        sep = append(pw, sep, indent, "hideParamSyntax = %s", spec.hideParamSyntax(), false);
        sep = appendTypeInfo(pw, sep, indent, spec.typeInfo());
        sep = appendTypeConverter(pw, sep, indent, spec.converters());
        sep = append(pw, sep, indent, "split = \"%s\"", spec.splitRegex(), "");
        sep = append(pw, sep, indent, "hidden = %s", spec.hidden(), false);
        sep = append(pw, sep, indent, "defaultValue = %s", spec.defaultValue() == null ? "__no_default_value__" : spec.defaultValue(), "__no_default_value__");
        sep = append(pw, sep, indent, "showDefaultValue = \"%s\"", spec.showDefaultValue(), CommandLine.Help.Visibility.ON_DEMAND);
        sep = appendCompletionCandidates(pw, sep, indent, spec);
        sep = append(pw, sep, indent, "interactive = %s", spec.interactive(), false);
        sep = append(pw, sep, indent, "descriptionKey = \"%s\"", spec.descriptionKey(), "");

        if (!"(".equals(sep)) {
            pw.print(")");
        }
    }

    private void printCommandAnnotation(PrintWriter pw, CommandSpec spec, String indent) {
        pw.printf("%s@%s", indent, importer.getImportedName(Command.class.getCanonicalName()));
        indent = String.format(",%n%s         ", indent);
        String sep = "(";

        sep = append(pw, sep, indent, "name = \"%s\"", spec.name(), "<main class>");
        sep = appendStringArray(pw, sep, indent, "aliases = %s", spec.aliases(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "mixinStandardHelpOptions = %s", spec.mixinStandardHelpOptions(), false);
        sep = append(pw, sep, indent, "headerHeading = \"%s\"", spec.usageMessage().headerHeading(), "");
        sep = appendStringArray(pw, sep, indent, "header = %s", spec.usageMessage().header(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "descriptionHeading = \"%s\"", spec.usageMessage().descriptionHeading(), "");
        sep = appendStringArray(pw, sep, indent, "description = %s", spec.usageMessage().description(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "synopsisHeading = \"%s\"", spec.usageMessage().synopsisHeading(), "Usage: ");
        sep = append(pw, sep, indent, "abbreviateSynopsis = %s", spec.usageMessage().abbreviateSynopsis(), false);
        sep = appendStringArray(pw, sep, indent, "customSynopsis = %s", spec.usageMessage().customSynopsis(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "optionListHeading = \"%s\"", spec.usageMessage().optionListHeading(), "");
        sep = append(pw, sep, indent, "parameterListHeading = \"%s\"", spec.usageMessage().parameterListHeading(), "");
        sep = append(pw, sep, indent, "commandListHeading = \"%s\"", spec.usageMessage().commandListHeading(), "Commands:%n");
        sep = append(pw, sep, indent, "footerHeading = \"%s\"", spec.usageMessage().footerHeading(), "");
        sep = appendStringArray(pw, sep, indent, "footer = %s", spec.usageMessage().footer(), EMPTY_ARRAY);
        sep = append(pw, sep, indent, "requiredOptionMarker = \'%s\'", spec.usageMessage().requiredOptionMarker(), ' ');
        sep = append(pw, sep, indent, "addMethodSubcommands = %s", spec.isAddMethodSubcommands(), !isCommandMethod(spec));
        sep = appendSubcommandClasses(pw, sep, indent, spec.subcommands());
        sep = appendStringArray(pw, sep, indent, "version = %s", spec.version(), EMPTY_ARRAY);
        sep = appendClassName(pw, sep, indent, "versionProvider = %s", spec.versionProvider());
        sep = append(pw, sep, indent, "showDefaultValues = %s", spec.usageMessage().showDefaultValues(), false);
        sep = appendClassName(pw, sep, indent, "defaultValueProvider = %s", spec.defaultValueProvider());
        sep = append(pw, sep, indent, "resourceBundle = \"%s\"", spec.resourceBundleBaseName(), "null");
        sep = append(pw, sep, indent, "sortOptions = %s", spec.usageMessage().sortOptions(), true);
        sep = append(pw, sep, indent, "hidden = %s", spec.usageMessage().hidden(), false);
        sep = append(pw, sep, indent, "helpCommand = %s", spec.helpCommand(), false);
        sep = append(pw, sep, indent, "separator = \"%s\"", spec.parser().separator(), "=");
        sep = append(pw, sep, indent, "usageHelpWidth = %s", spec.usageMessage().width(), 80);

        if (!"(".equals(sep)) {
            pw.print(")");
        }
    }

    private static String append(PrintWriter pw,
                                 String prefix,
                                 String newPrefix,
                                 String template,
                                 Object value,
                                 Object defaultValue) {
        if (defaultValue.equals(value) || ("null".equals(defaultValue) && value == null)) {
            return prefix;
        }
        pw.print(prefix);
        pw.printf(template, value);
        return newPrefix;
    }

    private static String appendStringArray(PrintWriter pw,
                                            String prefix,
                                            String newPrefix,
                                            String template,
                                            String[] values,
                                            String[] defaultValues) {
        if (values == null || Arrays.equals(values, defaultValues)) {
            return prefix;
        }
        List<String> quoted = new ArrayList<String>();
        for (String value : values) {
            quoted.add('"' + value + '"');
        }
        pw.print(prefix);
        pw.printf(template, listToString(quoted));
        return newPrefix;
    }

    private String appendSubcommandClasses(PrintWriter pw,
                                           String prefix,
                                           String newPrefix,
                                           Map<String, CommandLine> subcommands) {
        List<String> subcommandClasses = new ArrayList<String>();
        for (CommandLine cmd : subcommands.values()) {
            Object obj = cmd.getCommand();
            if (!(obj instanceof Method) && !(obj instanceof ExecutableElement)) {
                if (obj instanceof Element) {
                    subcommandClasses.add(importer.getImportedName(obj.toString()) + ".class");
                } else {
                    subcommandClasses.add(importer.getImportedName(obj.getClass().getCanonicalName()) + ".class");
                }
            }
        }
        if (subcommandClasses.isEmpty()) {
            return prefix;
        }
        pw.print(prefix);
        pw.printf("subcommands = %s", listToString(subcommandClasses));
        return newPrefix;
    }

    private String appendClassName(PrintWriter pw,
                                   String prefix,
                                   String newPrefix,
                                   String template,
                                   Object object) {
        if (object == null || isDefault(object)) {
            return prefix;
        }
        pw.print(prefix);
        pw.printf(template, extractClassName(object) + ".class");
        return newPrefix;
    }

    private String appendTypeInfo(PrintWriter pw,
                                  String prefix,
                                  String newPrefix,
                                  ITypeInfo typeInfo) {
        if (typeInfo.isCollection() || typeInfo.isMap()) {
            List<ITypeInfo> aux = typeInfo.getAuxiliaryTypeInfos();
            pw.print(prefix);
            pw.printf("type = %s", listToString(extractClassNames(aux)));
            return newPrefix;
        }
        return prefix;
    }

    private List<String> extractClassNames(List<ITypeInfo> list) {
        List<String> result = new ArrayList<String>();
        for (ITypeInfo typeInfo : list) {
            result.add(importer.getImportedName(typeInfo.getClassName()) + ".class");
        }
        return result;
    }

    private String appendTypeConverter(PrintWriter pw,
                                       String prefix,
                                       String newPrefix,
                                       ITypeConverter<?>[] typeConverters) {
        if (typeConverters == null) {
            return prefix;
        }

        List<String> classNames = new ArrayList<String>();
        for (ITypeConverter<?> converter : typeConverters) {
            if (!isDefault(converter)) {
                classNames.add(extractClassName(converter) + ".class");
            }
        }
        if (classNames.isEmpty()) {
            return prefix;
        }
        pw.print(prefix);
        pw.printf("converter = %s", listToString(classNames));
        return newPrefix;
    }

    private String appendCompletionCandidates(PrintWriter pw,
                                              String prefix,
                                              String newPrefix,
                                              ArgSpec argSpec) {
        Iterable<String> completionCandidates = argSpec.completionCandidates();
        if (completionCandidates == null || isDefault(completionCandidates) || argSpec.typeInfo().isEnum()) {
            return prefix;
        }
        pw.print(prefix);
        pw.printf("completionCandidates = %s.class", extractClassName(completionCandidates));
        return newPrefix;
    }

    private static String listToString(List<String> values) {
        if (values.isEmpty()) {
            return "{}";
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        return values.toString().replace('[', '{').replace(']', '}');
    }

    private static boolean isDefault(Object typeMetaData) {
        return typeMetaData instanceof ITypeMetaData && ((ITypeMetaData) typeMetaData).isDefault();
    }

    private String extractClassName(Object object) {
        if (object instanceof ITypeMetaData) {
            ITypeMetaData metaData = (ITypeMetaData) object;
            return importer.getImportedName(metaData.getTypeMirror().toString());
        } else if (object instanceof Element) {
            TypeElement typeElement = (TypeElement) object;
            return importer.getImportedName(typeElement.getQualifiedName().toString());
        } else {
            return importer.getImportedName(object.getClass().getCanonicalName());
        }
    }
}
