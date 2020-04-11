package picocli.examples.typeconverter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "myapp", mixinStandardHelpOptions = true,
        description = "Demonstrates interactive converter")
public class InteractiveConverterDemo implements Runnable {

    @Option(names = {"-c", "--config-file"},
            required = true, // we make it a required option in this example
            description = "Configuration file location.",
            converter = InteractiveConverter.class)
    Path configFile;

    @Override
    public void run() {
        System.out.printf("Found existing config file at: %s%n", configFile);
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new InteractiveConverterDemo()).execute(args));
    }

    /**
     * Interactive (and a bit demanding) path converter:    :-)
     * If the user supplied a non-existing path, this converter
     * will keep asking for an existing path, until the user either
     * supplies it, or we get end-of-stream (user pressed CTRL-D),
     * at which point we exit the application with exit code 2 (usage).
     */
    static class InteractiveConverter implements ITypeConverter<Path> {

        @Override
        public Path convert(String value) throws Exception {
            Path result = Paths.get(value);
            while (!Files.exists(result)) {
                // check if we *can* interact (not running in script, or streams are redirected)
                if (System.console() == null) {
                    // picocli will turn this into a user-facing error message
                    throw new FileNotFoundException("Not found: " + value);
                }
                String reply = System.console().readLine("Provide path to existing config file: ");
                if (reply == null) { // user entered Ctrl-D
                    System.exit(CommandLine.ExitCode.USAGE); // ... so we quit in anger! ;-)
                }
                result = Paths.get(reply);
            }
            return result;
        }
    }
}
