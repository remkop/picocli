package picocli.examples.cjk;

import picocli.AutoComplete;
import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.concurrent.Callable;

@Command(name = "checksum", mixinStandardHelpOptions = true, version = "checksum 4.0",
  subcommands = GenerateCompletion.class, defaultValueProvider = PropertiesDefaultProvider.class,
  description = "ファイルのチェックサム（デフォルトはSHA-1）を標準出力に表示する。",
       header = { "@|cyan " +
               "     _    _ _   _  ___ \n" +
               "  _ | |_ | | | | |/ __|\n" +
               " | || | || | |_| | (_ |\n" +
               "  \\__/ \\__/ \\___/ \\___|\n" +
               "|@"})
class CheckSum implements Callable<Integer> {

    @Parameters(index = "0", description = "このファイルのチェックサムを計算する")
    private File file;

    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
    private String algorithm = "SHA-1";

    public static void main(String... args) {
        System.exit(new CommandLine(new CheckSum()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        byte[] fileContents = Files.readAllBytes(file.toPath());
        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
        System.out.printf("%0" + (digest.length * 2) + "x%n", new BigInteger(1, digest));
        return 0;
    }
}

