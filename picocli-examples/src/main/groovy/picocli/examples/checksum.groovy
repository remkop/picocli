package picocli.examples

@Grab('info.picocli:picocli-groovy:4.5.1')
import picocli.CommandLine
import static picocli.CommandLine.*

import java.security.MessageDigest
import java.util.concurrent.Callable

@Command(name = 'checksum', mixinStandardHelpOptions = true, version = 'checksum 4.0',
        description = 'Print a checksum of each specified FILE, using the specified MessageDigest algorithm.')
class Checksum implements Callable<Integer> {

    @Parameters(arity = '1', paramLabel = 'FILE', description = 'The file(s) whose checksum to calculate.')
    File[] files

    @Option(names = ['-a', '--algorithm'], description = ['MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512,',
            '  or any other MessageDigest algorithm.'])
    String algorithm = 'SHA-1'

    Integer call() throws Exception {
        files.each {
            byte[] digest = MessageDigest.getInstance(algorithm).digest(it.bytes)
            println digest.encodeHex().toString() + "\t" + it
        }
        0
    }

    static void main(String[] args) {
        System.exit(new CommandLine(new Checksum()).execute(args))
    }
}
