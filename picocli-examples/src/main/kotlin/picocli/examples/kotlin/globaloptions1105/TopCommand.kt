package picocli.examples.kotlin.globaloptions1105

import picocli.CommandLine
import picocli.CommandLine.*
import java.util.*
import java.util.concurrent.Callable
import java.util.logging.Level

/**
 * This class is not an example (yet)
 * but is work in progress to resolve this issue:
 * https://github.com/remkop/picocli/issues/1105
 */
@Command(mixinStandardHelpOptions = true)
abstract class Base: Callable<Any> {

    @Option(names = ["--logging-level"],
            completionCandidates = LoggingLevelConverter.LoggingLevels::class,
            converter = [LoggingLevelConverter::class])
    var loggingLevel: Level = Level.INFO

    @Option(names = ["-v", "--verbose", "--log-to-console"])
    var verbose: Boolean = false

    open fun initLogging() {
        println("$this is initializing logging with verbose=$verbose and loggingLevel=$loggingLevel")
    }

    override fun call() {
        println("Hello from $this; verbose=$verbose and loggingLevel=$loggingLevel")
    }
}

abstract class WrapperBase: Base() {

    // These will be programmatically added here
    val subCommands: Nothing = TODO()


    override fun call() {
        println("Hello from $this")
    }
}

class SubCommand: WrapperBase() {
    // More options specific to registration
}

// This is the top command
class TopCommand: WrapperBase() {

    @Mixin
    val cmdLineOptions: Nothing = TODO()

    // This is where the subcommands are initialised and added to subCommands
    val mySubCommand = SubCommand()

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine(TopCommand()).execute(*args)
        }
    }
}

class LoggingLevelConverter: ITypeConverter<Level> {
    object LoggingLevels: Iterable<String> {
        override fun iterator(): Iterator<String> {
            return Arrays.asList(Level.ALL.name,
                    Level.CONFIG.name,
                    Level.FINE.name,
                    Level.FINER.name,
                    Level.FINEST.name,
                    Level.INFO.name,
                    Level.OFF.name,
                    Level.SEVERE.name,
                    Level.WARNING.name).iterator()
        }
    }

    override fun convert(value: String?): Level {
        return Level.parse(value)
    }
}
