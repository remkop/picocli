package picocli.examples.kotlin.factory

import picocli.CommandLine.defaultFactory
import picocli.CommandLine.IFactory

class MyCommandFactory: IFactory {
    override fun <K : Any?> create(cls: Class<K>?): K {
        println(cls)
        if (cls != null) {
            println("is cls an Example?")
            if (cls == Example::class.java) {
                println("Give away rabbit!")
                @Suppress("UNCHECKED_CAST")
                return Example("Rabbit") as K
            } else {
                println("NO")
            }
        }
        return defaultFactory().create(cls)
    }
}
