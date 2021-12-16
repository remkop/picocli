package picocli.examples.kotlin

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand
import java.util.Locale
import kotlin.system.exitProcess

@Command(name = "ISOCodeResolve", mixinStandardHelpOptions = true, version = ["1.0"], subcommands = [ HelpCommand::class ],
    description = ["Resolve ISO country codes (ISO-3166-1) or language codes (ISO 639-1 or -2)"])
class SubCmdsViaMethods : Runnable  {
    @CommandLine.Spec
    val spec: CommandLine.Model.CommandSpec? = null

    @Command(description = ["Resolve ISO country code (ISO-3166-1, Alpha-2 code)"])
    fun country( @CommandLine.Parameters( arity = "1..*n", paramLabel = "<country code>",
        description = ["country code(s) to be resolved"] ) vararg countryCodes : String)
    {
        for (code in countryCodes) {
            println("${code.uppercase()}: " + Locale("", code).displayCountry)
        }
    }

    @Command(description = ["Resolve ISO language code (ISO 639-1 or -2, two/three letters)"])
    fun language( @CommandLine.Parameters( arity = "1..*n", paramLabel = "<language code>",
        description = ["language code(s) to be resolved"] ) vararg languageCodes : String)
    {
        for (code in languageCodes) {
            println("${code.uppercase()}: " + Locale(code).displayLanguage)
        }
    }

    override fun run() = throw CommandLine.ParameterException(spec?.commandLine(), "Specify a subcommand")

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine(SubCmdsViaMethods()).execute(*args)
        }
    }
}
// NOTE: below is an alternative to defining a @JvmStatic main function in a companion object:
// fun main(args: Array<String>) : Unit = exitProcess(CommandLine(SubCmdsViaMethods()).execute(*args))
