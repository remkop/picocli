package picocli.examples.kotlin

import picocli.CommandLine
import picocli.CommandLine.*


@Command(name = "MyApp", version = ["Kotlin picocli demo v1.0"],
        mixinStandardHelpOptions = true,
        description = ["@|bold Kotlin|@ @|underline picocli|@ example"])
class MyApp : Runnable {

    @Option(names = ["-c", "--count"], paramLabel = "COUNT",
            description = ["the count"])
    private var count: Int = 0

    override fun run() {
        for (i in 0 until count) {
            println("hello world $i...")
        }
    }
//    companion object {
//        @JvmStatic fun main(args: Array<String>) {
//            CommandLine.run(MyApp(), *args)
//        }
//    }
}
fun main(args: Array<String>) = CommandLine.run(MyApp(), *args)