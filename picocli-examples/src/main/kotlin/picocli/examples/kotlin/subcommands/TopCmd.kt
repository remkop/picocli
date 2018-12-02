package picocli.examples.kotlin.subcommands

import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import picocli.examples.kotlin.MyApp


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
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine.run(TopCmd(), *args)
        }
    }
}
