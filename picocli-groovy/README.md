<p align="center">
<a href="https://groovy-lang.org"><img src="https://picocli.info/images/groovy-logo.png" height="150px" alt="Groovy programming language"></a>
<a href="https://picocli.info"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p></a>


# Picocli in Groovy Scripts

This module contains classes to allow the use of picocli annotations in Groovy scripts.

This module was introduced in picocli 4.0; in previous versions these classes were included in the main `picocli-$version` artifact.

## Example

```groovy
@Grab('info.picocli:picocli-groovy:4.7.5')
@Command(description = "Print a checksum of each specified FILE.",
        mixinStandardHelpOptions = true,
        version = 'checksum v1.2.3',
        showDefaultValues = true)
@picocli.groovy.PicocliScript
import groovy.transform.Field
import java.security.MessageDigest
import static picocli.CommandLine.*

@Parameters(arity="1", paramLabel="FILE", description="The file(s) whose checksum to calculate.")
@Field private File[] files

@Option(names = ["-a", "--algorithm"], description = [
        "MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512, or",
        "  any other MessageDigest algorithm."])
@Field private String algorithm = "SHA-1"

files.each {
  println MessageDigest.getInstance(algorithm).digest(it.bytes).encodeHex().toString() + "\t" + it
}
```

See the [Groovy Scripts on Steroids](https://picocli.info/picocli-2.0-groovy-scripts-on-steroids.html) article for more details.
