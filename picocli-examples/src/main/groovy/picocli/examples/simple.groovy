package picocli.examples

@Grab('info.picocli:picocli-groovy:4.1.1')
@GrabExclude('org.codehaus.groovy:groovy-all')
@picocli.groovy.PicocliScript
@picocli.CommandLine.Command
import picocli.CommandLine

println "Groovy version ${GroovySystem.version}"
println "Picocli version $CommandLine.VERSION"
println "picocli location: ${CommandLine.class.getResource("/picocli/CommandLine.class")}"
