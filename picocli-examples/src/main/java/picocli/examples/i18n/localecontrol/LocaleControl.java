package picocli.examples.i18n.localecontrol;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Unmatched;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

class InitLocale {
    @Option(names = { "-l", "--locale" }, description = "locale used for message texts (phase 1)")
    void setLocale(String locale) {
        Locale.setDefault(new Locale(locale));
    }

    @Unmatched
    List<String> remainder; // ignore any other parameters and options in the first parsing phase
}

@Command(name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0",
        resourceBundle = "picocli.examples.i18n.localecontrol.bundle",
        sortOptions = false)
public class LocaleControl implements Callable {

    ResourceBundle bundle = ResourceBundle.getBundle("picocli.examples.i18n.localecontrol.bundle");

    @Option(names = { "-l", "--locale" }, descriptionKey = "Locale", paramLabel = "<locale>", order = 1)
    private String ignored;

    @Parameters(index = "0", descriptionKey = "File")
    private File file;

    @Option(names = {"-a", "--algorithm"}, descriptionKey = "Algorithms", order = 2)
    private String algorithm = "SHA-1";

    @Override
    public Integer call() throws Exception {
        byte[] fileContents = Files.readAllBytes(file.toPath());
        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
        System.out.printf("%s: %s%n", bundle.getString("Label_File"), file);
        System.out.printf("%s: %s%n", bundle.getString("Label_Algorithm"), algorithm);
        System.out.printf("%s: %0" + (digest.length*2) + "x", bundle.getString("Label_Checksum"), new BigInteger(1, digest));
        return 0;
    }

    public static void main(String... args) {
        // first phase: configure locale
        new CommandLine(new InitLocale()).parseArgs(args);

        // second phase: parse all args (ignoring --locale) and run the app
        int exitCode = new CommandLine(new LocaleControl()).execute(args);
        System.exit(exitCode);
    }
}
