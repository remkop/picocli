package picocli.examples.kotlin.parameterconsumer

import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.ArgSpec
import picocli.CommandLine.Model.CommandSpec
import java.util.*

class App : Runnable {
    @Option(names = ["-x"], parameterConsumer = CustomConsumer::class)
    var x: String? = null

    @Option(names = ["-y"])
    var y: String? = null

    @Command
    fun mySubcommand() {
    }

    internal class CustomConsumer : IParameterConsumer {
        override fun consumeParameters(args: Stack<String>, argSpec: ArgSpec, cmdSpec: CommandSpec) {
            if (args.isEmpty()) {
                throw ParameterException(cmdSpec.commandLine(),
                    "Error: option '-x' requires a parameter"
                )
            }
            val arg = args.pop()
            argSpec.setValue(arg)
        }
    }

    override fun run() {
        System.out.printf("x='%s', y='%s'%n", x, y)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(App()).execute(*args)
        }
    }
}
