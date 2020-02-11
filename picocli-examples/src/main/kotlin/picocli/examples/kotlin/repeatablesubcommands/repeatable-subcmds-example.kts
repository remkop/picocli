#!/usr/bin/env kscript

//DEPS info.picocli:picocli:4.2.0

//@file:DependsOn("info.picocli:picocli:4.2.0")
//import DependsOn // this (and the line above) requires holgerbrandl/kscript


import java.util.concurrent.Callable

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name="MainCmd",
        subcommandsRepeatable = true,
        subcommands = [Container::class])
class MainCmd : Callable<Int> {
    @Option(names=["--help", "-h"], usageHelp=true)
    var helpRequested: Boolean = false

    override fun call(): Int {
        println(this)
        return 0
    }
}

@Command(name="--with-container")
class Container : Callable<Int> {
    @Parameters(arity="1")
    lateinit var path: String

    @Option(names=["--dataset", "-d"], arity="*")
    var datasets: Array<String>? = null

    @Option(names=["--help", "-h"], usageHelp=true)
    var helpRequested: Boolean = false

    override fun call(): Int {
        println(this)
        return 0
    }

    override fun toString() = ContainerData(path, datasets?.toList()).toString()
}

data class ContainerData(val path: String, val datasets: List<String>?)

val mainCmd = MainCmd()
val cl = CommandLine(mainCmd)
val exitCode = cl.execute(*args)


// example invocation:
// ./repeatable-subcmds-example.kts --with-container abc --dataset a --with-container xyz --dataset x
