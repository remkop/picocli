package picocli.examples.kotlin.i18n.localecontrol

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Unmatched

import java.io.File
import java.lang.Exception
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess

class InitLocale {
    @Option(names = ["-l", "--locale"], description = ["locale used for message texts (phase 1)"])
    fun setLocale(locale: String?) {
        Locale.setDefault(Locale(locale))
    }

    // ignore any other parameters and options in the first parsing phase
    @Unmatched
    var remainder : List<String>? = null
}

@Command(name = "checksum", mixinStandardHelpOptions = true, version = ["checksum 4.0"],
    resourceBundle = "picocli.examples.i18n.localecontrol.bundle", sortOptions = false)
class LocaleControl : Callable<Int> {
    private var bundle: ResourceBundle = ResourceBundle.getBundle("picocli.examples.kotlin.i18n.localecontrol.bundle")

    @Option(names = ["-l", "--locale"], descriptionKey = "Locale", paramLabel = "<locale>", order = 1)
    lateinit var ignored: String

    @Parameters(index = "0", descriptionKey = "File")
    lateinit var file: File

    @Option(names = ["-a", "--algorithm"], descriptionKey = "Algorithms", order = 2)
    var algorithm = "SHA-1"

    @Throws(Exception::class)
    override fun call(): Int {
        val fileContents = Files.readAllBytes(file.toPath())
        val digest = MessageDigest.getInstance(algorithm).digest(fileContents)
        println("%s: %s".format(bundle.getString("Label_File"), file))
        println("%s: %s".format(bundle.getString("Label_Algorithm"), algorithm))
        println(("%s: %0" + digest.size * 2 + "x").format(bundle.getString("Label_Checksum"), BigInteger(1, digest)))
        return 0
    }
}

fun main(args: Array<String>)  {
    // first phase: configure locale
    CommandLine(InitLocale()).parseArgs(*args)

    // second phase: parse all args (ignoring --locale) and run the app
    exitProcess(CommandLine(LocaleControl()).execute(*args))
}
