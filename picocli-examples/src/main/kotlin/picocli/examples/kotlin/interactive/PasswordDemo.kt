package picocli.examples.kotlin.interactive

import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

class PasswordDemo : Runnable {
    @Option(names = ["--password:file"])
    var passwordFile: File? = null
    @Option(names = ["--password:env"])
    var passwordEnvironmentVariable: String? = null
    @Option(names = ["--password"], interactive = true)
    var password: String? = null
    @Spec
    var spec: CommandSpec? = null

    override fun run() {
        when {
            password != null -> {
                login(password!!)
            }
            passwordEnvironmentVariable != null -> {
                login(System.getenv(passwordEnvironmentVariable))
            }
            passwordFile != null -> {
                login(String(Files.readAllBytes(passwordFile!!.toPath())))
            }
            else -> {
                throw ParameterException(spec!!.commandLine(), "Password required")
            }
        }
    }

    private fun login(pwd: String) {
        println("Password: %s%n".format(pwd))
    }
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine(PasswordDemo()).execute(*args)
        }
    }
}
// NOTE: below is an alternative to defining a @JvmStatic main function in a companion object:
// fun main(): Unit = exitProcess(CommandLine(PasswordDemo()).execute("--password"))
