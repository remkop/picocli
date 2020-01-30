package picocli.examples.kotlin.factory

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import kotlin.system.exitProcess

@Command(
        name = "TestMain",
        version = ["TestMain-0.1.0"],
        mixinStandardHelpOptions = true, // add --help and --version options
        description = ["@|bold blah |@"],
        showDefaultValues = true
)
class Example(val initial: String) : Runnable {
    @Option(
            names = ["--param"],
            required = false,
            description = ["..."]
    )
    protected var param = "blah"
    override fun run() {
        println("Did something: $param $initial")
    }
}

fun main(args: Array<String>) { exitProcess(CommandLine(Example::class.java, MyCommandFactory()).execute(*args)) }
