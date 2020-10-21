package picocli.groovy

import groovy.transform.Field
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = 'sub', description = 'my description')
@PicocliScript2 PicocliBaseScript2Subclass me

@Option(names = ["-h", "--help"], usageHelp = true)
@Field boolean usageHelpRequested = false

println getClass().getName()
println me.getClass().getName()
println getClass().getSuperclass().getName()
println getClass().getSuperclass().getSuperclass().getName()

hi()