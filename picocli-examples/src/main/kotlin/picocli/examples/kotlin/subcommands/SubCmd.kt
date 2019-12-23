package picocli.examples.kotlin.subcommands

import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec

@Command(name = "sub", mixinStandardHelpOptions = true,
        description = ["I'm a subcommand. Prints help if count=7."])
class SubCmd : Runnable {

    @Option(names = ["-c", "--count"], paramLabel = "COUNT",
            description = ["the count"])
    private var count: Int = 0

    @Spec
    var spec: CommandSpec? = null

    override fun run() {
        println("I'm a subcommand")
        for (i in 0 until count) {
            println("hello world $i...")
        }
        if (count == 7) {
            spec?.commandLine()?.usage(System.out)
        }
    }
}
