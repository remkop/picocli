package picocli.examples.atfile;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This example shows how to use a custom "help" option to generate {@literal @}files (argument files)
 * when requested by the user.
 *
 * See https://github.com/remkop/picocli/issues/1163
 */
@Command(name = "myapp", version = "myapp 1.0", mixinStandardHelpOptions = true)
public class AtFileGenerator implements Callable<Integer> {
    private static final String AT_FILE_OPTION_NAME = "--generate-at-file";
    @Spec
    CommandSpec spec;

    @Option(names = "-x")
    int x;

    @Option(names = "--required", required = true)
    int value;

    @Parameters
    String[] other;

    @SuppressWarnings("deprecation") // @Option(help) is deprecated but is useful for custom help options
    @Option(names = AT_FILE_OPTION_NAME,
            help = true, // don't validate required args if --generate-at-file is specified
            description = "Specify this option to generate an @file")
    File atFile;

    @Override
    public Integer call() throws IOException {
        if (atFile != null) {
            tryWriteAtFile();
            return 0;
        }
        // other business logic here...
        System.out.printf("Args were: %s%n", spec.commandLine().getParseResult().expandedArgs());
        return 0;
    }

    private void tryWriteAtFile() throws IOException {
        if (!atFile.exists() || (atFile.exists() && confirmOverwriteOrExit())) {
            writeAtFile(argsWithoutGenerateAtFileOption());
        }
    }

    private boolean confirmOverwriteOrExit() {
        while (System.console() != null) { // overwrite unconditionally if no interactive console
            String line = System.console().readLine("%s exists. Overwrite? (y/n)", atFile);
            if (line == null || "n".equalsIgnoreCase(line)) {
                System.err.println("Aborted, file was not modified.");
                System.exit(3);
            }
            if ("y".equalsIgnoreCase(line)) {
                return true;
            }
        }
        return true;
    }

    private List<String> argsWithoutGenerateAtFileOption() {
        List<String> args = spec.commandLine().getParseResult().originalArgs();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            if (AT_FILE_OPTION_NAME.equals(args.get(i))) { // ignoring abbreviations and case-insensitive parsing mode...
                ++i; // skip the file name
            } else if (!args.get(i).startsWith(AT_FILE_OPTION_NAME + spec.parser().separator())) {
                result.add(args.get(i));
            }
        }
        return result;
    }

    private void writeAtFile(List<String> args) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(atFile))) {
            pw.printf("# @%s argument file generated for %s on %s%n", atFile, spec.qualifiedName(), new Date());
            for (String arg : args) {
                pw.println(quoteAndEscapeBackslashes(arg));
            }
        }
    }

    private String quoteAndEscapeBackslashes(String original) {
        String result = original;
        boolean needsQuotes = result.startsWith("#");
        int c;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length(); i += Character.charCount(c)) {
            c = result.codePointAt(i);
            if (Character.isWhitespace(c)) {
                needsQuotes = true;
            }
            if (c == '\\') {
                sb.append('\\'); // escape any backslashes
            }
            sb.appendCodePoint(c);
        }
        if (needsQuotes) {
            sb.insert(0, '\"').append('\"'); // quote the result
            result = sb.toString();
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            System.exit(new CommandLine(new AtFileGenerator()).execute(args));
        } else {
            test();
        }
    }

    static void test() throws IOException {
        String[] args;
        File atFile = File.createTempFile("picocliAtFile", ".txt");
        args = new String[] {
                "#other-param", "param\t with\\spaces and tabs", "-x=3", //--required omitted
                "--generate-at-file=" + atFile
        };
        int exitCode = new CommandLine(new AtFileGenerator()).execute(args);
        Files.readAllLines(atFile.toPath()).forEach(System.out::println);
    }
}
