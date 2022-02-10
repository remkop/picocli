package picocli.examples

@Grab('info.picocli:picocli-groovy:4.1.1')
@GrabExclude('org.codehaus.groovy:groovy-all')
@Command(name = "checksum", mixinStandardHelpOptions = true, version = 'checksum 4.0',
        header = [
        $/@|bold,green    ___                            ___ _           _                  |@/$,
        $/@|bold,green   / __|_ _ ___  _____ ___  _     / __| |_  ___ __| |__ ____  _ _ __  |@/$,
        $/@|bold,green  | (_ | '_/ _ \/ _ \ V / || |   | (__| ' \/ -_) _| / /(_-< || | '  \ |@/$,
        $/@|bold,green   \___|_| \___/\___/\_/ \_, |    \___|_||_\___\__|_\_\/__/\_,_|_|_|_||@/$,
        $/@|bold,green                         |__/                                         |@/$
        ],
        description = "Print a checksum of each specified FILE, using the specified MessageDigest algorithm.",
        showDefaultValues = true,
        footerHeading = "%nFor more details, see:%n",
        footer = ["[1] https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html",
                "ASCII Art thanks to http://patorjk.com/software/taag/"]
)
@picocli.groovy.PicocliScript
import groovy.transform.Field
import java.security.MessageDigest
import static picocli.CommandLine.*

@Parameters(arity = "1", paramLabel = "FILE", description = "The file(s) whose checksum to calculate.")
@Field private File[] files

@Option(names = ["-a", "--algorithm"], description = ["MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512, or",
        "  any other MessageDigest algorithm. See [1] for more details."])
@Field private String algorithm = "SHA-1"

files.each {
    println MessageDigest.getInstance(algorithm).digest(it.bytes).encodeHex().toString() + "\t" + it
}
