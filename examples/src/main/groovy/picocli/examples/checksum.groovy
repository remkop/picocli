package picocli.examples

@Grab('info.picocli:picocli:2.0.1')
@GrabExclude('org.codehaus.groovy:groovy-all')
@picocli.groovy.PicocliScript
import groovy.transform.Field

import java.nio.file.Files
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
    byte[] fileContents = Files.readAllBytes(it.toPath())
    byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents)
    println javax.xml.bind.DatatypeConverter.printHexBinary(digest) + "\t" + it
}
