package picocli.examples.kotlin.globaloptions1105

import picocli.CommandLine
import picocli.CommandLine.*
import java.lang.Exception
import java.util.concurrent.Callable
import java.util.logging.Level
import kotlin.system.exitProcess

/**
 * See https://github.com/remkop/picocli/issues/1105
 */
@Command(mixinStandardHelpOptions = true)
abstract class Base(val alias: String, val description: String): Callable<Any> {

    lateinit var args: Array<String>

    override fun call() {
        println("Hello from $this") // verbose and logLevel values not accessible from here
    }
}

abstract class WrapperBase(alias: String, description: String): Base(alias, description) {

    // These will be programmatically added here
    val cmd by lazy {
        CommandLine(this).apply {
            commandSpec.name(alias)
            commandSpec.usageMessage().description(description)
            subCommands.forEach {
                val subCommand = CommandLine(it)
                it.args = args
                subCommand.commandSpec.usageMessage().description(it.description)
                commandSpec.addSubcommand(it.alias, subCommand)
            }
        }
    }

    override fun call() {
        println("Hello from $this") // verbose and logLevel values not accessible from here
    }

    val subCommands: Set<Base> by lazy {
        additionalSubCommands()
    }

    protected open fun additionalSubCommands(): Set<Base> = emptySet()
}

class SubCommand: WrapperBase("sub-command", "does some sub-command work")

class TopCommand: WrapperBase("top-command", "does top-command work") {

    private val subCommand by lazy { SubCommand() }

    override fun additionalSubCommands() = setOf(subCommand)

    @Option(names = ["-v", "--verbose", "--log-to-console"], scope = ScopeType.INHERIT,
            description = ["If set, prints logging to the console as well as to a file."]
    )
    var verbose: Boolean = false

    @Option(names = ["--logging-level"], scope = ScopeType.INHERIT,
            completionCandidates = LoggingLevelConverter.LoggingLevels::class,
            description = ["Enable logging at this level and higher. Possible values: \${COMPLETION-CANDIDATES}"],
            converter = [LoggingLevelConverter::class]
    )
    var loggingLevel: Level = Level.INFO

    fun initLogging() {
        println("$this is initializing logging with verbose=$verbose and loggingLevel=$loggingLevel")
    }

    override fun call() {
        super.call()
    }
}

fun WrapperBase.start(args: Array<String>) {

    this.args = args

    val exceptionHandler = IExecutionExceptionHandler { ex: Exception, _, parseResult: ParseResult ->
        val throwable = ex.cause ?: ex
        val top: TopCommand = parseResult.commandSpec().root().userObject() as TopCommand
        if (top.verbose) {
            throwable.printStackTrace()
        }
        ExitCode.SOFTWARE
    }

    val executionStrategy = IExecutionStrategy { parseResult: ParseResult ->
        val top: TopCommand = parseResult.commandSpec().root().userObject() as TopCommand
        top.initLogging()
        RunLast().execute(parseResult)
    }

    cmd.executionExceptionHandler = exceptionHandler // only print stacktraces if verbose requested by users
    cmd.executionStrategy = executionStrategy // init logging before invoking the business logic
    exitProcess(cmd.execute(*args))
}

fun main(args: Array<String>) {
    val cli = TopCommand()
    cli.start(arrayOf("--logging-level=FINEST", "--log-to-console", "sub-command"))
}

class LoggingLevelConverter: ITypeConverter<Level> {
    object LoggingLevels: Iterable<String> {
        override fun iterator(): Iterator<String> {
            return listOf(Level.ALL.name,
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
