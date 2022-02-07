package picocli.examples

@Grab('info.picocli:picocli-groovy:4.5.1')
//@GrabExclude('org.codehaus.groovy:groovy-all')
import groovy.transform.Field
import java.security.MessageDigest
import static picocli.CommandLine.*

@picocli.groovy.PicocliScript
@Command(name = 'checksum', mixinStandardHelpOptions = true, version = 'checksum 4.0',
        description = 'Print a checksum of each specified FILE, using the specified MessageDigest algorithm.')

@Parameters(arity = '1', paramLabel = 'FILE', description = 'The file(s) whose checksum to calculate.')
@Field private File[] files

@Option(names = ['-a', '--algorithm'], description = ['MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,',
        '  or any other MessageDigest algorithm.'])
@Field private String algorithm = 'SHA-1'

files.each {
    println MessageDigest.getInstance(algorithm).digest(it.bytes).encodeHex().toString() + "\t" + it
}
