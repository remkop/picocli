package picocli.examples.kotlin.sysprops
import picocli.CommandLine
import picocli.CommandLine.Option

internal class SystemPropertiesDemo {
    @Option(names = ["-D"])
    fun setProperty(props: Map<String, String?>) {
        props.forEach { (k: String, v: String?) -> System.setProperty(k, v ?: "") }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            System.getProperties().list(System.out)
            CommandLine(SystemPropertiesDemo()).parseArgs(*args)

            println("----------------------------------------------------")
            System.getProperties().list(System.out)
        }
    }
}