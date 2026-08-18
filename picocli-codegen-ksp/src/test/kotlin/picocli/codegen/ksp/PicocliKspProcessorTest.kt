package picocli.codegen.ksp

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Integration tests for [PicocliKspProcessor].
 *
 * Each test compiles a small Kotlin snippet with the KSP processor active and then
 * asserts on the content of the generated GraalVM native-image configuration files.
 */
class PicocliKspProcessorTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun compile(
        vararg sources: SourceFile,
        options: Map<String, String> = emptyMap()
    ): Pair<JvmCompilationResult, File> {
        val compilation = KotlinCompilation().apply {
            this.sources = sources.toList()
            symbolProcessorProviders = mutableListOf(PicocliKspProcessorProvider())
            kspProcessorOptions = options.toMutableMap()
            inheritClassPath = true
            messageOutputStream = System.out
        }
        return compilation.compile() to compilation.workingDir
    }

    /** Finds the first file matching the given name anywhere under the given root. */
    private fun File.findFile(name: String): File? =
        walkTopDown().firstOrNull { it.name == name }

    // -------------------------------------------------------------------------
    // Simple @Command class
    // -------------------------------------------------------------------------

    private val simpleCommandSource = SourceFile.kotlin(
        "SimpleCommand.kt", """
        import picocli.CommandLine.Command
        import picocli.CommandLine.Option
        import picocli.CommandLine.Parameters

        @Command(name = "simple", description = ["A simple command"])
        class SimpleCommand : Runnable {
            @Option(names = ["-v", "--verbose"])
            var verbose: Boolean = false

            @Parameters(index = "0")
            var file: String = ""

            override fun run() {}
        }
    """.trimIndent()
    )

    @Test
    fun `compilation succeeds for simple Command class`() {
        val (result, _) = compile(simpleCommandSource)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
    }

    @Test
    fun `generates all three config files by default`() {
        val (result, workingDir) = compile(simpleCommandSource)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        assertNotNull(workingDir.findFile("reflect-config.json"),  "reflect-config.json should be generated")
        assertNotNull(workingDir.findFile("resource-config.json"), "resource-config.json should be generated")
        assertNotNull(workingDir.findFile("proxy-config.json"),    "proxy-config.json should be generated")
    }

    @Test
    fun `reflect-config contains the Command class`() {
        val (result, workingDir) = compile(simpleCommandSource)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("reflect-config.json")?.readText()
            ?: fail("reflect-config.json not found")
        assertTrue(content.contains("SimpleCommand"),            "Should contain the command class name")
        assertTrue(content.contains("allDeclaredConstructors"), "Should have allDeclaredConstructors")
        assertTrue(content.contains("allPublicMethods"),        "Should have allPublicMethods")
    }

    @Test
    fun `reflect-config contains annotated fields`() {
        val (result, workingDir) = compile(simpleCommandSource)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("reflect-config.json")?.readText()
            ?: fail("reflect-config.json not found")
        assertTrue(content.contains("\"verbose\""), "Should contain the @Option field")
        assertTrue(content.contains("\"file\""),    "Should contain the @Parameters field")
    }

    @Test
    fun `resource-config is valid JSON object`() {
        val (result, workingDir) = compile(simpleCommandSource)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("resource-config.json")?.readText()
            ?: fail("resource-config.json not found")
        assertTrue(content.trimStart().startsWith("{"), "resource-config should be a JSON object")
        assertTrue(content.contains("\"bundles\""),    "resource-config should have a 'bundles' key")
        assertTrue(content.contains("\"resources\""),  "resource-config should have a 'resources' key")
    }

    @Test
    fun `proxy-config is valid JSON array`() {
        val (result, workingDir) = compile(simpleCommandSource)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("proxy-config.json")?.readText()
            ?: fail("proxy-config.json not found")
        assertTrue(content.trimStart().startsWith("["), "proxy-config should be a JSON array")
    }

    // -------------------------------------------------------------------------
    // Disable flags
    // -------------------------------------------------------------------------

    @Test
    fun `disable reflect-config via option`() {
        val (result, workingDir) = compile(
            simpleCommandSource,
            options = mapOf(PicocliKspProcessor.OPTION_DISABLE_REFLECT to "")
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertNull(workingDir.findFile("reflect-config.json"),  "reflect-config.json should NOT be generated")
        assertNotNull(workingDir.findFile("resource-config.json"))
        assertNotNull(workingDir.findFile("proxy-config.json"))
    }

    @Test
    fun `disable resource-config via option`() {
        val (result, workingDir) = compile(
            simpleCommandSource,
            options = mapOf(PicocliKspProcessor.OPTION_DISABLE_RESOURCE to "")
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertNotNull(workingDir.findFile("reflect-config.json"))
        assertNull(workingDir.findFile("resource-config.json"),  "resource-config.json should NOT be generated")
        assertNotNull(workingDir.findFile("proxy-config.json"))
    }

    @Test
    fun `disable proxy-config via option`() {
        val (result, workingDir) = compile(
            simpleCommandSource,
            options = mapOf(PicocliKspProcessor.OPTION_DISABLE_PROXY to "")
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        assertNotNull(workingDir.findFile("reflect-config.json"))
        assertNotNull(workingDir.findFile("resource-config.json"))
        assertNull(workingDir.findFile("proxy-config.json"),  "proxy-config.json should NOT be generated")
    }

    // -------------------------------------------------------------------------
    // Resource bundle
    // -------------------------------------------------------------------------

    @Test
    fun `resource bundle from @Command is in resource-config`() {
        val source = SourceFile.kotlin("BundleCommand.kt", """
            import picocli.CommandLine.Command

            @Command(name = "bundled", resourceBundle = "com.example.Messages")
            class BundleCommand
        """.trimIndent())

        val (result, workingDir) = compile(source)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("resource-config.json")?.readText()
            ?: fail("resource-config.json not found")
        assertTrue(content.contains("com.example.Messages"),
            "Resource bundle should appear in resource-config")
    }

    // -------------------------------------------------------------------------
    // Interface command → proxy-config
    // -------------------------------------------------------------------------

    @Test
    fun `interface @Command is added to proxy-config`() {
        val source = SourceFile.kotlin("InterfaceCommand.kt", """
            import picocli.CommandLine.Command
            import picocli.CommandLine.Option

            @Command(name = "iface")
            interface InterfaceCommand {
                @Option(names = ["-v"])
                fun verbose(v: Boolean)
            }
        """.trimIndent())

        val (result, workingDir) = compile(source)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("proxy-config.json")?.readText()
            ?: fail("proxy-config.json not found")
        assertTrue(content.contains("InterfaceCommand"),
            "Interface command should appear in proxy-config")
    }

    // -------------------------------------------------------------------------
    // extra resource bundles option
    // -------------------------------------------------------------------------

    @Test
    fun `extra bundles from option appear in resource-config`() {
        val (result, workingDir) = compile(
            simpleCommandSource,
            options = mapOf(PicocliKspProcessor.OPTION_BUNDLES to "com.example.Extra,com.example.More")
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("resource-config.json")?.readText()
            ?: fail("resource-config.json not found")
        assertTrue(content.contains("com.example.Extra"), "Extra bundle should be present")
        assertTrue(content.contains("com.example.More"),  "Second extra bundle should be present")
    }

    // -------------------------------------------------------------------------
    // @Mixin
    // -------------------------------------------------------------------------

    @Test
    fun `Mixin field appears in reflect-config`() {
        val source = SourceFile.kotlin("MixinCommand.kt", """
            import picocli.CommandLine.Command
            import picocli.CommandLine.Mixin
            import picocli.CommandLine.Option

            class VerboseMixin {
                @Option(names = ["-v"])
                var verbose: Boolean = false
            }

            @Command(name = "mixin-cmd")
            class MixinCommand {
                @Mixin
                var mixin: VerboseMixin = VerboseMixin()
            }
        """.trimIndent())

        val (result, workingDir) = compile(source)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("reflect-config.json")?.readText()
            ?: fail("reflect-config.json not found")
        assertTrue(content.contains("MixinCommand"), "MixinCommand should be in reflect-config")
        assertTrue(content.contains("\"mixin\""),    "Mixin field should be listed")
    }

    // -------------------------------------------------------------------------
    // project option
    // -------------------------------------------------------------------------

    @Test
    fun `project option places files in the correct subdirectory`() {
        val (result, workingDir) = compile(
            simpleCommandSource,
            options = mapOf(PicocliKspProcessor.OPTION_PROJECT to "my/project")
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val reflectFile = workingDir.findFile("reflect-config.json")
        assertNotNull(reflectFile, "reflect-config.json should be generated")
        assertTrue(
            reflectFile!!.path.replace('\\', '/').contains("my/project"),
            "File should be under the project subdirectory, but was at: ${reflectFile.path}"
        )
    }

    // -------------------------------------------------------------------------
    // Class with @Option but no @Command
    // -------------------------------------------------------------------------

    @Test
    fun `class with Option field but no Command annotation is still reflected`() {
        val source = SourceFile.kotlin("OptionOnly.kt", """
            import picocli.CommandLine.Option

            class OptionOnly {
                @Option(names = ["-x"])
                var x: Int = 0
            }
        """.trimIndent())

        val (result, workingDir) = compile(source)
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val content = workingDir.findFile("reflect-config.json")?.readText()
            ?: fail("reflect-config.json not found")
        assertTrue(content.contains("OptionOnly"),
            "Class with @Option fields should be in reflect-config even without @Command")
    }
}
