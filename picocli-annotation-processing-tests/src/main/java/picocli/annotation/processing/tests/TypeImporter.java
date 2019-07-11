package picocli.annotation.processing.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class TypeImporter {
    private static Set<String> javaDefaultTypes;

    private Set<String> registeredPackages = new HashSet<String>();
    private SortedSet<TypeName> imports = new TreeSet<TypeName>();

    /**
     * Creates an type importer for a compilation unit in the given package.
     * @param outputPackage the package of the compilation unit
     */
    public TypeImporter(String outputPackage) {
        registeredPackages.add(makeValid(outputPackage));
    }

    /*
     * Removes all code points that are not valid Java identifier parts from the given string if there is any.
     * The string is returned unchanged otherwise.
     */
    private static String makeValid(String s) {
        StringBuilder result = new StringBuilder(s.length());
        for (int ch = 0, i = 0; i < s.length(); i += Character.charCount(ch)) {
            ch = s.codePointAt(i);
            if (Character.isJavaIdentifierPart(ch) || ch > ' ') {
                result.appendCodePoint(ch);
            }
        }
        return result.toString();
    }

    public String getImportedName(String fullyQualifiedClass) {
        int i = fullyQualifiedClass.indexOf('<');
        if (i == -1) {
            return register(fullyQualifiedClass).getSimpleName();
        }

        StringBuilder result = new StringBuilder();
        for (int start = 0, end = fullyQualifiedClass.length(); i < end; i++) {
            char c = fullyQualifiedClass.charAt(i);
            switch (c) {
                case ',':
                case '<':
                case '>':
                case '&':
                case '[':
                case ']': {
                    if (start != i) {
                        String potentialClassName = fullyQualifiedClass.substring(start, i);
                        if (!isTypeVariable(potentialClassName)) {
                            Generic clean = new Generic(potentialClassName);
                            result.append(clean.specialization + register(clean.typeName).getSimpleName());
                        } else {
                            result.append(potentialClassName);
                        }
                    }
                    result.append(c);
                    start = i + 1;
                    break;
                }
                case '?': {
                    int j = i + 1;
                    while (j < end && Character.isWhitespace(fullyQualifiedClass.charAt(j))) {
                        j++;
                    }
                    if (j + 6 < end && "extends".equals(fullyQualifiedClass.substring(j, j + 7))) {
                        result.append(fullyQualifiedClass.substring(i, j + 7));
                        i = j + 6;
                    } else if (j + 4 < end && "super".equals(fullyQualifiedClass.substring(j, j + 5))) {
                        result.append(fullyQualifiedClass.substring(i, j + 5));
                        i = j + 4;
                    } else {
                        result.append(c);
                    }
                    start = i + 1;
                    break;
                }
                default: {
                    if (Character.isWhitespace(c) && start == i) {
                        result.append(c);
                        start++;
                    }
                    break;
                }
            }
        }
        return result.toString();
    }

    private boolean isTypeVariable(String potentialClassName) {
        return potentialClassName.length() == 1;
    }

    private TypeName register(String typeName) {
        return register(new TypeName(typeName));
    }
    private TypeName register(TypeName typeName) {
        String pkg = typeName.getPackageName();

        if (typeName.getSimpleName().equals("*")) {
            registeredPackages.add(pkg);
            imports.add(typeName);
        } else if (shouldImport(typeName)) {
            if ("".equals(pkg) && typeName.findMatchingSimpleName(imports) != null) {
                return typeName; // already registered
            }
            if (!registeredPackages.contains(pkg)) {
                imports.add(typeName);
            }
        }

        return typeName;
    }

    /**
     * Determines whether the given non-wildcard import should be added.
     * By default, this returns false if the simple name is a built-in Java language type name.
     */
    private boolean shouldImport(TypeName typeName) {
        // don't import classes from the java.lang package
        String pkg = typeName.getPackageName();
        String simpleName = typeName.getSimpleName();
        boolean exclude = (pkg.equals("java.lang") || pkg.equals("")) && getJavaDefaultTypes().contains(simpleName);
        return !exclude;
    }

    /**
     * Returns the set of simple names of the primitive types and types in the java.lang package
     * (classes that don't need to be imported).
     */
    static Set<String> getJavaDefaultTypes() {
        if (javaDefaultTypes == null) {
            javaDefaultTypes = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
                    "AbstractMethodError",
                    "Appendable",
                    "ArithmeticException",
                    "ArrayIndexOutOfBoundsException",
                    "ArrayStoreException",
                    "AssertionError",
                    "AutoCloseable",
                    "Boolean",
                    "BootstrapMethodError",
                    "Byte",
                    "Character",
                    "CharSequence",
                    "Class",
                    "ClassCastException",
                    "ClassCircularityError",
                    "ClassFormatError",
                    "ClassLoader",
                    "ClassNotFoundException",
                    "ClassValue",
                    "Cloneable",
                    "CloneNotSupportedException",
                    "Comparable",
                    "Compiler",
                    "Deprecated",
                    "Double",
                    "Enum",
                    "EnumConstantNotPresentException",
                    "Error",
                    "Exception",
                    "ExceptionInInitializerError",
                    "Float",
                    "FunctionalInterface",
                    "IllegalAccessError",
                    "IllegalAccessException",
                    "IllegalArgumentException",
                    "IllegalCallerException",
                    "IllegalMonitorStateException",
                    "IllegalStateException",
                    "IllegalThreadStateException",
                    "IncompatibleClassChangeError",
                    "IndexOutOfBoundsException",
                    "InheritableThreadLocal",
                    "InstantiationError",
                    "InstantiationException",
                    "Integer",
                    "InternalError",
                    "InterruptedException",
                    "Iterable",
                    "LayerInstantiationException",
                    "LinkageError",
                    "Long",
                    "Math",
                    "Module",
                    "ModuleLayer",
                    "NegativeArraySizeException",
                    "NoClassDefFoundError",
                    "NoSuchFieldError",
                    "NoSuchFieldException",
                    "NoSuchMethodError",
                    "NoSuchMethodException",
                    "NullPointerException",
                    "Number",
                    "NumberFormatException",
                    "Object",
                    "OutOfMemoryError",
                    "Override",
                    "Package",
                    "Process",
                    "ProcessBuilder",
                    "ProcessHandle",
                    "Readable",
                    "ReflectiveOperationException",
                    "Runnable",
                    "Runtime",
                    "RuntimeException",
                    "RuntimePermission",
                    "SafeVarargs",
                    "SecurityException",
                    "SecurityManager",
                    "Short",
                    "StackOverflowError",
                    "StackTraceElement",
                    "StackWalker",
                    "StrictMath",
                    "String",
                    "StringBuffer",
                    "StringBuilder",
                    "StringIndexOutOfBoundsException",
                    "SuppressWarnings",
                    "System",
                    "Thread",
                    "ThreadDeath",
                    "ThreadGroup",
                    "ThreadLocal",
                    "Throwable",
                    "TypeNotPresentException",
                    "UnknownError",
                    "UnsatisfiedLinkError",
                    "UnsupportedClassVersionError",
                    "UnsupportedOperationException",
                    "VerifyError",
                    "VirtualMachineError",
                    "Void",
                    "boolean",
                    "byte",
                    "char",
                    "double",
                    "float",
                    "int",
                    "long",
                    "short",
                    "void")));
        }
        return javaDefaultTypes;
    }

    /**
     * Returns a string with the import declarations to add to the class, using the system line separator.
     */
    public String createImportDeclaration() {
        return createImportDeclaration(System.getProperty("line.separator"));
    }
    /**
     * Returns a string with the import declarations to add to the class,
     * using the specified line separator to separate import lines.
     */
    public String createImportDeclaration(String lineDelimiter) {
        StringBuilder result = new StringBuilder();
        for (TypeName importName : getImports()) {
            result.append(lineDelimiter + "import " + importName + ";");
        }
        return result.toString();
    }

    private SortedSet<TypeName> getImports() {
        compactImports();
        return imports;
    }

    /*
     * Removes explicit imports covered by wildcards.
     */
    private void compactImports() {
        Iterator<TypeName> i = imports.iterator();
        while (i.hasNext()) {
            TypeName importName = i.next();
            if (!importName.getSimpleName().equals("*") && registeredPackages.contains(importName.getPackageName())) {
                i.remove();
            }
        }
    }

    static class TypeName implements Comparable<TypeName> {
        private final String qualifiedName;
        private final String packageName;
        private final String simpleName;
        private final String importName;

        TypeName(String qualifiedName) {
            this.qualifiedName = qualifiedName;
            String canonical = qualifiedName.replaceAll("\\$", ".");
            int dot = canonical.lastIndexOf('.');
            packageName = dot == -1 ? "" : makeValid(canonical.substring(0, dot));

            int from = dot + 1;
            int end = canonical.indexOf('[', from);
            if (end == -1) {
                end = canonical.length();
            }
            simpleName = makeValid(canonical.substring(from, end));
            importName = dot == -1 ? simpleName : packageName + "." + simpleName;
        }

        /*
         * Returns the normalized package name of this type name.
         */
        private String getPackageName() {
            return packageName;
        }

        /*
         * Returns the last segment (the part following the last '.') of this type name.
         * For member classes, this method returns the enclosing top-level class, <em>unless</em>
         * the member class was registered by its canonical name, in which case the simple name of the member class is returned.
         */
        private String getSimpleName() {
            return simpleName;
        }

        public String getImportName() {
            return importName;
        }

        @Override
        public int compareTo(TypeName o) {
            return this.qualifiedName.compareTo(o.qualifiedName);
        }

        @Override
        public String toString() {
            return qualifiedName;
        }

        public TypeName findMatchingSimpleName(Collection<TypeName> imports) {
            for (TypeName typeName: imports) {
                if (typeName.getSimpleName().equals(getSimpleName())) {
                    return typeName;
                }
            }
            return null;
        }
    }

    static class Generic {
        String specialization;
        String typeName;

        Generic(String name) {
            specialization = "";
            typeName = name;
            int i = name.indexOf("extends");
            if (i > 0 && Character.isWhitespace(name.charAt(i - 1)) && Character.isWhitespace(name.charAt(i + + "extends".length()))) {
                typeName = name.substring(i + "extends".length()).trim();
                specialization = name.substring(0, name.indexOf(typeName));
            }
            i = name.indexOf("super");
            if (i > 0 && Character.isWhitespace(name.charAt(i - 1)) && Character.isWhitespace(name.charAt(i + + "super".length()))) {
                typeName = name.substring(i + "super".length()).trim();
                specialization = name.substring(0, name.indexOf(typeName));
            }
        }
    }
}
