package picocli.examples.stdin;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This example reads a file, or from standard input, and prints the first line.
 *
 * It follows the UNIX convention that tools should read from STDIN (standard input)
 * when the end user specifies the `-` character instead of a file name.
 *
 * See POSIX Utility Syntax Guidelines, Guideline 13:
 * https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html#tag_12_02
 */
@Command(name = "firstline")
public class PrintFirstLine implements Callable<Integer> {

    @Parameters(arity = "1..*")
    List<File> files;

    @Option(names = "--charset", description = "Charset of the file (or STDIN) to read. Default: ${DEFAULT_VALUE}")
    Charset charset = Charset.defaultCharset();

    public Integer call() throws Exception {
        for (File file : files) {
            printFirstLine(file);
        }
        return 0;
    }

    private void printFirstLine(File file) throws IOException {
        try (BufferedReader reader = createReader(file)) {
            String line = reader.readLine();
            System.out.println(line);
        }
    }

    private BufferedReader createReader(File file) throws IOException {
        InputStream in = "-".equals(file.toString())
            ? System.in
            : Files.newInputStream(file.toPath());
        return new BufferedReader(new InputStreamReader(in, charset));
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new PrintFirstLine()).execute(args);
        //System.exit(exitCode); // prevents this method from being testable...
    }
}
