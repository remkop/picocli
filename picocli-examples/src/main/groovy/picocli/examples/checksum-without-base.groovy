package picocli.examples

@Grab('info.picocli:picocli:2.0.1')
@GrabExclude('org.codehaus.groovy:groovy-all')
import java.security.MessageDigest
import picocli.CommandLine
import static picocli.CommandLine.*

class Checksum {
    @Parameters(arity = "1", paramLabel = "FILE", description = "The file(s) whose checksum to calculate.")
    File[] files

    @Option(names = ["-a", "--algorithm"], description = ["MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,",
            "  or any other MessageDigest algorithm."])
    String algorithm = "MD5"

    @Option(names = ["-h", "--help"], usageHelp = true, description = "Show this help message and exit.")
    boolean helpRequested
}
Checksum checksum = new Checksum()
CommandLine commandLine = new CommandLine(checksum)
try {
    commandLine.parse(args)
    if (commandLine.usageHelpRequested) {
        commandLine.usage(System.out)
    } else {
        checksum.files.each {
            byte[] digest = MessageDigest.getInstance(checksum.algorithm).digest(it.bytes)
            println digest.encodeHex().toString() + "\t" + it
        }
    }
} catch (ParameterException ex) {
    println ex.message
    commandLine.usage(System.out)
}
