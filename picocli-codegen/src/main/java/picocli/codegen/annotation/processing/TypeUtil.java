package picocli.codegen.annotation.processing;

import picocli.CommandLine;

import javax.lang.model.type.DeclaredType;

final class TypeUtil {
    private TypeUtil() {}

    static boolean isOption(DeclaredType annotationType) {
        return CommandLine.Option.class.getName().equals(annotationType.toString());
    }

    static boolean isParameter(DeclaredType annotationType) {
        return CommandLine.Parameters.class.getName().equals(annotationType.toString());
    }
}
