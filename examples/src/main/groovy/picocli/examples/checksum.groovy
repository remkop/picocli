package picocli.examples

@Grab('info.picocli:picocli:2.0.2')
@GrabExclude('org.codehaus.groovy:groovy-all')
@picocli.groovy.PicocliScript
import groovy.transform.Field
import java.security.MessageDigest
import static picocli.CommandLine.*

@Parameters(arity = "1", paramLabel = "FILE", description = "The file(s) whose checksum to calculate.")
@Field private File[] files

@Option(names = ["-a", "--algorithm"], description = ["MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,",
        "  or any other MessageDigest algorithm."])
@Field private String algorithm = "MD5"

@Option(names = ["-h", "--help"], usageHelp = true, description = "Show this help message and exit.")
@Field private boolean helpRequested

files.each {
    println MessageDigest.getInstance(algorithm).digest(it.bytes).encodeHex().toString() + "\t" + it
}
