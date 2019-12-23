package picocli.examples.kotlin.subcommands

import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Command(name = "top", version = ["Kotlin picocli demo v3.8.1"],
        mixinStandardHelpOptions = true,
        description = ["@|bold Kotlin|@ @|underline picocli|@ example"],
        subcommands = [SubCmd::class])
class TopCmd : Runnable {
    @Spec
    var spec: CommandSpec? = null

    override fun run() {
        throw ParameterException(spec?.commandLine(), "Specify a subcommand")
    }
}
fun main(args: Array<String>) = System.exit(CommandLine(TopCmd()).execute(*args))
