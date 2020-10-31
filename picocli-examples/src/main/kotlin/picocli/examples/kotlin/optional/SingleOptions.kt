package picocli.examples.kotlin.optional

import picocli.CommandLine
import picocli.CommandLine.Option
import java.lang.Boolean
import java.util.*

class SingleOptions {
    @Option(names = ["-x"])
    lateinit var x: Optional<Int>

    @Option(names = ["-y"])
    var y: Optional<Int> = Optional.empty()

    @Option(names = ["-z", "--long"], negatable = true)
    var z = Optional.of(Boolean.FALSE)

    @CommandLine.Parameters(arity = "0..*")
    var positional = Optional.empty<String>()

    @Option(names = ["-D"], mapFallbackValue = "_NULL_")
    lateinit var map: Map<String, Optional<Int>>
}
