package picocli.examples

@Grab('info.picocli:picocli-groovy:4.1.1')
@Command(name = "myCommand",
        mixinStandardHelpOptions = true,
        description = "@|bold Groovy script|@ @|underline picocli|@ example")
@picocli.groovy.PicocliScript
import groovy.transform.Field
import static picocli.CommandLine.*

@Option(names = ["-c", "--count"], description = "number of repetitions")
@Field int count = 1;

//if (helpRequested) { // not necessary: PicocliBaseScript takes care of this
//    CommandLine.usage(this, System.out); return 0;
//}
count.times {
    println "hi"
}
// the CommandLine that parsed the args is available as a property
assert this.commandLine.commandName == "myCommand"
