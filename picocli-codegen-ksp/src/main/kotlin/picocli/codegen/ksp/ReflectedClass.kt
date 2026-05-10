package picocli.codegen.ksp

/**
 * Represents a class entry in the GraalVM `reflect-config.json` file.
 * Mirrors [picocli.codegen.aot.graalvm.ReflectionConfigGenerator.ReflectedClass].
 */
internal class ReflectedClass(val name: String) {

    private val fields = sortedSetOf<ReflectedField>(compareBy { it.name })
    private val methods = sortedSetOf<ReflectedMethod>(compareBy({ it.name }, { it.paramTypes.joinToString() }))

    fun addField(fieldName: String, isFinal: Boolean): ReflectedClass {
        fields += ReflectedField(fieldName, isFinal)
        return this
    }

    fun addMethod(methodName: String, paramTypes: List<String>): ReflectedClass {
        methods += ReflectedMethod(methodName, paramTypes)
        return this
    }

    /** Serialises this entry to the JSON fragment expected by GraalVM. */
    fun toJson(): String = buildString {
        append("  {\n")
        append("    \"name\" : \"$name\",\n")
        append("    \"allDeclaredConstructors\" : true,\n")
        append("    \"allPublicConstructors\" : true,\n")
        append("    \"allDeclaredMethods\" : true,\n")
        append("    \"allPublicMethods\" : true")
        if (fields.isNotEmpty()) {
            append(",\n    \"fields\" : [\n")
            fields.forEachIndexed { i, f ->
                if (i > 0) append(",\n")
                append("      ").append(f.toJson())
            }
            append("\n    ]")
        }
        if (methods.isNotEmpty()) {
            append(",\n    \"methods\" : [\n")
            methods.forEachIndexed { i, m ->
                if (i > 0) append(",\n")
                append("      ").append(m.toJson())
            }
            append("\n    ]")
        }
        append("\n  }")
    }
}

internal data class ReflectedField(val name: String, val isFinal: Boolean) {
    fun toJson(): String {
        val allowWrite = if (isFinal) ", \"allowWrite\" : true" else ""
        return "{\"name\" : \"$name\"$allowWrite}"
    }
}

internal data class ReflectedMethod(val name: String, val paramTypes: List<String>) {
    fun toJson(): String {
        val params = paramTypes.joinToString(", ") { "\"$it\"" }
        return "{\"name\" : \"$name\", \"parameterTypes\" : [$params]}"
    }
}
