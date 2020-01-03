package picocli.examples.kotlin.examples.kotlin.i18n

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.util.*
import kotlin.system.exitProcess

@Command(name = "ASCIIArtGenerator", mixinStandardHelpOptions = true,
    resourceBundle = "examples.kotlin.i18n.Messages", version = ["4.1.4"])
class I18NDemo : Runnable {

    @Parameters(paramLabel = "<word1> [<word2>]", arity = "0..*",
                descriptionKey = "words")
    private val words = arrayOf("Hello,", "world!")

    @Option(names = ["-f", "--font"], descriptionKey = "font")
    var font = "standard"

    override fun run() {
        val text = words.joinToString(separator = "+")
        val URL = "http://artii.herokuapp.com/make?text=%s&font=%s".format(text, font)

        try {
            Scanner(java.net.URL(URL).openStream()).use {
                    s -> println(s.useDelimiter("\\A").next())
            }
        } catch (e: Exception) {
            System.err.println("Invalid font or invalid text given!")
            exitProcess(1)
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine(I18NDemo()).execute(*args)
        }
    }
}
// NOTE: below is an alternative to defining a @JvmStatic main function in a companion object:
// fun main(args: Array<String>) : Unit = exitProcess(CommandLine(I18NDemo()).execute(*args))
