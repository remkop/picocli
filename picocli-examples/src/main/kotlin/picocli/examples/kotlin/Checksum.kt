package picocli.examples.kotlin

import picocli.CommandLine
import picocli.CommandLine.HelpCommand
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.util.ArrayList
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "checksum", mixinStandardHelpOptions = true,
        subcommands = [ HelpCommand::class ], version = ["checksum 4.1.4"],
        description = ["Prints the checksum (SHA-1 by default) of file(s) to STDOUT."])
class Checksum : Callable<Int> {

    @Parameters(index = "0..*", description = ["The file(s) whose checksum to calculate."],
                arity = "1..*", paramLabel = "<file 1> <file 2>")
    val files: ArrayList<File> = arrayListOf()

    @Option(names = ["-a", "--algorithm"], description = ["MD5, SHA-1, SHA-256, ..."])
    var algorithm = "SHA-1"

    override fun call(): Int {
        for (file in files)
        {
            val fileContents = Files.readAllBytes(file.toPath())
            val digest = MessageDigest.getInstance(algorithm).digest(fileContents)
            println(("%s: %0" + digest.size * 2 + "x").format(file.name, BigInteger(1, digest)))
        }

        return 0
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine(Checksum()).execute(*args)
        }
    }
}
// NOTE: below is an alternative to defining a @JvmStatic main function in a companion object:
// fun main(args: Array<String>) : Unit = exitProcess(CommandLine(Checksum()).execute(*args))
