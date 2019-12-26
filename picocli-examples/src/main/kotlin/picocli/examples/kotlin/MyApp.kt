package picocli.examples.kotlin

import picocli.CommandLine
import picocli.CommandLine.*
import java.util.concurrent.Callable

@Command(name = "MyApp", version = ["Kotlin picocli demo v4.0"],
        mixinStandardHelpOptions = true,
        description = ["@|bold Kotlin|@ @|underline picocli|@ example"],
        subcommands = [picocli.CommandLine.HelpCommand::class])
class MyApp : Callable<Int> {

    @Option(names = ["-c", "--count"], paramLabel = "COUNT",
            description = ["the count"])
    private var count: Int = 0

    override fun call(): Int {
        for (i in 0 until count) {
            println("hello world $i...")
        }
        return 123
    }
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine.run(MyApp(), *args)
        }
    }
}
// NOTE: below is an alternative to defining a @JvmStatic main function in a companion object:
//fun main(args: Array<String>) = System.exit(CommandLine(MyApp()).execute(*args))
