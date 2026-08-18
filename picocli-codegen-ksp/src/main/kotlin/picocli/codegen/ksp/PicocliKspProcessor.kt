package picocli.codegen.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

/**
 * KSP [SymbolProcessor] that generates GraalVM native-image configuration files for picocli-annotated classes.
 *
 * Generates the same output as [picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor]
 * (the kapt/APT-based processor), but using the KSP API instead of `javax.annotation.processing`.
 *
 * @since 4.7.8
 */
class PicocliKspProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private val options = environment.options

    // Accumulated data across processing rounds, keyed by binary class name
    private val reflectedClasses = sortedMapOf<String, ReflectedClass>()
    private val resourceBundles = linkedSetOf<String>()
    private val resourcePatterns = linkedSetOf<String>()
    private val proxyInterfaceNames = mutableListOf<String>()

    // Track already-visited class names to avoid re-processing
    private val visitedClasses = mutableSetOf<String>()

    companion object {
        private const val BASE_PATH = "META-INF/native-image/picocli-generated"

        const val OPTION_PROJECT = "project"
        const val OPTION_VERBOSE = "verbose"
        const val OPTION_DISABLE_REFLECT = "disable.reflect.config"
        const val OPTION_DISABLE_RESOURCE = "disable.resource.config"
        const val OPTION_DISABLE_PROXY = "disable.proxy.config"
        const val OPTION_BUNDLES = "other.resource.bundles"
        const val OPTION_RESOURCE_REGEX = "other.resource.patterns"
        const val OPTION_INTERFACE_CLASSES = "other.proxy.interfaces"

        private const val PICOCLI_PKG = "picocli.CommandLine"
        const val COMMAND_ANN = "$PICOCLI_PKG.Command"
        const val OPTION_ANN = "$PICOCLI_PKG.Option"
        const val PARAMETERS_ANN = "$PICOCLI_PKG.Parameters"
        const val MIXIN_ANN = "$PICOCLI_PKG.Mixin"
        const val ARG_GROUP_ANN = "$PICOCLI_PKG.ArgGroup"
        const val SPEC_ANN = "$PICOCLI_PKG.Spec"
        const val UNMATCHED_ANN = "$PICOCLI_PKG.Unmatched"
        const val PARENT_COMMAND_ANN = "$PICOCLI_PKG.ParentCommand"

        // "No-op" default classes picocli uses when an attribute is not set
        private val PICOCLI_NOOPS = setOf(
            "picocli.CommandLine.NoVersionProvider",
            "picocli.CommandLine.NoDefaultProvider"
        )

        // Types that don't need to appear in reflect-config (mirrors ReflectionConfigGenerator.Visitor.excluded)
        private val EXCLUDED_TYPES = setOf(
            "boolean", "byte", "char", "double", "float", "int", "long", "short",
            "boolean[]", "byte[]", "char[]", "double[]", "float[]", "int[]", "long[]", "short[]",
            "picocli.CommandLine.Model.CommandSpec",
            "java.lang.reflect.Method",
            "java.lang.Object",
            "java.lang.String", "java.lang.String[]",
            "java.io.File", "java.io.File[]",
            "java.util.List", "java.util.Set", "java.util.Map",
            "java.lang.Class", "java.lang.Class[]",
            "java.lang.reflect.Executable", "java.lang.reflect.Parameter",
            "org.fusesource.jansi.AnsiConsole",
            "java.util.ResourceBundle",
            "java.time.Duration", "java.time.Instant", "java.time.LocalDate",
            "java.time.LocalDateTime", "java.time.LocalTime", "java.time.MonthDay",
            "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.Period",
            "java.time.Year", "java.time.YearMonth", "java.time.ZonedDateTime",
            "java.time.ZoneId", "java.time.ZoneOffset",
            "java.nio.file.Path", "java.nio.file.Paths",
            "java.sql.Connection", "java.sql.Driver", "java.sql.DriverManager",
            "java.sql.Time", "java.sql.Timestamp"
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Process @Command on classes and interfaces
        resolver.getSymbolsWithAnnotation(COMMAND_ANN).forEach { symbol ->
            when (symbol) {
                is KSClassDeclaration -> processCommandClass(symbol)
                is KSFunctionDeclaration -> processCommandMethod(symbol)
                else -> {}
            }
        }

        // Also register classes that carry picocli member annotations but no @Command
        val memberAnnotations = listOf(
            OPTION_ANN, PARAMETERS_ANN, MIXIN_ANN, ARG_GROUP_ANN,
            SPEC_ANN, UNMATCHED_ANN, PARENT_COMMAND_ANN
        )
        memberAnnotations.forEach { annName ->
            resolver.getSymbolsWithAnnotation(annName).forEach { symbol ->
                val enclosing: KSClassDeclaration? = when (symbol) {
                    is KSPropertyDeclaration -> symbol.parentDeclaration as? KSClassDeclaration
                    is KSFunctionDeclaration -> symbol.parentDeclaration as? KSClassDeclaration
                    else -> null
                }
                if (enclosing != null) processCommandClass(enclosing)
            }
        }

        return emptyList()
    }

    // -------------------------------------------------------------------------
    // Command-level processing
    // -------------------------------------------------------------------------

    private fun processCommandClass(classDecl: KSClassDeclaration) {
        val className = classDecl.toBinaryName() ?: return
        if (!visitedClasses.add(className)) return   // skip if already visited

        logVerbose("Processing @Command class: $className")

        val reflectedClass = getOrCreateReflectedClass(className)

        // @Command-annotated interfaces need a dynamic proxy
        if (classDecl.classKind == ClassKind.INTERFACE) {
            proxyInterfaceNames += className
        }

        // Extract info from the @Command annotation itself
        classDecl.findAnnotation(COMMAND_ANN)?.let { ann ->
            ann.getStringArg("resourceBundle")?.takeIf { it.isNotEmpty() }
                ?.let { resourceBundles += it }

            ann.getClassArg("versionProvider")
                ?.takeUnless { it in PICOCLI_NOOPS }
                ?.let { registerReflectedClass(it) }

            ann.getClassArg("defaultValueProvider")
                ?.takeUnless { it in PICOCLI_NOOPS }
                ?.let { registerReflectedClass(it) }

            ann.getClassListArg("subcommands")
                ?.forEach { registerReflectedClass(it) }
        }

        // Walk the full type hierarchy (getAllProperties / getAllFunctions handle superclasses too)
        classDecl.getAllProperties().forEach { prop ->
            processAnnotatedProperty(prop, reflectedClass)
        }
        classDecl.getAllFunctions().forEach { func ->
            processAnnotatedFunction(func, classDecl, reflectedClass)
        }
    }

    private fun processCommandMethod(func: KSFunctionDeclaration) {
        // A method annotated with @Command is a subcommand factory method.
        // We need to register the enclosing class and the method.
        val enclosing = func.parentDeclaration as? KSClassDeclaration ?: return
        val className = enclosing.toBinaryName() ?: return
        logVerbose("Processing @Command method: ${func.simpleName.asString()} in $className")

        val reflectedClass = getOrCreateReflectedClass(className)
        reflectedClass.addMethod(func.simpleName.asString(), func.parameterTypeNames())

        // Parameters of the method may be annotated with @Option/@Parameters
        func.parameters.forEach { param ->
            processAnnotatedParameter(param, reflectedClass)
        }
    }

    // -------------------------------------------------------------------------
    // Member-level processing
    // -------------------------------------------------------------------------

    private fun processAnnotatedProperty(prop: KSPropertyDeclaration, enclosingClass: ReflectedClass) {
        val hasPicocli = listOf(
            OPTION_ANN, PARAMETERS_ANN, MIXIN_ANN, ARG_GROUP_ANN,
            SPEC_ANN, UNMATCHED_ANN, PARENT_COMMAND_ANN
        ).any { prop.hasAnnotation(it) }
        if (!hasPicocli) return

        val fieldName = prop.simpleName.asString()
        val isFinal = !prop.isMutable
        enclosingClass.addField(fieldName, isFinal)

        // Register the field's own type
        prop.type.resolve().registerType()

        // Extract class-valued attributes from @Option / @Parameters
        prop.findAnnotation(OPTION_ANN)?.extractConverterAttributes()
        prop.findAnnotation(PARAMETERS_ANN)?.extractConverterAttributes()
    }

    private fun processAnnotatedFunction(
        func: KSFunctionDeclaration,
        enclosingClass: KSClassDeclaration,
        reflectedClass: ReflectedClass
    ) {
        val hasPicocli = listOf(
            OPTION_ANN, PARAMETERS_ANN, MIXIN_ANN, ARG_GROUP_ANN,
            SPEC_ANN, UNMATCHED_ANN, PARENT_COMMAND_ANN
        ).any { func.hasAnnotation(it) }
        if (!hasPicocli) return

        val methodName = func.simpleName.asString()
        reflectedClass.addMethod(methodName, func.parameterTypeNames())

        // Register return type
        func.returnType?.resolve()?.registerType()

        // Converter attributes
        func.findAnnotation(OPTION_ANN)?.extractConverterAttributes()
        func.findAnnotation(PARAMETERS_ANN)?.extractConverterAttributes()
    }

    private fun processAnnotatedParameter(param: KSValueParameter, enclosingClass: ReflectedClass) {
        val hasPicocli = listOf(OPTION_ANN, PARAMETERS_ANN, MIXIN_ANN, ARG_GROUP_ANN)
            .any { param.hasAnnotation(it) }
        if (!hasPicocli) return

        param.type.resolve().registerType()
        param.findAnnotation(OPTION_ANN)?.extractConverterAttributes()
        param.findAnnotation(PARAMETERS_ANN)?.extractConverterAttributes()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Registers type converter, completion candidates, and parameter consumer classes. */
    private fun KSAnnotation.extractConverterAttributes() {
        getClassListArg("converter")?.forEach { registerReflectedClass(it) }
        getClassArg("completionCandidates")?.let { registerReflectedClass(it) }
        getClassArg("parameterConsumer")?.let { registerReflectedClass(it) }
    }

    /** Registers the type name in the reflect-config, including generic type arguments. */
    private fun KSType.registerType() {
        val name = toBinaryName() ?: return
        registerReflectedClass(name)
        // Also register generic type arguments (e.g. List<MyType> → MyType)
        arguments.forEach { it.type?.resolve()?.registerType() }
    }

    private fun registerReflectedClass(name: String) {
        if (!isExcluded(name)) getOrCreateReflectedClass(name)
    }

    private fun getOrCreateReflectedClass(name: String): ReflectedClass =
        reflectedClasses.getOrPut(name) { ReflectedClass(name) }

    private fun isExcluded(name: String): Boolean =
        name in EXCLUDED_TYPES || name.startsWith("[")

    // -------------------------------------------------------------------------
    // KSP annotation helpers
    // -------------------------------------------------------------------------

    private fun KSAnnotated.findAnnotation(qualifiedName: String): KSAnnotation? =
        annotations.find { ann ->
            ann.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
        }

    private fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean =
        findAnnotation(qualifiedName) != null

    private fun KSAnnotation.getStringArg(name: String): String? =
        arguments.find { it.name?.asString() == name }?.value as? String

    private fun KSAnnotation.getClassArg(name: String): String? {
        val value = arguments.find { it.name?.asString() == name }?.value as? KSType
        return value?.toBinaryName()
    }

    @Suppress("UNCHECKED_CAST")
    private fun KSAnnotation.getClassListArg(name: String): List<String>? {
        val list = arguments.find { it.name?.asString() == name }?.value as? List<*>
            ?: return null
        return list.mapNotNull { (it as? KSType)?.toBinaryName() }
    }

    /** Converts a [KSType] to its JVM binary class name (uses `$` for nested classes). */
    private fun KSType.toBinaryName(): String? = declaration.toBinaryName()

    /** Converts a [KSDeclaration] to its JVM binary class name. */
    private fun KSDeclaration.toBinaryName(): String? {
        val fqn = qualifiedName?.asString() ?: return null
        val parent = parentDeclaration
        return if (parent is KSClassDeclaration) {
            val parentName = parent.toBinaryName() ?: return null
            "$parentName\$${simpleName.asString()}"
        } else {
            fqn
        }
    }

    /** Returns the JVM binary names of a function's parameter types. */
    private fun KSFunctionDeclaration.parameterTypeNames(): List<String> =
        parameters.mapNotNull { it.type.resolve().toBinaryName() }

    // -------------------------------------------------------------------------
    // finish() – write the three config files
    // -------------------------------------------------------------------------

    override fun finish() {
        // Merge user-specified extra entries from KSP options
        options[OPTION_BUNDLES]?.splitTrimmed()?.forEach { resourceBundles += it }
        options[OPTION_RESOURCE_REGEX]?.splitTrimmed()?.forEach { resourcePatterns += it }
        options[OPTION_INTERFACE_CLASSES]?.splitTrimmed()?.forEach { proxyInterfaceNames += it }

        val basePath = buildBasePath()
        logVerbose("Writing native-image configs to: $basePath")

        if (!options.containsKey(OPTION_DISABLE_REFLECT)) {
            writeFile(basePath, "reflect-config.json", buildReflectConfig())
        }
        if (!options.containsKey(OPTION_DISABLE_RESOURCE)) {
            writeFile(basePath, "resource-config.json", buildResourceConfig())
        }
        if (!options.containsKey(OPTION_DISABLE_PROXY)) {
            writeFile(basePath, "proxy-config.json", buildProxyConfig())
        }
    }

    private fun buildBasePath(): String {
        val project = options[OPTION_PROJECT]?.replace('\\', '/')
        return if (project != null) "$BASE_PATH/$project/" else "$BASE_PATH/"
    }

    private fun buildReflectConfig(): String {
        val sb = StringBuilder("[\n")
        val entries = reflectedClasses.values.filter { !isExcluded(it.name) }
        entries.forEachIndexed { i, cls ->
            if (i > 0) sb.append(",\n")
            sb.append(cls.toJson())
        }
        sb.append("\n]\n")
        return sb.toString()
    }

    private fun buildResourceConfig(): String = buildString {
        append("{\n")
        append("  \"bundles\" : [")
        resourceBundles.forEachIndexed { i, bundle ->
            if (i > 0) append(",")
            append("\n    {\"name\" : \"$bundle\"}")
        }
        append("\n  ],\n")
        append("  \"resources\" : [")
        resourcePatterns.forEachIndexed { i, pattern ->
            if (i > 0) append(",")
            append("\n    {\"pattern\" : \"$pattern\"}")
        }
        append("\n  ]\n")
        append("}\n")
    }

    private fun buildProxyConfig(): String = buildString {
        append("[\n")
        proxyInterfaceNames.forEachIndexed { i, iface ->
            if (i > 0) append(",\n")
            append("  [\"$iface\"]")
        }
        append("\n]\n")
    }

    private fun writeFile(basePath: String, fileName: String, content: String) {
        try {
            val path = "$basePath$fileName"
            logVerbose("Writing: $path")
            // Strip extension from fileName for KSP API (it appends it back)
            val dotIdx = fileName.lastIndexOf('.')
            val fileNameNoExt = if (dotIdx >= 0) fileName.substring(0, dotIdx) else fileName
            val ext = if (dotIdx >= 0) fileName.substring(dotIdx + 1) else ""
            val stream = codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                packageName = "",
                fileName = "$basePath$fileNameNoExt",
                extensionName = ext
            )
            stream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
        } catch (e: FileAlreadyExistsException) {
            // KSP may call finish() across incremental rounds; ignore duplicate writes
            logVerbose("File already exists, skipping: $basePath$fileName")
        } catch (e: Exception) {
            logger.error("picocli-ksp: failed to write $basePath$fileName: ${e.message}")
        }
    }

    private fun logVerbose(msg: String) {
        if (options.containsKey(OPTION_VERBOSE)) logger.info("[picocli-ksp] $msg")
    }

    private fun String.splitTrimmed(): List<String> =
        split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
