package picocli.examples.i18n;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class I18NDemo {

    @Command(name = "ASCIIArtGenerator", mixinStandardHelpOptions = true,
             resourceBundle = "picocli.examples.i18n.Messages", version= "4.1.4")
    static class ASCIIArtCommand implements Runnable {

        @Parameters(paramLabel = "<word1> [<word2>]", arity = "0..*",
                    descriptionKey = "words")
        private String[] words = { "Hello,", "world!"};

        @Option(names = { "-f", "--font" }, descriptionKey = "font")
        String font = "standard";

        public void run() {
            String text = String.join("+", words);
            String URL = String.format("http://artii.herokuapp.com/make?text=%s&font=%s", text, font);

            try (java.util.Scanner s = new java.util.Scanner(new java.net.URL(URL).openStream())) {
                System.out.println(s.useDelimiter("\\A").next());
            }
            catch (Exception e) {
                System.err.println("Invalid font or invalid text given!");
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ASCIIArtCommand());
        cmd.execute(args);
    }
}